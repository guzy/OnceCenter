package oncecenter.views.xenconnectiontreeview.elements;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import oncecenter.maintabs.EventTab;
import oncecenter.maintabs.host.HostLogTab;
import oncecenter.maintabs.host.PerformanceTab;
import oncecenter.maintabs.host.SummaryTab;
import oncecenter.maintabs.host.VlanTagTab;
import oncecenter.maintabs.host.ServerVirtualTab;
import oncecenter.util.performance.drawchart.DrawHostPerformance;
import oncecenter.util.performance.grade.GradeHost;
import oncecenter.views.grouptreeview.VMTreeVMGroupView;
import oncecenter.views.grouptreeview.elements.VMTreeObjectHostinGroup;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.once.xenapi.Connection;
import com.once.xenapi.Host;
import com.once.xenapi.SR;
import com.once.xenapi.Types;
import com.once.xenapi.VM;
import com.once.xenapi.XenAPIObject;

public class VMTreeObjectHost extends VMTreeObjectRoot{
		
	transient public VM controlVM;
	transient private Host.Record record;
	//性能数据
	private String performFilePath;
	transient public DrawHostPerformance drawHost;
	transient public ConcurrentHashMap<String, List<String>> metricsTimelines;
	//解析后的性能数据
	public long endTime;
	public long startTime;
	public int columns;
	public long step;
	
	private double cpuUsagePercent=0;
	private HashMap<Integer, Double> cpuUsageList = new HashMap<Integer,Double>();
	
	//tab item
	transient private SummaryTab generalItem;
	transient private PerformanceTab performItem;
	transient private EventTab logItem;

	//单位是MB
	private double memoryTotalValue=0;
	private double memoryUsageValue=0;
	private double memoryUsagePercent=0;

	private double wNet = 0.0;
	private double rNet = 0.0;
	private HashMap<String,Double> rNetList = new HashMap<String,Double>();
	private HashMap<String,Double> wNetList = new HashMap<String,Double>();
	
	public Date performTimeStamp;
	
	//评分(0-100之间)
	public double grade = 0;
	
	public boolean isMaster = false;
	public boolean isBackup = false;
	
	public List<String> fiberList = new ArrayList<String>();
	
//	private boolean isPerformanceRecClosed = true;
	
	//transient private VMTreeObjectHostinGroup shadowHost;
	
	public VMTreeObjectHost(String name, Connection connection,
			XenAPIObject apiObject, Host.Record record) {
		super(name, connection, apiObject);
		this.setRecord(record);
		//metricsTimelines = new HashMap<String, String[]>();
	}	
		
	public double getGrade(){
		//此处可以修改
		try{
			grade=GradeHost.gradeHost(this);
			return grade;
		}catch(Exception e){
			e.printStackTrace();
			return 0;
		}
		
	}
	
//	public boolean isPerformanceRecClosed() {
//		return isPerformanceRecClosed;
//	}
//
//	public void setPerformanceRecClosed(boolean isPerformanceRecClosed) {
//		this.isPerformanceRecClosed = isPerformanceRecClosed;
//	}

	@Override
	public void addChild(VMTreeObject object) {
		if(object==null)
			return;
		if(object instanceof VMTreeObjectVM){
			int i;
			VMTreeObjectVM vmObject = (VMTreeObjectVM)object;
			boolean isOn = isOn(vmObject);
			for(i=0;i<children.size();i++){
				VMTreeObject o= children.get(i);
				if(o instanceof VMTreeObjectVM){
					VMTreeObjectVM vmO = (VMTreeObjectVM)o;
					if(isOn){
						if(isOn(vmO)&&o.getName().compareToIgnoreCase(object.getName())>0||!isOn(vmO))
							break;
					}else{
						if(!isOn(vmO)&&o.getName().compareToIgnoreCase(object.getName())>0)
							break;
					}
				}
				if(o instanceof VMTreeObjectSR ||o instanceof VMTreeObjectTemplate)
					break;
			}
			children.add(i, object);
			if(vmMap==null){
				vmMap = new HashMap<VM,VMTreeObjectVM>();
			}
			vmMap.put((VM)object.getApiObject(), (VMTreeObjectVM)object);
			object.setParent(this);
			VMTreeVMGroupView.addVM((VMTreeObjectVM)object);
			return;
		} else if (object instanceof VMTreeObjectSR){
			int i;
			for(i=0;i<children.size();i++){
				VMTreeObject o= children.get(i);
				if(o instanceof VMTreeObjectSR&&o.getName().compareToIgnoreCase(object.getName())>0||o instanceof VMTreeObjectTemplate)
					break;
			}
			children.add(i, object);
			if(srMap==null){
				srMap = new HashMap<SR,VMTreeObjectSR>();
			}
			srMap.put((SR)object.getApiObject(), (VMTreeObjectSR)object);
			object.setParent(this);
			return;
		}else if(object instanceof VMTreeObjectTemplate){
			int i;
			for(i=0;i<children.size();i++){
				VMTreeObject o= children.get(i);
				if(o instanceof VMTreeObjectTemplate&&o.getName().compareToIgnoreCase(object.getName())>0)
					break;
			}
			children.add(i, object);
			if(templateMap==null){
				templateMap = new HashMap<VM,VMTreeObjectTemplate>();
			}
			templateMap.put((VM)object.getApiObject(), (VMTreeObjectTemplate)object);
			object.setParent(this);
			return;
		}else{
			children.add(object);
			object.setParent(this);
			return;
		}
	}

