package com.hp.hpl.CHAOS.Queue;

/**
 * This queue is specially used by ECube The index will be set during enqueue
 * and cleared when dequeue The default index is -1, -1.
 * 
 */
public class SingleReaderEventPointerQueueArrayImp extends
		SingleReaderQueueArrayImpGeneric<byte[][]> {

	public SingleReaderEventPointerQueueArrayImp() {
		super();
	}

	@Override
	public byte[][][] addNewTypeVariableArray() {
		return new byte[this.MAX][][];
	}

	public byte[][] getByPhysicalIndex(int index) {
		if (index < 0)
			return null;
		int queueListIndex = index >> 16;
		int queueElementIndex = index & 0xffff;
		byte[][] tmp = queueList.get(queueListIndex).content[queueElementIndex];
		return tmp;
		// return queueList.get(index >> 16).content[index & 0xffff];
	}

	public byte[][] getPreviousByPhysicalIndex(int index) {
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
