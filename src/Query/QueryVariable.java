/**
 * Nicholas Fajardo
 * nafajardo@wpi.edu
 * The following signature indicates that the author above pertains all rights to any ideas implemented in the code below.
 * Signature: Nicholas Fajardo
 * Modified: MaryAnn VanValkenburg (mevanvalkenburg@wpi.edu) 02/06/2017, implemented method stubs
 */

package Query;

public class QueryVariable extends Query {
	
	//This is the string that has the set value and name of the variable
	private String set;

	public QueryVariable(Condition expression, String pertainingState, String queryName, String setValue) {
		super(expression, pertainingState, queryName);
		this.set = setValue;
	}

	/* (non-Javadoc)
	 * @see Query.Query#run()
	 */
	@Override
	public void run(int parameter){
		String expression = ex.getExpression();
		// An expression has an existing variable, a comparison operator, and a parameter to compare
		// TODO parameter to compare is currently stored as "?" in expression string
		String evaluable = expression.replace("?", ""+parameter);
		// search expression, input parameter, evaluate new expression
		boolean result = this.ex.evaluate(evaluable);
		if(result){
			execute(set);
		}
	}
	
	private void execute(String set){
		//Parse string, search for variable name and assignment operator
	}
}
