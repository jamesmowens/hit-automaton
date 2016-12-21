package com.hp.hpl.CHAOS.Statistics;

import com.hp.hpl.CHAOS.StreamOperator.StreamOperator;

public class ElapseTime extends StatisticElement {
	private static final long serialVersionUID = 1L;
	transient long totalElapseTime;
	transient long startTimestamp, endTimestamp;

	public ElapseTime() {
		super();
		this.totalElapseTime = 0;
		this.startTimestamp = 0;
		this.endTimestamp = 0;
	}

	public long getTotalElapseTime() {
		return totalElapseTime;
	}

	@Override
	public double getStat(StreamOperator op) {
		return this.totalElapseTime;
	}

	@Override
	public void postRun(StreamOperator op) {
		this.endTimestamp = System.currentTimeMillis();
		this.totalElapseTime += this.endTimestamp - this.startTimestamp;

	}

	@Override
	public void preRun(StreamOperator op) {
		this.startTimestamp = System.currentTimeMillis();
	}

	@Override
	public void resetStat(StreamOperator op) {
		this.totalElapseTime = 0;
		this.startTimestamp = 0;
		this.endTimestamp = 0;
	}

}
