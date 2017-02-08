package Query;

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
		int newC = Integer.parseInt(c);
		double newLat = Double.parseDouble(lat);
		double newLon = Double.parseDouble(lon);
		double newTime = Double.parseDouble(time.substring(0, 1)) + Double.parseDouble(time.substring(3,4))*(1/60);
		this.cost = newC;
		this.latitude = newLat;
		this.longitude = newLon;
		this.time = newTime;
	}
}
