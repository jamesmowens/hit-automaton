package com.hp.hpl.CHAOS.Aggregation;

import java.util.ArrayList;

public class Utility {
	
	/**
	 * This class is to include all the functions for general use
	 * not specific to any classes
	 * @author Yingmei Qi
	 */
	
	/**
	 * test whether one string is contained in another arrayList.
	 * 
	 * @param list
	 * @param astring
	 * @return
	 */
	public static boolean contains_notsensitive(ArrayList<String> list,
			String astring) {

		boolean contain = false;
		if (list.contains(astring)) {
			contain = true;
		} else {
			for (int i = 0; i < list.size(); i++) {
				contain = list.get(i).equalsIgnoreCase(astring);

				if (contain)
					break;
			}
		}

		return contain;
	}

}
