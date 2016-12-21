package com.hp.hpl.CHAOS.Statistics;

import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamOperator.StreamOperator;

//runtime online selectivity. Aggregated selectivity can be calculated offline
public class Selectivity extends StatisticElement {

	private static final long serialVersionUID = 1L;

	public Selectivity() {
		super();
	}

	@Override
	public double getStat(StreamOperator op) {
		// now for join and single input op only. Should not use for union.
		int totalDeltaIn = 1;
		int totalDeltaOut = 0;

		StreamQueue[] opInputQueueArray = op.getInputQueueArray();
		for (StreamQueue sq : opInputQueueArray)
			totalDeltaIn *= sq.getDeltaDequeued();

		StreamQueue[] opOutputQueueArray = op.getOutputQueueArray();
		totalDeltaOut += opOutputQueueArray[0].getDeltaEnqueued();

		return totalDeltaOut / totalDeltaIn;
	}

	@Override
	public void postRun(StreamOperator op) {
	}

	@Override
	public void preRun(StreamOperator op) {
	}

	@Override
	public void resetStat(StreamOperator op) {
		StreamQueue[] opInputQueueArray = op.getInputQueueArray();
		for (StreamQueue sq : opInputQueueArray)
			sq.clearDeltaStat();
		StreamQueue[] opOutputQueueArray = op.getOutputQueueArray();
		for (StreamQueue sq : opOutputQueueArray)
			sq.clearDeltaStat();
	}

}
