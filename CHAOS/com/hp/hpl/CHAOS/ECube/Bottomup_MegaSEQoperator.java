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

public class Bottomup_MegaSEQoperator extends SingleInputStreamOperator {

	static int query_ID = 0;
	// stores results for each query, used for duplicate removal and result
	// reuse;
	Hashtable<Integer, ArrayList<ArrayList<byte[]>>> resultBuffers = new Hashtable<Integer, ArrayList<ArrayList<byte[]>>>();

	// event types for a single query
	ArrayList<String> stackTypes = new ArrayList<String>();

	// it stores all the submitted queries information
	ArrayList<QueryInfo> queries = new ArrayList<QueryInfo>();

	// we sort submitted queries according to pattern and concept relationships
	QueryInfo[][] orderedQueries;;

	ArrayList<QueryInfo>[][] orderedQueries2;

	// to make sure the init function is called only once
	private boolean initStatus = false;

	// During bottom up result reuse, for each lower query (qid), we keep track
	// of the latest reused result' end timestamp for each upper query
	Hashtable<Integer, Hashtable<Integer, Double>> existingResultNums = new Hashtable<Integer, Hashtable<Integer, Double>>();

	// build concept hierarchy;
	ConceptTree tree = QueryCompiler.createTreeCompany();
	// the cost model
	CostModel CM = new CostModel(orderedQueries, tree);
	// AIS index is the same as concept encoding
	// for the higher level concept which encoding is an interval, we apply the
	// left boundary as the index
	EventActiveInstanceQueue[] AIS = new EventActiveInstanceQueue[(int) tree
			.getRoot().getRightBound(0).x];

	// the hash tables for representing the hierarchical pattern view
	HierarchicalQueryPlan hqp = new HierarchicalQueryPlan(
			new ArrayList<String>(), tree);

	public Bottomup_MegaSEQoperator(int operatorID, StreamQueue[] input,
			StreamQueue[] output) {
		super(operatorID, input, output);
	}

	/**
	 * put queries in order using a breadth first search
	 * 
	 * @return ordered array of queries and their parents
	 */
/*	public ArrayList<QueryInfo>[][] orderQueries2() {
		ArrayList<QueryInfo>[][] queryArray = findParentsHeuristic();

		ArrayList<QueryInfo> placed = new ArrayList<QueryInfo>();
		ArrayList<QueryInfo>[][] orderedArray = (ArrayList<QueryInfo>[][]) new ArrayList<?>[queries
				.size()][2];

		int index = 0;

		// place root(s)
		for (int i = 0; i < queryArray.length; i++) {
			if (queryArray[i][1].size() == 0) {
				orderedArray[index][0] = queryArray[i][0];
				orderedArray[index][1] = queryArray[i][1];
				index++;

				placed.addAll(queryArray[i][0]);
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

						placed.addAll(queryArray[i][0]);
					}
				}
			}

		}
		return orderedArray;
	}*/

	// It parse the query plan. set up the stack types as well
	@Override
	public void classVariableSetup(String key, String value)
			throws InstantiationException, IllegalAccessException,
			InvocationTargetException, SecurityException,
			ClassNotFoundException {
		// ArrayList used to store all event types in a query.
		ArrayList<String> ETypes_AQuery = new ArrayList<String>();

		if (key.equalsIgnoreCase("query")) {
			int q_index = value.indexOf("(");
			String trimedvalue = (String) value.subSequence(q_index + 1, value
					.length() - 1);
			String[] result = trimedvalue.split(",");
			for (int x = 0; x < result.length; x++) {
				ETypes_AQuery.add(result[x].toLowerCase());

				// check whether it exists already.
				if (!contains_notsensitive(stackTypes, result[x]))
					stackTypes.add(result[x].toLowerCase());

			}
			queries.add(new QueryInfo(Bottomup_MegaSEQoperator.query_ID++,
					ETypes_AQuery));

			orderedQueries = orderQueries();

			queries.clear();
			for (int i = 0; i < orderedQueries.length; i++) {
				queries.add(orderedQueries[i][0]);
			}

		}

	}

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

	public void bottomup_produceinorder_HStacks_concept_missing_sameNegation(
			int queryID, EventActiveInstanceQueue[] stacks, int stackIndex,
			byte[] tempevent, SchemaElement[] inputschArray, int eventTypeNum,
			boolean rebeReused) {

		// stackTypes = getQuery(queryID).stackTypes;
		int computesourceID = getQuery(queryID).computeSourceID;

		// int j = stackTypessource.size() - 2;
		// all the tuples pointed by the tempevent are returned
		ArrayList<PreviousTuples> previousTuples = previousTuple_HStacks_negated(
				queryID, stacks, stackIndex, tempevent, inputschArray);
		int joinedBefore = 1;
		for (int i = 0; i < previousTuples.size(); i++) {
			byte[] previousTuple = previousTuples.get(i).previousTuple;
			double stopTime = previousTuples.get(i).getStopTimestamp();
			String type = getTupleType(previousTuple, inputschArray)
					.toLowerCase();
			int prevousStackIndex = findStack(type);

			int previousRIPindex = StreamAccessor.getIndex(previousTuple);

			ArrayList<String> stackTypes = getQuery(computesourceID).stackTypes;

			ArrayList<ArrayList<byte[]>> sc = bottomup_sequenceConstruction_Hstacks_missing_concept_sameNegation(
					prevousStackIndex, previousRIPindex, stacks, queryID,
					inputschArray, joinedBefore, stackTypes, stopTime);

			for (int k = 0; k < sc.size(); k++) {
				ArrayList<byte[]> ar2 = new ArrayList<byte[]>();

				for (int h = 0; h < sc.get(k).size(); h++) {

					ar2.add(sc.get(k).get(h));
				}
				ar2.add(tempevent);

				if (Utility.windowOpt(ar2, inputschArray)) {
					Configure.resultNum += 1;
					// latency
					 Utility.accuLatency(ar2);

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

					/*
					 * System.out.println("======result======" + queryID);
					 * 
					 * System.out.print(StreamAccessor.toString(dest,
					 * schArray_Result));
					 */
					ArrayList<ArrayList<byte[]>> result = this.resultBuffers
							.get(queryID);

					boolean reuse = false;
					for (int check = 0; check < this.orderedQueries.length; check++) {
						if (this.orderedQueries[check][0].computeSourceID == queryID
								&& this.orderedQueries[check][0].queryID != queryID) {
							reuse = true;
							break;
						}
					}
					// we only store the results for one query if it serves as
					// the source for another query.
					if (reuse) {
						if (result != null) {

							this.resultBuffers.get(queryID).add(ar2);

						}

						else {
							result = new ArrayList<ArrayList<byte[]>>();
							result.add(ar2);
							this.resultBuffers
									.put(new Integer(queryID), result);

						}

					}

				}

			}

		}

	}

	/**
	 * function 1 missing result checking with only concept changes
	 * 
	 * @param queryID
	 * @param stacks
	 * @param stackIndex
	 * @param tempevent
	 * @param inputschArray
	 * @param eventTypeNum
	 * @param rebeReused
	 */
	public void bottomup_produceinorder_HStacks_concept_missing(int queryID,
			EventActiveInstanceQueue[] stacks, int stackIndex,
			byte[] tempevent, SchemaElement[] inputschArray, int eventTypeNum,
			boolean rebeReused) {

		// stackTypes = getQuery(queryID).stackTypes;
		int computesourceID = getQuery(queryID).computeSourceID;

		// int j = stackTypessource.size() - 2;
		// all the tuples pointed by the tempevent are returned
		ArrayList<byte[]> previousTuples = previousTuple_HStacks(queryID,
				stacks, stackIndex, tempevent, inputschArray);
		int joinedBefore = 1;
		for (int i = 0; i < previousTuples.size(); i++) {
			byte[] previousTuple = previousTuples.get(i);
			String type = getTupleType(previousTuple, inputschArray)
					.toLowerCase();
			int prevousStackIndex = findStack(type);

			int previousRIPindex = StreamAccessor.getIndex(previousTuple);

			ArrayList<String> stackTypes = getQuery(computesourceID).stackTypes;

			ArrayList<ArrayList<byte[]>> sc = bottomup_sequenceConstruction_Hstacks_missing_concept(
					prevousStackIndex, previousRIPindex, stacks, queryID,
					inputschArray, joinedBefore, stackTypes);

			for (int k = 0; k < sc.size(); k++) {
				ArrayList<byte[]> ar2 = new ArrayList<byte[]>();

				for (int h = 0; h < sc.get(k).size(); h++) {

					ar2.add(sc.get(k).get(h));
				}
				ar2.add(tempevent);

				if (Utility.windowOpt(ar2, inputschArray)) {
					Configure.resultNum += 1;
					// latency
					 Utility.accuLatency(ar2);

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

					/*
					 * System.out.println("======result======" + queryID);
					 * System.out.println(StreamAccessor.toString(dest,
					 * schArray_Result));
					 */
					ArrayList<ArrayList<byte[]>> result = this.resultBuffers
							.get(queryID);

					boolean reuse = false;
					for (int check = 0; check < this.orderedQueries.length; check++) {
						if (this.orderedQueries[check][0].computeSourceID == queryID
								&& this.orderedQueries[check][0].queryID != queryID) {
							reuse = true;
							break;
						}
					}
					// we only store the results for one query if it serves as
					// the source for another query.
					if (reuse) {
						if (result != null) {

							this.resultBuffers.get(queryID).add(ar2);

						}

						else {
							result = new ArrayList<ArrayList<byte[]>>();
							result.add(ar2);
							this.resultBuffers
									.put(new Integer(queryID), result);

						}

					}

				}

			}

		}

	}

	void bottomup_processQuery_conceptonly_missing_sameNegation(
			int checkQIndex, String eventType, int stackIndex, byte[] tuple,
			SchemaElement[] schArray, boolean tobeReuse) {

		// The index is the one in ordered queries
		ArrayList<String> stackTypes = orderedQueries[checkQIndex][0].stackTypes;
		String type = stackTypes.get(stackTypes.size() - 1);

		// if the event type is one children of the trigger
		// type, it will also trigger the sequence construction for the
		// compensation. we need to check the HPG? maybe no

		if (semanticMatch(type, eventType)) {
			int queryID = this.orderedQueries[checkQIndex][0].queryID;

			int stackTypeSize = 0;
			for (int i = 0; i < stackTypes.size(); i++) {
				if (!stackTypes.get(i).startsWith("-")) {
					stackTypeSize++;
				}
			}

			bottomup_produceinorder_HStacks_concept_missing_sameNegation(
					queryID, AIS, stackIndex, tuple, schArray, stackTypeSize,
					tobeReuse);

		}
	}

