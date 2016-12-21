package com.hp.hpl.CHAOS.Rewriting;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.Arrays;
import java.util.Comparator;
import java.util.StringTokenizer;

import java.util.Hashtable;

import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamOperator.*;
import com.hp.hpl.CHAOS.Queue.SingleReaderEventQueueArrayImp;
import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;
import com.hp.hpl.CHAOS.StreamData.StreamTupleCreator;
import com.hp.hpl.CHAOS.Rewriting.CompareEvent;

public class Top_down_MegaSEQoperator extends SingleInputStreamOperator {

	static int query_ID = 0;
	// stores results for each query, used for duplicate removal and result
	// reuse;
	Hashtable<Integer, ArrayList<ArrayList<byte[]>>> resultBuffers = new Hashtable<Integer, ArrayList<ArrayList<byte[]>>>();

	// collect results after run
	ArrayList<String> resultCollection = new ArrayList<String>();

	// it stores all the submitted queries information
	ArrayList<QueryInfo> queries = new ArrayList<QueryInfo>();
	QueryInfo[][] orderedQueries;

	private boolean initStatus = false;

	// build concept hierarchy;
	ConceptTree tree = QueryCompiler.createTreeCompany();
	// the cost model
	CostModel CM = new CostModel(orderedQueries, tree);

	// AIS index is the same as concept encoding
	// for the higher level concept which encoding is an interval, we apply the
	// left boundary as the index
	EventActiveInstanceQueue[] AIS = new EventActiveInstanceQueue[(int) tree
			.getRoot().getRightBound(0).x];

	Hashtable<String, Hashtable<Integer, Double>> lastPreviousIndexarr = new Hashtable<String, Hashtable<Integer, Double>>();

	// for upper query(first integer), the lower query(second integer) has
	// visited upto events with time (double)
	Hashtable<Integer, Hashtable<Integer, Double>> existingResultNums = new Hashtable<Integer, Hashtable<Integer, Double>>();

	// the hash tables for representing the hierarchical pattern view
	HierarchicalQueryPlan hqp = new HierarchicalQueryPlan(
			new ArrayList<String>(), tree);

	public Top_down_MegaSEQoperator(int operatorID, StreamQueue[] input,
			StreamQueue[] output) {
		super(operatorID, input, output);
	}

	// It parse the query plan. set up the stack types as well
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
			int q_index = value.indexOf("(");
			int q_index_end = value.indexOf(")");

			String trimedValue = (String) value.subSequence(q_index + 1,
					q_index_end);

			int q_index_id = value.indexOf("(", q_index_end);
			int q_index_id_end = value.indexOf(")", q_index_id);

			String idValue = (String) value.subSequence(q_index_id + 1,
					q_index_id_end);

			query.setQueryID(Integer.parseInt(idValue));

			int virtualQ = value.indexOf("virtual");
			if (virtualQ >= 0) {
				query.setVirtual(1);
			}

			int child_index = value.indexOf("child =", q_index_id_end);

			int parent_index = value.indexOf("parent =", q_index_id_end);

			int minus_index = value.indexOf("minus =", q_index_id_end);

			int cluster_index = value.indexOf("cluster =", q_index_id_end);

			// if the query has both parent and children queries

			ArrayList<childQueryInfo> children = new ArrayList<childQueryInfo>();

			ArrayList<parentQueryInfo> parents = new ArrayList<parentQueryInfo>();

