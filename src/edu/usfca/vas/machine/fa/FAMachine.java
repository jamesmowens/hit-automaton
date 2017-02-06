/*

[The "BSD licence"]
Copyright (c) 2004 Jean Bovet
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote products
derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

package edu.usfca.vas.machine.fa;

import edu.usfca.vas.graphics.fa.GElementFAMachine;
import edu.usfca.vas.graphics.fa.GElementFANickName;
import edu.usfca.vas.graphics.fa.GElementFASidePanel;
import edu.usfca.vas.graphics.fa.GElementFAState;
import edu.usfca.vas.graphics.fa.GElementFAStateDoubleCircle;
import edu.usfca.vas.graphics.fa.GElementFAStateDoubleRectangle;
import edu.usfca.vas.graphics.fa.GElementFAStateRectangle;
import edu.usfca.xj.appkit.gview.object.*;
import edu.usfca.xj.foundation.XJXMLSerializable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Handles drawing elements (states & transitions), collapsing, expanding, 
 * setting to accepting, coloring, etc.....
 */

public class FAMachine implements XJXMLSerializable {

    public static final int MACHINE_TYPE_DFA = 0;
    public static final int MACHINE_TYPE_NFA = 1;

    protected FAStates states = new FAStates();
    protected FAAlphabet alphabet = new FAAlphabet();
    protected FATransitions transitions = new FATransitions();
   	protected FAExport export = new FAExport();
   	protected ArrayList<GElement> elements = new ArrayList<GElement>();
   	//here we hold the namingPanel and the sidePanel so they can be edited/modified
   	protected GElementFANickName namingPanel=null;
   	protected GElementFASidePanel sidePanel=null;
    
    protected Set<?> stateSet;

    protected int type = MACHINE_TYPE_DFA;

    protected transient String debugString;
    protected transient String debugLastSymbol;
    
    protected GElementFAMachine elementMachine;

    public GElementFAMachine getElementMachine() {
		return elementMachine;
	}

	public void setElementMachine(GElementFAMachine elementMachine) {
		this.elementMachine = elementMachine;
	}

	public FAMachine() {
        init();
    }
    
    public FAMachine(Set<String> statesSet, Set<FATransition> transitionsSet, String startState, List<String> finalStates) {
        init();
        addState(statesSet, startState, finalStates);
        addTransitions(transitionsSet);
    }
    
    public ArrayList<GElement> getElements(){
    	return elements;
    }
    
    public void printMachineElements(){
    	System.out.println("printing machine elements");
    	for (int i = 0; i < elements.size(); i++){
    		System.out.println(elements.get(i));
    	}
    	System.out.println("done printing");
    }
    
    public void updateExport(List<GElement> list){
    	
    	System.out.println("elements: ");
    	System.out.println(elements);
    	
    	export = new FAExport();
    	for (int i = 0; i < list.size(); i++){
    		GElement ge = list.get(i);
    		if (ge instanceof GElementFAState){
    			GExport gExp = new GExport(list.get(i), "Atomic");
    			export.addGExport(gExp);
    		}
    		if (ge instanceof GElementFAStateDoubleCircle){
    			GExport gExp = new GExport(list.get(i), "EndAtomic");
    			export.addGExport(gExp);
    		}
    		if (ge instanceof GElementFAStateRectangle){
    			GExport gExp = new GExport(list.get(i), "NonAtomic");
    			export.addGExport(gExp);
    		}
    		if (ge instanceof GElementFAStateDoubleRectangle){
    			GExport gExp = new GExport(list.get(i), "EndNonAtomic");
    			export.addGExport(gExp);
    		}
    	}
    }
    
    public void addToElements(GElement ge){
    	elements.add(ge);
    }
    
    public GElementFANickName getNaming(){
    	return this.namingPanel;
    }
    
    public void setNaming(GElementFANickName namingPanel){
    	this.namingPanel = namingPanel;
    }
    
