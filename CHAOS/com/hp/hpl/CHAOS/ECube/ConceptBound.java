/**
 * 
 */
package com.hp.hpl.CHAOS.ECube;

/**
 * @author Kara Greenfield
 *
 */
public class ConceptBound 
{

	public double x;
	double y, z;
	int numBounds;

	/**default constructor
	 * 
	 */
	public ConceptBound()  
	{
		//default initialization
		this.x=0;
		this.y=0;
		this.z=0;
		this.numBounds = 0;
	}

	/**constructor for bounds with 1 dimension
	 * 
	 * @param x x dimension
	 */
	public ConceptBound(double x) 
	{
		this.x = x;
		this.y = 0;
		this.z = 0;
		this.numBounds = 1;
	}

	/**constructor for bounds with 2 dimensions
	 * 
	 * @param x x dimension
	 * @param y y dimension
	 */
	public ConceptBound(double x, double y) 
	{
		this.x = x;
		this.y = y;
		this.z = 0;
		this.numBounds = 2;
	}

	/**constructor for bounds with 3 dimensions
	 * 
	 * @param x x dimension
	 * @param y y dimension
	 * @param z z dimension
	 */
	public ConceptBound(double x, double y, double z) 
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.numBounds = 3;
	}


	

	/**returns the x value of the bound
	 * 
	 * @return x value of the bound
	 */
	public double getX()
	{
		return this.x;
	}

	/**returns the x\y value of the bound
	 * 
	 * @return y value of the bound
	 */
	public double getY()
	{
		return this.y;
	}

	/**returns the z value of the bound
	 * 
	 * @return z value of the bound
	 */
	public double getZ()
	{
		return this.z;
	}

	/**changes the x value of the bound
	 * 
	 * @param newX
	 */
	public void setX(double newX)
	{
		if(numBounds >= 1)
		{
			this.x = newX;
		}
	}

	/**changes the y value of the bound
	 * 
	 * @param newY
	 */
	public void setY(double newY)
	{
		if(numBounds >= 2)
		{
			this.y = newY;
		}
	}

	/**changes the z value of the bound
	 * 
	 * @param newZ
	 */
	public void setZ(double newZ)
	{
		if(numBounds >= 3)
		{
			this.z = newZ;
		}
	}

	/** Determines if this bound is less than the other bound
	 * "less than" is defined as being to the left of and below
	 * 
	 * @param otherBound other bound to compare this bound to
	 * @return true if this bound is less than the other bound
	 */
	public boolean lessThan(ConceptBound otherBound)
	{
		if(numBounds == 1)
		{
			return this.x < otherBound.x;
		}

		else if(numBounds == 2)
		{
			return (this.x < otherBound.x) && (this.y < otherBound.y);
		}
		else
		{
			return (this.x < otherBound.x) && (this.y < otherBound.y) && (this.z < otherBound.z);
		}
	}

	/** Determines if this bound is less than or equal to the other bound
	 * "less than" is defined as being to the left of and below
	 * 
	 * @param otherBound other bound to compare this bound to
	 * @return true if this bound is less than or equal to the other bound
	 */
	public boolean lessThanEqual(ConceptBound otherBound)
	{
		if(numBounds == 1)
		{
			return this.x <= otherBound.x;
		}

		else if(numBounds == 2)
		{
			return (this.x <= otherBound.x) && (this.y <= otherBound.y);
		}
		else
		{
			return (this.x <= otherBound.x) && (this.y <= otherBound.y) && (this.z <= otherBound.z);
		}
	}

	/** Determines if this bound is greater than the other bound
	 * "greater than" is defined as being to the right of and above
	 * 
	 * @param otherBound other bound to compare this bound to
	 * @return true if this bound is greater than the other bound
	 */
	public boolean greaterThan(ConceptBound otherBound)
	{
		if(numBounds == 1)
		{
			return this.x > otherBound.x;
		}

		else if(numBounds == 2)
		{
			return (this.x > otherBound.x) && (this.y > otherBound.y);
		}
		else
		{
			return (this.x > otherBound.x) && (this.y > otherBound.y) && (this.z > otherBound.z);
		}
	}

	/** Determines if this bound is greater than or equal to the other bound
	 * "greater than" is defined as being to the right of and above
	 * 
	 * @param otherBound other bound to compare this bound to
	 * @return true if this bound is greater than or equal to the other bound
	 */
	public boolean greaterThanEqual(ConceptBound otherBound)
	{
		if(numBounds == 1)
		{
			return this.x >= otherBound.x;
		}

		else if(numBounds == 2)
		{
			return (this.x >= otherBound.x) && (this.y >= otherBound.y);
		}
		else
		{
			return (this.x >= otherBound.x) && (this.y >= otherBound.y) && (this.z >= otherBound.z);
		}
	}

	/**Prints out the dimension values of this bound
	 * 
	 */
	public void printBound()
	{
		if(numBounds == 1)
		{
			System.out.print("(" + x + ")");
		}
		else if(numBounds == 2)
		{
			System.out.print("(" + x + ", " + y + ")");
		}
		else
		{
			System.out.println("(" + x + ", " + y + ", " + z + ")");
		}
	}

	
	/**Determines if this bound is in between the 2 given bounds
	 * 
	 * @param other1 bound to compare this bound to
	 * @param other2 bound to compare this bound to
	 * @return true if this bound is in the middle, false otherwise
	 */
	public boolean isBetween(ConceptBound other1, ConceptBound other2)
	{

		if(((this.x < other1.x) && (this.x> other2.x))
				|| ((this.x > other1.x) && (this.x< other2.x)))
		{
			if(this.y == 0)
			{
				return true;
			}
			if(((this.y < other1.y) && (this.y> other2.y))
					|| ((this.y > other1.y) && (this.y< other2.y)))
			{
				if(this.z == 0)
				{
					return true;
				}
				if(((this.z < other1.z) && (this.z> other2.z))
						|| ((this.z >= other1.z) && (this.z<= other2.z)))
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}

}
