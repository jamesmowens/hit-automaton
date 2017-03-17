package edu.usfca.vas.layout.Views;

import com.teamdev.jxmaps.LatLng;
import com.teamdev.jxmaps.swing.MapView;
import java.util.*;

/**
 * Created by jamesowens on 3/13/17.
 */
public class MapTest implements Runnable {
    private MapsView mapView;
    private Thread t;
    private ArrayList<Double[]> curMarked;

    public MapTest(MapsView view) {
        this.mapView =view;
        curMarked = new ArrayList<>();
    }

    public void run(){
        while(true){
            markRandomLocationInNewYork();
            Random r = new Random();
            int num = r.nextInt(6);
            if(curMarked.size()>15) //make sure there are no more than 15 markers on the map
                num =1;

            if(num !=0){
                removeRandomLocation();
            }
            try{
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void start(){
        if (t==null) {
            Thread t = new Thread(this, "Marker Creation Thread");
            t.start();
        }
    }

    private void  markRandomLocationInNewYork(){
        Random r = new Random();
        /*40.879466, 74.004525;
        40.709482, 73.957904;*/

        double lat = 40.709482 + (40.879466 - 40.709482) * r.nextDouble();
        double lng = 73.957904 + (74.004525 - 73.957904) * r.nextDouble();
        lng *= -1;
        Double[] latLng = {lat,lng};
        curMarked.add(latLng);
        mapView.markLatLng(lat,lng);
    }

    private void removeRandomLocation(){
        if(!curMarked.isEmpty()){
            Random r = new Random();
            int randIndex = r.nextInt(curMarked.size());
            Double[] latlng = curMarked.get(randIndex);
            mapView.removeMarker(latlng[0],latlng[1]);
            curMarked.remove(randIndex);
        }


    }
}
