
package com.hp.hpl.CHAOS.Aggregation;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;
import com.hp.hpl.CHAOS.StreamOperator.SingleInputStreamOperator;
import com.hp.hpl.CHAOS.ECube.*;


public class MultiQAggCCOperator extends SingleInputStreamOperator{
	/**
	 * This is a operator class to process the COUNT aggregation over multiple queries
	 * by applying chop-connect approach
	 * @author Yingmei Qi, Kenneth J. Loomis, Cheng Cheng
	 */

	//overload constructor
	 public MultiQAggCCOperator(int operatorID, StreamQueue[] input,
			StreamQueue[] output) {
		super(operatorID, input, output);
		
	}
	
	
	//snapshot trigger eventType set <--> subseqID list
	 EventTypeInfo SSES = new EventTypeInfo();	
	//starting eventType set<--> subseqID list
	 EventTypeInfo SES = new EventTypeInfo();
	//trigger eventType set<--> subseqID list
	 EventTypeInfo TES = new EventTypeInfo();
	//other eventType set<--> subseqID list
	EventTypeInfo OES = new EventTypeInfo();
	
	//subseq set <--> SeqMetaData list
	Map <Integer, ArrayList<SeqMetaData>> seqMetaListMap = new HashMap <Integer, ArrayList<SeqMetaData>>();

	
	ArrayList<QueryInfo> queryList = new ArrayList<QueryInfo>();
	ArrayList<SubsequenceInfo> subSeqList = new ArrayList<SubsequenceInfo>();
	//define a trigger list to store all trigger events for future TES setting up
	ArrayList<String> triggerList = new ArrayList<String>();
	
	
	// It parse the query plan. set up the stack types as well
	@Override
	public void classVariableSetup(String key, String value)
			throws InstantiationException, IllegalAccessException,
			InvocationTargetException, SecurityException,
			ClassNotFoundException {
		

		//begin to process each query
		//get first event as SES, last as TES
		if (key.equalsIgnoreCase("query")) {	
			
			// define single query specific parameters: eventTypes, queryID, window
			ArrayList<String> ETypes_AQuery = new ArrayList<String>();
			ArrayList<Integer> subList = new ArrayList<Integer>();
			int query_ID = 0;
			int qWindow = 0;
			int qLen = 0;
			
			XMLVarParser parser = new XMLVarParser(value);
			
			//get the query parameters
			//event types, query length, window sizes, query ID and subList compose this query
			ETypes_AQuery = parser.getEventTypes();
			qLen = ETypes_AQuery.size();
			qWindow = parser.getWindow();
			query_ID = parser.getID();
			subList = parser.getIDlist("subList");
			
		
			//extract the last event type as triggering event type
			String triggerType = ETypes_AQuery.get(qLen-1);
			
			//put the trigger event into triggerList
			triggerList.add(triggerType);
			
			//create this query, set the subList for this query
			QueryInfo aQuery = new QueryInfo(query_ID, qWindow, ETypes_AQuery);
			aQuery.setSubList(subList);
			
			//put this query into the queryList					
			queryList.add(aQuery);
			
		}
			
		if (key.equalsIgnoreCase("subseq")) {	
			
			//define single subsequence specific parameters:
			ArrayList<String> ETypes_ASubseq = new ArrayList<String>();
			int subseq_ID = 0;
			int subWindow = 0;
			int subLen = 0;
			ArrayList<SeqMetaData> seqMetaList = new ArrayList<SeqMetaData>();
			
			XMLVarParser parser = new XMLVarParser(value);
			
			//get the subseq parameters
			//event types, subseq length, window sizes
			ETypes_ASubseq = parser.getEventTypes();	
			subLen = ETypes_ASubseq.size();
			subWindow = parser.getWindow();
			subseq_ID = parser.getID();
			
			//set up seqMetaData from query subList
			for (int i = 0; i < queryList.size(); i++) {
				ArrayList<Integer> subList = queryList.get(i).getSubList();
				//if the this query contains current subseq
				//set up the seqMetaData
				if (subList != null && subList.contains(subseq_ID)){
					int queryID = queryList.get(i).getQueryID();
					int index;
					for (int j = 0; j < subList.size(); j++) {
						if (subList.get(j) == subseq_ID) {
							index = j;
							seqMetaList.add(new SeqMetaData(queryID, index, subList));
						}
					}
				}
			}
			seqMetaListMap.put(subseq_ID, seqMetaList);
			//extract the first event type as SES type or SSES type
			String firstType = ETypes_ASubseq.get(0);
			
			//define a event specific parameter 
			ArrayList<Integer> firstIDList =  new ArrayList<Integer>();
			
			//see whether the index of every item in the metaList is 0
			//if yes, this firstType is SES
			//if no, this firstType is SSES
			//put <eventType, metaData List> to SES or SSES		
			boolean isSES = true;
			for (SeqMetaData item: seqMetaList){
				if (!item.isFirst()){
					isSES = false;
				}			
			}	
			if(isSES){
				if (SES.isEventTypeExist(firstType)){
					firstIDList = SES.getIDList(firstType);
				}
				firstIDList.add(subseq_ID);
				SES.setIDList(firstType, firstIDList);
			}
			else {
				if (SSES.isEventTypeExist(firstType)){
					firstIDList = SSES.getIDList(firstType);
				}
				firstIDList.add(subseq_ID);
				SSES.setIDList(firstType, firstIDList);
			}	
			
			//put other event types into OES map
			for (int i = 1; i < subLen; i++) {
				String event = ETypes_ASubseq.get(i);
				ArrayList<Integer> list = new  ArrayList<Integer>();
				if (OES.isEventTypeExist(event)){
					list = OES.getIDList(event);
				}
				list.add(subseq_ID);
				OES.setIDList(event, list);
			}
			
			//extract the last event type, check whether it is the trigger type
			String lastType = ETypes_ASubseq.get(subLen-1);
			
			//see whether lastType exist in triggerList or not
			//if yes, then it is a trigger type, add it to TES
			//if no, it is not a trigger type, ignore
			if (triggerList.contains(lastType)) {
				ArrayList<Integer> lastIDList =  new ArrayList<Integer>();
		
				if (TES.isEventTypeExist(lastType)) {
					lastIDList = TES.getIDList(lastType);
				}
				lastIDList.add(subseq_ID);
				TES.setIDList(lastType, lastIDList);			
				
			}
			
			//create the SubseqPrefixInfo for this subseq
			QueryPrefixInfo subPrefixInfo = new QueryPrefixInfo(ETypes_ASubseq);
			
			//put all subseq into the subseqList					
			subSeqList.add(new SubsequenceInfo(subseq_ID, subWindow, ETypes_ASubseq,subPrefixInfo, seqMetaList));
		}
					

	}
	