    public GElementFASidePanel getSide(){
    	return this.sidePanel;
    }
    
    public void setSide(GElementFASidePanel sidePanel){
    	this.sidePanel = sidePanel;
    }

    public void init() {
        alphabet.setMachine(this);

        alphabet.setSymbolsString("01");
        stateSet = new HashSet<String>();
    }

    public void printStates(){
    	System.out.println("printing states");
    	for (int i = 0; i < states.getStates().size(); i++){
    		System.out.println(states.getStates().get(i));
    	}
    	System.out.println("done printing");
    }
    
    public void setStates(FAStates states) {
        this.states = states;
    }

    public FAStates getStates() {
        return states;
    }

    public void setAlphabet(FAAlphabet alphabet) {
        this.alphabet = alphabet;
        alphabet.setMachine(this);
    }

    public FAAlphabet getAlphabet() {
        return alphabet;
    }

    public void setTransitions(FATransitions transitions) {
        this.transitions = transitions;
    }

    public FATransitions getTransitions() {
        return transitions;
    }

    public void addState(State s) {
        states.addState(s);
    }

    public void addState(Set<?> set, String startState, List<String> finalStates) {
        Iterator<?> iterator = set.iterator();
        while(iterator.hasNext()) {
            Set<?> stateSet = (HashSet<?>)iterator.next();
            State state = new FAState(stateSet.toString());

            for(int f=0; f<finalStates.size(); f++) {                
                if(stateSet.contains(finalStates.get(f))) {
                    state.accepted = true;
                    break;
                }
            }

            if(state.name.equals(startState))
                state.start = true;
            addState(state);
        }
    }

    public void removeState(State s) {
        states.removeState(s);
        transitions.removeState(s.name);
    }

    public void renameState(State s, String oldName, String newName) {
    	if (!(oldName.equals(newName))){
    		s.name = newName;
    		transitions.renameState(oldName, newName);
    	}
    }

    public boolean containsStateName(String name) {
        return states.contains(name);
    }

    public List<State> getStateList() {
        return states.getStates();
    }

    public List<String> getStateNames() {
        return states.getStateNames();
    }

    public void setType(int type) {
        this.type = type;
    }

    public GElementFANickName getNamingPanel() {
		return namingPanel;
	}

	public void setNamingPanel(GElementFANickName namingPanel) {
		this.namingPanel = namingPanel;
	}

	public void setExport(FAExport export) {
		this.export = export;
	}

	public void setElements(ArrayList<GElement> elements) {
		this.elements = elements;
	}

	public int getType() {
        return type;
    }
    
    public void setSymbolsString(String s) {
        alphabet.setSymbolsString(s);
    }

    public String getSymbolsString() {
        return alphabet.getSymbolsString();
    }

    public void addSymbol(String s) {
        alphabet.addSymbol(s);
    }

    public Set getSymbols() {
        return alphabet.getSymbols();
    }

    public void addTransitionPattern(String s1, String pattern, String s2) {
        transitions.addTransitionPattern(s1, pattern, s2);
    }

    public boolean containsTransition(String s1, String symbol, String s2) {
        return transitions.containsTransition(s1, symbol, s2);
    }

    public void addTransitions(Set<FATransition> set) {
        Iterator<FATransition> iterator = set.iterator();
        while(iterator.hasNext()) {
            transitions.addTransition((FATransition)iterator.next());
        }
    }

    public void removeTransitionPattern(String s1, String pattern, String s2) {
        /*for(int i=0; i<pattern.length(); i++)
            transitions.removeTransition(s1, pattern.substring(i, i+1), s2);*/
    	transitions.removeTransition(s1, pattern, s2);
    }

    //clears everything from the drawing board (all links and transitions)
    public void clear() {
        states.clear();
        transitions.clear();
        namingPanel.clear();
        
    }

