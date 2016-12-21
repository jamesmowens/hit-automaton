package com.hp.hpl.CHAOS.StreamWrapper;

import java.io.IOException;

public class TestFormat extends InputStreamWrapper {

	private static final long serialVersionUID = 1L;
	long preTimestamp = 0;
	long interval = 0;

	public TestFormat() {
		super();
	}

	public TestFormat(String streamFile, boolean loop) {
		super(streamFile, loop);
		this.interval = 0;
	}

	public TestFormat(String streamFile, boolean loop, long interval) {
		super(streamFile, loop);
		this.interval = interval;
	}

	@Override
	public String readLine() {
		String ret = null;
		try {
			ret = in.readLine();
			if (ret == null) {
				this.finalizing();
				if (!loop)
					return null;
				this.init();
				ret = in.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return attach(ret);
	}

	private String attach(String input) {
		if (input == null)
			return null;
		return this.interval + " " + (preTimestamp += this.interval) + " "
				+ input;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}
}
