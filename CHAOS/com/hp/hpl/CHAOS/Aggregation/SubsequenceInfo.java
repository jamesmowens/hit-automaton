package com.hp.hpl.CHAOS.Aggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SubsequenceInfo {
	/**
	 * This class is used to manage the information of a subsequence
	 * @author Yingmei Qi
	 */
	
	//event types in this subseq
	ArrayList<String> eventTypes = new ArrayList<String>();
	
	//ID of this subseq
	int subseqID;
	
	//window size
	int window = 0;
	
	//store the queries this subsequence belongs to 
	//----- for only ID solution
	ArrayList<Integer> queryIDList = new ArrayList<Integer>();
	
	//store the SSIDList which should to make snapshot for or connect to
	//e.g. 1.AB 2.CD 3.EF  
	//SSIDList for 2 is 1, for 3 is 2
	//----- for only ID solution
	ArrayList<Integer> SSIDList = new ArrayList<Integer>();
	
	//store the seqMetaDataList for each subseq
	//----- for triple metadata solution
	ArrayList<SeqMetaData> seqMetaList = new ArrayList<SeqMetaData>();
	
	//SubSeqPrefixInfo for update counter
	QueryPrefixInfo prefixInfo = new QueryPrefixInfo(eventTypes);
	
	

	
	//constructor
	SubsequenceInfo(int subseqID, int window, ArrayList<String> eventTypes,
			ArrayList<Integer> queryIDList, ArrayList<Integer> SSIDList, QueryPrefixInfo qpi) {
		this.subseqID = subseqID;
		this.window = window;
		this.eventTypes = eventTypes;
		this.queryIDList = queryIDList;
		this.SSIDList = SSIDList;
		this.prefixInfo = qpi;
	}
	
	SubsequenceInfo(int subseqID, int window, ArrayList<String> eventTypes, QueryPrefixInfo qpi) {
		this.subseqID = subseqID;
		this.window = window;
		this.eventTypes = eventTypes;
		this.prefixInfo = qpi;
	}
	
	SubsequenceInfo(int subseqID, int window, ArrayList<String> eventTypes, QueryPrefixInfo qpi, ArrayList<SeqMetaData> seqMetaList) {
		this.subseqID = subseqID;
		this.window = window;
		this.eventTypes = eventTypes;
		this.prefixInfo = qpi;
		this.seqMetaList = seqMetaList;
	}
	
	//get methods
	ArrayList getSSIDList() {
		return this.SSIDList;
	}
	
	ArrayList getQueryIDList() {
		return this.queryIDList;
	}
	
	int getSubSeqID() {
		return this.subseqID;
	}
	
	int getWindow() {
		return this.window;
	}
	
	ArrayList getEventTypes() {
		return this.eventTypes;
	}
	
	int getLen() {
		int len = this.eventTypes.size();
		return len;
	}
	
	QueryPrefixInfo getPrefixInfo() {
		return this.prefixInfo;
	}
	
	ArrayList<SeqMetaData> getSeqMetaList() {
		return this.seqMetaList;
	}

}
