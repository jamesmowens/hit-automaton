package com.hp.hpl.CHAOS.Scheduler;

import com.hp.hpl.CHAOS.StreamOperator.StreamOperator;

public class RoundRobinScheduler extends Scheduler {

	private int index;
	private boolean progressArray[];
	private int activeOpNum;

	public RoundRobinScheduler(StreamOperator[] operators) {
		super(operators);
		this.activeOpNum = operators.length;
		this.progressArray = new boolean[operators.length];
		for (int i = 0; i < this.progressArray.length; i++)
			this.progressArray[i] = true;
		this.index = 0;
	}

	public int getIndex() {
		return index;
	}

	public boolean[] getProgress() {
		return progressArray;
	}

	public int getActiveOpNum() {
		return activeOpNum;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setProgress(boolean[] progress) {
		this.progressArray = progress;
	}

	public void setActiveOpNum(int activeOpNum) {
		this.activeOpNum = activeOpNum;
	}

	@Override
	public void setOperators(StreamOperator[] operators) {
		super.setOperators(operators);
		this.activeOpNum = operators.length;
		this.progressArray = new boolean[operators.length];
		for (int i = 0; i < this.progressArray.length; i++)
			this.progressArray[i] = true;
		this.index = 0;
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
		// System.out.println("index:"+index+":ret:"+ret);
		if (ret > 0) {
			this.progressArray[index] = true;
			index = (index + 1) % this.operators.length;

			return ret;
		} else if (ret == 0) {
			boolean progress = false;
			for (boolean b : this.progressArray)
				progress = progress || b;

			this.progressArray[index] = false;
			index = (index + 1) % this.operators.length;

			if (progress) {
				return 0;
			} else
				return -2;// to wait
		} else {// (ret < 0)
			boolean progress = false;
			for (boolean b : this.progressArray)
				progress = progress || b;

			this.status[index] = false;
			this.activeOpNum--;

			this.progressArray[index] = false;
			index = (index + 1) % this.operators.length;

			if (progress) {
				return 0;
			} else
				return -2;// to wait
		}

	}

	@Override
	public String toString() {
		return "RoundRobinScheduler";
	}

	@Override
	public void reset() {
		for (int i = 0; i < this.progressArray.length; i++)
			this.progressArray[i] = this.status[i];
	}
}
