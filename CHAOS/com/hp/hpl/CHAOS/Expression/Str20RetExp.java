package com.hp.hpl.CHAOS.Expression;

import java.io.Serializable;

public abstract class Str20RetExp extends Expression implements Serializable{
	abstract public char[] eval();

	@Override
	abstract public String toString();
}