	void predicatesFunction() {
		try{
			Thread.sleep((long) 0.0000000005);
		}
		catch(Exception e) {
			System.out.print("catch exception");
		}
	}

	
	@Override
	public int run(int maxDequeueSize) {
		
		//Print headers
		System.out.println("ExecTime\tTuplesPrcss\tQueryID\tResult");
		long tuplesProcessed = 0;
		
		// the schema for all input tuples is the same.
		StreamQueue inputQueue = getInputQueueArray()[0];
		SchemaElement[] schArray = inputQueue.getSchema();
		
		//store the aggregation result
		int aggResult = 0;
		
		//create a object of the MixedCounterMgrList for this workload subseqs
		MixCounterListMgr counterMgrs = new MixCounterListMgr();
		
		for (int i = maxDequeueSize; i > 0; i--) {
			
			long execution_Start = (new Date()).getTime();
			long executionTime1 = 0;
			tuplesProcessed++;
			//System.out.println("Execution Start:" + execution_Start);


			byte[] event = inputQueue.dequeue();

			if (event == null)
				break;

			for (SchemaElement sch : schArray)
				sch.setTuple(event);

			//get arrival time stamp of the incoming event
			double eTimeStamp = StreamAccessor.getDoubleCol(event, schArray, 1);
			
			//get the event type of the incoming event
			char[] type_char = StreamAccessor.getStr20Col(event, schArray, 0);
			String eventType = new String(type_char);
			eventType = eventType.trim().toUpperCase();
			
			
			//check eventType
			//if SES, create counter list
			//if SSES, create counter list, setup snapshot
			//if other event type, update corresponding counter list
			//if TES, trigger to construct result
			
			//if the current event is in SES, create new counter
			if (SES.isEventTypeExist(eventType)) {
				ArrayList<Integer> createList = SES.getIDList(eventType);
				
				//iterately create counter list for corresponding subseqs
				for (int j = 0; j < createList.size(); j++) {
					
					int subseqID = createList.get(j);
					//get this subseq parameters 
					SubsequenceInfo subseq = subSeqList.get(subseqID-1);
					int windowSize = subseq.getWindow();
					int prefixNum =  subseq.getLen() - 1;
					
					//create the prefix counter
					counterMgrs.createPrefixCounter(subseqID, eTimeStamp, windowSize, prefixNum);
				}	
				
			}
			
			//if the current event is in SSES, create new counter and create snapshot
			if (SSES.isEventTypeExist(eventType)) {
				ArrayList<Integer> createList = SSES.getIDList(eventType);
											
				//iterately create counter list for each subseq and create snapshot for each subseq
				for (int j = 0; j < createList.size(); j++) {
					
					//get the subseqID
					int subseqID = createList.get(j);
						
					//get this subseq parameters
					SubsequenceInfo subseq = subSeqList.get(subseqID-1);
					int windowSize = subseq.getWindow();
					int prefixNum =  subseq.getLen() - 1;
					ArrayList<SeqMetaData> seqMetaList = subseq.getSeqMetaList();
										
					//get the preceding Meta list for creating multiple snapshots	
					ArrayList<SeqMetaData> precedingMetaList = new ArrayList<SeqMetaData>();
					
					for(SeqMetaData metaData: seqMetaList){
						if (!metaData.isFirst()){
							precedingMetaList.add(metaData.clonePreceding());
						}				
					
					}
					//should create new mixed counter							
					counterMgrs.createMixCounter(subseqID, eTimeStamp, windowSize, prefixNum, precedingMetaList);				
					
						
				}
			}

			//if the current event is in OES,
			if(OES.isEventTypeExist(eventType)) {

				ArrayList<Integer> updateList = OES.getIDList(eventType);
				
				//iterately update the counters of each subseq
				for(int j = 0; j < updateList.size(); j++) {
					
					int subseqID = updateList.get(j);
					if(!counterMgrs.isEmpty(subseqID)) {
						//get this subseq
						SubsequenceInfo subseq = subSeqList.get(subseqID-1);
						//get the prefix index of this event type in this subsequence
						int index = subseq.prefixInfo.getIndex(eventType);
						
						//update counters
						counterMgrs.updateSeqCounters(subseqID, eTimeStamp, index);	
						if (eventType.equalsIgnoreCase("YHOO")){
							//predicatesFunction();

							//long execution_end1 = (new Date()).getTime();
							//executionTime1+= execution_end1 - execution_Start;
							//System.out.println("execution_end1" + "\t" + execution_end1);
							
						}
					}
					
				}
				
			}
			
				
			//if eventType is a trigger event type
			if(TES.isEventTypeExist(eventType)) {
				
				//trigger to construct result, find those subseqIDList	
				ArrayList<Integer> triggerList = TES.getIDList(eventType);
				
				//iteratively get the results of the counters of each subseq
				for(int j = 0; j < triggerList.size(); j++) {
					
					int subseqID = triggerList.get(j);
					if (!counterMgrs.isEmpty(subseqID)) {
						SubsequenceInfo subseq = subSeqList.get(subseqID - 1);
						ArrayList<SeqMetaData> seqMetaList = subseq.getSeqMetaList();
						
						for ( SeqMetaData resultList : seqMetaList)
						{
							if ( resultList.isLast() )
							{	
							//get results
							aggResult = counterMgrs.getResult(subseqID, eTimeStamp, resultList);
							
							
							//record the execution time
							long execution_end = (new Date()).getTime();
							//System.out.println("execution_end" + "\t" + execution_end);
							Configure.executionTime += execution_end - execution_Start;
							Configure.resultNum += aggResult;
							System.out.println(Configure.executionTime + "\t" + tuplesProcessed + "\t" + resultList.getQID() + "\t" + aggResult);
							}
						}
						
					}
					
				}
			}
			/*else
			{
				long execution_end = (new Date()).getTime();
				Configure.executionTime += execution_end - execution_Start;
				Configure.resultNum += aggResult;
				System.out.println(Configure.executionTime + "\t" + tuplesProcessed + "\t NA \t NA ");
			}*/

		}
		return 0;
	}
}


	
	


