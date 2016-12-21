package com.hp.hpl.CHAOS.Scheduler;

import com.hp.hpl.CHAOS.StreamOperator.StreamOperator;

public abstract class Scheduler {
	StreamOperator[] operators;
	boolean[] status;

	public Scheduler(StreamOperator[] operators) {
		super();
		this.operators = operators;
		this.status = new boolean[this.operators.length];
		for (int i = 0; i < this.status.length; i++)
			this.status[i] = true;
	}

	public StreamOperator[] getOperators() {
		return operators;
	}

	public void setOperators(StreamOperator[] operators) {
		this.operators = operators;
		this.status = new boolean[this.operators.length];
		for (int i = 0; i < this.status.length; i++)
			this.status[i] = true;
	}

	// return integer to executor.
	// >0: time to sleep (millisecond or microsecond)
	// >0: tuple processed
	// -1: nothing to run
	// -2: to wait()
	// else <0: something wrong
	// This will make decision of which operator to run, how many to run, and
	// run it.
	abstract public int runNext();

	abstract public String toString();

	public void reset() {
	}
}
