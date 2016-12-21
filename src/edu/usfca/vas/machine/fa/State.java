package edu.usfca.vas.machine.fa;

import edu.usfca.xj.foundation.XJXMLSerializable;

public class State implements XJXMLSerializable {

	public String name = null;
	public boolean start = false;
	public boolean accepted = false;

	public static FAState createState(String name) {
	    return new FAState(name);
	}

	public State() {
		super();
	}

	public String getName() {
	    return name;
	}

	public void setName(String name) {
	    this.name = name;
	}

	public boolean isStart() {
	    return start;
	}

	public void setStart(boolean start) {
	    this.start = start;
	}

	public boolean isAccepted() {
	    return accepted;
	}

	public void setAccepted(boolean accepted) {
	    this.accepted = accepted;
	}

}