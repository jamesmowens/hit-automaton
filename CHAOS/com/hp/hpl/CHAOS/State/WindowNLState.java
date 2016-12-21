package com.hp.hpl.CHAOS.State;

import com.hp.hpl.CHAOS.StreamData.SchemaElement;

import com.hp.hpl.CHAOS.StreamData.StreamAccessor;

public class WindowNLState extends NLState {
	private static final long serialVersionUID = 1L;
	long windowSize;

	public WindowNLState(SchemaElement[] array) {
		super(array);
		this.windowSize = 0;
	}

	public WindowNLState(SchemaElement[] array, long windowSize) {
		super(array);
		this.windowSize = windowSize;
	}

	@Override
	public int purge(byte[] tuple) {
		return purgeTail(tuple);
	}

	private int purgeTail(byte[] tuple) {

		int counter = 0;
		long maxTime = StreamAccessor.getMaxTimestamp(tuple) - windowSize;
		while (StreamAccessor.getMaxTimestamp(queue.peek()) < maxTime) {
			queue.dequeue();
			counter++;
		}
		return counter;
	}

	@SuppressWarnings("unused")
	private int purgeBinarySearch(byte[] tuple) {

		int counter, dequeueNum = 0;
		long maxTime = StreamAccessor.getMaxTimestamp(tuple) - windowSize - 1;

		counter = queue.purgeNumber(maxTime);
		dequeueNum = counter;

		while (dequeueNum > 0) {
			queue.dequeue();
			dequeueNum--;
		}

		return counter;
	}
	
	

	public long getWindowSize() {
		return windowSize;
	}
	
	@Override
	public void setWindowSize(long windowSize) {
		this.windowSize = windowSize;
	}

}
