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

package edu.usfca.vas.graphics.fa;

import edu.usfca.vas.app.Localized;
import edu.usfca.vas.machine.Tool;
import edu.usfca.vas.machine.fa.FAMachine;
import edu.usfca.vas.machine.fa.FAState;
import edu.usfca.vas.machine.fa.FATransition;
import edu.usfca.vas.machine.fa.State;
import edu.usfca.xj.appkit.gview.GView;
import edu.usfca.xj.appkit.gview.base.Vector2D;
import edu.usfca.xj.appkit.gview.object.GElement;
import edu.usfca.xj.appkit.gview.object.GElementDoubleRectangle;
import edu.usfca.xj.appkit.gview.object.GExport;
import edu.usfca.xj.appkit.gview.object.GLink;
import edu.usfca.xj.appkit.gview.shape.SLink;
import edu.usfca.xj.appkit.gview.shape.SLinkArc;
import edu.usfca.xj.foundation.XJXMLSerializable;

import javax.swing.*;

import java.awt.*;
import java.util.*;
import java.util.List;

public class GElementFAMachine extends GElement implements XJXMLSerializable {

	public static final int STATE_STOPPED = 0;
    public static final int STATE_READY = 1;
    public static final int STATE_RUNNING = 2;
    public static final int STATE_PAUSED = 3;
    
    protected FAMachine machine;
    
    protected transient int state = STATE_STOPPED;
    
    public GElementFAStateRectangle rectState;
    public GElementFAStateDoubleRectangle doubleRectState;
    public Color oldColor;
    public ArrayList<Color> colors = new ArrayList<Color>();
    public ArrayList<GElement> highlighted = new ArrayList<GElement>();
    
    public GElementFAMachine() {
    	updateAll();
    }

    public GElementFAMachine(FAMachine faMachine) {
        setMachine(faMachine);
        updateAll();
    }

    public Object[] getSymbols() {
        return machine.getSymbols().toArray();
    }

    public void setMachine(FAMachine machine) {
        this.machine = machine;
        machine.setElementMachine(this);
        updateAll();
    }

    public FAMachine getMachine() {
        return machine;
    }

    /**
     * highlights a given shape in red
     * @param ge - gelement to be highlighted
     */
    public void highlightShape(GElement ge){
    	if (elements.contains(ge)){
    		oldColor = ge.getColor();
    		colors.add(oldColor);
    		highlighted.add(ge);
    		ge.setColor(Color.RED);
    		ge.getPosition().color = Color.RED;
    		if (ge instanceof GElementFAState){
    			((GElementFAState)ge).setColor2(Color.RED);
    			((GElementFAState)ge).setColor3(Color.RED);
    			((GElementFAState)ge).highlighted = true;
    		}
    		if (ge instanceof GElementFAStateDoubleCircle){
    			((GElementFAStateDoubleCircle)ge).setColor2(Color.RED);
    			((GElementFAStateDoubleCircle)ge).setColor3(Color.RED);
    			((GElementFAStateDoubleCircle)ge).highlighted = true;
    		}
    		if (ge instanceof GElementFAStateRectangle){
    			((GElementFAStateRectangle)ge).setColor2(Color.RED);
    			((GElementFAStateRectangle)ge).setColor3(Color.RED);
    			((GElementFAStateRectangle)ge).highlighted = true;
    		}
    		if (ge instanceof GElementFAStateDoubleRectangle){
    			((GElementFAStateDoubleRectangle)ge).setColor2(Color.RED);
    			((GElementFAStateDoubleRectangle)ge).setColor3(Color.RED);
    			((GElementFAStateDoubleRectangle)ge).highlighted = true;
    		}
    	}
    	// check if element is collapsed
    	else if (collapsed.contains(ge)){
    		for (int i = 0; i < elements.size(); i++){
    			if (elements.get(i) instanceof GElementFAStateRectangle){
    				if (((GElementFAStateRectangle)elements.get(i)).isCollapsed == true){
    					if (ge instanceof GElementFAState || ge instanceof GElementFAStateDoubleCircle){
    						if (ge.getPositionX() > elements.get(i).collapsedPositions.x && ge.getPositionX() < elements.get(i).collapsedPositions.x2
    								&& ge.getPositionY() > elements.get(i).collapsedPositions.y
    								&& ge.getPositionY() < elements.get(i).collapsedPositions.y2){
    							highlightShape(elements.get(i));
    						}
    					}
    					else if (ge instanceof GElementFAStateRectangle || ge instanceof GElementFAStateDoubleRectangle){
    						if (ge.getPositionX() > elements.get(i).collapsedPositions.x && ge.getPositionX2() < elements.get(i).collapsedPositions.x2
    								&& ge.getPositionY() > elements.get(i).collapsedPositions.y
    								&& ge.getPositionY2() < elements.get(i).collapsedPositions.y2){
    							highlightShape(elements.get(i));
    						}
    					}
    				}
    			}
    			else if (elements.get(i) instanceof GElementFAStateDoubleRectangle){
    				if (((GElementFAStateDoubleRectangle)elements.get(i)).isCollapsed == true){
    					if (ge instanceof GElementFAState || ge instanceof GElementFAStateDoubleCircle){
    						if (ge.getPositionX() > elements.get(i).collapsedPositions.x && ge.getPositionX() < elements.get(i).collapsedPositions.x2
    								&& ge.getPositionY() > elements.get(i).collapsedPositions.y
    								&& ge.getPositionY() < elements.get(i).collapsedPositions.y2){
    							highlightShape(elements.get(i));
    						}
    					}
    					else if (ge instanceof GElementFAStateRectangle || ge instanceof GElementFAStateDoubleRectangle){
    						if (ge.getPositionX() > elements.get(i).collapsedPositions.x && ge.getPositionX2() < elements.get(i).collapsedPositions.x2
    								&& ge.getPositionY() > elements.get(i).collapsedPositions.y
    								&& ge.getPositionY2() < elements.get(i).collapsedPositions.y2){
    							highlightShape(elements.get(i));
    						}
    					}
    				}
    			}
    		}
    	}
    	
    }
    
