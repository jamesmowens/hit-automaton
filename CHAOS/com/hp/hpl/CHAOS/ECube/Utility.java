package com.hp.hpl.CHAOS.ECube;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Arrays;
import java.util.Comparator;

import com.hp.hpl.CHAOS.Queue.SingleReaderEventQueueArrayImp;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;

public class Utility {
	public static ArrayList<String> getAllstackTypes(
			ArrayList<MergedQueryInfo> queries) {
		ArrayList<String> types = new ArrayList<String>();
		for (int i = 0; i < queries.size(); i++) {
			ArrayList<String> typesi = queries.get(i).getStackTypes();
			for (int j = 0; j < typesi.size(); j++) {
				if (!types.contains(typesi.get(j)))
					types.add(typesi.get(j));
			}
		}
		return types;

	}

	public static void rewriteToFile(String write) {
		try {
			FileWriter fstream = new FileWriter("SEQ-q1.txt", true);
			BufferedWriter out = new BufferedWriter(fstream);

			out.write(write+'\n');

			// Close the output stream
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void accuLatency(ArrayList<byte[]> ar2) {
		// for latency, get the last event's time
		byte[] lastTuple = ar2.get(ar2.size() - 1);
		double minTime = StreamAccessor.getMinTimestamp(lastTuple);
		long outputTime = (new Date()).getTime();
		Configure.latency += outputTime - minTime;

	}

	public static ArrayList<String> lastQueryTypes(ArrayList<MergedQueryInfo> queries) {

		ArrayList<String> lastTypes = new ArrayList<String>();
		for (int i = 0; i < queries.size(); i++) {
			ArrayList<String> stacks = queries.get(i).stackTypes;
			String ltype = stacks.get(stacks.size() - 1);
			lastTypes.add(ltype);
		}

		return lastTypes;
	}

	/**
	 * similar as indexof in java but it is case insensitive
	 * 
	 * @param list
	 * @param astring
	 * @return
	 */
	public static int indexof_notcasesensitive(ArrayList<String> list,
			String astring) {
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
	 * 
	 * @param tuple
	 * @param inputschArray
	 * @return the tuple event type
	 */
	public static String getTupleType(byte[] tuple,
			SchemaElement[] inputschArray) {

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
	 * test whether one string is contained in another arrayList.
	 * 
	 * @param list
	 * @param astring
	 * @return
	 */
	public static boolean contains_notsensitive(ArrayList<String> list,
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

	/**
	 * return the negative event type index positions
	 * 
	 * @param stackTypes
	 * @return
	 */
	public static ArrayList<Integer> getNegativePos(ArrayList<String> stackTypes) {
		ArrayList<Integer> negativeNum = new ArrayList<Integer>();
		for (int i = 0; i < stackTypes.size(); i++) {
			String eventType = stackTypes.get(i);
			if (eventType.startsWith("-")) {
				negativeNum.add(new Integer(i));
			}
		}
		return negativeNum;
	}

	public static int getPositiveTypeNum(ArrayList<String> stackTypes) {
		int num = 0;
		for (int i = 0; i < stackTypes.size(); i++) {
			if (!stackTypes.get(i).startsWith("-"))
				num++;

		}
		return num;
	}

	public static void simulatePredicatesCost() {
		int total = 0;
		// 1000000
		// 100000

		// mo 100000
		for (int i = 0; i < 100000; i++) {
			total += i;
		}
	}

	protected static void purgeResultBuffer(double timestamp,
			Hashtable<Integer, ArrayList<ArrayList<byte[]>>> resultBuffers,
			SchemaElement[] schArray, int queryID) {
		if (!resultBuffers.isEmpty() && resultBuffers.containsKey(queryID)) {
			int pos = Utility.binarySearch(resultBuffers, timestamp, schArray,
					queryID);
			int counter = 0;
			while (counter < pos) {
				resultBuffers.get(queryID).remove(0);
				// System.out.println("purge result");
				counter++;
			}

			// (roughly)
			// when receiving events's timestamp and the result end timestamp
			// are
			// greater
			// than window size, then delete
		}

	}

	protected static int purgeStack(EventActiveInstanceQueue[] AIS,
			double expiringTime, SchemaElement[] schArray) {
		
		int purgedTuples = 0;
		
		for (int i = 0; i < AIS.length; i++) {
			if (AIS[i] == null)
				continue;
			else {
				byte[] tuple = AIS[i].eventQueue.peekLast();

				if (tuple == null)
					continue;
				else {

					while (tuple != null)
					{

						int index = StreamAccessor.getIndex(tuple);
						byte[][] retPointerArrayTemp = new byte[6][];

						double timestamp = StreamAccessor.getDoubleCol(tuple,
								schArray, 1);
						String tupleType = getTupleType(tuple, schArray);

						if (timestamp < expiringTime) {

							/*
							 * System.out .println(tupleType + timestamp +
							 * "purged"); if (timestamp == 8) {
							 * System.out.print(8); }
							 */

							// byte[] returnedTuple =
							// AIS[i].eventQueue.dequeue();
							double retrtimestamp = timestamp;
							while (retrtimestamp < expiringTime) {
								byte[] returnedTuple = AIS[i].eventQueue
										.dequeue();
								AIS[i].pointerQueue.dequeue();
								purgedTuples++;

								// System.out.println("purged" + retrtimestamp);

								returnedTuple = AIS[i].eventQueue.peek();
								if (returnedTuple != null)
									retrtimestamp = StreamAccessor
											.getDoubleCol(returnedTuple,
													schArray, 1);
								else {
									break;
								}

							}

							// Actually, deleting one tuple means all the above
							// tuples can be deleted also.

						}

						tuple = AIS[i].getPreviousByPhysicalIndex(index,
						retPointerArrayTemp);
					}

				}

			}

		}
		
		return purgedTuples;

	}

	/*
	 * protected ArrayList<byte[]> checkPurgeTuples(double expiringTime,
	 * SchemaElement[] schArray) { ArrayList<byte[]> purgingTuple = new
	 * ArrayList<byte[]>(); for (int i = 0; i < this.AIS.length; i++) { if
	 * (this.AIS[i] == null) continue; else { byte[] tuple =
	 * this.AIS[i].eventQueue.peek();
	 * 
	 * if (tuple == null) continue; else { while (tuple != null) {
	 * 
	 * int index = StreamAccessor.getIndex(tuple); byte[][] retPointerArrayTemp
	 * = new byte[6][];
	 * 
	 * double timestamp = StreamAccessor.getDoubleCol(tuple, schArray, 2);
	 * String tupleType = getTupleType(tuple, schArray);
	 * 
	 * if (timestamp < expiringTime) {
	 * 
	 * purgingTuple.add(tuple);
	 * 
	 * }
	 * 
	 * tuple = this.AIS[i].getPreviousByPhysicalIndex(index,
	 * retPointerArrayTemp); }
	 * 
	 * }
	 * 
	 * }
	 * 
	 * }
	 * 
	 * return purgingTuple;
	 * 
	 * }
	 */

	protected static boolean windowOpt(ArrayList<byte[]> result,
			SchemaElement[] schArray, int window) {
		boolean output = true;
		byte[] firstTuple = result.get(0);
		byte[] lastTuple = result.get(result.size() - 1);
		double timestampfirst = StreamAccessor.getDoubleCol(firstTuple,
				schArray, 1);
		double timestampsecond = StreamAccessor.getDoubleCol(lastTuple,
				schArray, 1);
		double timediff = timestampsecond - timestampfirst;

		if (timediff >= window){
			output = false;
		}
		return output;
	}
	/**
	 * get the time period of the result.
	 * @param result
	 * @param SchemaArray
	 * @return the time difference from first event to last event.
	 */
	protected static double getTime(ArrayList<byte[]> result,
			SchemaElement[] schArray) {
		byte[] firstTuple = result.get(0);
		byte[] lastTuple = result.get(result.size() - 1);
		double timestampfirst = StreamAccessor.getDoubleCol(firstTuple,
				schArray, 1);
		double timestampsecond = StreamAccessor.getDoubleCol(lastTuple,
				schArray, 1);

		double timeDiff =  timestampsecond - timestampfirst;
		
		return timeDiff;
	}

	protected static ArrayList<byte[]> checkPurgeTuples(
			EventActiveInstanceQueue[] AIS, double expiringTime,
			SchemaElement[] schArray) {
		ArrayList<byte[]> purgingTuple = new ArrayList<byte[]>();
		for (int i = 0; i < AIS.length; i++) {
			if (AIS[i] == null)
				continue;
			else {
				byte[] tuple = AIS[i].eventQueue.peek();

				if (tuple == null)
					continue;
				else {
					while (tuple != null) {

						int index = StreamAccessor.getIndex(tuple);
						byte[][] retPointerArrayTemp = new byte[6][];

						double timestamp = StreamAccessor.getDoubleCol(tuple,
								schArray, 1);
						String tupleType = getTupleType(tuple, schArray);

						if (timestamp < expiringTime) {

							purgingTuple.add(tuple);

						}

						tuple = AIS[i].getPreviousByPhysicalIndex(index,
								retPointerArrayTemp);
					}

				}

			}

		}

		return purgingTuple;

	}

	/**
	 * Return true if s1 is one ancestor of s2 in the concept hierarchy.
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static Boolean semanticMatch(String s1, String s2, ConceptTree tree) {
		if (s1.equalsIgnoreCase(s2))
			return true;
		else {
			double[] bounds_s1 = {};
			double[] bounds_s2 = {};

			bounds_s1 = searchTree(tree, s1);
			bounds_s2 = searchTree(tree, s2);
			if (bounds_s1[0] < bounds_s2[0] && bounds_s1[1] > bounds_s2[1])
				return true;
			else
				return false;
		}

	}

	boolean[] checkQueryPatternConceptR(int queryID1, int queryID2,
			ArrayList<String> middle, QueryInfo[][] orderedQueries,
			ConceptTree tree) {

		boolean pattern = false;
		boolean concept = false;

		if (queryID1 == queryID2) {
			boolean[] relations = { pattern, concept };
			return relations;

		} else {
			ArrayList<String> QueryTypes1 = getQuery(queryID1, orderedQueries).stackTypes;
			ArrayList<String> QueryTypes2 = getQuery(queryID2, orderedQueries).stackTypes;
			int matchCounter = 0;
			if (QueryTypes1.size() == QueryTypes2.size()) {
				for (int i = 0; i < QueryTypes1.size(); i++) {
					String type2 = QueryTypes2.get(i);
					if (QueryTypes1.get(i).equalsIgnoreCase(type2)) {
						matchCounter++;
						middle.add(type2);
						continue;
					} else {

						if (semanticMatch(QueryTypes1.get(i), type2, tree)) {
							middle.add(type2);
							matchCounter++;
							continue;
						} else if (semanticMatch(type2, QueryTypes1.get(i),
								tree)) {
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
							QueryTypes2.get(queryType2Index), tree)) {
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

	// ok. I have to deal with tie
	public static int binarySearch(
			Hashtable<Integer, ArrayList<ArrayList<byte[]>>> resultBuffers,
			double ts, SchemaElement[] schArray, int queryID) {
		// if not visited at all before, return -1
		if (ts < 0)
			return -1;

		int low = 0;

		int high = resultBuffers.get(queryID).size() - 1;

		int mid;

		int position = -1;
		if (high < 0)
			return 0;
		while (low <= high) {
			mid = (low + high) / 2;

			if (!resultBuffers.isEmpty()
					&& resultBuffers.containsKey(new Integer(queryID))) {
				ArrayList<byte[]> checkingTuple = resultBuffers.get(
						new Integer(queryID)).get(mid);
				if (checkingTuple.size() > 0) {
					double timestamp = StreamAccessor.getDoubleCol(
							checkingTuple.get(checkingTuple.size() - 1),
							schArray, 1);

					if (timestamp < ts)
						low = mid + 1;
					else if (timestamp > ts)
						high = mid - 1;
					else if (timestamp == ts) {
						position = mid;
						break;
					}

				}

			}

		}
		if (low > high) {
			position = low;
		}
		while (position < high && position + 1 <= high && position != -1) {
			ArrayList<byte[]> checkingTuple = resultBuffers.get(
					new Integer(queryID)).get(position);

			ArrayList<byte[]> checkingTupleNext = resultBuffers.get(
					new Integer(queryID)).get(position + 1);
			double timestamp = StreamAccessor.getDoubleCol(checkingTuple
					.get(checkingTuple.size() - 1), schArray, 1);

			double timestampNext = StreamAccessor.getDoubleCol(
					checkingTupleNext.get(checkingTupleNext.size() - 1),
					schArray, 1);
			if (timestamp == timestampNext)
				position++;
			else
				break;

		}

		return position;

	}

	public static int binarySearch(SingleReaderEventQueueArrayImp eventQueue,
			double ts, SchemaElement[] schArray) {
		int low = 0;

		byte[] tuple = eventQueue.peek();
		int high = StreamAccessor.getIndex(tuple);

		int mid;

		int position = 0;
		if (high < 0)
			return 0;
		while (low <= high) {
			mid = (low + high) / 2;

			byte[] checkingTuple = eventQueue.get((int) mid);
			double timestamp = StreamAccessor.getDoubleCol(checkingTuple,
					schArray, 1);
			double tempstamp = ts;

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
	 * gets the query with the given query id
	 * 
	 * @param queryID
	 * @return query
	 */
	public static QueryInfo getQuery(int queryID, QueryInfo[][] orderedQueries) {
		int i;
		for (i = 0; i < orderedQueries.length; i++) {
			if (orderedQueries[i][0].getQueryID() == queryID) {
				break;
			}
		}
		// temp bug
		if(i == orderedQueries.length)
		{i--;}
		return orderedQueries[i][0];
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
	protected static double[] searchTree(String s, ConceptTree tree) {
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
	 * The search should start from the largest level (root with the smallest
	 * level).
	 * 
	 * if not found, return -1.0 -1.0
	 * 
	 * @param s
	 * @return [leftbound, rightbound] in the concept tree
	 * 
	 */
	protected static double[] searchTree(ConceptTree tree, String s) {
		double[] bounds = { -1.0, -1.0 };
		for (int level = tree.getNumLevels() - 1; level > 0; level--) {
			for (int nodeIndex = 0; nodeIndex < tree.getLevel(level)
					.getNumNodes(); nodeIndex++) {
				if (tree.getLevel(level).getNode(nodeIndex).name
						.equalsIgnoreCase(s)) {// string match
					bounds[0] = tree.getLevel(level).getNode(nodeIndex)
							.getLeftBound(0).x;
					bounds[1] = tree.getLevel(level).getNode(nodeIndex)
							.getRightBound(0).x;
					break;
				}

			}

		}
		return bounds;

	}

}
