package com.hp.hpl.CHAOS.Expression;

import com.hp.hpl.CHAOS.StreamData.SchemaElement;

//Expression Evaluation:
//1. get tuple, get associated SchemaElement[]
//2. setTuple() for every schemaElement
//3. rootExp.eval()
//NOTE: the terminal expression include the same instance of the schemaElement

public class Str20Terminal extends UniStr20RetExp {
	private static final long serialVersionUID = 1L;
	SchemaElement sch;

	public Str20Terminal(Str20RetExp child) {
		super(null);
		this.sch = null;
	}

	public Str20Terminal(SchemaElement sch) {
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
	public char[] eval() {
		char[] ret = sch.getStr20Value();
		return ret;
	}

	@Override
	public String toString() {
		return "Str20Term " + sch;
	}

}
