/**
 * 
 */
package com.hp.hpl.CHAOS.StreamData;

/**
 * @author wangson This class will access the tuple column
 */
public class StreamAccessor {
	public static void setMinTimestamp(byte[] tuple, long ts) {
		Utility.fillTuple(tuple, Utility.toByta(ts), 0);
	}

	public static void setMaxTimestamp(byte[] tuple, long ts) {
		Utility.fillTuple(tuple, Utility.toByta(ts), Constant.LONG_S);
	}

	public static void setBitmap(byte[] tuple, byte[] b) {
		// bitmap should be 4 bytes.
		Utility.fillTuple(tuple, b, Constant.LONG_S * 2);
	}

	public static void setIndex(byte[] tuple, int index) {
		// index is the physical position of the tuple in the queue
		// index is read only after enqueue
		// end use should never use this method to change the index
		Utility.fillTuple(tuple, Utility.toByta(index), Constant.LONG_S * 2
				+ Constant.INT_S);
	}

	public static long getMinTimestamp(byte[] tuple) {
		return Utility.toLong(tuple, 0);
	}

	public static long getMaxTimestamp(byte[] tuple) {
		return Utility.toLong(tuple, Constant.LONG_S);
	}

	public static byte[] getBitmap(byte[] tuple) {
		// this return a 4-byte array
		return Utility.toByteA(tuple, Constant.LONG_S * 2);
	}

	public static int getIndex(byte[] tuple) {
		// index is the physical position of the tuple in the queue
		// index is read only after enqueue
		return Utility.toInt(tuple, Constant.LONG_S * 2 + Constant.INT_S);
	}

	public static void setCol(int value, byte[] tuple,
			SchemaElement[] scheArray, int colIndex) {
		Utility.fillTuple(tuple, Utility.toByta(value), scheArray[colIndex]
				.getTupleOffset());
	}

	public static void setCol(double value, byte[] tuple,
			SchemaElement[] scheArray, int colIndex) {
		Utility.fillTuple(tuple, Utility.toByta(value), scheArray[colIndex]
				.getTupleOffset());
	}

	public static void setCol(String value, byte[] tuple,
			SchemaElement[] scheArray, int colIndex) {
		Utility.fillTuple(tuple, Utility.toByta(value), scheArray[colIndex]
				.getTupleOffset());
	}

	public static int getIntCol(byte[] tuple, SchemaElement[] scheArray,
			int colIndex) {
		return Utility.toInt(tuple, scheArray[colIndex].getTupleOffset());
	}

	public static double getDoubleCol(byte[] tuple, SchemaElement[] scheArray,
			int colIndex) {
		return Utility.toDouble(tuple, scheArray[colIndex].getTupleOffset());
	}

	public static char[] getStr20Col(byte[] tuple, SchemaElement[] scheArray,
			int colIndex) {
		return Utility.toCharA(tuple, scheArray[colIndex].getTupleOffset());
	}
	
	public static String getStringCol(byte[] tuple, SchemaElement[] scheArray,
			int colIndex)
	{
		return Utility.toString(tuple, scheArray[colIndex].getTupleOffset());
	}

	// This function should only be used for debug purpose.
	public static String toString(byte[] tuple, SchemaElement[] scheArray) {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(Long.toString(Utility.toLong(tuple, 0)) + "\t");
		strBuf.append(Long.toString(Utility.toLong(tuple, Constant.LONG_S))
				+ "\t");
		strBuf.append(Integer.toString(Utility
				.toInt(tuple, Constant.LONG_S * 2))
				+ "\t");
		strBuf.append(Integer.toString(Utility.toInt(tuple, Constant.LONG_S * 2
				+ Constant.INT_S))
				+ "\t");
		for (int i = 0; i < scheArray.length; i++) {
			if (scheArray[i].getDataType() == Constant.INT_T)
				strBuf.append(Integer.toString(getIntCol(tuple, scheArray, i))
						+ "\t");
			if (scheArray[i].getDataType() == Constant.DOUBLE_T)
				strBuf.append(Double
						.toString(getDoubleCol(tuple, scheArray, i))
						+ "\t");
			if (scheArray[i].getDataType() == Constant.STR20_T)
				strBuf.append(Utility.toString(tuple, scheArray[i]
						.getTupleOffset())
						+ "\t");
		}
		return strBuf.toString();
	}
}
