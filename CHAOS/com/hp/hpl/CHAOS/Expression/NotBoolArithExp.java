package com.hp.hpl.CHAOS.Expression;

public class NotBoolArithExp extends BoolRetExp implements UniExpression {
	private static final long serialVersionUID = 1L;
	BoolRetExp eChild;

	public NotBoolArithExp(BoolRetExp child) {
		super();
		eChild = child;
	}

	public BoolRetExp getEChild() {
		return eChild;
	}

	public void setEChild(BoolRetExp child) {
		eChild = child;
	}

	@Override
	public boolean eval() {
		return !eChild.eval();
	}

	// for debug purpose only
	@Override
	public String toString() {
		return "NOT(" + eChild.toString() + ")";
	}
}
