package com.hp.hpl.CHAOS.HIT;

import java.util.Vector;

import com.hp.hpl.CHAOS.Expression.Constant;
import com.hp.hpl.CHAOS.Expression.DoubleArithExp;
import com.hp.hpl.CHAOS.Expression.DoubleCompExp;
import com.hp.hpl.CHAOS.Expression.DoubleConstant;
import com.hp.hpl.CHAOS.Expression.DoubleRef2Event;
import com.hp.hpl.CHAOS.Expression.DoubleRef2State;
import com.hp.hpl.CHAOS.Expression.DoubleRetExp;

public class ExtractStateTransitionLabel {
	static Vector<String> stateList = new Vector<String> ();
	static Vector<String> tranList = new Vector<String> ();
	
	/**
	 * Take a name of state from XML and parse it into StateAtom in CHAOS representation
	 * @param stateLabel The name of a state from XML
	 * @return StateAtom in CHAOS representation which is parsed from XML
	 */
	static StateAtom extractStateLabel(String stateLabel) {
		int i;
		i = stateLabel.indexOf(":");
		if(i==-1) // no semicolon
		{
			i = stateLabel.indexOf("(");
			if(i==-1) // no attribute
			{
				// case 1: j
				return new StateAtom(new StateID(""), stateLabel, new Vector<Attribute>());
			}
			else // has attributes
			{
				// case 2: j(...)
				return new StateAtom(new StateID(""), stateLabel.substring(0,i), extractAllAttributes(stateLabel.substring(i+1,stateLabel.length()-1)));
			}
		}
		else // has semicolon
		{
			String id = stateLabel.substring(0,i);
			stateLabel = stateLabel.substring(i+1);
			i = stateLabel.indexOf("(");
			if(i==-1) // no attribute
			{
				// case 3: j:Auction
				addState(id);
				return new StateAtom(new StateID(id),stateLabel,new Vector<Attribute>());
			}
			else // has attributes
			{
				// case 4: j:Auction(...)
				addState(id);
				return new StateAtom(new StateID(id),stateLabel.substring(0,i), extractAllAttributes(stateLabel.substring(i+1,stateLabel.length()-1)));				
			}
		}
	}

	/**
	 * Take a string containing attributes of a name of state, then parse it to a Vector of Attribute in CHAOS representation
	 * @param s String containing attributes of a name of state
	 * @return A vector of Attribute in CHAOS representation which is parsed from XML
	 */
	private static Vector<Attribute> extractAllAttributes(String s) {
		Vector<Attribute> attrs = new Vector<Attribute>();
		int i = s.indexOf(",");
		while(i!=-1) // for each attribute
		{
			attrs.add(extractEachAttribute(s.substring(0,i)));
			s = s. substring(i+1);
			i = s.indexOf(",");
		}
		attrs.add(extractEachAttribute(s));		
		return attrs;
	}
	
	/**
	 * Take a string containing an attribute of a name of state, then parse it to an Attribute in CHAOS representation
	 * @param s String containing an attribute of a name of state
	 * @return Attribute in CHAOS representation which is parsed from XML
	 */
	private static Attribute extractEachAttribute(String s) {
		int i = s.indexOf("(");
		String attrName = s.substring(0, i);
		String attrVar = s.substring(i+1,s.length()-1);
		if(attrVar.charAt(attrVar.length()-1)=='*') // if star (*)
		{
			attrVar = attrVar.substring(0,attrVar.length()-1);		
			return new NonGroundAttribute(attrName, new Variable(attrVar, true));	
		}
		else
		{
			return new NonGroundAttribute(attrName, new Variable(attrVar, false));				
		}
	}

	/**
	 * Take a name of transition from XML and parse it into EventAtom in CHAOS representation
	 * @param tranLabel The name of a transition from XML
	 * @return EventAtom in CHAOS representation which is parsed from XML
	 */
	public static Label extractTranLabel(String tranLabel) {
		int i = tranLabel.indexOf(";");
		String constraintString;
		
		// seperate constraint part
		if(i==-1)
		{
			constraintString = "";
		}
		else
		{
			constraintString = tranLabel.substring(i+1);
			tranLabel = tranLabel.substring(0,i);
		}
		Vector<DoubleCompExp> constraints = extractAllConstraints(constraintString);		
		
		i = tranLabel.indexOf(":");
		if(i==-1) // no semicolon
		{
			i = tranLabel.indexOf("(");
			if(i==-1) // no attribute
			{
				// case 1: b
				return new Label(new EventAtom(new EventID(""), tranLabel, new Vector<Attribute>()),
								 constraints);
			}
			else // has attributes
			{
				// case 2: b(...)
				return new Label(new EventAtom(new EventID(""), tranLabel.substring(0,i), extractAllAttributes(tranLabel.substring(i+1,tranLabel.length()-1))),
								 constraints);
			}
		}
		else // has semicolon
		{
			String id = tranLabel.substring(0,i);
			tranLabel = tranLabel.substring(i+1);
			i = tranLabel.indexOf("(");
			if(i==-1) // no attribute
			{
				// case 3: b:bid
				return new Label(new EventAtom(new EventID(id),tranLabel,new Vector<Attribute>()),
								 constraints);
			}
			else // has attributes
			{
				// case 4: b:bid(...)
				return new Label(new EventAtom(new EventID(id),tranLabel.substring(0,i), extractAllAttributes(tranLabel.substring(i+1,tranLabel.length()-1))),
							  	 constraints);
			}
		}
	}

