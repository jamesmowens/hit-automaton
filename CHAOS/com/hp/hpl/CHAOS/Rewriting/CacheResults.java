package com.hp.hpl.CHAOS.Rewriting;

import java.util.ArrayList;
import java.util.Hashtable;

public class CacheResults {
	ArrayList<ArrayList<byte[]>> cache = new ArrayList<ArrayList<byte[]>>();
	
	 double lefttime = 0;
	 double righttime = 0;
	public ArrayList<ArrayList<byte[]>> getCache() {
		return cache;
	}
	public void setCache(ArrayList<ArrayList<byte[]>> cache) {
		this.cache = cache;
	}
	public double getLefttime() {
		return lefttime;
	}
	public void setLefttime(double lefttime) {
		this.lefttime = lefttime;
	}
	public double getRighttime() {
		return righttime;
	}
	public void setRighttime(double righttime) {
		this.righttime = righttime;
	}
	
	
	public CacheResults(ArrayList<ArrayList<byte[]>> cache) {
		super();
		this.cache = cache;
	}
	public CacheResults(ArrayList<ArrayList<byte[]>> cache, double lefttime,
			double righttime) {
		super();
		this.cache = cache;
		this.lefttime = lefttime;
		this.righttime = righttime;
	}
	 
	 
	 
	 
}
