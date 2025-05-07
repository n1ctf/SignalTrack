package radio;

import java.awt.BasicStroke;
import java.awt.Color;
import java.text.DecimalFormat;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.IntervalXYDataset;

public class RadioCalibrationPlot extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int PRECISION = 1;
    private static final String X_DECIMAL_FORMAT = "##0";
    private static final String Y_DECIMAL_FORMAT = "-##0.0";
    private static final int W = 525;
    private static final int H = 325;

    private final transient IntervalXYDataset dataset;
    private final String xAxisLabel;
    private final String yAxisLabel;

    public RadioCalibrationPlot(IntervalXYDataset dataset, String xAxisLabel, String yAxisLabel) {
        this.dataset = dataset;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        buildChart();
    }
    
    private void buildChart() {
        final ChartPanel chart = new ChartPanel(createChart(), W, H, W, H, W, H, false, true, true, true, true, true);
        add(chart);
    }

    private JFreeChart createChart() {
        final XYItemRenderer renderer = new XYSplineRenderer(PRECISION);

        renderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator(
                StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
                new DecimalFormat(X_DECIMAL_FORMAT), new DecimalFormat(Y_DECIMAL_FORMAT)));

        renderer.setSeriesStroke(0, new BasicStroke(0.5F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        renderer.setSeriesPaint(0, Color.BLUE);

        final ValueAxis rssiAxis = new NumberAxis(xAxisLabel);
        rssiAxis.setRange(new Range(dataset.getEndXValue(0, 0), dataset.getEndXValue(0, dataset.getItemCount(0) - 1)));
        final ValueAxis dBmAxis = new NumberAxis(yAxisLabel);
        dBmAxis.setRange(new Range(dataset.getEndYValue(0, 0), dataset.getEndYValue(0, dataset.getItemCount(0) - 1)));
        final XYPlot plot = new XYPlot(dataset, rssiAxis, dBmAxis, renderer);
        plot.setBackgroundPaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setDomainGridlinesVisible(true);

        final JFreeChart chart = new JFreeChart(xAxisLabel + " x " + yAxisLabel, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        chart.setBackgroundPaint(Color.WHITE);
        return chart;
    }

}
