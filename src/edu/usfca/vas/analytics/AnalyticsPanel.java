package edu.usfca.vas.analytics;

import edu.usfca.vas.layout.JSONReader;
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

    public AnalyticsPanel(JSONReader jsonReader, XYDataset dat1, XYDataset dat2) {
        super(new GridLayout(2, 1));
        add(new PlotPanel(
                dat1,
                dat2,
                new GraphSettings(jsonReader.getAsSSMap("graph1")),
                new GraphSettings(jsonReader.getAsSSMap("graph2"))));
    }

}
