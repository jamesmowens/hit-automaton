package query;

import java.util.HashSet;

/**
 * This Query is the class that is used for query definitions
 * It includes the use of the Context class in the query package
 * This calss also contains statistics for a given context, such as the number of times the context was evaluated, or the success rates of the context
 * @author Nicholas Fajardo
 */
public class Query {
	
	/**
	 * These private instance variables are used to keep track of statistics and the context to evaluate.
	 */
	private Context context;
	private int numberOfEvaluated;
	private int sucsesses;

	/**
	 * This class is basically a context with data.
	 * @param context
	 */
	public Query(Context context)
	{
		this.context = context;
		this.numberOfEvaluated = 0;
		this.sucsesses = 0;
	}
	
	/**
	 * Returns the context of the query
	 * @return The context of the query
	 */
	public Context fetchContext()
	{
		return this.context;
	}
	
	/**
	 * Returns the successes of the query so far
	 * @return Returns the successes of the query so far
	 */
	public int fetchPasses(){
		return this.sucsesses;
	}
	
	/**
	 * Returns the failures of the query so far
	 * @return Returns the failures of the query so far
	 */
	public int fetchFails(){
		return this.numberOfEvaluated - this.sucsesses;
	}
	
	/**
	 * This runs an evaluation of the query, in which the user is expected to update the query variable to pass in beforehand
	 * This should also update the statistics inside the class
	 * @param parameter (integer), this is for the context, documentation can be found there for the specifics.
	 * @return the result of the context evaluation
	 */
	public boolean evaluateContext(int param){
		boolean rv = context.evaluate(param);
		if(rv)
			sucsesses++;
		numberOfEvaluated++;
		return rv;
	}
}