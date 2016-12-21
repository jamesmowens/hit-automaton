package com.hp.hpl.CHAOS.Aggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * This class is used for mixed counters (snapshots and prefix counters)
 * @author Kenneth J. Loomis, 2012, Cheng Cheng
 *
 */

public class MixCounter {
	
	PrefixCounter pc;
	//Map<SeqMetaData,SnapShotMap> SSLMap = new HashMap<SeqMetaData, SnapShotMap>();
	Map<String,SnapShotMap> SSLMap = new HashMap<String, SnapShotMap>();
	//private List<SnapShot> ssl;
    /**
     * Constructor
	 * compute the ending timestamp for slide by tuple window 
     * @param arrivalTime
     * @param windowSize
     */
	public MixCounter(double arrivalTime, double windowSize, int prefixNum) {

		this.pc = new PrefixCounter (arrivalTime, windowSize, prefixNum );
	}
	/**
	 * put new ssl into SSLMap
	 * @param queryID
	 * @param ssl
	 * @return
	 */
	public void updateSSLMap(SeqMetaData metaQueryID, List<SnapShot> newssl )
	{	
		
		SSLMap.put(metaQueryID.toString(), new SnapShotMap(newssl));
		//return SSLMap;
		
	}

	
	/**
	 * Update the underlying prefix counter
	 * @param index
	 */
	void updateCount(int index) {
		pc.updateCount (index);
	}
	
	/**
	 * Get the result for a given query in this mixed counter.
	 * use the queryID find the correct snapshot list
	 * calculate the result and return.
	 * @param curr_ts
	 */
	int getResult ( double curr_ts, SeqMetaData metaQueryID )
	{
		if ( metaQueryID.getQID() == 2) 
			pc.isExpired(curr_ts);
		if ( metaQueryID.isFirst() ) // If it is the first subsequence return only the prefix counter results
			return pc.getResult( );
		else 
			if (this.SSLMap.get(metaQueryID.clonePreceding().toString()) !=null){
				int rtnVal = this.SSLMap.get(metaQueryID.clonePreceding().toString()).getResult( curr_ts ) * pc.getResult( ); 
				return rtnVal; 
			}
			else
			{
				// This should never happen. Retained for testing purposes only.
				System.out.print(metaQueryID.clonePreceding().toString());
				return 0;
			}
	}
	
	/**
	 * Determine if the mix counter is expired. This is dependent
	 * upon the underlying prefix counter.
	 * @param curr_ts
	 */
	Boolean isExpired ( double curr_ts )
	{
		return ( pc.isExpired ( curr_ts ));
	}
	
	/** 
	 * Create a snapshot of the current mixed counter. This will be
	 * used to get a snapshot for the mixed counter of following subseq.
	 * 
	 *  Currently ignores expiration time
	 */
	List<SnapShot> createSnapshot ( double curr_ts, SeqMetaData metaQueryID )
	{	
		List<SnapShot> newssl = null;
		
		if ( !metaQueryID.isFirst() )
		{
			SeqMetaData preSeq = metaQueryID.clonePreceding();
			SnapShotMap currssl = this.SSLMap.get(preSeq.toString());
			if ( currssl != null ){
				int multipler = pc.getResult();
				newssl = currssl.createSnapShot( curr_ts, multipler );
				// There is a preceding snap shot list.
			}
				
			//newssl = this.SSLMap.get(preSeq).createSnapShot( curr_ts, pc.getResult() );
			// TODO: See if this is needed
			/*
			if ( newssl == null && pc.getResult() > 0 ) // There was nothing to populate
			{
				newssl = new ArrayList<SnapShot>( );
				newssl.add( pc.makeSnapShot() );
			}
			*/
		}
		else if ( pc.getResult() > 0 ) // This is the first subsequence && it was a count
		{
			newssl = new ArrayList<SnapShot>( );
			newssl.add( pc.makeSnapShot() );
		}
		
		// TODO: Clean up this mess
		/*SnapShotList currssl;
		currssl = this.SSLMap.get(preSeq);
		
		if ( currssl != null ) // There is a preceding snap shot list.
			newssl = currssl.createSnapShot( curr_ts, pc.getResult() );
		if ( newssl == null && pc.getResult() > 0 ) // There was nothing to populate
		{
			newssl = new ArrayList<SnapShot>( );
			newssl.add( pc.makeSnapShot() );
		}
		*/
		return newssl;
	}
	
}
