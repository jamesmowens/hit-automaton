package com.hp.hpl.CHAOS.State;

import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import com.hp.hpl.CHAOS.Expression.BoolRetExp;
import com.hp.hpl.CHAOS.Queue.SingleReaderQueueArrayImp;
import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.SchemaMap;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;
import com.hp.hpl.CHAOS.StreamData.StreamTupleCreator;

public abstract class HashState extends StreamState {

	transient protected Hashtable<TupleKey, StreamQueue> queueHash;
	protected SchemaElement[] elementSchArray;
	protected int keyIndex;
	protected int probeTupleIndex;

	public HashState(SchemaElement[] elementSchArray) {
		super();
		this.queueHash = new Hashtable<TupleKey, StreamQueue>();
		this.elementSchArray = elementSchArray;
		this.keyIndex = 0;
		this.probeTupleIndex = 0;
	}

	public HashState(SchemaElement[] elementSchArray, int keyIndex,
			int probeTupleIndex) {
		super();
		this.queueHash = new Hashtable<TupleKey, StreamQueue>();
		this.elementSchArray = elementSchArray;
		this.keyIndex = keyIndex;
		this.probeTupleIndex = probeTupleIndex;
	}

	public Hashtable<TupleKey, StreamQueue> getQueueHash() {
		return queueHash;
	}

	public SchemaElement[] getElementSchArray() {
		return elementSchArray;
	}
	
	@Override
	public void setKeyIndex(int keyIndex) {
		this.keyIndex = keyIndex;
	}

	public int getKeyIndex() {
		return keyIndex;
	}

	public int getProbeTupleIndex() {
		return probeTupleIndex;
	}

	@Override
	public void setProbeTupleIndex(int probeTupleIndex) {
		this.probeTupleIndex = probeTupleIndex;
	}

	public Collection<StreamQueue> getQueueList() {
		return queueHash.values();
	}

	@Override
	public int init() {
		if (initialized)
			return 0;
		super.init();
		this.queueHash = new Hashtable<TupleKey, StreamQueue>();
		initialized = true;
		return 0;
	}

	@Override
	public int getSize() {
		int ret = 0;
		for (StreamQueue sq : queueHash.values())
			ret += sq.getSize();
		return ret;
	}

	@Override
	public void insert(byte[] tuple) {
		elementSchArray[keyIndex].setTuple(tuple);
		byte[] keyChar = elementSchArray[keyIndex].extract();
		TupleKey key = new TupleKey(keyChar);
		if (queueHash.containsKey(key))
			queueHash.get(key).enqueue(tuple);
		else {
			StreamQueue newQueue = new SingleReaderQueueArrayImp(
					elementSchArray);
			queueHash.put(key, newQueue);
			newQueue.enqueue(tuple);
		}
	}

	@Override
	public void probe(BoolRetExp bool, byte[] tuple, SchemaElement[] array,
			SchemaMap map, StreamQueue outputQ) {

		array[this.probeTupleIndex].setTuple(tuple);
		byte[] keyChar = array[this.probeTupleIndex].extract();
		TupleKey key = new TupleKey(keyChar);

		if (queueHash.containsKey(key)) {

			StreamQueue queue = queueHash.get(key);

			for (Iterator<byte[]> it = queue.getIterator(); it.hasNext();) {

				byte[] stateTuple = it.next();

				byte[] dest = StreamTupleCreator.makeEmptyTuple(outputQ
						.getSchema());

				StreamTupleCreator.tupleCopy(dest, tuple, array, map);

				StreamTupleCreator.tupleCopy(dest, stateTuple, queue
						.getSchema(), map);

				setTimestamp(tuple, stateTuple, dest);

				outputQ.enqueue(dest);
			}

		}

	}

	@Override
	public void probe(BoolRetExp bool, byte[] tuple, SchemaElement[] array,
			SchemaMap map, StreamQueue[] outputQA) {

		array[this.probeTupleIndex].setTuple(tuple);
		byte[] keyChar = array[this.probeTupleIndex].extract();
		TupleKey key = new TupleKey(keyChar);

		if (queueHash.containsKey(key)) {

			StreamQueue queue = queueHash.get(key);

			for (Iterator<byte[]> it = queue.getIterator(); it.hasNext();) {

				byte[] stateTuple = it.next();

				byte[] dest = StreamTupleCreator.makeEmptyTuple(outputQA[0]
						.getSchema());

				StreamTupleCreator.tupleCopy(dest, tuple, array, map);

				StreamTupleCreator.tupleCopy(dest, stateTuple, queue
						.getSchema(), map);

				setTimestamp(tuple, stateTuple, dest);

				for (StreamQueue outputQ : outputQA)
					outputQ.enqueue(dest);
			}

		}

	}

	public class TupleKey {
		private byte[] content;

		TupleKey(byte[] content) {
			this.content = content;
		}

		byte[] getContent() {
			return this.content;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;

			if (!(o instanceof TupleKey))
				return false;

			TupleKey other = (TupleKey) o;
			return Arrays.equals(this.content, other.content);

		}

		@Override
		public int hashCode() {
			int hashCode = 1;
			for (byte b : content)
				hashCode = 31 * hashCode + b;
			return hashCode;
		}
	}

	@Override
	public String toString() {
		StringBuffer strBuf = new StringBuffer();

		for (StreamQueue sq : getQueueList()) {
			for (Iterator<byte[]> it = sq.getIterator(); it.hasNext();)
				strBuf.append(StreamAccessor
						.toString(it.next(), sq.getSchema())
						+ "\n");
		}
		return strBuf.toString();
	}

}
