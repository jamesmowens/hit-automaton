package com.hp.hpl.CHAOS.StreamOperator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;
import com.hp.hpl.CHAOS.StreamData.StreamTupleCreator;
import com.hp.hpl.CHAOS.StreamWrapper.InputStreamWrapper;
import com.hp.hpl.CHAOS.StreamWrapper.TestFormat;

public class StreamSourceOperator extends SingleInputStreamOperator {

	private static final long serialVersionUID = 1L;

	int arrivalRate = 0;// per second.

	InputStreamWrapper in;

	public StreamSourceOperator(int operatorID, StreamQueue[] input,
			StreamQueue[] output) {
		super(operatorID, input, output);
		this.arrivalRate = 0;
		this.in = null;
	}

	public StreamSourceOperator(int operatorID, StreamQueue[] input,
			StreamQueue[] output, int arrivalRate, InputStreamWrapper in) {
		super(operatorID, input, output);
		this.arrivalRate = arrivalRate;
		this.in = in;
	}

	public InputStreamWrapper getIn() {
		return in;
	}

	public void setIn(InputStreamWrapper in) {
		this.in = in;
	}

	public int getArrivalRate() {
		return arrivalRate;
	}

	public void setArrivalRate(int arrivalRate) {
		this.arrivalRate = arrivalRate;
	}

	@Override
	public int init() {
		if (initialized)
			return 0;
		super.init();
		if (in.init() < 0) {
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
		in.finalizing();
		initialized = false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void classVariableSetup(String key, String value)
			throws SecurityException, ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException {
		// InputStreamWrapper
		if (key.equalsIgnoreCase("InputStreamWrapper")) {
			InputStreamWrapper streamOp = null;

			String className = "com.hp.hpl.CHAOS.StreamWrapper.".concat(value);

			Constructor<InputStreamWrapper>[] cstaE = (Constructor<InputStreamWrapper>[]) Class
					.forName(className).getConstructors();
			Constructor<InputStreamWrapper> constructst;

			boolean right_constructorst = false;
			int tempst = 0;

			while (!right_constructorst && tempst < cstaE.length) {
				constructst = cstaE[tempst];
				try {

					streamOp = (InputStreamWrapper) constructst.newInstance();

					right_constructorst = true;
				} catch (IllegalArgumentException e) {
					tempst++;
				}
			}
			this.in = streamOp;

		}

		if (key.equalsIgnoreCase("source")) {
			if (this.getIn() != null)
				this.getIn().setStreamFile(value);
		} else if (key.equalsIgnoreCase("loop"))
			this.getIn().setLoop(Boolean.parseBoolean(value));
		else if (key.equalsIgnoreCase("arrival")) {
			this.setArrivalRate(Integer.parseInt(value));
			if (this.getIn() instanceof TestFormat)
				// interval in microsecond
				((TestFormat) this.getIn()).setInterval(1000000 / this
						.getArrivalRate());
		}

	}

	@Override
	public int run() {
		return run(this.arrivalRate);
	}

	@Override
	// return the millisecond that the next run should be invoked after
	// for fast stream, the return will be interpreted as microsecond
	// Since the sleep() of thread does not have precise clock,
	// we decided to use batch load of tuples. That is, every second read in
	// like 10k tuples and sleep for the rest of the second.
	// So the actual arrival time is meaningless.
	// And no arrival pattern is used.
	public int run(int maxDequeueSize) {
		int send_interval = 0;
		SchemaElement[] schEleArray = this.OutputQueueArray[0].getSchema();
		for (int i = 0; i < maxDequeueSize; i++) {
			try {
				String input = in.readLine();
				if (input == null)
					return -1;
				String[] splitString = input.split("\\s+");
				send_interval = Integer.parseInt(splitString[0]);
				long timestamp = Long.parseLong(splitString[1]);

				byte[] tuple = StreamTupleCreator.makeTuple(splitString, 2,
						schEleArray);

				StreamAccessor.setMinTimestamp(tuple, timestamp);
				StreamAccessor.setMaxTimestamp(tuple, timestamp);
				if (com.hp.hpl.CHAOS.Queue.Utility.enqueueGroup(
						OutputQueueArray, tuple) == null)
					return -1;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for (StreamQueue sq : OutputQueueArray)
			sq.flush();

		return send_interval;
	}

}
