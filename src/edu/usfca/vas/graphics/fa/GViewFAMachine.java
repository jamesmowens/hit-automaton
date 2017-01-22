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
import edu.usfca.vas.window.tools.DesignToolsFA;
import edu.usfca.xj.appkit.frame.XJFrame;
import edu.usfca.xj.appkit.gview.GView;
import edu.usfca.xj.appkit.gview.object.GElement;
import edu.usfca.xj.appkit.gview.object.GLink;
import edu.usfca.xj.appkit.menu.XJMenu;
import edu.usfca.xj.appkit.menu.XJMenuItem;
import edu.usfca.xj.appkit.utils.XJAlert;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GViewFAMachine extends GView {

    // ** Menu items
	
	private static final long serialVersionUID = 268759755016690823L;
	
	private static final int MI_ADD_STATE = 0;
    private static final int MI_REMOVE_STATE = 1;
    private static final int MI_SET_START_STATE = 2;
    private static final int MI_SET_ACCEPTED_STATE = 3;
    private static final int MI_CLEAR_ALL = 4;
    private static final int MI_EDIT_LINK = 5;
    private static final int MI_REMOVE_LINK = 6;
    private static final int MI_COLOR = 7;
    private static final int MI_COLOR_BLACK = 8;
    //private static final int MI_COLOR_RED = 9;
    private static final int MI_COLOR_BLUE = 9;
    private static final int MI_COLOR_GREEN = 10;
    private static final int MI_COLOR_YELLOW = 11;
    private static final int MI_COLOR_BROWN = 18;
	private static final int MI_COLOR_GOLD = 19;
	private static final int MI_COLOR_DARKGREEN = 20;
	private static final int MI_COLOR_LIGHTBLUE = 21;
	private static final int MI_COLOR_ORANGE = 22;
	private static final int MI_COLOR_PINK = 23;
	private static final int MI_COLOR_VIOLET = 24;
    private static final int MI_COLLAPSE_STATE = 12;
    private static final int MI_CONTAINS_COLOR = 13;
    private static final int MI_CONTAINS_COLOR_BLACK = 14;
    private static final int MI_CONTAINS_COLOR_BLUE = 15;
    private static final int MI_CONTAINS_COLOR_GREEN = 16;
    private static final int MI_CONTAINS_COLOR_YELLOW = 17;

    public int controller = 0;
    public int controller2 = 0;
    
    public GElementFAState state;
    public GElementFAStateDoubleCircle state2;
    public GElementFAStateRectangle state3;
    public GElementFAStateDoubleRectangle state4;
    
    protected DesignToolsFA designToolFA = null;
    protected XJFrame parent;

    public GViewFAMachine(XJFrame parent,GElementFAMachine machine) {
        super(machine);
        this.parent = parent;
    }

    public int defaultLinkShape() {
        return GLink.SHAPE_ARC;
    }

    public void setDesignToolsPanel(DesignToolsFA designToolFA) {
        this.designToolFA = designToolFA;
    }

    public DesignToolsFA getDesignToolsPanel(){
    	return this.designToolFA;
    }
    
    public void setMachine(GElementFAMachine machine) {
        setRootElement(machine);
    }

    public GElementFAMachine getMachine() {
        return (GElementFAMachine)getRootElement();
    }

    /**
     * highlights a given shape in red
     * @param ge - gelement to be highlighted
     */
    public void highlightShape(GElement ge){
    	getMachine().highlightShape(ge);
    	repaint();
    }
    
    public void unhighlightShape(GElement ge){
    	getMachine().unhighlightShape(ge);
    	repaint();
    }
    
    /**
     * creates menu that drops down when right-clicking a element
     */
    public JPopupMenu getContextualMenu(GElement element) {
    	// boolean for each type of element
        boolean stateSelected = false;
        boolean doubleCircleSelected = false;
        boolean rectangleSelected = false;
        boolean doubleRectangleSelected = false;
        boolean linkSelected = false;
        
        // checks type of element
        if(element != null) {
            stateSelected = element.getClass().equals(GElementFAState.class);
            doubleCircleSelected = element.getClass().equals(GElementFAStateDoubleCircle.class);
            rectangleSelected = element.getClass().equals(GElementFAStateRectangle.class);
            doubleRectangleSelected = element.getClass().equals(GElementFAStateDoubleRectangle.class);
            linkSelected = element.getClass().equals(GLink.class);
        }

        state = null;
        state2 = null;
        state3 = null;
        state4 = null;
        
        // creates menu
        JPopupMenu menu = new JPopupMenu();
        menu.addPopupMenuListener(new MyContextualMenuListener());   
        XJMenu colorMenu;
        XJMenu containsColorMenu;
        
        // handle based on element type
        if(stateSelected) {
            state = (GElementFAState)element;
            addMenuItem(menu, state.state.start?Localized.getString("faMIRemoveStartState"):Localized.getString("faMISetStartState"), MI_SET_START_STATE, element);
            addMenuItem(menu, state.state.accepted?Localized.getString("faMIRemoveAcceptedState"):Localized.getString("faMISetAcceptedState"), MI_SET_ACCEPTED_STATE, element);
            menu.addSeparator();
            addMenuItem(menu, Localized.getString("faMIDelete"), MI_REMOVE_STATE, element);
            /*menu.addSeparator();
            colorMenu = addSubMenu(menu,Localized.getString("faMIColor"), MI_COLOR, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorBlack"), MI_COLOR_BLACK, element);
           // addSubMenuItem(colorMenu, Localized.getString("faMIColorRed"), MI_COLOR_RED, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorBlue"), MI_COLOR_BLUE, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorGreen"), MI_COLOR_GREEN, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorYellow"), MI_COLOR_YELLOW, element);*/
        } else if(doubleCircleSelected){
        	state2 = (GElementFAStateDoubleCircle)element;
            addMenuItem(menu, state2.state.start?Localized.getString("faMIRemoveStartState"):Localized.getString("faMISetStartState"), MI_SET_START_STATE, element);
            addMenuItem(menu, state2.state.accepted?Localized.getString("faMIRemoveAcceptedState"):Localized.getString("faMISetAcceptedState"), MI_SET_ACCEPTED_STATE, element);
            menu.addSeparator();
            addMenuItem(menu, Localized.getString("faMIDelete"), MI_REMOVE_STATE, element);
            /*menu.addSeparator();
            colorMenu = addSubMenu(menu,Localized.getString("faMIColor"), MI_COLOR, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorBlack"), MI_COLOR_BLACK, element);
           // addSubMenuItem(colorMenu, Localized.getString("faMIColorRed"), MI_COLOR_RED, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorBlue"), MI_COLOR_BLUE, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorGreen"), MI_COLOR_GREEN, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorYellow"), MI_COLOR_YELLOW, element);*/
        } else if(rectangleSelected){
        	state3 = (GElementFAStateRectangle)element;
            addMenuItem(menu, Localized.getString("faMIDelete"), MI_REMOVE_STATE, element);
            //System.out.println("HERE: " + element.isCollapsed);
            addMenuItem(menu, element.isCollapsed?"Expand State":"Collapse State", MI_COLLAPSE_STATE, element);
            menu.addSeparator();
            colorMenu = addSubMenu(menu,Localized.getString("faMIColor"), MI_COLOR, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorBlack"), MI_COLOR_BLACK, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorBlue"), MI_COLOR_BLUE, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorBrown"), MI_COLOR_BROWN, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorGold"), MI_COLOR_GOLD, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorGreen"), MI_COLOR_GREEN, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorDarkGreen"), MI_COLOR_DARKGREEN, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorLightBlue"), MI_COLOR_LIGHTBLUE, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorOrange"), MI_COLOR_ORANGE, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorPink"), MI_COLOR_PINK, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorViolet"), MI_COLOR_VIOLET, element);
            
            /*containsColorMenu = addSubMenu(menu,"Contains Color", MI_CONTAINS_COLOR, element);
            addSubMenuItem(containsColorMenu, "Black", MI_CONTAINS_COLOR_BLACK, element);
            addSubMenuItem(containsColorMenu, "Blue", MI_CONTAINS_COLOR_BLUE, element);
            addSubMenuItem(containsColorMenu, "Green", MI_CONTAINS_COLOR_GREEN, element);
            addSubMenuItem(containsColorMenu, "Yellow", MI_CONTAINS_COLOR_YELLOW, element);*/
        } else if(doubleRectangleSelected){
        	state4 = (GElementFAStateDoubleRectangle)element;
            addMenuItem(menu, Localized.getString("faMIDelete"), MI_REMOVE_STATE, element);
            //System.out.println("HERE: " + element.isCollapsed);
            addMenuItem(menu, element.isCollapsed?"Expand State":"Collapse State", MI_COLLAPSE_STATE, element);
            menu.addSeparator();
            colorMenu = addSubMenu(menu,Localized.getString("faMIColor"), MI_COLOR, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorBlack"), MI_COLOR_BLACK, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorBlue"), MI_COLOR_BLUE, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorBrown"), MI_COLOR_BROWN, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorGold"), MI_COLOR_GOLD, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorGreen"), MI_COLOR_GREEN, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorDarkGreen"), MI_COLOR_DARKGREEN, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorLightBlue"), MI_COLOR_LIGHTBLUE, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorOrange"), MI_COLOR_ORANGE, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorPink"), MI_COLOR_PINK, element);
            addSubMenuItem(colorMenu, Localized.getString("faMIColorViolet"), MI_COLOR_VIOLET, element);
            /*containsColorMenu = addSubMenu(menu,"Contains Color", MI_CONTAINS_COLOR, element);
            addSubMenuItem(containsColorMenu, "Black", MI_CONTAINS_COLOR_BLACK, element);
            addSubMenuItem(containsColorMenu, "Blue", MI_CONTAINS_COLOR_BLUE, element);
            addSubMenuItem(containsColorMenu, "Green", MI_CONTAINS_COLOR_GREEN, element);
            addSubMenuItem(containsColorMenu, "Yellow", MI_CONTAINS_COLOR_YELLOW, element);*/
        } else if(linkSelected) {
            addMenuItem(menu, Localized.getString("faMIEdit"), MI_EDIT_LINK, element);
            addMenuItem(menu, Localized.getString("faMIDelete"), MI_REMOVE_LINK, element);
        } else {
            addMenuItem(menu, Localized.getString("faMIAddState"), MI_ADD_STATE, null);
            menu.addSeparator();
            addMenuItem(menu, Localized.getString("faMIDeleteAll"), MI_CLEAR_ALL, null);
        }

        return menu;
    }

    public void createStateAtXY(double x, double y) {
        String s = (String)JOptionPane.showInputDialog(null, Localized.getString("faNewStateMessage"), Localized.getString("faNewStateTitle"),
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        if(s != null) {
            if(getMachine().getMachine().containsStateName(s)){
            	controller = 0;
                XJAlert.display(parent.getJavaContainer(), Localized.getString("faNewStateTitle"), Localized.getString("faNewStateAlreadyExists"));
            } else {
                getMachine().addStateAtXY(s, x, y);
                changeDone();
                repaint();
            } 
        }
    }

    public void editState(GElement state) {
    	getMachine().getMachine().getNaming().removeElement(state);
    	String s;
    	String s2 = state.getColor().toString();
    	String nickname;
    	controller = 0;

    	if (state instanceof GElementFAState){
    		s = (String)JOptionPane.showInputDialog(null, Localized.getString("faEditStateMessage"), Localized.getString("faEditStateTitle"),
                    JOptionPane.QUESTION_MESSAGE, null, null, ((GElementFAState)state).state.name);
    		/*Object[] possibilities = {"No Change", "Black", "Blue", "Yellow", "Green"};
    
    		s2 = (String)JOptionPane.showInputDialog(null, "Choose New Color", "Choose New Color", JOptionPane.PLAIN_MESSAGE, null, possibilities, 
    	    		state.getColor());*/
    		nickname = (String)JOptionPane.showInputDialog(null, "Modify state nickname: ", "Modify state nickname: ",
                    JOptionPane.QUESTION_MESSAGE, null, null, state.getNickname());
    		/*Color c;
    		if (s2 == null || s2.equals("No Change")){
    			s2="Black";
    			if (state.getColor().equals(Color.BLUE))
    				s2 =  "Blue";
    			else if (state.getColor().equals(Color.GREEN))
    				s2 = "Green";
    			else if (state.getColor().equals(Color.YELLOW))
    				s2= "Yellow";
    		}*/
    		if (s == null)
    			s = state.getLabel();
    		if (nickname == null)
    			nickname = state.getNickname();
    		//changeDone();
    		state.setNickname(nickname);
    		/*if (s2.equals("Black"))
    			c = Color.BLACK;
    	    //else if (s2.equals("Red"))
    	    	//c = Color.RED;
    	    else if (s2.equals("Blue"))
    	    	c = Color.BLUE;
    	    else if (s2.equals("Yellow"))
    	    	c = Color.YELLOW;
    	    else if (s2.equals("Green"))
    	    	c = Color.GREEN;
    	    else
    	    	c = Color.BLACK;
    	    ((GElementFAState)state).setColor(c);
    		((GElementFAState)state).setColor2(c);
    		((GElementFAState)state).setColor3(c);
    		((GElementFAState)state).getPosition().color = c;
    		System.out.println("Editing State");*/
    		
    		if(s != null && !(s.equals(((GElementFAState) state).state.name))) {
                if(getMachine().getMachine().containsStateName(s))
                    XJAlert.display(parent.getJavaContainer(), Localized.getString("faEditStateTitle"), Localized.getString("faEditStateAlreadyExists"));
                else {
                    getMachine().getMachine().renameState(((GElementFAState)state).state, ((GElementFAState)state).state.name, s);
                    //changeDone();
                    repaint();
                }
            }
    	} else if (state instanceof GElementFAStateDoubleCircle){
    		s = (String)JOptionPane.showInputDialog(null, Localized.getString("faEditStateMessage"), Localized.getString("faEditStateTitle"),
                    JOptionPane.QUESTION_MESSAGE, null, null, ((GElementFAStateDoubleCircle)state).state.name);
    		nickname = (String)JOptionPane.showInputDialog(null, "Modify state nickname: ", "Modify state nickname: ",
                    JOptionPane.QUESTION_MESSAGE, null, null, state.getNickname());
    		/*Object[] possibilities = {"No Change", "Black", "Blue", "Yellow", "Green"};
    		s2 = (String)JOptionPane.showInputDialog(null, "Choose New Color", "Choose New Color", JOptionPane.PLAIN_MESSAGE, null, possibilities, 
    	    		state.getColor());
    		Color c;
    		if (s2 == null || s2.equals("No Change")){
    			s2="Black";
    			if (state.getColor().equals(Color.BLUE))
    				s2 =  "Blue";
    			else if (state.getColor().equals(Color.GREEN))
    				s2 = "Green";
    			else if (state.getColor().equals(Color.YELLOW))
    				s2= "Yellow";
    		}*/
    		if (s == null)
    			s = ((GElementFAStateRectangle) state).state.name;
    		if (nickname == null)
    			nickname = state.getNickname();
    		state.setNickname(nickname);
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
    	    	c = Color.BLACK;
    	    ((GElementFAStateDoubleCircle)state).setColor(c);
    		((GElementFAStateDoubleCircle)state).setColor2(c);
    		((GElementFAStateDoubleCircle)state).setColor3(c);
    		((GElementFAStateDoubleCircle)state).getPosition().color = c;*/
    		
    		if(s != null && !(s.equals(((GElementFAStateDoubleCircle) state).state.name))) {
                if(getMachine().getMachine().containsStateName(s))
                    XJAlert.display(parent.getJavaContainer(), Localized.getString("faEditStateTitle"), Localized.getString("faEditStateAlreadyExists"));
                else {
                    getMachine().getMachine().renameState(((GElementFAStateDoubleCircle)state).state, ((GElementFAStateDoubleCircle)state).state.name, s);
                    //changeDone();
                    repaint();
                }
            }
    	} else if (state instanceof GElementFAStateRectangle){
    		s = (String)JOptionPane.showInputDialog(null, Localized.getString("faEditStateMessage"), Localized.getString("faEditStateTitle"),
                    JOptionPane.QUESTION_MESSAGE, null, null, ((GElementFAStateRectangle)state).state.name);
    		Object[] possibilities = {"No Change", "Black","Blue", "Brown", "Gold", "Green", "DarkGreen", "LightBlue","Orange", "Pink", "Violate"};
    		s2 = (String)JOptionPane.showInputDialog(null, "Choose New Color", "Choose New Color", JOptionPane.PLAIN_MESSAGE, null, possibilities, 
    	    		state.getColor());
    		nickname = (String)JOptionPane.showInputDialog(null, "Modify state long name: ", "Modify state long name: ",
                    JOptionPane.QUESTION_MESSAGE, null, null, state.getNickname());
    		if (s == null)
    			s = ((GElementFAStateRectangle) state).state.name;
    		if (nickname == null)
    			nickname = state.getNickname();
    		state.setNickname(nickname);
    		Color c;
    		if(s2!=null && !s2.equals("No Change")) {
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
    			((GElementFAStateRectangle)state).setColor(c);
    			((GElementFAStateRectangle)state).setColor2(c);
    			((GElementFAStateRectangle)state).setColor3(c);
    			((GElementFAStateRectangle)state).getPosition().color = c;
    		}
    		
    		if(s != null && !(s.equals(((GElementFAStateRectangle) state).state.name))) {
                if(getMachine().getMachine().containsStateName(s))
                    XJAlert.display(parent.getJavaContainer(), Localized.getString("faEditStateTitle"), Localized.getString("faEditStateAlreadyExists"));
                else {
                    getMachine().getMachine().renameState(((GElementFAStateRectangle)state).state, ((GElementFAStateRectangle)state).state.name, s);
                    //changeDone();
                    repaint();
                }
            }
    	} else if (state instanceof GElementFAStateDoubleRectangle){
    		s = (String)JOptionPane.showInputDialog(null, Localized.getString("faEditStateMessage"), Localized.getString("faEditStateTitle"),
                    JOptionPane.QUESTION_MESSAGE, null, null, ((GElementFAStateDoubleRectangle)state).state.name);
    		Object[] possibilities = {"No Change", "Black","Blue", "Brown", "Gold", "Green", "DarkGreen", "LightBlue","Orange", "Pink", "Violate"};
    		s2 = (String)JOptionPane.showInputDialog(null, "Choose New Color", "Choose New Color", JOptionPane.PLAIN_MESSAGE, null, possibilities, 
    	    		state.getColor());
    		nickname = (String)JOptionPane.showInputDialog(null, "Modify state nickname: ", "Modify state nickname: ",
                    JOptionPane.QUESTION_MESSAGE, null, null, state.getNickname());
    		if (s == null)
    			s = ((GElementFAStateDoubleRectangle) state).state.name;
    		if (nickname == null)
    			nickname = state.getNickname();
    		state.setNickname(nickname);
    		Color c;
    		if(s2!=null && !s2.equals("No Change")) {
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
    			System.out.println(s2);
    			((GElementFAStateDoubleRectangle)state).setColor(c);
    			((GElementFAStateDoubleRectangle)state).setColor2(c);
    			((GElementFAStateDoubleRectangle)state).setColor3(c);
    			((GElementFAStateDoubleRectangle)state).getPosition().color = c;
    		}
    	
    		if(s != null && !(s.equals(((GElementFAStateDoubleRectangle) state).state.name))) {
                if(getMachine().getMachine().containsStateName(s))
                    XJAlert.display(parent.getJavaContainer(), Localized.getString("faEditStateTitle"), Localized.getString("faEditStateAlreadyExists"));
                else {
                    getMachine().getMachine().renameState(((GElementFAStateDoubleRectangle)state).state, ((GElementFAStateDoubleRectangle)state).state.name, s);
                    //changeDone();
                    repaint();
                }
            }
    	}
    	getMachine().getMachine().getNaming().addElement(state);
    	getMachine().getMachine().getNaming().repaint();
    	changeDone();
    }

    /**
     * ensures that a GElement is not contained by a sub-box
     * @param check - GElement being checked that it is not contained by a sub-box
     * @param largerBox - GElement containing check
     * @return false if check is contained in a sub-box, true if not contained by a sub-box
     */
    public boolean checkContained(GElement check, GElement largerBox){
    	System.out.println("checkContained... check: " + check + " ... largerBox: " + largerBox);
    	for (int i = 0; i < getMachine().getElements().size(); i++){
    		System.out.println("loop: " + i + " ... element: " + getMachine().getElements().get(i));
    		if (getMachine().getElements().get(i) != largerBox && getMachine().getElements().get(i) != check){
    			if (getMachine().getElements().get(i) instanceof GElementFAStateRectangle || getMachine().getElements().get(i) instanceof GElementFAStateDoubleRectangle){
    				// if check is contained by element
    				if (check.getPosition().x > getMachine().getElements().get(i).getPosition().x &&
    						check.getPosition().x < getMachine().getElements().get(i).getPosition().x2 &&
    						check.getPosition().y > getMachine().getElements().get(i).getPosition().y &&
    						check.getPosition().y < getMachine().getElements().get(i).getPosition().y2){
    					// if element is contained by largerBox
    					if (largerBox.getPosition().x < getMachine().getElements().get(i).getPosition().x &&
    							largerBox.getPosition().x2 > getMachine().getElements().get(i).getPosition().x2 &&
    							largerBox.getPosition().y < getMachine().getElements().get(i).getPosition().y &&
    							largerBox.getPosition().y2 > getMachine().getElements().get(i).getPosition().y2){
    						return false;
    					}
    				}
    			}
    		}
    	}
    	System.out.println("return true");
    	return true;
    }
    
    public void handleMenuEvent(XJMenu menu, XJMenuItem item) {
    	ArrayList<GElement> contained = new ArrayList<GElement>();
        switch(item.getTag()) {
            case MI_ADD_STATE:
                createStateAtXY(getLastMousePosition().getX(), getLastMousePosition().getY());
                break;

            case MI_REMOVE_STATE: {
                getMachine().removeState((GElement)item.getObject());
                getMachine().getMachine().getExport().removeGExport((GElement)item.getObject());
                changeDone();
                break;
            }

            case MI_COLLAPSE_STATE: {
            	GElement ge = (GElement)item.getObject();
            	if (ge.isCollapsed == false){
            		if (ge instanceof GElementFAStateRectangle){
            			//getMachine().getMachine().updateExport(getMachine().getElements());
            			getMachine().collapseState(ge);
            			changeDone();
            		}
            		else if (ge instanceof GElementFAStateDoubleRectangle){
            			//getMachine().getMachine().updateExport(getMachine().getElements());
            			getMachine().collapseState(ge);
            			changeDone();
            		}
            	}
            	else {
                	if (ge instanceof GElementFAStateRectangle){
                		//getMachine().getMachine().updateExport(getMachine().getElements());
                		getMachine().expandState(ge);
                		changeDone();
                	}
                	else if (ge instanceof GElementFAStateDoubleRectangle){
                		//getMachine().getMachine().updateExport(getMachine().getElements());
                		getMachine().expandState(ge);
                		changeDone();
                	}
            	}
            }		
            
            case MI_SET_START_STATE: {
            	GElement ge = (GElement)item.getObject();
            	if (ge instanceof GElementFAState){
            		GElementFAState state = (GElementFAState)item.getObject();
            		getMachine().toggleStartState(state);
            		changeDone();
            	} else if (ge instanceof GElementFAStateDoubleCircle){
            		GElementFAStateDoubleCircle state = (GElementFAStateDoubleCircle)item.getObject();
            		getMachine().toggleStartState(state);
            		changeDone();
            	}
                break;
            }

            case MI_SET_ACCEPTED_STATE: {
            	GElement ge = (GElement)item.getObject();
            	if (ge instanceof GElementFAState){
            		GElementFAState state = (GElementFAState)item.getObject();
            		state.toggleAccepted();
            		//getMachine().removeState(state);
            		//getMachine().changeStateToEndAtomic(state.state.name, state.getPosition().getX(), state.getPosition().getY(), state.color);
            		changeDone();
            	} else if (ge instanceof GElementFAStateDoubleCircle){
            		GElementFAStateDoubleCircle state = (GElementFAStateDoubleCircle)item.getObject();
            		state.toggleAccepted();
            		//getMachine().removeState(state);
            		//getMachine().changeStateToAtomic(state.state.name, state.getPosition().getX(), state.getPosition().getY(), state.color);
            		changeDone();
            	}
            	break;
            }

            case MI_CLEAR_ALL: {
                getMachine().clear();
                changeDone();
                break;
            }

            case MI_EDIT_LINK: {
                getMachine().editLink((GLink)item.getObject());
                changeDone();
                break;
            }

            case MI_REMOVE_LINK: {
                getMachine().removeLink((GLink)item.getObject());
                changeDone();
                break;
            }
                
            case MI_COLOR_BLACK: {
            	getMachine().getMachine().getNaming().removeElement((GElement)item.getObject());
            	if (state != null){
            		state.setColor(Color.BLACK);
            		state.setColor2(Color.BLACK);
            		state.setColor3(Color.BLACK);
            		state.getPosition().color = Color.BLACK;
            	} else if (state2 != null){
            		state2.setColor(Color.BLACK);
            		state2.setColor2(Color.BLACK);
            		state2.setColor3(Color.BLACK);
            		state2.getPosition().color = Color.BLACK;
            	} else if (state3 != null){
            		state3.setColor(Color.BLACK);
            		state3.setColor2(Color.BLACK);
            		state3.setColor3(Color.BLACK);
            		state3.getPosition().color = Color.BLACK;
            	} else if (state4 != null){
            		state4.setColor(Color.BLACK);
            		state4.setColor2(Color.BLACK);
            		state4.setColor3(Color.BLACK);
            		state4.getPosition().color = Color.BLACK;
            	} else
            		break;
            	getMachine().getMachine().getNaming().addElement((GElement)item.getObject());
            	getMachine().getMachine().getNaming().repaint();
            	changeDone();
            	break;
            }
            
           /* case MI_COLOR_RED: {
            	if (state != null){
            		state.setColor(Color.RED);
            		state.setColor2(Color.RED);
            		state.setColor3(Color.RED);
            		state.getPosition().color = Color.RED;
            	} else if (state2 != null){
            		state2.setColor(Color.RED);
            		state2.setColor2(Color.RED);
            		state2.setColor3(Color.RED);
            		state2.getPosition().color = Color.RED;
            	} else if (state3 != null){
            		state3.setColor(Color.RED);
            		state3.setColor2(Color.RED);
            		state3.setColor3(Color.RED);
            		state3.getPosition().color = Color.RED;
            	} else if (state4 != null){
            		state4.setColor(Color.RED);
            		state4.setColor2(Color.RED);
            		state4.setColor3(Color.RED);
            		state4.getPosition().color = Color.RED;
            	} else
            		break;
            	break;
            }*/
            
            case MI_COLOR_BLUE: {
            	getMachine().getMachine().getNaming().removeElement((GElement)item.getObject());
            	if (state != null){
            		state.setColor(Color.BLUE);
            		state.setColor2(Color.BLUE);
            		state.setColor3(Color.BLUE);
            		state.getPosition().color = Color.BLUE;
            	} else if (state2 != null){
            		state2.setColor(Color.BLUE);
            		state2.setColor2(Color.BLUE);
            		state2.setColor3(Color.BLUE);
            		state2.getPosition().color = Color.BLUE;
            	} else if (state3 != null){
            		state3.setColor(Color.BLUE);
            		state3.setColor2(Color.BLUE);
            		state3.setColor3(Color.BLUE);
            		state3.getPosition().color = Color.BLUE;
            	} else if (state4 != null){
            		state4.setColor(Color.BLUE);
            		state4.setColor2(Color.BLUE);
            		state4.setColor3(Color.BLUE);
            		state4.getPosition().color = Color.BLUE;
            	} else
            		break;
            	getMachine().getMachine().getNaming().addElement((GElement)item.getObject());
            	getMachine().getMachine().getNaming().repaint();
            	changeDone();
            	break;
            }
            
            case MI_COLOR_YELLOW: {
            	getMachine().getMachine().getNaming().removeElement((GElement)item.getObject());
            	if (state != null){
            		state.setColor(Color.YELLOW);
            		state.setColor2(Color.YELLOW);
            		state.setColor3(Color.YELLOW);
            		state.getPosition().color = Color.YELLOW;
            	} else if (state2 != null){
            		state2.setColor(Color.YELLOW);
            		state2.setColor2(Color.YELLOW);
            		state2.setColor3(Color.YELLOW);
            		state2.getPosition().color = Color.YELLOW;
            	} else if (state3 != null){
            		state3.setColor(Color.YELLOW);
            		state3.setColor2(Color.YELLOW);
            		state3.setColor3(Color.YELLOW);
            		state3.getPosition().color = Color.YELLOW;
            	} else if (state4 != null){
            		state4.setColor(Color.YELLOW);
            		state4.setColor2(Color.YELLOW);
            		state4.setColor3(Color.YELLOW);
            		state4.getPosition().color = Color.YELLOW;
            	} else
            		break;
            	getMachine().getMachine().getNaming().addElement((GElement)item.getObject());
            	getMachine().getMachine().getNaming().repaint();
            	changeDone();
            	break;
            }
            
            case MI_COLOR_BROWN: {
            	getMachine().getMachine().getNaming().removeElement((GElement)item.getObject());
            	Color brown = new Color(156, 93, 82);
            	if (state != null){
            		state.setColor(brown);
            		state.setColor2(brown);
            		state.setColor3(brown);
            		state.getPosition().color = brown;
            	} else if (state2 != null){
            		state2.setColor(brown);
            		state2.setColor2(brown);
            		state2.setColor3(brown);
            		state2.getPosition().color = brown;
            	} else if (state3 != null){
            		state3.setColor(brown);
            		state3.setColor2(brown);
            		state3.setColor3(brown);
            		state3.getPosition().color = brown;
            	} else if (state4 != null){
            		state4.setColor(brown);
            		state4.setColor2(brown);
            		state4.setColor3(brown);
            		state4.getPosition().color = brown;
            	} else
            		break;
            	getMachine().getMachine().getNaming().addElement((GElement)item.getObject());
            	getMachine().getMachine().getNaming().repaint();
            	changeDone();
            	break;
            }
            
            case MI_COLOR_GOLD: {
            	getMachine().getMachine().getNaming().removeElement((GElement)item.getObject());
            	Color gold = new Color(212,175,55);
            	if (state != null){
            		state.setColor(gold);
            		state.setColor2(gold);
            		state.setColor3(gold);
            		state.getPosition().color = gold;
            	} else if (state2 != null){
            		state2.setColor(gold);
            		state2.setColor2(gold);
            		state2.setColor3(gold);
            		state2.getPosition().color = gold;
            	} else if (state3 != null){
            		state3.setColor(gold);
            		state3.setColor2(gold);
            		state3.setColor3(gold);
            		state3.getPosition().color = gold;
            	} else if (state4 != null){
            		state4.setColor(gold);
            		state4.setColor2(gold);
            		state4.setColor3(gold);
            		state4.getPosition().color = gold;
            	} else
            		break;
            	getMachine().getMachine().getNaming().addElement((GElement)item.getObject());
            	getMachine().getMachine().getNaming().repaint();
            	changeDone();
            	break;
            }
            
            case MI_COLOR_DARKGREEN: {
            	getMachine().getMachine().getNaming().removeElement((GElement)item.getObject());
            	Color darkgreen = new Color(69,148,81);
            	if (state != null){
            		state.setColor(darkgreen);
            		state.setColor2(darkgreen);
            		state.setColor3(darkgreen);
            		state.getPosition().color = darkgreen;
            	} else if (state2 != null){
            		state2.setColor(darkgreen);
            		state2.setColor2(darkgreen);
            		state2.setColor3(darkgreen);
            		state2.getPosition().color = darkgreen;
            	} else if (state3 != null){
            		state3.setColor(darkgreen);
            		state3.setColor2(darkgreen);
            		state3.setColor3(darkgreen);
            		state3.getPosition().color = darkgreen;
            	} else if (state4 != null){
            		state4.setColor(darkgreen);
            		state4.setColor2(darkgreen);
            		state4.setColor3(darkgreen);
            		state4.getPosition().color = darkgreen;
            	} else
            		break;
            	getMachine().getMachine().getNaming().addElement((GElement)item.getObject());
            	getMachine().getMachine().getNaming().repaint();
            	changeDone();
            	break;
            }
            
            case MI_COLOR_LIGHTBLUE: {
            	getMachine().getMachine().getNaming().removeElement((GElement)item.getObject());
            	Color lightblue = new Color(29,231,190);
            	if (state != null){
            		state.setColor(lightblue);
            		state.setColor2(lightblue);
            		state.setColor3(lightblue);
            		state.getPosition().color = lightblue;
            	} else if (state2 != null){
            		state2.setColor(lightblue);
            		state2.setColor2(lightblue);
            		state2.setColor3(lightblue);
            		state2.getPosition().color = lightblue;
            	} else if (state3 != null){
            		state3.setColor(lightblue);
            		state3.setColor2(lightblue);
            		state3.setColor3(lightblue);
            		state3.getPosition().color = lightblue;
            	} else if (state4 != null){
            		state4.setColor(lightblue);
            		state4.setColor2(lightblue);
            		state4.setColor3(lightblue);
            		state4.getPosition().color = lightblue;
            	} else
            		break;
            	getMachine().getMachine().getNaming().addElement((GElement)item.getObject());
            	getMachine().getMachine().getNaming().repaint();
            	changeDone();
            	break;
            }
            
            case MI_COLOR_ORANGE: {
            	getMachine().getMachine().getNaming().removeElement((GElement)item.getObject());
            	Color orange = new Color(237,154,71);
            	if (state != null){
            		state.setColor(orange);
            		state.setColor2(orange);
            		state.setColor3(orange);
            		state.getPosition().color = orange;
            	} else if (state2 != null){
            		state2.setColor(orange);
            		state2.setColor2(orange);
            		state2.setColor3(orange);
            		state2.getPosition().color = orange;
            	} else if (state3 != null){
            		state3.setColor(orange);
            		state3.setColor2(orange);
            		state3.setColor3(orange);
            		state3.getPosition().color = orange;
            	} else if (state4 != null){
            		state4.setColor(orange);
            		state4.setColor2(orange);
            		state4.setColor3(orange);
            		state4.getPosition().color = orange;
            	} else
            		break;
            	getMachine().getMachine().getNaming().addElement((GElement)item.getObject());
            	getMachine().getMachine().getNaming().repaint();
            	changeDone();
            	break;
            }
            
            case MI_COLOR_PINK: {
            	getMachine().getMachine().getNaming().removeElement((GElement)item.getObject());
            	Color pink = new Color(216,70,219);
            	if (state != null){
            		state.setColor(pink);
            		state.setColor2(pink);
            		state.setColor3(pink);
            		state.getPosition().color = pink;
            	} else if (state2 != null){
            		state2.setColor(pink);
            		state2.setColor2(pink);
            		state2.setColor3(pink);
            		state2.getPosition().color = pink;
            	} else if (state3 != null){
            		state3.setColor(pink);
            		state3.setColor2(pink);
            		state3.setColor3(pink);
            		state3.getPosition().color = pink;
            	} else if (state4 != null){
            		state4.setColor(pink);
            		state4.setColor2(pink);
            		state4.setColor3(pink);
            		state4.getPosition().color = pink;
            	} else
            		break;
            	getMachine().getMachine().getNaming().addElement((GElement)item.getObject());
            	getMachine().getMachine().getNaming().repaint();
            	changeDone();
            	break;
            }
            
            case MI_COLOR_VIOLET: {
            	getMachine().getMachine().getNaming().removeElement((GElement)item.getObject());
            	Color violet = new Color(119,23,121);
            	if (state != null){
            		state.setColor(violet);
            		state.setColor2(violet);
            		state.setColor3(violet);
            		state.getPosition().color = violet;
            	} else if (state2 != null){
            		state2.setColor(violet);
            		state2.setColor2(violet);
            		state2.setColor3(violet);
            		state2.getPosition().color = violet;
            	} else if (state3 != null){
            		state3.setColor(violet);
            		state3.setColor2(violet);
            		state3.setColor3(violet);
            		state3.getPosition().color = violet;
            	} else if (state4 != null){
            		state4.setColor(violet);
            		state4.setColor2(violet);
            		state4.setColor3(violet);
            		state4.getPosition().color = violet;
            	} else
            		break;
            	getMachine().getMachine().getNaming().addElement((GElement)item.getObject());
            	getMachine().getMachine().getNaming().repaint();
            	changeDone();
            	break;
            }
            
            case MI_COLOR_GREEN: {
            	getMachine().getMachine().getNaming().removeElement((GElement)item.getObject());
            	if (state != null){
            		state.setColor(Color.GREEN);
            		state.setColor2(Color.GREEN);
            		state.setColor3(Color.GREEN);
            		state.getPosition().color = Color.GREEN;
            	} else if (state2 != null){
            		state2.setColor(Color.GREEN);
            		state2.setColor2(Color.GREEN);
            		state2.setColor3(Color.GREEN);
            		state2.getPosition().color = Color.GREEN;
            	} else if (state3 != null){
            		state3.setColor(Color.GREEN);
            		state3.setColor2(Color.GREEN);
            		state3.setColor3(Color.GREEN);
            		state3.getPosition().color = Color.GREEN;
            	} else if (state4 != null){
            		state4.setColor(Color.GREEN);
            		state4.setColor2(Color.GREEN);
            		state4.setColor3(Color.GREEN);
            		state4.getPosition().color = Color.GREEN;
            	} else
            		break;
            	getMachine().getMachine().getNaming().addElement((GElement)item.getObject());
            	getMachine().getMachine().getNaming().repaint();
            	changeDone();
            	break;
            }
            
            // turns all elements contained to the same color
            case MI_CONTAINS_COLOR_BLACK: {
            	getMachine().getMachine().getNaming().removeElement((GElement)item.getObject());
            	if (state != null){
            		state.setColor(Color.BLACK);
            		state.setColor2(Color.BLACK);
            		state.setColor3(Color.BLACK);
            		state.getPosition().color = Color.BLACK;
            		getMachine().getMachine().getNaming().addElement(state);
            	} else if (state2 != null){
            		state2.setColor(Color.BLACK);
            		state2.setColor2(Color.BLACK);
            		state2.setColor3(Color.BLACK);
            		state2.getPosition().color = Color.BLACK;
            		getMachine().getMachine().getNaming().addElement(state2);
            	} else if (state3 != null){
            		state3.setColor(Color.BLACK);
            		state3.setColor2(Color.BLACK);
            		state3.setColor3(Color.BLACK);
            		state3.getPosition().color = Color.BLACK;
            		contained.add(state3);
            		getMachine().getMachine().getNaming().addElement(state3);
            		for (int i = 0; i < getMachine().getElements().size(); i++){
                		if (getMachine().getElements().get(i) instanceof GElementFAState){
                			if (state3.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state3.getPositionX2() > getMachine().getElements().get(i).getPositionX()
                					&& state3.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state3.getPositionY2() > getMachine().getElements().get(i).getPositionY()){
                				if (checkContained(getMachine().getElements().get(i), state3) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAState)getMachine().getElements().get(i)).setColor(Color.BLACK);
                					((GElementFAState)getMachine().getElements().get(i)).setColor2(Color.BLACK);
                					((GElementFAState)getMachine().getElements().get(i)).setColor3(Color.BLACK);
                					((GElementFAState)getMachine().getElements().get(i)).color = Color.BLACK;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		if (getMachine().getElements().get(i) instanceof GElementFAStateDoubleCircle){
                			if (state3.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state3.getPositionX2() > getMachine().getElements().get(i).getPositionX()
                					&& state3.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state3.getPositionY2() > getMachine().getElements().get(i).getPositionY()){
                				if (checkContained(getMachine().getElements().get(i), state3) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).setColor(Color.BLACK);
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).setColor2(Color.BLACK);
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).setColor3(Color.BLACK);
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).color = Color.BLACK;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		/*
                		if (getMachine().getElements().get(i) instanceof GElementFAStateRectangle){
                			if (state3.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state3.getPositionX2() > getMachine().getElements().get(i).getPositionX2()
                					&& state3.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state3.getPositionY2() > getMachine().getElements().get(i).getPositionY2()){
                				if (checkContained(getMachine().getElements().get(i), state3) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).setColor(Color.BLACK);
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).setColor2(Color.BLACK);
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).setColor3(Color.BLACK);
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).color = Color.BLACK;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		if (getMachine().getElements().get(i) instanceof GElementFAStateDoubleRectangle){
                			if (state3.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state3.getPositionX2() > getMachine().getElements().get(i).getPositionX2()
                					&& state3.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state3.getPositionY2() > getMachine().getElements().get(i).getPositionY2()){
                				if (checkContained(getMachine().getElements().get(i), state3) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).setColor(Color.BLACK);
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).setColor2(Color.BLACK);
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).setColor3(Color.BLACK);
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).color = Color.BLACK;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		*/
            		}
            		for (int j = 0; j < getMachine().getElements().size(); j++){
            			if (getMachine().getElements().get(j) instanceof GLink){
            				for (int k = 0; k < contained.size(); k++){
            					if (((GLink)getMachine().getElements().get(j)).source == contained.get(k)){
            						((GLink)getMachine().getElements().get(j)).setColor(Color.BLACK);
            					}
            				}
            			}
            		}
            	} else if (state4 != null){
            		state4.setColor(Color.BLACK);
            		state4.setColor2(Color.BLACK);
            		state4.setColor3(Color.BLACK);
            		state4.getPosition().color = Color.BLACK;
            		contained.add(state4);
            		getMachine().getMachine().getNaming().addElement(state4);
            		for (int i = 0; i < getMachine().getElements().size(); i++){
                		if (getMachine().getElements().get(i) instanceof GElementFAState){
                			if (state4.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state4.getPositionX2() > getMachine().getElements().get(i).getPositionX()
                					&& state4.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state4.getPositionY2() > getMachine().getElements().get(i).getPositionY()){
                				if (checkContained(getMachine().getElements().get(i), state4) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAState)getMachine().getElements().get(i)).setColor(Color.BLACK);
                					((GElementFAState)getMachine().getElements().get(i)).setColor2(Color.BLACK);
                					((GElementFAState)getMachine().getElements().get(i)).setColor3(Color.BLACK);
                					((GElementFAState)getMachine().getElements().get(i)).color = Color.BLACK;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		if (getMachine().getElements().get(i) instanceof GElementFAStateDoubleCircle){
                			if (state4.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state4.getPositionX2() > getMachine().getElements().get(i).getPositionX()
                					&& state4.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state4.getPositionY2() > getMachine().getElements().get(i).getPositionY()){
                				if (checkContained(getMachine().getElements().get(i), state4) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).setColor(Color.BLACK);
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).setColor2(Color.BLACK);
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).setColor3(Color.BLACK);
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).color = Color.BLACK;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		/*
                		if (getMachine().getElements().get(i) instanceof GElementFAStateRectangle){
                			if (state4.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state4.getPositionX2() > getMachine().getElements().get(i).getPositionX2()
                					&& state4.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state4.getPositionY2() > getMachine().getElements().get(i).getPositionY2()){
                				if (checkContained(getMachine().getElements().get(i), state3) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).setColor(Color.BLACK);
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).setColor2(Color.BLACK);
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).setColor3(Color.BLACK);
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).color = Color.BLACK;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		if (getMachine().getElements().get(i) instanceof GElementFAStateDoubleRectangle){
                			if (state4.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state4.getPositionX2() > getMachine().getElements().get(i).getPositionX2()
                					&& state4.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state4.getPositionY2() > getMachine().getElements().get(i).getPositionY2()){
                				if (checkContained(getMachine().getElements().get(i), state3) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).setColor(Color.BLACK);
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).setColor2(Color.BLACK);
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).setColor3(Color.BLACK);
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).color = Color.BLACK;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		*/
            		}
            		for (int j = 0; j < getMachine().getElements().size(); j++){
            			if (getMachine().getElements().get(j) instanceof GLink){
            				for (int k = 0; k < contained.size(); k++){
            					if (((GLink)getMachine().getElements().get(j)).source == contained.get(k)){
            						((GLink)getMachine().getElements().get(j)).setColor(Color.BLACK);
            					}
            				}
            			}
            		}
            	} else
            		break;
            	getMachine().getMachine().getNaming().repaint();
            	changeDone();
            	break;
            }
            
            case MI_CONTAINS_COLOR_BLUE: {
            	getMachine().getMachine().getNaming().removeElement((GElement)item.getObject());
            	if (state != null){
            		state.setColor(Color.BLUE);
            		state.setColor2(Color.BLUE);
            		state.setColor3(Color.BLUE);
            		state.getPosition().color = Color.BLUE;
            		getMachine().getMachine().getNaming().addElement(state);
            	} else if (state2 != null){
            		state2.setColor(Color.BLUE);
            		state2.setColor2(Color.BLUE);
            		state2.setColor3(Color.BLUE);
            		state2.getPosition().color = Color.BLUE;
            		getMachine().getMachine().getNaming().addElement(state2);
            	} else if (state3 != null){
            		state3.setColor(Color.BLUE);
            		state3.setColor2(Color.BLUE);
            		state3.setColor3(Color.BLUE);
            		state3.getPosition().color = Color.BLUE;
            		contained.add(state3);
            		getMachine().getMachine().getNaming().addElement(state3);
            		for (int i = 0; i < getMachine().getElements().size(); i++){
                		if (getMachine().getElements().get(i) instanceof GElementFAState){
                			if (state3.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state3.getPositionX2() > getMachine().getElements().get(i).getPositionX()
                					&& state3.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state3.getPositionY2() > getMachine().getElements().get(i).getPositionY()){
                				if (checkContained(getMachine().getElements().get(i), state3) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAState)getMachine().getElements().get(i)).setColor(Color.BLUE);
                					((GElementFAState)getMachine().getElements().get(i)).setColor2(Color.BLUE);
                					((GElementFAState)getMachine().getElements().get(i)).setColor3(Color.BLUE);
                					((GElementFAState)getMachine().getElements().get(i)).color = Color.BLUE;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		if (getMachine().getElements().get(i) instanceof GElementFAStateDoubleCircle){
                			if (state3.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state3.getPositionX2() > getMachine().getElements().get(i).getPositionX()
                					&& state3.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state3.getPositionY2() > getMachine().getElements().get(i).getPositionY()){
                				if (checkContained(getMachine().getElements().get(i), state3) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).setColor(Color.BLUE);
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).setColor2(Color.BLUE);
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).setColor3(Color.BLUE);
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).color = Color.BLUE;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		/*
                		if (getMachine().getElements().get(i) instanceof GElementFAStateRectangle){
                			if (state3.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state3.getPositionX2() > getMachine().getElements().get(i).getPositionX2()
                					&& state3.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state3.getPositionY2() > getMachine().getElements().get(i).getPositionY2()){
                				if (checkContained(getMachine().getElements().get(i), state3) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).setColor(Color.BLUE);
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).setColor2(Color.BLUE);
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).setColor3(Color.BLUE);
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).color = Color.BLUE;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		if (getMachine().getElements().get(i) instanceof GElementFAStateDoubleRectangle){
                			if (state3.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state3.getPositionX2() > getMachine().getElements().get(i).getPositionX2()
                					&& state3.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state3.getPositionY2() > getMachine().getElements().get(i).getPositionY2()){
                				if (checkContained(getMachine().getElements().get(i), state3) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).setColor(Color.BLUE);
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).setColor2(Color.BLUE);
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).setColor3(Color.BLUE);
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).color = Color.BLUE;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		*/
            		}
            		for (int j = 0; j < getMachine().getElements().size(); j++){
            			if (getMachine().getElements().get(j) instanceof GLink){
            				for (int k = 0; k < contained.size(); k++){
            					if (((GLink)getMachine().getElements().get(j)).source == contained.get(k)){
            						((GLink)getMachine().getElements().get(j)).setColor(Color.BLUE);
            					}
            				}
            			}
            		}
            	} else if (state4 != null){
            		state4.setColor(Color.BLUE);
            		state4.setColor2(Color.BLUE);
            		state4.setColor3(Color.BLUE);
            		state4.getPosition().color = Color.BLUE;
            		contained.add(state4);
            		getMachine().getMachine().getNaming().addElement(state4);
            		for (int i = 0; i < getMachine().getElements().size(); i++){
                		if (getMachine().getElements().get(i) instanceof GElementFAState){
                			if (state4.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state4.getPositionX2() > getMachine().getElements().get(i).getPositionX()
                					&& state4.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state4.getPositionY2() > getMachine().getElements().get(i).getPositionY()){
                				if (checkContained(getMachine().getElements().get(i), state4) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAState)getMachine().getElements().get(i)).setColor(Color.BLUE);
                					((GElementFAState)getMachine().getElements().get(i)).setColor2(Color.BLUE);
                					((GElementFAState)getMachine().getElements().get(i)).setColor3(Color.BLUE);
                					((GElementFAState)getMachine().getElements().get(i)).color = Color.BLUE;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		if (getMachine().getElements().get(i) instanceof GElementFAStateDoubleCircle){
                			if (state4.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state4.getPositionX2() > getMachine().getElements().get(i).getPositionX()
                					&& state4.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state4.getPositionY2() > getMachine().getElements().get(i).getPositionY()){
                				if (checkContained(getMachine().getElements().get(i), state4) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).setColor(Color.BLUE);
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).setColor2(Color.BLUE);
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).setColor3(Color.BLUE);
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).color = Color.BLUE;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		/*
                		if (getMachine().getElements().get(i) instanceof GElementFAStateRectangle){
                			if (state4.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state4.getPositionX2() > getMachine().getElements().get(i).getPositionX2()
                					&& state4.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state4.getPositionY2() > getMachine().getElements().get(i).getPositionY2()){
                				if (checkContained(getMachine().getElements().get(i), state3) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).setColor(Color.BLUE);
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).setColor2(Color.BLUE);
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).setColor3(Color.BLUE);
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).color = Color.BLUE;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		if (getMachine().getElements().get(i) instanceof GElementFAStateDoubleRectangle){
                			if (state4.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state4.getPositionX2() > getMachine().getElements().get(i).getPositionX2()
                					&& state4.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state4.getPositionY2() > getMachine().getElements().get(i).getPositionY2()){
                				if (checkContained(getMachine().getElements().get(i), state3) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).setColor(Color.BLUE);
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).setColor2(Color.BLUE);
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).setColor3(Color.BLUE);
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).color = Color.BLUE;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		*/
            		}
            		for (int j = 0; j < getMachine().getElements().size(); j++){
            			if (getMachine().getElements().get(j) instanceof GLink){
            				for (int k = 0; k < contained.size(); k++){
            					if (((GLink)getMachine().getElements().get(j)).source == contained.get(k)){
            						((GLink)getMachine().getElements().get(j)).setColor(Color.BLUE);
            					}
            				}
            			}
            		}
            	} else
            		break;
            	getMachine().getMachine().getNaming().repaint();
            	changeDone();
            	break;
            }
            
            case MI_CONTAINS_COLOR_YELLOW: {
            	getMachine().getMachine().getNaming().removeElement((GElement)item.getObject());
            	if (state != null){
            		state.setColor(Color.YELLOW);
            		state.setColor2(Color.YELLOW);
            		state.setColor3(Color.YELLOW);
            		state.getPosition().color = Color.YELLOW;
            		getMachine().getMachine().getNaming().addElement(state);
            	} else if (state2 != null){
            		state2.setColor(Color.YELLOW);
            		state2.setColor2(Color.YELLOW);
            		state2.setColor3(Color.YELLOW);
            		state2.getPosition().color = Color.YELLOW;
            		getMachine().getMachine().getNaming().addElement(state2);
            	} else if (state3 != null){
            		state3.setColor(Color.YELLOW);
            		state3.setColor2(Color.YELLOW);
            		state3.setColor3(Color.YELLOW);
            		state3.getPosition().color = Color.YELLOW;
            		contained.add(state3);
            		getMachine().getMachine().getNaming().addElement(state3);
            		for (int i = 0; i < getMachine().getElements().size(); i++){
                		if (getMachine().getElements().get(i) instanceof GElementFAState){
                			if (state3.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state3.getPositionX2() > getMachine().getElements().get(i).getPositionX()
                					&& state3.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state3.getPositionY2() > getMachine().getElements().get(i).getPositionY()){
                				if (checkContained(getMachine().getElements().get(i), state3) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAState)getMachine().getElements().get(i)).setColor(Color.YELLOW);
                					((GElementFAState)getMachine().getElements().get(i)).setColor2(Color.YELLOW);
                					((GElementFAState)getMachine().getElements().get(i)).setColor3(Color.YELLOW);
                					((GElementFAState)getMachine().getElements().get(i)).color = Color.YELLOW;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		if (getMachine().getElements().get(i) instanceof GElementFAStateDoubleCircle){
                			if (state3.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state3.getPositionX2() > getMachine().getElements().get(i).getPositionX()
                					&& state3.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state3.getPositionY2() > getMachine().getElements().get(i).getPositionY()){
                				if (checkContained(getMachine().getElements().get(i), state3) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).setColor(Color.YELLOW);
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).setColor2(Color.YELLOW);
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).setColor3(Color.YELLOW);
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).color = Color.YELLOW;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		/*
                		if (getMachine().getElements().get(i) instanceof GElementFAStateRectangle){
                			if (state3.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state3.getPositionX2() > getMachine().getElements().get(i).getPositionX2()
                					&& state3.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state3.getPositionY2() > getMachine().getElements().get(i).getPositionY2()){
                				if (checkContained(getMachine().getElements().get(i), state3) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).setColor(Color.YELLOW);
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).setColor2(Color.YELLOW);
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).setColor3(Color.YELLOW);
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).color = Color.YELLOW;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		if (getMachine().getElements().get(i) instanceof GElementFAStateDoubleRectangle){
                			if (state3.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state3.getPositionX2() > getMachine().getElements().get(i).getPositionX2()
                					&& state3.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state3.getPositionY2() > getMachine().getElements().get(i).getPositionY2()){
                				if (checkContained(getMachine().getElements().get(i), state3) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).setColor(Color.YELLOW);
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).setColor2(Color.YELLOW);
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).setColor3(Color.YELLOW);
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).color = Color.YELLOW;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		*/
            		}
            		for (int j = 0; j < getMachine().getElements().size(); j++){
            			if (getMachine().getElements().get(j) instanceof GLink){
            				for (int k = 0; k < contained.size(); k++){
            					if (((GLink)getMachine().getElements().get(j)).source == contained.get(k)){
            						((GLink)getMachine().getElements().get(j)).setColor(Color.YELLOW);
            					}
            				}
            			}
            		}
            	} else if (state4 != null){
            		state4.setColor(Color.YELLOW);
            		state4.setColor2(Color.YELLOW);
            		state4.setColor3(Color.YELLOW);
            		state4.getPosition().color = Color.YELLOW;
            		contained.add(state4);
            		getMachine().getMachine().getNaming().addElement(state4);
            		for (int i = 0; i < getMachine().getElements().size(); i++){
                		if (getMachine().getElements().get(i) instanceof GElementFAState){
                			if (state4.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state4.getPositionX2() > getMachine().getElements().get(i).getPositionX()
                					&& state4.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state4.getPositionY2() > getMachine().getElements().get(i).getPositionY()){
                				if (checkContained(getMachine().getElements().get(i), state4) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAState)getMachine().getElements().get(i)).setColor(Color.YELLOW);
                					((GElementFAState)getMachine().getElements().get(i)).setColor2(Color.YELLOW);
                					((GElementFAState)getMachine().getElements().get(i)).setColor3(Color.YELLOW);
                					((GElementFAState)getMachine().getElements().get(i)).color = Color.YELLOW;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		if (getMachine().getElements().get(i) instanceof GElementFAStateDoubleCircle){
                			if (state4.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state4.getPositionX2() > getMachine().getElements().get(i).getPositionX()
                					&& state4.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state4.getPositionY2() > getMachine().getElements().get(i).getPositionY()){
                				if (checkContained(getMachine().getElements().get(i), state4) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).setColor(Color.YELLOW);
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).setColor2(Color.YELLOW);
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).setColor3(Color.YELLOW);
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).color = Color.YELLOW;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		/*
                		if (getMachine().getElements().get(i) instanceof GElementFAStateRectangle){
                			if (state4.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state4.getPositionX2() > getMachine().getElements().get(i).getPositionX2()
                					&& state4.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state4.getPositionY2() > getMachine().getElements().get(i).getPositionY2()){
                				if (checkContained(getMachine().getElements().get(i), state3) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).setColor(Color.YELLOW);
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).setColor2(Color.YELLOW);
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).setColor3(Color.YELLOW);
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).color = Color.YELLOW;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		if (getMachine().getElements().get(i) instanceof GElementFAStateDoubleRectangle){
                			if (state4.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state4.getPositionX2() > getMachine().getElements().get(i).getPositionX2()
                					&& state4.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state4.getPositionY2() > getMachine().getElements().get(i).getPositionY2()){
                				if (checkContained(getMachine().getElements().get(i), state3) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).setColor(Color.YELLOW);
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).setColor2(Color.YELLOW);
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).setColor3(Color.YELLOW);
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).color = Color.YELLOW;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		*/
            		}
            		for (int j = 0; j < getMachine().getElements().size(); j++){
            			if (getMachine().getElements().get(j) instanceof GLink){
            				for (int k = 0; k < contained.size(); k++){
            					if (((GLink)getMachine().getElements().get(j)).source == contained.get(k)){
            						((GLink)getMachine().getElements().get(j)).setColor(Color.YELLOW);
            					}
            				}
            			}
            		}
            	} else
            		break;
            	getMachine().getMachine().getNaming().repaint();
            	changeDone();
            	break;
            }
            
            case MI_CONTAINS_COLOR_GREEN: {
            	getMachine().getMachine().getNaming().removeElement((GElement)item.getObject());
            	if (state != null){
            		state.setColor(Color.GREEN);
            		state.setColor2(Color.GREEN);
            		state.setColor3(Color.GREEN);
            		state.getPosition().color = Color.GREEN;
            		getMachine().getMachine().getNaming().addElement(state);
            	} else if (state2 != null){
            		state2.setColor(Color.GREEN);
            		state2.setColor2(Color.GREEN);
            		state2.setColor3(Color.GREEN);
            		state2.getPosition().color = Color.GREEN;
            		getMachine().getMachine().getNaming().addElement(state2);
            	} else if (state3 != null){
            		state3.setColor(Color.GREEN);
            		state3.setColor2(Color.GREEN);
            		state3.setColor3(Color.GREEN);
            		state3.getPosition().color = Color.GREEN;
            		contained.add(state3);
            		getMachine().getMachine().getNaming().addElement(state3);
                	for (int i = 0; i < getMachine().getElements().size(); i++){
                		if (getMachine().getElements().get(i) instanceof GElementFAState){
                			if (state3.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state3.getPositionX2() > getMachine().getElements().get(i).getPositionX()
                					&& state3.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state3.getPositionY2() > getMachine().getElements().get(i).getPositionY()){
                				if (checkContained(getMachine().getElements().get(i), state3) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAState)getMachine().getElements().get(i)).setColor(Color.GREEN);
                					((GElementFAState)getMachine().getElements().get(i)).setColor2(Color.GREEN);
                					((GElementFAState)getMachine().getElements().get(i)).setColor3(Color.GREEN);
                					((GElementFAState)getMachine().getElements().get(i)).color = Color.GREEN;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		if (getMachine().getElements().get(i) instanceof GElementFAStateDoubleCircle){
                			if (state3.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state3.getPositionX2() > getMachine().getElements().get(i).getPositionX()
                					&& state3.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state3.getPositionY2() > getMachine().getElements().get(i).getPositionY()){
                				if (checkContained(getMachine().getElements().get(i), state3) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).setColor(Color.GREEN);
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).setColor2(Color.GREEN);
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).setColor3(Color.GREEN);
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).color = Color.GREEN;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		/*
                		if (getMachine().getElements().get(i) instanceof GElementFAStateRectangle){
                			if (state3.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state3.getPositionX2() > getMachine().getElements().get(i).getPositionX2()
                					&& state3.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state3.getPositionY2() > getMachine().getElements().get(i).getPositionY2()){
                				if (checkContained(getMachine().getElements().get(i), state3) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).setColor(Color.GREEN);
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).setColor2(Color.GREEN);
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).setColor3(Color.GREEN);
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).color = Color.GREEN;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		if (getMachine().getElements().get(i) instanceof GElementFAStateDoubleRectangle){
                			if (state3.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state3.getPositionX2() > getMachine().getElements().get(i).getPositionX2()
                					&& state3.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state3.getPositionY2() > getMachine().getElements().get(i).getPositionY2()){
                				if (checkContained(getMachine().getElements().get(i), state3) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).setColor(Color.GREEN);
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).setColor2(Color.GREEN);
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).setColor3(Color.GREEN);
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).color = Color.GREEN;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		*/
                	}
                	for (int j = 0; j < getMachine().getElements().size(); j++){
            			if (getMachine().getElements().get(j) instanceof GLink){
            				for (int k = 0; k < contained.size(); k++){
            					if (((GLink)getMachine().getElements().get(j)).source == contained.get(k)){
            						((GLink)getMachine().getElements().get(j)).setColor(Color.GREEN);
            					}
            				}
            			}
            		}
            	} else if (state4 != null){
            		state4.setColor(Color.GREEN);
            		state4.setColor2(Color.GREEN);
            		state4.setColor3(Color.GREEN);
            		state4.getPosition().color = Color.GREEN;
            		contained.add(state4);
            		getMachine().getMachine().getNaming().addElement(state4);
            		for (int i = 0; i < getMachine().getElements().size(); i++){
                		if (getMachine().getElements().get(i) instanceof GElementFAState){
                			if (state4.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state4.getPositionX2() > getMachine().getElements().get(i).getPositionX()
                					&& state4.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state4.getPositionY2() > getMachine().getElements().get(i).getPositionY()){
                				if (checkContained(getMachine().getElements().get(i), state4) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAState)getMachine().getElements().get(i)).setColor(Color.GREEN);
                					((GElementFAState)getMachine().getElements().get(i)).setColor2(Color.GREEN);
                					((GElementFAState)getMachine().getElements().get(i)).setColor3(Color.GREEN);
                					((GElementFAState)getMachine().getElements().get(i)).color = Color.GREEN;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		if (getMachine().getElements().get(i) instanceof GElementFAStateDoubleCircle){
                			if (state4.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state4.getPositionX2() > getMachine().getElements().get(i).getPositionX()
                					&& state4.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state4.getPositionY2() > getMachine().getElements().get(i).getPositionY()){
                				if (checkContained(getMachine().getElements().get(i), state4) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).setColor(Color.GREEN);
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).setColor2(Color.GREEN);
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).setColor3(Color.GREEN);
                					((GElementFAStateDoubleCircle)getMachine().getElements().get(i)).color = Color.GREEN;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		/*
                		if (getMachine().getElements().get(i) instanceof GElementFAStateRectangle){
                			if (state4.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state4.getPositionX2() > getMachine().getElements().get(i).getPositionX2()
                					&& state4.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state4.getPositionY2() > getMachine().getElements().get(i).getPositionY2()){
                				if (checkContained(getMachine().getElements().get(i), state3) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).setColor(Color.GREEN);
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).setColor2(Color.GREEN);
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).setColor3(Color.GREEN);
                					((GElementFAStateRectangle)getMachine().getElements().get(i)).color = Color.GREEN;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		if (getMachine().getElements().get(i) instanceof GElementFAStateDoubleRectangle){
                			if (state4.getPositionX() < getMachine().getElements().get(i).getPositionX() 
                					&& state4.getPositionX2() > getMachine().getElements().get(i).getPositionX2()
                					&& state4.getPositionY() < getMachine().getElements().get(i).getPositionY()
                					&& state4.getPositionY2() > getMachine().getElements().get(i).getPositionY2()){
                				if (checkContained(getMachine().getElements().get(i), state3) == true){
                					contained.add(getMachine().getElements().get(i));
                					getMachine().getMachine().getNaming().removeElement(getMachine().getElements().get(i));
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).setColor(Color.GREEN);
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).setColor2(Color.GREEN);
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).setColor3(Color.GREEN);
                					((GElementFAStateDoubleRectangle)getMachine().getElements().get(i)).color = Color.GREEN;
                					getMachine().getMachine().getNaming().addElement(getMachine().getElements().get(i));
                				}
                			}
                		}
                		*/
                	}
            		for (int j = 0; j < getMachine().getElements().size(); j++){
            			if (getMachine().getElements().get(j) instanceof GLink){
            				for (int k = 0; k < contained.size(); k++){
            					if (((GLink)getMachine().getElements().get(j)).source == contained.get(k)){
            						((GLink)getMachine().getElements().get(j)).setColor(Color.GREEN);
            					}
            				}
            			}
            		}
            	} else {
            		break;
            	}
            	getMachine().getMachine().getNaming().repaint();
            	changeDone();
            	break;
            }
        }
    }

    public void eventCreateElement(Point p, boolean doubleclick) {
        if(doubleclick)
            createStateAtXY(p.x, p.y);
        else if(designToolFA.getSelectedTool() != DesignToolsFA.TOOL_ARROW) {
            int tool = designToolFA.getSelectedTool(); // WHOOP WHOOP WHOOP
            if(tool == DesignToolsFA.TOOL_ARROW)
                return;

            if(tool == DesignToolsFA.TOOL_LINK)
                return;
            
            if(tool == DesignToolsFA.TOOL_STATE){
            	String pattern = designToolFA.popSelectedStatePattern(); // consumes selected state
            	if(pattern != null) {
            		controller = 0;
            		if(getMachine().getMachine().containsStateName(pattern))
                        XJAlert.display(parent.getJavaContainer(), Localized.getString("faNewStateTitle"), Localized.getString("faNewStateAlreadyExists"));
                    else {
                    	getMachine().addStateAtXY(pattern, p.x, p.y);
                    	changeDone();
                    	repaint();
                    }
            	}
            }
            
            if (tool == DesignToolsFA.TOOL_ENDATOMIC){
            	controller = 0;
            	String pattern = designToolFA.popSelectedStatePattern(); // consumes selected state
            	if (pattern != null){
            		if(getMachine().getMachine().containsStateName(pattern))
            			XJAlert.display(parent.getJavaContainer(), Localized.getString("faNewStateTitle"), Localized.getString("faNewStateAlreadyExists"));
            		else {
                		getMachine().addEndAtomicStateAtXY(pattern, p.x, p.y);
                		changeDone();
                		repaint();
                	}
            	}
            }
            
            if (tool == DesignToolsFA.TOOL_NONATOMIC){	
            	if (controller == 0){
            		getMachine().addNonAtomicStateAtXY(null, p.x, p.y, controller);
            		controller = 1;
            	}
            	else{
            		String pattern = designToolFA.popSelectedStatePattern(); // consumes selected state
            		if (pattern != null){
            			if(getMachine().getMachine().containsStateName(pattern)){
            				XJAlert.display(parent.getJavaContainer(), Localized.getString("faNewStateTitle"), Localized.getString("faNewStateAlreadyExists"));
            				controller = 0;
            			} else {
            				getMachine().addNonAtomicStateAtXY(pattern, p.x, p.y, controller);
                    		controller = 0;
                    		changeDone();
                    		repaint();
                    	}
                    }
            	}
            }
            
            if (tool == DesignToolsFA.TOOL_ENDNONATOMIC){	
            	if (controller2 == 0){
            		getMachine().addEndNonAtomicStateAtXY(null, p.x, p.y, controller2);
            		controller2 = 1;
            	}
            	else{
            		String pattern = designToolFA.popSelectedStatePattern(); // consumes selected state
            		if (pattern != null){
            			if(getMachine().getMachine().containsStateName(pattern)){
                            XJAlert.display(parent.getJavaContainer(), Localized.getString("faNewStateTitle"), Localized.getString("faNewStateAlreadyExists"));
                            controller = 0;
            			} else {
                        	getMachine().addEndNonAtomicStateAtXY(pattern, p.x, p.y, controller2);
                        	controller2 = 0;
                        	changeDone();
                        	repaint();
                        }
            		}
            	}
            }
            
        }

    }

    public boolean eventCanCreateLink() {
        int tool = designToolFA.getSelectedTool();
        if(tool == DesignToolsFA.TOOL_LINK) {
            designToolFA.consumeSelectedState();
            return true;
        } else
            return false;
    }

    /**
     * begins the process of creating the links
     * must use correct createLink version depending on target and source type b/c the type, GElement, cannot be used in this case
     */
    public void eventCreateLink(GElement source, String sourceAnchorKey, GElement target, String targetAnchorKey, int shape, Point p) {
    	System.out.println("GViewFAMachine eventCreateLink method started"); //TODO happens after you click source and target
    	// Double Circle to DoubleCircle
    	if (source instanceof GElementFAStateDoubleCircle && target instanceof GElementFAStateDoubleCircle){
    		getMachine().createLinkEndToEnd((GElementFAStateDoubleCircle)source, sourceAnchorKey, (GElementFAStateDoubleCircle)target, targetAnchorKey, shape, p);
    		changeDone();
    		System.out.println("GViewFAMachine eventCreateLink double circle to double circle");//TODO
    	} // Double Circle to Circle
    	if (source instanceof GElementFAStateDoubleCircle && target instanceof GElementFAState){
    		getMachine().createLinkEndToState((GElementFAStateDoubleCircle)source, sourceAnchorKey, (GElementFAState)target, targetAnchorKey, shape, p);
    		changeDone();
    	} // Circle to Double Circle
    	if (source instanceof GElementFAState && target instanceof GElementFAStateDoubleCircle){
    		getMachine().createLinkStateToEnd((GElementFAState)source, sourceAnchorKey, (GElementFAStateDoubleCircle)target, targetAnchorKey, shape, p);
    		changeDone();
    	} // Circle to Circle
    	if (source instanceof GElementFAState && target instanceof GElementFAState){
    		getMachine().createLink((GElementFAState)source, sourceAnchorKey, (GElementFAState)target, targetAnchorKey, shape, p);
    		changeDone();
    		System.out.println("GViewFAMachine eventCreateLink circle to circle");//TODO happens after you enter link name
    	} // Rectangle to State
    	if (source instanceof GElementFAStateRectangle && target instanceof GElementFAState) {
    		getMachine().createLinkRectangleToState((GElementFAStateRectangle)source, sourceAnchorKey, (GElementFAState)target, targetAnchorKey, shape, p); 
    		changeDone();
    	}
    	// Double Rectangle to State
    	if (source instanceof GElementFAStateDoubleRectangle && target instanceof GElementFAState) {
    		getMachine().createLinkRectangleToRectangle((GElementFAStateDoubleRectangle)source, sourceAnchorKey, (GElementFAState)target, targetAnchorKey, shape, p); 
    		changeDone();
    	}
    	// Rectangle to Double Circle
    	if (source instanceof GElementFAStateRectangle && target instanceof GElementFAStateDoubleCircle) {
    		getMachine().createLinkRectangleToRectangle((GElementFAStateRectangle)source, sourceAnchorKey, (GElementFAStateDoubleCircle)target, targetAnchorKey, shape, p); 
    		changeDone();
    	}
    	// Double Rectangle to Double Circle
    	if (source instanceof GElementFAStateDoubleRectangle && target instanceof GElementFAStateDoubleCircle) {
    		getMachine().createLinkRectangleToRectangle((GElementFAStateDoubleRectangle)source, sourceAnchorKey, (GElementFAStateDoubleCircle)target, targetAnchorKey, shape, p); 
    		changeDone();
    	}
    	// Rectangle to Rectangle (itself)
    	/*if (source instanceof GElementFAStateRectangle && target instanceof GElementFAStateRectangle) {
    		getMachine().createLinkRectangleToRectangle((GElementFAStateRectangle)source, sourceAnchorKey, (GElementFAStateRectangle)target, targetAnchorKey, shape, p); 
    		changeDone();
    	}
    	// Double Rectangle to Double Rectangle (itself)
    	if (source instanceof GElementFAStateDoubleRectangle && target instanceof GElementFAStateDoubleRectangle) {
    		getMachine().createLinkRectangleToRectangle((GElementFAStateDoubleRectangle)source, sourceAnchorKey, (GElementFAStateDoubleRectangle)target, targetAnchorKey, shape, p); 
    		changeDone();
    	}*/
    }

    public void eventEditElement(GElement e) {
        if(e instanceof GLink) {
            if(getMachine().editLink((GLink)e)) {
                changeDone();
                repaint();
            }
        } else if(e instanceof GElementFAState || e instanceof GElementFAStateDoubleCircle
        		|| e instanceof GElementFAStateRectangle || e instanceof GElementFAStateDoubleRectangle) {
            editState((GElement)e);
        }
    }

}
