package com.hp.hpl.CHAOS.Expression;

import java.util.Random;

public class BoolProb extends UniBoolRetExp {
	private static final long serialVersionUID = 1L;
	double selectivity;
	private Random rand;

	public BoolProb(BoolRetExp child) {
		super(child);
		this.selectivity = 0;
		this.rand = new Random();
	}

	public BoolProb(BoolRetExp child, double selectivity) {
		super(null);
		this.selectivity = selectivity;
		this.rand = new Random();
	}

	public double getSelectivity() {
		return selectivity;
	}

	public void setSelectivity(double selectivity) {
		this.selectivity = selectivity;
	}

	@Override
	public boolean eval() {
		return rand.nextDouble() < selectivity;
	}

	@Override
	public String toString() {
		return "BoolProb " + Double.toString(selectivity);
	}

}
