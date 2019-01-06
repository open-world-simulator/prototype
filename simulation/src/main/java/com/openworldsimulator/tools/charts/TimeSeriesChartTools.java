package com.openworldsimulator.tools.charts;

import com.openworldsimulator.tools.ConfigTools;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TimeSeriesChartTools {
    public static final int CHART_SMOOTHING_PERIOD = ConfigTools.getConfigInt("CHART_TIMES_SMOOTHING", 12);
    public static final int CHART_WIDTH = ConfigTools.getConfigInt("CHART_TIMES_WIDTH", 1024);
    public static final int CHART_HEIGHT = ConfigTools.getConfigInt("CHART_TIMES_HEIGHT", 800);
    public static final int CHART_Y_LABELS = ConfigTools.getConfigInt("CHART_TIMES_Y_LABELS", 5);

    private static List<Date> buildXSeries(int baseYear, int totalMonths) {
        List<Date> xData = new ArrayList<>();

        Calendar c = Calendar.getInstance();

        // Build xData
        for (int i = 0; i < totalMonths; i++) {
            c.set(Calendar.YEAR, baseYear + 1);
            c.set(Calendar.MONTH, 0);
            c.set(Calendar.DAY_OF_MONTH, 1);
            c.add(Calendar.MONTH, i);
            xData.add(c.getTime());
        }
        return xData;
    }

    private static List<Number> smoothing(List<Number> data, int window, double scalingFactor) {
        List<Number> newData = new ArrayList<>(data.size());
        for (int i = 0; i < data.size(); i++) {
            int min = i - window;
            if (min < 0) min = 0;
            Double average = data.subList(min, i + 1).stream().mapToDouble(Number::doubleValue).average().getAsDouble();
            newData.add(average * scalingFactor);
        }
        return newData;
    }

    private static void saveChart(String path, String fileName, Chart chart) throws IOException {
        // Save file
        File f = new File(path, fileName + ".png");

        System.out.println("- Saving chart: " + f.getCanonicalPath());

        BitmapEncoder.saveBitmap(chart, f.getAbsolutePath(), BitmapEncoder.BitmapFormat.PNG);
    }

    /* ---------------------------------------------------------------------------------------------------------------*/

    public static void writeTimeSeriesChart(
            String path,
            String fileName,
            String chartTitle,
            String yTitle,
            List<String> seriesTitle,
            List<List<Number>> seriesData,
            int baseYear
    ) throws IOException {
        List<Date> xData = buildXSeries(baseYear, seriesData.get(0).size());

        // Create Chart
        XYChart chart = new XYChart(CHART_WIDTH, CHART_HEIGHT);
        chart.setTitle(chartTitle);
        chart.setXAxisTitle("Year");
        chart.setYAxisTitle(yTitle);

        chart.getStyler().setXAxisLabelRotation(-90);

        double minValue = Double.MAX_VALUE;
        double maxValue = Double.MIN_VALUE;
        double displayFactor = 1.0;

        // Get
        for (List<Number> aSeriesData : seriesData) {
            DoubleSummaryStatistics statistics = aSeriesData.stream().mapToDouble(Number::doubleValue).summaryStatistics();
            minValue = Math.min(minValue, statistics.getMin());
            maxValue = Math.max(maxValue, statistics.getMax());
        }

        // Automatic units detection
        String units = "";
        if (maxValue > 1E6) {
            units = "M";
            displayFactor = 1E6;
        } else if (maxValue > 1E3) {
            units = "K";
            displayFactor = 1E3;
        }

        minValue /= displayFactor;
        maxValue /= displayFactor;

        // Create custom Y labels
        Map<Double, Object> override = new HashMap<>();
        double step = (maxValue - minValue) / CHART_Y_LABELS;

        for (int i = 0; i <= CHART_Y_LABELS; i++) {
            double y = minValue + step * i;
            override.put(y, String.format("%.2f", y) + units);
        }

        // Add 0 if crossed
        if (minValue <= 0 && maxValue >= 0) {
            override.put(0D, "0.0");
        }

        for (int i = 0; i < seriesData.size(); i++) {
            List<Number> data = smoothing(seriesData.get(i), CHART_SMOOTHING_PERIOD, 1D / displayFactor);

            double first = data.get(0).doubleValue();
            double last = data.get(data.size() - 1).doubleValue();
            DoubleSummaryStatistics statistics = data.stream().mapToDouble(Number::doubleValue).summaryStatistics();
            double seriesMinValue = statistics.getMin();
            double seriesMaxValue = statistics.getMax();

            double change = 0;
            if (first != 0) {
                change = (last - first) / first * 100.D;
            }

            //override.put(first, String.format("%.2f", first));

            String resultSummary = String.format("%.2f%s to %.2f%s", first, units, last, units);
            if (change <= 1000) {
                resultSummary += String.format(" [%.2f%%]", change);
            }
            if( !(seriesMinValue == first && seriesMaxValue == last) &&
                !(seriesMinValue == last  && seriesMaxValue == first)) {
                resultSummary += String.format("\nMin: %.2f%s Max: %.2f%s", seriesMinValue, units, seriesMaxValue, units);
            }

            XYSeries series = chart.addSeries(seriesTitle.get(i) + "\n" + resultSummary, xData, data);
            series.setMarker(SeriesMarkers.NONE);
        }

        chart.setYAxisLabelOverrideMap(override);
        //chart.setYAxisLabelOverrideMap(override, 2);

        saveChart(path, fileName, chart);
    }

    public static void writeTimeSeriesChart(
            String path,
            String fileName,
            String chartTitle,
            String yTitle,
            List<Number> yData,
            int baseYear
    ) throws IOException {
        writeTimeSeriesChart(path,
                fileName,
                chartTitle,
                yTitle,
                Collections.singletonList(yTitle),
                Collections.singletonList(yData),
                baseYear
        );
    }
}
