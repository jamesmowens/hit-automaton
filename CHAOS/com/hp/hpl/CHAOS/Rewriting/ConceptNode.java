/**
 * 
 */
package com.hp.hpl.CHAOS.Rewriting;




/**
 * @author Kara Greenfield
 *
 */
public class ConceptNode 
{

	public String name;
	int level;
	ConceptBound[][] bounds;

	/**default constructor
	 * 
	 */
	public ConceptNode() 
	{
		this.name = "";
		this.level = 0;
		this.bounds = new ConceptBound[2][0];
	}

	/**overloaded constructor
	 * 
	 * @param name name associated with this node
	 * @param level level of this node
	 */
	public ConceptNode(String name, int level) 
	{
		this.name = name;
		this.level = level;

		int numBounds = bounds[0].length;
		this.bounds = new ConceptBound[2][numBounds];
	}

	/**overloaded constructor
	 * 
	 * @param name name associated with this node
	 * @param level level of this node
	 * @param bounds array of the left and right bounds of this node
	 */
	public ConceptNode(String name, int level, ConceptBound[][] bounds) 
	{
		this.name = name;
		this.level = level;

		int numBounds = bounds[0].length;
		this.bounds = new ConceptBound[2][numBounds];
		for(int i = 0; i<numBounds; i++)
		{
			this.bounds[0][i] = bounds[0][i]; //left values
			this.bounds[1][i] = bounds[1][i]; //right values
		}

	}

	/** returns the name of this node
	 * 
	 * @return the name associated with this node
	 */
	public String getName()
	{
		return this.name;
	}

	/** Assigns a new name to this node
	 * 
	 * @param newName new name to be given to node
	 */
	public void setName(String newName)
	{
		this.name = newName;
	}

	/** returns the level of this node
	 * 
	 * @return the level of this node
	 */
	public int getLevel()
	{
		return this.level;
	}

	/** Assigns a new level to this node
	 * 
	 * @param newLevel new level to be assigned to this node
	 */
	public void setLevel(int newLevel)
	{
		this.level = newLevel;
	}

	/** Counts the number of pairs of bounds of this node
	 * 
	 * @return the number of pairs of left and right bounds
	 */
	public int getNumBounds()
	{
		return this.bounds[0].length;
	}

	/** returns one of the left bounds of this node
	 * 
	 * @param i index of left bound to be returned
	 * @return left bound at index i
	 */
	public ConceptBound getLeftBound(int i)
	{
		return this.bounds[0][i];
	}

	/** returns one of the right bounds of this node
	 * 
	 * @param i index of right bound to be returned
	 * @return right bound at index i
	 */
	public ConceptBound getRightBound(int i)
	{
		return this.bounds[1][i];
	}

	/** Adds a new pair of bounds to the list of bounds for this node
	 * Bounds have 1 dimension
	 * 
	 * @param leftx x value of new left bound to be added
	 * @param rightx x  value of new right bound to be added
	 */
	public void addBounds(double leftx, double rightx)
	{
		incrementNumBounds();
		addLeftBound(leftx);
		addRightBound(rightx);
	}

	/** Adds a new pair of bounds to the list of bounds for this node
	 * Bounds have 2 dimensions
	 * 
	 * @param leftx x value of new left bound to be added
	 * @param rightx x  value of new right bound to be added
	 * @param lefty y value of new left bound to be added
	 * @param righty y  value of new right bound to be added
	 */
	public void addBounds(double leftx, double lefty, double rightx, double righty)
	{
		incrementNumBounds();
		addLeftBound(leftx, lefty);
		addRightBound(rightx, righty);
	}

	/** Adds a new pair of bounds to the list of bounds for this node
	 * Bounds have 3 dimensions
	 * 
	 * @param leftx x value of new left bound to be added
	 * @param rightx x  value of new right bound to be added
	 * @param lefty y value of new left bound to be added
	 * @param righty y  value of new right bound to be added
	 * @param leftz z value of new left bound to be added
	 * @param rightz z  value of new right bound to be added
	 */
	public void addBounds(double leftx, double lefty, double leftz, double rightx, double righty, double rightz)
	{
		incrementNumBounds();
		addLeftBound(leftx, lefty, leftz);
		addRightBound(rightx, righty, rightz);
	}

