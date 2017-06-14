package com.pushtechnology.support.logfiledigest.gui;

import static com.pushtechnology.support.logfiledigest.LogfileDigester.DATE_FORMAT;
import static com.pushtechnology.support.logfiledigest.gui.PlotInfo.buildFrom;
import static java.awt.Color.GRAY;
import static java.awt.Color.darkGray;
import static java.lang.Integer.parseInt;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.JSplitPane.HORIZONTAL_SPLIT;
import static org.jfree.chart.JFreeChart.DEFAULT_TITLE_FONT;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;


public class TimeSeriesVizualiser extends ApplicationFrame {

    private static final long serialVersionUID = -7808166134871042994L;
    private static final String DATE = "Date";

    private final List<PlotInfo> plotInfos;
    private final XYItemRenderer renderer;
    private final CombinedDomainXYPlot combinedPlot;

    /**
     * A demonstration application showing how to create a simple time series
     * chart.  This example uses monthly data.
     *
     * @param title  the frame title.
     * @param xyDatasets
     */
    public TimeSeriesVizualiser(String title, List<XYDataset> xyDatasets, String xAxisTitle, LogEntries logEntries) {
        super(title);
        final DateAxis domainAxis = new DateAxis("Time");
        domainAxis.setAutoRange(true);

        this.renderer = new XYLineAndShapeRenderer(true, false);
        this.plotInfos = buildFrom(xyDatasets, renderer);
        this.combinedPlot = new CombinedDomainXYPlot(domainAxis);

        final JPanel buttonPanel = new JPanel();
        renderer.setSeriesPaint(0, darkGray);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, Y_AXIS));

        for(final PlotInfo plotInfo: plotInfos) {
            final JPanel groupPanel = new JPanel();
            groupPanel.setLayout(new BoxLayout(groupPanel, Y_AXIS));

            final String seriesName = plotInfo.getName();
            final JCheckBox checkBox = new JCheckBox(seriesName, true);
            checkBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    // Find the plot for this dataset, and add/remove it
                    plotInfo.setVisible(checkBox.isSelected());

                    //TODO: redrawing the map from scratch - is there anything more elegant?
                    refillChart();
                }
            });
            groupPanel.add(checkBox);

            // Decorate with logging information, if this is a PUSH- designation
            final LogEntry entry = logEntries.getWithDesignation(seriesName);
            if (logEntries != null) {
                final JLabel formatLabel = new JLabel(entry.getFormat());
                formatLabel.setForeground(GRAY);
                groupPanel.add(formatLabel);
            }

            // Scale information
            groupPanel.add(new JLabel("max value: " + plotInfo.getMax()));

            buttonPanel.add(groupPanel);
        }

        refillChart();

        final JFreeChart chart = new JFreeChart(null, DEFAULT_TITLE_FONT, combinedPlot, false);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setInitialDelay(100);
        chartPanel.setDismissDelay(60_000);
//        chartPanel.setPreferredSize(new java.awt.Dimension(500, 100 * plotInfos.size()));

        final JScrollPane buttonScroller = new JScrollPane(buttonPanel);
        buttonScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        final JScrollPane chartPanelScroller = new JScrollPane(chartPanel);
        chartPanelScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JSplitPane splitPane = new JSplitPane(HORIZONTAL_SPLIT, buttonScroller, chartPanelScroller);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(180);
        setContentPane(splitPane);
    }

    @SuppressWarnings("unchecked")
    private void refillChart() {

        // Clear old plots
        for(XYPlot plot : new ArrayList<XYPlot>(combinedPlot.getSubplots())) {
            combinedPlot.remove(plot);
        }

        // And the new plots
        for(PlotInfo plotInfo : plotInfos) {
            if(plotInfo.isVisible()) {
                combinedPlot.add(plotInfo.getPlot());
            }
        }
    }

    public static ApplicationFrame showTimeSeries(String title, List<XYDataset> dataSets, String xAxisTitle) throws IOException {
        final TimeSeriesVizualiser result = new TimeSeriesVizualiser(title, dataSets, xAxisTitle, LogEntries.build());
        result.pack();
        RefineryUtilities.centerFrameOnScreen(result);
        result.setVisible(true);
        return result;
    }

    private static List<XYDataset> toDataSets(List<CSVRecord> records) throws ParseException {
        // Pre-populate the set of series "serieses", misspelled for clarity.
        final Map<String,TimeSeries> serieses = new TreeMap<>();

        // Process each record
        for(CSVRecord record : records) {
            final Map<String, String> recordMap = record.toMap();
            final List<String> columns = new ArrayList<>(recordMap.keySet());
            columns.remove(DATE);

            final Date date = DATE_FORMAT.parse(record.get(DATE));
            for(String column : columns) {
                final TimeSeries series;
                if (serieses.containsKey(column)) {
                    series = serieses.get(column);
                } else {
                    serieses.put(column, series = new TimeSeries(column));
                }

                series.add(
                    new FixedMillisecond(date),
                    parseInt(record.get(column)));
            }
        }

        final List<XYDataset> result = new ArrayList<>();
        for(TimeSeries series : serieses.values()) {
            result.add(new TimeSeriesCollection(series));
        }

        return result;
    }

    private static String timeRange(ArrayList<CSVRecord> recordList) throws ParseException {
        final Date firstDate = DATE_FORMAT.parse(recordList.get(0).get(DATE));
        final Date lastDate = DATE_FORMAT.parse(recordList.get(recordList.size() -1).get(DATE));

        return String.format("%s to %s", DATE_FORMAT.format(firstDate), DATE_FORMAT.format(lastDate));
    }

    public static void main(String args[]) throws Exception {
        if (args.length < 1) {
            System.err.println("wrong # args");
            System.exit(1);
        }

        final ArrayList<CSVRecord> csvRecords = new ArrayList<CSVRecord>();
        for(String arg : args) {
            final FileReader reader = new FileReader(new File(arg));
            CSVFormat.RFC4180
                .withFirstRecordAsHeader()
                .withIgnoreSurroundingSpaces(true)
                .parse(reader)
                .forEach(csvRecords::add);
        }

        final String title = args.length == 1 ? new File(args[0]).getName() : args.length + " files";
        showTimeSeries( title,
            toDataSets(csvRecords),
            timeRange(csvRecords));
    }

}
