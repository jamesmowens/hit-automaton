package com.hp.hpl.CHAOS.Expression;

public abstract class BinStr20RetExp extends Str20RetExp implements
		BinExpression {
	Str20RetExp eLeft, eRight;

	public BinStr20RetExp(Str20RetExp left, Str20RetExp right) {
		super();
		eLeft = left;
		eRight = right;
	}

	public Str20RetExp getELeft() {
		return eLeft;
	}

	public Str20RetExp getERight() {
		return eRight;
	}

	public void setELeft(Str20RetExp left) {
		eLeft = left;
	}

	public void setERight(Str20RetExp right) {
		eRight = right;
	}

}
