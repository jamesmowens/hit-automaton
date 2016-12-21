package com.hp.hpl.CHAOS.CommandProcess;

import java.io.Serializable;

public abstract class CmdMsg implements Serializable {

	private static final long serialVersionUID = 1L;
	int sn = 0;

	public CmdMsg() {
		super();
		this.sn = Utility.getUniqueInt();
	}

	public int getSn() {
		return sn;
	}

	abstract public void visit(CmdReceiver cmdReceiver);

}
