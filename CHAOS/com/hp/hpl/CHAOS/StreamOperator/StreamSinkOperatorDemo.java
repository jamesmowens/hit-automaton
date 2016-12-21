package com.hp.hpl.CHAOS.StreamOperator;

import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;

public class StreamSinkOperatorDemo extends StreamSinkOperator {

	private static final long serialVersionUID = 1L;

	public StreamSinkOperatorDemo(int operatorID, StreamQueue[] input,
			StreamQueue[] output) {
		super(operatorID, input, output);
	}

	public StreamSinkOperatorDemo(int operatorID, StreamQueue[] input,
			StreamQueue[] output, String outputFileName) {
		super(operatorID, input, output, outputFileName);
	}

	@Override
	public int run(int maxDequeueSize) {
		int counter = 0;

		StreamQueue inputQueue = InputQueueArray[0];
		SchemaElement[] schArray = inputQueue.getSchema();

		for (int i = maxDequeueSize; i > 0; i--) {
			byte[] tuple = inputQueue.dequeue();
			if (tuple == null)
				break;
			counter++;
			out.println(StreamAccessor.toString(tuple, schArray));
		}
		return counter;
	}

}
