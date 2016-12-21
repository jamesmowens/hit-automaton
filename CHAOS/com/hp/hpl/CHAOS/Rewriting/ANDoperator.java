package com.hp.hpl.CHAOS.Rewriting;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamOperator.*;
import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;
import com.hp.hpl.CHAOS.StreamData.StreamTupleCreator;

public class ANDoperator extends SingleInputStreamOperator {

	static int query_ID = 0;

	// collect results after run
	ArrayList<String> resultCollection = new ArrayList<String>();

	Hashtable<Integer, ArrayList<ArrayList<byte[]>>> resultBuffers = new Hashtable<Integer, ArrayList<ArrayList<byte[]>>>();

	// event types for a single query
	ArrayList<String> stackTypes = new ArrayList<String>();

	// it stores all the submitted queries information
	ArrayList<QueryInfo> queries = new ArrayList<QueryInfo>();
	// QueryInfo[][] orderedQueries;

	private boolean initStatus = false;

	// build concept hierarchy;
	ConceptTree tree = QueryCompiler.createTreeCompany();
	// the cost model
	// CostModel CM = new CostModel(orderedQueries, tree);

	// AIS index is the same as concept encoding
	// for the higher level concept which encoding is an interval, we apply the
	// left boundary as the index
	EventActiveInstanceQueue[] AIS = new EventActiveInstanceQueue[(int) tree
			.getRoot().getRightBound(0).x];

	HierarchicalQueryPlan hqp = new HierarchicalQueryPlan(
			new ArrayList<String>(), tree);

	public ANDoperator(int operatorID, StreamQueue[] input, StreamQueue[] output) {
		super(operatorID, input, output);
	}

