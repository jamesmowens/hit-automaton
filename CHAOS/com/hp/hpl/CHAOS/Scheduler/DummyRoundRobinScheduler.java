package com.hp.hpl.CHAOS.Scheduler;

import com.hp.hpl.CHAOS.StreamOperator.StreamOperator;

public class DummyRoundRobinScheduler extends Scheduler {

	private int index;
	private int activeOpNum;

	public DummyRoundRobinScheduler(StreamOperator[] operators) {
		super(operators);
		this.index = 0;
		this.activeOpNum = operators.length;
	}

	public int getIndex() {
		return index;
	}

	public int getActiveOpNum() {
		return activeOpNum;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setActiveOpNum(int activeOpNum) {
		this.activeOpNum = activeOpNum;
	}

	@Override
	public void setOperators(StreamOperator[] operators) {
		super.setOperators(operators);
		this.index = 0;
		this.activeOpNum = operators.length;
	}

	@Override
	public int runNext() {
		int ret = 0;

		if (this.activeOpNum <= 0)
			return -1;// nothing to run

		while (!this.status[index])
			index = (index + 1) % this.operators.length;

		StreamOperator op = this.operators[index];
		op.preRun();
		ret = op.run();
		op.postRun();

		if (ret < 0) {
			this.status[index] = false;
			this.activeOpNum--;
		}
		index = (index + 1) % this.operators.length;
		return 1;
	}

	@Override
	public String toString() {
		return "RoundRobinScheduler";
	}
}
