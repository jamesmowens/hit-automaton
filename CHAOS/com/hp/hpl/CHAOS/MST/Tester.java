package com.hp.hpl.CHAOS.MST;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import com.hp.hpl.CHAOS.ECube.EdgeLabel;

public class Tester {
	public static void main(String[] args) {
		test();
	}

	public static void test() {

		ArrayList<Node> nodes = new ArrayList<Node>();
		Node vn = new Node(0);
		Node n1 = new Node(1);
		Node n2 = new Node(2);
		Node n3 = new Node(3);
		Node n4 = new Node(4);
		Node n5 = new Node(5);
		Node n6 = new Node(6);
		Node n7 = new Node(7);
		Node n8 = new Node(8);
		Node n9 = new Node(9);
		Node n10 = new Node(10);

		Node n11 = new Node(11);
		Node n12 = new Node(12);
		Node n13 = new Node(13);
		Node n14 = new Node(14);
		Node n15 = new Node(15);
		Node n16 = new Node(16);
		Node n17 = new Node(17);
		Node n18 = new Node(18);
		Node n19 = new Node(19);
		Node n20 = new Node(20);

		Node n21 = new Node(21);
		Node n22 = new Node(22);
		Node n23 = new Node(23);
		Node n24 = new Node(24);
		Node n25 = new Node(25);
		Node n26 = new Node(26);
		Node n27 = new Node(27);
		Node n28 = new Node(28);
		Node n29 = new Node(29);
		Node n30 = new Node(30);

		Node n31 = new Node(31);
		Node n32 = new Node(32);
		Node n33 = new Node(33);
		Node n34 = new Node(34);
		Node n35 = new Node(35);
		Node n36 = new Node(36);
		Node n37 = new Node(37);
		Node n38 = new Node(38);
		Node n39 = new Node(39);
		Node n40 = new Node(40);

		Node n41 = new Node(41);
		Node n42 = new Node(42);
		Node n43 = new Node(43);
		Node n44 = new Node(44);
		Node n45 = new Node(45);
		Node n46 = new Node(46);
		Node n47 = new Node(47);
		Node n48 = new Node(48);
		Node n49 = new Node(49);
		Node n50 = new Node(50);
		nodes.add(vn);
		nodes.add(n1);
		nodes.add(n2);
		nodes.add(n3);
		nodes.add(n4);
		nodes.add(n5);
		nodes.add(n6);
		nodes.add(n7);
		nodes.add(n8);
		nodes.add(n9);
		nodes.add(n10);

		/*
		 * Edge e11 = new Edge(n2, n1, 10); Edge e12 = new Edge(vn, n1, 5); Edge
		 * e13 = new Edge(n5, n1, 6);
		 * 
		 * Edge e21 = new Edge(n6, n2, 4); Edge e22 = new Edge(vn, n2, 11); Edge
		 * e23 = new Edge(n5, n2, 26);
		 * 
		 * Edge e31 = new Edge(n12, n3, 10); Edge e32 = new Edge(vn, n3, 11);
		 * Edge e33 = new Edge(n15, n3, 18);
		 * 
		 * 
		 * Edge e41 = new Edge(n12, n4, 10); Edge e42 = new Edge(vn, n4, 11);
		 * Edge e43 = new Edge(n9, n4, 9);
		 * 
		 * 
		 * Edge e51 = new Edge(n12, n5, 10); Edge e61 = new Edge(n12, n6, 10);
		 * Edge e71 = new Edge(n12, n7, 10); Edge e81 = new Edge(n12, n8, 10);
		 * Edge e91 = new Edge(n12, n9, 10); Edge e101 = new Edge(n12, n10, 10);
		 * Edge e111 = new Edge(n12, n11, 10); Edge e121 = new Edge(n12, n12,
		 * 10); Edge e131 = new Edge(n12, n13, 10); Edge e141 = new Edge(n12,
		 * n14, 10); Edge e151 = new Edge(n12, n15, 10);
		 */
		AdjacencyList aList = new AdjacencyList();
		aList.addEdge(vn, n1, 6);
		aList.addEdge(vn, n2, 5);
		aList.addEdge(vn, n3, 20);
		aList.addEdge(vn, n4, 4);
		aList.addEdge(vn, n5, 5);
		aList.addEdge(vn, n6, 6);
		aList.addEdge(vn, n7, 7);
		aList.addEdge(vn, n8, 8);
		aList.addEdge(vn, n9, 9);
		aList.addEdge(vn, n10, 10);

		aList.addEdge(n1, n2, 1);
		aList.addEdge(n2, n3, 2);
		aList.addEdge(n3, n1, 3);
		aList.addEdge(n4, n5, 4);
		aList.addEdge(n5, n6, 5);
		aList.addEdge(n6, n4, 6);
		aList.addEdge(n1, n7, 12);
		aList.addEdge(n5, n8, 6);
		aList.addEdge(n7, n9, 10);
		aList.addEdge(n8, n10, 9);

		Edmonds ed = new Edmonds();

		AdjacencyList returnedList = ed.getMinBranching(vn, aList);
		System.out.println("=======start=========");
		for (int i = 0; i < nodes.size(); i++) {
			ArrayList<Edge> edgesn1 = returnedList.getAdjacent(nodes.get(i));
			nodes.get(i).printName();
		/*	if (nodes.get(i).name == 6)
				System.out.println("checking");*/

			for (int j = 0; edgesn1 != null && j < edgesn1.size(); j++) {
				System.out.println("from"); 
				edgesn1.get(j).getFrom().printName();
				System.out.println("to"); 
				edgesn1.get(j).getTo().printName();
				System.out.println("weight"); 
				System.out.println(edgesn1.get(j).getWeight());

			}
			System.out.println("================");
		}

		System.out.println("======over======");
	}

}
