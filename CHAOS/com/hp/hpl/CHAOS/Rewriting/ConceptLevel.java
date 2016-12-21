/**
 * 
 */
package com.hp.hpl.CHAOS.Rewriting;

/**
 * @author Kara Greenfield
 *
 */
public class ConceptLevel 
{
	
	ConceptNode[] nodes;
	int level;

	/** default constructor
	 * 
	 */
	public ConceptLevel() 
	{
		this.level = 0;
		this.nodes = new ConceptNode[0];
	}
	
	/**overloaded constructor
	 * 
	 * @param level index of this level
	 */
	public ConceptLevel(int level)
	{
		this.level = level;
		this.nodes = new ConceptNode[0];
	}
	
	/** overloaded constructor
	 * 
	 * @param level index of this level
	 * @param nodes array of nodes at this level
	 */
	public ConceptLevel(int level, ConceptNode[] nodes)
	{
		this.level = level;
		this.nodes = new ConceptNode[nodes.length];
		for(int i = 0; i< nodes.length; i++)
		{
			this.nodes[i] = nodes[i];
		}
	}
	
	/** returns the index of this level
	 * 
	 * @return the index of this level
	 */
	public int getLevel()
	{
		return this.level;
	}
	
	/** Add a new node to this level
	 * 
	 * @param node new node to be added
	 */
	public void addNode(ConceptNode node)
	{
		if(node.level != this.level)
		{
			return;
		}
		else
		{
			this.incrementNumNodes();
			int numNodes = this.getNumNodes();
			this.nodes[numNodes-1] = node;
		}
	}
	
	/**removes a concept node from this level
	 * 
	 * @param index index of node to be removed
	 */
	public void removeNode(int index)
	{
		int numNodes = this.getNumNodes();
		for(int i = index; i< numNodes-1; i++)
		{
			nodes[i] = nodes[i+1];
		}
		
		ConceptNode[] newNodes = new ConceptNode[numNodes-1];
		for(int i = 0; i<numNodes-1; i++)
		{
			newNodes[i] = nodes[i];
		}
		
		nodes = new ConceptNode[numNodes-1];
		for(int i = 0; i<numNodes-1; i++)
		{
			nodes[i] = newNodes[i];
		}
		
		
	}
	
	/**Add a new node with a give name to this level
	 * No other information about the node is known at this time
	 * 
	 * @param name name of node being added
	 */
	public void addNode(String name)
	{
		ConceptNode node = new ConceptNode(name, this.level);
		this.incrementNumNodes();
		int numNodes = this.getNumNodes();
		this.nodes[numNodes-1] = node;
	}
	
	/** increases the number of pairs of nodes in this level by 1
	 * 
	 */
	public void incrementNumNodes()
	{
		int numNodes = getNumNodes();
		numNodes++;
		ConceptNode[] newNodes = new ConceptNode[numNodes];
		
		//create temp array
		for(int i = 0; i<numNodes-1; i++)
		{
			newNodes[i] = nodes[i];
		}

		nodes = new ConceptNode[numNodes];
		
		//copy temp array into bounds
		for(int i = 0; i<numNodes-1; i++)
		{
			nodes[i] = newNodes[i];
		}
		
	}
	
	/** Counts the number of nodes at this level
	 * 
	 * @return the number of nodes at this level
	 */
	public int getNumNodes()
	{
		return nodes.length;
	}

	/** returns one of the nodes at this level
	 * 
	 * @param i index of node to be returned
	 * @return node at this level located at index i
	 */
	public ConceptNode getNode(int i)
	{
		return nodes[i];
	}
	
	/**Prints out a level of the tree
	 * 
	 */
	public void printLevel()
	{
		int numNodes = getNumNodes();
		ConceptNode node = new ConceptNode();
		for(int i = 0; i< numNodes; i++)
		{
			node = getNode(i);
			node.printNode();
			System.out.print("\t");
		}
	}
}
