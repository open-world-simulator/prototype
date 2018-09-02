package com.openworldsimulator.tools;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.Styler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HistogramChartTools {
    public static final int CHART_WIDTH = 1024;
    public static final int CHART_HEIGHT = 800;


    private static void fillDataSeries(Map<Long, Long> histoData, List<Long> xData, List<Long> yData, long min, long max) {
        // For better representation, we want x values with no representation in the histogram
        // long sum = yData.stream().collect(Collectors.summingLong(l->l)).longValue();

        for (long i = min; i <= max; i++) {
            xData.add(i);
            Long value = histoData.get(i);
            if (value != null) {
                yData.add(value);
            } else {
                yData.add(null);
            }
        }
    }

    private static void saveChart(String path, String fileName, Chart chart) throws IOException {
        // Save file
        File f = new File(path, fileName + ".png");

        System.out.println("- Saving chart: " + f.getCanonicalPath());

        BitmapEncoder.saveBitmap(chart, f.getAbsolutePath(), BitmapEncoder.BitmapFormat.PNG);
    }

    /**
     * Writes an histogram like chart for frequency of long ranges
     *
     * @param path
     * @param fileName
     * @param title
     * @param histoData
     * @throws IOException
     */
    public static void writeHistoChart(
            String path,
            String fileName,
            String title,
            Map<Long, Long> histoData
    ) {

        List<Long> xData = new ArrayList<>();
        List<Long> yData = new ArrayList<>();

        long min = 0;
        long max = 0;

        if (!histoData.isEmpty()) {
            // Calc min and max values
            min = histoData.keySet().stream().min(Long::compareTo).get();
            max = histoData.keySet().stream().max(Long::compareTo).get();
        }
        fillDataSeries(histoData, xData, yData, min, max);

        // Create Chart
        CategoryChart chart =
                new CategoryChartBuilder()
                        .width(CHART_WIDTH)
                        .height(CHART_HEIGHT)
                        .title(title)
                        .xAxisTitle(title)
                        .yAxisTitle("Frequency").build();

        // Customize Chart
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setXAxisLabelRotation(-90);
        chart.getStyler().setHasAnnotations(false);

        // Series
        chart.addSeries(title,
                xData,
                yData);

        try {
            saveChart(path, fileName, chart);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes an histogram like chart for frequency of long ranges with 2 series
     */
    public static void writeHistoChart(
            String path,
            String fileName,
            String title,
            String series1,
            Map<Long, Long> histoData1,
            String series2,
            Map<Long, Long> histoData2
    ) {

        long min = 0;
        long max = 0;

        if (!histoData1.isEmpty()) {
            // Calc min and max values
            min = histoData1.keySet().stream().min(Long::compareTo).get();
            max = histoData1.keySet().stream().max(Long::compareTo).get();
        }

        if (!histoData2.isEmpty()) {
            // Calc min and max values
            min = Math.min(min, histoData2.keySet().stream().min(Long::compareTo).get());
            max = Math.max(max, histoData1.keySet().stream().max(Long::compareTo).get());
        }

        List<Long> xData1 = new ArrayList<>();
        List<Long> yData1 = new ArrayList<>();
        List<Long> xData2 = new ArrayList<>();
        List<Long> yData2 = new ArrayList<>();

        fillDataSeries(histoData1, xData1, yData1, min, max);
        fillDataSeries(histoData2, xData2, yData2, min, max);

        // Create Chart
        CategoryChart chart =
                new CategoryChartBuilder()
                        .width(CHART_WIDTH)
                        .height(CHART_HEIGHT)
                        .title(title)
                        .xAxisTitle(title)
                        .yAxisTitle("Frequency").build();

        // Customize Chart
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setXAxisLabelRotation(-90);
        chart.getStyler().setHasAnnotations(false);

        // Series
        chart.addSeries(series1,
                xData1,
                yData1);

        chart.addSeries(series2,
                xData2,
                yData2);

        try {
            saveChart(path, fileName, chart);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
