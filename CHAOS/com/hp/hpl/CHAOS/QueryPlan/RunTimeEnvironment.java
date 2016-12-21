package com.hp.hpl.CHAOS.QueryPlan;

import java.io.Serializable;
import java.util.ArrayList;

import com.hp.hpl.CHAOS.Network.NetworkOutputQueue;
import com.hp.hpl.CHAOS.Queue.LogQueue;
import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamOperator.StreamOperator;

public class RunTimeEnvironment implements Serializable, RunTimeBlock {
	private static final long serialVersionUID = 1L;

	String reName = "";

	StreamOperator[] operators;

	// for NetworkTupleReciever
	transient StreamQueue[] inputBoundaryQueues;

	// for StreamEngineExecutor and StreamGeneratorExecutor
	transient NetworkOutputQueue[] outputBoundaryQueues;

	transient LogQueue[] logQueues;

	transient boolean initialized = false;

	public RunTimeEnvironment(StreamOperator[] operators, String reName) {
		super();
		this.operators = operators;
		this.reName = reName;
	}

	public StreamOperator[] getOperators() {
		return operators;
	}

	public StreamQueue[] getInputBoundaryQueues() {
		return inputBoundaryQueues;
	}

	public NetworkOutputQueue[] getOutputBoundaryQueues() {
		return outputBoundaryQueues;
	}

	public void setOperators(StreamOperator[] operators) {
		this.operators = operators;
	}

	public void setInputBoundaryQueues(StreamQueue[] inputBoundaryQueues) {
		this.inputBoundaryQueues = inputBoundaryQueues;
	}

	public void setOutputBoundaryQueues(
			NetworkOutputQueue[] outputBoundaryQueues) {
		this.outputBoundaryQueues = outputBoundaryQueues;
	}

	public LogQueue[] getLogQueues() {
		return logQueues;
	}

	public void setLogQueues(LogQueue[] logQueues) {
		this.logQueues = logQueues;
	}

	public String getReName() {
		return reName;
	}

	public void setReName(String reName) {
		this.reName = reName;
	}

	public synchronized int init() {
		if (initialized)
			return 0;
		for (StreamOperator op : operators)
			if (op.init() < 0)
				return -1;
		// queues should be all set now

		this.inputBoundaryQueues = new StreamQueue[] {};
		ArrayList<StreamQueue> inQueueArray = new ArrayList<StreamQueue>();
		ArrayList<StreamQueue> outQueueArray = new ArrayList<StreamQueue>();
		for (StreamOperator op : this.getOperators()) {
			if (op.getInputQueueArray() != null)
				for (StreamQueue sq : op.getInputQueueArray())
					inQueueArray.add(sq);
			if (op.getOutputQueueArray() != null)
				for (StreamQueue sq : op.getOutputQueueArray())
					outQueueArray.add(sq);
		}
		inQueueArray.removeAll(outQueueArray);
		this.inputBoundaryQueues = inQueueArray
				.toArray(this.inputBoundaryQueues);

		this.outputBoundaryQueues = new NetworkOutputQueue[] {};
		ArrayList<NetworkOutputQueue> outputQueueArray = new ArrayList<NetworkOutputQueue>();
		for (StreamOperator op : this.getOperators())
			if (op.getOutputQueueArray() != null)
				for (StreamQueue sq : op.getOutputQueueArray())
					if (sq instanceof NetworkOutputQueue)
						outputQueueArray.add((NetworkOutputQueue) sq);
		this.outputBoundaryQueues = outputQueueArray
				.toArray(this.outputBoundaryQueues);

		this.logQueues = new LogQueue[] {};
		ArrayList<LogQueue> logQueueArray = new ArrayList<LogQueue>();
		for (StreamOperator op : this.getOperators())
			if (op.getOutputQueueArray() != null)
				for (StreamQueue sq : op.getOutputQueueArray())
					if (sq instanceof LogQueue)
						if (!((LogQueue) sq).isAlreadyLogged()) {
							logQueueArray.add((LogQueue) sq);
							((LogQueue) sq).setAlreadyLogged(true);
						}
		for (StreamQueue sq : this.inputBoundaryQueues)
			if (sq instanceof LogQueue && !logQueueArray.contains(sq))
				if (!((LogQueue) sq).isAlreadyLogged()) {
					logQueueArray.add((LogQueue) sq);
					((LogQueue) sq).setAlreadyLogged(true);
				}

		this.logQueues = logQueueArray.toArray(this.logQueues);

		initialized = true;
		return 0;
	}

	public void finalizing() {
		if (!initialized)
			return;
		for (StreamOperator op : operators)
			op.finalizing();
		// queues should be all set now
		initialized = false;
	}
}
