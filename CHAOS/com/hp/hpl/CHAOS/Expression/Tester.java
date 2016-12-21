package com.hp.hpl.CHAOS.Expression;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

import com.hp.hpl.CHAOS.Queue.SingleReaderQueueArrayImp;
import com.hp.hpl.CHAOS.StreamData.Constant;
import com.hp.hpl.CHAOS.StreamData.DoubleSchemaElement;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamTupleCreator;

public class Tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		test2();

	}

	private static void test2() {

		int offset = 0;
		SchemaElement sch2 = new DoubleSchemaElement("col_C",
				Constant.DOUBLE_T, offset, Constant.DOUBLE_S);
		offset += sch2.getLength();
		SchemaElement sch3 = new DoubleSchemaElement("col_D",
				Constant.DOUBLE_T, offset, Constant.DOUBLE_S);

		SchemaElement[] schArray = new SchemaElement[] { sch2, sch3 };

		DoubleTerminal left = new DoubleTerminal(sch2);
		DoubleConstant right = new DoubleConstant(0.5);
		DoubleCompExp root = new DoubleCompExp(
				com.hp.hpl.CHAOS.Expression.Constant.GT, left, right);

		try {
			// SingleReaderQueue queue = new SingleReaderQueue(schArray);
			SingleReaderQueueArrayImp queue = new SingleReaderQueueArrayImp(
					schArray);

			long pre = sysPrint(0);

			for (int i = 0; i < 100; i++) {

				// Open the file that is the first
				// command line parameter
				FileInputStream fstream = new FileInputStream(
						".\\resource\\stream.txt");
				// Get the object of DataInputStream
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));
				String strLine;

				// Read File Line By Line
				int j = 0;
				while ((strLine = br.readLine()) != null) {
					// Print the content on the console
					// System.out.println(strLine);
					j++;
					if (j > 40000)
						break;

					byte[] tuple = StreamTupleCreator.makeTuple(strLine,
							schArray, "\t");
					queue.enqueue(tuple);

					// System.out.println(StreamAccessor.toString(tuple,
					// schArray));
				}

				for (int ii = 0; ii < 40000; ii++) {
					byte[] tuple = queue.dequeue();
					for (SchemaElement sch : schArray)
						sch.setTuple(tuple);
					root.eval();

				}
				// sysPrint();
				// Close the input stream
				in.close();
			}

			sysPrint(pre);

		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

	}

	public static long sysPrint(long pre) {
		Runtime rt = Runtime.getRuntime();
		DecimalFormat df = new DecimalFormat("0.00#");
		long ret = System.currentTimeMillis();
		System.out.println(ret);
		System.out.println(ret - pre);
		System.out.println("-------------Memory Information  --------------");
		System.gc();
		System.out.println("Free memory: "
				+ df.format((rt.freeMemory() / 1024.0 / 1024.0))
				+ "MB");
		System.out.println("Total memory: "
				+ df.format((rt.totalMemory() / 1024.0 / 1024.0))
				+ "MB");
		System.out
				.println("Max memory: "
						+ df
								.format((rt.maxMemory() / 1024.0 / 1024.0))
						+ "MB");
		System.out
				.println("Used memory: "
						+ df.format(((rt.totalMemory() - rt
								.freeMemory()) / 1024.0 / 1024.0)) + "MB");
		return ret;
	}

}
