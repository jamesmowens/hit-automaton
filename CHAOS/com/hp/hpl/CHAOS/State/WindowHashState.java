package com.hp.hpl.CHAOS.State;

import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;

public class WindowHashState extends HashState {
	private static final long serialVersionUID = 1L;
	long windowSize;

	public WindowHashState(SchemaElement[] elementSchArray) {
		super(elementSchArray);
		this.windowSize = 0;
	}

	public WindowHashState(SchemaElement[] elementSchArray, int keyIndex,
			int probeTupleIndex) {
		super(elementSchArray, keyIndex, probeTupleIndex);
		this.windowSize = 0;
	}

	public WindowHashState(SchemaElement[] elementSchArray, int keyIndex,
			int probeTupleIndex, long windowSize) {
		super(elementSchArray, keyIndex, probeTupleIndex);
		this.windowSize = windowSize;
	}

	public long getWindowSize() {
		return windowSize;
	}
	
	@Override
	public void setWindowSize(long windowSize) {
		this.windowSize = windowSize;
	}

	@Override
	// by window size purge
	public int purge(byte[] tuple) {

		return purgeTail(tuple);

	}

	private int purgeTail(byte[] tuple) {

		int counter = 0;
		long maxTime = StreamAccessor.getMaxTimestamp(tuple) - windowSize;

		for (StreamQueue sq : getQueueList()) {
			while (StreamAccessor.getMaxTimestamp(sq.peek()) < maxTime) {
				sq.dequeue();
				counter++;
			}
		}
		return counter;
	}

	@SuppressWarnings("unused")
	private int purgeBinarySearch(byte[] tuple) {

		int counter = 0;
		int dequeueNum = 0;

		long maxTime = StreamAccessor.getMaxTimestamp(tuple) - windowSize - 1;

		for (StreamQueue sq : getQueueList()) {
			dequeueNum = sq.purgeNumber(maxTime);
			counter += dequeueNum;
			while (dequeueNum > 0) {
				sq.dequeue();
				dequeueNum--;
			}
		}
		return counter;

	}

}