	/** Adds a new left bound to the list of bounds for this node
	 * New bound has 1 dimension
	 * 
	 * @param x x value of new left bound
	 */
	public void addLeftBound(double x)
	{
		int numBounds = getNumBounds();

		ConceptBound left = new ConceptBound(x);

		bounds[0][numBounds-1] = left;
	}

	/** Adds a new left bound to the list of bounds for this node
	 * New bound has 2 dimensions
	 * 
	 * @param x x value of new left bound
	 * @param y y value of new left bound
	 */
	public void addLeftBound(double x, double y)
	{
		int numBounds = getNumBounds();

		ConceptBound left = new ConceptBound(x, y);

		bounds[0][numBounds-1] = left;
	}

	/** Adds a new left bound to the list of bounds for this node
	 * New bound has 3 dimensions
	 * 
	 * @param x x value of new left bound
	 * @param y y value of new left bound
	 * @param z z value of new left bound
	 */
	public void addLeftBound(double x, double y, double z)
	{
		int numBounds = getNumBounds();

		ConceptBound left = new ConceptBound(x, y, z);

		bounds[0][numBounds-1] = left;
	}

	/** Adds a new right bound to the list of bounds for this node
	 * New bound has 1 dimension
	 * 
	 * @param x x value of new right bound
	 */
	public void addRightBound(double x)
	{
		int numBounds = getNumBounds();

		ConceptBound right = new ConceptBound(x);

		bounds[0][numBounds-1] = right;
	}

	/** Adds a new right bound to the list of bounds for this node
	 * New bound has 2 dimensions
	 * 
	 * @param x x value of new right bound
	 * @param y y value of new right bound
	 */
	public void addRightBound(double x, double y)
	{
		int numBounds = getNumBounds();

		ConceptBound right = new ConceptBound(x, y);

		bounds[0][numBounds-1] = right;
	}

	/** Adds a new right bound to the list of bounds for this node
	 * New bound has 3 dimensions
	 * 
	 * @param x x value of new right bound
	 * @param y y value of new right bound
	 * @param z z value of new right bound
	 */
	public void addRightBound(double x, double y, double z)
	{
		int numBounds = getNumBounds();

		ConceptBound right = new ConceptBound(x, y, z);

		bounds[0][numBounds-1] = right;
	}

	/** increases the number of pairs of bounds for this node by 1
	 * 
	 */
	public void incrementNumBounds()
	{
		int numBounds = getNumBounds();
		numBounds++;
		ConceptBound[][] newBounds = new ConceptBound[2][numBounds];

		//create temp array
		for(int i = 0; i<numBounds-1; i++)
		{
			newBounds[0][i] = bounds[0][i];
			newBounds[1][i] = bounds[1][i];
		}

		bounds = new ConceptBound[2][numBounds];

		//copy temp array into bounds
		for(int i = 0; i<numBounds-1; i++)
		{
			bounds[0][i] = newBounds[0][i];
			bounds[1][i] = newBounds[1][i];
		}

	}

	/** Determines whether or not this node is a child of the given parent node
	 * 
	 * @param parent possible parent node
	 * @return true if this node is a child of the given parent node, false otherwise
	 */
	public boolean isContainedIn(ConceptNode parent)
	{
		int parentLevel = parent.getLevel();
		int childLevel = this.getLevel();

		if(childLevel >= parentLevel)
		{
			return false;
		}

		int numBoundsParent = parent.getNumBounds();
		int numBoundsChild = this.getNumBounds();

		for(int i = 0; i< numBoundsParent; i++)
		{
			for(int j = 0; j < numBoundsChild; j++)
			{
				if(parent.bounds[0][i].lessThanEqual(this.bounds[0][j])
						&& parent.bounds[1][i].greaterThanEqual(this.bounds[1][j]))
				{
					return true;
				}
			}
		}

		return false;
	}

