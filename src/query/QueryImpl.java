package query;

public class QueryImpl implements Query{
	
	private String frequency = new String();
	private String condition = new String();
	private String set = new String();
	
	/**
	 * Default frequency set by this constructor is once
	 * @param condVal
	 * @param setVal
	 */
	public QueryImpl(String condVal, String setVal){
		this.condition = condVal;
		this.set = setVal;
		this.frequency = "once";
	}
	
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public String getFrequency() {
		return this.frequency;
	}
	
	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	public String getCondition() {
		return this.condition;
	}
	
	public void setSet(String set) {
		this.set = set;
	}
	
	public String getSet() {
		return this.set;
	}
	
	/**
     * Runs the query condition and returns the result
     * @return boolean signifying the result, and also represents if the result method is run
     */
    //@Override
    public boolean evaluate() throws Exception{ //TODO
        if (Evaluator.evaluateLine(condition)) {
            result();
            return true;
        }
            return false;
    }

    /** //TODO do we really need this??
     * Runs the change that the query makes if the condition is met
     * @return boolean depending if it successfully ran or not
     */
   // @Override
    public boolean result() throws Exception {
        return Evaluator.evaluateLine(set);

    }

    /**
     * Access to a descriptive string of the query
     * @return the description of the query
     */
    //@Override
    public String queryInfo() {
        return new String("Frequency: "+frequency+", Condition: "+condition+", Set: "+set);
    } 
    
    /*
    public static void main(String args[]) throws Exception{
        QueryImpl practice = new QueryImpl("x > 0","classes = classes + 1");

        practice.evaluate();
    }
    */
    
    
    
}
