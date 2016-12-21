/**
 * 
 */
package com.hp.hpl.CHAOS.Rewriting;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Kara Greenfield, Mo Liu One thing we forgot is HQP has trigger
 *         assertion to indicate the last event type in a query also. Here, we
 *         can think it like for the edge with step 0, the key in hash1 is the
 *         trigger assertion also.
 * */
public class HierarchicalQueryPlan {
	Hashtable<String, Hashtable> hash1 = new Hashtable<String, Hashtable>();
	ArrayList<String> stackTypes = null;
	ArrayList<String> allstackTypes = null;
	ConceptTree tree = null;
	ArrayList<QueryInfo> queries = new ArrayList<QueryInfo>();

	ArrayList<String> getAllstackTypes() {
		ArrayList<String> types = new ArrayList<String>();
		for (int i = 0; i < queries.size(); i++) {
			ArrayList<String> typesi = queries.get(i).getStackTypes();
			for (int j = 0; j < typesi.size(); j++) {
				if (!types.contains(typesi.get(j)))
					types.add(typesi.get(j));
			}
		}
		return types;

	}

	public ArrayList<QueryInfo> getQueries() {
		return queries;
	}

	public void setqueries(ArrayList<QueryInfo> q) {
		queries = q;
	}

	public String toString() {
		String returned = null;
		Set<String> set = hash1.keySet();

		Iterator<String> itr = set.iterator();

		while (itr.hasNext()) {
			String str = itr.next();
			System.out.println("==hash1 key==");
			System.out.println(str);

			System.out.println(str + ": " + hash1.get(str));

			Hashtable<String, ArrayList<EdgeLabel>> table2 = hash1.get(str);

			Set<String> set2 = table2.keySet();

			Iterator<String> itr2 = set2.iterator();

			while (itr2.hasNext()) {
				String str2 = itr2.next();
				System.out.println("==hash2 key==");
				System.out.println(str2);

				for (int i = 0; i < table2.get(str2).size(); i++) {
					System.out.println("edge"
							+ table2.get(str2).get(i).getQueryID()
							+ table2.get(str2).get(i).getStep());
				}

			}

		}

		return returned;
	}

	/**
	 * default constructor
	 * 
	 */
	public HierarchicalQueryPlan() {
		stackTypes = new ArrayList<String>();
		tree = new ConceptTree();
	}

	/**
	 * check existence of key/value and update hashtables key exist, value exist
	 * (update label) key exist, value not exist (update hash2) key not exist
	 * (no change, create new hash tables)
	 */
	protected boolean checkKeyExist(String newkey) {

		Set<String> set = hash1.keySet();
		boolean exist = false;

		Iterator<String> itr = set.iterator();

		while (itr.hasNext()) {
			String str = itr.next();

			if (str.equalsIgnoreCase(newkey)) {
				exist = true;
				break;
			}
		}
		return exist;
	}

	protected boolean checkKeyValueExist(String newkey, String newvalue) {

		Set<String> set = hash1.keySet();
		boolean exist = false;

		Iterator<String> itr = set.iterator();

		while (itr.hasNext()) {
			String str = itr.next();

			if (str.equalsIgnoreCase(newkey)) {
				{
					// update value
					Hashtable<String, EdgeLabel> table2 = hash1.get(str);

					Set<String> set2 = table2.keySet();

					Iterator<String> itr2 = set2.iterator();

					while (itr2.hasNext()) {
						String str2 = itr2.next();

						if (str2.equalsIgnoreCase(newvalue)) {

							// both of the key and value exist, so just update
							// the
							// queryID and stepID
							exist = true;
							break;

						}

					}
				}
			}

		}
		return exist;
	}

	/**
	 * overloaded constructor
	 * 
	 */
	public HierarchicalQueryPlan(ArrayList<String> stackTypes, ConceptTree tree) {
		this.stackTypes = stackTypes;
		this.tree = tree;
	}

	/**
	 * resets the stack types array
	 * 
	 * @param stackTypes
	 *            new array of stack types
	 */
	public void setStackTypes(ArrayList<String> stackTypes) {
		this.stackTypes = stackTypes;
	}

