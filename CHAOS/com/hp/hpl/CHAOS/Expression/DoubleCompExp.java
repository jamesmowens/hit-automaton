package com.hp.hpl.CHAOS.Expression;

import java.util.Vector;

import com.hp.hpl.CHAOS.HIT.Event;
import com.hp.hpl.CHAOS.HIT.StateInstance;
import com.hp.hpl.CHAOS.HIT.Substitution;

public class DoubleCompExp extends BoolRetExp implements CompExpression {
	private static final long serialVersionUID = 1L;
	int op;
	DoubleRetExp eLeft, eRight;

	public DoubleCompExp(int op, DoubleRetExp left, DoubleRetExp right) {
		super();
		this.op = op;
		eLeft = left;
		eRight = right;
	}

	public int getOp() {
		return op;
	}

	public DoubleRetExp getELeft() {
		return eLeft;
	}

	public DoubleRetExp getERight() {
		return eRight;
	}

	public void setOp(int op) {
		this.op = op;
	}

	public void setELeft(DoubleRetExp left) {
		eLeft = left;
	}

	public void setERight(DoubleRetExp right) {
		eRight = right;
	}
	
	public DoubleCompExp applySubstitutions(Vector<StateInstance> config, Vector<Substitution> sub, Event e){
		return new DoubleCompExp(this.op, this.eLeft.applySubstitutions(config,sub,e), this.eRight.applySubstitutions(config,sub,e));
	}

	@Override
	public boolean eval() {
		boolean bReturn;
		double dLeft = eLeft.eval();
		double dRight = eRight.eval();

		switch (op) {
		case Constant.LT:
			bReturn = (dLeft < dRight);
			break;
		case Constant.GT:
			bReturn = (dLeft > dRight);
			break;
		case Constant.LEQ:
			bReturn = (dLeft <= dRight);
			break;
		case Constant.GEQ:
			bReturn = (dLeft >= dRight);
			break;
		case Constant.EQ:
			bReturn = (dLeft == dRight);
			break;
		case Constant.NEQ:
			bReturn = (dLeft != dRight);
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
}
