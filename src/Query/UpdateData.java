/**
 * MaryAnn VanValkenburg
 * mevanvalkenburg@wpi.edu
 * The following signature indicates that the author above pertains all rights to any ideas implemented in the code below.
 * Signature: MaryAnn VanValkenburg
 * Modified: MaryAnn VanValkenburg (mevanvalkenburg@wpi.edu) 02/22/2017, implemented updateData and updateVariables
 */

package Query;

import java.util.Map;

public class UpdateData {
	/**
	 * Given a DataNode with cost, latitude, longitude, and time, updates variables.txt with those values. If variables
	 * do not exist, instantiates them within variables.txt. If variables do exist, overwrites them with the new value.
	 * @param node DataNode with cost, latitude, longitude, time, and associated getter methods.
	 */
	public static void updateData(DataNode node, Map<String, Variable> varMap){
		if (varMap.containsKey("cost")) {
			varMap.get("cost").setValue(node.getCost());
		} else {
			varMap.put("cost", new Variable("cost",node.getCost(),true));
		}
		
		if (varMap.containsKey("latitude")) {
			varMap.get("latitude").setValue(node.getCost());
		} else {
			varMap.put("latitude", new Variable("latitude",node.getCost(),true));
		}
		
		if (varMap.containsKey("longitude")) {
			varMap.get("longitude").setValue(node.getCost());
		} else {
			varMap.put("longitude", new Variable("longitude",node.getCost(),true));
		}
		
		if (varMap.containsKey("time")) {
			varMap.get("time").setValue(node.getCost());
		} else {
			varMap.put("time", new Variable("time",node.getCost(),true));
		}

	}


}
