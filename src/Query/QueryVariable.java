/**
 * Nicholas Fajardo
 * nafajardo@wpi.edu
 * The following signature indicates that the author above pertains all rights to any ideas implemented in the code below.
 * Signature: Nicholas Fajardo
 * Modified: MaryAnn VanValkenburg (mevanvalkenburg@wpi.edu) 02/06/2017, implemented method stubs
 */

package Query;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		System.out.println("VariableQuery running");
		String expression = ex.getExpression();

		boolean result = this.ex.evaluate(expression);
		if(result){
			execute(set);
		} //TODO else, execute different set?
	}

	/**
	 * Called in ex.evaluate is true
	 * @param set Expression that defines the variable to be changed and the value to change it to
	 */
	private void execute(String set){
		//System.out.println("Set is being run"); //TODO implement this

		//Parse string, search for variable name and assignment operator
		Pattern pattern = Pattern.compile("([a-zA-Z]+) = ([0-9]+)");
		Matcher matcher = pattern.matcher(set);

		String variable = "";
		String value = "";

		if(matcher.matches()) {
			System.out.println(matcher.group(1)); // variable to be reset
			System.out.println(matcher.group(2)); // value to set it to
			variable = matcher.group(1);
			value = matcher.group(2);
		} else {
			System.err.println("Expression not evaluable: no variable specified");
		}

		//Update variables.txt
		try {			
			System.out.println("File exists");
			BufferedReader file = new BufferedReader(new FileReader(new File("variables.txt")));
			BufferedWriter temp = new BufferedWriter(new FileWriter(new File("temp.txt")));

			String line = file.readLine();
			while (line != null) {
				if (line.startsWith(variable + " =")) {
					//System.out.println(line);
					temp.append(variable + " = " + value + "\n");
					//System.out.println(variable + " = " + value + "\n");
				} else {
					temp.append(line+"\n");
				}
				line = file.readLine();
			}
			file.close();
			temp.close();
			
			// Copy over new file
			Files.copy(new File("temp.txt").toPath(), new File("variables.txt").toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.delete(new File("temp.txt").toPath());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}



	public static void main(String args[]) {

		Query test1 = new QueryVariable(new Condition("x > 4"), "B1", "x greater than", "x = 6");
		test1.run();
	}

}
