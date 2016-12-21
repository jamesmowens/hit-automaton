package com.hp.hpl.CHAOS.CommandProcess;

import com.hp.hpl.CHAOS.Component.DStreamGenerator;

public class RemoveCmd extends CmdMsg {

	private static final long serialVersionUID = 1L;

	String[] queryName = null;

	public RemoveCmd(String[] queryName) {
		super();
		this.queryName = queryName;
	}

	@Override
	public void visit(CmdReceiver cmdReceiver) {
		if (cmdReceiver instanceof DStreamGenerator) {
			DStreamGenerator dsg = (DStreamGenerator) cmdReceiver;
			try {
				dsg.remove(queryName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
