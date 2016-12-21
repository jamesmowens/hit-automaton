package com.hp.hpl.CHAOS.Queue;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

//Use a circular array to implement a queue.
//Every queue array has ONLY one active head and tail pointer
//A queue array will not be reuse until empty. That is, waste of one queue array is allowed
//The array of the queue is stored in an linked list.
//queue size only increase, never decrease.

public abstract class SingleReaderQueueArrayImpGeneric<T> extends
		StreamQueueGeneric<T> {

	// protected final LinkedList<QueueElement> queueList;
	protected final List<QueueElement<T>> queueList;
	int headIndex, tailIndex, numQueueElement;

	int MAX = 256;

	abstract public T[] addNewTypeVariableArray();

	public SingleReaderQueueArrayImpGeneric() {
		super();
		this.queueList = new LinkedList<QueueElement<T>>();
		this.queueList.add(new QueueElement<T>(this.addNewTypeVariableArray(),
				MAX));
		this.headIndex = 0;
		this.tailIndex = 0;
		this.numQueueElement = 1;
	}

	@Override
	public void enqueue(T k) {
		if (queueList.get(tailIndex).isFull()
				|| (tailIndex == headIndex && queueList.get(tailIndex).rear < queueList
						.get(tailIndex).front)) {
			tailIndex = ++tailIndex % numQueueElement;
			if (tailIndex == headIndex) {
				headIndex++;
				queueList.add(tailIndex, new QueueElement<T>(this
						.addNewTypeVariableArray(), MAX));
				numQueueElement++;
				queueList.get(tailIndex).enqueue(k);
			} else {
				queueList.get(tailIndex).enqueue(k);
			}
		} else
			queueList.get(tailIndex).enqueue(k);
		this.enqueueStat();
	}

	@Override
	public T dequeue() {
		if (this.isEmpty())
			return null;
		T retTuple = queueList.get(headIndex).dequeue();

		if (queueList.get(headIndex).isEmpty() && headIndex != tailIndex) {
			headIndex = ++headIndex % numQueueElement;
		}
		this.dequeueStat();
		return retTuple;
	}

	@Override
	public T dequeueLast() {
		if (this.isEmpty())
			return null;
		T retTuple = queueList.get(tailIndex).dequeueLast();

		if (queueList.get(tailIndex).isEmpty() && headIndex != tailIndex) {
			tailIndex = tailIndex - 1 + numQueueElement % numQueueElement;
		}
		this.dequeueStat();
		return retTuple;
	}

	@Override
	public T peek() {
		if (this.isEmpty())
			return null;
		else
			return queueList.get(headIndex).peek();
	}

	@Override
	public T peekLast() {
		if (this.isEmpty())
			return null;
		else
			return queueList.get(tailIndex).peekLast();
	}

	public boolean isEmpty() {
		return headIndex == tailIndex && queueList.get(headIndex).isEmpty();
	}

	@Override
	public Iterator<T> getIterator() {
		return new QueueIterator(this);
	}

	protected class QueueElement<TT> {
		int MAX;
		TT[] content;
		int front, rear;

		public QueueElement(TT[] typeVariableArray, int max) {
			super();
			content = typeVariableArray;
			this.MAX = max;
			this.front = 0;
			this.rear = 0;
		}

		boolean isEmpty() {
			return front == rear;
		}

		boolean isFull() {
			return ((rear + 1) & 0xff) == front;
		}

		public void enqueue(TT k) {
			content[rear] = k;
			// rear = ++rear % MAX;
			rear = ++rear & 0xff;
		}

		public TT dequeue() {
			TT tuple = content[front];
			content[front] = null;
			// front = ++front % MAX;
			front = ++front & 0xff;
			return tuple;
		}

		public TT dequeueLast() {
			rear = rear - 1 + MAX & 0xff;
			TT tuple = content[rear];
			content[rear] = null;
			// front = ++front % MAX;
			return tuple;
		}

		public TT peek() {
			return content[front];
		}

		public TT peekLast() {
			return content[rear - 1 + MAX & 0xff];
		}
	}

	public class QueueIterator implements Iterator<T> {
		SingleReaderQueueArrayImpGeneric<T> queue;
		int headIndex, tailIndex, numQueueElement;
		int frontHead, rearHead;

		public QueueIterator(SingleReaderQueueArrayImpGeneric<T> queue) {
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

		public T next() {
			T ret = queueList.get(headIndex).content[frontHead];
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
