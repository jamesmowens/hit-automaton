package com.hp.hpl.CHAOS.AnormalDetection;

import java.util.ArrayList;
import java.util.List;


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
	

	public int getNum() {
		int num = 0;
		for (int i = 0; i < paraNum; i++) {
			String content = parameters[i];
			if (content.contains("num")) {
				int conLen = content.length();
				int start = content.indexOf("=");
				num = Integer.parseInt(content.substring(start + 1, conLen));
				break;
			}
		}
		return num;
	}

	public int getWindow() {
		int window = 0;
		for (int i = 0; i < paraNum; i++) {
			String content = parameters[i];
			if (content.contains("window")) {
				int conLen = content.length();
				int start = content.indexOf("=");
				window = Integer.parseInt(content.substring(start + 1, conLen));
				break;
			}
		}
		return window;
	}
	
	public int getSlide() {
		int slide = 0;
		for (int i = 0; i < paraNum; i++) {
			String content = parameters[i];
			if (content.contains("slide")) {
				int conLen = content.length();
				int start = content.indexOf("=");
				slide = Integer.parseInt(content.substring(start + 1, conLen));
				break;
			}
		}
		return slide;
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
	
	/*
	public static void main (String[] args) {
		String value = "id=1 range=10 num=15 window=50 slide=3 queryList=(1,2)";
		XMLVarParser parser = new XMLVarParser(value);
		int id = parser.getId();
		System.out.println(id);
		int range = parser.getRange();
		System.out.println(range);
		int num = parser.getNum();
		System.out.println(num);
		int window = parser.getWindow();
		System.out.println(window);
		int slide = parser.getSlide();
		System.out.println(slide);
		List<Integer> queryIdList = parser.getQueryIdList();
		for (int i = 0; i < queryIdList.size(); i++) {
			System.out.println(queryIdList.get(i));
		}
	}
	*/

	
}
