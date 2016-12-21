package com.hp.hpl.CHAOS.StreamData;

import java.util.StringTokenizer;

/**
 * @author wangson This class will be used to build tuples
 */
public class StreamTupleCreator {

	public static byte[] makeEmptyTuple(SchemaElement[] schEleArray) {
		int size = 0;
		for (int i = 0; i < schEleArray.length; i++) {
			size += schEleArray[i].getLength();
		}
		byte[] tuple = StreamBufferManager.mallocTupleSpace(size
				+ Constant.TUPLE_HEAD_S);
		return tuple;
	}

	public static byte[] makeEmptyTuple(int size) {
		byte[] tuple = StreamBufferManager.mallocTupleSpace(size
				+ Constant.TUPLE_HEAD_S);
		return tuple;
	}

	public static byte[] makeTuple(String[] strArray, int offset,
			SchemaElement[] schEleArray) {
		// we assume the strArray and the schEleArray are correctly set.
		byte[] tuple = makeEmptyTuple(schEleArray);

		for (int i = 0; i < schEleArray.length; i++) {
			byte[] field = schEleArray[i].buildByteArray(strArray[i + offset]);
			Utility.fillTuple(tuple, field, schEleArray[i].getTupleOffset());
		}

		return tuple;
	}

	public static byte[] makeTuple(String[] strArray,
			SchemaElement[] schEleArray) {
		return makeTuple(strArray, 0, schEleArray);
	}

	public static byte[] makeTuple(String line, SchemaElement[] schEleArray,
			String delim) {
		StringTokenizer st = new StringTokenizer(line, delim);
		String[] strArray = new String[st.countTokens()];
		int i = 0;
		while (st.hasMoreTokens()) {
			strArray[i++] = st.nextToken();
		}
		return makeTuple(strArray, schEleArray);
	}

	public static byte[] makeTuple(String line, SchemaElement[] schEleArray) {
		return makeTuple(line, schEleArray, ",");
	}

	public static void tupleCopy(byte[] dest, byte[] src,
			SchemaElement[] srcSchema, SchemaMap sMap) {
		for (int i = 0; i < srcSchema.length; i++) {
			int[] offLength = sMap.getOffsetLength(srcSchema[i]);
			Utility.fillTuple(dest, src, offLength[0], srcSchema[i]
					.getTupleOffset(), offLength[1]);
		}
	}

	public static void tupleCopyWithTimestamp(byte[] dest, byte[] src,
			SchemaElement[] srcSchema, SchemaMap sMap) {
		Utility.fillTuple(dest, src, 0, 0, Constant.TUPLE_HEAD_S);
		tupleCopy(dest, src, srcSchema, sMap);
	}

	public static void tupleAppend(byte[] dest, byte[] src, int destOffset) {
		Utility.fillTuple(dest, src, destOffset + Constant.TUPLE_HEAD_S,
				Constant.TUPLE_HEAD_S, src.length - Constant.TUPLE_HEAD_S);
	}
}
