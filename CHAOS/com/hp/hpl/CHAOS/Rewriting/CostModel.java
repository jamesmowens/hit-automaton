package com.hp.hpl.CHAOS.Rewriting;

import java.lang.Math;
import java.util.ArrayList;

/**
 * I should revise it Currently, only the formula such as (8)(9)(10)(14) etc are
 * considered. such formula is simple as only one piece is off
 * 
 * @author liumo
 * 
 */
public class CostModel {
	QueryInfo[][] orderedQueries;
	ConceptTree tree;

	protected CostModel(QueryInfo[][] orderedQueries, ConceptTree tree) {
		super();
		this.orderedQueries = orderedQueries;
		this.tree = tree;
	}

	public ConceptTree getTree() {
		return tree;
	}

	public void setTree(ConceptTree tree) {
		this.tree = tree;
	}

	public QueryInfo[][] getOrderedQueries() {
		return orderedQueries;
	}

	public void setOrderedQueries(QueryInfo[][] orderedQueries) {
		this.orderedQueries = orderedQueries;
	}

	protected CostModel(QueryInfo[][] orderedQueries) {
		super();
		this.orderedQueries = orderedQueries;
	}

	/**
	 * compute the cost for shared query processing
	 * 
	 * @param qroot
	 * @param numQueries
	 *            : shared query number
	 * @param numCheckPoints
	 * @return
	 */
	protected double sharedProcessing(int qroot, int numQueries,
			int numCheckPoints) {
		double cost = singleCompute(qroot);
		double rootSize = getResultSize(qroot);

		cost += rootSize * numCheckPoints * numQueries;

		return cost;
	}

	/**
	 * try to simulate the negation checking cost in a single query
	 * 
	 * @param qid
	 * @return
	 */
	protected double singleComputewithNegation_bp(int qid, int negationPos) {
		int length = Utility.getQuery(qid, this.orderedQueries).stackTypes
				.size();
		String eventType = Utility.getQuery(qid, this.orderedQueries).stackTypes
				.get(0);
		int childrenNum = getNumChildren(eventType);

		double cost = resultSize(length, eventType) * Statistics.constantCost
				* length;

		int i = 1;
		while (i <= length - 1) {
			int multiple = 1;

			for (int j = 1; j <= i; j++) {

				if (i == negationPos) {
					multiple *= Statistics.Stack_Size
							* childrenNum
							* Statistics.selectivity
							* (1 - Statistics.selectivity
									* Statistics.selectivity);
					negationPos = -1;

				} else
					multiple *= Statistics.Stack_Size * childrenNum
							* Statistics.selectivity;
			}

			cost += Statistics.Stack_Size * childrenNum * multiple;
			i++;
		}
		return cost;
	}

	protected int singleComputewithNum(int joiningNum, String eventType) {
		int cost = 0;
		int stackNum = joiningNum;
		int childrenNum = getNumChildren(eventType);
		int i = 1;
		while (i <= stackNum - 1) {
			int multiple = 1;
			for (int j = 1; j <= i; j++) {
				multiple *= Statistics.Stack_Size * childrenNum
						* Statistics.selectivity;
			}
			cost += Statistics.Stack_Size * childrenNum * multiple;
			i++;
		}
		return cost;
	}

