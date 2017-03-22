/**
 * Nicholas Fajardo
 * nafajardo@wpi.edu
 * The following signature indicates that the author above pertains all rights to any ideas implemented in the code below.
 * Signature: Nicholas Fajardo
 * Modified: MaryAnn VanValkenburg (mevanvalkenburg@wpi.edu) 02/06/2017, implemented method stubs
 * Modified: MaryAnn VanValkenburg (mevanvalkenburg@wpi.edu) 02/19/2017, implemented execute()
 * Modified: MaryAnn VanValkenburg (mevanvalkenburg@wpi.edu) 02/24/2017, refactored to "VariableQuery" (from QueryVariable)
 */

package Query;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import edu.usfca.vas.graphics.fa.GElementFAMachine;

public class VariableQuery extends Query {

	//This is the string that has the set value and name of the variable
	private String set;

	public VariableQuery(Condition expression, String pertainingState, String queryName, String setValue) {
		super(expression, pertainingState, queryName);
		this.set = setValue;
	}

	/** Explicit constructor */
	public VariableQuery(String state, String info, String pattern, Condition ex, String set) {
		super(state, info, pattern, ex);
		this.set = set;
	}

	/**
	 * Create an empty VariableQuery
	 */
	public VariableQuery() {
		super();
	}

	public String getSet(){
		return this.set;
	}

	/* (non-Javadoc)
	 * @see Query.Query#run()
	 */
	@Override
	public boolean run(){
		System.out.println("VariableQuery running");
		String expression = ex.getExpression();

		boolean result = this.ex.evaluate(expression);
		if(result){
			execute(set);
            return true;
		} else return false;
	}

	/**
	 * Called when ex.evaluate is true
	 * @param set Expression that defines the variable to be changed and the value to change it to
	 */
	private void execute(String set){
		//System.out.println("Set is being run"); //TODO implement this

		//Split string by whitespace
		String variable = "";
		Number value = 0;
		String[] splitSet = set.split("\\s+");
		
		// First one should always be variable to add
		//System.out.println("String 0 is: "+splitSet[0]); 
		variable = splitSet[0];

		// Second should always be assignment
		//System.out.println("String 1 is: "+splitSet[1]);

		// For remaining values, convert to numbers and operators, concatenate into a string
		StringBuilder toEvaluate = new StringBuilder();
		for (int i = 2; i < splitSet.length; i++) {
			// Check if string is a number
			if (isNumeric(splitSet[i])) {
				//System.out.println("String "+i+" is a double: "+value);
				toEvaluate.append(splitSet[i]);
			} 
			// Check if string is an operator
			else if (isOperator(splitSet[i])) {
				//System.out.println("String "+i+" is an operator: "+value);
				toEvaluate.append(splitSet[i]);
			} 
			// Check if string is a variable in the map
			else if (GElementFAMachine.variableMap.containsKey(splitSet[2])) {
				//System.out.println("String 2 is a variable in the map.");
				Object tempValue = GElementFAMachine.variableMap.get(splitSet[2]).getValue();
				toEvaluate.append(tempValue);
			} else {
                // if variable does not exist, assume it is zero (e.g. init state of drivers=drivers+1)
                toEvaluate.append(0);
				//System.err.println("Cannot evaluate the following: "+splitSet[2]);
			}
		}
		// Evaluate the concatenated string (should only be numbers and operators)
		String evaluable = toEvaluate.toString();
		value = evaluateArithmetic(evaluable);

		// If map contains key, overwrite with new value. Otherwise, make new variable
		if (GElementFAMachine.variableMap.containsKey(variable)) {
			//System.out.println("Now updating variable: "+variable);
			GElementFAMachine.variableMap.get(variable).setValue(value);
		} else {
			GElementFAMachine.variableMap.put(variable, new Variable(variable,value,false));
			//System.out.println("Now adding new variable: "+variable);
		}			

	}

	// Checks if string is a double
	private static boolean isNumeric(String str) {  
		try {  
			Double.parseDouble(str);  
		} catch (NumberFormatException e)   {  
			return false;  
		}  
		return true;  
	}
	
	// Checks if string is an arithmetic operator
	private static boolean isOperator(String str) {
		return str.equals("+") || str.equals("-") || str.equals("*") || str.equals("/");
	}

	/**
	 * Evaluates a boolean expression using JavaScript engine
	 * @param comparable String form of boolean expression to be evaluated
	 * @return The result of evaluating the boolean expression
	 */
	private static Number evaluateArithmetic(String comparable) { //TODO handle exception
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("JavaScript");

		try {
			System.out.println(engine.eval(comparable));
			return (Number) engine.eval(comparable);
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0.0;
		} //TODO remove when done
	}	
	
	public String queryPattern(){return pattern;}
	
	public void setPattern(String pat){
		this.pattern = pat;
	}
	
	

//	public static void main(String args[]) {
//
//		VariableQuery test1 = new VariableQuery(new Condition("x > 4"), "B1", "x greater than", "x = 6");
//		test1.execute("x = 6");
//	}


}
