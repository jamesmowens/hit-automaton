package com.hp.hpl.CHAOS.AnormalDetection;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;
import com.hp.hpl.CHAOS.StreamOperator.SingleInputStreamOperator;

public class SingleQAnormalDetectionGreedyOperator extends SingleInputStreamOperator {
	
	public SingleQAnormalDetectionGreedyOperator(int operatorID,
			StreamQueue[] input, StreamQueue[] output) {
		super(operatorID, input, output);
	}

	//String value = "id=1 range=6 num=10 window=10 slide=5";
	int id = 0;
	double range = 0.00;
	int num = 0;
	int window = 0;
	int slideLen = 0;
	ArrayList<Integer> queryList = null;
	int slideNumInWindow = 0;
	
	private int totalTriggeredNum;
	
	//store the start and end time stamp of window in each slide
	double start = 0;
	double end = start + (double) window;

	//store the tuple in the current window
	//ArrayList<Tuple> aliveTuples = new ArrayList<Tuple>();

	//separate window into different slides
	ArrayList<Slide> slides = new ArrayList<Slide>();
	//store output ourlier
	ArrayList<Tuple> outlier = new ArrayList<Tuple>();
	//next slide
	Slide nextGlobalSlide = new Slide();
	
	
	// It parse the query plan, set up the stack types as well
	@Override
	public void classVariableSetup(String key, String value)
			throws InstantiationException, IllegalAccessException,
			InvocationTargetException, SecurityException,
			ClassNotFoundException {
	
		if (key.equalsIgnoreCase("query")) {	
			XMLVarParser parser = new XMLVarParser(value);
			
			id = parser.getId();
			range = parser.getRange();
			num = parser.getNum();
			window = parser.getWindow();
			slideLen = parser.getSlide();
			queryList = parser.getQueryIdList();
			if (window % slideLen == 0) {
				slideNumInWindow = window / slideLen;
			} else {
				try {
					throw new WindowSlideNotOverlapException();
				} catch (WindowSlideNotOverlapException e) {
					e.printStackTrace();
					System.out.println("please input reasonable window and slide!");
				}
			}
			
		}
		
//System.out.println("r = " + " " + range + " " + "k = " + " " + num + " " + "windowSize = " + " " + window + " " + "slideNumInWin = " + " " + slideNumInWindow);
	}
	
	
	@Override
	public int run(int maxDequeueSize) {
		
		
		double firstAttr;
		double secondAttr;
		double identifier;
		
		boolean initialForWindow = true;
		
		//Print headers
		System.out.println("ExecTime\tTuplesPrcss\tResult");
		long tuplesProcessed = 0;

	
		// the schema for all input tuples is the same.
		StreamQueue inputQueue = getInputQueueArray()[0];
		SchemaElement[] schArray = inputQueue.getSchema();
		
		//get tuple from queue
		for (int i = maxDequeueSize; i > 0; i--) {
			long execution_Start = (new Date()).getTime();
//System.out.println("Execution Start:" + execution_Start);
			
			tuplesProcessed++;
			byte[] event = inputQueue.dequeue();
	
			if (event == null)
				break;
	
			for (SchemaElement sch : schArray)
				sch.setTuple(event);
			
			Tuple tuple = new Tuple();
			//initial each tuple
			tuple = setTupleForm(event, schArray);
			//get field from each tuple 
			firstAttr = tuple.getFirstAttribute();
			secondAttr = tuple.getSecondAttribute();
			identifier = tuple.getIdentifier();
			
			//set start and end time of first window and separate window into fixed number of slides
			if (tuplesProcessed == 1) {
				start = identifier;
				end = start + (double) window - 1;
				for (int j = 0; j < slideNumInWindow; j++) {
					Slide slide = new Slide();
					double slideStart = 0;
					double slideEnd = 0;
					if (j == 0) {
						slideStart = start;
						slideEnd = slideStart + (double) slideLen - 1;
					} else {
						slideStart = slides.get(j - 1).getEnd();
						slideEnd = slideStart + (double) slideLen;
					}

					slide.start = slideStart;
					slide.end = slideEnd;
					slides.add(slide);
				}
System.out.println("this is the initialization of the window");	
/*				
System.out.println("start : " + start + " " + "end : " + end + "");
for (int j = 0; j < slides.size(); j++) {
	System.out.println("slide" + j + " " + slides.get(j).start + " " + slides.get(j).end);
}
*/
				Slide nextSlide = new Slide();
				nextSlide.start = end;
				nextSlide.end = end + slideLen;
				nextGlobalSlide = nextSlide;
			}
		
			
			//slide the window 
			while ((identifier >= (end + (long) slideLen))) {
				if (initialForWindow) {
					processArrivedTuple(slides, slides.get(slideNumInWindow - 1));
				}
				initialForWindow = false;
				end = end + slideLen;
				start = start + slideLen;
				Slide expiredSlide = slides.get(0);
				slides.remove(0);
				slides.add(nextGlobalSlide);
				Slide nextSlide = new Slide();
				nextSlide.start = end;
				nextSlide.end = end + slideLen;
				nextGlobalSlide = nextSlide;
					
/*
System.out.println("start : " + start + " " + "end : " + end + "");
for (int j = 0; j < slides.size(); j++) {
	System.out.println("slide" + j + " " + slides.get(j).start + " " + slides.get(j).end);
}
System.out.println("identifier" + " " + identifier);
*/
				
				//clean all old outlier record
				for (int m = 0; m < outlier.size(); m++) {
					Tuple outlierTuple = outlier.get(0);
					if (outlierTuple.getIdentifier() < start) {
						outlier.remove(0);
					} else {
						break;
					}
				}
				
				//process each tuple (arrival)
				processArrivedTuple(slides, slides.get(slideNumInWindow - 1));

//testTupleInSlide(slides, slides.get(slideNumInWindow - 1));
				
				//process each tuple (departure)
				processExpiredTuple(expiredSlide, slides);
				
				printInfor(slides);
				printOutliers(outlier);
			}
			
			//add tuple into current slides
			for (int k = 0; k < slides.size(); k++) {
				if (identifier >= slides.get(k).getStart() && identifier < slides.get(k).getEnd()) {
					slides.get(k).getTuples().add(tuple);
//System.out.println(k + " " + "add tuple " + slides.get(k).getTuples().size() + "identifier : " + identifier);
					break;
				}
			}
			
			if (identifier >= nextGlobalSlide.getStart() && identifier < nextGlobalSlide.getEnd()) {
				nextGlobalSlide.getTuples().add(tuple);
//System.out.println("globalslide " + "add tuple " + nextGlobalSlide.getTuples().size() + "identifier : " + identifier);
			}
			
		}
		
		return 0;
			
	}
	