	/**
	 * construct the missing results between queries with only concept changes
	 * 
	 * @param checkQIndex
	 * @param eventType
	 * @param stackIndex
	 * @param tuple
	 * @param schArray
	 * @param tobeReuse
	 */
	void bottomup_processQuery_conceptonly_missing(int checkQIndex,
			String eventType, int stackIndex, byte[] tuple,
			SchemaElement[] schArray, boolean tobeReuse) {

		// The index is the one in ordered queries
		ArrayList<String> stackTypes = orderedQueries[checkQIndex][0].stackTypes;
		String type = stackTypes.get(stackTypes.size() - 1);

		// if the event type is one children of the trigger
		// type, it will also trigger the sequence construction for the
		// compensation. we need to check the HPG? maybe no

		if (semanticMatch(type, eventType)) {
			int queryID = this.orderedQueries[checkQIndex][0].queryID;

			int stackTypeSize = 0;
			for (int i = 0; i < stackTypes.size(); i++) {
				if (!stackTypes.get(i).startsWith("-")) {
					stackTypeSize++;
				}
			}

			bottomup_produceinorder_HStacks_concept_missing(queryID, AIS,
					stackIndex, tuple, schArray, stackTypeSize, tobeReuse);

		}
	}

	/**
	 * check the pattern and concept relationships between two queries
	 * 
	 * @param queryID1
	 * @param queryID2
	 * @param middle
	 * @return boolean indicators pattern; concept changed?
	 */
	boolean[] checkQueryPatternConceptR(int queryID1, int queryID2,
			ArrayList<String> middle, ArrayList<Integer> negatePos) {

		boolean pattern = false;
		boolean concept = false;
		boolean negation = false;

		if (queryID1 == queryID2) {
			boolean[] relations = { pattern, concept, negation };
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
						negation = true;
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
				negation = true; // only the lower level query include negation
				pattern = true;

				ArrayList<Integer> rnegatePos = Utility
						.getNegativePos(QueryTypes2);
				for (int j = 0; j < rnegatePos.size(); j++) {
					negatePos.add(rnegatePos.get(j));
				}

			} else {
				if (Utility.getPositiveTypeNum(QueryTypes1) != QueryTypes1
						.size()
						|| Utility.getPositiveTypeNum(QueryTypes2) != QueryTypes2
								.size()) {
					negation = true;
				}

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
			boolean[] relations = { pattern, concept, negation };
			return relations;

		}

	}

	/**
	 * for test, reuse results from the bottom query no matter what
	 */
	public void forTestonly_setupExecutionOrder() {
		// hard code

		for (int i = 0; i < this.orderedQueries.length; i++) {
			int currentqID = this.orderedQueries[i][0].queryID;
			double singleCost = this.CM.singleCompute(currentqID);

			double fromBottomCost = Double.MAX_VALUE;
			double min = singleCost;
			this.orderedQueries[i][0].setComputeSourceID(currentqID);

			boolean[] check = new boolean[2];
			ArrayList<String> middle = new ArrayList<String>();

			// hard code
			// this.orderedQueries[i][0].setComputeSourceID(childIDchosen);

			ArrayList<QueryInfo> children = getChildren(getQuery(currentqID));
			if (children.size() != 0) {
				int childIDchosen = children.get(0).queryID;
				for (int ci = 0; ci < children.size(); ci++) {
					int childID = children.get(ci).queryID;
					double fromBottomCostTemp = Double.MAX_VALUE;
					// for each child query, we should check the cost and chose
					// the
					// minimum one
					ArrayList<Integer> negatePos = new ArrayList<Integer>();
					check = checkQueryPatternConceptR(currentqID, childID,
							middle, negatePos);
					ArrayList<String> nonexistTypes = Bottomup_getNonexistTypes(
							currentqID, childID);
					if (nonexistTypes.size() != 0) {
						// String nonexist = nonexistTypes.get(0);
						// ArrayList<String> childrenTypes =
						// getQuery(childID).stackTypes;
						// int negationPos = childrenTypes.indexOf(nonexist);
						// both concept and pattern are changed
						if (check[0] == true && check[1] == true) {
							// here, I assume only one event type doesn't exist

							// use bottom up cp
							fromBottomCostTemp = 0;
						}
						// only pattern is changed
						else if (check[0] == true) {
							// use pattern bottom up
							fromBottomCostTemp = 0;
						}
					}

					// only concept is changed
					if (check[1] == true && check[0] == false) {
						// use concept bottom up
						fromBottomCostTemp = 0;
					}
					if (fromBottomCostTemp < fromBottomCost) {
						fromBottomCost = fromBottomCostTemp;
						childIDchosen = childID;
					}

				}

				if (fromBottomCost < min) {
					min = fromBottomCost;
					this.orderedQueries[i][0].setComputeSourceID(childIDchosen);
				}

			}

		}

		// this.orderedQueries[0][0].setComputeSourceID(1);
		// this.orderedQueries[1][0].setComputeSourceID(1);
	}

