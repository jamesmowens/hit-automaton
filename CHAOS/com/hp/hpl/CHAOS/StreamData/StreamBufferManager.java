package com.hp.hpl.CHAOS.StreamData;

/**
 * @author wangson This class will be used as the manager of all stream tuples
 *         at runtime. For now, it is a place holder
 */
public class StreamBufferManager {
	public static byte[] mallocTupleSpace(int size) {
		return new byte[size];
	}

}
