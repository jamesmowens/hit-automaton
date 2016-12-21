package com.hp.hpl.CHAOS.queryplangenerator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.hp.hpl.CHAOS.Expression.Expression;

import com.hp.hpl.CHAOS.QueryPlan.PlanNode;
import com.hp.hpl.CHAOS.State.StreamState;
import com.hp.hpl.CHAOS.Statistics.StatisticElement;
import com.hp.hpl.CHAOS.StreamData.Constant;
import com.hp.hpl.CHAOS.StreamData.DoubleSchemaElement;
import com.hp.hpl.CHAOS.StreamData.IntSchemaElement;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.SchemaMap;
import com.hp.hpl.CHAOS.StreamData.Str20SchemaElement;

import com.hp.hpl.CHAOS.StreamOperator.*;
import com.hp.hpl.CHAOS.QueryPlan.PlanDAG;

import com.hp.hpl.CHAOS.Queue.SingleReaderLogQueue;
import com.hp.hpl.CHAOS.Queue.SingleReaderQueueArrayImp;

import com.hp.hpl.CHAOS.Queue.StreamQueue;

public class QueryPlanGenerator {
	private PlanDAG queryPlan;
	private ArrayList<String> sources = new ArrayList<String>();

	public QueryPlanGenerator() {
		queryPlan = new PlanDAG();

	}

	private Node getNodeByName(String name, Node sibling) throws IOException {
		while (sibling != null) {
			if (sibling instanceof Element) {
				if (sibling.getNodeName().equals(name)) {
					return sibling;
				}
			}
			sibling = sibling.getNextSibling();
		}
		return sibling;
	}

	private PlanNode findPlanNodeWithID(PlanDAG planDag, String ID) {
		for (int pindex = 0; pindex < planDag.getNodes().size(); pindex++) {
			PlanNode pNode = planDag.getNodes().get(pindex);
			if (pNode.getNodeID() == Integer.parseInt(ID)) {
				return pNode;
			}
		}
		return null;

	}

	private void setupExpression(PlanDAG planDag, String planName)
			throws SecurityException, ClassNotFoundException, IOException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, SAXException {

		Document document = queryPlanFileParser(planName);

		NodeList expressions = document.getElementsByTagName("expressions");

		ArrayList<IdExpressionPair> pairs = new ArrayList<IdExpressionPair>();

		String input = null, valtype = null, lid = null, rid = null, column = null;
		String type = null, id = null, value = null;

		for (int mapIndex = 0; mapIndex < expressions.getLength(); mapIndex++) {
			Element expr = (Element) expressions.item(mapIndex);
			NodeList exprs = expr.getElementsByTagName("expr");

			for (int i = 0; i < exprs.getLength(); i++) {

				id = exprs.item(i).getAttributes().getNamedItem("id")
						.getNodeValue();

				type = exprs.item(i).getAttributes().getNamedItem("type")
						.getNodeValue();

				if (exprs.item(i).getAttributes().getNamedItem("input") != null) {
					input = exprs.item(i).getAttributes().getNamedItem("input")
							.getNodeValue();
				}

				if (exprs.item(i).getAttributes().getNamedItem("column") != null) {
					column = exprs.item(i).getAttributes().getNamedItem(
							"column").getNodeValue();

				}

				if (exprs.item(i).getAttributes().getNamedItem("valtype") != null) {
					valtype = exprs.item(i).getAttributes().getNamedItem(
							"valtype").getNodeValue();
				}

				if (exprs.item(i).getAttributes().getNamedItem("value") != null) {
					value = exprs.item(i).getAttributes().getNamedItem("value")
							.getNodeValue();
				}

				if (exprs.item(i).getAttributes().getNamedItem("lid") != null) {
					lid = exprs.item(i).getAttributes().getNamedItem("lid")
							.getNodeValue();

				}

				if (exprs.item(i).getAttributes().getNamedItem("rid") != null) {
					rid = exprs.item(i).getAttributes().getNamedItem("rid")
							.getNodeValue();

				}

				// The current op ID.
				Node parentNode = exprs.item(i).getParentNode().getParentNode();

				NamedNodeMap nodeAttributes = parentNode.getAttributes();

				Attr nodeIdentifier = (Attr) nodeAttributes.getNamedItem("id");
				if (nodeIdentifier == null) {
					throw new DOMException(DOMException.SYNTAX_ERR,
							"The ID attribute was not set properly");
				}

				String parentID = nodeIdentifier.getValue();

				
				PlanNode node = findPlanNodeWithID(planDag, parentID);

				PlanNode node2 = findPlanNodeWithID(planDag, input);
				StreamQueue[] node2outQ = node2.getOperator()
						.getOutputQueueArray();
				SchemaElement[] schs = null;
				
				

				int columnIndex = Integer.parseInt(column);
				SchemaElement exE = null;

				boolean loopth = true;
				for (int it = 0; it < node2outQ.length; it++) {
					schs = node2outQ[it].getSchema();
					int queueID = node2outQ[it].queueID;
					for (int it2 = 0; it2 < node.getOperator().getInputQueueArray().length; it2++) {
						int queueID2 = node.getOperator().getInputQueueArray()[it2].queueID;
						if (queueID != queueID2) {
							continue;
						} else {
							loopth = false;
							break;
						}
					}

					if (loopth == false)
						break;
				}

				exE = schs[columnIndex];

				if (valtype == null) {

					String className = "com.hp.hpl.CHAOS.Expression."
							.concat(type);
					Constructor<Expression>[] cstaE = (Constructor<Expression>[]) Class
							.forName(className).getConstructors();
					Constructor<Expression> constructst;
					boolean right_constructorst = false;
					int tempst = 0;
					Expression left = null;

					while (!right_constructorst && tempst < cstaE.length) {
						constructst = cstaE[tempst];
						try {
							SchemaElement schLeft = exE;

							if (value != null) {
								Object[] paramsl = { Double.parseDouble(value) };
								left = (Expression) constructst
										.newInstance(paramsl);
							} else {
								Object[] paramsl = { schLeft };
								left = (Expression) constructst
										.newInstance(paramsl);
							}

							IdExpressionPair pair = new IdExpressionPair(
									Integer.parseInt(id), left);
							pairs.add(pair);

							right_constructorst = true;
						} catch (IllegalArgumentException e) {
							tempst++;
						}
					}
				} else if (i == exprs.getLength() - 1) {
					Constructor<Expression>[] rootCons = (Constructor<Expression>[]) Class
							.forName("com.hp.hpl.CHAOS.Expression." + type)
							.getConstructors();
					Constructor<Expression> rootCon;
					boolean right_rootConstructor = false;
					int tempRoot = 0;
					Expression root = null;

					while (!right_rootConstructor && tempRoot < rootCons.length) {
						rootCon = rootCons[tempRoot];
						try {
							Expression left = pairs
									.get(Integer.parseInt(lid) - 1).value;
							Expression right = pairs
									.get(Integer.parseInt(rid) - 1).value;

							if (valtype.equalsIgnoreCase("EQ")) {
								Object[] paramsl = {
										com.hp.hpl.CHAOS.Expression.Constant.EQ,
										left, right };

								root = (Expression) rootCon
										.newInstance(paramsl);
							} else if (valtype.equalsIgnoreCase("GT")) {
								Object[] paramsl = {
										com.hp.hpl.CHAOS.Expression.Constant.GT,
										left, right };

								root = (Expression) rootCon
										.newInstance(paramsl);
							} else if (valtype.equalsIgnoreCase("LT")) {
								Object[] paramsl = {
										com.hp.hpl.CHAOS.Expression.Constant.LT,
										left, right };

								root = (Expression) rootCon
										.newInstance(paramsl);
							} else if (valtype.equalsIgnoreCase("GEQ")) {
								Object[] paramsl = {
										com.hp.hpl.CHAOS.Expression.Constant.GEQ,
										left, right };

								root = (Expression) rootCon
										.newInstance(paramsl);
							} else if (valtype.equalsIgnoreCase("LEQ")) {
								Object[] paramsl = {
										com.hp.hpl.CHAOS.Expression.Constant.LEQ,
										left, right };

								root = (Expression) rootCon
										.newInstance(paramsl);
							} else if (valtype.equalsIgnoreCase("NEQ")) {
								Object[] paramsl = {
										com.hp.hpl.CHAOS.Expression.Constant.NEQ,
										left, right };

								root = (Expression) rootCon
										.newInstance(paramsl);
							}

							node.getOperator().setExpression(root);

							right_rootConstructor = true;
						} catch (IllegalArgumentException e) {
							tempRoot++;
						}
					}
				}
			}
			pairs = new ArrayList<IdExpressionPair>();
			valtype = null;
			input = null;
			lid = null;
			rid = null;
			column = null;
			type = null;
			value = null;

		}
	}

