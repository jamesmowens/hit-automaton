package com.hp.hpl.CHAOS.QueryPlan;

public interface RunTimeBlock {
	//return -1 if something wrong
	public int init();
	public void finalizing();
}
