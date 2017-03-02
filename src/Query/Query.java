/**
 * Nicholas Fajardo
 * nafajardo@wpi.edu
 * The following signature indicates that the author above pertains all rights to any ideas implemented in the code below.
 * Signature: Nicholas Fajardo
 * --------------------------------------------------------------------------------------------------------------------------
 */

package Query;

public abstract class Query {

	//This is the state the query belongs to
	protected String state;
	protected String info;
	protected Condition ex;
	
	public Query(Condition expression, String pertainingState, String queryName){
		this.ex = expression;
		this.state = pertainingState;
		constructInfo(queryName);
	}
	
	//TODO add evaluate method here
	
	//This is the method that will be run when the query is asked to check stuff
	public abstract void run();
	
	//This is the method that will be run when the query is asked for what it does and other information
	public String queryInfo(){return "";}
	
	private void constructInfo(String name){
		info = "Name of query: " + name + "\n belongs to this State: " + this.state + ".\nFunction of this query: " + ex.toString() + ".";
	}
	
	public void printinfo(){
		if(!info.isEmpty())
			System.out.println(info);
		else
			System.out.println("the query does not contain any information or has not been instantiated.");
	}
}
