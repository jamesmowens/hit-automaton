package com.hp.hpl.CHAOS.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.hp.hpl.CHAOS.Executor.Executor;
import com.hp.hpl.CHAOS.Executor.StreamEngineExecutor;
import com.hp.hpl.CHAOS.Executor.StreamGeneratorExecutor;
import com.hp.hpl.CHAOS.Executor.StreamStatisticExecutor;
import com.hp.hpl.CHAOS.QueryPlan.PlanDAG;
import com.hp.hpl.CHAOS.QueryPlan.PlanNode;
import com.hp.hpl.CHAOS.QueryPlan.RunTimeEnvironment;
import com.hp.hpl.CHAOS.Queue.SingleReaderLogQueue;
import com.hp.hpl.CHAOS.Queue.SingleReaderLogQueueSyn;
import com.hp.hpl.CHAOS.Queue.SingleReaderQueueArrayImpSyn;
import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.Scheduler.RoundRobinScheduler;
import com.hp.hpl.CHAOS.Scheduler.Scheduler;
import com.hp.hpl.CHAOS.Scheduler.StreamGeneratorSchedulerBatch;
import com.hp.hpl.CHAOS.StreamOperator.StreamOperator;
import com.hp.hpl.CHAOS.queryplangenerator.QueryPlanGenerator;

//Centralized CHAOS
public class CCHAOS {

	static StreamEngineExecutor se = null;
	static StreamGeneratorExecutor sg = null;
	static StreamStatisticExecutor ssse = null;
	static StreamStatisticExecutor sssg = null;
	static ArrayList<PlanDAG> queryPlans = new ArrayList<PlanDAG>();
	static long pre;

