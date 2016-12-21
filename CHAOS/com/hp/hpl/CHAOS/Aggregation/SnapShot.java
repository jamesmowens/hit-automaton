package com.hp.hpl.CHAOS.Aggregation;

/**
 * This class represents an individual snapshot component
 * @author Kenneth J. Loomis, 2012
 *
 */

public class SnapShot {
	
	private double ts;
	private int count;
	
    /**
     * Constructor
	 * compute the ending timestamp for slide by tuple window 
     * @param arrivalTime
     * @param windowSize
     */
	public SnapShot(double time, int count) {
		this.ts = time;
		this.count = count;
	}
	
	/** 
	 * Get time
	 * @param time 
	 */
	double getTime ( )
	{
		return this.ts;
	}
	
	/** 
	 * Get count if time is valid
	 * @param time 
	 */
	int getCount ( )
	{
		return count;
	}
	
	/**
	 * Check if the snapshot is valid
	 * @param time
	 */
	Boolean isValid ( double time )
	{
		return ( this.ts > time );
	}

}
