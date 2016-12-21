package edu.usfca.vas.graphics.fa;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import edu.usfca.xj.appkit.gview.object.GElement;
import edu.usfca.xj.appkit.gview.object.GElementDoubleCircle;
import edu.usfca.xj.appkit.gview.object.GElementDoubleRectangle;
import edu.usfca.xj.appkit.gview.object.GElementRectangle;
import edu.usfca.xj.foundation.XJXMLSerializable;
import edu.usfca.vas.machine.fa.FAState;
import edu.usfca.vas.machine.fa.State;
import edu.usfca.xj.appkit.gview.base.Anchor2D;
import edu.usfca.xj.appkit.gview.base.Vector2D;
import edu.usfca.xj.appkit.gview.shape.SArrow;

public class GElementFAStateDoubleCircle extends GElementDoubleCircle 
	implements GElementFAStateInterface, XJXMLSerializable {

	protected State state = new FAState();
	protected Color color = Color.BLACK;
	
	protected transient SArrow startArrow = new SArrow();
    protected transient Vector2D startArrowDirection = new Vector2D(-1, 0);
    public boolean highlighted = false;
    
    public GElementFAStateDoubleCircle() {
    	super();
        setDraggable(true);
    }
    
    public GElementFAStateDoubleCircle(GElementFAMachine machine) {
    	super(machine);
        setDraggable(true);
    }
    
    public GElementFAStateDoubleCircle(State state, double x, double y,GElementFAMachine machine) {
    	super(machine);
        setState(state);
        setPosition(x, y);
        setDraggable(true);
        toggleAccepted();
    }

    public void setColor2(Color color){
    	this.color = color;
    }
    
    public void setState(State state) {
        this.state = state;
    }

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
        
        g.drawOval(x, y, (int)(getRadius()*2), (int)(getRadius()*2));
        
        if(state.accepted){
            g.drawOval(x + 4, y + 4, (int)(getRadius2() *2), (int)(getRadius2()*2));
        }
        if(highlighted == true){
        	Stroke previousStroke = g.getStroke();
        	g.setStroke(new BasicStroke(3.0f));//2 pixel width
        	g.drawOval(x, y, (int)(getRadius() *2), (int)(getRadius()*2));
        	g.drawOval(x + 4, y + 4, (int)(getRadius2() *2), (int)(getRadius2()*2));
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
