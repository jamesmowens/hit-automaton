package com.hp.hpl.CHAOS.Rewriting;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Date;
import com.hp.hpl.CHAOS.Queue.SingleReaderEventQueueArrayImp;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;

public class Utility {
	public static ArrayList<String> getAllstackTypes(
			ArrayList<QueryInfo> queries) {
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
		/*try {




			FileWriter fstream = new FileWriter("1118-q6n-result.txt",
					true);
			BufferedWriter out = new BufferedWriter(fstream);

			out.write(write + '\n');
                         
			out.close();
                        

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

	}



	public static void rewriteToFile(String write, JFrame frame) {
		/*try {

                    
        // Set it so that it terminates the program when the window is closed.

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set the windows size and then show it.

        frame.setSize(400,400);

        frame.setVisible(true);







			FileWriter fstream = new FileWriter("1118-q6n-result.txt",
					true);
			BufferedWriter out = new BufferedWriter(fstream);

			out.write(write + '\n');
                         JTextArea basicInformation1;
                         basicInformation1 = new JTextArea(write);
			// Close the output stream
			out.close();
                        frame.add(basicInformation1);


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
                
	}

	
	
	// write statistics to a file
	public static void rewriteToFile(ArrayList<String> writes) {
		/*try {
			FileWriter fstream = new FileWriter("1118-q6n.txt", true);
			BufferedWriter out = new BufferedWriter(fstream);

			for (int i = 0; i < writes.size(); i++) {
				out.write(writes.get(i) + '\n');
			}

			// Close the output stream
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
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

	public static void accuLatency(ArrayList<byte[]> ar2) {
		// for latency, get the last event's time
		byte[] lastTuple = ar2.get(ar2.size() - 1);
		double minTime = StreamAccessor.getMinTimestamp(lastTuple);
		long outputTime = (new Date()).getTime();
		Configure.latency += outputTime - minTime;

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

	// for caching results
	protected static void purgeResultBuffer(
			ArrayList<ArrayList<byte[]>> resultBuffers, double expiringTime,
			SchemaElement[] schArray) {
		// as events in a result buffer is sorted by end timestamp only,
		// I have to scan the whole buffer to purge expired results.

		// System.out.println("size " + ": " + results.size());

		// check one event expired or not
		for (int j = 0; j < resultBuffers.size(); j++) {
			ArrayList<byte[]> oneResult = resultBuffers.get(j);
			byte[] firstTuple = oneResult.get(0);

			double timestamp = StreamAccessor.getDoubleCol(firstTuple,
					schArray, 1);
			if (timestamp < expiringTime) {
				resultBuffers.remove(j);
				// System.out.println("removed");
				j--;
				// delete it;
			}

		}

	}
	
	//for caching results
	
	protected static void purgeResultCache(
			Hashtable<Integer, CacheResults> resultBuffers,
			double expiringTime, SchemaElement[] schArray) {
		// as events in a result buffer is sorted by end timestamp only,
		// I have to scan the whole buffer to purge expired results.

		Set<Integer> set1 = resultBuffers.keySet();

		Iterator<Integer> itr1 = set1.iterator();

		while (itr1.hasNext()) {
			Integer str = itr1.next();
			// System.out.println("id " + str);

			ArrayList<ArrayList<byte[]>> results = resultBuffers.get(str).getCache();

			// System.out.println("size " + ": " + results.size());

			// check one event expired or not
			for (int j = 0; j < results.size(); j++) {
				ArrayList<byte[]> oneResult = results.get(j);
				byte[] firstTuple = oneResult.get(0);

				double timestamp = StreamAccessor.getDoubleCol(firstTuple,
						schArray, 1);
				if (timestamp < expiringTime) {
					results.remove(j);
					//System.out.println("removed" + resultBuffers.get(str).getCache().size());
					
					; 
					j--;
					// delete it;
				}

			}

		}

	}

	public static void purgeResultCacheList(
			Hashtable<Integer, ArrayList<CacheResults>> cacheResultsNeg,
			double expiringTimestamp, SchemaElement[] schArray){
		
		ArrayList<CacheResults> results = cacheResultsNeg.get(1);
		if(results!=null){
		for(int i=0;i<results.size();i++)
		{
			CacheResults currentResult = results.get(i);
			if(currentResult.lefttime<expiringTimestamp)
				results.remove(i);
		}
		}
		
		// as events in a result buffer is sorted by end timestamp only,
		// I have to scan the whole buffer to purge expired results.

//		Set<Integer> set1 = resultBuffers.keySet();

//		Iterator<Integer> itr1 = set1.iterator();

//		while (itr1.hasNext()) {
//			Integer str = itr1.next();
			// System.out.println("id " + str);

//			ArrayList<NegativeCache> results = resultBuffers.get(str).getCache();

			// System.out.println("size " + ": " + results.size());

			// check one event expired or not
//			ArrayList<NegativeCache> resultList = resultBuffers.get(1);
//			for (int j = 0; j < results.size(); j++) {
//				ArrayList<byte[]> oneResult = results.get(j);
//				byte[] firstTuple = oneResult.get(0);
//
//				double timestamp = StreamAccessor.getDoubleCol(firstTuple,
//						schArray, 1);
//				if (timestamp < expiringTime) {
//					results.remove(j);
//					//System.out.println("removed" + resultBuffers.get(str).getCache().size());
//					
//					; 
//					j--;
					// delete it;
//				}
//
//			}
//
//		}

	}

	
	protected static void purgeResultBuffer(
			Hashtable<Integer, ArrayList<ArrayList<byte[]>>> resultBuffers,
			double expiringTime, SchemaElement[] schArray) {
		// as events in a result buffer is sorted by end timestamp only,
		// I have to scan the whole buffer to purge expired results.

		Set<Integer> set1 = resultBuffers.keySet();

		Iterator<Integer> itr1 = set1.iterator();

		while (itr1.hasNext()) {
			Integer str = itr1.next();
			// System.out.println("id " + str);

			ArrayList<ArrayList<byte[]>> results = resultBuffers.get(str);

			// System.out.println("size " + ": " + results.size());

			// check one event expired or not
			for (int j = 0; j < results.size(); j++) {
				ArrayList<byte[]> oneResult = results.get(j);
				byte[] firstTuple = oneResult.get(0);

				double timestamp = StreamAccessor.getDoubleCol(firstTuple,
						schArray, 1);
				if (timestamp < expiringTime) {
					results.remove(j);
					// System.out.println("removed");
					j--;
					// delete it;
				}

			}

		}

	}

	protected static int purgeStack(EventActiveInstanceQueue[] AIS,
			double expiringTime, SchemaElement[] schArray) {
		
		int purgedTuples = 0;
		
		for (int i = 0; i < AIS.length; i++) {
			if (AIS[i] == null)
				continue;
			else {
				byte[] tuple = AIS[i].eventQueue.peek();

				if (tuple == null)
					continue;
				else {

					// while (tuple != null)
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

						// tuple = AIS[i].getPreviousByPhysicalIndex(index,
						// retPointerArrayTemp);
					}

				}

			}

		}
		return purgedTuples;

	}

	/*
	 * protected static void purgeStack(EventActiveInstanceQueue[] AIS, double
	 * expiringTime, SchemaElement[] schArray) { for (int i = 0; i < AIS.length;
	 * i++) { if (AIS[i] == null) continue; else { byte[] tuple =
	 * AIS[i].eventQueue.peekLast();
	 * 
	 * if (tuple == null) continue; else {
	 * 
	 * // while (tuple != null) {
	 * 
	 * int index = StreamAccessor.getIndex(tuple); byte[][] retPointerArrayTemp
	 * = new byte[6][];
	 * 
	 * double timestamp = StreamAccessor.getDoubleCol(tuple, schArray, 1);
	 * String tupleType = getTupleType(tuple, schArray);
	 * 
	 * if (timestamp < expiringTime) {
	 * 
	 * 
	 * System.out .println(tupleType + timestamp + "purged"); if (timestamp ==
	 * 8) { System.out.print(8); }
	 * 
	 * 
	 * // byte[] returnedTuple = // AIS[i].eventQueue.dequeue(); double
	 * retrtimestamp = timestamp; while (retrtimestamp < expiringTime) { byte[]
	 * returnedTuple = AIS[i].eventQueue .dequeue();
	 * AIS[i].pointerQueue.dequeue();
	 * 
	 * // System.out.println("purged" + retrtimestamp);
	 * 
	 * returnedTuple = AIS[i].eventQueue.peek(); if (returnedTuple != null)
	 * retrtimestamp = StreamAccessor .getDoubleCol(returnedTuple, schArray, 1);
	 * else { break; }
	 * 
	 * }
	 * 
	 * // Actually, deleting one tuple means all the above // tuples can be
	 * deleted also.
	 * 
	 * }
	 * 
	 * // tuple = AIS[i].getPreviousByPhysicalIndex(index, //
	 * retPointerArrayTemp); }
	 * 
	 * }
	 * 
	 * }
	 * 
	 * }
	 * 
	 * }
	 *//*
		 * protected ArrayList<byte[]> checkPurgeTuples(double expiringTime,
		 * SchemaElement[] schArray) { ArrayList<byte[]> purgingTuple = new
		 * ArrayList<byte[]>(); for (int i = 0; i < this.AIS.length; i++) { if
		 * (this.AIS[i] == null) continue; else { byte[] tuple =
		 * this.AIS[i].eventQueue.peek();
		 * 
		 * if (tuple == null) continue; else { while (tuple != null) {
		 * 
		 * int index = StreamAccessor.getIndex(tuple); byte[][]
		 * retPointerArrayTemp = new byte[6][];
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

	/**
	 * check window constraint for And operator.
	 */
	protected static boolean windowOptAnd(ArrayList<byte[]> result,
			SchemaElement[] schArray) {
		// maintain the smallest timestamp so far and the largest timestamp so
		// far.
		// if timestamp is within, good.
		// if timestamp t is greater than largest, t-smallest < window, good,
		// update largest
		// if timestamp t is less than smallest, largest - t < window good,
		// update smallest,

		boolean output = true;

		double smallestTime = -1;
		double largestTime = -1;
		double currentTime = 0;

		// iterate each tuple in the result
		for (int i = 0; i < result.size(); i++) {
			byte[] currentTuple = result.get(i);
			currentTime = StreamAccessor
					.getDoubleCol(currentTuple, schArray, 1);

			// update boundary value for the first tuple
			if (smallestTime == -1 && largestTime == -1) {
				smallestTime = currentTime;
				largestTime = currentTime;
			}
			if (currentTime > largestTime) {
				if (currentTime - smallestTime < Configure.windowsize) {
					largestTime = currentTime;

				} else {
					output = false;
					break;
				}
			}

			else if (currentTime < smallestTime) {
				if (largestTime - currentTime < Configure.windowsize) {
					smallestTime = currentTime;
				} else {
					output = false;
					break;
				}
			} else if (currentTime > smallestTime && currentTime < largestTime) {
				continue;
			}

		}

		return output;
	}

	/**
	 * check window constraint for SEQ operator.
	 */
	protected static boolean windowOpt(ArrayList<byte[]> result,
			SchemaElement[] schArray) {
		boolean output = true;
		byte[] firstTuple = result.get(0);
		byte[] lastTuple = result.get(result.size() - 1);
		double timestampfirst = StreamAccessor.getDoubleCol(firstTuple,
				schArray, 1);
		double timestampsecond = StreamAccessor.getDoubleCol(lastTuple,
				schArray, 1);

		if (timestampsecond - timestampfirst >= Configure.windowsize)
			output = false;

		return output;
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
		
		if(ts < 0)
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
					double tempstamp = ts;
					
					if(ts == timestamp)
					{
						position = mid;  
					}

					if ((int) mid + 1 < resultBuffers.get(new Integer(queryID))
							.size()) {
						ArrayList<byte[]> checkingTuple_plus1 = resultBuffers
								.get(new Integer(queryID)).get((int) mid + 1);
						double timestamp_plus1 = StreamAccessor.getDoubleCol(
								checkingTuple_plus1.get(checkingTuple_plus1
										.size() - 1), schArray, 1);

						if (timestamp < tempstamp)
							low = mid + 1;
						else if (timestamp > tempstamp)
							high = mid - 1;
						else if (mid + 1 <= resultBuffers.get(
								new Integer(queryID)).size() - 1
								&& timestamp <= tempstamp
								&& timestamp_plus1 >= tempstamp) {
							position = mid;
							break;
						} else {
							position = mid + 1;
							break;
						}
					} else
						break;
				}

			}

		}
		if (low > high) {
			position = low;
		}
		while (position + 1 <= high && position != -1) {
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

	/**
	 * currenttuple: the upper composite tuple ear: the inner to to joined
	 */
	public static ArrayList<ArrayList<byte[]>> connect_bytime_SEQ(
			ArrayList<byte[]> currenttuple, ArrayList<ArrayList<byte[]>> ear,
			int insertPos, SchemaElement[] schArray) {
		ArrayList<ArrayList<byte[]>> earray = new ArrayList<ArrayList<byte[]>>();

		int copyinsertPos = insertPos;
		int i = 0;

		// for every going to be joined tuple
		for (; ear != null && i < ear.size(); i++) {
			boolean added = false;
			ArrayList<byte[]> array = new ArrayList<byte[]>();

			array = ear.get(i);

			ArrayList<byte[]> array_updated = new ArrayList<byte[]>();

			for (int tIndex = 0; tIndex < currenttuple.size(); tIndex++) {
			
			 if (insertPos == tIndex) {
					double timestamp = StreamAccessor.getDoubleCol(currenttuple
							.get(insertPos), schArray, 1);

					double current_left = 0; 
					if(insertPos > 0)
					{
						 current_left = StreamAccessor.getDoubleCol(
								currenttuple.get(insertPos - 1), schArray, 1);
	
					}else
					{
						current_left = StreamAccessor.getDoubleCol(
								currenttuple.get(0), schArray, 1);
					}
					
					double child_left = StreamAccessor.getDoubleCol(array
							.get(0), schArray, 1);

					double beforetime = StreamAccessor.getDoubleCol(array
							.get(array.size() - 1), schArray, 1);

					// test start
					/*
					 * if(current_left == 34198.925 && timestamp == 34201.581)
					 * System.out.println("bug place");
					 * 
					 * if(child_left == 34201.168 && beforetime == 34201.408)
					 * System.out.println("bug place");
					 */
					// test end

					if (insertPos > 0 && timestamp > beforetime
							&& child_left > current_left) {
						for (byte[] addingTuple : array) {
							array_updated.add(addingTuple);
							added = true;
						}

						tIndex--;
						insertPos = -1;
					}
					// compare the timestamp

					else if (insertPos == 0 && beforetime < current_left) {
						for (byte[] addingTuple : array) {
							array_updated.add(addingTuple);
							added = true;
						}

						tIndex--;
						insertPos = -1;
					}
				} else {
					array_updated.add(currenttuple.get(tIndex));
				}

			}

			if (insertPos == array.size()) {
				double currentTime = StreamAccessor.getDoubleCol(currenttuple
						.get(insertPos), schArray, 1);
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

	public static ArrayList<ArrayList<byte[]>> connect_bytime_AND(
			ArrayList<byte[]> currenttuple, ArrayList<ArrayList<byte[]>> ear,
			int insertPos, SchemaElement[] schArray) {
		ArrayList<ArrayList<byte[]>> earray = new ArrayList<ArrayList<byte[]>>();

		int copyinsertPos = insertPos;
		int i = 0;

		// for every going to be joined tuple
		for (; ear != null && i < ear.size(); i++) {
			boolean added = false;
			ArrayList<byte[]> array = new ArrayList<byte[]>();

			array = ear.get(i);

			ArrayList<byte[]> array_updated = new ArrayList<byte[]>();

			for (int tIndex = 0; tIndex < currenttuple.size(); tIndex++) {
				if (insertPos == tIndex) {

					if (insertPos > 0) {
						for (byte[] addingTuple : array) {
							array_updated.add(addingTuple);
							added = true;
						}

						tIndex--;
						insertPos = -1;
					}

					else if (insertPos == 0) {
						for (byte[] addingTuple : currenttuple) {
							array_updated.add(addingTuple);
							added = true;
						}

						tIndex--;
						insertPos = -1;
					}
				} else {
					array_updated.add(currenttuple.get(tIndex));
				}

			}

			if (insertPos == array.size()) {
				for (byte[] addingTuple : currenttuple) {
					array_updated.add(addingTuple);
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
	
	public static ArrayList<String> lastQueryTypes(ArrayList<QueryInfo> queries) {

		ArrayList<String> lastTypes = new ArrayList<String>();
		for (int i = 0; i < queries.size(); i++) {
			ArrayList<String> stacks = queries.get(i).stackTypes;
			String ltype = stacks.get(stacks.size() - 1);
			lastTypes.add(ltype);
		}

		return lastTypes;
	}

	
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


	public static byte[] binarySearch_rightbounds(
			EventActiveInstanceQueue eventQueue, double ts,
			SchemaElement[] schArray) {

		byte[] tuplepeekLast = eventQueue.eventQueue.peekLast();

		// get the timestamp
		double timestamppeekLast = StreamAccessor.getDoubleCol(tuplepeekLast,
				schArray, 1);
		int tuplepeekLastIndex = StreamAccessor.getIndex(tuplepeekLast);

		// test start
		byte[] tu = eventQueue.eventQueue.get(tuplepeekLastIndex);
		double t = StreamAccessor.getDoubleCol(tu, schArray, 1); // test end

		if (timestamppeekLast <= ts)
			return tuplepeekLast;
		byte[] tuple = eventQueue.eventQueue.peekLast();
		int high = StreamAccessor.getIndex(tuple);

		if (high < 0)
			return tuple;

		while (tuple != null) {

			int index = StreamAccessor.getIndex(tuple);
			byte[][] retPointerArrayTemp = new byte[6][];

			double timestamp = StreamAccessor.getDoubleCol(tuple, schArray, 1);

			if (timestamp <= ts)
				break;

			String tupleType = getTupleType(tuple, schArray);
			tuple = eventQueue.getPreviousByPhysicalIndex(index,
					retPointerArrayTemp);

		}

		return tuple;

	}

	public static byte[] binarySearch_leftbounds(
			EventActiveInstanceQueue eventQueue, double ts,
			SchemaElement[] schArray) {

		byte[] tuplepeekLast = eventQueue.eventQueue.peekLast();

		// get the timestamp
		double timestamppeekLast = StreamAccessor.getDoubleCol(tuplepeekLast,
				schArray, 1);
		int tuplepeekLastIndex = StreamAccessor.getIndex(tuplepeekLast);

		// test start
		byte[] tu = eventQueue.eventQueue.get(tuplepeekLastIndex);
		double t = StreamAccessor.getDoubleCol(tu, schArray, 1); // test end

		if (timestamppeekLast <= ts)
			return tuplepeekLast;
		byte[] tuple = eventQueue.eventQueue.peekLast();
		int high = StreamAccessor.getIndex(tuple);

		if (high < 0)

			return tuple;

		double previousTimestamp = 0;
		byte[] previousTuple = null;
		double timestamp = 0;
		while (tuple != null) {

			int index = StreamAccessor.getIndex(tuple);
			byte[][] retPointerArrayTemp = new byte[6][];

			String tupleType = getTupleType(tuple, schArray);

			previousTimestamp = timestamp;
			previousTuple = tuple;

			tuple = eventQueue.getPreviousByPhysicalIndex(index,
					retPointerArrayTemp);

			timestamp = StreamAccessor.getDoubleCol(tuple, schArray, 1);

			if (timestamp < ts && previousTimestamp >= ts)
				break;

		}

		return previousTuple;

	}

	/*
	 * public static int binarySearch_leftbounds( EventActiveInstanceQueue
	 * eventQueue, double ts, SchemaElement[] schArray) { // I need to add one
	 * special case. // if the first event's timestamp (smallest-peek) is
	 * greater than ts, // return ts.
	 * 
	 * byte[] tuplepeek = eventQueue.eventQueue.peek();
	 * 
	 * // get the timestamp double timestamppeek =
	 * StreamAccessor.getDoubleCol(tuplepeek, schArray, 1); int tuplepeekIndex =
	 * StreamAccessor.getIndex(tuplepeek);
	 * 
	 * if (timestamppeek >= ts) return tuplepeekIndex;
	 * 
	 * int low = 0;
	 * 
	 * byte[] tuple = eventQueue.eventQueue.peekLast(); int high =
	 * StreamAccessor.getIndex(tuple);
	 * 
	 * int mid = 0; int lastmid;
	 * 
	 * int position = 0; if (high < 0) return 0; while (low <= high) { lastmid =
	 * mid; mid = (low + high) / 2;
	 * 
	 * byte[] checkingTuple = eventQueue.eventQueue.get((int) mid);
	 * 
	 * if (checkingTuple == null) { mid = lastmid; position = mid; break; }
	 * double timestamp = StreamAccessor.getDoubleCol(checkingTuple, schArray,
	 * 1); double tempstamp = ts;
	 * 
	 * byte[] checkingTuple_plus1 = eventQueue.eventQueue .get((int) mid + 1);
	 * double timestamp_plus1 = StreamAccessor.getDoubleCol(
	 * checkingTuple_plus1, schArray, 1);
	 * 
	 * if (timestamp < tempstamp) low = mid + 1; else if (timestamp > tempstamp)
	 * high = mid - 1; else if (mid + 1 <= eventQueue.getSize() - 1 && timestamp
	 * <= tempstamp && timestamp_plus1 >= tempstamp) { position = mid; break; }
	 * else { position = mid + 1; break; } } if (low > high) { position = low; }
	 * return position;
	 * 
	 * }
	 */
	public static int binarySearch(SingleReaderEventQueueArrayImp eventQueue,
			double ts, SchemaElement[] schArray) {
		// I need to add two special cases.
		// if the last event's timestamp (largest-peeklast) is less than ts,
		// return the last event

		// if the first event's timestamp (smallest-peek) is greater than ts,

		int low = 0;

		byte[] tuple = eventQueue.peekLast();
		int high = StreamAccessor.getIndex(tuple);

		int mid = 0;
		int lastmid;

		int position = 0;
		if (high < 0)
			return 0;
		while (low <= high) {
			lastmid = mid;
			mid = (low + high) / 2;

			byte[] checkingTuple = eventQueue.get((int) mid);

			if (checkingTuple == null) {
				mid = lastmid;
				position = mid;
				break;
			}
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
