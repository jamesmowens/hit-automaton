package com.hp.hpl.CHAOS.Queue;

import java.util.Iterator;

//only tuple queue are Serializable.
public abstract class StreamQueueGeneric<T> {

	/** Each queue in a query plan has a unique queue id */
	private final int queueID;

	/** The total number of dequeued tuples. */
	private int totalDequeued = 0;

	/** The total number of enqueued tuples. */
	private int totalEnqueued = 0;

	/** The delta number of dequeued tuples. */
	private int deltaDequeued = 0;

	/** The delta number of enqueued tuples. */
	private int deltaEnqueued = 0;

	/** The size of queue. */
	private int size = 0;

	public StreamQueueGeneric() {
		super();
		this.queueID = Utility.getUniqueInt();
	}

	/*------------- BEGIN ABSTRACT CLASSES ----------------------------------*/

	abstract public T dequeue();

	// to support stack
	abstract public T dequeueLast();

	abstract public void enqueue(T tuple);

	abstract public T peek();

	// to support stack
	abstract public T peekLast();

	abstract public Iterator<T> getIterator();

	// should be overwritten by subclasses if needed.
	public void init() {
	}

	/*------------- END ABSTRACT CLASSES ------------------------------------*/

	public int getQueueID() {
		return queueID;
	}

	public int getTotalDequeued() {
		return totalDequeued;
	}

	public int getTotalEnqueued() {
		return totalEnqueued;
	}

	public int getDeltaDequeued() {
		return deltaDequeued;
	}

	public int getDeltaEnqueued() {
		return deltaEnqueued;
	}

	public int getSize() {
		return size;
	}

	public void setDeltaDequeued(int deltaDequeued) {
		this.deltaDequeued = deltaDequeued;
	}

	public void setDeltaEnqueued(int deltaEnqueued) {
		this.deltaEnqueued = deltaEnqueued;
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
		String ret = "Queue: ";
		ret += this.getQueueID() + "\t";
		ret += this.getSize() + "\t";
		ret += this.getTotalDequeued() + "\t";
		ret += this.getTotalEnqueued();
		return ret;
	}

}