	// It parse the query plan. set up the stack types as well
	@Override
	public void classVariableSetup(String key, String value)
			throws InstantiationException, IllegalAccessException,
			InvocationTargetException, SecurityException,
			ClassNotFoundException {

		// ArrayList used to store all event types in a query.
		ArrayList<String> ETypes_AQuery = new ArrayList<String>();

		/*
		 * if (key.equalsIgnoreCase("predicate")) {
		 * 
		 * String received = value; System.out.print(received); }
		 */

		if (key.equalsIgnoreCase("query")) {
			QueryInfo query = new QueryInfo();

			// check the operator Type first
			if (value.contains("SEQ")) {
				query.setOperatorType("SEQ");
			} else if (value.contains("AND")) {
				query.setOperatorType("AND");
			} else if (value.contains("OR")) {
				query.setOperatorType("OR");
			}

			int q_index = value.indexOf("(");
			int q_index_end = value.indexOf(")");

			String trimedValue = (String) value.subSequence(q_index + 1,
					q_index_end);

			int q_index_id = value.indexOf("(", q_index_end);
			int q_index_id_end = value.indexOf(")", q_index_id);

			String idValue = (String) value.subSequence(q_index_id + 1,
					q_index_id_end);

			query.setQueryID(Integer.parseInt(idValue));

			int child_index = value.indexOf("child =", q_index_id_end);

			int parent_index = value.indexOf("parent =", q_index_id_end);

			// if the query has both parent and children queries

			ArrayList<childQueryInfo> children = new ArrayList<childQueryInfo>();

			ArrayList<parentQueryInfo> parents = new ArrayList<parentQueryInfo>();

			String copyValue = value;
			if (child_index > 0) {

				childQueryInfo cinfo = new childQueryInfo();

				value = value.substring(child_index + "child =".length() + 1,
						value.length());

				// ok. the next few lines are not general enough,
				// I am using "|" to represent OR operator.

				int or_index = value.indexOf("|");

				if (or_index >= 0) {
					query.setMixType("OR");
				}

				int lastIndex = value.lastIndexOf(")");

				int c_index_id = value.indexOf("(", child_index);
				int c_index_id_end = value.indexOf(",", c_index_id);

				int c_index_position = c_index_id_end + 1;
				int c_index_position_end = value.indexOf(",", c_index_position);

				int c_index_type = c_index_position_end + 1;
				int c_index_type_end = value.indexOf(")", c_index_type);

				String cidValue = (String) value.subSequence(c_index_id + 2,
						c_index_id_end);
				String cpositionValue = (String) value.subSequence(
						c_index_position, c_index_position_end);
				String ctypeValue = (String) value.subSequence(c_index_type,
						c_index_type_end);

				cinfo.setChildID(Integer.parseInt(cidValue));
				cinfo.setNestedposition(Integer.parseInt(cpositionValue));
				cinfo.setPositiveComponent(Integer.parseInt(ctypeValue));
				children.add(cinfo);

				// System.out.println("children" + cidValue + cpositionValue
				// + ctypeValue);
				while (c_index_type_end < lastIndex) {
					cinfo = new childQueryInfo();

					child_index = c_index_type_end;
					c_index_id = value.indexOf("(", child_index);
					c_index_id_end = value.indexOf(",", c_index_id);

					c_index_position = c_index_id_end + 1;
					c_index_position_end = value.indexOf(",", c_index_position);

					c_index_type = c_index_position_end + 1;
					c_index_type_end = value.indexOf(")", c_index_type);

					cidValue = (String) value.subSequence(c_index_id + 1,
							c_index_id_end);
					cpositionValue = (String) value.subSequence(
							c_index_position, c_index_position_end);
					ctypeValue = (String) value.subSequence(c_index_type,
							c_index_type_end);
					// System.out.println("children" + cidValue + cpositionValue
					// + ctypeValue);

					cinfo.setChildID(Integer.parseInt(cidValue));
					cinfo.setNestedposition(Integer.parseInt(cpositionValue));
					cinfo.setPositiveComponent(Integer.parseInt(ctypeValue));
					children.add(cinfo);

				}

			}

			value = copyValue;
			if (parent_index > 0) {
				parentQueryInfo parentq = new parentQueryInfo();
				if (child_index < 0)
					value = value.substring(parent_index + "parent =".length()
							+ 1, value.length());
				else
					value = value.substring(parent_index + "parent =".length()
							+ 1, child_index - 1);

				int lastIndex = value.lastIndexOf(")");

				int p_index_id = value.indexOf("(", parent_index);
				int p_index_id_end = value.indexOf(",", p_index_id);

				int p_index_position = p_index_id_end + 1;
				int p_index_position_end = value.indexOf(")", p_index_position);

				String pidValue = (String) value.subSequence(p_index_id + 2,
						p_index_id_end);
				String ppositionValue = (String) value.subSequence(
						p_index_position, p_index_position_end);

				parentq.setParentID(Integer.parseInt(pidValue));
				parentq.setInParentPosition(Integer.parseInt(ppositionValue));
				parents.add(parentq);

				// System.out.println("parents" + pidValue + ppositionValue);

				while (p_index_position_end < lastIndex) {
					lastIndex = value.lastIndexOf(")");

					p_index_id = value.indexOf("(", p_index_position_end);
					p_index_id_end = value.indexOf(",", p_index_id);

					p_index_position = p_index_id_end + 1;
					p_index_position_end = value.indexOf(")", p_index_position);

					pidValue = (String) value.subSequence(p_index_id + 1,
							p_index_id_end);
					ppositionValue = (String) value.subSequence(
							p_index_position, p_index_position_end);

					// System.out.println("parents" + pidValue +
					// ppositionValue);

					parentq.setParentID(Integer.parseInt(pidValue));
					parentq.setInParentPosition(Integer
							.parseInt(ppositionValue));
					parents.add(parentq);

				}
			}

			String[] result = trimedValue.split(",");
			for (int x = 0; x < result.length; x++) {
				ETypes_AQuery.add(result[x].toLowerCase());

				// check whether it exists already.
				if (!contains_notsensitive(stackTypes, result[x])) {

					stackTypes.add(result[x].toLowerCase());

				}

			}

			query.setStackTypes(ETypes_AQuery);
			query.setChildren(children);
			query.setParents(parents);

			queries.add(query);

			// ok, now I know more information about a query and I can fill in
			// others.
			// or, do I really need to? I can search queries information.

			// wondering why I need these below
			/*
			 * orderedQueries = orderQueries(); queries.clear(); for (int i = 0;
			 * i < orderedQueries.length; i++) {
			 * queries.add(orderedQueries[i][0]); }
			 */

		}

	}

