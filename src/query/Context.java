package query;

public interface Context {

	//whatever the Context might be, it must have an evaluating factor that decides if it was fulfilled or not.
	//This should return a boolean or not depending on the result of the evaluation
	public boolean evaluate();
}
