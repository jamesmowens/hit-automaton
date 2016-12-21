package com.hp.hpl.CHAOS.MST;

public class Node implements Comparable<Node> {

	int name; //queryID
	boolean visited = false; // used for Kosaraju's algorithm and Edmonds's
								// algorithm
	int lowlink = -1; // used for Tarjan's algorithm
	int index = -1; // used for Tarjan's algorithm

	public Node()
	{
		 
	}
	public Node(int n) {
		name = n;
	}

	public int compareTo(Node n) {
		if (n == this)
			return 0;
		return -1;
	}

	public int getName() {
		return name;
	}

	public void setName(int name) {
		this.name = name;
	}

	public void printName() {
		System.out.println(name);
	}
}
