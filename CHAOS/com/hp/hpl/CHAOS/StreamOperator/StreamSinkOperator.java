package com.hp.hpl.CHAOS.StreamOperator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;

import com.hp.hpl.CHAOS.Queue.StreamQueue;

public class StreamSinkOperator extends SingleInputStreamOperator {

	private static final long serialVersionUID = 1L;

	transient PrintWriter out = null;
	String outputFileName = null;
	String queryName = null;

	public StreamSinkOperator(int operatorID, StreamQueue[] input,
			StreamQueue[] output) {
		super(operatorID, input, output);
		this.outputFileName = null;
	}

	public StreamSinkOperator(int operatorID, StreamQueue[] input,
			StreamQueue[] output, String outputFileName) {
		super(operatorID, input, output);
		this.outputFileName = outputFileName;
	}

	public String getOutputFileName() {
		return outputFileName;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	public String getQueryName() {
		return queryName;
	}

	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}

	@Override
	public int init() {
		if (initialized)
			return 0;
		super.init();
		if (this.outputFileName == null) {
			System.out.println("Error: Must have output file name.");
			initialized = false;
			return -1;
		} else
			try {
				out = new PrintWriter(new BufferedWriter(new FileWriter(
						this.outputFileName)));
			} catch (Exception e) {
				e.printStackTrace();
				initialized = false;
				return -1;
			}

		initialized = true;
		return 0;
	}

	@Override
	public void finalizing() {
		if (!initialized)
			return;
		super.finalizing();
		this.out.flush();
		this.out.close();

		initialized = false;
	}

	@Override
	public void classVariableSetup(String key, String value) {
		if (key.equalsIgnoreCase("output")) {
			this.setQueryName(value);
			this.setOutputFileName(value.concat(".txt"));
		}

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
			out.println(tuple);
		}
		return counter;
	}

}
