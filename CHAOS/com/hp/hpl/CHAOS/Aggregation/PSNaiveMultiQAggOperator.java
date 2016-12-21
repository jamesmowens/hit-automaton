
package com.hp.hpl.CHAOS.Aggregation;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;
import com.hp.hpl.CHAOS.StreamOperator.SingleInputStreamOperator;
import com.hp.hpl.CHAOS.ECube.*;


public class PSNaiveMultiQAggOperator extends SingleInputStreamOperator{
	/**
	 * This is a operator class to handle the COUNT aggregation over multiple queries
	 * without computation sharing among queries
	 * @author Yingmei Qi
	 */

	//overload constructor
	public PSNaiveMultiQAggOperator(int operatorID, StreamQueue[] input,
			StreamQueue[] output) {
		super(operatorID, input, output);
	
	}
	
	//starting eventType set<--> subseqID list
	EventTypeInfo SES = new EventTypeInfo();
	//trigger eventType set<--> subseqID list
	EventTypeInfo TES = new EventTypeInfo();
	//other eventType set<--> subseqID list
	EventTypeInfo OES = new EventTypeInfo();
	//connect eventType set(the start event type of each tree sub-node)<--> pid list
	EventTypeInfo CES = new EventTypeInfo();
	//Root eventType set(to create a whole tree for prefix Sharing)<--> sub tree sequence ID list
	EventTypeInfo RES = new EventTypeInfo();
	
		
	//store information of all queries & subsequences
	ArrayList<AggQueryInfo> queryList = new ArrayList<AggQueryInfo>();
	ArrayList<SubsequenceInfo> subSeqList = new ArrayList<SubsequenceInfo>();
	//define a trigger map to store all trigger events <--> to query ID pairs, also for future TES setting up
	Map<String,Integer> triggerMap = new HashMap<String,Integer>();
	
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
			
			//add it to query list
			queryList.add(new AggQueryInfo(query_ID, qWindow, ETypes_AQuery));
				
