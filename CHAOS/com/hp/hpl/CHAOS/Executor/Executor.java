package com.hp.hpl.CHAOS.Executor;

import java.util.ArrayList;

import com.hp.hpl.CHAOS.QueryPlan.RunTimeBlock;
import com.hp.hpl.CHAOS.QueryPlan.RunTimeEnvironment;
import com.hp.hpl.CHAOS.Scheduler.Scheduler;
import com.hp.hpl.CHAOS.StreamOperator.StreamOperator;

public abstract class Executor extends Thread implements RunTimeBlock {
	protected ArrayList<RunTimeEnvironment> reArray;
	protected Scheduler sch;

	volatile protected Thread stopSign;
	volatile protected boolean threadSuspended;

	volatile protected boolean threadToStart;

	volatile protected boolean reAdded;
	volatile protected boolean reRemoved;
	protected ArrayList<RunTimeEnvironment> reToBeAdded;
	protected ArrayList<String> reNameToBeRemoved;

	public Executor(ArrayList<RunTimeEnvironment> reArray, Scheduler sch) {
		super();
		this.reArray = reArray;
		this.sch = sch;
		this.stopSign = this;
		this.threadSuspended = false;
		this.reAdded = false;
		this.reRemoved = false;
		this.reToBeAdded = new ArrayList<RunTimeEnvironment>();
		this.reNameToBeRemoved = new ArrayList<String>();
	}

	public ArrayList<RunTimeEnvironment> getReArray() {
		return reArray;
	}

	public Scheduler getSch() {
		return sch;
	}

	public void setReArray(ArrayList<RunTimeEnvironment> reArray) {
		this.reArray = reArray;
	}

	public void setSch(Scheduler sch) {
		this.sch = sch;
	}

	public ArrayList<RunTimeEnvironment> getReToBeAdded() {
		return reToBeAdded;
	}

	public ArrayList<String> getReNameToBeRemoArray() {
		return reNameToBeRemoved;
	}

	public void setReToBeAdded(ArrayList<RunTimeEnvironment> reToBeAdded) {
		synchronized (this.reToBeAdded) {
			this.reToBeAdded.addAll(reToBeAdded);
		}
		this.reAdded = true;
		synchronized (this) {
			notifyAll();
		}
		Thread.yield();
	}

	public void setReNameToBeRemoArray(ArrayList<String> reNameToBeRemoved) {
		synchronized (this.reNameToBeRemoved) {
			this.reNameToBeRemoved.addAll(reNameToBeRemoved);
		}
		this.reRemoved = true;
		synchronized (this) {
			notifyAll();
		}
		Thread.yield();
	}

	protected void addRE() {
		synchronized (this.reToBeAdded) {
			for (RunTimeEnvironment re : this.reToBeAdded)
				re.init();
			this.reArray.addAll(this.reToBeAdded);
			if (this.sch != null) {
				ArrayList<StreamOperator> opArray = new ArrayList<StreamOperator>();
				for (RunTimeEnvironment re : this.reArray)
					for (StreamOperator op : re.getOperators())
						opArray.add(op);
				this.sch.setOperators(opArray.toArray(new StreamOperator[] {}));
			}
			this.reToBeAdded.clear();
		}
		this.reAdded = false;
		System.out.println(this.toString() + ": Query Added");
		synchronized (this) {
			notifyAll();
		}
	}

	protected void removeREbyName() {
		synchronized (this.reNameToBeRemoved) {
			for (String reName : this.reNameToBeRemoved)
				for (int index = this.reArray.size() - 1; index >= 0; index--)
					if (this.reArray.get(index).getReName().equalsIgnoreCase(
							reName))
						this.reArray.remove(index).finalizing();

			if (this.sch != null) {
				ArrayList<StreamOperator> opArray = new ArrayList<StreamOperator>();
				for (RunTimeEnvironment re : this.reArray)
					for (StreamOperator op : re.getOperators())
						opArray.add(op);
				this.sch.setOperators(opArray.toArray(new StreamOperator[] {}));
			}
			this.reNameToBeRemoved.clear();
		}
		this.reRemoved = false;
		System.out.println(this.toString() + ": Query Removed");
		synchronized (this) {
			notifyAll();
		}
	}

	public void waitReToBeAdded() {
		while (this.reAdded)
			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {
					return;
				}
			}
	}

	public void waitReNameToBeRemoArray() {
		while (this.reRemoved)
			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {
					return;
				}
			}
	}

	public int init() {
		for (int index = 0; index < this.reArray.size(); index++)
			this.reArray.get(index).init();
		return 0;
	}

	public void finalizing() {
		for (int index = 0; index < this.reArray.size(); index++)
			this.reArray.get(index).finalizing();
	}

	// run() template
	//
	// @Override
	// public void run() {
	// System.out.println(this.toString()+": Started");
	// synchronized (this) {
	// this.threadToStart = false;
	// notifyAll();
	// }
	//
	// Thread thisThread = Thread.currentThread();
	// while (stopSign == thisThread) {
	// try {
	// if (threadSuspended) {
	// synchronized (this) {
	// while (threadSuspended && stopSign == thisThread) {
	// System.out
	// .println(this.toString()+": Suspended");
	// wait();
	// System.out.println(this.toString()+": Resumed");
	// }
	// }
	// }
	//
	// if (this.reAdded)
	// this.addRE();
	// if (this.reRemoved)
	// this.removeREbyName();
	//
	// // real work
	// } catch (InterruptedException e) {
	// System.out.println(this.toString()+": Stopped");
	// return;// set to stop
	// }
	// }
	// System.out.println(this.toString()+": Stopped");
	// }

	abstract public void run();

	abstract public String toString();

	public synchronized void setStop() {
		stopSign = null;
		this.interrupt();
		Thread.yield();
	}

	public void setStart() {
		this.stopSign = this;
		this.threadSuspended = false;
		this.threadToStart = true;
		this.start();
		Thread.yield();
	}

	public void waitStart() {
		while (this.threadToStart)
			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {
					return;
				}
			}
	}

	public void setSuspended() {
		threadSuspended = true;
		Thread.yield();
	}

	public synchronized void setResume() {
		threadSuspended = false;
		notifyAll();
		Thread.yield();
	}

}
