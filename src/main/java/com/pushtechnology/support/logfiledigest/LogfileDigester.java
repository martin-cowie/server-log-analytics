package com.pushtechnology.support.logfiledigest;

import static com.pushtechnology.support.logfiledigest.Bucket.sumDesignations;
import static java.util.stream.Collectors.joining;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogfileDigester {

    private static final int DURATION = 15;
    private static final Pattern pattern = Pattern.compile("(\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d.\\d\\d\\d)\\|([A-Z]+).*?\\|(PUSH-\\d\\d\\d\\d\\d\\d)\\|");
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws Exception {

        final Map<Date, Bucket> allBuckets = new TreeMap<Date, Bucket>();
        for(String arg : args) {
            allBuckets.putAll(process(new File(arg)));
        }
        final List<Date> dates = new ArrayList<Date>(allBuckets.keySet());
        System.err.printf("Found %d buckets in %d files, from %s to %s\n", allBuckets.size(), args.length, dates.get(0), dates.get(dates.size()-1));

        // Find the set of all designations
        final List<String> designations = sumDesignations(allBuckets.values());
        System.err.printf("%d designations: %s\n", designations.size(), designations);

        final File outputFile = new File(basename(args[0]) + ".csv");
        try (final PrintWriter ps = new PrintWriter(outputFile)) {
            printCSVData(ps, designations, allBuckets.values());
            System.err.println("Saved " + outputFile);
        }

    }

    private static String basename(String str) {
        final String[] tokens = str.split("\\.(?=[^\\.]+$)");
        if(tokens == null || tokens.length != 2) {
            return str;
        }
        return tokens[0];
    }

    private static void printCSVData(PrintWriter ps, List<String> columns, Iterable<Bucket> buckets) {
        final List<String> headers = new ArrayList<>(Collections.singleton("Date"));
        headers.addAll(columns);
        final String header = headers.stream().collect(joining(", "));
        ps.println(header);
        for(Bucket bucket : buckets) {
            ps.println(bucket.toCSV(DATE_FORMAT, columns));
        }
    }

    private static Map<Date,Bucket> process(File file) throws ParseException, IOException {
        final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        final Map<Date, Bucket> buckets = new TreeMap<>();

        String line;
        while ((line = br.readLine()) != null) {
            final Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                final String timeStamp = matcher.group(1);
                final String designation = matcher.group(3);
                final Date date = DATE_FORMAT.parse(timeStamp);
                final Date timeBucket = quantize(date, DURATION);

                final Bucket bucket;
                if (buckets.containsKey(timeBucket)) {
                    bucket = buckets.get(timeBucket);
                }
                else {
                    bucket = new Bucket(timeBucket, DURATION);
                    buckets.put(timeBucket, bucket);
                }

                bucket.count(designation);
            }
        }
        br.close();
        return buckets;
    }

    private static Date quantize(Date date,int bucketSeconds) {
        final Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        final int seconds = cal.get(Calendar.SECOND);
        cal.set(Calendar.SECOND, seconds - (seconds % bucketSeconds));
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

}
