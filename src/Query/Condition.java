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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;

public class Condition {
	// Query expression, contains "?" that will be replaced with parameter of interest
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
		// TODO: Right now, variable name must be strictly alphabetic, maybe we want this to be alphanumeric later
		Pattern pattern = Pattern.compile("*([A-z])*");
		Matcher matcher = pattern.matcher(comparable);

		// Extract the variable from the expression
		String variable;
		if (matcher.matches()) {
			variable = matcher.group(1);
			// find match in variable list and deal with it from there
		} else {
			System.err.println("Expression not evaluable: no variable specified");
			return false;
		}

		// Search for variable in global variable list. If exists, update comparable with value
		// If does not exist, replaces variable with 0. That variable will then be added to the global var list
		double var;
		try {
			var = getVariable(variable);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
		comparable.replaceAll("[a-z]", ""+var);

		try {
			return simpleEvaluate(comparable);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Pulls the current value of a variable from global "variables.txt"
	 *
	 * @param name name of variable
	 * @return integer value of variable
	 * @throws Exception //TODO
	 */
	private double getVariable(String name) throws Exception { //TODO should this be static?

		// Retrieves variables.txt from relative file path
		Path currentRelativePath = Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString();
		BufferedReader file = new BufferedReader(new FileReader(s +"/variables.txt")); //TODO stored in hit_automaton

		// Checks for line matching
		String line = file.readLine();
		while (line != null) {
			if (line.startsWith(name + " =")) {
				// assuming the var to the right of the equals is an int
				double temp = Float.parseFloat(line.substring(line.lastIndexOf("= ") + 2));
				return temp;
			}
			line = file.readLine();
		}
		System.err.println("Error: no variable of that name present"); //TODO
		file.close();
		return 0;
	}

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
