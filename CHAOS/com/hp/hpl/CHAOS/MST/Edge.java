package com.hp.hpl.CHAOS.MST;

public class Edge implements Comparable<Edge> {

	   Node from, to;
	   double weight;

	   Edge(Node f, Node t, double w){
	       from = f;
	       to = t;
	       weight = w;
	   }

	   public int compareTo(Edge e){
	       return (int)(weight - e.weight);
	   }

	public Node getFrom() {
		return from;
	}

	public void setFrom(Node from) {
		this.from = from;
	}

	public Node getTo() {
		return to;
	}

	public void setTo(Node to) {
		this.to = to;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
	   
	}

