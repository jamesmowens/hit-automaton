/**
 * 
 */
package com.hp.hpl.CHAOS.ECube;

/**
 * @author Kara Greenfield
 *
 */
public class ConceptTree 
{
	
	ConceptLevel[] levels;

	/**default constructor
	 * 
	 */
	public ConceptTree() 
	{
		levels = new ConceptLevel[0];
	}
	
	/**overloaded constructor
	 * 
	 * @param levels array of levels in hierarchy
	 */
	public ConceptTree(ConceptLevel[] levels)
	{
		this.levels = new ConceptLevel[levels.length];
		for(int i = 0; i< levels.length; i++)
		{
			this.levels[i] = levels[i];
		}
			
	}
	
	/** counts the number of levels in the concept hierarchy tree
	 * 
	 * @return the number of levels
	 */
	public int getNumLevels()
	{
		return levels.length;
	}
	
	/** Gets the root of the concept hierarchy tree
	 * 
	 * @return the node located at the root of the tree
	 */
	public ConceptNode getRoot()
	{
		return (levels[0]).nodes[0];
	}
	
	
	/**
	 * 
	 * @return the number of nodes in the tree
	 */
	public int getNodesNum()
	{
		int num = 0;
	   for(int i =0 ; i< levels.length; i++)
	   {
		   num +=levels[i].getNumNodes();
	   }
		return num;
	}
	/** returns a level of the tree
	 * 
	 * @param i index of level to return
	 * @return ith level of the tree
	 */
	public ConceptLevel getLevel(int i)
	{
		return levels[i];
	}
	
	/**Adds a level to the tree
	 * 
	 * @param level new level to be added
	 */
	public void addLevel(ConceptLevel level)
	{
		this.incrementNumLevels();
		int numLevels = this.getNumLevels();
		this.levels[numLevels-1] = level;
	}
	
	/**Adds an empty level to the tree
	 * 
	 */
	public void addLevel()
	{
		int num = getNumLevels();
		ConceptLevel level = new ConceptLevel(num);
		this.incrementNumLevels();
		int numLevels = this.getNumLevels();
		this.levels[numLevels-1] = level;
	}
	
	/** increases the number of levels in this tree by 1
	 * 
	 */
	public void incrementNumLevels()
	{
		int numLevels = getNumLevels();
		numLevels++;
		ConceptLevel[] newLevels = new ConceptLevel[numLevels];
		
		//create temp array
		for(int i = 0; i<numLevels-1; i++)
		{
			newLevels[i] = levels[i];
		}

		levels = new ConceptLevel[numLevels];
		
		//copy temp array into bounds
		for(int i = 0; i<numLevels-1; i++)
		{
			levels[i] = newLevels[i];
		}
		
	}
	
	/**prints out the tree
	 * 
	 */
	public void printTree()
	{
		int numLevels = getNumLevels();
		ConceptLevel level = new ConceptLevel();
		for(int i = 0; i< numLevels; i++)
		{
			level = getLevel(i);
			System.out.print(i+1 + ": ");
			level.printLevel();
			System.out.println();
		}
	}

	//TODO
	/*public void reformat()
	{
		int numLevels = getNumLevels();
		for(int i = 1; i<numLevels; i++)
		{
			ConceptLevel currentLevel = this.getLevel(i);
			int numNodes = currentLevel.getNumNodes();
			
			ConceptLevel prevLevel = this.getLevel(i-1);
			int numNodesAbove = prevLevel.getNumNodes();
			
			for(int j = 0; j< numNodes; j++)
			{
				for(int k = 0; k<numNodesAbove; k++)
				{
					if(currentLevel.getNode(j).intersect(prevLevel.getNode(k)))//there is an intersection
					{
						
					}
				}
			}
		}
	}*/
}
