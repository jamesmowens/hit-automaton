package edu.usfca.xj.appkit.document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PredicateParser {
	public PredicateParser() {

	}

	/**
	 * Parse a string as separated by semicolons and output the results in an ArrayList.
	 * @param in string
	 * @return parsed string
	 */
	// TODO: Fix parsing nulls?
	public List<List<String>> parse(String in) { 
		List<String> predList = new ArrayList<String>();
		List<List<String>> out = new ArrayList<List<String>>();
		if (in == null) {
			return out;
		}
		predList = Arrays.asList(in.split(";"));
		
		// remove whitespace
		for (int i = 0; i < out.size(); i++) {
			predList.set(i, predList.get(i).trim());
		}
		
		for (String pred : predList) {
			out.add(parseParens(pred));
		}
		
		return out;
	}
	
	/**
	 * Parse a string as separated by commas and output the results in an ArrayList.
	 * The first element in the resulting ArrayList is the beginning of the predicate,
	 * and the remaining elements are its attributes.
	 * @param in string
	 * @return parsed string
	 */
	public static List<String> parseParens(String in) {
		List<String> out = new ArrayList<String>();
		
		int openParen = in.indexOf("(");
		int closeParen = in.lastIndexOf(")");
		
		// predicate has no parentheses
		if (openParen == closeParen) {
			out.add(in);
			return out;
		}
		
		out.add(in.substring(0, openParen)); // add predicate ("name" in XML)
		String inner = in.substring(openParen+1, closeParen); // part inside the parentheses ("attribute" in XML)
		
		
		for (String str : Arrays.asList(inner.split(","))) { // output each substring of the inner parentheses as separate strings
			out.add(str);
		}
		
		// remove whitespace
		for (int i = 0; i < out.size(); i++) {
			out.set(i, out.get(i).trim());
		}
		
		return out;
	}
}
