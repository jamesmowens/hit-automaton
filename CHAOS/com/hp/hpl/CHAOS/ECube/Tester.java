package com.hp.hpl.CHAOS.ECube;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import com.hp.hpl.CHAOS.StreamData.*;

public class Tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		test7();

	}

	private static void test7() {
		// we will create 3 event active stacks for event type A, B, C
		// we want A follow C and B follow C
		// for event active stack A and B, the pointerArray has null pointer
		// since no precedent.
		// for event active stack C, the pointer Array has two pointers, one
		// points to type A and the other type B
		// all 3 event active stacks share the same schema.
		// all the tuples are read from the file.

		int offset = 0;
		SchemaElement sch1 = new IntSchemaElement("col_A", Constant.INT_T,
				offset, Constant.INT_S);
		offset += sch1.getLength();
		SchemaElement sch2 = new DoubleSchemaElement("col_B",
				Constant.DOUBLE_T, offset, Constant.DOUBLE_S);
		offset += sch2.getLength();
		SchemaElement sch3 = new Str20SchemaElement("col_C", Constant.STR20_T,
				offset, Constant.STR20_S);

		SchemaElement[] schArray = new SchemaElement[] { sch1, sch2, sch3 };

		// step 2, build event active stack.
		int pointerSizeA = 0;// by default is null
		int pointerSizeB = 1;// need manual set null, then same effect as 0
		int pointerSizeC = 2;
		EventActiveInstanceQueue aisA = new EventActiveInstanceQueue(schArray,
				pointerSizeA, "A");
		EventActiveInstanceQueue aisB = new EventActiveInstanceQueue(schArray,
				pointerSizeB, "B");
		EventActiveInstanceQueue aisC = new EventActiveInstanceQueue(schArray,
				pointerSizeC, "C");

		try {
			// System.out.println(System.currentTimeMillis());

			// step 3, read tuples from file and fill in the stacks
			// eventually the stacks looks like:
			// stackA stackB stackC
			// null<-t0 null<-t1 (t0,t1)<-t2
			// null<-t3 null<-t4 (t3,t4)<-t5
			// null<-t6 null<-t7 (t6,t7)<-t8
			// ...
			// total 1500 tuples. and each stack 500 tuples
			// note push(...) and enqueue(...) is the same
			// note pop(...) and dequeueLast(...) is the same

			FileInputStream fstream = new FileInputStream(".\\streama.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			for (int j = 0; j < 500; j++) {
				byte[] tupleA = null;
				byte[] tupleB = null;
				byte[] tupleC = null;
				if ((strLine = br.readLine()) != null) {
					tupleA = StreamTupleCreator.makeTuple(strLine, schArray,
							"\t");
					aisA.enqueue(tupleA);// default null
				}
				if ((strLine = br.readLine()) != null) {
					tupleB = StreamTupleCreator.makeTuple(strLine, schArray,
							"\t");
					byte[][] pointerArrayB = new byte[pointerSizeB][];
					pointerArrayB[0] = null;// manual set null
					aisB.enqueue(tupleB, pointerArrayB);
				}
				if ((strLine = br.readLine()) != null) {
					tupleC = StreamTupleCreator.makeTuple(strLine, schArray,
							"\t");
					byte[][] pointerArrayC = new byte[pointerSizeC][];
					pointerArrayC[0] = tupleA;
					pointerArrayC[1] = tupleB;
					aisC.enqueue(tupleC, pointerArrayC);
				}
			}

			br.close();
			in.close();
			fstream.close();

			// step 4 testing index access
			// note: if you dequeue/dequeuelast/pop a tuple, then the index in
			// the tuple will gone since it does not exist in the stack anymore,
			// the index is non-valid.
			// so we use peek/peekLast instead.
			// we also need a pointerArray to accept the returned pointers.

			byte[][] retPointerArrayC = new byte[pointerSizeC][];

			// peek() get the first/oldest
			byte[] tupleC = aisC.peek(retPointerArrayC);
			int index = StreamAccessor.getIndex(tupleC);
			// tupleC is get by queue operation, tupleC2 is access by index,
			// tupleC and tupleC2 should be references to the
			// same tuple
			System.out.print("index" + index);
			byte[] tupleC2 = aisC.getByPhysicalIndex(index, retPointerArrayC);
			/*
			 * System.out.println(StreamAccessor.toString(tupleC, schArray));
			 * System.out.println(StreamAccessor.toString(tupleC2, schArray));
			 * System.out.println(tupleC == tupleC2);// must be true
			 */
			// peekLast() get the last/newest
			tupleC = aisC.peekLast(retPointerArrayC);
			index = StreamAccessor.getIndex(tupleC);
			// tupleC is get by queue operation, tupleC2 is access by index,
			// tupleC and tupleC2 should be references to the
			// same tuple
			tupleC2 = aisC.getByPhysicalIndex(index, retPointerArrayC);
			/*
			 * System.out.println(StreamAccessor.toString(tupleC, schArray));
			 * System.out.println(StreamAccessor.toString(tupleC2, schArray));
			 * System.out.println(tupleC == tupleC2);// must be true
			 * 
			 * System.out.println("Finish testing index access");
			 */

			// step 5.1 testing vertical traverse
			tupleC = aisC.peekLast(retPointerArrayC);
			while (tupleC != null) {
				// System.out.println(StreamAccessor.toString(tupleC,
				// schArray));
				index = StreamAccessor.getIndex(tupleC);
				tupleC = aisC.getPreviousByPhysicalIndex(index,
						retPointerArrayC);
			}
			// System.out.println("Finish testing vertical traverse in Stack
			// C");

			// step 6 testing horizontal traverse

			tupleC = aisC.peekLast(retPointerArrayC);
			byte[] tupleA = retPointerArrayC[0];
			byte[] tupleB = retPointerArrayC[1];

			// if (tupleA != null)
			// System.out.println(StreamAccessor.toString(tupleA, schArray));
			// if (tupleB != null)
			// System.out.println(StreamAccessor.toString(tupleB, schArray));
			// System.out.println("Finish testing horizontal traverse");

			// System.out.println(System.currentTimeMillis());

			// step 7 considering constructing results

			int offset_result = 0;
			SchemaElement sch01 = new IntSchemaElement("col_A", Constant.INT_T,
					offset_result, Constant.INT_S);
			offset_result += sch01.getLength();
			SchemaElement sch02 = new DoubleSchemaElement("col_B",
					Constant.DOUBLE_T, offset_result, Constant.DOUBLE_S);
			offset_result += sch02.getLength();
			SchemaElement sch03 = new Str20SchemaElement("col_C",
					Constant.STR20_T, offset_result, Constant.STR20_S);

			offset_result += sch03.getLength();
			// ============second element

			SchemaElement sch11 = new IntSchemaElement("col_A", Constant.INT_T,
					offset_result, Constant.INT_S);
			offset_result += sch11.getLength();
			SchemaElement sch12 = new DoubleSchemaElement("col_B",
					Constant.DOUBLE_T, offset_result, Constant.DOUBLE_S);
			offset_result += sch12.getLength();
			SchemaElement sch13 = new Str20SchemaElement("col_C",
					Constant.STR20_T, offset_result, Constant.STR20_S);

			offset_result += sch13.getLength();
			// ============third element

			SchemaElement sch21 = new IntSchemaElement("col_A", Constant.INT_T,
					offset_result, Constant.INT_S);
			offset_result += sch21.getLength();
			SchemaElement sch22 = new DoubleSchemaElement("col_B",
					Constant.DOUBLE_T, offset_result, Constant.DOUBLE_S);
			offset_result += sch22.getLength();
			SchemaElement sch23 = new Str20SchemaElement("col_C",
					Constant.STR20_T, offset_result, Constant.STR20_S);

			// SchemaElement[] schArray_Result = new SchemaElement[] {sch01,
			// sch02, sch03, sch11, sch12, sch13, sch21, sch22, sch23};

			SchemaElement[] schArray_Result = new SchemaElement[] { sch01,
					sch02, sch03, sch11, sch12, sch13, sch21, sch22, sch23 };

			if (false) {
				
				SchemaElement sch1A = (SchemaElement) sch1;
				SchemaElement sch1B = (SchemaElement) sch1.clone();
				SchemaElement sch1C = (SchemaElement) sch1.clone();
				SchemaElement sch2A = (SchemaElement) sch2;
				SchemaElement sch2B = (SchemaElement) sch2.clone();
				SchemaElement sch2C = (SchemaElement) sch2.clone();
				SchemaElement sch3A = (SchemaElement) sch3;
				SchemaElement sch3B = (SchemaElement) sch3.clone();
				SchemaElement sch3C = (SchemaElement) sch3.clone();

				SchemaElement[] schArrayA = new SchemaElement[] { sch1A, sch2A,
						sch3A };
				SchemaElement[] schArrayB = new SchemaElement[] { sch1B, sch2B,
						sch3B };
				SchemaElement[] schArrayC = new SchemaElement[] { sch1C, sch2C,
						sch3C };

				SchemaMap smap = new SchemaMap();
				smap.addEntry(sch1, sch01);
				smap.addEntry(sch2, sch02);
				smap.addEntry(sch3, sch03);

				smap.addEntry(sch1B, sch11);
				smap.addEntry(sch2B, sch12);
				smap.addEntry(sch3B, sch13);

				smap.addEntry(sch1C, sch21);
				smap.addEntry(sch2C, sch22);
				smap.addEntry(sch3C, sch23);

				byte[] dest = StreamTupleCreator
						.makeEmptyTuple(schArray_Result);

				StreamTupleCreator.tupleCopy(dest, tupleA, schArrayA, smap);

				StreamTupleCreator.tupleCopy(dest, tupleB, schArrayB, smap);

				StreamTupleCreator.tupleCopy(dest, tupleC, schArrayC, smap);

				// setTimestamp(tuple, stateTuple, dest);

				System.out.println("tuple A"
						+ StreamAccessor.toString(tupleA, schArrayA));
				System.out.println("tuple B"
						+ StreamAccessor.toString(tupleB, schArrayB));
				System.out.println("tuple C"
						+ StreamAccessor.toString(tupleC, schArrayC));
				System.out.println("Result"
						+ StreamAccessor.toString(dest, schArray_Result));
			} else {

				byte[] dest = StreamTupleCreator
						.makeEmptyTuple(schArray_Result);
				StreamTupleCreator.tupleAppend(dest, tupleA, sch01.getOffset());
				StreamTupleCreator.tupleAppend(dest, tupleB, sch11.getOffset());
				StreamTupleCreator.tupleAppend(dest, tupleC, sch21.getOffset());

				// setTimestamp(tuple, stateTuple, dest);

				System.out.println("tuple A"
						+ StreamAccessor.toString(tupleA, schArray));
				System.out.println("tuple B"
						+ StreamAccessor.toString(tupleB, schArray));
				System.out.println("tuple C"
						+ StreamAccessor.toString(tupleC, schArray));
				System.out.println("Result"
						+ StreamAccessor.toString(dest, schArray_Result));

			}
			//step 8: test get the ith tuple from a specified stack. 
			

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		
		

	}
}
