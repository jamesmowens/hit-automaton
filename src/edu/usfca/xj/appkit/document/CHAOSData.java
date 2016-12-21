package edu.usfca.xj.appkit.document;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import edu.usfca.vas.data.DataWrapperFA;
import edu.usfca.vas.machine.fa.FATransition;
import edu.usfca.vas.machine.fa.State;
import edu.usfca.vas.data.DataAbstract;
import edu.usfca.vas.graphics.fa.GElementFAMachine;
import edu.usfca.vas.graphics.fa.GElementFAStateDoubleRectangle;
import edu.usfca.vas.graphics.fa.GElementFAStateInterface;
import edu.usfca.vas.graphics.fa.GElementFAStateRectangle;
import edu.usfca.vas.machine.fa.FAMachine;
import edu.usfca.xj.appkit.gview.object.GElement;
import edu.usfca.xj.appkit.gview.object.GElementDoubleRectangle;
import edu.usfca.xj.appkit.gview.object.GExport;
import edu.usfca.xj.appkit.gview.object.GLink;
import edu.usfca.xj.appkit.gview.shape.SLinkArc;



/**
 * @author Devin
 *
 */
public class CHAOSData extends DataAbstract {
	
	public static int num_tabs = 0;
	
	/**
	 * Prints the contains information in XML format.
	 * @param ge
	 * @return string with the information
	 */
	public static String printGEContains(GExport ge, FAMachine machine) {
		CHAOSUtil cu = new CHAOSUtil();
		String data = "";
		
		GElementFAStateInterface newState = (GElementFAStateInterface)ge.getGE();
		for (int k = 0; k < num_tabs; k++){
			data = data.concat("\t");
		}
		data = data.concat("<state>\n");
		num_tabs++;
		for (int l = 0; l < num_tabs; l++){
			data = data.concat("\t");
		}
		data = data.concat("<name>" + CHAOSUtil.findStateLongName(newState.getState(), machine) + "</name>\n");
		for (int m = 0; m < num_tabs; m++){
			data = data.concat("\t");
		}
		data = data.concat("<start>"+isStart(newState.getState().getName(),machine)+"</start>\n");
		for (int p = 0; p < num_tabs; p++){
			data = data.concat("\t");
		}
		data = data.concat("<end>"+newState.getState().isAccepted()+"</end>\n");
		for (int p = 0; p < num_tabs; p++){
			data = data.concat("\t");
		}
		data = data.concat("<contains>\n");
		num_tabs++;
		for (int i = 0; i < ge.getContains().size(); i++){
			if (ge.getContains().get(i).getGE() instanceof GElementFAStateInterface) {
				data = data.concat(printGEContains(ge.getContains().get(i), machine));				
			}
		}
		num_tabs--;
		for (int n = 0; n < num_tabs; n++){
			data = data.concat("\t");
		}
		data = data.concat("</contains>\n");
		for (int o = 0; o < num_tabs; o++){
			data = data.concat("\t");
		}
		data = data.concat("<outgoingTransitions>\n");
		num_tabs++;
		data = data.concat(printOutgoingTransitions(newState.getState(), machine));
		num_tabs--;
		for (int o = 0; o < num_tabs; o++){
			data = data.concat("\t");
		}
		data = data.concat("</outgoingTransitions>\n");
		if((newState instanceof GElementFAStateRectangle) || (newState instanceof GElementFAStateDoubleRectangle)) {
			for (int o = 0; o < num_tabs; o++){
				data = data.concat("\t");
			}
			data = data.concat("<ingoingTransitions>\n");
			num_tabs++;
			data = data.concat(printIngoingTransitions(newState.getState(), machine));
			num_tabs--;
			for (int o = 0; o < num_tabs; o++){
				data = data.concat("\t");
			}
			data = data.concat("</ingoingTransitions>\n");
		}
		num_tabs--;
		for (int n = 0; n < num_tabs; n++){
			data = data.concat("\t");
		}
		data = data.concat("</state>\n");
    	return data;
    }

	public static void preExportExpand(){
		((DataWrapperFA)wrappers.get(0)).getGraphicMachine().preExportExpand();
	}
	
