package com.pushtechnology.support.logfiledigest.gui;

import static java.awt.Toolkit.getDefaultToolkit;
import static org.jfree.chart.JFreeChart.DEFAULT_TITLE_FONT;
import static org.jfree.ui.RefineryUtilities.centerFrameOnScreen;

import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;

public class TimeSeriesGraph {

    private final JFrame frame;
    private final XYDataset dataSet;
    private final CombinedDomainXYPlot rootPlot;


    public TimeSeriesGraph(JFrame frame,XYDataset dataSet, CombinedDomainXYPlot rootPlot) {
        this.frame = frame;
        this.dataSet = dataSet;
        this.rootPlot = rootPlot;
    }

    public static TimeSeriesGraph createWithDataset(XYDataset dataSet) {

        final String title = dataSet.getSeriesKey(0).toString();
        final JFrame frame = new JFrame(title);

        // Create the 'root' plot and associated pieces
        final DateAxis domainAxis = new DateAxis("Time");
        domainAxis.setAutoRange(true);
        final CombinedDomainXYPlot rootPlot = new CombinedDomainXYPlot(domainAxis);
        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);

        // Create the plot for the 1st dataset
        final XYPlot plot = new XYPlot(dataSet, null, new NumberAxis(), renderer);
        rootPlot.add(plot);
        XYPlotUtils.enableXYPoints(plot, true);

        final JFreeChart chart = new JFreeChart(null, DEFAULT_TITLE_FONT, rootPlot, true);

        final ColorProvider provider = new ColorProvider();
        final ChartPanel chartPanel = new ChartPanel(chart);

        chartPanel.setPreferredSize(scale(getDefaultToolkit().getScreenSize(), 0.8));
        final DropTarget dropTarget = new DropTarget(chartPanel, new DropTargetListener() {

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {
                System.err.printf("dropActionChanged(%s)%n", dtde);
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                final Transferable transferable = dtde.getTransferable();

                try {
                    final List<DataFlavor> flavours = Arrays.asList(transferable.getTransferDataFlavors());
                    System.err.printf("Got %d flavours%n", flavours.size());

                    for(DataFlavor flv : flavours) {
                        final Object data = transferable.getTransferData(flv);
                        System.err.printf("\tmimetype: \"%s\", class: \"%s\"%n", flv.getMimeType(), data.getClass().getName());
                        if(data instanceof XYDataset) {
                            final XYDataset newDataset = (XYDataset)data;
                            System.err.println("\tGot a XYDataset");

                            final XYLineAndShapeRenderer plotRenderer = new XYLineAndShapeRenderer(true, false);
                            plotRenderer.setSeriesPaint(0, provider.nextColor());
                            final XYPlot subPlot = new XYPlot(newDataset, null, new NumberAxis(), plotRenderer);

                            XYPlotUtils.enableXYPoints(subPlot, true);
                            rootPlot.add(subPlot); // creates a new graph
//                            plot.setDataset(plot.getDatasetCount(), newDataset); // Why no 'addDataSet'?
                        }
                    }
                }
                catch (UnsupportedFlavorException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {
                System.err.printf("dragOver(%s)%n", dtde);
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                System.err.printf("dragExit(%s)%n", dte);
            }

            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                System.err.printf("dragEnter(%s)%n", dtde);
            }
        });

        chartPanel.setDropTarget(dropTarget);
        frame.setContentPane(chartPanel);

        //TODO: wire up the (X) button correctly
        frame.pack();
        frame.setVisible(true);
        centerFrameOnScreen(frame);

        return new TimeSeriesGraph(frame, dataSet, rootPlot);
    }

    private static Dimension scale(Dimension dim, double d) {
        return new Dimension(
            (int)(dim.getWidth() * d),
            (int)(dim.getHeight() *d));
    }
}