	protected double[] searchTree(String s) {
		double[] bounds = { -1.0, -1.0 };
		boolean found = false;
		for (int level = tree.getNumLevels() - 1; level > 0; level--) {
			for (int nodeIndex = 0; nodeIndex < tree.getLevel(level)
					.getNumNodes(); nodeIndex++) {
				if (tree.getLevel(level).getNode(nodeIndex).name
						.equalsIgnoreCase(s)) {// string match
					bounds[0] = tree.getLevel(level).getNode(nodeIndex)
							.getLeftBound(0).x;
					bounds[1] = tree.getLevel(level).getNode(nodeIndex)
							.getRightBound(0).x;
					found = true;
					break;
				}

			}
			if (found == true)
				break;

		}
		return bounds;

	}

	protected boolean contains_notsensitive(ArrayList<String> list,
			String astring) {
		boolean contain = false;
		if (list.contains(astring)) {
			contain = true;
		} else {
			for (int i = 0; i < list.size(); i++) {
				contain = list.get(i).equalsIgnoreCase(astring);
				if (contain)
					break;
			}
		}

		return contain;
	}

	public int init(SchemaElement[] schArray) {
		super.init();

		hqp.setTree(tree);
		ArrayList<String> queryTypes = new ArrayList<String>();
		hqp.setqueries(queries);

		// create hierarchical pattern graph
		for (int j = 0; j < this.queries.size(); j++) {
			queryTypes = queries.get(j).getStackTypes();
			hqp.setStackTypes(queryTypes);
			hqp.createHashtable(this.queries.get(j).getQueryID());

		}

		// build event active stacks.
		EventActiveInstanceQueue stack = null;

		ArrayList<String> types = new ArrayList<String>();
		for (int i = 0; i < this.queries.size(); i++) {
			ArrayList<String> typesi = this.queries.get(i).getStackTypes();
			for (int j = 0; j < typesi.size(); j++) {
				String typeI = typesi.get(j);
				if (typeI.startsWith("-")) {
					typeI = typeI.substring(1, typeI.length());
				}
				if (!contains_notsensitive(types, typeI))
					types.add(typeI);
			}
		}

		for (int i = 0; i < types.size(); i++) {
			Hashtable<String, ArrayList<EdgeLabel>> hash2 = hqp
					.getHierarchicalQueryPlan().get(types.get(i));
			int pointerSize = 1; // default value
			if (hash2 != null)// key exists
			{
				Set<String> set2 = hash2.keySet();
				pointerSize = set2.size();

			}
			stack = new EventActiveInstanceQueue(schArray, pointerSize, types
					.get(i));

			double[] bounds = searchTree(types.get(i));

			// add created stack to the index by concept encoding
			if (bounds[0] >= 0)
				AIS[(int) bounds[0]] = stack;
		}

		// CM.setOrderedQueries(orderedQueries);

		// setupExecutionOrderfornaive();

		initStatus = true;
		return 1;
	}

	public QueryInfo getQuery(int queryID) {
		int i = 0;
		for (; i < this.queries.size(); i++) {
			if (this.queries.get(i).getQueryID() == queryID) {
				break;
			}
		}

		return this.queries.get(i);
	}