	public String getPerformFilePath() {
		return performFilePath;
	}

	public void setPerformFilePath(String performFilePath) {
		this.performFilePath = performFilePath;
	}

	public double getCpuUsagePercent() {
		return cpuUsagePercent;
	}

	public HashMap<Integer, Double> getCpuUsageList() {
		return cpuUsageList;
	}

	public double getMemoryTotalValue() {
		return memoryTotalValue;
	}

	public double getMemoryUsageValue() {
		return memoryUsageValue;
	}

	public double getMemoryUsagePercent() {
		return memoryUsagePercent;
	}

	public HashMap<String, Double> getrNetList() {
		return rNetList;
	}

	public HashMap<String, Double> getwNetList() {
		return wNetList;
	}
	
	public void setMemoryTotalValue(double memoryTotalValue) {
		this.memoryTotalValue = memoryTotalValue;
	}
	
	public void setMemoryUsageValue(double memoryUsageValue) {
		this.memoryUsageValue = memoryUsageValue;
	}
	
	public void setMemoryUsagePercent(double memoryUsagePercent) {
		this.memoryUsagePercent = memoryUsagePercent;
	}

	@Override
	public void createFolder(Composite parent) {
		
		super.createFolder(parent);
		Host host = (Host)this.getApiObject();
		try {
			if(this.getItemState().equals(ItemState.able))
			{
				generalItem = new SummaryTab(folder,SWT.NONE,0,this);
				performItem = new PerformanceTab(folder,SWT.NONE,1,this);
				//new VlanTagTab(folder,SWT.NONE,2,this);
				//new ServerVirtualTab(folder,SWT.NONE,3,this);
				logItem = new EventTab(folder,SWT.NONE,2,this);
				new HostLogTab(folder,SWT.NONE,2,this);
			}
			else
			{
				generalItem = new SummaryTab(folder,SWT.NONE,0,this);
				logItem = new EventTab(folder,SWT.NONE,1,this);
			}
		} catch (Exception e) {
			
			e.printStackTrace();
			generalItem = new SummaryTab(folder,SWT.NONE,0,this);
			logItem = new EventTab(folder,SWT.NONE,1,this);
		}
		folder.setSelection(0);
	}
	
	
	public void clearMetrics() {
		if(metricsTimelines==null){
			metricsTimelines = new ConcurrentHashMap<String, List<String>>();
		}else{
			metricsTimelines.clear();
		}
		
	}
	
	public ConcurrentHashMap<String, List<String>> getMetrics(){
		return this.metricsTimelines;
	}
	
	public void setwNet(double a) {
		wNet = a;
	}
	
	public double getwNet() {
		return this.wNet;
	}
	
	public void setrNet(double a) {
		rNet = a;
	}
	
	public double getrwNet() {
		return this.rNet;
	}
	
	public void setCpuUsagePercent(double a) {
		this.cpuUsagePercent = a;
	}
	public Host.Record getRecord() {
		return record;
	}

	public boolean setRecord(Host.Record record) {
		if(record!=null){
			if(this.record!=null&&this.record.timestamp>record.timestamp)
				return false;
			this.record = record;
			this.setName(record.nameLabel);
			this.setUuid(record.uuid);
		}
		return true;
	}

//	public VMTreeObjectHostinGroup getShadowHost() {
//		return shadowHost;
//	}
//
//	public void setShadowHost(VMTreeObjectHostinGroup shadowHost) {
//		this.shadowHost = shadowHost;
//	}

}
