package com.hp.hpl.CHAOS.Rewriting;

import java.io.Serializable;
import com.hp.hpl.CHAOS.QueryPlan.RunTimeBlock;
import com.hp.hpl.CHAOS.Queue.SingleReaderEventPointerQueueArrayImp;
import com.hp.hpl.CHAOS.Queue.SingleReaderEventQueueArrayImp;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;
import com.hp.hpl.CHAOS.Queue.Utility;

public class EventActiveInstanceQueue implements Serializable, RunTimeBlock {

	private static final long serialVersionUID = 1L;

	/** The schema of this queue. */
	public SchemaElement[] schema;

	/** Each queue in a query plan has a unique queue id */
	public int queueID;

	/** Each stack in a query plan has a type */
	public String stackType;

	/** size of the pointer array */
	public int pointerSize;

	transient private byte[][] defaultPointerArray;

	transient public boolean initialized = false;

	/** queue for pointer array */
	transient public SingleReaderEventPointerQueueArrayImp pointerQueue;

	/** queue for event array */
	transient public SingleReaderEventQueueArrayImp eventQueue;

	public EventActiveInstanceQueue(SchemaElement[] schema, int pointerSize,
			String stackType) {
		this.queueID = Utility.getUniqueInt();
		this.schema = schema;
		this.pointerSize = pointerSize;
		this.defaultPointerArray = new byte[this.pointerSize][];
		for (int i = 0; i < this.pointerSize; i++)
			this.defaultPointerArray[i] = null;
		this.pointerQueue = new SingleReaderEventPointerQueueArrayImp();
		this.eventQueue = new SingleReaderEventQueueArrayImp(schema);
		this.stackType = stackType;
	}

	/* begin of important functions */

	// if push() returns null, means push failed.
	public byte[] push(byte[] tuple, byte[][] pointerArray) {
		return this.enqueue(tuple, pointerArray);
	}

	// if push() returns null, means push failed.
	public byte[] push(byte[] tuple) {
		return this.enqueue(tuple);
	}

	public byte[] pop(byte[][] pointerArray) {
		return this.dequeueLast(pointerArray);
	}

	// if enqueue() returns null, means enqueue failed.
	public byte[] enqueue(byte[] tuple, byte[][] pointerArray) {
		this.eventQueue.enqueue(tuple);
		this.pointerQueue.enqueue(pointerArray);
		return tuple;
	}

	// if enqueue() returns null, means enqueue failed.
	public byte[] enqueue(byte[] tuple) {
		this.eventQueue.enqueue(tuple);
		this.pointerQueue.enqueue(this.defaultPointerArray);
		return tuple;
	}

	public byte[] dequeue(byte[][] pointerArray) {
		byte[][] tmpPointerArray = this.pointerQueue.dequeue();
		for (int i = 0; i < this.pointerSize; i++)
			pointerArray[i] = tmpPointerArray[i];
		return this.eventQueue.dequeue();
	}

	public byte[] dequeueLast(byte[][] pointerArray) {
		byte[][] tmpPointerArray = this.pointerQueue.dequeueLast();
		for (int i = 0; i < this.pointerSize; i++)
			pointerArray[i] = tmpPointerArray[i];
		return this.eventQueue.dequeueLast();
	}

	public byte[] peek(byte[][] pointerArray) {
		byte[][] tmpPointerArray = this.pointerQueue.peek();
		for (int i = 0; i < this.pointerSize; i++)
			pointerArray[i] = tmpPointerArray[i];
		return this.eventQueue.peek();
	}

	public byte[] peekLast(byte[][] pointerArray) {
		byte[][] tmpPointerArray = this.pointerQueue.peekLast();
		for (int i = 0; i < this.pointerSize; i++)
			pointerArray[i] = tmpPointerArray[i];
		return this.eventQueue.peekLast();
	}

