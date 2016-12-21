package com.hp.hpl.CHAOS.Expression;

public abstract class UniBoolRetExp extends BoolRetExp implements UniExpression {
	BoolRetExp eChild;

	public UniBoolRetExp(BoolRetExp child) {
		super();
		eChild = child;
	}

	public BoolRetExp getEChild() {
		return eChild;
	}

	public void setEChild(BoolRetExp child) {
		eChild = child;
	}

}
