package edu.usfca.vas.analytics;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.PieDataset;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Created by Thomas on 2/8/2017.
 *
 * Lower Panels in Analytics View containing pie charts
 */
public class PiePanel extends JPanel {
    public PiePanel(PieDataset pie1, PieDataset pie2, PieDataset pie3, Map<String, String> pie1Settings,
                    Map<String, String> pie2Settings, Map<String, String> pie3Settings) {
        super(new GridLayout(1, 3));
        add(new ChartPanel(createChart(pie1, new GraphSettings(pie1Settings))), 0);
        add(new ChartPanel(createChart(pie2, new GraphSettings(pie2Settings))), 1);
        add(new ChartPanel(createChart(pie3, new GraphSettings(pie3Settings))), 2);
    }

    /**
     * Creates a pie chart with the given labels and data
     * @param dataset The data to plot
     * @param settings the GraphSettings associated with the plot
     * @return A JFreeChart with given specifications
     */
    private JFreeChart createChart(PieDataset dataset, GraphSettings settings) {
        JFreeChart chart = ChartFactory.createPieChart(
                settings.getTitle(),
                dataset,
                true,
                true,
                false
        );

        //PiePlot plot = (PiePlot) chart.getPlot();
        //TODO Customization
        return chart;
    }
}
