package com.hp.hpl.CHAOS.Aggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
/**
 * 
 * @author chengcheng, Yingmei Qi
 *
 */

public class QueryPrefixInfo {
	
	//Mapping eventType  <--> index in counter;
	//for start event type, either -1 or the subseq # in prefix sharing cases
	Map<String,Integer> prefixInfo = new HashMap<String, Integer>();
	
	//a flag to show whether this subsequence is in the tree structure for prefix share or not
	boolean isPS = false;
	//the parent subseqID of current subseq, -1 means no parent
	int parentSubID = -1;
	
	//the count index of the parent subseq, for PS update
	int parentIndex = -1;
	
	/**
	 * constructor 
	 * get information of Query, put them into HashMap
	 * @param Query
	 */
	public QueryPrefixInfo(ArrayList<String> eventTypesQuery)
	{   
		int index = 0;
		
        for(int i = 0; i < eventTypesQuery.size();i++)
        {
        	String eventType = eventTypesQuery.get(i);
        	
        	//if the event type is negation 
        	if (eventType.contains("!")){
        		
        		//remove the "!" 
        		eventType = eventType.substring(1);
        		//put to prefixinfo, with value of its prefix's location in the counter
        		prefixInfo.put(eventType, index - 2);
        	}
        	else{
        		prefixInfo.put(eventType,index-1);
        		index++;
        	}
        }
	}
	
	/**
	 * constructor
	 * for prefix sharing, value of first event type is 0
	 * get information of Query, put them into HashMap
	 * @param Query
	 */
	QueryPrefixInfo(ArrayList<String> eventTypesQuery, int parentSubID, int parentIndex)
	{   
		//set the value of first event type
		this.parentSubID = parentSubID;
		this.parentIndex = parentIndex;
		//for other event types
        for(int indexOfMap=0; indexOfMap < eventTypesQuery.size();indexOfMap++)
        {
        	String eventType = eventTypesQuery.get(indexOfMap);
        	prefixInfo.put(eventType,indexOfMap);
        }
	}
	
	/**
	 * 
	 * @param queryInfo2
	 * @param eventType
	 * @return
	 */
    int getIndex(String eventType) {
    	  int index =  prefixInfo.get(eventType);
    	  return index;
	 }
    
    /**
     * get the parentSubID
     */
    int getParentSubID() {
    	return this.parentSubID;
    }
    
    int getParentIndex() {
    	return this.parentIndex;
    }
    
    void setParentSubID(int pid) {
    	this.parentSubID = pid;
    }

    /*
    public static void main(String args[])
    {   
    	QueryPrefixInfo query = new QueryPrefixInfo();
		ArrayList<String> array =  new ArrayList<String>();
		array.add("a");
		array.add("b");
		array.add("e");
		query.getMap(array);
		System.out.println(query.QueryInfo);

		System.out.println(query.getIndex(query.QueryInfo, "e"));
    }*/

}
