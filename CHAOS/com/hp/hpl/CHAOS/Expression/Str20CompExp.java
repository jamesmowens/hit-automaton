package com.hp.hpl.CHAOS.Expression;

import java.util.Arrays;

public class Str20CompExp extends BoolRetExp implements CompExpression {
	private static final long serialVersionUID = 1L;
	int op;
	Str20RetExp eLeft, eRight;

	public Str20CompExp(int op, Str20RetExp left, Str20RetExp right) {
		super();
		this.op = op;
		eLeft = left;
		eRight = right;
	}

	public int getOp() {
		return op;
	}

	public Str20RetExp getELeft() {
		return eLeft;
	}

	public Str20RetExp getERight() {
		return eRight;
	}

	public void setOp(int op) {
		this.op = op;
	}

	public void setELeft(Str20RetExp left) {
		eLeft = left;
	}

	public void setERight(Str20RetExp right) {
		eRight = right;
	}

	@Override
	public boolean eval() {
		boolean bReturn;
		char[] sLeft = eLeft.eval();
		char[] sRight = eRight.eval();

		switch (op) {
		case Constant.LT:
			bReturn = (compareTo(sLeft, sRight) < 0);
			break;
		case Constant.GT:
			bReturn = (compareTo(sLeft, sRight) > 0);
			break;
		case Constant.LEQ:
			bReturn = (compareTo(sLeft, sRight) <= 0);
			break;
		case Constant.GEQ:
			bReturn = (compareTo(sLeft, sRight) >= 0);
			break;
		case Constant.EQ:
			bReturn = Arrays.equals(sLeft, sRight);
			break;
		case Constant.NEQ:
			bReturn = (compareTo(sLeft, sRight) != 0);
			break;
		default:
			/* fail */
			bReturn = false;
		}

		return bReturn;
	}

	// for debug purpose only
	@Override
	public String toString() {
		switch (op) {
		case Constant.LT:
			return "(" + eLeft.toString() + " < " + eRight.toString() + ")";
		case Constant.GT:
			return "(" + eLeft.toString() + " > " + eRight.toString() + ")";
		case Constant.LEQ:
			return "(" + eLeft.toString() + " <= " + eRight.toString() + ")";
		case Constant.GEQ:
			return "(" + eLeft.toString() + " >= " + eRight.toString() + ")";
		case Constant.EQ:
			return "(" + eLeft.toString() + " == " + eRight.toString() + ")";
		case Constant.NEQ:
			return "(" + eLeft.toString() + " != " + eRight.toString() + ")";
		default:
			return "(" + eLeft.toString() + " COMP(" + "UNKNOWN" + ") "
					+ eRight.toString() + ")";
		}
	}

	private int compareTo(char[] left, char[] right) {
		for (int i = 0; i < com.hp.hpl.CHAOS.StreamData.Constant.STR20_S; i++) {
			int diff = left[i] - right[i];
			if (diff == 0)
				continue;
			return diff;
		}
		return 0;
	}
}
