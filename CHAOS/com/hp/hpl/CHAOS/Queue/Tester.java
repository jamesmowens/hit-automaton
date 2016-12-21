package com.hp.hpl.CHAOS.Queue;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Iterator;

import com.hp.hpl.CHAOS.Expression.DoubleCompExp;
import com.hp.hpl.CHAOS.Expression.DoubleConstant;
import com.hp.hpl.CHAOS.Expression.DoubleTerminal;
import com.hp.hpl.CHAOS.StreamData.Constant;
import com.hp.hpl.CHAOS.StreamData.DoubleSchemaElement;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;
import com.hp.hpl.CHAOS.StreamData.StreamTupleCreator;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.util.Calendar;
import com.hp.hpl.CHAOS.StreamData.IntSchemaElement;

public class Tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		test5();
		test6(args);

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
			// SingleReaderQueueArrayImp queue = new
			// SingleReaderQueueArrayImp(schArray);
			SingleReaderQueueArrayImpGeneric<byte[]> queue = null;
			// new SingleReaderQueueArrayImpGeneric<byte[]>(schArray);

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

	private static void test3() {

		int offset = 0;
		SchemaElement sch2 = new DoubleSchemaElement("col_C",
				Constant.DOUBLE_T, offset, Constant.DOUBLE_S);
		offset += sch2.getLength();
		SchemaElement sch3 = new DoubleSchemaElement("col_D",
				Constant.DOUBLE_T, offset, Constant.DOUBLE_S);

		SchemaElement[] schArray = new SchemaElement[] { sch2, sch3 };

		try {
			SingleReaderQueue queue = new SingleReaderQueue(schArray);
			// SingleReaderQueueArrayImp queueA = new
			// SingleReaderQueueArrayImp(schArray);
			SingleReaderQueueArrayImpGeneric<byte[]> queueA = null;
			// new SingleReaderQueueArrayImpGeneric<byte[]>(schArray);

			long pre = sysPrint(0);

			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream(
					".\\resource\\stream.txt");
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			// // Read File Line By Line
			// while ((strLine = br.readLine()) != null) {
			// // Print the content on the console
			// //System.out.println(strLine);
			//				
			// byte[] tuple = StreamTupleCreator.makeTuple(strLine, schArray,
			// "\t");
			// queue.enqueue(tuple);
			// queueA.enqueue(tuple);
			//				
			// //System.out.println(StreamAccessor.toString(tuple, schArray));
			// }
			//			
			// // Close the input stream
			enqueuBoth(queue, queueA, 2000, br, schArray);
			dequeueBoth(queue, queueA, 2000, schArray);

			in.close();

			sysPrint(pre);

		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

	}

	private static void test4() {

		int offset = 0;
		SchemaElement sch2 = new DoubleSchemaElement("col_C",
				Constant.DOUBLE_T, offset, Constant.DOUBLE_S);
		offset += sch2.getLength();
		SchemaElement sch3 = new DoubleSchemaElement("col_D",
				Constant.DOUBLE_T, offset, Constant.DOUBLE_S);

		SchemaElement[] schArray = new SchemaElement[] { sch2, sch3 };

		try {
			// SingleReaderQueue queue = new SingleReaderQueue(schArray);
			// SingleReaderQueueArrayImp queue = new
			// SingleReaderQueueArrayImp(schArray);
			SingleReaderQueueArrayImpGeneric<byte[]> queue = null;
			// new SingleReaderQueueArrayImpGeneric<byte[]>(schArray);

			long pre = sysPrint(0);

			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream(
					".\\resource\\stream.txt");
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			// // Read File Line By Line
			// while ((strLine = br.readLine()) != null) {
			// // Print the content on the console
			// //System.out.println(strLine);
			//				
			// byte[] tuple = StreamTupleCreator.makeTuple(strLine, schArray,
			// "\t");
			// queue.enqueue(tuple);
			// queueA.enqueue(tuple);
			//				
			// //System.out.println(StreamAccessor.toString(tuple, schArray));
			// }
			//			
			// // Close the input stream
			enqueuOne(queue, 20000, br, schArray);
			dequeueIterator(queue, 20000, schArray);

			in.close();

			sysPrint(pre);

		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

	}

	private static void test5() {

		int offset = 0;
		SchemaElement sch2 = new DoubleSchemaElement("col_C",
				Constant.DOUBLE_T, offset, Constant.DOUBLE_S);
		offset += sch2.getLength();
		SchemaElement sch3 = new DoubleSchemaElement("col_D",
				Constant.DOUBLE_T, offset, Constant.DOUBLE_S);

		SchemaElement[] schArray = new SchemaElement[] { sch2, sch3 };

		try {
			SingleReaderQueue queue = new SingleReaderQueue(schArray);
			SingleReaderQueueArrayImp queueA = new SingleReaderQueueArrayImp(
					schArray);
			// SingleReaderQueueArrayImpGeneric<byte[]> queueA = new
			// SingleReaderQueueArrayImpGeneric<byte[]>(schArray);

			long pre = sysPrint(0);

			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream(
					".\\resource\\stream.txt");
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			// // Read File Line By Line
			// while ((strLine = br.readLine()) != null) {
			// // Print the content on the console
			// //System.out.println(strLine);
			//				
			// byte[] tuple = StreamTupleCreator.makeTuple(strLine, schArray,
			// "\t");
			// queue.enqueue(tuple);
			// queueA.enqueue(tuple);
			//				
			// //System.out.println(StreamAccessor.toString(tuple, schArray));
			// }
			//			
			// // Close the input stream
			enqueuBoth(queue, queueA, 2000, br, schArray);
			for (int j = 0; j < 2000; j++)
				compare(queue.get(j), queueA.get(j), schArray);

			in.close();

			sysPrint(pre);

		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
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

	public static void enqueuBoth(SingleReaderQueue queue,
			SingleReaderQueueArrayImpGeneric<byte[]> queueA, int num,
			BufferedReader br, SchemaElement[] schArray) throws IOException {
		String strLine;
		for (int i = 0; i < num; i++) {
			if ((strLine = br.readLine()) == null)
				break;
			byte[] tuple = StreamTupleCreator
					.makeTuple(strLine, schArray, "\t");
			queue.enqueue(tuple);
			queueA.enqueue(tuple);

		}
	}

	public static void dequeueBoth(SingleReaderQueue queue,
			SingleReaderQueueArrayImpGeneric<byte[]> queueA, int num,
			SchemaElement[] schArray) {
		for (int i = 0; i < num; i++) {
			byte[] tuple1, tuple2;
			tuple1 = queue.dequeue();
			tuple2 = queueA.dequeue();
			if (StreamAccessor.toString(tuple1, schArray).compareTo(
					StreamAccessor.toString(tuple2, schArray)) == 0) {
				// System.out.println("GOOD");
				// System.out.println(StreamAccessor.toString(tuple1,
				// schArray)+"---"+StreamAccessor.toString(tuple2, schArray));
			} else {
				System.out.println("BAD");
				// System.out.println(StreamAccessor.toString(tuple1,
				// schArray)+"---"+StreamAccessor.toString(tuple2, schArray));
			}
		}
	}

	public static void enqueuBoth(SingleReaderQueue queue,
			SingleReaderQueueArrayImp queueA, int num, BufferedReader br,
			SchemaElement[] schArray) throws IOException {
		String strLine;
		for (int i = 0; i < num; i++) {
			if ((strLine = br.readLine()) == null)
				break;
			byte[] tuple = StreamTupleCreator
					.makeTuple(strLine, schArray, "\t");
			queue.enqueue(tuple);
			queueA.enqueue(tuple);

		}
	}

	public static void dequeueBoth(SingleReaderQueue queue,
			SingleReaderQueueArrayImp queueA, int num, SchemaElement[] schArray) {
		for (int i = 0; i < num; i++) {
			byte[] tuple1, tuple2;
			tuple1 = queue.dequeue();
			tuple2 = queueA.dequeue();
			if (StreamAccessor.toString(tuple1, schArray).compareTo(
					StreamAccessor.toString(tuple2, schArray)) == 0) {
				// System.out.println("GOOD");
				// System.out.println(StreamAccessor.toString(tuple1,
				// schArray)+"---"+StreamAccessor.toString(tuple2, schArray));
			} else {
				System.out.println("BAD");
				// System.out.println(StreamAccessor.toString(tuple1,
				// schArray)+"---"+StreamAccessor.toString(tuple2, schArray));
			}
		}
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

	public static void enqueuOne(StreamQueueGeneric<byte[]> queue, int num,
			BufferedReader br, SchemaElement[] schArray) throws IOException {
		String strLine;
		for (int i = 0; i < num; i++) {
			if ((strLine = br.readLine()) == null)
				break;
			byte[] tuple = StreamTupleCreator
					.makeTuple(strLine, schArray, "\t");
			queue.enqueue(tuple);

		}
	}

	public static void dequeueIterator(StreamQueueGeneric<byte[]> queue,
			int num, SchemaElement[] schArray) {
		// num must == queue.size()
		byte[] tuple1, tuple2;
		byte[][] storage = new byte[num][];
		int i = 0;
		for (Iterator<byte[]> it = queue.getIterator(); it.hasNext();) {
			storage[i++] = it.next();
		}
		for (int j = 0; j < num; j++) {
			tuple1 = queue.dequeue();
			tuple2 = storage[j];
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
	}

	public static void write(String[] args) {
		String filename = "time.ser";
		if (args.length > 0) {
			filename = args[0];
		}
		SingleReaderQueueArrayImp queue = new SingleReaderQueueArrayImp(
				new SchemaElement[] { new IntSchemaElement("col1", 0) });
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);
			out.writeObject(queue);
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
		SingleReaderQueueArrayImp queue = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			queue = (SingleReaderQueueArrayImp) in.readObject();
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		// print out restored time
		System.out.println(queue);
		System.out.println();
		// print out the current time
		System.out.println("Current time: " + Calendar.getInstance().getTime());
	}

}
