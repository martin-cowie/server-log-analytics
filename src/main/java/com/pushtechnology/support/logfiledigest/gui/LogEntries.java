package com.pushtechnology.support.logfiledigest.gui;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class LogEntries {

    private Map<String, LogEntry> map;

    private LogEntries(Map<String, LogEntry> map) {
        this.map = map;
    }

    public static LogEntries build() throws IOException {
        final Properties props = new Properties();
        try (InputStream is = LogEntries.class.getResourceAsStream("/com/pushtechnology/diffusion/logs/i18n/messages.properties")) {
            props.load(is);
            final Map<String, LogEntry> map = new HashMap<>();

            for (String key : props.stringPropertyNames()) {
                if (key.endsWith(".ID")) {
                    final String prefix = key.substring(0, key.length() - 3);

                    final String id = props.getProperty(key);
                    final String format = props.getProperty(prefix);
                    final String description = props.getProperty(prefix + ".DESCRIPTION");

                    map.put(id, new LogEntry(id, format, description));
                }
            }

            return new LogEntries(map);
        }
    }

    public int getSize() {
        return map.size();
    }

    public LogEntry getWithDesignation(String designation) {
        return map.get(designation);
    }
}
