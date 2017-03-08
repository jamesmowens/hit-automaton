package edu.usfca.vas.layout.Views;

import edu.usfca.vas.analytics.AnalyticsPanel;
import edu.usfca.vas.layout.JSONReader;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by Goutham.
 *
 * Class representing the Map View
 */
public class MapView {
    JPanel panel;
    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

    //private static final float[] xSample = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    //private static final float[] ySample = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    private float[] xSample, ySample;

    public MapView() {
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
        panel = new JPanel(new BorderLayout());
        JSONReader jsonReader;
        try {
            jsonReader = new JSONReader("src/edu/usfca/vas/analytics/analyticsSettings.json");
        } catch (FileNotFoundException f) {
            System.err.println("Couldn't find analyticsSettings.json");
            f.printStackTrace();
            return;
        }
        panel.add(tabbedPane);

        panel.setVisible(true);
    }

    public JPanel getPanel() {
        return panel;
    }

 
}
