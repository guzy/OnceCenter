package oncecenter.util.performance.analyse;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

import oncecenter.Constants;
import oncecenter.util.XmlUtil;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;

public class DB2Metrics {
	
	/**
	 * Analyze the XML file and store the data to one metric.
	 * @param host
	 */
	public static void getMetricsTimelines(VMTreeObjectHost host) {
		host.clearMetrics();
		File performFile = new File(host.getPerformFilePath()+Constants.performname);
		if(!performFile.exists())
			return ;
		parseDocument(performFile,host);
	}
	
	public static void parseDocument(File file, VMTreeObjectHost host){
		Document document = XmlUtil.getDocument(file);
		if(document==null){
//			System.out.println("document is empty");
			return;
		}
		Element root = document.getRootElement(); 
		int rowCount = 0;
		for(Iterator<Element> child = root.elementIterator(); child.hasNext();){ 
			Element element = child.next();
			if(element.getName().equals("length")){
				 host.columns = Integer.parseInt(element.getText());
				// System.out.println("rows:"+host.rows);
			}else if(element.getName().equals("row")){
				parseRow(element, host, rowCount);
				rowCount++;
			}
			for (Map.Entry<String, List<String>> e : host.getMetrics().entrySet()) {
            	if(e.getValue().size() != rowCount) {
            		e.getValue().add("0");
            	}
            }
		}
	}
	
	public static void parseRow(Element row, VMTreeObjectHost host, int rowCount){
		for(Iterator<Element> child = row.elementIterator(); child.hasNext();){ 
			Element element = child.next();
			if(element.getName().equals("t")){
				if(rowCount == 0) {
    				host.startTime = Long.parseLong(element.getText());
    				//System.out.println("state time:"+host.startTime);
    			}
    			if(rowCount == host.columns-1) {
    				host.endTime = Long.parseLong(element.getText());
    				//System.out.println("end time:"+host.endTime);
    			}
			}else{
				String hostvmType = getMachineType(element.getName());
    			String hostvmUuid = getMachineUuid(element.getName());
    			parseValue(element,hostvmType,hostvmUuid, host, rowCount);
			}
		}
	}
	
	public static void parseValue(Element hostVm, String hostvmType, String hostvmUuid,
			VMTreeObjectHost host, int rowCount){
		for(Iterator<Element> child = hostVm.elementIterator(); child.hasNext();){ 
			Element value = child.next();
			if(value.getName().equals("v")){
				String dataType = getDataType(value.getText());
				String data = getData(value.getText());
				String key = hostvmType + ":" + hostvmUuid + ":" + dataType;
				if (host.getMetrics().containsKey(key)) {
					host.getMetrics().get(key).add(data);
				} else  {
					List<String> lt = new ArrayList<String>();
					for(int fillNum = 0; fillNum < rowCount; fillNum++) {
						lt.add("0");
					}
					lt.add(data);
					host.getMetrics().put(key, lt);
				}
			}
		}
	}
	
	/**
	 * get the machine type(host or vm)
	 * @param row
	 * @return
	 */
	public static String getMachineType(String row) {
		return row.substring(0, row.indexOf("_"));
	}
	
	/**
	 * get the uuid of machine
	 * @param row
	 * @return
	 */
	public static String getMachineUuid(String row) {
		return row.substring(row.indexOf("_")+1);
	}
	
	/**
	 * get the data type(cpu1/cpu2... or memoryusage or ....)
	 * @param hostvm
	 * @return
	 */
	public static String getDataType(String hostvm) {
		return hostvm.substring(0, hostvm.indexOf(":"));
	}
	
	/**
	 * get the data(cpu usage percent or...)
	 * @param hostvm
	 * @return
	 */
	public static String getData(String hostvm) {
		return hostvm.substring(hostvm.indexOf(":")+1);
	}

}
