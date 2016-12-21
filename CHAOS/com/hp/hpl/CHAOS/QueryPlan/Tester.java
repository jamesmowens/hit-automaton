package com.hp.hpl.CHAOS.QueryPlan;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import com.hp.hpl.CHAOS.Queue.SingleReaderQueueArrayImp;
import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.util.Calendar;
import com.hp.hpl.CHAOS.StreamData.IntSchemaElement;
import com.hp.hpl.CHAOS.StreamOperator.StreamOperator;
import com.hp.hpl.CHAOS.StreamOperator.StreamSelectOperator;

public class Tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		test6(args);

	}

	private static void test6(String[] args) {

		write(args);
		read(args);
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
				+ df.format((rt.freeMemory() / 1024.0 / 1024.0)) + "MB");
		System.out.println("Total memory: "
				+ df.format((rt.totalMemory() / 1024.0 / 1024.0)) + "MB");
		System.out.println("Max memory: "
				+ df.format((rt.maxMemory() / 1024.0 / 1024.0)) + "MB");
		System.out
				.println("Used memory: "
						+ df
								.format(((rt.totalMemory() - rt.freeMemory()) / 1024.0 / 1024.0))
						+ "MB");
		return ret;
	}

	public static void compare(byte[] tuple1, byte[] tuple2,
			SchemaElement[] schArray) {
		if (StreamAccessor.toString(tuple1, schArray).compareTo(
				StreamAccessor.toString(tuple2, schArray)) == 0) {
			// System.out.println("GOOD");
			// System.out.println(StreamAccessor.toString(tuple1,
			// schArray)+"---"+StreamAccessor.toString(tuple2, schArray));
		} else {
			System.out.println("BAD");
			System.out.println(StreamAccessor.toString(tuple1, schArray)
					+ "---" + StreamAccessor.toString(tuple2, schArray));
		}
	}

	public static void write(String[] args) {
		String filename = "time.ser";
		if (args.length > 0) {
			filename = args[0];
		}
		SingleReaderQueueArrayImp queue = new SingleReaderQueueArrayImp(
				new SchemaElement[] { new IntSchemaElement("col1", 0) });

		StreamOperator op1 = new StreamSelectOperator(0, null,
				new StreamQueue[] { queue }, null);
		StreamOperator op2 = new StreamSelectOperator(1,
				new StreamQueue[] { queue }, null, null);
		// RunTimeEnvironment rte = new RunTimeEnvironment(new
		// StreamOperator[]{op1,op2}, new StreamQueue[]{queue},null);
		RunTimeEnvironment rte = new RunTimeEnvironment(new StreamOperator[] {
				op1, op2 }, "test");

		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);
			out.writeObject(rte);
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void read(String[] args) {
		String filename = "time.ser";
		if (args.length > 0) {
			filename = args[0];
		}
		RunTimeEnvironment rte = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			rte = (RunTimeEnvironment) in.readObject();
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		// print out restored time
		// System.out.println(queue);
		System.out.println();
		// print out the current time
		System.out.println("Current time: " + Calendar.getInstance().getTime());
	}

}
