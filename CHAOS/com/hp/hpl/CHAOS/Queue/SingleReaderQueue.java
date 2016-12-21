package com.hp.hpl.CHAOS.Queue;

import java.util.Iterator;
import java.util.LinkedList;

import com.hp.hpl.CHAOS.StreamData.SchemaElement;

@Deprecated
//We should avoid using this. Instead, using SingleReaderQueueArrayImp
public class SingleReaderQueue extends StreamQueue {

	private static final long serialVersionUID = 1L;
	/** The list containing all contents of the queue. */
	transient protected LinkedList<byte[]> contents = new LinkedList<byte[]>();

	public SingleReaderQueue(SchemaElement[] schema) {
		super(schema);
	}

	@Override
	public byte[] dequeue() {
		byte[] tuple = this.contents.remove();
		this.dequeueStat();
		return tuple;
	}

	@Override
	public byte[] enqueue(byte[] tuple) {
		this.contents.add(tuple);
		this.enqueueStat();
		return tuple;
	}

	@Override
	public byte[] peek() {
		byte[] tuple = this.contents.peek();
		return tuple;
	}

	@Override
	public Iterator<byte[]> getIterator() {
		return this.contents.iterator();
	}

	@Override
	public byte[] get(int index) {
		return contents.get(index);
	}

	@Override
	public int init() {
		if (initialized)
			return 0;
		super.init();
		this.contents = new LinkedList<byte[]>();
		initialized = true;
		return 0;
	}

	@Override
	public byte[] dequeueLast() {
		byte[] tuple = this.contents.removeLast();
		this.dequeueStat();
		return tuple;
	}

	@Override
	public byte[] peekLast() {
		byte[] tuple = this.contents.peekLast();
		return tuple;
	}
}
