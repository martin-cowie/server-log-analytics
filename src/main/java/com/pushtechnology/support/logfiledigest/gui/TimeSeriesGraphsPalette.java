package com.pushtechnology.support.logfiledigest.gui;

import static com.pushtechnology.support.logfiledigest.gui.PlotInfo.buildFrom;
import static java.awt.Color.darkGray;
import static javax.swing.BoxLayout.Y_AXIS;
import static org.jfree.chart.JFreeChart.DEFAULT_TITLE_FONT;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;


public class TimeSeriesGraphsPalette extends ApplicationFrame {

    private static final long serialVersionUID = -7808166134871042994L;

    private final List<PlotInfo> plotInfos;
    private final XYItemRenderer renderer;

    /**
     * A demonstration application showing how to create a simple time series
     * chart.  This example uses monthly data.
     *
     * @param title  the frame title.
     * @param xyDatasets
     */
    public TimeSeriesGraphsPalette(String title, List<XYDataset> xyDatasets, String xAxisTitle, LogEntries logEntries) {
        // Pleh - could this come from a builder?
        super(title);
        // final DateAxis domainAxis = new DateAxis("Time");
        // domainAxis.setAutoRange(true);

        this.renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, darkGray);

        this.plotInfos = buildFrom(xyDatasets, renderer);

        final JPanel chartsPanel = new JPanel();
        chartsPanel.setLayout(new BoxLayout(chartsPanel, Y_AXIS));

        final JScrollPane chartPanelScroller = new JScrollPane(chartsPanel);
        chartPanelScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Calculate the widest range
        final Range commonRange = combine(plotInfos);

        for (PlotInfo plotInfo: plotInfos) {
            final XYPlot plot = plotInfo.getPlot();

            // Set all plots to a common range
            plot.getDomainAxis().setRange(commonRange);

            final JFreeChart chart = new JFreeChart(null, DEFAULT_TITLE_FONT, plot, false);
            final ChartPanel chartPanel = new SingleDatasetChartPanel(chart, plotInfo.getDataset(), plotInfo.getName());
            chartPanel.setPreferredSize(new Dimension(500, 200));
            chartPanel.setPopupMenu(null);
            chartPanel.setMouseZoomable(false);
            // chartPanel.getName();

            // Decorate with logging information, if this is a PUSH- designation
            final LogEntry entry = logEntries.getWithDesignation(plotInfo.getName());
            if (entry != null) {
                // put this inside the graph
                final JLabel formatLabel = new JLabel(entry.getFormat());
                formatLabel.setForeground(Color.GRAY);
                chartsPanel.add(formatLabel);
            }
            chartsPanel.add(chartPanel);
        }

        setContentPane(chartPanelScroller);
    }

    /**
     * @return the sum/combination of all given PlotInfo objects
     * TODO: could be done using reduce
     */
    private static final Range combine(List<PlotInfo> plotInfos) {
        Range result = null;
        for (PlotInfo plotInfo: plotInfos) {
            result = Range.combine(plotInfo.getPlot().getDomainAxis().getRange(), result);
        }
        return result;
    }

    public static ApplicationFrame showGraphPalette(String title, List<XYDataset> dataSets, String xAxisTitle) throws IOException {
        final TimeSeriesGraphsPalette result = new TimeSeriesGraphsPalette(title, dataSets, xAxisTitle, LogEntries.build());
        result.pack();
        RefineryUtilities.positionFrameOnScreen(result, 0.05, 0.5);
        result.setVisible(true);
        return result;
    }

}