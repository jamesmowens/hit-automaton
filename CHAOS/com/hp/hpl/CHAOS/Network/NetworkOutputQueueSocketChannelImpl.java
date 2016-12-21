package com.hp.hpl.CHAOS.Network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;

public class NetworkOutputQueueSocketChannelImpl extends StreamQueue implements
		NetworkOutputQueue {
	private static final long serialVersionUID = 1L;

	private String hostName;
	private int port;

	transient private SocketChannel channel = null;
	transient private ByteBuffer buffer = null;

	public NetworkOutputQueueSocketChannelImpl(SchemaElement[] schema) {
		super(schema);
		this.hostName = null;
		this.port = 0;
	}

	public NetworkOutputQueueSocketChannelImpl(SchemaElement[] schema,
			String hostName, int port) {
		super(schema);
		this.hostName = hostName;
		this.port = port;
	}

	public NetworkOutputQueueSocketChannelImpl(StreamQueue queue,
			String hostName, int port) {
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

		if (tuple.length > buffer.remaining())
			return this.flush();
		buffer.put(tuple);
		this.enqueueStat();
		this.dequeueStat();
		return tuple;
	}

	@Override
	public byte[] flush() {
		try {
			buffer.flip();
			channel.write(buffer);
			buffer.clear();
		} catch (IOException e) {
			return null;
		}
		return new byte[] {};
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
			SocketAddress remote = new InetSocketAddress(hostName, port);
			channel = SocketChannel.open(remote);
			channel.socket().getOutputStream().write((byte) this.getQueueID());
			channel.socket().getOutputStream().flush();
			buffer = ByteBuffer.allocate(Constant.TUPLE_SENDER_BUFFER * 100);
			buffer.clear();
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
			channel.socket().getOutputStream().flush();
			this.channel.close();
			this.buffer = null;
			this.channel = null;
		} catch (Exception e) {
			// System.err.println("Error: " + e.getMessage());
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
