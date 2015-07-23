package oncecenter.views.xenconnectiontreeview.elements;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import oncecenter.action.vm.PerformAlarmAction;
import oncecenter.maintabs.EventTab;
import oncecenter.maintabs.vm.CPUBindTab;
import oncecenter.maintabs.vm.ConsoleTab;
import oncecenter.maintabs.vm.DiskAdjustTab;
import oncecenter.maintabs.vm.NetWorkTab;
import oncecenter.maintabs.vm.OpenRecordFlag;
import oncecenter.maintabs.vm.PriorityTab;
import oncecenter.maintabs.vm.VMPerformanceTab;
import oncecenter.maintabs.vm.VMSnapShotsTab;
import oncecenter.maintabs.vm.VMSummaryTab;
import oncecenter.util.performance.drawchart.DrawVmPerformance;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.once.xenapi.Connection;
import com.once.xenapi.VDI;
import com.once.xenapi.VM;
import com.once.xenapi.XenAPIObject;
import com.tightvnc.vncviewer.VncViewer;

public class VMTreeObjectVM extends VMTreeObject {
	
	public VMTreeObjectVM(String name, Connection connection,
			XenAPIObject apiObject, VM.Record record) {
		super(name, connection, apiObject);
		this.setRecord(record);
		if(record!=null){
			if(record.ipaddr!=null)
				this.setIpAddress(new ArrayList<String>(record.ipaddr));
			this.subNetMask = "";
			this.gateway = "";
			this.macAddress = record.MAC;
		}else{
			this.setIpAddress(new ArrayList<String>());
			this.subNetMask = "";
			this.gateway = "";
			this.macAddress = new HashSet<String>();
		}
		
		this.rate = "1000";
		this.burst = "100";
	}
	public String getSubNetMask() {
		return subNetMask;
	}
	public void setSubNetMask(String subNetMask) {
		this.subNetMask = subNetMask;
	}
	transient private VM.Record record;
	private List<String> ipAddress;
	private String subNetMask;
	private Set<String> macAddress;
	private String gateway;
	public String getGateway() {
		return gateway;
	}
	public void setGateway(String gateway) {
		this.gateway = gateway;
	}
	
	private String rate;
	private String burst;
	
	private int priority=1;
	
	//性能数据
	transient public DrawVmPerformance drawVM;
	transient public ConcurrentHashMap<String, List<String>> metricsTimelines;
	
	//解析后的性能数据
	public long endTime;
	public long startTime;
	public int columns;
	public long step;
	
	private double cpuUsagePercent=0;
	private HashMap<Integer, Double> cpuUsageList = new HashMap<Integer,Double>();
	
	//单位是MB
	private double memoryTotalValue=0;
	private double memoryUsageValue=0;
	private double memoryUsagePercent=0;
	 
	private double diskTotalValue=0;
	private double diskUsageValue=0;
	private double diskUsagePercent=0;
	
	
	transient private ArrayList<Disk> diskList = new ArrayList<Disk>();
	
	
	private double wNet = 0.0;
	private double rNet = 0.0;
	private HashMap<String,Double> rNetList = new HashMap<String,Double>();
	private HashMap<String,Double> wNetList = new HashMap<String,Double>();
		
	//虚拟机上部署的应用
	private ResourceTypes appType = ResourceTypes.UNRECOGNIZED;
	private String appName="";
	
	//VDI信息

	//vncViewer
	transient public VncViewer localVncViewer;
	private int vncPort=0;
		
	//阈值
	public double CPU_LOWER_LIMIT=0;
	public double CPU_UPPER_LIMIT=80;
	
	public double MEMONY_LOWER_LIMIT = 0;
	public double MEMONY_UPPER_LIMIT = 60;
	
	public double DISK_LOWER_LIMIT = 0;
	public double DISK_UPPER_LIMIT = 70;
	
