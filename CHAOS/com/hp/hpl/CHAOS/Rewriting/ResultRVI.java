package com.hp.hpl.CHAOS.Rewriting;

import java.util.ArrayList;


public class ResultRVI {

	protected ArrayList<byte[]> result = new ArrayList<byte[]>();
	protected ArrayList<ArrayList<Integer>> checkingQid = new ArrayList<ArrayList<Integer>>();

	
	
	public ResultRVI(ArrayList<ArrayList<Integer>> checkingQid,
			ArrayList<byte[]> result) {
		super();
		this.checkingQid = checkingQid;
		this.result = result;
	}

	public ArrayList<byte[]> getResult() {
		
		return result;
	}

	public void setResult(ArrayList<byte[]> result) {
		this.result = result;
	}

	public ArrayList<ArrayList<Integer>> getCheckingQid() {
		return checkingQid;
	}

	public void setCheckingQid(ArrayList<ArrayList<Integer>> checkingQid) {
		this.checkingQid = checkingQid;
	}

}
