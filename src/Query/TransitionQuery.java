package Query;

import connection.Step;

public class TransitionQuery extends Query {
	
	Step successStep;  
	Step currentStep; 

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

	@Override
	public void run(int parameter) {
		// TODO Auto-generated method stub
		String expression = ex.getExpression();
		// An expression has an existing variable, a comparison operator, and a parameter to compare
		// TODO parameter to compare is currently stored as "?" in expression string
		String evaluable = expression.replace("?", ""+parameter);
		// search expression, input parameter, evaluate new expression
		boolean result = this.ex.evaluate(evaluable);
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

}