package com.hp.hpl.CHAOS.Aggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MixCounterListMgr {
	/**
	 * This class is used to manage all counter lists
	 * Basically a mapping from each query/subsequence to its counter list
	 * @author Yingmei Qi, Kenneth J. Loomis
	 */
	
	//an ArrayList which stores the PrefixCounterManager for each subseq/query
	//index is the subseq/query ID, value is the counter list
	Map<Integer, MixCounterList>  counterMgrs = new HashMap<Integer, MixCounterList>();
	
	//check whether this subseq has a PrefixCounterManager or not
	boolean isEmpty(int subseqID) {
		
		return (!counterMgrs.containsKey(subseqID));
			
	}
	
	/**
	 * given a subseq information, create a new counter for this subseq
	 * @param subseqID
	 * @param time
	 * @param window
	 * @param prefixNum
	 */
	void createPrefixCounter(int subseqID, double curr_ts, int window, int prefixNum) {
		
		//if it is the first start event for this subseq
		if(isEmpty(subseqID)) {
			//initialize the PrefixCounterManager for this subseq
			MixCounterList aCounterMgr = new MixCounterList(subseqID);
			counterMgrs.put(subseqID, aCounterMgr);
		}
		
		//create a new counter and add it to the counterMgr
		MixCounterList aCounterMgr = counterMgrs.get(subseqID);
		aCounterMgr.addPrefixCounter( curr_ts, window, prefixNum );
	}
	
	/**
	 * Create a new mixed counter for this subsequence by getting the snapshots
	 * otherwise the same as the other constructor.
	 * @param subseqID
	 * @param time
	 * @param window
	 * @param prefixNum
	 * @param SSIDList
	 */
	void createMixCounter(int subseqID, double curr_ts, int window, int prefixNum, ArrayList<SeqMetaData> precedingMetaList) {
		
		//if it is the first start event for this subseq
		if( isEmpty( subseqID ) ) {
			//initialize the PrefixCounterManager for this subseq
			MixCounterList aCounterMgr = new MixCounterList( subseqID );
			counterMgrs.put( subseqID, aCounterMgr );
		}
		
		// Create a new counter
		MixCounter tmpCounter = new MixCounter( curr_ts, window, prefixNum );
		
		if (subseqID == 2)
			subseqID = 2;
		// Get snapshots from previous seqCounters for all preceding mix counters and
		// add them to the new mix counter. 
		for ( SeqMetaData metaData: precedingMetaList ){
			tmpCounter.updateSSLMap ( metaData, constructSnapShotList( metaData, curr_ts ) );
		}
		
		// Add it to the counterMgr
		counterMgrs.get( subseqID ).addMixCounter( tmpCounter );
	}

	
	/**
	 * get all snapshot from the previous seq counters
	 * and add them to a single list
	 */
	List<SnapShot> constructSnapShotList( SeqMetaData precedingID, double curr_ts ) {
		
		List<SnapShot> ssl = new ArrayList<SnapShot>( );
		//create snapshot
		int preSeq = precedingID.getSubID( );
		MixCounterList precedingList = counterMgrs.get( preSeq );
		// This may be null if the preceding list is not created yet
		if ( precedingList != null ) 
		{			
			List<SnapShot> newssl = precedingList.getSnapShots ( curr_ts, precedingID );
			// This may be null if no snapshots were created
			if ( newssl != null )
				ssl.addAll ( newssl );
		}
		
		return ssl;
	}
	

	
	void updateSeqCounters(int subseqID, double curr_ts, int index) {
		
		counterMgrs.get(subseqID).updateCounters(curr_ts, index);
	}
	
	int getResult ( int subseqID, double curr_ts, SeqMetaData curr_SMD )
	{
		return counterMgrs.get(subseqID).getSumandExpire( curr_ts, curr_SMD );
	}

}
