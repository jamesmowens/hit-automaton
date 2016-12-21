package com.hp.hpl.CHAOS.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.hp.hpl.CHAOS.CommandProcess.CmdMsg;
import com.hp.hpl.CHAOS.CommandProcess.CmdReceiver;
import com.hp.hpl.CHAOS.CommandProcess.ExecuteCmd;
import com.hp.hpl.CHAOS.CommandProcess.ExitCmd;
import com.hp.hpl.CHAOS.CommandProcess.RemoveCmd;
import com.hp.hpl.CHAOS.CommandProcess.StopCmd;
import com.hp.hpl.CHAOS.Executor.Executor;
import com.hp.hpl.CHAOS.Executor.StreamEngineExecutor;
import com.hp.hpl.CHAOS.Executor.StreamGeneratorExecutor;
import com.hp.hpl.CHAOS.Executor.StreamStatisticExecutor;
import com.hp.hpl.CHAOS.Network.NetworkOutputQueueSocketChannelImpl;
import com.hp.hpl.CHAOS.Network.NetworkOutputQueueSocketChannelImplLog;
import com.hp.hpl.CHAOS.Network.NetworkTupleReceiverSocketChannelImpl;
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

public class DCHAOS extends CmdReceiver {

	public StreamEngineExecutor se = null;
	public StreamStatisticExecutor ssse = null;
	public NetworkTupleReceiverSocketChannelImpl sg = null;
	public ArrayList<PlanDAG> queryPlans = new ArrayList<PlanDAG>();
	public long pre;

	public Socket clientSocket = null;
	public ObjectInputStream in = null;
	public ObjectOutputStream out = null;
	public int port = Constant.CMD_RECEIVER_PORT;

	public int tuplePort = com.hp.hpl.CHAOS.Network.Constant.TUPLE_RECEIVER_PORT;
	public String hostName = "";

