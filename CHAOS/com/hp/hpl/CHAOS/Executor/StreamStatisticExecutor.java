package com.hp.hpl.CHAOS.Executor;

import java.util.ArrayList;

import com.hp.hpl.CHAOS.QueryPlan.RunTimeEnvironment;
import com.hp.hpl.CHAOS.Queue.LogQueue;

public class StreamStatisticExecutor extends Executor {

	int interval = 0;// unit: second

	public StreamStatisticExecutor(ArrayList<RunTimeEnvironment> reArray,
			int interval) {
		super(reArray, null);
		this.interval = interval > 1 ? interval : 1;
	}

	@Override
	public void run() {
		System.out.println("StreamStatisticExecutor: Started");
		synchronized (this) {
			this.threadToStart = false;
			notifyAll();
		}

		long timeToSleep = interval * 1000;

		Thread thisThread = Thread.currentThread();
		while (stopSign == thisThread) {
			try {
				if (threadSuspended) {
					synchronized (this) {
						while (threadSuspended && stopSign == thisThread) {
							System.out
									.println("StreamStatisticExecutor: Suspended");
							wait();
							System.out
									.println("StreamStatisticExecutor: Resumed");
						}
					}
				}
				if (this.reAdded)
					this.addRE();
				if (this.reRemoved)
					this.removeREbyName();

				// real work
				for (RunTimeEnvironment re : this.reArray)
					for (LogQueue lq : re.getLogQueues())
						lq.log();
				sleep(timeToSleep);
			} catch (InterruptedException e) {
				System.out.println("StreamStatisticExecutor: Stopped");
				return;// set to stop
			} catch (Exception e) {
			}
		}

		System.out.println("StreamStatisticExecutor: Stopped");
	}

	@Override
	public String toString() {
		return "StreamStatisticExecutor";
	}
}
