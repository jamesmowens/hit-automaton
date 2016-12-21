package com.hp.hpl.CHAOS.StreamOperator;

import com.hp.hpl.CHAOS.Queue.StreamQueue;

public abstract class SingleInputStreamOperator extends StreamOperator {

	public SingleInputStreamOperator(int operatorID, StreamQueue[] input,
			StreamQueue[] output) {
		super(operatorID, input, output);

	}
	
	@Override
	public int run() {
		return this.run((int)(InputQueueArray[0].getSize()));
	}
}
