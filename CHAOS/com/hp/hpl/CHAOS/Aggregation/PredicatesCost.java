package com.hp.hpl.CHAOS.Aggregation;

import java.util.Date;

public class PredicatesCost {
	
	void predicatesFunction() {
		try{
			Thread.sleep(1);
		}
		catch(Exception e) {
			System.out.print("catch exception");
		}
		
		/*for (int i = 0; i <= 10000000; i++) {
			for (int j = 0; j <= 10000000; j++) {
				//for (int k = 0; k <= 100000; k++) {
					int total = i+j;
				}
			}*/
			
	}
	
	public static void main(String[] args) {
		PredicatesCost pc = new PredicatesCost();
		long execution_Start = (new Date()).getTime();
		pc.predicatesFunction();
		long execution_end = (new Date()).getTime();
		
		long executionTime = execution_end - execution_Start;
		
		System.out.println("executionTime is " + executionTime);
	}
}
