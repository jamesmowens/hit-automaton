package com.hp.hpl.CHAOS.Expression;

import java.util.Vector;

import com.hp.hpl.CHAOS.HIT.Event;
import com.hp.hpl.CHAOS.HIT.StateInstance;
import com.hp.hpl.CHAOS.HIT.Substitution;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;

//Expression Evaluation:
//1. get tuple, get associated SchemaElement[]
//2. setTuple() for every schemaElement
//3. rootExp.eval()
//NOTE: the terminal expression include the same instance of the schemaElement

public class DoubleTerminal extends UniDoubleRetExp {
	private static final long serialVersionUID = 1L;
	SchemaElement sch;

	public DoubleTerminal(DoubleRetExp child) {
		super(null);
		this.sch = null;
	}

	public DoubleTerminal(SchemaElement sch) {
		super(null);
		this.sch = sch;
	}

	public SchemaElement getSch() {
		return sch;
	}

	public void setSch(SchemaElement sch) {
		this.sch = sch;
	}

	@Override
	public double eval() {
		double ret = sch.getValue();
		return ret;
	}

	@Override
	public String toString() {
		return "DoubleTerm " + sch;
	}

	@Override
	public DoubleRetExp applySubstitutions(Vector<StateInstance> config, Vector<Substitution> sub, Event e) {
		// TODO Auto-generated method stub
		return null;
	}

}
