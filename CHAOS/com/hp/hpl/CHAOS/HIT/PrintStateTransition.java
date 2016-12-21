package com.hp.hpl.CHAOS.HIT;

public class PrintStateTransition {
	static int num = 0;

	static public void printState(State s) {
		if(s instanceof AtomicState) {
			printAtomicState((AtomicState)s);
		}
		else
		{
			printNonAtomicState((NonAtomicState)s);
		}
	}

	private static void printNonAtomicState(NonAtomicState s) {
		print("NonAtomicState " + printStateAtom(s.name));
		print("status: start = " +s.start + ", end = " +s.end);
		if(s.outgoingTransitions==null) print("null");
		else
		{
			print("OutgoingTransition (" + s.outgoingTransitions.size() + ")");
			for(int i=0;i<s.outgoingTransitions.size();i++)
			{
				num++;
				printTransition(s.outgoingTransitions.elementAt(i));
				num--;
			}
		}
		if(s.parent==null) {
			print("parent = null");
		}
		else
		{
			print("parent = " + s.parent.name.type);
		}
		if(s.ingoingTransitions==null) print("null");
		else
		{
			print("IngoingTransition (" + s.ingoingTransitions.size() + ")");
			for(int i=0;i<s.ingoingTransitions.size();i++)
			{
				num++;
				printTransition(s.ingoingTransitions.elementAt(i));
				num--;
			}
		}
		print("Chilren (" + s.children.size() + ")");	
		for(int i=0;i<s.children.size();i++)
		{
			num++;
			printState(s.children.elementAt(i));
			num--;
		}
	}

	private static String printStateAtom(StateAtom name) {
		String s = "";
		s+= "id = " + name.id.name;
		s+= ",type = " + name.type;
		s+= ",attrs = ";
		for(int i=0;i<name.attributes.size();i++)
		{
			if(i!=0) s+=",";
			s+=((NonGroundAttribute)name.attributes.elementAt(i)).name;
			s+="(" + ((NonGroundAttribute)name.attributes.elementAt(i)).value.name;
			if(((NonGroundAttribute)name.attributes.elementAt(i)).value.flagged) {
				s+="*";
			}
			s+=")";
		}
		return s;
	}

	private static void printAtomicState(AtomicState s) {
		print("AtomicState " + printStateAtom(s.name));
		print("status: start = " +s.start + ", end = " +s.end);
		if(s.outgoingTransitions==null) print("null");
		else
		{
			print("OutgoingTransition (" + s.outgoingTransitions.size() + ")");
			for(int i=0;i<s.outgoingTransitions.size();i++)
			{
				num++;
				printTransition(s.outgoingTransitions.elementAt(i));
				num--;
			}
		}
		if(s.parent==null) {
			print("parent = null");
		}
		else
		{
			print("parent = " + s.parent.name.type);
		}
	}

	private static void printTransition(Transition t) {
		String s = "Transition " + printLable(t.label);
		print(s);
		s="from ";
		if(t.source==null)
		{
			s+="no where";
		}
		else
		{
			s+=t.source.name.type;
		}
		s+=" to " + t.target.name.type;
		print(s);
		s="container = ";
		if(t.container==null) {
			s+="null";
		}
		else
		{
			s+=t.container.name.type;
		}
		print(s);
	}

	private static String printLable(Label label) {
		String s = "";
		s+=printEventAtom(label.atom);
		for(int i=0;i<label.constraints.size();i++) {
			s+=";" + label.constraints.elementAt(i).toString();
		}
		return s;
	}


	private static String printEventAtom(EventAtom name) {
		String s = "";
		s+= "id = " + name.id.name;
		s+= ",type = " + name.type;
		s+= ",attrs = ";
		for(int i=0;i<name.attributes.size();i++)
		{
			if(i!=0) s+=",";
			s+=((NonGroundAttribute)name.attributes.elementAt(i)).name;
			s+="(" + ((NonGroundAttribute)name.attributes.elementAt(i)).value.name;
			if(((NonGroundAttribute)name.attributes.elementAt(i)).value.flagged) {
				s+="*";
			}
			s+=")";
		}
		return s;
	}

	private static void print(String s) {
		for(int i=0;i<num;i++) {
			System.out.print("\t");
		}
		System.out.println(s);
	}
}