	/**
	 * Compute qid1 with the tuples not been processed in qid2. The total cost
	 * should be the single compute cost for qid1 minus the single compute cost
	 * for qid2
	 * 
	 * @param qid1
	 *            the upper concept
	 * @param qid2
	 * @return
	 */
	protected int singleComputeDiffer(int qid1, int qid2) {
		return singleCompute(qid1) - singleCompute(qid2);
		/*
		 * int cost = 0; int stackNum = Utility.getQuery(qid1,
		 * this.orderedQueries).stackTypes .size();
		 */
		// String stackType = Utility.getQuery(qid,
		// this.orderedQueries).stackTypes
		// .get(0);
		// int childrenNum = getNumChildren(stackType);
		// actually, I need to store childrenNum per event type
		/*
		 * ArrayList<Integer> childrenNums = new ArrayList<Integer>();
		 * 
		 * for (int j = 0; j < Utility.getQuery(qid1,
		 * this.orderedQueries).stackTypes .size(); j++) { String stackType1 =
		 * Utility.getQuery(qid1, this.orderedQueries).stackTypes .get(j); int
		 * childrenNum1 = getNumChildren(stackType1);
		 * 
		 * String stackType2 = Utility.getQuery(qid2,
		 * this.orderedQueries).stackTypes .get(j); int childrenNum2 =
		 * getNumChildren(stackType2);
		 * 
		 * childrenNums.add(childrenNum1 - childrenNum2); }
		 */

		/*
		 * int i = 1; while (i <= stackNum - 1) { int multiple = 1; for (int j =
		 * 1; j <= i; j++) { multiple *= (Statistics.Stack_Size
		 * childrenNums.get(j) .intValue()) Statistics.selectivity; //
		 * (Statistics.Stack_Size childrenNum) is the number of events //
		 * satisfying the large concept; // and here I assume all the event type
		 * in a query has the same // concept level and number of children for
		 * simplicity } cost += Statistics.Stack_Size
		 * childrenNums.get(0).intValue() multiple; i++; } return cost;
		 */
	}

	/**
	 * compute the size of qid query results
	 * 
	 * @param qid
	 * @return
	 */
	protected int getResultSize(int qid) {
		int size = 0;
		int stackNum = Utility.getQuery(qid, this.orderedQueries).stackTypes
				.size();
		size = Statistics.Stack_Size;
		for (int i = 0; i < stackNum - 1; i++) {
			size *= Statistics.Stack_Size * Statistics.selectivity;
		}
		return size;
	}

	/**
	 * the cost for pointer based join
	 * 
	 * @param qid
	 * @return
	 */
	protected int singleCompute(int qid) {
		int cost = 0;
		int stackNum = Utility.getQuery(qid, this.orderedQueries).stackTypes
				.size();
		// extended to support negation
		// check the type with"-" before it
		int positiveStackNum = 0;
		for (int i = 0; i < stackNum; i++) {
			if (Utility.getQuery(qid, this.orderedQueries).stackTypes.get(i)
					.startsWith("-")) {
				continue;

			} else
				positiveStackNum++;

		}

		// actually, I need to store childrenNum per event type
		ArrayList<Integer> childrenNums = new ArrayList<Integer>();

		for (int j = 0; j < Utility.getQuery(qid, this.orderedQueries).stackTypes
				.size(); j++) {
			String stackType = Utility.getQuery(qid, this.orderedQueries).stackTypes
					.get(j);
			// /////////////////////////
			if (stackType.startsWith("-")) {
				stackType = stackType.substring(1, stackType.length());
				int childrenNum = getNumChildren(stackType);
				childrenNums.add(-childrenNum);
			}
			// /////////////////////////
			else {
				int childrenNum = getNumChildren(stackType);
				childrenNums.add(childrenNum);
			}

		}

		int i = 1;
		while (i <= positiveStackNum - 1) {
			int multiple = 1;
			for (int j = 1; j <= i; j++) {

				// extended to support negation
				if (childrenNums.get(j).intValue() < 0) {
					multiple *= (Statistics.Stack_Size
							* childrenNums.get(j).intValue() * -1)
							* Statistics.selectivity
							* (1 - Statistics.selectivity
									* Statistics.selectivity);

				} else {
					multiple *= (Statistics.Stack_Size * childrenNums.get(j)
							.intValue())
							* Statistics.selectivity;

				}

				// (Statistics.Stack_Size * childrenNum) is the number of events
				// satisfying the large concept;
				// and here I assume all the event type in a query has the same
				// concept level and number of children for simplicity
			}
			cost += Statistics.Stack_Size * childrenNums.get(0).intValue()
					* multiple;
			i++;
		}
		return cost;
	}

