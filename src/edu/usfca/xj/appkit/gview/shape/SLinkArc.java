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

package edu.usfca.xj.appkit.gview.shape;

import edu.usfca.vas.graphics.fa.GElementFAStateDoubleRectangle;
import edu.usfca.vas.graphics.fa.GElementFAStateInterface;
import edu.usfca.vas.graphics.fa.GElementFAStateRectangle;
import edu.usfca.xj.appkit.gview.base.Vector2D;
import edu.usfca.xj.appkit.gview.object.GElement;
import edu.usfca.xj.foundation.XJXMLSerializable;

import java.awt.*;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.QuadCurve2D;

public class SLinkArc extends SLink implements XJXMLSerializable {

    protected transient QuadCurve2D.Double quad;
    protected transient CubicCurve2D.Double cubic;

    protected transient Shape shape;

    Vector2D vlabel;
    Vector2D pmiddle;

    public SLinkArc() {
        super();
    }

    public void setMouse(Point mouse) {
        setMouse(Vector2D.vector(mouse));
    }

    public void setMouse(Vector2D mouse) {
        setDirection(mouse.sub(end));
    }

    public void setFlatenessByMouse(Vector2D mouse) {
        Vector2D corde = getEndWithOffset().sub(getStartWithOffset());
        double dot = mouse.sub(getStartWithOffset()).dot(corde.normalize());
        corde.setLength(dot);

        Vector2D z = getStartWithOffset().add(corde);
        Vector2D f = mouse.sub(z);
        double cross = corde.cross(f);
        if(cross == 0)
            setFlateness(0);
        else
            setFlateness(-2*f.length()*cross/Math.abs(cross));
    }

    public void setMousePosition(Vector2D position) {
        setFlatenessByMouse(position);
    }

    public boolean contains(PathIterator iterator, double x, double y) {
        double coord[] = new double[6];
        double oldx = -1, oldy = -1;

        final double flateness = 0.8;
        final double inset = 4;

        FlatteningPathIterator i = new FlatteningPathIterator(iterator, flateness);
        while(!i.isDone()) {
            switch(i.currentSegment(coord)) {
                case FlatteningPathIterator.SEG_MOVETO:
                    oldx = coord[0];
                    oldy = coord[1];
                    break;

                case FlatteningPathIterator.SEG_LINETO:
                    double nx = coord[0];
                    double ny = coord[1];

                    double rx1 = Math.min(oldx, nx);
                    double ry1 = Math.min(oldy, ny);
                    double rx2 = Math.max(oldx, nx);
                    double ry2 = Math.max(oldy, ny);

                    if(Math.abs(rx1-rx2)<inset || Math.abs(ry1-ry2)<inset) {
                        rx1 -= inset;
                        ry1 -= inset;
                        rx2 += inset;
                        ry2 += inset;
                    }

                    if(x>=rx1 && x<=rx2 && y>=ry1 && y<=ry2)
                        return true;

                    oldx = nx;
                    oldy = ny;
                    break;
            }
            i.next();
        }
        return false;
    }

    public boolean contains(double x, double y) {
        if(selfLoop && cubic != null)
            return contains(cubic.getPathIterator(null), x, y);
        if(!selfLoop && quad != null)
            return contains(quad.getPathIterator(null), x, y);

        return false;
    }

