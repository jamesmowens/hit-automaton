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
import edu.usfca.xj.appkit.gview.base.Rect;
import edu.usfca.xj.appkit.gview.base.Vector2D;
import edu.usfca.xj.appkit.gview.shape.SLink;
import edu.usfca.xj.appkit.gview.shape.SLinkArc;
import edu.usfca.xj.appkit.gview.shape.SLinkBezier;
import edu.usfca.xj.appkit.gview.shape.SLinkElbow;
import edu.usfca.xj.foundation.XJXMLSerializable;

import java.awt.*;
import java.util.ArrayList;


public class GLink extends GElement implements XJXMLSerializable {

	public static final int SHAPE_ARC = 0;
	public static final int SHAPE_ELBOW = 1;
	public static final int SHAPE_BEZIER = 2;

	public GElement source = null;
	public GElement target = null;
	public String sourceAnchorKey = null;
	public String targetAnchorKey = null;
	public String pattern = null;
	public String nickname = null;

	protected SLink link = null;
	protected Color color = Color.BLACK;

	protected int shape = SHAPE_ARC;
	protected GElementFAMachine machine;

	public ArrayList<GElement> prevSource = new ArrayList<GElement>();
	public ArrayList<GElement> changeSourceWhenExpand = new ArrayList<GElement>();
	public ArrayList<GElement> prevTarget = new ArrayList<GElement>();
	public ArrayList<GElement> changeTargetWhenExpand = new ArrayList<GElement>();
	public ArrayList<SLink> prevLink = new ArrayList<SLink>();

	protected GElement container;

	public GElementFAMachine getMachine() {
		return machine;
	}

	public void setMachine(GElementFAMachine machine) {
		this.machine = machine;
	}

	public GLink() {
		super();
	}

	public GLink(GElementFAMachine machine) {
		super();
		this.machine=machine;
	}

	public GLink(GElement source, String sourceAnchorKey, GElement target, String targetAnchorKey, int shape, String pattern, Point mouse, double flateness,GElementFAMachine machine) {
		this.source = source;
		this.target = target;
		this.sourceAnchorKey = sourceAnchorKey;
		this.targetAnchorKey = targetAnchorKey;
		this.shape = shape;
		this.pattern = pattern;
		initializeLink(flateness);
		setSourceTangentOffset(getSourceOvalRadius(source, sourceAnchorKey, target));
		setTargetTangentOffset(getTargetOvalRadius(source, target, targetAnchorKey));
		link.setDirection(Vector2D.vector(mouse).sub(target.getPosition()));
		this.machine=machine;
	}

	public GLink(GElement source, String sourceAnchorKey, GElement target, String targetAnchorKey, int shape, String pattern, double flateness) {
		this.source = source;
		this.target = target;
		this.sourceAnchorKey = sourceAnchorKey;
		this.targetAnchorKey = targetAnchorKey;
		this.shape = shape;
		this.pattern = pattern;
		initializeLink(flateness);
		if(source == target)
			link.setDirection(new Vector2D(0, 1));
		else
			link.setDirection(source.getPosition().sub(target.getPosition()));

		setSourceTangentOffset(getSourceOvalRadius(source, sourceAnchorKey, target));
		setTargetTangentOffset(getTargetOvalRadius(source, target, targetAnchorKey));
	}

	// Gets the dynamic radius for a link that starts from a circle

	/**
	 * Gets the dynamic radius for a link that starts from a state (oval)
	 * @param source Starting element
	 * @param sourceAnchorKey Default anchor key (used if element is not a circle or double circle)
	 * @param target Ending element
	 * @return Length of radius for that specific point
	 */
	public static double getSourceOvalRadius(GElement source, String sourceAnchorKey, GElement target) {
		if (source instanceof GElementCircle || source instanceof GElementDoubleCircle) {
			double yRadius;
			if (source instanceof GElementCircle) {
				yRadius = ((GElementCircle)source).getRadius();
			} else {
				yRadius = ((GElementDoubleCircle)source).getRadius();
			}
			// For now, font is hard-coded to be 6
			int xString = (source.getLabel().length() * 6);

			// per GElementFAState, xRadius is (xString + radius)/2
			double xRadius = (xString + yRadius)/2;

			// get the components of the vector connecting the target and source
			double x_len = (-1) * source.getPosition().sub(target.getPosition()).getX();
			double y_len = (-1) * source.getPosition().sub(target.getPosition()).getY();

			double theta;
			// avoid dividing by zero
			if (x_len == 0) {
				theta = Math.PI/2;
			} else {
				theta = Math.atan(y_len / x_len);
			}

			// Because of bezier, tangent should really be offset by about pi/10
			double theta_bezier = theta - Math.PI/10; //tweaking it so it looks good

			double  newRadius = ((xRadius * yRadius) / Math.sqrt(
					Math.pow(xRadius * Math.sin(theta_bezier), 2) +
					Math.pow(yRadius * Math.cos(theta_bezier), 2)));
			return newRadius;
		} else {
			return source.getDefaultAnchorOffset(sourceAnchorKey);
		}
	}