    public void unhighlightShape(GElement ge){
    	if (elements.contains(ge)){
    		int i = 0;
    		int value = -1;
    		for (GElement test: highlighted){
    			if (test == ge){
    				value = i;
    				break;
    			}
    			i++;
    		}
    		if (value != -1){
    			oldColor = colors.get(value);
    			colors.remove(value);
    			highlighted.remove(value);
    		}
    		ge.setColor(oldColor);
    		ge.getPosition().color = oldColor;
    		if (ge instanceof GElementFAState){
    			((GElementFAState)ge).setColor2(oldColor);
    			((GElementFAState)ge).setColor3(oldColor);
    			((GElementFAState)ge).highlighted = false;
    		}
    		if (ge instanceof GElementFAStateDoubleCircle){
    			((GElementFAStateDoubleCircle)ge).setColor2(oldColor);
    			((GElementFAStateDoubleCircle)ge).setColor3(oldColor);
    			((GElementFAStateDoubleCircle)ge).highlighted = false;
    		}
    		if (ge instanceof GElementFAStateRectangle){
    			((GElementFAStateRectangle)ge).setColor2(oldColor);
    			((GElementFAStateRectangle)ge).setColor3(oldColor);
    			((GElementFAStateRectangle)ge).highlighted = false;
    		}
    		if (ge instanceof GElementFAStateDoubleRectangle){
    			((GElementFAStateDoubleRectangle)ge).setColor2(oldColor);
    			((GElementFAStateDoubleRectangle)ge).setColor3(oldColor);
    			((GElementFAStateDoubleRectangle)ge).highlighted = false;
    		}
    	}
    	else if (collapsed.contains(ge)){
    		for (int i = 0; i < elements.size(); i++){
    			if (elements.get(i) instanceof GElementFAStateRectangle){
    				if (((GElementFAStateRectangle)elements.get(i)).isCollapsed == true){
    					if (ge instanceof GElementFAState || ge instanceof GElementFAStateDoubleCircle){
    						if (ge.getPositionX() > elements.get(i).collapsedPositions.x && ge.getPositionX() < elements.get(i).collapsedPositions.x2
    								&& ge.getPositionY() > elements.get(i).collapsedPositions.y
    								&& ge.getPositionY() < elements.get(i).collapsedPositions.y2){
    							unhighlightShape(elements.get(i));
    						}
    					}
    					else if (ge instanceof GElementFAStateRectangle || ge instanceof GElementFAStateDoubleRectangle){
    						if (ge.getPositionX() > elements.get(i).collapsedPositions.x && ge.getPositionX2() < elements.get(i).collapsedPositions.x2
    								&& ge.getPositionY() > elements.get(i).collapsedPositions.y
    								&& ge.getPositionY2() < elements.get(i).collapsedPositions.y2){
    							unhighlightShape(elements.get(i));
    						}
    					}
    				}
    			}
    			else if (elements.get(i) instanceof GElementFAStateDoubleRectangle){
    				if (((GElementFAStateDoubleRectangle)elements.get(i)).isCollapsed == true){
    					if (ge instanceof GElementFAState || ge instanceof GElementFAStateDoubleCircle){
    						if (ge.getPositionX() > elements.get(i).collapsedPositions.x && ge.getPositionX() < elements.get(i).collapsedPositions.x2
    								&& ge.getPositionY() > elements.get(i).collapsedPositions.y
    								&& ge.getPositionY() < elements.get(i).collapsedPositions.y2){
    							unhighlightShape(elements.get(i));
    						}
    					}
    					else if (ge instanceof GElementFAStateRectangle || ge instanceof GElementFAStateDoubleRectangle){
    						if (ge.getPositionX() > elements.get(i).collapsedPositions.x && ge.getPositionX2() < elements.get(i).collapsedPositions.x2
    								&& ge.getPositionY() > elements.get(i).collapsedPositions.y
    								&& ge.getPositionY2() < elements.get(i).collapsedPositions.y2){
    							unhighlightShape(elements.get(i));
    						}
    					}
    				}
    			}
    		}
    	}
    }
    
    // returns true if ge1 contains ge2
    // ge1 is a GElementFAStateRectangle or GElementFAStateDoubleRectangle
    public boolean isContained(GElement ge1, GElement ge2){	
    	// is ge2 circular
    	if (ge2 instanceof GElementFAState || ge2 instanceof GElementFAStateDoubleCircle){
    		// is it inside ge1
    		if (ge1.getPosition().getX() < ge2.getPosition().getX() && 
    				ge1.getPosition().getX2() > ge2.getPosition().getX() &&
    				ge1.getPosition().getY() < ge2.getPosition().getY() &&
    				ge1.getPosition().getY2() > ge2.getPosition().getY()){
    			return true; // yes
    		}
    		else {
    			return false; // no
    		}
    	}
    	// is ge2 rectangular
    	else {
    		// is it inside ge1
    		if (ge1.getPosition().getX() < ge2.getPosition().getX() && 
    				ge1.getPosition().getX2() > ge2.getPosition().getX2() &&
    				ge1.getPosition().getY() < ge2.getPosition().getY() &&
    				ge1.getPosition().getY2() > ge2.getPosition().getY2()){
    			return true; // yes
    		}
    		else {
    			return false; // no
    		}
    	}
    }
    
    /**
     * expands a collapsed GElementFAStateRectangle or GElementFAStateDoubleRectangle
     * @param ge
     */
    public void expandState(GElement ge){
    	// reverts to original positions
    	ge.setPosition(ge.collapsedPositions.x, ge.collapsedPositions.y);
    	ge.setPosition2(ge.collapsedPositions.x2, ge.collapsedPositions.y2);
    	ge.isCollapsed = false;
    	
    	// loop thru hidden elements that are contained by ge
    	for (int i = 0; i < collapsed.size(); i++){
    		GElement e = collapsed.get(i);
    		if(appearWhenExpand.get(i)!=null && appearWhenExpand.get(i).equals(ge)) {
    			getElements().add(e);
    			collapsed.remove(i);
    			appearWhenExpand.remove(i);
    			i--;
    		}
    	}
    	// handle transitions
    	for (int j = 0; j < collapsed.size(); j++){
    		// if it is a GLink
    		if (collapsed.get(j) instanceof GLink){
    			GLink link = (GLink) collapsed.get(j);
    			int size;
    			boolean flag=false;
    			size = link.changeSourceWhenExpand.size();
    			if(size!=0 && link.changeSourceWhenExpand.get(size-1).equals(ge)) {
    				link.source = link.prevSource.get(size-1);
    				link.changeSourceWhenExpand.remove(size-1);
    				link.prevSource.remove(size-1);
    				flag=true;
    			}
    			size = link.changeTargetWhenExpand.size();
    			if(size!=0 && link.changeTargetWhenExpand.get(size-1).equals(ge)) {
    				link.target = link.prevTarget.get(size-1);
    				link.changeTargetWhenExpand.remove(size-1);
    				link.prevTarget.remove(size-1);
    				flag=true;
    			}
    			if(flag) {
    				collapsed.remove(j);
    				appearWhenExpand.remove(j);
    				j--;
    				
    				size = link.prevLink.size();
    				SLink newLink = link.prevLink.get(size-1);
    				link.prevLink.remove(size-1);
    				link.setLink(newLink);
    			}
    		}
    	}
    }
    
