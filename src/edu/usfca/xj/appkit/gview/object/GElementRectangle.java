package edu.usfca.xj.appkit.gview.object;

import edu.usfca.vas.graphics.fa.GElementFAMachine;
import edu.usfca.xj.appkit.gview.base.Anchor2D;
import edu.usfca.xj.appkit.gview.base.Rect;
import edu.usfca.xj.appkit.gview.base.Vector2D;
import edu.usfca.xj.appkit.gview.shape.SLabel;
import edu.usfca.xj.foundation.XJXMLSerializable;

import java.awt.*;

public abstract class GElementRectangle extends GElement implements XJXMLSerializable {
	public static final int DEFAULT_WIDTH = 100;
	public static final int DEFAULT_HEIGHT = 100;

    protected double width = DEFAULT_WIDTH;
    protected double height = DEFAULT_HEIGHT;
    
    protected GElementFAMachine machine;
    
    public GElementFAMachine getMachine() {
		return machine;
	}

	public void setMachine(GElementFAMachine machine) {
		this.machine = machine;
	}
    //protected Color color = Color.BLACK;
	
    public GElementRectangle() {
    }
    
    public GElementRectangle(GElementFAMachine machine) {
		this.machine = machine;
    }
    
    public void setColor3(Color newColor){
    	this.color = newColor;
    }
    
    public void setHeight(double height) {
        this.height = height;
        elementDimensionDidChange();
    }
    
    public void setWidth(double width) {
        this.width = width;
        elementDimensionDidChange();
    }

    public double getHeight() {
        return height;
    }
    
    public double getWidth(){
    	return width;
    }

    public void updateAnchors() { // probably is wrong
        setAnchor(ANCHOR_CENTER, position.add(new Vector2D(0, 0)), Anchor2D.DIRECTION_FREE);
        setAnchor(ANCHOR_TOP, position.add(new Vector2D(0, -(height))), Anchor2D.DIRECTION_TOP);
        setAnchor(ANCHOR_BOTTOM, position.add(new Vector2D(0, (height))), Anchor2D.DIRECTION_BOTTOM);
        setAnchor(ANCHOR_LEFT, position.add(new Vector2D(-(width), 0)), Anchor2D.DIRECTION_LEFT);
        setAnchor(ANCHOR_RIGHT, position.add(new Vector2D((width), 0)), Anchor2D.DIRECTION_RIGHT);
    }

    public double getDefaultAnchorOffset(String anchorKey) {
    	return 0;
        /*if(anchorKey != null && anchorKey.equals(ANCHOR_CENTER))
            return height;  // may be wrong
        else
            return 0;*/
    }

    public Rect getFrame() {
        double x = getPositionX();
        double y = getPositionY();
        double dx = getPositionX2();
        double dy = getPositionY2();
        return new Rect(x, y, dx, dy);
    }

    public boolean isInside(Point p) {
    	if (p.getX() >= getPositionX() && p.getY() >= getPositionY() &&
    			p.getX() <= getPositionX2() && p.getY() <= getPositionY2()){
    		if (p.getX() >= (getPositionX() + 10) && p.getY() >= (getPositionY() + 10) &&
    				p.getX() <= (getPositionX2() - 10) && p.getY() <= (getPositionY2() - 10)){
    			return false;
    		}
    		else {
    			return true;
    		}
    	}
    	else {
    		return false;
    	}
    }

    public void draw(Graphics2D g) {
        if(labelVisible) {
            g.setColor(labelColor);
            SLabel.drawCenteredString(getLabel(), (int)((getPositionX2() + getPositionX())/2), (int)getPositionY() + 10, g);
        }

        if(shouldHighLight()) {
        	g.setColor(color.RED);
        }
        else
        {
        	if(color != null)
        		g.setColor(color);
        	else
        		g.setColor(Color.black);
        }

        g.setStroke(strokeSize);

        drawShape(g);

        g.setStroke(strokeNormal);
    }
    
    private boolean shouldHighLight() {
    	GElement highlightState = machine.getHiddenHighlightState();
    	if(highlightState==null) return false;
    	return machine.hiddenInside(this,highlightState);
	}

	public void drawShape(Graphics2D g) {
        super.drawShape(g);

        //color = getPosition().color; 
        
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

        g.drawRect(x, y, Math.abs(x2 - x), Math.abs(y2 - y));
        g.drawLine(lineX, lineY, lineX2, lineY2);
    }    
}
