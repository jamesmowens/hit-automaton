package com.hp.hpl.CHAOS.Aggregation;


import java.util.ArrayList;

public class XMLVarParser {
	/**
	 * This class is used to parse the query/subsequence parameters
	 * get the information including event types, window size, ID, querylist, SSlist, etc
	 * @author Yingmei Qi
	 */
	
	//get trimed value
	String[] parameters;
	public int paraNum;
	
	public XMLVarParser(){
		
	}
	
	public XMLVarParser(String value) {
		
		
		this.parameters = value.split(" ");
		paraNum= parameters.length;
		
	}
	
	 
	
	//get the event types of this query/subseq
	public ArrayList<String> getEventTypes() {
		
		ArrayList<String> eventTypes = new ArrayList<String>();
		
		for (int i = 0; i < paraNum; i++) {
			String content = parameters[i];
			if (content.contains("SEQ")){
				
				int q_SEQ_start = content.indexOf("(");
				int q_SEQ_end = content.indexOf(")");
				String trimedvalue1 = (String) content.subSequence(q_SEQ_start + 1, q_SEQ_end
						);
				String[] result = trimedvalue1.split(",");
				
				for (int j = 0; j < result.length; j++) {
					eventTypes.add(result[j]);
				}
				break;
			}
			
		}
		return eventTypes;
	}
	
	public int getID() {
		int ID = 0;
		for (int i = 0; i < paraNum; i++) {
			String content = parameters[i];
			int conLen = content.length();
			if (content.contains("id")){
				int id_start = content.indexOf("=");
				ID = Integer.parseInt((String)content.subSequence(id_start+1, conLen));
				break;
			}
			
		}
		return ID;
	}
	
	public int getOtherID(String idName) {
		int ID = 0;
		for (int i = 0; i < paraNum; i++) {
			String content = parameters[i];
			int conLen = content.length();
			if (content.contains(idName)){
				int id_start = content.indexOf("=");
				ID = Integer.parseInt((String)content.subSequence(id_start+1, conLen));
				break;
			}
			
		}
		return ID;
	}
	
	public int getWindow() {
		int window = 0;
		for (int i = 0; i < paraNum; i++) {
			String content = parameters[i];
			if (content.contains("window")){
				int conLen = content.length();
				int id_start = content.indexOf("=");
				window = Integer.parseInt((String)content.subSequence(id_start+1, conLen));
System.out.println(id_start + 1 + " " + conLen);
				break;
			}
			
		}
		return window;
		
	}
	
	public ArrayList<Integer> getIDlist(String para) {
		ArrayList<Integer> IDList = new ArrayList<Integer>();
		
		for (int i = 0; i < paraNum; i++) {
			String content = parameters[i];
			if (content.contains(para)){
				
				int q_SEQ_start = content.indexOf("(");
				int q_SEQ_end = content.indexOf(")");
				String trimedvalue1 = (String) content.subSequence(q_SEQ_start + 1, q_SEQ_end
						);
				String[] result = trimedvalue1.split(",");
				
				for (int j = 0; j < result.length; j++) {
					IDList.add(Integer.parseInt(result[j]));
				}
				break;
			}
			
		}
		return IDList;
	}
	
	/*
	public static void main (String[] args) {
		String value = "COUNT SEQ(A,B,C) id=1 window=50 queryList=(1,2) SSList=(4)";
		XMLVarParser parser = new XMLVarParser(value);
		
		int id = parser.getID();
		System.out.println(id);

		int window = parser.getWindow();

		System.out.println(window);
		ArrayList<Integer> queryIDlist = parser.getIDlist("queryList");
		
		for (int i = 0; i < queryIDlist.size(); i++) {
			System.out.print(queryIDlist.get(i));
		}
	}
	*/

}