    /**
     * collapses a GElementFAStateRectangle or GElementFAStateDoubleRectangle
     * @param ge
     */
    public void collapseState(GElement ge){
    	ArrayList<GElement> holder =  new ArrayList<GElement>(); // holds elements made visible
    	// loop thru visible elements for elements contained by ge
    	for (int i = 0; i < getElements().size(); i++){
    		// if it is contained by ge
    		if ((getElements().get(i) instanceof GElementFAStateInterface) && isContained(ge, getElements().get(i)) == true){
    			collapsed.add(getElements().get(i)); // add to invisible element list
    			appearWhenExpand.add(ge);
    			holder.add(getElements().get(i)); // add to holder
    			getElements().remove(i); // remove from visible elements
    			i--;
    		}
    	}
    	//handle transitions
    	for (int j = 0; j < getElements().size(); j++){
    		// if it is a GLink
    		if (getElements().get(j) instanceof GLink){
    			GLink link = (GLink) getElements().get(j);
    			GElement container = link.findContainer();
    			if(container!=null && (ge.equals(container) || isContained(ge,container))) {
    				collapsed.add(getElements().get(j)); // make invisible
    				appearWhenExpand.add(ge);
    				getElements().remove(j);
    				j--;
    			}
    			else
    			{
    				if (holder.contains(((GLink)getElements().get(j)).getSource()) || holder.contains(((GLink)getElements().get(j)).getTarget())) {
    					collapsed.add(getElements().get(j)); // make invisible
    					appearWhenExpand.add(null);
    				}
    				boolean flag = false;
    				if(holder.contains(((GLink)getElements().get(j)).getSource())) {
    					link.prevSource.add(link.source);
    					link.changeSourceWhenExpand.add(ge);
    					link.source = ge;
    					flag = true;
    				}
    				if(holder.contains(((GLink)getElements().get(j)).getTarget())) {
    					link.prevTarget.add(link.target);
    					link.changeTargetWhenExpand.add(ge);
    					link.target = ge;
    					flag = true;
    				}
    				if(flag) {
    					link.prevLink.add(((SLinkArc) link.getLink()).createCopy());
    				}
    			}
    		}
    	}
    	
    	// change and save position of ge
    	ge.isCollapsed = true;
    	ge.collapsedPositions.x = ge.getPositionX();
    	ge.collapsedPositions.y = ge.getPositionY();
    	ge.collapsedPositions.x2 = ge.getPositionX2();
    	ge.collapsedPositions.y2 = ge.getPositionY2();
    	ge.getPosition().x2 = ge.getPositionX() + 50;
    	ge.getPosition().y2 = ge.getPositionY() + 50;
    }
    
    /**
     * expands all shapes before exporting. Should be called before saving or exporting.
     */
    public void preExportExpand(){
    	for(GElement i : elements){
    		for(GElement j: appearWhenExpand) {
    			if(i.equals(j)) {
    				expandState(i);
    				preExportExpand();
    				return;
    			}
    		}
    	}
    }
    
    /**
     * changes an atomic state to an end atomic state
     * @param s - name
     * @param x - x coordinate
     * @param y - y coordinate
     * @param c - color
     */
    public void changeStateToEndAtomic(String s, double x, double y, Color c){
    	// create new state and GElement
    	State state = new FAState(s);
        machine.addState(state);
        GElementFAStateDoubleCircle newFAState = new GElementFAStateDoubleCircle(state, x, y,this);
        // set color
        newFAState.setColor(c);
		newFAState.setColor2(c);
		newFAState.setColor3(c);
		newFAState.getPosition().color = c;
		// add to machine
		addElement(newFAState);
        //GExport gE = new GExport(newFAState, "GElementFAState");
        //machine.getExport().addGExport(gE);
    }
    
    /**
     * changes a end atomic state to an atomic state
     * @param s - name
     * @param x - x coordinate
     * @param y - y coordinate
     * @param c - color
     */
    public void changeStateToAtomic(String s, double x, double y, Color c){
    	// create new state and GElement
    	State state = new FAState(s);
        machine.addState(state);
        GElementFAState newFAState = new GElementFAState(state, x, y,this);
        // set color
        newFAState.setColor(c);
		newFAState.setColor2(c);
		newFAState.setColor3(c);
		newFAState.getPosition().color = c;
		// add to machine
		addElement(newFAState);
        //GExport gE = new GExport(newFAState, "GElementFAState");
        //machine.getExport().addGExport(gE);
    }
    
    /**
     * creates a circle
     * @param s - name
     * @param x - x coordinate
     * @param y - y coordinate
     */
    public void addStateAtXY(String s, double x, double y) {
        State state = new FAState(s); // create state
        updateAll();
        machine.addState(state); // add state to FAMachine
        GElementFAState newFAState = new GElementFAState(state, x, y, this); // create GElementFAState
        addElement(newFAState); // add GElement to GElement array
        //add the element to the nickname panel
        getMachine().getNaming().addElement(newFAState);
        getMachine().getNaming().repaint();
        //create GExport
        GExport gE = new GExport(newFAState, "Atomic");
        machine.getExport().addGExport(gE);
        machine.addToElements(newFAState);
    }

    /**
     * adds double circle
     * @param s - name
     * @param x - x coordinate
     * @param y - y coordinate
     */
    public void addEndAtomicStateAtXY(String s, double x, double y) {
        State state = new FAState(s); // create state
       // updateAll();
        machine.addState(state); // add state to FAMachine
        GElementFAStateDoubleCircle newFAState = new GElementFAStateDoubleCircle(state, x, y,this); // create GElement
        addElement(newFAState); // add GElement to GElement array
        // add element to nickname panel
        getMachine().getNaming().addElement(newFAState);
        getMachine().getNaming().repaint();
        // create GExport item
        GExport gE = new GExport(newFAState, "EndAtomic");
        machine.getExport().addGExport(gE);
        machine.addToElements(newFAState);
    }
    