	private ArrayList<String> ReadSchemaMap(int ID, String planName)
			throws SecurityException, ClassNotFoundException, IOException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, SAXException {

		ArrayList<String> statEList = new ArrayList<String>();

		Document document = queryPlanFileParser(planName);

		NodeList schemaMap = document.getElementsByTagName("schemaMap");
		for (int i = 0; i < schemaMap.getLength(); i++) {
			String outeropID = schemaMap.item(i).getParentNode()
					.getAttributes().getNamedItem("id").getNodeValue();
			if (ID == Integer.parseInt(outeropID)) {
				{
					Element schemaE = (Element) schemaMap.item(i);
					NodeList schemas = schemaE.getElementsByTagName("input");

					for (int len = 0; len < schemas.getLength(); len++) {
						String id = schemas.item(len).getAttributes()
								.getNamedItem("id").getNodeValue();

						String scolumn = schemas.item(len).getAttributes()
								.getNamedItem("scolumn").getNodeValue();

						String schemaString = id.concat("_").concat(scolumn);
						statEList.add(schemaString);
					}
				}
			}
		}
		return statEList;
	}

	private Integer[] getchildOpwithParentID(int ID, String planName)
			throws SecurityException, ClassNotFoundException, IOException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, SAXException {

		ArrayList<Integer> statEList = new ArrayList<Integer>();
		Integer[] statEArray = null;
		Document document = queryPlanFileParser(planName);
		NodeList operators = document.getElementsByTagName("operator");

		for (int i = 0; i < operators.getLength(); i++) {
			String id = operators.item(i).getAttributes().getNamedItem("id")
					.getNodeValue();

			// the current node is the one we are looking for
			if (Integer.parseInt(id) == ID) {
				Element op = (Element) operators.item(i);
				NodeList children = op.getElementsByTagName("child");
				for (int cIndex = 0; cIndex < children.getLength(); cIndex++) {
					String childId = children.item(cIndex).getAttributes()
							.getNamedItem("id").getNodeValue();
					statEList.add(Integer.parseInt(childId));
				}

				break;

			}
		}
		statEArray = (Integer[]) statEList.toArray(new Integer[] {});

		return statEArray;

	}

