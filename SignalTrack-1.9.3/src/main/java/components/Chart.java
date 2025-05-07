package components;

import java.awt.geom.Ellipse2D;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Chart extends JPanel {
    private static final long serialVersionUID = 1L;

    public Chart(double[][] data, String seriesLabel, String title, String xAxisLabel, String yAxisLabel) {
        buildChart(data, seriesLabel, title, xAxisLabel, yAxisLabel);   
    }
    
    private void buildChart(double[][] data, String seriesLabel, String title, String xAxisLabel, String yAxisLabel) {
    	final ChartPanel chartPanel;
    	final XYPlot plot;
    	final XYSplineRenderer renderer;
    	final JFreeChart jfc;
    	final XYSeriesCollection seriesData;
    	final XYSeries series;
        setOpaque(true);
        setDoubleBuffered(true);
        setVisible(true);

        series = new XYSeries(seriesLabel);

        seriesData = new XYSeriesCollection(series);

        jfc = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, seriesData,
                PlotOrientation.VERTICAL, false, false, false);

        plot = (XYPlot) jfc.getPlot();

        renderer = new XYSplineRenderer();

        chartPanel = new ChartPanel(jfc);

        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new Ellipse2D.Double(-1, -1, 2, 2));

        plot.setRenderer(renderer);

        chartPanel.setPreferredSize(new java.awt.Dimension(400, 300));

        jfc.setAntiAlias(true);

        for (final double[] element : data) {
            series.add(element[0], element[1]);
        }

        add(chartPanel);
    }
}
