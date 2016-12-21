package com.hp.hpl.CHAOS.ECube;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import java.util.Hashtable;

import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamOperator.*;
import com.hp.hpl.CHAOS.Queue.SingleReaderEventQueueArrayImp;
import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;
import com.hp.hpl.CHAOS.StreamData.StreamTupleCreator;

public class Naive_MegaSEQoperator extends SingleInputStreamOperator {

	static int query_ID = 0;

	// event types for a single query
	ArrayList<String> stackTypes = new ArrayList<String>();

	// it stores all the submitted queries information
	ArrayList<MergedQueryInfo> queries = new ArrayList<MergedQueryInfo>();
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

	// a hierarchicalQueryPlan includes the event types and the concept tree
	HierarchicalQueryPlan hqp = new HierarchicalQueryPlan(
			new ArrayList<String>(), tree);

	public Naive_MegaSEQoperator(int operatorID, StreamQueue[] input,
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

		if (key.equalsIgnoreCase("query")) {	
			int window = 0;
			
			//get the event types
			int q_SEQ_start = value.indexOf("(");
			int q_SEQ_end = value.indexOf(")");
			String trimedvalue1 = (String) value.subSequence(q_SEQ_start + 1, q_SEQ_end
					);
			String[] result1 = trimedvalue1.split(",");
			
			for (int i = 0; i < result1.length; i++) {
				ETypes_AQuery.add(result1[i].toLowerCase());

				// check whether it exists already.
				if (!contains_notsensitive(stackTypes, result1[i])) {

					stackTypes.add(result1[i].toLowerCase());

				}
			
			//get the window sizes and query ID
			
			String win_str;
			String ID_str;
			
			String trimedvalue2 = (String) value.subSequence(q_SEQ_end + 2, value.length()-1);
			String[] result2 = trimedvalue2.split(" ");
			
			ID_str = result2[0];
			query_ID = Integer.parseInt((String)ID_str.subSequence(ID_str.indexOf("=")+1, ID_str.length()));
			
			win_str = result2[1];			
			window = Integer.parseInt((String)win_str.subSequence(win_str.indexOf("=")+1, win_str.length()));
			}
			queries.add(new MergedQueryInfo(Naive_MegaSEQoperator.query_ID++, window, 
					ETypes_AQuery));

			orderedQueries = orderQueries();
			queries.clear();
			for (int i = 0; i < orderedQueries.length; i++) {
				queries.add((MergedQueryInfo) orderedQueries[i][0]);
			}

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
		// long executionTimeStart =
		hqp.setTree(tree);
		ArrayList<String> queryTypes = new ArrayList<String>();
		hqp.setqueries(queries);

		// create hierarchical pattern graph
		for (int j = 0; j < orderedQueries.length; j++) {
			queryTypes = queries.get(j).getStackTypes();
			hqp.setStackTypes(queryTypes);
			hqp.createHashtable(orderedQueries[j][0].getQueryID());

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

		CM.setOrderedQueries(orderedQueries);

		setupExecutionOrderfornaive();

		initStatus = true;
		return 1;
	}

	public void setupExecutionOrderfornaive() {
		for (int i = 0; i < this.orderedQueries.length; i++) {
			int currentqID = this.orderedQueries[i][0].queryID;
			this.orderedQueries[i][0].setComputeSourceID(currentqID);

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
			if (currenttimestamp >= timestamp) {
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

	/**
	 * extended to support negation
	 * 
	 * @param queryID
	 * @param stacks
	 * @param stackIndex
	 * @param tempevent
	 * @param inputschArray
	 * @return
	 */
	ArrayList<PreviousTuples> previousTuple_HStacks_negated(int queryID,
			EventActiveInstanceQueue[] stacks, int stackIndex,
			byte[] tempevent, SchemaElement[] inputschArray) {

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
				// simulate the predicate evaluation cost
				// Utility.simulatePredicatesCost();
				previoustuple = retPointerArrayTemp[i];

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
							}

						}

					}

				}
			}
			// there are no negated tuples, all the current positive tuples
			// should be added
			if (previousnegatedTuples.size() == 0
					&& previouscurrentTuples.size() != 0) {

				for (int j = 0; j < previouscurrentTuples.size(); j++) {
					PreviousTuples e = new PreviousTuples(previouscurrentTuples
							.get(j), negationPointedtimestamp);

					previousTuples.add(e);
				}
			} else if (previousnegatedTuples.size() != 0
					&& previouscurrentTuples.size() != 0) {
				for (int j = 0; j < previouscurrentTuples.size(); j++) {
					byte[] currentTuple = previouscurrentTuples.get(j);
					double currentTime = StreamAccessor.getDoubleCol(
							currentTuple, inputschArray, 1);

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
								byte[] tuplebeforeNeg = negatedPointerArrayTemp[p];
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
									&& negationPointedtimestamp > currentTime) {
								found = false;
								break;
							}

						}

					}

