
package Query;

import connection.Step;

public class TransitionQuery extends Query {

	Step successStep;  
	transient Step currentStep;

	/**
	 * 
	 * @param expression The expression to evaluate whether or not to transition
	 * @param pertainingState The state that owns this query
	 * @param queryName The name of the query
	 * @param successStep The step to perform should the expression evaluate to true
	 */
	public TransitionQuery(Condition expression, String pertainingState, String queryName, Step successStep) {
		super(expression, pertainingState, queryName);
		// TODO Auto-generated constructor 
		this.successStep = successStep;
	}

	/**
	 * Explicit constructor - each field specified specifically
	 */
	public TransitionQuery(String state, String info, Condition ex, Step successStep) {
		super(state, info, ex);
		this.successStep = successStep;
	}

	/**
	 * Create an empty TransitionQuery
	 */
	public TransitionQuery() {
		super();
	}

	@Override
	public void run() {
		System.out.println("Transition Query being run");
		// TODO Auto-generated method stub
		String expression = ex.getExpression();
		// An expression has an existing variable, a comparison operator, and a parameter to compare
		// TODO parameter to compare is currently stored as "?" in expression string
		// search expression, input parameter, evaluate new expression
		boolean result = this.ex.evaluate(expression);
		if (result) {
			currentStep =  successStep;
		} else {
			currentStep = new Step(successStep.getSource(),successStep.getSource(),"Didn't Move");;
		}
		
		
	}
	
	
	/*
	 * Returns the step set by the last run of the Query
	 */
	public Step getStep() {
		return currentStep; 
	}
	
	public String queryPattern(){return pattern;}
	
	public void setPattern(String pat){
		this.pattern = pat;
	}

}