			//get the trigger event type, and put to TES
			String triggerType = ETypes_AQuery.get(qLen-1);
			triggerMap.put(triggerType, query_ID);
		}
		
		if (key.equalsIgnoreCase("subseq")) {
			
			//define single subsequence specific parameters:
			ArrayList<String> ETypes_ASubseq = new ArrayList<String>();
			int subseq_ID = 0;
			int subWindow = 0;
			int subLen = 0;
			int pid = 0;
			int pIndex = -1;
			
			XMLVarParser parser = new XMLVarParser(value);
			
			//get the subseq parameters
			//event types, subseq length, window sizes
			ETypes_ASubseq = parser.getEventTypes();	
			subLen = ETypes_ASubseq.size();
			subWindow = parser.getWindow();
			subseq_ID = parser.getID();
			pid = parser.getOtherID("p");
			
			//extract the first event type as SES type or SSES type
			String firstType = ETypes_ASubseq.get(0);
			
			//define a event specific parameter 
			ArrayList<Integer> firstIDList =  new ArrayList<Integer>();
			
			//if this subsequence is a shared prefix or a single query (not in the PS tree)
			if(pid == 0 || pid == -1) {
				
				//create the SubseqPrefixInfo for this subseq
				QueryPrefixInfo subPrefixInfo = new QueryPrefixInfo(ETypes_ASubseq);
				subPrefixInfo.setParentSubID(pid);
				
				//put all subseq into the subseqList					
				subSeqList.add(new SubsequenceInfo(subseq_ID, subWindow, ETypes_ASubseq, subPrefixInfo));
				
				//if this is a shared Prefix seq
				if (pid == 0) {
					//get the tree ID list
					ArrayList<Integer> treeIDList = new ArrayList<Integer>();
					treeIDList = parser.getIDlist("treeIDList");
					//put the root event type and its corresponding create ID list to RES, no duplicate event type allowed
					RES.setIDList(firstType, treeIDList);
					
				}
				//if this sequence is a separate subseq, not in a tree
				if (pid == -1) {
					if (SES.isEventTypeExist(firstType)){
						firstIDList = SES.getIDList(firstType);
					}
					firstIDList.add(subseq_ID);
					SES.setIDList(firstType, firstIDList);
				}					
			}
			//for those subseqs with pid is not 0 or -1
			else {
				pIndex = parser.getOtherID("p");
				//create the SubseqPrefixInfo for this subseq
				QueryPrefixInfo subPrefixInfo = new QueryPrefixInfo(ETypes_ASubseq,pid,pIndex);		
				//put all subseq into the subseqList					
				subSeqList.add(new SubsequenceInfo(subseq_ID, subWindow, ETypes_ASubseq, subPrefixInfo));
				
				if (CES.isEventTypeExist(firstType)){
					firstIDList = CES.getIDList(firstType);
				}
				firstIDList.add(subseq_ID);
				CES.setIDList(firstType, firstIDList);
							
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
			if (triggerMap.containsKey(lastType)) {
				ArrayList<Integer> lastIDList =  new ArrayList<Integer>();
		
				if (TES.isEventTypeExist(lastType)) {
					lastIDList = TES.getIDList(lastType);
				}
				lastIDList.add(subseq_ID);
				TES.setIDList(lastType, lastIDList);			
				
			}
		
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
			//if tree start event type (RES), create all counters for the tree
			//if starting eventType (SES), the first eventType in a query, create counter
			//if start event types in a tree (CES, not the root), update counter by pipelined result
			//if other eventType (OES),update count
			//if trigger eventType (TES), get sum, output result
			
		    //if in RES, create all counters for this tree)		
			if (RES.isEventTypeExist(eventType)) {
				ArrayList<Integer> createList = RES.getIDList(eventType);
				
				//iterately create counter list for all subseqs in the tree
				for (int j = 0; j < createList.size(); j++) {
					int subseqID = createList.get(j);
					int prefixNum;
					
					//get this subseq parameters 
					SubsequenceInfo subseq = subSeqList.get(subseqID-1);
					int windowSize = subseq.getWindow();
					
					//for the root subseq (the first in the treeIDList), as normal query
					if (j == 0) 
						 prefixNum =  subseq.getLen() - 1;
					//for other subseq in the tree
					else
						prefixNum = subseq.getLen();
					
					
					//create the prefix counter
					counterMgrs.createPrefixCounter(subseqID, eTimeStamp, windowSize, prefixNum);
				}
			}
			
			//if in SES
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
			//if in CES, update count by pipelined result
			if(CES.isEventTypeExist(eventType)) {
				ArrayList<Integer> updateList = CES.getIDList(eventType);
				
				//iterately create counter list for corresponding subseqs
				for (int j = 0; j < updateList.size(); j++) {
					
					int subseqID = updateList.get(j);
					if(!counterMgrs.isEmpty(subseqID)) {
						//get this query
						SubsequenceInfo subseq = subSeqList.get(subseqID-1);
						
						//get the parent ID and parent Index for update
						int pid = subseq.getPrefixInfo().getParentSubID();
						int parentIndex = subseq.getPrefixInfo().getParentIndex();
						
						//update counters
						counterMgrs.updatePSCounters(subseqID, eTimeStamp, pid, parentIndex);		
						
					}	
					
				}
			}
					
			//other event type
			//if the current event is in OES,
			if(OES.isEventTypeExist(eventType)) {
				ArrayList<Integer> updateList = OES.getIDList(eventType);
				
				//iterately update the counters of each query
				for(int j = 0; j < updateList.size(); j++) {
					
					int subseqID = updateList.get(j);
					if(!counterMgrs.isEmpty(subseqID)) {
						//get this subseq
						SubsequenceInfo subseq = subSeqList.get(subseqID-1);
						
						//get the prefix index of this event type in this subseq	
						int index = subseq.prefixInfo.getIndex(eventType);

						
						//update counters
						counterMgrs.updateSeqCounters(subseqID, eTimeStamp, index);	
						
					/*	if (eventType.equalsIgnoreCase("AMAT")) {
							counterMgrs.expireTreeCounters(eTimeStamp, RES.getIDList("DELL"));
						}*/
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
						SubsequenceInfo subseq = subSeqList.get(subseqID-1);
						
						//get results, if in the PS tree, only get result but not expire counters
						if (subseq.getPrefixInfo().getParentSubID() != -1) {
							aggResult = counterMgrs.getOnlyResult(subseqID, eTimeStamp);
						}
						//if not int the PS tree, get result and expire counters.
						else{
							aggResult = counterMgrs.getResult(subseqID, eTimeStamp);
						}
						
						int triggerQueryID = triggerMap.get(eventType);
						
						//record the execution time
						long execution_end = (new Date()).getTime();
						Configure.executionTime += execution_end - execution_Start;
						Configure.resultNum += aggResult;
						System.out.println(Configure.executionTime + "\t" + tuplesProcessed + "\t" + triggerQueryID + "\t" + aggResult);
					}
					
				}
				
				
			}
		/*	else
			{
				long execution_end = (new Date()).getTime();
				Configure.executionTime += execution_end - execution_Start;
				Configure.resultNum += aggResult;
				System.out.println(Configure.executionTime + "\t\t" + tuplesProcessed + "\t\t NA \t\t NA ");
			}*/
			
}
			
	
		
		return 0;
	}
	
	

}
