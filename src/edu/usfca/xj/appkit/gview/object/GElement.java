/*

[The "BSD licence"]
Copyright (c) 2005 Jean Bovet
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

package edu.usfca.xj.appkit.gview.object;

import edu.usfca.vas.graphics.fa.GElementFAMachine;
import edu.usfca.xj.appkit.gview.GView;
import edu.usfca.xj.appkit.gview.base.Anchor2D;
import edu.usfca.xj.appkit.gview.base.Rect;
import edu.usfca.xj.appkit.gview.base.Vector2D;
import edu.usfca.xj.foundation.XJXMLSerializable;
import query.Query;

import java.awt.*;
import java.util.*;
import java.util.List;
import query.Query;

import connection.Step;

public abstract class GElement implements XJXMLSerializable {

    public static final String ANCHOR_CENTER = "CENTER";
    public static final String ANCHOR_TOP = "TOP";
    public static final String ANCHOR_BOTTOM = "BOTTOM";
    public static final String ANCHOR_LEFT = "LEFT";
    public static final String ANCHOR_RIGHT = "RIGHT";

    protected transient GView view = null;
    protected List<GElement> elements = new ArrayList<GElement>(); // shown elements
    public ArrayList<GElement> collapsed = new ArrayList<GElement>(); // hidden elements
    public ArrayList<GElement> appearWhenExpand = new ArrayList<GElement>(); // hidden elements

    protected Vector2D position = new Vector2D(); // position
    protected transient Vector2D oldPosition = null;
    public Vector2D collapsedPositions = new Vector2D(); // used for holding old position when collapsing
    public boolean isCollapsed = false;

    protected transient Map anchors = new HashMap();

    protected String label = null;
    protected String nickname = null;
    protected Color labelColor = Color.black;
    protected boolean labelVisible = true;

	protected transient boolean selected = false;
    protected transient boolean focused = false;

    protected transient Color color = Color.black;
    protected transient int penSize = 1;

    protected transient BasicStroke strokeSize = new BasicStroke(penSize);
    protected transient BasicStroke strokeNormal = new BasicStroke(1);
    protected transient BasicStroke strokeBold = new BasicStroke(3);

    protected boolean draggable = false;

    protected final Object lock = new Object();
    
    protected boolean highlight = false;
    
    protected LinkedList<Query> queries = new LinkedList();

	public GElement () {
    }
    
    // prints all shown elements
    public void printElements(){
    	System.out.println("Printing Elements");
    	for (int i = 0; i < elements.size(); i++){
    		System.out.println(elements.get(i));
    	}
    	System.out.println("done printing");
    }
    
    public void setPanel(GView view) {
        this.view = view;
        synchronized(lock) {
            for (int i = 0; i < elements.size(); i++) {
                GElement element = (GElement) elements.get(i);
                element.setPanel(view);
            }
        }
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isLabelEqualsTo(String otherLabel) {
        if(label == null)
            return otherLabel == null;
        else
            return label.equals(otherLabel);
    }

    public void setLabelColor(Color color) {
        this.labelColor = color;
    }

    public Color getLabelColor() {
        return labelColor;
    }

    public void setLabelVisible(boolean flag) {
        this.labelVisible = flag;
    }

    public boolean isLabelVisible() {
        return labelVisible;
    }

    public void setPosition2(Vector2D newPosition){
    	position = newPosition;
    }
    
    public void setCollapsedPosition(Vector2D pos){
    	collapsedPositions = pos;
    }
    
    public void setPosition(double x, double y) {
        // This is the position of the center of the element
        position.setX(x);
        position.setY(y);
        elementPositionDidChange();
    }
    
    public void setPosition2(double x, double y){
    	position.setX2(x);
        position.setY2(y);
        elementPositionDidChange();
    }

    public double getPositionX() {
        return position.getX();
    }

    public double getPositionY() {
        return position.getY();
    }
    
    public double getPositionX2(){
    	return position.getX2();
    }
    
    public double getPositionY2(){
    	return position.getY2();
    }

    public void swapPositions(double newX, double newY, double newX2, double newY2){
    	this.position.x = newX;
    	this.position.y = newY;
    	this.position.y2 = newY2;
    	this.position.x2 = newX2;
    }
    
    public void setPosition(Vector2D position) {
        this.position = position;
        elementPositionDidChange();
    }

    public Vector2D getPosition() {
        return position;
    }

    public void setElements(List<GElement> elements) {
        this.elements = elements;
    }

    public List<GElement> getElements() {
        return elements;
    }

    // adds element to shown array
	public void addElement(GElement element) {
        element.setPanel(view);
        synchronized(lock) {
            elements.add(element);
        }
    }

	// removes element from shown array
    public void removeElement(GElement element) {
        synchronized(lock) {
            elements.remove(element);
        }
    }

    public GElement getFirstElement() {
        if(elements == null || elements.isEmpty())
            return null;
        else
            return (GElement) elements.get(0);
    }

    public GElement getLastElement() {
        if(elements == null || elements.isEmpty())
            return null;
        else
            return (GElement) elements.get(elements.size()-1);
    }

    public GElement findElementWithLabel(String label) {
        if(isLabelEqualsTo(label))
            return this;

        if(elements == null)
            return null;

        for(int index=0; index<elements.size(); index++) {
            GElement element = (GElement)elements.get(index);
            if(element.isLabelEqualsTo(label))
                return element;
            else {
                element = element.findElementWithLabel(label);
                if(element != null)
                    return element;
            }
        }

        return null;
    }

    public void updateAnchors() {
    }

    public void setAnchor(String key, Vector2D position, Vector2D direction) {
        Anchor2D anchor = getAnchor(key);
        if(anchor == null) {
            anchor = new Anchor2D();
            anchors.put(key, anchor);
        }
        anchor.setPosition(position);
        anchor.setDirection(direction);
    }

    public double getDefaultAnchorOffset(String anchorKey) {
        return 0;
    }

    public Anchor2D getAnchor(String key) {
        return (Anchor2D)anchors.get(key);
    }

    public String getAnchorKeyClosestToPoint(Point p) {
    	return "CENTER";
        /*Anchor2D anchor = getAnchorClosestToPoint(p);
        for (Iterator iterator = anchors.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            if(anchors.get(key) == anchor)
                return key;
        }
        return null;*/
    }

    public Anchor2D getAnchorClosestToPoint(Point p) {
        double smallest_distance = Integer.MAX_VALUE;
        Anchor2D closest_anchor = null;

        Iterator iterator = anchors.values().iterator();
        while(iterator.hasNext()) {
            Anchor2D anchor = (Anchor2D)iterator.next();
            double dx = anchor.position.getX()-p.x;
            double dy = anchor.position.getY()-p.y;
            double d = Math.sqrt(dx*dx+dy*dy);
            if(d<smallest_distance) {
                smallest_distance = d;
                closest_anchor = anchor;
            }
        }

        return closest_anchor;
    }

    public Rect bounds() {
        Rect r = getFrame();
        synchronized(lock) {
            for (int i = 0; i < elements.size(); i++) {
                GElement element = (GElement) elements.get(i);
                if(element == this)
                    continue;

                if(r == null)
                    r = element.bounds();
                else
                    r = r.union(element.bounds());
            }
        }
        return r;
    }

    public Rect getFrame() {
        return null;
    }

    public void setFocused(boolean flag) {
        focused = flag;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setSelected(boolean flag) {
        selected = flag;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean acceptIncomingLink() {
        return false;
    }

    public boolean acceptOutgoingLink() {
        return false;
    }

    public void setDraggable(boolean flag) {
        this.draggable = flag;
    }

    public boolean isDraggable() {
        return draggable;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public Color getColor(){
    	return this.color;
    }

    public void setPenSize(int size) {
        penSize = size;
        strokeSize = new BasicStroke(penSize);
    }

    public boolean isInside(Point p) {
        return false;
    }

    /**
     * moves an object
     * @param dx - amount to shift in x direction
     * @param dy - amount to shift in y direction
     */
    public void move(double dx, double dy, Point p) {
    	if (position.x2 == 0 && position.y2 == 0){
    		position.shift(dx, dy);
    		// Recursively move every other children objects
    		synchronized(lock) {
    			for (int i = 0; i < elements.size(); i++) {
    				GElement element = (GElement) elements.get(i);
    				element.move(dx, dy, p);
    			}
    		}
    		elementPositionDidChange();
    	}
    	else {
    		if (p != null){
    			if ((p.x - position.x) >= 50 || (position.x2 - p.x) >= 50){
    				if (Math.abs(position.x2 - p.x) < Math.abs(position.x - p.x)){
    					// in 1st or 4th quadrant
    					if (Math.abs(position.y - p.y) < Math.abs(position.y2 - p.y)){
    						// in 1st quadrant
    						position.x2 = p.x;
    					}
    					else {
    						// in 4th quadrant
    						position.x2 = p.x;
    					}
    				}
    				else {
    					// in 2nd or 3rd quadrant
    					if (Math.abs(position.y - p.y) > Math.abs(position.y2 - p.y)){
    						// in 3rd quadrant
    						position.x = p.x;
    					}
    					else {
    						// in 2nd quadrant
    						position.x = p.x;
    					}
    				}
    				//position.shift(dx, 0);
    				// Recursively move every other children objects
    				synchronized(lock) {
    					for (int i = 0; i < elements.size(); i++) {
    						GElement element = (GElement) elements.get(i);
    						element.move(dx, dy, p);
    					}
    				}
    				elementPositionDidChange();
    			}
    			if ((p.y - position.y) >= 50 || (position.y2 - p.y) >= 50){
    				if (Math.abs(position.x2 - p.x) < Math.abs(position.x - p.x)){
    					// in 1st or 4th quadrant
    					if (Math.abs(position.y - p.y) < Math.abs(position.y2 - p.y)){
    						// in 1st quadrant
    						position.y = p.y;
    					}
    					else {
    						// in 4th quadrant
    						position.y2 = p.y;
    					}
    				}
    				else {
    					// in 2nd or 3rd quadrant
    					if (Math.abs(position.y - p.y) > Math.abs(position.y2 - p.y)){
    						// in 3rd quadrant
    						position.y2 = p.y;
    					}
    					else {
    						// in 2nd quadrant
    						position.y = p.y;
    					}
    				}
    				//position.shift(0, dy);
    				// Recursively move every other children objects
    				synchronized(lock) {
    					for (int i = 0; i < elements.size(); i++) {
    						GElement element = (GElement) elements.get(i);
    						element.move(dx, dy, p);
    					}
    				}
    				elementPositionDidChange();
    			}
    		}
    	}
    }

    public void moveToPosition(Vector2D position, Point p) {
        double dx = position.x-getPosition().x;
        double dy = position.y-getPosition().y;
        move(dx, dy, p);
    }

    /**
     * finds element that mouse is over
     * @param p
     * @return
     */
    public GElement match(Point p) {
        synchronized(lock) {
            for (int i = 0; i < elements.size(); i++) {
                GElement element = (GElement)elements.get(i);
                GElement match = element.match(p);
                if(match != null){
                    return match;
                }
            }            
        }

        if(isInside(p)){
            return this;
        }
        else {
            return null;
        }
    }

    public void beginDrag() {
        oldPosition = null;
    }

    public Vector2D dragElementPosition(Vector2D p) {
        Vector2D ep = p.copy();
        if(oldPosition != null) {
            ep.x += p.x-oldPosition.x;
            ep.y += p.y-oldPosition.y;
        }
        return ep;
    }

    public void drag(Vector2D p) {
        double dx = 0;
        double dy = 0;

        if(oldPosition == null) {
            oldPosition = new Vector2D();
        }   else {
            dx = p.x-oldPosition.x;
            dy = p.y-oldPosition.y;
        }

        oldPosition.x = p.x;
        oldPosition.y = p.y;

        move(dx, dy, null);
    }

    public void drawRecursive(Graphics2D g) {
        synchronized(lock) {
            for (int i = 0; i < elements.size(); i++) {
                GElement element = (GElement) elements.get(i);
                element.drawRecursive(g);
            }
        }

        draw(g);
        if(isSelected()){
            drawSelected(g);
        }
        else if(isFocused()){
            drawFocused(g);
        }
    }

    public void draw(Graphics2D g) {

    }

    public void drawShape(Graphics2D g) {

    }

    private void drawSelected(Graphics2D g) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, view.getSelectionAlphaValue()));
        g.setColor(Color.gray);
        g.setStroke(strokeBold);

        drawShape(g);

        g.setStroke(strokeNormal);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f ));
    }

    private void drawFocused(Graphics2D g) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, view.getFocusAlphaValue()));
        g.setColor(Color.blue);
        g.setStroke(strokeBold);

        drawShape(g);

        g.setStroke(strokeNormal);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f ));
    }

    // *** Notifications

    public void elementPositionDidChange() {
        updateAnchors();
    }

    public void elementDimensionDidChange() {
        updateAnchors();
    }

    /**
     * Method invoked when the element has been loaded from disk
     */
    public void elementDidLoad() {
        synchronized(lock) {
            for (int i = 0; i < elements.size(); i++) {
                GElement element = (GElement) elements.get(i);
                element.elementDidLoad();
            }
        }

        // Update the anchors
        updateAnchors();
    }

    public String toString() {
        return getClass().getName()+": "+position.x+"/"+position.y;
    }
    
    public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public void setAllLinkMachine(GElementFAMachine machine) {
		for(GElement e: elements) {
			if(e instanceof GLink) {
				((GLink) e).setMachine(machine);
			}
			if(e instanceof GElementCircle) {
				((GElementCircle) e).setMachine(machine);
			}
			if(e instanceof GElementDoubleCircle) {
				((GElementDoubleCircle) e).setMachine(machine);
			}
		}
	}

	public abstract boolean isInside(GElement e);
	
	public abstract int maxCoorX();
	
	public abstract int maxCoorY();

	public void setHighLight(boolean b) {
		highlight = b;
	}

    public boolean isHighlight() {
		return highlight;
	}
    
    /* Runs a list of queries associated with that particular element */
    public boolean runQuery() {
    	System.out.println("GElement runQuery()");
    	System.out.println("GElement runQuery(), Size of the queries: "+queries.size());
    	for(Query query: queries){
    		try {
    			System.out.println("GElement runQuery(), About to run query: "+query.queryInfo());
				query.evaluate();
			} catch (Exception e) {
				System.out.println("Query did not run");
				e.printStackTrace();
			}
    	}
    	return true;
    }
    
    public void addQuery(Query query){
    	queries.add(query);
    }
    
    /*
    public void addQueries(GElement findState, LinkedList<Query> updatedQueries) {
		// TODO Auto-generated method stub
		for(Query query: updatedQueries)
		{
			queries.add(query);
		}
	}
	*/
    
    public void addQueries(LinkedList<Query> updatedQueries){
    	queries.clear();
    	for(Query query: updatedQueries)
		{
			queries.add(query);
		}
    }

    public void runQueries(){}
    public ArrayList<Step> grabStepList(){return null;}
    public GElement getParentState(){return null;}
}