	private void testTupleInSlide(ArrayList<Slide> slides, Slide slide) {
		/*
		for (int i = 0; i < slides.size(); i++) {
			Slide prevSlide = slides.get(i);
			ArrayList<Tuple> prefTuples = prevSlide.getTuples();
			for (int k = 0; k < prefTuples.size(); k++) {
				System.out.println("old tuple : " + prefTuples.get(k).getIdentifier());
			}
		}
		*/
		
		ArrayList<Tuple> tuples = slide.getTuples();
		for (int i = 0; i < tuples.size(); i++) {
			System.out.println("current tuple : " + tuples.get(i).getIdentifier());
			for (int j = slides.size() - 1; j > 0 ; j--) {
				Slide prevSlide = slides.get(j - 1);
				ArrayList<Tuple> prefTuples = prevSlide.getTuples();
				for (int k = 0; k < prefTuples.size(); k++) {
					System.out.println("old tuple : " + prefTuples.get(k).getIdentifier());
				}
			}
		}		
	}

	public Tuple setTupleForm(byte[] event, SchemaElement[] schArray) {
		Tuple tuple = new Tuple();
		//get two attribute of the incoming event
		double firstAttr = StreamAccessor.getDoubleCol(event, schArray, 0);
		double secondAttr= StreamAccessor.getDoubleCol(event, schArray, 1);
		//get arrival time stamp of the incoming event
		double eTimeStamp = StreamAccessor.getDoubleCol(event, schArray, 2);
		
		//set tuple
		tuple.setFirstAttribute(firstAttr);
		tuple.setSecondAttribute(secondAttr);
		tuple.setIdentifier(eTimeStamp);
		
		return tuple;
	}

