package com.hp.hpl.CHAOS.Aggregation;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.hp.hpl.CHAOS.ECube.*;

public class PrefixCounterList {
	/**
	 * This class is used to manage a list of prefixCounters or mixCounters
	 * Each counter list is specific to a query or a subsequence 
	 * @author Yingmei Qi, Kenneth J. Loomis, Cheng Cheng.
	 */
	
	int seqID;	
	//define a LinkedList to accommodate all MixCounters
	LinkedList<PrefixCounter> counterList = new LinkedList<PrefixCounter>();
	
	/**
	 * Constructor 
	 */
	PrefixCounterList(int seqID) {
		this.seqID = seqID;
	}

	/**
	 * Create a new prefix counter, and append it to the counterList 
	 */
	void addPrefixCounter(double arrivalTime, double windowSize, int prefixNum) {
		PrefixCounter counter = new PrefixCounter(arrivalTime, windowSize, prefixNum);
		this.counterList.add(counter);
	}
	

	/**
	 * check ending timestamps of each counter, 
	 * if not expired, update the count
	 * if expired, just skip all the counters and before 
	 */
	void updateCounters (double curr_ts, int index) {
		
		//use iterator to access all the counters in counterList
		Iterator< PrefixCounter > it = counterList.descendingIterator();
		while (it.hasNext()) {
			PrefixCounter pc = (PrefixCounter) it.next();
			
			//if not expired
			if ( !pc.isExpired( curr_ts ) ) {
				pc.updateCount( index );
			}
			else break;
		}
	}
	
	/**
	 * update counters for negaation event type
	 * @param curr_ts
	 * @param index
	 */
	void updateNegCounters (double curr_ts, int index) {
		
		//use iterator to access all the counters in counterList
		Iterator< PrefixCounter > it = counterList.descendingIterator();
		while (it.hasNext()) {
			PrefixCounter pc = (PrefixCounter) it.next();
			
			//if not expired
			if ( !pc.isExpired( curr_ts ) ) {
				pc.updateNegCount( index );
			}
			else break;
		}
	}
	
	/**
	 * check ending timestamps of each counter, 
	 * if not expired, update the count based on the pipelined result from its prefix
	 * if expired, just skip all the counters and before 
	 */
	void updatePSCounters (double curr_ts, PrefixCounterList parentCounterList, int parentIndex) {
		
		//use iterator to access all the counters in counterList
		Iterator< PrefixCounter > it = counterList.descendingIterator();
		//begin from the last counter to update
		int size = counterList.size();
		int updateIndex = counterList.size() - 1;
		
		while (it.hasNext()) {
			PrefixCounter pc = (PrefixCounter) it.next();
	
			//if not expired
			if ( !pc.isExpired( curr_ts ) ) {
				
				//get the count of its prefix(parent) counter at the same location
				int parentCount = parentCounterList.getCount(updateIndex, parentIndex);
				pc.updatePSCount( parentCount );
				//move one step to the previous counter
				updateIndex--;
			}
			else break;
		}
	}
	
	/**
	 * get the total count (at a certain index in the counter) of a counter (at a certain location in the counter list)
	 */
	int getCount(int loc, int parentIndex) {
		return counterList.get(loc).getCertainCount(parentIndex);
	}

	/**
	 * get the memory status (how many prefix counters have been created)
	 * @return
	 */
	int getMemoryStatistics(double curr_ts) {
		
		int pc_num = 0;
		Iterator< PrefixCounter > it = counterList.descendingIterator();
		
		while(it.hasNext()) {
			PrefixCounter pc = (PrefixCounter) it.next();
			if ( !pc.isExpired( curr_ts ) ) {
				pc_num++;
			}
		}
		
		return pc_num;
	}
	
	/**
	 * get the sum of all valid counters and remove the expired counters
	 * @param curr_ts 
	 */
	int getSumandExpire( double curr_ts ) {
		
		int aggResult = 0;
		//int pc_num = 0;
		
		Iterator< PrefixCounter > it = counterList.descendingIterator();
		while (it.hasNext()) {
			PrefixCounter pc = (PrefixCounter) it.next();
			if ( !pc.isExpired( curr_ts ) ) {
				aggResult = aggResult + pc.getResult( );
				//pc_num++;
			}
			//find the first expired counter, remove all counters before it
			else {
				int expireStart = counterList.indexOf( pc );
				if (expireStart != 0){
					counterList.subList(0, expireStart).clear();
				}
				break;
			}
			
		}
		
		return aggResult;
	}
	
	
	/**
	 * only get the sum of all valid counters
	 * @param curr_ts 
	 */
	int getSum( double curr_ts ) {
		
		int aggResult = 0;
		
		Iterator< PrefixCounter > it = counterList.descendingIterator();
		while (it.hasNext()) {
			PrefixCounter pc = (PrefixCounter) it.next();
			if ( !pc.isExpired( curr_ts ) ) {
				aggResult = aggResult + pc.getResult( );
			}		
		}
		
		return aggResult;
	}
	
	/**
	 * get index of the first expired counter
	 * @param curr_ts
	 * @return
	 */
	int getExpireIndex(double curr_ts){
		
		int expireIndex = -1;
		
		Iterator< PrefixCounter > it = counterList.descendingIterator();
		while (it.hasNext()) {
			PrefixCounter pc = it.next();
			if (pc.isExpired(curr_ts)) {
				expireIndex = counterList.indexOf(pc);
				break;
			}
		}
		
		return expireIndex;
	}
	
	/**
	 * remove the counters from the leftmost to a certain location
	 */
	void rangeRemove(double curr_ts) {
		int expireIndex = getExpireIndex(curr_ts);
		if (expireIndex != -1){
			counterList.subList(0, expireIndex).clear();
		}
		
	}
}
