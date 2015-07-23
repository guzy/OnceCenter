package oncecenter.util.performance.drawchart;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.w3c.dom.Document;

/**
 * this class implements the drawing host performance
 * @author Administrator
 *
 */
public class DrawHostPerformance {
	
	public JFreeChart cpuChart;
	public JFreeChart memoryChart;
	public JFreeChart netChart;
	
	public TimeSeriesCollection lineDatasetCPU;
	public TimeSeriesCollection lineDatasetMemory;
	public TimeSeriesCollection lineDatasetNic;
	
	public void drawHost(ConcurrentHashMap<String, List<String>> metricsTimelines, long endTime, double totalMemory, int columns,String UUID, long hoststep) {
		
		/*如果xml上给的网络硬盘数据时以Byte为单位则不需乘8*/
		long step = hoststep;//单位是S
		int stepnumber = columns;
		//endTime单位是S，endTime - startTime = stepnumber * step = 监控的时间
		lineDatasetCPU = new TimeSeriesCollection();		
		lineDatasetMemory = new TimeSeriesCollection();
		TimeSeries timeSeriesMemory = new TimeSeries("UsedMemory",Second.class);		
		lineDatasetNic = new TimeSeriesCollection();
		
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
		/* TimeSeries timeSeriesCPUtotal = new TimeSeries("xx",Second.class);
		for(int i = stepnumber-1; i>=0; i--) {
			double total = 0;
			
			for(Map.Entry<String, double[]> e: metricsTimelines.entrySet()) {
				if(handleUUID(e.getKey()).equals(UUID)
		        		&& contains(handlePart(e.getKey()),"cpu")){
					double[] cpu = e.getValue();
		        	total = total + cpu[i];	        		 	        		
				}
			}
			 
			Date date = new Date((endTime-i*step)*1000);
	        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        String t = df.format(date);
			timeSeriesCPUtotal.add(new Second(getSecond(t),getMinute(t),
	        					getHour(t),getDay(t),getMonth(t),getYear(t)), total*100);	
		}
		
		lineDatasetCPU.addSeries(timeSeriesCPUtotal);*/
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		if(metricsTimelines!=null){
			//iterate all the metrics
	        for(Map.Entry<String, List<String>> e: metricsTimelines.entrySet()) {
	        	
	        	//get the line data of cpu
	        	if(handleUUID(e.getKey()).equals(UUID)
	        			&& contains(handlePart(e.getKey()),"cpu")){
	        		double[] cpu = toDoubleArray(e.getValue());
	        		TimeSeries timeSeriesCPU = new TimeSeries(handlePart(e.getKey()),Second.class);
	        		for(int i=stepnumber-1; i>=0;i--){	               
	        			Date date = new Date((endTime-i*step*1000));
	        			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        	        String t = df.format(date);
	        			timeSeriesCPU.addOrUpdate(new Second(getSecond(t),getMinute(t),
	        					getHour(t),getDay(t),getMonth(t),getYear(t)), cpu[stepnumber-i-1]*100);
	        		}
	        		lineDatasetCPU.addSeries(timeSeriesCPU);
	        	}
	        	
	        	//get the line data of memory
	        	if(handleUUID(e.getKey()).equals(UUID)
	        			&& handlePart(e.getKey()) .equals("memory_free_kib")){
	        		double[] freeMemorys = toDoubleArray(e.getValue());
	        		for (int i = stepnumber-1; i>=0; i--) {
	        			Date date = new Date((endTime-i*step*1000));
	        			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        	        String t = df.format(date);
	        			timeSeriesMemory.addOrUpdate(new Second(getSecond(t),getMinute(t),
	        					getHour(t),getDay(t),getMonth(t),getYear(t)), totalMemory-freeMemorys[stepnumber-i-1]/1024);
	        		}
	        	}
	        	
	        	//get the line data of pif send
	        	if(handleUUID(e.getKey()).equals(UUID)
	        			&& contains(handlePart(e.getKey()),"pif_")&& contains(handlePart(e.getKey()),"tx")){
	        		double[] netSend = toDoubleArray(e.getValue());
	        		TimeSeries timeSeriesNIC = new TimeSeries(handleNIC(e.getKey())+"Send",Second.class);
	        		for(int i=stepnumber-1; i>=0;i--){	               
	        			Date date = new Date((endTime-i*step*1000));
	        			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        	        String t = df.format(date);
	        	        timeSeriesNIC.addOrUpdate(new Second(getSecond(t),getMinute(t),
	        					getHour(t),getDay(t),getMonth(t),getYear(t)), netSend[stepnumber-i-1]);
	        		}
	        		lineDatasetNic.addSeries(timeSeriesNIC);
	        	}
	        	
	        	//get the line data of pif Rev
	        	if(handleUUID(e.getKey()).equals(UUID)
	        			&& contains(handlePart(e.getKey()),"pif_")&& contains(handlePart(e.getKey()),"rx")){
	        		double[] netRev = toDoubleArray(e.getValue());
	        		TimeSeries timeSeriesNIC = new TimeSeries(handleNIC(e.getKey())+"Recieve",Second.class);
	        		for(int i=stepnumber-1; i>=0;i--){	               
	        			Date date = new Date((endTime-i*step*1000));
	        			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        	        String t = df.format(date);
	        	        timeSeriesNIC.addOrUpdate(new Second(getSecond(t),getMinute(t),
	        					getHour(t),getDay(t),getMonth(t),getYear(t)), netRev[stepnumber-i-1]);
	        		}
	        		lineDatasetNic.addSeries(timeSeriesNIC);       		
	        	}
	        	       	
	        }
		}
        cpuChart=drawCPU.draw(lineDatasetCPU);
        lineDatasetMemory.addSeries(timeSeriesMemory);
        memoryChart=drawMemory.draw(lineDatasetMemory, totalMemory);
        netChart=drawNet.draw(lineDatasetNic);
	}
	
	
	public static void save(Document doc, String filepath)
    {
    	TransformerFactory tFactory = TransformerFactory.newInstance();
    	try {
			Transformer transformer = tFactory.newTransformer();
			DOMSource source =new DOMSource(doc);
			StreamResult result = new StreamResult(new File(filepath));
			try {
				transformer.transform(source, result);
			} catch (TransformerException e) {
				// TODO Auto-g enerated catch block
				e.printStackTrace();
			}
		} catch (TransformerConfigurationException e) {
			
			e.printStackTrace();
		}	    		    	
    }
    