	/**
	 * return the result size for the stack num joining num here, to get the
	 * result size, only given the joining number is not good enough I should
	 * add the event type to get the matching children event types in the tree
	 * 
	 * @param joiningNum
	 * @return
	 */
	protected double resultSize(int joiningNum, String eventType) {
		double childrenNum = getNumChildren(eventType);
		double resultSize = Statistics.Stack_Size * childrenNum;

		for (int i = 0; i < (joiningNum - 1); i++) {
			resultSize *= (Statistics.Stack_Size * childrenNum)
					* Statistics.selectivity;

		}
		return resultSize;
	}

	protected double topdownConCost_pattern_middle(int middleNum, int qbottom) {
		double cost = 0;
		int stackNum = middleNum;
		// the middle one has the same event type level as the bottom one, so as
		// for the event type, I use the bottom one instead.

		int stackNumbottom = Utility.getQuery(qbottom, this.orderedQueries).stackTypes
				.size();

		String eventType = Utility.getQuery(qbottom, this.orderedQueries).stackTypes
				.get(0);

		int childrenNum = getNumChildren(eventType);
		double resultSize = Statistics.Stack_Size * childrenNum;
		for (int i = 0; i < (stackNum - 1); i++) {
			resultSize *= Statistics.Stack_Size * childrenNum
					* Statistics.selectivity;

		}

		double resultSizebottomextra = Statistics.Stack_Size * childrenNum;
		for (int i = 0; i < (stackNumbottom - stackNum - 1); i++) {
			resultSizebottomextra *= Statistics.Stack_Size * childrenNum
					* Statistics.selectivity;

		}
		cost = singleComputewithNum(stackNumbottom - stackNum, eventType);
		cost += (resultSize) * (resultSizebottomextra)
				* (Statistics.selectivity) + (resultSize)
				+ (resultSizebottomextra);
		return cost;
	}

	/**
	 * The conditional cost for computing the query qbottom with qtop without
	 * sorting
	 * 
	 * @param qtop
	 * @param qbottom
	 * @return
	 */
	protected double topdownConCost_pattern(int qtop, int qbottom) {
		double cost = 0;
		int stackNum = Utility.getQuery(qtop, this.orderedQueries).stackTypes
				.size();
		int stackNumbottom = Utility.getQuery(qbottom, this.orderedQueries).stackTypes
				.size();

		String eventType = Utility.getQuery(qtop, this.orderedQueries).stackTypes
				.get(0);
		int childrenNum = getNumChildren(eventType);

		String bottomeventType = Utility.getQuery(qbottom, this.orderedQueries).stackTypes
				.get(0);
		int bottomchildrenNum = getNumChildren(bottomeventType);

		double resultSize = Statistics.Stack_Size * childrenNum;
		for (int i = 0; i < (stackNum - 1); i++) {
			resultSize *= Statistics.Stack_Size * childrenNum
					* Statistics.selectivity;

		}

		double resultSizebottomextra = Statistics.Stack_Size
				* bottomchildrenNum;
		for (int i = 0; i < (stackNumbottom - stackNum - 1); i++) {
			resultSizebottomextra *= Statistics.Stack_Size * bottomchildrenNum
					* Statistics.selectivity;

		}
		// added parameter
		cost = 3 * (resultSize) * (resultSizebottomextra)
				* (Statistics.selectivity) + (resultSize)
				+ (resultSizebottomextra);
		return cost;
	}

