package edu.usfca.vas.layout.Views;

/**
 * Created by jamesowens on 3/8/17.
 */
import com.teamdev.jxmaps.*;
import com.teamdev.jxmaps.swing.MapView;
import sun.jvm.hotspot.oops.Mark;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class MapsView extends  MapView {
  HashMap<String, Marker> markerDict;
  
  public MapsView(MapViewOptions options) {
    super(options);
    markerDict = new HashMap<>();
    setOnMapReadyHandler(new MapReadyHandler() {
      @Override
      public void onMapReady(MapStatus status) {
        if (status == MapStatus.MAP_STATUS_OK) {
          final Map map = getMap();
          map.setZoom(12);
          GeocoderRequest request = new GeocoderRequest();
          //request.setAddress("New York, USA");
          request.setLocation(new LatLng(40.775048, -73.971120));
          markLatLng(40.8,-74.0);
          //markLatLng(40.7234,-73.988);
          //removeMarker(40.8,-74.0);
          
          
          getServices().getGeocoder().geocode(request, new GeocoderCallback(map) {
            @Override
            public void onComplete(GeocoderResult[] result, GeocoderStatus status) {
              if (status == GeocoderStatus.OK) {
                map.setCenter(result[0].getGeometry().getLocation());
                System.out.println(map.getBounds());
                /*Marker marker = new Marker(map);
                 marker.setPosition(result[0].getGeometry().getLocation());
                 
                 final InfoWindow window = new InfoWindow(map);
                 window.setContent("Hello, World!");
                 window.open(map, marker);*/
                
                
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
  
  public void markLatLng(double lat, double lng){
    LatLng location = new LatLng(lat,lng);
    final Map map = getMap();
    Marker marker = new Marker(map);
    marker.setPosition(location);
    
    String key = ""+lat+lng;
    System.out.println(key);
    markerDict.put(key,marker);
    
  }
  
  public void removeMarker(double lat, double lng){
    String key = ""+lat+lng;
    Marker marker = markerDict.get(key);
    marker.remove();
    markerDict.remove(key);
  }
  
  
  public static void main(String[] args) {
    
    MapViewOptions options = new MapViewOptions();
    options.importPlaces();
    final MapsView mapView = new MapsView(options);
    //mapView.markLatLng(46.936208, 34.168202);
    
    JFrame frame = new JFrame("JxMaps - Hello, World!");
    
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.add(mapView, BorderLayout.CENTER);
    frame.setSize(700, 500);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
    //mapView.markLatLng(46.936208, 34.168202);
    
  }
  
  
}