	private PlanNode[] getparentplanNodewithchildID(int ID, PlanDAG planDag,
			String planName) throws SecurityException, ClassNotFoundException,
			IOException, InstantiationException, IllegalAccessException,
			InvocationTargetException, SAXException {

		ArrayList<PlanNode> statEList = new ArrayList<PlanNode>();
		PlanNode[] statEArray = null;
		Document document = queryPlanFileParser(planName);
		NodeList operators = document.getElementsByTagName("child");

		for (int i = 0; i < operators.getLength(); i++) {
			String id = operators.item(i).getAttributes().getNamedItem("id")
					.getNodeValue();

			if (Integer.parseInt(id) == ID) {
				Node parentNode = operators.item(i).getParentNode()
						.getParentNode();
				String opID = parentNode.getAttributes().getNamedItem("id")
						.getNodeValue();

				int opId = Integer.parseInt(opID);

				for (int parentIndex = 0; parentIndex < planDag.getNodes()
						.size(); parentIndex++) {
					if (planDag.getNodes().get(parentIndex).getNodeID() == opId) {
						statEList.add(planDag.getNodes().get(parentIndex));

					}

				}
			}
		}
		statEArray = (PlanNode[]) statEList.toArray(new PlanNode[] {});

		return statEArray;

	}

	private Integer[] getparentOpwithchildID(int ID, String planName)
			throws SecurityException, ClassNotFoundException, IOException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, SAXException {

		ArrayList<Integer> statEList = new ArrayList<Integer>();
		Integer[] statEArray = null;
		Document document = queryPlanFileParser(planName);
		NodeList operators = document.getElementsByTagName("child");

		for (int i = 0; i < operators.getLength(); i++) {
			String id = operators.item(i).getAttributes().getNamedItem("id")
					.getNodeValue();

			if (Integer.parseInt(id) == ID) {
				Node parentNode = operators.item(i).getParentNode()
						.getParentNode();
				String opID = parentNode.getAttributes().getNamedItem("id")
						.getNodeValue();

				statEList.add(Integer.parseInt(opID));
			}
		}
		statEArray = (Integer[]) statEList.toArray(new Integer[] {});

		return statEArray;

	}

	private SchemaElement[] getsourceSchema(String planName, PlanNode checking)
			throws SecurityException, ClassNotFoundException, IOException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, SAXException {

		ArrayList<SchemaElement> statEList = new ArrayList<SchemaElement>();
		SchemaElement[] statEArray = null;

		Document document = queryPlanFileParser(planName);
		NodeList schemaA = document.getElementsByTagName("schema");
		NodeList operators = null;
		for (int index = 0; index < schemaA.getLength(); index++) {
			String parentId = schemaA.item(index).getParentNode()
					.getAttributes().getNamedItem("id").getNodeValue();

			if (Integer.parseInt(parentId) == checking.getNodeID()) {
				Element schemas = (Element) schemaA.item(index);
				operators = schemas.getElementsByTagName("schemaElement");
				break;
			}
		}

		int offset = 0;
		SchemaElement sch = null;
		if (operators == null)
			return null;
		for (int i = 0; i < operators.getLength(); i++) {
			String typeName = operators.item(i).getAttributes().getNamedItem(
					"type").getNodeValue();
			if (typeName.equalsIgnoreCase("Integer")) {
				sch = new IntSchemaElement("col_A", Constant.INT_T, offset,
						Constant.INT_S);
				offset += sch.getLength();
			}

			else if (typeName.equalsIgnoreCase("Double")) {
				sch = new DoubleSchemaElement("col_B", Constant.DOUBLE_T,
						offset, Constant.DOUBLE_S);
				offset += sch.getLength();
			}

			else if (typeName.equalsIgnoreCase("String")) {
				sch = new Str20SchemaElement("col_C", Constant.STR20_T, offset,
						Constant.STR20_S);
				offset += sch.getLength();
			}
			statEList.add(sch);

		}
		statEArray = (SchemaElement[]) statEList
				.toArray(new SchemaElement[] {});

		return statEArray;

	}

	private void setState(PlanDAG planDag, PlanNode checking, int stateIndex,
			SchemaElement[] schArrayOp, String planName)
			throws SecurityException, ClassNotFoundException, IOException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, SAXException {

		Node checkingNode = null;
		int nodeId = checking.getNodeID();
		Document document = queryPlanFileParser(planName);
		for (int i = 0; i < document.getElementsByTagName("operator")
				.getLength(); i++) {
			checkingNode = document.getElementsByTagName("operator").item(i);

			NamedNodeMap nodeAttributes = checkingNode.getAttributes();

			Attr nodeIdentifier = (Attr) nodeAttributes.getNamedItem("id");
			if (nodeIdentifier == null) {
				throw new DOMException(DOMException.SYNTAX_ERR,
						"The ID attribute was not set properly");
			}

			String id = nodeIdentifier.getValue();

			if (Integer.parseInt(id) == nodeId) {
				break;
			}
		}

		Element stateElementtag = (Element) checkingNode;

		NodeList stateElement = stateElementtag.getElementsByTagName("states");
		NodeList stateElementfirst = null;

		if (stateElement != null) {
			Element stateFirst = (Element) stateElement.item(0);
			if (stateFirst != null)
				stateElementfirst = stateFirst.getElementsByTagName("state");

			if (stateElementfirst != null) {

				NamedNodeMap statenodeAttributes = stateElementfirst.item(
						stateIndex).getAttributes();

				Attr statenodeIdentifier = (Attr) statenodeAttributes
						.getNamedItem("className");

				String statesname = statenodeIdentifier.getValue();

				Attr windowIdentifier = (Attr) statenodeAttributes
						.getNamedItem("windowsize");

				Attr keyIndexIdentifier = (Attr) statenodeAttributes
						.getNamedItem("keyIndex");
				String keyIndex = null;
				if (keyIndexIdentifier != null)
					keyIndex = keyIndexIdentifier.getValue();

				String probeTupleIndex = null;
				Attr probeTupleIndexIdentifier = (Attr) statenodeAttributes
						.getNamedItem("probeTupleIndex");
				if (probeTupleIndexIdentifier != null)
					probeTupleIndex = probeTupleIndexIdentifier.getValue();

				String windowSize = null;

				if (windowIdentifier != null)
					windowSize = windowIdentifier.getValue();

				checking.getOperator().classVariableSetup("className",
						statesname);

				checking.getOperator().classVariableSetup("windowSize",
						windowSize);

				if (keyIndex != null && probeTupleIndex != null) {
					checking.getOperator().classVariableSetup("keyIndex",
							keyIndex);
					checking.getOperator().classVariableSetup(
							"probeTupleIndex", probeTupleIndex);
				}

			}

		}

	}

