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
import edu.usfca.vas.graphics.fa.GElementFAStateDoubleRectangle;
import edu.usfca.vas.graphics.fa.GElementFAStateRectangle;
import edu.usfca.xj.appkit.gview.base.Anchor2D;
import edu.usfca.xj.appkit.gview.base.Rect;
import edu.usfca.xj.appkit.gview.base.Vector2D;
import edu.usfca.xj.appkit.gview.shape.SLabel;
import edu.usfca.xj.foundation.XJXMLSerializable;

import java.awt.*;

public abstract class GElementCircle extends GElement implements XJXMLSerializable {

    public static final int DEFAULT_RADIUS = 20;
    public static final int DEFAULT_RADIUS_INNER = 16;

    protected double radius = DEFAULT_RADIUS;
    protected double radius2 = DEFAULT_RADIUS_INNER;
    //protected Color color = Color.BLACK;
    
    protected GElementFAMachine machine;
    
    public GElementFAMachine getMachine() {
		return machine;
	}

	public void setMachine(GElementFAMachine machine) {
		this.machine = machine;
	}

	public GElementCircle() {
    }

	public GElementCircle(GElementFAMachine machine) {
    	this.machine = machine;
    }

    public void setRadius(double radius) {
        this.radius = radius;
        elementDimensionDidChange();
    }

    public void setColor3(Color newColor){
    	this.color = newColor;
    }
    
    public double getRadius() { //returns the constant defined above
        return radius;
    }
    
    public double getRadius2(){ //returns the constant defined above
    	return radius2;
    }

    public void updateAnchors() {
        setAnchor(ANCHOR_CENTER, position, Anchor2D.DIRECTION_FREE);
        setAnchor(ANCHOR_TOP, position.add(new Vector2D(0, -radius)), Anchor2D.DIRECTION_TOP);
        setAnchor(ANCHOR_BOTTOM, position.add(new Vector2D(0, radius)), Anchor2D.DIRECTION_BOTTOM);
        setAnchor(ANCHOR_LEFT, position.add(new Vector2D(-radius, 0)), Anchor2D.DIRECTION_LEFT);
        setAnchor(ANCHOR_RIGHT, position.add(new Vector2D(radius, 0)), Anchor2D.DIRECTION_RIGHT);
    }

    public double getDefaultAnchorOffset(String anchorKey) {
        if(anchorKey != null && anchorKey.equals(ANCHOR_CENTER))
            return radius;
        else
            return 0;
    }

    public Rect getFrame() {
        double x = getPositionX()-radius;
        double y = getPositionY()-radius;
        double dx = radius*2;
        double dy = radius*2;
        return new Rect(x, y, dx, dy);
    }

    public boolean isInside(Point p) {
        return Math.abs(p.getX()-getPositionX())<radius && Math.abs(p.getY()-getPositionY())<radius;
    }

    public void draw(Graphics2D g) {
        if(labelVisible) {
            g.setColor(labelColor);
            SLabel.drawCenteredString(getLabel(), (int)getPositionX(), (int)getPositionY(), g);
        }

        if(highlight) {
        	g.setColor(Color.RED);
        }
        else
        {
        	g.setColor(findColor());
        }
        
        g.setStroke(strokeSize);

        drawShape(g);

        g.setStroke(strokeNormal);
    }

    private Color findColor() {
		GElement parent = findParent();
		if(parent==null) return Color.BLACK;
		return parent.getColor();
	}

	private GElement findParent() {
		GElement parent = null;
		for(GElement e: machine.getElements()) {
			if(isInside(e)) {
				if(parent==null || e.isInside(parent)) {
					parent = e;
				}
			}
		}
		return parent;
		
	}

	// draws the circle
    public void drawShape(Graphics2D g) {
        super.drawShape(g);

        //color = getPosition().color;
        
        int x = (int)(getPositionX()-radius);
        int y = (int)(getPositionY()-radius);

        g.drawOval(x, y, (int)(radius*2), (int)(radius*2));
    }

	@Override
	public boolean isInside(GElement e) {
		
		//System.out.println("in");
		
		if(!(e instanceof GElementFAStateRectangle) && !(e instanceof GElementFAStateDoubleRectangle)) {
			return false;
		}
		
		int thisX = (int)(getPositionX()-radius);
        int thisY = (int)(getPositionY()-radius);
        int thisX2 = (int)(getPositionX()+radius);
        int thisY2 = (int)(getPositionY()+radius);
        
        int eX = (int)(e.getPositionX());
        int eY = (int)(e.getPositionY());
        int eX2 = (int)(e.getPositionX2());
        int eY2 = (int)(e.getPositionY2()); 
        
        /*System.out.println("checking " + label + " " + e.getLabel());
        System.out.println(thisX + " " + thisY + " " + thisX2 + " " + thisY2);
        System.out.println(eX + " " + eY + " " + eX2 + " " + eY2);*/
        
        if(thisX>=eX && thisY>=eY && thisX2<=eX2 && thisY2<=eY2) return true;
        return false;
	}
}
