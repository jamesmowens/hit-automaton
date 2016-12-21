package com.hp.hpl.CHAOS.ECube;
/**
 * It is used for sequence construction. 
 * 
 * @author liumo
 *
 */
public class PreviousTuples {
	byte[] previousTuple;
	double stopTimestamp = 0;

	public PreviousTuples() {
		super();
	}

	public PreviousTuples(byte[] previousTuple, double stopTimestamp) {
		super();
		this.previousTuple = previousTuple;
		this.stopTimestamp = stopTimestamp;
	}

	public byte[] getPreviousTuple() {
		return previousTuple;
	}

	public void setPreviousTuple(byte[] previousTuple) {
		this.previousTuple = previousTuple;
	}

	public double getStopTimestamp() {
		return stopTimestamp;
	}

	public void setStopTimestamp(double stopTimestamp) {
		this.stopTimestamp = stopTimestamp;
	}

}
