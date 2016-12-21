package com.hp.hpl.CHAOS.Queue;

@Deprecated
public class SingleReaderQueueGenericSyn<T> extends SingleReaderQueueGeneric<T> {

	public SingleReaderQueueGenericSyn() {
		super();
	}

	@Override
	public synchronized T dequeue() {
		return super.dequeue();
	}

	@Override
	public synchronized T dequeueLast() {
		return super.dequeueLast();
	}

	@Override
	public synchronized void enqueue(T tuple) {
		super.enqueue(tuple);
	}

	@Override
	public synchronized T peek() {
		return super.peek();
	}

	@Override
	public synchronized T peekLast() {
		return super.peekLast();
	}

}
