package com.openworldsimulator.tools.charts;

import com.openworldsimulator.tools.ConfigTools;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.Styler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistogramChartTools {
    public static final int CHART_WIDTH = ConfigTools.getConfigInt("CHART_HISTOGRAM_WIDTH", 1024);
    public static final int CHART_HEIGHT = ConfigTools.getConfigInt("CHART_HISTOGRAM_HEIGHT", 1024);
    public static final int CHART_Y_LABELS = ConfigTools.getConfigInt("CHART_HISTOGRAM_Y_LABELS", 20);


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

        long minX = 0;
        long maxX = 0;
        long minFreq = 0;
        long maxFreq = 0;
        long countTotal = 0;

        if (!histoData.isEmpty()) {
            // Calc minX and maxX values
            minX = histoData.keySet().stream().min(Long::compareTo).get();
            maxX = histoData.keySet().stream().max(Long::compareTo).get();

            minFreq = histoData.values().stream().min(Long::compareTo).get();
            maxFreq = histoData.values().stream().max(Long::compareTo).get();

            countTotal = histoData.values().stream().mapToLong(k -> k).sum();
        }

        // Automatic units detection
        double displayFactor = 1.0;

        String units = "";
        if (maxFreq > 1E6) {
            units = "M";
            displayFactor = 1E6;
        } else if (maxFreq > 1E3) {
            units = "K";
            displayFactor = 1E3;
        }

        // Create custom Y labels
        Map<Double, Object> override = new HashMap<>();
        double step = (maxFreq - minFreq) / CHART_Y_LABELS;

        for (int i = 0; i <= CHART_Y_LABELS; i++) {
            double y = minFreq + step * i;
            override.put(y,
                    String.format("%.2f", y / displayFactor)
                            + units +
                            String.format(" [%.2f%%]", y / countTotal * 100.0D));
        }

        fillDataSeries(histoData, xData, yData, minX, maxX);

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

        // Change Y axis
        chart.setYAxisLabelOverrideMap(override);

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
}
