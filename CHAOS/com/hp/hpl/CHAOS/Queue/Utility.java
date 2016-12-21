package com.hp.hpl.CHAOS.Queue;

import com.hp.hpl.CHAOS.StreamData.StreamAccessor;

public class Utility {
	static int value = 0;

	public static int getUniqueInt() {
		if (value == Integer.MAX_VALUE)
			value = 0;
		value++;
		return value;
	}

	public static byte[] enqueueGroup(StreamQueue[] sqArray, byte[] tuple) {
		for (StreamQueue sq : sqArray)
			if (sq.enqueue(tuple) == null)
				return null;
		return tuple;
	}

	public static int dequeueGroup(StreamQueue[] sqArray) {
		int queueWithMinTimestampIndex = -1;

		byte[] minTuple = null;
		byte[] tuple = null;

		for (int i = 0; i < sqArray.length; i++) {
			tuple = sqArray[i].peek();
			if (tuple == null)
				return -1;
			if (minTuple == null
					|| StreamAccessor.getMaxTimestamp(tuple) <= StreamAccessor
							.getMaxTimestamp(minTuple)) {
				queueWithMinTimestampIndex = i;
				minTuple = tuple;
			}
		}
		return queueWithMinTimestampIndex;

	}
}