	/**
	 * The conditional cost for computing the query qbottom with qtop with
	 * sorting NOT TESTED YET
	 * 
	 * @param qtop
	 * @param qbottom
	 * @return
	 */
	protected double topdownConCost_pattern_sort(int qtop, int qbottom) {

		double cost = 0;
		int stackNum = Utility.getQuery(qtop, this.orderedQueries).stackTypes
				.size();
		int stackNumbottom = Utility.getQuery(qbottom, this.orderedQueries).stackTypes
				.size();

		String eventType = Utility.getQuery(qtop, this.orderedQueries).stackTypes
				.get(0);
		int childrenNum = getNumChildren(eventType);

		String bottomeventType = Utility.getQuery(qbottom, this.orderedQueries).stackTypes
				.get(0);
		int bottomchildrenNum = getNumChildren(bottomeventType);

		double resultSize = Statistics.Stack_Size * childrenNum;
		for (int i = 0; i < (stackNum - 1); i++) {
			resultSize *= Statistics.Stack_Size * childrenNum
					* Statistics.selectivity;

		}

		double resultSizebottomextra = Statistics.Stack_Size
				* bottomchildrenNum;
		for (int i = 0; i < (stackNumbottom - stackNum - 1); i++) {
			resultSizebottomextra *= Statistics.Stack_Size * bottomchildrenNum
					* Statistics.selectivity;

		}
		cost = resultSize * Math.log(resultSize) + (resultSize)
				* (resultSizebottomextra) * (Statistics.selectivity)
				+ (resultSize) + (resultSizebottomextra)
				+ (Statistics.constantCost);
		return cost;

	}

	/**
	 * The conditional cost for computing the query qtopp with bottom up
	 * computation with concept changes only
	 * 
	 * @param qtopp
	 * @param qbottom
	 * @return
	 */
	protected double bottomup_concept(int qtop, int qbottom) {
		double cost = 0;
		// here, I assume the event types in the query are in the same level

		// here, I use single compute to simulate the cost with less stack tuple
		// number.

		cost = singleComputeDiffer(qtop, qbottom);
		return cost;
		/*
		 * String stackType = Utility.getQuery(qtop,
		 * this.orderedQueries).stackTypes .get(0);
		 * 
		 * String stackTypebottom = Utility.getQuery(qbottom,
		 * this.orderedQueries).stackTypes .get(0);
		 * 
		 * int childrenNum = getNumChildren(stackType); int childrenNumbottom =
		 * getNumChildren(stackTypebottom); cost = Statistics.Stack_Size
		 * (childrenNum - childrenNumbottom) Statistics.Stack_Size (childrenNum
		 * - childrenNumbottom) Statistics.selectivity;
		 */

	}

	/**
	 * The upper and lower level queries have the same length but the lower
	 * level query has more negative event constraint.
	 * 
	 * @param qtop
	 * @return
	 */
	protected double topdown_negation_sameLength(int qtop) {
		double cost = 0;
		int length = Utility.getQuery(qtop, this.orderedQueries).stackTypes
				.size();
		String eventType = Utility.getQuery(qtop, this.orderedQueries).stackTypes
				.get(0);

		// Statistics.constantCost * length
		cost = resultSize(length, eventType)
				* (1 - Statistics.selectivity * Statistics.selectivity);
		return cost;
	}

	/**
	 * return the conditional cost for qbottom with top down;
	 * 
	 * @param qtop
	 * @param qbottom
	 * @return
	 */
	protected double topdown_concept(int qtop) {
		double cost = 0;
		int length = Utility.getQuery(qtop, this.orderedQueries).stackTypes
				.size();
		String eventType = Utility.getQuery(qtop, this.orderedQueries).stackTypes
				.get(0);

		cost = resultSize(length, eventType) * Statistics.constantCost * length;
		return cost;
	}

	/**
	 * Return the number of children node
	 * 
	 * @return
	 */
	protected int getNumChildren(String type) {
		int children = 0;
		for (int i = 0; i < this.tree.getNumLevels(); i++) {
			for (int j = 0; j < this.tree.getLevel(i).nodes.length; j++) {
				if (Utility.semanticMatch(type,
						this.tree.getLevel(i).nodes[j].name, this.tree)) {
					children++;
				}
			}
		}
		return children;
	}

	/**
	 * return the conditional cost for qbottom when both levels are changed
	 * 
	 * @param qtop
	 * @param qbottom
	 * @return
	 */
	protected double topdown_cp(int qtop, int qbottom) {
		double cost = 0;
		// Simplified assumption, the middle query's event types number is equal
		// to the top one
		int middleNum = Utility.getQuery(qtop, this.orderedQueries).stackTypes
				.size();

		double cost1 = topdown_concept(qtop);
		double cost2 = topdownConCost_pattern_middle(middleNum, qbottom);
		cost = topdown_concept(qtop)
				+ topdownConCost_pattern_middle(middleNum, qbottom);

		return cost;
	}