	private int getStateNumber(PlanDAG planDag, PlanNode checking,
			String planName) throws SAXException, IOException {

		int num = 0;
		Node checkingNode = null;
		int nodeId = checking.getNodeID();
		Document document = queryPlanFileParser(planName);
		for (int i = 0; i < document.getElementsByTagName("operator")
				.getLength(); i++) {
			checkingNode = document.getElementsByTagName("operator").item(i);

			NamedNodeMap nodeAttributes = checkingNode.getAttributes();

			Attr nodeIdentifier = (Attr) nodeAttributes.getNamedItem("id");
			if (nodeIdentifier == null) {
				throw new DOMException(DOMException.SYNTAX_ERR,
						"The ID attribute was not set properly");
			}

			String id = nodeIdentifier.getValue();

			if (Integer.parseInt(id) == nodeId) {
				break;
			}
		}

		Element stateElementtag = (Element) checkingNode;
		Element stateFirst = null;
		NodeList stateElementfirst = null;

		NodeList stateElement = stateElementtag.getElementsByTagName("states");
		if (stateElement != null) {
			stateFirst = (Element) stateElement.item(0);
			if (stateFirst != null)
				stateElementfirst = stateFirst.getElementsByTagName("state");
			if (stateElementfirst != null)
				num = stateElementfirst.getLength();
		}
		return num;
	}

	private StatisticElement[] getStatistics(Node nextNode)
			throws SecurityException, ClassNotFoundException, IOException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException {
		Node statistics = getNodeByName("Statistic", nextNode);
		ArrayList<StatisticElement> statEArrayList = new ArrayList<StatisticElement>();

		if (statistics != null) {
			Element statisElementtag = (Element) statistics;

			NodeList statisticsElement = statisElementtag
					.getElementsByTagName("StatisticElement");

			if (statisticsElement != null) {
				for (int statIndex = 0; statIndex < statisticsElement
						.getLength(); statIndex++) {
					NamedNodeMap statnodeAttributes = statisticsElement.item(
							statIndex).getAttributes();

					Attr statnodeIdentifier = (Attr) statnodeAttributes
							.getNamedItem("name");

					String statisname = statnodeIdentifier.getValue();
					statisname = "com.hp.hpl.CHAOS.Statistics."
							.concat(statisname);

					// setStatArray

					Constructor<StatisticElement>[] cstaE = (Constructor<StatisticElement>[]) Class
							.forName(statisname).getConstructors();
					Constructor<StatisticElement> constructst;
					boolean right_constructorst = false;
					int tempst = 0;
					StatisticElement staEle = null;

					while (!right_constructorst && tempst < cstaE.length) {
						constructst = cstaE[tempst];
						try {
							staEle = (StatisticElement) constructst
									.newInstance();

							right_constructorst = true;
						} catch (IllegalArgumentException e) {
							tempst++;
						}
					}
					statEArrayList.add(staEle);

				}

				StatisticElement[] statEArray = (StatisticElement[]) statEArrayList
						.toArray(new StatisticElement[] {});
				return statEArray;
			} else
				return null;
		}

		else
			return null;

	}

