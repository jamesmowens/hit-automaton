package com.hp.hpl.CHAOS.Rewriting;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * query plan for normalized queries
 * 
 * @author liumo
 * 
 */
public class QueryPlan {
	
	//what is query plan name: its component queries. 
	//why we need query plan name,

	//how the query plan can be expanded? 
	//by adding new entry or adding new queries to existing entry. 
	
	//Actually there should be several group in each category. Group can be identified by shared expression. 
	
	// using shared for queries with the same positive but different negative
	// types; shared expression is the same positive types. 
	ArrayList<String> planName = new ArrayList<String>(); 
	
	

	Hashtable<ArrayList<String>, ArrayList<QueryInfo>> clusteredQueryTable = new Hashtable<ArrayList<String>, ArrayList<QueryInfo>>();
	
	
	// using topdown/naive; shared expression is the common prefix/suffix
	Hashtable<ArrayList<String>, ArrayList<QueryInfo>> unclusteredQueryTable = new Hashtable<ArrayList<String>, ArrayList<QueryInfo>>();
	public QueryPlan(
			ArrayList<String> planName,
			Hashtable<ArrayList<String>, ArrayList<QueryInfo>> clusteredQueryTable,
			Hashtable<ArrayList<String>, ArrayList<QueryInfo>> unclusteredQueryTable) {
		super();
		this.planName = planName;
		this.clusteredQueryTable = clusteredQueryTable;
		this.unclusteredQueryTable = unclusteredQueryTable;
	}

	public QueryPlan(ArrayList<String> planName) {
		super();
		this.planName = planName;
	}

	public ArrayList<String> getPlanName() {
		return planName;
	}
	public void setPlanName(ArrayList<String> planName) {
		this.planName = planName;
	}
	public Hashtable<ArrayList<String>, ArrayList<QueryInfo>> getClusteredQueryTable() {
		return clusteredQueryTable;
	}
	public void setClusteredQueryTable(
			Hashtable<ArrayList<String>, ArrayList<QueryInfo>> clusteredQueryTable) {
		this.clusteredQueryTable = clusteredQueryTable;
	}
	public Hashtable<ArrayList<String>, ArrayList<QueryInfo>> getUnclusteredQueryTable() {
		return unclusteredQueryTable;
	}
	public void setUnclusteredQueryTable(
			Hashtable<ArrayList<String>, ArrayList<QueryInfo>> unclusteredQueryTable) {
		this.unclusteredQueryTable = unclusteredQueryTable;
	}

}
