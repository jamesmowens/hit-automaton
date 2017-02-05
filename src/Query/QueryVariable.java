/**
 * Nicholas Fajardo
 * nafajardo@wpi.edu
 * The following signature indicates that the author above pertains all rights to any ideas implemented in the code below.
 * Signature: Nicholas Fajardo
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
		boolean result = this.ex.evaluate(parameter);
		if(result){
			
		}
		else{
			
		}
	}
	
	private void execute(){
		//TODO 
	}
}