	@SuppressWarnings("unchecked")
	private void parseOperator(Node operatorElement) throws Exception {

		/** first make sure we were given an element */
		if (operatorElement.getNodeType() != Node.ELEMENT_NODE) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,
					"The parameter was not a DOM Element");
		}
		/** next make sure we were given an operator element */
		if (!operatorElement.getNodeName().equalsIgnoreCase("operator")) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,
					"The parameter was not an operator Element");
		}

		Element opElement = (Element) operatorElement;

		NamedNodeMap nodeAttributes = operatorElement.getAttributes();

		Attr nodeIdentifier = (Attr) nodeAttributes.getNamedItem("id");
		if (nodeIdentifier == null) {
			throw new DOMException(DOMException.SYNTAX_ERR,
					"The ID attribute was not set properly");
		}

		String id = nodeIdentifier.getValue();

		PlanNode dagNode = null;

		/** first get the className attribute */
		nodeIdentifier = (Attr) nodeAttributes.getNamedItem("className");
		if (nodeIdentifier == null) {
			throw new DOMException(DOMException.SYNTAX_ERR,
					"The className attribute was not set properly");
		}
		String className = nodeIdentifier.getValue();
		if(!className.contains("."))
		{
			className = "com.hp.hpl.CHAOS.StreamOperator.".concat(className);
		}

		Node nextNode = operatorElement.getFirstChild(),

		children = null;
		NodeList childElement = null;

		StatisticElement[] statArray = getStatistics(nextNode);

		children = getNodeByName("children", nextNode);

		Element childElementtag = (Element) children;

		if (childElement != null) {
			childElement = childElementtag.getElementsByTagName("child");
		}

		Constructor<StreamOperator>[] c1 = (Constructor<StreamOperator>[]) Class
				.forName(className).getConstructors();
		Constructor<StreamOperator> construct;

		boolean right_constructor = false;
		int temp = 0;
		StreamOperator operator = null;

		while (!right_constructor && temp < c1.length) {
			construct = c1[temp];
			try {

				Object[] params = { Integer.parseInt(id), null, null };

				operator = (StreamOperator) construct.newInstance(params);

				right_constructor = true;
			} catch (IllegalArgumentException e) {
				temp++;
			}
		}

		if (statArray == null)
			statArray = new StatisticElement[] {};

		operator.setStatisticArray(statArray);
		// classvariable setup
		NodeList classVariables = opElement
				.getElementsByTagName("classVariables");
		String key = null;
		String value = null;

		if (classVariables.getLength() > 0) {
			NodeList parameters = classVariables.item(0).getChildNodes();
			
			//here call the CompareandGroupQueries function to reorganize queries
			NodeList merged_parameters = null;
			if (parameters.item(1).getNodeName() == "query"){
				merged_parameters = CompareandGroupQueries(parameters);
			}
			else{
			    merged_parameters = parameters;
			}
			//pass the key value pairs to set up classvariables
			int length = merged_parameters.getLength();
			for (int i = 1; i < length; i++) {
				key = merged_parameters.item(i).getNodeName();
				//System.out.print(key);
				if (key != "states") {
					value = merged_parameters.item(i).getAttributes().item(0)
							.getNodeValue();
					//System.out.print(value);
					if (key != null) {
						operator.classVariableSetup(key, value);
					}
				}
				i += 1;
			}
		}

		dagNode = new PlanNode(operator, Integer.parseInt(id));

		queryPlan.addNode(dagNode);

	}
	
	/**
	 * This function will take the original queries as input, compare the patterns of all queries 
	 * and merged those with same pattern but different windows as one query
	 * 
	 * @param parameters:a bunch of queries in format Òquery1 name = "(A,B,C) id=3 window=100"Ó
	 * @return merged_parameters in format Òquery1 name = "(A,B,C) id=3 id=5 window=100 window=500"Ó
	 */
	public NodeList CompareandGroupQueries (NodeList parameters) {

		//initialize the first query as the previous pattern to compare
		String pre_query; 
		String curr_query;
		
		//store the merged queries
		NodeList merged_parameters = parameters;
		
		//store the SEQ pattern for compare
		String pre_pattern; 
		String curr_pattern;
		String curr_window_id;
		String key;

		int k = 1;
		key = parameters.item(1).getNodeName();  
		pre_query = parameters.item(1).getAttributes().item(0).getNodeValue();		 		
		Node next = parameters.item(1).getNextSibling().getNextSibling();
		Node previous;
		Node pre1;
		Node next1;
		while (next != null){	
				
			pre_pattern = pre_query.substring(pre_query.indexOf("(")+1, pre_query.indexOf(")"));
			curr_query = next.getAttributes().item(0).getNodeValue();
			curr_pattern = curr_query.substring(curr_query.indexOf("(")+1, curr_query.indexOf(")"));
				
			//compare the SEQ pattern of current query with previous one
			if(curr_pattern.equalsIgnoreCase(pre_pattern)){
				curr_window_id = curr_query.substring(curr_query.indexOf(")")+2,curr_query.length());
				//append new window and id to the existing same pattern query
				pre_query = (new StringBuilder(pre_query)).append(" ").append(curr_window_id).toString();
				//rewrite the new query to merged_parameters
				previous = next.getPreviousSibling().getPreviousSibling();
				previous.getAttributes().item(0).setNodeValue(pre_query);
				previous = next;
				next = next.getNextSibling().getNextSibling();
				//remove this query and its previous sibling and next sibling
				pre1 = previous.getPreviousSibling();
				next1 = previous.getNextSibling();
				//pre1.getParentNode().removeChild(pre1);
				next1.getParentNode().removeChild(next1);
				previous.getParentNode().removeChild(previous);
			}
			else{
				pre_query = curr_query;
				next = next.getNextSibling().getNextSibling();
			}
		}	
		return merged_parameters;

	}

	public Document queryPlanFileParser(String fileName) throws SAXException,
			IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		Document document = null;

		document = builder.parse(fileName);

		return document;

	}

	/**
	 * This will first parse the file, find all operator elements, then create
	 * operators one by one. Then the data models (table or queue) will be
	 * connected
	 * 
	 */

	public PlanDAG ConstuctPlanNodes(String planName)
			throws ParserConfigurationException, SAXException, IOException {
		Document document = queryPlanFileParser(planName);

		NodeList operators = document.getElementsByTagName("operator");

		for (int i = 0; i < operators.getLength(); i++) {
			try {
				parseOperator(operators.item(i));

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return queryPlan;
	}

	private void replacewithID(File fileName) throws IOException {
		String line = null;
		int times = sources.size();
		{

			String fileN = "internal_".concat(fileName.toString().substring(5,
					fileName.toString().length()));

			File toFileFinal = new File(fileN);

			File fromFile = fileName;

			// ... Loop as long as there are input lines.

			BufferedReader readerTemp = new BufferedReader(new FileReader(
					fromFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					toFileFinal));

			while ((line = readerTemp.readLine()) != null) {
				for (int i = 0; i < times; i++) {
					String replace = sources.get(i);
					int index = line.indexOf(replace);
					if (index != -1) {

						line = line.replace(replace, String.valueOf(Math
								.abs(replace.hashCode())));

					}
				}

				writer.write(line);
				writer.newLine(); // Write system dependent end of line.

			}
			// ... Close reader and writer.
			readerTemp.close(); // Close to unlock.
			writer.close();
		}

	}

	public void copyFile(String planName, ArrayList<String> sourceFile)
			throws IOException {

		File planFile = new File(planName); // File to read from.

		File toFile = new File("temp_".concat(planName));

		if (!toFile.exists()) {
			toFile.createNewFile();
		}

		// ... Loop as long as there are input lines.
		BufferedReader reader = new BufferedReader(new FileReader(planFile));

		BufferedWriter writer = new BufferedWriter(new FileWriter(toFile));
		BufferedReader readerSource = null;
		// ... Loop as long as there are input lines.
		String line = null;
		String sourceLine = null;
		while ((line = reader.readLine()) != null) {
			if (!line.equalsIgnoreCase("</queryplan>")) {
				writer.write(line);
				writer.newLine(); // Write system dependent end of line.

			} else {
				for (int times = 0; times < sourceFile.size(); times++) {
					File sourFile = new File(sourceFile.get(times));
					readerSource = new BufferedReader(new FileReader(sourFile));
					// read source
					while ((sourceLine = readerSource.readLine()) != null) {
						writer.write(sourceLine);
						writer.newLine();

					}
				}

				readerSource.close();
				writer.write(line);
				writer.newLine();

			}

		}

		reader.close(); // Close to unlock.
		writer.close(); // Close to unlock and flush to disk.

	}

	public void addingSourceFiles(String planName) throws SAXException,
			IOException {
		Document document = queryPlanFileParser(planName);

		NodeList children = document.getElementsByTagName("child");
		for (int i = 0; i < children.getLength(); i++) {
			Node operatorElement = children.item(i);
			Element opElement = (Element) operatorElement;
			Attr idAttr = (Attr) opElement.getAttributes().getNamedItem("id");
			String idAtt = idAttr.getValue();
			try {
				Integer.parseInt(idAtt);
			} catch (NumberFormatException e) {

				String sourceFile = idAtt;
				if (sources.size() == 0) {
					sources.add(sourceFile);
				} else {
					int index = 0;
					for (; index < sources.size(); index++) {
						if (sources.get(index).equalsIgnoreCase(sourceFile))
							break;
					}
					if (index >= sources.size())
						sources.add(sourceFile);

				}

			}

		}

		copyFile(planName, sources);

	}

	public void clearnupInternalFiles(String newplanName) {
		File toFileFinal = new File(newplanName);
		if (toFileFinal.exists()) {
			toFileFinal.delete();
		}
		int index = "internal_".length();
		String name = "temp_".concat(newplanName.substring(index, newplanName
				.length()));
		File toFileFinal2 = new File(name);
		if (toFileFinal2.exists()) {
			toFileFinal2.delete();
		}

	}

	public PlanDAG generateQueryPlan(String planName) {

		try {
			File toFileFinal = new File(planName);
			if (toFileFinal.exists()) {
				addingSourceFiles(planName);
				replacewithID(new File("temp_".concat(planName)));

				String newplanName = "internal_".concat(planName);
				queryPlan = (PlanDAG) ConstuctPlanNodes(newplanName);

				queryPlan = constructPlanQueuesStates(queryPlan, newplanName);

				// set expression if any
				setupExpression(queryPlan, newplanName);
				clearnupInternalFiles(newplanName);
			} else {
				System.out
						.println("The system cannot find the file specified)");
			}
			;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return queryPlan;
	}

	public void constructOutputQueues(PlanDAG planDag, StreamOperator op,
			SchemaElement[] schA, String planName) throws SecurityException,
			ClassNotFoundException, IOException, InstantiationException,
			IllegalAccessException, InvocationTargetException, SAXException {

		int oID = op.getOperatorID();
		Boolean set1th = false;
		Integer[] ops = getparentOpwithchildID(oID, planName);

		ArrayList<StreamQueue> queueList = new ArrayList<StreamQueue>();

		for (int opindex = 0; opindex < ops.length; opindex++) {

			StreamQueue inputQueueA = null;

			if (readLog(planName, oID) && set1th == false) {
				inputQueueA = createLogQueueBySchema(planName, op.getClass()
						.getSimpleName(), op.getOperatorID(), schA);
				set1th = true;

			} else {
				inputQueueA = createQueueBySchema(schA);

			}
			queueList.add(inputQueueA);

		}

		StreamQueue[] ouputQueues = (StreamQueue[]) queueList
				.toArray(new StreamQueue[] {});

		op.setOutputQueues(ouputQueues);
	}

	public PlanDAG constructPlanQueuesStates(PlanDAG planDag, String planName)
			throws SAXException, IOException, SecurityException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, InvocationTargetException,
			CloneNotSupportedException {
		// first let us find out how many output queues the source operator has.

		Integer[] ops = null;

		ArrayList<Integer> opsbackup = new ArrayList<Integer>();

		int count = 0;
		int opsIndex2 = 0;
		int itemIndex = 0;

		for (int sinkIndex = 0; sinkIndex < planDag.getRoots().size(); sinkIndex++) {
			itemIndex = sinkIndex;
			while (true) {

				int oID = planDag.getNodes().get(itemIndex).getNodeID();

				int opsIndex = 0;

				if (ops == null || opsIndex2 == opsbackup.size()) {
					ops = getchildOpwithParentID(oID, planName);
					
					{

						int childSize = opsbackup.size();
						for (int i = 0; i < childSize && i < opsbackup.size();) {
							if (opsbackup.get(i).intValue() == oID) {
								opsbackup.remove(i);
								i = 0;

							} else {
								i++;
							}
						}

					}
					if (ops.length != 0) {
						for (int it = 0; it < ops.length; it++) {
							opsbackup.add(ops[it]);
						}

						opsIndex2 = 0;
					} else {
						break;
					}

				} else {
					ops = getchildOpwithParentID(oID, planName);
					{
						int childSize = opsbackup.size();
						for (int i = 0; i < childSize && i < opsbackup.size();) {
							if (opsbackup.get(i).intValue() == oID) {
								opsbackup.remove(i);
								i = 0;
								opsIndex2 = 0;
								
							} else {
								i++;
							}
						}
					}
				
					for (int it = 0; it < ops.length; it++) {
						opsbackup.add(ops[it]);
					}

				}

				if (ops.length == 0 && opsbackup.size() == 0) {
					break;
				}
				for (int j = 0; j < ops.length; j++) {

					for (int index2 = 0; index2 < planDag.getNodes().size(); index2++) {
						if (opsIndex < ops.length
								&& planDag.getNodes().get(index2).getNodeID() == ops[opsIndex]) {
							// set parent child relationship
							PlanDAG.addParentChild(planDag.getNodes().get(
									itemIndex), planDag.getNodes().get(index2));

							planDag.getNodes().get(itemIndex).setBitmap(
									(byte) 3);
							count++;
							opsIndex++;

						}
						if (opsIndex == ops.length)
							break;

					}

				}

				for (int index = 0; index < planDag.getNodes().size(); index++) {

					if (opsbackup.size() > 0
							&& opsIndex2 < opsbackup.size()
							&& planDag.getNodes().get(index).getNodeID() == opsbackup
									.get(opsIndex2)) {
						if (planDag.getNodes().get(index).getBitmap() == 3)
							continue;
						else {
							itemIndex = index;
							opsIndex2++;
							break;

						}

					}
				}

			}

		}

		PlanNode[] parentNodes = {};

		StreamOperator checkingOp = null;
		ArrayList<PlanNode> nodeList = new ArrayList<PlanNode>();

		nodeList.addAll(planDag.getLeaves());

		// mark leafNodes first;
		for (int i = 0; i < nodeList.size(); i++) {
			nodeList.get(i).setBitmap((byte) 2);
		}

		int check = 0;

		while (!nodeList.isEmpty()) {
			PlanNode checkingNode = null;
			if (check > nodeList.size())
				break;
			else
				checkingNode = nodeList.get(check);
			Boolean iteration = false;
			checkingOp = checkingNode.getOperator();
			ArrayList<StreamQueue> outputQueueList = new ArrayList<StreamQueue>();
			ArrayList<StreamQueue> inputQueueList = new ArrayList<StreamQueue>();

			int leafID = checkingOp.getOperatorID();
			// remove this leaf from current nodeList
			parentNodes = getparentplanNodewithchildID(leafID, planDag,
					planName);

			// check bitmap first, if the node has been set up before, we don't
			// touch it
			if (checkingNode.getBitmap() == (byte) 1) {
				nodeList.remove(checkingNode);
				continue;
			} else {

				// if the current node is the parent of some op
				if (checkingNode.getBitmap() != (byte) 2) {
					// first we need to get all its children
					// parent child need to set input queue also.
					int childrenNum = checkingNode.getChildren().size();
					for (int i = 0; i < childrenNum; i++) {

						// get the index of the parent.
						PlanNode child = checkingNode.getChild(i);
						ArrayList<PlanNode> parents = child.getParent();
						int parentIndex = 0;
						for (int pIndex = 0; pIndex < parents.size(); pIndex++) {
							if (parents.get(pIndex).equals(checkingNode)) {
								parentIndex = pIndex;
								break;
							}
						}

						StreamQueue[] sqs = checkingNode.getChild(i)
								.getOperator().getOutputQueueArray();
						if (sqs == null) {
							check++;
							iteration = true;
							break;
						}

						else {
							StreamQueue inputQ = checkingNode.getChild(i)
									.getOperator().getOutputQueueArray()[parentIndex];
							inputQueueList.add(inputQ);
						}

					}
					if (iteration == true) {
						continue;
					} else {

						StreamQueue[] ouputQueues = (StreamQueue[]) inputQueueList
								.toArray(new StreamQueue[] {});

						checkingOp.setInputQueueArray(ouputQueues);

						// set up state if any

						// We should add each state according to each input
						// child's
						// schema separately.
						int stateNum = getStateNumber(planDag, checkingNode,
								planName);
						StreamState[] st = new StreamState[stateNum];

						if (stateNum > 0) {
							checkingOp.setStateArray(st);
						}

						for (int stateIndex = 0; stateIndex < stateNum; stateIndex++) {
							SchemaElement[] stateSch = checkingOp
									.getInputQueueArray()[stateIndex]
									.getSchema();

							setState(planDag, checkingNode, stateIndex,
									stateSch, planName);

						}

						// set schemamap if any

						ArrayList<String> schemaArray = ReadSchemaMap(
								checkingOp.getOperatorID(), planName);

						SchemaElement[] schArr = null;
						if (schemaArray.size() == 0
								&& checkingOp.getInputQueueArray().length > 0) {

							// added to support output like aggregation.
							SchemaElement[] outputSchemaA = getsourceSchema(
									planName, checkingNode);
							if (outputSchemaA == null) {

								schArr = checkingOp.getInputQueueArray()[0]
										.getSchema();
								constructOutputQueues(planDag, checkingOp,
										schArr, planName);

							} else {

								constructOutputQueues(planDag, checkingOp,
										outputSchemaA, planName);

							}

						} else {
							SchemaMap smap = new SchemaMap();
							ArrayList<SchemaElement> pairs = new ArrayList<SchemaElement>();

							for (int entry = 0; entry < schemaArray.size(); entry++) {
								int endIndex = schemaArray.get(entry).indexOf(
										"_");

								int inputID = Integer.parseInt(schemaArray.get(
										entry).substring(0, endIndex));
								int srcColumn = Integer
										.parseInt(schemaArray.get(entry)
												.substring(
														endIndex + 1,
														schemaArray.get(entry)
																.length()));

								// find out child op output queue schema
								for (int i = 0; i < checkingNode.getChildren()
										.size(); i++) {
									if (checkingNode.getChildren().get(i)
											.getOperator().getOperatorID() == inputID) {

										

										SchemaElement[] schs = null;
										boolean loopth = true;
										// find out child op output queue schema
										for (int in = 0; in < checkingNode
												.getChildren().size(); in++) {
											if (checkingNode.getChildren().get(
													in).getOperator()
													.getOperatorID() == inputID) {

												StreamQueue[] queues = checkingNode
														.getChildren().get(in)
														.getOperator()
														.getOutputQueueArray();
												for (int it = 0; it < queues.length; it++) {
													schs = queues[it]
															.getSchema();
													int queueID = queues[it].queueID;
													StreamQueue[] queuesParent = checkingNode
															.getOperator()
															.getInputQueueArray();
													for (int it2 = 0; it2 < queuesParent.length; it2++) {
														int queueID2 = queuesParent[it2].queueID;
														if (queueID != queueID2) {
															continue;
														} else {
															loopth = false;
															break;
														}
													}

													if (loopth == false)
														break;
												}
											}
										}

										

										SchemaElement sch0 = schs[srcColumn];

										pairs.add(sch0);
									}
								}
							}
							ArrayList<SchemaElement> outputSchema = new ArrayList<SchemaElement>();

							int counter = 0, offset = 0;
							for (; counter < pairs.size(); counter++) {

								SchemaElement sch = pairs.get(counter);
								SchemaElement sch1 = null;
								try {
									sch1 = (SchemaElement) sch.clone();
								} catch (CloneNotSupportedException e) {
									e.printStackTrace();
								}
								sch1.setOffset(offset);
								outputSchema.add(sch1);
								smap.addEntry(sch, sch1);

								offset += sch1.getLength();

							}

							// added to support output like aggregation.
							SchemaElement[] outputSchemaA = getsourceSchema(
									planName, checkingNode);

							if (outputSchemaA == null
									&& outputSchema.size() == 0
									&& checkingOp.getInputQueueArray().length > 0) {
								schArr = checkingOp.getInputQueueArray()[0]
										.getSchema();
							} else {
								if (outputSchemaA == null) {
									schArr = (SchemaElement[]) outputSchema
											.toArray(new SchemaElement[0]);
								} else {
									schArr = (SchemaElement[]) outputSchemaA;
								}
							}

							// set smap
							checkingOp.setSMap(smap);
							// output queue schema is determined by the right
							// side
							// schemas in the querymap.

							Boolean set1th = false;
							StreamQueue inputQueueA = null;
							for (int opindex = 0; opindex < parentNodes.length; opindex++) {

								if (readLog(planName, checkingOp
										.getOperatorID())
										&& set1th == false) {
									inputQueueA = createLogQueueBySchema(
											planName, checkingOp.getClass()
													.getSimpleName(),
											checkingOp.getOperatorID(), schArr);
									set1th = true;

								} else {
									inputQueueA = createQueueBySchema(schArr);

								}
								outputQueueList.add(inputQueueA);

							}

							// output queue need to be set no matter what.
							StreamQueue[] ouputQueueA = (StreamQueue[]) outputQueueList
									.toArray(new StreamQueue[] {});
							checkingOp.setOutputQueueArray(ouputQueueA);

						}
					}
				}

				else if (checkingNode.getBitmap() == (byte) 2) {
					Boolean set1th = false;
					for (int opindex = 0; opindex < parentNodes.length; opindex++) {

						SchemaElement[] sourceSchemaA = getsourceSchema(
								planName, checkingNode);

						if (readLog(planName, checkingOp.getOperatorID())
								&& set1th == false) {
							StreamQueue inputQueueA = createLogQueueBySchema(
									planName, checkingOp.getClass()
											.getSimpleName(), checkingOp
											.getOperatorID(), sourceSchemaA);
							set1th = true;
							outputQueueList.add(inputQueueA);
						} else {
							StreamQueue inputQueueA = new SingleReaderQueueArrayImp(
									sourceSchemaA);
							outputQueueList.add(inputQueueA);
						}

					}

					// output queue need to be set no matter what.
					StreamQueue[] ouputQueues = (StreamQueue[]) outputQueueList
							.toArray(new StreamQueue[] {});
					checkingOp.setOutputQueueArray(ouputQueues);
				}

				// set bitmap if untouched before
				checkingNode.setBitmap((byte) 1);
				nodeList.remove(checkingNode);

				// add parent Nodes to nodeList

				for (int parentIndex = 0; parentIndex < parentNodes.length; parentIndex++) {

					nodeList.add(parentNodes[parentIndex]);

				}

			}
			check = 0;
		}

		return planDag;
	}

	public Boolean readLog(String planName, int opID) throws SAXException,
			IOException {
		Document document = queryPlanFileParser(planName);
		NodeList ops = document.getElementsByTagName("operator");
		String log = null;

		for (int i = 0; i < ops.getLength(); i++) {
			Node opNode = ops.item(i);
			Attr nodeIdentifierID = (Attr) opNode.getAttributes().getNamedItem(
					"id");
			if (Integer.parseInt(nodeIdentifierID.getValue()) == opID) {
				Attr nodeIdentifier = (Attr) opNode.getAttributes()
						.getNamedItem("log");
				if (nodeIdentifier == null) {
					throw new DOMException(DOMException.SYNTAX_ERR,
							"The ID attribute was not set properly");
				}

				log = nodeIdentifier.getValue();
				break;
			}

		}
		return Boolean.parseBoolean(log);

	}

	public SingleReaderLogQueue createLogQueueBySchema(String planName,
			String opName, int opID, SchemaElement[] schArray)
			throws SAXException, IOException {

		int length = "internal_".length();
		String fileName = planName.substring(length, planName.length() - 4)
				.concat("_");
		SingleReaderLogQueue queueA = new SingleReaderLogQueue(schArray,
				fileName);

		fileName = fileName.concat("id" + String.valueOf(opID)).concat(".log");
		queueA.setLogFileName(fileName);
		return queueA;
	}

	public StreamQueue createQueueBySchema(SchemaElement[] schArray) {
		SingleReaderQueueArrayImp queueA = new SingleReaderQueueArrayImp(
				schArray);
		return queueA;
	}

	public String toString() {
		String ret = new String();

		ret += "Query Plan: " + "\n";
		ret += "\t DAG \n";
		ret += queryPlan.toString();
		return ret;
	}

	public static void main(String[] args) throws ParserConfigurationException,
			SAXException, IOException {
		try {
			QueryPlanGenerator qp = new QueryPlanGenerator();

			PlanDAG queryPlan = qp.generateQueryPlan("query2.xml");

			System.out.println(queryPlan);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
