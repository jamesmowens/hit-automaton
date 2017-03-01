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

		// Searches for an alphabetic string
		Pattern pattern = Pattern.compile("([a-zA-Z_0-9]+).*");
		Matcher matcher = pattern.matcher(comparable);

		String variable;
		if(matcher.matches()) {
			System.out.println(matcher.group(1));
			variable = matcher.group(1);
		} else {
			System.err.println("Expression not evaluable: no variable specified");
			return false;
		}

		// Search for variable in global variable list. If exists, update comparable with value
		double value;
		if (GElementFAMachine.variableMap.containsKey(variable)) {
			value = GElementFAMachine.variableMap.get(variable).getValue();
			comparable = comparable.replaceAll(variable, ""+value);
			System.out.println(comparable);

			try {
				return simpleEvaluate(comparable);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.err.println("Variable does not exist in map");
			return false;
		}	
		return false;
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
			System.out.println(engine.eval(comparable));
			return (Boolean) engine.eval(comparable);
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} //TODO remove when done
	}
}