    /**
     * adds rectangle
     * @param s - name
     * @param x - x coordinate
     * @param y - y coordinate
     * @param controller - controls if first coordinate or second coordinate
     */
    public void addNonAtomicStateAtXY(String s, double x, double y, int controller) {
    	// if it is first coordinate
    	if (controller == 0){
        	State state = new FAState(s); // make state
        	rectState = new GElementFAStateRectangle(state, x, y,this); // make GElement
        }
    	// if second coordinate
        else {
        	State state2 = new FAState(s); // remake state
            machine.addState(state2); // add state to FAMachine
            rectState.setState(state2); // set state
            rectState.setPositions(x, y); // set second coordinate
            // make sure positions are in the correct order
            if (rectState.getPosition().x > rectState.getPosition().x2 
            		&& rectState.getPosition().y > rectState.getPosition().y2){
            	rectState.swapPositions(rectState.getPosition().x2, rectState.getPosition().y2, rectState.getPosition().x, rectState.getPosition().y);
            }
            // make sure that the rectangle is not too small
            if (Math.abs(rectState.getPosition().x - rectState.getPosition().x2) <= 50){
            	rectState.getPosition().x2 = rectState.getPosition().x + 50;
            }
            if (Math.abs(rectState.getPosition().y - rectState.getPosition().y2) <= 50){
            	rectState.getPosition().y2 = rectState.getPosition().y + 50;
            }
            // pop-up for color
            Object[] possibilities = {"Black", "Blue", "Brown", "Gold", "Green", "DarkGreen", "LightBlue","Orange", "Pink", "Violate"};
        	String s2 = (String)JOptionPane.showInputDialog(null, "Choose Color", "Choose Color", JOptionPane.PLAIN_MESSAGE, null, possibilities, 
        			"Black");
        	Color c;
        	if (s2.equals("Black"))
        		c = Color.BLACK;
        	else if (s2.equals("Blue"))
        		c = Color.BLUE;
        	else if (s2.equals("Brown"))
        		c = new Color(156, 93, 82);
        	else if (s2.equals("Gold"))
        		c = new Color(212,175,55);
        	else if (s2.equals("Green"))
        		c = Color.GREEN;
        	else if (s2.equals("DarkGreen"))
        		c = new Color(69,148,81);
        	else if (s2.equals("LightBlue"))
        		c = new Color(29,231,190);
        	else if (s2.equals("Orange"))
        		c = new Color(237,154,71);
        	else if (s2.equals("Pink"))
        		c = new Color(216,70,219);
        	else if (s2.equals("Violate"))
        		c = new Color(119,23,121);
        	else
        		c = Color.BLACK;
            // set color
        	rectState.setColor(c);
    		rectState.setColor2(c);
    		rectState.setColor3(c);
    		rectState.getPosition().color = c;
        	
        	addElement(rectState); // add GElement to GElement
        	// add state to naming panel
        	getMachine().getNaming().addElement(rectState);
            getMachine().getNaming().repaint();
            // create GExport
        	GExport gE = new GExport(rectState, "NonAtomic");
            machine.getExport().addGExport(gE);
            machine.addToElements(rectState);
        }
    }
    
    /**
     * adds double rectangle
     * @param s - name
     * @param x - x coordinate
     * @param y - y coordinate
     * @param controller - controls if first coordinate or second coordinate
     */
    public void addEndNonAtomicStateAtXY(String s, double x, double y, int controller) {
    	//updateAll();
    	// if first coordinate
        if (controller == 0){
        	State state = new FAState(s); // create state
        	doubleRectState = new GElementFAStateDoubleRectangle(state, x, y,this); // create GElement
        }
        // if second coordinate
        else {
        	State state2 = new FAState(s); // remake state
        	state2.setAccepted(true);
        	machine.addState(state2); // add state to FAMachine
        	doubleRectState.setPositions(x, y); // set second position
        	doubleRectState.setState(state2); // set state
        	// ensure positions are in order
        	if (doubleRectState.getPosition().x > doubleRectState.getPosition().x2 
            		&& doubleRectState.getPosition().y > doubleRectState.getPosition().y2){
            	doubleRectState.swapPositions(doubleRectState.getPosition().x2, doubleRectState.getPosition().y2, doubleRectState.getPosition().x, doubleRectState.getPosition().y);
            }
        	// ensure it is not too small
        	if (Math.abs(doubleRectState.getPosition().x - doubleRectState.getPosition().x2) <= 50){
            	doubleRectState.getPosition().x2 = doubleRectState.getPosition().x + 50;
            }
            if (Math.abs(doubleRectState.getPosition().y - doubleRectState.getPosition().y2) <= 50){
            	doubleRectState.getPosition().y2 = doubleRectState.getPosition().y + 50;
            }
            // pop-up for color
        	Object[] possibilities = {"Black", "Blue", "Brown", "Gold", "Green", "DarkGreen", "LightBlue","Orange", "Pink", "Violate"};
        	String s2 = (String)JOptionPane.showInputDialog(null, "Choose Color", "Choose Color", JOptionPane.PLAIN_MESSAGE, null, possibilities, 
        			"Black");
        	
        	Color c;
        	if (s2.equals("Black"))
        		c = Color.BLACK;
        	else if (s2.equals("Blue"))
        		c = Color.BLUE;
        	else if (s2.equals("Brown"))
        		c = new Color(156, 93, 82);
        	else if (s2.equals("Gold"))
        		c = new Color(212,175,55);
        	else if (s2.equals("Green"))
        		c = Color.GREEN;
        	else if (s2.equals("DarkGreen"))
        		c = new Color(69,148,81);
        	else if (s2.equals("LightBlue"))
        		c = new Color(29,231,190);
        	else if (s2.equals("Orange"))
        		c = new Color(237,154,71);
        	else if (s2.equals("Pink"))
        		c = new Color(216,70,219);
        	else if (s2.equals("Violate"))
        		c = new Color(119,23,121);
        	else
        		c = Color.BLACK;
        	
        	// set color
        	doubleRectState.setColor(c);
    		doubleRectState.setColor2(c);
    		doubleRectState.setColor3(c);
    		doubleRectState.getPosition().color = c;
        	
        	addElement(doubleRectState); // add GElement to GElement array
        	// handle naming panel
        	getMachine().getNaming().addElement(doubleRectState); 
            getMachine().getNaming().repaint();
            // handle GExport
        	GExport gE = new GExport(doubleRectState, "EndNonAtomic");
            machine.getExport().addGExport(gE);
            machine.addToElements(doubleRectState);
        }
    }
    
