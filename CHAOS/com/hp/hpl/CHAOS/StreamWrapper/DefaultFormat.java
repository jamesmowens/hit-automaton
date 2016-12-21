package com.hp.hpl.CHAOS.StreamWrapper;

import java.io.IOException;

public class DefaultFormat extends InputStreamWrapper {

	private static final long serialVersionUID = 1L;

	public DefaultFormat(String streamFile, boolean loop) {
		super(streamFile, loop);
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
		return ret;
	}
}
