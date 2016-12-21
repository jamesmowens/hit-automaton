package com.hp.hpl.CHAOS.StreamData;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class Tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		test2();
		
		/*System.out.println();
		int[] b = new int[3];
		for(int i = 0; i<3; i++)
		{
			b[i] = i;
		}
		System.out.println("b: " + b);
		int address = b.clone();
		b.
		System.out.println("b: " + address);*/

	}

	private static void test2() {

		int offset = 0;
		SchemaElement sch2 = new DoubleSchemaElement("col_C",
				Constant.DOUBLE_T, offset, Constant.DOUBLE_S);
		offset += sch2.getLength();
		SchemaElement sch3 = new DoubleSchemaElement("col_D",
				Constant.DOUBLE_T, offset, Constant.DOUBLE_S);

		SchemaElement[] schArray = new SchemaElement[] { sch2, sch3 };

		try {
			System.out.println(System.currentTimeMillis());

			for (int i = 0; i < 100; i++) {

				// Open the file that is the first
				// command line parameter
				FileInputStream fstream = new FileInputStream(
						".\\stream1.txt");
				// Get the object of DataInputStream
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));
				String strLine;

				// Read File Line By Line
				while ((strLine = br.readLine()) != null) {
					// Print the content on the console
					// System.out.println(strLine);
					byte[] tuple = StreamTupleCreator.makeTuple(strLine,
							schArray, "\t");
					// System.out.println(StreamAccessor.toString(tuple,
					// schArray));
				}

				// Close the input stream
				in.close();
			}
			System.out.println(System.currentTimeMillis());

		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

	}

	private static void test1() {
		int offset = 0;
		SchemaElement sch0 = new IntSchemaElement("col_A", Constant.INT_T,
				offset, Constant.INT_S);
		offset += sch0.getLength();
		SchemaElement sch1 = new IntSchemaElement("col_B", Constant.INT_T,
				offset, Constant.INT_S);
		offset += sch1.getLength();
		SchemaElement sch2 = new DoubleSchemaElement("col_C",
				Constant.DOUBLE_T, offset, Constant.DOUBLE_S);
		offset += sch2.getLength();
		SchemaElement sch3 = new DoubleSchemaElement("col_D",
				Constant.DOUBLE_T, offset, Constant.DOUBLE_S);
		offset += sch3.getLength();
		SchemaElement sch4 = new Str20SchemaElement("col_E", Constant.STR20_T,
				offset, Constant.STR20_S);

		SchemaElement[] schArray = new SchemaElement[] { sch0, sch1, sch2,
				sch3, sch4 };

		for (int i = 0; i < 5; i++)
			System.out.println(schArray[i]);

		byte[] tuple = StreamTupleCreator.makeTuple("10,20,23.3,0.999,TTT",
				schArray);

		System.out.println(StreamAccessor.toString(tuple, schArray));

		int offset1 = 0;
		SchemaElement sch01 = new IntSchemaElement("col_B", Constant.INT_T,
				offset1, Constant.INT_S);
		offset1 += sch0.getLength();
		SchemaElement sch11 = new IntSchemaElement("col_A", Constant.INT_T,
				offset1, Constant.INT_S);

		SchemaElement[] schArray1 = new SchemaElement[] { sch01, sch11 };

		SchemaMap smap = new SchemaMap();
		smap.addEntry(sch0, sch11);
		smap.addEntry(sch1, sch01);

		byte[] dest = StreamTupleCreator.makeEmptyTuple(schArray1);

		StreamTupleCreator.tupleCopy(dest, tuple, schArray, smap);

		System.out.println(StreamAccessor.toString(dest, schArray1));

	}
}
