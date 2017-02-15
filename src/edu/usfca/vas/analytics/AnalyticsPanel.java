package edu.usfca.vas.analytics;

import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Created by Thomas Schweich on 2/8/2017.
 *
 * Panel containing analytics for one of the analytics modes
 */
public class AnalyticsPanel extends JPanel {

    public AnalyticsPanel(Map<String, String> graph1Settings, Map<String, String> graph2Settings,
                          Map<String, String> pie1Settings, Map<String, String> pie2Settings, Map<String, String> pie3Settings,
                          XYDataset dat1, XYDataset dat2, PieDataset pie1, PieDataset pie2, PieDataset pie3) {
        super(new GridLayout(2, 1));
        add(new PlotPanel(
                dat1,
                dat2,
                new GraphSettings(graph1Settings),
                new GraphSettings(graph2Settings)), 0);
        add(new PiePanel(pie1, pie2, pie3, pie1Settings, pie2Settings, pie3Settings));
    }

}
