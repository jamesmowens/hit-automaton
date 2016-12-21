package com.hp.hpl.CHAOS.HIT;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

public class Transition {
	Label label;
	boolean isTerminating;
	boolean isEnter;
	boolean isInstantiating;
	NonAtomicState container;
	State source;
	AtomicState target;	
	String hitVisSourceLabel;
	String hitVisTargetLabel;
	String hitVIsTranLabel;
	
	public Transition(Label l, boolean t, boolean i, boolean e, NonAtomicState c) {
		this.label = l;	
		this.isTerminating = t; 
		this.isInstantiating = i;
		this.isEnter = e;
		this.container = c;
	}
	
	// returns true if an enter transition is enabled 
	public boolean isEnabled(Event e) {
		return this.label.isSatisfied(new Vector<StateInstance>(), new Vector<Substitution>(), e);
	}
	
	// returns true if a transition is enabled in a state instance i
	public boolean isEnabled(Vector<StateInstance> config, StateInstance i, Event e) {
		if (i.activeState == this.source) {					
			return this.label.isSatisfied(config, i.substitutions, e);
		} else {
			for (StateInstance c : i.getDescendants()) {				
				if (c.activeState == this.source) {					
					return this.label.isSatisfied(config, i.substitutions, e);
		}}}
		return false;
	}
	
	// creates a new instance r of a root non-atomic state root
	public Vector<StateInstance> instantiate(Vector<StateInstance> configuration, Event e, NonAtomicState root) {
		
		// s1 is a substitution for variables and event identifiers (including last)
		Vector<Substitution> s1 = this.label.atom.getSubstitutions(e);
		
		StateAtom name = root.name.applySubstitutions(s1).skipID();	
		double start = e.end;
		double end = -1;
		State activeState = this.target.getAncestorOrSelf(root.children);
			
		// s2 is a substitution for state identifiers
		StateIDSubst s2 = root.name.getSubstitutions(name);
		if (s2 != null) {
			s1.add(s2);
		}
			
		StateInstance r = new StateInstance(root, name, start, end, activeState, s1, null, new Vector<StateInstance>());
		configuration.add(r);		
		
		if (activeState != this.target) {
			configuration = instantiate(configuration, e, (NonAtomicState) activeState, r); // !!!
		}		
		
		return configuration;
	}
	
	// creates a new instance j of a non-atomic state n, j is a subinstance of i, i is updated
	public Vector<StateInstance> instantiate(Vector<StateInstance> configuration, Event e, NonAtomicState n, StateInstance i) {
			
		// s1 is a substitution for variables and event identifiers (including last)
		Vector<Substitution> s1 = this.label.atom.getSubstitutions(e);
		
		StateAtom name = n.name.applySubstitutions(s1).applySubstitutions(i.substitutions).skipID();	
		double start = e.end;
		double end = -1;
		State activeState = this.target.getAncestorOrSelf(n.children);
		
		// s2 is a substitution for state identifiers
		StateIDSubst s2 = n.name.getSubstitutions(name);
		if (s2 != null) {
			s1.add(s2);
		}
		
		i.mergeSubstitutions(s1);
		
		Vector<Substitution> new_sub = new Vector<Substitution>();
		new_sub.addAll(s1);
		
		for(Substitution old_sub : i.substitutions) {
			
			if(!old_sub.isPresentIn(new_sub)) {
				new_sub.add(old_sub);
		}}		
				
		StateInstance j = new StateInstance(n, name, start, end, activeState, new_sub, i, new Vector<StateInstance>());
		i.children.add(j);				
		configuration.add(j);	
			
		if (activeState != this.target) {
			configuration = instantiate(configuration, e, (NonAtomicState) activeState, j); // !!!
		}		
		return configuration;
	}
	
