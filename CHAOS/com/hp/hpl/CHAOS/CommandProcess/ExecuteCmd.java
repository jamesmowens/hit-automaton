package com.hp.hpl.CHAOS.CommandProcess;

import java.util.ArrayList;

import com.hp.hpl.CHAOS.Component.DStreamGenerator;
import com.hp.hpl.CHAOS.QueryPlan.RunTimeEnvironment;

public class ExecuteCmd extends CmdMsg {

	private static final long serialVersionUID = 1L;

	ArrayList<RunTimeEnvironment> reArray = null;

	public ExecuteCmd(ArrayList<RunTimeEnvironment> reArray) {
		super();
		this.reArray = reArray;
	}

	@Override
	public void visit(CmdReceiver cmdReceiver) {
		if (cmdReceiver instanceof DStreamGenerator) {
			DStreamGenerator dsg = (DStreamGenerator) cmdReceiver;
			dsg.execute(reArray);
		}

	}

}
