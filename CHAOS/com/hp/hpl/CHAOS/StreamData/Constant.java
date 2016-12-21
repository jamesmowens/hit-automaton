package com.hp.hpl.CHAOS.StreamData;

public class Constant {

	// byte length of the base data types.

	public final static int INT_T = 0;
	public final static int DOUBLE_T = 1;
	public final static int STR20_T = 2;

	public final static int INT_S = Integer.SIZE / 8;
	public final static int DOUBLE_S = Double.SIZE / 8;
	public final static int STR20_S = 20;

	public final static int LONG_S = Long.SIZE / 8;

	//public final static int TUPLE_HEAD_S = 2 * Long.SIZE / 8 + INT_S;
	
	/* From version 0.1 we have four reserved fields in a tuple
	 * MinTimestamp: a long
	 * MaxTimestamp: a long
	 * Bitmap: an integer
	 * Index: an integer
	 * */
	public final static int TUPLE_HEAD_S = 2 * Long.SIZE / 8 + 2 * INT_S;

}
