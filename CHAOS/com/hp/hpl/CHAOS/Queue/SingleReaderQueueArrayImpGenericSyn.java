package com.hp.hpl.CHAOS.Queue;

abstract public class SingleReaderQueueArrayImpGenericSyn<T> extends
		SingleReaderQueueArrayImpGeneric<T> {

	public SingleReaderQueueArrayImpGenericSyn() {
		super();
	}

	@Override
	public synchronized void enqueue(T k) {
		super.enqueue(k);
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
	public synchronized T peek() {
		return super.peek();
	}

	@Override
	public synchronized T peekLast() {
		return super.peekLast();
	}

	public synchronized boolean isEmpty() {
		return super.isEmpty();
	}

}
