package Query;

import edu.usfca.vas.window.fa.*;
import connection.*;

public class Query {

	private String state;
	private String toState;
	private Expression ex;
	private int argumentOne;
	private int argumentTwo;
	private Step step;
	
	public Query(){
		//TODO implement this
	}
	
	public void run(){
		if(ex != null){
			if(ex.eval(argumentOne, argumentTwo)){
				runStep();
			}
		}
		else{
			System.out.println("The query could not run because the expression was null");
		}
	}
	
	private void runStep(){
		//TODO implement this
	}
}
