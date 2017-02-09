package edu.usfca.vas.analytics;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Collection;

/**
 * Created by Thomas Schweich on 2/8/2017.
 *
 * Panel in Analytics View containing graphs of data
 */
public class PlotPanel extends JPanel {

    public PlotPanel(String graph1Title, String graph2Title, String graph1X, String graph2X, String graph1Y,
                     String graph2Y, XYDataset dat1, XYDataset dat2) {
        super(new GridLayout(1, 2));
        add(new ChartPanel(createChart(dat1, graph1Title, graph1X, graph1Y)));
        add(new ChartPanel(createChart(dat2, graph2Title, graph2X, graph2Y)));
    }

    /**
     * Creates a line chart with the given labels and data
     * @param dataset The data to plot
     * @param title The title of the graph
     * @param xLabel The x-label of the graph
     * @param yLabel The y-label of the graph
     * @return JFreeChart with given specifications
     */
    private JFreeChart createChart(XYDataset dataset, String title, String xLabel, String yLabel) {
        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                xLabel,
                yLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        float radius = 5;
        Shape circle = new Ellipse2D.Float(-radius, -radius, radius*2, radius*2);
        renderer.setSeriesShape(0, circle);

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.white);

        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.BLACK);

        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.BLACK);

        chart.getLegend().setFrame(BlockBorder.NONE);
        return chart;

    }
}
