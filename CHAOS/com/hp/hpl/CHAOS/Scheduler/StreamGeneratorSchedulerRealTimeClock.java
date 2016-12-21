package com.hp.hpl.CHAOS.Scheduler;

import com.hp.hpl.CHAOS.StreamOperator.StreamOperator;

public class StreamGeneratorSchedulerRealTimeClock extends Scheduler {

	private int index;
	private int timeToSleepArray[];
	private int activeOpNum;

	private int lastSleepTime = 0;

	public StreamGeneratorSchedulerRealTimeClock(StreamOperator[] operators) {
		super(operators);
		this.activeOpNum = operators.length;
		this.timeToSleepArray = new int[operators.length];
		for (int i = 0; i < this.timeToSleepArray.length; i++)
			this.timeToSleepArray[i] = 0;
		this.index = 0;
	}

	public int getIndex() {
		return index;
	}

	public int[] getTimeToSleepArray() {
		return timeToSleepArray;
	}

	public int getActiveOpNum() {
		return activeOpNum;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setTimeToSleepArray(int[] timeToSleepArray) {
		this.timeToSleepArray = timeToSleepArray;
	}

	public void setActiveOpNum(int activeOpNum) {
		this.activeOpNum = activeOpNum;
	}

	@Override
	public void setOperators(StreamOperator[] operators) {
		super.setOperators(operators);
		this.activeOpNum = operators.length;
		this.timeToSleepArray = new int[operators.length];
		for (int i = 0; i < this.timeToSleepArray.length; i++)
			this.timeToSleepArray[i] = 0;
		this.index = 0;
	}

	@Override
	public int runNext() {

		int ret = 0;
		int retIndex = -1;

		if (this.activeOpNum <= 0)
			return -1;// nothing to run

		StreamOperator op = this.operators[index];
		op.preRun();
		ret = op.run(1);
		op.postRun();

		if (ret > 0) {
			int min = Integer.MAX_VALUE;
			for (int i = 0; i < index; i++) {
				if (!this.status[i])
					continue;
				this.timeToSleepArray[i] -= lastSleepTime;
				if (this.timeToSleepArray[i] < min) {
					retIndex = i;
					min = this.timeToSleepArray[i];
				}
			}

			this.timeToSleepArray[index] = ret;
			if (ret < min) {
				retIndex = index;
				min = ret;
			}

			for (int i = index + 1; i < this.timeToSleepArray.length; i++) {
				if (!this.status[i])
					continue;
				this.timeToSleepArray[i] -= lastSleepTime;
				if (this.timeToSleepArray[i] < min) {
					retIndex = i;
					min = this.timeToSleepArray[i];
				}
			}

			lastSleepTime = min;
			index = retIndex;
			return min;

		} else {

			this.status[index] = false;
			this.activeOpNum--;

			int min = Integer.MAX_VALUE;
			for (int i = 0; i < index; i++) {
				if (!this.status[i])
					continue;
				this.timeToSleepArray[i] -= lastSleepTime;
				if (this.timeToSleepArray[i] < min) {
					retIndex = i;
					min = this.timeToSleepArray[i];
				}
			}

			for (int i = index + 1; i < this.timeToSleepArray.length; i++) {
				if (!this.status[i])
					continue;
				this.timeToSleepArray[i] -= lastSleepTime;
				if (this.timeToSleepArray[i] < min) {
					retIndex = i;
					min = this.timeToSleepArray[i];
				}
			}

			lastSleepTime = min;
			index = retIndex;
			if (index < 0)
				return -1;// nothing to do
			else
				return min;
		}
	}

	@Override
	public String toString() {
		return "StreamGeneratorRoundRobinScheduler";
	}
}
