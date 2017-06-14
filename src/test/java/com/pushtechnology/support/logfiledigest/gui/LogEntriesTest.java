package com.pushtechnology.support.logfiledigest.gui;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class LogEntriesTest {

    @Test
    public void testBuild() throws Exception {
        final LogEntries entries = LogEntries.build();
        assertThat(entries, notNullValue());

    }

    @Test
    public void testGetWithDesignation() throws Exception {
        final LogEntries entries = LogEntries.build();
        final LogEntry entry = entries.getWithDesignation("PUSH-000515");
        assertThat(entry.getId(), is("PUSH-000515"));
        assertThat(entry.getFormat(), is("Cannot place JMS session to provider {}."));
        assertThat(entry.getDescription(), is("An error occurred when trying to place a JMS session to the given provider."));
    }

    @Test
    public void testGetSize() throws Exception {
        final LogEntries entries = LogEntries.build();
        assertThat(entries.getSize(), greaterThan(0));
    }

}
