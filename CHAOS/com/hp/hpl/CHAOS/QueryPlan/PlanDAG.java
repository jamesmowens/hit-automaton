package com.hp.hpl.CHAOS.QueryPlan;

import java.util.ArrayList;

import com.hp.hpl.CHAOS.StreamOperator.StreamSinkOperator;
import com.hp.hpl.CHAOS.StreamOperator.StreamSourceOperator;

public class PlanDAG {

	String name = "";

	protected final ArrayList<PlanNode> roots;
	protected final ArrayList<PlanNode> leaves;
	protected final ArrayList<PlanNode> nodes;

	public PlanDAG() {
		super();
		this.roots = new ArrayList<PlanNode>();
		this.leaves = new ArrayList<PlanNode>();
		this.nodes = new ArrayList<PlanNode>();
	}

	public ArrayList<PlanNode> getRoots() {
		return roots;
	}

	public ArrayList<PlanNode> getLeaves() {
		return leaves;
	}

	public ArrayList<PlanNode> getNodes() {
		return nodes;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addNode(PlanNode node) {
		if (node == null)
			return;
		if (this.nodes.contains(node))
			return;
		if (node.getOperator() instanceof StreamSourceOperator)
			this.leaves.add(node);
		if (node.getOperator() instanceof StreamSinkOperator)
			this.roots.add(node);
		this.nodes.add(node);
	}

	public static void addParentChild(PlanNode parent, PlanNode child) {
		if (parent == null || child == null)
			return;
		child.addParent(parent);
		parent.addChild(child);
	}

	private static void removeParentChild(PlanNode parent, PlanNode child) {
		if (parent == null || child == null)
			return;
		child.removeParent(parent);
		parent.removeChild(child);
	}

	public void removeNode(PlanNode node) {
		if (node == null)
			return;
		if (!this.nodes.contains(node))
			return;
		if (node.getOperator() instanceof StreamSourceOperator)
			this.leaves.remove(node);
		if (node.getOperator() instanceof StreamSinkOperator)
			this.roots.remove(node);
		this.nodes.remove(node);
		for (PlanNode parent : node.getParent())
			PlanDAG.removeParentChild(parent, node);
		for (PlanNode child : node.getChildren())
			PlanDAG.removeParentChild(node, child);
	}

	public String toString() {

		StringBuffer output = new StringBuffer();
		toString(this.roots, output, 0);
		return output.toString();
	}

	private void toString(ArrayList<PlanNode> root, StringBuffer output,
			int indent) {
		for (PlanNode rootNode : root) {
			for (int i = 0; i < indent; i++) {
				output.append("  ");
			}
			output.append(rootNode.toString() + "\n");
			toString(rootNode.getChildren(), output, indent + 1);

		}

	}
}
