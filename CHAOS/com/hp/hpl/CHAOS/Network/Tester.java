package com.hp.hpl.CHAOS.Network;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

import com.hp.hpl.CHAOS.Executor.StreamEngineExecutor;
import com.hp.hpl.CHAOS.Executor.StreamGeneratorExecutor;
import com.hp.hpl.CHAOS.Expression.DoubleCompExp;
import com.hp.hpl.CHAOS.Expression.DoubleConstant;
import com.hp.hpl.CHAOS.Expression.DoubleTerminal;
import com.hp.hpl.CHAOS.Expression.Str20CompExp;
import com.hp.hpl.CHAOS.Expression.Str20Terminal;
import com.hp.hpl.CHAOS.QueryPlan.RunTimeEnvironment;
import com.hp.hpl.CHAOS.Queue.SingleReaderQueueArrayImp;
import com.hp.hpl.CHAOS.Queue.SingleReaderQueueArrayImpSyn;
import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.Scheduler.DummyRoundRobinScheduler;
import com.hp.hpl.CHAOS.Scheduler.RoundRobinScheduler;
import com.hp.hpl.CHAOS.Scheduler.Scheduler;
import com.hp.hpl.CHAOS.Scheduler.StreamGeneratorSchedulerBatch;
import com.hp.hpl.CHAOS.Scheduler.StreamGeneratorSchedulerRealTimeClock;
import com.hp.hpl.CHAOS.State.CountNLState;
import com.hp.hpl.CHAOS.State.StreamState;
import com.hp.hpl.CHAOS.State.WindowHashState;
import com.hp.hpl.CHAOS.State.WindowNLState;
import com.hp.hpl.CHAOS.Statistics.StatisticElement;
import com.hp.hpl.CHAOS.StreamData.Constant;
import com.hp.hpl.CHAOS.StreamData.DoubleSchemaElement;
import com.hp.hpl.CHAOS.StreamData.IntSchemaElement;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.SchemaMap;
import com.hp.hpl.CHAOS.StreamData.Str20SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;
import com.hp.hpl.CHAOS.StreamData.StreamTupleCreator;
import com.hp.hpl.CHAOS.StreamOperator.StreamOperator;
import com.hp.hpl.CHAOS.StreamOperator.StreamSelectOperator;
import com.hp.hpl.CHAOS.StreamOperator.StreamSinkOperator;
import com.hp.hpl.CHAOS.StreamOperator.StreamSinkOperatorDiscard;
import com.hp.hpl.CHAOS.StreamOperator.StreamSinkOperatorDemo;
import com.hp.hpl.CHAOS.StreamOperator.StreamSourceOperator;
import com.hp.hpl.CHAOS.StreamWrapper.TestFormat;

public class Tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		test3();

	}

	private static void test3() {
		int offset = 0;
		SchemaElement sch2 = new DoubleSchemaElement("col_C",
				Constant.DOUBLE_T, offset, Constant.DOUBLE_S);
		offset += sch2.getLength();
		SchemaElement sch3 = new DoubleSchemaElement("col_D",
				Constant.DOUBLE_T, offset, Constant.DOUBLE_S);

		SchemaElement[] schArray = new SchemaElement[] { sch2, sch3 };

		StreamQueue queue = new SingleReaderQueueArrayImpSyn(schArray);

		StreamQueue queueNet = new NetworkOutputQueueSocketChannelImpl(queue,
				"localhost",
				com.hp.hpl.CHAOS.Network.Constant.TUPLE_RECEIVER_PORT);

		StreamQueue queue2 = new SingleReaderQueueArrayImpSyn(schArray);

		StreamQueue queueNet2 = new NetworkOutputQueueSocketImpl(queue2,
				"localhost",
				com.hp.hpl.CHAOS.Network.Constant.TUPLE_RECEIVER_PORT);

		StreamOperator op = new StreamSourceOperator(1, null,
				new StreamQueue[] { queueNet }, 20000, new TestFormat(
						".\\resource\\stream.txt", true, 1));

		StreamOperator op2 = new StreamSourceOperator(2, null,
				new StreamQueue[] { queueNet2 }, 10000, new TestFormat(
						".\\resource\\stream2.txt", true, 1));

		op.setStatisticArray(new StatisticElement[0]);
		op2.setStatisticArray(new StatisticElement[0]);

		StreamOperator[] opArray = new StreamOperator[] { op };

		// RunTimeEnvironment re = new RunTimeEnvironment(opArray, null,
		// new StreamQueue[] { queueNet });
		RunTimeEnvironment re = new RunTimeEnvironment(opArray, "test");

		// re.init();

		Scheduler sche = new StreamGeneratorSchedulerBatch(opArray);

		ArrayList<RunTimeEnvironment> reArray = new ArrayList<RunTimeEnvironment>();
		reArray.add(re);

		StreamGeneratorExecutor thread = new StreamGeneratorExecutor(reArray,
				sche, null);

		// //////////////////////////////////////////////

		DoubleTerminal left = new DoubleTerminal(sch2);
		DoubleConstant right = new DoubleConstant(0.5);
		DoubleCompExp root = new DoubleCompExp(
				com.hp.hpl.CHAOS.Expression.Constant.GT, left, right);

		SingleReaderQueueArrayImp queueOut = new SingleReaderQueueArrayImp(
				schArray);

		StreamSelectOperator selectOp = new StreamSelectOperator(3,
				new StreamQueue[] { queue }, new StreamQueue[] { queueOut },
				root);

		StreamOperator sinkOp = new StreamSinkOperatorDemo(4,
				new StreamQueue[] { queueOut }, new StreamQueue[] {},
				"result.txt");

		selectOp.setStatisticArray(new StatisticElement[0]);
		sinkOp.setStatisticArray(new StatisticElement[0]);

		StreamOperator[] opArray2 = new StreamOperator[] { selectOp, sinkOp };

		// RunTimeEnvironment re2 = new RunTimeEnvironment(opArray2,
		// new StreamQueue[] { queue }, null);
		RunTimeEnvironment re2 = new RunTimeEnvironment(opArray2, "test");

		// re2.init();

		Scheduler sche2 = new DummyRoundRobinScheduler(opArray2);

		ArrayList<RunTimeEnvironment> reArray2 = new ArrayList<RunTimeEnvironment>();
		reArray2.add(re2);
		StreamEngineExecutor thread2 = new StreamEngineExecutor(reArray2, sche2);

		// ////////////////////////////////////////

		// NetworkTupleReceiverSocketChannelImpl ntr = new
		// NetworkTupleReceiverSocketChannelImpl(
		// re2, null);
		ArrayList<RunTimeEnvironment> reArrayList = new ArrayList<RunTimeEnvironment>();
		reArrayList.add(re2);

		NetworkTupleReceiverSocketImpl ntr = new NetworkTupleReceiverSocketImpl(
				reArrayList, null);

		// ///////////////////////////////////////

		int ret = thread2.init();

		ret = ntr.init();

		// /////////////////////////////////////////

		long pre = sysPrint(0);

		// thread.setEngine(thread2);

		thread2.setStart();
		ntr.setStart();

		ret = thread.init();

		thread.setStart();

		try {
			Thread.sleep(10000);
			thread.setStop();
			ntr.setStop();
			thread2.setStop();
			thread.join();
			ntr.join();
			thread2.join();
		} catch (InterruptedException e) {

			e.printStackTrace();
		}

		sysPrint(pre);

		System.out.println(queue.getSize());
		// System.out.println(queue2.getSize());

		// for (int i=0;i<queue.getSize();i++){
		// System.out.println(StreamAccessor.toString(queue.dequeue(),schArray));
		// }
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
}