    //makes sure all elements are in correctly in the naming panel
    public void updateAll(){
    	for (GElement update: this.elements){ 
    		if (update instanceof GLink){
    			getMachine().getNaming().addLink((GLink) update);
    			getMachine().getNaming().addLink((GLink) update);
    		}
    		else {
    			getMachine().getNaming().removeElement(update);
    			getMachine().getNaming().addElement(update);
    		}
    	getMachine().getNaming().repaint();
    	
    	}
    	
    }
    
    public GElementFAState getState(String name) {
        ListIterator e = elements.listIterator();
        while(e.hasNext()) {
            GElement element = (GElement)e.next();
            if(element.getClass().equals(GElementFAState.class)) {
                GElementFAState state = (GElementFAState)element;
                if(state.state.name.equals(name))
                    return state;
            }
        }
        return null;
    }

    public GElementFAStateInterface getState1(GLink link) {
        return (GElementFAStateInterface)link.source;
    }

    public GElementFAStateInterface getState2(GLink link) {
        return (GElementFAStateInterface)link.target;
    }

    public GLink getTransition(FATransition transition) {
        ListIterator e = elements.listIterator();
        while(e.hasNext()) {
            GElement element = (GElement)e.next();
            if(element.getClass().equals(GLink.class)) {
                GLink link = (GLink)element;
                if(getState1(link).getState().name.equals(transition.s1) &&
                   getState2(link).getState().name.equals(transition.s2)  &&
                    Tool.symbolsInPattern(link.pattern).contains(transition.symbol))
                {
                    return link;
                }
            }
        }
        return null;
    }
    
    /**
     * deletes a GElement
     * @param s
     */
    public void removeState(GElement s) {
    	// 1. find correct GElement type
    	// 2. cast to correct GElement
    	// 3. remove from FAMachine
    	if (s instanceof GElementFAState){
    		GElementFAState s2 = (GElementFAState)s;
    		machine.removeState(s2.state);
    	} else if (s instanceof GElementFAStateDoubleCircle){
    		GElementFAStateDoubleCircle s2 = (GElementFAStateDoubleCircle)s;
    		machine.removeState(s2.state);
    	} else if (s instanceof GElementFAStateRectangle){
    		GElementFAStateRectangle s2 = (GElementFAStateRectangle)s;
    		machine.removeState(s2.state);
    	} else if (s instanceof GElementFAStateDoubleRectangle){
    		GElementFAStateDoubleRectangle s2 = (GElementFAStateDoubleRectangle)s;
    		machine.removeState(s2.state);
    	}
    	
        removeElement(s); // remove state from GElement array
        // remove from naming panel
        getMachine().getNaming().removeElement(s);
        getMachine().getNaming().repaint();
        // Remove any other link which is using the state s
        ListIterator e = elements.listIterator();
        while(e.hasNext()) {
            GElement element = (GElement)e.next();
            if(element.getClass().equals(GLink.class)) {
                GLink link = (GLink)element;
                if(link.source == s || link.target == s) {
                    removeElement(link);
                    getMachine().getNaming().removeLink(link);
                    getMachine().getNaming().repaint();
                    e = elements.listIterator();
                }
            }
        }
    }

    public void addState(GElement s){
    	//updateAll();
    	// 1. find GElement type
    	// 2. cast to correct GElement state
    	// 3. add to FAMachine
    	if (s instanceof GElementFAState){
    		GElementFAState s2 = (GElementFAState)s;
    		machine.addState(s2.state);
    	} else if (s instanceof GElementFAStateDoubleCircle){
    		GElementFAStateDoubleCircle s2 = (GElementFAStateDoubleCircle)s;
    		machine.addState(s2.state);
    	} else if (s instanceof GElementFAStateRectangle){
    		GElementFAStateRectangle s2 = (GElementFAStateRectangle)s;
    		machine.addState(s2.state);
    	} else if (s instanceof GElementFAStateDoubleRectangle){
    		GElementFAStateDoubleRectangle s2 = (GElementFAStateDoubleRectangle)s;
    		machine.addState(s2.state);
    	}
    }
    
    /**
     * creates a link between a GElementFAState and GElementFAState
     */
    public boolean createLink(GElementFAState source, String sourceAnchorKey, GElementFAState target, String targetAnchorKey, int shape, Point mouse) {
        //updateAll();
    	//pop-up for naming transition
    	String pattern = (String)JOptionPane.showInputDialog(null, Localized.getString("faNewLinkMessage"),
                                    Localized.getString("faNewLinkTitle"),
                                    JOptionPane.QUESTION_MESSAGE, null, null, null);
    	// pop-up for color selection
        /*Object[] possibilities = {"Black","Blue", "Yellow", "Green"};
		String s2 = (String)JOptionPane.showInputDialog(null, "Choose Color", "Choose Color", JOptionPane.PLAIN_MESSAGE, null, possibilities, 
	    		"Black");
		Color c;
		if (s2.equals("Black"))
			c = Color.BLACK;
	   // else if (s2.equals("Red"))
	    //	c = Color.RED;
	    else if (s2.equals("Blue"))
	    	c = Color.BLUE;
	    else if (s2.equals("Yellow"))
	    	c = Color.YELLOW;
	    else if (s2.equals("Green"))
	    	c = Color.GREEN;
	    else
	    	c = Color.BLACK;*/
        
		// if name is not null
        if(pattern != null) {
            machine.addTransitionPattern(source.state.name, pattern, target.state.name); // add transition to FAMachine
            // create graphical link
            GLink link = new GLink(source, sourceAnchorKey, target, targetAnchorKey, shape, pattern, mouse, GView.DEFAULT_LINK_FLATENESS,this);
            //link.setColor(c); // set color
            addElement(link); // add link to GElement array
            //add the element to the nickname panel
            getMachine().getNaming().addLink(link);
            getMachine().getNaming().repaint();
        }
        return pattern != null;
    }
    
