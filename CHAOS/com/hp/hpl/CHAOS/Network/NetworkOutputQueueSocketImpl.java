package com.hp.hpl.CHAOS.Network;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Iterator;

import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;

@Deprecated
//use NetworkOutputQueueSocketChannelImpl instead
public class NetworkOutputQueueSocketImpl extends StreamQueue implements
		NetworkOutputQueue {
	private static final long serialVersionUID = 1L;

	private String hostName;
	private int port;

	transient private Socket socket = null;
	transient private OutputStream outputStream = null;

	byte[][] tupleList = new byte[Constant.TUPLE_SENDER_BUFFER][];
	int index = 0;

	public NetworkOutputQueueSocketImpl(SchemaElement[] schema) {
		super(schema);
		this.hostName = null;
		this.port = 0;
	}

	public NetworkOutputQueueSocketImpl(SchemaElement[] schema,
			String hostName, int port) {
		super(schema);
		this.hostName = hostName;
		this.port = port;
	}

	public NetworkOutputQueueSocketImpl(StreamQueue queue, String hostName,
			int port) {
		super(queue.getSchema());
		this.queueID = queue.queueID;
		this.hostName = hostName;
		this.port = port;
	}

	public String getHostName() {
		return hostName;
	}

	public int getPort() {
		return port;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public byte[] dequeue() {
		return null;
	}

	@Override
	public byte[] enqueue(byte[] tuple) {
		tupleList[index++] = tuple;
		if (index == tupleList.length)
			return this.flush();
		return tuple;
	}

	@Override
	public byte[] flush() {
		byte[] ret = null;
		try {
			for (int i = 0; i < index; i++) {
				ret = tupleList[i];
				this.outputStream.write(ret);
			}
			index = 0;
		} catch (IOException e) {
			return null;
		}
		return ret;
	}

	@Override
	public byte[] get(int index) {
		return null;
	}

	@Override
	public Iterator<byte[]> getIterator() {
		return null;
	}

	@Override
	public byte[] peek() {
		return null;
	}

	@Override
	public int init() {
		if (initialized)
			return 0;
		super.init();
		try {
			this.socket = new Socket(this.hostName, this.port);
			this.outputStream = socket.getOutputStream();
			this.outputStream.write((byte) this.getQueueID());
			this.outputStream.flush();
		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
			initialized = false;
			return -1;
		}
		initialized = true;
		return 0;
	}

	@Override
	public void finalizing() {
		if (!initialized)
			return;
		super.finalizing();
		try {
			this.flush();
			this.outputStream.flush();
			this.outputStream.close();
			this.socket.close();
			this.outputStream = null;
			this.socket = null;
		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
		}
		initialized = false;
	}

	@Override
	public byte[] dequeueLast() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] peekLast() {
		// TODO Auto-generated method stub
		return null;
	}

}
