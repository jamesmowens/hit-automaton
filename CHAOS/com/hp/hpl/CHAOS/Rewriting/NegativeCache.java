/**
 * 
 */
package com.hp.hpl.CHAOS.Rewriting;

/**
 * @author medhabi
 *
 */
public class NegativeCache {

	/**
	 * This class will be used for negative sub query caching 
	 * where the actual results will not be caches but only the intervals and 
	 * an Empty or Non-Empty flag will be maintained 
	 */
	
	double lefttime = 0;
	public double getLefttime() {
		return lefttime;
	}



	public void setLefttime(double lefttime) {
		this.lefttime = lefttime;
	}



	public double getRighttime() {
		return righttime;
	}



	public void setRighttime(double righttime) {
		this.righttime = righttime;
	}



	public boolean isEmptyFlag() {
		return emptyFlag;
	}



	public void setEmptyFlag(boolean emptyNonEmptyFlag) {
		this.emptyFlag = emptyNonEmptyFlag;
	}



	double righttime = 0;
	
	boolean emptyFlag;
	
	
	
	public NegativeCache(double leftTimebound, double rightTimebound, boolean b) {
		// TODO Auto-generated constructor stub
		this.lefttime = leftTimebound;
		this.righttime = rightTimebound;
		this.emptyFlag = b;
	}

}
