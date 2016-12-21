package com.hp.hpl.CHAOS.Queue;

import java.util.Iterator;
import java.util.LinkedList;

@Deprecated
//We should avoid using this. Instead, using SingleReaderQueueArrayImp
public class SingleReaderQueueGeneric<T> extends StreamQueueGeneric<T> {

	/** The list containing all contents of the queue. */
	protected final LinkedList<T> contents = new LinkedList<T>();

	public SingleReaderQueueGeneric() {
		super();
	}

	@Override
	public T dequeue() {
		T tuple = this.contents.remove();
		this.dequeueStat();
		return tuple;
	}

	@Override
	public void enqueue(T tuple) {
		this.contents.add(tuple);
		this.enqueueStat();
	}

	@Override
	public T peek() {
		T tuple = this.contents.peek();
		return tuple;
	}

	@Override
	public Iterator<T> getIterator() {
		return this.contents.iterator();
	}

	@Override
	public T dequeueLast() {
		T tuple = this.contents.removeLast();
		this.dequeueStat();
		return tuple;
	}

	@Override
	public T peekLast() {
		T tuple = this.contents.peekLast();
		return tuple;
	}

}