	// prints pairs of transitions and events to file
	public void printPair(Event e){
		String userHomeFolder = System.getProperty("user.home");
		File file = new File(userHomeFolder, "toHIT.txt");
		try {
			if (!file.exists()) {			
				file.createNewFile();			
			}
			FileWriter fw;
			fw = new FileWriter(file, true);
		
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(e.type);
			bw.write(" ");
			bw.write(String.valueOf(e.start));
			bw.write(" ");
			bw.write(String.valueOf(e.end));
			bw.write(" ");
			for (GroundAttribute a : e.attributes){
				bw.write(a.name);
				bw.write(" ");
				bw.write(String.valueOf(a.value.value));
			}
			bw.write(" ");
			bw.write("from_");
			if (source != null) {
				bw.write(source.toString());
			} else {
				bw.write("nowhere");
			}
			bw.write("_to_");
			bw.write(target.toString()); // id
			bw.write(" ");
			bw.write(label.toString()); // label
			bw.write(" ");
			if (source != null) {  // source
				bw.write(source.toString());
			} else {
				bw.write("null");
			}
			bw.write(" ");
			bw.write(target.toString()); // target
			bw.write(" ");
			if (isEnter) { // enter
				bw.write("true");
			} else {
				bw.write("false");
			}		 
			bw.write(" ");
			if (isInstantiating) { // instatiating
				bw.write("true");
			} else {
				bw.write("false");
			}	 
			bw.write(" ");
			if (isTerminating) { // terminating
				bw.write("true");
			} else {
				bw.write("false");
			}	 
			bw.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
	}
	
	// fires this transition which belongs to no non-atomic state
	public Vector<StateInstance> fire(Vector<StateInstance> configuration, Event e) {
		
		// print out this transition
		System.out.println(this.toString());
		
		// I. Termination: All existing state instances are terminated
		if (this.isTerminating) {
			configuration.removeAllElements();
		}
		 	
		// II. Instantiation: Create the new instance of the root non-atomic state root	
		if (this.isInstantiating) {
			configuration = instantiate(configuration, e, this.target.getRoot());						
		}
		
		// print out current configuration 
		System.out.println("Configuration consists of: \n");
		for(StateInstance z : configuration) {
			System.out.println(z.toString());
		}
		return configuration;
	}
	
	// fires this transition which belongs to a non-atomic state within the instance i of that non-atomic state
	public Vector<StateInstance> fire(Vector<StateInstance> configuration, StateInstance i, Event e) {
		
		// print out this transition
		System.out.println(this.toString());
		
		// I. Termination: Subinstances of i are terminated
		if (this.isTerminating) {
			configuration.removeAll(i.getDescendants());
			i.children.removeAllElements();
		}
		
		// II. Transformation: Update the active state, the substitution of i and the substitutions of ancestors of i 		
		i.activeState = this.target.getAncestorOrSelf(i.state.children);
		
		// s1 is a substitution for variables and event identifiers (including last)
		Vector<Substitution> s1 = this.label.atom.getSubstitutions(e);
		i.mergeSubstitutions(s1);
		
		// propagate the substitution last -> e in all superinstances of i
		i.updateLastInAncestors(e);		
		
		// III. Instantiation: Create the new instance j, update the substitution and children of i	
		if (this.isInstantiating) {					
			configuration = instantiate(configuration, e, (NonAtomicState) i.activeState, i); // !!!
		}
		
		// print out current configuration 
		System.out.println("Configuration consists of: \n");
		for(StateInstance z : configuration) {
			System.out.println(z.toString());
		}		
		return configuration;
	}
	
	public String toString() {
		String s = "The ";
		if (this.isTerminating) {
			s = s.concat("terminating ");
		}
		if (this.isInstantiating) {
			s = s.concat("instantiating ");
		}
		if (this.isEnter) {
			s = s.concat("enter ");
		}
		return s + "transition with the label " + this.label.toString() + " fires.\n";
	}

	public String getHitVisSourceLabel() {
		return hitVisSourceLabel;
	}

	public void setHitVisSourceLabel(String hitVisSourceLabel) {
		this.hitVisSourceLabel = hitVisSourceLabel;
	}

	public String getHitVisTargetLabel() {
		return hitVisTargetLabel;
	}

	public void setHitVisTargetLabel(String hitVisTargetLabel) {
		this.hitVisTargetLabel = hitVisTargetLabel;
	}

	public String getHitVIsTranLabel() {
		return hitVIsTranLabel;
	}

	public void setHitVIsTranLabel(String hitVIsTranLabel) {
		this.hitVIsTranLabel = hitVIsTranLabel;
	}
}


