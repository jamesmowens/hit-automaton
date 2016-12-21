package com.hp.hpl.CHAOS.Aggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PrefixCounterListMgr {
	/**
	 * This class is used to manage all counter lists
	 * Basically a mapping from each query to its counter list
	 * @author Yingmei Qi, Kenneth J. Loomis
	 */
	
	//an ArrayList which stores the PrefixCounterManager for each subseq/query
	//index is the subseq/query ID, value is the counter list
	Map<Integer, PrefixCounterList>  counterMgrs = new HashMap<Integer, PrefixCounterList>();
	
	//check whether this query has a PrefixCounterManager or not
	boolean isEmpty(int queryID) {
		
		return (!counterMgrs.containsKey(queryID));
			
	}
	
	/**
	 * given a query information, create a new counter for this query
	 * @param queryID
	 * @param time
	 * @param window
	 * @param prefixNum
	 */
	void createPrefixCounter(int queryID, double curr_ts, int window, int prefixNum) {
		
		//if it is the first start event for this subseq
		if(isEmpty(queryID)) {
			//initialize the PrefixCounterManager for this subseq
			PrefixCounterList aCounterMgr = new PrefixCounterList(queryID);
			counterMgrs.put(queryID, aCounterMgr);
		}
		
		//create a new counter and add it to the counterMgr
		PrefixCounterList aCounterMgr = counterMgrs.get(queryID);
		aCounterMgr.addPrefixCounter( curr_ts, window, prefixNum );
	}
	
	/**
	 * update all valid counters in the counter list
	 * @param queryID
	 * @param curr_ts
	 * @param index
	 */
	void updateSeqCounters(int queryID, double curr_ts, int index) {
		
		counterMgrs.get(queryID).updateCounters(curr_ts, index);
	}
	
	
	/**
	 * update counters in a Prefix Share tree, based on the pipelined parent result
	 * @param queryID
	 * @param curr_ts
	 * @param pid
	 */
	void updatePSCounters(int queryID, double curr_ts, int pid, int parentIndex) {
		
		//get the paraent counter list
		PrefixCounterList parentCounterList = counterMgrs.get(pid);
		
		PrefixCounterList counterList = counterMgrs.get(queryID);
		counterList.updatePSCounters(curr_ts, parentCounterList, parentIndex);
	}
	
	/**
	 * remove expired counters for all subseqs in the Prefix Share tree
	 * @param expireIndex
	 * @param treeIDList
	 */
	void expireTreeCounters(double curr_ts, ArrayList<Integer> treeIDList) {
		
		for(int i = 0; i< treeIDList.size(); i++) {
			if (!isEmpty(i)){
				counterMgrs.get(i).rangeRemove(curr_ts);
				
			}
		}
	}
	
	/**get result for a query/subseq (a counter list) and remove the expired counters
	 * @param queryID
	 * @param curr_ts
	 * @return
	 */
	int getResult ( int queryID, double curr_ts)
	{
		return counterMgrs.get(queryID).getSumandExpire( curr_ts );
	}
	
	/**only get result for a query/subseq (a counter list) but NOT remove the expired counters
	 * @param queryID
	 * @param curr_ts
	 * @return
	 */
	int getOnlyResult ( int queryID, double curr_ts)
	{
		return counterMgrs.get(queryID).getSum( curr_ts );
	}
	
	
	PrefixCounterList getCounterList(int ID) {
		return counterMgrs.get(ID);
	}

}
