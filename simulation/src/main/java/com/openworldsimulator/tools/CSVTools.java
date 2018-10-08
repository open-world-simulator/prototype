package com.openworldsimulator.tools;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CSVTools {

    public static void writeCSV(File outputFile, int baseYear, List<String> series, List<List<Number>> numbers) throws IOException {
        BufferedWriter writer = null;

        try {

            writer = new BufferedWriter(
                    new FileWriter(outputFile, false)
            );

            System.out.println("Writing CSV to " + outputFile.getAbsolutePath());

            // Output years in columns
            int nYears = numbers.get(0).size();
            List<String> headers = new ArrayList<>();
            headers.add("Series");
            Calendar c = Calendar.getInstance();

            c.set(Calendar.DAY_OF_MONTH, 1);
            c.set(Calendar.YEAR, baseYear);
            c.set(Calendar.MONTH, Calendar.JANUARY);

            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            // Print headers as dates
            for (int i = 0; i < nYears; i++) {
                headers.add(dateFormat.format(c.getTime()));
                c.add(Calendar.MONTH, 1);
            }

            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                    .withHeader(headers.toArray(new String[0])));

            int nSeries = series.size();

            for (int i = 0; i < nSeries; i++) {
                csvPrinter.print(series.get(i));
                numbers.get(i).forEach(
                        n -> {
                            try {
                                csvPrinter.print(n);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                );
                csvPrinter.println();
            }

            csvPrinter.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