	/**
	 * set up the execution ordering by computing cost model
	 */
	public void setupExecutionOrder() {
		for (int i = 0; i < this.orderedQueries.length; i++) {
			int currentqID = this.orderedQueries[i][0].queryID;
			double singleCost = this.CM.singleCompute(currentqID);

			double fromBottomCost = Double.MAX_VALUE;
			double min = singleCost;
			this.orderedQueries[i][0].setComputeSourceID(currentqID);

			boolean[] check = new boolean[2];
			ArrayList<String> middle = new ArrayList<String>();

			ArrayList<QueryInfo> children = getChildren(getQuery(currentqID));
			if (children.size() != 0) {
				int childIDchosen = children.get(0).queryID;
				for (int ci = 0; ci < children.size(); ci++) {
					int childID = children.get(ci).queryID;
					double fromBottomCostTemp = Double.MAX_VALUE;
					// for each child query, we should check the cost and chose
					// the
					// minimum one
					ArrayList<Integer> negatePos = new ArrayList<Integer>();

					check = checkQueryPatternConceptR(currentqID, childID,
							middle, negatePos);
					ArrayList<String> nonexistTypes = Bottomup_getNonexistTypes(
							currentqID, childID);
					if (nonexistTypes.size() != 0) {
						String nonexist = nonexistTypes.get(0);
						ArrayList<String> childrenTypes = getQuery(childID).stackTypes;
						int negationPos = childrenTypes.indexOf(nonexist);
						// both concept and pattern are changed
						if (check[0] == true && check[1] == true) {
							// here, I assume only one event type doesn't exist

							// use bottom up cp
							fromBottomCostTemp = CM.bottomup_cp(currentqID,
									childID, negationPos);
						}
						// only pattern is changed
						else if (check[0] == true) {
							// use pattern bottom up
							fromBottomCostTemp = CM.bottomup_p(childID,
									negationPos);
							// not compute from bottom
							fromBottomCostTemp = fromBottomCostTemp * 100000;
						}
					}

					// only concept is changed
					if (check[1] == true && check[0] == false) {
						// use concept bottom up
						fromBottomCostTemp = CM.bottomup_concept(currentqID,
								childID);
						// do use bottom up
						fromBottomCostTemp = fromBottomCostTemp / 100000;
					}
					if (fromBottomCostTemp < fromBottomCost) {
						fromBottomCost = fromBottomCostTemp;
						childIDchosen = childID;
					}

				}

				if (fromBottomCost < min) {
					min = fromBottomCost;
					this.orderedQueries[i][0].setComputeSourceID(childIDchosen);
				}

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
	 * Given one string s, return the bounds in the concept hierarchy for event
	 * types The search should start from the largest level (root with the
	 * smallest level).
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

	protected int semanticcontains_notsensitive_position(
			ArrayList<String> list, String astring) {

		int index = list.indexOf(astring);
		if (index < 0) {
			for (int i = 0; i < list.size(); i++) {
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
	 * 
	 * 
	 * @param list
	 * @param astring
	 * @return the position for a give string
	 */
	protected int contains_notsensitive_position(ArrayList<String> list,
			String astring) {

		int index = list.indexOf(astring);
		if (index < 0) {
			for (int i = 0; i < list.size(); i++) {
				boolean contain = list.get(i).equalsIgnoreCase(astring);

				if (contain) {
					index = i;
					break;
				}

			}

		}
		return index;
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
		/*
		 * boolean contain = false; if (list.contains(astring)) { contain =
		 * true; } else { for (int i = 0; i < list.size(); i++) { contain =
		 * semanticMatch(list.get(i), astring) || semanticMatch(astring,
		 * list.get(i));
		 * 
		 * if (contain) break; } }
		 * 
		 * return contain;
		 */
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

	protected boolean purecontains_notsensitive(ArrayList<String> list,
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
		orderedQueries = orderQueries();
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

		// orderedQueries2 = orderQueries2();

		// please note that the orderedQueries we only setup the parent and
		// child relationship. the compute source is not setted up yet.
		CM.setOrderedQueries(orderedQueries);
		setupExecutionOrder();
		 //forTestonly_setupExecutionOrder();

		initStatus = true;
		return 1;
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
	 * Return the left bound for a give event type in a concept hierarchy and
	 * the left bound is the stack index for the event type
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
	 * join a tuple with a list of tuples to form a longer tuple list
	 * 
	 * @param currenttuple
	 * @param ear
	 * @param inputschArray
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
	 * Return the event type of a given tuple
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

	/**
	 * return the index for the given type in a query
	 * 
	 * @param type
	 * @param queryID
	 * @return
	 */
	int getPosition(String type, int queryID) {
		ArrayList<String> stackTypes = getQuery(queryID).stackTypes;
		return semanticcontains_notsensitive_position(stackTypes, type);
	}

	/**
	 * compute the previous tuples before the given tuple of a query if there is
	 * no "negated" event, then it is the same as the regular previousTuple
	 * function. otherwise, only tuples with timestamp greater than the negated
	 * event should be joined.
	 * 
	 * @param queryID
	 * @param stacks
	 * @param stackIndex
	 * @param tempevent
	 * @param inputschArray
	 * @return
	 */
	/*
	 * ArrayList<PreviousTuples>
	 * bottomup_previousTuple_HStacks_patternbottomupReuse( int queryID,
	 * EventActiveInstanceQueue[] stacks, int stackIndex, byte[] tempevent,
	 * SchemaElement[] inputschArray) { double timestamp =
	 * StreamAccessor.getDoubleCol(tempevent, inputschArray, 1);
	 * 
	 * 
	 * if (timestamp == 25264.226) { System.out.print("check"); }
	 * 
	 * int compareQID = getQuery(queryID).computeSourceID;
	 * 
	 * String type = getTupleType(tempevent, inputschArray);
	 * 
	 * ArrayList<PreviousTuples> previousTuples = new
	 * ArrayList<PreviousTuples>();
	 * 
	 * // here, still, the type should be the stack type instead of event type.
	 * Hashtable<String, ArrayList<EdgeLabel>> table2 = this.hqp
	 * .getHierarchicalQueryPlan().get(
	 * AIS[findStack(type)].stackType.toLowerCase()); boolean found = false; if
	 * (table2 != null) { int pointerSize = table2.keySet().size(); byte[][]
	 * retPointerArrayTemp = new byte[pointerSize][]; int index =
	 * StreamAccessor.getIndex(tempevent);
	 * 
	 * stacks[stackIndex].getByPhysicalIndex(index, retPointerArrayTemp); byte[]
	 * previoustuple = null;
	 * 
	 * ArrayList<byte[]> previouscurrentTuples = new ArrayList<byte[]>();
	 * ArrayList<byte[]> previousnegatedTuples = new ArrayList<byte[]>(); // if
	 * the off event type is the last event type in the lower query, // we need
	 * to check String previousType = new String(); double
	 * negationPointedtimestamp = -1; for (int i = 0; i < pointerSize; i++) { //
	 * simulate the predicate evaluation cost Utility.simulatePredicatesCost();
	 * previoustuple = retPointerArrayTemp[i];
	 * 
	 * if (previoustuple != null) { previousType = getTupleType(previoustuple,
	 * inputschArray);
	 * 
	 * // check in the HPG, the previousType - currenttype edge
	 * 
	 * // check whether it belongs to the current query
	 * 
	 * int positionlower = getPosition(type, compareQID);
	 * 
	 * int positionlowerpre = getPosition(previousType, compareQID);
	 * 
	 * int position = getPosition(type, queryID);
	 * 
	 * int positionpre = getPosition(previousType, queryID); // if it is the
	 * negated type // it is continuous in both queries if (positionpre >= 0 &&
	 * position >= 0 && positionlower >= 0 && positionlowerpre >= 0 &&
	 * positionpre + 1 == position && positionlowerpre + 1 == positionlower) {
	 * previouscurrentTuples.add(previoustuple); } else if (positionlowerpre + 1
	 * == positionlower) {
	 * 
	 * previousnegatedTuples.add(previoustuple); } else if (positionpre + 1 ==
	 * position) { previouscurrentTuples.add(previoustuple); } // the off event
	 * type is the last event type else if (positionlowerpre + 1 ==
	 * positionlower && positionlower < 0) {
	 * 
	 * byte[] e = AIS[findStack(previousType)].peekLast(null);
	 * previouscurrentTuples.add(e); }
	 * 
	 * } } // there are no negated tuples, all the current positive tuples //
	 * should be added if (previousnegatedTuples.size() == 0 &&
	 * previouscurrentTuples.size() != 0) {
	 * 
	 * for (int j = 0; j < previouscurrentTuples.size(); j++) { PreviousTuples e
	 * = new PreviousTuples(previouscurrentTuples .get(j),
	 * negationPointedtimestamp);
	 * 
	 * previousTuples.add(e); } } else if (previousnegatedTuples.size() != 0 &&
	 * previouscurrentTuples.size() != 0) {
	 * 
	 * for (int i = 0; i < previousnegatedTuples.size(); i++) {
	 * 
	 * byte[] negatedTuple = previousnegatedTuples.get(i); String pType =
	 * getTupleType(negatedTuple, inputschArray);
	 * 
	 * String pStackType = AIS[findStack(pType)].stackType; int negatedindex =
	 * StreamAccessor.getIndex(negatedTuple);
	 * 
	 * Hashtable<String, ArrayList<EdgeLabel>> negatedTable = this.hqp
	 * .getHierarchicalQueryPlan().get( pStackType.toLowerCase());
	 * 
	 * if (negatedTable != null) { int negatedpointerSize =
	 * negatedTable.keySet().size(); byte[][] negatedPointerArrayTemp = new
	 * byte[negatedpointerSize][]; stacks[findStack(pType)].getByPhysicalIndex(
	 * negatedindex, negatedPointerArrayTemp);
	 * 
	 * for (int p = 0; p < negatedpointerSize; p++) { byte[] tuplebeforeNeg =
	 * negatedPointerArrayTemp[p]; if (tuplebeforeNeg != null) { String
	 * tuplebeforeNegType = getTupleType( tuplebeforeNeg, inputschArray); if
	 * (tuplebeforeNegType .equalsIgnoreCase(previousType)) {
	 * negationPointedtimestamp = StreamAccessor .getDoubleCol(tuplebeforeNeg,
	 * inputschArray, 1); break; }
	 * 
	 * }
	 * 
	 * }
	 * 
	 * for (int j = 0; j < previouscurrentTuples.size(); j++) { byte[]
	 * currentTuple = previouscurrentTuples.get(j); double currentTime =
	 * StreamAccessor.getDoubleCol( currentTuple, inputschArray, 1);
	 * 
	 * if (negationPointedtimestamp >= 0 && negationPointedtimestamp <
	 * currentTime || negationPointedtimestamp < 0) {
	 * 
	 * // found = true; PreviousTuples e = new PreviousTuples( currentTuple,
	 * negationPointedtimestamp); previousTuples.add(e); break;
	 * 
	 * } }
	 * 
	 * }
	 * 
	 * }
	 * 
	 * }
	 * 
	 * 
	 * if (found == true && previouscurrentTuples.size() != 0) { PreviousTuples
	 * e = new PreviousTuples(previouscurrentTuples .get(0),
	 * negationPointedtimestamp);
	 * 
	 * previousTuples.add(e); }
	 * 
	 * 
	 * } return previousTuples;
	 * 
	 * }
	 */

	/**
	 * need further test given a query id, return the initial stacks, that is,
	 * there are no points before these stacks.
	 * 
	 * 
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
		//
		ArrayList<String> firstTypes = new ArrayList<String>();

		String firstType = stackTypes.get(0);
		if (firstType.startsWith("-")) {
			firstType = stackTypes.get(1);
		}

		firstTypes.add(firstType);

		for (int i = 0; i < this.orderedQueries.length; i++) {
			ArrayList<String> stackTs = this.orderedQueries[i][0].stackTypes;
			for (int j = 0; j < stackTs.size(); j++) {
				String singleType = stackTs.get(j);
				if (semanticMatch(firstType, singleType)
						&& !firstTypes.contains(singleType)) {
					firstTypes.add(singleType);
				}
			}

		}

		int[] firstIndexs = new int[firstTypes.size()];
		for (int i = 0; i < firstTypes.size(); i++) {
			firstIndexs[i] = findStack(firstTypes.get(i));
		}
		return firstIndexs;
	}

	ArrayList<ArrayList<byte[]>> bottomup_sequenceConstruction_Hstacks_missing_concept_sameNegation(
			int k, int index, EventActiveInstanceQueue[] stacks, int queryID,
			SchemaElement[] inputschArray, int joinedBeforeNum,
			ArrayList<String> stackTypes, double stopTime) {

		ArrayList<ArrayList<byte[]>> earray = new ArrayList<ArrayList<byte[]>>();
		ArrayList<ArrayList<byte[]>> earray3 = new ArrayList<ArrayList<byte[]>>();
		ArrayList<byte[]> array = new ArrayList<byte[]>();

		String stackType = stacks[k].stackType.toLowerCase();
		// note, we should use the stack type instead of the event type in
		// checking the hierarchical pattern graph
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

			boolean reuse = false;
			String tupleType = new String();
			if (tuple != null) {
				tupleType = getTupleType(tuple, inputschArray);
				int joinedBeforeNumsave = 0;

				if (joinedBeforeNum == 0) {
					reuse = true;
				} else {
					joinedBeforeNumsave = joinedBeforeNum;
				}
			}

			while (tuple != null) {
				ArrayList<byte[]> array2 = new ArrayList<byte[]>();
				int queryLength = Utility.getPositiveTypeNum(stackTypes);

				if (reuse == false
						&& semanticMatch(stackTypes.get(0), tupleType)) {
					joinedBeforeNum++;
				}

				if (reuse == true || joinedBeforeNum != queryLength) {
					array.add(tuple);
					index = StreamAccessor.getIndex(tuple);

					for (int m3 = 0; m3 < array.size(); m3++) {
						array2.add(array.get(m3));
					}
					earray.add(array2);

					array.clear();
					tuple = stacks[k].getPreviousByPhysicalIndex(index,
							retPointerArrayTemp);

					double timestamp = StreamAccessor.getDoubleCol(tuple,
							inputschArray, 1);
					if (timestamp <= stopTime)
						break;

				} else {
					break;
				}

			}
			// joinedBeforeNum = joinedBeforeNumsave;
			return earray;
		} else {
			int previousRIPindex = 0;
			boolean joinedUpper = true;

			byte[] currenttuple = stacks[k].eventQueue.peekLast();

			int joinedBeforeNumsaved = joinedBeforeNum;

			String tupleType = getTupleType(currenttuple, inputschArray);
			// if the component primitive event type was not in the computed
			// lower level query, results involving such primitive events
			// are new results and we don't need to worry much about
			int i = 0;
			for (; i < stackTypes.size(); i++) {
				String type = stackTypes.get(i);
				if (type.startsWith("-")) {
					continue;
				}
				if (semanticMatch(type, tupleType)) {
					break;
				} else {
					continue;

				}

			}

			// if one middle part is new, we can stop counting
			// and start regular processing
			if (i == stackTypes.size()) {
				joinedUpper = false;
				joinedBeforeNum = 0;
			}

			if (joinedUpper)
				joinedBeforeNum++;

			while (currenttuple != null) {

				index = StreamAccessor.getIndex(currenttuple);

				ArrayList<PreviousTuples> previousTuples = previousTuple_HStacks_negated(
						queryID, stacks, k, currenttuple, inputschArray);
				for (int j = 0; j < previousTuples.size(); j++) {
					byte[] previousTuple = previousTuples.get(j).previousTuple;
					double stop = previousTuples.get(j).getStopTimestamp();
					previousRIPindex = StreamAccessor.getIndex(previousTuple);

					int sIndex = findStack(getTupleType(previousTuple,
							inputschArray));

					earray3 = connect(
							currenttuple,
							bottomup_sequenceConstruction_Hstacks_missing_concept_sameNegation(
									sIndex,// check
									previousRIPindex, stacks, queryID,
									inputschArray, joinedBeforeNum, stackTypes,
									stop), inputschArray);
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
			joinedBeforeNum = joinedBeforeNumsaved;
		}
		return earray;
	}

	/**
	 * construct delta results for patterns with concept changes only
	 * 
	 * @param k
	 * @param index
	 * @param stacks
	 * @param queryID
	 * @param inputschArray
	 * @param nextTuple
	 * @return
	 */
	ArrayList<ArrayList<byte[]>> bottomup_sequenceConstruction_Hstacks_missing_concept(
			int k, int index, EventActiveInstanceQueue[] stacks, int queryID,
			SchemaElement[] inputschArray, int joinedBeforeNum,
			ArrayList<String> stackTypes) {

		ArrayList<ArrayList<byte[]>> earray = new ArrayList<ArrayList<byte[]>>();
		ArrayList<ArrayList<byte[]>> earray3 = new ArrayList<ArrayList<byte[]>>();
		ArrayList<byte[]> array = new ArrayList<byte[]>();

		String stackType = stacks[k].stackType.toLowerCase();
		// note, we should use the stack type instead of the event type in
		// checking the hierarchical pattern graph
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
			boolean reuse = false;
			String tupleType = new String();
			if (tuple != null) {
				tupleType = getTupleType(tuple, inputschArray);
				int joinedBeforeNumsave = 0;

				if (joinedBeforeNum == 0) {
					reuse = true;
				} else {
					joinedBeforeNumsave = joinedBeforeNum;
				}
			}

			while (tuple != null) {
				ArrayList<byte[]> array2 = new ArrayList<byte[]>();
				int queryLength = stackTypes.size();

				if (reuse == false
						&& semanticMatch(stackTypes.get(0), tupleType)) {
					joinedBeforeNum++;
				}

				if (reuse == true || joinedBeforeNum != queryLength) {
					array.add(tuple);
					index = StreamAccessor.getIndex(tuple);

					for (int m3 = 0; m3 < array.size(); m3++) {
						array2.add(array.get(m3));
					}
					earray.add(array2);

					array.clear();
					tuple = stacks[k].getPreviousByPhysicalIndex(index,
							retPointerArrayTemp);
				} else {
					break;
				}

			}
			// joinedBeforeNum = joinedBeforeNumsave;
			return earray;
		} else {
			int previousRIPindex = 0;
			boolean joinedUpper = true;

			byte[] currenttuple = stacks[k].eventQueue.peekLast();

			int joinedBeforeNumsaved = joinedBeforeNum;

			if (currenttuple != null) {
				String tupleType = getTupleType(currenttuple, inputschArray);
				// if the component primitive event type was not in the computed
				// lower level query, results involving such primitive events
				// are new results and we don't need to worry much about
				int i = 0;
				for (; i < stackTypes.size(); i++) {
					if (semanticMatch(stackTypes.get(i), tupleType)) {
						break;
					} else {
						continue;

					}

				}

				// if one middle part is new, we can stop counting
				// and start regular processing
				if (i == stackTypes.size()) {
					joinedUpper = false;
					joinedBeforeNum = 0;
				}

				if (joinedUpper)
					joinedBeforeNum++;
			}

			while (currenttuple != null) {

				index = StreamAccessor.getIndex(currenttuple);

				ArrayList<byte[]> previousTuples = previousTuple_HStacks(
						queryID, stacks, k, currenttuple, inputschArray);
				for (int j = 0; j < previousTuples.size(); j++) {
					byte[] previousTuple = previousTuples.get(j);
					previousRIPindex = StreamAccessor.getIndex(previousTuple);

					int sIndex = findStack(getTupleType(previousTuple,
							inputschArray));

					earray3 = connect(
							currenttuple,
							bottomup_sequenceConstruction_Hstacks_missing_concept(
									sIndex,// check
									previousRIPindex, stacks, queryID,
									inputschArray, joinedBeforeNum, stackTypes),
							inputschArray);
					for (int t = 0; t < earray3.size(); t++) {
						earray.add(earray3.get(t));
					}
				}

				currenttuple = stacks[k].getPreviousByPhysicalIndex(index,
						retPointerArrayTemp);

			}
			joinedBeforeNum = joinedBeforeNumsaved;
		}
		return earray;
	}

	ArrayList<ArrayList<byte[]>> sequenceConstruction_Hstacks(int k, int index,
			EventActiveInstanceQueue[] stacks, int queryID,
			SchemaElement[] inputschArray, byte[] nextTuple) {

		ArrayList<ArrayList<byte[]>> earray = new ArrayList<ArrayList<byte[]>>();
		ArrayList<ArrayList<byte[]>> earray3 = new ArrayList<ArrayList<byte[]>>();
		ArrayList<byte[]> array = new ArrayList<byte[]>();

		// String nextType = getTupleType(nextTuple,
		// inputschArray).toLowerCase()
		// .toLowerCase();
		String stackType = stacks[k].stackType.toLowerCase();
		// I should use the stack type instead of the event type
		Hashtable<String, ArrayList<EdgeLabel>> table2 = this.hqp.hash1
				.get(stackType.toLowerCase());
		byte[][] retPointerArrayTemp = new byte[1][];
		if (table2 != null)
			retPointerArrayTemp = new byte[table2.keySet().size()][];

		// change for multiple queries.

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

				// String Type = getTupleType(tuple,
				// inputschArray).toLowerCase();

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
	ArrayList<ArrayList<byte[]>> bottomup_sequenceConstruction_Hstacks_patternonly(
			int k, int index, EventActiveInstanceQueue[] stacks, int queryID,
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

			int compareQID = getQuery(queryID).computeSourceID;

			ArrayList<String> offTypes = Bottomup_getNonexistTypes(queryID,
					compareQID);
			String firstType = new String();

			firstType = getQuery(compareQID).stackTypes.get(0);

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

			long queueSize = stacks[k].eventQueue.getSize();
			int tupleCounter = 0;
			while (tuple != null && tupleCounter <= queueSize) {
				tupleCounter++;
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

				// ok, there should be two cases at least: if there are negative
				// event type before the current
				// event type, the previousTuple_HStacks_bottomupReuse_negated
				// is in use.
				// otherwise, the regular previous tuple function in the naive
				// solution is in use
				String currentTuple = getTupleType(currenttuple, inputschArray);
				ArrayList<String> QueryTypes_current = getQuery(queryID).stackTypes;
				int typePos = getPosition(currentTuple, queryID);
				String preType = QueryTypes_current.get(typePos - 1);
				ArrayList<PreviousTuples> previousTuples = new ArrayList<PreviousTuples>();
				if (preType.startsWith("-")) {
					previousTuples = previousTuple_HStacks_bottomupReuse_negated(
							queryID, stacks, k, currenttuple, inputschArray);
				} else {
					previousTuples = previousTuple_HStacks_negated(queryID,
							stacks, k, currenttuple, inputschArray);
				}
				for (int i = 0; i < previousTuples.size(); i++) {
					byte[] previousTuple = previousTuples.get(i)
							.getPreviousTuple();
					double stop = previousTuples.get(i).getStopTimestamp();

					previousRIPindex = StreamAccessor.getIndex(previousTuple);

					int sIndex = findStack(getTupleType(previousTuple,
							inputschArray));

					earray3 = connect(currenttuple,
							bottomup_sequenceConstruction_Hstacks_patternonly(
									sIndex, previousRIPindex, stacks, queryID,
									inputschArray, currenttuple, stop),
							inputschArray);
					for (int t = 0; t < earray3.size(); t++) {
						earray.add(earray3.get(t));
					}
				}

				currenttuple = stacks[k].getPreviousByPhysicalIndex(index,
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

					if (indexof_notcasesensitive(cQueryTypes, curType) + 1 == indexof_notcasesensitive(
							pQueryTypes, type)) {
						nonType = curType;
						find = true;
						break;
					}
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
	ArrayList<String> Bottomup_getNonexistTypes(int Query1, int Query2) {
		ArrayList<String> nonexistTypes = new ArrayList<String>();
		ArrayList<String> pQueryTypes = getQuery(Query1).stackTypes;
		ArrayList<String> cQueryTypes = getQuery(Query2).stackTypes;

		int ip = pQueryTypes.size() - 1;
		int ic = cQueryTypes.size() - 1;

		while (ip >= 0 && ic >= 0) {
			String preType = pQueryTypes.get(ip);
			String curType = cQueryTypes.get(ic);
			if (semanticMatch(preType, curType)
					|| semanticMatch(curType, preType)) {
				ip--;
				ic--;

			} else {

				// we think the current query has more query types
				if (contains_notsensitive(cQueryTypes, curType)
						&& contains_notsensitive(pQueryTypes, curType)) {
					ip--;
					nonexistTypes.add(preType);
				} else {
					ic--;
					nonexistTypes.add(curType);
				}

			}

		}

		// the left event types
		while (ic >= 0) {
			nonexistTypes.add(cQueryTypes.get(ic));
			ic--;
		}
		while (ip >= 0) {
			nonexistTypes.add(pQueryTypes.get(ip));
			ip--;
		}
		return nonexistTypes;
	}

	/**
	 * check whether all the off event types are negative
	 * 
	 * @param stackTypes
	 * @return
	 */
	public boolean isAllNegative(ArrayList<String> offTypes) {
		boolean allNegative = true;
		for (int i = 0; i < offTypes.size(); i++) {
			if (offTypes.get(i).startsWith("-")) {
				continue;
			} else {
				allNegative = false;
				break;
			}
		}
		return allNegative;
	}

	/**
	 * result reuse from previous constructed query results. reuse suitable for
	 * both concept and pattern changes
	 * 
	 * extended to support negation
	 * 
	 * @param ar
	 *            result for reuse
	 * @param schArray
	 *            schema elements
	 * @param bottomqueryID
	 * @param topqueryID
	 * @param tempevent
	 *            new arrival event
	 * @param iresult
	 *            the result index in the lower query result buffer
	 */
	public void bottomup_pc_reuse_enqueueSubsequences(ArrayList<byte[]> ar,
			SchemaElement[] schArray, int bottomqueryID, int topqueryID,
			byte[] tempevent, int stackIndex, int iresult) {

		ArrayList<byte[]> ar2 = ar;

		ArrayList<String> nonexistTypes = new ArrayList<String>();
		int currentQueryID = topqueryID;
		ArrayList<String> topQueryTypes = getQuery(topqueryID).stackTypes;

		nonexistTypes.addAll(Bottomup_getNonexistTypes(bottomqueryID,
				topqueryID));

		SchemaElement[] schArray_Result = generateResultSchemas(Utility
				.getPositiveTypeNum(topQueryTypes), schArray);
		byte[] dest = StreamTupleCreator.makeEmptyTuple(schArray_Result);
		boolean existbyte = false;

		// ok, I think I found one possible easy way for result reuse
		// to make sure we can win for the bottom up method in a concept
		// hierarchy.

		// we assume the upper query and lower query for reuse for a strict
		// concept/pattern changes. For example, we don't handle SEQ(A, B, c)
		// and SEQ(A, b, C) for result reuse

		// only concept changes/the lower query has stronger negative
		// constraints, direct reuse
		if (nonexistTypes.size() == 0 || isAllNegative(nonexistTypes)) {

			dest = converttoByteArray(ar2, schArray_Result, schArray,
					topqueryID);

		} else {
			int schIndex = 0;

			// pattern changes, we need to subtract subsequences from each
			// result
			for (int Ibuffer = 0; Ibuffer < ar2.size(); Ibuffer++) {
				byte[] checkingTuple = ar2.get(Ibuffer);
				String tupleType = getTupleType(checkingTuple, schArray);

				// if one component primitive event's type of the lower level
				// query doesn't exist in the upper level query, we need to
				// remove such component primitive event from the each result.
				if (semanticcontains_notsensitive_position(nonexistTypes,
						tupleType) >= 0) {

					ar2.remove(Ibuffer);
					Ibuffer--;
				} else {

					StreamTupleCreator.tupleAppend(dest, checkingTuple,
							schArray_Result[schIndex].getOffset());
					schIndex += schArray.length;

				}
			}

			ArrayList<ArrayList<byte[]>> results = this.resultBuffers
					.get(currentQueryID);

			// check whether such subtracted sequence results was generated
			// before
			if (results != null)
				for (int i = 0; i < results.size() && results.get(i) != null; i++) {

					byte[] dest2 = converttoByteArray(results.get(i),
							schArray_Result, schArray, currentQueryID);
					int byteIndex = 0;
					for (; byteIndex < dest.length; byteIndex++) {
						if (dest[byteIndex] == dest2[byteIndex]) {
							continue;
						} else {
							existbyte = false;
							break;
						}
					}

					if (byteIndex == dest.length) {
						existbyte = true;
						break;
					}

					if (existbyte)
						break;

				}
		}

		boolean output = Utility.windowOpt(ar2, schArray);
		if (output && existbyte == false) {
			// System.out.println("reuse for query: " + currentQueryID);

			Configure.resultNum += 1;

			// latency
			 Utility.accuLatency(ar2);

			// need duplicate removal
			// System.out.println(StreamAccessor.toString(dest,
			// schArray_Result));

			ArrayList<ArrayList<byte[]>> result = this.resultBuffers
					.get(currentQueryID);

			boolean reuse = false;
			for (int k = 0; k < this.orderedQueries.length; k++) {
				if (this.orderedQueries[k][0].computeSourceID == currentQueryID) {
					reuse = true;
					break;
				}
			}

			if (reuse || !reuse && nonexistTypes.size() != 0
					&& !isAllNegative(nonexistTypes))
			// if pattern changes need to buffer the results for further
			// duplicated result checking
			{
				if (result != null) {
					this.resultBuffers.get(currentQueryID).add(ar2);
				}

				else {
					result = new ArrayList<ArrayList<byte[]>>();
					result.add(ar2);
					this.resultBuffers.put(new Integer(currentQueryID), result);

				}
			}

		}

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

	/**
	 * It produce the missing sequence results for the upper level query in the
	 * bottom up result reuse with only pattern changes
	 * 
	 * 
	 * @param stacks
	 * @param tempevent
	 * @param queryid
	 *            : the query we compute the missing results for
	 */
	public void bottomup_produceinorder_HStacks_patternonly_missing(
			int queryID, EventActiveInstanceQueue[] stacks, int stackIndex,
			byte[] tempevent, SchemaElement[] inputschArray, int eventTypeNum,
			ArrayList<Integer> negatePos) {
		int compareQID = getQuery(queryID).computeSourceID;

		stackTypes = getQuery(queryID).stackTypes;
		ArrayList<String> bottomStackTypes = getQuery(compareQID).stackTypes;

		String upsecondLastType = stackTypes.get(stackTypes.size() - 2);
		String bottomsecondLastType = bottomStackTypes.get(bottomStackTypes
				.size() - 2);

		ArrayList<PreviousTuples> previousTuples = new ArrayList<PreviousTuples>();

		// first step: check the negative event type position
		// if the negative event type is located before the last event type
		if (upsecondLastType.startsWith("-")
				&& bottomsecondLastType.startsWith("-")) {
			upsecondLastType = upsecondLastType.substring(1, upsecondLastType
					.length());
			bottomsecondLastType = bottomsecondLastType.substring(1,
					bottomsecondLastType.length());
			if (upsecondLastType.equalsIgnoreCase(bottomsecondLastType)) {
				previousTuples = previousTuple_HStacks_bottomupReuse_negated(
						queryID, stacks, stackIndex, tempevent, inputschArray);
			} else if (semanticMatch(bottomsecondLastType, upsecondLastType)) {
				previousTuples = previousTuple_HStacks_bottomupReuse_negated(
						queryID, stacks, stackIndex, tempevent, inputschArray);
			}

		}
		// only the bottom query includes negation
		else if (bottomsecondLastType.startsWith("-")) {
			previousTuples = previousTuple_HStacks_bottomupReuse_negated_lower(
					queryID, stacks, stackIndex, tempevent, inputschArray);
		} else {
			// both are positive event types, use the regular previous tuple
			// function in the naive method
			previousTuples = previousTuple_HStacks_negated(queryID, stacks,
					stackIndex, tempevent, inputschArray);
		}

		for (int i = 0; i < previousTuples.size(); i++) {
			byte[] previousTuple = previousTuples.get(i).getPreviousTuple();
			double stopTime = previousTuples.get(i).getStopTimestamp();
			String type = getTupleType(previousTuple, inputschArray)
					.toLowerCase();
			int prevousStackIndex = findStack(type);

			int previousRIPindex = StreamAccessor.getIndex(previousTuple);

			ArrayList<ArrayList<byte[]>> sc = bottomup_sequenceConstruction_Hstacks_patternonly(
					prevousStackIndex, previousRIPindex, stacks, queryID,
					inputschArray, tempevent, stopTime);

			for (int k = 0; k < sc.size(); k++) {
				ArrayList<byte[]> ar2 = new ArrayList<byte[]>();

				for (int h = 0; h < sc.get(k).size(); h++) {

					ar2.add(sc.get(k).get(h));
				}
				ar2.add(tempevent);

				boolean output = Utility.windowOpt(ar2, inputschArray);
				if (output) {
					Configure.resultNum += 1;
					// latency
					 Utility.accuLatency(ar2);

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

					// System.out.println("======miss result======" + queryID);
					// System.out.println(StreamAccessor.toString(dest,
					// schArray_Result));

					ArrayList<ArrayList<byte[]>> result = this.resultBuffers
							.get(queryID);
					boolean reuse = false;
					for (int check = 0; check < this.orderedQueries.length; check++) {
						if (this.orderedQueries[check][0].computeSourceID == queryID
								&& this.orderedQueries[check][0].queryID != queryID) {
							reuse = true;
							break;
						}
					}

					// we only store the results for one query if it servers as
					// the source for another query.
					if (reuse) {
						if (result != null) {

							this.resultBuffers.get(queryID).add(ar2);

						}

						else {
							result = new ArrayList<ArrayList<byte[]>>();
							result.add(ar2);
							this.resultBuffers
									.put(new Integer(queryID), result);

						}
					}

				}

			}

		}

	}

	ArrayList<ArrayList<byte[]>> bottomup_sequenceConstruction_Hstacks_patternonly_expiring(
			int k, int index, EventActiveInstanceQueue[] stacks, int queryID,
			SchemaElement[] inputschArray, byte[] nextTuple, double stopTime,
			ArrayList<byte[]> expiringTuples) {

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

			int compareQID = getQuery(queryID).computeSourceID;

			ArrayList<String> offTypes = Bottomup_getNonexistTypes(queryID,
					compareQID);
			String firstType = new String();

			firstType = getQuery(compareQID).stackTypes.get(0);

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

				if (found[0] == true && expiringTuples.contains(tuple)) {
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

				ArrayList<PreviousTuples> previousTuples = previousTuple_HStacks_bottomupReuse_negated(
						queryID, stacks, k, currenttuple, inputschArray);
				for (int i = 0; i < previousTuples.size(); i++) {
					byte[] previousTuple = previousTuples.get(i)
							.getPreviousTuple();
					double stop = previousTuples.get(i).getStopTimestamp();

					previousRIPindex = StreamAccessor.getIndex(previousTuple);

					int sIndex = findStack(getTupleType(previousTuple,
							inputschArray));

					earray3 = connect(
							currenttuple,
							bottomup_sequenceConstruction_Hstacks_patternonly_expiring(
									sIndex, previousRIPindex, stacks, queryID,
									inputschArray, currenttuple, stop,
									expiringTuples), inputschArray);
					for (int t = 0; t < earray3.size(); t++) {
						earray.add(earray3.get(t));
					}
				}

				currenttuple = stacks[k].getPreviousByPhysicalIndex(index,
						retPointerArrayTemp);

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
	 * @param eventTypeNum
	 * @param rebeReused
	 * @param expiringTuples
	 */
	public void bottomup_produceinorder_HStacks_expiring(int queryID,
			EventActiveInstanceQueue[] stacks, int stackIndex,
			byte[] tempevent, SchemaElement[] inputschArray, int eventTypeNum,
			boolean rebeReused, ArrayList<byte[]> expiringTuples) {

		stackTypes = getQuery(queryID).stackTypes;

		ArrayList<PreviousTuples> previousTuples = previousTuple_HStacks_bottomupReuse_negated(
				queryID, stacks, stackIndex, tempevent, inputschArray);

		for (int i = 0; i < previousTuples.size(); i++) {
			byte[] previousTuple = previousTuples.get(i).getPreviousTuple();
			String type = getTupleType(previousTuple, inputschArray);
			int prevousStackIndex = findStack(type);

			int previousRIPindex = StreamAccessor.getIndex(previousTuple);

			double stop = previousTuples.get(i).getStopTimestamp();

			ArrayList<ArrayList<byte[]>> sc = bottomup_sequenceConstruction_Hstacks_patternonly_expiring(
					prevousStackIndex, previousRIPindex, stacks, queryID,
					inputschArray, tempevent, stop, expiringTuples);

			for (int k = 0; k < sc.size(); k++) {
				ArrayList<byte[]> ar2 = new ArrayList<byte[]>();

				for (int h = 0; h < sc.get(k).size(); h++) {

					ar2.add(sc.get(k).get(h));
				}
				ar2.add(tempevent);

				boolean output = Utility.windowOpt(ar2, inputschArray);

				if (output) {
					Configure.resultNum += 1;
					// latency
					 Utility.accuLatency(ar2);

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

					/*
					 * System.out
					 * .println("======expiring triggered result for query======"
					 * + queryID);
					 * 
					 * System.out.println(StreamAccessor.toString(dest,
					 * schArray_Result));
					 */

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
	boolean[] checkQueryPatternConceptR(int queryID1, int queryID2) {

		boolean pattern = false;
		boolean concept = false;

		if (queryID1 == queryID2) {
			boolean[] relations = { pattern, concept };
			return relations;

		} else {
			ArrayList<String> QueryTypes1 = this.queries.get(queryID1).stackTypes;
			ArrayList<String> QueryTypes2 = this.queries.get(queryID2).stackTypes;
			int matchCounter = 0;
			if (QueryTypes1.size() == QueryTypes2.size()) {
				for (int i = 0; i < QueryTypes1.size(); i++) {
					String type2 = QueryTypes2.get(i);
					if (QueryTypes1.get(i).equalsIgnoreCase(type2)) {
						matchCounter++;
						continue;
					} else {

						if (semanticMatch(QueryTypes1.get(i), type2)
								|| semanticMatch(type2, QueryTypes1.get(i))) {
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
				int stringMatchingCounter = 0;
				int queryType1Index = 0;
				int queryType2Index = 0;
				while (queryType1Index < QueryTypes1.size()
						&& queryType2Index < QueryTypes2.size()) {
					if (QueryTypes1.get(queryType1Index).equalsIgnoreCase(
							QueryTypes2.get(queryType2Index))) {
						matchCounter++;
						stringMatchingCounter++;
						queryType1Index++;
						queryType2Index++;

					} else if (semanticMatch(QueryTypes1.get(queryType1Index),
							QueryTypes2.get(queryType2Index))) {
						matchCounter++;

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

	/**
	 * compute the nearest tuples for an given event instance and one query
	 * 
	 * @param queryID
	 * @param stacks
	 * @param stackIndex
	 * @param tempevent
	 * @param inputschArray
	 * @return
	 */
	ArrayList<byte[]> previousTuple_HStacks(int queryID,
			EventActiveInstanceQueue[] stacks, int stackIndex,
			byte[] tempevent, SchemaElement[] inputschArray) {
		String type = getTupleType(tempevent, inputschArray);

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
				// Utility.simulatePredicatesCost();

				if (previoustuple != null) {
					String previousType = getTupleType(previoustuple,
							inputschArray);

					String previousStackType = AIS[findStack(previousType)].stackType;
					// check in the HPG, the previousType - currenttype edge

					ArrayList<EdgeLabel> edges = table2.get(previousStackType);
					boolean found = false;

					if (isCurrentQuery(type, previousType, queryID)) {
						found = true;
					}
					/*
					 * for (int j = 0; edges != null && j < edges.size(); j++) {
					 * if (edges.get(j).queryID == queryID) {
					 * 
					 * found = true; break;
					 * 
					 * } }
					 */

					if (found)
						previousTuples.add(previoustuple);
				}

			}

		}
		return previousTuples;
	}

	/**
	 * only the lower query includes negation.
	 * 
	 * @param queryID
	 * @param stacks
	 * @param stackIndex
	 * @param tempevent
	 * @param inputschArray
	 * @return
	 */

	ArrayList<PreviousTuples> previousTuple_HStacks_bottomupReuse_negated_lower(
			int queryID, EventActiveInstanceQueue[] stacks, int stackIndex,
			byte[] tempevent, SchemaElement[] inputschArray) {

		String type = getTupleType(tempevent, inputschArray);
		stackTypes = getQuery(queryID).stackTypes;
		int comparedID = getQuery(queryID).computeSourceID;
		ArrayList<String> lower_stackTypes = new ArrayList<String>();
		lower_stackTypes = getQuery(comparedID).stackTypes;

		ArrayList<String> negatedTypes = new ArrayList<String>();
		ArrayList<String> uppernegatedTypes = new ArrayList<String>();

		// the upper level query doesn't include negative type
		if (Utility.getPositiveTypeNum(stackTypes) == stackTypes.size()) {
			for (int j = 0; j < lower_stackTypes.size(); j++) {
				String stype = lower_stackTypes.get(j);
				if (stype.startsWith("-")) {
					stype = stype.substring(1, stype.length());
					negatedTypes.add(stype);
				}
			}
		} else // both of the queries include negation
		{
			for (int j = 0; j < stackTypes.size(); j++) {
				String stype = stackTypes.get(j);
				if (stype.startsWith("-")) {
					stype = stype.substring(1, stype.length());
					negatedTypes.add(stype);
				}
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
			byte[][] retPointerArrayTemp = new byte[pointerSize][];
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
				// if there are no negative events, the bottom query has
				// computed the results
				found = false;
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
									&& negationPointedtimestamp < currentTime) {
								// no negation
								found = false;

							} else if (negationPointedtimestamp >= 0
									&& negationPointedtimestamp >= currentTime) {
								// have negation and it is the current negation
								// type, the result was filtered by the negative
								// event before.
								if (semanticcontains_notsensitive_position(
										negatedTypes, pType) >= 0) {
									found = true;
									break;

								}

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
		}
		return previousTuples;

	}

	ArrayList<PreviousTuples> previousTuple_HStacks_bottomupReuse_negated(
			int queryID, EventActiveInstanceQueue[] stacks, int stackIndex,
			byte[] tempevent, SchemaElement[] inputschArray) {

		String type = getTupleType(tempevent, inputschArray);
		stackTypes = getQuery(queryID).stackTypes;
		int comparedID = getQuery(queryID).computeSourceID;
		ArrayList<String> lower_stackTypes = new ArrayList<String>();
		lower_stackTypes = getQuery(comparedID).stackTypes;

		ArrayList<String> negatedTypes = new ArrayList<String>();

		// the upper level query doesn't include negative type
		if (Utility.getPositiveTypeNum(stackTypes) == stackTypes.size()) {
			for (int j = 0; j < lower_stackTypes.size(); j++) {
				String stype = lower_stackTypes.get(j);
				if (stype.startsWith("-")) {
					stype = stype.substring(1, stype.length());
					negatedTypes.add(stype);
				}
			}
		} else // both of the queries include negation
		{
			for (int j = 0; j < stackTypes.size(); j++) {
				String stype = stackTypes.get(j);
				if (stype.startsWith("-")) {
					stype = stype.substring(1, stype.length());
					negatedTypes.add(stype);
				}
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
			byte[][] retPointerArrayTemp = new byte[pointerSize][];
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
				// if there are no negative events, the bottom query has
				// computed the results
				found = false;
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
									&& negationPointedtimestamp < currentTime) {
								// no negation
								found = false;

							} else if (negationPointedtimestamp >= 0
									&& negationPointedtimestamp > currentTime) {
								// have negation and it is the current negation
								// type
								if (negatedTypes.contains(pType)) {
									found = false;
									break;
								} else {
									found = true;
								}

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
		}
		return previousTuples;

	}

	/**
	 * Single query includes negation
	 */

	ArrayList<PreviousTuples> previousTuple_HStacks_negated_forSingleQ(
			int queryID, EventActiveInstanceQueue[] stacks, int stackIndex,
			byte[] tempevent, SchemaElement[] inputschArray) {
		String currentType = getTupleType(tempevent, inputschArray)
				.toLowerCase();

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

				for (int i = 0; i < previousnegatedTuples.size(); i++) {
					byte[] negatedTuple = previousnegatedTuples.get(i);
					String pType = getTupleType(negatedTuple, inputschArray);

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
								String tuplebeforeNegType = getTupleType(
										tuplebeforeNeg, inputschArray);

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

						/*
						 * if (negationPointedtimestamp >= 0 &&
						 * negationPointedtimestamp < currentTime ||
						 * negationPointedtimestamp < 0) {
						 * 
						 * found = true; break;
						 * 
						 * }
						 */

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
	 * To support negation, the lower result un-constructed due to the existence
	 * of negative event type should be computed
	 * 
	 * @param queryID
	 * @param stacks
	 * @param stackIndex
	 * @param tempevent
	 * @param inputschArray
	 * @return
	 */

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

				ArrayList<PreviousTuples> previousTuples = previousTuple_HStacks_negated_forSingleQ(
						queryID, stacks, k, currenttuple, inputschArray);
				for (int i = 0; i < previousTuples.size(); i++) {
					byte[] previousTuple = previousTuples.get(i)
							.getPreviousTuple();
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

	public void produceinorder_HStacks(int queryID,
			EventActiveInstanceQueue[] stacks, int stackIndex,
			byte[] tempevent, SchemaElement[] inputschArray, int eventTypeNum) {

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

			// in the case of negation, we need to check whether the event pair
			// belongs to the current query
			ArrayList<ArrayList<byte[]>> sc = sequenceConstruction_Hstacks_negated(
					prevousStackIndex, previousRIPindex, stacks, queryID,
					inputschArray, tempevent, stopTime);

			for (int k = 0; k < sc.size(); k++) {
				ArrayList<byte[]> ar2 = new ArrayList<byte[]>();

				for (int h = 0; h < sc.get(k).size(); h++) {

					ar2.add(sc.get(k).get(h));
				}
				ar2.add(tempevent);

				boolean output = Utility.windowOpt(ar2, inputschArray);
				if (output) {
					Configure.resultNum += 1;

					// latency
					 Utility.accuLatency(ar2);

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

					/*
					 * System.out.println("======result======" + queryID);
					 * 
					 * 
					 * System.out.println(StreamAccessor.toString(dest,
					 * schArray_Result));
					 */

					ArrayList<ArrayList<byte[]>> result = this.resultBuffers
							.get(queryID);

					boolean reuse = false;
					for (int check = 0; check < this.orderedQueries.length; check++) {
						if (this.orderedQueries[check][0].computeSourceID == queryID
								&& this.orderedQueries[check][0].queryID != queryID) {
							reuse = true;
							break;
						}
					}

					/*
					 * boolean reuse = false; for (int check = 0; check <
					 * this.orderedQueries.length; check++) { if
					 * (this.orderedQueries[check][0].computeSourceID ==
					 * queryID) { reuse = true; break; } }
					 */
					// we only store the results for one query if it servers as
					// the
					// source for another query.
					if (reuse) {
						if (result != null) {

							this.resultBuffers.get(queryID).add(ar2);

						}

						else {
							result = new ArrayList<ArrayList<byte[]>>();
							result.add(ar2);
							this.resultBuffers
									.put(new Integer(queryID), result);

						}
					}

				}

			}

		}

	}

	void processQuery(int checkQIndex, String eventType, int stackIndex,
			byte[] tuple, SchemaElement[] schArray) {

		ArrayList<String> stackTypes = orderedQueries[checkQIndex][0].stackTypes;
		String type = stackTypes.get(stackTypes.size() - 1);

		// extended to support negation end
		if (type.startsWith("-")) {
			type = stackTypes.get(stackTypes.size() - 2);
		}

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.CHAOS.StreamOperator.StreamOperator#run(int)
	 */
	@Override
	public int run(int maxDequeueSize) {

		// long executionTimeStart = (new Date()).getTime();
		// Configure.previousresultNum = Configure.resultNum;

		// the schema for all input tuples is the same.
		StreamQueue inputQueue = getInputQueueArray()[0];
		SchemaElement[] schArray = inputQueue.getSchema();

		if (initStatus == false) {
			init(schArray);
		}

		for (int i = maxDequeueSize; i > 0; i--) {
			long execution_Start = (new Date()).getTime();

			Configure.previousresultNum = Configure.resultNum;
			byte[] tuple = inputQueue.dequeue();

			if (tuple == null)
				break;

			for (SchemaElement sch : schArray)
				sch.setTuple(tuple);

			//the current time = the timestamp of current tuple
			double timestamp = StreamAccessor.getDoubleCol(tuple, schArray, 1);

			// apply the aggressive purge, stored events with timestamp
			// less than the expiring timestamp should be removed.
			double expiringTimestamp = timestamp - Configure.windowsize;

			String eventType = getTupleType(tuple, schArray);

			// for latency measurement; set min time
			
			  if (semanticcontains_notsensitive_position(Utility
			  .lastQueryTypes(this.queries), eventType.toLowerCase()) >= 0) {
			  StreamAccessor.setMinTimestamp(tuple, (new Date()).getTime()); }
			 

			// find out the stack index matches the tuple event type
			int stackIndex = findStack(eventType);

			if (stackIndex >= 0 && AIS[stackIndex] != null) {

				// enqueue tuple and set up pointers.

				// store tuple to stackIndex position in AIS
				// pointer size should be greater than 1
				tuple_insertion(stackIndex, tuple);

				int len = this.orderedQueries.length;

				// in the bottom up computation, go though the ordered queries
				for (int iter = len - 1; iter >= 0; iter--) {

					int checkQIndex = iter;

					// if we decided that one query should be computed from
					// scratch
					if (this.orderedQueries[checkQIndex][0].computeSourceID == this.orderedQueries[checkQIndex][0].queryID) {

						processQuery(checkQIndex, eventType, stackIndex, tuple,
								schArray);

					} else {
						// we decided to reuse results from one lower level
						// query.

						// if only pattern is changed, we need to subtract
						// subsequence results
						// and check the miss tuples

						// if only concept is changed, we need to reuse results
						// and check the miss tuples also

						// if both levels changes, we need to plug in one
						// middle state

						int bottomqID = this.orderedQueries[checkQIndex][0].computeSourceID;
						int upper = this.orderedQueries[checkQIndex][0].queryID;

						// record the last lower level reused result' end
						// timestamp
						double visited = -1;
						if (!this.existingResultNums.isEmpty()
								&& this.existingResultNums
										.containsKey(bottomqID)
								&& this.existingResultNums.get(bottomqID)
										.containsKey(upper)) {
							visited = this.existingResultNums.get(bottomqID)
									.get(upper).doubleValue();

						}

						// record the last currently available lower level
						// results ' end timestamp
						double lastTimestamp = 0;
						if (!this.resultBuffers.isEmpty()
								&& this.resultBuffers.containsKey(bottomqID)
								&& this.resultBuffers.get(bottomqID).size() != 0) {
							ArrayList<byte[]> lastResult = this.resultBuffers
									.get(bottomqID).get(
											this.resultBuffers.get(bottomqID)
													.size() - 1);
							byte[] lastTuple = lastResult
									.get(lastResult.size() - 1);

							lastTimestamp = StreamAccessor.getDoubleCol(
									lastTuple, schArray, 1);
						}

						// more results are available for reuse
						if (visited < lastTimestamp) {
							int iresult = 0;

							if (!this.resultBuffers.isEmpty()
									&& this.resultBuffers
											.containsKey(bottomqID)) {

								ArrayList<String> topQueryTypes = getQuery(upper).stackTypes;

								SchemaElement[] schArray_Result = generateResultSchemas(
										Utility
												.getPositiveTypeNum(topQueryTypes),
										schArray);

								if (!this.existingResultNums.isEmpty()
										&& this.existingResultNums
												.containsKey(bottomqID)
										&& this.existingResultNums.get(
												bottomqID).containsKey(upper))
									// binary search the position that we need
									// to continue to reuse
									// in the lower level query result buffer
									iresult = 1 + Utility.binarySearch(
											this.resultBuffers, visited,
											schArray_Result, bottomqID);
								if (iresult < 0)
									iresult = 0;

								for (; iresult < this.resultBuffers.get(
										bottomqID).size(); iresult++) {

									// ar2 stores the lower level result for
									// reuse
									ArrayList<byte[]> ar2 = this.resultBuffers
											.get(bottomqID).get(iresult);

									// call reuse methods
									bottomup_pc_reuse_enqueueSubsequences(ar2,
											schArray, bottomqID, upper, tuple,
											stackIndex, iresult);
								}

								// update the reused results' end timestamp
								Hashtable<Integer, Double> existing = this.existingResultNums
										.get(new Integer(bottomqID));
								if (existing != null) {
									existing.put(new Integer(upper),
											new Double(lastTimestamp));
									this.existingResultNums.put(new Integer(
											bottomqID), existing);
								} else {
									existing = new Hashtable<Integer, Double>();
									existing.put(new Integer(upper),
											new Double(lastTimestamp));
									this.existingResultNums.put(new Integer(
											bottomqID), existing);
								}

							}
						}

						// delta result computation is not needed for concept
						// positive change
						boolean[] check = new boolean[2];
						ArrayList<String> middle = new ArrayList<String>();
						ArrayList<Integer> negatePos = new ArrayList<Integer>();
						check = checkQueryPatternConceptR(upper, bottomqID,
								middle, negatePos);
						{
							int size = getQuery(this.orderedQueries[checkQIndex][0].queryID).stackTypes
									.size();
							String upperTriggerType = getQuery(this.orderedQueries[checkQIndex][0].queryID).stackTypes
									.get(size - 1);
							int bottomSize = getQuery(bottomqID).stackTypes
									.size();
							String triggerType = getQuery(bottomqID).stackTypes
									.get(bottomSize - 1);

							missResultChecking(schArray, tuple, stackIndex,
									checkQIndex, bottomqID, upperTriggerType,
									eventType, triggerType);
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

			long executionTimeEnd = (new Date()).getTime();

			Configure.executionTime += executionTimeEnd - execution_Start;

			// first chart with x- execution time; y - total sequence results
			// generated
			// first run
			/*if (Configure.previousresultNum != Configure.resultNum) {
				System.out.println(Configure.executionTime + " "
						+ Configure.resultNum);

			}*/

			// second run-latency

			
			  if (Configure.previousresultNum != Configure.resultNum) {
			  System.out.println(Configure.latency + " " +
			  Configure.resultNum); }
			 

		}

		return 0;
	}

	/**
	 * Delta result construction
	 * 
	 * @param schArray
	 * @param tuple
	 * @param stackIndex
	 * @param checkQIndex
	 * @param bottomqID
	 * @param upperTriggerType
	 * @param eventType
	 * @param triggerType
	 */
	protected void missResultChecking(SchemaElement[] schArray, byte[] tuple,
			int stackIndex, int checkQIndex, int bottomqID,
			String upperTriggerType, String eventType, String triggerType) {

		ArrayList<String> middle = new ArrayList<String>();
		ArrayList<Integer> negatePos = new ArrayList<Integer>();
		// analyze the pattern relationships
		boolean[] relation = checkQueryPatternConceptR(
				this.orderedQueries[checkQIndex][0].queryID, bottomqID, middle,
				negatePos);

		// determine whether the negation is the same for the two queries,
		// if yes, we need to use the bottomup_processQuery_conceptonly_missing
		Boolean sameNegative = true;
		for (int i = 0; i < negatePos.size(); i++) {
			String negativeType_u = getQuery(
					this.orderedQueries[checkQIndex][0].queryID)
					.getStackTypes().get(negatePos.get(i));
			String negativeType_l = getQuery(bottomqID).getStackTypes().get(
					negatePos.get(i));

			negativeType_u = negativeType_u.substring(1, negativeType_u
					.length());
			negativeType_l = negativeType_l.substring(1, negativeType_l
					.length());

			if (!negativeType_u.equalsIgnoreCase(negativeType_l)) {
				sameNegative = false;
				break;
			}

		}

		// if pattern is changed / support negation
		// after submission, I need to re-analysis the cases I support
		if (relation[0] == true || relation[2] == true && sameNegative == false) {
			bottomup_processQuery_patternonly_missing(checkQIndex, eventType,
					stackIndex, tuple, schArray, negatePos);

		}
		// only concept is changed
		if (relation[1] == true && relation[2] != true) {
			// if the new arrival event can not trigger the lower query but
			// can trigger the upper query, it triggers the regular stack based
			// join for the upper query.
			if (semanticMatch(upperTriggerType, eventType)
					&& !semanticMatch(triggerType, eventType)) {
				processQuery(checkQIndex, eventType, stackIndex, tuple,
						schArray);

			}
			// we need to be careful about this case.
			// only a portion of events are joined each time to avoid generating
			// the results already constructed.
			else if (semanticMatch(triggerType, eventType)) {
				bottomup_processQuery_conceptonly_missing(checkQIndex,
						eventType, stackIndex, tuple, schArray, true);

			}

		}

		// only concept change on positive type, negative type is the same
		if (relation[1] && relation[2] && sameNegative) {
			if (semanticMatch(triggerType, eventType)) {
				bottomup_processQuery_conceptonly_missing_sameNegation(
						checkQIndex, eventType, stackIndex, tuple, schArray,
						true);

			}

		}

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
				if (AIS[prestackIndex].eventQueue != null)
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
	 * sequence construction triggered by expiring events
	 * 
	 * @param expiringTimestamp
	 * @param schArray
	 */
	protected void expiringTriggeredConstruction(double expiringTimestamp,
			SchemaElement[] schArray, int checkQIndex) {
		ArrayList<byte[]> expiringTuples = Utility.checkPurgeTuples(this.AIS,
				expiringTimestamp, schArray);

		// check the query and compute results if any before
		// expiring them. check whether exist some events in window without any
		// bottom query triggers after it. Such events should trigger the
		// construction involving the expiring ones.

		for (int i = 0; i < expiringTuples.size(); i++) {
			byte[] tuple = expiringTuples.get(i);

			String eventType = getTupleType(tuple, schArray);

			// if the purging event is the first event type in the upper query
			if (eventType
					.equalsIgnoreCase(this.orderedQueries[checkQIndex][0].stackTypes
							.get(0))) {
				ArrayList<String> stacks = getQuery(this.orderedQueries[checkQIndex][0].computeSourceID).stackTypes;

				String bottomqLastType = stacks.get(stacks.size() - 1);

				ArrayList<String> stacksupper = getQuery(this.orderedQueries[checkQIndex][0].queryID).stackTypes;

				String bottomqLastType_upper = stacksupper.get(stacksupper
						.size() - 1);

				// get the last one in window
				byte[] lastTupleofbottomLastType = this.AIS[findStack(bottomqLastType)].eventQueue
						.peekLast();
				double timestamp_lastE3 = StreamAccessor.getDoubleCol(
						lastTupleofbottomLastType, schArray, 1);

				byte[] windowlastTuple = this.AIS[findStack(bottomqLastType_upper)].eventQueue
						.peekLast();

				String stackType = this.AIS[findStack(bottomqLastType_upper)].stackType
						.toLowerCase();
				// I should use the stack type instead of the event type
				Hashtable<String, ArrayList<EdgeLabel>> table2 = this.hqp.hash1
						.get(stackType.toLowerCase());
				byte[][] retPointerArrayTemp = new byte[1][];
				if (table2 != null)
					retPointerArrayTemp = new byte[table2.keySet().size()][];

				// produce results for such last
				// events with the expiring
				// events.

				windowlastTuple = this.AIS[findStack(bottomqLastType_upper)].eventQueue
						.peekLast();

				double t = StreamAccessor.getDoubleCol(windowlastTuple,
						schArray, 1);
				if (timestamp_lastE3 < t) {

					while (windowlastTuple != null && timestamp_lastE3 < t) {
						int index = StreamAccessor.getIndex(windowlastTuple);

						bottomup_produceinorder_HStacks_expiring(
								this.orderedQueries[checkQIndex][0].queryID,
								AIS, findStack(bottomqLastType_upper),
								windowlastTuple, schArray, stacksupper.size(),
								false, expiringTuples);

						windowlastTuple = this.AIS[findStack(bottomqLastType_upper)]
								.getPreviousByPhysicalIndex(index,
										retPointerArrayTemp);
						t = StreamAccessor.getDoubleCol(windowlastTuple,
								schArray, 1);

					}
				}

			}
		}

	}

	/**
	 * Generate missing results for queries with only pattern changes
	 * 
	 * @param checkQIndex
	 *            the index for the query in ordered queries
	 * @param eventType
	 *            the incoming tuple
	 * @param stackIndex
	 *            the stack index for the incoming tulpe
	 * @param tuple
	 * @param schArray
	 * 
	 */
	void bottomup_processQuery_patternonly_missing(int checkQIndex,
			String eventType, int stackIndex, byte[] tuple,
			SchemaElement[] schArray, ArrayList<Integer> negatePos) {

		ArrayList<String> stackTypes = orderedQueries[checkQIndex][0].stackTypes;
		String type = stackTypes.get(stackTypes.size() - 1);

		String lowertype = null;

		int lowerQid = orderedQueries[checkQIndex][0].computeSourceID;
		lowertype = getQuery(lowerQid).stackTypes
				.get(getQuery(lowerQid).stackTypes.size() - 1);

		// if the event type is one child type of the trigger
		// type(the last event type for a query) in the concept hierarchy, it
		// will also trigger the missing results construction, I still need to
		// think about when it is the right time for missing results
		// construction

		if (semanticMatch(type, eventType) && lowertype.equalsIgnoreCase(type)) {
			int queryID = this.orderedQueries[checkQIndex][0].queryID;

			int stackTypeSize = 0;
			for (int i = 0; i < stackTypes.size(); i++) {
				if (!stackTypes.get(i).startsWith("-")) {
					stackTypeSize++;
				}
			}

			bottomup_produceinorder_HStacks_patternonly_missing(queryID, AIS,
					stackIndex, tuple, schArray, stackTypeSize, negatePos);

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
					//use ancestorMatch to compare two queries
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
	 * determines the parents of all of the queries given to the system uses the
	 * heuristic function when there are 2 direct parents
	 * 
	 * @return
	 */
	protected ArrayList<QueryInfo>[][] findParentsHeuristic() {
		// initialize array
		ArrayList<QueryInfo>[][] queryArray;
		queryArray = new ArrayList[queries.size()][2];

		for (int i = 0; i < queryArray.length; i++) {
			QueryInfo qi = queries.get(i);
			queryArray[i][0] = new ArrayList<QueryInfo>();
			queryArray[i][1] = new ArrayList<QueryInfo>();
			queryArray[i][0].add(queries.get(i));
			queryArray[i][1] = findParentHeuristic(queries.get(i));
		}

		return queryArray;
	}

	/**
	 * Currently, this function is not in used it was designed for heuristic
	 * based method
	 * 
	 * @param query
	 * @return
	 */
	public ArrayList<QueryInfo> findParentHeuristic(QueryInfo query) {
		ArrayList<QueryInfo> directParents = findAllDirectParents(query);
		ArrayList<QueryInfo> h1 = new ArrayList<QueryInfo>();
		ArrayList<QueryInfo> h2 = new ArrayList<QueryInfo>();

		if (directParents.size() == 0) {
			return directParents;
		} else if (directParents.size() == 1) {
			return directParents;
		} else {
			// heuristic 1: longest parent
			int maxLength = 0;

			for (int i = 0; i < directParents.size(); i++) {
				if (directParents.get(i).getSize() > maxLength) {
					maxLength = directParents.get(i).getSize();
					h1.clear();
					h1.add(directParents.get(i));
				} else if (directParents.get(i).getSize() == maxLength) {
					h1.add(directParents.get(i));
				}

			}

			if (h1.size() == 1) {
				return h1;
			} else {
				// heuristic 2: join types
				int max = 0;
				for (int i = 0; i < h1.size(); i++) {
					if (numSortedMerge(h1.get(i), query) > max) {
						max = numSortedMerge(h1.get(i), query);
						h2.clear();
						h2.add(h1.get(i));
					} else if (numSortedMerge(h1.get(i), query) == max) {
						h2.add(h1.get(i));
					}
				}
			}

			return h2;
		}

	}

	public int numSortedMerge(QueryInfo parent, QueryInfo child) {
		int num = 0;
		int parentIndex = parent.getSize() - 1;
		for (int i = child.getSize() - 1; i >= 0; i--) {
			if (!child.getStackTypes().get(i).equals(
					parent.getStackTypes().get(parentIndex))) {
				num++;
			} else {
				break;
			}
		}
		return num;
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
			//if the query does not have parent, which means it is the root
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