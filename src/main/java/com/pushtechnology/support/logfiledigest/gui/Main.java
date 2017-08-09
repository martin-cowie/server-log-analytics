package com.pushtechnology.support.logfiledigest.gui;

import static com.pushtechnology.support.logfiledigest.LogfileDigester.DATE_FORMAT;
import static com.pushtechnology.support.logfiledigest.gui.TimeSeriesGraphsPalette.showGraphPalette;
import static java.lang.Integer.parseInt;

import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import com.pushtechnology.support.logfiledigest.LogfileDigester;

public class Main {
    private static final String DATE = "Date";

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
        if (args.length < 1) {
            System.err.println("wrong # args");
            System.exit(1);
        }

        final ArrayList<CSVRecord> csvRecords = new ArrayList<CSVRecord>();

        for(String arg : args) {

            if(arg.toLowerCase().endsWith(".csv")) {
                System.err.println("Consuming " + arg);
                final FileReader reader = new FileReader(new File(arg));
                CSVFormat.RFC4180
                    .withFirstRecordAsHeader()
                    .withIgnoreSurroundingSpaces(true)
                    .parse(reader)
                    .forEach(csvRecords::add);

            } else if(arg.toLowerCase().endsWith(".log")) {
                //Convert to CSV on the fly
                System.err.println("Handling " + arg);
                final File csvFilename = LogfileDigester.digest(new File(arg));
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
            System.err.println("Gathered no records from " + Arrays.toString(args));
            System.exit(1);
        }

        final String title = args.length == 1 ? new File(args[0]).getName() : args.length + " files";
        showGraphPalette(title,
            toDataSets(csvRecords),
            timeRange(csvRecords));
    }

}
