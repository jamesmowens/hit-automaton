package com.hp.hpl.CHAOS.StreamWrapper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import com.hp.hpl.CHAOS.QueryPlan.RunTimeBlock;

public abstract class InputStreamWrapper implements Serializable, RunTimeBlock {
	String streamFile = null;
	transient BufferedReader in = null;
	boolean loop;

	boolean initialized = false;

	public InputStreamWrapper() {

	}

	public InputStreamWrapper(String streamFile, boolean loop) {
		super();
		this.streamFile = streamFile;
		this.loop = loop;
	}

	public String getStreamFile() {
		return streamFile;
	}

	public boolean isLoop() {
		return loop;
	}

	public void setStreamFile(String streamFile) {
		this.streamFile = streamFile;
	}

	public void setLoop(boolean loop) {
		this.loop = loop;
	}

	public int init() {
		if (initialized)
			return 0;

		try {
			this.in = new BufferedReader(new FileReader(streamFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		}

		initialized = true;
		return 0;
	}

	public void finalizing() {
		if (!initialized)
			return;

		try {
			this.in.close();
		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
		}

		initialized = false;
	}

	abstract public String readLine();

}
