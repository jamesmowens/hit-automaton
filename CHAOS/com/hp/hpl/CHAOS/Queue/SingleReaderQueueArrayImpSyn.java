package com.hp.hpl.CHAOS.Queue;

import com.hp.hpl.CHAOS.StreamData.SchemaElement;

public class SingleReaderQueueArrayImpSyn extends SingleReaderQueueArrayImp {

	private static final long serialVersionUID = 1L;

	public SingleReaderQueueArrayImpSyn(SchemaElement[] schema) {
		super(schema);
	}

	@Override
	public synchronized byte[] enqueue(byte[] k) {
		return super.enqueue(k);
	}

	@Override
	public synchronized byte[] dequeue() {
		return super.dequeue();
	}

	@Override
	public synchronized byte[] dequeueLast() {
		return super.dequeueLast();
	}

	@Override
	public synchronized byte[] peek() {
		return super.peek();
	}

	@Override
	public synchronized byte[] peekLast() {
		return super.peekLast();
	}

	public synchronized boolean isEmpty() {
		return super.isEmpty();
	}

	@Override
	public synchronized byte[] get(int index) {
		return super.get(index);
	}
}
