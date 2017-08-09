package com.pushtechnology.support.logfiledigest.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;


final class PlotInfo implements Comparable<PlotInfo> {
    private final String name;
    private final XYDataset dataset;
    private final int max;
    private final XYPlot plot;

    private static PlotInfo build(XYDataset dataset, XYItemRenderer renderer) {
        final String name = dataset.getSeriesKey(0).toString();
        final DateAxis domainAxis = new DateAxis(null);
        domainAxis.setAutoRange(true);

        final XYPlot plot = new LabelledXYPlot(dataset, domainAxis, new NumberAxis(null), renderer, name, 10, 5);

        return new PlotInfo(name, dataset, plot);
    }

    public static List<PlotInfo> buildFrom(List<XYDataset> xyDatasets, XYItemRenderer renderer) {
        final List<PlotInfo> result = new ArrayList<>(xyDatasets.size());
        for(XYDataset dataset : xyDatasets) {
            result.add(build(dataset, renderer));
        }

        Collections.sort(result);
        return result;
    }

    private PlotInfo(String name, XYDataset dataset, XYPlot plot) {
        this.name = name;
        this.dataset = dataset;
        this.max = getMax(dataset);
        this.plot = plot;
    }

    private static int getMax(XYDataset dataset) {
        int result = 0;

        for(int series=0; series < dataset.getSeriesCount(); series++) {
            for(int i=0; i< dataset.getItemCount(0); i++) {
                result = Math.max(dataset.getY(0, i).intValue(), result);
            }
        }
        return result;
    }

    /**
     * @return the name
     */
    String getName() {
        return name;
    }


    /**
     * @return the dataset
     */
    XYDataset getDataset() {
        return dataset;
    }

    @Override
    public int compareTo(PlotInfo other) {
        Objects.nonNull(other);
        return other.max - this.max;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PlotInfo)) {
            return false;
        }
        final PlotInfo other = (PlotInfo)obj;
        return this.name.equals(other.name) &&
            this.dataset.equals(other.dataset) &&
            this.plot.equals(other.plot) &&
            this.max == other.max;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, dataset, plot, max);
    }

    /**
     * Returns max.
     *
     * @return the max
     */
    int getMax() {
        return max;
    }

    /**
     * Returns plot.
     *
     * @return the plot
     */
    XYPlot getPlot() {
        return plot;
    }



}