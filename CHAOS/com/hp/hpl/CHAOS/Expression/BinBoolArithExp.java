package com.hp.hpl.CHAOS.Expression;

public class BinBoolArithExp extends BoolRetExp implements CompExpression {
	private static final long serialVersionUID = 1L;
	int op;
	BoolRetExp eLeft, eRight;

	public BinBoolArithExp(int op, BoolRetExp left, BoolRetExp right) {
		super();
		this.op = op;
		eLeft = left;
		eRight = right;
	}

	public int getOp() {
		return op;
	}

	public BoolRetExp getELeft() {
		return eLeft;
	}

	public BoolRetExp getERight() {
		return eRight;
	}

	public void setOp(int op) {
		this.op = op;
	}

	public void setELeft(BoolRetExp left) {
		eLeft = left;
	}

	public void setERight(BoolRetExp right) {
		eRight = right;
	}

	@Override
	public boolean eval() {
		boolean bReturn;
		boolean bLeft = eLeft.eval();
		boolean bRight = eRight.eval();

		switch (op) {
		case Constant.AND:
			bReturn = (bLeft && bRight);
			break;
		case Constant.OR:
			bReturn = (bLeft || bRight);
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
		case Constant.AND:
			return "(" + eLeft.toString() + " AND " + eRight.toString() + ")";
		case Constant.OR:
			return "(" + eLeft.toString() + " OR " + eRight.toString() + ")";
		default:
			return "(" + eLeft.toString() + " BINBOOLARITH(" + "UNKNOWN" + ") "
					+ eRight.toString() + ")";
		}
	}
}