	/**
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws Exception {

		System.out.print("fh");
		printLogo();

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String inputLine = "";
		String input = "";
		while (true) {
			printPrompt();
			inputLine = in.readLine();
			String[] splitString = inputLine.split("\\s+");
			input = splitString[0];

			if (input.equalsIgnoreCase("help")) {
				printMenu();
				continue;
			}
			if (input.equalsIgnoreCase("exit")) {
				cleanup();
				break;
			}
			if (input.equalsIgnoreCase("run")) {
				execute(splitString);
				continue;
			}
			if (input.equalsIgnoreCase("remove")) {
				remove(splitString);
				continue;
			}
			if (input.equalsIgnoreCase("list")) {
				list();
				continue;
			}
			if (input.equalsIgnoreCase("mem")) {
				sysPrint(pre);
				continue;
			}
			if (input.equalsIgnoreCase("stop")) {
				stop();
				continue;
			}
			if (input.equalsIgnoreCase("")) {
				continue;
			}

			System.out.println("Not a valid command");
		}

	}

	private static void remove(String[] splitString) throws Exception {
		ArrayList<String> reName = new ArrayList<String>();

		for (int i = 1; i < splitString.length; i++) {
			String planName = splitString[i];
			for (int index = queryPlans.size() - 1; index >= 0; index--)
				if (queryPlans.get(index).getName().equalsIgnoreCase(planName)) {
					reName.add(planName);
					queryPlans.remove(index);
				}
		}

		if (queryPlans.size() == 0)
			stop();

		if (sg != null || se != null || ssse != null || sssg != null) {

			sg.setReNameToBeRemoArray(reName);
			sssg.setReNameToBeRemoArray(reName);
			sg.waitReNameToBeRemoArray();
			sssg.waitReNameToBeRemoArray();

			se.setReNameToBeRemoArray(reName);
			ssse.setReNameToBeRemoArray(reName);
			se.waitReNameToBeRemoArray();
			ssse.waitReNameToBeRemoArray();
		}

	}

	public static void stop() throws Exception {

		if (sg == null || se == null || ssse == null || sssg == null) {
			System.out.println("No stream generator & engine to stop");
			return;
		}
		sg.setStop();
		sssg.setStop();
		sg.join();
		sssg.join();

		se.setStop();
		ssse.setStop();
		se.join();
		ssse.join();

		se.finalizing();
		sg.finalizing();
		ssse.finalizing();
		sssg.finalizing();
		sysPrint(pre);
		queryPlans.clear();
		se = null;
		sg = null;
		ssse = null;
		sssg = null;

	}

	private static void list() {
		if (queryPlans.size() == 0) {
			System.out.println("No queryplan available");
			return;
		}
		for (PlanDAG queryPlan : queryPlans) {
			System.out.println(queryPlan.getName());
			System.out.println(queryPlan);
			System.out.println();
		}
	}

	public static void execute(String[] splitString) {

		ArrayList<PlanDAG> newQueryPlans = new ArrayList<PlanDAG>();

		for (int i = 1; i < splitString.length; i++) {
			String planName = splitString[i];
			PlanDAG queryPlan = prepare(splitString[i]);
			queryPlan.setName(planName);
			newQueryPlans.add(queryPlan);
		}
		queryPlans.addAll(newQueryPlans);

		if (sg == null || se == null || ssse == null || sssg == null) {
			sg = buildStreamGenerator(buildStreamGeneratorREs(newQueryPlans),
					null);
			se = buildStreamEngine(buildStreamEngineREs(newQueryPlans));

			sg.setEngine(se);

			ssse = new StreamStatisticExecutor(
					new ArrayList<RunTimeEnvironment>(se.getReArray()), 1);
			sssg = new StreamStatisticExecutor(
					new ArrayList<RunTimeEnvironment>(sg.getReArray()), 1);

			se.init();
			sg.init();
			ssse.init();
			sssg.init();
			pre = sysPrint(0);

			se.setStart();
			ssse.setStart();

			se.waitStart();
			ssse.waitStart();

			sg.setStart();
			sssg.setStart();

			sg.waitStart();
			sssg.waitStart();

		} else {

			ArrayList<RunTimeEnvironment> reStreamEngineArray = new ArrayList<RunTimeEnvironment>();
			ArrayList<RunTimeEnvironment> reStreamGeneratorArray = new ArrayList<RunTimeEnvironment>();

			for (PlanDAG queryPlan : newQueryPlans) {
				reStreamEngineArray.add(buildStreamEngineRE(queryPlan));
				reStreamGeneratorArray.add(buildStreamGeneratorRE(queryPlan));
			}

			se.setReToBeAdded(reStreamEngineArray);
			ssse.setReToBeAdded(reStreamEngineArray);

			se.waitReToBeAdded();
			ssse.waitReToBeAdded();

			sg.setReToBeAdded(reStreamGeneratorArray);
			sssg.setReToBeAdded(reStreamGeneratorArray);

			sg.waitReToBeAdded();
			sssg.waitReToBeAdded();

		}

	}

	private static ArrayList<RunTimeEnvironment> buildStreamEngineREs(
			ArrayList<PlanDAG> queryPlans2) {
		ArrayList<RunTimeEnvironment> ret = new ArrayList<RunTimeEnvironment>();
		for (PlanDAG queryPlan : queryPlans2)
			ret.add(buildStreamEngineRE(queryPlan));
		return ret;
	}

	private static ArrayList<RunTimeEnvironment> buildStreamGeneratorREs(
			ArrayList<PlanDAG> queryPlans2) {
		ArrayList<RunTimeEnvironment> ret = new ArrayList<RunTimeEnvironment>();
		for (PlanDAG queryPlan : queryPlans2)
			ret.add(buildStreamGeneratorRE(queryPlan));
		return ret;
	}

	private static void cleanup() throws InterruptedException {
		System.out.println("CCHAOS shutdown...");
		if (sg != null && se != null && ssse != null) {
			sg.setStop();
			sssg.setStop();
			sg.join();
			sssg.join();

			se.setStop();
			ssse.setStop();
			se.join();
			ssse.join();

			se.finalizing();
			sg.finalizing();
			ssse.finalizing();
			sssg.finalizing();
		}
		System.out.println("done");
	}

	private static void printMenu() {
		System.out.println("Menu:");
		System.out.println("help: show this menu");
		System.out.println("exit: exit chaos");
		System.out
				.println("run <queryfilename> [<queryfilename>+]: generate query plan from XML file");
		System.out.println("list: list queryplan");
		System.out.println("stop: stop stream generator & engine");
		System.out.println("mem: show runtime mem info");
	}

	private static void printPrompt() {
		System.out.print("CCHAOS:");
	}

	private static void printLogo() {
		System.out.println("Centralized CHAOS Engine. Type help for menu");
	}

	public static PlanDAG prepare(String queryFileName) {

		QueryPlanGenerator qg = new QueryPlanGenerator();
		PlanDAG queryPlan = qg.generateQueryPlan(queryFileName);
		System.out.println(queryFileName);
		System.out.println(queryPlan);
		return queryPlan;
	}

	public static RunTimeEnvironment buildStreamGeneratorRE(PlanDAG queryPlan) {
		for (PlanNode childNode : queryPlan.getLeaves()) {
			int parentIndex = 0;
			for (PlanNode parentNode : childNode.getParent()) {
				StreamOperator childOp = childNode.getOperator();
				StreamOperator parentOp = parentNode.getOperator();
				StreamQueue queueNotSyn = childOp.getOutputQueueArray()[parentIndex];
				int childIndex = findChildIndex(childNode, parentNode
						.getChildren());

				StreamQueue queueSyn = null;
				if (queueNotSyn instanceof SingleReaderLogQueue)
					queueSyn = new SingleReaderLogQueueSyn(queueNotSyn
							.getSchema(), ((SingleReaderLogQueue) queueNotSyn)
							.getLogFileName());
				else
					queueSyn = new SingleReaderQueueArrayImpSyn(queueNotSyn
							.getSchema());

				childOp.setOutputQueue(queueSyn, parentIndex);
				parentOp.setInputQueue(queueSyn, childIndex);

				parentIndex++;
			}
		}
		StreamOperator[] opArray = new StreamOperator[queryPlan.getLeaves()
				.size()];
		for (int i = 0; i < opArray.length; i++)
			opArray[i] = queryPlan.getLeaves().get(i).getOperator();

		RunTimeEnvironment re = new RunTimeEnvironment(opArray, queryPlan
				.getName());
		return re;
	}

	private static int findChildIndex(PlanNode childNode,
			ArrayList<PlanNode> children) {
		int ret = -1;
		for (int i = 0; i < children.size(); i++)
			if (childNode == children.get(i)) {
				ret = i;
				break;
			}

		return ret;
	}

	public static RunTimeEnvironment buildStreamEngineRE(PlanDAG queryPlan) {

		StreamOperator[] opArray = new StreamOperator[queryPlan.getNodes()
				.size()
				- queryPlan.getLeaves().size()];
		int index = 0;
		for (int i = 0; i < queryPlan.getNodes().size(); i++) {
			PlanNode node = queryPlan.getNodes().get(i);
			if (queryPlan.getLeaves().contains(node))
				continue;
			opArray[index++] = node.getOperator();
		}
		RunTimeEnvironment re = new RunTimeEnvironment(opArray, queryPlan
				.getName());
		return re;
	}

	public static StreamGeneratorExecutor buildStreamGenerator(
			ArrayList<RunTimeEnvironment> reArray, Executor engine) {
		ArrayList<StreamOperator> opArray = new ArrayList<StreamOperator>();
		for (RunTimeEnvironment re : reArray)
			for (int i = 0; i < re.getOperators().length; i++)
				opArray.add(re.getOperators()[i]);
		Scheduler sche = new StreamGeneratorSchedulerBatch(opArray
				.toArray(new StreamOperator[] {}));
		return new StreamGeneratorExecutor(reArray, sche, engine);
	}

	public static StreamEngineExecutor buildStreamEngine(
			ArrayList<RunTimeEnvironment> reArray) {
		ArrayList<StreamOperator> opArray = new ArrayList<StreamOperator>();
		for (RunTimeEnvironment re : reArray)
			for (int i = 0; i < re.getOperators().length; i++)
				opArray.add(re.getOperators()[i]);
		Scheduler sche = new RoundRobinScheduler(opArray
				.toArray(new StreamOperator[] {}));
		return new StreamEngineExecutor(reArray, sche);
	}

	public static long sysPrint(long pre) {
		Runtime rt = Runtime.getRuntime();
		DecimalFormat df = new DecimalFormat("0.00#");
		long ret = System.currentTimeMillis();
		// System.gc();
		System.out.println("------------- Memory Information -------------");
		System.out.println("Time: " + (pre == 0 ? 0 : (ret - pre)));

		System.out.println("Max memory: "
				+ df.format((rt.maxMemory() / 1024.0 / 1024.0)) + "MB");
		System.out
				.println("Used memory: "
						+ df
								.format(((rt.totalMemory() - rt.freeMemory()) / 1024.0 / 1024.0))
						+ "MB");
		System.out.println("----------------------------------------------");
		return ret;
	}

}
