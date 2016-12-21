package com.hp.hpl.CHAOS.HIT;

import java.util.ArrayList;

public class XMLVarParser {
	private	String[] parameters = null;
	private int paraNum = 0;

	public XMLVarParser() {
		
	}
	
	public XMLVarParser(String value) {
		parameters = value.split(" ");
		paraNum = parameters.length;
	}

	public int getId() {
		int id = 0;
		for (int i = 0; i < paraNum; i++) {
			String content = parameters[0];
			if (content.contains("id")) {
				int conLen = content.length();
				int start = content.indexOf("=");
				id = Integer.parseInt(content.substring(start + 1, conLen));
				break;
			}
		}
		return id;
	}

	public double getRange() {
		double range = 0;
		for (int i = 0; i < paraNum; i++) {
			String content = parameters[i];
			if (content.contains("range")) {
				int conLen = content.length();
				int start = content.indexOf("=");
				range = Double.parseDouble(content.substring(start + 1, conLen));
				break;
			}
		}
		return range;
	}
	
	public ArrayList<Integer> getQueryIdList() {
		ArrayList<Integer> queryIdList = new ArrayList<Integer>();
		for (int i = 0; i < paraNum; i++) {
			String content = parameters[i];
			if (content.contains("queryList")) {
				int start = content.indexOf("(");
				int end = content.indexOf(")");
				String trimedVaule = content.substring(start + 1, end);
				String[] results = trimedVaule.split(",");
				for (int j = 0; j < results.length; j++) {
					queryIdList.add(Integer.parseInt(results[j]));
				}
				break;
			}
		}
		return queryIdList;
	}
}
