package com.hp.hpl.CHAOS.Queue;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import com.hp.hpl.CHAOS.StreamData.SchemaElement;

public class SingleReaderLogQueue extends SingleReaderQueueArrayImp implements
		LogQueue {
	private static final long serialVersionUID = 1L;
	transient private BufferedWriter bw;
	private String logFileName;
	private boolean alreadyLogged;

	public SingleReaderLogQueue(SchemaElement[] schema) {
		super(schema);
		this.bw = null;
		this.logFileName = null;
	}

	public SingleReaderLogQueue(SchemaElement[] schema, String logfilename) {
		super(schema);
		this.bw = null;
		this.logFileName = logfilename;
	}

	public String getLogFileName() {
		return logFileName;
	}

	public void setLogFileName(String logFileName) {
		this.logFileName = logFileName;
	}

	public boolean isAlreadyLogged() {
		return alreadyLogged;
	}

	public void setAlreadyLogged(boolean alreadyLogged) {
		this.alreadyLogged = alreadyLogged;
	}

	@Override
	public int init() {
		if (initialized)
			return 0;
		super.init();
		try {
			FileOutputStream fstream = new FileOutputStream(logFileName);
			// Get the object of DataInputStream
			DataOutputStream out = new DataOutputStream(fstream);
			bw = new BufferedWriter(new OutputStreamWriter(out));

		} catch (Exception e) {// Catch exception if any
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
			this.bw.flush();
			this.bw.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		initialized = false;
	}

	public void log() {
		try {
			bw.write(System.currentTimeMillis() + "," + this.getDeltaEnqueued()
					+ "," + this.getDeltaDequeued() + "\n");
			this.clearDeltaStat();
		} catch (IOException e) {
			// e.printStackTrace();
		}
	}
}
