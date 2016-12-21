package com.hp.hpl.CHAOS.Queue;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;

//Use a circular array to implement a queue.
//Every queue array has ONLY one active head and tail pointer
//A queue array will not be reuse until empty. That is, waste of one queue array is allowed
//The array of the queue is stored in an linked list.
//queue size only increase, never decrease.

public class SingleReaderQueueArrayImp extends StreamQueue {

	private static final long serialVersionUID = 1L;
	// protected final LinkedList<QueueElement> queueList;
	transient protected List<QueueElement> queueList;
	transient int headIndex, tailIndex, numQueueElement;

	public SingleReaderQueueArrayImp(SchemaElement[] schema) {
		super(schema);
		this.queueList = new LinkedList<QueueElement>();
		this.queueList.add(new QueueElement());
		this.headIndex = 0;
		this.tailIndex = 0;
		this.numQueueElement = 1;
	}

	@Override
	public int init() {
		if (initialized)
			return 0;
		super.init();
		this.queueList = new LinkedList<QueueElement>();
		this.queueList.add(new QueueElement());
		this.headIndex = 0;
		this.tailIndex = 0;
		this.numQueueElement = 1;
		initialized = true;
		return 0;
	}

	@Override
	public byte[] enqueue(byte[] k) {
		if (queueList.get(tailIndex).isFull()
				|| (tailIndex == headIndex && queueList.get(tailIndex).rear < queueList
						.get(tailIndex).front)) {
			tailIndex = ++tailIndex % numQueueElement;
			if (tailIndex == headIndex) {
				headIndex++;
				queueList.add(tailIndex, new QueueElement());
				numQueueElement++;
				queueList.get(tailIndex).enqueue(k);
			} else {
				queueList.get(tailIndex).enqueue(k);
			}
		} else
			queueList.get(tailIndex).enqueue(k);
		this.enqueueStat();
		return k;
	}

	@Override
	public byte[] dequeue() {
		if (this.isEmpty())
			return null;
		byte[] retTuple = queueList.get(headIndex).dequeue();

		if (queueList.get(headIndex).isEmpty() && headIndex != tailIndex) {
			headIndex = ++headIndex % numQueueElement;
		}
		this.dequeueStat();
		return retTuple;
	}

	@Override
	public byte[] dequeueLast() {
		if (this.isEmpty())
			return null;
		byte[] retTuple = queueList.get(tailIndex).dequeueLast();

		if (queueList.get(tailIndex).isEmpty() && headIndex != tailIndex) {
			tailIndex = tailIndex - 1 + numQueueElement % numQueueElement;
		}
		this.dequeueStat();
		return retTuple;
	}

	@Override
	public byte[] peek() {
		if (this.isEmpty())
			return null;
		else
			return queueList.get(headIndex).peek();
	}

	@Override
	public byte[] peekLast() {
		if (this.isEmpty())
			return null;
		else
			return queueList.get(tailIndex).peekLast();
	}

	public boolean isEmpty() {
		return headIndex == tailIndex && queueList.get(headIndex).isEmpty();
	}

	@Override
	public Iterator<byte[]> getIterator() {
		return new QueueIterator(this);
	}

	@Override
	public byte[] get(int index) {
		int realMax = QueueElement.MAX - 1;// Each QueueElement has 255 tuples
		// maximumly.
		int headI = (headIndex + (index - queueList.get(headIndex).size() + realMax)
				/ realMax)
				% numQueueElement;
		int frontI = (index > queueList.get(headIndex).size() - 1) ? (index
				- queueList.get(headIndex).size() + realMax)
				% realMax : index;
		return queueList.get(headI).get(frontI);
	}

	protected class QueueElement {
		final static int MAX = 256;
		byte[][] content;
		int front, rear;

		public QueueElement() {
			super();
			this.content = new byte[MAX][];
			this.front = 0;
			this.rear = 0;
		}

		boolean isEmpty() {
			return front == rear;
		}

		boolean isFull() {
			return ((rear + 1) & 0xff) == front;
		}

		public void enqueue(byte[] k) {
			content[rear] = k;
			// rear = ++rear % MAX;
			rear = ++rear & 0xff;
		}

		public void enqueueWithSetIndex(byte[] k) {
			content[rear] = k;
			StreamAccessor.setIndex(k, rear);
			// rear = ++rear % MAX;
			rear = ++rear & 0xff;
		}

		public byte[] dequeue() {
			byte[] tuple = content[front];
			content[front] = null;
			// front = ++front % MAX;
			front = ++front & 0xff;
			return tuple;
		}

		public byte[] dequeueLast() {
			rear = rear - 1 + MAX & 0xff;
			byte[] tuple = content[rear];
			content[rear] = null;
			// front = ++front % MAX;
			return tuple;
		}

		public byte[] peek() {
			return content[front];
		}

		public byte[] peekLast() {
			return content[rear - 1 + MAX & 0xff];
		}

		public int size() {
			return (rear < front) ? rear + MAX - front : rear - front;
		}

		public byte[] get(int index) {
			return content[(front + index) & 0xff];
		}
	}

	public class QueueIterator implements Iterator<byte[]> {
		SingleReaderQueueArrayImp queue;
		int headIndex, tailIndex, numQueueElement;
		int frontHead, rearHead;

		public QueueIterator(SingleReaderQueueArrayImp queue) {
			super();
			this.queue = queue;
			this.headIndex = queue.headIndex;
			this.tailIndex = queue.tailIndex;
			this.numQueueElement = queue.numQueueElement;
			this.frontHead = queueList.get(headIndex).front;
			this.rearHead = queueList.get(headIndex).rear;
		}

		public boolean hasNext() {
			return !(headIndex == tailIndex && frontHead == rearHead);
		}

		public byte[] next() {
			byte[] ret = queueList.get(headIndex).content[frontHead];
			frontHead = ++frontHead & 0xff;
			if (frontHead == rearHead && headIndex != tailIndex) {
				headIndex = ++headIndex % numQueueElement;
				this.frontHead = queueList.get(headIndex).front;
				this.rearHead = queueList.get(headIndex).rear;
			}
			return ret;
		}

		public void remove() {
		}

	}

}
