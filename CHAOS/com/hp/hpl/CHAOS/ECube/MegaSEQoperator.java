package com.hp.hpl.CHAOS.ECube;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import java.util.Hashtable;

import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamOperator.*;
import com.hp.hpl.CHAOS.MST.AdjacencyList;
import com.hp.hpl.CHAOS.MST.Edge;
import com.hp.hpl.CHAOS.MST.Edmonds;
import com.hp.hpl.CHAOS.MST.Node;
import com.hp.hpl.CHAOS.Queue.SingleReaderEventQueueArrayImp;
import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;
import com.hp.hpl.CHAOS.StreamData.StreamTupleCreator;
import com.hp.hpl.CHAOS.MST.*;

public class MegaSEQoperator extends SingleInputStreamOperator {

	static int query_ID = 1;

	// stores results for each query, used for duplicate removal and result
	// reuse;
	Hashtable<Integer, ArrayList<ArrayList<byte[]>>> resultBuffers = new Hashtable<Integer, ArrayList<ArrayList<byte[]>>>();
	Hashtable<String, Hashtable<Integer, Double>> lastPreviousIndexarr = new Hashtable<String, Hashtable<Integer, Double>>();

	// ArrayList<Integer> executionOrder = new ArrayList<Integer>();

	// event types for a single query
	ArrayList<String> stackTypes = new ArrayList<String>();

	// it stores all the submitted queries information
	ArrayList<QueryInfo> queries = new ArrayList<QueryInfo>();
	QueryInfo[][] orderedQueries = new QueryInfo[this.queries.size()][];

	// ArrayList<QueryInfo>[][] orderedQueries2;

	ArrayList<QueryInfo> execution = new ArrayList<QueryInfo>();

	// Hashtable<String, Double> LastestTimestamp = new Hashtable<String,
	// Double>();

	private boolean initStatus = false;

	Hashtable<Integer, Hashtable<Integer, Double>> existingResultNums = new Hashtable<Integer, Hashtable<Integer, Double>>();

	// Also, the firstCheck status should be recored per query.
	// Hashtable<Integer, Boolean> firstChecks = new Hashtable<Integer,
	// Boolean>();
	// whether the results of each query has been updated.

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

