package edu.usfca.vas.graphics.fa;

import edu.usfca.xj.appkit.gview.object.GElement;
import edu.usfca.xj.appkit.gview.object.GElementDoubleRectangle;
import edu.usfca.xj.appkit.gview.object.GElementRectangle;
import edu.usfca.xj.foundation.XJXMLSerializable;
import edu.usfca.vas.machine.fa.FAState;
import edu.usfca.vas.machine.fa.State;
import edu.usfca.xj.appkit.gview.base.Anchor2D;
import edu.usfca.xj.appkit.gview.base.Vector2D;
import edu.usfca.xj.appkit.gview.shape.SArrow;
import java.awt.*;

public class GElementFAStateDoubleRectangle extends GElementDoubleRectangle 
	implements GElementFAStateInterface, XJXMLSerializable {

	protected State state = new FAState();
	protected Color color = Color.BLACK;
	public boolean collapsed = false;
	
	protected transient SArrow startArrow = new SArrow();
    protected transient Vector2D startArrowDirection = new Vector2D(-1, 0);
    public boolean highlighted = false;

    public GElementFAStateDoubleRectangle() {
        setDraggable(true);
    }
    
    public GElementFAStateDoubleRectangle(GElementFAMachine machine) {
    	super(machine);
        setDraggable(true);
    }
	
    public GElementFAStateDoubleRectangle(State state, double x, double y,GElementFAMachine machine) {
    	super(machine);
        setState(state);
        setPosition(x, y);
        setDraggable(true);
    }
    
    public void setColor2(Color color){
    	this.color = color;
    }
    
    public void setPositions(double x, double y){
    	setPosition2(x, y);
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
        return false;
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
        
        int x = (int)(getPositionX());
        int y = (int)(getPositionY());
        int x2 = (int)(getPositionX2());
        int y2 = (int)(getPositionY2());
        
        int lineX = x;
        int lineY = y + 20;
        int lineX2 = x2;
        int lineY2 = y + 20;
        if (lineY >= y2){
        	lineY = (int)Math.ceil(((double)y2 - (double)y)/2);
        	lineY = lineY + y;
        }
        
        if(state.start) {
            startArrow.setAnchor(width, height); // TODO: fix anchors here!
            startArrow.setDirection(startArrowDirection);
            startArrow.setLength(20);
            startArrow.setAngle(30);
            startArrow.draw(g);
        }
        
        g.drawRect(x, y, Math.abs(x2 - x), Math.abs(y2 - y));
        g.drawRect(x + 5, y + 5, Math.abs((x2 - x) - 10), Math.abs((y2 - y) - 10));
        g.drawLine(lineX, lineY, lineX2, lineY2);
        
        if(state.accepted){
        	g.drawRect(x, y, Math.abs(x2 - x), Math.abs(y2 - y));
            g.drawRect(x + 5, y + 5, Math.abs((x2 - x) - 10), Math.abs((y2 - y) - 10));
            g.drawLine(lineX, lineY, lineX2, lineY2);
        }
        if(highlighted == true){
        	Stroke previousStroke = g.getStroke();
        	g.setStroke(new BasicStroke(3.0f));//2 pixel width
        	g.drawRect(x, y, Math.abs(x2 - x), Math.abs(y2 - y));
            g.drawRect(x + 5, y + 5, Math.abs((x2 - x) - 10), Math.abs((y2 - y) - 10));
            g.drawLine(lineX, lineY, lineX2, lineY2);
        	g.setStroke(previousStroke);
        }
    }

	@Override
	public boolean isInside(GElement e) {
		
		if(!(e instanceof GElementFAStateRectangle) && !(e instanceof GElementFAStateDoubleRectangle)) {
			return false;
		}
		
		int thisX = (int)(getPositionX());
        int thisY = (int)(getPositionY());
        int thisX2 = (int)(getPositionX2());
        int thisY2 = (int)(getPositionY2()); 
        
        int eX = (int)(e.getPositionX());
        int eY = (int)(e.getPositionY());
        int eX2 = (int)(e.getPositionX2());
        int eY2 = (int)(e.getPositionY2()); 
        
        if(thisX>=eX && thisY>=eY && thisX2<=eX2 && thisY2<=eY2) return true;
        return false;
	}   

	@Override
	public int maxCoorX() {
		return (int) getPositionX2();
	}

	@Override
	public int maxCoorY() {
		return (int) getPositionY2();
	}
}
