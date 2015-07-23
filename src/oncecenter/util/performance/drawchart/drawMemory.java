package oncecenter.util.performance.drawchart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.TimeSeriesCollection;
/**
 * this class implements the drawing memory performance
 * @author Administrator
 *
 */
public class drawMemory {
	public static JFreeChart draw(TimeSeriesCollection lineDataset, double totalMemory) {
        JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(  
                "Memory(MB)",       //题 标  
                "",        
                "",  
                lineDataset,// dataset  
                false,      // legend  
                true,       // tooltips  
                true);      // URLs  

        Font font = new Font("Times New Roman", Font.CENTER_BASELINE, 12);
        TextTitle title = new TextTitle("Memory(MB)");
        title.setFont(font);
        jfreechart.setTitle(title);
        jfreechart.setBackgroundPaint(Color.white);  
      //jfreechart.getLegend().setPosition(RectangleEdge.RIGHT);
        XYPlot xyplot = (XYPlot) jfreechart.getPlot(); // 获得 plot：XYPlot！ 
        xyplot.getRangeAxis().setUpperBound(totalMemory);
        xyplot.getRangeAxis().setLowerBound(0.0);
        xyplot.setBackgroundPaint(Color.white);
        xyplot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        xyplot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        xyplot.setDomainGridlineStroke(new BasicStroke());
        xyplot.setRangeGridlineStroke(new BasicStroke());
//        FileOutputStream out = null;
//        String outputPath = "C:/workspace/data/memory.png";
//        //输出图片到磁盘--/data
//        try {        	
//			out = new FileOutputStream(outputPath);
//			ChartUtilities.writeChartAsPNG(out, jfreechart, 603, 128);
//			out.flush(); 
//			} catch (FileNotFoundException e1) {
//				e1.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			} finally {  
//                if (out != null) {  
//                    try {  
//                        out.close();  
//                    } catch (IOException e) {  
//                        // do nothing  
//                    }  
//                }  
//            }
        return jfreechart;
	}
}
