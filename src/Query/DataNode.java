/**
 * Nicholas Fajardo
 * nafajardo@wpi.edu
 * The following signature indicates that the author above pertains all rights to any ideas implemented in the code below.
 * Signature: Nicholas Fajardo
 * Modified: MaryAnn VanValkenburg (mevanvalkenburg@wpi.edu) 02/22/2017, added getters, fixed parsing of time string
 */

package Query;

public class DataNode {
	private int cost, area, id;
	private double latitude, longitude, time;
	private String pattern;


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

	public DataNode (String c, String lat, String lon, String time, String _pattern, String _id, String _area){
		int newCost = Integer.parseInt(c);
		int newID = Integer.parseInt(_id);
		int newArea = Integer.parseInt(_area);
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
		this.pattern = _pattern;
		this.id = newID;
		this.area = newArea;
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

	public String getPattern(){
		return this.pattern;
	}

	public double getArea(){return this.area;}

	public double getID(){return this.id;}
}