			String copyValue = value;
			if (child_index > 0) {
				childQueryInfo cinfo = new childQueryInfo();

				value = value.substring(child_index + "child =".length() + 1,
						value.length());
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

			value = copyValue;
			ArrayList<Integer> minusQueriesID = new ArrayList<Integer>();
			if (minus_index > 0) {

				// //////////////////////////

				value = value.substring(minus_index + "minus =".length() + 2,
						value.length() - 1);

				StringTokenizer st = new StringTokenizer(value);
				while (st.hasMoreTokens()) {
					String token = st.nextToken();
					int comaIndex = token.indexOf(",");
					if (comaIndex > 0) {
						token = token.substring(0, token.length() - 1);
					}
					// System.out.println(token);
					minusQueriesID.add(new Integer(token));

				}

			}

			// set up cluster queries
			value = copyValue;
			ArrayList<Integer> clusteredQueryIDs = new ArrayList<Integer>();
			if (cluster_index > 0) {

				value = value.substring(cluster_index + "cluster =".length()
						+ 2, value.length() - 1);

				StringTokenizer st = new StringTokenizer(value);
				while (st.hasMoreTokens()) {
					String token = st.nextToken();
					int comaIndex = token.indexOf(",");
					if (comaIndex > 0) {
						token = token.substring(0, token.length() - 1);
					}
					// System.out.println(token);
					clusteredQueryIDs.add(new Integer(token));

				}

				// ////////////////////////////////////////////

			}

			String[] result = trimedValue.split(",");
			for (int x = 0; x < result.length; x++) {
				ETypes_AQuery.add(result[x].toLowerCase());

				// check whether it exists already.
				/*
				 * if (!contains_notsensitive(stackTypes, result[x])) {
				 * 
				 * stackTypes.add(result[x].toLowerCase());
				 * 
				 * }
				 */

			}

			query.setStackTypes(ETypes_AQuery);
			query.setChildren(children);
			query.setParents(parents);
			query.setMinusQueriesID(minusQueriesID);
			query.setClusteredQueryIDs(clusteredQueryIDs);

			queries.add(query);

			// /////////////////////////////////////////////////

			/*
			 * queries.add(new QueryInfo(Naive_SEQoperator.query_ID++,
			 * ETypes_AQuery));
			 */

			orderedQueries = orderQueries();
			queries.clear();
			for (int i = 0; i < orderedQueries.length; i++) {
				queries.add(orderedQueries[i][0]);
			}

		}

	}

	/**
	 * generate virtual query between queries with both concept and pattern
	 * changes
	 * 
	 * @param parentqID
	 * @param currentqID
	 */
	public ArrayList<String> generateVirtualQuery(int parentqID, int currentqID) {
		ArrayList<String> ETypes_TopQuery = new ArrayList<String>();
		ETypes_TopQuery = getQuery(parentqID).stackTypes;

		ArrayList<String> ETypes_LowQuery = new ArrayList<String>();
		ETypes_LowQuery = getQuery(currentqID).stackTypes;

		ArrayList<String> ETypes_AQuery = new ArrayList<String>();

		int wType = 0;
		for (int iType = 0; iType < ETypes_TopQuery.size(); iType++) {
			String topType = ETypes_TopQuery.get(iType);
			for (; wType < ETypes_LowQuery.size(); wType++) {
				String lowType = ETypes_LowQuery.get(wType);
				if (semanticMatch(topType, lowType)) {
					ETypes_AQuery.add(lowType);
					break;
				}
			}

		}
		return ETypes_AQuery;
	}

	/**
	 * The search should start from the largest level (root with the smallest
	 * level).
	 * 
	 * if not found, return {-1.0, -1.0}
	 * 
	 * @param s
	 *            the string we are searching for in the concept tree
	 * @return [leftbound, rightbound] in the concept tree
	 * 
	 */
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

	public int init(SchemaElement[] schArray) {
		super.init();
		// Configure.executionStart = (new Date()).getTime();

		// record the query size before adding virtual queries.

		this.orderedQueries = orderQueries();

		CM.setOrderedQueries(orderedQueries);
		setupExecutionOrder();
		// setupExecutionOrderfortest();

		/************ Hierarchy Pattern Graph **********/

		hqp.setTree(tree);
		ArrayList<String> queryTypes = new ArrayList<String>();
		hqp.setqueries(queries);

		// create hierarchical pattern graph
		for (int j = 0; j < orderedQueries.length; j++) {
			if (orderedQueries[j][0] != null && queries.get(j) != null) {
				queryTypes = queries.get(j).getStackTypes();
				hqp.setStackTypes(queryTypes);
				hqp.createHashtable(orderedQueries[j][0].getQueryID());
			}

		}

		// build event active stacks.
		EventActiveInstanceQueue stack = null;

		ArrayList<String> types = new ArrayList<String>();
		for (int i = 0; i < orderedQueries.length; i++) {
			ArrayList<String> typesi = orderedQueries[i][0].getStackTypes();
			for (int j = 0; j < typesi.size(); j++) {
				String typeI = typesi.get(j);
				if (typeI.startsWith("-")) {
					typeI = typeI.substring(1, typeI.length());
				}
				if (!Utility.contains_notsensitive(types, typeI))
					types.add(typeI);
			}
		}

		for (int i = 0; i < types.size(); i++) {
			// Hashtable<String, ArrayList<EdgeLabel>> hash2 = hqp
			// .getHierarchicalQueryPlan().get(types.get(i));

			Hashtable<String, ArrayList<EdgeLabel>> hash2 = this.hqp.hash1
					.get(types.get(i).toLowerCase());

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

		for (int i = 0; i < AIS.length; i++) {
			if (AIS[i] != null) {
				for (int j = 0; j < this.queries.size(); j++) {
					int queryID = this.queries.get(j).queryID;
					if (getQuery(queryID).stackTypes.contains(AIS[i].stackType)) {
						Hashtable<Integer, Double> table = lastPreviousIndexarr
								.get(AIS[i].stackType);
						if (table != null) {
							table.put(new Integer(queryID), new Double(-1));
							lastPreviousIndexarr.put(AIS[i].stackType, table);
						} else {
							table = new Hashtable<Integer, Double>();
							table.put(new Integer(queryID), new Double(-1));
							lastPreviousIndexarr.put(AIS[i].stackType, table);
						}

					}
				}

			}

		}

		/***************************/

		initStatus = true;
		return 1;
	}

	public void setupExecutionOrderfortest() {

		// virtual query array.
		for (int i = 0; i < this.orderedQueries.length; i++) {
			int currentqID = this.orderedQueries[i][0].queryID;
			double singleCost = this.CM.singleCompute(currentqID);
			double fromTopCost = Double.MAX_VALUE;

			double min = singleCost;
			this.orderedQueries[i][0].setComputeSourceID(currentqID);

			// we only have two choices in the top down evaluation: from itself
			// or from the top query
			if (this.orderedQueries[i][1] != null) {
				int parentqID = this.orderedQueries[i][1].queryID;
				// I need to call the function to get the relationship among two
				// patterns
				boolean[] check = new boolean[2];
				ArrayList<String> middle = new ArrayList<String>();
				ArrayList<Integer> negatePos = new ArrayList<Integer>();
				check = checkQueryPatternConceptR(parentqID, currentqID,
						middle, negatePos);

				// both concept and pattern are changed
				if (check[0] == true && check[1] == true) {
					// use top down cp
					fromTopCost = 0;

					ArrayList<String> ETypes_AQuery = generateVirtualQuery(
							parentqID, currentqID);

					// I should generate a virtual query and put it into ordered
					// queries array
					int virtualqID = Top_down_MegaSEQoperator.query_ID++;
					QueryInfo virtualQ = new QueryInfo(virtualqID,
							ETypes_AQuery);
					virtualQ.setComputeSourceID(parentqID);

					virtualQ.setVirtual(1);

					this.queries.add(virtualQ);

					// compute from the virtual query results
					this.orderedQueries[i][0].setComputeSourceID(virtualqID);

				}
				// only pattern is changed
				else if (check[0] == true) {
					// use pattern top down
					fromTopCost = 0;

					// if (fromTopCost - 100 < min)
					if (fromTopCost - 100 < min
							&& (check[0] == false || check[1] == false)) {
						// Here, I relax the constraint, hoping top down will be
						// indeed better
						min = fromTopCost;
						this.orderedQueries[i][0].setComputeSourceID(parentqID);
					}

				}
				// only concept is changed
				else if (check[1] == true) {
					// use concept top down
					fromTopCost = 0;

					// if (fromTopCost - 100 < min)
					if (fromTopCost - 100 < min
							&& (check[0] == false || check[1] == false)) {
						// Here, I relax the constraint, hoping top down will be
						// indeed better
						min = fromTopCost;
						this.orderedQueries[i][0].setComputeSourceID(parentqID);
					}
				}

			}

		}

	}

	public void setupExecutionOrder() {
		for (int i = 0; i < this.orderedQueries.length; i++) {
			int currentqID = this.orderedQueries[i][0].queryID;

			double singleCost = this.CM.singleCompute(currentqID);
			double fromTopCost = Double.MAX_VALUE;

			double min = singleCost;
			this.orderedQueries[i][0].setComputeSourceID(currentqID);

			// we only have two choices in the top down evaluation: from itself
			// or from the top query
			if (this.orderedQueries[i][1] != null) {
				int parentqID = this.orderedQueries[i][1].queryID;
				// I need to call the function to get the relationship among two
				// patterns
				boolean[] check = new boolean[2];
				ArrayList<String> middle = new ArrayList<String>();
				ArrayList<Integer> negatePos = new ArrayList<Integer>();
				check = checkQueryPatternConceptR(parentqID, currentqID,
						middle, negatePos);

				// both concept and pattern are changed
				if (check[0] == true && check[1] == true) {
					// use top down cp
					fromTopCost = CM.topdown_cp(parentqID, currentqID);
				}
				// only pattern is changed
				else if (check[0] == true) {
					// use pattern top down
					fromTopCost = CM.topdownConCost_pattern(parentqID,
							currentqID);

					// do want
					fromTopCost = fromTopCost / 100000;

				}
				// only concept is changed
				else if (check[1] == true) {
					// use concept top down
					fromTopCost = CM.topdown_concept(parentqID);

					// do n't want
					fromTopCost = fromTopCost * 100000;
				}

				if (fromTopCost - 100 < min) {// Here, I relax the constraint,
					// hoping top down will be
					// indeed better
					min = fromTopCost;
					this.orderedQueries[i][0].setComputeSourceID(parentqID);
				}
			}

		}
	}

	/**
	 * Return true if s1 is one ancestor of s2 in the concept hierarchy.
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
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

	/**
	 * returns true if q1 is an ancestor of q2
	 * 
	 * @param q1
	 * @param q2
	 * @return
	 */
	boolean ancestorMatch(QueryInfo q1, QueryInfo q2) {
		if (q1.getSize() > q2.getSize()) {
			return false;
		} else {
			int maxSize = q2.getSize();
			int current = 0;
			int j = 0;
			for (int i = 0; i < q1.getSize(); i++) {
				if (current >= maxSize) {
					return false;

				} else {
					for (j = current; j < maxSize; j++) {
						if (semanticMatch(q1.getStackTypes().get(i), q2
								.getStackTypes().get(j))) {
							current = j;
							break;
						}
					}
					if (j == maxSize) {
						return false;
					}

				}
			}
			return true;
		}
	}

	/**
	 * @param String
	 *            event type of the tuple
	 * @return index of stack for the tuple
	 */
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

	/***
	 * 
	 * @param int the number of event types in the final result
	 * @param SchemaElement
	 *            [] the schema of the input event
	 * @return the schema array for the final results
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
		schArray_Result = schArray_result.toArray(new SchemaElement[] {});
		return schArray_Result;

	}

	ArrayList<ArrayList<byte[]>> connect_bytime(byte[] currenttuple,
			int currentPos, ArrayList<ArrayList<byte[]>> ear, int insertPos,
			SchemaElement[] schArray) {
		ArrayList<ArrayList<byte[]>> earray = new ArrayList<ArrayList<byte[]>>();

		int copyinsertPos = insertPos;
		int i = 0;

		for (; ear != null && i < ear.size(); i++) {
			boolean added = false;
			ArrayList<byte[]> array = new ArrayList<byte[]>();

			array = ear.get(i);

			ArrayList<byte[]> array_updated = new ArrayList<byte[]>();

			for (int tIndex = 0; tIndex < array.size(); tIndex++) {
				if (insertPos == tIndex) {
					double timestamp = StreamAccessor.getDoubleCol(
							currenttuple, schArray, 1);

					double aftertime = StreamAccessor.getDoubleCol(array
							.get(tIndex), schArray, 1);

					if (insertPos > 0
							&& timestamp > StreamAccessor.getDoubleCol(array
									.get(tIndex - 1), schArray, 1)
							&& timestamp < aftertime) {

						array_updated.add(currenttuple);
						added = true;

						tIndex--;
						insertPos = -1;
					}
					// compare the timestamp

					else if (insertPos == 0 && timestamp < aftertime) {
						{
							array_updated.add(currenttuple);
							added = true;
						}

						tIndex--;
						insertPos = -1;
					}
				} else {
					array_updated.add(array.get(tIndex));
				}

			}

			if (insertPos == array.size()) {
				double currentTime = StreamAccessor.getDoubleCol(currenttuple,
						schArray, 1);
				double lastTime = StreamAccessor.getDoubleCol(array.get(array
						.size() - 1), schArray, 1);
				if (currentTime > lastTime) {
					// results are ordered by last timestamp
					{
						array_updated.add(currenttuple);
						added = true;
					}

				}
			}
			if (added) {

				earray.add(array_updated);
				array_updated = new ArrayList<byte[]>();

			}

			insertPos = copyinsertPos;
		}
		return earray;
	}

	ArrayList<ArrayList<byte[]>> connect_bytime_stackevents(
			ArrayList<byte[]> currenttuple, int currentPos,
			EventActiveInstanceQueue stack, int insertPos,
			SchemaElement[] schArray) {
		ArrayList<ArrayList<byte[]>> earray = new ArrayList<ArrayList<byte[]>>();

		int copyinsertPos = insertPos;
		int i = 0;
		byte[][] retPointerArrayTemp = new byte[10][];// hard code

		byte[] visiting_tuple = stack.eventQueue.peekLast();
		while (visiting_tuple != null) {
			boolean added = false;
			ArrayList<byte[]> array = new ArrayList<byte[]>();
			array.add(visiting_tuple);

			ArrayList<byte[]> array_updated = new ArrayList<byte[]>();

			for (int tIndex = 0; tIndex < currenttuple.size(); tIndex++) {
				if (insertPos == tIndex) {
					double timestamp = StreamAccessor.getDoubleCol(currenttuple
							.get(tIndex), schArray, 1);

					double beforetime = StreamAccessor.getDoubleCol(
							visiting_tuple, schArray, 1);

					// compare the timestamp

					if (timestamp > beforetime) {
						// ok. before adding the visiting_tuple, I need to check
						// whether its timestamp is greater than the left event
						// if exist.
						if (array_updated.size() == 0) {
							// no left events added. we are good
							array_updated.add(visiting_tuple);
							added = true;
							tIndex--;
							insertPos = -1;

						} else {
							// events added; need to check
							byte[] leftEvent = array_updated.get(array_updated
									.size() - 1);
							double leftTime = StreamAccessor.getDoubleCol(
									leftEvent, schArray, 1);
							if (leftTime < beforetime) {
								array_updated.add(visiting_tuple);
								added = true;
								tIndex--;
								insertPos = -1;
							}
						}

					}
				} else {
					array_updated.add(currenttuple.get(tIndex));
				}

			}

			if (insertPos == currenttuple.size()) {
				double currentTime = StreamAccessor.getDoubleCol(currenttuple
						.get(currentPos), schArray, 1);
				double lastTime = StreamAccessor.getDoubleCol(visiting_tuple,
						schArray, 1);
				if (currentTime > lastTime) {
					// results are ordered by last timestamp
					for (byte[] addingTuple : currenttuple) {
						array_updated.add(addingTuple);
						added = true;
					}

					// add the tuple
					array_updated.add(visiting_tuple);
				}
			}
			if (added) {

				earray.add(array_updated);
				array_updated = new ArrayList<byte[]>();

			}

			insertPos = copyinsertPos;

			int index = StreamAccessor.getIndex(visiting_tuple);

			visiting_tuple = stack.getPreviousByPhysicalIndex(index,
					retPointerArrayTemp);

		}
		return earray;
	}

	/**
	 * To join one composite result with another sequence results SEQ(A, B) join
	 * SEQ(C, D)
	 * 
	 * @param currenttuple
	 * @param ear
	 * @param schArray
	 * @param insertPos
	 * @return
	 */
	ArrayList<ArrayList<byte[]>> connect_bytime(ArrayList<byte[]> currenttuple,
			int currentPos, ArrayList<ArrayList<byte[]>> ear, int insertPos,
			SchemaElement[] schArray) {
		ArrayList<ArrayList<byte[]>> earray = new ArrayList<ArrayList<byte[]>>();

		int copyinsertPos = insertPos;
		int i = 0;

		for (; ear != null && i < ear.size(); i++) {
			boolean added = false;
			ArrayList<byte[]> array = new ArrayList<byte[]>();

			array = ear.get(i);

			ArrayList<byte[]> array_updated = new ArrayList<byte[]>();

			for (int tIndex = 0; tIndex < array.size(); tIndex++) {
				if (insertPos == tIndex) {
					double timestamp = StreamAccessor.getDoubleCol(currenttuple
							.get(currentPos), schArray, 1);

					double aftertime = StreamAccessor.getDoubleCol(array
							.get(tIndex), schArray, 1);

					if (insertPos > 0
							&& timestamp > StreamAccessor.getDoubleCol(array
									.get(tIndex - 1), schArray, 1)
							&& timestamp < aftertime) {
						for (byte[] addingTuple : currenttuple) {
							array_updated.add(addingTuple);
							added = true;
						}

						tIndex--;
						insertPos = -1;
					}
					// compare the timestamp

					else if (insertPos == 0 && timestamp < aftertime) {
						for (byte[] addingTuple : currenttuple) {
							array_updated.add(addingTuple);
							added = true;
						}

						tIndex--;
						insertPos = -1;
					}
				} else {
					array_updated.add(array.get(tIndex));
				}

			}

			if (insertPos == array.size()) {
				double currentTime = StreamAccessor.getDoubleCol(currenttuple
						.get(currentPos), schArray, 1);
				double lastTime = StreamAccessor.getDoubleCol(array.get(array
						.size() - 1), schArray, 1);
				if (currentTime > lastTime) {
					// results are ordered by last timestamp
					for (byte[] addingTuple : currenttuple) {
						array_updated.add(addingTuple);
						added = true;
					}

				}
			}
			if (added) {

				earray.add(array_updated);
				array_updated = new ArrayList<byte[]>();

			}

			insertPos = copyinsertPos;
		}
		return earray;
	}

	/**
	 * join one tuple with a set of sequence results. for example given results
	 * of SEQ(A, B), and a tuple c1, it returns a longer result SEQ(A, B, c1).
	 * 
	 * @param currenttuple
	 * @param ear
	 * @return
	 */
	ArrayList<ArrayList<byte[]>> connect_bytime(byte[] currenttuple,
			ArrayList<ArrayList<byte[]>> ear, SchemaElement[] schArray,
			int insertPos) {
		ArrayList<ArrayList<byte[]>> earray = new ArrayList<ArrayList<byte[]>>();

		int copyinsertPos = insertPos;
		int i = 0;

		for (; ear != null && i < ear.size(); i++) {
			boolean added = false;
			ArrayList<byte[]> array = new ArrayList<byte[]>();

			array = ear.get(i);

			ArrayList<byte[]> array_updated = new ArrayList<byte[]>();

			for (int tIndex = 0; tIndex < array.size(); tIndex++) {
				if (insertPos == tIndex) {
					double timestamp = StreamAccessor.getDoubleCol(
							currenttuple, schArray, 1);

					double aftertime = StreamAccessor.getDoubleCol(array
							.get(tIndex), schArray, 1);

					if (insertPos > 0
							&& timestamp > StreamAccessor.getDoubleCol(array
									.get(tIndex - 1), schArray, 1)
							&& timestamp < aftertime) {
						array_updated.add(currenttuple);
						added = true;

						tIndex--;
						insertPos = -1;
					}
					// compare the timestamp

					else if (insertPos == 0 && timestamp < aftertime) {
						array_updated.add(currenttuple);
						added = true;

						tIndex--;
						insertPos = -1;
					}
				} else {
					array_updated.add(array.get(tIndex));
				}

			}

			if (insertPos == array.size()) {
				double currentTime = StreamAccessor.getDoubleCol(currenttuple,
						schArray, 1);
				double lastTime = StreamAccessor.getDoubleCol(array.get(array
						.size() - 1), schArray, 1);
				if (currentTime > lastTime) {// results are ordered by last
					// timestamp

					array_updated.add(currenttuple);
					added = true;

				}
			}
			if (added) {

				earray.add(array_updated);
				array_updated = new ArrayList<byte[]>();

			}

			insertPos = copyinsertPos;
		}
		return earray;
	}

	/**
	 * join one tuple with a set of sequence results. for example given results
	 * of SEQ(A, B), and a tuple c1, it returns a longer result SEQ(A, B, c1).
	 * 
	 * @param currenttuple
	 * @param ear
	 * @return
	 */
	ArrayList<ArrayList<byte[]>> connect(byte[] currenttuple,
			ArrayList<ArrayList<byte[]>> ear, SchemaElement[] inputschArray) {
		ArrayList<ArrayList<byte[]>> earray = new ArrayList<ArrayList<byte[]>>();

		for (int i = 0; i < ear.size(); i++) {
			ArrayList<byte[]> array = new ArrayList<byte[]>();
			array = ear.get(i);

			byte[] lastTuple = array.get(array.size() - 1);
			double timestamp = StreamAccessor.getDoubleCol(lastTuple,
					inputschArray, 1);

			double currenttimestamp = StreamAccessor.getDoubleCol(currenttuple,
					inputschArray, 1);

			if (currenttimestamp > timestamp) {
				array.add(currenttuple);// here, I should compare the timestamp

				ArrayList<byte[]> array2 = new ArrayList<byte[]>();
				for (int j = 0; j < array.size(); j++) {
					array2.add(array.get(j));
				}
				earray.add(array2);
			}

		}
		return earray;
	}

	/**
	 * 
	 * @param queryID
	 * @param stacks
	 * @param stackIndex
	 * @param tempevent
	 * @param inputschArray
	 * @return the nearest tuple in the previous stack for the current incoming
	 *         tuple
	 */

	ArrayList<byte[]> previousTuple_HStacks(int queryID,
			EventActiveInstanceQueue[] stacks, int stackIndex,
			byte[] tempevent, SchemaElement[] inputschArray) {
		String type = Utility.getTupleType(tempevent, inputschArray);

		ArrayList<byte[]> previousTuples = new ArrayList<byte[]>();
		// here, still, the type should be the stack type instead of event type.
		Hashtable<String, ArrayList<EdgeLabel>> table2 = this.hqp
				.getHierarchicalQueryPlan().get(
						AIS[findStack(type)].stackType.toLowerCase());
		if (table2 != null) {
			int pointerSize = table2.keySet().size();
			byte[][] retPointerArrayTemp = new byte[pointerSize][];// change
			int index = StreamAccessor.getIndex(tempevent);
			stacks[stackIndex].getByPhysicalIndex(index, retPointerArrayTemp);
			byte[] previoustuple = null;

			for (int i = 0; i < pointerSize; i++) {

				previoustuple = retPointerArrayTemp[i];

				if (previoustuple != null) {
					String previousType = Utility.getTupleType(previoustuple,
							inputschArray);

					String previousStackType = AIS[findStack(previousType)].stackType;
					// check in the HPG, the previousType - currenttype edge

					ArrayList<EdgeLabel> edges = table2.get(previousStackType);
					boolean found = false;

					for (int j = 0; edges != null && j < edges.size(); j++) {
						if (edges.get(j).queryID == queryID) {
							found = true;
							break;

						}
					}

					if (found)
						previousTuples.add(previoustuple);
				}

			}

		}
		return previousTuples;
	}

	/**
	 * 
	 * 
	 * @param queryID
	 * @return
	 */
	int[] firstQueryTypes(ArrayList<String> stackTypes) {

		// here, I should not use stackType information. Instead, I should
		// follow the hash tables.
		//
		ArrayList<String> firstTypes = new ArrayList<String>();
		String firstType = stackTypes.get(0);

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

	/**
	 * Sequence construction in hierarchical stacks for the query specified by
	 * ID.
	 * 
	 * @param k
	 * @param index
	 * @param stacks
	 * @param queryID
	 * @param inputschArray
	 * @return partial sequence results
	 */
	ArrayList<ArrayList<byte[]>> sequenceConstruction_Hstacks(int k, int index,
			EventActiveInstanceQueue[] stacks, int queryID,
			SchemaElement[] inputschArray, byte[] nextTuple) {

		ArrayList<ArrayList<byte[]>> earray = new ArrayList<ArrayList<byte[]>>();
		ArrayList<ArrayList<byte[]>> earray3 = new ArrayList<ArrayList<byte[]>>();
		ArrayList<byte[]> array = new ArrayList<byte[]>();

		String nextType = Utility.getTupleType(nextTuple, inputschArray)
				.toLowerCase().toLowerCase();
		String stackType = stacks[k].stackType.toLowerCase();
		// I should use the stack type instead of the event type
		Hashtable<String, ArrayList<EdgeLabel>> table2 = this.hqp.hash1
				.get(stackType.toLowerCase());
		byte[][] retPointerArrayTemp = new byte[1][];
		if (table2 != null)
			retPointerArrayTemp = new byte[table2.keySet().size()][];

		// change for multiple queries.

		int[] firstStacks = firstQueryTypes(getQuery(queryID).stackTypes);

		boolean isFirst = false;

		for (int i = 0; i < firstStacks.length; i++) {
			if (firstStacks[i] == k) {
				isFirst = true;
				break;
			}
		}

		if (isFirst) {

			byte[] tuple = stacks[k].getByPhysicalIndex(index,
					retPointerArrayTemp);

			while (tuple != null) {
				ArrayList<byte[]> array2 = new ArrayList<byte[]>();

				array.add(tuple);
				index = StreamAccessor.getIndex(tuple);

				for (int m3 = 0; m3 < array.size(); m3++) {
					array2.add(array.get(m3));
				}
				earray.add(array2);

				array.clear();

				tuple = stacks[k].getPreviousByPhysicalIndex(index,
						retPointerArrayTemp);
			}

			return earray;
		} else {
			int previousRIPindex = 0;

			byte[] tuple = stacks[k].eventQueue.peekLast();

			while (tuple != null) {
				index = StreamAccessor.getIndex(tuple);

				ArrayList<byte[]> previousTuples = previousTuple_HStacks(
						queryID, stacks, k, tuple, inputschArray);
				for (int i = 0; i < previousTuples.size(); i++) {
					byte[] previousTuple = previousTuples.get(i);

					previousRIPindex = StreamAccessor.getIndex(previousTuple);

					int sIndex = findStack(Utility.getTupleType(previousTuple,
							inputschArray));

					earray3 = connect(tuple, sequenceConstruction_Hstacks(
							sIndex, previousRIPindex, stacks, queryID,
							inputschArray, tuple), inputschArray);
					for (int t = 0; t < earray3.size(); t++) {
						earray.add(earray3.get(t));
					}
				}

				tuple = stacks[k].getPreviousByPhysicalIndex(index,
						retPointerArrayTemp);
			}

		}
		return earray;
	}

	/**
	 * return the non exist event types in one query as compared to another
	 * queries
	 * 
	 * @param currentQuery
	 *            QUERYID is not the index here
	 * @param previousQuery
	 * @return
	 */
	ArrayList<String> Topdown_getNonexistTypes(int Query1, int Query2) {
		ArrayList<String> nonexistTypes = new ArrayList<String>();
		ArrayList<String> pQueryTypes = getQuery(Query1).stackTypes;
		ArrayList<String> cQueryTypes = getQuery(Query2).stackTypes;

		int ip = 0;
		int ic = 0;

		while (ip < pQueryTypes.size() && ic < cQueryTypes.size()) {
			String preType = pQueryTypes.get(ip);
			String curType = cQueryTypes.get(ic);
			if (semanticMatch(preType, curType)
					|| semanticMatch(curType, preType)) {
				ip++;
				ic++;

			} else {

				// we think the current query has more query types
				if (Utility.contains_notsensitive(cQueryTypes, curType)
						&& Utility.contains_notsensitive(pQueryTypes, curType)) {
					ip++;
					nonexistTypes.add(preType);
				} else {
					ic++;
					nonexistTypes.add(curType);
				}

			}

		}

		// the left event types
		while (ic < cQueryTypes.size()) {
			nonexistTypes.add(cQueryTypes.get(ic));
			ic++;
		}
		while (ip < pQueryTypes.size()) {
			nonexistTypes.add(pQueryTypes.get(ip));
			ip++;
		}
		return nonexistTypes;
	}

	protected int semanticcontains_notsensitive_position(
			ArrayList<String> list, String astring) {

		int index = list.indexOf(astring);
		if (index < 0) {
			for (int i = 0; i < list.size(); i++) {
				String listT = list.get(i);
				if (listT.startsWith("-")) {
					listT = listT.substring(1, listT.length());
				}

				boolean contain = list.get(i).equalsIgnoreCase(astring)
						|| semanticMatch(list.get(i), astring);

				if (contain) {
					index = i;
					break;
				}

			}

		}

		return index;
	}

	/**
	 * Convert one array list storing each tuple in a sequence result to a whole
	 * byte[].
	 * 
	 * @param ar2
	 * @param schArray_Result
	 *            , the result schema
	 * @param schArray
	 *            , the source schema element
	 * @param qi
	 * @return
	 */
	byte[] converttoByteArray(ArrayList<byte[]> ar2,
			SchemaElement[] schArray_Result, SchemaElement[] schArray, int qi) {
		byte[] dest_result = StreamTupleCreator.makeEmptyTuple(schArray_Result);

		int schIndex_q = 0;
		for (int arIndex = 0; arIndex < getQuery(qi).stackTypes.size()
				&& arIndex < ar2.size(); arIndex++) {
			StreamTupleCreator.tupleAppend(dest_result, ar2.get(arIndex),
					schArray_Result[schIndex_q].getOffset());
			schIndex_q += schArray.length;

		}
		return dest_result;
	}

	ArrayList<PreviousTuples> previousTuple_HStacks_negated(
			ArrayList<String> stackTypes, EventActiveInstanceQueue[] stacks,
			int stackIndex, byte[] tempevent, SchemaElement[] inputschArray) {

		String type = Utility.getTupleType(tempevent, inputschArray);

		ArrayList<String> negatedTypes = new ArrayList<String>();

		for (int j = 0; j < stackTypes.size(); j++) {
			String stype = stackTypes.get(j);
			if (stype.startsWith("-")) {
				stype = stype.substring(1, stype.length());
				negatedTypes.add(stype);
			}
		}

		ArrayList<PreviousTuples> previousTuples = new ArrayList<PreviousTuples>();

		// here, still, the type should be the stack type instead of event type.
		Hashtable<String, ArrayList<EdgeLabel>> table2 = this.hqp
				.getHierarchicalQueryPlan().get(
						AIS[findStack(type)].stackType.toLowerCase());
		boolean found = false;
		if (table2 != null) {
			int pointerSize = table2.keySet().size();
			byte[][] retPointerArrayTemp = new byte[pointerSize][];// change
			int index = StreamAccessor.getIndex(tempevent);

			stacks[stackIndex].getByPhysicalIndex(index, retPointerArrayTemp);
			byte[] previoustuple = null;

			ArrayList<byte[]> previouscurrentTuples = new ArrayList<byte[]>();
			ArrayList<byte[]> previousnegatedTuples = new ArrayList<byte[]>();
			// if the off event type is the last event type in the lower query,
			// we need to check

			String previousType = new String();
			ArrayList<String> previousPosiveTypes = new ArrayList<String>();
			double negationPointedtimestamp = -1;
			for (int i = 0; i < pointerSize; i++) {

				previoustuple = retPointerArrayTemp[i];

				if (previoustuple != null) {
					previousType = Utility.getTupleType(previoustuple,
							inputschArray);

					if (semanticcontains_notsensitive_position(negatedTypes,
							previousType) >= 0) {
						previousnegatedTuples.add(previoustuple);
					} else {
						// not belonging to the current negation type doesn't
						// necessary mean belonging to the current positive type
						// we need to check the current query
						if (semanticcontains_notsensitive_position(stackTypes,
								previousType) >= 0) {

							// in the case of negation, one positive event
							// may point to another non-previous positive event
							if (isCurrentQuery(type, previousType, stackTypes)) {
								previouscurrentTuples.add(previoustuple);
								previousPosiveTypes.add(previousType);
							}

						}

					}

				}
			}
			// there are no negated tuples, all the current positive tuples
			// should be added
			if (previousnegatedTuples.size() == 0
					&& previouscurrentTuples.size() != 0) {

				// found = true;
				for (int j = 0; j < previouscurrentTuples.size(); j++) {
					PreviousTuples e = new PreviousTuples(previouscurrentTuples
							.get(j), negationPointedtimestamp);

					previousTuples.add(e);
				}
			} else if (previousnegatedTuples.size() != 0
					&& previouscurrentTuples.size() != 0) {

				for (int i = 0; i < previousnegatedTuples.size(); i++) {

					byte[] negatedTuple = previousnegatedTuples.get(i);
					String pType = Utility.getTupleType(negatedTuple,
							inputschArray);

					String pStackType = AIS[findStack(pType)].stackType;
					int negatedindex = StreamAccessor.getIndex(negatedTuple);

					Hashtable<String, ArrayList<EdgeLabel>> negatedTable = this.hqp
							.getHierarchicalQueryPlan().get(
									pStackType.toLowerCase());

					if (negatedTable != null) {
						int negatedpointerSize = negatedTable.keySet().size();
						byte[][] negatedPointerArrayTemp = new byte[negatedpointerSize][];
						stacks[findStack(pType)].getByPhysicalIndex(
								negatedindex, negatedPointerArrayTemp);

						for (int p = 0; p < negatedpointerSize; p++) {
							byte[] tuplebeforeNeg = negatedPointerArrayTemp[p];
							if (tuplebeforeNeg != null) {
								String tuplebeforeNegType = Utility
										.getTupleType(tuplebeforeNeg,
												inputschArray);

								if (semanticcontains_notsensitive_position(
										previousPosiveTypes, tuplebeforeNegType) >= 0) {
									negationPointedtimestamp = StreamAccessor
											.getDoubleCol(tuplebeforeNeg,
													inputschArray, 1);
									break;
								}

							}

						}
						for (int j = 0; j < previouscurrentTuples.size(); j++) {
							byte[] currentTuple = previouscurrentTuples.get(j);
							double currentTime = StreamAccessor.getDoubleCol(
									currentTuple, inputschArray, 1);

							if (negationPointedtimestamp >= 0
									&& negationPointedtimestamp < currentTime
									|| negationPointedtimestamp < 0) {

								// found = true;
								PreviousTuples e = new PreviousTuples(
										currentTuple, negationPointedtimestamp);
								previousTuples.add(e);
								break;
							}
						}

					}

				}

			}

		}
		return previousTuples;

	}

	/**
	 * tell whether one event type exists before another type in the query
	 * 
	 * @param currentType
	 * @param previousType
	 * @param queryID
	 * @return
	 */
	boolean isCurrentQuery(String currentType, String previousType,
			ArrayList<String> stackTypes) {
		boolean isCurrentQ = false;
		int position = semanticcontains_notsensitive_position(stackTypes,
				currentType);
		int preposition = semanticcontains_notsensitive_position(stackTypes,
				previousType);

		if (position >= 0 && preposition >= 0 && preposition + 1 == position) {
			isCurrentQ = true;
		} else if (position >= 0 && preposition >= 0
				&& preposition + 2 == position
				&& stackTypes.get(preposition + 1).startsWith("-")) {
			isCurrentQ = true;
		}
		return isCurrentQ;
	}

	ArrayList<ArrayList<byte[]>> sequenceConstruction_Hstacks_negated(int k,
			int index, EventActiveInstanceQueue[] stacks,
			ArrayList<String> stackTypes, SchemaElement[] inputschArray,
			byte[] nextTuple, double stopTime) {

		ArrayList<ArrayList<byte[]>> earray = new ArrayList<ArrayList<byte[]>>();
		ArrayList<ArrayList<byte[]>> earray3 = new ArrayList<ArrayList<byte[]>>();
		ArrayList<byte[]> array = new ArrayList<byte[]>();
		String stackType = stacks[k].stackType.toLowerCase();
		// I should use the stack type instead of the event type
		Hashtable<String, ArrayList<EdgeLabel>> table2 = this.hqp.hash1
				.get(stackType.toLowerCase());
		byte[][] retPointerArrayTemp = new byte[1][];
		int pointerSize = 0;
		if (table2 != null) {
			pointerSize = table2.keySet().size();
			retPointerArrayTemp = new byte[pointerSize][];
		}

		int[] firstStacks = firstQueryTypes(stackTypes);

		boolean isFirst = false;

		for (int i = 0; i < firstStacks.length; i++) {
			if (firstStacks[i] == k) {
				isFirst = true;
				break;
			}
		}

		if (isFirst) {

			byte[] tuple = stacks[k].getByPhysicalIndex(index,
					retPointerArrayTemp);

			boolean[] found = { true };

			ArrayList<String> offTypes = new ArrayList<String>();
			for (int j = 0; j < stackTypes.size(); j++) {
				if (stackTypes.get(j).startsWith("-")) {
					offTypes.add(stackTypes.get(j).substring(1,
							stackTypes.get(j).length()));
				}
			}

			String firstType = new String();

			firstType = stackTypes.get(0);
			if (firstType.startsWith("-")) {
				firstType = firstType.substring(1, firstType.length());
			}

			if (offTypes.contains(firstType)) {
				int i = 0;

				for (; i < pointerSize; i++) {

					byte[] lowerfirsttuple = retPointerArrayTemp[i];
					if (lowerfirsttuple != null) {
						String tupleT = Utility.getTupleType(lowerfirsttuple,
								inputschArray);
						if (semanticMatch(firstType, tupleT)) {
							found[0] = false;
							break;
						}

					}

				}

				if (i == pointerSize) {
					found[0] = true;
				}

			} else {
				found[0] = true;
			}

			while (tuple != null) {
				ArrayList<byte[]> array2 = new ArrayList<byte[]>();

				if (found[0] == true) {
					array.add(tuple);
					index = StreamAccessor.getIndex(tuple);

					for (int m3 = 0; m3 < array.size(); m3++) {
						array2.add(array.get(m3));
					}
					earray.add(array2);

					array.clear();
				}

				tuple = stacks[k].getPreviousByPhysicalIndex(index,
						retPointerArrayTemp);

				double timestamp = StreamAccessor.getDoubleCol(tuple,
						inputschArray, 1);
				if (timestamp <= stopTime)
					break;

				if (offTypes.contains(firstType)) {
					int i = 0;

					for (; i < pointerSize; i++) {

						byte[] lowerfirsttuple = retPointerArrayTemp[i];
						if (lowerfirsttuple != null) {
							String tupleT = Utility.getTupleType(
									lowerfirsttuple, inputschArray);
							if (semanticMatch(firstType, tupleT)) {
								found[0] = false;
								break;
							}

						}

					}

					if (i == pointerSize) {
						found[0] = true;
					}

				} else {
					found[0] = true;
				}

			}

			return earray;
		} else {
			int previousRIPindex = 0;
			byte[] currenttuple = stacks[k].eventQueue.peekLast();
			while (currenttuple != null) {

				index = StreamAccessor.getIndex(currenttuple);

				ArrayList<PreviousTuples> previousTuples = previousTuple_HStacks_negated(
						stackTypes, stacks, k, currenttuple, inputschArray);
				for (int i = 0; i < previousTuples.size(); i++) {
					byte[] previousTuple = previousTuples.get(i)
							.getPreviousTuple();
					String type = Utility.getTupleType(previousTuple,
							inputschArray);

					double stop = previousTuples.get(i).getStopTimestamp();

					previousRIPindex = StreamAccessor.getIndex(previousTuple);

					int sIndex = findStack(Utility.getTupleType(previousTuple,
							inputschArray));

					earray3 = connect(currenttuple,
							sequenceConstruction_Hstacks_negated(sIndex,
									previousRIPindex, stacks, stackTypes,
									inputschArray, currenttuple, stop),
							inputschArray);
					for (int t = 0; t < earray3.size(); t++) {
						earray.add(earray3.get(t));
					}
				}

				currenttuple = stacks[k].getPreviousByPhysicalIndex(index,
						retPointerArrayTemp);

				double currenttimestamp = StreamAccessor.getDoubleCol(
						currenttuple, inputschArray, 1);

				if (currenttimestamp <= stopTime) {
					currenttuple = null;
					break;
				}

			}

		}
		return earray;
	}

	/**
	 * It produce the sequence results in a hierarchical stack for a single
	 * query triggered by one event.
	 * 
	 * @param stacks
	 * @param tempevent
	 */
	public void produceinorder_HStacks(int queryID,
			EventActiveInstanceQueue[] stacks, int stackIndex,
			byte[] tempevent, SchemaElement[] inputschArray, int eventTypeNum) {

		ArrayList<String> stackTypes = getQuery(queryID).stackTypes;

		ArrayList<PreviousTuples> previousTuples = previousTuple_HStacks_negated(
				stackTypes, stacks, stackIndex, tempevent, inputschArray);

		for (int i = 0; i < previousTuples.size(); i++) {
			byte[] previousTuple = previousTuples.get(i).getPreviousTuple();
			double stopTime = previousTuples.get(i).getStopTimestamp();

			String type = Utility.getTupleType(previousTuple, inputschArray)
					.toLowerCase();
			int prevousStackIndex = findStack(type);

			int previousRIPindex = StreamAccessor.getIndex(previousTuple);

			ArrayList<ArrayList<byte[]>> sc = sequenceConstruction_Hstacks_negated(
					prevousStackIndex, previousRIPindex, stacks, stackTypes,
					inputschArray, tempevent, stopTime);

			for (int k = 0; k < sc.size(); k++) {
				ArrayList<byte[]> ar2 = new ArrayList<byte[]>();

				for (int h = 0; h < sc.get(k).size(); h++) {

					ar2.add(sc.get(k).get(h));
				}
				ar2.add(tempevent);

				SchemaElement[] schArray_Result = generateResultSchemas(
						eventTypeNum, inputschArray);

				byte[] dest = StreamTupleCreator
						.makeEmptyTuple(schArray_Result);

				int schIndex = 0;
				for (int arIndex = 0; arIndex < eventTypeNum; arIndex++) {
					StreamTupleCreator.tupleAppend(dest, ar2.get(arIndex),
							schArray_Result[schIndex].getOffset());
					schIndex += inputschArray.length;

				}

				boolean reuse = false;
				topdown_bufferResult(queryID, dest, schArray_Result, ar2,
						reuse, inputschArray);

			}

		}

	}

	public void topdown_bufferResult(int queryID, byte[] dest,
			SchemaElement[] schArray_Result, ArrayList<byte[]> ar2,
			boolean reuseresults, SchemaElement[] schArray) {

		if (Utility.windowOpt(ar2, schArray)) {

			boolean reuse = false;
			for (int k = 0; k < this.orderedQueries.length; k++) {
				if (this.orderedQueries[k][0].computeSourceID == queryID) {
					reuse = true;
					break;
				}
			}

			// if the current query is a not a virtual query
			if (getQuery(queryID).getVirtual() == 0) {
				if (reuseresults) {
					// Utility.rewriteToFile("======reuse for query======"
					// + queryID);

					// System.out.println("======reuse======" + queryID);
				} else {
					// Utility.rewriteToFile("======result for query======"
					// + queryID);
					// System.out
					// .println("======result for query======" + queryID);
				}

				Utility.rewriteToFile("====== result======" + queryID);
				Utility.rewriteToFile(StreamAccessor.toString(dest,
						schArray_Result));

				/*
				 * System.out.println(StreamAccessor.toString(dest,
				 * schArray_Result));
				 */

				Configure.resultNum += 1;
				// latency
				Utility.accuLatency(ar2);
				com.hp.hpl.CHAOS.Queue.Utility.enqueueGroup(OutputQueueArray,
						dest);
			}

			if (reuse) {
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

	}

	/**
	 * result reuse from previous constructed upper query results. One trigger,
	 * and impact multiple patterns
	 * 
	 * @param schArray
	 * @param queryID
	 *            get subsequence for queries above such query
	 */
	public void topdown_enqueueSubsequences(int topqID, int bottomqID,
			ArrayList<ArrayList<byte[]>> upperRBuffer,
			SchemaElement[] schArray, double lastVisited, byte[] tuple) {
		{
			// boolean indicator between queries for pattern, concept changes
			// and negation
			boolean[] check = new boolean[4];
			ArrayList<String> middle = new ArrayList<String>();
			ArrayList<Integer> negatePos = new ArrayList<Integer>();
			check = checkQueryPatternConceptR(topqID, bottomqID, middle,
					negatePos);

			// pattern, concept, offNeg, conceptNeg

			// only pattern is changed
			if (check[0] == true && check[1] == false && check[2] == false) {
				// join results from shorter one
				topdown_PatternJoins(schArray, topqID, bottomqID, this.AIS,
						schArray, upperRBuffer, lastVisited, tuple);

			}
			// only concept is changed
			else if (check[0] == false && check[1] == true && check[3] == false) {
				topdown_ConceptInterPrete(upperRBuffer, schArray, topqID,
						bottomqID, lastVisited);
			}
			// both concept and pattern are changed
			// Actually, I don't have this case now any more ,as after adding
			// virtual queries, this case is broken into the above two cases.

			// only one pattern changed with negation
			else if (check[0] == true && check[1] == false && check[2] == true) {
				topdown_PatternNegation(upperRBuffer, schArray, topqID,
						bottomqID, lastVisited, negatePos);
			}

			// only one concept change with negation
			else if (check[0] == false && check[1] == true && check[3] == true) {
				topdown_ConceptNegation(upperRBuffer, schArray, topqID,
						bottomqID, lastVisited, negatePos);

			}
		}

	}

	/**
	 * only the lower level query includes negation need to remember where we
	 * were last time
	 * 
	 * @param ar2
	 * @param schArray
	 * @param queryID
	 * @param cqID
	 * @param iresult
	 * @param newResult
	 * @param negatePos
	 */

	void topdown_PatternNegation(ArrayList<ArrayList<byte[]>> upperRBuffer,
			SchemaElement[] schArray, int queryID, int cqID,
			double lastVisited, ArrayList<Integer> negatePos) {

		// binary search for lastVisited, if the found index is the last event,
		// return
		int indexBuffer = Utility.binarySearch(resultBuffers, lastVisited,
				schArray, queryID);
		// no new results are added
		if (indexBuffer == upperRBuffer.size() - 1)
			return;

		// child query stacks
		ArrayList<String> childQueryTypes = getQuery(cqID).stackTypes;

		// parent query stacks
		ArrayList<String> parentQueryTypes = getQuery(queryID).stackTypes;

		// release some subsequence results
		SchemaElement[] schArray_Result = generateResultSchemas(Utility
				.getPositiveTypeNum(childQueryTypes), schArray);
		byte[] dest = StreamTupleCreator.makeEmptyTuple(schArray_Result);
		Boolean negativeExist = false;

		// check each result
		for (int iresult = indexBuffer + 1; iresult < upperRBuffer.size(); iresult++) {
			for (int negateIndex = 0; negateIndex < negatePos.size(); negateIndex++) {
				byte[] checkingTuple = upperRBuffer.get(iresult).get(
						negatePos.get(negateIndex));
				byte[] checkingPrevTuple = null;
				double checkingTimeStamp = StreamAccessor.getDoubleCol(
						checkingTuple, schArray, 1);
				double checkingPrevtimestamp = 0;

				if (negatePos.get(negateIndex) > 0) {
					checkingPrevTuple = upperRBuffer.get(iresult).get(
							negatePos.get(negateIndex) - 1);
					checkingPrevtimestamp = StreamAccessor.getDoubleCol(
							checkingPrevTuple, schArray, 1);
				}

				String cEtype = childQueryTypes.get(negatePos.get(negateIndex));

				// remove the "-" notation from the event type
				cEtype = cEtype.substring(1, cEtype.length());

				// //////////////////////////////////////////////////////////////

				String tupleType = Utility
						.getTupleType(checkingTuple, schArray);
				int stackIndex = findStack(tupleType);

				// here, still, the type should be the stack type instead of
				// event
				// type.
				Hashtable<String, ArrayList<EdgeLabel>> table2 = this.hqp
						.getHierarchicalQueryPlan().get(
								AIS[stackIndex].stackType.toLowerCase());
				if (table2 != null) {
					int pointerSize = table2.keySet().size();
					byte[][] retPointerArrayTemp = new byte[pointerSize][];
					int index = StreamAccessor.getIndex(checkingTuple);
					AIS[stackIndex].getByPhysicalIndex(index,
							retPointerArrayTemp);
					byte[] previoustuple = null;

					for (int pIndex = 0; pIndex < pointerSize; pIndex++) {

						previoustuple = retPointerArrayTemp[pIndex];

						if (previoustuple != null) {
							String previousType = Utility.getTupleType(
									previoustuple, schArray);

							// check the negative events between positive tuples
							// in the result
							if (semanticMatch(cEtype, previousType)) {
								double timestamp = StreamAccessor.getDoubleCol(
										previoustuple, schArray, 1);
								if (checkingPrevtimestamp != 0
										&& checkingPrevtimestamp < timestamp
										&& timestamp < checkingTimeStamp
										|| checkingPrevtimestamp == 0
										&& timestamp < checkingTimeStamp) {
									negativeExist = true;
									break;

								}

							}

						}
					}
				}

			}

			if (negativeExist == false) {
				dest = converttoByteArray(upperRBuffer.get(iresult),
						schArray_Result, schArray, cqID);
				topdown_bufferResult(cqID, dest, schArray_Result, upperRBuffer
						.get(iresult), true, schArray);
			}
		}

	}

	// only concept change on negative types
	void topdown_ConceptNegation(ArrayList<ArrayList<byte[]>> upperRBuffer,
			SchemaElement[] schArray, int queryID, int cqID,
			double lastVisited, ArrayList<Integer> negatePos) {

		// binary search for lastVisited, if the found index is the last event,
		// return
		int indexBuffer = Utility.binarySearch(resultBuffers, lastVisited,
				schArray, queryID);
		// no new results are added
		if (indexBuffer == upperRBuffer.size() - 1)
			return;

		// child query stacks
		ArrayList<String> childQueryTypes = getQuery(cqID).stackTypes;

		// parent query stacks
		ArrayList<String> parentQueryTypes = getQuery(queryID).stackTypes;

		// release some subsequence results

		SchemaElement[] schArray_Result = generateResultSchemas(Utility
				.getPositiveTypeNum(childQueryTypes), schArray);
		byte[] dest = StreamTupleCreator.makeEmptyTuple(schArray_Result);
		Boolean negativeExist = false;

		// check each result, the negative type changes concept level
		// currently, I don't support both concept changes on positive
		// and negative types.
		for (int iresult = indexBuffer + 1; iresult < upperRBuffer.size(); iresult++) {
			for (int nIndex = 0; nIndex < negatePos.size(); nIndex++) {
				byte[] cTuple = upperRBuffer.get(iresult).get(
						negatePos.get(nIndex));

				double cTimeStamp = StreamAccessor.getDoubleCol(cTuple,
						schArray, 1);

				double checkingPrevtimestamp = 0;

				if (negatePos.get(nIndex) > 0) {
					byte[] checkingPrevTuple = upperRBuffer.get(iresult).get(
							negatePos.get(nIndex) - 1);
					checkingPrevtimestamp = StreamAccessor.getDoubleCol(
							checkingPrevTuple, schArray, 1);
				}

				String ptype = parentQueryTypes.get(negatePos.get(nIndex));
				String ctype = childQueryTypes.get(negatePos.get(nIndex));

				// remove the "-" notation from the event type
				ptype = ptype.substring(1, ptype.length());
				ctype = ctype.substring(1, ctype.length());

				String tupleType = Utility.getTupleType(cTuple, schArray);
				int stackIndex = findStack(tupleType);

				// here, still, the type should be the stack type instead of
				// event type.
				Hashtable<String, ArrayList<EdgeLabel>> table2 = this.hqp
						.getHierarchicalQueryPlan().get(
								AIS[stackIndex].stackType.toLowerCase());
				if (table2 != null) {
					int pointerSize = table2.keySet().size();
					byte[][] retPointerArrayTemp = new byte[pointerSize][];
					int index = StreamAccessor.getIndex(cTuple);
					AIS[stackIndex].getByPhysicalIndex(index,
							retPointerArrayTemp);
					byte[] previoustuple = null;

					for (int pIndex = 0; pIndex < pointerSize; pIndex++) {

						previoustuple = retPointerArrayTemp[pIndex];

						if (previoustuple != null) {
							String previousType = Utility.getTupleType(
									previoustuple, schArray);
							// if the parent query has evaluated the negated
							// type
							// already, skip it
							if (semanticMatch(ptype, previousType)) {
								continue;
							}
							// if the event type doesn't match the parent
							// query,
							// but
							// it matches the child query
							else if (semanticMatch(ctype, previousType)) {
								double timestamp = StreamAccessor.getDoubleCol(
										previoustuple, schArray, 1);
								// filter by child negative
								if (checkingPrevtimestamp != 0
										&& checkingPrevtimestamp < timestamp
										&& timestamp < cTimeStamp
										|| checkingPrevtimestamp == 0
										&& timestamp < cTimeStamp) {
									negativeExist = true;
									break;

								}

							}

						}
					}
				}

			}

			dest = converttoByteArray(upperRBuffer.get(iresult),
					schArray_Result, schArray, cqID);
			// can not be filtered
			if (negativeExist == false) {

				ArrayList<byte[]> ar2 = upperRBuffer.get(iresult);

				schArray_Result = generateResultSchemas(Utility
						.getPositiveTypeNum(childQueryTypes), schArray);

				byte[] result = StreamTupleCreator
						.makeEmptyTuple(schArray_Result);

				int schIndex = 0;
				for (int arIndex = 0; arIndex < Utility
						.getPositiveTypeNum(childQueryTypes); arIndex++) {
					StreamTupleCreator.tupleAppend(result, ar2.get(arIndex),
							schArray_Result[schIndex].getOffset());
					schIndex += schArray.length;

				}

				topdown_bufferResult(cqID, result, schArray_Result, ar2, true,
						schArray);
			}

		}

	}

	/**
	 * It is for result reuse(result interpret in concept hierarchy) both the
	 * two queries have the same negation
	 * 
	 * @param ar2
	 * @param schArray
	 * @param queryID
	 */
	void topdown_ConceptInterPrete_sameNegation(ArrayList<byte[]> ar2,
			SchemaElement[] schArray, int queryID, int cqID, int iresult,
			boolean newResult) {

		if (!newResult)
			return;

		// top down evaluation for each of the child
		ArrayList<String> childQueryTypes = getQuery(cqID).stackTypes;

		// release some subsequence results
		int schIndex = 0;
		SchemaElement[] schArray_Result = generateResultSchemas(Utility
				.getPositiveTypeNum(childQueryTypes), schArray);
		byte[] dest = StreamTupleCreator.makeEmptyTuple(schArray_Result);
		int i = 0;
		int destLength = 0;

		// check each result
		for (int Ibuffer = 0; Ibuffer < ar2.size(); Ibuffer++) {
			byte[] checkingTuple = ar2.get(Ibuffer);

			String tupleType = Utility.getTupleType(checkingTuple, schArray);

			for (; i < childQueryTypes.size();) {
				String curChildType = childQueryTypes.get(i);
				if (curChildType.startsWith("-")) {
					i++;
					continue;
				}
				if (semanticMatch(childQueryTypes.get(i), tupleType)) {
					// should be changed for which part match
					StreamTupleCreator.tupleAppend(dest, checkingTuple,
							schArray_Result[schIndex].getOffset());
					destLength++;
					schIndex += schArray.length;
					i++;
					break;

				} else
					i++;
			}

		}
		if (destLength == Utility.getPositiveTypeNum(childQueryTypes)) {

			topdown_bufferResult(cqID, dest, schArray_Result, ar2, true,
					schArray);
		}
	}

	/**
	 * It is for result reuse(result interpret in concept hierarchy)
	 * upperRBuffer, schArray, topqID, bottomqID, lastVisited);
	 * 
	 * @param ar2
	 * @param schArray
	 * @param queryID
	 */
	void topdown_ConceptInterPrete(ArrayList<ArrayList<byte[]>> upperRBuffer,
			SchemaElement[] schArray, int queryID, int cqID, double lastVisited) {

		// binary search for lastVisited, if the found index is the last event,
		// return
		int index = Utility.binarySearch(resultBuffers, lastVisited, schArray,
				queryID);
		// no new results are added
		if (index == upperRBuffer.size() - 1)
			return;
		// top down evaluation for each of the child
		ArrayList<String> childQueryTypes = getQuery(cqID).stackTypes;

		// release some subsequence results

		SchemaElement[] schArray_Result = generateResultSchemas(Utility
				.getPositiveTypeNum(childQueryTypes), schArray);

		// check each result
		for (int iresult = index + 1; iresult < upperRBuffer.size(); iresult++) {
			int destLength = 0;
			int schIndex = 0;
			byte[] dest = StreamTupleCreator.makeEmptyTuple(schArray_Result);
			for (int Ibuffer = 0; Ibuffer < upperRBuffer.get(iresult).size(); Ibuffer++) {

				byte[] checkingTuple = upperRBuffer.get(iresult).get(Ibuffer);

				String tupleType = Utility
						.getTupleType(checkingTuple, schArray);

				if (semanticMatch(childQueryTypes.get(destLength), tupleType)) {
					// should be changed for which part match
					StreamTupleCreator.tupleAppend(dest, checkingTuple,
							schArray_Result[schIndex].getOffset());
					destLength++;
					schIndex += schArray.length;

				} else
					break;

			}

			if (destLength == Utility.getPositiveTypeNum(childQueryTypes)) {

				topdown_bufferResult(cqID, dest, schArray_Result, upperRBuffer
						.get(iresult), true, schArray);
			}
		}

	}

	/**
	 * produceinorder_HStacks second version, without queryID construct results
	 * given stacks and return them
	 * 
	 * @param queryID
	 * @param stackTypes
	 * @param stacks
	 * @param stackIndex
	 * @param tempevent
	 * @param inputschArray
	 * @param eventTypeNum
	 */
	public ArrayList<ArrayList<byte[]>> produceinorder_HStacks(
			ArrayList<String> stackTypes, EventActiveInstanceQueue[] stacks,
			int stackIndex, byte[] tempevent, SchemaElement[] inputschArray,
			int eventTypeNum) {

		ArrayList<ArrayList<byte[]>> results = new ArrayList<ArrayList<byte[]>>();
		ArrayList<PreviousTuples> previousTuples = previousTuple_HStacks_negated(
				stackTypes, stacks, stackIndex, tempevent, inputschArray);

		for (int i = 0; i < previousTuples.size(); i++) {
			byte[] previousTuple = previousTuples.get(i).getPreviousTuple();
			double stopTime = previousTuples.get(i).getStopTimestamp();

			String type = Utility.getTupleType(previousTuple, inputschArray)
					.toLowerCase();
			int prevousStackIndex = findStack(type);

			int previousRIPindex = StreamAccessor.getIndex(previousTuple);

			ArrayList<ArrayList<byte[]>> sc = sequenceConstruction_Hstacks_negated(
					prevousStackIndex, previousRIPindex, stacks, stackTypes,
					inputschArray, tempevent, stopTime);

			for (int k = 0; k < sc.size(); k++) {
				ArrayList<byte[]> ar2 = new ArrayList<byte[]>();

				for (int h = 0; h < sc.get(k).size(); h++) {

					ar2.add(sc.get(k).get(h));
				}
				ar2.add(tempevent);

				SchemaElement[] schArray_Result = generateResultSchemas(
						eventTypeNum, inputschArray);

				byte[] dest = StreamTupleCreator
						.makeEmptyTuple(schArray_Result);

				results.add(ar2);
				/*
				 * int schIndex = 0; for (int arIndex = 0; arIndex <
				 * eventTypeNum; arIndex++) {
				 * StreamTupleCreator.tupleAppend(dest, ar2.get(arIndex),
				 * schArray_Result[schIndex].getOffset()); schIndex +=
				 * inputschArray.length;
				 * 
				 * }
				 */

			}

		}

		return results;
	}

	void topdown_PatternJoins(SchemaElement[] schArray, int queryID,
			int childID, EventActiveInstanceQueue[] stacks,
			SchemaElement[] inputschArray,
			ArrayList<ArrayList<byte[]>> resultBuffer, double lastVisited,
			byte[] tuple) {
		// wait, I should notice that the index for queries is not the same for
		// the order of queryID also.
		ArrayList<String> cQueryTypes = getQuery(childID).stackTypes;
		ArrayList<String> QueryTypes = getQuery(queryID).stackTypes;

		SchemaElement[] schArray_Result = generateResultSchemas(Utility
				.getPositiveTypeNum(cQueryTypes), schArray);

		// first check what event types are missing
		ArrayList<String> nonexistTypes = Topdown_getNonexistTypes(queryID,
				childID);

		String lastNonType = nonexistTypes.get(nonexistTypes.size() - 1);
		String lastCqType = cQueryTypes.get(cQueryTypes.size() - 1);
		String lastqType = QueryTypes.get(QueryTypes.size() - 1);

		int left_jIndex = 0;

		int right_jIndex = 0;

		// boolean upperLeft = false;
		int negativeTypeNum = 0;
		// I need to compute the joining events position
		for (left_jIndex = 0; left_jIndex < QueryTypes.size(); left_jIndex++) {
			if (QueryTypes.get(left_jIndex).equalsIgnoreCase(
					cQueryTypes.get(left_jIndex))) {
				if (QueryTypes.get(left_jIndex).startsWith("-")) {
					negativeTypeNum++;
				}
				continue;
			} else {
				break;
			}
		}

		// everything matches so far
		// seq(a,b) vs. seq(a, b, c, d)
		if (left_jIndex == QueryTypes.size()) {
			// left_jIndex--;
			left_jIndex = left_jIndex - negativeTypeNum;
			right_jIndex = 0;
			// upperLeft = true;
		} else if (left_jIndex == QueryTypes.size() - 1) {
			left_jIndex = left_jIndex - negativeTypeNum;
			right_jIndex = cQueryTypes.size() - 1;
			// upperLeft = true;
		} else {
			right_jIndex = left_jIndex;// may also need to consider
			// negativeTypeNum
			// left_jIndex = cQueryTypes.size() - 1;
			// upperLeft = false;
		}

		// if sharing queries have the same last event type, these results
		// generated just now for the upper query should be joined with all
		// events in the
		// nonexistTypes.
		if (lastCqType.equalsIgnoreCase(lastqType)) {

			// if only one event type is off, directly use that stack for join
			if (Utility.getPositiveTypeNum(nonexistTypes) == 1) {
				// left_jIndex = 0;
				int stackIndex = findStack(nonexistTypes.get(0));
				int lastIndex = Utility.binarySearch(resultBuffers,
						lastVisited, schArray, queryID);
				// for each new upper query result, it should connect with
				// nonexistTypes results

				for (int i = lastIndex + 1; i < resultBuffer.size(); i++) {
					ArrayList<ArrayList<byte[]>> c_result = new ArrayList<ArrayList<byte[]>>();
					ArrayList<byte[]> bufferedTuple = resultBuffer.get(i);

					// test start
					byte[] firstTuple = bufferedTuple.get(0);
					byte[] secondTuple = bufferedTuple.get(1);
					byte[] thirdTuple = bufferedTuple.get(2);

					double firsttimestamp = StreamAccessor.getDoubleCol(
							firstTuple, schArray, 1);
					double secondtimestamp = StreamAccessor.getDoubleCol(
							secondTuple, schArray, 1);
					double thirdtimestamp = StreamAccessor.getDoubleCol(
							thirdTuple, schArray, 1);

					if (firsttimestamp == 34187.625
							&& secondtimestamp == 34194.715
							&& thirdtimestamp == 34196.159) {
						System.out.println("check");
					}

					// test end

					c_result = connect_bytime_stackevents(bufferedTuple,
							right_jIndex, stacks[stackIndex], left_jIndex,
							schArray);
					byte[] dest_result = StreamTupleCreator
							.makeEmptyTuple(schArray_Result);
					for (ArrayList<byte[]> sc_result : c_result) {
						dest_result = ArrayListToByte(childID, sc_result,
								inputschArray);
						topdown_bufferResult(childID, dest_result,
								schArray_Result, sc_result, true, schArray);
					}

				}

			} else {
				int stackIndex = findStack(lastNonType);
				// compute nonexistTypes results

				byte[] offTuple = AIS[stackIndex].eventQueue.peekLast();

				ArrayList<ArrayList<byte[]>> partialnonResults = new ArrayList<ArrayList<byte[]>>();

				while (offTuple != null) {
					int index = StreamAccessor.getIndex(offTuple);

					// if off by more than one event type, use below, joining
					// events by stack based join

					// /////////////////////////////////////////////////////
					ArrayList<ArrayList<byte[]>> nonTypesresults = new ArrayList<ArrayList<byte[]>>();

					nonTypesresults = produceinorder_HStacks(nonexistTypes,
							AIS, stackIndex, offTuple, schArray, nonexistTypes
									.size());

					// //////////////////////////////////////////////////

					byte[][] retPointerArrayTemp = new byte[1][];

					Hashtable<String, ArrayList<EdgeLabel>> hash2 = this.hqp.hash1
							.get(stacks[stackIndex].stackType.toLowerCase());
					if (hash2 != null) {
						retPointerArrayTemp = new byte[hash2.keySet().size()][];
					}

					offTuple = stacks[stackIndex].getPreviousByPhysicalIndex(
							index, retPointerArrayTemp);
					for (ArrayList<byte[]> partialR : nonTypesresults) {
						partialnonResults.add(partialR);
					}

				}

				int lastIndex = Utility.binarySearch(resultBuffers,
						lastVisited, schArray, queryID);

				// for each new upper query result, it should connect with
				// nonexistTypes results

				for (int i = lastIndex + 1; i < resultBuffer.size(); i++) {
					ArrayList<ArrayList<byte[]>> c_result = new ArrayList<ArrayList<byte[]>>();
					ArrayList<byte[]> bufferedTuple = resultBuffer.get(i);
					c_result = connect_bytime(bufferedTuple, right_jIndex,
							partialnonResults, nonexistTypes.size(), schArray);
					byte[] dest_result = StreamTupleCreator
							.makeEmptyTuple(schArray_Result);
					for (ArrayList<byte[]> sc_result : c_result) {
						dest_result = ArrayListToByte(childID, sc_result,
								inputschArray);
						topdown_bufferResult(childID, dest_result,
								schArray_Result, sc_result, true, schArray);
					}

				}
			}

		} else {

			// for each result triggered by the incoming tuple, it should
			// connect with buffered results
			String type = Utility.getTupleType(tuple, inputschArray);
			if (type.equalsIgnoreCase(lastCqType)) {

				ArrayList<ArrayList<byte[]>> nonTypesresults = new ArrayList<ArrayList<byte[]>>();
				// I should classify two cases:
				if (nonexistTypes.size() > 1) {
					// check the event type of the incoming tuple, whether it
					// can
					// trigger the construction for the upper query
					// if yes, first compute nonexistTypes results

					int stackIndex = findStack(type);
					nonTypesresults = produceinorder_HStacks(nonexistTypes,
							AIS, stackIndex, tuple, schArray, nonexistTypes
									.size());

					for (ArrayList<byte[]> nonTuple : nonTypesresults) {
						ArrayList<ArrayList<byte[]>> c_result = new ArrayList<ArrayList<byte[]>>();

						c_result = connect_bytime(nonTuple, 0, resultBuffer,
								left_jIndex, schArray);

						byte[] dest_result = StreamTupleCreator
								.makeEmptyTuple(schArray_Result);
						for (ArrayList<byte[]> sc_result : c_result) {
							dest_result = ArrayListToByte(childID, sc_result,
									inputschArray);
							topdown_bufferResult(childID, dest_result,
									schArray_Result, sc_result, true, schArray);
						}

					}
				} else {
					// only the new incoming tuple should be joined with the
					// buffered results.

					int stackIndex = findStack(type);
					int index = StreamAccessor.getIndex(tuple);

					Hashtable<String, ArrayList<EdgeLabel>> hash2 = this.hqp.hash1
							.get(stacks[stackIndex].stackType.toLowerCase());
					byte[][] retPointerArrayTemp = new byte[1][];
					if (hash2 != null) {
						retPointerArrayTemp = new byte[hash2.keySet().size()][];
					}

					byte[] join_tuple = stacks[stackIndex].getByPhysicalIndex(
							index, retPointerArrayTemp);

					ArrayList<ArrayList<byte[]>> c_result = new ArrayList<ArrayList<byte[]>>();

					// int currentPos, int insertPos

					c_result = connect_bytime(join_tuple, 0, resultBuffer,
							left_jIndex, schArray);

					byte[] dest_result = StreamTupleCreator
							.makeEmptyTuple(schArray_Result);
					for (ArrayList<byte[]> sc_result : c_result) {
						dest_result = ArrayListToByte(childID, sc_result,
								inputschArray);
						topdown_bufferResult(childID, dest_result,
								schArray_Result, sc_result, true, schArray);
					}

				}

			}

		}

	}

	/**
	 * convert a composite result to a byte array
	 * 
	 * @param queryID
	 * @param results
	 * @param inputschArray
	 * @return
	 */
	byte[] ArrayListToByte(int queryID, ArrayList<byte[]> results,
			SchemaElement[] inputschArray) {
		int eventTypeNum = Utility
				.getPositiveTypeNum(getQuery(queryID).stackTypes);

		SchemaElement[] schArray_Result = generateResultSchemas(eventTypeNum,
				inputschArray);

		byte[] dest = StreamTupleCreator.makeEmptyTuple(schArray_Result);

		int schIndex = 0;
		for (int arIndex = 0; arIndex < eventTypeNum; arIndex++) {
			StreamTupleCreator.tupleAppend(dest, results.get(arIndex),
					schArray_Result[schIndex].getOffset());
			schIndex += inputschArray.length;

		}
		return dest;
	}

	/**
	 * I should clear the buffer before adding new tuples in.
	 * 
	 * @param dest
	 * @param queryID
	 * @param schArray_Result
	 * @param inputschArray
	 */
	void bufferResult(ArrayList<byte[]> dest, int queryID,
			SchemaElement[] schArray_Result, SchemaElement[] inputschArray) {

		ArrayList<ArrayList<byte[]>> result = this.resultBuffers.get(queryID);
		boolean reuse = false;
		for (int check = 0; check < this.orderedQueries.length; check++) {
			if (this.orderedQueries[check][0].computeSourceID == queryID
					&& this.orderedQueries[check][0].queryID != queryID) {
				reuse = true;
				break;
			}
		}
		if (reuse)
			if (result != null) {
				this.resultBuffers.get(queryID).add(dest);
			} else {
				result = new ArrayList<ArrayList<byte[]>>();
				result.add(dest);
				this.resultBuffers.put(new Integer(queryID), result);
			}

	}

	/**
	 * extended to support negation type checking Needs further test. compute
	 * the relationships between two queries. return two boolean values. The
	 * first one indicate whether pattern hierarchy is changed. The second one
	 * indicate whether concept hierarchy is changed.
	 * 
	 * I have the assumption in mind that the given two queries already have
	 * certain relationships.
	 * 
	 * @param queryID1
	 *            upper query, has less event types. That is, the one exist on
	 *            queryID1 must exist on 2.
	 * @param queryID2
	 * @return pattern concept
	 * 
	 */
	boolean[] checkQueryPatternConceptR(int queryID1, int queryID2,
			ArrayList<String> middle, ArrayList<Integer> negatePos) {

		boolean pattern = false;
		boolean concept = false;
		boolean offNeg = false;
		boolean conceptNeg = false;

		if (queryID1 == queryID2) {
			boolean[] relations = { pattern, concept, offNeg, conceptNeg };
			return relations;

		} else {
			ArrayList<String> QueryTypes1 = getQuery(queryID1).stackTypes;
			ArrayList<String> QueryTypes2 = getQuery(queryID2).stackTypes;
			int matchCounter = 0;
			if (QueryTypes1.size() == QueryTypes2.size()) {

				for (int i = 0; i < QueryTypes1.size(); i++) {
					String type2 = QueryTypes2.get(i);
					String type1 = QueryTypes1.get(i);

					// extended to support negation
					if (type2.startsWith("-") && type1.startsWith("-")) {
						type2 = type2.substring(1, type2.length());
						type1 = type1.substring(1, type1.length());
						if (semanticMatch(type2, type1)
								| semanticMatch(type1, type2)) {
							conceptNeg = true;
						}
						negatePos.add(new Integer(i));

					}
					if (type1.equalsIgnoreCase(type2)) {
						matchCounter++;
						middle.add(type2);
						continue;
					} else {

						if (semanticMatch(type1, type2)) {
							middle.add(type2);
							matchCounter++;
							continue;
						} else if (semanticMatch(type2, type1)) {
							middle.add(type1);
							matchCounter++;
							continue;
						}
					}
				}
				if (matchCounter == QueryTypes1.size()) {
					concept = true;
				}

			} else if (Utility.getPositiveTypeNum(QueryTypes1) == QueryTypes1
					.size()
					&& Utility.getPositiveTypeNum(QueryTypes1) == Utility
							.getPositiveTypeNum(QueryTypes2)) {
				offNeg = true; // only the lower level query include negation
				pattern = true;

				ArrayList<Integer> rnegatePos = Utility
						.getNegativePos(QueryTypes2);
				for (int j = 0; j < rnegatePos.size(); j++) {
					negatePos.add(rnegatePos.get(j));
				}

			} else {

				pattern = true; // the length is not the same
				matchCounter = 0;
				int stringMatchingCounter = 0;
				int queryType1Index = 0;
				int queryType2Index = 0;
				while (queryType1Index < QueryTypes1.size()
						&& queryType2Index < QueryTypes2.size()) {
					if (QueryTypes1.get(queryType1Index).equalsIgnoreCase(
							QueryTypes2.get(queryType2Index))) {
						middle.add(QueryTypes2.get(queryType2Index));
						matchCounter++;
						stringMatchingCounter++;
						queryType1Index++;
						queryType2Index++;

					} else if (semanticMatch(QueryTypes1.get(queryType1Index),
							QueryTypes2.get(queryType2Index))) {
						matchCounter++;
						middle.add(QueryTypes2.get(queryType2Index));
						queryType1Index++;
						queryType2Index++;
					} else {
						queryType2Index++;
					}

				}

				if (stringMatchingCounter == QueryTypes1.size()) {
					concept = false;
				} else if (matchCounter == QueryTypes1.size()) {
					concept = true;
				}
				// if everything matches so for

			}
			boolean[] relations = { pattern, concept, offNeg, conceptNeg };
			return relations;

		}

	}

	// get memory consumption
	int getMemoryStatistics() {
		int buffersize = 0;

		// buffer size
		for (int iter = 0; iter < this.orderedQueries.length; iter++) {
			int queryID = this.orderedQueries[iter][0].getQueryID();
			ArrayList<ArrayList<byte[]>> result = this.resultBuffers
					.get(queryID);
			if (result != null) {
				buffersize += this.resultBuffers.get(queryID).size()
						* getQuery(queryID).getSize();
			}
		}
		// stack size
		for (int i = 0; i < this.AIS.length; i++) {
			if (AIS[i] != null) {
				buffersize += AIS[i].getSize();
			}
		}

		// System.out.println("memory consmption"+buffersize);
		return buffersize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.CHAOS.StreamOperator.StreamOperator#run(int)
	 */
	@Override
	public int run(int maxDequeueSize) {

		// orderedQueries = orderQueries();

		// the schema for all input tuples is the same.
		StreamQueue inputQueue = getInputQueueArray()[0];
		SchemaElement[] schArray = inputQueue.getSchema();

		if (initStatus == false) {
			init(schArray);
		}

		for (int i = maxDequeueSize; i > 0; i--) {
			long execution_Start = (new Date()).getTime();

			Configure.previousresultNum = Configure.resultNum;
			// first, tuple insertion with pointer set up
			byte[] tuple = inputQueue.dequeue();

			if (tuple == null)
				break;

			for (SchemaElement sch : schArray)
				sch.setTuple(tuple);

			double timestamp = StreamAccessor.getDoubleCol(tuple, schArray, 1);

			if (timestamp == 34196.159) {
				System.out.println("check");
			}

			// here, first apply the aggressive purge
			double expiringTimestamp = timestamp - Configure.windowsize;

			String eventType = Utility.getTupleType(tuple, schArray);

			// for latency measurement; set min time

			if (semanticcontains_notsensitive_position(Utility
					.lastQueryTypes(this.queries), eventType.toLowerCase()) >= 0) {
				StreamAccessor.setMinTimestamp(tuple, (new Date()).getTime());
			}

			// find out the stack index matching the tuple event type
			int stackIndex = findStack(eventType);

			if (stackIndex >= 0 && AIS[stackIndex] != null) {
				// use the stackType for storing the current
				// tuple instead of using the event type.

				// enqueue tuple and set up pointers.

				// pointer size should be greater than 1
				tuple_insertion(stackIndex, tuple, schArray);

				for (int iter = 0; iter < this.orderedQueries.length; iter++) {
					int checkQIndex = iter;

					if (this.orderedQueries[checkQIndex][0].computeSourceID == this.orderedQueries[checkQIndex][0].queryID) {
						// regular pointer join for self computation
						processQuery(checkQIndex, eventType, stackIndex, tuple,
								schArray);
					} else// compute from the upper level query
					{
						int upperqID = this.orderedQueries[checkQIndex][0].computeSourceID;
						int downqID = this.orderedQueries[checkQIndex][0].queryID;

						double visitedLastTime = -1;
						if (checkVisited(upperqID, downqID)) {
							visitedLastTime = this.existingResultNums.get(
									upperqID).get(downqID).doubleValue();

						}
						double lastTimestamp = lastVisitUpperQuery(upperqID,
								schArray);

						if (!this.resultBuffers.isEmpty()
								&& this.resultBuffers.containsKey(upperqID)) {
							//System.out.println("checking query" + downqID);
							

							// top down evaluation
							topdown_enqueueSubsequences(
									upperqID,
									this.orderedQueries[checkQIndex][0].queryID,
									this.resultBuffers.get(upperqID), schArray,
									visitedLastTime, tuple);

							recordVisitedBufferTuple(checkQIndex, lastTimestamp);

						}

					}

				}

			}

			// we actually delete tuples.
			if (expiringTimestamp >= 0) {
				// purge tuples with timestamp less than the expiring timestamp
				// purge tuple and reset the RIP pointer
				Utility.purgeStack(this.AIS, expiringTimestamp, schArray);
				for (int qi = 0; qi < this.queries.size(); qi++) {
					int queryID = this.queries.get(qi).queryID;
					Utility.purgeResultBuffer(expiringTimestamp,
							this.resultBuffers, schArray, queryID);
				}

			}

			// first run
			long executionTimeEnd = (new Date()).getTime();

			Configure.executionTime += executionTimeEnd - execution_Start;

			// test
			/*
			 * if (Configure.previousresultNum != Configure.resultNum)
			 * Utility.rewriteToFile("result" + " " + Configure.resultNum);
			 */

			// first
			if (Configure.previousresultNum != Configure.resultNum) {
				String write = Configure.resultNum + " "
						+ Configure.executionTime;

				System.out.println(write);

				resultCollection.add(write);

			}

			// second run-latency

			/*
			 * if (Configure.previousresultNum != Configure.resultNum) {
			 * System.out.println(Configure.latency + " " +
			 * Configure.resultNum);
			 * 
			 * }
			 */

			if (timestamp == 35148.521) {
				Utility.rewriteToFile(resultCollection);
				System.out.print("output");
			}

		}

		return 0;
	}

	/**
	 * return the last visited event's timestamp in the upper query
	 * 
	 * @param upperqID
	 * @param schArray
	 * @return
	 */
	protected double lastVisitUpperQuery(int upperqID, SchemaElement[] schArray) {
		double lastTimestamp = 0;
		if (!this.resultBuffers.isEmpty()
				&& this.resultBuffers.containsKey(upperqID)
				&& this.resultBuffers.get(upperqID).size() != 0) {
			ArrayList<byte[]> lastResult = this.resultBuffers.get(upperqID)
					.get(this.resultBuffers.get(upperqID).size() - 1);
			byte[] lastTuple = lastResult.get(lastResult.size() - 1);

			// record the results that visited previously in the
			// upper query
			lastTimestamp = StreamAccessor.getDoubleCol(lastTuple, schArray, 1);
		}
		return lastTimestamp;
	}

	/**
	 * check whether the lower query has visited the upper query before
	 * 
	 * @param upperqID
	 * @param downqID
	 * @return
	 */
	protected boolean checkVisited(int upperqID, int downqID) {
		if (!this.existingResultNums.isEmpty()
				&& this.existingResultNums.containsKey(upperqID)
				&& this.existingResultNums.get(upperqID).containsKey(downqID))
			return true;
		else
			return false;
	}

	/**
	 * Record buffer results visited in the last round
	 * 
	 * @param checkQIndex
	 *            the query index in the ordered query array
	 * @param lastTime
	 *            the last visited event's timestamp
	 */
	protected void recordVisitedBufferTuple(int checkQIndex, double lastTime) {
		int upperqID = this.orderedQueries[checkQIndex][0].computeSourceID;

		Hashtable<Integer, Double> existing = this.existingResultNums
				.get(new Integer(upperqID));
		if (existing != null) {
			existing.put(new Integer(
					this.orderedQueries[checkQIndex][0].queryID), new Double(
					lastTime));
			this.existingResultNums.put(new Integer(upperqID), existing);
		} else {
			existing = new Hashtable<Integer, Double>();
			existing.put(new Integer(
					this.orderedQueries[checkQIndex][0].queryID), new Double(
					lastTime));
			this.existingResultNums.put(new Integer(upperqID), existing);
		}
	}

	/**
	 * insert the tuple into the stack
	 * 
	 * @param stackIndex
	 * @param tuple
	 */
	protected void tuple_insertion(int stackIndex, byte[] tuple,
			SchemaElement[] schArray) {
		Hashtable<String, ArrayList<EdgeLabel>> table2 = hqp
				.getHierarchicalQueryPlan().get(
						AIS[stackIndex].stackType.toLowerCase());
		byte[][] pointerArray = null;
		if (table2 != null) {
			Set<String> set2 = table2.keySet();
			pointerArray = new byte[set2.size()][];
			Iterator<String> itr2 = set2.iterator();
			int pinterIndex = 0;
			while (itr2.hasNext()) {
				String event_type_pre = itr2.next();

				// find out the previous stack index matching the
				// tuple event type
				int prestackIndex = findStack(event_type_pre);

				if (AIS[prestackIndex] != null
						&& AIS[prestackIndex].eventQueue != null)
					pointerArray[pinterIndex++] = AIS[prestackIndex].eventQueue
							.peekLast();

			}
		}

		AIS[stackIndex].enqueue(tuple, pointerArray);
		/*
		 * byte[][] retPointerArrayTemp = new byte[2][];// change int index =
		 * StreamAccessor.getIndex(tuple);
		 * 
		 * byte[] tuple2 = AIS[stackIndex].getByPhysicalIndex(index,
		 * retPointerArrayTemp); double timestamp =
		 * StreamAccessor.getDoubleCol(tuple2, schArray, 1);
		 */

	}

	/**
	 * Process results if the incoming tulpe is a trigger tuple.
	 * 
	 * @param checkQIndex
	 *            the index for the query in ordered queries
	 * @param eventType
	 *            the incoming tuple
	 * @param stackIndex
	 *            the stack index for the incoming tulpe
	 * @param tuple
	 * @param schArray
	 */
	void processQuery(int checkQIndex, String eventType, int stackIndex,
			byte[] tuple, SchemaElement[] schArray) {

		ArrayList<String> stackTypes = orderedQueries[checkQIndex][0].stackTypes;
		String type = stackTypes.get(stackTypes.size() - 1);

		// Top down method

		// if the event type is one children of the trigger
		// type, it will also trigger the sequence construction for the
		// compensation. we need to check the HPG

		if (semanticMatch(type, eventType)) {
			int queryID = this.orderedQueries[checkQIndex][0].queryID;

			int stackTypeSize = 0;
			for (int i = 0; i < stackTypes.size(); i++) {
				if (!stackTypes.get(i).startsWith("-")) {
					stackTypeSize++;
				}
			}

			produceinorder_HStacks(queryID, AIS, stackIndex, tuple, schArray,
					stackTypeSize);

		}
	}

	/**
	 * determines the parents of all of the queries given to the system
	 * 
	 * @return
	 */
	protected QueryInfo[][] findParents() {
		// initialize array
		QueryInfo[][] queryArray = new QueryInfo[queries.size()][2];

		for (int i = 0; i < queryArray.length; i++) {
			queryArray[i][0] = queries.get(i);
			queryArray[i][1] = null;
		}

		QueryInfo possibleParent;
		// find parents
		for (int i = 0; i < queryArray.length; i++) {
			possibleParent = queryArray[i][0];
			for (int j = 0; j < queryArray.length; j++) {
				if (i != j) {
					if (ancestorMatch(possibleParent, queryArray[j][0])) {

						if (queryArray[j][1] == null) {
							queryArray[j][1] = possibleParent;
						} else if (ancestorMatch(queryArray[j][1],
								possibleParent)) {
							queryArray[j][1] = possibleParent;
						}
					}

				}
			}

		}

		return queryArray;
	}

	public ArrayList<QueryInfo> findAllDirectChildren(QueryInfo parent) {

		ArrayList<QueryInfo> directChildren = new ArrayList<QueryInfo>();
		boolean isDirect;

		for (int i = 0; i < queries.size(); i++) {
			isDirect = true;
			if (parent.getQueryID() != queries.get(i).getQueryID()) {
				if (ancestorMatch(parent, queries.get(i))) {
					if (!directChildren.contains(queries.get(i))) {
						for (int j = 0; j < directChildren.size(); j++) {
							if (ancestorMatch(directChildren.get(j), queries
									.get(i))) {
								isDirect = false;
							}
						}
						if (isDirect) {
							directChildren.add(queries.get(i));
						}
					}
				}
			}
		}

		return directChildren;
	}

	public ArrayList<QueryInfo> findAllDirectParents(QueryInfo child) {

		ArrayList<QueryInfo> directParents = new ArrayList<QueryInfo>();
		boolean isDirect;

		for (int i = 0; i < queries.size(); i++) {
			isDirect = true;
			if (child.getQueryID() != queries.get(i).getQueryID()) {
				if (ancestorMatch(queries.get(i), child)) {
					if (!directParents.contains(queries.get(i))) {
						for (int j = 0; j < directParents.size(); j++) {
							if (ancestorMatch(queries.get(i), directParents
									.get(j))) {
								isDirect = false;
							}
						}
						if (isDirect) {
							directParents.add(queries.get(i));
						}
					}
				}
			}
		}

		return directParents;
	}

	/**
	 * put queries in order using a breadth first search
	 * 
	 * @return ordered array of queries and their parents
	 */
	public QueryInfo[][] orderQueries() {
		QueryInfo[][] queryArray = findParents();

		ArrayList<QueryInfo> placed = new ArrayList<QueryInfo>();
		QueryInfo[][] orderedArray = new QueryInfo[queries.size()][2];

		int index = 0;

		// place root(s)
		for (int i = 0; i < queryArray.length; i++) {
			if (queryArray[i][1] == null) {
				orderedArray[index][0] = queryArray[i][0];
				orderedArray[index][1] = queryArray[i][1];
				index++;

				placed.add(queryArray[i][0]);
			}
		}

		// place other queries

		while (index < queryArray.length) {
			for (int i = 0; i < queryArray.length; i++) {
				if (!placed.contains(queryArray[i][0])) {
					if (placed.contains(queryArray[i][1])) {
						orderedArray[index][0] = queryArray[i][0];
						orderedArray[index][1] = queryArray[i][1];
						index++;

						placed.add(queryArray[i][0]);
					}
				}
			}

		}
		return orderedArray;
	}

	/**
	 * gets the direct children of the given query
	 * 
	 * @param parent
	 *            parent of children to be returned
	 * @return array list of children queries
	 */
	public ArrayList<QueryInfo> getChildren(QueryInfo parent) {
		ArrayList<QueryInfo> children = new ArrayList<QueryInfo>();

		for (int i = 0; i < orderedQueries.length; i++) {
			if (orderedQueries[i][1] != null) {
				if (orderedQueries[i][1].getQueryID() == parent.getQueryID()) {
					children.add(orderedQueries[i][0]);
				}
			}
		}
		return children;
	}

	/**
	 * gets the direct children of the query with the given query id
	 * 
	 * @param queryID
	 *            query id of parent query
	 * @return array list of children queries
	 */
	public ArrayList<QueryInfo> getChildren(int queryID) {
		ArrayList<QueryInfo> children = new ArrayList<QueryInfo>();

		for (int i = 0; i < orderedQueries.length; i++) {
			if (orderedQueries[i][1] != null) {
				if (orderedQueries[i][1].getQueryID() == queryID) {
					children.add(orderedQueries[i][0]);
				}
			}
		}
		return children;
	}

	/**
	 * gets the stack types of all of the children of the given query
	 * 
	 * @param parent
	 *            parent query
	 * @return array list of stack types
	 */
	public ArrayList<String> getChildrenStackTypes(QueryInfo parent) {
		ArrayList<String> stackTypes = new ArrayList<String>();
		ArrayList<QueryInfo> children = getChildren(parent);

		for (int i = 0; i < children.size(); i++) {
			stackTypes.addAll(children.get(i).getStackTypes());
		}
		return stackTypes;
	}

	/**
	 * gets the stack types of all of the children of the query with the given
	 * query id
	 * 
	 * @param queryId
	 *            id of parent query
	 * @return array list of stack types
	 */
	public ArrayList<String> getChildrenStackTypes(int queryID) {
		ArrayList<String> stackTypes = new ArrayList<String>();
		ArrayList<QueryInfo> children = getChildren(queryID);
		for (int i = 0; i < children.size(); i++) {
			stackTypes.addAll(children.get(i).getStackTypes());
		}
		return stackTypes;
	}

	/**
	 * determines the parent query of the given query
	 * 
	 * @param child
	 * @return
	 */
	public QueryInfo getParent(QueryInfo child) {
		int i;
		for (i = 0; i < orderedQueries.length; i++) {
			if (orderedQueries[i][0].getQueryID() == child.getQueryID()) {
				break;
			}
		}
		if (orderedQueries[i][1].getQueryID() == child.getQueryID()) {
			return orderedQueries[i][1];
		} else {
			return null;
		}
	}

	/**
	 * determines the parent query of the query with the given query id
	 * 
	 * @param queryID
	 *            query id of child query
	 * @return parent query
	 */
	public QueryInfo getParent(int queryID) {
		int i;
		for (i = 0; i < orderedQueries.length; i++) {
			if (orderedQueries[i][0].getQueryID() == queryID) {
				break;
			}
		}
		if (orderedQueries[i][0].getQueryID() == queryID) {
			return orderedQueries[i][1];
		} else {
			return null;
		}
	}

	/**
	 * determines the ancestors of the query with the given query id
	 * 
	 * @param queryID
	 *            id of descendant query
	 * @return array list of ancestor queries
	 */
	public ArrayList<QueryInfo> getAncestors(int queryID) {
		ArrayList<QueryInfo> ancestors = new ArrayList<QueryInfo>();
		QueryInfo current = getQuery(queryID);
		while (getParent(current.getQueryID()) != null) {
			if (!ancestors.contains(getParent(current.getQueryID()))) {
				ancestors.add(getParent(current.getQueryID()));
			}
			current = getParent(current.getQueryID());
		}

		return ancestors;
	}

	/**
	 * gets the stack types of the parent query of the given query
	 * 
	 * @param child
	 *            child query
	 * @return array ulist of stack types
	 */
	public ArrayList<String> getParentStackTypes(QueryInfo child) {
		QueryInfo parent = getParent(child);
		return orderedQueries[parent.getQueryID()][0].getStackTypes();
	}

	/**
	 * gets the query with the given query id
	 * 
	 * @param queryID
	 * @return query
	 */
	public QueryInfo getQuery(int queryID) {
		int i;
		for (i = 0; i < orderedQueries.length; i++) {
			if (orderedQueries[i][0].getQueryID() == queryID) {
				break;
			}
		}
		return orderedQueries[i][0];
	}

}
