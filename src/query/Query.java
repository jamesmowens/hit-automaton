package query;

public interface Query {
	
	/**
	 * Runs the query condition and returns the result
	 * @return boolean signifying the result, and also represents if the result method is run
	 */
	public boolean evaluate();
	
	/**
	 * Runs the change that the query makes if the condition is met
	 * @return boolean depending if it successfully ran or not
	 */
	public boolean result();
	
	/**
	 * Access to a descriptive string of the query
	 * @return the description of the query
	 */
	public String queryInfo();
}
