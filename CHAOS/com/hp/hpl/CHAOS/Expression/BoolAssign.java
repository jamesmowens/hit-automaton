package com.hp.hpl.CHAOS.Expression;

import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;

public class BoolAssign extends UniBoolRetExp {
	private static final long serialVersionUID = 1L;
	SchemaElement[] schA;
	int colIndex;
	byte[] tuple;

	public BoolAssign(BoolRetExp child) {
		super(child);
		this.schA = null;
		this.colIndex = 0;
		this.tuple = null;
	}

	public BoolAssign(BoolRetExp child, SchemaElement[] schA, int colIndex,
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
	public boolean eval() {
		// Before evaluation, setTuple(byte[] targetTuple) must be called.
		boolean ret = this.eChild.eval();
		String value = ret ? "TRUE" : "FALSE";
		StreamAccessor.setCol(value, tuple, schA, colIndex);
		this.tuple = null;
		return ret;
	}

	@Override
	public String toString() {
		return "BoolAssign " + Integer.toString(colIndex);
	}

}
