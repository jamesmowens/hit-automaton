package com.hp.hpl.CHAOS.Rewriting;

import java.util.ArrayList;

//So far, I only support nested queries like (,SEQ,SEQ,SEQ ) and _(,OR,OR,). There two type of children queries
//can't be mixed. 
public class QueryInfo {
	ArrayList<String> stackTypes = new ArrayList<String>();
	int queryID ;
	int donestatus ; // indicate whether all children results have been passed up. 
	String operatorType = new String(); 
	
	boolean root = false; //indicate whether the query is the root or not
	
	public boolean isRoot() {
		return root;
	}

	public void setRoot(boolean root) {
		this.root = root;
	}

	int virtual = 0; // indicate whether it is a virtual query;

	
	ArrayList<Integer> clusteredQueryIDs = new ArrayList<Integer>();
	String mixType = "SEQ"; //by default is SEQ. 
	
	public String getMixType() {
		return mixType;
	}

	public void setMixType(String mixType) {
		this.mixType = mixType;
	}

	public ArrayList<Integer> getClusteredQueryIDs() {
		return clusteredQueryIDs;
	}
	
	public int getVirtual() {
		return virtual;
	}

	public void setVirtual(int virtual) {
		this.virtual = virtual;
	}


	public void setClusteredQueryIDs(ArrayList<Integer> clusteredQueryIDs) {
		this.clusteredQueryIDs = clusteredQueryIDs;
	}

	ArrayList<Integer> minusQueriesID = new ArrayList<Integer>();
	
	public ArrayList<Integer> getMinusQueriesID() {
		return minusQueriesID;
	}

	public void setMinusQueriesID(ArrayList<Integer> minusQueriesID) {
		this.minusQueriesID = minusQueriesID;
	}


	
	public String getOperatorType() {
		return operatorType;
	}

	public void setOperatorType(String operatorType) {
		this.operatorType = operatorType;
	}

	ArrayList<childQueryInfo> children = new ArrayList<childQueryInfo>();
	ArrayList<parentQueryInfo> parents = new ArrayList<parentQueryInfo>();
	
	int computeSourceID = -1; // the resource to compute from

	public ArrayList<childQueryInfo> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<childQueryInfo> children) {
		this.children = children;
	}

	public ArrayList<parentQueryInfo> getParents() {
		return parents;
	}

	public void setParents(ArrayList<parentQueryInfo> parents) {
		this.parents = parents;
	}

	public int getComputeSourceID() {
		return computeSourceID;
	}

	public void setComputeSourceID(int computeSourceID) {
		this.computeSourceID = computeSourceID;
	}

	protected QueryInfo(int queryID, ArrayList<String> stackTypes) {
		this.queryID = queryID;
		this.stackTypes = stackTypes;
	}

	protected QueryInfo(int queryID, byte done) {
		this.queryID = queryID;
		this.donestatus = done;
	}

	public int getDonestatus() {
		return donestatus;
	}

	public void setDonestatus(int donestatus) {
		this.donestatus = donestatus;
	}

	protected QueryInfo() {
		super();
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

	/**
	 * 
	 * @return number of elements in the query
	 */
	public int getSize() {
		return stackTypes.size();
	}

}
