package edu.usfca.vas.maps;

/**
 * Created by James on 2/28/2017.
 */
import com.teamdev.jxmaps.*;
import com.teamdev.jxmaps.swing.MapView;

import javax.swing.*;
import java.awt.*;

public class HelloWorld extends MapView {
	JPanel mapPanel; 
    public HelloWorld(MapViewOptions options) {
        super(options);
        setOnMapReadyHandler(new MapReadyHandler() {
            @Override
            public void onMapReady(MapStatus status) {
                if (status == MapStatus.MAP_STATUS_OK) {
                    final Map map = getMap();
                    map.setZoom(12);
                    GeocoderRequest request = new GeocoderRequest(map);
                    //request.setAddress("New York, USA");
                    request.setLocation(new LatLng(40.775048, -73.971120));
                    getServices().getGeocoder().geocode(request, new GeocoderCallback(map) {
                        @Override
                        public void onComplete(GeocoderResult[] result, GeocoderStatus status) {
                            if (status == GeocoderStatus.OK) {
                                map.setCenter(result[0].getGeometry().getLocation());
                                Marker marker = new Marker(map);
                                marker.setPosition(result[0].getGeometry().getLocation());

                                final InfoWindow window = new InfoWindow(map);
                                window.setContent("Hello, World!");
                                window.open(map, marker);
                            }
                        }
                    });
                }
            }
        });
        
        
    }

    public JPanel getPanel() {
    	if(this.mapPanel==null){
    		this.mapPanel = new JPanel();
    		this.mapPanel.add(this,BorderLayout.CENTER);
    		this.mapPanel.setSize(700,500);
    		this.mapPanel.setVisible(true);
    		
    	}
    	return this.mapPanel; 
    }
    
    public static HelloWorld makeMap(){
    	MapViewOptions options = new MapViewOptions();
    	options.importPlaces();
    	return new HelloWorld(options);
    }
    public static void main(String[] args) {

        MapViewOptions options = new MapViewOptions();
        options.importPlaces();
        final HelloWorld mapView = new HelloWorld(options);

        JFrame frame = new JFrame("JxMaps - Hello, World!");

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(HelloWorld.makeMap(), BorderLayout.CENTER);
        frame.setSize(700, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}