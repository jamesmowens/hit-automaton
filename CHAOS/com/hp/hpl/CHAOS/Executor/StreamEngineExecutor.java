package com.hp.hpl.CHAOS.Executor;

import java.util.ArrayList;

import com.hp.hpl.CHAOS.QueryPlan.RunTimeEnvironment;
import com.hp.hpl.CHAOS.Scheduler.Scheduler;

public class StreamEngineExecutor extends Executor {

	public StreamEngineExecutor(ArrayList<RunTimeEnvironment> reArray,
			Scheduler sch) {
		super(reArray, sch);
	}

	@Override
	public void run() {
		System.out.println("StreamEngineExecutor: Started");
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
							System.out
									.println("StreamEngineExecutor: Suspended");
							wait();
							System.out.println("StreamEngineExecutor: Resumed");
						}
					}
				}

				if (this.reAdded)
					this.addRE();
				if (this.reRemoved)
					this.removeREbyName();

				// real work
				int tupleCounter = sch.runNext();
				if (tupleCounter == -1) {// nothing to do
					System.out.println("StreamEngineExecutor: Stopped");
					return;
				} else if (tupleCounter == -2) {// to wait
					synchronized (this) {
						wait();
					}
					sch.reset();
				} else if (tupleCounter >= 0) {
					yield();
				}
			} catch (InterruptedException e) {
				System.out.println("StreamEngineExecutor: Stopped");
				return;// set to stop
			}
		}
		System.out.println("StreamEngineExecutor: Stopped");
	}

	@Override
	public String toString() {
		return "StreamEngineExecutor";
	}
}
