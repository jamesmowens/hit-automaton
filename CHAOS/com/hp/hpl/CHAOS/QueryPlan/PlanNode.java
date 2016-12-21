package com.hp.hpl.CHAOS.QueryPlan;

import java.util.ArrayList;

import com.hp.hpl.CHAOS.StreamOperator.StreamOperator;

public class PlanNode {

	protected StreamOperator operator;

	protected final ArrayList<PlanNode> parent;

	protected final ArrayList<PlanNode> children;

	protected int nodeID;
	
	protected byte bitmap;

	public PlanNode(StreamOperator operator, int nodeID) {
		super();
		this.operator = operator;
		this.parent = new ArrayList<PlanNode>();
		this.children = new ArrayList<PlanNode>();
		this.nodeID = operator.getOperatorID();
		this.bitmap=0;
	}

	public byte getBitmap() {
		return bitmap;
	}

	public void setBitmap(byte bitmap) {
		this.bitmap = bitmap;
	}

	public StreamOperator getOperator() {
		return operator;
	}

	public ArrayList<PlanNode> getParent() {
		return parent;
	}

	public ArrayList<PlanNode> getChildren() {
		return children;
	}

	public int getNodeID() {
		return nodeID;
	}

	public void setOperator(StreamOperator operator) {
		this.operator = operator;
		this.nodeID = operator.getOperatorID();
	}

	public void addChild(PlanNode child) {
		if ((child != null) && !this.children.contains(child))
			this.children.add(child);
	}

	public void addChild(PlanNode child, int position) {
		if ((child != null) && !this.children.contains(child))
			this.children.add(position, child);
	}

	public void addParent(PlanNode parent) {
		if ((parent != null) && !this.parent.contains(parent))
			this.parent.add(parent);
	}

	public void addParent(PlanNode parent, int position) {
		if ((parent != null) && !this.parent.contains(parent))
			this.parent.add(position, parent);
	}

	public boolean removeChild(PlanNode child) {
		return this.children.remove(child);
	}

	public boolean removeParent(PlanNode parent) {
		return this.parent.remove(parent);
	}

	public PlanNode getChild(int position) {

		if (position > this.children.size() - 1)
			return null;
		return this.children.get(position);
	}

	public PlanNode getParent(int position) {

		if (position > this.parent.size() - 1)
			return null;
		return this.parent.get(position);
	}

	public String toString() {
		return operator.toString();
	}

}