	//Tab item
	transient private VMSummaryTab vmGeneralItem;
	transient private ConsoleTab consoleItem;
	transient private VMPerformanceTab vmPerformItem;
	transient private CPUBindTab cpubindItem;
	transient private DiskAdjustTab diskAdjustItem;
	transient private VMSnapShotsTab snapshotItem;
	transient private NetWorkTab networkItem;
	transient private EventTab logItem;
	
	transient private VMTreeObjectSR storageObject;
	transient private VDI vdi;

	transient private PriorityTab priorityItem;
	
	
	public void alarm(){
		switch(appType){
		case CPU:
			if(cpuUsagePercent>CPU_UPPER_LIMIT){
				PerformAlarmAction action = new PerformAlarmAction(this, ResourceTypes.CPU);
				action.run();
			}
			break;
		case MEMORY:
			if(memoryUsagePercent>MEMONY_UPPER_LIMIT){
				PerformAlarmAction action = new PerformAlarmAction(this, ResourceTypes.MEMORY);
				action.run();
			}
			break;
		case DISK:
			if(diskUsagePercent>DISK_UPPER_LIMIT){
				PerformAlarmAction action = new PerformAlarmAction(this, ResourceTypes.DISK);
				action.run();
			}
			break;
		}
	}
	public boolean setVncViewer(Frame frame){
		//VM vm=(VM)this.getApiObject();
//		VNCConsole console=new VNCConsole(vm,this.getConnection());
//		frame.add(console);
//		frame.pack();
//		frame.setVisible(true);
		//
		//frame.setBackground(Color.BLUE);
		localVncViewer = new VncViewer();
		//localVncViewer.setBackground(Color.RED);
		String hostIp;
		VMTreeObjectHost host = (VMTreeObjectHost)this.getParent();
		hostIp = host.getIpAddress();
		String [] args=new String [4];
		 args[0]="HOST";
		 args[1]=hostIp;
		 args[2]="PORT";
//		 int port=Integer.MAX_VALUE;
		 VM instance=(VM)this.getApiObject();
		 try{
			 String location =  instance.getVNCLocation(connection);
			 args[3] = location.substring(location.indexOf(":")+1);
			 System.out.println("vnclocation:"+args[3]);
		 }catch(Exception e){
			 e.printStackTrace();
			 return false;
		 }
//		 Iterator<Console> cIt=null;
//		 try{
//			 cIt = instance.getConsoles(connection).iterator();
//		 }catch(Exception e){
//			 e.printStackTrace();
//			 return false;
//		 }
//		 String location="";
//			while (cIt.hasNext()){
//				try{
//					location=cIt.next().getLocation(connection);
//				}catch(Exception e){
//					e.printStackTrace();
//					continue;
//				}
//				//System.out.println(location);
//				//break;
//				//System.out.println(cIt.next().getRecord(connection));
//				int len=location.length();
//				if( len>5&&location.charAt(len-5)==':'){
//					port=Integer.parseInt(location.substring(len-4));
//					break;
//				}
//			} 
//			if(port==Integer.MAX_VALUE)
//				return false;
//			args[3]=port+"";
//		 args[4]="Show Controls";
//		 args[5]="No";
		 localVncViewer.mainArgs=args;
		 localVncViewer.inAnApplet = false;
		 localVncViewer.inSeparateFrame = true;
		 localVncViewer.vncFrame=frame;
		 
		 localVncViewer.init();
		 
//		 localVncViewer.readParameters();
//		 localVncViewer.showControls = false;
//		 
//		 frame.add("Center", localVncViewer);
//		 localVncViewer.vncContainer = localVncViewer.vncFrame;
//		 
//		 localVncViewer.recordingSync = new Object();
//
//		 localVncViewer.options = new OptionsFrame(localVncViewer);
//		 localVncViewer.clipboard = new ClipboardFrame(localVncViewer);
//		    if (RecordingFrame.checkSecurity()) {
//		    	localVncViewer.rec = new RecordingFrame(localVncViewer);
//		    }
//		    localVncViewer.sessionFileName = null;
//		    localVncViewer.recordingActive = false;
//		    localVncViewer.recordingStatusChanged = false;
//		    localVncViewer.cursorUpdatesDef = null;
//		    localVncViewer.eightBitColorsDef = null;
//
//		    localVncViewer.vncFrame.addWindowListener(localVncViewer);
//		    localVncViewer.rfbThread = new Thread(localVncViewer);
//		    localVncViewer.rfbThread.start();
//		    
		    localVncViewer.start();
		    
		    return true;
	}

