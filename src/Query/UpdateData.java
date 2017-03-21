/**
 * MaryAnn VanValkenburg
 * mevanvalkenburg@wpi.edu
 * The following signature indicates that the author above pertains all rights to any ideas implemented in the code below.
 * Signature: MaryAnn VanValkenburg
 * Modified: MaryAnn VanValkenburg (mevanvalkenburg@wpi.edu) 02/22/2017, implemented updateData and updateVariables
 */

package Query;

import edu.usfca.vas.graphics.fa.GElementFAMachine;
import edu.usfca.xj.appkit.gview.event.GEventCreateLinkElement;
import edu.usfca.xj.appkit.gview.object.GElement;

import java.util.Map;

public class UpdateData {
	/**
	 * Given a DataNode with cost, latitude, longitude, and time, updates variables.txt with those values. If variables
	 * do not exist, instantiates them within variables.txt. If variables do exist, overwrites them with the new value.
	 * @param node DataNode with cost, latitude, longitude, time, and associated getter methods.
	 */
	public static void updateData(DataNode node){
        // cost
		if (GElementFAMachine.variableMap.containsKey("cost")) {
            GElementFAMachine.variableMap.get("cost").setValue(node.getCost());
		} 
		else {
            GElementFAMachine.variableMap.put("cost", new Variable("cost",node.getCost(),true));
		}

		// latitude
		if (GElementFAMachine.variableMap.containsKey("latitude")) {
            GElementFAMachine.variableMap.get("latitude").setValue(node.getLatitude());
		} 
		else {
            GElementFAMachine.variableMap.put("latitude", new Variable("latitude",node.getLatitude(),true));
		}

		// longitude
		if (GElementFAMachine.variableMap.containsKey("longitude")) {
            GElementFAMachine.variableMap.get("longitude").setValue(node.getLongitude());
		} 
		else {
            GElementFAMachine.variableMap.put("longitude", new Variable("longitude",node.getLongitude(),true));
		}

		// time
		if (GElementFAMachine.variableMap.containsKey("time")) {
            GElementFAMachine.variableMap.get("time").setValue(node.getTime());
		}
		else {
            GElementFAMachine.variableMap.put("time", new Variable("time",node.getTime(),true));
		}

		// area
        if (GElementFAMachine.variableMap.containsKey("area")) {
            GElementFAMachine.variableMap.get("area").setValue(node.getArea());
        } else {
            GElementFAMachine.variableMap.put("area", new Variable("area",node.getArea(),true));
        }

        // id
        if (GElementFAMachine.variableMap.containsKey("id")) {
            GElementFAMachine.variableMap.get("id").setValue(node.getArea());
        } else {
            GElementFAMachine.variableMap.put("id", new Variable("id",node.getArea(),true));
        }
	}
}