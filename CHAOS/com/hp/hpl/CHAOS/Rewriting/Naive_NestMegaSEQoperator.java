package com.hp.hpl.CHAOS.Rewriting;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import java.awt.*;
import java.io.*;

import java.util.Hashtable;

import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamOperator.*;
import com.hp.hpl.CHAOS.Queue.SingleReaderEventQueueArrayImp;
import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;
import com.hp.hpl.CHAOS.StreamData.StreamTupleCreator;

public class Naive_NestMegaSEQoperator extends SingleInputStreamOperator {
//        JTextArea jTextArea1;
//
//    public JTextArea getjTextArea1() {
//        return jTextArea1;
//    }
//    @Override
//    public void setjTextArea1(JTextArea jTextArea1) {
//        this.jTextArea1 = jTextArea1;
//    }

	// test cost model
	// Hashtable<String, ArrayList<Integer>> stackSizeTesting = new
	// Hashtable<String, ArrayList<Integer>>();

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

	public Naive_NestMegaSEQoperator(int operatorID, StreamQueue[] input,
			StreamQueue[] output) {
		super(operatorID, input, output);

	}

	// It parses the query plan. set up the stack types as well
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

	/**
	 * similar as indexof in java but it is case insensitive
	 * 
	 * @param list
	 * @param astring
	 * @return
	 */
	int indexof_notcasesensitive(ArrayList<String> list, String astring) {
		int index = -1;
		index = list.indexOf(astring);
		if (index < 0) {
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).equalsIgnoreCase(astring)) {
					index = i;
					break;
				}

			}
		}
		return index;
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

	/**
	 * test whether one string is contained in another arrayList.
	 * 
	 * @param list
	 * @param astring
	 * @return
	 */
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

	public void setupExecutionOrderfornaive() {
		for (int i = 0; i < this.queries.size(); i++) {
			int currentqID = this.queries.get(i).queryID;
			this.queries.get(i).setComputeSourceID(currentqID);

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
	 * Given an event type, return the stack index it corresponds to
	 * 
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
			// compare the timestamp
			if (currenttimestamp > timestamp) {
				array.add(currenttuple);
				// Utility.simulatePredicatesCost();
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
	 * return the root query indice in queries
	 * 
	 * @return
	 */
	protected ArrayList<Integer> getRoots() {
		ArrayList<Integer> roots = new ArrayList<Integer>();
		int i = 0;
		for (; i < this.queries.size(); i++) {
			if (this.queries.get(i).parents.size() == 0)
				roots.add(new Integer(i));
		}

		return roots;

	}

	/**
	 * 
	 * @param tuple
	 * @param inputschArray
	 * @return the tuple event type
	 */
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

	int getPosition(String type, int queryID) {
		ArrayList<String> stackTypes = getQuery(queryID).stackTypes;
		return semanticcontains_notsensitive_position(stackTypes, type);
	}

	/**
	 * we assume each event type is only allowed to occur once
	 * 
	 * @param list
	 * @param astring
	 * @return
	 */
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
	 * tell whether one event type exists before another type in the query
	 * 
	 * @param currentType
	 * @param previousType
	 * @param queryID
	 * @return
	 */
	boolean isCurrentQuery(String currentType, String previousType, int queryID) {
		boolean isCurrentQ = false;
		int position = getPosition(currentType, queryID);
		int preposition = getPosition(previousType, queryID);

		if (position >= 0 && preposition >= 0 && preposition + 1 == position) {
			isCurrentQ = true;
		} else if (position >= 0
				&& preposition >= 0
				&& preposition + 2 == position
				&& getQuery(queryID).stackTypes.get(preposition + 1)
						.startsWith("-")) {
			isCurrentQ = true;
		}
		return isCurrentQ;
	}

	ArrayList<PreviousTuples> previousTuple_HStacks_negated(int queryID,
			EventActiveInstanceQueue[] stacks, int stackIndex,
			byte[] tempevent, SchemaElement[] inputschArray) {
		// double timestamp = StreamAccessor.getDoubleCol(tempevent,
		// inputschArray, 1);
		String type = getTupleType(tempevent, inputschArray);
		stackTypes = getQuery(queryID).stackTypes;

		ArrayList<String> negatedTypes = new ArrayList<String>();

		for (int j = 0; j < stackTypes.size(); j++) {
			String stype = stackTypes.get(j);
			if (stype.startsWith("-")) {
				stype = stype.substring(1, stype.length());
				negatedTypes.add(stype);
			}
		}

		ArrayList<PreviousTuples> previousTuples = new ArrayList<PreviousTuples>();

		// the type should be the stack type instead of event type.
		Hashtable<String, ArrayList<EdgeLabel>> table2 = this.hqp
				.getHierarchicalQueryPlan().get(
						AIS[findStack(type)].stackType.toLowerCase());
		boolean found = false;

		// For the given tempevent, we compute all the previous
		// positive/negative events.
		if (table2 != null) {
			int pointerSize = table2.keySet().size();
			byte[][] retPointerArrayTemp = new byte[pointerSize][];
			int index = StreamAccessor.getIndex(tempevent);

			stacks[stackIndex].getByPhysicalIndex(index, retPointerArrayTemp);
			byte[] previoustuple = null;

			ArrayList<byte[]> previouscurrentTuples = new ArrayList<byte[]>();
			ArrayList<byte[]> previousnegatedTuples = new ArrayList<byte[]>();
			// if the off event type is the last event type in the lower query,
			// we need to check
			String positivePreviousTypes = new String();
			String previousType = new String();
			ArrayList<String> previousPosiveTypes = new ArrayList<String>();
			double negationPointedtimestamp = -1;
			byte[] tuplebeforeNeg = null;
			for (int i = 0; i < pointerSize; i++) {
				// simulate the predicate evaluation cost
				previoustuple = retPointerArrayTemp[i];
				// double timestamp2 =
				// StreamAccessor.getDoubleCol(previoustuple,
				// inputschArray, 1);

				if (previoustuple != null) {
					previousType = getTupleType(previoustuple, inputschArray);

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
							if (isCurrentQuery(type, previousType, queryID)) {
								previouscurrentTuples.add(previoustuple);
								previousPosiveTypes.add(previousType);
								positivePreviousTypes = previousType;
							}

						}

					}

				}
			}

			/**
			 * OK. at this point, all positive tuples are there. I need to
			 * further check other existence requirement.
			 */

			// there are no negated tuples, all the current positive tuples
			// should be added
			if (previousnegatedTuples.size() == 0
					&& previouscurrentTuples.size() != 0) {

				for (int j = 0; j < previouscurrentTuples.size(); j++) {
					PreviousTuples e = new PreviousTuples(previouscurrentTuples
							.get(j), negationPointedtimestamp, tuplebeforeNeg);

					previousTuples.add(e);
				}
			} else if (previousnegatedTuples.size() != 0
					&& previouscurrentTuples.size() != 0) {
				for (int j = 0; j < previouscurrentTuples.size(); j++) {
					byte[] currentTuple = previouscurrentTuples.get(j);
					double currentTime = StreamAccessor.getDoubleCol(
							currentTuple, inputschArray, 1);

					// we set found = true between temp event and current tuple,
					// no negative events exist
					for (int i = 0; i < previousnegatedTuples.size(); i++) {

						byte[] negatedTuple = previousnegatedTuples.get(i);
						String pType = getTupleType(negatedTuple, inputschArray);

						String pStackType = AIS[findStack(pType)].stackType;
						int negatedindex = StreamAccessor
								.getIndex(negatedTuple);

						Hashtable<String, ArrayList<EdgeLabel>> negatedTable = this.hqp
								.getHierarchicalQueryPlan().get(
										pStackType.toLowerCase());

						if (negatedTable != null) {
							int negatedpointerSize = negatedTable.keySet()
									.size();
							byte[][] negatedPointerArrayTemp = new byte[negatedpointerSize][];
							stacks[findStack(pType)].getByPhysicalIndex(
									negatedindex, negatedPointerArrayTemp);

							for (int p = 0; p < negatedpointerSize; p++) {
								tuplebeforeNeg = negatedPointerArrayTemp[p];
								if (tuplebeforeNeg != null) {
									String tuplebeforeNegType = getTupleType(
											tuplebeforeNeg, inputschArray);

									if (semanticcontains_notsensitive_position(
											previousPosiveTypes,
											tuplebeforeNegType) >= 0) {
										negationPointedtimestamp = StreamAccessor
												.getDoubleCol(tuplebeforeNeg,
														inputschArray, 1);
										break;
									}

								}

							}

							if (negationPointedtimestamp >= 0
									&& negationPointedtimestamp < currentTime
									|| negationPointedtimestamp < 0) {

								found = true;

							} else if (negationPointedtimestamp >= 0
									&& negationPointedtimestamp >= currentTime) {
								found = false;
								break;
							}

						}

					}

					if (found == true) {
						PreviousTuples e = new PreviousTuples(currentTuple,
								negationPointedtimestamp, tuplebeforeNeg);
						ArrayList<Integer> fitQueriesID = new ArrayList<Integer>();
						fitQueriesID.add(queryID);
						e.setFitQueriesID(fitQueriesID);
						previousTuples.add(e);
					}
				}

			}

		}
		return previousTuples;

	}

	/**
	 * 
	 * @param queryID
	 * @param stacks
	 * @param stackIndex
	 * @param tempevent
	 * @param inputschArray
	 * @return the nearest tuple in the previous stack for the current incoming
	 *         tuple for a give query
	 */

	ArrayList<byte[]> previousTuple_HStacks(int queryID,
			EventActiveInstanceQueue[] stacks, int stackIndex,
			byte[] tempevent, SchemaElement[] inputschArray) {
		String type = getTupleType(tempevent, inputschArray);

		stackTypes = getQuery(queryID).stackTypes;

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
				// ok, spend sometime in finding the previous tuple
				// simulate the predicate evaluation cost
				// Utility.simulatePredicatesCost();

				if (previoustuple != null) {
					String previousType = getTupleType(previoustuple,
							inputschArray);

					String previousStackType = AIS[findStack(previousType)].stackType;
					// check in the HPG, the previousType - currenttype edge

					ArrayList<EdgeLabel> edges = table2.get(previousStackType);
					boolean found = false;

					for (int j = 0; edges != null && j < edges.size(); j++) {
						if (edges.get(j).queryID == queryID) {
							/*
							 * double timestampprevious = StreamAccessor
							 * .getDoubleCol(previoustuple, inputschArray, 1);
							 */// if (timestampprevious <= timestamp)
							{
								found = true;
								break;
							}

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
	 * @param queryID
	 * @return
	 */
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

		String nextType = getTupleType(nextTuple, inputschArray).toLowerCase()
				.toLowerCase();
		String stackType = stacks[k].stackType.toLowerCase();
		// use the stack type instead of the event type
		Hashtable<String, ArrayList<EdgeLabel>> table2 = this.hqp.hash1
				.get(stackType.toLowerCase());
		byte[][] retPointerArrayTemp = new byte[1][];
		if (table2 != null)
			retPointerArrayTemp = new byte[table2.keySet().size()][];

		int[] firstStacks = firstQueryTypes(queryID);

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

				ArrayList<PreviousTuples> previousTuples = previousTuple_HStacks_negated(
						queryID, stacks, k, tuple, inputschArray);
				for (int i = 0; i < previousTuples.size(); i++) {
					byte[] previousTuple = previousTuples.get(i)
							.getPreviousTuple();
					previousRIPindex = StreamAccessor.getIndex(previousTuple);

					int sIndex = findStack(getTupleType(previousTuple,
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

	byte[] converttoByteArrayNested(ArrayList<byte[]> ar2,
			SchemaElement[] schArray_Result, SchemaElement[] schArray, int size) {
		byte[] dest_result = StreamTupleCreator.makeEmptyTuple(schArray_Result);

		int schIndex_q = 0;
		for (int arIndex = 0; arIndex < size && arIndex < ar2.size(); arIndex++) {
			StreamTupleCreator.tupleAppend(dest_result, ar2.get(arIndex),
					schArray_Result[schIndex_q].getOffset());
			schIndex_q += schArray.length;

		}
		return dest_result;
	}

	/**
	 * get the previous stack type
	 * 
	 * @param eventType
	 * @return
	 */
	int getPreviousStackIndex(String eventType, ArrayList<String> stackTypes) {
		int previousIndex = -1;
		String previousType = new String();
		int curr_index = -1;

		for (int i = 0; i < stackTypes.size(); i++) {
			if (eventType.equalsIgnoreCase(stackTypes.get(i))) {
				curr_index = i;
				break;
			}
		}

		if (curr_index > 0) {
			previousType = stackTypes.get(curr_index - 1);
			previousIndex = findStack(previousType);
		}

		return previousIndex;

	}

	ArrayList<ArrayList<byte[]>> sequenceConstructionAnd(int queryID,
			int traverseStackIndex, int triggerStack,
			EventActiveInstanceQueue[] stacks, double leftTimebound,
			double rightTimebound, SchemaElement[] inputschArray) {

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
					previousStackIndex, triggerStack, stacks, leftTimebound,
					rightTimebound, inputschArray));
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

					if (tupleTimestamp <= rightTimebound
							&& tupleTimestamp >= leftTimebound) {
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

				if (tupleTimestamp >= leftTimebound
						&& tupleTimestamp <= rightTimebound) {
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
				if (currentTupleTimestamp >= leftTimebound
						&& currentTupleTimestamp <= rightTimebound) {
					earray3 = connectAnd(currenttuple, sequenceConstructionAnd(
							queryID, previousStackIndex, triggerStack, stacks,
							leftTimebound, rightTimebound, inputschArray));

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

	ArrayList<ArrayList<byte[]>> sequenceConstruction_Hstacks_negated(int k,
			int index, EventActiveInstanceQueue[] stacks, int queryID,
			SchemaElement[] inputschArray, byte[] nextTuple, double stopTime) {

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

		int[] firstStacks = firstQueryTypes(queryID);

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

			stackTypes = getQuery(queryID).stackTypes;
			ArrayList<String> offTypes = new ArrayList<String>();
			for (int j = 0; j < stackTypes.size(); j++) {
				if (stackTypes.get(j).startsWith("-")) {
					offTypes.add(stackTypes.get(j).substring(1,
							stackTypes.get(j).length()));
				}
			}

			String firstType = new String();

			firstType = getQuery(queryID).stackTypes.get(0);
			if (firstType.startsWith("-")) {
				firstType = firstType.substring(1, firstType.length());
			}

			if (offTypes.contains(firstType)) {
				int i = 0;

				for (; i < pointerSize; i++) {

					byte[] lowerfirsttuple = retPointerArrayTemp[i];
					if (lowerfirsttuple != null) {
						String tupleT = getTupleType(lowerfirsttuple,
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
							String tupleT = getTupleType(lowerfirsttuple,
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

			}

			return earray;
		} else {
			int previousRIPindex = 0;
			byte[] currenttuple = stacks[k].getByPhysicalIndex(index,
					retPointerArrayTemp);
			double timestamp = StreamAccessor.getDoubleCol(currenttuple,
					inputschArray, 1);

			while (currenttuple != null) {

				index = StreamAccessor.getIndex(currenttuple);

				ArrayList<PreviousTuples> previousTuples = previousTuple_HStacks_negated(
						queryID, stacks, k, currenttuple, inputschArray);
				for (int i = 0; i < previousTuples.size(); i++) {
					byte[] previousTuple = previousTuples.get(i)
							.getPreviousTuple();
					timestamp = StreamAccessor.getDoubleCol(previousTuple,
							inputschArray, 1);
					double stop = previousTuples.get(i).getStopTimestamp();

					previousRIPindex = StreamAccessor.getIndex(previousTuple);

					int sIndex = findStack(getTupleType(previousTuple,
							inputschArray));

					earray3 = connect(currenttuple,
							sequenceConstruction_Hstacks_negated(sIndex,
									previousRIPindex, stacks, queryID,
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
	 * AND operator
	 * 
	 * @param stacks
	 * @param tempevent
	 * @param inputschArray
	 * @param stackindex
	 * @param positiveTypeNum
	 */

	public void produceAnd(int queryID, EventActiveInstanceQueue[] stacks,
			byte[] tempevent, SchemaElement[] inputschArray,
			int triggerStackindex, int positiveTypeNum, double leftTimebound,
			double rightTimebound) {

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
				lastStackIndex, triggerStackindex, stacks, leftTimebound,
				rightTimebound, inputschArray);

		for (int k = 0; k < sc.size(); k++) {
			ArrayList<byte[]> ar2 = new ArrayList<byte[]>();

			for (int h = 0; h < sc.get(k).size(); h++) {

				ar2.add(sc.get(k).get(h));
			}

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
					inputschArray, leftTimebound);

		}
	}

	/**
	 * It produce the sequence results in a hierarchical stack for a single SEQ
	 * query triggered by one event.
	 * 
	 * @param stacks
	 * @param tempevent
	 */
	public void produceinorder_HStacks(int queryID,
			EventActiveInstanceQueue[] stacks, byte[] tempevent,
			SchemaElement[] inputschArray, double leftTimebound) {

		// test start
		/*
		 * {
		 * 
		 * double timestamptemp = StreamAccessor.getDoubleCol( tempevent,
		 * inputschArray, 1);
		 * 
		 * if (timestamptemp ==34394.707) {
		 * 
		 * System.out.println("bug place"); } }
		 */

		// test end

		String eventType = getTupleType(tempevent, inputschArray);
		// find out the stack index matching the tuple event type
		int stackIndex = findStack(eventType);

		stackTypes = getQuery(queryID).stackTypes;

		if (stackTypes.size() == 1
				&& stackTypes.get(0).equalsIgnoreCase(eventType)) {

			ArrayList<byte[]> ar2 = new ArrayList<byte[]>();
			ar2.add(tempevent);

			// buffer the tempevent
			bufferResult(queryID, tempevent, inputschArray, ar2, true,
					inputschArray, leftTimebound);
			return;

		}

		int eventTypeNum = 0;
		for (int i = 0; i < stackTypes.size(); i++) {
			if (!stackTypes.get(i).startsWith("-")) {
				eventTypeNum++;
			}
		}

		ArrayList<PreviousTuples> previousTuples = previousTuple_HStacks_negated(
				queryID, stacks, stackIndex, tempevent, inputschArray);

		for (int i = 0; i < previousTuples.size(); i++) {
			byte[] previousTuple = previousTuples.get(i).getPreviousTuple();

			// test start

			/*
			 * double timestampprevious = StreamAccessor.getDoubleCol(
			 * previousTuple, inputschArray, 1); if(timestampprevious ==
			 * 34391.409) { System.out.println("bug place"); }
			 */
			// test end

			double stopTime = previousTuples.get(i).getStopTimestamp();
			String type = getTupleType(previousTuple, inputschArray)
					.toLowerCase();
			int prevousStackIndex = findStack(type);

			int previousRIPindex = StreamAccessor.getIndex(previousTuple);

			ArrayList<ArrayList<byte[]>> sc = sequenceConstruction_Hstacks_negated(
					prevousStackIndex, previousRIPindex, stacks, queryID,
					inputschArray, tempevent, stopTime);

			for (int k = 0; k < sc.size(); k++) {
				ArrayList<byte[]> ar2 = new ArrayList<byte[]>();

				for (int h = 0; h < sc.get(k).size(); h++) {

					ar2.add(sc.get(k).get(h));
				}
				ar2.add(tempevent);

				// compute the first event's timestamp
				double timestamp = StreamAccessor.getDoubleCol(ar2.get(0),// why
						// first
						inputschArray, 1);
				if (timestamp > leftTimebound) {
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

					// test start

					/*
					 * if (ar2.size() == 3) { byte[] firstTuple = ar2.get(0);
					 * double timestampFirst = StreamAccessor
					 * .getDoubleCol(firstTuple, inputschArray, 1);
					 * 
					 * byte[] secondTuple = ar2.get(1); double timestampSecond =
					 * StreamAccessor .getDoubleCol(secondTuple, inputschArray,
					 * 1);
					 * 
					 * byte[] lastTuple = ar2.get(2); double timestampLast =
					 * StreamAccessor.getDoubleCol( lastTuple, inputschArray,
					 * 1);
					 * 
					 * if (timestampSecond == 34391.409 && timestampLast ==
					 * 34394.707) {
					 * 
					 * System.out.println("bug place"); } }
					 */
					// test end

					boolean reuse = true;
					bufferResult(queryID, dest, schArray_Result, ar2, reuse,
							inputschArray, leftTimebound);
				}

			}

		}

	}

	public void bufferResult(int queryID, byte[] dest,
			SchemaElement[] schArray_Result, ArrayList<byte[]> ar2,
			boolean reuseresults, SchemaElement[] schArray, double leftTimebound) {
		boolean withinWindow = false;
		QueryInfo qiInfo = getQuery(queryID);

		if (qiInfo.getOperatorType().equalsIgnoreCase("SEQ")) {
			if (Utility.windowOpt(ar2, schArray)) {
				withinWindow = true;

			}
		} else if (qiInfo.getOperatorType().equalsIgnoreCase("AND")) {
			if (Utility.windowOptAnd(ar2, schArray)) {
				withinWindow = true;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.CHAOS.StreamOperator.StreamOperator#run(int)
	 */
	@Override
	public int run(int maxDequeueSize) {
		
		int MemorySize = 0;

		//Print headers
		System.out.println("ExecTime\tTuplesPrcss\tResult");
		long tuplesProcessed = 0;

		// Configure.previousresultNum = Configure.resultNum;

		// the schema for all input tuples is the same.
		StreamQueue inputQueue = getInputQueueArray()[0];
		SchemaElement[] schArray = inputQueue.getSchema();

		if (initStatus == false) {
			init(schArray);
		}

		for (int i = maxDequeueSize; i > 0; i--) {

			Configure.inputConsumed++;
			tuplesProcessed++;

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

			/*if (timestamp == 34200.36) {
				System.out.print("check");
			}*/

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
				MemorySize++;

				ArrayList<Integer> rootIndex = getRoots();
				// process each query independently
				for (int qIndex = 0; qIndex < rootIndex.size(); qIndex++) {
					ArrayList<String> stackTypes = this.queries.get(rootIndex
							.get(qIndex)).stackTypes;

					String type = stackTypes.get(stackTypes.size() - 1);

					// compute(root qi), for root, we still require tuple is of
					// the
					// last event type
					if (eventType.equalsIgnoreCase(type)) {
						processQuery(rootIndex.get(qIndex), tuple, schArray);
					}
				}

			}

			// here, we actually delete tuples.
			if (expiringTimestamp >= 0) {
				// purge tuples with timestamp less than the expiring timestamp
				// purge tuple and reset the RIP pointer
				int purgedTuples = Utility.purgeStack(this.AIS, expiringTimestamp, schArray);
				MemorySize = MemorySize - purgedTuples;
				System.out.println("Memory Usage:" + "\t\t" + MemorySize);

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
			/*if (Configure.previousresultNum != Configure.resultNum) {
				String write = Configure.resultNum + " "
						+ Configure.executionTime + " "
						+ Configure.totalChildren + " "
						+ Configure.timeComputeChildren + " "
						+ Configure.timeJoiningResult;
//                                StringBuilder sb = new StringBuilder(this.getJTextArea1().getText());
//                                this.getJTextArea1().setText(sb.append(write).toString());

				System.out.println(write);
				if(Configure.resultNum == 52)
					System.out.print("");
                                try {
                                        FileWriter fstream = new FileWriter("Iterative.txt", false);
                                        BufferedWriter out = new BufferedWriter(fstream);

                                           out.write(write);
					// Close the output stream
                                           out.close();
                                    } catch (IOException e) {
			// TODO Auto-generated catch block
                                    e.printStackTrace();
                                  }*/
                                


				// test start
				// if (Configure.resultNum == 859)
				// System.out.println("bug point");

				// test end
				//resultCollection.add(write);

				//System.out.println(timestamp + " " + Configure.executionTime
				 //+ " " + Configure.resultNum);
			}

			// three dimension statistics
			// String write = Configure.inputConsumed + " " +
			// Configure.executionTime + " " + Configure.resultNum;
			// resultCollection.add(write);

			//if (timestamp == 35148.521) {
//				System.out.println("output");
				//Utility.rewriteToFile(resultCollection);

				// cost model test start

				/*
				 * Set<Integer> set12 = resultBuffers.keySet();i
				 * 
				 * Iterator<Integer> itr12 = set1.iterator();
				 * 
				 * while (itr12.hasNext()) { Integer str = itr12.next();
				 * System.out.println("id " + str);
				 * 
				 * ArrayList<ArrayList<byte[]>> results =
				 * resultBuffers.get(str);
				 * 
				 * System.out.println("size " + ": " + results.size()); }
				 * 
				 * 
				 * 
				 * Set<String> set = stackSizeTesting.keySet();
				 * 
				 * Iterator<String> itr = set.iterator();
				 * 
				 * while (itr.hasNext()) { String str = itr.next();
				 * System.out.println("type " + str);
				 * 
				 * ArrayList<Integer> sizes = stackSizeTesting.get(str); int sum
				 * = 0; for (int si = 0; si < sizes.size(); si++) { sum +=
				 * sizes.get(si); } int ave = sum / sizes.size();
				 * System.out.println("size " + ": " + ave); }
				 */// cost model test end

			
			/*
			 * //second chart with x- execution time; y - memory used int
			 * memoryConsumption = getMemoryStatistics();
			 * System.out.println("=======Memory Consumption===========" +
			 * memoryConsumption);
			 */
		
		System.out.println(Configure.executionTime + "\t\t" + tuplesProcessed);
		return 0;
		
	}

	// get memory consumption
	int getMemoryStatistics() {
		int buffersize = 0;
		// stack size
		for (int i = 0; i < this.AIS.length; i++) {
			if (AIS[i] != null) {
				buffersize += AIS[i].getSize();
			}
		}
		return buffersize;
	}

	/**
	 * insert the tuple into the stack
	 * 
	 * @param stackIndex
	 * @param tuple
	 */
	protected void tuple_insertion(int stackIndex, byte[] tuple) {
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

		AIS[stackIndex].enqueue(tuple, pointerArray);// default null
	}

	/**
	 * binary search
	 * 
	 * @param sortingList
	 * @param tempevent
	 * @return
	 */

	public static long binarySearch(SingleReaderEventQueueArrayImp eventQueue,
			byte[] tempevent, SchemaElement[] schArray) {
		long low = 0;
		long high = eventQueue.getSize() - 1;
		long mid;

		long position = 0;
		if (high < 0)
			return 0;
		while (low <= high) {
			mid = (low + high) / 2;

			byte[] checkingTuple = eventQueue.get((int) mid);
			double timestamp = StreamAccessor.getDoubleCol(checkingTuple,
					schArray, 1);
			double tempstamp = StreamAccessor.getDoubleCol(tempevent, schArray,
					1);

			byte[] checkingTuple_plus1 = eventQueue.get((int) mid + 1);
			double timestamp_plus1 = StreamAccessor.getDoubleCol(
					checkingTuple_plus1, schArray, 1);

			if (timestamp < tempstamp)
				low = mid + 1;
			else if (timestamp > tempstamp)
				high = mid - 1;
			else if (mid + 1 <= eventQueue.getSize() - 1
					&& timestamp <= tempstamp && timestamp_plus1 >= tempstamp) {
				position = mid;
				break;
			} else {
				position = mid + 1;
				break;
			}
		}
		if (low > high) {
			position = low;
		}
		return position;

	}

	/**
	 * @param childID
	 * @param cstackTypes
	 * @param result
	 * @param nestPos
	 * @param inputschArray
	 */
	protected ArrayList<ArrayList<byte[]>> computeChildrenQueries(int childID,
			ArrayList<String> cstackTypes, ArrayList<byte[]> result,
			int nestPos, SchemaElement[] inputschArray) {

		ArrayList<ArrayList<byte[]>> childBuffer = new ArrayList<ArrayList<byte[]>>();

		ArrayList<ArrayList<byte[]>> tempchildBuffer = new ArrayList<ArrayList<byte[]>>();

		double timestampl = 0;

		double timestampr = 0;

		if (nestPos >= 1) {
			timestampl = StreamAccessor.getDoubleCol(result.get(nestPos - 1),
					inputschArray, 1);

			if (nestPos == result.size()) {
				double firstEvent = StreamAccessor.getDoubleCol(result.get(0),
						inputschArray, 1);

				timestampr = firstEvent + Configure.windowsize;
			} else {
				timestampr = StreamAccessor.getDoubleCol(result.get(nestPos),
						inputschArray, 1);
			}
		} else if (nestPos == 0)

		{
			timestampr = StreamAccessor.getDoubleCol(result.get(nestPos),
					inputschArray, 1);
			double lastEvent = StreamAccessor.getDoubleCol(result.get(result
					.size() - 1), inputschArray, 1);

			timestampl = lastEvent - Configure.windowsize;

		}

		// use the right interval to search for a particular
		// stack portion
		// use the left interval to check qualified events
		String lastcType = cstackTypes.get(cstackTypes.size() - 1);
		int lastcIndex = findStack(lastcType);

		byte[] tu = Utility.binarySearch_rightbounds(AIS[lastcIndex],
				timestampr, inputschArray);

		byte[][] retPointerArrayTemp = new byte[2][];

		// I should remember how to traverse an event queue.

		double t = StreamAccessor.getDoubleCol(tu, inputschArray, 1);
		int tupleIndex;

		// small adjustment.
		// need debug binary search right bound
		if (t > timestampr) {// I give you another chance to see
			tupleIndex = StreamAccessor.getIndex(tu);

			tu = AIS[lastcIndex].getPreviousByPhysicalIndex(tupleIndex,
					retPointerArrayTemp);
			t = StreamAccessor.getDoubleCol(tu, inputschArray, 1);
		}

		// only triggering events exist between the interval
		while (tu != null && t >= timestampl && t <= timestampr) {

			tupleIndex = StreamAccessor.getIndex(tu);

			tempchildBuffer = compute(childID, AIS, tu, inputschArray,
					timestampl, timestampr);

			// only store distinct results
			if (tempchildBuffer != null) {
				for (int i = 0; i < tempchildBuffer.size(); i++) {
					if (!childBuffer.contains(tempchildBuffer.get(i))) {
						childBuffer.add(tempchildBuffer.get(i));
					}

				}

			}

			tu = AIS[lastcIndex].getPreviousByPhysicalIndex(tupleIndex,
					retPointerArrayTemp);
			t = StreamAccessor.getDoubleCol(tu, inputschArray, 1);

		}

		return childBuffer;
	}

	/**
	 * 
	 * @param qi
	 *            query ID
	 * @param stacks
	 *            AIS
	 * @param tempevent
	 *            triggering event
	 * @param inputschArray
	 *            schema
	 * @param leftTimebound
	 *            the left time bound for results
	 * @return qi results
	 */

	protected ArrayList<ArrayList<byte[]>> compute(int qi,
			EventActiveInstanceQueue[] stacks, byte[] tempevent,
			SchemaElement[] inputschArray, double leftTimebound,
			double rightTimebound) {

		String triggeringEventType = getTupleType(tempevent, inputschArray);
		// find out the stack index matching the tuple event type
		int triggeringStackIndex = findStack(triggeringEventType);

		ArrayList<ArrayList<byte[]>> qiResult = new ArrayList<ArrayList<byte[]>>();

		Hashtable<Integer, ArrayList<ArrayList<byte[]>>> childrenBuffers = new Hashtable<Integer, ArrayList<ArrayList<byte[]>>>();

		ArrayList<ArrayList<byte[]>> childBuffer = new ArrayList<ArrayList<byte[]>>();

		QueryInfo qiInfo = getQuery(qi);

		ArrayList<String> qiStackTypes = qiInfo.getStackTypes();

		int posEventTypeNum = 0;
		for (int i = 0; i < qiStackTypes.size(); i++) {
			if (!qiStackTypes.get(i).startsWith("-")) {
				posEventTypeNum++;
			}
		}

		ArrayList<childQueryInfo> children = qiInfo.children;

		ArrayList<parentQueryInfo> parents = qiInfo.parents;

		// temporal buffer for joining internal nodes
		Hashtable<Integer, ArrayList<ArrayList<byte[]>>> tempBuffers = new Hashtable<Integer, ArrayList<ArrayList<byte[]>>>();

		if (children.size() != 0) {

			int processedNum = 0;
			// I should remember how many outer results have been processed.
			if (this.resultBuffers.get(qi) != null) {
				processedNum = this.resultBuffers.get(qi).size();
			}
			

			// compute for events in one level
			// check operator type first
			if (qiInfo.getOperatorType().equalsIgnoreCase("SEQ"))
				produceinorder_HStacks(qi, stacks, tempevent, inputschArray,
						leftTimebound);
			else if (qiInfo.getOperatorType().equalsIgnoreCase("AND")) {
				produceAnd(qi, stacks, tempevent, inputschArray,
						triggeringStackIndex, posEventTypeNum, leftTimebound,
						rightTimebound);

			}

			// if qi has no children queries, then qi results are the stack
			// based join results

			if (children.size() == 0) {
				qiResult = this.resultBuffers.get(qi);
				return qiResult;
			}
			// if qi has children queries, then qi results = qi stack based
			// results join children query 1, ... join children query i.

			else if (this.resultBuffers.get(qi) != null) {
				long totalChildrenStart = 0;
				if (qi == 1) {
					totalChildrenStart = (new Date()).getTime();
				}

				// for each of its result, we pass down intervals for children
				{

					// System.out.println(" size " +
					// this.resultBuffers.get(qi).size());
					for (int i = processedNum; i < this.resultBuffers.get(qi)
							.size(); i++) {
						// System.out.println("iteration" + i);

						ArrayList<byte[]> result = this.resultBuffers.get(qi)
								.get(i);

						// test start

						/*if (result.size() == 3) {
							byte[] firstTuple = result.get(0);
							double timestampFirst = StreamAccessor
									.getDoubleCol(firstTuple, inputschArray, 1);

							byte[] secondTuple = result.get(1);
							double timestampSecond = StreamAccessor
									.getDoubleCol(secondTuple, inputschArray, 1);

							byte[] lastTuple = result.get(2);
							double timestampLast = StreamAccessor.getDoubleCol(
									lastTuple, inputschArray, 1);

							if (timestampFirst == 34197.793
									&& timestampSecond == 34200.887
									&& timestampLast == 34201.297) {

								System.out.println("bug place");
							}
						}*/

						// test end

						// computing children queries

						long execution_Start_child = 0;
						if (qi == 1) {
							execution_Start_child = (new Date()).getTime();

						}

						for (int childIndex = 0; childIndex < children.size(); childIndex++) {
							int childID = children.get(childIndex).getChildID();
							ArrayList<String> cstackTypes = getQuery(childID).stackTypes;
							int nestPos = children.get(childIndex)
									.getNestedposition();

							// nestPos > 1, between nestPos th and nestPos + 1
							// th events
							childBuffer = computeChildrenQueries(childID,
									cstackTypes, result, nestPos, inputschArray);

							// some optimization, should I apply them now?
							// if a child query has no results, and it is a
							// positive child, I skip this run

							// if a child query has no results, and it is a
							// negative child, I continue this run

							// if a child query has results, and it is a
							// positive child, I store the results

							// if a child query has results, and it is a
							// negative child, I skip this run

							if (childBuffer != null)
								childrenBuffers.put(new Integer(childID),
										childBuffer); // test

						}

						if (qi == 1) {
							long executionTimeEnd_child = (new Date())
									.getTime();

							Configure.timeComputeChildren += executionTimeEnd_child
									- execution_Start_child;

						}

						long execution_Start_join = 0;

						if (qi == 1) {
							execution_Start_join = (new Date()).getTime();

						}

						// join for positive
						// stop evaluation for negative

						String mixedType = qiInfo.getMixType();
						
						//

						// proceed as before;
						if (mixedType.equalsIgnoreCase("SEQ")) {
							// join results together
							// each partial outer result should be connected
							// with
							// each computed child result
							{
								boolean stop = false; // stop sequence
								// evaluation
								int lastcID = 0;
								int accumChildSize = 0;
								for (int childIndex = 0; childIndex < children
										.size(); childIndex++) {
									if (stop == true)
										break;
									int cID = children.get(childIndex)
											.getChildID();

									int type = children.get(childIndex)
											.getPositiveComponent();
									int nestPos = children.get(childIndex)
											.getNestedposition();

									if (childIndex == 0) {
										if (type == 1) {
											// join results for positive
											// sub-query
											lastcID = cID;

											ArrayList<ArrayList<byte[]>> cresult = new ArrayList<ArrayList<byte[]>>();
											ArrayList<ArrayList<byte[]>> bufferer = childrenBuffers
													.get(cID);

											if (qiInfo.getOperatorType()
													.equalsIgnoreCase("SEQ")) {
												cresult = Utility
														.connect_bytime_SEQ(
																result,
																bufferer,
																nestPos,
																inputschArray);
											} else if (qiInfo.getOperatorType()
													.equalsIgnoreCase("AND")) {
												cresult = Utility
														.connect_bytime_AND(
																result,
																bufferer,
																nestPos,
																inputschArray);
											}

											ArrayList<ArrayList<byte[]>> temp = tempBuffers
													.get(cID);

											if (temp != null) {

												tempBuffers.get(cID).addAll(
														cresult);

											} else {
												temp = new ArrayList<ArrayList<byte[]>>();
												temp.addAll(cresult);
												tempBuffers.put(
														new Integer(cID),
														cresult);

											}
										} else {
											ArrayList<ArrayList<byte[]>> bufferedTuple = childrenBuffers
													.get(cID);
											if (bufferedTuple != null
													&& bufferedTuple.size() != 0) {
												stop = true;
												break;
											} else // for future joining
											// results, I
											// should copy the existing
											// partial results to its
											// tempBuffers.
											{
												// the current child is not the
												// last
												// child.
												if (childIndex + 1 < children
														.size()) {
													ArrayList<ArrayList<byte[]>> cresult = new ArrayList<ArrayList<byte[]>>();
													cresult.add(result);
													tempBuffers.put(
															new Integer(cID),
															cresult);
												}

											}

										}

									} else {
										// adjust joining index after adding
										// children partial results

										// only adjusting the nestPos when the
										// previous joined child query is
										// positive
										if (children.get(childIndex - 1)
												.getPositiveComponent() == 1) {
											accumChildSize += getQuery(children
													.get(childIndex - 1)
													.getChildID()).stackTypes
													.size();

											nestPos += accumChildSize;

										}

										for (int pI = 0; tempBuffers != null
												&& tempBuffers.size() > 0
												&& tempBuffers.get(children
														.get(childIndex - 1)
														.getChildID()) != null
												&& pI < tempBuffers.get(
														children.get(
																childIndex - 1)
																.getChildID())
														.size(); pI++) {
											result = tempBuffers
													.get(
															children
																	.get(
																			childIndex - 1)
																	.getChildID())
													.get(pI);

											if (type == 1) {
												lastcID = cID;
												ArrayList<ArrayList<byte[]>> c_result = new ArrayList<ArrayList<byte[]>>();
												ArrayList<ArrayList<byte[]>> bufferedTuple = childrenBuffers
														.get(cID);

												if (qiInfo
														.getOperatorType()
														.equalsIgnoreCase("SEQ")) {
													c_result = Utility
															.connect_bytime_SEQ(
																	result,
																	bufferedTuple,
																	nestPos,
																	inputschArray);
												} else if (qiInfo
														.getOperatorType()
														.equalsIgnoreCase("AND")) {
													c_result = Utility
															.connect_bytime_AND(
																	result,
																	bufferedTuple,
																	nestPos,
																	inputschArray);
												}

												ArrayList<ArrayList<byte[]>> tempresult = tempBuffers
														.get(cID);

												if (tempresult != null) {

													tempBuffers.get(cID)
															.addAll(c_result);

												} else {

													tempBuffers.put(
															new Integer(cID),
															c_result);
												}

											} else if (type == 0)// negative,
											// filter
											// results
											{

												ArrayList<ArrayList<byte[]>> bufferedTuple = childrenBuffers
														.get(cID);
												if (bufferedTuple != null
														&& bufferedTuple.size() != 0) {
													stop = true;
													break;
												} else // for further joining
												// results, put all the
												// previous joining results
												// in
												{

													// the current child is not
													// the
													// last child.
													if (childIndex + 1 < children
															.size()) {
														ArrayList<ArrayList<byte[]>> tempresult = tempBuffers
																.get(cID);

														if (tempresult != null) {
															tempresult.clear();

															tempBuffers
																	.get(cID)
																	.addAll(
																			tempBuffers
																					.get(children
																							.get(
																									childIndex - 1)
																							.getChildID()));

														} else {

															tempBuffers
																	.put(
																			new Integer(
																					cID),
																			tempBuffers
																					.get(children
																							.get(
																									childIndex - 1)
																							.getChildID()));
														}

													}

												}
											}
										}

									}

								}

								if (qi == 1) {
									long execution_End_join = (new Date())
											.getTime();

									Configure.timeJoiningResult += execution_End_join
											- execution_Start_join;

								}

								// store results to file for the root query or
								// store
								// qiResult to buffer for child queries
								if (stop != true) {

									if (lastcID > 0
											&& tempBuffers.get(lastcID) != null) {

										for (ArrayList<byte[]> iresult : tempBuffers
												.get(lastcID)) {

											byte[] dest = converttoByteArrayNested(
													iresult,
													generateResultSchemas(
															getNestedStackNum(qi),
															inputschArray),
													inputschArray,
													getNestedStackNum(qi));

											if (parents.size() == 0) {
												Configure.resultNum++;

												Utility
														.rewriteToFile("====== result======"
																+ qi);
												Utility
														.rewriteToFile(StreamAccessor
																.toString(
																		dest,
																		generateResultSchemas(
																				getNestedStackNum(qi),
																				inputschArray)));

												com.hp.hpl.CHAOS.Queue.Utility
														.enqueueGroup(
																OutputQueueArray,
																dest);

											} else {
												qiResult.add(iresult);
											}

										}
									} else if (lastcID == 0)
									// only one child query/no result? test
									{
										// test window
										if (Utility.windowOpt(result,
												inputschArray)) {

											SchemaElement[] schArray_Result = generateResultSchemas(
													getNestedStackNum(qi),
													inputschArray);

											byte[] dest = converttoByteArray(
													result, schArray_Result,
													inputschArray, qi);

											if (parents.size() == 0) {
												Configure.resultNum++;

												Utility
														.rewriteToFile("====== result======"
																+ qi);
												Utility
														.rewriteToFile(StreamAccessor
																.toString(dest,
																		schArray_Result));

												com.hp.hpl.CHAOS.Queue.Utility
														.enqueueGroup(
																OutputQueueArray,
																dest);

											} else {
												qiResult.add(result);
											}

										}

									}

								}

							}
						} else {

							// the type is OR, we only need to join each child
							// query result with qi result.
							{
								boolean stop = false; // stop sequence
								// evaluation
								int lastcID = 0;

								for (int childIndex = 0; childIndex < children
										.size(); childIndex++) {
									if (stop == true)
										break;
									int cID = children.get(childIndex)
											.getChildID();

									int type = children.get(childIndex)
											.getPositiveComponent();
									int nestPos = children.get(childIndex)
											.getNestedposition();

									// if (childIndex == 0)
									{
										if (type == 1) {
											// join results for positive
											// sub-query
											lastcID = cID;

											ArrayList<ArrayList<byte[]>> cresult = new ArrayList<ArrayList<byte[]>>();
											ArrayList<ArrayList<byte[]>> bufferer = childrenBuffers
													.get(cID);

											if (qiInfo.getOperatorType()
													.equalsIgnoreCase("SEQ")) {
												cresult = Utility
														.connect_bytime_SEQ(
																result,
																bufferer,
																nestPos,
																inputschArray);
											} else if (qiInfo.getOperatorType()
													.equalsIgnoreCase("AND")) {
												cresult = Utility
														.connect_bytime_AND(
																result,
																bufferer,
																nestPos,
																inputschArray);
											}

											ArrayList<ArrayList<byte[]>> temp = tempBuffers
													.get(cID);

											if (temp != null) {

												tempBuffers.get(cID).addAll(
														cresult);

											} else {
												temp = new ArrayList<ArrayList<byte[]>>();
												temp.addAll(cresult);
												tempBuffers.put(
														new Integer(cID),
														cresult);

											}
										} else {
											ArrayList<ArrayList<byte[]>> bufferedTuple = childrenBuffers
													.get(cID);
											if (bufferedTuple != null
													&& bufferedTuple.size() != 0) {
												stop = true;
												break;
											} else // for future joining
											// results, I
											// should copy the existing
											// partial results to its
											// tempBuffers.
											{
												// the current child is not the
												// last
												// child.
												if (childIndex + 1 < children
														.size()) {
													ArrayList<ArrayList<byte[]>> cresult = new ArrayList<ArrayList<byte[]>>();
													cresult.add(result);
													tempBuffers.put(
															new Integer(cID),
															cresult);
												}

											}

										}

									}

								}

								if (qi == 1) {
									long execution_End_join = (new Date())
											.getTime();

									Configure.timeJoiningResult += execution_End_join
											- execution_Start_join;

								}

								// store results to file for the root query or
								// store
								// qiResult to buffer for child queries
								if (stop != true) {

									// if (lastcID > 0)

									{

										for (int childIndex = 0; childIndex < children
												.size(); childIndex++) {
											int cID = children.get(childIndex)
													.getChildID();
											if (tempBuffers.get(cID) != null) {
												for (ArrayList<byte[]> iresult : tempBuffers
														.get(cID)) {

													if (Utility.windowOpt(
															iresult,
															inputschArray)) {
														byte[] dest = converttoByteArrayNested(
																iresult,
																generateResultSchemas(
																		getNestedStackNum(
																				qi,
																				cID),
																		inputschArray),
																inputschArray,
																getNestedStackNum(qi));

														if (parents.size() == 0) {
															Configure.resultNum++;

															Utility
																	.rewriteToFile("====== result======"
																			+ cID);
															Utility
																	.rewriteToFile(StreamAccessor
																			.toString(
																					dest,
																					generateResultSchemas(
																							getNestedStackNum(
																									qi,
																									cID),
																							inputschArray)));

															com.hp.hpl.CHAOS.Queue.Utility
																	.enqueueGroup(
																			OutputQueueArray,
																			dest);

														} else {
															qiResult
																	.add(iresult);
														}
													}

												}
											}
										}

									}

								}

							}

						}

						// clear result Buffer
						for (int childIndex = 0; childIndex < children.size(); childIndex++) {
							int cID = children.get(childIndex).getChildID();
							if (this.resultBuffers.get(cID) != null)
								this.resultBuffers.get(cID).clear();
							// clear tempBuffer
							if (tempBuffers.get(cID) != null)
								tempBuffers.get(cID).clear();
						}

						// clear children buffer, as each outer result compute
						// it individually.
						if (childrenBuffers != null)
							childrenBuffers.clear();
						if (childBuffer != null)
							childBuffer.clear();

					}

					if (qi == 1) {

						long totalChildrenEnd = (new Date()).getTime();
						Configure.totalChildren += totalChildrenEnd
								- totalChildrenStart;
						// System.out.println("total child start time"
						// + totalChildrenStart + "child end time"
						// + totalChildrenEnd);
					}

				}
			}

		}

		else // when a query has no children, we are good.
		{

			if (qiInfo.getOperatorType().equalsIgnoreCase("SEQ"))
				// stack based join
				produceinorder_HStacks(qi, stacks, tempevent, inputschArray,
						leftTimebound);
			else if (qiInfo.getOperatorType().equalsIgnoreCase("AND")) {
				produceAnd(qi, stacks, tempevent, inputschArray,
						triggeringStackIndex, posEventTypeNum, leftTimebound,
						rightTimebound);

			}

			if (this.resultBuffers != null)
				qiResult = this.resultBuffers.get(qi);

		}

		return qiResult;
	}

	/**
	 * works for OR operator as different children queries will have different
	 * result type number
	 * 
	 * @param qi
	 *            nested query ID
	 * @param cID
	 * @return
	 */
	protected int getNestedStackNum(int qi, int childID) {
		int num = 0;
		ArrayList<String> types = getQuery(qi).stackTypes;

		// consider the positive stack only
		num += Utility.getPositiveTypeNum(types);

		ArrayList<String> childrentypes = getQuery(childID).getStackTypes();

		num += Utility.getPositiveTypeNum(childrentypes);

		return num;

	}

	/**
	 * work for SEQ/AND operator
	 * 
	 * @param qi
	 *            nested query ID
	 * @return result event type number
	 */
	protected int getNestedStackNum(int qi) {
		int num = 0;
		ArrayList<String> types = getQuery(qi).stackTypes;
		// consider the positive stack only
		num += Utility.getPositiveTypeNum(types);

		ArrayList<childQueryInfo> childrenq = getQuery(qi).children;

		if (childrenq.size() != 0) {
			for (int i = 0; i < childrenq.size(); i++) {
				int cID = childrenq.get(i).getChildID();
				int type = childrenq.get(i).getPositiveComponent();

				if (type == 1)// positive event types
					num += Utility.getPositiveTypeNum(getQuery(cID).stackTypes);
			}

		}

		return num;

	}

	/**
	 * Process results involving tuple
	 * 
	 * @param stackIndex
	 *            the stack index for the incoming tulpe
	 * @param tuple
	 * @param schArray
	 * 
	 */
	void processQuery(int checkQIndex, byte[] tuple, SchemaElement[] schArray) {

		ArrayList<String> stackTypes = this.queries.get(checkQIndex).stackTypes;
		String type = stackTypes.get(stackTypes.size() - 1);
		int queryID = this.queries.get(checkQIndex).queryID;

		// extended to support negation end
		if (type.startsWith("-")) {
			type = stackTypes.get(stackTypes.size() - 2);
		}

		// here, should be the start of recursive function call
		// stackcompute(qi)

		double leftTimebound = 0;
		double rightTimebound = 0;
		// produceinorder_HStacks(queryID, AIS, tuple, schArray, leftTimebound);

		compute(queryID, AIS, tuple, schArray, leftTimebound, rightTimebound);
		// pass down intervals.

		// for each interval, call compute(qi.child) again

		// after calling each compute(qi.child), and each results are non-empty,
		// call nopointer join(qi, qi.children)

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
	 * gets the direct children of the query with the given query id
	 * 
	 * @param queryID
	 *            query id of parent query
	 * @return array list of children queries
	 */
	public ArrayList<QueryInfo> getChildren(int queryID) {
		ArrayList<QueryInfo> children = new ArrayList<QueryInfo>();

		int i = 0;
		for (; i < this.queries.size(); i++) {
			if (this.queries.get(i).queryID == queryID) {
				break;
			}
		}

		// I should return childrenList actually. but this list don't include
		// stack
		// types and etc full information
		ArrayList<childQueryInfo> childrenList = this.queries.get(i).children;

		for (int j = 0; j < childrenList.size(); j++) {
			int childID = childrenList.get(j).getChildID();
			children.add(getQuery(childID));
		}
		return children;
	}

	/**
	 * a query can have multiple children but only have one parent. determines
	 * the parent query of the query with the given query id
	 * 
	 * @param queryID
	 *            query id of child query
	 * @return parent query
	 */
	public QueryInfo getParent(int queryID) {
		int i = 0;
		for (; i < this.queries.size(); i++) {
			if (this.queries.get(i).getQueryID() == queryID) {
				break;
			}
		}
		// get its parent info
		int parentID = this.queries.get(i).parents.get(0).parentID;
		return getQuery(parentID);

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
	 * gets the query with the given query id
	 * 
	 * @param queryID
	 * @return query
	 */
	public QueryInfo getQuery(int queryID) {
		int i = 0;
		for (; i < this.queries.size(); i++) {
			if (this.queries.get(i).getQueryID() == queryID) {
				break;
			}
		}

		return this.queries.get(i);
	}

}