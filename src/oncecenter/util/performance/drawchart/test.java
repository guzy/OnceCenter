package oncecenter.util.performance.drawchart;

import java.io.File;
import java.io.IOException;


import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeriesCollection;

public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		{
			JFreeChart chart = drawCPU.draw(new TimeSeriesCollection());
			try { 
				ChartUtilities.saveChartAsPNG(new File("D:/kongbiao/cpu.png"), chart, 1100, 500); 
				} catch (IOException e) { 
				 
				e.printStackTrace(); 
				} 
		}
		
		{
			JFreeChart chart = drawMemory.draw(new TimeSeriesCollection(),1024);
			try { 
				ChartUtilities.saveChartAsPNG(new File("D:/kongbiao/memory.png"), chart, 1100, 500); 
				} catch (IOException e) { 
				 
				e.printStackTrace(); 
				} 
		}
		{
			JFreeChart chart = drawDisk.draw(new TimeSeriesCollection());
			try { 
				ChartUtilities.saveChartAsPNG(new File("D:/kongbiao/disk.png"), chart, 1100, 500); 
				} catch (IOException e) { 
				 
				e.printStackTrace(); 
				} 
		}
		{
			JFreeChart chart = drawNet.draw(new TimeSeriesCollection());
			try { 
				ChartUtilities.saveChartAsPNG(new File("D:/kongbiao/network.png"), chart, 1100, 500); 
				} catch (IOException e) { 
				 
				e.printStackTrace(); 
				} 
		}
	}

}
