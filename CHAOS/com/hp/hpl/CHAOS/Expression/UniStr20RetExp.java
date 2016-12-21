package com.hp.hpl.CHAOS.Expression;

public abstract class UniStr20RetExp extends Str20RetExp implements
		UniExpression {
	Str20RetExp eChild;

	public UniStr20RetExp(Str20RetExp child) {
		super();
		eChild = child;
	}

	public Str20RetExp getEChild() {
		return eChild;
	}

	public void setEChild(Str20RetExp child) {
		eChild = child;
	}

}
