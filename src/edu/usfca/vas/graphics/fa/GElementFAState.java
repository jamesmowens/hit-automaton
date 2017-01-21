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

import edu.usfca.vas.machine.fa.FAState;
import edu.usfca.vas.machine.fa.State;
import edu.usfca.xj.appkit.gview.base.Anchor2D;
import edu.usfca.xj.appkit.gview.base.Vector2D;
import edu.usfca.xj.appkit.gview.object.GElement;
import edu.usfca.xj.appkit.gview.object.GElementCircle;
import edu.usfca.xj.appkit.gview.object.GElementDoubleRectangle;
import edu.usfca.xj.appkit.gview.object.GElementRectangle;
import edu.usfca.xj.appkit.gview.shape.SArrow;
import edu.usfca.xj.foundation.XJXMLSerializable;
import java.awt.*;
import query.Query;
import java.util.LinkedList;



public class GElementFAState extends GElementCircle implements XJXMLSerializable, GElementFAStateInterface {

    protected State state = new FAState();
    protected Color color = Color.BLACK;
    
    protected transient SArrow startArrow = new SArrow();
    protected transient Vector2D startArrowDirection = new Vector2D(-1, 0);
    public boolean highlighted = false;
    
    public GElementFAState() {
    	super();
        setDraggable(true);
    }    
    
    public GElementFAState(GElementFAMachine machine) {
    	super(machine);
        setDraggable(true);
    }
    
    public GElementFAState(State state, double x, double y,GElementFAMachine machine) {
    	super(machine);
        setState(state);
        setPosition(x, y);
        setDraggable(true);
        setAccepted(false);
    }
    
    public void setColor2(Color color){
    	this.color = color;
    }
    
    public void setState(State state) {
        this.state = state;
    }

    /* (non-Javadoc)
	 * @see edu.usfca.vas.graphics.fa.GElementFAStateInterface#getState()
	 */
    @Override
	public State getState() {
        return state;
    }

    public void setStart(boolean start) {
        state.start = start;
    }

    public boolean isStart() {
        return state.start;
    }

    public void toggleStart() {
        state.start = !state.start;
    }

    public void setAccepted(boolean accepted) {
        state.accepted = accepted;
    }

    public void toggleAccepted() {
        state.accepted = !state.accepted;
    }

    public String getLabel() {
        return state.name;
    }

    public boolean acceptIncomingLink() {
        return true;
    }

    public boolean acceptOutgoingLink() {
        return true;
    }

    public void updateAnchors() {
        setAnchor(ANCHOR_CENTER, position, Anchor2D.DIRECTION_FREE);
    }

    public void drawShape(Graphics2D g) {
        super.drawShape(g);

        color = getPosition().color; 
        
        int x = (int)(getPositionX()-getRadius());
        int y = (int)(getPositionY()-getRadius());

        if(state.start) {
            startArrow.setAnchor(x, y+getRadius());
            startArrow.setDirection(startArrowDirection);
            startArrow.setLength(20);
            startArrow.setAngle(30);
            startArrow.draw(g);
        }
        
        g.drawOval(x, y, (int)(getRadius()*2), (int)(getRadius()*2)); //draw outer circle
        if(state.accepted){
        	g.drawOval(x + 4, y + 4, (int)(getRadius2() *2), (int)(getRadius2()*2));  //draw inner circle
        }
        if(highlighted == true){
        	System.out.println("GElementFAState drawShape highlighted==true");
        	Stroke previousStroke = g.getStroke();
        	g.setStroke(new BasicStroke(3.0f));//2 pixel width
        	g.drawOval(x, y, (int)(getRadius() *2), (int)(getRadius()*2));
        	g.setStroke(previousStroke);
        }
    }

	@Override
	public int maxCoorX() {
		return (int) (getPositionX()+radius);
	}

	@Override
	public int maxCoorY() {
		return (int) (getPositionY()+radius);
	}
}
