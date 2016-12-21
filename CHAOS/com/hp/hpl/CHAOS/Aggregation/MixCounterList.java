package com.hp.hpl.CHAOS.Aggregation;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.hp.hpl.CHAOS.ECube.*;

public class MixCounterList {
	/**
	 * This class is used to manage a list of prefixCounters or mixCounters
	 * Each counter list is specific to a query or a subsequence 
	 * @author Yingmei Qi, Kenneth J. Loomis, Cheng Cheng.
	 */
	
	int seqID;	
	//define a LinkedList to accommodate all MixCounters
	LinkedList<MixCounter> counterList = new LinkedList<MixCounter>();
	
	/**
	 * Constructor 
	 * @author Yingmei Qi, Cheng Cheng.
	 */
	MixCounterList(int seqID) {
		this.seqID = seqID;
	}

	/**
	 * Create a new prefix counter, and append it to the counterList 
	 * @author Yingmei Qi, Kenneth J. Loomis, Cheng Cheng.
	 */
	void addPrefixCounter(double arrivalTime, double windowSize, int prefixNum) {
		MixCounter counter = new MixCounter(arrivalTime, windowSize, prefixNum);
		counterList.add(counter);
	}
	
	// TODO: Might not need this anymore, remove if possible
	/**
	 * create a new mix counter with its snapshot map, and 
	 * append it to the counter list 
	 * @author Yingmei Qi, Kenneth J. Loomis, Cheng Cheng.
	 */
	void addMixCounter(double arrivalTime, double windowSize, int prefixNum, Map<SeqMetaData, List<SnapShot>> preSnapshotList){
		MixCounter counter = new MixCounter(arrivalTime, windowSize, prefixNum);
		
		//add all snapshot list to this counter
		Iterator iter = preSnapshotList.entrySet().iterator();
		while (iter.hasNext()){
			SeqMetaData metaQueryID = (SeqMetaData) iter.next();
			counter.updateSSLMap(metaQueryID, preSnapshotList.get(metaQueryID));
		}
		
		counterList.add(counter);
	}
	
	/**
	 * Append a counter to the counter list 
	 * @author Kenneth J. Loomis.
	 */
	void addMixCounter ( MixCounter tmp )
	{
		counterList.add( tmp );
	}
	
	/**
	 * check ending timestamps of each counter, 
	 * if not expired, update the count
	 * if expired, just skip all the counters and before 
	 * @author Yingmei Qi, Kenneth J. Loomis, Cheng Cheng.
	 */
	void updateCounters (double curr_ts, int index) {
		
		//use iterator to access all the counters in counterList
		Iterator< MixCounter > it = counterList.descendingIterator();
		while (it.hasNext()) {
			MixCounter mc = (MixCounter) it.next();
			
			//if not expired
			if ( !mc.isExpired( curr_ts ) ) {
				mc.updateCount( index );
			}
			else break;
		}
	}
	
	/**
	 * get the sum of all valid counters and remove the expired counters
	 * @param curr_ts 
	 * @author Yingmei Qi, Kenneth J. Loomis, Cheng Cheng.
	 */
	int getSumandExpire(double curr_ts, SeqMetaData curr_SMD ) {
		
		int aggResult = 0;
		
		Iterator< MixCounter > it = counterList.descendingIterator();
		while (it.hasNext()) {
			MixCounter mc = (MixCounter) it.next();
			if ( !mc.isExpired( curr_ts ) ) {
				aggResult = aggResult + mc.getResult( curr_ts, curr_SMD );
			}
			//find the first expired counter, remove all counters before it
			else {
				int expireStart = counterList.indexOf( mc );
				counterList.subList( 0, expireStart+1 ).clear( );
				break;
			}
			
		}
		
		return aggResult;
	}
	
	/**
	 * add all <exptime, count> pairs of each counter to this snapshot list
	 * @param curr_ts
	 * @return a snapshot list of all counters
	 * @author Yingmei Qi, Kenneth J. Loomis, Cheng Cheng.
	 */
	List<SnapShot> getSnapShots ( double curr_ts,  SeqMetaData metaQueryID)
	{
		List<SnapShot> ssl = new ArrayList<SnapShot>();
		
		Iterator< MixCounter > it = counterList.descendingIterator();
		while (it.hasNext()) {
			MixCounter mc = (MixCounter) it.next();
			if ( !mc.isExpired( curr_ts ) ) {
				List<SnapShot> tmp = mc.createSnapshot ( curr_ts, metaQueryID );
				if (tmp != null)
					ssl.addAll( tmp );
			}
			//find the first expired counter, remove all counters before it
			else {
				int expireStart = counterList.indexOf( mc );
				counterList.subList( 0, expireStart+1 ).clear( );
				break;
			}
		}
		return ssl;
	}
	
}