    /*
     * creates a link between a GElementFAState and GElementFAStateDoubleCircle
     */
    public boolean createLinkStateToEnd(GElementFAState source, String sourceAnchorKey, GElementFAStateDoubleCircle target, String targetAnchorKey, int shape, Point mouse){
    	//updateAll();
    	String pattern = (String)JOptionPane.showInputDialog(null, Localized.getString("faNewLinkMessage"),
                Localized.getString("faNewLinkTitle"),
                JOptionPane.QUESTION_MESSAGE, null, null, null);
    	/*Object[] possibilities = {"Black","Blue", "Yellow", "Green"};
		String s2 = (String)JOptionPane.showInputDialog(null, "Choose Color", "Choose Color", JOptionPane.PLAIN_MESSAGE, null, possibilities, 
	    		"Black");
		Color c;
		if (s2.equals("Black"))
			c = Color.BLACK;
	   // else if (s2.equals("Red"))
	    //	c = Color.RED;
	    else if (s2.equals("Blue"))
	    	c = Color.BLUE;
	    else if (s2.equals("Yellow"))
	    	c = Color.YELLOW;
	    else if (s2.equals("Green"))
	    	c = Color.GREEN;
	    else
	    	c = Color.BLACK;*/
    	
    	if(pattern != null) {
    		machine.addTransitionPattern(source.state.name, pattern, target.state.name);
    		GLink link = new GLink(source, sourceAnchorKey, target, targetAnchorKey, shape, pattern, mouse, GView.DEFAULT_LINK_FLATENESS,this);
    		//link.setColor(c);
    		addElement(link);
            //add the element to the nickname panel
            getMachine().getNaming().addLink(link);
            getMachine().getNaming().repaint();
    	}
    	
    	return pattern != null;
    }
    
    /*
     * creates a link between a GElementFAStateDoubleCircle and GElementFAState
     */
    public boolean createLinkEndToState(GElementFAStateDoubleCircle source, String sourceAnchorKey, GElementFAState target, String targetAnchorKey, int shape, Point mouse){
    	//updateAll();
    	String pattern = (String)JOptionPane.showInputDialog(null, Localized.getString("faNewLinkMessage"),
                Localized.getString("faNewLinkTitle"),
                JOptionPane.QUESTION_MESSAGE, null, null, null);
    	/*Object[] possibilities = {"Black","Blue", "Yellow", "Green"};
		String s2 = (String)JOptionPane.showInputDialog(null, "Choose Color", "Choose Color", JOptionPane.PLAIN_MESSAGE, null, possibilities, 
	    		"Black");
		Color c;
		if (s2.equals("Black"))
			c = Color.BLACK;
	   // else if (s2.equals("Red"))
	    //	c = Color.RED;
	    else if (s2.equals("Blue"))
	    	c = Color.BLUE;
	    else if (s2.equals("Yellow"))
	    	c = Color.YELLOW;
	    else if (s2.equals("Green"))
	    	c = Color.GREEN;
	    else
	    	c = Color.BLACK;*/
    	
    	if(pattern != null) {
    		machine.addTransitionPattern(source.state.name, pattern, target.state.name);
    		GLink link = new GLink(source, sourceAnchorKey, target, targetAnchorKey, shape, pattern, mouse, GView.DEFAULT_LINK_FLATENESS,this);
    		//link.setColor(c);
    		addElement(link);
            //add the element to the nickname panel
            getMachine().getNaming().addLink(link);
            getMachine().getNaming().repaint();
    	}
    	
    	return pattern != null;
    }

    /*
     * creates a link between a GElementFAStateDoubleCircle and GElementFAStateDoubleCircle
     */
    public boolean createLinkEndToEnd(GElementFAStateDoubleCircle source, String sourceAnchorKey, GElementFAStateDoubleCircle target, String targetAnchorKey, int shape, Point mouse){
    	//updateAll();
    	String pattern = (String)JOptionPane.showInputDialog(null, Localized.getString("faNewLinkMessage"),
                Localized.getString("faNewLinkTitle"),
                JOptionPane.QUESTION_MESSAGE, null, null, null);
    	/*Object[] possibilities = {"Black","Blue", "Yellow", "Green"};
		String s2 = (String)JOptionPane.showInputDialog(null, "Choose Color", "Choose Color", JOptionPane.PLAIN_MESSAGE, null, possibilities, 
	    		"Black");
		Color c;
		if (s2.equals("Black"))
			c = Color.BLACK;
	   // else if (s2.equals("Red"))
	    //	c = Color.RED;
	    else if (s2.equals("Blue"))
	    	c = Color.BLUE;
	    else if (s2.equals("Yellow"))
	    	c = Color.YELLOW;
	    else if (s2.equals("Green"))
	    	c = Color.GREEN;
	    else
	    	c = Color.BLACK;*/
		
    	if(pattern != null) {
    		machine.addTransitionPattern(source.state.name, pattern, target.state.name);
    		GLink link = new GLink(source, sourceAnchorKey, target, targetAnchorKey, shape, pattern, mouse, GView.DEFAULT_LINK_FLATENESS,this);
    		//link.setColor(c);
    		addElement(link);
            //add the element to the nickname panel
            getMachine().getNaming().addLink(link);
            getMachine().getNaming().repaint();
    	}
    	
    	return pattern != null;
    }
    
    public boolean editLink(GLink link) {
    	String oldPattern = link.pattern;
        String pattern = (String)JOptionPane.showInputDialog(null, Localized.getString("faEditLinkMessage"),
                                    Localized.getString("faEditLinkTitle"),
                                    JOptionPane.QUESTION_MESSAGE, null, null, link.pattern);
       
        /*Object[] possibilities = {"No Change", "Black", "Blue", "Yellow", "Green"};
    	
        String s2 = link.getColor().toString();
		s2 = (String)JOptionPane.showInputDialog(null, "Choose New Color", "Choose New Color", JOptionPane.PLAIN_MESSAGE, null, possibilities, 
	    		link.getColor());*/
		String nickname = (String)JOptionPane.showInputDialog(null, "Modify the long name of this link:",
              "Modify the long name of this link:",
                JOptionPane.QUESTION_MESSAGE, null, null, link.nickname);
		Color c;
		getMachine().getNaming().removeLink(link);
		/*if (s2 == null || s2.equals("No Change")){
			s2="Black";
			if (link.getColor().equals(Color.BLUE))
				s2 =  "Blue";
			else if (link.getColor().equals(Color.GREEN))
				s2 = "Green";
			else if (link.getColor().equals(Color.YELLOW))
				s2= "Yellow";
		}*/
		if (pattern == null)
			pattern = link.pattern;
		if (nickname == null)
			nickname = link.nickname;
		/*if (s2.equals("Black"))
			c = Color.BLACK;
	   // else if (s2.equals("Red"))
	    //	c = Color.RED;
	    else if (s2.equals("Blue"))
	    	c = Color.BLUE;
	    else if (s2.equals("Yellow"))
	    	c = Color.YELLOW;
	    else if (s2.equals("Green"))
	    	c = Color.GREEN;
	    else
	    	c = Color.BLACK;*/
        if(pattern != null) {
        	//getMachine().getNaming().removeLink(link);
        	removeElement(link);
            link.pattern = pattern;
            link.nickname = nickname;
            machine.removeTransitionPattern(getState1(link).getState().name, oldPattern, getState2(link).getState().name);
            machine.addTransitionPattern(getState1(link).getState().name, link.pattern, getState2(link).getState().name);
            //link.setColor(c);
            System.out.println("editLink");
            getMachine().getNaming().addLink(link);
            updateAll();
            getMachine().getNaming().repaint();
            addElement(link);
        }

        return pattern != null;
    }