	@Override
	public void addChild(VMTreeObject object) {
		if(object==null)
			return;
		children.add(object);
		object.setParent(this);
		return;
	}
	
	public int getVncPort() {
		return vncPort;
	}

	public void setVncPort(int vncPort) {
		this.vncPort = vncPort;
	}

	public ResourceTypes getAppType() {
		return appType;
	}

	public void setAppType(ResourceTypes appType) {
		this.appType = appType;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}
	
	public double getCpuUsagePercent() {
		return cpuUsagePercent;
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

	public double getDiskTotalValue() {
		return diskTotalValue;
	}

	public double getDiskUsageValue() {
		return diskUsageValue;
	}

	public double getDiskUsagePercent() {
		return diskUsagePercent;
	}
	
	public HashMap<String, Double> getrNetList() {
		return rNetList;
	}

	public HashMap<String, Double> getwNetList() {
		return wNetList;
	}
	
	public ArrayList<Disk> getDiskList() {
		return diskList;
	}

	public class Disk
	{
		private String uuid;
		private String nameLabel;
		private String nameDescription;
		private String location;
		private double totalValue;
		private double usageValue;
		private double availableSpace;
		private VDI vdi;
		private double maxValue;
		public String getUuid() {
			return uuid;
		}
		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
		public String getNameLabel() {
			return nameLabel;
		}
		public void setNameLabel(String nameLabel) {
			this.nameLabel = nameLabel;
		}
		public String getNameDescription() {
			return nameDescription;
		}
		public void setNameDescription(String nameDescription) {
			this.nameDescription = nameDescription;
		}
		public String getLocation() {
			return location;
		}
		public void setLocation(String location) {
			this.location = location;
		}
		public double getTotalValue() {
			return totalValue;
		}
		public void setTotalValue(double totalValue) {
			this.totalValue = totalValue;
		}
		public double getUsageValue() {
			return usageValue;
		}
		public void setUsageValue(double usageValue) {
			this.usageValue = usageValue;
		}
		public double getAvailableSpace() {
			return availableSpace;
		}
		public void setAvailableSpace(double availableSpace) {
			this.availableSpace = availableSpace;
		}
		public VDI getVdi() {
			return vdi;
		}
		public void setVdi(VDI vdi) {
			this.vdi = vdi;
		}
		public double getMaxValue() {
			return maxValue;
		}
		public void setMaxValue(double maxValue) {
			this.maxValue = maxValue;
		}
	}
	
	public enum ResourceTypes {
		/**
		 * The value does not belong to this enumeration.
		 */
		UNRECOGNIZED,
		/**
		 * CPU resource related type.
		 */
		CPU,
		/**
		 * Network throughput related type.
		 */
		THROUGHPUT,
		/**
		 * Memory related type.
		 */
		MEMORY,
		/**
		 * Disk storage related type.
		 */
		DISK;
		
		public String toString() {
			if (this==UNRECOGNIZED) return "UNRECOGNIZED";
			if (this==CPU) return "cpu";
			if (this==THROUGHPUT) return "throughput";
			if (this==MEMORY) return "memory";
			if (this==DISK) return "disk";
			/* this can never be reached.*/
			return "illegal enum";
		}
	}
	
	public void create(){
		
	}

	@Override
	public void createFolder(Composite parent) {
		super.createFolder(parent);
		vmGeneralItem = new VMSummaryTab(folder,SWT.NONE,0,this);
		consoleItem = new ConsoleTab(folder,SWT.NONE,1,this);
		if (this.getParent() instanceof VMTreeObjectHost) {
			VMTreeObjectHost objectHost = (VMTreeObjectHost) this.getParent();
		}
		vmPerformItem = new VMPerformanceTab(folder,SWT.NONE,2,this);
		cpubindItem = new CPUBindTab(folder,SWT.NONE,3,this);
		diskAdjustItem = new DiskAdjustTab(folder,SWT.NONE,4,this);
		networkItem = new NetWorkTab(folder,SWT.NONE,5,this);
		//priorityItem = new PriorityTab(folder,SWT.NONE,6,this);
		snapshotItem = new VMSnapShotsTab(folder,SWT.NONE,6,this);
		logItem = new EventTab(folder,SWT.NONE,7,this);
		folder.setSelection(0);
	}
	
	public void startVM(){		
		if(consoleItem!=null&&consoleItem.composite!=null)
//			consoleItem.consoleFresh(true);
			consoleItem.Init();
	}
	
	public void shutVM(){
		if(consoleItem!=null&&consoleItem.composite!=null)
			consoleItem.consoleFresh(false);
	}
	
	public void disconnectVNCClient(){
		if(localVncViewer!=null)
			localVncViewer.disconnect();
	}
	
	public void refresh()
	{
		if(vmGeneralItem!=null){
			vmGeneralItem.refresh();
		}
		if(logItem!=null){
			logItem.logFresh();
		}
		if(networkItem!=null){
			networkItem.refresh();
		}
	}

	public VMTreeObjectSR getStorageObject() {
		return storageObject;
	}

	public void setStorageObject(VMTreeObjectSR storageObject) {
		this.storageObject = storageObject;
	}

	public VDI getVdi() {
		return vdi;
	}

	public void setVdi(VDI vdi) {
		this.vdi = vdi;
	}
	
	public void newMetics() {
		this.metricsTimelines = new ConcurrentHashMap<String, List<String>>();
	}
	
	public ConcurrentHashMap<String, List<String>> returnMetric() {
		return this.metricsTimelines;
	}
	
	public HashMap<Integer, Double> getCpuUsageList() {
		return this.cpuUsageList;
	}
	
	public void setMemoryTotalValue(double a) {
		memoryTotalValue = a;
	}
	
	public void setMemoryUsagePercent(double a) {
		memoryUsagePercent = a;
	}
	
	public void setMemoryUsageValue(double a) {
		memoryUsageValue = a;
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

	public void setCpuUsagePercent(double d) {
		
		this.cpuUsagePercent = d;
	}
	
	public void setDiskTotalValue(double d) {
		this.diskTotalValue =d;
	}
	
	public void setDiskUsageValue(double d) {
		this.diskUsageValue =d;
	}
	
	public void setDiskUsagePercent(double d) {
		this.diskUsagePercent = d;
	}
	
	public void setMetric(ConcurrentHashMap<String, List<String>> m) {
		this.metricsTimelines = null;
		this.metricsTimelines = m;
	}
	
	public VM.Record getRecord() {
		return record;
	}

	public boolean setRecord(VM.Record record) {
		if(record!=null){
			if(this.record!=null&&this.record.timestamp>record.timestamp)
				return false;
			this.record = record;
			this.setName(record.nameLabel);
			this.setUuid(record.uuid);
			if(record.memoryDynamicMax!=null){
				this.setMemoryTotalValue(record.memoryDynamicMax/1024.0/1024.0);
			}
		}
		return true;
	}
	public Set<String> getMacAddress() {
		return macAddress;
	}
	public void setMacAddress(Set<String> macAddress) {
		this.macAddress = macAddress;
	}
	public String getRate() {
		return rate;
	}
	public void setRate(String rate) {
		this.rate = rate;
	}
	public String getBurst() {
		return burst;
	}
	public void setBurst(String burst) {
		this.burst = burst;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public List<String> getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(List<String> ipAddress) {
		this.ipAddress = ipAddress;
	}
}
