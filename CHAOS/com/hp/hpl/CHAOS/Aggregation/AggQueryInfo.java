package com.hp.hpl.CHAOS.Aggregation;
import java.util.ArrayList;

import com.hp.hpl.CHAOS.ECube.*;

public class AggQueryInfo extends QueryInfo{
	
	//eventTypes in this query
	ArrayList<String> ETypes_AQuery = new ArrayList<String>();
	
	//indicate the subList compose this query
	ArrayList<Integer> subList = new ArrayList<Integer>();
	
	//queryPrefixInfo for update counter
	QueryPrefixInfo prefixInfo = new QueryPrefixInfo(ETypes_AQuery);
	
	//constructor
	
	
	public AggQueryInfo(int query_ID, int qWindow, ArrayList<String> ETypes_AQuery){
		super(query_ID, qWindow, ETypes_AQuery);
		
	}
	
	public AggQueryInfo(int query_ID, int qWindow, ArrayList<String> ETypes_AQuery, QueryPrefixInfo prefixInfo) {
		super(query_ID, qWindow, ETypes_AQuery);
		this.prefixInfo = prefixInfo;
	}
	
	
	//get methods	
	public QueryPrefixInfo getPrefixInfo() {
		return this.prefixInfo;
	}
	
	public ArrayList<Integer> getSubList() {
		return this.subList;
	}
	
	//get methods
	public void setSubList(ArrayList<Integer> subList){
		this.subList = subList;
	}

}
