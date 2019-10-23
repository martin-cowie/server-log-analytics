package com.pushtechnology.support.logfiledigest.gui;

import static com.pushtechnology.support.logfiledigest.LogfileDigester.DATE_FORMAT;
import static com.pushtechnology.support.logfiledigest.gui.TimeSeriesGraphsPalette.showGraphPalette;
import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.general.SeriesException;

import com.pushtechnology.support.logfiledigest.LogfileDigester;

public class Main {
    private static final String DATE = "Date";
    private static final String LOGS_DIR = "last.logs.dir";
    private static final Preferences PREFS = Preferences.userNodeForPackage(Main.class);

    private static final FileNameExtensionFilter LICENCE_LOG_FILES =
        new FileNameExtensionFilter("Diffusion logs", "log", "csv");

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

                try {
                    series.add(
                        new FixedMillisecond(date),
                        parseInt(record.get(column)));
                    }
                catch(SeriesException ex) {
                    System.err.println(ex);
                }
            }
        }

        final List<XYDataset> result = new ArrayList<>();
        for(TimeSeries series : serieses.values()) {
            result.add(new TimeSeriesCollection(series));
        }

        return result;
    }

    /**
     * Time range from an ordered list of CSVRecords.
     * <P>
     * Suitable for graph headers.
     *
     * @throws ParseException if any of the embedded dates cannot be parsed
     */
    private static String timeRange(List<CSVRecord> recordList) throws ParseException {
        final Date firstDate = DATE_FORMAT.parse(recordList.get(0).get(DATE));
        final Date lastDate = DATE_FORMAT.parse(recordList.get(recordList.size() -1).get(DATE));

        return String.format("%s to %s", DATE_FORMAT.format(firstDate), DATE_FORMAT.format(lastDate));
    }

    //TODO: swap File for Path
    public static void main(String args[]) throws Exception {
        if (args.length > 0) {
            process((asList(args)
                .stream()
                .map(File::new)
                .collect(Collectors.toList())));
        }
        else {
            // Open an Open File dialog instead
            final String dir = PREFS.get(LOGS_DIR, null);
            final JFileChooser chooser = new JFileChooser(dir);
            chooser.setFileFilter(LICENCE_LOG_FILES);
            chooser.setMultiSelectionEnabled(true);
            chooser.setDialogTitle("Select log or CSV files");
            chooser.setApproveButtonText("Consume");

            if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(null)) {
                PREFS.put(LOGS_DIR, chooser.getCurrentDirectory().toString());
                process(asList(chooser.getSelectedFiles()));
            }
        }

    }

    private static void process(List<File> files) throws FileNotFoundException, IOException, ParseException {
        final ArrayList<CSVRecord> csvRecords = new ArrayList<CSVRecord>();
        for(File arg : files) {

            if(arg.getName().toLowerCase().endsWith(".csv")) {
                final FileReader reader = new FileReader(arg);
                CSVFormat.RFC4180
                    .withFirstRecordAsHeader()
                    .withIgnoreSurroundingSpaces(true)
                    .parse(reader)
                    .forEach(csvRecords::add);

            } else if(arg.getName().toLowerCase().endsWith(".log")) {
                //Convert to CSV on the fly
                System.err.println("Handling " + arg);
                final File csvFilename = LogfileDigester.digest(arg);
                System.err.println("Consuming temporary file " + csvFilename);
                final FileReader reader = new FileReader(csvFilename);
                CSVFormat.RFC4180
                    .withFirstRecordAsHeader()
                    .withIgnoreSurroundingSpaces(true)
                    .parse(reader)
                    .forEach(csvRecords::add);
            } else {
                System.err.println("Cannot process " + arg);
            }
        }

        if (csvRecords.isEmpty()) {
            System.err.println("Gathered no records from " + files);
            System.exit(1);
        }

        final String title = files.size() == 1 ? files.get(0).getName() : files.size() + " files";
        showGraphPalette(title,
            toDataSets(csvRecords),
            timeRange(csvRecords));
    }

}
