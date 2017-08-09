package com.pushtechnology.support.logfiledigest.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.TransferHandler;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;

/**
 * Encapsulation of a single XCDataset, a ChartPanel that shows it, and drag gesture handling.
 */
public class SingleDatasetChartPanel extends ChartPanel {

    private static final long serialVersionUID = -7472765720802581307L;

    private final XYDataset dataset;
    private final String dataSetName;

    SingleDatasetChartPanel(JFreeChart chart,XYDataset dataset, String dataSetName) {
        super(chart);
        this.dataset = dataset;
        this.dataSetName = dataSetName;
        makeDragable();
    }

    /**
     * @return the dataset
     */
    public XYDataset getDataset() {
        return dataset;
    }

    private void makeDragable() {
        final TransferHandler th = new TransferHandler("dataset");
        this.setTransferHandler(th);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {

                //TODO: only export as drag after - say - twice the double click time instead of the metakey
                if ((evt.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0) {

                    //FIXME: make this work
//                    final Image image = chartPanel.createImage(300, 200);
//                    th.setDragImage(image);
                    th.exportAsDrag(SingleDatasetChartPanel.this, evt, TransferHandler.COPY);
                    System.err.println("exportAsDrag");
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    System.out.println("Double clicked on " + dataSetName);
                    TimeSeriesGraph.createWithDataset(getDataset());
                }
            }
          });
    }



}