	/**
	 * return the conditional cost for qtop when both levels are changed
	 * 
	 * @param qtop
	 * @param qbottom
	 * @return
	 */
	protected double bottomup_cp(int qtop, int qbottom, int negationPos) {
		double cost = 0;
		int middleNum = Utility.getQuery(qtop, this.orderedQueries).stackTypes
				.size();
		double cost1 = bottomup_p(qbottom, negationPos);
		double cost2 = bottomup_concept(qtop, qbottom);

		cost = bottomup_p(qbottom, negationPos)
				+ bottomup_concept(qtop, qbottom);
		// here, I use the qbottom as the query id for the middle one as the
		// event type level is not changed
		// between the middle one and the bottom one

		return cost;
	}

	/**
	 * return the conditional cost for qtop Need to be changed as this cost only
	 * consider equation (13)
	 * 
	 * @param qtop
	 * @param qbottom
	 * @return
	 */
	protected double bottomup_p(int qbottom, int negationPos) {
		double cost = 0;
		int length = Utility.getQuery(qbottom, this.orderedQueries).stackTypes
				.size();
		String eventType = Utility.getQuery(qbottom, this.orderedQueries).stackTypes
				.get(0);
		double resultSize = resultSize(length, eventType);
		cost = resultSize * Statistics.constantCost * length
				+ singleComputewithNegation_bp(qbottom, negationPos);
		return cost;
	}

