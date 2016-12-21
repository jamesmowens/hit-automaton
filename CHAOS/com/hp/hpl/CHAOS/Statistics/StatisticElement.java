package com.hp.hpl.CHAOS.Statistics;

import java.io.Serializable;

import com.hp.hpl.CHAOS.StreamOperator.StreamOperator;

public abstract class StatisticElement implements Serializable{
	abstract public double getStat(StreamOperator op);
	abstract public void resetStat(StreamOperator op);
	abstract public void preRun(StreamOperator op);
	abstract public void postRun(StreamOperator op);
	
}