    public void update() {
        // Compute the control point of the curve

        if(selfLoop) {
            if(cubic == null)
                cubic = new CubicCurve2D.Double();

            Vector2D corde = direction.copy();
            corde.stretch(1.7);
            if(corde.length()<100)
                corde.setLength(100);

            corde.rotate(-40);
            cubic.ctrlx1 = getStartWithOffset().getX()+corde.getX();
            cubic.ctrly1 = getStartWithOffset().getY()+corde.getY();

            corde.rotate(+80);
            cubic.ctrlx2 = getStartWithOffset().getX()+corde.getX();
            cubic.ctrly2 = getStartWithOffset().getY()+corde.getY();

            // Move the start/end point according to offset

            Vector2D v1 = new Vector2D(cubic.ctrlx1, cubic.ctrly1).sub(getStartWithOffset());
            Vector2D v2 = new Vector2D(cubic.ctrlx2, cubic.ctrly2).sub(getStartWithOffset());

            v1 = v1.normalize();
            v1.stretch(startTangentOffset);

            v2 = v2.normalize();
            v2.stretch(endTangentOffset);

            if(sourceIsRec) {
                cubic.x1 = getStartWithOffset().getX();
                cubic.y1 = getStartWithOffset().getY();
                cubic.x2 = getEndWithOffset().getX();
                cubic.y2 = getEndWithOffset().getY();

            }
            else
            {
            	cubic.x1 = getStartWithOffset().getX()+v1.getX();
            	cubic.y1 = getStartWithOffset().getY()+v1.getY();
            	cubic.x2 = getEndWithOffset().getX()+v2.getX();
            	cubic.y2 = getEndWithOffset().getY()+v2.getY();
            }

            // Position of the label

            Vector2D vlabel = direction.copy();
            vlabel.setLength(vlabel.length()+15);
            if(vlabel.length()<75)
                vlabel.setLength(75);

            Vector2D plabel = getStartWithOffset().add(vlabel);
            label.setPosition(plabel);

            // Create the arrow at the end of the path

            arrow.setAnchor(cubic.x2, cubic.y2);
            arrow.setDirection(new Vector2D(cubic.ctrlx2-cubic.x2, cubic.ctrly2-cubic.y2));

            shape = cubic;
        } else {
            Vector2D middle = getEndWithOffset().sub(getStartWithOffset());
            middle.stretch(0.5);

            Vector2D height = middle.normalize();
            height.rotate(-90);

            if(flateness == 0)
                height.setLength(0.01);
            else
                height.setLength(flateness);

            Vector2D ctrl = middle.add(height);

            if(quad == null)
                quad = new QuadCurve2D.Double();

            quad.x1 = getStartWithOffset().getX();
            quad.y1 = getStartWithOffset().getY();
            quad.x2 = getEndWithOffset().getX();
            quad.y2 = getEndWithOffset().getY();
            quad.ctrlx = getStartWithOffset().getX()+ctrl.getX();
            quad.ctrly = getStartWithOffset().getY()+ctrl.getY();

            Vector2D controlPoint = new Vector2D(quad.ctrlx, quad.ctrly);

            // Move the start/end point according to offset

            Vector2D v1 = controlPoint.sub(getStartWithOffset());
            Vector2D v2 = controlPoint.sub(getEndWithOffset());

            v1 = v1.normalize();
            v1.stretch(startTangentOffset);

            v2 = v2.normalize();
            v2.stretch(endTangentOffset);

            if(sourceIsRec) {
            	quad.x1 = getStartWithOffset().getX();
                quad.y1 = getStartWithOffset().getY();
            }
            else
            {
            	quad.x1 = getStartWithOffset().getX()+v1.getX();
            	quad.y1 = getStartWithOffset().getY()+v1.getY();
            }
            if(targetIsRec) {            	
            	quad.x2 = getEndWithOffset().getX();
            	quad.y2 = getEndWithOffset().getY();
            }
            else
            {
            	quad.x2 = getEndWithOffset().getX()+v2.getX();
            	quad.y2 = getEndWithOffset().getY()+v2.getY();
            }

            // Position of the label

            pmiddle = new Vector2D(quad.x1+(quad.x2-quad.x1)*0.5, quad.y1+(quad.y2-quad.y1)*0.5);
            vlabel = new Vector2D(quad.x2-quad.x1, quad.y2-quad.y1).rotate(90*(flateness<0?1:-1));

            vlabel.setLength(Math.abs(flateness)*0.5+20);
            label.setPosition(pmiddle.add(vlabel));

            // Create the arrow at the end of the path

            arrow.setAnchor(quad.x2, quad.y2);
            arrow.setDirection(controlPoint.sub(getEndWithOffset()));

            shape = quad;

            /*System.out.println("quad");
            System.out.println("x1 = " + quad.x1 + ", y1 = " + quad.y1);
            System.out.println("ctrlx = " + quad.ctrlx + ", ctrly = " + quad.ctrly);
            System.out.println("x2 = " + quad.x2 + ", y2 = " + quad.y2);*/
        }
    }

    public void draw(Graphics2D g) {
        if(shape == null || arrow == null || label == null)
            return;

        g.setColor(color);

        drawShape(g);
        label.draw(g);
    }

    public void drawShape(Graphics2D g) {
        if(shape == null || arrow == null || label == null)
            return;

        g.draw(shape);
        if(arrowVisible)
            arrow.draw(g);
    }

	public boolean intersectWith(GElement ge) {
		if(!(ge instanceof GElementFAStateRectangle) && !(ge instanceof GElementFAStateDoubleRectangle)) return false;
		int x1 = (int) ge.getPositionX();
		int x2 = (int) ge.getPositionX2();
		int y1 = (int) ge.getPositionY();
		int y2 = (int) ge.getPositionY2();
		if(!selfLoop) {
			if(quad.intersects(x1, y1-1, x2-x1, 1)) return false;
			if(quad.intersects(x1, y2, x2-x1, 1)) return false;
			if(quad.intersects(x1-1, y1, 1, y2-y1)) return false;
			if(quad.intersects(x2, y1, 1, y2-y1)) return false;
			return true;
		}
		else
		{
			if(cubic.intersects(x1, y1-1, x2-x1, 1)) return false;
			if(cubic.intersects(x1, y2, x2-x1, 1)) return false;
			if(cubic.intersects(x1-1, y1, 1, y2-y1)) return false;
			if(cubic.intersects(x2, y1, 1, y2-y1)) return false;
			return true;
		}
	}

	public SLinkArc createCopy() {
		SLinkArc copy = new SLinkArc();

		copy.start = createCopyVector2D(this.start);
		copy.end = createCopyVector2D(this.end);
		copy.startDirection = createCopyVector2D(this.startDirection);
		copy.endDirection = createCopyVector2D(this.endDirection);
		copy.startOffset = createCopyVector2D(this.startOffset);
		copy.endOffset = createCopyVector2D(this.endOffset);
		copy.startTangentOffset = this.startTangentOffset;
		copy.endTangentOffset = this.endTangentOffset;
		copy.direction = createCopyVector2D(this.direction);
		copy.flateness = this.flateness;
		copy.arrow = this.arrow.createCopy();
		copy.label  = this.label.createCopy(); 
		copy.selfLoop  = this.selfLoop;
		copy.arrowVisible  = this.arrowVisible;
		copy.color = this.color;
		copy.startWithOffset= createCopyVector2D(this.startWithOffset);
		copy.endWithOffset = createCopyVector2D(this.endWithOffset);
		
		return copy;
	}
	
	private Vector2D createCopyVector2D(Vector2D origin) {		
		Vector2D copy = null;
		if(origin!=null) copy = new Vector2D(origin.x, origin.y);
		return copy;
	}
}
