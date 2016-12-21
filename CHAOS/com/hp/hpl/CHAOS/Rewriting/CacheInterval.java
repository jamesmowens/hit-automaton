package com.hp.hpl.CHAOS.Rewriting;

public class CacheInterval {

		double left;
		double right;
		
		double getLeft()
		{
			return left;
			
		}
		double getRight()
		{
			return right;
			
		}
		void setInterval(double left, double right)
		{
			this.left = left;
			this.right = right;
			
		}
}
