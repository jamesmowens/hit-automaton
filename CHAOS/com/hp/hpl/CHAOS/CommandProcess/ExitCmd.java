package com.hp.hpl.CHAOS.CommandProcess;

import com.hp.hpl.CHAOS.Component.DStreamGenerator;
//import com.hp.hpl.CHAOS.Distribute.CHAOSSlave;

public class ExitCmd extends CmdMsg {

	private static final long serialVersionUID = 1L;

	public ExitCmd() {
		super();
	}

	@Override
	public void visit(CmdReceiver cmdReceiver) {
		if (cmdReceiver instanceof DStreamGenerator) {
			DStreamGenerator dsg = (DStreamGenerator) cmdReceiver;
			try {
				dsg.exit();
			} catch (Exception e) {
				e.printStackTrace();
			}}
//		} else if (cmdReceiver instanceof CHAOSSlave) {
//			CHAOSSlave slave = (CHAOSSlave) cmdReceiver;
//			try {
//				slave.exit();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}

	}

}