	/**
	 * Manually write CHAOS-formatted XML data and output to a file.
	 */
	public static void writeChaosData(String file) {
		CHAOSUtil cu = new CHAOSUtil();
		
		String data = "<machine>\n";
		FAMachine machine = ((DataWrapperFA)wrappers.get(0)).getGraphicMachine().getMachine();
		List<FATransition> transitions = machine.getTransitions().getTransitions();
		List<GExport> export = machine.getExport().getExport();
		GElementFAMachine gfaMachine = ((DataWrapperFA)wrappers.get(0)).getGraphicMachine();
		gfaMachine.preExportExpand();
		machine.updateExport(gfaMachine.getElements());
		data = data.concat("\t<states>\n");
		for(int i = 0; i < export.size(); i++){
    		num_tabs = 2;
    		if (((GExport)export.get(i)).getGE() instanceof GElementFAStateInterface) {
    			GElementFAStateInterface newState = (GElementFAStateInterface)export.get(i).getGE();
    			if(newState.getState().isStart()) continue;
    			data = data.concat(printGEContains(export.get(i), machine));
    		}
    	}
		
		data = data.concat("\t</states>\n");	
		data = data.concat("\t<transitions>\n");
		for (int i = 0; i < machine.getTransitions().getTransitions().size(); i++) {
			FATransition currentTransition = (FATransition)transitions.get(i);
			
			data = data.concat("\t\t<transition>\n");
			data = data.concat("\t\t\t<identifier>"+cu.createTransitionUID(currentTransition)+"</identifier>\n");
			data = data.concat("\t\t\t<source>");
			State s = ((GElementFAStateInterface) cu.findExport(currentTransition.getS1(), machine).getGE()).getState();
			if(!s.isStart()) {
				data = data.concat(cu.findStateLongName(s, machine));					
			}
			data = data.concat("</source>\n");
			s = ((GElementFAStateInterface) cu.findExport(currentTransition.getS2(), machine).getGE()).getState();
			data = data.concat("\t\t\t<target>"+cu.findStateLongName(s, machine)+"</target>\n");
			data = data.concat("\t\t\t<label>" + cu.findLabel(currentTransition, machine) + "</label>\n");
			data = data.concat("\t\t\t<entering>"+cu.isEnteringTransition(currentTransition, machine)+"</entering>\n");
			data = data.concat("\t\t\t<instantiating>"+isInstantiatingTransition(currentTransition, machine)+"</instantiating>\n");
			data = data.concat("\t\t\t<terminating>"+isTerminatingTransition(currentTransition, machine)+"</terminating>\n");

			GExport container = findContainerExport(currentTransition,machine);
			String containerString = "";
			if(container!=null) {
				containerString = CHAOSUtil.findStateLongName(((GElementFAStateInterface)container.getGE()).getState(), machine);
			}
			data = data.concat("\t\t\t<container>" + containerString + "</container>\n");

			data = data.concat("\t\t</transition>\n");
		}
		data = data.concat("\t</transitions>\n");
		data = data.concat("</machine>\n");
		
		try {
			PrintWriter out = new PrintWriter(file);
			out.print(data);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private static GExport findContainerExport(FATransition currentTransition,
			FAMachine machine) {
		
		GLink link = null;
		
		for(GElement e: machine.getElementMachine().getElements()) {
			if(e instanceof GLink) {
				if(((GLink) e).getPattern().equals(currentTransition.symbol) &&
						((GLink) e).getSource().getLabel().equals(currentTransition.s1) &&
						((GLink) e).getTarget().getLabel().equals(currentTransition.s2)) {
					link = (GLink) e;
					break;
				}
			}
		}
		
		if(link==null) return null;
		
		GExport e1 = CHAOSUtil.findExport(currentTransition.getS1(),machine);
		GExport e2 = CHAOSUtil.findExport(currentTransition.getS2(),machine);
		GExport container = null;
		for(State s: machine.getStates().getStates()) {
			GExport e = CHAOSUtil.findExport(s.name, machine);
			if(!e.equals(e1) && !e.equals(e2) && checkIfContain(e, e1) && checkIfContain(e,e2) && ((SLinkArc) link.getLink()).intersectWith(e.getGE())) {
				if(container==null || checkIfContain(container, e)) {
					container = e;
				}
			}
		}
		return container;
	}	


	/**
	 * @param state state to get outgoing transitions from
	 * @param machine current machine
	 * @return
	 */
	private static String printOutgoingTransitions(State state, FAMachine machine) {
		CHAOSUtil cu = new CHAOSUtil();
		GExport e = CHAOSUtil.findExport(state.getName(), machine);
		String data = "";
		for (FATransition t : (List<FATransition>)machine.getTransitions().getTransitions()) {
			GExport e1 = CHAOSUtil.findExport(t.getS1(), machine);
			GExport e2 = CHAOSUtil.findExport(t.getS2(), machine);
			GExport container = findContainerExport(t, machine);
			if((state.getName().equals(t.getS1())) || (checkIfContain(e,e1) && (!checkIfContain(e,e2) || !checkIfContain(e, container)))) {
				for (int o = 0; o < num_tabs; o++){
					data = data.concat("\t");
				}
				data = data.concat("<transition>"+cu.createTransitionUID(t)+"</transition>\n"); // transition names to be added
			}
		}
		return data;
	}
	
	private static String printIngoingTransitions(State state, FAMachine machine) {
		CHAOSUtil cu = new CHAOSUtil();
		GExport e = CHAOSUtil.findExport(state.getName(), machine);
		String data = "";
		for (FATransition t : (List<FATransition>)machine.getTransitions().getTransitions()) {
			GExport e1 = CHAOSUtil.findExport(t.getS1(), machine);
			GExport e2 = CHAOSUtil.findExport(t.getS2(), machine);
			GExport container = findContainerExport(t, machine);
			if(checkIfContain(e,e2) && (!checkIfContain(e,e1) || !checkIfContain(e, container))) {
				for (int o = 0; o < num_tabs; o++){
					data = data.concat("\t");
				}
				data = data.concat("<transition>"+cu.createTransitionUID(t)+"</transition>\n"); // transition names to be added
			}
		}
		return data;
	}
	
	private static boolean checkIfContain(GExport s1,GExport s2) {
		if(s1==null || s2==null) return false;
		if(!(s1.getGE() instanceof GElementFAStateInterface) || !(s2.getGE() instanceof GElementFAStateInterface)) return false;
		String name1 = ((GElementFAStateInterface) s1.getGE()).getState().getName();
		String name2 = ((GElementFAStateInterface) s2.getGE()).getState().getName();
		if(name1.equals(name2)) {
			return true;
		}
		for(GExport ge : s1.getContains()) {
			if(checkIfContain(ge,s2)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if a state is a start state
	 * @param name
	 * @param machine
	 * @return
	 */
	public static boolean isStart(String name, FAMachine machine) {
		GExport e = CHAOSUtil.findExport(name, machine);
		GExport parent = findParentGExport(name, machine);
		if(parent==null) {
			return true;
		}
		//System.out.println(name);
		List<FATransition> trans = machine.getTransitions().getTransitions();
		for(FATransition current: trans) { // for each transition
			GExport e1 = CHAOSUtil.findExport(current.getS1(),machine);
			GExport e2 = CHAOSUtil.findExport(current.getS2(),machine);
			GExport container = findContainerExport(current, machine);
			if(checkIfContain(e, e2) && (container==null || !checkIfContain(parent, container))) {
				return true;
			}
		}
		return false;
	}	
	
	/**
	 * @param trans Transition to check
	 * @param machine current machine
	 * @return true if the transition is terminating, false otherwise
	 */
	protected static boolean isTerminatingTransition(FATransition trans, FAMachine machine) { 
		GExport e1 = CHAOSUtil.findExport(trans.getS1(),machine);
		GExport e2 = CHAOSUtil.findExport(trans.getS2(),machine);
		GExport container = findContainerExport(trans, machine);
		GExport parentSource = findParentGExport(trans.getS1(), machine);
		if(container==null) return false;
		//System.out.println("AA " + trans.getS1() + " " + trans.getS2());
		//System.out.println(parentSource.getGE().getLabel() + " " + container.getGE().getLabel());
		if(checkIfContain(parentSource, container)) return false;
		return true;
	}
	
	/**
	 * @param trans transition to check
	 * @param machine current machine
	 * @return true if the transition is instantiating
	 */
	protected static boolean isInstantiatingTransition(FATransition trans, FAMachine machine) { 
		GExport e1 = CHAOSUtil.findExport(trans.getS1(),machine);
		GExport e2 = CHAOSUtil.findExport(trans.getS2(),machine);
		GExport container = findContainerExport(trans, machine);
		GExport parentTarget = findParentGExport(trans.getS2(), machine);
		if(container==null) return true;
		return !checkIfContain(parentTarget, container);
	}
	
	/**
	 * Return GExport of the parent of a state
	 * @param name The name of State which we want the parent
	 * @param machine The Machine
	 * @return GExport of the parent
	 */
	protected static GExport findParentGExport(String name,FAMachine machine) {
		GExport e = CHAOSUtil.findExport(name, machine); // this state
		GExport parent = null;
		for(State s: machine.getStates().getStates()) { // for each state
			GExport e1 = CHAOSUtil.findExport(s.getName(), machine);
			if(!s.getName().equals(name) && checkIfContain(e1,e)) { // check if an ancestor
				if(parent==null || checkIfContain(parent,e1)) { // find the lower ancestor
					parent = e1;
				}
			}
		}
		return parent;
	}
}