	/**
	 * Take a string containing a list of constraints of a transition, then parse it to a Vector of constraints in CHAOS representation
	 * @param constraintString A string containing a list of constraints of a transition
	 * @return A Vector of constraints in CHAOS representation which is parsed from XML
	 */
	private static Vector<DoubleCompExp> extractAllConstraints(
			String constraintString) {
		Vector<DoubleCompExp> constraints= new Vector<DoubleCompExp>();
		if(constraintString.equals("")) return constraints;
		int i = constraintString.indexOf(";");
		while(i!=-1)
		{
			constraints.add(extractEachConstraint(constraintString.substring(0,i)));
			constraintString = constraintString.substring(i+1);
			i = constraintString.indexOf(";");
		}
		constraints.add(extractEachConstraint(constraintString));
		return constraints;
	}

	/**
	 * Take a string containing a constraint of a transition, then parse it to a constraint in CHAOS representation
	 * @param s A string containing a constraint of a transition
	 * @return A constraint in CHAOS representation which is parsed from XML
	 */
	private static DoubleCompExp extractEachConstraint(String s) {
		int i;
		i = s.indexOf("<=");
		if(i!=-1) {
			return new DoubleCompExp(Constant.LEQ,extractArith(s.substring(0,i)),extractArith(s.substring(i+2,s.length())));
		}
		i = s.indexOf(">=");
		if(i!=-1) {
			return new DoubleCompExp(Constant.GEQ,extractArith(s.substring(0,i)),extractArith(s.substring(i+2,s.length())));
		}
		i = s.indexOf("!=");
		if(i!=-1) {
			return new DoubleCompExp(Constant.NEQ,extractArith(s.substring(0,i)),extractArith(s.substring(i+2,s.length())));
		}
		i = s.indexOf("<");
		if(i!=-1) {
			return new DoubleCompExp(Constant.LT,extractArith(s.substring(0,i)),extractArith(s.substring(i+1,s.length())));
		}
		i = s.indexOf(">");
		if(i!=-1) {
			return new DoubleCompExp(Constant.GT,extractArith(s.substring(0,i)),extractArith(s.substring(i+1,s.length())));
		}
		i = s.indexOf("=");
		if(i!=-1) {
			return new DoubleCompExp(Constant.EQ,extractArith(s.substring(0,i)),extractArith(s.substring(i+1,s.length())));
		}
		return null;
	}
	
	/**
	 * Take a string of arithmetic expression, then parse it to CHAOS representation
	 * @param s A string of arithmetic expression
	 * @return An arithmetic expression in CHAOS representation
	 */
	private static DoubleRetExp extractArith(String s) {								
		int i;
		i = s.indexOf("+");
		if(i!=-1)
		{
			return new DoubleArithExp(Constant.PLUS, extractValue(s.substring(0,i)), extractValue(s.substring(i+1)));
		}
		i = s.indexOf("-");
		if(i!=-1)
		{
			return new DoubleArithExp(Constant.MINUS, extractValue(s.substring(0,i)), extractValue(s.substring(i+1)));
		}
		i = s.indexOf("*");
		if(i!=-1)
		{
			return new DoubleArithExp(Constant.MULTIPLY, extractValue(s.substring(0,i)), extractValue(s.substring(i+1)));
		}
		i = s.indexOf("/");
		if(i!=-1)
		{
			return new DoubleArithExp(Constant.DIVIDE, extractValue(s.substring(0,i)), extractValue(s.substring(i+1)));
		}
		i = s.indexOf("%");
		if(i!=-1)
		{
			return new DoubleArithExp(Constant.MOD, extractValue(s.substring(0,i)), extractValue(s.substring(i+1)));
		}
		return extractValue(s);
	}

	/**
	 * Take a string of value, then parse it to CHAOS representation
	 * @param s A string of value
	 * @return The value in CHAOS representation
	 */
	private static DoubleRetExp extractValue(String s) {
		int i = s.indexOf(".");
		try
		{
			double value = Double.valueOf(s);
			return new DoubleConstant(value);
		}
		catch(Exception e) {
			
		}
		
		String id = s.substring(0,i);
		s = s.substring(i+1);
		if(stateList.contains(id)) {
			return new DoubleRef2State(new StateID(id), s);
		}
		if(tranList.contains(id)) {
			return new DoubleRef2Event(new EventID(id), s);			
		}
		if(id.equals("this") || id.equals("last")) {
			return new DoubleRef2Event(new EventID(id), s);				
		}
		//System.out.println("NOOOOOO" + id);
		return null;
	}
	
	private static void addState(String id) {
		if(!(stateList.contains(id))) {
			stateList.add(id);
		}
	}
	
	static void addTran(String id) {
		int i = id.indexOf(":");
		if(i==-1) return;
		id = id.substring(0,i);
		if(!(tranList.contains(id))) {
			tranList.add(id);
		}
	}
}