	public void bufferResult(int queryID, byte[] dest,
			SchemaElement[] schArray_Result, ArrayList<byte[]> ar2,
			boolean reuseresults, SchemaElement[] schArray) {
		boolean withinWindow = false;
		QueryInfo qiInfo = getQuery(queryID);

		if (qiInfo.getOperatorType().equalsIgnoreCase("AND")) {
			if (Utility.windowOptAnd(ar2, schArray)) {
				withinWindow = true;
				Configure.resultNum++;

				System.out.println("====== result======" + queryID);
				System.out.println(StreamAccessor.toString(dest,
						generateResultSchemas(getQuery(queryID).stackTypes
								.size(), schArray)));

				// test start
				byte[] firstTuple = ar2.get(0);

				byte[] secondTuple = ar2.get(1);

				byte[] thirdTuple = ar2.get(2);

				double firsttupleTimestamp = StreamAccessor.getDoubleCol(
						firstTuple, schArray, 1);

				double secondtupleTimestamp = StreamAccessor.getDoubleCol(
						secondTuple, schArray, 1);

				double thirdtupleTimestamp = StreamAccessor.getDoubleCol(
						thirdTuple, schArray, 1);

				if (firsttupleTimestamp >= 34197.793
						&& firsttupleTimestamp <= 34200.887) {
					if (secondtupleTimestamp >= 34197.793
							&& secondtupleTimestamp <= 34200.887) {
						if (thirdtupleTimestamp >= 34197.793
								&& thirdtupleTimestamp <= 34200.887) {
							System.out.println("Really?????????????");
						}
					}
				}

				// test end
				com.hp.hpl.CHAOS.Queue.Utility.enqueueGroup(OutputQueueArray,
						dest);
			}
		}

		// /////////Buffer Results /////////////////////////s

		if (withinWindow && reuseresults) {
			ArrayList<ArrayList<byte[]>> result = this.resultBuffers
					.get(queryID);

			if (result != null) {

				this.resultBuffers.get(queryID).add(ar2);

			} else {
				result = new ArrayList<ArrayList<byte[]>>();
				result.add(ar2);
				this.resultBuffers.put(new Integer(queryID), result);

			}
		}

	}

