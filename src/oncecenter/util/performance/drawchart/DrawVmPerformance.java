package oncecenter.util.performance.drawchart;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
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
 * this class implements the drawing VM performance
 * @author Administrator
 *
 */
public class DrawVmPerformance {

	public JFreeChart cpuChart;
	public JFreeChart memoryChart;
	public JFreeChart netChart;
	public JFreeChart diskChart;
	
	public TimeSeriesCollection lineDatasetCPU;
	public TimeSeriesCollection lineDatasetMemory;
	public TimeSeriesCollection lineDatasetNet;	
	public TimeSeriesCollection lineDatasetDisk;
	
	public void drawVm(ConcurrentHashMap<String, List<String>> metricsTimelines, long endTime, double totalMemory, int columns,String UUID,long vmstep) {	
		/*如果xml上给的网络硬盘数据时以Byte为单位则不需乘8*/
		long step = vmstep;//单位是S
		int stepnumber = columns;
		//endTime单位是S，endTime - startTime = stepnumber * step = 监控的时间
		lineDatasetCPU = new TimeSeriesCollection();		
		lineDatasetMemory = new TimeSeriesCollection();
		TimeSeries timeSeriesMemory = new TimeSeries("UsedMemory",Second.class);		
		lineDatasetNet = new TimeSeriesCollection();	
		lineDatasetDisk = new TimeSeriesCollection();
		//iterate all metrics
		boolean hasCurrentVM = false;
		String currentKey = null;
		List<String> currentValue = null;
        for(Map.Entry<String, List<String>> e: metricsTimelines.entrySet()) {
        	if(handleUUID(e.getKey()).equals(UUID)){
        		hasCurrentVM = true;
        		currentKey = handleUUID(e.getKey());
//        		currentValue = e.getValue();
        	}
        	//get the line data of cpu
        	if(handleUUID(e.getKey()).equals(UUID)
        			&& contains(handlePart(e.getKey()),"cpu")){
        		double[] cpu = toDoubleArray(e.getValue());
        		TimeSeries timeSeriesCPU = new TimeSeries(handlePart(e.getKey()),Second.class);
        		for(int i=stepnumber-1; i>=0;i--){
        			Date date = new Date((endTime-i*step*1000));
        			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        	        String t = df.format(date);
//        	        System.out.println("用于画图的CPU信息是 = " + cpu[stepnumber-i-1]*100);
        			timeSeriesCPU.addOrUpdate(new Second(getSecond(t),getMinute(t),
        					getHour(t),getDay(t),getMonth(t),getYear(t)), cpu[stepnumber-i-1]*100);
        		}
        		//System.out.println(timeSeriesCPU.getKey().toString());
        		lineDatasetCPU.getSeries(timeSeriesCPU.getKey());
        		lineDatasetCPU.addSeries(timeSeriesCPU);
        	}
        	
        	//get the line data of memory
        	if(handleUUID(e.getKey()).equals(UUID)
        			&& handlePart(e.getKey()) .equals("mem_free")){
        		double[] freeMemorys = toDoubleArray(e.getValue());
        		for (int i = stepnumber-1; i>=0; i--) {
        			Date date = new Date((endTime-i*step*1000));
        			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        	        String t = df.format(date);
//        	        System.out.println("老版本用于画图的内存数据 = " + (totalMemory-freeMemorys[stepnumber-i-1]));
        			timeSeriesMemory.addOrUpdate(new Second(getSecond(t),getMinute(t),
        					getHour(t),getDay(t),getMonth(t),getYear(t)), totalMemory-freeMemorys[stepnumber-i-1]);
        		}
        	}
        	
        	//get the line data of vif send
        	if(handleUUID(e.getKey()).equals(UUID)
        			&& contains(handlePart(e.getKey()),"vif") && contains(handlePart(e.getKey()),"tx")){
        		double[] netSend = toDoubleArray(e.getValue());
        		TimeSeries timeSeriesNetSend = new TimeSeries("Net"+handleSign(e.getKey())+"Send",Second.class);
        		for(int i=stepnumber-1; i>=0;i--){	               
        			Date date = new Date((endTime-i*step*1000));
        			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        	        String t = df.format(date);
//        	        System.out.println("用于画图的网络发送流量是 = " + netSend[stepnumber-i-1]);
        			timeSeriesNetSend.addOrUpdate(new Second(getSecond(t),getMinute(t),
        					getHour(t),getDay(t),getMonth(t),getYear(t)), netSend[stepnumber-i-1]); 
        		}	
        		lineDatasetNet.addSeries(timeSeriesNetSend);
        	}
        	
        	//get the line data of vif Rev
        	if(handleUUID(e.getKey()).equals(UUID)
        			&& contains(handlePart(e.getKey()),"vif") && contains(handlePart(e.getKey()),"rx")){
        		double[] netRev = toDoubleArray(e.getValue());
        		TimeSeries timeSeriesNetRev = new TimeSeries("Net"+handleSign(e.getKey())+"Rev",Second.class);
        		for(int i=stepnumber-1; i>=0;i--){	               
        			Date date = new Date((endTime-i*step*1000));
        			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        	        String t = df.format(date);
//        	        System.out.println("用于画图的网络接收流量是 = " + netRev[stepnumber-i-1]);
        	        if(stepnumber-i-1 < netRev.length){
        	        	timeSeriesNetRev.addOrUpdate(new Second(getSecond(t),getMinute(t),
            					getHour(t),getDay(t),getMonth(t),getYear(t)), netRev[stepnumber-i-1]);
        	        }
        	        
        		}      
        		lineDatasetNet.addSeries(timeSeriesNetRev);
        	}
        	
        	//get the line data of vbd Read
        	if(handleUUID(e.getKey()).equals(UUID)
        			&& contains(handlePart(e.getKey()),"vbd") && contains(handlePart(e.getKey()),"read")){
        		double[] DiskRead = toDoubleArray(e.getValue());
        		TimeSeries timeSeriesDiskRead = new TimeSeries("Disk"+handleSign(e.getKey())+"Read",Second.class);
        		for(int i=stepnumber-1; i>=0;i--){	               
        			Date date = new Date((endTime-i*step*1000));
        			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        	        String t = df.format(date);
//        	        System.out.println("用于画图的硬盘读速度是 = " + DiskRead[stepnumber-i-1]/1024/1024);
        			timeSeriesDiskRead.addOrUpdate(new Second(getSecond(t),getMinute(t),
        					getHour(t),getDay(t),getMonth(t),getYear(t)), DiskRead[stepnumber-i-1]/1024/1024);
        		}	   
        		lineDatasetDisk.addSeries(timeSeriesDiskRead);
        	}
        	
        	//get the line data of vbd write
        	if(handleUUID(e.getKey()).equals(UUID)
        			&& contains(handlePart(e.getKey()),"vbd") && contains(handlePart(e.getKey()),"write")){
        		double[] DiskWrite = toDoubleArray(e.getValue());
        		TimeSeries timeSeriesDiskWrite = new TimeSeries("Disk"+handleSign(e.getKey())+"Write",Second.class);
        		for(int i=stepnumber-1; i>=0;i--){	               
        			Date date = new Date((endTime-i*step*1000));
        			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        	        String t = df.format(date);
//        	        System.out.println("用于画图的硬盘写速度是 = " + DiskWrite[stepnumber-i-1]/1024/1024);
        			timeSeriesDiskWrite.addOrUpdate(new Second(getSecond(t),getMinute(t),
        					getHour(t),getDay(t),getMonth(t),getYear(t)), DiskWrite[stepnumber-i-1]/1024/1024);
        		}	     
        		lineDatasetDisk.addSeries(timeSeriesDiskWrite);
        	}
        	              	       	
        }
        if (hasCurrentVM == true){
//        	System.out.println("键是 = " + currentKey);
        }
		
        cpuChart=drawCPU.draw(lineDatasetCPU);
        lineDatasetMemory.addSeries(timeSeriesMemory);
        memoryChart=drawMemory.draw(lineDatasetMemory, totalMemory);
        netChart=drawNet.draw(lineDatasetNet);
        diskChart=drawDisk.draw(lineDatasetDisk);
	}
	
