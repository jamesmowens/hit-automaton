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

/**
 * Created by Thomas Schweich on 2/8/2017.
 *
 * Panel in Analytics View containing graphs of data
 */
public class PlotPanel extends JPanel {

    public PlotPanel(XYDataset dat1, XYDataset dat2, GraphSettings settings1, GraphSettings settings2) {
        super(new GridLayout(1, 2));
        add(new ChartPanel(createChart(dat1, settings1)));
        add(new ChartPanel(createChart(dat2, settings2)));
    }

    /**
     * Creates a line chart with the given labels and data
     * @param dataset The data to plot
     * @param settings the GraphSettings associated with the plot
     * @return A JFreeChart with given specifications
     */
    private JFreeChart createChart(XYDataset dataset, GraphSettings settings) {
        JFreeChart chart = ChartFactory.createXYLineChart(
                settings.getTitle(),
                settings.getxLabel(),
                settings.getyLabel(),
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, settings.getLineColor());
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesShape(0, settings.getShape());

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(settings.getBackground());

        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(settings.getGridColor());

        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(settings.getGridColor());

        chart.getLegend().setFrame(BlockBorder.NONE);
        return chart;

    }
}