	public byte[] getByPhysicalIndex(int index, byte[][] pointerArray) {
		byte[] tuple = this.eventQueue.getByPhysicalIndex(index);
		if (tuple == null) {
			for (int i = 0; i < this.pointerSize; i++)
				pointerArray[i] = null;
			return null;
		}
		byte[][] tmpPointerArray = this.pointerQueue.getByPhysicalIndex(index);
		for (int i = 0; i < this.pointerSize; i++)
			if (tmpPointerArray == null)
				pointerArray[i] = null;
			else
				pointerArray[i] = tmpPointerArray[i];
		return tuple;
	}

	public byte[] getPreviousByPhysicalIndex(int index, byte[][] pointerArray) {
		byte[] tuple = this.eventQueue.getPreviousByPhysicalIndex(index);
		if (tuple == null) {
			for (int i = 0; i < this.pointerSize; i++)
				pointerArray[i] = null;
			return null;
		}
		byte[][] tmpPointerArray = this.pointerQueue
				.getPreviousByPhysicalIndex(index);
		for (int i = 0; i < this.pointerSize; i++)
			if (tmpPointerArray != null)
				pointerArray[i] = tmpPointerArray[i];
		return tuple;
	}

	public int init() {
		if (initialized)
			return 0;
		this.defaultPointerArray = new byte[this.pointerSize][];
		for (int i = 0; i < this.pointerSize; i++)
			this.defaultPointerArray[i] = null;
		this.pointerQueue = new SingleReaderEventPointerQueueArrayImp();
		this.pointerQueue.init();
		this.eventQueue = new SingleReaderEventQueueArrayImp(schema);
		this.eventQueue.init();
		initialized = true;
		return 0;
	}

	public void finalizing() {
		this.initialized = false;
	}

	/* end of important functions */

	public SchemaElement[] getSchema() {
		return schema;
	}

	public int getQueueID() {
		return queueID;
	}

	public long getTotalDequeued() {
		return this.eventQueue.getTotalDequeued();
	}

	public long getTotalEnqueued() {
		return this.eventQueue.getTotalEnqueued();

	}

	public long getDeltaDequeued() {
		return this.eventQueue.getDeltaDequeued();
	}

	public long getDeltaEnqueued() {
		return this.eventQueue.getDeltaEnqueued();
	}

	public long getSize() {
		return this.eventQueue.getSize();
	}

	public void setSchema(SchemaElement[] schema) {
		this.schema = schema;
		for (int i = 0; i < this.schema.length; i++) {
			this.eventQueue.tupleSize += schema[i].getLength();
		}
	}

	public void setDeltaDequeued(long deltaDequeued) {
		this.eventQueue.setDeltaDequeued(deltaDequeued);
	}

	public void setDeltaEnqueued(long deltaEnqueued) {
		this.eventQueue.setDeltaEnqueued(deltaEnqueued);
	}

	public void setQueueID(int queueID) {
		this.queueID = queueID;
	}

	public void clearDeltaStat() {
		this.eventQueue.clearDeltaStat();
		this.pointerQueue.clearDeltaStat();
	}

	@Override
	public String toString() {
		String ret = this.getClass().getSimpleName() + " ID "
				+ this.getQueueID();
		// ret += this.getQueueID() + "\t";
		// ret += this.getSize() + "\t";
		// ret += this.getTotalDequeued() + "\t";
		// ret += this.getTotalEnqueued();
		return ret;
	}

	public int purgeNumber(long timeStamp) {
		int low = 0;
		int high = (int) this.getSize() - 1;

		while (low <= high) {
			int mid = (low + high) / 2;
			long midVal = StreamAccessor.getMaxTimestamp(this.eventQueue
					.get(mid));

			if (midVal < timeStamp)
				low = mid + 1;
			else if (midVal > timeStamp)
				high = mid - 1;
			else {// key found
				while (midVal == StreamAccessor.getMaxTimestamp(this.eventQueue
						.get(mid)))
					mid++;
				return mid;
			}
		}
		return low; // key not found.

	}

}