	/**
	 * resets the concept tree
	 * 
	 * @param tree
	 *            new concept tree
	 */
	public void setTree(ConceptTree tree) {
		this.tree = tree;
	}

	/**
	 * retrieves the stack types array
	 * 
	 * @return array of stack types
	 */
	public ArrayList<String> getStackTypes() {
		return stackTypes;
	}

	/**
	 * retrieves the concept tree
	 * 
	 * @return concept tree
	 */
	public ConceptTree getTree() {
		return tree;
	}

	/**
	 * creates the hash table that stores the hierarchical pattern graph
	 * 
	 */
	public void createHashtable(int queryID) {

		// check whether such key and value already exist
		int step = stackTypes.size() - 2;
		for (int i = 0; i < stackTypes.size(); i++) {

			// set up hierarchical pattern graph
			if (i != stackTypes.size() - 1) {

				String key = stackTypes.get(i + 1).toLowerCase();

				// I should re-consider the relative position for key and value
				String value = stackTypes.get(i).toLowerCase();

				// one key could map to multiple values
				ArrayList<String> values = new ArrayList<String>();

				// I extend values with multiple negations
				for (int n = 1; n < i; n++) {
					String nvalue = stackTypes.get(i - n).toLowerCase();
					if (nvalue.startsWith("-")) {
						nvalue = nvalue.substring(1, nvalue.length());

						// check existence before adding in
						if (!values.contains(nvalue))
						{
							if(!key.startsWith("-")) // value was negation and can be added in only if key is not negation
							values.add(nvalue);
						}
							

					} else {

						if (!values.contains(nvalue) && value.startsWith("-")) //for multiple negation
						{
							values.add(nvalue);

						}
						if(!value.startsWith("-"))	
						break;
					} // break until we see a positive event type and add it in
				}

				// extended to support multiple negations between event types
				//
				if (key.startsWith("-") && value.startsWith("-")) {
					// we don't build up index between them,
					value = null;
				}

				String precedingValue = new String();
				if (value != null && value.startsWith("-")) {
					if (i > 0)
						precedingValue = stackTypes.get(i - 1).toLowerCase();
					else {
						precedingValue = null;
					}

					value = value.substring(1, value.length());
				}

				if (key.startsWith("-")) {
					key = key.substring(1, key.length());
				}

				// key should also includes tulsa
				ArrayList<String> keys = new ArrayList<String>();
				ArrayList<String> allETypes = getAllstackTypes();
				for (int iter = 0; iter < allETypes.size(); iter++) {
					String existType = allETypes.get(iter);

					if (existType.startsWith("-")) {
						existType = existType.substring(1, existType.length());
					}

					if (Utility.semanticMatch(key, existType, tree))
						keys.add(existType);

				}

				/*
				 * if (key.equalsIgnoreCase("tulsa") &&
				 * value.equalsIgnoreCase("dallas"))
				 * System.out.print("checking tulsa point");
				 */
				// ok, I found a bug again, I should find out in the all stack
				// types,
				// the children of the value also
				// and for each such key value pair, the hpg should has a
				// corresponding one
				// ArrayList<String> allETypes = getAllstackTypes();
				for (int iter = 0; iter < allETypes.size(); iter++) {
					String existType = allETypes.get(iter);

					if (existType.startsWith("-")) {
						existType = existType.substring(1, existType.length());
					}
					if (value != null
							&& Utility.semanticMatch(value, existType, tree))
						if (!values.contains(existType))
							values.add(existType);

				}

				for (int kk = 0; kk < keys.size(); kk++) {
					key = keys.get(kk);
					for (int kv = 0; kv < values.size(); kv++) {

						// if yes, just update
						if (checkKeyExist(key)) {

							if (checkKeyValueExist(key, values.get(kv))) {

								ArrayList<EdgeLabel> hash2value = (ArrayList<EdgeLabel>) hash1
										.get(key).get(values.get(kv));

								ArrayList<EdgeLabel> updatevalue = new ArrayList<EdgeLabel>();
								EdgeLabel label = new EdgeLabel(queryID, step);

								for (int j = 0; j < hash2value.size(); j++) {
									updatevalue.add(hash2value.get(j));
								}

								boolean exist = false;
								for (int uI = 0; uI < updatevalue.size(); uI++) {
									if (updatevalue.get(uI).getQueryID() == label
											.getQueryID()
											&& updatevalue.get(uI).getStep() == label
													.getStep()) {
										exist = true;
									}
								}
								if (!exist)
									updatevalue.add(label);
								hash1.get(key).remove(values.get(kv));
								hash1.get(key).put(values.get(kv), updatevalue);
								// toString();
								continue;
							} else {
								Hashtable<String, ArrayList<EdgeLabel>> hash2 = hash1
										.get(key);
								ArrayList<EdgeLabel> edgelist = new ArrayList<EdgeLabel>();
								EdgeLabel label = new EdgeLabel(queryID, step);

								boolean exist = false;
								for (int uI = 0; uI < edgelist.size(); uI++) {
									if (edgelist.get(uI).getQueryID() == label
											.getQueryID()
											&& edgelist.get(uI).getStep() == label
													.getStep()) {
										exist = true;
									}
								}
								if (!exist)
									edgelist.add(label);

								hash2.put(values.get(kv), edgelist);// check
								// toString();

								continue;
							}
						}
					}

					// if no, create new entry
					if (!checkKeyExist(key)) {
						EdgeLabel label = new EdgeLabel(queryID, step);
						ArrayList<EdgeLabel> hash2value = new ArrayList<EdgeLabel>();
						hash2value.add(label);

						ArrayList<String> correntLevel1Children = new ArrayList<String>();
						ArrayList<String> correntLevel2Children = new ArrayList<String>();

						// find out the children for key and value

						ArrayList<String> allTypes = getAllstackTypes();

						// for the key, it is a type in the query and it is a
						// child
						// of the key
						for (int j = 0; j < getAllstackTypes().size(); j++) {
							Hashtable<String, ArrayList<EdgeLabel>> hash2 = new Hashtable<String, ArrayList<EdgeLabel>>();

							String stackT = allTypes.get(j);
							if (stackT.startsWith("-")) {
								stackT = stackT.substring(1, stackT.length());
							}
							if (Utility.semanticMatch(key, stackT, tree)) {

								String currentkey = getAllstackTypes().get(j)
										.toLowerCase();

								if (currentkey.startsWith("-")) {
									currentkey = currentkey.substring(1,
											currentkey.length());

								}

								//if (currentkey.equalsIgnoreCase("dell")) {
									//System.out.println("dell");
								//}
								// only if some event type exists in the query
								// and
								// it is a child of the value

								for (int k = 0; k < allTypes.size(); k++) {
									String type = allTypes.get(k);

									if (type.startsWith("-")) {
										type = type.substring(1, type.length());
									}
									
									//iterate values
									for(int v = 0; v < values.size(); v++)
									{
										value = values.get(v); 	
									if (value != null
											&& Utility.semanticMatch(value,
													type, tree)) {
										correntLevel2Children.add(type);
									}
									}

									

									if (precedingValue != null) {
										if (Utility.semanticMatch(
												precedingValue, type, tree)) {
											correntLevel2Children.add(type);
										}
									}

								}

								// set up hierarchy for level 2
								for (int k = 0; k < correntLevel2Children
										.size(); k++) {
									hash2.put(correntLevel2Children.get(k)
											.toLowerCase(), hash2value);
								}
								hash1.put(currentkey, hash2);
								correntLevel1Children.clear();
								correntLevel2Children.clear();

								// correntLevel1Children.add(getAllstackTypes().
								// get(j
								// ));
							}
						}

					}
				}
				// ////////////////////////////////////////////////////////

				// /////////////////////////////////////////////
			}
			step--;
		}
		// toString();
	}

	/**
	 * retrieves the hash table that stores the hierarchical query plan
	 * 
	 * @return hierarchical query plan
	 */
	public Hashtable<String, Hashtable> getHierarchicalQueryPlan() {
		return hash1;
	}

}
