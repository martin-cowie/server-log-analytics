package com.pushtechnology.support.logfiledigest.infrascope;

import static com.pushtechnology.support.logfiledigest.LogfileDigester.DATE_FORMAT;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

/**
 * Make Introscope CSVs a little less wooly
 */
public class IntroscopePreprocessor {

    static class Args {
        @Parameter(required = true, names={"-f"})
        String filename;

        @Parameter(required = true, names={"-df"})
        String dateFormat;

        @Parameter(required = true, names={"-dc"})
        String dateColumn;

        @Parameter(required = true, names={"-vc"})
        String valueColumn;

        @Parameter(names={"odc"})
        String outputDateColumnName = "Date";

        @Parameter(names={"-ovc"})
        String outputValueColumnName = "Value";
    }

    public static void main(String[] argv) throws IOException, ParseException {
        final Args args = new Args();
        JCommander.newBuilder()
            .addObject(args)
            .build()
            .parse(argv);

        final FileReader reader = new FileReader(new File(args.filename));
        final CSVFormat parser = CSVFormat
            .RFC4180
            .withFirstRecordAsHeader()
            .withIgnoreSurroundingSpaces(true);

        final SimpleDateFormat df = new SimpleDateFormat(args.dateFormat);

        final Iterable<CSVRecord> records = parser.parse(reader);
        final CSVPrinter printer = CSVFormat.RFC4180
            .withHeader(args.outputDateColumnName, args.outputValueColumnName)
            .print(System.out);

        for(CSVRecord record : records) {
            final Date date = df.parse(record.get(args.dateColumn));
            final String value = record.get(args.valueColumn);


            printer.printRecord(DATE_FORMAT.format(date), value);
        }

        printer.close();
    }

}
