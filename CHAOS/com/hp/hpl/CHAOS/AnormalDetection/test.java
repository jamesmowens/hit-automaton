package com.hp.hpl.CHAOS.AnormalDetection;

import java.util.ArrayList;

public class test {
	public static void main(String[] args) {
		ArrayList<Integer> test = new ArrayList<Integer>();
		test.add(1);
		test.add(2);
		test.add(3);
		System.out.println(test);
		test.remove(0);
		System.out.print(test);
		test.add(1);
		System.out.println(test);
	}
}
