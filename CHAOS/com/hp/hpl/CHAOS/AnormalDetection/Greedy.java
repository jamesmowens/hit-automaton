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
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.Discarder;

public class Greedy extends SingleInputStreamOperator  {

	public Greedy(int operatorID,
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
				end = start + (double) window;
				for (int j = 0; j < slideNumInWindow; j++) {
					Slide slide = new Slide();
					double slideStart = 0;
					double slideEnd = 0;
					if (j == 0) {
						slideStart = start;
						slideEnd = slideStart + (double) slideLen;
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
//System.out.println("expired tuple id" + outlierTuple.getIdentifier() + start);
						outlier.remove(0);
					} else {
						break;
					}
				}
				
				
				//process each tuple (departure)
				processExpiredTuple(expiredSlide, slides);
				
				//process each tuple (arrival)
				processArrivedTuple(slides, slides.get(slides.size() - 1));

//testTupleInSlide(slides, slides.get(slideNumInWindow - 1));

				printOutliers(outlier);
			//	printInfor(slides);
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
		//process its own slide
		for (int i = 0; i < tuples.size(); i ++) {
			Tuple newTuple = tuples.get(i);
			for (int j = i + 1; j < tuples.size(); j++) {
				Tuple comparedTuple = tuples.get(j);
				double dis = newTuple.getDis(comparedTuple);
//System.out.println("new_identifier: " + newTuple.getIdentifier() + " ; " + "old_tuple: " + comparedTuple.getIdentifier() + " ; " + "dis :" + dis);
String output = "new_identifier: " + newTuple.getIdentifier() + " ; " + "old_tuple: " + comparedTuple.getIdentifier() + " ; " + "dis :" + dis + "\n";
try {
	FileWriter fw = new FileWriter("C:\\Users\\zhwang\\Desktop\\greedyprocedure.txt", true);
	fw.write(output);
	fw.close();
} catch (IOException e) {
	e.printStackTrace();
}
				if (dis <= (double) range) {
					newTuple.setSuccNghbNum(newTuple.getSuccNghbNum() + 1);
					comparedTuple.setSuccNghbNum(comparedTuple.getSuccNghbNum() + 1);
				}

			}
			newTuple.updateStatus(num);
		}
		
		//get each new tuple (loop1)
		for (int k = 0; k < tuples.size(); k ++) {
			Tuple newTuple = tuples.get(k);
			double comparedIdentifier = 0;
			//if (!newTuple.isOutlier()) {
//System.out.println("Identifier: " + newTuple.getIdentifier() + " " + "prevNum : " + newTuple.getPrevNghbNum() + "; " + "succNum: " + newTuple.getSuccNghbNum() + " ;" + newTuple.isOutlier() );
String output = "Identifier: " + newTuple.getIdentifier() + " " + "prevNum : " + newTuple.getPrevNghbNum() + "; " + "succNum: " + newTuple.getSuccNghbNum() + " ;" + newTuple.isOutlier()+ "\n" ;
try {
	FileWriter fw = new FileWriter("C:\\Users\\zhwang\\Desktop\\greedyprocedure.txt", true);
	fw.write(output);
	fw.close();
} catch (IOException e) {
	e.printStackTrace();
}
			//	continue;
			//}
			//get each old slide (loop2)
			for (int i = slides.size() - 1; i > 0; i--) {
				if (newTuple.isSafe()) {
					comparedIdentifier = slides.get(slides.size() - 1).start;
					break;
				}
				Slide oldSlide = slides.get(i - 1);
				int neighbour = 0;
				ArrayList<Tuple> oldTuples = oldSlide.getTuples();
				//get each old tuple in slide (loop3)
				for (int j = oldTuples.size(); j > 0; j--) {
					Tuple comparedTuple = oldTuples.get(j - 1);
					comparedIdentifier = comparedTuple.getIdentifier();
					double dis = newTuple.getDis(comparedTuple);
//System.out.println("new_identifier: " + newTuple.getIdentifier() + " ; " + "old_tuple: " + comparedTuple.getIdentifier() + " ; " + "dis :" + dis);
String output2 ="new_identifier: " + newTuple.getIdentifier() + " ; " + "old_tuple: " + comparedTuple.getIdentifier() + " ; " + "dis :" + dis+ "\n";
try {
	FileWriter fw = new FileWriter("C:\\Users\\zhwang\\Desktop\\greedyprocedure.txt", true);
	fw.write(output2);
	fw.close();
} catch (IOException e) {
	e.printStackTrace();
}

					if (dis <= (double) range) {
						neighbour++;
						newTuple.setPrevNghbNum(newTuple.getPrevNghbNum() + 1);
						newTuple.updateStatus(num);
						comparedTuple.setSuccNghbNum(comparedTuple.getSuccNghbNum() + 1);
						comparedTuple.updateStatus(num);
						comparedTuple.updateTriggeredSlide(slides, num);
						updateOutliers(comparedTuple);
						
						if (!newTuple.isOutlier()) {
							oldSlide.triggeredOutliers.add(newTuple);
						}
					}
//System.out.println("Identifier: " + newTuple.getIdentifier() + " " + "prevNum : " + newTuple.getPrevNghbNum() + "; " + "succNum: " + newTuple.getSuccNghbNum() + " ;" + newTuple.isOutlier() );
String output1 = "Identifier: " + newTuple.getIdentifier() + " " + "prevNum : " + newTuple.getPrevNghbNum() + "; " + "succNum: " + newTuple.getSuccNghbNum() + " ;" + newTuple.isOutlier()+ "\n" ;
try {
	FileWriter fw = new FileWriter("C:\\Users\\zhwang\\Desktop\\greedyprocedure.txt", true);
	fw.write(output1);
	fw.close();
} catch (IOException e) {
	e.printStackTrace();
}	
					
					if (!newTuple.isOutlier()) {
						break;
					}
					
				} //get old tuple in old slide one by one (loop3)
				
				//set number of neighbour in each slide mapping
				newTuple.numOfNghbInEachSlideMapBySlide.put(oldSlide, neighbour);
				if (!newTuple.isOutlier()) {
					break;
				}				
			} //get each old slide (loop2)
			
			if (newTuple.isOutlier()) {
				outlier.add(newTuple);
			}
			
			//compare all unsafe and outlier with new tuple
			if (comparedIdentifier == slides.get(0).getTuples().get(0).getIdentifier()) {
				continue;
			} else {
				// get old slide (loop2)
				for (int j = 0; j < slides.size() - 1; j++) {
					boolean done = false;
					Slide slide = slides.get(j);
					ArrayList<Tuple> oldTuples = slide.getTuples();
					for (int l = 0; l < oldTuples.size(); l++) {
						//difference with icde
						if (oldTuples.get(l).isSafe()) {
String output4 ="safe tuple identifier:  " + oldTuples.get(l).getIdentifier() + "\n";
try {
	FileWriter fw = new FileWriter("C:\\Users\\zhwang\\Desktop\\greedyprocedure.txt", true);
	fw.write(output4);
	fw.close();
} catch (IOException e) {
	e.printStackTrace();
}
							continue;
						}
						//keep getting all unsafe and outlier tuple until it comes to the compared one when current tuple first arrives
						if (comparedIdentifier <= oldTuples.get(l).getIdentifier()) {

String output4 ="arrive comparedtuple " + comparedIdentifier + "\n";
try {
	FileWriter fw = new FileWriter("C:\\Users\\zhwang\\Desktop\\greedyprocedure.txt", true);
	fw.write(output4);
	fw.close();
} catch (IOException e) {
	e.printStackTrace();
}
		
							done = true;
							break;
						} else {
							double dis = oldTuples.get(l).getDis(newTuple);
//System.out.println("new_identifier: " + newTuple.getIdentifier() + " ; " + "old_tuple: " + oldTuples.get(l).getIdentifier() + " ; " + "dis :" + dis);
String output3 ="new_identifier: " + newTuple.getIdentifier() + " ; " + "old_tuple: " + oldTuples.get(l).getIdentifier() + " ; " + "dis :" + dis+ "\n";
try {
	FileWriter fw = new FileWriter("C:\\Users\\zhwang\\Desktop\\greedyprocedure.txt", true);
	fw.write(output3);
	fw.close();
} catch (IOException e) {
	e.printStackTrace();
}
							if (dis <= (double) range) {
								oldTuples.get(l).setSuccNghbNum(oldTuples.get(l).getSuccNghbNum() + 1);
								oldTuples.get(l).updateTriggeredSlide(slides, num);
								oldTuples.get(l).updateStatus(num);
								updateOutliers(oldTuples.get(l));
								//this condition should never happen
								if (newTuple.isOutlier()) {
									newTuple.setPrevNghbNum(newTuple.getPrevNghbNum() + 1);
								}
							}
//System.out.println("Identifier: " + newTuple.getIdentifier() + " " + "prevNum : " + newTuple.getPrevNghbNum() + "; " + "succNum: " + newTuple.getSuccNghbNum() + " ;" + newTuple.isOutlier() );		
String output1 = "Identifier: " + newTuple.getIdentifier() + " " + "prevNum : " + newTuple.getPrevNghbNum() + "; " + "succNum: " + newTuple.getSuccNghbNum() + " ;" + newTuple.isOutlier()+ "\n" ;
try {
	FileWriter fw = new FileWriter("C:\\Users\\zhwang\\Desktop\\greedyprocedure.txt", true);
	fw.write(output1);
	fw.close();
} catch (IOException e) {
	e.printStackTrace();
}	
						}
					}
					if (done) {
						break;
					}
				}
			} //compare all unsafe and outlier with new tuple
		} //compare each new tuple with its previous tuple in previous slide (loop1)
		
	}
	
