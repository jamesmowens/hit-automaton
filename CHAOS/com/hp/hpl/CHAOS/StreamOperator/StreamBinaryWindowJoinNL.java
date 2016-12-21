package com.hp.hpl.CHAOS.StreamOperator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


import com.hp.hpl.CHAOS.Expression.BoolRetExp;
import com.hp.hpl.CHAOS.Expression.Expression;
import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.State.StreamState;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.SchemaMap;

public class StreamBinaryWindowJoinNL extends MultiInputStreamOperator {

	private static final long serialVersionUID = 1L;
	private int visitIndex = 0;
	

	public StreamBinaryWindowJoinNL(int operatorID, StreamQueue[] input,
			StreamQueue[] output) {
		super(operatorID, input, output);
		this.stateArray = null;
		this.expression = null;
		this.sMap = null;
	}

	public StreamBinaryWindowJoinNL(int operatorID, StreamQueue[] input,
			StreamQueue[] output, StreamState[] stateArray,
			BoolRetExp expression, SchemaMap smap) {
		super(operatorID, input, output);
		this.stateArray = stateArray;
		this.expression = expression;
		this.sMap = smap;
	}

	@Override
	public void classVariableSetup(String key, String value)
			throws InstantiationException, IllegalAccessException,
			InvocationTargetException, SecurityException,
			ClassNotFoundException {
		if (key.equalsIgnoreCase("className")) {

			String statesname = "com.hp.hpl.CHAOS.State.".concat(value);

			Constructor<StreamState>[] cstaE = (Constructor<StreamState>[]) Class
					.forName(statesname).getConstructors();
			Constructor<StreamState> constructst;

			boolean right_constructorst = false;
			int tempst = 0;

			while (!right_constructorst && tempst < cstaE.length) {
				constructst = cstaE[tempst];
				try {
					SchemaElement[] schArray = this.getInputQueueArray()[visitIndex]
							.getSchema();

					Object[] params = { schArray };

					
					StreamState staEle = (StreamState) constructst
							.newInstance(params);

					this.setStateIthArray(visitIndex, staEle);
					
					right_constructorst = true;
				} catch (IllegalArgumentException e) {
					tempst++;
				}
			}
		} else if (key.equalsIgnoreCase("windowSize")) {
			
			this.stateArray[visitIndex].setWindowSize(Long.parseLong(value));
			this.stateArray[visitIndex].setWindowSize(Integer.parseInt(value));
			
			visitIndex++;
		}

	}

	@Override
	public int run(int maxDequeueSize) {
		int counter = 0;

		for (int i = maxDequeueSize; i > 0; i--) {

			int queueIndex = com.hp.hpl.CHAOS.Queue.Utility
					.dequeueGroup(this.InputQueueArray);

			if (queueIndex < 0)
				return counter;

			counter++;

			byte[] tuple = this.InputQueueArray[queueIndex].dequeue();

			this.stateArray[queueIndex].insert(tuple);
			this.stateArray[(queueIndex + 1) & 0x01].purge(tuple);
			this.stateArray[(queueIndex + 1) & 0x01].probe(
					(BoolRetExp) this.expression, tuple,
					this.InputQueueArray[queueIndex].getSchema(), sMap,
					this.OutputQueueArray);
		}
		return counter;
	}

	@Override
	public void setExpression(Expression expression) {
		if (expression instanceof BoolRetExp)
			this.expression = expression;
		else {
			throw (new IllegalArgumentException());
		}
	}
}
