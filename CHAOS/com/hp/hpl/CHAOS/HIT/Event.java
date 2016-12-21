package com.hp.hpl.CHAOS.HIT;

import java.util.*;
import com.hp.hpl.CHAOS.Expression.DoubleConstant;

public class Event {
	String type;
	public double start;
	public double end;
	public Vector<GroundAttribute> attributes;
	
	public Event (String t, double s, double e, Vector<GroundAttribute> a) {
		this.type = t;
		this.start = s;
		this.end = e;
		this.attributes = a;		
	}
	
	public DoubleConstant getConstant(String name) {
		// get the value of a time attribute
		if (name.equals("start")) {
			return new DoubleConstant(this.start);
		} else {
		if (name.equals("end")) {
			return new DoubleConstant(this.end);
		} else {
		// get the value of a data attribute
		for (GroundAttribute a : this.attributes) {
			DoubleConstant c = a.getConstant(name);
			if (c!=null) {
				return c; 
		}}}}
		return null;
	}
	
	public String toString() {
		String s = this.type + "( ";
		for (Attribute attr : this.attributes) {
			s = s.concat(attr.toString() + " ");
		}
		return s + ") with occurrence time [" + this.start + "," + this.end + "]";
	}

}
