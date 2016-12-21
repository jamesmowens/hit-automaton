package com.hp.hpl.CHAOS.Queue;

import com.hp.hpl.CHAOS.StreamData.SchemaElement;

@Deprecated
public class SingleReaderQueueSyn extends SingleReaderQueue {

	private static final long serialVersionUID = 1L;

	public SingleReaderQueueSyn(SchemaElement[] schema) {
		super(schema);
	}

	@Override
	public synchronized byte[] dequeue() {
		return super.dequeue();

	}

	@Override
	public synchronized byte[] enqueue(byte[] tuple) {
		return super.enqueue(tuple);
	}

	@Override
	public synchronized byte[] peek() {
		return super.peek();

	}

	@Override
	public synchronized byte[] get(int index) {

		return super.get(index);
	}

}
