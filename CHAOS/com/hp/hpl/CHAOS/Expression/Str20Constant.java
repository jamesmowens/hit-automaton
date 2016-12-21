package com.hp.hpl.CHAOS.Expression;

import com.hp.hpl.CHAOS.StreamData.Utility;

public class Str20Constant extends UniStr20RetExp {
	private static final long serialVersionUID = 1L;
	char[] value;

	public Str20Constant(Str20RetExp child) {
		super(null);
		this.value = null;
	}

	public Str20Constant(char[] value) {
		super(null);
		this.value = value;
	}

	public Str20Constant(String str) {
		super(null);
		this.value = Utility.toCharA(Utility.toByta(str));
	}

	public char[] getValue() {
		return value;
	}

	public void setValue(char[] value) {
		this.value = value;
	}

	@Override
	public char[] eval() {
		return this.value;
	}

	@Override
	public String toString() {
		return "Str20Const " + new String(this.value);
	}

}