	//save a document as a file at the given file path
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
    public static  String handleUUID(String key) {
    	String str1 = null;
    	int index1 = key.indexOf(":");
        int index3 = key.lastIndexOf(":");
        str1 = key.substring(index1+1, index3);
        int index2 = str1.indexOf(":");
        return str1.substring(index2+1);
    }
    
    public static  String handlePart(String key) {
    	int index3 = key.lastIndexOf(":");
    	return key.substring(index3+1);
    }
    
    public static String handleSign(String key) {
    	return key.substring(key.indexOf("_")+1, key.lastIndexOf("_"));
    }
    public static  int getYear(String date) {
    	int index;
    	index = date.indexOf("-");
    	return Integer.parseInt(date.substring(0, index));
    }
    
    public static  int getMonth(String date) {
    	return Integer.parseInt(date.substring(date.indexOf("-")+1, date.lastIndexOf("-")));
    }
    
    public static  int getDay(String date) {
    	return Integer.parseInt(date.substring(date.lastIndexOf("-")+1, date.indexOf(" ")));
    }
    
    public static  int getHour(String date) {
    	return Integer.parseInt(date.substring(date.indexOf(" ")+1, date.indexOf(":")));
    }
    
    public static  int getMinute(String date) {
    	return Integer.parseInt(date.substring(date.indexOf(":")+1, date.lastIndexOf(":")));
    }
    
    public static  int getSecond(String date) {
    	return Integer.parseInt(date.substring(date.lastIndexOf(":")+1));
    }
    
    public static  boolean contains(String mother, String child) {
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
			if(stringArray.get(i) != null){
				result[i] = Double.parseDouble(stringArray.get(i));
				result[i] /= 1024.0;
			}else{
				result[i] = 0.0;
			}			
		}
		return result;
	}
}
