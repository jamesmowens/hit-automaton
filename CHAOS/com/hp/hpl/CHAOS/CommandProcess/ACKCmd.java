package com.hp.hpl.CHAOS.CommandProcess;

import com.hp.hpl.CHAOS.Component.DCHAOS;
//import com.hp.hpl.CHAOS.Distribute.CHAOSMaster;

public class ACKCmd extends CmdMsg {

	private static final long serialVersionUID = 1L;
	public String msg = null;

	public ACKCmd() {
		super();
	}

	@Override
	public void visit(CmdReceiver cmdReceiver) {
		if (cmdReceiver instanceof DCHAOS) {
			return;
		}
//		if (cmdReceiver instanceof CHAOSMaster) {
//			if (this.msg != null)
//				System.out.println(this.msg);
//		}

	}
}
