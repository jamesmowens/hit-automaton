package com.hp.hpl.CHAOS.CommandProcess;

public abstract class CmdReceiver {
	public void accept(CmdMsg visitor) {
		visitor.visit(this);
	}
}
