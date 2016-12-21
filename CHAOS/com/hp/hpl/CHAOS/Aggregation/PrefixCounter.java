package com.hp.hpl.CHAOS.Aggregation;

/**
 * This class is used for set prefix Count Info
 * construct the array
 * set endTime
 * set startTime
 * set array
 * @author chengcheng, Yingmei Qi
 *
 */

public class PrefixCounter {
	
	double endTime;
	double startTime = (double) 0;
	int prefixNum;
	int[] prefixCounter;
	
    /**
     * Constructor
	 * compute the ending timestamp for slide by tuple window 
     * @param arrivalTime
     * @param windowSize
     */
	public PrefixCounter(double arrivalTime, double windowSize, int prefixNum) {
		startTime = arrivalTime;
		endTime =  arrivalTime + windowSize;
		this.prefixNum = prefixNum;
		this.prefixCounter = new int[prefixNum];
	}
	
    /**
     * Constructor
	 * compute the ending timestamp for time based window 
	 * endTime equals to the unbound of arrivalTime divide windowSize times slidingSize minus 1
     * @param arrivalTime
     * @param windowSize
     * @param slidingSize
     */
	public PrefixCounter(double arrivalTime, double windowSize, double slidingSize) {
		endTime = (double) (Math.ceil(arrivalTime/windowSize) * slidingSize -1);
	}
	
    /**
	 * starting event is negative type, set the second event's start valid timestamp
     * @param NegativeEndTime
     */
	void setValidTimestamp(double NegativeEndTime) {
		startTime = NegativeEndTime;
	}
	/**
	 * set array for positive tuple
	 * @param index
	 */
	void updateCount(int index) {
		if (index == 0) {
			prefixCounter[index] = prefixCounter[index] +1;
		}
		else
		prefixCounter[index]= prefixCounter[index-1] + prefixCounter[index];
	}
	
	/**
	 * update count for Prefix Sharing(PS) case
	 * @param parentCount
	 */
	void updatePSCount(int parentCount){
			prefixCounter[0] = parentCount + prefixCounter[0];
	}
	
	/**
	 * clear count to 0 for the prefix previous to the negation type
	 * @param index
	 */
	void updateNegCount(int index) {   
		prefixCounter[index] = 0;
	}
	
	/**
	 * Determine if the counter has expired
	 * @param curr_ts
	 */
	Boolean isExpired ( double curr_ts )
	{
		return ( curr_ts > endTime );
	}

	/**
	 * Return the result of the counter
	 * @param curr_ts
	 */
	int getResult ( )
	{
		return this.prefixCounter[ this.prefixNum-1 ];
	}
	
	/**
	 * 
	 * @param index
	 * @return the count at a certain index, for Prefix Sharing
	 */
	int getCertainCount(int parentIndex) {
		return this.prefixCounter[parentIndex-1];
	}
	
	/**
	 * Make a snapshot of the prefix counter
	 * 
	 */
	SnapShot makeSnapShot ()
	{
		//make a snapshot as <endtime, count> pair
		return new SnapShot( this.endTime, this.prefixCounter[ this.prefixNum-1 ] );
	}

}
