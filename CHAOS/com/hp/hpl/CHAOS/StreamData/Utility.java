package com.hp.hpl.CHAOS.StreamData;

public class Utility {

	public static void fillTuple(byte[] tuple, byte[] field, int offset) {
		for (int i = 0; i < field.length; i++)
			tuple[i + offset] = field[i];
	}

	public static void fillTuple(byte[] dest, byte[] src, int destOffset,
			int srcOffset, int length) {
		for (int i = 0; i < length; i++)
			dest[i + destOffset] = src[i + srcOffset];
	}

	public static byte[] extractTuple(byte[] src, int srcOffset, int length) {
		byte[] ret = new byte[length];
		for (int i = 0; i < length; i++)
			ret[i] = src[i + srcOffset];
		return ret;
	}

	/* "primitive type --> byte[] data" Methods */
	/* ========================= */

	public static byte[] toByta(int data) {
		return new byte[] { (byte) ((data >> 24) & 0xff),
				(byte) ((data >> 16) & 0xff), (byte) ((data >> 8) & 0xff),
				(byte) ((data >> 0) & 0xff), };
	}

	/* ========================= */

	public static byte[] toByta(long data) {
		return new byte[] { (byte) ((data >> 56) & 0xff),
				(byte) ((data >> 48) & 0xff), (byte) ((data >> 40) & 0xff),
				(byte) ((data >> 32) & 0xff), (byte) ((data >> 24) & 0xff),
				(byte) ((data >> 16) & 0xff), (byte) ((data >> 8) & 0xff),
				(byte) ((data >> 0) & 0xff), };
	}

	/* ========================= */

	public static byte[] toByta(double data) {
		return toByta(Double.doubleToRawLongBits(data));
	}

	/* ========================= */

	public static byte[] toByta(String data) {
		byte[] ret = new byte[Constant.STR20_S];
		if (data == null)
			return ret;
		else {
			int strLength = data.length() > Constant.STR20_S ? Constant.STR20_S
					: data.length();
			for (int i = 0; i < strLength; i++)
				ret[i] = (byte) data.charAt(i);
			return ret;
		}
	}

	/* ========================= */
	/* "byte[] data --> primitive type" Methods */
	/* ========================= */

	public static int toInt(byte[] data) {
		if (data == null || data.length != 4)
			return 0x0;
		// ----------
		return ( // NOTE: type cast not necessary for int
		(0xff & data[0]) << 24 | (0xff & data[1]) << 16 | (0xff & data[2]) << 8 | (0xff & data[3]) << 0);
	}

	/* ========================= */
	public static int toInt(byte[] data, int offset) {
		if (data == null || data.length - offset < 4)
			return 0x0;
		// ----------
		return ( // NOTE: type cast not necessary for int
		(0xff & data[0 + offset]) << 24 | (0xff & data[1 + offset]) << 16
				| (0xff & data[2 + offset]) << 8 | (0xff & data[3 + offset]) << 0);
	}

	/* ========================= */

	public static long toLong(byte[] data) {
		if (data == null || data.length != 8)
			return 0x0;
		// ----------
		return (
		// (Below) convert to longs before shift because digits
		// are lost with ints beyond the 32-bit limit
		(long) (0xff & data[0]) << 56 | (long) (0xff & data[1]) << 48
				| (long) (0xff & data[2]) << 40 | (long) (0xff & data[3]) << 32
				| (long) (0xff & data[4]) << 24 | (long) (0xff & data[5]) << 16
				| (long) (0xff & data[6]) << 8 | (long) (0xff & data[7]) << 0);
	}

	/* ========================= */
	public static long toLong(byte[] data, int offset) {
		if (data == null || data.length - offset < 8)
			return 0x0;
		// ----------
		return (
		// (Below) convert to longs before shift because digits
		// are lost with ints beyond the 32-bit limit
		(long) (0xff & data[0 + offset]) << 56
				| (long) (0xff & data[1 + offset]) << 48
				| (long) (0xff & data[2 + offset]) << 40
				| (long) (0xff & data[3 + offset]) << 32
				| (long) (0xff & data[4 + offset]) << 24
				| (long) (0xff & data[5 + offset]) << 16
				| (long) (0xff & data[6 + offset]) << 8 | (long) (0xff & data[7 + offset]) << 0);
	}

	/* ========================= */

	public static double toDouble(byte[] data) {
		if (data == null || data.length != 8)
			return 0x0;
		// ---------- simple:
		return Double.longBitsToDouble(toLong(data));
	}

	/* ========================= */

	public static double toDouble(byte[] data, int offset) {
		if (data == null || data.length - offset < 8)
			return 0x0;
		// ---------- simple:
		byte[] trim = new byte[] { data[0 + offset], data[1 + offset],
				data[2 + offset], data[3 + offset], data[4 + offset],
				data[5 + offset], data[6 + offset], data[7 + offset] };
		return Double.longBitsToDouble(toLong(trim));
	}

	/* ========================= */

	public static char[] toCharA(byte[] data) {
		char[] cData = new char[Constant.STR20_S];
		for (int i = 0; i < cData.length; i++)
			cData[i] = (char) data[i];
		return cData;
	}

	/* ========================= */

	public static char[] toCharA(byte[] data, int offset) {
		char[] cData = new char[Constant.STR20_S];
		for (int i = 0; i < cData.length; i++)
			cData[i] = (char) data[i + offset];
		return cData;
	}

	/* ========================= */
	// for bitmap only
	public static byte[] toByteA(byte[] data, int offset) {
		return (data == null) ? null : new byte[] { data[0 + offset],
				data[1 + offset], data[2 + offset], data[3 + offset] };
	}

	/* ========================= */
	// for toString() debug only
	public static String toString(byte[] data) {
		char[] cData = new char[Constant.STR20_S];
		for (int i = 0; i < cData.length; i++) {
			if (data[i] == 0)
				cData[i] = ' ';
			else
				cData[i] = (char) data[i];
		}
		return new String(cData);
	}

	/* ========================= */
	// for toString() debug only
	public static String toString(byte[] data, int offset) {
		//System.out.println("in func");
		char[] cData = new char[Constant.STR20_S];
		//System.out.println("data length: " + data.length);
		for (int i = 0; i < cData.length; i++) {
			//System.out.println("i: " + i);
			//System.out.println("offset: " + offset + "\n");
			if (data[i + offset] == 0)
				cData[i] = ' ';
			else
				cData[i] = (char) data[i + offset];
		}
		return new String(cData);
	}

}