	public MegaSEQoperator(int operatorID, StreamQueue[] input,
			StreamQueue[] output) {
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
			queries
					.add(new QueryInfo(MegaSEQoperator.query_ID++,
							ETypes_AQuery));

			orderedQueries = orderQueries();
			queries.clear();
			for (int i = 0; i < orderedQueries.length; i++) {
				queries.add(orderedQueries[i][0]);
			}

		}

	}
	 
	/**
	 * put queries in order using a breadth first search
	 * 
	 * @return ordered array of queries and their parents
	 */
	public ArrayList<QueryInfo>[][] orderQueries2() {
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
					for (int j = 0; j < queryArray[i][1].size() - 1; j++) {
						if (placed.contains(queryArray[i][1].get(j))) {
							orderedArray[index][0] = queryArray[i][0];
							orderedArray[index][1] = queryArray[i][1];
							index++;

							placed.addAll(queryArray[i][0]);
						}
					}
				}
			}

		}
		return orderedArray;
	}

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

	public void forTestonly_setupExecutionOrder() {
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
					// the minimum one
					ArrayList<Integer> negatePos = new ArrayList<Integer>();

					check = checkQueryPatternConceptR(currentqID, childID,
							middle, negatePos);

					ArrayList<String> nonexistTypes = Bottomup_getNonexistTypes(
							currentqID, childID);
					if (nonexistTypes.size() != 0) {

						// both concept and pattern are changed
						if (check[0] == true && check[1] == true) {
							// here, I assume only one event type doesn't exist

							// use bottom up cp
							fromBottomCostTemp = 10;
						}
						// only pattern is changed
						else if (check[0] == true) {
							// use pattern bottom up
							fromBottomCostTemp = 10;
						}
					}

					// only concept is changed
					if (check[1] == true && check[0] == false) {
						// use concept bottom up
						fromBottomCostTemp = 10;
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
	 * Construct directed graph from query nodes; Translate these ECube query
	 * nodes to the MST nodes with directed edges and weights
	 * 
	 * @return
	 */
	protected AdjacencyList createDirectedGraph(ArrayList<Node> nodes) {
		int length = this.orderedQueries.length;

		nodes.add(0, new Node(0)); // add the virtual ground with id = -1;

		// create and add MST nodes. The name (queryID) is equal to the node
		// index
		for (int i = 0; i < length; i++) {
			int nodeName = this.orderedQueries[i][0].getQueryID();

			nodes.add(new Node(nodeName));
		}

		// from this point, I need to ajust the ordering of nodes according to
		// nameID

		for (int i = 1; i < nodes.size(); i++) {
			if (nodes.get(i).getName() != i) {
				// search for the i
				for (int j = i; j < nodes.size(); j++) {
					if (nodes.get(j).getName() == i) {
						Node inode = nodes.get(i);
						nodes.remove(i);
						Node jnode = nodes.get(j - 1);
						nodes.add(i, jnode);
						nodes.remove(j);
						if (j - 1 != i)
							nodes.add(j - 1, inode);
						else
							nodes.add(j, inode);
						break;
					}
				}
			}
		}

		AdjacencyList aList = new AdjacencyList();

		// assign cost to directed edge
		// consider every query node except the virtual ground
		for (int j = 1; j < nodes.size(); j++) {
			if (nodes.get(j) != null) {

				double selfCompute = this.CM.singleCompute(j);

				aList.addEdge(nodes.get(0), nodes.get(j), selfCompute);

				QueryInfo parent = getParent(new QueryInfo(j, (byte) 0));
				ArrayList<String> middle = new ArrayList<String>();
				if (parent != null) {
					int parentqID = parent.queryID;
					double fromTopCost = this.CM.topdownConCost_pattern(
							parentqID, j);

					setupParentEdges(fromTopCost, parentqID, nodes, nodes
							.get(j).getName(), middle, selfCompute, aList);
				}

				setupChildrenEdges(nodes, nodes.get(j).getName(), middle,
						selfCompute, aList);
			}
		}
		return aList;
	}

	/**
	 * set up the optimum execution ordering according to the MST results
	 * 
	 * @return
	 */
	private void setupOptimumExeOrder() {
		ArrayList<Node> nodes = new ArrayList<Node>();

		AdjacencyList aList = createDirectedGraph(nodes);

		// apply Edmond ALG to compute MST over the directed graph
		Edmonds ed = new Edmonds();

		AdjacencyList returnedList = ed.getMinBranching(nodes.get(0), aList);

		// translate the result to Ecube query execution order
		this.execution = translatefromMSTtoExecutionOrder(returnedList, nodes);

		// even though execution ordering is right, I need to sort it to make
		// sure
		// compute source query is listed before query id.

		for (int i = 0; i < this.execution.size(); i++) {
			int sourceID = this.execution.get(i).computeSourceID;
			// check whether sourceID has been processed.

			int j = 0;
			for (; j <= i; j++) {
				if (this.execution.get(j).queryID == sourceID) {
					// yes, processed before, we are good.
					break;
				}
			}

			if (j > i) {
				// no, not processed before, put the current query in the end
				QueryInfo iquery = this.execution.get(i);
				this.execution.remove(i);
				this.execution.add(iquery);
				i--;
			}

		}

		// need to sort orderedqueries

		// compute source query is listed before query id.

		for (int i = 0; i < this.orderedQueries.length; i++) {
			int sourceID = this.orderedQueries[i][0].computeSourceID;
			// check whether sourceID has been processed.

			int j = 0;
			for (; j <= i; j++) {
				if (this.orderedQueries[j][0].queryID == sourceID) {
					// yes, processed before, we are good.
					break;
				}
			}

			if (j > i) {
				// no, not processed before, put the current query in the end
				QueryInfo[] iquery = this.orderedQueries[i];
				for (int k = i; k < this.orderedQueries.length - 1; k++) {
					this.orderedQueries[k] = this.orderedQueries[k + 1];
				}
				this.orderedQueries[this.orderedQueries.length - 1] = iquery;

				i--;
			}

		}
		// need to tune up the cost model after submission

	}

	/**
	 * set up the directed edge pointing from directed parents
	 * 
	 * @param fromTopCost
	 * @param parentqID
	 * @param nodes
	 * @param check
	 * @param j
	 * @param middle
	 * @param selfCompute
	 * @param aList
	 */
	protected void setupParentEdges(double fromTopCost, int parentqID,
			ArrayList<Node> nodes, int j, ArrayList<String> middle,
			double selfCompute, AdjacencyList aList) {

		ArrayList<Integer> negatePos = new ArrayList<Integer>();

		boolean[] check = checkQueryPatternConceptR(parentqID, j, middle,
				negatePos);

		// both concept and pattern are changed
		if (check[0] == true && check[1] == true) {
			// use top down cp
			fromTopCost = CM.topdown_cp(parentqID, j);
		}
		// only pattern is changed
		else if (check[0] == true) {
			// use pattern top down
			fromTopCost = CM.topdownConCost_pattern(parentqID, j);
		}
		// only concept is changed
		else if (check[1] == true) {
			// use concept top down
			fromTopCost = CM.topdown_concept(parentqID);
		}

		// only store those cost less than self computation cost
		// temp bug
		if (fromTopCost < selfCompute && parentqID < this.queries.size()
				&& j < this.queries.size()) {
			aList.addEdge(nodes.get(parentqID), nodes.get(j), fromTopCost);
		}

	}

	/**
	 * set up the directed edges from directed children
	 * 
	 * @param nodes
	 * @param check
	 * @param j
	 * @param middle
	 * @param selfCompute
	 * @param aList
	 */
	protected void setupChildrenEdges(ArrayList<Node> nodes, int j,
			ArrayList<String> middle, double selfCompute, AdjacencyList aList) {
		ArrayList<QueryInfo> children = getChildren(getQuery(j));

		for (int k = 0; k < children.size(); k++) {
			int childID = children.get(k).queryID;

			double fromBottomCostTemp = 0;
			// for each child query, we should check the cost and chose
			// the minimum one
			ArrayList<Integer> negatePos = new ArrayList<Integer>();
			boolean[] check = checkQueryPatternConceptR(j, childID, middle,
					negatePos);
			ArrayList<String> nonexistTypes = Bottomup_getNonexistTypes(j,
					childID);
			if (nonexistTypes.size() != 0) {
				String nonexist = nonexistTypes.get(0);
				ArrayList<String> childrenTypes = getQuery(childID).stackTypes;
				int negationPos = childrenTypes.indexOf(nonexist);
				// both concept and pattern are changed
				if (check[0] == true && check[1] == true) {
					// here, I assume only one event type doesn't exist

					// use bottom up cp
					fromBottomCostTemp = CM
							.bottomup_cp(j, childID, negationPos);
				}
				// only pattern is changed
				else if (check[0] == true) {
					// use pattern bottom up
					fromBottomCostTemp = CM.bottomup_p(childID, negationPos);
				}
			}

			// only concept is changed
			if (check[1] == true && check[0] == false) {
				// use concept bottom up
				fromBottomCostTemp = CM.bottomup_concept(j, childID);
			}

			if (fromBottomCostTemp < selfCompute) {
				aList.addEdge(nodes.get(childID), nodes.get(j),
						fromBottomCostTemp);
			}

		}

	}

	/**
	 * After computing MST, we translate the execution order explicitly.
	 * 
	 * @param returnedList
	 * @param nodes
	 * @return
	 */
	public ArrayList<QueryInfo> translatefromMSTtoExecutionOrder(
			AdjacencyList returnedList, ArrayList<Node> nodes) {

		ArrayList<QueryInfo> queries = new ArrayList<QueryInfo>();
		for (int i = 0; i < nodes.size(); i++) {
			ArrayList<Edge> edgesn1 = returnedList.getAdjacent(nodes.get(i));
			for (int j = 0; edgesn1 != null && j < edgesn1.size(); j++) {
				/*
				 * System.out.println("from");
				 * edgesn1.get(j).getFrom().printName();
				 * System.out.println("to"); edgesn1.get(j).getTo().printName();
				 */
				QueryInfo qinfro = new QueryInfo(edgesn1.get(j).getTo()
						.getName(), (byte) 0);

				qinfro.computeSourceID = edgesn1.get(j).getFrom().getName();
				if (qinfro.computeSourceID == 0)
					qinfro.computeSourceID = qinfro.queryID;

				// update ordered queries array
				for (int k = 0; k < this.orderedQueries.length; k++) {
					if (this.orderedQueries[k][0].queryID == edgesn1.get(j)
							.getTo().getName()) {

						this.orderedQueries[k][0].computeSourceID = qinfro.computeSourceID;
						break;

					}
				}

				queries.add(qinfro);

			}
		}

		return queries;

	}

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
	 * set up the execution ordering here, I should only consider computing from
	 * the bottom.
	 */
	public void setupExecutionOrder() {

		for (int i = 0; i < this.orderedQueries.length; i++) {
			int currentqID = this.orderedQueries[i][0].queryID;
			double singleCost = this.CM.singleCompute(currentqID);
			double fromTopCost = Double.MAX_VALUE;
			double fromBottomCost = Double.MAX_VALUE;
			double min = singleCost;
			this.orderedQueries[i][0].setComputeSourceID(currentqID);

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
				}
				// only concept is changed
				else if (check[1] == true) {
					// use concept top down
					fromTopCost = CM.topdown_concept(parentqID);
				}

				if (fromTopCost < min) {
					min = fromTopCost;
					if (!(getQuery(parentqID).computeSourceID == this.orderedQueries[i][0].queryID))

						this.orderedQueries[i][0].setComputeSourceID(parentqID);
				}
			}

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
						}
					}

					// only concept is changed
					if (check[1] == true && check[0] == false) {
						// use concept bottom up
						fromBottomCostTemp = CM.bottomup_concept(currentqID,
								childID);
					}

					if (fromBottomCostTemp < fromBottomCost) {
						fromBottomCost = fromBottomCostTemp;
						childIDchosen = childID;
					}

				}

				if (fromBottomCost < min) {
					min = fromBottomCost;
					if (!(getQuery(childIDchosen).computeSourceID == this.orderedQueries[i][0].queryID))
						this.orderedQueries[i][0]
								.setComputeSourceID(childIDchosen);
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

		CM.setOrderedQueries(orderedQueries);
		// setupExecutionOrder();
		// long edmond_start = (new Date()).getTime();

		setupOptimumExeOrder();
		// long edmond_end = (new Date()).getTime();
		/*
		 * System.out.println("start:" + edmond_start + "end:"+ edmond_end);
		 * System.out.println("edmond time" + (edmond_end - edmond_start));
		 */
		// analyzeOrderedQueries();
		// forTestonly_setupExecutionOrder();
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

			if (currenttimestamp >= timestamp) {
				array.add(currenttuple);// here, I should compare the timestamp
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
			/*
			 * double timestamp = StreamAccessor.getDoubleCol(tempevent,
			 * inputschArray, 1);
			 */
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

	// get memory consumption
	int getMemoryStatistics() {
		int buffersize = 0;

		// buffer size
		for (int iter = 0; iter < this.execution.size(); iter++) {
			int queryID = this.execution.get(iter).queryID;
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

		}
		return previousTuples;

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

					/*System.out.println("======result======" + queryID);

					System.out.println(StreamAccessor.toString(dest,
							schArray_Result));
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.CHAOS.StreamOperator.StreamOperator#run(int)
	 */
	@Override
	public int run(int maxDequeueSize) {

		// the schema for all input tuples is the same.
		StreamQueue inputQueue = getInputQueueArray()[0];
		SchemaElement[] schArray = inputQueue.getSchema();

		if (initStatus == false) {
			init(schArray);
		}

		for (int i = maxDequeueSize; i > 0; i--) {
			long execution_Start = (new Date()).getTime();

			// first, tuple insertion with pointer set up
			Configure.previousresultNum = Configure.resultNum;
			byte[] tuple = inputQueue.dequeue();

			if (tuple == null)
				break;

			for (SchemaElement sch : schArray)
				sch.setTuple(tuple);

			double timestamp = StreamAccessor.getDoubleCol(tuple, schArray, 1);

			// here, first apply the aggressive purge
			double expiringTimestamp = timestamp - Configure.windowsize;

			String eventType = getTupleType(tuple, schArray);

			// for latency measurement; set min time
			
			  if (semanticcontains_notsensitive_position(Utility
			  .lastQueryTypes(this.queries), eventType.toLowerCase()) >= 0) {
			  StreamAccessor.setMinTimestamp(tuple, (new Date()).getTime()); }
			

			// find out the stack index matching the tuple event type
			int stackIndex = findStack(eventType);

			if (stackIndex >= 0 && AIS[stackIndex] != null) {
				// enqueue tuple and set up pointers.

				// pointer size should be greater than 1
				tuple_insertion(stackIndex, tuple);

				// go through the ordered queries

				for (int checkQIndex = 0; checkQIndex < this.orderedQueries.length; checkQIndex++) {

					int queryID = this.orderedQueries[checkQIndex][0].queryID;
					int sourceID = this.orderedQueries[checkQIndex][0].computeSourceID;

					// int computeSourceID = getQuery(queryID).computeSourceID;

					// if the compute source ID is equal to 0, then self
					// compute.
					if (queryID == sourceID) {
						// check whether other query's compute sourceID equal to
						// the current queryID,
						// if yes, we need to store the result for further
						// computation
						processQuery(checkQIndex, eventType, stackIndex, tuple,
								schArray);

					}

					else if (this.orderedQueries[checkQIndex][1] != null
							&& this.orderedQueries[checkQIndex][1].queryID == sourceID) {
						// compute from the upper query by top down method
						computefromTop(checkQIndex, schArray, tuple);
					}

					else {

						// compute from the bottom
						computefromBottom(eventType, checkQIndex, stackIndex,
								tuple, sourceID, queryID, schArray);

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
	/*		if (Configure.previousresultNum != Configure.resultNum) {
				System.out.println(Configure.executionTime + " "
						+ Configure.resultNum);

			}*/

			// second run-latency
			
			  if (Configure.previousresultNum != Configure.resultNum) {
			  System.out.println(Configure.latency + " " +
			  Configure.resultNum);
			  
			  }
			 

		}

		return 0;
	}

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

			//System.out.println("reuse for query: " + currentQueryID);

			Configure.resultNum += 1;

			// latency
			 Utility.accuLatency(ar2);

			// need duplicate removal

			//System.out.println(StreamAccessor.toString(dest, schArray_Result));

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

	protected void computefromBottom(String eventType, int checkQIndex,
			int stackIndex, byte[] tuple, int bottomqID, int upper,
			SchemaElement[] schArray) {
		// we decided to reuse results from one lower level
		// query.

		// if only pattern is changed, we need to subtract
		// subsequence results
		// and check the miss tuples

		// if only concept is changed, we need to reuse results
		// and check the miss tuples also

		// if both levels changes, we need to plug in one
		// middle state

		// record the last lower level reused result' end
		// timestamp
		double visited = -1;
		if (!this.existingResultNums.isEmpty()
				&& this.existingResultNums.containsKey(bottomqID)
				&& this.existingResultNums.get(bottomqID).containsKey(upper)) {
			visited = this.existingResultNums.get(bottomqID).get(upper)
					.doubleValue();

		}

		// record the last currently available lower level
		// results ' end timestamp
		double lastTimestamp = 0;
		if (!this.resultBuffers.isEmpty()
				&& this.resultBuffers.containsKey(bottomqID)
				&& this.resultBuffers.get(bottomqID).size() != 0) {
			ArrayList<byte[]> lastResult = this.resultBuffers.get(bottomqID)
					.get(this.resultBuffers.get(bottomqID).size() - 1);
			byte[] lastTuple = lastResult.get(lastResult.size() - 1);

			lastTimestamp = StreamAccessor.getDoubleCol(lastTuple, schArray, 1);
		}

		// more results are available for reuse
		if (visited < lastTimestamp) {
			int iresult = 0;

			if (!this.resultBuffers.isEmpty()
					&& this.resultBuffers.containsKey(bottomqID)) {

				ArrayList<String> topQueryTypes = getQuery(upper).stackTypes;

				SchemaElement[] schArray_Result = generateResultSchemas(Utility
						.getPositiveTypeNum(topQueryTypes), schArray);

				if (!this.existingResultNums.isEmpty()
						&& this.existingResultNums.containsKey(bottomqID)
						&& this.existingResultNums.get(bottomqID).containsKey(
								upper))
					// binary search the position that we need
					// to continue to reuse
					// in the lower level query result buffer
					iresult = 1 + Utility.binarySearch(this.resultBuffers,
							visited, schArray_Result, bottomqID);
				if (iresult < 0)
					iresult = 0;

				for (; iresult < this.resultBuffers.get(bottomqID).size(); iresult++) {

					// ar2 stores the lower level result for
					// reuse
					ArrayList<byte[]> ar2 = this.resultBuffers.get(bottomqID)
							.get(iresult);

					// call reuse methods
					bottomup_pc_reuse_enqueueSubsequences(ar2, schArray,
							bottomqID, upper, tuple, stackIndex, iresult);
				}

				// update the reused results' end timestamp
				Hashtable<Integer, Double> existing = this.existingResultNums
						.get(new Integer(bottomqID));
				if (existing != null) {
					existing.put(new Integer(upper), new Double(lastTimestamp));
					this.existingResultNums.put(new Integer(bottomqID),
							existing);
				} else {
					existing = new Hashtable<Integer, Double>();
					existing.put(new Integer(upper), new Double(lastTimestamp));
					this.existingResultNums.put(new Integer(bottomqID),
							existing);
				}

			}
		}

		// delta result computation is not needed for concept
		// positive change
		boolean[] check = new boolean[2];
		ArrayList<String> middle = new ArrayList<String>();
		ArrayList<Integer> negatePos = new ArrayList<Integer>();
		check = checkQueryPatternConceptR(upper, bottomqID, middle, negatePos);
		{
			int size = getQuery(this.orderedQueries[checkQIndex][0].queryID).stackTypes
					.size();
			String upperTriggerType = getQuery(this.orderedQueries[checkQIndex][0].queryID).stackTypes
					.get(size - 1);
			int bottomSize = getQuery(bottomqID).stackTypes.size();
			String triggerType = getQuery(bottomqID).stackTypes
					.get(bottomSize - 1);

			missResultChecking(schArray, tuple, stackIndex, checkQIndex,
					bottomqID, upperTriggerType, eventType, triggerType);
		}
	}

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

					/*System.out.println("======result======" + queryID);
					System.out.println(StreamAccessor.toString(dest,
							schArray_Result));*/

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

					/*System.out.println("======result======" + queryID);
					System.out.println(StreamAccessor.toString(dest,
							schArray_Result));*/

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

		}
		return previousTuples;

	}

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

					/*System.out.println("======miss result======" + queryID);
					System.out.println(StreamAccessor.toString(dest,
							schArray_Result));*/
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

	public QueryInfo[] getAllParents(int queryID) {
		// initialize array
		ArrayList<QueryInfo> allParents = new ArrayList<QueryInfo>();
		QueryInfo[] queryArray = new QueryInfo[queries.size()];

		for (int i = 0; i < queryArray.length; i++) {
			queryArray[i] = queries.get(i);
		}

		QueryInfo possibleParent;
		// find parents
		for (int i = 0; i < queryArray.length; i++) {
			possibleParent = queryArray[i];
			for (int j = 0; j < queryArray.length; j++) {
				if (i != j) {
					// check to see if it is an ancestor
					if (ancestorMatch(possibleParent, queryArray[j])) {
						boolean isParent = true;
						// now check to see if it is a direct parent
						for (int k = 0; k < allParents.size(); k++) {
							if (ancestorMatch(possibleParent, allParents.get(k))) {
								isParent = false;
								break;
							}
						}

						// remove any ancestors of the query being checked,
						// since they can't be direct parents
						for (int k = 0; k < allParents.size(); k++) {
							if (ancestorMatch(allParents.get(k), possibleParent)) {
								allParents.remove(k);
							}
						}

						// if the query in question was found to be a parent,
						// add it to the list of parents
						if (isParent) {
							allParents.add(possibleParent);
						}
					}

				}
			}

		}

		QueryInfo[] allParentsArray = new QueryInfo[allParents.size()];
		for (int i = 0; i < allParents.size(); i++) {
			allParentsArray[i] = allParents.get(i);
		}

		return allParentsArray;

		// return allParents;
	}

	protected boolean checkVisited(int upperqID, int downqID) {
		if (!this.existingResultNums.isEmpty()
				&& this.existingResultNums.containsKey(upperqID)
				&& this.existingResultNums.get(upperqID).containsKey(downqID))
			return true;
		else
			return false;
	}

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

	protected void computefromTop(int checkQIndex, SchemaElement[] schArray,
			byte[] tuple) {

		int upperqID = this.orderedQueries[checkQIndex][0].computeSourceID;
		int downqID = this.orderedQueries[checkQIndex][0].queryID;

		double visitedLastTime = -1;
		if (checkVisited(upperqID, downqID)) {
			visitedLastTime = this.existingResultNums.get(upperqID)
					.get(downqID).doubleValue();

		}
		double lastTimestamp = lastVisitUpperQuery(upperqID, schArray);

		if (!this.resultBuffers.isEmpty()
				&& this.resultBuffers.containsKey(upperqID)) {

			// top down evaluation
			topdown_enqueueSubsequences(upperqID,
					this.orderedQueries[checkQIndex][0].queryID,
					this.resultBuffers.get(upperqID), schArray,
					visitedLastTime, tuple);

			recordVisitedBufferTuple(checkQIndex, lastTimestamp);

		}
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

						// Utility.simulatePredicatesCost();
						tIndex--;
						insertPos = -1;
					}
					// compare the timestamp

					else if (insertPos == 0 && timestamp < aftertime) {
						{
							array_updated.add(currenttuple);
							added = true;
						}

						// Utility.simulatePredicatesCost();
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

					// Utility.simulatePredicatesCost();
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

						// Utility.simulatePredicatesCost();
						tIndex--;
						insertPos = -1;
					}
					// compare the timestamp

					else if (insertPos == 0 && timestamp < aftertime) {
						for (byte[] addingTuple : currenttuple) {
							array_updated.add(addingTuple);
							added = true;
						}

						// Utility.simulatePredicatesCost();
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

					// Utility.simulatePredicatesCost();
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
						// Utility.simulatePredicatesCost();
						tIndex--;
						insertPos = -1;
					}
					// compare the timestamp

					else if (insertPos == 0 && timestamp < aftertime) {
						array_updated.add(currenttuple);
						added = true;
						// Utility.simulatePredicatesCost();
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
					// Utility.simulatePredicatesCost();
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

	ArrayList<ArrayList<byte[]>> topdown_sequenceConstruction_Hstacks_negated(
			int k, int index, EventActiveInstanceQueue[] stacks,
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

				ArrayList<PreviousTuples> previousTuples = topdown_previousTuple_HStacks_negated(
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
							topdown_sequenceConstruction_Hstacks_negated(
									sIndex, previousRIPindex, stacks,
									stackTypes, inputschArray, currenttuple,
									stop), inputschArray);
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

	public void topdown_produceinorder_HStacks(int queryID,
			EventActiveInstanceQueue[] stacks, int stackIndex,
			byte[] tempevent, SchemaElement[] inputschArray, int eventTypeNum) {

		ArrayList<String> stackTypes = getQuery(queryID).stackTypes;

		ArrayList<PreviousTuples> previousTuples = topdown_previousTuple_HStacks_negated(
				stackTypes, stacks, stackIndex, tempevent, inputschArray);

		for (int i = 0; i < previousTuples.size(); i++) {
			byte[] previousTuple = previousTuples.get(i).getPreviousTuple();
			double stopTime = previousTuples.get(i).getStopTimestamp();

			String type = Utility.getTupleType(previousTuple, inputschArray)
					.toLowerCase();
			int prevousStackIndex = findStack(type);

			int previousRIPindex = StreamAccessor.getIndex(previousTuple);

			ArrayList<ArrayList<byte[]>> sc = topdown_sequenceConstruction_Hstacks_negated(
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

	ArrayList<PreviousTuples> topdown_previousTuple_HStacks_negated(
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
				// simulate the predicate evaluation cost
				// Utility.simulatePredicatesCost();
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

	public ArrayList<ArrayList<byte[]>> topdown_produceinorder_HStacks(
			ArrayList<String> stackTypes, EventActiveInstanceQueue[] stacks,
			int stackIndex, byte[] tempevent, SchemaElement[] inputschArray,
			int eventTypeNum) {

		ArrayList<ArrayList<byte[]>> results = new ArrayList<ArrayList<byte[]>>();
		ArrayList<PreviousTuples> previousTuples = topdown_previousTuple_HStacks_negated(
				stackTypes, stacks, stackIndex, tempevent, inputschArray);

		for (int i = 0; i < previousTuples.size(); i++) {
			byte[] previousTuple = previousTuples.get(i).getPreviousTuple();
			double stopTime = previousTuples.get(i).getStopTimestamp();

			String type = Utility.getTupleType(previousTuple, inputschArray)
					.toLowerCase();
			int prevousStackIndex = findStack(type);

			int previousRIPindex = StreamAccessor.getIndex(previousTuple);

			ArrayList<ArrayList<byte[]>> sc = topdown_sequenceConstruction_Hstacks_negated(
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

		// I need to compute the joining events position
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

					ArrayList<ArrayList<byte[]>> nonTypesresults = new ArrayList<ArrayList<byte[]>>();

					nonTypesresults = topdown_produceinorder_HStacks(
							nonexistTypes, AIS, stackIndex, offTuple, schArray,
							nonexistTypes.size());

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

				for (int i = lastIndex + 1; i < resultBuffer.size() - 1; i++) {
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
					nonTypesresults = topdown_produceinorder_HStacks(
							nonexistTypes, AIS, stackIndex, tuple, schArray,
							nonexistTypes.size());

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

					//System.out.println("======reuse for query======" + queryID);

				} else {
					// Utility.rewriteToFile("======result for query======"
					// + queryID);
					//System.out
							//.println("======result for query======" + queryID);

				}

				// Utility.rewriteToFile(StreamAccessor.toString(dest,
				// schArray_Result));

				//System.out.println(StreamAccessor.toString(dest,
						//schArray_Result));

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

	boolean[] topdown_checkQueryPatternConceptR(int queryID1, int queryID2,
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

	public void topdown_enqueueSubsequences(int topqID, int bottomqID,
			ArrayList<ArrayList<byte[]>> upperRBuffer,
			SchemaElement[] schArray, double lastVisited, byte[] tuple) {
		{
			// boolean indicator between queries for pattern, concept changes
			// and negation
			boolean[] check = new boolean[4];
			ArrayList<String> middle = new ArrayList<String>();
			ArrayList<Integer> negatePos = new ArrayList<Integer>();
			check = topdown_checkQueryPatternConceptR(topqID, bottomqID,
					middle, negatePos);

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
						// Utility.simulatePredicatesCost();

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

				////////////////////////////////////////////////////////////////

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
						// Utility.simulatePredicatesCost();

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

	public void bufferResult(int queryID, byte[] dest,
			SchemaElement[] schArray_Result, ArrayList<byte[]> ar2,
			boolean reuseresults, SchemaElement[] schArray) {

		if (Utility.windowOpt(ar2, schArray)) {
			getQuery(queryID).donestatus = (byte) 1;

			Configure.resultNum += 1;
			// latency
			 Utility.accuLatency(ar2);
			if (reuseresults) {
				//System.out.println("======reuse======" + queryID);

				// Utility.rewriteToFile("======reuse======" + queryID);
			} else {
				// Utility.rewriteToFile("======result======" + queryID);

				//System.out.println("======result======" + queryID);
			}
			// Utility.rewriteToFile(StreamAccessor
			// .toString(dest, schArray_Result));

			//System.out.println(StreamAccessor.toString(dest, schArray_Result));
		}

		boolean reuse = false;
		for (int check = 0; check < this.orderedQueries.length; check++) {
			if (this.orderedQueries[check][0].computeSourceID == queryID
					&& this.orderedQueries[check][0].queryID != queryID) {
				reuse = true;
				break;
			}
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

			queryArray[i][0].add(queries.get(i));
			queryArray[i][1] = findParentHeuristic(queries.get(i));
		}

		return queryArray;
	}

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
		// temp bug
		if (i == orderedQueries.length) {
			i--;
		}
		return orderedQueries[i][1];
		/*
		 * if (orderedQueries[i][1].getQueryID() == child.getQueryID()) { return
		 * orderedQueries[i][1]; } else { return null; }
		 */
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
