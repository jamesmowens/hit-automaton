package com.hp.hpl.CHAOS.CommandProcess;

public class Utility {
	static int value = 0;

	public static int getUniqueInt() {
		if (value == Integer.MAX_VALUE)
			value = 0;
		value++;
		return value;
	}

}
