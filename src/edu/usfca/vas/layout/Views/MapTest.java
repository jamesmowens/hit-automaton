package edu.usfca.vas.layout.Views;

import com.teamdev.jxmaps.LatLng;
import com.teamdev.jxmaps.swing.MapView;

import java.util.Random;

/**
 * Created by jamesowens on 3/13/17.
 */
public class MapTest implements Runnable {
    private MapsView mapView;
    private Thread t;

    public MapTest(MapsView view) {
        this.mapView =view;
    }

    public void run(){
        while(true){
            markRandomLocationInNewYork();
            try{
                Thread.sleep(5000);
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
        mapView.markLatLng(lat,lng);

    }
}