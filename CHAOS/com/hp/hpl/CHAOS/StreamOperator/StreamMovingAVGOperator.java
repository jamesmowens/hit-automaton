package com.hp.hpl.CHAOS.StreamOperator;

import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;
import com.hp.hpl.CHAOS.StreamData.StreamTupleCreator;

public class StreamMovingAVGOperator extends SingleInputStreamOperator {

	private static final long serialVersionUID = 1L;

	int length;
	double movingAVG = 0.0;
	double[] lastNElement;
	int front = 0, rear = 0;

	int inputCol = 0;
	int outputCol = 0;

	public StreamMovingAVGOperator(int operatorID, StreamQueue[] input,
			StreamQueue[] output) {
		super(operatorID, input, output);
	}

	@Override
	public int run(int maxDequeueSize) {
		int counter = 0;
		StreamQueue inputQueue = InputQueueArray[0];
		SchemaElement[] schArray = inputQueue.getSchema();

		for (int i = 0; i < maxDequeueSize; i++) {
			byte[] tuple = inputQueue.dequeue();
			if (tuple == null)
				break;
			counter++;

			double newValue = StreamAccessor.getDoubleCol(tuple, schArray,
					inputCol);
			double newMovingAVG = (movingAVG * length - this.peekN(0) + newValue)
					/ length;

			this.get();
			this.put(newValue);

			this.movingAVG = newMovingAVG;

			SchemaElement[] scheArray = this.getOutputQueueArray()[0]
					.getSchema();
			byte[] dest = StreamTupleCreator.makeEmptyTuple(scheArray);

			StreamAccessor.setMinTimestamp(dest, StreamAccessor
					.getMinTimestamp(tuple));
			StreamAccessor.setMaxTimestamp(dest, StreamAccessor
					.getMaxTimestamp(tuple));

			StreamAccessor.setCol(this.movingAVG, dest, scheArray,
					this.outputCol);
			com.hp.hpl.CHAOS.Queue.Utility.enqueueGroup(OutputQueueArray, dest);

		}

		return counter;
	}

	private void put(double k) {
		lastNElement[rear] = k;
		rear = ++rear % length;
	}

	public double get() {
		double tuple = lastNElement[front];
		front = ++front % length;
		return tuple;
	}

	public double peekN(int index) {
		return lastNElement[(front + index) % length];
	}

	@Override
	public void classVariableSetup(String key, String value) {
		if (key.equalsIgnoreCase("input")) {
			this.inputCol = Integer.valueOf(value);
		}
		if (key.equalsIgnoreCase("output")) {
			this.outputCol = Integer.valueOf(value);
		}
		if (key.equalsIgnoreCase("length")) {
			this.length = Integer.valueOf(value);
			this.lastNElement = new double[length];
		}

	}
}