    public String check() {
        if(type == MACHINE_TYPE_DFA) {
            String error = states.check();
            if(error != null)
                return error;

            error = transitions.check(alphabet.getSymbols().size(), states);
            if(error != null)
                return error;
        }
        return null;
    }

    public boolean accept(String s) {
        reset();
        stateSet = getStartStates();
        
        for(int i=0; i<s.length(); i++) {
            put(s.charAt(i));
        }
        return isAcceptedState(stateSet);
    }

    public boolean isAccepting() {
        return isAcceptedState(stateSet);
    }

    public void setStateSet(Set<?> stateSet) {
        this.stateSet = stateSet;
    }

    public Set<?> getStateSet() {
        return stateSet;
    }

    public Set<FATransition> getLastTransitionSet() {
        return getTransitions().getLastTransitionSet();
    }

    public Set<String> getStartStates() {
        return transitions.getEpsilonClosureStateSet(states.getStartState());
    }

    public Set<?> getNextStateSet(Set<?> stateSet, String symbol) {
        return getStateSet(stateSet, symbol);
    }

    public boolean isAcceptedState(String state) {
        return states.isAccepted(state);
    }

    public boolean isAcceptedState(Set stateSet) {
        return states.isAccepted(stateSet);
    }

    // *** Conversion

    public FAMachine convertNFA2DFA() {
        Set dfaStatesSet = new HashSet();
        Set transitionsSet = new HashSet();

        String startState = states.getStartState();

        Set startSet = new HashSet();
        startSet.add(startState);
        dfaStatesSet.add(startSet);
        recursiveBuildDFA(startSet, dfaStatesSet, transitionsSet);

        return new FAMachine(dfaStatesSet, transitionsSet, startSet.toString(), states.getFinalStates());
    }

    public void recursiveBuildDFA(Set statesSet, Set dfaStatesSet, Set transitionsSet) {
        Iterator iterator = alphabet.getSymbols().iterator();
        while(iterator.hasNext()) {
            String symbol = (String)iterator.next();
            Set newSet = getStateSet(statesSet, symbol);

            if(newSet.size()>0)
                transitionsSet.add(new FATransition(statesSet.toString(), symbol, newSet.toString()));

            if(!dfaStatesSet.contains(newSet) && newSet.size()>0) {
                dfaStatesSet.add(newSet);
                recursiveBuildDFA(newSet, dfaStatesSet, transitionsSet);
            }
        }
    }

    public Set getStateSet(Set statesSet, String symbol) {
        Set newStateSet = new HashSet();
        Iterator iterator = statesSet.iterator();
        while(iterator.hasNext()) {
            String state = (String)iterator.next();
            Set set = transitions.getClosureStateSet(state, symbol);
            if(set.size()>0)
                newStateSet.addAll(set);
        }
        return newStateSet;
    }

    // *** Debug methods

    public void debugReset(String s) {
        reset();
        debugString = s;
    }

    public boolean debugStepForward() {
        if(debugString.length() == 0)
            return false;

        if(stateSet.isEmpty())
            stateSet = getStartStates();

        transitions.getLastTransitionSet().clear();

        put(debugString.charAt(0));

        debugLastSymbol = debugString.substring(0, 1);
        debugString = debugString.substring(1);

        if(stateSet.isEmpty())
            return false;
        else
            return debugString.length() > 0;
    }

    public String debugLastSymbol() {
        return debugLastSymbol;
    }

    public String debugString() {
        return debugString;
    }
    
    public String toString() {
        String s = "Description of the machine:\n";
        s += states;
        s += transitions;
        return s;
    }

    // *** Processing methods

    public void reset() {
        stateSet.clear();
        transitions.getLastTransitionSet().clear();
        debugLastSymbol = "";
    }

    public void put(char c) {
        stateSet = getNextStateSet(stateSet, String.valueOf(c));
    }

    public FAExport getExport(){
    	return this.export;
    }   
}
