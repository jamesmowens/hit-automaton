package edu.usfca.vas.layout.Views;

import com.teamdev.jxmaps.*;
import com.teamdev.jxmaps.swing.MapView;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class MapsView extends MapView {
	JPanel mapPanel; 
    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

    private float[] xSample, ySample;

    
    public MapsView(MapViewOptions options) {
        super(options);

    	
        final int dataSize = 20;
        xSample = new float[dataSize];
        ySample = new float[dataSize];
        Random random = new Random();
        random.setSeed(123456789);
        for(int i = 0; i < dataSize; i++) {
            ySample[i] = random.nextFloat() * i + .5f * i;
            xSample[i] = i;
        }
        ArrayList<Float> xVals = new ArrayList<Float>(), yVals = new ArrayList<Float>();
        for(float f : xSample) xVals.add(f);
        for(float f : ySample) yVals.add(f);
        mapPanel = new JPanel(new BorderLayout());
        
    	
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
                                window.setContent("Set Location");
                                window.open(map, marker);
                            }
                        }
                    });
                }           	
                MapViewOptions options = new MapViewOptions();
                options.importPlaces();
                final MapsView mapView = new MapsView(options);
                mapPanel.add(MapsView.makeMap(), BorderLayout.CENTER);
                mapPanel.setSize(700, 500);
                mapPanel.setLocation(null);
                mapPanel.setVisible(true);        
                mapPanel.add(mapView);
            }
        });
        
        
    }

    public JPanel getPanel() {
    	return mapPanel; 
    }
        
    public static MapsView makeMap(){
    	MapViewOptions options = new MapViewOptions();
    	options.importPlaces();
    	return new MapsView(options);
    }
}
