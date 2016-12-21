package com.hp.hpl.CHAOS.AnormalDetection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;

public class Slide {
	double start;
	double end;
	
	ArrayList<Tuple> triggeredOutliers = new ArrayList<Tuple>();
	ArrayList<Tuple> tuples = new ArrayList<Tuple>();
	//Map<Integer, ArrayList<Tuple>> slideId2PotentialOutliers = new HashMap<Integer, ArrayList<Tuple>>();

	public double getStart() {
		return start;
	}
	public void setStart(double start) {
		this.start = start;
	}
	public double getEnd() {
		return end;
	}
	public void setEnd(double end) {
		this.end = end;
	}	
	
	public ArrayList<Tuple> getTriggeredOutliers() {
		return triggeredOutliers;
	}
	public void setTriggeredOutliers(ArrayList<Tuple> triggeredOutliers) {
		this.triggeredOutliers = triggeredOutliers;
	}	

	public ArrayList<Tuple> getTuples() {
		return tuples;
	}
	public void setTuples(ArrayList<Tuple> tuples) {
		this.tuples = tuples;
	}
	
	/*
	public Map<Integer, ArrayList<Tuple>> getSlideId2PotentialOutliers() {
		return slideId2PotentialOutliers;
	}
	public void setSlideId2PotentialOutliers(
			Map<Integer, ArrayList<Tuple>> slideId2PotentialOutliers) {
		this.slideId2PotentialOutliers = slideId2PotentialOutliers;
	}
	
	*/
	
	/*
	//store the different type of tuple in the current window
	ArrayList<Tuple> safeTuples = new ArrayList<Tuple>();
	ArrayList<Tuple> unsafeTuples = new ArrayList<Tuple>();
	ArrayList<Tuple> outlierTuples = new ArrayList<Tuple>();
	public ArrayList<Tuple> getSafeTuples() {
		return safeTuples;
	}
	public void setSafeTuples(ArrayList<Tuple> safeTuples) {
		this.safeTuples = safeTuples;
	}
	public ArrayList<Tuple> getUnsafeTuples() {
		return unsafeTuples;
	}
	public void setUnsafeTuples(ArrayList<Tuple> unsafeTuples) {
		this.unsafeTuples = unsafeTuples;
	}
	public ArrayList<Tuple> getOutlierTuples() {
		return outlierTuples;
	}
	public void setOutlierTuples(ArrayList<Tuple> outlierTuples) {
		this.outlierTuples = outlierTuples;
	}
	*/

	/*
	public static void main(String[] args) {
		Slide slide = new Slide();
		ArrayList<Tuple> triggeredOutlierArrayList = slide.getTriggeredOutliers();
		Tuple tuple = new Tuple();
		triggeredOutlierArrayList.add(tuple);
		if (triggeredOutlierArrayList == null) {
			System.out.println("null");
		} else {
			System.out.println("not null");
		}
		System.out.println(triggeredOutlierArrayList);
		System.out.println(triggeredOutlierArrayList.size());
	}
	*/
}
