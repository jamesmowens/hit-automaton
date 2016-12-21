package com.hp.hpl.CHAOS.Network;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import com.hp.hpl.CHAOS.Executor.Executor;
import com.hp.hpl.CHAOS.QueryPlan.RunTimeEnvironment;
import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.StreamTupleCreator;

public class NetworkTupleReceiverSocketChannelImpl extends Executor {

	ServerSocket serverSocket = null;
	ServerSocketChannel ssc = null;
	int port = Constant.TUPLE_RECEIVER_PORT;
	Selector selector = null;
	Hashtable<SocketChannel, ChannelTarget> socket2Queue = null;
	ByteBuffer buffer = null;
	byte[] tuple = null;
	int off = 0;

	Executor engine = null;
	boolean initialized = false;

	public NetworkTupleReceiverSocketChannelImpl(
			ArrayList<RunTimeEnvironment> reArray, Executor engine) {
		super(reArray, null);
		this.engine = engine;
	}

	public NetworkTupleReceiverSocketChannelImpl(
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
			ssc = ServerSocketChannel.open();
			ssc.configureBlocking(false);
			serverSocket = ssc.socket();
			InetSocketAddress isa = new InetSocketAddress(port);
			serverSocket.bind(isa);

			selector = Selector.open();
			ssc.register(selector, SelectionKey.OP_ACCEPT);
			socket2Queue = new Hashtable<SocketChannel, ChannelTarget>();
			buffer = ByteBuffer.allocate(Constant.TUPLE_SENDER_BUFFER * 100);

		} catch (IOException e) {
			System.err.println("Could not listen on port: " + port);
			return -1;
		}

		initialized = true;
		return 0;
	}

	public void finalizing() {
		if (!initialized)
			return;
		super.finalizing();
		try {
			selector.close();
			ssc.close();
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

				int num = selector.select();

				if (this.reAdded)
					this.addRE();
				if (this.reRemoved)
					this.removeREbyName();

				if (num == 0) {
					yield();
					continue;
				}

				Set<SelectionKey> keys = selector.selectedKeys();
				Iterator<SelectionKey> it = keys.iterator();

				while (it.hasNext()) {
					SelectionKey key = it.next();
					if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {

						SocketChannel sc = ((ServerSocketChannel) key.channel())
								.accept();
						Socket clientSocket = sc.socket();
						this.addClient(clientSocket);

						sc.configureBlocking(false);
						sc.register(selector, SelectionKey.OP_READ);

					} else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {

						SocketChannel sc = null;
						try {
							sc = (SocketChannel) key.channel();
							boolean ok = processInput(sc);

							if (this.engine != null)
								synchronized (engine) {
									engine.notifyAll();
								}

							if (!ok) {
								key.cancel();
								try {
									sc.socket().close();
									sc.close();
								} catch (IOException ie) {
									ie.printStackTrace();
								}
							}
						} catch (IOException ie) {
							if (this.engine != null)
								synchronized (engine) {
									engine.notifyAll();
								}

							key.cancel();
							try {
								sc.socket().close();
								sc.close();
							} catch (IOException ie2) {
								ie2.printStackTrace();
							}
						}
					}
				}

				keys.clear();

			} catch (Exception e) {
				// set to stop
				if (this.engine != null)
					synchronized (engine) {
						engine.notifyAll();
					}

				try {
					selector.close();
				} catch (IOException ie2) {
					ie2.printStackTrace();
				}
				e.printStackTrace();
				System.out.println("NetworkTupleReceiver: Stopped");
				return;
			}
		}
		// set to stop
		if (this.engine != null)
			synchronized (engine) {
				engine.notifyAll();
			}

		try {
			selector.close();
		} catch (IOException ie2) {
			ie2.printStackTrace();
		}
		System.out.println("NetworkTupleReceiver: Stopped");
		return;
	}

	private boolean processInput(SocketChannel sc) throws IOException {
		StreamQueue targetQueue = this.socket2Queue.get(sc).getStreamQueue();
		tuple = this.socket2Queue.get(sc).getTuple();
		off = this.socket2Queue.get(sc).getOff();

		int tupleSize = targetQueue.getTupleSize();
		int tupleSizeWithHeader = tupleSize
				+ com.hp.hpl.CHAOS.StreamData.Constant.TUPLE_HEAD_S;

		buffer.clear();
		sc.read(buffer);
		buffer.flip();

		int expectLength = tupleSizeWithHeader - off;
		int realLength = buffer.limit() - buffer.position();
		if (realLength >= expectLength) {
			buffer.get(tuple, off, expectLength);
			targetQueue.enqueue(tuple);
			tuple = StreamTupleCreator.makeEmptyTuple(tupleSize);
			off = 0;
		} else {
			buffer.get(tuple, off, realLength);
			off += realLength;

			this.socket2Queue.get(sc).setTuple(tuple);
			this.socket2Queue.get(sc).setOff(off);
			return true;
		}

		int loop = (buffer.limit() - buffer.position()) / tupleSizeWithHeader;
		for (int i = 0; i < loop; i++) {
			buffer.get(tuple);
			targetQueue.enqueue(tuple);
			tuple = StreamTupleCreator.makeEmptyTuple(tupleSize);
			off = 0;
		}

		int length = buffer.limit() - buffer.position();
		buffer.get(tuple, off, length);
		off = length;

		this.socket2Queue.get(sc).setTuple(tuple);
		this.socket2Queue.get(sc).setOff(off);
		return true;
	}

	private void addClient(Socket sc) throws IOException {
		InputStream in = sc.getInputStream();
		int queueID = in.read();
		// System.out.println("queueID:" + queueID);
		for (RunTimeEnvironment re : this.reArray)
			for (StreamQueue sq : re.getInputBoundaryQueues()) {
				if (sq.getQueueID() == queueID) {
					this.socket2Queue.put(sc.getChannel(), new ChannelTarget(
							sq, StreamTupleCreator.makeEmptyTuple(sq
									.getTupleSize()), 0));
					break;
				}
			}
	}

	public String toString() {
		return "NetworkTupleReceiver";
	}

	class ChannelTarget {
		StreamQueue sq = null;
		byte[] tuple = null;
		int off = 0;

		public ChannelTarget(StreamQueue sq, byte[] tuple, int off) {
			super();
			this.sq = sq;
			this.tuple = tuple;
			this.off = off;
		}

		public StreamQueue getStreamQueue() {
			return sq;
		}

		public byte[] getTuple() {
			return tuple;
		}

		public int getOff() {
			return off;
		}

		public void setStreamQueue(StreamQueue sq) {
			this.sq = sq;
		}

		public void setTuple(byte[] tuple) {
			this.tuple = tuple;
		}

		public void setOff(int off) {
			this.off = off;
		}

	}
}
