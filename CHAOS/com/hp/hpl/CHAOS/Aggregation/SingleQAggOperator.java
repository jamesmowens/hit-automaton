
package com.hp.hpl.CHAOS.Aggregation;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;

import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;
import com.hp.hpl.CHAOS.StreamOperator.SingleInputStreamOperator;
import com.hp.hpl.CHAOS.ECube.*;


public class SingleQAggOperator extends SingleInputStreamOperator{
	/**
	 * This is a operator class to handle the COUNT aggregation over 1 single SEQ query
	 * @author Yingmei Qi
	 */

	//overload constructor
	public SingleQAggOperator(int operatorID, StreamQueue[] input,
			StreamQueue[] output) {
		super(operatorID, input, output);
	
	}
	
	// define query specific parameters: eventTypes, queryID, window
	ArrayList<String> ETypes_AQuery = new ArrayList<String>();
	static int query_ID = 0;
	int window = 0;
	
	//define start event type, negation type and trigger event type
	String SET;
	String Negation;
	String TET;
	
	//define the prefix counter size of this query, for update counter use
	int prefixNum = 0;
	
	QueryInfo currQuery = new QueryInfo();
	
	// It parse the query plan. set up the stack types as well
	@Override
	public void classVariableSetup(String key, String value)
			throws InstantiationException, IllegalAccessException,
			InvocationTargetException, SecurityException,
			ClassNotFoundException {
	

		if (key.equalsIgnoreCase("query")) {	
			
			//get query specific parameters: event types, ID, and window size
			XMLVarParser parser = new XMLVarParser(value);
			
			ETypes_AQuery = parser.getEventTypes();
			
			query_ID = parser.getID();			
			window = parser.getWindow();
			currQuery = new AggQueryInfo(query_ID, window, ETypes_AQuery);
			
			//get the query length
			int lenQ = ETypes_AQuery.size();
			
			//get the start event type
			SET = ETypes_AQuery.get(0);
			//get the trigger event type
			TET = ETypes_AQuery.get(lenQ-1);		
			
			//get the prefix counter size (no negation)
			prefixNum = lenQ - 1;
			
			//get the negation event type
			for (int i = 0; i < ETypes_AQuery.size(); i++) {
				if (ETypes_AQuery.get(i).contains("!")) {
					//remove the "!"
					Negation = ETypes_AQuery.get(i).substring(1);
					//get the prefix counter size with negation
					prefixNum = lenQ - 2;
					break;
				}
			}
		}
	}

	
	@Override
	public int run(int maxDequeueSize) {
		
		//Print headers
		System.out.println("ExecTime\tTuplesPrcss\tResult");
		long tuplesProcessed = 0;
		
		// the schema for all input tuples is the same.
		StreamQueue inputQueue = getInputQueueArray()[0];
		SchemaElement[] schArray = inputQueue.getSchema();
		
		//store the aggregation result
		int aggResult = 0;
		
		//create the prefix info for this query
		QueryPrefixInfo qpi = new QueryPrefixInfo(ETypes_AQuery);
		
		//initialize the PrefixCounterManager for the query
		PrefixCounterList pcm = new PrefixCounterList(1);

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
			if (eventType.equalsIgnoreCase(SET)) {
				pcm.addPrefixCounter(eTimeStamp, window, prefixNum);
			}
			//other event type
			else {
				
				//if eventType is relevant to the query
				if (qpi.prefixInfo.containsKey(eventType)) {
					
					
					//get the prefix index of this eventType
					int index = qpi.getIndex(eventType);
					
					//if the event is negation type
					if (eventType.equalsIgnoreCase(Negation)) {
						pcm.updateNegCounters(eTimeStamp, index);
					}
					else{
						//update the corresponding count of all counter
						pcm.updateCounters(eTimeStamp,index);
					}
					
					if (eventType.equalsIgnoreCase(TET)) {
						aggResult = pcm.getSumandExpire(eTimeStamp);
						int memory = pcm.getMemoryStatistics(eTimeStamp);
						System.out.println("Memory Usage:" + "\t\t" + memory);
						
						//com.hp.hpl.CHAOS.Queue.Utility.enqueueGroup(OutputQueueArray,
								//dest);
						//long execution_end = (new Date()).getTime();
						//System.out.println("Execution End:" + execution_end);
						//Configure.executionTime += execution_end - execution_Start;
						//System.out.println("Execution Time:" + Configure.executionTime);
						long execution_end = (new Date()).getTime();
						Configure.executionTime += execution_end - execution_Start;
						
						//Configure.resultNum += aggResult;
						
						//System.out.println("The number of sequences are:" + Configure.resultNum + "\t" + Configure.executionTime);	
						/*if (tuplesProcessed == 2000 || tuplesProcessed == 4000 ||
							tuplesProcessed == 6000	|| tuplesProcessed == 8000 ||
							tuplesProcessed == 9836){*/
							//int memory = pcm.getMemoryStatistics(eTimeStamp);
							//System.out.println("Memory Usage:" + "\t\t" + memory);
							//System.out.println(Configure.executionTime + "\t\t" + tuplesProcessed + "\t\t" + "\t\t" + aggResult);
						//}
					}
					else
					{
						long execution_end = (new Date()).getTime();
						Configure.executionTime += execution_end - execution_Start;
						//Configure.resultNum += aggResult;
						/*if (tuplesProcessed == 2000 || tuplesProcessed == 4000 ||
								tuplesProcessed == 6000	|| tuplesProcessed == 8000 ||
								tuplesProcessed == 9836){*/
							//System.out.println(Configure.executionTime + "\t\t" + tuplesProcessed + "\t\t" + "\t\t" + aggResult);
							//}
					}
					
				}
				
			}
		
		}
		
		//long execution_end = (new Date()).getTime();
		//Configure.executionTime = execution_end - execution_Start;
		System.out.println(Configure.executionTime + "\t\t" + tuplesProcessed + aggResult);
		
		return 0;
	}

}
