package com.hp.hpl.CHAOS.Aggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EventTypeInfo {
	/**
	 * This class is used to set the Mapping structure for each event type
	 * <eventTye, IDList>
	 * @author Yingmei Qi
	 */
	
	//mappings from event types to the queryID/subseqID list
	Map<String, ArrayList<Integer>> event2ID = new HashMap<String, ArrayList<Integer>>();
	
	
	public boolean isEventTypeExist(String eventType) {
		if (event2ID.containsKey(eventType)) {
			return true;
		}
		else
			return false;
	}

	public void setIDList(String eventType, ArrayList<Integer> IDList) {
		event2ID.put(eventType, IDList);
		
	}

	public ArrayList<Integer> getIDList(String eventType) {
		return event2ID.get(eventType);
	}


}