	// Same as getSourceOvalRadius, except gets the radius for the target element
	// Offset differently because of the bezier
	public static double getTargetOvalRadius(GElement source, GElement target, String targetAnchorKey) {
		if (target instanceof GElementCircle || target instanceof GElementDoubleCircle) {

			double yRadius;
			if (target instanceof GElementCircle) {
				yRadius = ((GElementCircle)target).getRadius();
			} else {
				yRadius = ((GElementDoubleCircle)target).getRadius();
			}

			// For now, font is hard-coded to be 6
			int xString = (target.getLabel().length() * 6);

			// per GElementFAState, xRadius is (xString + radius)/2
			double xRadius = (xString + yRadius)/2;

			// get the components of the vector connecting the target and source
			double x_len = (-1) * target.getPosition().sub(source.getPosition()).getX();
			double y_len = (-1) * target.getPosition().sub(source.getPosition()).getY();

			double theta;
			// avoid dividing by zero
			if (x_len == 0) {
				theta = Math.PI/2;
			} else {
				theta = Math.atan(y_len / x_len);
			}

			// Because of bezier, tangent should really be offset by about pi/10
			double theta_bezier = theta + Math.PI/10; //tweaking it so it looks good

			double  newRadius = ((xRadius * yRadius) / Math.sqrt(
					Math.pow(xRadius * Math.sin(theta_bezier), 2) +
							Math.pow(yRadius * Math.cos(theta_bezier), 2)));
			return newRadius;
		} else {
			return target.getDefaultAnchorOffset(targetAnchorKey);
		}
	}

	public void setBezierControlPoints(Vector2D points[]) {
		if(link instanceof SLinkBezier) {
			SLinkBezier lb = (SLinkBezier)link;
			lb.setControlPoints(points);
		}
	}

	public void setBezierLabelPosition(Vector2D position) {
		if(link instanceof SLinkBezier) {
			SLinkBezier lb = (SLinkBezier)link;
			lb.setLabelPosition(position);
		}
	}

	protected SLink createLinkInstance() {
		switch(shape) {
		case SHAPE_ARC: return new SLinkArc();
		case SHAPE_ELBOW: return new SLinkElbow();
		case SHAPE_BEZIER: return new SLinkBezier();
		}
		return null;
	}

	protected void initializeLink(double flateness) {
		if(link == null) {
			link = createLinkInstance();
			link.setFlateness(flateness);
		}
	}

	public void setSource(GElement source) {
		this.source = source;
	}

	public GElement getSource() {
		return source;
	}

	public void setTarget(GElement target) {
		this.target = target;
	}

	public GElement getTarget() {
		return target;
	}

	public void setSourceAnchorKey(String key) {
		this.sourceAnchorKey = key;
	}

	public String getSourceAnchorKey() {
		return sourceAnchorKey;
	}

	public void setTargetAnchorKey(String key) {
		this.targetAnchorKey = key;
	}

	public String getTargetAnchorKey() {
		return targetAnchorKey;
	}

	public void setSourceTangentOffset(double offset) {
		link.setStartTangentOffset(offset);
	}

	public void setTargetTangentOffset(double offset) {
		link.setEndTangentOffset(offset);
	}

	public void setSourceOffset(double x, double y) {
		setSourceOffset(new Vector2D(x, y));
	}

	public void setSourceOffset(Vector2D offset) {
		link.setStartOffset(offset);
	}

	public void setTargetOffset(double x, double y) {
		setTargetOffset(new Vector2D(x, y));
	}

	public void setTargetOffset(Vector2D offset) {
		link.setEndOffset(offset);
	}

	public void setLink(SLink link) {
		this.link = link;
	}

