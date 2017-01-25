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
    public boolean evaluate() {
        boolean result = false;

        String c = this.condition;

        //TODO

        if (result) {
            result();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Pulls the current value of a variable from global "variables" file
     */
    public int getVariable(String name) throws Exception{ //TODO replace with a generic

        // input the file content to the String "input"
       // BufferedReader file = new BufferedReader(new FileReader("variables.txt"));
       // String line;
       // String input = "";
        return 0;
    }
    
	/**
	 * Runs the change that the query makes if the condition is met
	 * @return boolean depending if it successfully ran or not
	 */
	@Override
	public boolean result() {
		// TODO run evaluate(), if TRUE, apply "set"
		return false;
	}

	/**
	 * Access to a descriptive string of the query
	 * @return the description of the query
	 */
	@Override
	public String queryInfo() {
		return new String("Frequency: "+frequency+", Condition: "+condition+", Set: "+set);
	}

}
