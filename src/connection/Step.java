package connection;

public class Step {
	final String source;
	final String target;
	final String label;
	
	public Step(String source, String target, String label) {
		this.source = source;
		this.target = target;
		this.label = label;
	}

	public String getSource() {
		return source;
	}
	public String getTarget() {
		return target;
	}
	public String getLabel() {
		return label;
	}
	
	@Override
	public String toString() {
		return "source = " + source + ", target = " + target + ", label = " + label;
	}
}
