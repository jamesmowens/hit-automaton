package query;

//I am adding this so we can compare the context at hand.
public interface Context extends Comparable<Context> {

	/**
	 * Whatever the Context might be, it must have an evaluating factor that decides if it was fulfilled or not
	 * This should return a boolean or not depending on the result of the evaluation
	 * The defined context should use helper classes to access and fully evaluate the context at hand.
	 * @param parameter (integer). We can use this as a price, a binary true or false, or anything else 
	 * @return the result of the evaluation.
	 */
	public boolean evaluate(int parameter);
	
	/**
	 * This just returns the description of the context at hand	
	 * @return some string that describes the context
	 */
	public String description();
	
}