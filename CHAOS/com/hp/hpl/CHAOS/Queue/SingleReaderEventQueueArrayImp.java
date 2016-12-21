package com.hp.hpl.CHAOS.Queue;

import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;

/**
 * This queue is specially used by ECube The index will be set during enqueue
 * and cleared when dequeue The default index is -1, -1.
 * 
 */
public class SingleReaderEventQueueArrayImp extends SingleReaderQueueArrayImp {

	private static final long serialVersionUID = 1L;

	public SingleReaderEventQueueArrayImp(SchemaElement[] schema) {
		super(schema);
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
				queueList.get(tailIndex).enqueueWithSetIndex(k);

				// modify affected tuples
				byte[] tmp;
				for (int i = headIndex; i < numQueueElement; i++)
					for (int j = 0; j < QueueElement.MAX; j++)
						if ((tmp = queueList.get(i).content[j]) != null)
							StreamAccessor.setIndex(tmp, (i << 16)
									| (StreamAccessor.getIndex(tmp) & 0xffff));
			} else {
				queueList.get(tailIndex).enqueueWithSetIndex(k);
			}
		} else
			queueList.get(tailIndex).enqueueWithSetIndex(k);

		StreamAccessor.setIndex(k, (tailIndex << 16)
				| (StreamAccessor.getIndex(k) & 0xffff));

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

		StreamAccessor.setIndex(retTuple, -1);

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

		StreamAccessor.setIndex(retTuple, -1);

		this.dequeueStat();
		return retTuple;
	}

	public byte[] getByPhysicalIndex(int index) {
		if (index < 0)
			return null;
		return queueList.get(index >> 16).content[index & 0xffff];
	}

	public byte[] getPreviousByPhysicalIndex(int index) {
		if (index < 0)
			return null;

		int queueListIndex = index >> 16;
		int queueElementIndex = index & 0xffff;

		if (queueElementIndex == queueList.get(queueListIndex).front) {
			if (queueListIndex == this.headIndex)
				return null;
			else {
				queueListIndex = (queueListIndex - 1 + numQueueElement)
						% numQueueElement;
				queueElementIndex = (queueList.get(queueListIndex).rear - 1 + 256) & 0xff;// MAX=256
			}
		} else {
			queueElementIndex = (queueElementIndex - 1 + 256) & 0xff;// MAX=256
		}
		return queueList.get(queueListIndex).content[queueElementIndex];
	}
}