	private void processArrivedTuple(ArrayList<Slide> slides, Slide newSlide) {
		ArrayList<Tuple> tuples = newSlide.getTuples();
		double comparedIdentifier = 0;
		//get new tuple in the new slide one by one
		for (int k = 0; k < tuples.size(); k++) {
			Tuple currentTuple = tuples.get(k);
			Map<Slide, Integer> numOfNghbInEachSlideMapBySlide = currentTuple.getNumOfNghbInEachSlideMapBySlide(); 
			//get each slide in current window
			for (int i = slides.size(); i > 0; i--) {
				Slide slide;
				if (k == 0) {
					if (i == 1) {
						if (currentTuple.isOutlier()) {
//System.out.println("found a outlier,  is " + currentTuple.getIdentifier());
							outlier.add(currentTuple);
						}
						continue;
					}
					numOfNghbInEachSlideMapBySlide.put(slide = slides.get(i - 1), 0);
					currentTuple.setNumOfNghbInEachSlideMapBySlide(numOfNghbInEachSlideMapBySlide);
					slide = slides.get(i - 2);
				} else {
					slide = slides.get(i - 1);
				}
				ArrayList<Tuple> comparedTuples = slide.getTuples();
				ArrayList<Tuple> triggeredOutliers = slide.getTriggeredOutliers();
				//store number of neighbour in this slide
				int neighbour = 0;
				
				//get each tuple in current slide
				for (int j = comparedTuples.size(); j > 0 ; j--) {
					if (slide == newSlide) {
						if (j > k) {
							continue;
						}
					}
					Tuple oldTuple = comparedTuples.get(j - 1);
					comparedIdentifier = oldTuple.getIdentifier();
					//store the distance between two events
					double dis = currentTuple.getDis(oldTuple);
//System.out.println("currentTuple" + " " + currentTuple.getIdentifier() + " oldTuple" + " " + oldTuple.getIdentifier() + " dis :" + dis);
					//if tuple find a neighbour in this slide
					if (dis <= (double) range) {
						currentTuple.setPrevNghbNum(currentTuple.getPrevNghbNum() + 1);
						neighbour ++;
						oldTuple.setSuccNghbNum(oldTuple.getSuccNghbNum() + 1);
						oldTuple.updateTriggeredSlide(slides, num);
						oldTuple.updateStatus(num);
						updateOutliers(oldTuple);
//System.out.println(i + " " + " neighbour" + neighbour);
						if (currentTuple.getPrevNghbNum() == num) {
							currentTuple.setUnsafe(true);
							currentTuple.setOutlier(false);
							//insert into triggered event
							triggeredOutliers.add(currentTuple);
							slide.setTriggeredOutliers(triggeredOutliers);
							break;
						}
					}
					oldTuple.updateStatus(num);
					updateOutliers(oldTuple);
				}
				//set number of neighbour in each slide mapping
				numOfNghbInEachSlideMapBySlide.put(slide, neighbour);
				currentTuple.setNumOfNghbInEachSlideMapBySlide(numOfNghbInEachSlideMapBySlide);
				if (!currentTuple.isOutlier()) {
					break;
				}
				currentTuple.updateStatus(num);
				if (i == 1) {
					if (currentTuple.isOutlier()) {
//System.out.println("found a outlier,  is " + currentTuple.getIdentifier());
						outlier.add(currentTuple);
					}
				}
			}
//System.out.println(currentTuple.getIdentifier() + " prevNGHB " + currentTuple.getPrevNghbNum());
			
			//compare all unsafe and outlier with new tuple
			if (comparedIdentifier == slides.get(0).getTuples().get(0).getIdentifier()) {
				continue;
			} else {
				for (int j = 0; j < slides.size(); j++) {
					boolean done = false;
					Slide slide = slides.get(j);
					ArrayList<Tuple> oldTuples = slide.getTuples();
					for (int l = 0; l < oldTuples.size(); l++) {
						//difference
						if (oldTuples.get(l).isSafe()) {
							continue;
						}
						//keep getting all unsafe and outlier tuple until it comes to the compared one when current tuple first arrives
						if (comparedIdentifier == oldTuples.get(l).getIdentifier()) {
							done = true;
							break;
						} else {
							double dis = oldTuples.get(l).getDis(currentTuple);
							if (dis <= (double) range) {
								oldTuples.get(l).setSuccNghbNum(oldTuples.get(l).getSuccNghbNum() + 1);
								oldTuples.get(l).updateTriggeredSlide(slides, num);
								oldTuples.get(l).updateStatus(num);
								updateOutliers(oldTuples.get(l));
								if (currentTuple.isOutlier()) {
									currentTuple.setPrevNghbNum(currentTuple.getPrevNghbNum() + 1);
								}
							}
							
						}
					}
					if (done) {
						break;
					}
				}
			}
			
			
		} //for each new tuple in current slide
	} // for method
	
