package com.hp.hpl.CHAOS.Expression;

import java.io.Serializable;
import java.util.Vector;
import com.hp.hpl.CHAOS.HIT.Event;
import com.hp.hpl.CHAOS.HIT.StateInstance;
import com.hp.hpl.CHAOS.HIT.Substitution;

public abstract class DoubleRetExp extends Expression implements Serializable{
	
	abstract public DoubleRetExp applySubstitutions(Vector<StateInstance> config, Vector<Substitution> sub, Event e);

	abstract public double eval();

	abstract public String toString();
}
