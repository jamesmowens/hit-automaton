package com.hp.hpl.CHAOS.Rewriting;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import java.util.Hashtable;

import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamOperator.*;
import com.hp.hpl.CHAOS.Queue.SingleReaderEventQueueArrayImp;
import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;
import com.hp.hpl.CHAOS.StreamData.StreamTupleCreator;

public class Shared_MegaSEQoperator extends SingleInputStreamOperator {

	// collect results after run
	ArrayList<String> resultCollection = new ArrayList<String>();

	static int query_ID = 0;

	Hashtable<Integer, ArrayList<ArrayList<byte[]>>> resultBuffers = new Hashtable<Integer, ArrayList<ArrayList<byte[]>>>();

	// event types for a single query
	ArrayList<String> stackTypes = new ArrayList<String>();

	// it stores all the submitted queries information
	ArrayList<QueryInfo> queries = new ArrayList<QueryInfo>();

	// for each root query ID, exist a set of sPoint
	Hashtable<Integer, ArrayList<String>> sPoint = new Hashtable<Integer, ArrayList<String>>();

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

	public Shared_MegaSEQoperator(int operatorID, StreamQueue[] input,
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

				query.setRoot(true);

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
				if (!contains_notsensitive(stackTypes, result[x])) {

					stackTypes.add(result[x].toLowerCase());

				}

			}

			query.setStackTypes(ETypes_AQuery);
			query.setChildren(children);
			query.setParents(parents);
			query.setMinusQueriesID(minusQueriesID);
			query.setClusteredQueryIDs(clusteredQueryIDs);

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
		// long executionTimeStart =

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
		// iterate all query and find out the root queries with clusters
		for (int r = 0; r < this.queries.size(); r++) {
			int qID = this.queries.get(r).getQueryID();

			if (this.queries.get(r).getClusteredQueryIDs().size() != 0) {

				ArrayList<String> points = getPoints(qID);

				this.sPoint.put(new Integer(qID), points);

			}
		}

