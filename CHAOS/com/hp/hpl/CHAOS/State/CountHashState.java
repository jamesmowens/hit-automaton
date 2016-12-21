package com.hp.hpl.CHAOS.State;

import com.hp.hpl.CHAOS.Queue.SingleReaderQueueArrayImp;
import com.hp.hpl.CHAOS.Queue.SingleReaderTupleKeyQueueArrayImp;
import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.Queue.StreamQueueGeneric;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;

public class CountHashState extends HashState {

	private static final long serialVersionUID = 1L;
	private int windowSize;
	transient private StreamQueueGeneric<TupleKey> keyQueue;

	public CountHashState(SchemaElement[] elementSchArray) {
		super(elementSchArray);
		this.windowSize = 0;
		this.keyQueue = new SingleReaderTupleKeyQueueArrayImp();
	}

	public CountHashState(SchemaElement[] elementSchArray, int keyIndex,
			int probeTupleIndex) {
		super(elementSchArray, keyIndex, probeTupleIndex);
		this.windowSize = 0;
		this.keyQueue = new SingleReaderTupleKeyQueueArrayImp();
	}

	public CountHashState(SchemaElement[] elementSchArray, int keyIndex,
			int probeTupleIndex, int windowSize) {
		super(elementSchArray, keyIndex, probeTupleIndex);
		this.windowSize = windowSize;
		this.keyQueue = new SingleReaderTupleKeyQueueArrayImp();
	}

	public StreamQueueGeneric<TupleKey> getKeyQueue() {
		return keyQueue;
	}

	public int getWindowSize() {
		return windowSize;
	}

	@Override
	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	@Override
	public int init() {
		if (initialized)
			return 0;
		super.init();
		this.keyQueue = new SingleReaderTupleKeyQueueArrayImp();
		initialized = true;
		return 0;
	}

	@Override
	public int getSize() {
		return keyQueue.getSize();
	}

	@Override
	public void insert(byte[] tuple) {
		elementSchArray[keyIndex].setTuple(tuple);
		byte[] keyChar = elementSchArray[keyIndex].extract();
		TupleKey key = new TupleKey(keyChar);
		if (queueHash.containsKey(key)) {
			queueHash.get(key).enqueue(tuple);
			keyQueue.enqueue(key);
		} else {
			StreamQueue newQueue = new SingleReaderQueueArrayImp(
					elementSchArray);
			queueHash.put(key, newQueue);
			newQueue.enqueue(tuple);
			keyQueue.enqueue(key);
		}
	}

	@Override
	public int purge(byte[] tuple) {

		int curStateSize = this.getSize();

		int ret = curStateSize - windowSize;

		while (curStateSize > windowSize) {

			TupleKey key = keyQueue.dequeue();
			queueHash.get(key).dequeue();
			curStateSize--;
		}
		return ret;

	}
}
