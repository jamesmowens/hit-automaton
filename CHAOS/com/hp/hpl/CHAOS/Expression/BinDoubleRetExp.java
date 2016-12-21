package com.hp.hpl.CHAOS.Expression;

public abstract class BinDoubleRetExp extends DoubleRetExp {
	DoubleRetExp eLeft, eRight;

	public BinDoubleRetExp(DoubleRetExp left, DoubleRetExp right) {
		super();
		eLeft = left;
		eRight = right;
	}

	public DoubleRetExp getELeft() {
		return eLeft;
	}

	public DoubleRetExp getERight() {
		return eRight;
	}

	public void setELeft(DoubleRetExp left) {
		eLeft = left;
	}

	public void setERight(DoubleRetExp right) {
		eRight = right;
	}

}
