package edu.usfca.vas.analytics;

import edu.usfca.vas.layout.JSONReader;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by Thomas Schweich on 2/1/2017.
 *
 * Class representing the Analytics View
 */
public class AnalyticsView {
    JPanel panel;
    JFreeChart chart1;
    JFreeChart chart2;
    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

    private static final float[] xSample = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    private static final float[] ySample = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    public AnalyticsView() {
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
        XYDataset set1 = createDataset("Test1", xVals, yVals);
        XYDataset set2 = createDataset("Test2", xVals, yVals);
        AnalyticsPanel
                context = new AnalyticsPanel(new JSONReader(jsonReader.getJsonObject("Context")), set1, set2),
                drivers = new AnalyticsPanel(new JSONReader(jsonReader.getJsonObject("Drivers")), set1, set2),
                riders = new AnalyticsPanel(new JSONReader(jsonReader.getJsonObject("Riders")), set1, set2);
        tabbedPane.add(context);
        tabbedPane.add(drivers);
        tabbedPane.add(riders);
        //populate(xVals, yVals);
        panel.setVisible(true);
    }

    public JPanel getPanel() {
        return panel;
    }

    private XYDataset createDataset(String name, Collection<Float> xData, Collection<Float> yData) {
        Iterator<Float> xIter = xData.iterator(), yIter = yData.iterator();
        XYSeries series = new XYSeries(name);
        while(xIter.hasNext() && yIter.hasNext()) {
            series.add(xIter.next(), yIter.next());
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        return dataset;
    }

}
