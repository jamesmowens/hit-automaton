package com.hp.hpl.CHAOS.Executor;

import java.util.ArrayList;

import com.hp.hpl.CHAOS.QueryPlan.RunTimeEnvironment;
import com.hp.hpl.CHAOS.Scheduler.Scheduler;

public class StreamGeneratorExecutor extends Executor {
	Executor engine = null;

	public StreamGeneratorExecutor(ArrayList<RunTimeEnvironment> reArray,
			Scheduler sch, Executor engine) {
		super(reArray, sch);
		this.engine = engine;
	}

	public Executor getEngine() {
		return engine;
	}

	public void setEngine(Executor engine) {
		this.engine = engine;
	}

	@Override
	public void run() {
		System.out.println("StreamGeneratorExecutor: Started");
		synchronized (this) {
			this.threadToStart = false;
			notifyAll();
		}

		Thread thisThread = Thread.currentThread();
		while (stopSign == thisThread) {
			try {
				if (threadSuspended) {
					synchronized (this) {
						while (threadSuspended && stopSign == thisThread) {
							if (this.engine != null)
								synchronized (engine) {
									engine.notify();
								}
							System.out
									.println("StreamGeneratorExecutor: Suspended");
							wait();
							if (this.engine != null)
								synchronized (engine) {
									engine.notify();
								}
							System.out
									.println("StreamGeneratorExecutor: Resumed");
						}
					}
				}
				if (this.reAdded)
					this.addRE();
				if (this.reRemoved)
					this.removeREbyName();

				// real work
				int timeToSleep = sch.runNext();
				if (timeToSleep >= 0) {
					if (this.engine != null)
						synchronized (engine) {
							engine.notify();
						}
					mySleep(timeToSleep);
				} else {// nothing to do or something wrong
					if (this.engine != null)
						synchronized (engine) {
							engine.notify();
						}
					System.out.println("StreamGeneratorExecutor: Stopped");
					return;
				}
			} catch (InterruptedException e) {

				if (this.engine != null)
					synchronized (engine) {
						engine.notify();
					}
				System.out.println("StreamGeneratorExecutor: Stopped");
				return;// set to stop
			}
		}
		if (this.engine != null)
			synchronized (engine) {
				engine.notify();
			}
		System.out.println("StreamGeneratorExecutor: Stopped");
	}

	@Override
	public String toString() {
		return "StreamGeneratorExecutor";
	}

	// interval in milliseconds
	void mySleep(long interval) throws InterruptedException {
		sleep(interval);
	}
}
