package edu.usfca.vas.analytics;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

/**
 * Created by thoma on 2/1/2017.
 */
public class AnalyticsView {
    JPanel panel;
    JFreeChart chart1;
    JFreeChart chart2;

    public AnalyticsView() {

    }

    private void populate(Collection<Float> set1, Collection<Float> set2) {
        GridLayout grid = new GridLayout(2, 0);
        panel.setLayout(grid);
        JPanel graphPanel = new JPanel();
        panel.add(graphPanel, 0);
        ChartPanel chartPanel1 = new ChartPanel(chart1);
        ChartPanel chartPanel2 = new ChartPanel(chart2);
        graphPanel.add(chartPanel1);
        graphPanel.add(chartPanel2);
    }

    private void createGraphs(Collection<Float> set1, Collection<Float> set2) {
        DefaultCategoryDataset d1 = new DefaultCategoryDataset();
        CategoryDataset d2 = new DefaultCategoryDataset();
        for(float d : set1) {
            d1.addValue((Number) d, 0, 0);
        }
        for(float d : set2) {
            d1.addValue((Number) d, 0, 0);
        }
        chart1 = ChartFactory.createLineChart("Chart 1", "x", "x", d1);
        chart2 = ChartFactory.createLineChart("Chart 1", "x", "x", d2);
    }

}