	/**
	 * 一些字符串处理函数
	 * @param key
	 * @return
	 */
    public static String handleUUID(String key) {
    	String str1 = null;
    	int index1 = key.indexOf(":");
        int index3 = key.lastIndexOf(":");
        str1 = key.substring(index1+1, index3);
        int index2 = str1.indexOf(":");
        return str1.substring(index2+1);
    }
    
    public static String handlePart(String key) {
    	int index3 = key.lastIndexOf(":");
    	return key.substring(index3+1);
    }
    
    public static String handleNIC(String key) {
    	return key.substring(key.indexOf("_")+1, key.lastIndexOf("_"));
    }
    
    public static int getYear(String date) {
    	int index;
    	index = date.indexOf("-");
    	return Integer.parseInt(date.substring(0, index));
    }
    
    public static int getMonth(String date) {
    	return Integer.parseInt(date.substring(date.indexOf("-")+1, date.lastIndexOf("-")));
    }
    
    public static int getDay(String date) {
    	return Integer.parseInt(date.substring(date.lastIndexOf("-")+1, date.indexOf(" ")));
    }
    
    public static int getHour(String date) {
    	return Integer.parseInt(date.substring(date.indexOf(" ")+1, date.indexOf(":")));
    }
    
    public static int getMinute(String date) {
    	return Integer.parseInt(date.substring(date.indexOf(":")+1, date.lastIndexOf(":")));
    }
    
    public static int getSecond(String date) {
    	return Integer.parseInt(date.substring(date.lastIndexOf(":")+1));
    }
    
       
    public static boolean contains(String mother, String child) {
    	if (mother.indexOf(child, 0)>=0) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
	public double[] toDoubleArray(List<String> stringArray) {
		if (stringArray == null) {
			return null;
		}
		double[] result = new double[stringArray.size()];
		for (int i = 0; i < stringArray.size(); i++) {
			result[i] = Double.parseDouble(stringArray.get(i));
		}
		return result;
	}
}
