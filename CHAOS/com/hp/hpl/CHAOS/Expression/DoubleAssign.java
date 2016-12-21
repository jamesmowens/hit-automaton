package com.hp.hpl.CHAOS.Expression;

import java.util.Vector;

import com.hp.hpl.CHAOS.HIT.Event;
import com.hp.hpl.CHAOS.HIT.StateInstance;
import com.hp.hpl.CHAOS.HIT.Substitution;
import com.hp.hpl.CHAOS.HIT.VariableSubst;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;

public class DoubleAssign extends UniDoubleRetExp {
	private static final long serialVersionUID = 1L;
	SchemaElement[] schA;
	int colIndex;
	byte[] tuple;

	public DoubleAssign(DoubleRetExp child) {
		super(child);
		this.schA = null;
		this.colIndex = 0;
		this.tuple = null;
	}

	public DoubleAssign(DoubleRetExp child, SchemaElement[] schA, int colIndex,
			byte[] tuple) {
		super(child);
		this.schA = schA;
		this.colIndex = colIndex;
		this.tuple = tuple;
	}

	public SchemaElement[] getSchA() {
		return schA;
	}

	public int getColIndex() {
		return colIndex;
	}

	public byte[] getTuple() {
		return tuple;
	}

	public void setSchA(SchemaElement[] schA) {
		this.schA = schA;
	}

	public void setColIndex(int colIndex) {
		this.colIndex = colIndex;
	}

	public void setTuple(byte[] tuple) {
		this.tuple = tuple;
	}

	@Override
	public double eval() {
		// Before evaluation, setTuple(byte[] targetTuple) must be called.
		double ret = this.eChild.eval();
		StreamAccessor.setCol(ret, tuple, schA, colIndex);
		this.tuple = null;
		return ret;
	}

	@Override
	public String toString() {
		return "DoubleAssign " + Integer.toString(colIndex);
	}

	@Override
	public DoubleRetExp applySubstitutions(Vector<StateInstance> config, Vector<Substitution> sub, Event e) {
		// TODO Auto-generated method stub
		return null;
	}

}
