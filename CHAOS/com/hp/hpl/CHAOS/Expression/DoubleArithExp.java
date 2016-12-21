package com.hp.hpl.CHAOS.Expression;

import java.util.Vector;
import com.hp.hpl.CHAOS.HIT.Event;
import com.hp.hpl.CHAOS.HIT.StateInstance;
import com.hp.hpl.CHAOS.HIT.Substitution;

public class DoubleArithExp extends BinDoubleRetExp implements CompExpression {
	private static final long serialVersionUID = 1L;
	int op;

	public DoubleArithExp(int op, DoubleRetExp left, DoubleRetExp right) {
		super(left, right);
		this.op = op;
	}

	public int getOp() {
		return op;
	}

	public void setOp(int op) {
		this.op = op;
	}
	
	@Override
	public DoubleArithExp applySubstitutions(Vector<StateInstance> config, Vector<Substitution> sub, Event e) {
		return new DoubleArithExp(this.op, this.eLeft.applySubstitutions(config,sub,e), this.eRight.applySubstitutions(config,sub,e)); 
	}

	@Override
	public double eval() {
		double dReturn;
		double dLeft = eLeft.eval();
		double dRight = eRight.eval();

		switch (op) {
		case Constant.PLUS:
			dReturn = dLeft + dRight;
			break;
		case Constant.MINUS:
			dReturn = dLeft - dRight;
			break;
		case Constant.MULTIPLY:
			dReturn = dLeft * dRight;
			break;
		case Constant.DIVIDE:
			dReturn = dLeft / dRight;
			break;
		case Constant.MOD:
			dReturn = dLeft % dRight;
			break;
		default:
			/* fail */
			dReturn = 0.0;
		}

		return dReturn;
	}

	// for debug purpose only
	@Override
	public String toString() {
		switch (op) {
		case Constant.PLUS:
			return "(" + eLeft.toString() + " + " + eRight.toString() + ")";
		case Constant.MINUS:
			return "(" + eLeft.toString() + " - " + eRight.toString() + ")";
		case Constant.MULTIPLY:
			return "(" + eLeft.toString() + " * " + eRight.toString() + ")";
		case Constant.DIVIDE:
			return "(" + eLeft.toString() + " / " + eRight.toString() + ")";
		case Constant.MOD:
			return "(" + eLeft.toString() + " % " + eRight.toString() + ")";
		default:
			return "(" + eLeft.toString() + " Arith(" + "UNKNOWN" + ") "
					+ eRight.toString() + ")";
		}
	}
}
