package com.hp.hpl.CHAOS.Aggregation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * This class is used to maintain a list of ShapShots
 * @author Kenneth J. Loomis, 2012
 *
 */

public class SnapShotMap {
	
	private Map<Double, SnapShot> ssm;
	
	/**
     * Constructor
	 * Copy the snapshot list that is passed into the function
     */
	public SnapShotMap( List<SnapShot> ssl ) {
		ssm = new HashMap<Double, SnapShot>();
		if ( ssl != null )
		{
			Iterator< SnapShot > iter = ssl.iterator();
			while ( iter.hasNext() )
			{
				SnapShot ss = iter.next();
				if ( ssm.containsKey(ss.getTime()) ) // Merge snapshots
					ssm.put(ss.getTime(), new SnapShot(ss.getTime(), ss.getCount() + ssm.get(ss.getTime()).getCount() ) );
				else // New snapshot
					ssm.put(ss.getTime(), ss);     		
			}
		}
	}
	
	/**
     * Create a list of snapshots to be used to create a new snapshot
     */
	public List<SnapShot> createSnapShot( double curr_ts, int multiplier ) {
		if ( this.ssm.isEmpty() || multiplier == 0 ) return null;
		List<SnapShot> newssl = new ArrayList<SnapShot>( );
		Collection<SnapShot> ssSet = this.ssm.values();
		// Iterate existing snapshots, creating a new snapshot list. Expiring old ones.
		if ( !ssSet.isEmpty() )
		{
			for ( SnapShot ss: ssSet)
			{
				SnapShot newss = new SnapShot (ss.getTime(), ss.getCount()*multiplier);
				if ( ss.isValid ( curr_ts ) && newss.getCount() > 0 )
					newssl.add(newss);    // Add non-zero snapshots 
				//else
				//	ssm.remove( ss.getTime() ); // Remove expired timestamps
			}
		}
		// return a list of valid snapshots with the multiplier applied.
		return newssl;
	}
	
	/**
     * Returns the result
	 * 
	 * Currently uses expiration time, still testing 
     */
	public int getResult ( double curr_ts )//should use current timestamp? 
	{
		int result = 0;
		Collection<SnapShot> ssSet = ssm.values();
		// Iterate existing snapshots, calculating the result. Expiring old snapshots.
		for ( SnapShot ss: ssSet)
		{
			if ( ss.isValid ( curr_ts ) && ss.getCount() > 0 )
				result += ss.getCount(); 
			//else
			//	ssm.remove( ss.getTime() );
		}
		// return the total from all unexpired snapshots
		return result;
	}
	

}
