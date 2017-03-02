/**
 * MaryAnn VanValkenburg
 * mevanvalkenburg@wpi.edu
 * The following signature indicates that the author above pertains all rights to any ideas implemented in the code below.
 * Signature: MaryAnn VanValkenburg
 * Modified: MaryAnn VanValkenburg (mevanvalkenburg@wpi.edu) 02/25/2017, first commits
 */

package Query;

/**
 * Container class for variables to be used by queries
 * A variable is an intermediate data point created by parsing a raw data stream. Variables represent any piece of
 * data relevant to the state at that particular moment in time. Variables can be queried by VariableQuery and
 * TransitionQuery. A VariableQuery will query one particular variable (a "simple" variable) and, based on the result 
 * of the query, will update another variable (a "complex" variable). A TransitionQuery will query a complex variable
 * and, based on the result of the query, will change the current state of the machine.
 * 
 * @author mevanvalkenburg
 *
 * @param <T> A value of a variable, can be any numeric type
 */
public class Variable { //change to <N extends Number>, for now all are doubles
	private String name;
	private double value;
	private boolean simple;
	
	public Variable() {
	}
	
	public Variable(String name, double value, boolean simple) {
		this.name = name;
		this.value = value;
		this.simple = simple;
	}
	
	public String getName() {
		return name;
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
	public double getValue() {
		return value;
	}
	
	public boolean isSimple() {
		return this.simple;
	}
	
}
