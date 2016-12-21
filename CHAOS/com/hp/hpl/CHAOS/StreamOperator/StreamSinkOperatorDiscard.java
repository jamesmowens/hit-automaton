package com.hp.hpl.CHAOS.StreamOperator;

import com.hp.hpl.CHAOS.Queue.StreamQueue;

public class StreamSinkOperatorDiscard extends StreamSinkOperator {

	private static final long serialVersionUID = 1L;

	public StreamSinkOperatorDiscard(int operatorID, StreamQueue[] input,
			StreamQueue[] output) {
		super(operatorID, input, output);
	}

	public StreamSinkOperatorDiscard(int operatorID, StreamQueue[] input,
			StreamQueue[] output, String outputFileName) {
		super(operatorID, input, output, outputFileName);
	}

	@Override
	public int run(int maxDequeueSize) {
		int counter = 0;

		StreamQueue inputQueue = InputQueueArray[0];

		for (int i = maxDequeueSize; i > 0; i--) {
			byte[] tuple = inputQueue.dequeue();
			if (tuple == null)
				break;
			counter++;
		}
		return counter;
	}

}
