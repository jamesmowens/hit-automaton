package com.hp.hpl.CHAOS.AnormalDetection;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;
import com.hp.hpl.CHAOS.StreamOperator.SingleInputStreamOperator;
import com.sun.org.omg.CORBA.IdentifierHelper;

public class SingleQAnormalDetectionLazyOperator extends SingleInputStreamOperator  {

	public SingleQAnormalDetectionLazyOperator(int operatorID,
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
	
	//statistics
	long totalTriggeredNum = 0;	
	
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
				nextSlide.end = end + slideLen ;
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
				nextSlide.end = end + slideLen ;
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
					//store the distance between two events
					double dis = currentTuple.getDis(oldTuple);
//System.out.println("currentTuple" + " " + currentTuple.getIdentifier() + " oldTuple" + " " + oldTuple.getIdentifier() + " dis :" + dis);
					//if tuple find a neighbour in this slide
					if (dis <= (double) range) {
						currentTuple.setPrevNghbNum(currentTuple.getPrevNghbNum() + 1);
						neighbour ++;
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
				}
				//set number of neighbour in each slide mapping
				numOfNghbInEachSlideMapBySlide.put(slide, neighbour);
				currentTuple.setNumOfNghbInEachSlideMapBySlide(numOfNghbInEachSlideMapBySlide);
				if (!currentTuple.isOutlier()) {
					break;
				}
				if (i == 1) {
					if (currentTuple.isOutlier()) {
//System.out.println("found a outlier,  is " + currentTuple.getIdentifier());
						outlier.add(currentTuple);
					}
				}
			}
//System.out.println(currentTuple.getIdentifier() + " prevNGHB " + currentTuple.getPrevNghbNum());
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
			Tuple comparedPoint = null;
 			//if this potential outlier is safe, just skip this potential outlier to next one
			if (potentialOutlier.isSafe()) {
				continue;
			}
			Map<Slide, Integer> numOfNghbInEachSlideMapBySlide = potentialOutlier.getNumOfNghbInEachSlideMapBySlide();
			if (numOfNghbInEachSlideMapBySlide.get(expiredSlide) != null) {
				potentialOutlier.setPrevNghbNum(potentialOutlier.getPrevNghbNum() - numOfNghbInEachSlideMapBySlide.get(expiredSlide));
			}
			
			//if this potential outlier is unsafe, just skip this potential outlier to next one
			if (potentialOutlier.getSuccNghbNum() + potentialOutlier.getPrevNghbNum() >= num) {
				potentialOutlier.setUnsafe(true);
				potentialOutlier.setOutlier(false);
				numOfNghbInEachSlideMapBySlide.remove(expiredSlide);
				continue;
			} else {
				potentialOutlier.setOutlier(true);
				potentialOutlier.setUnsafe(false);
			}
			numOfNghbInEachSlideMapBySlide.remove(expiredSlide);

			//start comparing new tuples
			int slideTag = 0;
			int tupleTag = 0;
			double comparedPointIdentifier = 0;
			//find the compare point
			if (potentialOutlier.getComparedPoint() != potentialOutlier) {
				comparedPointIdentifier = potentialOutlier.getComparedPoint().getIdentifier();
			} else {
				comparedPointIdentifier = potentialOutlier.getIdentifier();
			}
			
			//find which slide this potential outlier is in 
			for (int j = 0; j < slides.size(); j++) {
				Slide currentSlide = slides.get(j);
				if (comparedPointIdentifier >= currentSlide.getStart() && comparedPointIdentifier < currentSlide.getEnd()) {
					slideTag = j;
					for (int k = 0; k < currentSlide.getTuples().size(); k++) {
						if (comparedPointIdentifier == currentSlide.getTuples().get(k).getIdentifier()) {
							tupleTag = k;
							break;
						}
					}
					break;
				}					
			}
//System.out.println("slide Tag : " + slideTag);			
//System.out.println("tuple Tag : " + tupleTag);

			//start to compare uncompared tuple at the compared point
			for (int j = slideTag; j < slides.size(); j++) {
				ArrayList<Tuple> uncomparedTuples = slides.get(j).getTuples();

					for (int k = 0; k < uncomparedTuples.size(); k++) {
						//if it is the slide that this potential outlier is in
						if (j == slideTag && k <= tupleTag) {
							continue;
						}
						if (k == uncomparedTuples.size()) {
							break;
						}
						double dis = potentialOutlier.getDis(uncomparedTuples.get(k));
//System.out.println("distance : " + dis);
						if (dis <= (double) range) {
							potentialOutlier.setSuccNghbNum(potentialOutlier.getSuccNghbNum() + 1);
							if (potentialOutlier.getSuccNghbNum() == num) {
								potentialOutlier.setSafe(true);
								potentialOutlier.setOutlier(false);
								potentialOutlier.setUnsafe(false);
								potentialOutlier.setComparePoint(null);
								break;
							}
						}
						comparedPoint = uncomparedTuples.get(k);
					}
					if (potentialOutlier.isSafe()) {
						for (int n = 0; n < outlier.size(); n++) {
							if (outlier.get(n).getIdentifier() == potentialOutlier.getIdentifier()) {
								outlier.remove(outlier.get(n));
							}
						}
						break;
					}
			}
			
			if (!potentialOutlier.isSafe()) {
				if (potentialOutlier.getSuccNghbNum() + potentialOutlier.getPrevNghbNum() >= num) {
					potentialOutlier.setUnsafe(true);
					potentialOutlier.setOutlier(false);
					potentialOutlier.setComparePoint(comparedPoint);
					for (int n = 0; n < outlier.size(); n++) {
						if (outlier.get(n).getIdentifier() == potentialOutlier.getIdentifier()) {
							outlier.remove(outlier.get(n));
						}
					}
					
				}
				if (potentialOutlier.isOutlier()) {
					boolean findone = false;
					outlier.add(potentialOutlier);
					for (int j = 0; j < outlier.size() - 1; j++) {
						if (outlier.get(j).getIdentifier() == potentialOutlier.getIdentifier()) {
							findone = true;
						}
						if (findone) {
							outlier.remove(outlier.get(j));
						}
					
					}
				}
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
