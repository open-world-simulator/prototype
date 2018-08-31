package com.openworldsimulator.tools;

import org.knowm.xchart.*;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.File;
import java.io.IOException;
import java.util.*;

// TODO: Parametrize Chart Size
public class ChartTools {

    private static List<Date> buildXSeries(int baseYear, int totalMonths) {
        List<Date> xData = new ArrayList<>();

        Calendar c = Calendar.getInstance();

        // Build xData
        for (int i = 0; i < totalMonths; i++) {
            c.set(Calendar.YEAR, baseYear+1);
            c.set(Calendar.MONTH, 0);
            c.set(Calendar.DAY_OF_MONTH, 1);
            c.add(Calendar.MONTH, i);
            xData.add(c.getTime());
        }
        return xData;
    }

    public static void writeTimeChart(
            String path,
            String fileName,
            String title,
            List<Number> yData,
            int baseYear
    ) {

        List<Date> xData = buildXSeries(baseYear, yData.size());

        // Create Chart
        XYChart chart = new XYChart(1024, 800);
        chart.setTitle(title);
        chart.setXAxisTitle("Year");
        chart.setYAxisTitle(title);

        chart.getStyler().setXAxisLabelRotation(-90);

        XYSeries series = chart.addSeries(title, xData, yData);
        series.setMarker(SeriesMarkers.NONE);

        try {
            saveChart(path, fileName, chart);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeTimeChart(
            String path,
            String fileName,
            String title,
            List<String> seriesTitle,
            List<List<Number>> seriesData,
            int baseYear
    ) {
        List<Date> xData = buildXSeries(baseYear, seriesData.get(0).size());

        // Create Chart
        XYChart chart = new XYChart(1024, 800);
        chart.setTitle(title);
        chart.setXAxisTitle("Year");
        chart.setYAxisTitle(title);

        chart.getStyler().setXAxisLabelRotation(-90);

        for (int i = 0; i < seriesData.size(); i++) {
            XYSeries series = chart.addSeries(seriesTitle.get(i), xData, seriesData.get(i));
            series.setMarker(SeriesMarkers.NONE);
        }

        try {
            saveChart(path, fileName, chart);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                        .width(1024)
                        .height(800)
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

    private static void fillDataSeries(Map<Long, Long> histoData, List<Long> xData, List<Long> yData, long min, long max) {
        // For better representation, we want x values with no representation in the histogram
       // long sum = yData.stream().collect(Collectors.summingLong(l->l)).longValue();

        for (long i = min; i <= max; i++) {
            xData.add(i);
            Long value = histoData.get(i);
            if (value != null ) {
                yData.add(value);
            } else {
                yData.add(null);
            }
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
                        .width(1024)
                        .height(800)
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

    private static void saveChart(String path, String fileName, Chart chart) throws IOException {
        // Save file
        File f = new File(path, fileName + ".png");

        System.out.println("- Saving chart: " + f.getCanonicalPath());

        BitmapEncoder.saveBitmap(chart, f.getAbsolutePath(), BitmapEncoder.BitmapFormat.PNG);
    }
}
