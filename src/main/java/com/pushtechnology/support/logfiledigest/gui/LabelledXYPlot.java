package com.pushtechnology.support.logfiledigest.gui;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;

public class LabelledXYPlot extends XYPlot {

    private static final long serialVersionUID = -625699443962995950L;

    private final String label;

    private int x;

    private int y;

    public LabelledXYPlot(XYDataset dataset,ValueAxis domainAxis,ValueAxis rangeAxis,XYItemRenderer renderer, String label, int x, int y) {
        super(dataset, domainAxis, rangeAxis, renderer);
        this.label = label;
        this.x = x;
        this.y = y;
    }
    @Override
    public void draw(Graphics2D g2,Rectangle2D area,Point2D anchor,PlotState parentState,PlotRenderingInfo info) {
        super.draw(g2, area, anchor, parentState, info);

        Rectangle2D myArea = info.getDataArea();

        final FontMetrics metrics = g2.getFontMetrics();
        final int height = metrics.getHeight();
        g2.drawString(label,
            (int)myArea.getX() + x,
            (int)myArea.getY() + y + height);

//        final FontRenderContext frc = new FontRenderContext(null, true, true);
//        final Rectangle2D bounds = g2.getFont().getStringBounds(label, frc);
//        g2.drawRect(
//            (int)(bounds.getX() + myArea.getX()),
//            (int)(bounds.getY() + myArea.getY() + height),
//            (int)bounds.getWidth(),
//            (int)bounds.getHeight());
    }

}
