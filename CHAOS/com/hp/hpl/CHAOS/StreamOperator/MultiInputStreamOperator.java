package com.hp.hpl.CHAOS.StreamOperator;

import com.hp.hpl.CHAOS.Queue.StreamQueue;

public abstract class MultiInputStreamOperator extends StreamOperator {

	public MultiInputStreamOperator(int operatorID, StreamQueue[] input,
			StreamQueue[] output) {
		super(operatorID, input, output);

	}

	@Override
	public int run() {
		int queueSizeTotal = 0;
		for (StreamQueue sq : this.InputQueueArray)
			queueSizeTotal += (int)sq.getSize();
		return run(queueSizeTotal);
	}

}
