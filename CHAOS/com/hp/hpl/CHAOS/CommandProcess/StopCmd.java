package com.hp.hpl.CHAOS.CommandProcess;

import com.hp.hpl.CHAOS.Component.DStreamGenerator;

public class StopCmd extends CmdMsg {

	private static final long serialVersionUID = 1L;

	public StopCmd() {
		super();
	}

	@Override
	public void visit(CmdReceiver cmdReceiver) {
		if (cmdReceiver instanceof DStreamGenerator) {
			DStreamGenerator dsg = (DStreamGenerator) cmdReceiver;
			try {
				dsg.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
