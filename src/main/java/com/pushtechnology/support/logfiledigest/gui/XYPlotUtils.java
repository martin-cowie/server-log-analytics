package com.pushtechnology.support.logfiledigest.gui;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

public final class XYPlotUtils {

    private XYPlotUtils() {
        /* do nothing */
    }

    public static void enableXYPoints(XYPlot plot,boolean value) {
        final XYItemRenderer renderer = plot.getRenderer();
        if(renderer instanceof XYLineAndShapeRenderer) {
            final XYLineAndShapeRenderer lineAndShapeRenderer = (XYLineAndShapeRenderer)renderer;
            lineAndShapeRenderer.setBaseShapesVisible(value);

            //TODO: lighter colour for the shape
        }
    }

}