    public void removeLink(GLink link) {
    	//updateAll();
        removeElement(link);
        System.out.println("removeLink");
        getMachine().getNaming().removeLink(link);
        getMachine().getNaming().repaint();
        machine.removeTransitionPattern(getState1(link).getState().name, link.pattern, getState2(link).getState().name);
    }
    
    /*
     * creates a link between a GElementFAStateRectangle and GElementFAState
     */
    public boolean createLinkRectangleToState(GElementFAStateRectangle source, String sourceAnchorKey, GElementFAState target, String targetAnchorKey, int shape, Point mouse){
    	//updateAll();
    	String pattern = (String)JOptionPane.showInputDialog(null, Localized.getString("faNewLinkMessage"),
                Localized.getString("faNewLinkTitle"),
                JOptionPane.QUESTION_MESSAGE, null, null, null);
    	/*Object[] possibilities = {"Black","Blue", "Yellow", "Green"};
		String s2 = (String)JOptionPane.showInputDialog(null, "Choose Color", "Choose Color", JOptionPane.PLAIN_MESSAGE, null, possibilities, 
	    		"Black");
		Color c;
		if (s2.equals("Black"))
			c = Color.BLACK;
	   // else if (s2.equals("Red"))
	    //	c = Color.RED;
	    else if (s2.equals("Blue"))
	    	c = Color.BLUE;
	    else if (s2.equals("Yellow"))
	    	c = Color.YELLOW;
	    else if (s2.equals("Green"))
	    	c = Color.GREEN;
	    else
	    	c = Color.BLACK;*/
		
    	if(pattern != null) {
    		machine.addTransitionPattern(source.state.name, pattern, target.state.name);
    		GLink link = new GLink(source, sourceAnchorKey, target, targetAnchorKey, shape, pattern, mouse, GView.DEFAULT_LINK_FLATENESS,this);
    		//link.setColor(c);
    		addElement(link);
            //add the element to the nickname panel
            getMachine().getNaming().addLink(link);
            getMachine().getNaming().repaint();
    	}
    	
    	return pattern != null;
    }

    public boolean createLinkRectangleToRectangle(GElement source, String sourceAnchorKey, GElement target, String targetAnchorKey, int shape, Point mouse){
    	//updateAll();
    	String pattern = (String)JOptionPane.showInputDialog(null, Localized.getString("faNewLinkMessage"),
                Localized.getString("faNewLinkTitle"),
                JOptionPane.QUESTION_MESSAGE, null, null, null);
    	/*Object[] possibilities = {"Black","Blue", "Yellow", "Green"};
		String s2 = (String)JOptionPane.showInputDialog(null, "Choose Color", "Choose Color", JOptionPane.PLAIN_MESSAGE, null, possibilities, 
	    		"Black");
		Color c;
		if (s2.equals("Black"))
			c = Color.BLACK;
	   // else if (s2.equals("Red"))
	    //	c = Color.RED;
	    else if (s2.equals("Blue"))
	    	c = Color.BLUE;
	    else if (s2.equals("Yellow"))
	    	c = Color.YELLOW;
	    else if (s2.equals("Green"))
	    	c = Color.GREEN;
	    else
	    	c = Color.BLACK;*/
		
    	if(pattern != null) {
    		if (source instanceof GElementFAStateDoubleRectangle && target instanceof GElementFAState)
    			machine.addTransitionPattern(((GElementFAStateDoubleRectangle)source).state.name, pattern, ((GElementFAState)target).state.name);
    		else if (source instanceof GElementFAStateRectangle && target instanceof GElementFAStateDoubleCircle)
    			machine.addTransitionPattern(((GElementFAStateRectangle)source).state.name, pattern, ((GElementFAStateDoubleCircle)target).state.name);
    		else if (source instanceof GElementFAStateDoubleRectangle && target instanceof GElementFAStateDoubleCircle)
    			machine.addTransitionPattern(((GElementFAStateDoubleRectangle)source).state.name, pattern, ((GElementFAStateDoubleCircle)target).state.name);
    		else if (source instanceof GElementFAStateRectangle && target instanceof GElementFAStateRectangle)
    			machine.addTransitionPattern(((GElementFAStateRectangle)source).state.name, pattern, ((GElementFAStateRectangle)target).state.name);
    		else if (source instanceof GElementFAStateDoubleRectangle && target instanceof GElementFAStateDoubleRectangle)
    			machine.addTransitionPattern(((GElementFAStateDoubleRectangle)source).state.name, pattern, ((GElementFAStateDoubleRectangle)target).state.name);
    		GLink link = new GLink(source, sourceAnchorKey, target, targetAnchorKey, shape, pattern, mouse, GView.DEFAULT_LINK_FLATENESS,this);
    		//link.setColor(c);
    		addElement(link);
            //add the element to the nickname panel
            getMachine().getNaming().addLink(link);
            getMachine().getNaming().repaint();
    	}
    	
    	return pattern != null;
    }

    
    public void reconstruct() {
    	updateAll();
        elements.clear();
        List stateNames = machine.getStateList();

        ListIterator iterator = stateNames.listIterator(stateNames.size());

        int x = 0;
        int y = 0;
        while(iterator.hasPrevious()) {
            addElement(new GElementFAState((State)iterator.previous(), 100+x*200, 50+y*200,this));
            x++;
            if(x>4) {
                y++;
                x = 0;
            }
        }

        List transitions = machine.getTransitions().getTransitions();
        iterator = transitions.listIterator();
        while(iterator.hasNext()) {
            FATransition transition = (FATransition)iterator.next();

            GElementFAState s1 = getState(transition.s1);
            GElementFAState s2 = s1;
            if(transition.s1.equals(transition.s2) == false)
                s2 = getState(transition.s2);

            GLink link = getLink(s1, s2);
            if(link == null)
                addElement(new GLink(s1, GElementFAState.ANCHOR_CENTER,
                            s2, GElementFAState.ANCHOR_CENTER,
                            GLink.SHAPE_ARC, transition.symbol, 20));
            else
                link.pattern = Tool.addSymbolToPattern(link.pattern, transition.symbol);
        }
    }

