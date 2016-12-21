
package com.hp.hpl.CHAOS.Aggregation;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;

import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;
import com.hp.hpl.CHAOS.StreamOperator.SingleInputStreamOperator;
import com.hp.hpl.CHAOS.ECube.*;


public class NaiveMultiQAggOperator extends SingleInputStreamOperator{
	/**
	 * This is a operator class to handle the COUNT aggregation over multiple queries
	 * without computation sharing among queries
	 * @author Yingmei Qi
	 */

	//overload constructor
	public NaiveMultiQAggOperator(int operatorID, StreamQueue[] input,
			StreamQueue[] output) {
		super(operatorID, input, output);
	
	}
	
	//starting eventType set<--> SeqMetaData list
	EventTypeInfo SES = new EventTypeInfo();
	//trigger eventType set<--> SeqMetaData list
	EventTypeInfo TES = new EventTypeInfo();
	//other eventType set<--> SeqMetaData list
	EventTypeInfo OES = new EventTypeInfo();
		
	//store information of all queries
	ArrayList<AggQueryInfo> queryList = new ArrayList<AggQueryInfo>();
	
	// It parse the query plan. set up the stack types as well
	@Override
	public void classVariableSetup(String key, String value)
			throws InstantiationException, IllegalAccessException,
			InvocationTargetException, SecurityException,
			ClassNotFoundException {
		

		if (key.equalsIgnoreCase("query")) {	
			
			// define single query specific parameters: eventTypes, queryID, window
			ArrayList<String> ETypes_AQuery = new ArrayList<String>();
			int query_ID = 0;
			int qWindow = 0;
			int qLen = 0;

			//new a parser to get query specific parameters
			XMLVarParser parser = new XMLVarParser(value);
			
			//get the query parameters
			//event types, query length, window sizes, query ID 
			ETypes_AQuery = parser.getEventTypes();
			qLen = ETypes_AQuery.size();
			qWindow = parser.getWindow();
			query_ID = parser.getID();
			
			//create the SubseqPrefixInfo for this query
			QueryPrefixInfo queryPrefixInfo = new QueryPrefixInfo(ETypes_AQuery);
			
			//add it to query list
			queryList.add(new AggQueryInfo(query_ID, qWindow, ETypes_AQuery, queryPrefixInfo));
			
			
			//get the start event type of current query
			String firstType = ETypes_AQuery.get(0);		
			//create a ID list to put all queryIDs that this start event will create for
			ArrayList<Integer> firstIDList =  new ArrayList<Integer>();			
			//add it to SES, with its queryID list
			if (SES.isEventTypeExist(firstType)){
				firstIDList = SES.getIDList(firstType);
			}
			firstIDList.add(query_ID);
			SES.setIDList(firstType, firstIDList);
			
			//put other event types into OES map
			for (int i = 1; i < qLen; i++) {
				String event = ETypes_AQuery.get(i);
				ArrayList<Integer> list = new  ArrayList<Integer>();
				if (OES.isEventTypeExist(event)){
					list = OES.getIDList(event);
				}
				list.add(query_ID);
				OES.setIDList(event, list);
			}
			
			
			//get the trigger event type, and put to TES
			String lastType = ETypes_AQuery.get(qLen-1);
			ArrayList<Integer> lastIDList = new ArrayList<Integer>();

			if (TES.isEventTypeExist(lastType)) {
				lastIDList = TES.getIDList(lastType);
			}
			lastIDList.add(query_ID);
			TES.setIDList(lastType, lastIDList);			
			
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
		System.out.println("ExecTime\tMemoryUsed\tTuplesPrcss\tQueryID\tResult");
		long tuplesProcessed = 0;
		
		// the schema for all input tuples is the same.
		StreamQueue inputQueue = getInputQueueArray()[0];
		SchemaElement[] schArray = inputQueue.getSchema();
		
		//store the aggregation result
		int aggResult = 0;
		
		//store counter list for all queries. 
		PrefixCounterListMgr counterMgrs = new PrefixCounterListMgr();

		for (int i = maxDequeueSize; i > 0; i--) {
			
			long execution_Start = (new Date()).getTime();
			//System.out.println("Execution Start:" + execution_Start);
			tuplesProcessed++;

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
			eventType = eventType.trim();
			
			
			//check eventType
			//if starting eventType, the first eventType in a query, create counter
			//if other eventType,update count
			//if trigger eventType, get sum, output result
			
			//starting event type
			if (SES.isEventTypeExist(eventType)) {
				ArrayList<Integer> createList = SES.getIDList(eventType);
				
				//iterately create counter list for corresponding subseqs
				for (int j = 0; j < createList.size(); j++) {
					
					int subseqID = createList.get(j);
					//get this subseq parameters 
					AggQueryInfo query = queryList.get(subseqID-1);
					int windowSize = query.getWindowSize();
					int prefixNum =  query.getSize() - 1;
					
					//create the prefix counter
					counterMgrs.createPrefixCounter(subseqID, eTimeStamp, windowSize, prefixNum);
				}	
				
			}
					
			//other event type
			//if the current event is in OES,
			if(OES.isEventTypeExist(eventType)) {
				ArrayList<Integer> updateList = OES.getIDList(eventType);
				
				//iterately update the counters of each query
				for(int j = 0; j < updateList.size(); j++) {
					
					int queryID = updateList.get(j);
					if(!counterMgrs.isEmpty(queryID)) {
						//get this query
						AggQueryInfo query = queryList.get(queryID-1);
						
						//get the prefix index of this event type in this query
						int index = query.prefixInfo.getIndex(eventType);

						
						//update counters
						counterMgrs.updateSeqCounters(queryID, eTimeStamp, index);	
						if (eventType.equalsIgnoreCase("YHOO")){
							//predicatesFunction();
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
					
					int queryID = triggerList.get(j);
					if (!counterMgrs.isEmpty(queryID)) {
						AggQueryInfo subseq = queryList.get(queryID - 1);
						
						//get results
						aggResult = counterMgrs.getResult(queryID, eTimeStamp);
						
						//record the execution time
						long execution_end = (new Date()).getTime();
						Configure.executionTime += execution_end - execution_Start;
						Configure.resultNum += aggResult;
						System.out.println(Configure.executionTime + "\t" + tuplesProcessed + "\t" + queryID + "\t" + aggResult);
					}
					
				}
				
				
			}
		/*	else
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
