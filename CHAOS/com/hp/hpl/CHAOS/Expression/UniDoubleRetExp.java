package com.hp.hpl.CHAOS.Expression;

public abstract class UniDoubleRetExp extends DoubleRetExp implements
		UniExpression {
	DoubleRetExp eChild;

	public UniDoubleRetExp(DoubleRetExp child) {
		super();
		eChild = child;
	}

	public DoubleRetExp getEChild() {
		return eChild;
	}

	public void setEChild(DoubleRetExp child) {
		eChild = child;
	}

}
