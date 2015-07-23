package oncecenter.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oncecenter.Constants;
import oncecenter.views.grouptreeview.elements.VMTreeObjectHostinGroup;
import oncecenter.views.grouptreeview.elements.VMTreeObjectPoolinGroup;
import oncecenter.views.grouptreeview.elements.VMTreeObjectRootinGroup;
import oncecenter.views.grouptreeview.elements.VMTreeObjectVMinGroup;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.once.xenapi.Host;
import com.once.xenapi.Pool;
import com.once.xenapi.VM;


public class GroupUtil {
	
	public final static String fileRoot = "GroupConfig";
	public final static String rootID = "Root";
	public final static String groupID = "Group";
	public final static String vmID = "VM";
	
	public static Map<String,Map<String,List<String>>> groupConfig;
	
	public static void loadConfig(){
		if(groupConfig == null){
			groupConfig = new HashMap<String,Map<String,List<String>>>();
		}
		File configFile = new File(FileUtil.getGroupConfigFile());
		if(configFile.exists()) {
			Document document = XmlUtil.getDocument(configFile);
			if(!isNull(document)) {
				Element fileroot = document.getRootElement();
				for(Iterator<Element> child = fileroot.elementIterator(rootID); child.hasNext();){ 
					Element rootElem = (Element) child.next();
					String rootUuid = rootElem.getText().trim();
					Map<String,List<String>> groupMap = new HashMap<String,List<String>>();
					for(Iterator<Element> child1 = rootElem.elementIterator(groupID); child1.hasNext();){ 
						Element groupElem = (Element) child1.next();
						String groupName = groupElem.getText().trim();
						List<String> vmList = new ArrayList<String>();
						for(Iterator<Element> child2 = groupElem.elementIterator(vmID); child2.hasNext();){ 
							Element vmElem = (Element) child2.next();
							String vmUuid = vmElem.getText().trim();
							vmList.add(vmUuid);
						}
						groupMap.put(groupName, vmList);
					}
					groupConfig.put(rootUuid, groupMap);
				}
			}
		} else {
			
		}
	}
	
	public static void saveConfig(){
		File groupConfigFile = new File(FileUtil.getGroupConfigFile());
		Element root = null;
		Document document = null;
		if(groupConfigFile.exists()) {
			document = XmlUtil.getDocument(groupConfigFile);
			if(!isNull(document)) {
				root = document.getRootElement();
			}
		}
		if(document == null || root == null){
			document = DocumentHelper.createDocument();
			root = document.addElement(fileRoot);
		}
		for(String rootUuid:groupConfig.keySet()){
			Map<String,List<String>> config = groupConfig.get(rootUuid);
			for(Iterator<Element> child = root.elementIterator(rootID); child.hasNext();){ 
				Element rootElem = (Element) child.next();
				//System.out.println(rootElem.getText());
				if(rootElem.getText().trim().equals(rootUuid)){
					root.remove(rootElem);
				}
			}
			Element rootElm = root.addElement(rootID);
			rootElm.setText(rootUuid);
			for(String groupName:config.keySet()){
				if(groupName.equals(Constants.VM_GROUP_DEFAULT_NAME)
						||groupName.equals(Constants.TEMPLATE_GROUP_DEFAULT_NAME))
					continue;
				Element groupElm = rootElm.addElement(groupID);
				groupElm.setText(groupName);
				for(String vmUuid:config.get(groupName)){
					Element vmElm = groupElm.addElement(vmID);
					vmElm.setText(vmUuid);
				}
				
			}
		}
//		if(Constants.VMGROUPS_TREE!=null){
//			for(VMTreeObject o : Constants.VMGROUPS_TREE.getChildren()){
//				String rootUuid = "";
//				if(o instanceof VMTreeObjectPoolinGroup){
//					VMTreeObjectPoolinGroup pool = (VMTreeObjectPoolinGroup)o;
//					rootUuid = ((Pool)pool.getPoolObject().getApiObject()).toWireString();
//				}else if (o instanceof VMTreeObjectHostinGroup){
//					VMTreeObjectHostinGroup host = (VMTreeObjectHostinGroup)o;
//					rootUuid = ((Host)host.getHostObject().getApiObject()).toWireString();
//				}
//				for(Iterator<Element> child = root.elementIterator(rootID); child.hasNext();){ 
//					Element rootElem = (Element) child.next();
//					//System.out.println(rootElem.getText());
//					if(rootElem.getText().trim().equals(rootUuid)){
//						root.remove(rootElem);
//					}
//				}
//				Element rootElm = root.addElement(rootID);
//				rootElm.setText(rootUuid);
//				for(VMTreeObject o1:o.getChildren()){
//					String groupName = o1.getName();
//					if(groupName.equals(Constants.VM_GROUP_DEFAULT_NAME)
//							||groupName.equals(Constants.TEMPLATE_GROUP_DEFAULT_NAME))
//						continue;
//					Element groupElm = rootElm.addElement(groupID);
//					groupElm.setText(groupName);
//					for(VMTreeObject o2:o1.getChildren()){
//						if(o2 instanceof VMTreeObjectVMinGroup){
//							VMTreeObjectVMinGroup vm = (VMTreeObjectVMinGroup)o2;
//							String vmUuid = ((VM)vm.getVmObject().getApiObject()).toWireString();
//							Element vmElm = groupElm.addElement(vmID);
//							vmElm.setText(vmUuid);
//						}
//					}
//				}
//			}
//		}
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("GBK");    // Ö¸¶¨XML±àÂë        
		try {
			XMLWriter writer = new XMLWriter(new FileWriter(FileUtil.getGroupConfigFile()),format);
			writer.write(document);
	        writer.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	public static Map<String,List<String>> getGroupConfig(VMTreeObject o){
		String rootUuid = "";
		if(o instanceof VMTreeObjectHostinGroup){
			Host h = (Host)(((VMTreeObjectHostinGroup) o).getHostObject().getApiObject());
			rootUuid = h.toWireString();
		}else{
			Pool p = (Pool)(((VMTreeObjectPoolinGroup) o).getPoolObject().getApiObject());
			rootUuid = p.toWireString();
		}
		if(groupConfig!=null){
			return groupConfig.get(rootUuid);
		}else{
			return null;
		}
	}
	
	public static void addGroupConfig(VMTreeObjectRootinGroup o){
		String rootUuid = "";
		if(o instanceof VMTreeObjectHostinGroup){
			Host h = (Host)(((VMTreeObjectHostinGroup) o).getHostObject().getApiObject());
			rootUuid = h.toWireString();
		}else{
			Pool p = (Pool)(((VMTreeObjectPoolinGroup) o).getPoolObject().getApiObject());
			rootUuid = p.toWireString();
		}
		groupConfig.put(rootUuid, o.groupMap);
	}
	
	private static boolean isNull(Object obj) {
		return (obj == Constants.NULL_OBJECT) ? true : false;
	}
}
