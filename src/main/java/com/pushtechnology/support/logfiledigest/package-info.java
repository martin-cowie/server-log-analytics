/**
 * Consumes one or more Diffusion server log files in the order presented and produces a CSV file
 * containing columns for each PUSH log entry and the count of occurences for each 15s time bucket.
 *
 * Useful for feeding to {@link com.pushtechnology.support.logfiledigest.gui}
 */
package com.pushtechnology.support.logfiledigest;