	/***
	 * 
	 * @param int the number of event types in the final result
	 * @param SchemaElement
	 *            [] the schema of the input event
	 * @return the schema array for results
	 */
	SchemaElement[] generateResultSchemas(int size,
			SchemaElement[] inputschArray) {

		SchemaElement[] schArray_Result = {};
		ArrayList<SchemaElement> schArray_result = new ArrayList<SchemaElement>();

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < inputschArray.length; j++) {
				try {

					schArray_result.add((SchemaElement) inputschArray[j]
							.clone());
				} catch (CloneNotSupportedException e1) {

					e1.printStackTrace();
				}
			}

		}

		// set up offset
		int offsetcheck = 0;
		for (int i = 0; i < schArray_result.size(); i++) {

			schArray_result.get(i).setOffset(offsetcheck);

			offsetcheck += schArray_result.get(i).getLength();
		}

		// convert the arrayList to an array
		schArray_Result = (SchemaElement[]) schArray_result
				.toArray(new SchemaElement[] {});
		return schArray_Result;

	}

	int findStack(String eType) {
		double[] bounds = searchTree(eType);
		boolean found = false;
		// not found exact match
		if (bounds[0] == -1 || AIS[(int) bounds[0]] == null) {

			for (int level = tree.getNumLevels() - 1; level > 0; level--) {
				for (int nodeIndex = 0; nodeIndex < tree.getLevel(level)
						.getNumNodes(); nodeIndex++) {

					if (semanticMatch(
							tree.getLevel(level).getNode(nodeIndex).name, eType)) {
						bounds[0] = tree.getLevel(level).getNode(nodeIndex)
								.getLeftBound(0).x;
						bounds[1] = tree.getLevel(level).getNode(nodeIndex)
								.getRightBound(0).x;
						if (AIS[(int) bounds[0]] != null)
							found = true;
						break;

					}
				}
				if (found == true)
					break;
			}
		}

		return (int) bounds[0];
	}

	Boolean semanticMatch(String s1, String s2) {
		if (s1.equalsIgnoreCase(s2))
			return true;
		else {
			double[] bounds_s1 = {};
			double[] bounds_s2 = {};

			bounds_s1 = searchTree(s1);
			bounds_s2 = searchTree(s2);
			if (bounds_s1[0] < bounds_s2[0] && bounds_s1[1] > bounds_s2[1]) {
				return true;
			} else
				return false;
		}

	}

	int[] firstQueryTypes(int queryID) {
		ArrayList<String> stackTypes = new ArrayList<String>();
		for (int j = 0; j < this.queries.size(); j++) {
			int queryid = this.queries.get(j).getQueryID();
			if (queryid == queryID) {
				stackTypes = this.queries.get(j).getStackTypes();
				break;
			}

		}

		// here, I should not use stackType information. Instead, I should
		// follow the hash tables.

		ArrayList<String> firstTypes = new ArrayList<String>();
		String firstType = stackTypes.get(0);

		// extended to support negation start
		if (firstType.startsWith("-")) {
			firstType = stackTypes.get(1);
		}

		ArrayList<String> allETypes = Utility.getAllstackTypes(this.queries);
		for (int iter = 0; iter < allETypes.size(); iter++) {
			if (Utility.semanticMatch(firstType, allETypes.get(iter), tree)) {
				firstTypes.add(allETypes.get(iter));
			}

		}

		int[] firstIndexs = new int[firstTypes.size()];
		for (int i = 0; i < firstTypes.size(); i++) {
			firstIndexs[i] = findStack(firstTypes.get(i));
		}
		return firstIndexs;
	}

	ArrayList<ArrayList<byte[]>> connectAnd(byte[] currenttuple,
			ArrayList<ArrayList<byte[]>> ear) {
		ArrayList<ArrayList<byte[]>> earray = new ArrayList<ArrayList<byte[]>>();

		for (int i = 0; i < ear.size(); i++) {
			ArrayList<byte[]> array = new ArrayList<byte[]>();
			array = ear.get(i);

			array.add(currenttuple);

			ArrayList<byte[]> array2 = new ArrayList<byte[]>();
			for (int j = 0; j < array.size(); j++) {
				array2.add(array.get(j));
			}
			earray.add(array2);

		}
		return earray;
	}

	ArrayList<ArrayList<byte[]>> sequenceConstructionAnd(int queryID,
			int traverseStackIndex, int triggerStack,
			EventActiveInstanceQueue[] stacks, SchemaElement[] inputschArray) {

		ArrayList<ArrayList<byte[]>> earray = new ArrayList<ArrayList<byte[]>>();
		ArrayList<ArrayList<byte[]>> earray3 = new ArrayList<ArrayList<byte[]>>();
		ArrayList<byte[]> array = new ArrayList<byte[]>();
		byte[][] retPointerArrayTemp = new byte[1][];

		// stackTypes
		ArrayList<String> stackTypes = getQuery(queryID).getStackTypes();

		// get the previous stack index for the given query
		// I can just check the stack type

		int currentTraverseIndex = traverseStackIndex;
		String currentTraverseType = stacks[currentTraverseIndex].stackType;

		int currentTypeIndex = stackTypes.indexOf(currentTraverseType);

		int[] firstStacks = firstQueryTypes(queryID);

		boolean isFirst = false;

		boolean triggerisFirst = false;

		for (int i = 0; i < firstStacks.length; i++) {
			if (firstStacks[i] == traverseStackIndex) {
				isFirst = true;
				break;
			}
		}

		for (int i = 0; i < firstStacks.length; i++) {
			if (firstStacks[i] == triggerStack) {
				triggerisFirst = true;
				break;
			}
		}

		if ((traverseStackIndex == triggerStack) && (isFirst == false)) {

			String previousType = stackTypes.get(currentTypeIndex - 1);

			int previousStackIndex = findStack(previousType);

			earray3 = connectAnd(stacks[traverseStackIndex].eventQueue
					.peekLast(), sequenceConstructionAnd(queryID,
					previousStackIndex, triggerStack, stacks, inputschArray));
			for (int t = 0; t < earray3.size(); t++) {
				earray.add(earray3.get(t));
			}

			return earray;
		}

		if (isFirst) {// ok. bug place, instead of "0", I should say whether it
			// is the first event stack.

			byte[] tuple = stacks[traverseStackIndex].eventQueue.peekLast();
			int index = StreamAccessor.getIndex(tuple);

			// get tuple time stamp
			double tupleTimestamp = StreamAccessor.getDoubleCol(tuple,
					inputschArray, 1);

			if (!triggerisFirst) {
				while (tuple != null) {

					{
						ArrayList<byte[]> array2 = new ArrayList<byte[]>();

						array.add(tuple);

						for (int m3 = 0; m3 < array.size(); m3++) {
							array2.add(array.get(m3));
						}

						earray.add(array2);

						array.clear();

					}

					tuple = stacks[traverseStackIndex]
							.getPreviousByPhysicalIndex(index,
									retPointerArrayTemp);
					index = StreamAccessor.getIndex(tuple);
					tupleTimestamp = StreamAccessor.getDoubleCol(tuple,
							inputschArray, 1);

				}

			} else {

				ArrayList<byte[]> array2 = new ArrayList<byte[]>();

				{
					array.add(tuple);
					array2.add(array.get(0));

					earray.add(array2);

					array.clear();

				}

				index = StreamAccessor.getIndex(tuple);
				tuple = stacks[traverseStackIndex].getPreviousByPhysicalIndex(
						index, retPointerArrayTemp);

			}
			return earray;
		} else {

			String previousType = stackTypes.get(currentTypeIndex - 1);

			int previousStackIndex = findStack(previousType);

			byte[] currenttuple = stacks[traverseStackIndex].eventQueue
					.peekLast();
			int index = StreamAccessor.getIndex(currenttuple); // Medhabi, bug
			// place

			double currentTupleTimestamp = StreamAccessor.getDoubleCol(
					currenttuple, inputschArray, 1);

			while (currenttuple != null) {

				// currenttuple =
				// stacks[traverseStackIndex].eventQueue.get(index);

				{
					earray3 = connectAnd(currenttuple, sequenceConstructionAnd(
							queryID, previousStackIndex, triggerStack, stacks,
							inputschArray));

					for (int t = 0; t < earray3.size(); t++) {
						earray.add(earray3.get(t));

					}
				}

				currenttuple = stacks[traverseStackIndex]
						.getPreviousByPhysicalIndex(index, retPointerArrayTemp);

				index = StreamAccessor.getIndex(currenttuple);
				currentTupleTimestamp = StreamAccessor.getDoubleCol(
						currenttuple, inputschArray, 1);

			}

		}
		return earray;
	}

	public void produceAnd(int queryID, EventActiveInstanceQueue[] stacks,
			byte[] tempevent, SchemaElement[] inputschArray,
			int triggerStackindex, int positiveTypeNum) {

		ArrayList<String> queryStackTypes = getQuery(queryID).stackTypes;

		int checkAllEventTypes = 1;

		for (int loop = 0; loop < stacks.length; loop++) {
			if (stacks[loop] != null
					&& queryStackTypes.contains(stacks[loop].stackType)
					&& stacks[loop].eventQueue.getSize() == 0)
				checkAllEventTypes = 0;
		}

		if (checkAllEventTypes == 0)
			return;

		// get the last query type
		String lastQType = queryStackTypes.get(queryStackTypes.size() - 1);

		// get the last query type stack index
		int lastStackIndex = findStack(lastQType);

		ArrayList<ArrayList<byte[]>> sc = sequenceConstructionAnd(queryID,
				lastStackIndex, triggerStackindex, stacks, inputschArray);

		for (int k = 0; k < sc.size(); k++) {
			ArrayList<byte[]> ar2 = new ArrayList<byte[]>();

			for (int h = 0; h < sc.get(k).size(); h++) {

				ar2.add(sc.get(k).get(h));
			}
			// ar2.add(tempevent);

			SchemaElement[] schArray_Result = generateResultSchemas(
					positiveTypeNum, inputschArray);

			byte[] dest = StreamTupleCreator.makeEmptyTuple(schArray_Result);

			int schIndex = 0;
			for (int arIndex = 0; arIndex < positiveTypeNum; arIndex++) {
				StreamTupleCreator.tupleAppend(dest, ar2.get(arIndex),
						schArray_Result[schIndex].getOffset());
				schIndex += inputschArray.length;

			}

			boolean reuse = true;
			bufferResult(queryID, dest, schArray_Result, ar2, reuse,
					inputschArray);

		}
	}

	String getTupleType(byte[] tuple, SchemaElement[] inputschArray) {

		char[] type_char = StreamAccessor.getStr20Col(tuple, inputschArray, 0);

		String event_type = new String(type_char);

		int charIndex = 0;

		while (Character.isLetterOrDigit(event_type.charAt(charIndex))) {
			charIndex++;
		}
		// find out the stack index matching the tuple event type
		String eventType = event_type.substring(0, charIndex);
		return eventType;
	}

	void processQuery(int queryID, byte[] tuple, SchemaElement[] schArray) {

		String triggeringEventType = getTupleType(tuple, schArray);
		// find out the stack index matching the tuple event type
		int triggeringStackIndex = findStack(triggeringEventType);

		QueryInfo qiInfo = getQuery(queryID);

		ArrayList<String> stackTypes = qiInfo.getStackTypes();

		int posEventTypeNum = 0;
		for (int i = 0; i < stackTypes.size(); i++) {
			if (!stackTypes.get(i).startsWith("-")) {
				posEventTypeNum++;
			}
		}

		String type = stackTypes.get(stackTypes.size() - 1);

		// extended to support negation end
		if (type.startsWith("-")) {
			type = stackTypes.get(stackTypes.size() - 2);
		}

		// here, should be the start of recursive function call
		// stackcompute(qi)

		produceAnd(queryID, AIS, tuple, schArray, triggeringStackIndex,
				posEventTypeNum);
		// pass down intervals.

		// for each interval, call compute(qi.child) again

		// after calling each compute(qi.child), and each results are non-empty,
		// call nopointer join(qi, qi.children)

	}

	protected void tuple_insertion(int stackIndex, byte[] tuple) {

		AIS[stackIndex].enqueue(tuple, null);// default null
	}

	@Override
	public int run(int maxDequeueSize) {

		// Configure.previousresultNum = Configure.resultNum;

		// the schema for all input tuples is the same.
		StreamQueue inputQueue = getInputQueueArray()[0];
		SchemaElement[] schArray = inputQueue.getSchema();

		if (initStatus == false) {
			init(schArray);
		}

		for (int i = maxDequeueSize; i > 0; i--) {

			Configure.inputConsumed++;

			long execution_Start = (new Date()).getTime();

			Configure.previousresultNum = Configure.resultNum;
			// first, tuple insertion with pointer set up

			byte[] tuple = inputQueue.dequeue();

			if (tuple == null)
				break;

			for (SchemaElement sch : schArray)
				sch.setTuple(tuple);

			double timestamp = StreamAccessor.getDoubleCol(tuple, schArray, 1);

			// System.out.println(timestamp);

			if (timestamp == 28532.982) {
				System.out.print("check");
			}

			// here, first apply the aggressive purge
			double expiringTimestamp = timestamp - Configure.windowsize;

			String eventType = getTupleType(tuple, schArray);
			// find out the stack index matching the tuple event type
			int stackIndex = findStack(eventType);

			if (stackIndex >= 0 && AIS[stackIndex] != null) {

				// here, I should use the stackType for storing the current
				// tuple instead of using the event type.
				// enqueue tuple and set up pointers.
				// pointer size should be greater than 1
				tuple_insertion(stackIndex, tuple);

				// note, any component event can trigger and operator.
				processQuery(0, tuple, schArray);

			}

			// here, we actually delete tuples.
			if (expiringTimestamp >= 0) {
				// purge tuples with timestamp less than the expiring timestamp
				// purge tuple and reset the RIP pointer
				Utility.purgeStack(this.AIS, expiringTimestamp, schArray);

				/*
				 * Utility.purgeResultBuffer(resultBuffers, expiringTimestamp,
				 * schArray);
				 */

			}
			long executionTimeEnd = (new Date()).getTime();
			Configure.executionTime += executionTimeEnd - execution_Start;
			// System.out.println("exe start time" + execution_Start +
			// "end time"
			// + executionTimeEnd);

			// first chart with x- execution time; y - total sequence results
			// generated

			// only after receiving the last event will write the statistics to
			// the file.

			// experiment 1 x-result number, y-CPU processing time
			if (Configure.previousresultNum != Configure.resultNum) {
				String write = Configure.resultNum + " "
						+ Configure.executionTime;

				System.out.println(write);

				// test start
				// if (Configure.resultNum == 859)
				// System.out.println("bug point");

				// test end
				resultCollection.add(write);

				// System.out.println(timestamp + " " + Configure.executionTime
				// + " " + Configure.resultNum);
			}

			// three dimension statistics
			// String write = Configure.inputConsumed + " " +
			// Configure.executionTime + " " + Configure.resultNum;
			// resultCollection.add(write);

			if (timestamp == 35148.521) {
				System.out.println("output");
				Utility.rewriteToFile(resultCollection);

			}

		}

		return 0;

	}
}
