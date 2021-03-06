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
	protected String pattern;
	protected Condition ex;
	
	public Query(Condition expression, String pertainingState, String queryName){
		this.ex = expression;
		this.state = pertainingState;
		this.pattern = "";
		constructInfo(queryName);
	}

	/** Explicit constructor used by ProjectSave. Must update all fields. */
	public Query(String state, String info, String pattern, Condition ex) {
		this.state = state;
		this.info = info;
		this.pattern = pattern;
		this.ex = ex;
	}

	public String getEx(){
		return this.ex.getExpression();
	}

	public String getState(){
		return this.state;
	}

	public Query() {}
	
	//TODO add evaluate method here
	
	//This is the method that will be run when the query is asked to check stuff
	public abstract boolean run();
	
	//This is the method that will be run when the query is asked for what it does and other information
	public String queryInfo(){return info;}
	
	public String queryPattern(){return pattern;}
	
	public void setPattern(String pat){
		this.pattern = pat;
	}
	
	private void constructInfo(String name){
		info = name + " : Condition " + ex.getExpression() + ".";
	}
	
	public void printinfo(){
		if(!info.isEmpty())
			System.out.println(info);
		else
			System.out.println("the query does not contain any information or has not been instantiated.");
	}
	
}
