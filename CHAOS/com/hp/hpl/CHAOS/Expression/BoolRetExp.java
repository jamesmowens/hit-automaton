package com.hp.hpl.CHAOS.Expression;

import java.io.Serializable;

public abstract class BoolRetExp extends Expression implements Serializable{
	abstract public boolean eval();

	@Override
	abstract public String toString();
}