	public SLink getLink() {
		return link;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getPattern() {
		return pattern;
	}

	public void setLabel(String label) {
		this.pattern = label;
	}

	public void setLabelColor(Color color) {
		link.setLabelColor(color);
		setColor(color);
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor(){
		return this.color;
	}

	public void setLabelVisible(boolean flag) {
		if(link != null)
			link.setLabelVisible(flag);
	}

	public boolean isLabelVisible() {
		if(link == null)
			return false;
		else
			return link.isLabelVisible();
	}

	public void setShape(int type) {
		this.shape = type;
	}

	public int getShape() {
		return shape;
	}

	public String getNickname(){
		return nickname;
	}

	public void setNickname(String newNickname){
		this.nickname = newNickname;
		//TODO
	}

	public void toggleShape() {
		switch(shape) {
		case SHAPE_ARC:
			shape = SHAPE_ELBOW;
			break;
		case SHAPE_ELBOW:
			shape = SHAPE_ARC;
			break;
		case SHAPE_BEZIER:
			// Cannot toggle a bezier link
			return;
		}
		double flateness = link.getFlateness();
		Vector2D direction = link.getDirection();

		link = createLinkInstance();
		link.setFlateness(flateness);
		link.setDirection(direction);
	}

	public void setMousePosition(Point mouse) {
		link.setDirection(Vector2D.vector(mouse).sub(target.getPosition()));
		link.setMousePosition(Vector2D.vector(mouse));
	}

	public Rect getFrame() {
		update();
		return link.getFrame();
	}

	public boolean isInside(Point p) {
		if(link == null)
			return false;
		else
			return link.contains(p.x, p.y);
	}

	public void update() {
		initializeLink(0);
		//System.out.println("I am updating the link");

		source.updateAnchors();
		target.updateAnchors();

		// Updates the offset for the source and target (computationally heavy)
		setSourceTangentOffset(getSourceOvalRadius(source, sourceAnchorKey, target));
		setTargetTangentOffset(getTargetOvalRadius(source, target, targetAnchorKey));

		link.setStartAnchor(source.getAnchor(sourceAnchorKey));
		link.setEndAnchor(target.getAnchor(targetAnchorKey));
		link.setLabel(pattern);
		link.setSelfLoop(source == target);
		if((source instanceof GElementFAStateRectangle) || (source instanceof GElementFAStateDoubleRectangle)) {
			link.sourceIsRec = true;
		}
		else
		{
			link.sourceIsRec = false;
		}
		if((target instanceof GElementFAStateRectangle) || (target instanceof GElementFAStateDoubleRectangle)) {
			link.targetIsRec = true;
		}
		else
		{
			link.targetIsRec = false;
		}

		link.update();
	}

	public void draw(Graphics2D g) {
		update();

		g.setStroke(new BasicStroke(penSize));

		if(highlight) {
			link.setColor(Color.RED);
		}
		else {
			link.setColor(findColor());
		}

		link.draw(g);
	}

	private Color findColor() {
		GElement container = findContainer();
		/*System.out.println("This link: " + label);
		if(container!=null) 
		System.out.println("container = " + container.getLabel());
		else 
			System.out.println("container = null");*/
		if(container==null) {
			return Color.BLACK;
		}
		return container.getColor();
	}

	public GElement findContainer() {

		if(prevSource.size()!=0 || prevTarget.size()!=0) {
			return container;
		}

		GElement container = null;
		int containerX1 = 0,containerY1 = 0,containerX2 = 0,containerY2 = 0;
		//System.out.println("finding " + this.pattern + " " + machine.getElements().size());
		for(GElement e : machine.getElements()) {
			//System.out.println(e.label);
			if(!source.equals(e) && source.isInside(e) && target.isInside(e) && ((SLinkArc)link).intersectWith(e)) {
				int x1 = (int)(e.getPositionX());
				int y1 = (int)(e.getPositionY());
				int x2 = (int)(e.getPositionX2());
				int y2 = (int)(e.getPositionY2());
				if(container==null || (x1>=containerX1 && x2<=containerX2 && y1>=containerY1 && y2<=containerY2)) {
					container=e;
					containerX1=x1;
					containerY1=y1;
					containerX2=x2;
					containerY2=y2;
				}
			}
		}
		this.container = container;
		return container;
	}

	public void drawShape(Graphics2D g) {
		link.drawShape(g);
	}

	@Override
	public boolean isInside(GElement e) {
		// TODO Auto-generated method stub
		return false;
	}

	public GElement getRealSource() {
		if(prevSource.size()!=0) {
			return prevSource.get(0);
		}
		return source;
	}

	public GElement getRealTarget() {
		if(prevTarget.size()!=0) {
			return prevTarget.get(0);
		}
		return target;
	}

	@Override
	public int maxCoorX() {
		return 0;
	}

	@Override
	public int maxCoorY() {
		return 0;
	}

}
