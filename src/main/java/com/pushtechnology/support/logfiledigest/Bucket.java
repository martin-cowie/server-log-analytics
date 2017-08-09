package com.pushtechnology.support.logfiledigest;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.TreeSet;

public final class Bucket {

    final Date startDate;
    final int durationInSeconds;
    final Map<String, Integer> bucketData = new TreeMap<>();

    public Bucket(Date startDate,int durationInSeconds) {
        this.startDate = new Date(startDate.getTime());
        this.durationInSeconds = durationInSeconds;
    }

    public void count(String designation) {
        final int newValue = (bucketData.containsKey(designation)) ? bucketData.get(designation) +1 : 1;
        bucketData.put(designation, newValue);
    }

    /**
     * @return the results
     */
    Map<String,Integer> getData() {
        return bucketData;
    }

    @Override
    public String toString() {
        return String.format("{date: %s, duration: %d, data=%d}", startDate, durationInSeconds, bucketData.size());
    }

    public static List<String> sumDesignations(Iterable<Bucket> buckets) {
        final Set<String> result = new TreeSet<>();
        for (Bucket bucket : buckets) {
            result.addAll(bucket.getData().keySet());
        }

        return new ArrayList<>(result);
    }

    public String toCSV(DateFormat df, List<String> designations) {
        final StringJoiner result = new StringJoiner(", ");
        result.add(df.format(startDate));

        for (String designation : designations) {
            result.add(Integer.toString(getValue(designation)));
        }
        return result.toString();
    }

    /**
     * Returns startDate.
     *
     * @return the startDate
     */
    Date getStartDate() {
        return startDate;
    }

    /**
     * Returns durationInSeconds.
     *
     * @return the durationInSeconds
     */
    int getDurationInSeconds() {
        return durationInSeconds;
    }

    public Integer getValue(String designation) {
        return (bucketData.containsKey(designation)) ?
            bucketData.get(designation) :
            0;
    }

}
