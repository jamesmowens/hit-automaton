package com.hp.hpl.CHAOS.ECube;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;
import com.sun.tools.javac.util.List;

public class MergedQueryInfo extends QueryInfo{
		
	//store the information of all the sub queries: <windowSize,subQueryID>
	Map<Integer,Integer> subQueryList = new HashMap<Integer, Integer>();
	
	boolean merged = false;
	
	int virtual = 0; // indicate whether it is a virtual query;
	//1 indicates virtual query. 

	//Constructor 
	protected MergedQueryInfo() {
		super();
	}

	protected MergedQueryInfo(int queryID, byte done) {
		this.queryID = queryID;
		this.donestatus = done;
	}
	
	protected MergedQueryInfo(int queryID, int window, ArrayList<String> stackTypes) {
		this.queryID = queryID;
		this.stackTypes = stackTypes;
		this.window = window;
	}

	protected MergedQueryInfo(int queryID, HashMap subQueryList, ArrayList<String> stackTypes) {
		this.queryID = queryID;
		this.stackTypes = stackTypes;
		this.subQueryList = subQueryList;
	}
	
	//get values
	
	public int getQueryID() {
		return queryID;
	}
	
	public int getComputeSourceID() {
		return computeSourceID;
	}

	public int getVirtual() {
		return virtual;
	}
	
	public byte getDonestatus() {
		return donestatus;
	}
	
	public ArrayList<String> getStackTypes() {
		return stackTypes;
	}
	
	public int getWindowSize(){
		return window;
	}

	public double getMaxWindowSize(HashMap<Integer, Integer> subQueryList){

	    double maxWin = 0;
	    
	    Iterator iter = subQueryList.entrySet().iterator();
	    while(iter.hasNext()){
	    	Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>)iter.next();
            double window = (double)entry.getValue();
            if(window > maxWin){
            	maxWin = window;
            }
	    }
	    
		return maxWin;
	}
	public int getSubWindowSize(int subQueryID){
		return subQueryList.get(subQueryID);
	}
	public Map<Integer, Integer> getSubQueryList (HashMap subueryList){
		return subQueryList;
	}

	// set values
	public void setQueryID(int queryID) {
		this.queryID = queryID;
	}
	
	public void setVirtual(int virtual) {
		this.virtual = virtual;
	}

	public void setComputeSourceID(int computeSourceID) {
		this.computeSourceID = computeSourceID;
	}

	public void setDonestatus(byte donestatus) {
		this.donestatus = donestatus;
	}

	public void setStackTypes(ArrayList<String> stackTypes) {
		this.stackTypes = stackTypes;
	}

	public void setWindowSize(int window){
		this.window = window;
	}
	
	public void setWindowSizes(HashMap<Integer,Integer> subQueryList){
		this.subQueryList = subQueryList;
	}
	/**
	 * 
	 * @return number of elements in the query
	 */
	public int getSize() {
		return stackTypes.size();
	}
	/**
	 * 
	 * @return number of sub queries of this merged query
	 */
	public int getQueriesNum(){
		return subQueryList.size();
	}
}