		initStatus = true;
		return 1;
	}

	/**
	 * compute the event types before which have extra positive and negative
	 * event types.
	 * 
	 * @return
	 */
	protected ArrayList<String> getPoints(int queryID) {
		// get the clustered query IDs for the given query

		ArrayList<Integer> clusteredQueryIDs = getQuery(queryID)
				.getClusteredQueryIDs();

		ArrayList<String> points = new ArrayList<String>();
		ArrayList<String> types = getQuery(queryID).stackTypes;

		for (int i = 1; i < clusteredQueryIDs.size(); i++) {
			ArrayList<String> cTypes = getQuery(clusteredQueryIDs.get(i))
					.getStackTypes();
			int tempindex = 0;
			for (int j = 0; j < cTypes.size(); j++) {
				tempindex = types.indexOf(cTypes.get(j));
				if (tempindex == -1) {
					continue;
				} else if (j > 1 && types.indexOf(cTypes.get(j - 1)) == -1
						&& tempindex > 0) {
					if (!points.contains(cTypes.get(j)))
						points.add(cTypes.get(j));
				}

			}
		}
		return points;
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
	ArrayList<ResultRVI> connect(byte[] currenttuple, ArrayList<ResultRVI> ear,
			SchemaElement[] inputschArray, EventActiveInstanceQueue[] stacks,
			int queryID, int stackPosition, double leftTimeStamp) {
		ArrayList<ResultRVI> earray = new ArrayList<ResultRVI>();
		// the final result

		for (int i = 0; i < ear.size(); i++) {
			ArrayList<byte[]> array = new ArrayList<byte[]>();
			array = ear.get(i).getResult();

			byte[] lastTuple = array.get(array.size() - 1);
			double timestamp = StreamAccessor.getDoubleCol(lastTuple,
					inputschArray, 1);

			if (timestamp < leftTimeStamp) {
				continue;
			}

			// test start
			/*
			 * if (timestamp == 34394.707 || timestamp == 34391.409 || timestamp
			 * == 34386.535) System.out.println("bug");
			 */
			// test end

			double currenttimestamp = StreamAccessor.getDoubleCol(currenttuple,
					inputschArray, 1);

			// test start
			// if (currenttimestamp == 34680.333 && timestamp == 34673.886)
			// System.out.println("bug");
			// test end

			// compare the timestamp
			if (currenttimestamp > timestamp) {

				// check if the current tuple is a checking point
				String eventType = getTupleType(currenttuple, inputschArray);

				if (contains_notsensitive(this.sPoint.get(queryID), eventType)) {
					ArrayList<ArrayList<Integer>> checkingQid = ear.get(i)
							.getCheckingQid();

					ArrayList<Integer> checking = new ArrayList<Integer>();
					// check whether it fails some query
					int stackIndex = findStack(eventType);

					boolean allFail = true;

					// note, I shouldn't apply this.queries that much, actually,
					// for a given queryID, I should only check its clustered
					// queries

					// get clustered queries
					ArrayList<Integer> clusteredQueries = getQuery(queryID)
							.getClusteredQueryIDs();

					for (int j = 0; j < clusteredQueries.size(); j++) {

						// if (clusteredQueries.get(j) >= 0)
						{

							// cost model start
							// long execution_Start_check = (new
							// Date()).getTime();

							boolean add = checkPoints(lastTuple, currenttuple,
									clusteredQueries.get(j), stacks,
									stackIndex, inputschArray);
							// long execution_End_check = (new
							// Date()).getTime();
							// long exeCheckTime = execution_End_check
							// - execution_Start_check;
							// System.out.println(exeCheckTime);
							// cost model end
							if (add) {

								// || !add && this.queries.get(j).queryID < 0

								// being true is not good enough, we need to see
								// if its minus queries are not satisfied.

								ArrayList<Integer> minusQueriesID = getQuery(
										clusteredQueries.get(j))
										.getMinusQueriesID();

								// process the combo query
								// for every tuple within the interval.

								boolean shouldAdd = true;

								if (minusQueriesID.size() > 0) {
									// get the root query in the minusQueries

									int rootID = minusQueriesID.get(0)
											.intValue();

									ArrayList<String> cstackTypes = getQuery(
											rootID).getStackTypes();

									boolean hasResults = computeChildrenQueries(
											rootID, cstackTypes, array,
											timestamp, currenttimestamp,
											inputschArray);

									if (hasResults) {
										shouldAdd = false;
									}

								}

								if (shouldAdd) {
									checking.add(new Integer(1));
									allFail = false;
								}

							} else {
								checking.add(new Integer(0));
							}

							// checkingQid.add(checking);
						}

					}

					// stop evaluation when all queries fail one checking point
					if (!allFail) {
						checkingQid.add(checking);

						array.add(currenttuple);

						ArrayList<byte[]> array2 = new ArrayList<byte[]>();
						for (int j = 0; j < array.size(); j++) {
							array2.add(array.get(j));
						}

						// if yes, call function and check the queries it
						// matches.
						ResultRVI rvi = new ResultRVI(checkingQid, array2);

						earray.add(rvi);
					}

				}

				else {
					// no update on checking qid
					ArrayList<ArrayList<Integer>> checkingQid = ear.get(i)
							.getCheckingQid();

					// checkingQid.add(new Integer());
					array.add(currenttuple);

					// maybe each time, I call the connect, I should check for
					// each
					// connecting tuple,
					// whether query has satisfied.

					ArrayList<byte[]> array2 = new ArrayList<byte[]>();
					for (int j = 0; j < array.size(); j++) {
						array2.add(array.get(j));
					}

					// if yes, call function and check the queries it matches.
					ResultRVI rvi = new ResultRVI(checkingQid, array2);

					earray.add(rvi);
				}

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

		// I shouldn't limit the number of negations relative to the positions.
		// It should return true if all the event types in between are negative.
		else {
			int i = 0;
			for (i = preposition + 1; i < position; i++) {
				if (!getQuery(queryID).stackTypes.get(i).startsWith("-")) {

					break;
				}
			}

			if (i == position) {
				isCurrentQ = true;
			}
		}

		return isCurrentQ;
	}

	/**
	 * extended to support negation get the previous positive tuples before a
	 * given event for a given query. We considered previous positive(with no
	 * negatives).
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

				previoustuple = retPointerArrayTemp[i];
				double timestamp2 = StreamAccessor.getDoubleCol(previoustuple,
						inputschArray, 1);

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
	ArrayList<ResultRVI> sequenceConstruction_Hstacks(int k, int index,
			EventActiveInstanceQueue[] stacks, int queryID,
			SchemaElement[] inputschArray, byte[] nextTuple,
			double timestampleft) {

		ArrayList<ResultRVI> earray = new ArrayList<ResultRVI>();
		ArrayList<ResultRVI> earray3 = new ArrayList<ResultRVI>();
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
				ArrayList<ArrayList<Integer>> checkingQid = new ArrayList<ArrayList<Integer>>();

				ResultRVI rvi = new ResultRVI(checkingQid, array);

				earray.add(rvi);

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
							inputschArray, tuple, timestampleft),
							inputschArray, stacks, queryID, k, timestampleft);
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

	ArrayList<ResultRVI> sequenceConstruction_Hstacks_negated_oneResult(int k,
			int index, EventActiveInstanceQueue[] stacks, int queryID,
			SchemaElement[] inputschArray, byte[] nextTuple, double stopTime,
			double timestampleft) {

		ArrayList<ResultRVI> earray = new ArrayList<ResultRVI>();
		ArrayList<ResultRVI> earray3 = new ArrayList<ResultRVI>();
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

					ArrayList<ArrayList<Integer>> checkingQid = new ArrayList<ArrayList<Integer>>();
					ResultRVI rvi = new ResultRVI(checkingQid, array2);

					earray.add(rvi);

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

			while (currenttuple != null) {

				index = StreamAccessor.getIndex(currenttuple);

				ArrayList<PreviousTuples> previousTuples = previousTuple_HStacks_negated(
						queryID, stacks, k, currenttuple, inputschArray);
				boolean loop = true;
				while (previousTuples.size() != 0 && loop) {

					for (int i = 0; i < previousTuples.size(); i++) {
						byte[] previousTuple = previousTuples.get(i)
								.getPreviousTuple();

						String previousTupleType = getTupleType(previousTuple,
								inputschArray);

						// check if the event type is actually negative
						// (rewritten)
						// actually, I am trying to get the positive types.
						// the positive types depends on the the cluster you are
						// working on.
						Enumeration<Integer> keys = this.sPoint.keys();

						int rootID = 0;
						while (keys.hasMoreElements()) {
							int queryRoot = keys.nextElement().intValue();
							if (queryRoot == queryID) {
								rootID = queryRoot;
								break;
							}
							if (getQuery(queryRoot).clusteredQueryIDs
									.contains(new Integer(queryID))) {
								// we found the root
								rootID = queryRoot;
								break;

							}

						}

						if (contains_notsensitive(getQuery(rootID).stackTypes,
								previousTupleType)) {
							// it is actually positive, we should connect it
							// with
							// other events
							loop = false;
							double stop = previousTuples.get(i)
									.getStopTimestamp();

							previousRIPindex = StreamAccessor
									.getIndex(previousTuple);

							int sIndex = findStack(getTupleType(previousTuple,
									inputschArray));

							earray3 = connect(currenttuple,
									sequenceConstruction_Hstacks_negated(
											sIndex, previousRIPindex, stacks,
											queryID, inputschArray,
											currenttuple, stop, timestampleft),
									inputschArray, stacks, queryID, k,
									timestampleft);

							for (int t = 0; t < earray3.size(); t++) {
								earray.add(earray3.get(t));
							}

							// knowing the existence of one result is good
							// enough
							if (earray.size() > 0) {
								return earray;
							}
							// System.out.println("break");
						}

						else {
							// it is actually negative, we don't connect it.
							// Instead, we should
							// call previous tuple function again.
							// after knowing the existence
							k = findStack(previousTupleType);

							previousTuples = previousTuple_HStacks_negated(
									queryID, stacks, k, previousTuple,
									inputschArray);

						}

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

	ArrayList<ResultRVI> sequenceConstruction_Hstacks_negated(int k, int index,
			EventActiveInstanceQueue[] stacks, int queryID,
			SchemaElement[] inputschArray, byte[] nextTuple, double stopTime,
			double timestampleft) {

		ArrayList<ResultRVI> earray = new ArrayList<ResultRVI>();
		ArrayList<ResultRVI> earray3 = new ArrayList<ResultRVI>();
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

					ArrayList<ArrayList<Integer>> checkingQid = new ArrayList<ArrayList<Integer>>();
					ResultRVI rvi = new ResultRVI(checkingQid, array2);

					earray.add(rvi);

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

			while (currenttuple != null) {

				index = StreamAccessor.getIndex(currenttuple);

				ArrayList<PreviousTuples> previousTuples = previousTuple_HStacks_negated(
						queryID, stacks, k, currenttuple, inputschArray);
				boolean loop = true;
				while (previousTuples.size() != 0 && loop) {

					for (int i = 0; i < previousTuples.size(); i++) {
						byte[] previousTuple = previousTuples.get(i)
								.getPreviousTuple();

						String previousTupleType = getTupleType(previousTuple,
								inputschArray);

						// check if the event type is actually negative
						// (rewritten)
						// actually, I am trying to get the positive types.
						// the positive types depends on the the cluster you are
						// working on.
						Enumeration<Integer> keys = this.sPoint.keys();

						int rootID = 0;
						while (keys.hasMoreElements()) {
							int queryRoot = keys.nextElement().intValue();
							if (queryRoot == queryID) {
								rootID = queryRoot;
								break;
							}
							if (getQuery(queryRoot).clusteredQueryIDs
									.contains(new Integer(queryID))) {
								// we found the root
								rootID = queryRoot;
								break;

							}

						}

						if (contains_notsensitive(getQuery(rootID).stackTypes,
								previousTupleType)) {
							// it is actually positive, we should connect it
							// with
							// other events
							loop = false;
							double stop = previousTuples.get(i)
									.getStopTimestamp();

							previousRIPindex = StreamAccessor
									.getIndex(previousTuple);

							int sIndex = findStack(getTupleType(previousTuple,
									inputschArray));

							earray3 = connect(currenttuple,
									sequenceConstruction_Hstacks_negated(
											sIndex, previousRIPindex, stacks,
											queryID, inputschArray,
											currenttuple, stop, timestampleft),
									inputschArray, stacks, queryID, k,
									timestampleft);
							for (int t = 0; t < earray3.size(); t++) {
								earray.add(earray3.get(t));
							}
							// System.out.println("break");
						}

						else {
							// it is actually negative, we don't connect it.
							// Instead, we should
							// call previous tuple function again.
							// after knowing the existence
							k = findStack(previousTupleType);

							previousTuples = previousTuple_HStacks_negated(
									queryID, stacks, k, previousTuple,
									inputschArray);

						}

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
	 * check points for minusQueries, the different with regular checkPoint is I
	 * need to iterate all the possible values before I find one
	 * 
	 * @param leftTuple
	 * @param rightTuple
	 * @param queryID
	 * @param stacks
	 * @param stackIndex
	 * @param inputschArray
	 * @return
	 */

	protected boolean checkMinusPoints(byte[] leftTuple, byte[] rightTuple,
			int queryID, EventActiveInstanceQueue[] stacks, int stackIndex,
			SchemaElement[] inputschArray) {

		boolean addQuery = false;
		double timeleft = StreamAccessor.getDoubleCol(leftTuple, inputschArray,
				1);
		// double timeright = StreamAccessor.getDoubleCol(rightTuple,
		// inputschArray, 1);

		// process another combo query

		ArrayList<PreviousTuples> previousTuples = previousTuple_HStacks_negated(
				queryID, stacks, stackIndex, rightTuple, inputschArray);
		// all RIMM

		byte[] previous = null;

		double prevousTime = 0;
		double stopTime = -1;

		// test start
		/*
		 * 
		 * AIS[14].getByPhysicalIndex(40, retPointerArrayTemp1);
		 */
		// test end

		if (previousTuples.size() != 0) {
			previous = previousTuples.get(0).getPreviousTuple();

			stopTime = previousTuples.get(0).getStopTimestamp();
			String previousEventType = getTupleType(previous, inputschArray);

			int index = StreamAccessor.getIndex(previous);

			int sIndex = findStack(previousEventType);
			byte[][] retPointerArrayTemp1 = new byte[10][];

			while (previous != null && prevousTime >= stopTime) {

				prevousTime = StreamAccessor.getDoubleCol(previous,
						inputschArray, 1);

				previousEventType = getTupleType(previous, inputschArray);

				if (prevousTime >= timeleft) {

					int stackIndextemp = findStack(previousEventType);

					byte[][] retPointerArrayTemp = new byte[2][]; // hard code
					// get the RIMM tuple above it

					Enumeration<Integer> keys = this.sPoint.keys();

					int rootID = 0;
					while (keys.hasMoreElements()) {
						int queryRoot = keys.nextElement().intValue();
						if (queryRoot == queryID) {
							rootID = queryRoot;
							break;
						}
						if (getQuery(queryRoot).clusteredQueryIDs
								.contains(new Integer(queryID))) {
							// we found the root
							rootID = queryRoot;
							break;

						}

					}
					// while (loop && previousTuples.size() != 0)
					while (!contains_notsensitive(getQuery(rootID)
							.getStackTypes(), previousEventType)
							&& previous != null) {
						// the type is actually negative
						if (!contains_notsensitive(getQuery(rootID)
								.getStackTypes(), previousEventType)) {

							previousTuples = previousTuple_HStacks_negated(
									queryID, stacks, stackIndextemp, previous,
									inputschArray);

							if (previousTuples.size() == 0) {
								addQuery = false; // iterate another tuple
								break;
							}

							if (previousTuples != null
									&& previousTuples.size() != 0) {
								previousEventType = getTupleType(previousTuples
										.get(0).getPreviousTuple(),
										inputschArray);

								// is positive
								previous = previousTuples.get(0)
										.getPreviousTuple();

								previousEventType = getTupleType(previous,
										inputschArray);

								stackIndextemp = findStack(previousEventType);

								prevousTime = StreamAccessor.getDoubleCol(
										previous, inputschArray, 1);

								stopTime = previousTuples.get(0)
										.getStopTimestamp();

								double timestamp = StreamAccessor.getDoubleCol(
										previous, inputschArray, 1);

							}

						}

					}

					if (previousTuples.size() != 0) {
						double timeprevious = StreamAccessor.getDoubleCol(
								previousTuples.get(0).getPreviousTuple(),
								inputschArray, 1);
						// only if the two timestamps are the same???
						if (timeleft > stopTime && timeleft <= timeprevious) {// or
							// timeprevious
							// >
							// timeleft
							addQuery = true;
							break;// break as long as we find one?

						}

					}

					index--;
					previous = stacks[sIndex].getPreviousByPhysicalIndex(index,
							retPointerArrayTemp1);
				} else {
					break;
				}

			}

		}

		return addQuery;
	}

	protected boolean checkPoints(byte[] leftTuple, byte[] rightTuple,
			int queryID, EventActiveInstanceQueue[] stacks, int stackIndex,
			SchemaElement[] inputschArray) {

		boolean addQuery = false;
		double timeleft = StreamAccessor.getDoubleCol(leftTuple, inputschArray,
				1);
		double timeright = StreamAccessor.getDoubleCol(rightTuple,
				inputschArray, 1);

		ArrayList<PreviousTuples> previousTuples = previousTuple_HStacks_negated(
				queryID, stacks, stackIndex, rightTuple, inputschArray);
		// all RIMM

		byte[] previous = null;
		double prevousTime = 0;
		double stopTime = -1;

		// test start
		/*
		 * byte[][] retPointerArrayTemp1 = new byte[2][];
		 * AIS[14].getByPhysicalIndex(40, retPointerArrayTemp1);
		 */
		// test end

		if (previousTuples.size() != 0) {
			previous = previousTuples.get(0).getPreviousTuple();

			int index = StreamAccessor.getIndex(previous);

			String previousEventType = getTupleType(previous, inputschArray);
			prevousTime = StreamAccessor.getDoubleCol(previous, inputschArray,
					1);

			int stackIndextemp = findStack(previousEventType);

			byte[][] retPointerArrayTemp = new byte[2][]; // hard code
			// get the RIMM tuple above it

			stopTime = previousTuples.get(0).getStopTimestamp();

			Enumeration<Integer> keys = this.sPoint.keys();

			int rootID = 0;
			while (keys.hasMoreElements()) {
				int queryRoot = keys.nextElement().intValue();
				if (queryRoot == queryID) {
					rootID = queryRoot;
					break;
				}
				if (getQuery(queryRoot).clusteredQueryIDs.contains(new Integer(
						queryID))) {
					// we found the root
					rootID = queryRoot;
					break;

				}

			}

			// while (loop && previousTuples.size() != 0)
			while (!contains_notsensitive(getQuery(rootID).getStackTypes(),
					previousEventType)
					&& previousTuples.size() != 0 && previous != null) {
				// the type is actually negative
				if (!contains_notsensitive(getQuery(rootID).getStackTypes(),
						previousEventType)) {

					previousTuples = previousTuple_HStacks_negated(queryID,
							stacks, stackIndextemp, previous, inputschArray);

					if (previousTuples != null && previousTuples.size() != 0) {
						previousEventType = getTupleType(previousTuples.get(0)
								.getPreviousTuple(), inputschArray);

						// if the previous event type is positive but not in the
						// top query
						// keep looping
						// if the previous event type is positive and is in the
						// top query
						/*
						 * if (contains_notsensitive(
						 * this.queries.get(0).stackTypes, previousEventType))
						 */

						{
							// is positive
							previous = previousTuples.get(0).getPreviousTuple();

							previousEventType = getTupleType(previous,
									inputschArray);

							stackIndextemp = findStack(previousEventType);

							prevousTime = StreamAccessor.getDoubleCol(previous,
									inputschArray, 1);

							stopTime = previousTuples.get(0).getStopTimestamp();
						}
						// if the previous event type is negative and is in the
						// top query

						// if the previous event type is negative and is not in
						// the top query

					}

				}

			}
		}
		if (previousTuples.size() != 0) {

			double timeprevious = StreamAccessor.getDoubleCol(previous,
					inputschArray, 1);
			// only if the two timestamps are the same???
			if (timeleft > stopTime && timeleft <= timeprevious) {// or
				// timeprevious
				// >
				// timeleft
				addQuery = true;
			}

		}

		return addQuery;
	}

	ArrayList<ArrayList<byte[]>> connect_regular(byte[] currenttuple,
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

	ArrayList<ArrayList<byte[]>> sequenceConstruction_Hstacks_negated_regular(
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

					earray3 = connect_regular(currenttuple,
							sequenceConstruction_Hstacks_negated_regular(
									sIndex, previousRIPindex, stacks, queryID,
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
	public void produceinorder_HStacks_regular(int queryID,
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
					inputschArray);
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

			ArrayList<ArrayList<byte[]>> sc = sequenceConstruction_Hstacks_negated_regular(
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
							inputschArray);
				}

			}

		}

	}

	/**
	 * return indicate whether one of the child query has results
	 * 
	 * @param childID
	 * @param cstackTypes
	 * @param result
	 * @param nestPos
	 * @param inputschArray
	 */
	protected boolean computeChildrenQueries(int childID,
			ArrayList<String> cstackTypes, ArrayList<byte[]> result,
			double left, double right, SchemaElement[] inputschArray) {

		boolean hasResult = false;

		ArrayList<ArrayList<byte[]>> tempchildBuffer = new ArrayList<ArrayList<byte[]>>();

		double timestampl = left;

		double timestampr = right;

		// use the right interval to search for a particular
		// stack portion
		// use the left interval to check qualified events
		String lastcType = cstackTypes.get(cstackTypes.size() - 1);
		int lastcIndex = findStack(lastcType);

		byte[] tu = Utility.binarySearch_rightbounds(AIS[lastcIndex],
				timestampr, inputschArray);

		byte[][] retPointerArrayTemp = new byte[10][];// hard code

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

			hasResult = processQuery_oneResult(childID, tu, inputschArray,
					timestampl);
			if (hasResult == true)
				break;

			/*
			 * tempchildBuffer = this.resultBuffers.get(childID);// check
			 * 
			 * // only store distinct results if (tempchildBuffer != null) {
			 * 
			 * if (tempchildBuffer.size() > 0) { hasResult = true; break; }
			 * 
			 * }
			 */

			tu = AIS[lastcIndex].getPreviousByPhysicalIndex(tupleIndex,
					retPointerArrayTemp);
			t = StreamAccessor.getDoubleCol(tu, inputschArray, 1);

		}

		return hasResult;
	}

	public boolean produceinorder_HStacks_oneResult(int queryID,
			EventActiveInstanceQueue[] stacks, byte[] tempevent,
			SchemaElement[] inputschArray, double timestampleft) {

		boolean resultExist = false;
		String eventType = getTupleType(tempevent, inputschArray);
		// find out the stack index matching the tuple event type
		int stackIndex = findStack(eventType);

		stackTypes = getQuery(queryID).stackTypes;

		// first, compute what positive event type needs such extra care (done)
		// second, I need to compute previous tuple for each query given
		// tempevent

		// actually, I only need to remember the POINT timestamp for each query
		// for (int j = 1; j < this.queries.size(); j++)

		ArrayList<PreviousTuples> previousTuples = previousTuple_HStacks_negated(
				queryID, stacks, stackIndex, tempevent, inputschArray);

		// last, For each distinct previous tuple, it associates with a bit
		// marking indicating which query still in process
		for (int i = 0; i < previousTuples.size(); i++) {
			byte[] previousTuple = previousTuples.get(i).getPreviousTuple();
			double timestampPrevious = StreamAccessor.getDoubleCol(
					previousTuple, inputschArray, 1);

			// String previousEventType = getTupleType(previousTuple,
			// inputschArray);

			double stopTime = previousTuples.get(i).getStopTimestamp();
			if (timestampPrevious > stopTime) {
				String type = getTupleType(previousTuple, inputschArray)
						.toLowerCase();

				int prevousStackIndex = findStack(type);

				int previousRIPindex = StreamAccessor.getIndex(previousTuple);

				ArrayList<ResultRVI> sc = sequenceConstruction_Hstacks_negated(
						prevousStackIndex, previousRIPindex, stacks, queryID,
						inputschArray, tempevent, stopTime, timestampleft);

				// my logic was correct. But it is not efficient as I am
				// releasing one result one by one.
				// instead of several.

				// one right tuple corresponds to several results, I only need
				// to check once
				// for the right tuple. and all the tuples above the previous
				// one are qualified.
				// but it is hard for me to figure it out now.

				for (int k = 0; k < sc.size(); k++) {
					ArrayList<ArrayList<Integer>> checkingQid = sc.get(k)
							.getCheckingQid();

					ArrayList<Integer> checking = new ArrayList<Integer>();
					// check output it or not?

					// and for which query we output results?

					ArrayList<byte[]> ar2 = new ArrayList<byte[]>();

					for (int h = 0; h < sc.get(k).getResult().size(); h++) {

						ar2.add(sc.get(k).getResult().get(h));
					}

					// test
					byte[] lastT = ar2.get(ar2.size() - 1);
					double left = StreamAccessor.getDoubleCol(lastT,
							inputschArray, 1);

					if (left < timestampleft) // we require the constructed
					// results are within the
					// interval
					{
						continue;
					}
					double right = StreamAccessor.getDoubleCol(tempevent,
							inputschArray, 1);

					// byte[] firstT = ar2.get(0);
					// double firstTime = StreamAccessor.getDoubleCol(firstT,
					// inputschArray, 1);

					// here, it is the point also. when adding the event, I
					// should check ...

					// check if the current tuple is a checking point

					if (contains_notsensitive(this.sPoint.get(queryID),
							eventType)) {
						ArrayList<ArrayList<Integer>> checkingQidLast = sc.get(
								k).getCheckingQid();

						// again, I don't need to check all queries, only for
						// the clustered ones

						ArrayList<Integer> clusteredQueryIDs = getQuery(queryID)
								.getClusteredQueryIDs();

						// check whether it fails some query
						for (int j = 0; j < clusteredQueryIDs.size(); j++) {

							// double lasteT = StreamAccessor.getDoubleCol(
							// tempevent, inputschArray, 1);

							if (clusteredQueryIDs.get(j) >= 0) {
								boolean add = checkPoints(lastT, tempevent,
										clusteredQueryIDs.get(j), stacks,
										stackIndex, inputschArray);
								if (add) {

									// contents: position, positive root, other
									// checking queries with negative types
									ArrayList<Integer> minusQueriesID = getQuery(
											clusteredQueryIDs.get(j))
											.getMinusQueriesID();

									// process the combo query
									// for every tuple within the interval.

									// get the root query in the minusQueries

									int rootID = minusQueriesID.get(0)
											.intValue();

									ArrayList<String> cstackTypes = getQuery(
											rootID).getStackTypes();

									boolean hasResults = computeChildrenQueries(
											rootID, cstackTypes, ar2, left,
											right, inputschArray);

									boolean shouldAdd = true;

									if (hasResults) {
										shouldAdd = false;
									}

									if (shouldAdd) {
										checking.add(new Integer(1));
									}

								} else {
									checking.add(new Integer(0));
								}
								checkingQid.add(checking);

							}

						}
						ar2.add(tempevent);

					} else {
						ar2.add(tempevent);

					}

					// compute the first event's timestamp
					// double timestamp =
					// StreamAccessor.getDoubleCol(ar2.get(0),
					// inputschArray, 1);

					SchemaElement[] schArray_Result = generateResultSchemas(
							getQueryResultSize(0), inputschArray);

					byte[] dest = StreamTupleCreator
							.makeEmptyTuple(schArray_Result);

					int schIndex = 0;
					for (int arIndex = 0; arIndex < getQueryResultSize(0); arIndex++) {
						StreamTupleCreator.tupleAppend(dest, ar2.get(arIndex),
								schArray_Result[schIndex].getOffset());
						schIndex += inputschArray.length;

					}

					boolean reuse = true;
					ArrayList<ArrayList<Integer>> checkingq = sc.get(k)
							.getCheckingQid();

					boolean output = true;
					// bug place
					ArrayList<Integer> clusteredQueryIDs = getQuery(queryID)
							.getClusteredQueryIDs();

					for (int qi = 0; qi < clusteredQueryIDs.size(); qi++) {
						output = true;
						int j = 0;
						for (; checkingq.size() > j
								&& j < checkingq.get(j).size(); j++) {
							if (checkingq.get(j).get(qi).intValue() != 1) {

								output = false;

								break;
							}
						}

						// finding one query satisfied is good enough.
						if (output == true) {
							resultExist = true;
							return resultExist;
						}
						// I should remember: one byte array is only generated
						// once for the query set.

					}

					if (output == true) {
						bufferResult(getQuery(0).getQueryID(), dest,
								schArray_Result, ar2, reuse, inputschArray);
					}

				}

			}

		}

		return resultExist;

	}

	/**
	 * It produce the sequence results in a hierarchical stack for a single
	 * query triggered by one event.
	 * 
	 * @param stacks
	 * @param tempevent
	 */
	public void produceinorder_HStacks(int queryID,
			EventActiveInstanceQueue[] stacks, byte[] tempevent,
			SchemaElement[] inputschArray, double leftTimeStamp) {
		ArrayList<Integer> clusteredQueryIDs = getQuery(queryID)
				.getClusteredQueryIDs();

		double timestampleft = -1; // no need here actually

		String eventType = getTupleType(tempevent, inputschArray);
		// find out the stack index matching the tuple event type
		int stackIndex = findStack(eventType);

		stackTypes = getQuery(queryID).stackTypes;

		// first, compute what positive event type needs such extra care (done)
		// second, I need to compute previous tuple for each query given
		// tempevent

		// actually, I only need to remember the POINT timestamp for each query
		// for (int j = 1; j < this.queries.size(); j++)

		ArrayList<PreviousTuples> previousTuples = previousTuple_HStacks_negated(
				queryID, stacks, stackIndex, tempevent, inputschArray);

		// last, For each distinct previous tuple, it associates with a bit
		// marking indicating which query still in process
		for (int i = 0; i < previousTuples.size(); i++) {
			byte[] previousTuple = previousTuples.get(i).getPreviousTuple();
			double timestampPrevious = StreamAccessor.getDoubleCol(
					previousTuple, inputschArray, 1);

			// String previousEventType = getTupleType(previousTuple,
			// inputschArray);

			double stopTime = previousTuples.get(i).getStopTimestamp();
			if (timestampPrevious > stopTime) {
				String type = getTupleType(previousTuple, inputschArray)
						.toLowerCase();

				int prevousStackIndex = findStack(type);

				int previousRIPindex = StreamAccessor.getIndex(previousTuple);

				ArrayList<ResultRVI> sc = sequenceConstruction_Hstacks_negated(
						prevousStackIndex, previousRIPindex, stacks, queryID,
						inputschArray, tempevent, stopTime, timestampleft);

				// my logic was correct. But it is not efficient as I am
				// releasing one result one by one.
				// instead of several.

				// one right tuple corresponds to several results, I only need
				// to check once
				// for the right tuple. and all the tuples above the previous
				// one are qualified.
				// but it is hard for me to figure it out now.

				for (int k = 0; k < sc.size(); k++) {

					long vectorEva_Start = (new Date()).getTime();

					ArrayList<ArrayList<Integer>> checkingQid = sc.get(k)
							.getCheckingQid();

					ArrayList<Integer> checking = new ArrayList<Integer>();
					// check output it or not?

					// and for which query we output results?

					ArrayList<byte[]> ar2 = new ArrayList<byte[]>();

					for (int h = 0; h < sc.get(k).getResult().size(); h++) {

						ar2.add(sc.get(k).getResult().get(h));
					}

					// test
					byte[] lastT = ar2.get(ar2.size() - 1);
					double left = StreamAccessor.getDoubleCol(lastT,
							inputschArray, 1);

					if (left < leftTimeStamp) {
						continue;
					}
					double right = StreamAccessor.getDoubleCol(tempevent,
							inputschArray, 1);

					// byte[] firstT = ar2.get(0);
					// double firstTime = StreamAccessor.getDoubleCol(firstT,
					// inputschArray, 1);

					// here, it is the point also. when adding the event, I
					// should check ...

					// check if the current tuple is a checking point

					if (contains_notsensitive(this.sPoint.get(queryID),
							eventType)) {
						ArrayList<ArrayList<Integer>> checkingQidLast = sc.get(
								k).getCheckingQid();

						// again, I don't need to check all queries, only for
						// the clustered ones

						// check whether it fails some query
						for (int j = 0; j < clusteredQueryIDs.size(); j++) {

							// double lasteT = StreamAccessor.getDoubleCol(
							// tempevent, inputschArray, 1);

							if (clusteredQueryIDs.get(j) >= 0) {
								boolean add = checkPoints(lastT, tempevent,
										clusteredQueryIDs.get(j), stacks,
										stackIndex, inputschArray);
								if (add) {

									// contents: position, positive root, other
									// checking queries with negative types
									ArrayList<Integer> minusQueriesID = getQuery(
											clusteredQueryIDs.get(j))
											.getMinusQueriesID();

									// process the combo query
									// for every tuple within the interval.

									// get the root query in the minusQueries

									if (minusQueriesID.size() != 0) {
										int rootID = minusQueriesID.get(0)
												.intValue();

										ArrayList<String> cstackTypes = getQuery(
												rootID).getStackTypes();

										boolean hasResults = computeChildrenQueries(
												rootID, cstackTypes, ar2, left,
												right, inputschArray);

										boolean shouldAdd = true;

										if (hasResults) {
											shouldAdd = false;
										}

										if (shouldAdd) {
											checking.add(new Integer(1));
										}
									} else {
										checking.add(new Integer(1));
									}

								} else {
									checking.add(new Integer(0));
								}
								checkingQid.add(checking);

							}

						}
						ar2.add(tempevent);

					} else {
						ar2.add(tempevent);

					}

					// compute the first event's timestamp
					// double timestamp =
					// StreamAccessor.getDoubleCol(ar2.get(0),
					// inputschArray, 1);

					SchemaElement[] schArray_Result = generateResultSchemas(
							getQueryResultSize(queryID), inputschArray);

					byte[] dest = StreamTupleCreator
							.makeEmptyTuple(schArray_Result);

					int schIndex = 0;
					for (int arIndex = 0; arIndex < getQueryResultSize(queryID); arIndex++) {
						StreamTupleCreator.tupleAppend(dest, ar2.get(arIndex),
								schArray_Result[schIndex].getOffset());
						schIndex += inputschArray.length;

					}

					boolean reuse = true;
					ArrayList<ArrayList<Integer>> checkingq = sc.get(k)
							.getCheckingQid();

					boolean output = true;

					// cost model start
					// long execution_Start_vector = (new Date()).getTime();

					// I should only check against the clustered queries with
					// the same root.

					for (int qi = 0; qi < clusteredQueryIDs.size(); qi++) {
						output = true;
						int j = 0;
						for (; checkingq.size() > j
								&& j < checkingq.get(j).size(); j++) {
							if (checkingq.get(j).get(qi).intValue() != 1) {

								output = false;

								break;
							}
						}

						// finding one query satisfied is good enough.
						if (output == true)
							break;
						// I should remember: one byte array is only generated
						// once for the query set.
					}

					/*
					 * for (int qi = 1; qi < this.queries.size(); qi++) { output
					 * = true; int j = 0; for (; checkingq.size() > j && j <
					 * checkingq.get(j).size(); j++) { if
					 * (checkingq.get(j).get(qi - 1).intValue() != 1) {
					 * 
					 * output = false;
					 * 
					 * break; } }
					 * 
					 * // finding one query satisfied is good enough. if (output
					 * == true) break; // I should remember: one byte array is
					 * only generated // once for the query set.
					 * 
					 * }
					 */
					// long execution_End_vector = (new Date()).getTime();

					// long vectorTime = execution_End_vector -
					// execution_Start_vector;
					// System.out.println(vectorTime);
					// cost model end

					long vectorEva_End = (new Date()).getTime();

					Configure.vectorEva += vectorEva_End - vectorEva_Start;

					if (output == true) {
						bufferResult(queryID, dest, schArray_Result, ar2,
								reuse, inputschArray);
					}

				}

			}

		}

	}

	/**
	 * compute the number of positive event types given a query ID
	 * 
	 * @return
	 */
	protected int getQueryResultSize(int qid) {

		int size = 0;

		ArrayList<String> types = this.queries.get(qid).stackTypes;

		for (int i = 0; i < types.size(); i++) {
			if (!types.get(i).startsWith("-"))
				size++;
		}
		return size;

	}

	public void bufferResult(int queryID, byte[] dest,
			SchemaElement[] schArray_Result, ArrayList<byte[]> ar2,
			boolean reuseresults, SchemaElement[] schArray) {

		if (Utility.windowOpt(ar2, schArray)) {
			// getQuery(queryID).donestatus = (byte) 1;

			Configure.resultNum += 1;

			/*Utility.rewriteToFile("====== result======" + queryID);
			Utility.rewriteToFile(StreamAccessor
					.toString(dest, schArray_Result));*/

			com.hp.hpl.CHAOS.Queue.Utility.enqueueGroup(OutputQueueArray, dest);

			// /////////Buffer Results /////////////////////////

			if (reuseresults) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.CHAOS.StreamOperator.StreamOperator#run(int)
	 */
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

			if (timestamp == 28549.544) {
				System.out.print("check");
			}

			/*
			 * System.out.println(timestamp);
			 */

			// here, first apply the aggressive purge
			double expiringTimestamp = timestamp - Configure.windowsize;

			String eventType = getTupleType(tuple, schArray);

			// test start
			/*
			 * if (eventType.equalsIgnoreCase("IPIX"))
			 * System.out.println("check ipix");
			 */// test end

			// find out the stack index matching the tuple event type
			int stackIndex = findStack(eventType);

			if (stackIndex >= 0 && AIS[stackIndex] != null) {

				// here, I should use the stackType for storing the current
				// tuple instead of using the event type.
				// enqueue tuple and set up pointers.
				// pointer size should be greater than 1
				tuple_insertion(stackIndex, tuple);

				// As I know all these queries have the same positive but
				// different negative event types.

				// query with index 0 is reserved for all positive only
				// ArrayList<String> stackTypes = getQuery(0).getStackTypes();

				// compute(root qi), for root, we still require tuple is of
				// the last event type

				// if (eventType.equalsIgnoreCase(type))
				{
					// iterate all root queries
					for (int qIter = 0; qIter < this.queries.size(); qIter++) {
						if (this.queries.get(qIter).isRoot()) {
							ArrayList<String> stackTypes = this.queries.get(
									qIter).getStackTypes();

							if (eventType.equalsIgnoreCase(stackTypes
									.get(stackTypes.size() - 1)))
								processQuery(this.queries.get(qIter)
										.getQueryID(), tuple, schArray,
										expiringTimestamp);
						}

					}

				}

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

			// first run-eclipsed time

			long executionTimeEnd = (new Date()).getTime();
			Configure.executionTime += executionTimeEnd - execution_Start;

			// only after receiving the last event will write the statistics to
			// the file.
			if (Configure.previousresultNum != Configure.resultNum) {
				String write = Configure.resultNum + " "
						+ Configure.executionTime + " " + Configure.vectorEva;

				System.out.println(write);

				// test start
				/*
				 * if (timestamp == 34662.262) {
				 * System.out.print(Configure.resultNum);
				 * 
				 * }
				 */

				resultCollection.add(write);

				// test end

				// test start

				/*
				 * if (Configure.resultNum == 6435) {
				 * System.out.println("check place"); }
				 */

				// test end

				// System.out.println(timestamp + " " + Configure.executionTime
				// + " " + Configure.resultNum);

			}

			// three dimension statistics
			// String write = Configure.inputConsumed + " " +
			// Configure.executionTime + " " + Configure.resultNum;
			// resultCollection.add(write);

			// test start
			// System.out.println(Configure.resultNum);
			// test end

			if (timestamp == 35148.521) {
				Utility.rewriteToFile(resultCollection);
				System.out.print("output");
			}

			/*
			 * if (Configure.resultNum == 6825) { System.out.println("check"); }
			 */

			// first chart with x- execution time; y - total sequence results
			// generated
			/*
			 * if (Configure.previousresultNum != Configure.resultNum) {
			 * System.out.println("=======at time==========" +
			 * Configure.executionTime);
			 * System.out.println("=======Total resultNum===========" +
			 * Configure.resultNum);
			 * 
			 * } System.out.println("=======at time==========" +
			 * Configure.executionTime);
			 * 
			 * //second chart with x- execution time; y - memory used int
			 * memoryConsumption = getMemoryStatistics();
			 * System.out.println("=======Memory Consumption===========" +
			 * memoryConsumption);
			 */

		}

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
		Set<String> set2 = null;
		if (table2 != null) {
			set2 = table2.keySet();
			pointerArray = new byte[set2.size()][];
			Iterator<String> itr2 = set2.iterator();
			int pinterIndex = 0;
			while (itr2.hasNext()) {
				String event_type_pre = itr2.next();

				// find out the previous stack index matching the
				// tuple event type
				int prestackIndex = findStack(event_type_pre);

				/*
				 * //test start if(prestackIndex > AIS.length)
				 * System.out.println("bug place"); //test end
				 */
				if (AIS[prestackIndex] != null
						&& AIS[prestackIndex].eventQueue != null)
					pointerArray[pinterIndex++] = AIS[prestackIndex].eventQueue
							.peekLast();

			}
		}

		AIS[stackIndex].enqueue(tuple, pointerArray);// default null5

		/*
		 * int index = StreamAccessor.getIndex(tuple);
		 * 
		 * byte[][] retPointerArrayTemp = new byte[set2.size()][];
		 * 
		 * AIS[stackIndex].getByPhysicalIndex(index, retPointerArrayTemp);
		 */

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

	protected int getNestedStackNum(int qi) {
		int num = 0;
		ArrayList<String> types = getQuery(qi).stackTypes;

		// consider the positive stack only
		for (int j = 0; j < types.size(); j++) {
			String stype = types.get(j);
			if (!stype.startsWith("-")) {
				num++;
			}
		}

		ArrayList<childQueryInfo> childrenq = getQuery(qi).children;

		if (childrenq.size() != 0) {
			for (int i = 0; i < childrenq.size(); i++) {
				int cID = childrenq.get(i).getChildID();
				int type = childrenq.get(i).getPositiveComponent();

				if (type == 1)// positive event types
					num += getQuery(cID).stackTypes.size();
			}

		}

		return num;

	}

	/**
	 * Process queries in a cluster with the given queryID as the root. Process
	 * results involving tuple
	 * 
	 * 
	 * @param stackIndex
	 *            the stack index for the incoming tuple
	 * @param tuple
	 * @param schArray
	 * 
	 */
	void processQuery(int queryID, byte[] tuple, SchemaElement[] schArray,
			double timestampleft) {

		produceinorder_HStacks(queryID, AIS, tuple, schArray, timestampleft);

	}

	boolean processQuery_oneResult(int queryID, byte[] tuple,
			SchemaElement[] schArray, double timestampleft) {

		boolean resultExist = produceinorder_HStacks_oneResult(queryID, AIS,
				tuple, schArray, timestampleft);
		return resultExist;

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