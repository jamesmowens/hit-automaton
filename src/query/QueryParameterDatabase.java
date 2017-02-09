package query;

public interface QueryParameterDatabase {

	/**
	 * Returns the corresponding thing you need
	 * @param key is the key for the thing you need to access
	 * @return the thing you need to access
	 */
	public int returnVal(String key);
	
	/**
	 * Depending on the implementation you are going to use, this is versatile and allows the user to specify what he or she needs
	 * @param key is the key for the parameter you need to access
	 * @param thing is something you feed in to update it if necessary. Feed null if not
	 * @param action can be a string regarding increments, adding, etc...
	 */
	public void updateVal(String key, int thing, String action);
}
