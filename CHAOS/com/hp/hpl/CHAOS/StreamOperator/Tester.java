package com.hp.hpl.CHAOS.StreamOperator;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Iterator;

import com.hp.hpl.CHAOS.Expression.DoubleCompExp;
import com.hp.hpl.CHAOS.Expression.DoubleConstant;
import com.hp.hpl.CHAOS.Expression.DoubleTerminal;
import com.hp.hpl.CHAOS.Expression.Str20CompExp;
import com.hp.hpl.CHAOS.Expression.Str20Terminal;
import com.hp.hpl.CHAOS.Queue.SingleReaderQueueArrayImp;
import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.State.CountNLState;
import com.hp.hpl.CHAOS.State.StreamState;
import com.hp.hpl.CHAOS.State.WindowHashState;
import com.hp.hpl.CHAOS.State.WindowNLState;
import com.hp.hpl.CHAOS.StreamData.Constant;
import com.hp.hpl.CHAOS.StreamData.DoubleSchemaElement;
import com.hp.hpl.CHAOS.StreamData.IntSchemaElement;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.SchemaMap;
import com.hp.hpl.CHAOS.StreamData.Str20SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;
import com.hp.hpl.CHAOS.StreamData.StreamTupleCreator;

public class Tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		test5();

	}

	private static void test1() {
		int offset = 0;

		SchemaElement sch0 = new IntSchemaElement("col_S1A", Constant.INT_T,
				offset, Constant.INT_S);
		offset += sch0.getLength();
		SchemaElement sch1 = new IntSchemaElement("col_S1B", Constant.INT_T,
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

		byte[] tuple1 = StreamTupleCreator.makeTuple("10,20,23.3,0.999,TTT",
				schArray);

		byte[] tuple2 = StreamTupleCreator.makeTuple("20,20,23.3,0.999,TTT",
				schArray);
		byte[] tuple3 = StreamTupleCreator.makeTuple("30,20,23.3,0.999,AAA",
				schArray);
		byte[] tuple4 = StreamTupleCreator.makeTuple("40,20,23.3,0.999,AAA",
				schArray);

		int offset2 = 0;

		SchemaElement sch02 = new IntSchemaElement("col_S2A", Constant.INT_T,
				offset2, Constant.INT_S);
		offset2 += sch02.getLength();
		SchemaElement sch12 = new IntSchemaElement("col_S2B", Constant.INT_T,
				offset2, Constant.INT_S);
		offset2 += sch12.getLength();
		SchemaElement sch22 = new DoubleSchemaElement("col_C",
				Constant.DOUBLE_T, offset2, Constant.DOUBLE_S);
		offset2 += sch22.getLength();
		SchemaElement sch32 = new DoubleSchemaElement("col_D",
				Constant.DOUBLE_T, offset2, Constant.DOUBLE_S);
		offset2 += sch32.getLength();
		SchemaElement sch42 = new Str20SchemaElement("col_E", Constant.STR20_T,
				offset2, Constant.STR20_S);

		SchemaElement[] schArray2 = new SchemaElement[] { sch02, sch12, sch22,
				sch32, sch42 };

		byte[] tuple5 = StreamTupleCreator.makeTuple("50,20,23.3,0.999,AAA",
				schArray2);

		// CountHashState state = new CountHashState(schArray, 4, 4, 2);
		CountNLState state = new CountNLState(schArray, 2);

		state.insert(tuple1);
		state.insert(tuple2);
		state.insert(tuple3);
		state.insert(tuple4);

		System.out.println("==========Before============");
		System.out.println(state);

		state.purge(null);

		System.out.println("==========After purge============");
		System.out.println(state);

		int offset1 = 0;
		SchemaElement sch01 = new IntSchemaElement("col_D1A", Constant.INT_T,
				offset1, Constant.INT_S);
		offset1 += sch01.getLength();
		SchemaElement sch11 = new IntSchemaElement("col_D1B", Constant.INT_T,
				offset1, Constant.INT_S);

		SchemaElement[] schArray1 = new SchemaElement[] { sch01, sch11 };

		SchemaMap smap = new SchemaMap();
		smap.addEntry(sch0, sch01);
		smap.addEntry(sch02, sch11);

		SingleReaderQueueArrayImp srq = new SingleReaderQueueArrayImp(schArray1);
		// --------------------for boolretExp

		int exoffset = 0;
		SchemaElement exsch2 = new DoubleSchemaElement("col_C",
				Constant.DOUBLE_T, exoffset, Constant.DOUBLE_S);
		exoffset += exsch2.getLength();
		SchemaElement exsch3 = new DoubleSchemaElement("col_D",
				Constant.DOUBLE_T, exoffset, Constant.DOUBLE_S);

		SchemaElement[] exschArray = new SchemaElement[] { exsch2, exsch3 };

		DoubleTerminal left = new DoubleTerminal(sch0);
		DoubleTerminal right = new DoubleTerminal(sch02);
		DoubleCompExp root = new DoubleCompExp(
				com.hp.hpl.CHAOS.Expression.Constant.GT, left, right);

		// --------------------------
		state.probe(root, tuple5, schArray2, smap, srq);

		String ret = new String();
		for (Iterator<byte[]> it = srq.getIterator(); it.hasNext();)
			ret += StreamAccessor.toString(it.next(), srq.getSchema()) + "\n";

		System.out.println("==========After probe============");
		System.out.println(ret);

	}

	private static void test2() {
		int offset = 0;

		SchemaElement sch0 = new IntSchemaElement("col_S1A", Constant.INT_T,
				offset, Constant.INT_S);
		offset += sch0.getLength();
		SchemaElement sch1 = new IntSchemaElement("col_S1B", Constant.INT_T,
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

		// CountHashState state = new CountHashState(schArray, 4, 4, 60000);
		// CountNLState state = new CountNLState(schArray, 60000);
		// WindowHashState state = new WindowHashState(schArray, 4, 4, 60000);
		WindowNLState state = new WindowNLState(schArray, 60000);
		
		byte[] tuple1 = null;
		long pre = sysPrint(0);
		for (int i = 1; i < 1000000; i++) {

			tuple1 = StreamTupleCreator.makeTuple(i + ",20,23.3,0.999,TTT",
					schArray);
			StreamAccessor.setMaxTimestamp(tuple1, i);
			StreamAccessor.setMinTimestamp(tuple1, i);
			state.insert(tuple1);
			state.purge(tuple1);
		}

		sysPrint(pre);
		System.exit(0);

		int offset2 = 0;

		SchemaElement sch02 = new IntSchemaElement("col_S2A", Constant.INT_T,
				offset2, Constant.INT_S);
		offset2 += sch02.getLength();
		SchemaElement sch12 = new IntSchemaElement("col_S2B", Constant.INT_T,
				offset2, Constant.INT_S);
		offset2 += sch12.getLength();
		SchemaElement sch22 = new DoubleSchemaElement("col_C",
				Constant.DOUBLE_T, offset2, Constant.DOUBLE_S);
		offset2 += sch22.getLength();
		SchemaElement sch32 = new DoubleSchemaElement("col_D",
				Constant.DOUBLE_T, offset2, Constant.DOUBLE_S);
		offset2 += sch32.getLength();
		SchemaElement sch42 = new Str20SchemaElement("col_E", Constant.STR20_T,
				offset2, Constant.STR20_S);

		SchemaElement[] schArray2 = new SchemaElement[] { sch02, sch12, sch22,
				sch32, sch42 };

		byte[] tuple5 = StreamTupleCreator.makeTuple("50,20,23.3,0.999,TTT",
				schArray2);
		// for windowbased testing
		StreamAccessor.setMaxTimestamp(tuple5, 1000000);
		StreamAccessor.setMinTimestamp(tuple5, 1000000);

		System.out.println("==========Before new tuple============");
		// System.out.println(state);

		// state.insert(tuple5);

		System.out.println("==========Before============");
		// System.out.println(state);

		// state.purge(tuple5);
		System.out.println("==========After purge============");

		// System.out.println(state);

		int offset1 = 0;
		SchemaElement sch01 = new IntSchemaElement("col_D1A", Constant.INT_T,
				offset1, Constant.INT_S);
		offset1 += sch01.getLength();
		SchemaElement sch11 = new IntSchemaElement("col_D1B", Constant.INT_T,
				offset1, Constant.INT_S);

		SchemaElement[] schArray1 = new SchemaElement[] { sch01, sch11 };

		SchemaMap smap = new SchemaMap();
		smap.addEntry(sch0, sch01);
		smap.addEntry(sch02, sch11);

		// state.probe(null, tuple5, schArray2, smap, srq);
		sysPrint(pre);

		// --------------------for boolretExp

		int exoffset = 0;
		SchemaElement exsch2 = new DoubleSchemaElement("col_C",
				Constant.DOUBLE_T, exoffset, Constant.DOUBLE_S);
		exoffset += exsch2.getLength();
		SchemaElement exsch3 = new DoubleSchemaElement("col_D",
				Constant.DOUBLE_T, exoffset, Constant.DOUBLE_S);

		SchemaElement[] exschArray = new SchemaElement[] { exsch2, exsch3 };

		Str20Terminal left = new Str20Terminal(sch4);
		Str20Terminal right = new Str20Terminal(sch42);
		Str20CompExp root = new Str20CompExp(
				com.hp.hpl.CHAOS.Expression.Constant.EQ, left, right);

		SingleReaderQueueArrayImp srq = new SingleReaderQueueArrayImp(schArray1);

		for (int k = 0; k < 100; k++) {
			state.probe(root, tuple5, schArray2, smap, srq);
		}

		sysPrint(pre);

		// String ret = new String();
		// for (Iterator<byte[]> it = srq.getIterator(); it.hasNext();)
		// ret += StreamAccessor.toString(it.next(), srq.getSchema()) + "\n";
		//
		System.out.println("==========After probe============");
		// System.out.println(ret);

	}
	
	
	private static void test3() {

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
			SingleReaderQueueArrayImp queueIn = new SingleReaderQueueArrayImp(
					schArray);
			SingleReaderQueueArrayImp queueOut = new SingleReaderQueueArrayImp(
					schArray);
		
			StreamSelectOperator select = new StreamSelectOperator(0,new StreamQueue[]{queueIn},new StreamQueue[]{queueOut}, root);
			
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
					queueIn.enqueue(tuple);

					// System.out.println(StreamAccessor.toString(tuple,
					// schArray));
				}

				select.run();
				
				for (int ii = 0; ii < 40000; ii++) {
					byte[] tuple = queueOut.dequeue();
					if (tuple==null) break;
					//System.out.println(StreamAccessor.toString(tuple, schArray));
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
	private static void test4() {

		int offset = 0;
		SchemaElement sch2 = new DoubleSchemaElement("col_C",
				Constant.DOUBLE_T, offset, Constant.DOUBLE_S);
		offset += sch2.getLength();
		SchemaElement sch3 = new DoubleSchemaElement("col_D",
				Constant.DOUBLE_T, offset, Constant.DOUBLE_S);

		SchemaElement[] schArray = new SchemaElement[] { sch2, sch3 };

		int offset2 = 0;
		SchemaElement sch22 = new DoubleSchemaElement("col_C",
				Constant.DOUBLE_T, offset2, Constant.DOUBLE_S);
		
		SchemaElement[] schArray2 = new SchemaElement[] { sch2 };
		
		SchemaMap smap = new SchemaMap();
		smap.addEntry(sch2, sch22);
		

		try {
			// SingleReaderQueue queue = new SingleReaderQueue(schArray);
			SingleReaderQueueArrayImp queueIn = new SingleReaderQueueArrayImp(
					schArray);
			SingleReaderQueueArrayImp queueOut = new SingleReaderQueueArrayImp(
					schArray);
			
			queueIn.setSchema(schArray);
			queueOut.setSchema(schArray2);
			
			
			StreamProjectOperator project = new StreamProjectOperator(0,new StreamQueue[]{queueIn},new StreamQueue[]{queueOut}, smap);
		
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
					StreamAccessor.setMinTimestamp(tuple, j);
					StreamAccessor.setMaxTimestamp(tuple, j);
					
					queueIn.enqueue(tuple);

					// System.out.println(StreamAccessor.toString(tuple,
					// schArray));
				}

				project.run();
				
				for (int ii = 0; ii < 40000; ii++) {
					byte[] tuple = queueOut.dequeue();
					if (tuple==null) break;
					//System.out.println(StreamAccessor.toString(tuple, schArray2));
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

	private static void test5() {
		int offset = 0;

		SchemaElement sch0 = new IntSchemaElement("col_S1A", Constant.INT_T,
				offset, Constant.INT_S);
		offset += sch0.getLength();
		SchemaElement sch1 = new IntSchemaElement("col_S1B", Constant.INT_T,
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

		// CountHashState state = new CountHashState(schArray, 4, 4, 60000);
		// CountNLState state = new CountNLState(schArray, 60000);
		//WindowHashState state = new WindowHashState(schArray, 4, 4, 60000);
		WindowNLState state = new WindowNLState(schArray, 60000);
		
		StreamQueue srcQueue = new SingleReaderQueueArrayImp(schArray);
		
//////////////////////////////////////
		
		int offset2 = 0;

		SchemaElement sch02 = new IntSchemaElement("col_S2A", Constant.INT_T,
				offset2, Constant.INT_S);
		offset2 += sch02.getLength();
		SchemaElement sch12 = new IntSchemaElement("col_S2B", Constant.INT_T,
				offset2, Constant.INT_S);
		offset2 += sch12.getLength();
		SchemaElement sch22 = new DoubleSchemaElement("col_C",
				Constant.DOUBLE_T, offset2, Constant.DOUBLE_S);
		offset2 += sch22.getLength();
		SchemaElement sch32 = new DoubleSchemaElement("col_D",
				Constant.DOUBLE_T, offset2, Constant.DOUBLE_S);
		offset2 += sch32.getLength();
		SchemaElement sch42 = new Str20SchemaElement("col_E", Constant.STR20_T,
				offset2, Constant.STR20_S);

		SchemaElement[] schArray2 = new SchemaElement[] { sch02, sch12, sch22,
				sch32, sch42 };

		// CountHashState state2 = new CountHashState(schArray2, 4, 4, 60000);
		// CountNLState state2 = new CountNLState(schArray2, 60000);
		//WindowHashState state2 = new WindowHashState(schArray2, 4, 4, 60000);
		WindowNLState state2 = new WindowNLState(schArray2, 60000);

		StreamQueue srcQueue2 = new SingleReaderQueueArrayImp(schArray2);

		
		StreamQueue[] inArray= new StreamQueue[]{srcQueue,srcQueue2};
		StreamState[] statArray= new StreamState[]{state,state2};		
////////////////////////////////////////		
		
		int offset1 = 0;
		SchemaElement sch01 = new IntSchemaElement("col_D1A", Constant.INT_T,
				offset1, Constant.INT_S);
		offset1 += sch01.getLength();
		SchemaElement sch11 = new IntSchemaElement("col_D1B", Constant.INT_T,
				offset1, Constant.INT_S);

		SchemaElement[] schArray1 = new SchemaElement[] { sch01, sch11 };

		StreamQueue srcQueue1 = new SingleReaderQueueArrayImp(schArray1);

		
		StreamQueue[] outArray= new StreamQueue[]{srcQueue1};

///////////////////////////////////////
		
		
		SchemaMap smap = new SchemaMap();
		smap.addEntry(sch0, sch01);
		smap.addEntry(sch02, sch11);

		
///////////////////////////////////////
		
		// --------------------for boolretExp


		Str20Terminal left = new Str20Terminal(sch4);
		Str20Terminal right = new Str20Terminal(sch42);
		Str20CompExp root = new Str20CompExp(
				com.hp.hpl.CHAOS.Expression.Constant.EQ, left, right);

		
///////////////////////////////////////////
		
		StreamBinaryWindowJoinNL join = new StreamBinaryWindowJoinNL(0, inArray, outArray, statArray, root,smap);
		//StreamBinaryWindowJoinHash join = new StreamBinaryWindowJoinHash(0, inArray, outArray,statArray, smap);		
		
		byte[] tuple1 = null;

		
		
		for (int i = 1; i < 1000; i++) {

			tuple1 = StreamTupleCreator.makeTuple(i + ",20,23.3,0.999,TTT",
					schArray);
			StreamAccessor.setMaxTimestamp(tuple1, i);
			StreamAccessor.setMinTimestamp(tuple1, i);
			srcQueue.enqueue(tuple1);
		}

		for (int i = 1; i < 1000; i++) {

			tuple1 = StreamTupleCreator.makeTuple((i+10000) + ",20,23.3,0.999,TTT1",
					schArray);
			StreamAccessor.setMaxTimestamp(tuple1, i+1);
			StreamAccessor.setMinTimestamp(tuple1, i+1);
			srcQueue2.enqueue(tuple1);
		}
		
		long pre = sysPrint(0);
		
		join.run();
		
		for (int ii = 0; ii < 1000000; ii++) {
			byte[] tuple = srcQueue1.dequeue();
			if (tuple==null) break;
			//System.out.println(StreamAccessor.toString(tuple, schArray1));
		}


		sysPrint(pre);

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
