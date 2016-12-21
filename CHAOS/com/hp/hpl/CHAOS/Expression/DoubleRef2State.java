package com.hp.hpl.CHAOS.Expression;

import java.util.Vector;
import com.hp.hpl.CHAOS.HIT.Event;
import com.hp.hpl.CHAOS.HIT.StateID;
import com.hp.hpl.CHAOS.HIT.StateAtom;
import com.hp.hpl.CHAOS.HIT.StateInstance;
import com.hp.hpl.CHAOS.HIT.Substitution;

public class DoubleRef2State extends DoubleReference {
	private static final long serialVersionUID = 1L;
	
	public DoubleRef2State(DoubleRetExp child) {
		super(null);	
	}
	
	public DoubleRef2State(StateID i, String n) {
		super(i,n);
	}

	public DoubleConstant applySubstitutions(Vector<StateInstance> config, Vector<Substitution> sub, Event event) {
		// find the binding of this.id in sub
		for (Substitution s : sub) {			
			if (this.id.equals(s.symbol) && s.getAtoms(this.id.name)!=null) {
				// find the total number of the state instances referenced by this.id
				Vector<StateAtom> atoms = s.getAtoms(this.id.name);
				if (this.name.equals("countAll")) {
					int c = atoms.size();					
					return new DoubleConstant(c);
				} else {
				// find the number of finished state instances referenced by this.id
				if (this.name.equals("countFinished")) {
					int c = 0;
					for(StateAtom a : atoms) {
						if (a.isFinished(config)) {
							c = c+1;
					}}
					return new DoubleConstant(c);
				} else {
					// find the name a of the last state instance referenced by this.id
					StateAtom a = atoms.lastElement();
					// find the value of the attribute this.name carried by the state instance a
					DoubleConstant c = a.getConstant(config, this.name);
					return c;
		}}}}			
		return null;
	}
}