	private void updateOutliers(Tuple tuple) {
		if (!tuple.isOutlier()) {
			for (int i = 0; i < outlier.size(); i++) {
				if (tuple.getIdentifier() == outlier.get(i).getIdentifier()) {
					outlier.remove(outlier.get(i));
					break;
				}
			}
		}
		if (tuple.isOutlier()) {
			outlier.add(tuple);
			for (int i = 0; i < outlier.size() - 1; i++) {
				if (tuple.getIdentifier() == outlier.get(i).getIdentifier()) {
					outlier.remove(outlier.get(i));
					break;
				}
			}
		}
	}
	
	private void processExpiredTuple(Slide expiredSlide, ArrayList<Slide> slides) {
		ArrayList<Tuple> triggeredOutliers = expiredSlide.getTriggeredOutliers();
		triggeredOutliers.addAll(outlier);
		//totalTriggeredNum
		totalTriggeredNum += triggeredOutliers.size();
System.out.println("triggeredOutlier num : " + totalTriggeredNum);	
String output = "triggeredOutlier num : " + totalTriggeredNum + "\n";
//write to a file
try {
	FileWriter fw = new FileWriter("C:\\Users\\zhwang\\Desktop\\output.txt", true);
	fw.write(output);
	fw.close();
} catch (IOException e) {
	e.printStackTrace();
}
//System.out.println("triggeredOutlier num : " + triggeredOutliers.size());
		for (int i = 0; i < triggeredOutliers.size(); i++) {
			Tuple potentialOutlier = triggeredOutliers.get(i);
			if (potentialOutlier.getIdentifier() < slides.get(0).getStart()) {
				outlier.remove(potentialOutlier);
				continue; 
			}
 			//if this potential outlier is safe, just skip this potential outlier to next one
			if (potentialOutlier.isSafe()) {
				continue;
			}
			
			if (potentialOutlier.numOfNghbInEachSlideMapBySlide.get(expiredSlide) != null) {
				potentialOutlier.setPrevNghbNum(potentialOutlier.getPrevNghbNum() - potentialOutlier.numOfNghbInEachSlideMapBySlide.get(expiredSlide));
			}
			
			potentialOutlier.updateStatus(num);
			updateOutliers(potentialOutlier);
			potentialOutlier.numOfNghbInEachSlideMapBySlide.remove(expiredSlide);			
		}

	}
	
	private void printInfor(ArrayList<Slide> slides) {
		for (int i = 0; i < slides.size(); i++) {
			Slide slide = slides.get(i);
			ArrayList<Tuple> tuples = slide.getTuples();
			for (int k = 0; k < tuples.size(); k++) {
				Tuple tuple = tuples.get(k);
				System.out.println("Identifier: " + tuple.getIdentifier() + " " + "prevNum : " + tuple.getPrevNghbNum() + "; " + "succNum: " + tuple.getSuccNghbNum() );
			}
				
		}
	}
	
	private void printOutliers(ArrayList<Tuple> outlierTuples) {
		System.out.println("outlier num: " + outlierTuples.size());
		for (Iterator<Tuple> iterator = outlierTuples.iterator(); iterator.hasNext();) {
			Tuple outlier = iterator.next();
			System.out.println("outlier identifier: " + outlier.getIdentifier());
			//System.out.println("its prevNghb is : " + outlier.getPrevNghbNum() + " its succNghb is : " + outlier.getSuccNghbNum());
		}
		//write to file
		String output = "outlier num: " + outlierTuples.size() + "\n";
		try {
			FileWriter fw = new FileWriter("C:\\Users\\zhwang\\Desktop\\output.txt", true);
			fw.write(output);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

		
}

