package com.hp.hpl.CHAOS.Rewriting;

import java.util.ArrayList;

/**
 * It is used for sequence construction.
 * 
 * @author liumo
 * 
 */
public class PreviousTuples {
	byte[] previousTuple;
	double stopTimestamp = 0;
	byte[] stopTuple; 
	
	ArrayList<Integer> fitQueriesID = new ArrayList<Integer>();

	public PreviousTuples() {
		super();
	}

	public ArrayList<Integer> getFitQueriesID() {
		return fitQueriesID;
	}

	public void setFitQueriesID(ArrayList<Integer> fitQueriesID) {
		this.fitQueriesID = fitQueriesID;
	}

	public byte[] getStopTuple() {
		return stopTuple;
	}

	public void setStopTuple(byte[] StopTuple) {
		this.stopTuple = StopTuple;
	}
	
	public PreviousTuples(byte[] previousTuple, double stopTimestamp) {
		super();
		this.previousTuple = previousTuple; 
		this.stopTimestamp = stopTimestamp;
		
	}

	public PreviousTuples(byte[] previousTuple, double stopTimestamp, byte[] StopTuple) {
		super();
		this.previousTuple = previousTuple; 
		this.stopTimestamp = stopTimestamp;
		this.stopTuple = StopTuple; 
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
