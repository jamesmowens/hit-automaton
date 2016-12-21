package com.hp.hpl.CHAOS.State;

import java.io.Serializable;

import com.hp.hpl.CHAOS.Expression.BoolRetExp;
import com.hp.hpl.CHAOS.QueryPlan.RunTimeBlock;
import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.SchemaMap;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;

/**
 * @author liumo
 * 
 */
public abstract class StreamState implements Serializable, RunTimeBlock {

	boolean initialized = false;

	abstract public void probe(BoolRetExp bool, byte[] tuple,
			SchemaElement[] sArray, SchemaMap sMap, StreamQueue outputQ);

	abstract public void probe(BoolRetExp bool, byte[] tuple,
			SchemaElement[] sArray, SchemaMap sMap, StreamQueue[] outputQA);

	abstract public int purge(byte[] tuple);

	abstract public void insert(byte[] tuple);

	abstract public int getSize();

	@Override
	abstract public String toString();

	public void setWindowSize(long windowSize) {

	}

	public void setWindowSize(int windowSize) {

	}

	public void setKeyIndex(int keyIndex) {

	}

	public void setProbeTupleIndex(int probeTupleIndex) {

	}

	protected void setTimestamp(byte[] tuple, byte[] stateTuple, byte[] dest) {

		long maxTime1 = StreamAccessor.getMaxTimestamp(tuple);
		long maxTime2 = StreamAccessor.getMaxTimestamp(stateTuple);

		long minTime1 = StreamAccessor.getMinTimestamp(tuple);
		long minTime2 = StreamAccessor.getMinTimestamp(stateTuple);

		long maxTime = (maxTime1 > maxTime2) ? maxTime1 : maxTime2;
		long minTime = (minTime1 < minTime2) ? minTime1 : minTime2;

		StreamAccessor.setMaxTimestamp(dest, maxTime);
		StreamAccessor.setMinTimestamp(dest, minTime);
	}

	// should be overwritten by subclasses if needed.
	public int init() {
		return 0;
	}

	// should be overwritten by subclasses if needed.
	public void finalizing() {
		this.initialized = false;
	}

}
