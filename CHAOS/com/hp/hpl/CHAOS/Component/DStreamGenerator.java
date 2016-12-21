package com.hp.hpl.CHAOS.Component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.hp.hpl.CHAOS.CommandProcess.ACKCmd;
import com.hp.hpl.CHAOS.CommandProcess.CmdMsg;
import com.hp.hpl.CHAOS.CommandProcess.CmdReceiver;
import com.hp.hpl.CHAOS.Executor.Executor;
import com.hp.hpl.CHAOS.Executor.StreamGeneratorExecutor;
import com.hp.hpl.CHAOS.Executor.StreamStatisticExecutor;
import com.hp.hpl.CHAOS.QueryPlan.RunTimeEnvironment;
import com.hp.hpl.CHAOS.Scheduler.Scheduler;
import com.hp.hpl.CHAOS.Scheduler.StreamGeneratorSchedulerBatch;
import com.hp.hpl.CHAOS.StreamOperator.StreamOperator;

public class DStreamGenerator extends CmdReceiver {

	public Executor ex = null;
	public StreamStatisticExecutor sse = null;
	public long pre;

	public ServerSocket serverSocket = null;
	public Socket clientSocket = null;
	public ObjectInputStream in = null;
	public ObjectOutputStream out = null;

	public int port = Constant.CMD_RECEIVER_PORT;

	public int queryCounter = 0;
	public boolean running;

	/**
	 * @param args
	 * @throws Exception
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) {
		DStreamGenerator dsg = new DStreamGenerator();
		try {
			dsg.run();
		} catch (Exception e) {
		}
	}

	public void run() throws Exception {
		this.running = true;

		printLogo();

		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("Could not listen on port: " + port);
			return;
		}

		clientSocket = serverSocket.accept();
		out = new ObjectOutputStream(clientSocket.getOutputStream());
		out.flush();
		in = new ObjectInputStream(clientSocket.getInputStream());

		System.out.println("Connected");

		while (this.running) {
			CmdMsg cmdMsg = (CmdMsg) in.readObject();
			this.accept(cmdMsg);
			CmdMsg ack = new ACKCmd();
			out.writeObject(ack);
			out.flush();
		}
	}

	public void remove(String[] queryName) throws Exception {
		ArrayList<String> reName = new ArrayList<String>();

		for (int i = 0; i < queryName.length; i++) {
			String planName = queryName[i];
			reName.add(planName);
		}

		queryCounter -= reName.size();

		if (queryCounter <= 0)
			stop();
		else if (ex != null || sse != null) {
			ex.setReNameToBeRemoArray(reName);
			sse.setReNameToBeRemoArray(reName);
			ex.waitReNameToBeRemoArray();
			sse.waitReNameToBeRemoArray();
		}
	}

	public void stop() throws Exception {

		if (ex == null || sse == null) {
			System.out.println("No stream generator to stop");
			return;
		}
		ex.setStop();
		sse.setStop();
		ex.join();
		sse.join();
		ex.finalizing();
		sse.finalizing();
		sysPrint(pre);
		ex = null;
		sse = null;
	}

	public void execute(ArrayList<RunTimeEnvironment> reArray) {

		this.queryCounter += reArray.size();
		if (ex == null || sse == null) {
			ex = buildStreamGenerator(reArray, null);
			sse = new StreamStatisticExecutor(
					new ArrayList<RunTimeEnvironment>(ex.getReArray()), 1);

			ex.init();
			sse.init();
			pre = sysPrint(0);
			ex.setStart();
			sse.setStart();
			ex.waitStart();
			sse.waitStart();

		} else {
			ex.setReToBeAdded(reArray);
			sse.setReToBeAdded(new ArrayList<RunTimeEnvironment>(reArray));
			ex.waitReToBeAdded();
			sse.waitReToBeAdded();
		}
	}

	public void exit() throws InterruptedException {
		System.out.println("Distributed Stream Generator shutdown...");
		if (ex != null && sse != null) {
			ex.setStop();
			sse.setStop();
			ex.join();
			sse.join();
			ex.finalizing();
			sse.finalizing();
			ex = null;
			sse = null;
		}
		System.out.println("done");
		this.running = false;
	}

	private void printLogo() {
		String hostName = "";
		System.out
				.println("Distributed Stream Generator. Wait for connections.");
		try {
			java.net.InetAddress localMachine = java.net.InetAddress
					.getLocalHost();
			hostName = localMachine.getHostName();
		} catch (java.net.UnknownHostException ex) {
		}
		System.out.println("Host Name: " + hostName);
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