					if (found == true) {
						PreviousTuples e = new PreviousTuples(currentTuple,
								negationPointedtimestamp);
						previousTuples.add(e);
					}
				}

			}

			/*
			 * if (found == true && previouscurrentTuples.size() != 0) {
			 * PreviousTuples e = new PreviousTuples(previouscurrentTuples
			 * .get(0), negationPointedtimestamp);
			 * 
			 * previousTuples.add(e); }
			 */

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
		for (int j = 0; j < orderedQueries.length; j++) {
			int queryid = orderedQueries[j][0].getQueryID();
			if (queryid == queryID) {
				stackTypes = orderedQueries[j][0].getStackTypes();
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
	 * Given current(bottom) query 1 and query 2(upper), return the type that
	 * doesn't exist in query2 but exist in query1 and it is before the type
	 * specified in the parameter
	 * 
	 * @param Query1
	 * @param Query2
	 * @param type
	 * @return
	 */
	String getNonexistTypebeforeAType(int Query1, int Query2, String type) {

		ArrayList<String> nonexistTypes = new ArrayList<String>();
		String nonType = new String();
		ArrayList<String> pQueryTypes = this.orderedQueries[Query1][0].stackTypes;
		ArrayList<String> cQueryTypes = this.orderedQueries[Query2][0].stackTypes;

		boolean find = false;
		int ip = pQueryTypes.size() - 1;
		int ic = cQueryTypes.size() - 1;

		while (ip >= 0 && ic >= 0) {
			String preType = pQueryTypes.get(ip);
			String curType = cQueryTypes.get(ic);
			if (semanticMatch(preType, curType)) {
				ip--;
				ic--;

			} else {

				if (cQueryTypes.contains(curType)
						&& pQueryTypes.contains(curType)) {
					ip--;
					nonexistTypes.add(preType); // not exist
					if (indexof_notcasesensitive(pQueryTypes, preType) + 1 == indexof_notcasesensitive(
							pQueryTypes, type)) {
						nonType = preType;
						find = true;
						break;
					}

				} else {
					ic--;
					nonexistTypes.add(curType);
					nonType = curType;
					find = true;
				}

			}

		}

		if (find == false) {
			while (ic >= 0) {

				nonexistTypes.add(cQueryTypes.get(ic));
				if (indexof_notcasesensitive(cQueryTypes, cQueryTypes.get(ic)) + 1 == indexof_notcasesensitive(
						cQueryTypes, type)) {
					nonType = cQueryTypes.get(ic);
					find = true;
					break;
				}

				ic--;
			}
		}
		// the rest event types
		if (find == false) {
			while (ip >= 0) {
				nonexistTypes.add(pQueryTypes.get(ip));
				int typeIndex = indexof_notcasesensitive(pQueryTypes, type);
				if (ip + 1 == typeIndex) {
					nonType = pQueryTypes.get(ip);
					find = true;
					break;
				}
				ip--;
			}

		}

		return nonType;

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
	ArrayList<String> getNonexistTypes(int Query1, int Query2) {
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
				if (contains_notsensitive(cQueryTypes, curType)
						&& contains_notsensitive(pQueryTypes, curType)) {
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
			byte[] currenttuple = stacks[k].eventQueue.peekLast();
			while (currenttuple != null) {

				index = StreamAccessor.getIndex(currenttuple);

				ArrayList<PreviousTuples> previousTuples = previousTuple_HStacks_negated(
						queryID, stacks, k, currenttuple, inputschArray);
				for (int i = 0; i < previousTuples.size(); i++) {
					byte[] previousTuple = previousTuples.get(i)
							.getPreviousTuple();
					double stop = previousTuples.get(i).getStopTimestamp();

					previousRIPindex = StreamAccessor.getIndex(previousTuple);

					int sIndex = findStack(getTupleType(previousTuple,
							inputschArray));
//iteratly call sequenceConstruction_Hstacks_negated
					
					
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
				/*
				 * if (currenttimestamp == 25267.56) System.out.print("check");
				 */
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
			byte[] tempevent, SchemaElement[] inputschArray, int eventTypeNum, int window) {

		stackTypes = getQuery(queryID).stackTypes;

		ArrayList<PreviousTuples> previousTuples = previousTuple_HStacks_negated(
				queryID, stacks, stackIndex, tempevent, inputschArray);

		for (int i = 0; i < previousTuples.size(); i++) {
			byte[] previousTuple = previousTuples.get(i).getPreviousTuple();
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

				double timestamp = StreamAccessor.getDoubleCol(tempevent,
						inputschArray, 1);
		
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
				bufferResult(queryID, dest, schArray_Result, ar2, reuse,
						inputschArray, window);

			}

		}

	}

	public void bufferResult(int queryID, byte[] dest,
			SchemaElement[] schArray_Result, ArrayList<byte[]> ar2,
			boolean reuseresults, SchemaElement[] schArray, int window) {

		if (Utility.windowOpt(ar2, schArray, window)) {
			getQuery(queryID).donestatus = (byte) 1;
			
			long executionTime4 = (new Date()).getTime();
			//System.out.println("Execution Time4:" + executionTime4);

			Configure.resultNum += 1;
			//System.out.println("Result #:" + Configure.resultNum);
			
			//latency
			//Utility.accuLatency(ar2); 
			
			if (reuseresults) {
				//System.out.println("======reuse======" + queryID); 
				com.hp.hpl.CHAOS.Queue.Utility.enqueueGroup(OutputQueueArray,
						dest);

			} else {
				//System.out.println("======result======" + queryID); 
				com.hp.hpl.CHAOS.Queue.Utility.enqueueGroup(OutputQueueArray,
						dest);
				//Utility.rewriteToFile("======result======" + queryID);
				//Utility.rewriteToFile(StreamAccessor.toString(dest,
						//schArray_Result));

			}
	
			/*System.out.println(StreamAccessor.toString(dest,
					 schArray_Result)); */
			
			long executionTime5 = (new Date()).getTime();
			//System.out.println("Execution Time5:" + executionTime5);

		}

	}

	/**
	 * Needs further test. compute the relationships between two queries. return
	 * two boolean values. The first one indicate whether pattern hierarchy is
	 * changed. The second one indicate whether concept hierarchy is changed.
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
			ArrayList<String> middle) {

		boolean pattern = false;
		boolean concept = false;

		if (queryID1 == queryID2) {
			boolean[] relations = { pattern, concept };
			return relations;

		} else {
			ArrayList<String> QueryTypes1 = getQuery(queryID1).stackTypes;
			ArrayList<String> QueryTypes2 = getQuery(queryID2).stackTypes;
			int matchCounter = 0;
			//same pattern, check whether concept change
			if (QueryTypes1.size() == QueryTypes2.size()) {
				for (int i = 0; i < QueryTypes1.size(); i++) {
					String type2 = QueryTypes2.get(i);
					//check whether event types are alpha-beta equal (ignore case)
					if (QueryTypes1.get(i).equalsIgnoreCase(type2)) {
						matchCounter++;
						middle.add(type2);
						continue;
					} else {
						//check whether event types in Q1 are the higher lever concept than Q2
						if (semanticMatch(QueryTypes1.get(i), type2)) {
							middle.add(type2);
							matchCounter++;
							continue;
						//check whether event types in Q2 are the higher lever concept than Q1	
						} else if (semanticMatch(type2, QueryTypes1.get(i))) {
							middle.add(QueryTypes1.get(i));
							matchCounter++;
							continue;
						}
					}
				}
				if (matchCounter == QueryTypes1.size()) {
					concept = true;
				}

			} else {
				pattern = true; // the length is not the same
				matchCounter = 0;
				//record the match number of alpha-beta match (ignore case)
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
			boolean[] relations = { pattern, concept };
			return relations;

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
		
		orderedQueries = orderQueries();

		// the schema for all input tuples is the same.
		StreamQueue inputQueue = getInputQueueArray()[0];
		SchemaElement[] schArray = inputQueue.getSchema();

		if (initStatus == false) {
			init(schArray);
		}

		
		for (int i = maxDequeueSize; i > 0; i--) {
			
			long execution_Start = (new Date()).getTime();
			
			//System.out.println("Execution Start:" + execution_Start);
			tuplesProcessed++;
			
			Configure.previousresultNum = Configure.resultNum;
			// first, tuple insertion with pointer set up

			byte[] tuple = inputQueue.dequeue();

			if (tuple == null)
				break;

			for (SchemaElement sch : schArray)
				sch.setTuple(tuple);

			double timestamp = StreamAccessor.getDoubleCol(tuple, schArray, 1);

			// System.out.println(timestamp);
			/*
			 * if (timestamp == 29939.579) { System.out.print("check"); }
			 */

			// here, first apply the aggressive purge
			double expiringTimestamp = timestamp - Configure.windowsize;

			String eventType = getTupleType(tuple, schArray);
			
			// for latency measurement; set min time
			/*if (semanticcontains_notsensitive_position(Utility
					.lastQueryTypes(this.queries), eventType.toLowerCase()) >= 0) {
				StreamAccessor.setMinTimestamp(tuple, (new Date()).getTime());
			}*/
			
			// find out the stack index matching the tuple event type
			int stackIndex = findStack(eventType);
			

			boolean trigger = false;
			if (stackIndex >= 0 && AIS[stackIndex] != null) {

				// here, I should use the stackType for storing the current
				// tuple instead of using the event type.
				// enqueue tuple and set up pointers.
				// pointer size should be greater than 1
				tuple_insertion(stackIndex, tuple);  
				MemorySize++;
				
				//long executionTime1 = (new Date()).getTime();
				//System.out.println("Execution time1:" + execution_Start);
				
				for (int iter = 0; iter < this.orderedQueries.length; iter++) {
					int checkQIndex = iter;
					boolean reuse = false;
					for (int k = 0; k < this.orderedQueries.length; k++) {
						if (this.orderedQueries[k][0].computeSourceID == this.orderedQueries[checkQIndex][0].queryID) {
							reuse = true;
							break;
						}
					}
					if (this.orderedQueries[checkQIndex][0].computeSourceID == this.orderedQueries[checkQIndex][0].queryID) {

						//processQuery(checkQIndex, eventType, stackIndex, tuple,
								//schArray);
						//to record whether the construction result is executed or not 
						boolean executed = processQuery(checkQIndex, eventType, stackIndex, tuple,
								schArray);
						if (executed){
							trigger = executed;
						}
							
					}
					
					
				}

			}

			// here, we actually delete tuples.
			if (trigger){
				if (expiringTimestamp >= 0) {
				
					// purge tuples with timestamp less than the expiring timestamp
					// purge tuple and reset the RIP pointer
					int purgedTuples = Utility.purgeStack(this.AIS, expiringTimestamp, schArray);
					MemorySize = MemorySize - purgedTuples;
				}
				
				//int Memory = getMemoryStatistics();
				//System.out.println("Memory Usage:" + "\t\t" + MemorySize);

				
				//first run-eclipsed time
				long executionTimeEnd = (new Date()).getTime();
				//System.out.println("Execution End:" + executionTimeEnd);
				Configure.executionTime += executionTimeEnd - execution_Start;
				
				Configure.resultNum = Configure.previousresultNum + Configure.resultNum;
				//if (Configure.previousresultNum != Configure.resultNum){
					//int size = OutputQueueArray;
				//System.out.println(Configure.executionTime + "       "
//							+ Configure.resultNum);
					//System.out.println(Configure.executionTime + "\t\t" + tuplesProcessed + "\t\t" + Configure.resultNum);
				//}
			}
			else
			{
				long execution_end = (new Date()).getTime();
				Configure.executionTime += execution_end - execution_Start;
				//Configure.resultNum += aggResult;
				//System.out.println(Configure.executionTime + "\t\t" + tuplesProcessed + "\t\t NA \t\t NA ");
			}		
			
			
			
			
			//second run-latency 
			
			/*if (Configure.previousresultNum != Configure.resultNum)
			{
				
				System.out.println(Configure.latency  + " " + Configure.resultNum); 
					
			}	*/		

		}
	
		//long executionTimeEnd = (new Date()).getTime();
		//Configure.executionTime = executionTimeEnd - execution_Start;
		System.out.println(Configure.executionTime + "\t\t" + tuplesProcessed + "\t\t" + Configure.resultNum);

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
	 * @param tobeReuse
	 *            : indicate whether the results will be reused or not. That is,
	 *            for queries that are independently processed, we will not call
	 *            the enqueue... function.
	 */
	boolean processQuery(int checkQIndex, String eventType, int stackIndex,
			byte[] tuple, SchemaElement[] schArray) {
		
		boolean trigger = false;
		ArrayList<String> stackTypes = orderedQueries[checkQIndex][0].stackTypes;
		String type = stackTypes.get(stackTypes.size() - 1);

		// extended to support negation end
		if (type.startsWith("-")) {
			type = stackTypes.get(stackTypes.size() - 2);
		}
		//check whether the incoming tuple is the last event type of the query
		if (semanticMatch(type, eventType)) {

			trigger = true;
			int queryID = this.orderedQueries[checkQIndex][0].queryID;
			int window = this.orderedQueries[checkQIndex][0].window;
			
			long executionTime2 = (new Date()).getTime();
			//System.out.println("Execution Time2:" + executionTime2);

			int stackTypeSize = 0;
			for (int i = 0; i < stackTypes.size(); i++) {
				if (!stackTypes.get(i).startsWith("-")) {
					stackTypeSize++;
				}
			}

			produceinorder_HStacks(queryID, AIS, stackIndex, tuple, schArray,
					stackTypeSize, window);
			
			long executionTime3 = (new Date()).getTime();
			//System.out.println("Execution Time3:" + executionTime3);

		}
		return trigger;
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
