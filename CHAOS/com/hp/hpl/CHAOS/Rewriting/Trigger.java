package com.hp.hpl.CHAOS.Rewriting;

public class Trigger {
 String triggerType  = new String();
 int queryID = -1;
public String getTriggerType() {
	return triggerType;
}
public void setTriggerType(String triggerType) {
	this.triggerType = triggerType;
}

public Trigger() {
	super();
}
public Trigger(String triggerType, int queryID) {
	super();
	this.triggerType = triggerType;
	this.queryID = queryID;
}
public int getQueryID() {
	return queryID;
}
public void setQueryID(int queryID) {
	this.queryID = queryID;
}

}
