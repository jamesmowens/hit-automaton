package com.hp.hpl.CHAOS.Queue;

import java.io.Serializable;
import java.util.Iterator;

import com.hp.hpl.CHAOS.QueryPlan.RunTimeBlock;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;

public abstract class StreamQueue implements Serializable, RunTimeBlock {

	/** The schema of this queue. */
	public SchemaElement[] schema;

	/** Each queue in a query plan has a unique queue id */
	public int queueID;

	/** byte size of a tuple in the queue */
	public int tupleSize;

	transient public boolean initialized = false;

	/** The total number of dequeued tuples. */
	transient private long totalDequeued = 0;

	/** The total number of enqueued tuples. */
	transient private long totalEnqueued = 0;

	/** The delta number of dequeued tuples. */
	transient private long deltaDequeued = 0;

	/** The delta number of enqueued tuples. */
	transient private long deltaEnqueued = 0;

	/** The size of queue. */
	transient private long size = 0;

	public StreamQueue(SchemaElement[] schema) {
		super();
		this.schema = schema;
		this.queueID = Utility.getUniqueInt();
		for (int i = 0; i < this.schema.length; i++) {
			this.tupleSize += schema[i].getLength();
		}
	}

	/*------------- BEGIN ABSTRACT CLASSES ----------------------------------*/

	abstract public byte[] dequeue();

	// to support stack
	abstract public byte[] dequeueLast();

	// if enqueue() returns null, means enqueue failed.
	abstract public byte[] enqueue(byte[] tuple);

	abstract public byte[] peek();

	// to support stack
	abstract public byte[] peekLast();

	abstract public Iterator<byte[]> getIterator();

	abstract public byte[] get(int index);

	// should be overwritten by subclasses if needed.
	public int init() {
		return 0;
	}

	// should be overwritten by subclasses if needed.
	public void finalizing() {
		this.initialized = false;
	}

	// should be overwritten by network queue only.
	public byte[] flush() {
		return null;
	}

	/*------------- END ABSTRACT CLASSES ------------------------------------*/

	public SchemaElement[] getSchema() {
		return schema;
	}

	public int getQueueID() {
		return queueID;
	}

	public long getTotalDequeued() {
		return totalDequeued;
	}

	public long getTotalEnqueued() {
		return totalEnqueued;
	}

	public long getDeltaDequeued() {
		return deltaDequeued;
	}

	public long getDeltaEnqueued() {
		return deltaEnqueued;
	}

	public long getSize() {
		return size;
	}

	public int getTupleSize() {
		return tupleSize;
	}

	public void setSchema(SchemaElement[] schema) {
		this.schema = schema;
		for (int i = 0; i < this.schema.length; i++) {
			this.tupleSize += schema[i].getLength();
		}
	}

	public void setDeltaDequeued(long deltaDequeued) {
		this.deltaDequeued = deltaDequeued;
	}

	public void setDeltaEnqueued(long deltaEnqueued) {
		this.deltaEnqueued = deltaEnqueued;
	}

	public void setQueueID(int queueID) {
		this.queueID = queueID;
	}

	public void enqueueStat() {
		this.deltaEnqueued++;
		this.totalEnqueued++;
		this.size++;
	}

	public void dequeueStat() {
		this.deltaDequeued++;
		this.totalDequeued++;
		this.size--;
	}

	public void clearDeltaStat() {
		this.deltaDequeued = 0;
		this.deltaEnqueued = 0;
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
			long midVal = StreamAccessor.getMaxTimestamp(this.get(mid));

			if (midVal < timeStamp)
				low = mid + 1;
			else if (midVal > timeStamp)
				high = mid - 1;
			else {// key found
				while (midVal == StreamAccessor.getMaxTimestamp(this.get(mid)))
					mid++;
				return mid;
			}
		}
		return low; // key not found.

	}

}
