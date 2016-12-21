package com.hp.hpl.CHAOS.AnormalDetection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Tuple {
	
	private double firstAttr;
	private double secondAttr;
	private double identifier;
	private int prevNghbNum = 0;
	private int succNghbNum = 0;
	private boolean safe = false;
	private boolean unsafe = false;
	private boolean outlier = true;
	Map<Slide, Integer> numOfNghbInEachSlideMapBySlide = new HashMap<Slide, Integer>();
	Tuple comparedPoint = this;
	Slide comparedSlide = null;
	
	public Tuple getComparedPoint() {
		return comparedPoint;
	}

	public void setComparePoint(Tuple comparePoint) {
		this.comparedPoint = comparePoint;
	}

	public double getDis(Tuple tuple) {
		//(this.firstAttr - tuple.getFirstAttribute()) * (this.firstAttr - tuple.getFirstAttribute()) + (this.secondAttr - tuple.getSecondAttribute()) * (this.secondAttr - tuple.getSecondAttribute());
		double dis = Math.sqrt(Math.pow((this.firstAttr - tuple.getFirstAttribute()), 2) + Math.pow((this.secondAttr - tuple.getSecondAttribute()), 2));
		return dis;
	}

	public double getFirstAttribute() {
		return firstAttr;
	}

	public void setFirstAttribute(double firstAttr) {
		this.firstAttr = firstAttr;
	}
	
	public double getSecondAttribute() {
		return secondAttr;
	}
	
	public void setSecondAttribute(double secondAttr) {
		this.secondAttr = secondAttr;
	}

	public double getIdentifier() {
		return identifier;
	}

	public void setIdentifier(double identifier) {
		this.identifier = identifier;
	}

	public int getPrevNghbNum() {
		return prevNghbNum;
	}
	
	public void setPrevNghbNum(int prevNghbNum) {
		this.prevNghbNum = prevNghbNum;
	}
	
	public int getSuccNghbNum() {
		return succNghbNum;
	}
	
	public void setSuccNghbNum(int succNghbNum) {
		this.succNghbNum = succNghbNum;
	}
	
	public boolean isSafe() {
		return safe;
	}
	
	public void setSafe(boolean safe) {
		this.safe = safe;
	}
	
	public boolean isUnsafe() {
		return unsafe;
	}
	
	public void setUnsafe(boolean unsafe) {
		this.unsafe = unsafe;
	}
	
	public boolean isOutlier() {
		return outlier;
	}
	
	public void setOutlier(boolean outlier) {
		this.outlier = outlier;
	}
	
	public Map<Slide, Integer> getNumOfNghbInEachSlideMapBySlide() {
		return numOfNghbInEachSlideMapBySlide;
	}

	public void setNumOfNghbInEachSlideMapBySlide(
			Map<Slide, Integer> numOfNghbInEachSlideMapBySlide) {
		this.numOfNghbInEachSlideMapBySlide = numOfNghbInEachSlideMapBySlide;
	}
	
	public void updateTriggeredSlide(ArrayList<Slide> slides, int num) {
		boolean findSlide = false;
		int count = this.succNghbNum;
		for (int i = slides.size(); i > 0; i--) {
			Slide slide = slides.get(i - 1);
			if (!findSlide) {
				int prevCount = count;
				if (this.numOfNghbInEachSlideMapBySlide.containsKey(slide)) {
					count += this.numOfNghbInEachSlideMapBySlide.get(slide);
					if (count >= num) {
						this.numOfNghbInEachSlideMapBySlide.remove(slide);
						if ((num - prevCount) <= 0) {
							slide.getTriggeredOutliers().remove(this);
						} else {
							this.numOfNghbInEachSlideMapBySlide.put(slide, num - prevCount);
						}
						findSlide = true;
					}
				} else {
					continue;
				}
			} else {
				this.numOfNghbInEachSlideMapBySlide.remove(slide);
				slide.getTriggeredOutliers().remove(this);
			}
			
		}
	}
	
	public void updateTriggeredSlide(ArrayList<Slide> slides, int num, Slide expiredSlide) {
		Collections.reverse(slides);
		slides.add(expiredSlide);
		Collections.reverse(slides);
		boolean findSlide = false;
		int count = this.succNghbNum;
		for (int i = slides.size(); i > 0; i--) {
			Slide slide = slides.get(i - 1);
			if (!findSlide) {
				int prevCount = count;
				if (this.numOfNghbInEachSlideMapBySlide.containsKey(slide)) {
					count += this.numOfNghbInEachSlideMapBySlide.get(slide);
					if (count >= num) {
						this.numOfNghbInEachSlideMapBySlide.remove(slide);
						if ((num - prevCount) <= 0) {
							slide.getTriggeredOutliers().remove(this);
						} else {
							this.numOfNghbInEachSlideMapBySlide.put(slide, num - prevCount);
						}
						findSlide = true;
					}
				} else {
					continue;
				}
			} else {
				this.numOfNghbInEachSlideMapBySlide.remove(slide);
				slide.getTriggeredOutliers().remove(this);
			}
			
		}
	}
	
	public void updateStatus(int num) {
		if (this.succNghbNum >= num) {
			this.succNghbNum = num;
			this.safe = true;
			this.unsafe = false;
			this.outlier = false;
		} else if (this.succNghbNum + this.prevNghbNum >= num) {
			this.safe = false;
			this.unsafe = true;
			this.outlier = false;
		} else if (this.succNghbNum + this.prevNghbNum < num) {
			this.safe = false;
			this.unsafe = false;
			this.outlier = true;
		}
	}
	
	/*
	public static void main ( String[] args) {
		Tuple tuple = new Tuple();
		tuple.setSuccNghbNum(4);
		ArrayList<Slide> slides = new ArrayList<Slide>();
		Slide slide1 = new Slide();
		slide1.setStart(1);
		slide1.setEnd(2);
		slides.add(slide1);
		Slide slide2 = new Slide();
		slide2.setStart(3);
		slide2.setEnd(4);
		slides.add(slide2);

		int num = 6;
		tuple.getNumOfNghbInEachSlideMapBySlide().put(slide1, 4);
		tuple.getNumOfNghbInEachSlideMapBySlide().put(slide2, 2);
		System.out.println("before: " + tuple.getNumOfNghbInEachSlideMapBySlide().get(slide2) + tuple.getNumOfNghbInEachSlideMapBySlide().get(slide1));
		Slide slide3 = new Slide();
		slide3.setStart(5);
		slide3.setEnd(6);
		slides.add(slide3);
		tuple.getNumOfNghbInEachSlideMapBySlide().put(slide3, 5);
		tuple.updateTriggeredSlide(slides, num);
		System.out.println("after: " + tuple.getNumOfNghbInEachSlideMapBySlide().get(slide3) + tuple.getNumOfNghbInEachSlideMapBySlide().get(slide2) + tuple.getNumOfNghbInEachSlideMapBySlide().get(slide1));
	}
	*/
}
