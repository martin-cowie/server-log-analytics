package com.pushtechnology.support.logfiledigest.gui;

public final class LogEntry {
    final String id;
    final String format;
    final String description;

    public LogEntry(String id,String format,String description) {
        this.id = id;
        this.format = format;
        this.description = description;
    }

    /**
     * @return the id
     */
    String getId() {
        return id;
    }

    /**
     * @return the format
     */
    String getFormat() {
        return format;
    }

    /**
     * @return the description
     */
    String getDescription() {
        return description;
    }
}