	/**Prints out a node
	 * 
	 */
	public void printNode()
	{
		System.out.print(name + " (" );
		int numBounds = getNumBounds();
		ConceptBound left = new ConceptBound();
		ConceptBound right = new ConceptBound();
		for(int i = 0; i< numBounds; i++)
		{
			left = getLeftBound(i);
			right = getRightBound(i);

			System.out.print("[");
			left.printBound();
			System.out.print(" : ");
			right.printBound();
			System.out.print("]");

			if(i!=numBounds-1)
			{
				System.out.print(", ");
			}
		}
	}

	/**Determines whether this node intersects another node
	 * 
	 * @param otherNode node that this node is being compared with
	 * @return whether or not there is an intersection
	 */
	public boolean intersect(ConceptNode otherNode)
	{
		int numBounds1 = this.getNumBounds();
		int numBounds2 = otherNode.getNumBounds();


		for(int i = 0; i<numBounds1; i++)
		{
			for(int j = 0; j< numBounds2; j++)
			{
				//node is entirely contained in other node
				if(this.bounds[0][i].isBetween(otherNode.bounds[0][j], otherNode.bounds[1][j])
						&& this.bounds[1][i].isBetween(otherNode.bounds[0][j], otherNode.bounds[1][j]))
				{
					return false;
				}
				
				// other node is entirely contained in this node
				if(otherNode.bounds[0][j].isBetween(this.bounds[0][i], this.bounds[1][i])
						&& otherNode.bounds[1][j].isBetween(this.bounds[0][i], this.bounds[1][i]))
				{
					return false;
				}
				
				//current left bound located in-between
				if(this.bounds[0][i].isBetween(otherNode.bounds[0][j], otherNode.bounds[1][j]))
				{
					return true;
				}

				//current left bound located in-between
				else if(this.bounds[1][i].isBetween(otherNode.bounds[0][j], otherNode.bounds[1][j]))
				{
					return true;
				}

				

				else if(this.bounds[0][i].getX()<=otherNode.bounds[0][j].getX()
						&& this.bounds[1][i].getX()>=otherNode.bounds[1][j].getX()
						&& this.bounds[0][i].getY()<=otherNode.bounds[1][j].getY()
						&& this.bounds[1][i].getY()>=otherNode.bounds[0][j].getY()
						&& this.bounds[0][i].getZ()<=otherNode.bounds[1][j].getZ()
						&& this.bounds[1][i].getZ()>=otherNode.bounds[0][j].getZ())
				{
					return true;
				}
				
				else if(this.bounds[0][i].getY()<=otherNode.bounds[0][j].getY()
						&& this.bounds[1][i].getY()>=otherNode.bounds[1][j].getY()
						&& this.bounds[0][i].getX()<=otherNode.bounds[1][j].getX()
						&& this.bounds[1][i].getX()>=otherNode.bounds[0][j].getX()
						&& this.bounds[0][i].getZ()<=otherNode.bounds[1][j].getZ()
						&& this.bounds[1][i].getZ()>=otherNode.bounds[0][j].getZ())
				{
					return true;
				}
				
				else if(this.bounds[0][i].getZ()<=otherNode.bounds[0][j].getZ()
						&& this.bounds[1][i].getZ()>=otherNode.bounds[1][j].getZ()
						&& this.bounds[0][i].getY()<=otherNode.bounds[1][j].getY()
						&& this.bounds[1][i].getY()>=otherNode.bounds[0][j].getY()
						&& this.bounds[0][i].getX()<=otherNode.bounds[1][j].getX()
						&& this.bounds[1][i].getX()>=otherNode.bounds[0][j].getX())
				{
					return true;
				}

			}
		}
		return false;
	}


}
