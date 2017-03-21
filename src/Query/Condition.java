/**
 * Nicholas Fajardo
 * nafajardo@wpi.edu
 * The following signature indicates that the author above pertains all rights to any ideas implemented in the code below.
 * Signature: Nicholas Fajardo
 * Modified by MaryAnn VanValkenburg (mevanvalkenburg@wpi.edu) 02/07/2017, added String ex, implemented evaluate()
 * 	added getVariable() and simpleEvaluate()
 */
package Query;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import edu.usfca.vas.graphics.fa.GElementFAMachine;
import edu.usfca.xj.appkit.gview.object.GElement;


public class Condition {
	private String ex;

	public Condition(){}

	public Condition(String ex) {
		this.ex = ex;
	}

	public String getExpression() {
		return ex;
	}

	/**
	 * Evaluates a string containing comparison operator and returns the value
	 * @param comparable string containing variable name, comparison operator, and parameter to be compared
	 * @return true if variable exists and comparable evaluates to true, else returns false
	 * @throws Exception 
	 */
	public boolean evaluate(String comparable) {

		//Split string by whitespace
		String[] splitSet = comparable.split("\\s+");

		// For remaining values, convert to numbers and operators, concatenate into a string
		StringBuilder toEvaluate = new StringBuilder();

		for (int i = 0; i < splitSet.length; i++) {
			// null case, to initialize state and get first data set
			if (i == 0 && splitSet[i].equals("NULL")) {
				return GElementFAMachine.variableMap.isEmpty();
			}

			// multiple comparison strings, evaluate first one
			if (splitSet[i].equals("AND")) {
				// evaluate string, set result to new value
				String evaluable = toEvaluate.toString();
				if (simpleEvaluate(evaluable)) {
					toEvaluate = new StringBuilder();
				} else {
					return false;
				}
			}

			// OR
			if (splitSet[i].equals("OR")) {
				// evaluate string, set result to new value
				String evaluable = toEvaluate.toString();
				if (simpleEvaluate(evaluable)) {
					return true;
				} else {
					toEvaluate = new StringBuilder();
				}
			}

			// Check if string is a comparison
			else if (isComparison(splitSet[i])) {
				toEvaluate.append(splitSet[i]);
			}
			// Check if string is a number
			else if (isNumeric(splitSet[i])) {
				//System.out.println("String "+i+" is a double: "+value);
				toEvaluate.append(splitSet[i]);
			} 
			// Check if string is an operator
			else if (isOperator(splitSet[i])) {
				//System.out.println("String "+i+" is an operator: "+value);
				toEvaluate.append(splitSet[i]);
			} 
			// Check if string is a variable in the map
			else if (GElementFAMachine.variableMap.containsKey(splitSet[i])) {
				double tempValue = GElementFAMachine.variableMap.get(splitSet[i]).getValue();
				toEvaluate.append(tempValue);
			} else {
				System.err.println("Cannot evaluate the following: "+splitSet[i]);
			}
		}
		// Evaluate the concatenated string (should only be numbers and operators)
		String evaluable = toEvaluate.toString();
		return simpleEvaluate(evaluable);
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

	// Checks if string is a comparison operator
	private static boolean isComparison(String str) {
		return str.equals(">") || str.equals("<") || str.equals("==") || str.equals(">=") ||
				str.equals("<=") || str.equals("!=");
	}


	/**
	 * Evaluates a boolean expression using JavaScript engine
	 * @param comparable String form of boolean expression to be evaluated
	 * @return The result of evaluating the boolean expression
	 */
	private boolean simpleEvaluate(String comparable) { //TODO handle exception
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("JavaScript");

		try {
			//System.out.println(engine.eval(comparable));
			return (Boolean) engine.eval(comparable);
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} //TODO remove when done
	}


//	public static void main(String[] args) {
//		GElementFAMachine.variableMap.put("x", new Variable("x",4.0,true));
//		Condition myCondition = new Condition();
//		System.out.println(myCondition.evaluate("x > 2"));
//		System.out.println(myCondition.evaluate("x > 2 AND 3 != 1"));
//	}
}
