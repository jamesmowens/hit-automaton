package com.hp.hpl.CHAOS.StreamOperator;

import com.hp.hpl.CHAOS.Expression.BoolRetExp;
import com.hp.hpl.CHAOS.Expression.Expression;
import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;

public class StreamSelectOperator extends SingleInputStreamOperator {

	private static final long serialVersionUID = 1L;

	public StreamSelectOperator(int operatorID, StreamQueue[] input,
			StreamQueue[] output) {
		super(operatorID, input, output);
		this.expression = null;
	}

	public StreamSelectOperator(int operatorID, StreamQueue[] input,
			StreamQueue[] output, BoolRetExp expression) {
		super(operatorID, input, output);
		this.expression = expression;
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
			for (SchemaElement sch : schArray)
				sch.setTuple(tuple);
			if (((BoolRetExp) this.expression).eval()) {
				com.hp.hpl.CHAOS.Queue.Utility.enqueueGroup(OutputQueueArray,
						tuple);
				System.out.println(StreamAccessor.toString(tuple, schArray));
			}

		}
		return counter;
	}

	@Override
	public void setExpression(Expression expression)
			throws IllegalArgumentException {
		if (expression instanceof BoolRetExp)
			this.expression = expression;
		else {
			throw (new IllegalArgumentException());
		}
	}
}
