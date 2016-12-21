package com.hp.hpl.CHAOS.Executor;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

import com.hp.hpl.CHAOS.Expression.DoubleCompExp;
import com.hp.hpl.CHAOS.Expression.DoubleConstant;
import com.hp.hpl.CHAOS.Expression.DoubleTerminal;
import com.hp.hpl.CHAOS.Expression.Str20CompExp;
import com.hp.hpl.CHAOS.Expression.Str20Terminal;
import com.hp.hpl.CHAOS.QueryPlan.PlanDAG;
import com.hp.hpl.CHAOS.QueryPlan.PlanNode;
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
import com.hp.hpl.CHAOS.queryplangenerator.QueryPlanGenerator;

public class Test2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		test3();
	}

	private static void test3() {
		int offset = 0;

		QueryPlanGenerator qp = new QueryPlanGenerator();
		PlanDAG queryPlan = qp.generateQueryPlan("test2.xml");
		System.out.println(queryPlan.toString());

		int planSize = queryPlan.getNodes().size();
		// SourceOp
		StreamOperator op = queryPlan.getLeaves().get(0).getOperator();
		// ((StreamSourceOperator) op).setArrivalRate(1000);

		// StreamOperator op2 = queryPlan.getLeaves().get(1).getOperator();
		// ((StreamSourceOperator) op2).setArrivalRate(1000);

		StreamOperator[] opArray = new StreamOperator[] { op };

		// start
		// opArray: just source;
		RunTimeEnvironment re = new RunTimeEnvironment(opArray, "test");

		re.init();

		Scheduler sche = new StreamGeneratorSchedulerBatch(opArray);

		ArrayList<RunTimeEnvironment> reArray = new ArrayList<RunTimeEnvironment>();
		reArray.add(re);

		StreamGeneratorExecutor thread = new StreamGeneratorExecutor(reArray,
				sche, null);

		ArrayList<StreamOperator> opArr = new ArrayList<StreamOperator>();
		for (int i = 0; i < planSize - 1; i++) {
			opArr.add(queryPlan.getNodes().get(i).getOperator());
		}

		StreamOperator[] opArray2 = (StreamOperator[]) opArr
				.toArray(new StreamOperator[] {});

		RunTimeEnvironment re2 = new RunTimeEnvironment(opArray2, "test");

		re2.init();

		Scheduler sche2 = new RoundRobinScheduler(opArray2);

		ArrayList<RunTimeEnvironment> reArray2 = new ArrayList<RunTimeEnvironment>();
		reArray2.add(re2);
		StreamEngineExecutor thread2 = new StreamEngineExecutor(reArray2, sche2);

		long pre = sysPrint(0);

		thread.setEngine(thread2);

		thread.setStart();
		thread2.setStart();

		try {
			Thread.sleep(10000);
			thread.setStop();
			thread2.setStop();
			thread.join();
			thread2.join();
		} catch (InterruptedException e) {

			e.printStackTrace();
		}

		sysPrint(pre);

		System.out.println("==checking finished====");

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