	/**
	 * @param args
	 * @throws Exception
	 * @throws IOException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) {
		DCHAOS dCHAOS = new DCHAOS();
		try {
			dCHAOS.run(args);
		} catch (Exception e) {
		}
	}

	public void run(String[] args) throws Exception {
		try {
			java.net.InetAddress localMachine = java.net.InetAddress
					.getLocalHost();
			hostName = localMachine.getHostName();
		} catch (java.net.UnknownHostException ex) {
		}

		String dsgHostName = "localhost";
		if (args.length > 0)
			dsgHostName = args[0];

		printLogo();

		System.out.println("Connecting to Stream Generator.");

		try {
			clientSocket = new Socket(dsgHostName, port);
			out = new ObjectOutputStream(clientSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(clientSocket.getInputStream());
		} catch (IOException e) {
			System.out.println("Cannot Connect to Stream Generator.");
			return;
		}

		System.out.println("Connected");

		BufferedReader inLine = new BufferedReader(new InputStreamReader(
				System.in));
		String inputLine = "";
		String input = "";

		while (true) {
			printPrompt();
			inputLine = inLine.readLine();
			String[] splitString = inputLine.split("\\s+");
			input = splitString[0];

			if (input.equalsIgnoreCase("help")) {
				printMenu();
				continue;
			}
			if (input.equalsIgnoreCase("exit")) {
				CmdMsg cmdMsg = new ExitCmd();
				out.writeObject(cmdMsg);
				out.flush();

				CmdMsg cmdMsgACK = (CmdMsg) in.readObject();
				this.accept(cmdMsgACK);

				cleanup();
				break;
			}
			if (input.equalsIgnoreCase("run")) {
				execute(splitString);

				CmdMsg cmdMsgACK = (CmdMsg) in.readObject();
				this.accept(cmdMsgACK);

				continue;
			}
			if (input.equalsIgnoreCase("remove")) {

				String[] sentStr = new String[splitString.length - 1];
				for (int i = 0; i < sentStr.length; i++)
					sentStr[i] = splitString[i + 1];
				CmdMsg cmdMsg = new RemoveCmd(sentStr);
				out.writeObject(cmdMsg);
				out.flush();

				CmdMsg cmdMsgACK = (CmdMsg) in.readObject();
				this.accept(cmdMsgACK);

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
				CmdMsg cmdMsg = new StopCmd();
				out.writeObject(cmdMsg);
				out.flush();

				CmdMsg cmdMsgACK = (CmdMsg) in.readObject();
				this.accept(cmdMsgACK);

				stop();
				continue;
			}
			if (input.equalsIgnoreCase("")) {
				continue;
			}

			System.out.println("Not a valid command");
		}

	}

	private void remove(String[] splitString) throws Exception {
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

		if (sg != null || se != null || ssse != null) {
			sg.setReNameToBeRemoArray(reName);
			sg.waitReNameToBeRemoArray();

			se.setReNameToBeRemoArray(reName);
			ssse.setReNameToBeRemoArray(reName);
			se.waitReNameToBeRemoArray();
			ssse.waitReNameToBeRemoArray();
		}

	}

	private void stop() throws Exception {

		if (sg == null || se == null || ssse == null) {
			System.out.println("No stream generator & engine to stop");
			return;
		}

		sg.setStop();
		sg.join();

		se.setStop();
		ssse.setStop();
		se.join();
		ssse.join();

		se.finalizing();
		sg.finalizing();
		ssse.finalizing();

		sysPrint(pre);
		queryPlans.clear();
		se = null;
		sg = null;
		ssse = null;

	}

	private void list() {
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

	private void execute(String[] splitString) throws Exception {

		ArrayList<PlanDAG> newQueryPlans = new ArrayList<PlanDAG>();

		for (int i = 1; i < splitString.length; i++) {
			String planName = splitString[i];
			PlanDAG queryPlan = prepare(splitString[i]);
			queryPlan.setName(planName);
			newQueryPlans.add(queryPlan);
		}
		queryPlans.addAll(newQueryPlans);

		CmdMsg cmdMsg = new ExecuteCmd(buildStreamGeneratorREs(newQueryPlans));

		if (sg == null || se == null || ssse == null) {
			se = buildStreamEngine(buildStreamEngineREs(newQueryPlans));
			sg = new NetworkTupleReceiverSocketChannelImpl(
					new ArrayList<RunTimeEnvironment>(se.getReArray()), se);

			ssse = new StreamStatisticExecutor(
					new ArrayList<RunTimeEnvironment>(se.getReArray()), 1);

			se.init();
			sg.init();
			ssse.init();
			pre = sysPrint(0);

			se.setStart();
			ssse.setStart();
			se.waitStart();
			ssse.waitStart();

			sg.setStart();
			sg.waitStart();
		} else {

			ArrayList<RunTimeEnvironment> reStreamEngineArray = new ArrayList<RunTimeEnvironment>();

			for (PlanDAG queryPlan : newQueryPlans) {
				reStreamEngineArray.add(buildStreamEngineRE(queryPlan));
			}

			se.setReToBeAdded(reStreamEngineArray);
			ssse.setReToBeAdded(new ArrayList<RunTimeEnvironment>(
					reStreamEngineArray));
			se.waitReToBeAdded();
			ssse.waitReToBeAdded();

			sg.setReToBeAdded(new ArrayList<RunTimeEnvironment>(
					reStreamEngineArray));
			sg.waitReToBeAdded();
		}
		out.writeObject(cmdMsg);
		out.flush();
	}

	private ArrayList<RunTimeEnvironment> buildStreamEngineREs(
			ArrayList<PlanDAG> queryPlans2) {
		ArrayList<RunTimeEnvironment> ret = new ArrayList<RunTimeEnvironment>();
		for (PlanDAG queryPlan : queryPlans2)
			ret.add(buildStreamEngineRE(queryPlan));
		return ret;
	}

	private ArrayList<RunTimeEnvironment> buildStreamGeneratorREs(
			ArrayList<PlanDAG> queryPlans2) {
		ArrayList<RunTimeEnvironment> ret = new ArrayList<RunTimeEnvironment>();
		for (PlanDAG queryPlan : queryPlans2)
			ret.add(buildStreamGeneratorRE(queryPlan));
		return ret;
	}

	private void cleanup() throws InterruptedException {
		System.out.println("DCHAOS shutdown...");
		if (sg != null && se != null && ssse != null) {
			sg.setStop();
			sg.join();

			se.setStop();
			ssse.setStop();
			se.join();
			ssse.join();

			se.finalizing();
			sg.finalizing();
			ssse.finalizing();
		}

		try {
			out.close();
			clientSocket.close();
		} catch (IOException e) {
		}

		System.out.println("done");
	}

	private void printMenu() {
		System.out.println("Menu:");
		System.out.println("help: show this menu");
		System.out.println("exit: exit chaos");
		System.out
				.println("run <queryfilename> [<queryfilename>+]: generate query plan from XML file");
		System.out.println("list: list queryplan");
		System.out.println("stop: stop stream generator & engine");
		System.out.println("mem: show runtime mem info");
	}

	private void printPrompt() {
		System.out.print("DCHAOS:");
	}

	private void printLogo() {
		System.out
				.println("CHAOS Engine with Distributed Stream Generator. Type help for menu");
	}

	public PlanDAG prepare(String queryFileName) {

		QueryPlanGenerator qg = new QueryPlanGenerator();
		PlanDAG queryPlan = qg.generateQueryPlan(queryFileName);
		System.out.println(queryFileName);
		System.out.println(queryPlan);
		return queryPlan;
	}

	public RunTimeEnvironment buildStreamGeneratorRE(PlanDAG queryPlan) {
		for (PlanNode childNode : queryPlan.getLeaves()) {
			int parentIndex = 0;
			for (PlanNode parentNode : childNode.getParent()) {
				StreamOperator childOp = childNode.getOperator();
				StreamOperator parentOp = parentNode.getOperator();
				StreamQueue queueNotSyn = childOp.getOutputQueueArray()[parentIndex];
				int childIndex = findChildIndex(childNode, parentNode
						.getChildren());

				StreamQueue queueNet = null;
				if (queueNotSyn instanceof SingleReaderLogQueue)
					queueNet = new NetworkOutputQueueSocketChannelImplLog(
							queueNotSyn, hostName, tuplePort, "send-"
									+ ((SingleReaderLogQueue) queueNotSyn)
											.getLogFileName());
				else
					queueNet = new NetworkOutputQueueSocketChannelImpl(
							queueNotSyn, hostName, tuplePort);

				queueNet.setQueueID(queueNotSyn.getQueueID());

				StreamQueue queueSyn = null;
				if (queueNotSyn instanceof SingleReaderLogQueue)
					queueSyn = new SingleReaderLogQueueSyn(queueNotSyn
							.getSchema(), "receive-"
							+ ((SingleReaderLogQueue) queueNotSyn)
									.getLogFileName());
				else
					queueSyn = new SingleReaderQueueArrayImpSyn(queueNotSyn
							.getSchema());

				queueSyn.setQueueID(queueNotSyn.getQueueID());

				childOp.setOutputQueue(queueNet, parentIndex);
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

	private int findChildIndex(PlanNode childNode, ArrayList<PlanNode> children) {
		int ret = -1;
		for (int i = 0; i < children.size(); i++)
			if (childNode == children.get(i)) {
				ret = i;
				break;
			}

		return ret;
	}

	public RunTimeEnvironment buildStreamEngineRE(PlanDAG queryPlan) {

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

	public StreamGeneratorExecutor buildStreamGenerator(
			ArrayList<RunTimeEnvironment> reArray, Executor engine) {
		ArrayList<StreamOperator> opArray = new ArrayList<StreamOperator>();
		for (RunTimeEnvironment re : reArray)
			for (int i = 0; i < re.getOperators().length; i++)
				opArray.add(re.getOperators()[i]);
		Scheduler sche = new StreamGeneratorSchedulerBatch(opArray
				.toArray(new StreamOperator[] {}));
		return new StreamGeneratorExecutor(reArray, sche, engine);
	}

	public StreamEngineExecutor buildStreamEngine(
			ArrayList<RunTimeEnvironment> reArray) {
		ArrayList<StreamOperator> opArray = new ArrayList<StreamOperator>();
		for (RunTimeEnvironment re : reArray)
			for (int i = 0; i < re.getOperators().length; i++)
				opArray.add(re.getOperators()[i]);
		Scheduler sche = new RoundRobinScheduler(opArray
				.toArray(new StreamOperator[] {}));
		return new StreamEngineExecutor(reArray, sche);
	}

	public long sysPrint(long pre) {
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
