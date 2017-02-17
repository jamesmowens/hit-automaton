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
	public void run(){
		String expression = ex.getExpression();
		
		boolean result = this.ex.evaluate(expression);
		if(result){
			execute(set);
		} //TODO else, execute different set?
	}
	
	private void execute(String set){
		System.out.println("Set is being run");
		
		//Parse string, search for variable name and assignment operator
		
		//Update variables.txt
	}
	
	
	public static void main(String args[]) {
	
		Query test1 = new QueryVariable(new Condition("x > 4"), "B1", "X greater than", "x = 6");
		test1.run();
	}
	
	
	
}
