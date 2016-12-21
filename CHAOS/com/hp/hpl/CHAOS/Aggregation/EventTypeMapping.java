package com.hp.hpl.CHAOS.Aggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EventTypeMapping {
	
	/**
	 * This class is used to define the mapping structure from event type to SeqMetaData List
	 * @author yingmeiqi
	 */
	
Map<String, ArrayList<SeqMetaData>> event2Meta = new HashMap<String, ArrayList<SeqMetaData>>();
	
	
	public boolean isEventTypeExist(String eventType) {
		if (event2Meta.containsKey(eventType)) {
			return true;
		}
		else
			return false;
	}

	public void setIDList(String eventType, ArrayList<SeqMetaData> MetaList) {
		event2Meta.put(eventType, MetaList);
		
	}

	public ArrayList getIDList(String eventType) {
		return event2Meta.get(eventType);
	}


}
