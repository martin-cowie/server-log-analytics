package com.pushtechnology.support.logfiledigest;

import static java.util.regex.Pattern.compile;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogfileDigester {

    private static final int DEFAULT_DURATION = 15;
    private static final Pattern PATTERN = compile("(\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d.\\d\\d\\d)\\|([A-Z]+).*?\\|(PUSH-\\d\\d\\d\\d\\d\\d)\\|");
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final Map<Date, Bucket> allBuckets = new TreeMap<Date, Bucket>();
    private final int duration;

    public static void main(String[] args) throws Exception {

        final LogfileDigester digester = new LogfileDigester(DEFAULT_DURATION);
        for (String arg : args) {
            digester.process(new File(arg));
        }

        // Find the set of all designations
        final List<String> designations = digester.sumDesignations();
        System.err.printf("%d designations: %s%n", designations.size(), designations);

        final File outputFile = new File(basename(args[0]) + ".csv");
        try (PrintWriter ps = new PrintWriter(outputFile)) {
            digester.printCSVData(ps, designations);
            System.err.println("Saved " + outputFile);
        }
    }

    /*package*/ LogfileDigester(int duration) {
        this.duration = duration;
    }

    /**
     * Convenience: convert a log file to a temporary CSV file.
     * @return the CSV filename
     * @throws IOException
     * @throws ParseException
     */
    public static File digest(File file) throws ParseException, IOException {
        final LogfileDigester digester = new LogfileDigester(DEFAULT_DURATION);
        digester.process(file);
        final List<String> designations = digester.sumDesignations();
        final File result = File.createTempFile(LogfileDigester.class.getSimpleName(), ".csv");
        try (PrintWriter pw = new PrintWriter(result)) {
            digester.printCSVData(pw, designations);
            return result;
        }
    }

    private List<String> sumDesignations() {
        return Bucket.sumDesignations(allBuckets.values());
    }

    private static String basename(String str) {
        final String[] tokens = str.split("\\.(?=[^\\.]+$)");
        if (tokens == null || tokens.length != 2) {
            return str;
        }
        return tokens[0];
    }

    private void printCSVData(PrintWriter ps, List<String> columns) {
        final Collection<Bucket> buckets = allBuckets.values();
        final List<String> headers = new ArrayList<>(Collections.singleton("Date"));
        headers.addAll(columns);
        final String header = headers.stream().collect(joining(", "));
        ps.println(header);
        for (Bucket bucket : buckets) {
            ps.println(bucket.toCSV(DATE_FORMAT, columns));
        }
    }

    private void process(File file) throws ParseException, IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            final Map<Date, Bucket> buckets = new TreeMap<>();

            String line;
            while ((line = br.readLine()) != null) {
                final Matcher matcher = PATTERN.matcher(line);
                if (matcher.find()) {
                    final String timeStamp = matcher.group(1);
                    final String designation = matcher.group(3);
                    final Date date = DATE_FORMAT.parse(timeStamp);
                    final Date timeBucket = quantize(date, duration);

                    final Bucket bucket;
                    if (buckets.containsKey(timeBucket)) {
                        bucket = buckets.get(timeBucket);
                    }
                    else {
                        bucket = new Bucket(timeBucket, duration);
                        buckets.put(timeBucket, bucket);
                    }

                    bucket.count(designation);
                }
            }
            allBuckets.putAll(buckets);
        }
    }

    private Date quantize(Date date, int bucketSeconds) {
        final Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        final int seconds = cal.get(Calendar.SECOND);
        cal.set(Calendar.SECOND, seconds - (seconds % bucketSeconds));
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }


}
