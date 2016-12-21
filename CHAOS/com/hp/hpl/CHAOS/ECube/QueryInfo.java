package com.hp.hpl.CHAOS.ECube;

import java.util.ArrayList;

public class QueryInfo {
	
	//boolean merged = false;
	ArrayList<String> stackTypes = new ArrayList<String>();
	int queryID = 0;
	byte donestatus = 0; // indicate whether the query has been processed for a
	// given trigger.
	// 0 means no and 1 means yes.

	int computeSourceID = -1; // the resource to compute from
	
	int window = 0;// indicate the window size of this query
	
	int virtual = 0; // indicate whether it is a virtual query;
	//1 indicates virtual query. 
	
	//indicate the subList compose this query
	ArrayList<Integer> subList = new ArrayList<Integer>();
	

	public QueryInfo() {
		super();
	}
	
	public QueryInfo(int queryID, int window, ArrayList<String> stackTypes) {
		this.queryID = queryID;
		this.stackTypes = stackTypes;
		this.window = window;
	}
	protected QueryInfo(int queryID, byte done) {
		this.queryID = queryID;
		this.donestatus = done;
	}

	public int getComputeSourceID() {
		return computeSourceID;
	}

	public int getVirtual() {
		return virtual;
	}

	public void setVirtual(int virtual) {
		this.virtual = virtual;
	}

	public void setComputeSourceID(int computeSourceID) {
		this.computeSourceID = computeSourceID;
	}

	public byte getDonestatus() {
		return donestatus;
	}

	public int getWindowSize(){
		return window;
	}
	public void setDonestatus(byte donestatus) {
		this.donestatus = donestatus;
	}

	public ArrayList<String> getStackTypes() {
		return stackTypes;
	}

	public void setStackTypes(ArrayList<String> stackTypes) {
		this.stackTypes = stackTypes;
	}

	public int getQueryID() {
		return queryID;
	}

	public void setQueryID(int queryID) {
		this.queryID = queryID;
	}
	public void setWindowSize(int window){
		this.window = window;
	}

	/**
	 * 
	 * @return number of elements in the query
	 */
	public int getSize() {
		return stackTypes.size();
	}

	
	public void setSubList(ArrayList<Integer> setSubList){
		this.subList = setSubList;
	}
	
	public ArrayList<Integer> getSubList() {
		return this.subList;
	}




}
