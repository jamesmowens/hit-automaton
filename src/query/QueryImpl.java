package query;

public class QueryImpl implements Query{
	
	private String frequency;
	private String condition = new String();
	private String set = new String();
	
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public String getFrequency() {
		return this.frequency;
	}
	
	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	public String getCondition() {
		return this.condition;
	}
	
	public void setSet(String set) {
		this.set = set;
	}
	
	public String getSet() {
		return this.set;
	}
	
	@Override
	public boolean evaluate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean result() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String queryInfo() {
		return new String("Frequency: "+frequency+", Condition: "+condition+", Set: "+set);
	}

}
