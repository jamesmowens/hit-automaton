/**
 * 
 */
package com.hp.hpl.CHAOS.Rewriting;

/**
 * @author Kara Greenfield
 *
 */
public class EdgeLabel 
{
	int stepNum;
	int queryID;

	/** default constructor
	 * 
	 */
	public EdgeLabel() 
	{
		stepNum = 0;
		queryID = 0;
	}
	
	/**overloaded constructor
	 * 
	 */
	public EdgeLabel(int q, int s)
	{
		queryID = q;
		stepNum = s;
	}
	
	/**\retrieves the query id
	 * 
	 * @return query ID
	 */
	public int getQueryID()
	{
		return queryID;
	}
	
	/**retrieves the step number
	 * 
	 * @return step number
	 */
	public int getStep()
	{
		return stepNum;
	}
	
	/**resets the query ID
	 * 
	 * @param q new query ID
	 */
	public void setQuery(int q)
	{
		queryID = q;
	}
	
	/**resets the step number
	 * 
	 * @param s new step number
	 */
	public void setStep(int s)
	{
		stepNum = s;
	}

}
