package Query;

import connection.Step;

public class TransitionQuery extends Query {
	
	Step myStep;  

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
		myStep = successStep;
		

	}

	@Override
	public void run(int parameter) {
		// TODO Auto-generated method stub
		
		
	}
	
	public Step getResultingStep() {
		if(expression.evaluate()){
			return myStep;
		} else {
			return new Step(myStep.getSource(),myStep.getSource(),"Didn't Move"); 
		}
		
	}

}
