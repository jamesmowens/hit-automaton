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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by Thomas Schweich on 2/8/2017.
 *
 * Panel containing analytics for one of the analytics modes
 */
public class AnalyticsPanel extends JPanel {
    private String graph1Title;

    private String graph2Title;
    private String graph1X;
    private String graph2X;
    private String graph1Y;
    private String graph2Y;
    private XYDataset dat1, dat2;

    private AnalyticsPanel(String graph1Title, String graph2Title, String graph1X, String graph2X, String graph1Y,
                          String graph2Y, XYDataset dat1, XYDataset dat2) {
        super(new GridLayout(2, 1));
        this.graph1Title = graph1Title;
        this.graph2Title = graph2Title;
        this.graph1X = graph1X;
        this.graph2X = graph2X;
        this.graph1Y = graph1Y;
        this.graph2Y = graph2Y;
        this.dat1 = dat1;
        this.dat2 = dat2;
        add(new PlotPanel(graph1Title, graph2Title, graph1X, graph2X, graph1Y, graph2Y, dat1, dat2), 0);
        add(new PiePanel(), 1);
    }

    public AnalyticsPanel(JSONReader jsonReader, XYDataset dat1, XYDataset dat2) {
        this(
                jsonReader.getValue("graph1Title"),
                jsonReader.getValue("graph2Title"),
                jsonReader.getValue("graph1X"),
                jsonReader.getValue("graph2X"),
                jsonReader.getValue("graph1Y"),
                jsonReader.getValue("graph2Y"),
                dat1, dat2
        );
    }

}
