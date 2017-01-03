package query;

public class Event {
	
	private Context context;
	private int numberOfEvaluated;
	private int sucsesses;

	public Event(Context context)
	{
		this.context = context;
		this.numberOfEvaluated = 0;
		this.sucsesses = 0;
	}
	
	public int fetchPasses(){
		return this.sucsesses;
	}
	
	public int fetchFails(){
		return this.numberOfEvaluated - this.sucsesses;
	}
	
	public boolean evaluate(){
		boolean rv = context.evaluate();
		if(rv)
			sucsesses++;
		numberOfEvaluated++;
		return rv;
	}
}
