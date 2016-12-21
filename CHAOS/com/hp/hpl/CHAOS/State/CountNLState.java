package com.hp.hpl.CHAOS.State;

import com.hp.hpl.CHAOS.StreamData.SchemaElement;

/**
 * @author liumo
 * 
 */
public class CountNLState extends NLState {

	private static final long serialVersionUID = 1L;
	int windowSize;

	public CountNLState(SchemaElement[] array) {
		super(array);
		this.windowSize = 0;
	}

	public CountNLState(SchemaElement[] array, int windowSize) {
		super(array);
		this.windowSize = windowSize;
	}

	@Override
	public int purge(byte[] tuple) {

		int curStateSize = (int) queue.getSize();
		int ret = curStateSize - windowSize;
		while (curStateSize > windowSize) {
			queue.dequeue();
			curStateSize--;
		}

		return ret;
	}

	public int getWindowSize() {
		return windowSize;
	}

	@Override
	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

}
