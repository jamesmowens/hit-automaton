package com.hp.hpl.CHAOS.StreamOperator;

import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.SchemaMap;
import com.hp.hpl.CHAOS.StreamData.StreamTupleCreator;

public class StreamProjectOperator extends SingleInputStreamOperator {

	private static final long serialVersionUID = 1L;

	public StreamProjectOperator(int operatorID, StreamQueue[] input,
			StreamQueue[] output) {
		super(operatorID, input, output);
		this.sMap = null;
	}

	public StreamProjectOperator(int operatorID, StreamQueue[] input,
			StreamQueue[] output, SchemaMap smap) {
		super(operatorID, output, output);
		this.sMap = smap;
	}

	@Override
	public int run(int maxDequeueSize) {

		int counter = 0;

		StreamQueue inputQueue = InputQueueArray[0];
		SchemaElement[] schInArray = inputQueue.getSchema();

		SchemaElement[] schOutArray = OutputQueueArray[0].getSchema();

		for (int i = maxDequeueSize; i > 0; i--) {
			byte[] tuple = inputQueue.dequeue();
			if (tuple == null)
				break;
			counter++;
			byte[] dest = StreamTupleCreator.makeEmptyTuple(schOutArray);
			StreamTupleCreator.tupleCopyWithTimestamp(dest, tuple, schInArray,
					sMap);
			com.hp.hpl.CHAOS.Queue.Utility.enqueueGroup(OutputQueueArray, dest);
		}
		return counter;
	}
}
