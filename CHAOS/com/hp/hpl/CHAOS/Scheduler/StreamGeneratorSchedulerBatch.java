package com.hp.hpl.CHAOS.Scheduler;

import com.hp.hpl.CHAOS.StreamOperator.StreamOperator;

public class StreamGeneratorSchedulerBatch extends Scheduler {

	private int activeOpNum;

	public StreamGeneratorSchedulerBatch(StreamOperator[] operators) {
		super(operators);
		this.activeOpNum = operators.length;
	}

	public int getActiveOpNum() {
		return activeOpNum;
	}

	public void setActiveOpNum(int activeOpNum) {
		this.activeOpNum = activeOpNum;
	}

	@Override
	public void setOperators(StreamOperator[] operators) {
		super.setOperators(operators);
		this.activeOpNum = operators.length;
	}

	@Override
	public int runNext() {

		int ret = 0;
		long runningTime = System.currentTimeMillis();

		if (this.activeOpNum <= 0)
			return -1;// nothing to run

		for (int i = 0; i < this.operators.length; i++) {
			if (this.status[i]) {
				StreamOperator op = this.operators[i];
				op.preRun();
				ret = op.run();
				op.postRun();

				if (ret <= 0) {
					this.status[i] = false;
					this.activeOpNum--;
					if (this.activeOpNum <= 0)
						return -1;// nothing to do
				}
			}
		}

		runningTime = System.currentTimeMillis() - runningTime;
		ret = (int) (1000 - runningTime);
		if (ret < 0)
			System.out.println("Cannot Generate So fast Streams. Exiting");

		return ret;
	}

	@Override
	public String toString() {
		return "StreamGeneratorRoundRobinScheduler";
	}
}
