/**
 * MaryAnn VanValkenburg
 * mevanvalkenburg@wpi.edu
 * The following signature indicates that the author above pertains all rights to any ideas implemented in the code below.
 * Signature: MaryAnn VanValkenburg
 * Modified: MaryAnn VanValkenburg (mevanvalkenburg@wpi.edu) 02/22/2017, implemented updateData and updateVariables
 */

package Query;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class UpdateData {
	/**
	 * Given a DataNode with cost, latitude, longitude, and time, updates variables.txt with those values. If variables
	 * do not exist, instantiates them within variables.txt. If variables do exist, overwrites them with the new value.
	 * @param node DataNode with cost, latitude, longitude, time, and associated getter methods.
	 */
	public static void updateData(DataNode node){
		updateVariables("cost",""+node.getCost());
		updateVariables("latitude",""+node.getLatitude());
		updateVariables("longitude",""+node.getLongitude());
		updateVariables("time",""+node.getTime());
	}

	/**
	 * Updates variables.txt with the variable and its associated value. If variable exists in variables.txt, overwrites
	 * it. If not, adds it to the end of variables.txt.
	 * @param variable Name of a variable to be kept/overwritten within variables.txt
	 * @param value Value associated with variable to be kept in variables.txt
	 */
	public static void updateVariables(String variable, String value) {
		try {
			//System.out.println("File exists");
			BufferedReader file = new BufferedReader(new FileReader(new File("variables.txt")));
			BufferedWriter temp = new BufferedWriter(new FileWriter(new File("temp.txt")));

			Boolean varExists = false;
			String line = file.readLine();
			while (line != null) {
				if (line.startsWith(variable + " =")) {
					varExists = true;
					//System.out.println(line);
					temp.append(variable + " = " + value + "\n");
					//System.out.println(variable + " = " + value + "\n");
				} else {
					temp.append(line+"\n");
				}
				line = file.readLine();
			}

			// if variable was not found within variables.txt, add it to the end
			if (!varExists) {
				temp.append(variable + " = " + value + "\n");
			}
			file.close();
			temp.close();

			// Copy over new file
			Files.copy(new File("temp.txt").toPath(), new File("variables.txt").toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.delete(new File("temp.txt").toPath());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	public static void main(String args[]) {
		// add a node, make sure variables.txt is updated correctly
		DataNode node = new DataNode("10","100.0","200.0","12:34");
		updateData(node);
		try {
			BufferedReader file = new BufferedReader(new FileReader(new File("variables.txt")));
			String line = file.readLine();
			System.out.println("Printing variables.txt after adding node: cost=10,lat=100.0,lon=200.0,time=12:34");
			while (line != null) {
				System.out.println(line);
				line = file.readLine();
			}
			file.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// add another node, make sure variables.txt is updated correctly
		DataNode node2 = new DataNode("25","150.5","205.2","12:00");
		updateData(node2);
		try {
			BufferedReader file = new BufferedReader(new FileReader(new File("variables.txt")));
			String line = file.readLine();
			System.out.println("Printing variables.txt after adding node: cost=25,lat=150.5,lon=205.2,time=12:00");
			while (line != null) {
				System.out.println(line);
				line = file.readLine();
			}
			file.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/

}
