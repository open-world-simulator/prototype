package com.openworldsimulator.tools;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestCSVExport {

    @Test
    public void testExportCSV() throws IOException {
        File baseDir = new File(".", "output-tests");
        File outputFile = new File(baseDir, "test.csv");

        CSVTools.writeCSV(
                outputFile,
                2010,
                Arrays.asList("series 1", "series 2", "series 3"),
                Arrays.asList(
                        buildTestSeries(10, 1000, 0.1D),
                        buildTestSeries(10, 2000, 10D),
                        buildTestSeries(10, 3000, 100D)
                )
        );
    }

    private List<Number> buildTestSeries(int length, double start, double delta) {
        List<Number> res = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            res.add(
                    start + delta * i
            );
        }
        return res;
    }
}