    public GLink getLink(GElementFAStateInterface s1, GElementFAStateInterface s2) {
        Iterator elements = getElements().iterator();
        while(elements.hasNext()) {
            Object e = elements.next();
            if(e instanceof GLink) {
                GLink l = (GLink)e;
                if(l.source == s1 && l.target == s2)
                    return l;
            }
        }
        return null;
    }

    // *** Exec methods

    public String check(String s) {
        String error = machine.check();
        if(error != null) {
            JOptionPane.showMessageDialog(null, Localized.getString("faCannotStart")+"\n"+error, Localized.getString("error"),
                    JOptionPane.INFORMATION_MESSAGE, null);
            return null;
        }

        if(s == null)
            s = (String)JOptionPane.showInputDialog(null, Localized.getString("faParseString"), Localized.getString("faStartTitle"),
                                    JOptionPane.QUESTION_MESSAGE, null, null, null);

        return s;
    }

    public void run(String s) {
    	updateAll();
        s = check(s);
        if(s == null)
            return;

        if(machine.accept(s))
            JOptionPane.showMessageDialog(null, Localized.getString("faAcceptString"), Localized.getString("automaton"),
                    JOptionPane.INFORMATION_MESSAGE, null);
        else
            JOptionPane.showMessageDialog(null, Localized.getString("faRejectString"), Localized.getString("automaton"),
                    JOptionPane.ERROR_MESSAGE, null);
    }

    public boolean isStopped() {
    	updateAll();
        return state == STATE_STOPPED;
    }

    public boolean isReady() {
    	updateAll();
        return state == STATE_READY;
    }

    public boolean isRunning() {
    	updateAll();
        return state == STATE_RUNNING;
    }

    public boolean isPaused() {
    	updateAll();
        return state == STATE_PAUSED;
    }

    // *** Debug methods

    public void debugReset(String s) {
        String r = check(s);
        if(r == null)
            return;

        state = STATE_READY;
        machine.debugReset(r);
    }

    public void debugStepForward() {
        if(machine.debugStepForward())
            state = STATE_PAUSED;
        else
            state = STATE_STOPPED;
    }

    public List debugLastStates() {
        List states = new ArrayList();

        Set set = machine.getStateSet();
        if(set.isEmpty())
            set = machine.getStartStates();

        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
            states.add(getState((String)iterator.next()));
        }

        return states;
    }

    public List debugLastTransitions() {
        List transitions = new ArrayList();

        Iterator iterator = machine.getLastTransitionSet().iterator();
        while(iterator.hasNext()) {
            transitions.add(getTransition((FATransition)iterator.next()));
        }

        return transitions;
    }

    /**
     * set circle as start state
     * @param state
     */
    public void toggleStartState(GElementFAState state) {
        if(!state.isStart()) {
            Iterator iterator = elements.iterator();
            while(iterator.hasNext()) {
                GElement element = (GElement)iterator.next();
                if(element instanceof GElementFAState) {
                    ((GElementFAState)element).setStart(false);
                }
            }
        }
        state.toggleStart();
    }

    /**
     * sets double circle as start state
     * @param state
     */
    public void toggleStartState(GElementFAStateDoubleCircle state) {
        if(state.isStart() == false) {
            Iterator iterator = elements.iterator();
            while(iterator.hasNext()) {
                GElement element = (GElement)iterator.next();
                if(element instanceof GElementFAState) {
                    ((GElementFAState)element).setStart(false);
                }
            }
        }
        state.toggleStart();
        if (state.isStart()) {
            machine.getTransitions().addTransition("", "", state.getState().getName());        	
        }
        else {
        	machine.getTransitions().removeTransition("", "", state.getState().getName());
        }
    }
    
    public void clear() {
        machine.clear();
        elements.clear();
    }
    
    public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	@Override
	public boolean isInside(GElement e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int maxCoorX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int maxCoorY() {
		// TODO Auto-generated method stub
		return 0;
	}

	public GLink findTransition(String source, String target, String label) {
		for(GElement e : elements) {
			if(e instanceof GLink) {
				GLink link = ((GLink) e);
				if(getStateFullName(link.getRealSource()).equals(source) &&
						getStateFullName(link.getRealTarget()).equals(target) && 
						getTranFullName(link).equals(label)) {
					return link;
				}
			}
		}
		for(GElement e : collapsed) {
			if(e instanceof GLink) {
				GLink link = ((GLink) e);
				if(getStateFullName(link.getRealSource()).equals(source) &&
						getStateFullName(link.getRealTarget()).equals(target) && 
						getTranFullName(link).equals(label)) {
					return link;
				}
			}
		}
		return null;
	}
	
	private String getTranFullName(GLink link) {
		if(link.getNickname()==null || link.getNickname().equals("")) {
			return link.getPattern();
		}
		return link.getNickname();
	}

	private String getStateFullName(GElement state) {
		if(state.getNickname()==null || state.getNickname().equals("")) {
			return state.getLabel();
		}
		return state.getNickname();
	}

	public GElement findState(String target) {
		for(GElement e : elements) {
			if(e instanceof GElementFAStateInterface) {
				if(getStateFullName(e).equals(target)) {
					return e;
				}
			}
		}
		for(GElement e : collapsed) {
			if(e instanceof GElementFAStateInterface) {
				if(getStateFullName(e).equals(target)) {
					return e;
				}
			}
		}
		return null;
	}
	
	public GElement getHiddenHighlightState() {
		for(GElement e : collapsed) {
			if((e instanceof GElementFAStateDoubleCircle) || (e instanceof GElementFAState)) {
				if(e.isHighlight()) {
					return e;
				}
			}
		}
		return null;
	}

	public boolean hiddenInside(
			GElement bigState,
			GElement smallState) {
		for(int i=0;i<collapsed.size();i++) {
			if(collapsed.get(i)==smallState && appearWhenExpand.get(i)==bigState) {
				return true;
			}
		}
		return false;
	}
}
