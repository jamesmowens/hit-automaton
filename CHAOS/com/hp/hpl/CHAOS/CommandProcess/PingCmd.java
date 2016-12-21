package com.hp.hpl.CHAOS.CommandProcess;

//import com.hp.hpl.CHAOS.Distribute.CHAOSSlave;

public class PingCmd extends CmdMsg {

	private static final long serialVersionUID = 1L;

	public PingCmd() {
		super();
	}

	@Override
	public void visit(CmdReceiver cmdReceiver) {
//		if (cmdReceiver instanceof CHAOSSlave) {
//			return;
//		}
	}
}
