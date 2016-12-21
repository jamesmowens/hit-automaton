package com.hp.hpl.CHAOS.Network;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import com.hp.hpl.CHAOS.Executor.Executor;
import com.hp.hpl.CHAOS.QueryPlan.RunTimeEnvironment;
import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.StreamTupleCreator;

@Deprecated
//use NetworkTupleReceiverSocketChannelImpl instead
public class NetworkTupleReceiverSocketImpl extends Executor {

	ServerSocket serverSocket = null;
	int port = Constant.TUPLE_RECEIVER_PORT;
	ArrayList<NetworkTupleReceiverHandler> threadArray = null;

	Executor engine = null;

	boolean initialized = false;

	public NetworkTupleReceiverSocketImpl(
			ArrayList<RunTimeEnvironment> reArray, Executor engine) {
		super(reArray, null);
		this.engine = engine;
	}

	public NetworkTupleReceiverSocketImpl(
			ArrayList<RunTimeEnvironment> reArray, int port, Executor engine) {
		super(reArray, null);
		this.port = port;
		this.engine = engine;
	}

	public Executor getEngine() {
		return engine;
	}

	public void setEngine(Executor engine) {
		this.engine = engine;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int init() {
		if (initialized)
			return 0;
		super.init();

		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("Could not listen on port: " + port);
			return -1;
		}
		threadArray = new ArrayList<NetworkTupleReceiverHandler>();
		initialized = true;
		return 0;
	}

	public void finalizing() {
		if (!initialized)
			return;
		super.finalizing();
		try {
			serverSocket.close();
		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
		}
		initialized = false;
	}

	public void run() {
		System.out.println("NetworkTupleReceiver: Started");
		synchronized (this) {
			this.threadToStart = false;
			notifyAll();
		}

		Thread thisThread = Thread.currentThread();
		while (stopSign == thisThread) {
			try {
				if (threadSuspended) {
					synchronized (this) {
						while (threadSuspended && stopSign == thisThread) {
							System.out
									.println("NetworkTupleReceiver: Suspended");
							if (this.engine != null)
								synchronized (engine) {
									engine.notifyAll();
								}

							wait();
							if (this.engine != null)
								synchronized (engine) {
									engine.notifyAll();
								}
							System.out.println("NetworkTupleReceiver: Resumed");

						}
					}
				}

				// real work
				if (this.reAdded)
					this.addRE();
				if (this.reRemoved)
					this.removeREbyName();

				Socket clientSocket = serverSocket.accept();

				if (this.reAdded)
					this.addRE();
				if (this.reRemoved)
					this.removeREbyName();

				StreamQueue sq = this.addClient(clientSocket);

				// System.out.println("Connected to queue:" + sq.getQueueID());
				NetworkTupleReceiverHandler t = new NetworkTupleReceiverHandler(
						clientSocket, sq, this.engine);

				t.setStart();

				threadArray.add(t);

			} catch (Exception e) {
				// set to stop
				if (this.engine != null)
					synchronized (engine) {
						engine.notifyAll();
					}

				try {
					for (NetworkTupleReceiverHandler th : threadArray)
						th.setStop();
					for (NetworkTupleReceiverHandler th : threadArray)
						th.join();
					serverSocket.close();
				} catch (Exception e1) {
					// e.printStackTrace();
				}
				System.out.println("NetworkTupleReceiver: Stopped");
				return;
			}
		}
		if (this.engine != null)
			synchronized (engine) {
				engine.notifyAll();
			}

		try {
			for (NetworkTupleReceiverHandler th : threadArray)
				th.setStop();
			for (NetworkTupleReceiverHandler th : threadArray)
				th.join();
			serverSocket.close();
		} catch (Exception e1) {
			// e.printStackTrace();
		}
		System.out.println("NetworkTupleReceiver: Stopped");
	}

	private StreamQueue addClient(Socket sc) throws IOException {
		StreamQueue ret = null;
		InputStream in = sc.getInputStream();
		int queueID = in.read();
		in = null;
		// System.out.println("queueID:" + queueID);
		for (RunTimeEnvironment re : this.reArray)
			for (StreamQueue sq : re.getInputBoundaryQueues()) {
				if (sq.getQueueID() == queueID) {
					ret = sq;
					break;
				}
			}
		return ret;
	}

	public String toString() {
		return "NetworkTupleReceiver";
	}

	class NetworkTupleReceiverHandler extends Thread {

		volatile Thread stopSign;
		volatile boolean threadSuspended;

		Socket clientSocket = null;
		StreamQueue targetQueue = null;

		Executor engine = null;

		boolean initialized = false;

		public NetworkTupleReceiverHandler(Socket clientSocket,
				StreamQueue targetQ, Executor engine) {
			super();
			this.clientSocket = clientSocket;
			this.targetQueue = targetQ;
			this.engine = engine;
		}

		public Socket getClientSocket() {
			return clientSocket;
		}

		public StreamQueue getTargetQueue() {
			return targetQueue;
		}

		public void setClientSocket(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}

		public void setTargetQueue(StreamQueue targetQueue) {
			this.targetQueue = targetQueue;
		}

		public void run() {
			Thread thisThread = Thread.currentThread();

			int tupleSize = targetQueue.getTupleSize();
			int tupleSizeWithHeader = tupleSize
					+ com.hp.hpl.CHAOS.StreamData.Constant.TUPLE_HEAD_S;
			InputStream in = null;
			try {
				in = clientSocket.getInputStream();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			byte[] tuple = StreamTupleCreator.makeEmptyTuple(tupleSize);
			int off = 0;

			while (stopSign == thisThread) {
				try {
					if (threadSuspended) {
						synchronized (this) {
							while (threadSuspended && stopSign == thisThread)
								if (this.engine != null)
									synchronized (engine) {
										engine.notifyAll();
									}

							wait();
							if (this.engine != null)
								synchronized (engine) {
									engine.notifyAll();
								}
						}
					}

					// real work
					int ret = in.read(tuple, off, tupleSizeWithHeader - off);
					if (ret < 0)
						break;
					if (ret == tupleSizeWithHeader - off) {
						targetQueue.enqueue(tuple);
						tuple = StreamTupleCreator.makeEmptyTuple(tupleSize);
						off = 0;
					} else {
						off += ret;
						continue;
					}
					while ((ret = in.read(tuple)) == tupleSizeWithHeader) {
						targetQueue.enqueue(tuple);
						tuple = StreamTupleCreator.makeEmptyTuple(tupleSize);
					}

					if (this.engine != null)
						synchronized (engine) {
							engine.notifyAll();
						}

					if (ret < 0)
						break;
					off = ret;

				} catch (Exception e) {
					// set to stop
					if (this.engine != null)
						synchronized (engine) {
							engine.notifyAll();
						}

					try {
						in.close();
						clientSocket.close();
					} catch (IOException e1) {
					}
					return;
				}
			}

			if (this.engine != null)
				synchronized (engine) {
					engine.notifyAll();
				}

			try {
				in.close();
				clientSocket.close();
			} catch (IOException e1) {
			}
		}

		public String toString() {
			return "NetworkTupleReceiverHandler";
		}

		public synchronized void setStop() {
			stopSign = null;
			try {
				this.clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.interrupt();
		}

		public void setStart() {
			this.stopSign = this;
			this.threadSuspended = false;
			this.start();
		}

		public void setSuspended() {
			threadSuspended = true;
		}

		public synchronized void setResume() {
			threadSuspended = false;
			notifyAll();
		}

	}
}