	private void updateOutliers(Tuple tuple) {
		if (!tuple.isOutlier()) {
			for (int i = 0; i < outlier.size(); i++) {
				if (tuple.getIdentifier() == outlier.get(i).getIdentifier()) {
					outlier.remove(outlier.get(i));
					//break;
				}
			}
		}
		if (tuple.isOutlier()) {
			outlier.add(tuple);
			for (int i = 0; i < outlier.size() - 1; i++) {
				if (tuple.getIdentifier() == outlier.get(i).getIdentifier()) {
					outlier.remove(outlier.get(i));
					//break;
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
	FileWriter fw = new FileWriter("C:\\Users\\zhwang\\Desktop\\greedyprocedure.txt", true);
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
				updateOutliers(potentialOutlier);
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
				System.out.println("Identifier: " + tuple.getIdentifier() + " " + "prevNum : " + tuple.getPrevNghbNum() + "; " + "succNum: " + tuple.getSuccNghbNum() + " ;" + tuple.isOutlier() );
			}
				
		}
	}
	
	private void printOutliers(ArrayList<Tuple> outlierTuples) {
		checkOutliers(outlierTuples);
		System.out.println("outlier num: " + outlierTuples.size());
		for (Iterator<Tuple> iterator = outlierTuples.iterator(); iterator.hasNext();) {
			Tuple outlier = iterator.next();
			System.out.println("outlier identifier: " + outlier.getIdentifier());
			//System.out.println("its prevNghb is : " + outlier.getPrevNghbNum() + " its succNghb is : " + outlier.getSuccNghbNum());
		}
		//write to file
		String output = "outlier num: " + outlierTuples.size() + "\n";
		try {
			FileWriter fw = new FileWriter("C:\\Users\\zhwang\\Desktop\\greedy.txt", true);
			fw.write(output);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void checkOutliers(ArrayList<Tuple> outlierTuples) {
		for (int i = 0; i < outlierTuples.size(); i++) {
			Tuple tuple = outlierTuples.get(i);
			for (int j = i + 1; j < outlierTuples.size(); j++) {
				Tuple repeatTuple = outlierTuples.get(j);
				if (tuple.getIdentifier() == repeatTuple.getIdentifier()) {
					outlierTuples.remove(repeatTuple);
				}
			}
		}
	}
		
}
