package com.hp.hpl.CHAOS.State;

import java.util.Iterator;

import com.hp.hpl.CHAOS.Expression.BoolRetExp;
import com.hp.hpl.CHAOS.Queue.SingleReaderQueueArrayImp;
import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.SchemaMap;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;
import com.hp.hpl.CHAOS.StreamData.StreamTupleCreator;

public abstract class NLState extends StreamState {
	protected StreamQueue queue;

	public NLState(SchemaElement[] sArray) {
		super();
		this.queue = new SingleReaderQueueArrayImp(sArray);
	}

	public StreamQueue getQueue() {
		return queue;
	}

	public void setQueue(StreamQueue queue) {
		this.queue = queue;
	}

	@Override
	public void insert(byte[] tuple) {
		queue.enqueue(tuple);
	}

	@Override
	public void probe(BoolRetExp bool, byte[] tuple, SchemaElement[] array,
			SchemaMap map, StreamQueue outputQ) {

		for (Iterator<byte[]> it = queue.getIterator(); it.hasNext();) {

			byte[] stateTuple = it.next();

			for (SchemaElement sch : queue.getSchema())
				sch.setTuple(stateTuple);

			for (SchemaElement sch : array)
				sch.setTuple(tuple);

			if (bool.eval()) {
				byte[] dest = StreamTupleCreator.makeEmptyTuple(outputQ
						.getSchema());

				StreamTupleCreator.tupleCopy(dest, tuple, array, map);

				StreamTupleCreator.tupleCopy(dest, stateTuple, queue
						.getSchema(), map);

				setTimestamp(tuple, stateTuple, dest);

				outputQ.enqueue(dest);
			}

		}

	}

	@Override
	public void probe(BoolRetExp bool, byte[] tuple, SchemaElement[] array,
			SchemaMap map, StreamQueue[] outputQA) {
		for (Iterator<byte[]> it = queue.getIterator(); it.hasNext();) {

			byte[] stateTuple = it.next();

			for (SchemaElement sch : queue.getSchema())
				sch.setTuple(stateTuple);

			for (SchemaElement sch : array)
				sch.setTuple(tuple);

			if (bool.eval()) {
				byte[] dest = StreamTupleCreator.makeEmptyTuple(outputQA[0]
						.getSchema());

				StreamTupleCreator.tupleCopy(dest, tuple, array, map);

				StreamTupleCreator.tupleCopy(dest, stateTuple, queue
						.getSchema(), map);

				setTimestamp(tuple, stateTuple, dest);

				for (StreamQueue outputQ : outputQA)
					outputQ.enqueue(dest);
			}

		}

	}

	@Override
	public int getSize() {

		return (int)queue.getSize();
	}

	@Override
	public String toString() {
		StringBuffer strBuf = new StringBuffer();
		for (Iterator<byte[]> it = queue.getIterator(); it.hasNext();)
			strBuf.append(StreamAccessor.toString(it.next(), queue.getSchema())
					+ "\n");
		return strBuf.toString();
	}
}
