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
 * Created by Thomas Schweich on 2/1/2017.
 *
 * Class representing the Analytics View
 */
public class AnalyticsView {
    JPanel panel;
    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

    //private static final float[] xSample = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    //private static final float[] ySample = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    private float[] xSample, ySample;

    public AnalyticsView() {
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
            jsonReader = new JSONReader("analyticsSettings.json");
        } catch (FileNotFoundException f) {
            System.err.println("Couldn't find analyticsSettings.json");
            f.printStackTrace();
            return;
        }
        panel.add(tabbedPane);
        XYDataset set1 = createDataset("Test1", xVals, yVals);
        XYDataset set2 = createDataset("Test2", xVals, yVals);
        PieDataset pie1Set = createPieDataset(), pie2Set = waitTimeDataset(), pie3Set = driverRiderDataset();
        /*AnalyticsPanel
                context = new AnalyticsPanel(new JSONReader(jsonReader.getJsonObject("Context")), set1, set2),
                drivers = new AnalyticsPanel(new JSONReader(jsonReader.getJsonObject("Drivers")), set1, set2),
                riders = new AnalyticsPanel(new JSONReader(jsonReader.getJsonObject("Riders")), set1, set2);*/
        JSONReader contextSettings, driversSettings, ridersSettings;
        contextSettings = new JSONReader(jsonReader.getJsonObject("Context"));
        driversSettings = new JSONReader(jsonReader.getJsonObject("Drivers"));
        ridersSettings = new JSONReader(jsonReader.getJsonObject("Riders"));
        AnalyticsPanel context = new AnalyticsPanel(
                contextSettings.getAsSSMap("graph1"),
                contextSettings.getAsSSMap("graph2"),
                contextSettings.getAsSSMap("pie1"),
                contextSettings.getAsSSMap("pie2"),
                contextSettings.getAsSSMap("pie3"),
                set1, set2, pie1Set, pie2Set, pie3Set);
        AnalyticsPanel drivers = new AnalyticsPanel(
                driversSettings.getAsSSMap("graph1"),
                driversSettings.getAsSSMap("graph2"),
                driversSettings.getAsSSMap("pie1"),
                driversSettings.getAsSSMap("pie2"),
                driversSettings.getAsSSMap("pie3"),
                set1, set2, pie1Set, pie2Set, pie3Set);
        AnalyticsPanel riders = new AnalyticsPanel(
                ridersSettings.getAsSSMap("graph1"),
                ridersSettings.getAsSSMap("graph2"),
                ridersSettings.getAsSSMap("pie1"),
                ridersSettings.getAsSSMap("pie2"),
                ridersSettings.getAsSSMap("pie3"),
                set1, set2, pie1Set, pie2Set, pie3Set);
        tabbedPane.add(context);
        tabbedPane.add(drivers);
        tabbedPane.add(riders);
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

    private PieDataset createPieDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("33% Price increase", new Double(33));
        dataset.setValue("", new Double(77));
        return dataset;
    }

    private PieDataset waitTimeDataset(){
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("8.5 Fold Increase",new Double(2));
        dataset.setValue("",new Double(17));
        return dataset;
    }

    private PieDataset driverRiderDataset(){
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("13.5 Fold Increase",new Double(0.2));
        dataset.setValue("",new Double(2.7));
        return dataset;
    }

}
