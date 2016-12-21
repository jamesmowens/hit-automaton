package com.hp.hpl.CHAOS.Queue;

public interface LogQueue {
	public void log();

	public boolean isAlreadyLogged();

	public void setAlreadyLogged(boolean alreadyLogged);
}
