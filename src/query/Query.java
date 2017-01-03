package query;

import java.util.HashSet;

//This is the interface I will use to define some of the method headers.
public class Query {
	
	private HashSet<Event> events;
	
	public Query(){}
	
	/**
	 * This method just adds an event to the event list in this class
	 * @param event
	 */
	public void addEvent(Context context)
	{
		events.add(new Event(context));
	}
	
	//public void printData
}