	public static void main(String[] args) {
		ConceptTree tree = QueryCompiler.createTree();

		/*
		 * ArrayList<String> stackTypes = new ArrayList<String>();
		 * stackTypes.add("dallas"); stackTypes.add("tulsa");
		 * stackTypes.add("austin"); QueryInfo q1 = new QueryInfo(0,
		 * stackTypes); // ////////////////////////////////////////
		 * ArrayList<String> stackTypes2 = new ArrayList<String>();
		 * stackTypes2.add("dallas"); stackTypes2.add("tulsa");
		 * stackTypes2.add("austin"); stackTypes2.add("OKCity"); QueryInfo q2 =
		 * new QueryInfo(1, stackTypes2); //
		 * //////////////////////////////////////// QueryInfo[][] orderedQueries
		 * = { { q1, null }, { q2, q1 } };
		 * 
		 * int cost = CM.singleCompute(1);
		 * System.out.println("single computing bottom Q2:");
		 * System.out.println(cost); int cost0 = CM.singleCompute(0);
		 * System.out.println("single computing top Q1:");
		 * System.out.println(cost0);
		 * 
		 * double size = CM.resultSize(2); System.out.println("Size 2 :" +
		 * size);
		 * 
		 * double size3 = CM.resultSize(3); System.out.println("Size 3 :" +
		 * size3);
		 * 
		 * double costNeg = CM.singleComputewithNegation_bp(1, 2);
		 * System.out.println("conditional Q1 by bottomup:" + costNeg);
		 * 
		 * double costt = CM.topdownConCost_pattern_middle(3, 1);
		 * System.out.println("conditional Q2 by topdown:" + costt);
		 * 
		 * double cost1 = CM.topdownConCost_pattern(0, 1);
		 * System.out.println("conditional down by topdown:" + cost1); //
		 * //////////////////////////////////////////
		 */
		/*
		 * ArrayList<String> stackTypes3 = new ArrayList<String>();
		 * stackTypes3.add("tx"); stackTypes3.add("ok"); QueryInfo q3 = new
		 * QueryInfo(3, stackTypes3);
		 * 
		 * ArrayList<String> stackTypes4 = new ArrayList<String>(); //
		 * stackTypes4.add("austin"); stackTypes4.add("dallas");
		 * stackTypes4.add("tulsa");
		 * 
		 * QueryInfo q4 = new QueryInfo(4, stackTypes4);
		 * 
		 * QueryInfo[][] orderedQueries2 = { { q3, null }, { q4, q3 } };
		 * CostModel CM = new CostModel(orderedQueries2, tree); int cost4 =
		 * CM.singleCompute(4); System.out.println("single computing bottom Q4:"
		 * + cost4);
		 * 
		 * int cost3 = CM.singleCompute(3);
		 * System.out.println("single computing top Q3:" + cost3);
		 * 
		 * double cost2 = CM.bottomup_concept(3, 4);
		 * System.out.println("conditional Q3 by bottomup_concept:" + cost2); //
		 * topdown_concept(int qtop) double cost5 = CM.topdown_concept(3);
		 * System.out.println("conditional Q4 by topdown_concept:" + cost5);
		 */

		ArrayList<String> stackTypes5 = new ArrayList<String>();
		stackTypes5.add("tx");
		stackTypes5.add("ok");
		QueryInfo q5 = new QueryInfo(5, stackTypes5);

		ArrayList<String> stackTypes6 = new ArrayList<String>();
		stackTypes6.add("austin");
		stackTypes6.add("dallas");

		QueryInfo q6 = new QueryInfo(6, stackTypes6);

		ArrayList<String> stackTypes7 = new ArrayList<String>();
		stackTypes7.add("austin");
		stackTypes7.add("dallas");
		stackTypes7.add("tulsa");
		stackTypes7.add("OKCity");
		stackTypes7.add("OKCity");
		QueryInfo q7 = new QueryInfo(7, stackTypes7);
		QueryInfo[][] orderedQueries3 = { { q6, null }, { q7, q6 } };
		CostModel CM = new CostModel(orderedQueries3, tree);

		/*
		 * QueryInfo q7 = new QueryInfo(7, stackTypes7);
		 * 
		 * QueryInfo[][] orderedQueries3 = { { q5, null }, { q6, q5 }, { q7, q6
		 * } }; CostModel CM = new CostModel(orderedQueries3, tree); int cost5 =
		 * CM.singleCompute(5); System.out.println("single computing top Q5:" +
		 * cost5);
		 * 
		 * int cost6 = CM.singleCompute(6);
		 * System.out.println("single computing bottom Q6:" + cost6);
		 * 
		 * int cost62 = CM.singleCompute(7);
		 * System.out.println("single computing bottom Q7:" + cost62); //
		 * //////// double cost56 = CM.bottomup_concept(5, 6);
		 * System.out.println("conditional Q5 by bottomup_concept:" + cost56);
		 * 
		 * double cost67 = CM.topdownConCost_pattern(6, 7);
		 * System.out.println("conditional Q7 by topdown_pattern:" + cost67);
		 * 
		 * double cost56t = CM.topdown_concept(5);
		 * System.out.println("conditional Q6 by topdown_concept:" + cost56t);
		 * 
		 * double cost76 = CM.bottomup_p(7, 2);
		 * System.out.println("conditional Q6 by bottomup pattern:" + cost76);
		 */

		// bottomup_p(qbottom, negationPos);
		double cost62 = CM.topdownConCost_pattern_sort(6, 7);
		System.out.println(cost62);
		/*
		 * int cost5 = CM.singleCompute(5);
		 * System.out.println("single computing top Q5:" + cost5);
		 * 
		 * int cost6 = CM.singleCompute(6);
		 * System.out.println("single computing bottom Q6:" + cost6); double
		 * cost7 = CM.topdown_cp(5, 6);
		 * System.out.println("conditional Q6 by top down:" + cost7); //
		 * bottomup_cp(int qtop, int qbottom, int negationPos) double cost8 =
		 * CM.bottomup_cp(5, 6, 2);
		 * System.out.println("conditional Q5 by bottomup :" + cost8)
		 */;

		/*
		 * double cost7 = CM.bottomup_concept(3, 4);
		 * System.out.println("conditional Q3 by bottomup_concept:" + cost7); //
		 * topdown_concept(int qtop) double cost8 = CM.topdown_concept(3);
		 * System.out.println("conditional Q4 by topdown_concept:" + cost8);
		 */

	}
}
