package edu.usfca.xj.appkit.document;

import edu.usfca.vas.graphics.fa.GElementFAStateDoubleRectangle;
import edu.usfca.vas.graphics.fa.GElementFAStateInterface;
import edu.usfca.vas.graphics.fa.GElementFAStateRectangle;
import edu.usfca.vas.machine.fa.FAMachine;
import edu.usfca.vas.machine.fa.FATransition;
import edu.usfca.vas.machine.fa.State;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;
import edu.usfca.xj.appkit.gview.object.GElement;
import edu.usfca.xj.appkit.gview.object.GExport;
import edu.usfca.xj.appkit.gview.object.GLink;

public class CHAOSUtil {
	/**
	 * Gets a state's long name (nickname) from the graphical element 
	 * @param trans transition from which to find predicate
	 * @param machine current machine
	 * @return transition's predicate
	 */
	protected static String findStateLongName(State state, FAMachine machine) {
		String name = "";
		
		for (GElement ge : machine.getNaming().getGElements()) {
			// check that the GElement is a state
			if (ge instanceof GElementFAStateInterface) {
				if (((GElementFAStateInterface) ge).getState().getName().equals(state.getName())) {
					name = ge.getNickname();
				}
			}
		}
		if (name == null || name.length() == 0) {
			name = state.getName();
		}
		return name;
	}
	
	/**
	 * A transition is an "entering transition" if its beginning element is marked as a start state.
	 * @param currentTransition transition to check
	 * @return true if the transition is entering, false otherwise.
	 */
	protected static boolean isEnteringTransition(FATransition currentTransition, FAMachine machine) {
		boolean isEntering = false;
		for (String s : machine.getStartStates()) {
			if (s == null) {
				// do nothing
			}
			else {
				if (currentTransition.getS1() != null 
						&& s.equals(currentTransition.getS1())) {
					isEntering = true;
				}
			}
		}
		
		return isEntering;
	}
	
	/**
	 * Gets a transition's predicate (nickname) from the graphical element 
	 * @param trans transition from which to find predicate
	 * @param machine current machine
	 * @return transition's predicate
	 */
	protected String findLabel(FATransition trans, FAMachine machine) {
		String label = "";
		
		for (GLink link : machine.getNaming().getGLinks()) {
			if (trans.getSymbol().equals(link.getPattern())) {
				label = link.getNickname();
				if(label==null || label.equals("")) {
					label = link.getPattern();
				}
				break;
			}
		}
		
		int i =label.indexOf("<");
		while(i!=-1) {
			label = label.substring(0,i) + "&lt;" + label.substring(i+1);
			i = label.indexOf("<");
		}
		
		i =label.indexOf(">");
		while(i!=-1) {
			label = label.substring(0,i) + "&gt;" + label.substring(i+1);
			i = label.indexOf(">");
		}
		
		return label;
	}
	
	/**
	 * Look through each GExport contained by a certain GExport for a state name.
	 * @param stateName name to look for
	 * @param machine current machine
	 * @return GExport linked to the state's name if found
	 */
	protected static GExport findExport(String stateName, FAMachine machine) {
		GExport sGExport = null;
		
		for (GExport ge : machine.getExport().getExport()) {
			if (((GElementFAStateInterface)ge.getGE()).getState().getName().equals(stateName)) {
				sGExport = ge;
			}
			
			if (sGExport != null) {
				break;
			}
			
			if ((ge.getGE() instanceof GElementFAStateRectangle
					|| ge.getGE() instanceof GElementFAStateDoubleRectangle) && ge.getGE().isCollapsed == false) {
				sGExport = findExportFromContains(stateName, ge);
			}
			
			if (sGExport != null) {
				break;
			}
		}
		return sGExport;
	}

	/**
	 * Recursively look through each GExport contained by a certain GExport for a state name. For use in findExport().
	 * @param stateName name to look for
	 * @param export GExport to search through
	 * @return GExport linked to the state's name if found
	 */
	protected static GExport findExportFromContains(String stateName, GExport export) {
		GExport sGExport = null;
		
		for (GExport ge : export.getContains()) {
			if (ge.getGE() instanceof GElementFAStateInterface 
					&& ((GElementFAStateInterface)ge.getGE()).getState().getName().equals(stateName)) {
					sGExport = ge;
			}
			
			if (sGExport != null) {
				break;
			}
			
			if (ge.getGE() instanceof GElementFAStateRectangle 
					|| ge.getGE() instanceof GElementFAStateDoubleRectangle) {
				sGExport = findExportFromContains(stateName, ge);
			}
			
			if (sGExport != null) {
				break;
			}
		}
		return sGExport;
	}
	
	/**
	 * Generate a simple unique ID for outgoing transitions. 
	 * @param state1 beginning state
	 * @param state2 end state
	 * @return generated ID
	 */
	protected String createTransitionUID(FATransition trans) {
		String uid = "";
		uid = "from_" + trans.getS1() + "_to_" + trans.getS2() +"_label_" + trans.getSymbol();
		return uid;
	}


	public boolean isContained(GExport s1GExport, GExport ge) {
		boolean is = true;
		if (s1GExport.getContainedBy().contains(ge)) { // states are contained inside the same rectangle
			is = false;
			return is;
		}
		for (GExport s1Cont : s1GExport.getContainedBy()) {
			if (!isContained(s1Cont, ge)) {
				is = false;
			}
		}
		return is;
	}
	
	public static void copyFile(File sourceFile, File destFile) throws IOException {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;

	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}

	public static String getStringFromFile(String filePath) {
		BufferedReader br = null;
		String data = "";

		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader(filePath));

			while ((sCurrentLine = br.readLine()) != null) {
				data+=sCurrentLine + "\n";
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		return data;
	}
}
