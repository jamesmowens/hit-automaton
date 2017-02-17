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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;


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
		try {
			value = getVariable(variable);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("Variable not found");
			return false;
		}		

		comparable = comparable.replaceAll(variable, ""+value);
		System.out.println(comparable);

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
		String inputFile = "variables.txt";

		try {			
			File inputfile = new File(inputFile);

			// check if the file exists
			if(inputfile.exists()) {
				System.out.println("File exists");

				BufferedReader file = new BufferedReader(new FileReader(inputFile));

				String line = file.readLine();
				while (line != null) {
					if (line.startsWith(name + " =")) {
						// assuming the var to the right of the equals is an int
						double temp = Float.parseFloat(line.substring(line.lastIndexOf("= ") + 2));
						file.close();
						return temp;

					}
					line = file.readLine();
				}
				System.err.println("Error: no variable of that name present"); //TODO
				file.close();
				return 0;
			}
			else {
				System.out.println("The system cannot find the file specified");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
