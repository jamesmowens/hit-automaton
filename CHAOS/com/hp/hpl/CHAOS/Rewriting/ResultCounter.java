package com.hp.hpl.CHAOS.Rewriting;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class ResultCounter {
	public static void main(String args[]) throws FileNotFoundException {
		Scanner inputstream = new Scanner(new FileInputStream("file"));
		int c = 0; 

		while (inputstream.hasNext()) {
			String type = inputstream.next();
			if (type.equalsIgnoreCase("======result")) {
				c++;

			} else if(type.equalsIgnoreCase("======result for query======1")){
				c++; 

			}
			else if (type.equalsIgnoreCase("======reuse======"))
			{
				c++;
			}

		}

		System.out.print("========== number:===========" + c);

	}
}
