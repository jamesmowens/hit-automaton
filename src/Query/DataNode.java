/**
 * Nicholas Fajardo
 * nafajardo@wpi.edu
 * The following signature indicates that the author above pertains all rights to any ideas implemented in the code below.
 * Signature: Nicholas Fajardo
 * Modified: MaryAnn VanValkenburg (mevanvalkenburg@wpi.edu) 02/22/2017, added getters, fixed parsing of time string
 */

package query;

public class DataNode {
	private int cost;
	private double latitude, longitude, time;

	public DataNode(){
		this.cost = 0;
		this.latitude = 0.0;
		this.longitude = 0.0;
		this.time = 0.0;
	}

	public DataNode (String c, String lat, String lon, String time){
		int newCost = Integer.parseInt(c);
		double newLat = Double.parseDouble(lat);
		double newLon = Double.parseDouble(lon);
		double newTime = (Double.parseDouble(time.substring(0, 2))*(60) + Double.parseDouble(time.substring(3,5)));
		//System.out.println("newTime is: "+newTime);
		//System.out.println("Subset 0-2 of time is "+Double.parseDouble(time.substring(0,2)));
		//System.out.println("Subset 3-5 of time is "+Double.parseDouble(time.substring(3,5)));

		this.cost = newCost;
		this.latitude = newLat;
		this.longitude = newLon;
		this.time = newTime;
	}

	public int getCost() {
		return this.cost;
	}

	public double getLatitude() {
		return this.latitude;
	}

	public double getLongitude() {
		return this.longitude;
	}

	public double getTime() {
		return this.time;
	}
}