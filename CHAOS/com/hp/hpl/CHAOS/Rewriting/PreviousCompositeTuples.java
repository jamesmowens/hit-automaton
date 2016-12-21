package com.hp.hpl.CHAOS.Rewriting;

import java.util.ArrayList;

/**
 * It is used for sequence construction.
 * 
 * @author liumo
 * 
 */
public class PreviousCompositeTuples {
	ArrayList<byte[]> previousTuple;
	

	public PreviousCompositeTuples() {
		super();
	}

	public PreviousCompositeTuples(ArrayList<byte[]> previousTuple,
			double stopTimestamp) {
		super();
		this.previousTuple = previousTuple;
		
	}

	public ArrayList<byte[]> getPreviousTuple() {
		return previousTuple;
	}

	public void setPreviousTuple(ArrayList<byte[]> previousTuple) {
		this.previousTuple = previousTuple;
	}

	
	public int getTuplSize() {
		return previousTuple.size();
	}

	public byte[] getIthTuple(int i) {
		return previousTuple.get(i);
	}
}
