package com.hp.hpl.CHAOS.StreamOperator;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import com.hp.hpl.CHAOS.Expression.Expression;
import com.hp.hpl.CHAOS.QueryPlan.RunTimeBlock;
import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.State.StreamState;
import com.hp.hpl.CHAOS.Statistics.StatisticElement;
import com.hp.hpl.CHAOS.StreamData.SchemaMap;

/**
 * @author liumo
 * 
 */
public abstract class StreamOperator implements Serializable, RunTimeBlock {

	private static final long serialVersionUID = 1L;

	int operatorID;

	boolean initialized = false;

	StreamQueue[] InputQueueArray = null;
	protected StreamQueue[] OutputQueueArray = null;

	StatisticElement[] statisticArray = null;

	SchemaMap sMap = null;
	Expression expression = null;

	StreamState[] stateArray = null;

	public StreamOperator(int operatorID, StreamQueue[] input,
			StreamQueue[] output) {
		super();
		this.operatorID = operatorID;
		this.InputQueueArray = input;
		this.OutputQueueArray = output;
	}

	// ******************************************

	public void preRun() {
		for (StatisticElement se : statisticArray)
			se.preRun(this);
	}

	public void postRun() {
		for (StatisticElement se : statisticArray)
			se.postRun(this);
	}

	// for class variable settings, called by query plan generator
	// Should be overloaded by subclass if needed
	public void classVariableSetup(String key, String value)
			throws SecurityException, ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException {
	}

	// for initialize. called by executor
	// Should be override by subclass if needed
	public int init() {
		if (initialized)
			return 0;

		if (this.InputQueueArray != null)
			for (StreamQueue sq : this.InputQueueArray)
				if (sq.init() < 0)
					return -1;
		if (this.OutputQueueArray != null)
			for (StreamQueue sq : this.OutputQueueArray)
				if (sq.init() < 0)
					return -1;
		if (this.stateArray != null)
			for (StreamState ss : this.stateArray)
				if (ss.init() < 0)
					return -1;

		initialized = true;
		return 0;
	}

	// for finalizing. called by executor
	// Should be override by subclass if needed
	public void finalizing() {
		if (!initialized)
			return;

		if (this.InputQueueArray != null)
			for (StreamQueue sq : this.InputQueueArray)
				sq.finalizing();
		if (this.OutputQueueArray != null)
			for (StreamQueue sq : this.OutputQueueArray)
				sq.finalizing();
		if (this.stateArray != null)
			for (StreamState ss : this.stateArray)
				ss.finalizing();

		initialized = false;
	}

	// the meaning of the returned integer:
	// >0: time to sleep (millisecond or microsecond)
	// >0: tuple processed
	// <0: to wait()
	// <0: something wrong
	abstract public int run(int maxDequeueSize);

	abstract public int run();

	// ****************************************

	public int getOperatorID() {
		return operatorID;
	}

	public StreamQueue[] getInputQueueArray() {
		return InputQueueArray;
	}

	public StreamQueue[] getOutputQueueArray() {
		return OutputQueueArray;
	}

	public StatisticElement[] getStatisticArray() {
		return statisticArray;
	}

	public SchemaMap getSMap() {
		return sMap;
	}

	public Expression getExpression() {
		return expression;
	}

	public StreamState[] getStateArray() {
		return stateArray;
	}

	public void setOperatorID(int operatorID) {
		this.operatorID = operatorID;
	}

	public void setInputQueueArray(StreamQueue[] inputQueueArray) {
		InputQueueArray = inputQueueArray;
	}

	public void setOutputQueueArray(StreamQueue[] outputQueueArray) {
		OutputQueueArray = outputQueueArray;
	}

	public void setStatisticArray(StatisticElement[] statisticArray) {
		this.statisticArray = statisticArray;
	}

	public void setSMap(SchemaMap map) {
		sMap = map;
	}

	public void setExpression(Expression expression)
			throws IllegalArgumentException {
		this.expression = expression;
	}

	public void setStateArray(StreamState[] stateArray) {
		this.stateArray = stateArray;
	}

	public void setStateIthArray(int index, StreamState stateArray) {

		this.stateArray[index] = stateArray;
	}

	public void setInputQueues(StreamQueue[] newinputQueueArray) {
		if (this.InputQueueArray == null)
			InputQueueArray = newinputQueueArray;
		else {
			ArrayList<StreamQueue> streamQueues = new ArrayList<StreamQueue>();

			int arrayIndex = 0;
			while (arrayIndex < this.InputQueueArray.length) {
				streamQueues.add(this.InputQueueArray[arrayIndex]);
				arrayIndex++;
			}
			int newAdd = newinputQueueArray.length - 1;
			while (newAdd >= 0) {
				// streamQueues.add(0, newinputQueueArray[newAdd]);
				streamQueues.add(newinputQueueArray[newAdd]);
				newAdd--;
			}

			StreamQueue[] queues = (StreamQueue[]) streamQueues
					.toArray(new StreamQueue[0]);
			this.InputQueueArray = queues;

		}
	}

	public void setOutputQueues(StreamQueue[] newOutputQueueArray) {
		if (this.OutputQueueArray == null)
			OutputQueueArray = newOutputQueueArray;
		else {
			ArrayList<StreamQueue> streamQueues = new ArrayList<StreamQueue>();
			int arrayIndex = 0;
			while (arrayIndex < this.OutputQueueArray.length) {
				streamQueues.add(this.OutputQueueArray[arrayIndex]);
				arrayIndex++;
			}
			int newAdd = newOutputQueueArray.length - 1;
			while (newAdd >= 0) {
				streamQueues.add(newOutputQueueArray[newAdd]);
				newAdd--;
			}

			StreamQueue[] queues = (StreamQueue[]) streamQueues
					.toArray(new StreamQueue[0]);
			this.OutputQueueArray = queues;

		}
	}

	public void setOutputQueue(StreamQueue queue, int index) {
		if (this.OutputQueueArray == null
				|| this.OutputQueueArray.length < index + 1)
			return;
		this.OutputQueueArray[index] = queue;
	}

	public void setInputQueue(StreamQueue queue, int index) {
		if (this.InputQueueArray == null
				|| this.InputQueueArray.length < index + 1)
			return;
		this.InputQueueArray[index] = queue;
	}

	public String toString() {
		// String sep = "|";
		// String sep2 = ";";
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(this.getClass().getSimpleName());
		// strBuf.append(sep);
		// strBuf.append("InputQueues:");
		// if (this.InputQueueArray != null) {
		// for (StreamQueue sq : this.InputQueueArray) {
		// strBuf.append(sq.toString());
		// strBuf.append(sep2);
		// }
		//
		// }
		//
		// strBuf.append(sep);
		//
		// if (this.OutputQueueArray != null) {
		// strBuf.append("OutputQueues:");
		// for (StreamQueue sq : this.OutputQueueArray) {
		// strBuf.append(sq.toString());
		// strBuf.append(sep2);
		// }
		// }
		//
		// if (this.expression != null) {
		// strBuf.append(sep);
		// strBuf.append("Expression:");
		// strBuf.append(this.getExpression().toString());
		// }
		//
		return strBuf.toString();
	}

}
