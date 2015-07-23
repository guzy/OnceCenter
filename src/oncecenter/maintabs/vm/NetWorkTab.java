package oncecenter.maintabs.vm;

import java.util.ArrayList;

import oncecenter.maintabs.OnceVMTabItem;
import oncecenter.maintabs.vm.dialog.BindNetworkDialog;
import oncecenter.maintabs.vm.dialog.EditNetworkDialog;
import oncecenter.util.VMUtil;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Types;
import com.once.xenapi.VIF;
import com.once.xenapi.VM;

public class NetWorkTab extends OnceVMTabItem {
	Table netTable;
	TableViewer tableViewer;
	VM vm;
	//VM.Record vmRecord;
	
	Button refreshButton;
	Button addButton;
	Button editButton;
	Button deleteButton;
	Button bindButton;
	
	private ArrayList<NetworkInClient> netList = new ArrayList<NetworkInClient>();
	//private ArrayList<TableEditor> editorList = new ArrayList<TableEditor>();
	
	public NetWorkTab(CTabFolder arg0, int arg1, VMTreeObjectVM object) {
		super(arg0, arg1, object);
		setText("网络");
		//this.objectVM = object;
		this.vm = (VM)object.getApiObject();
	}

	public NetWorkTab(CTabFolder arg0, int arg1, int arg2, VMTreeObjectVM object) {
		super(arg0, arg1, arg2, object);
		setText("网络");
		//this.objectVM = object;
		this.vm = (VM)object.getApiObject();
	}
	
	public boolean Init(){
//		vmRecord = objectVM.getRecord();
//		if(vmRecord==null){
//			try {
//				vmRecord = vm.getRecord(objectVM.getConnection());
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		
		composite = new Composite(folder, SWT.FILL); 
		setControl(composite);
		composite.setBackground(new Color(null,255,255,255));
		composite.setBackgroundMode(SWT.INHERIT_DEFAULT);  
		composite.setLayout(new GridLayout(1,false));
		
		netTable=new Table(composite, SWT.BORDER | SWT.V_SCROLL
		        | SWT.H_SCROLL|SWT.FULL_SELECTION);
		netTable.setHeaderVisible(true);
		netTable.setLinesVisible(false);
		GridData griddata = new GridData(GridData.FILL_BOTH);
		netTable.setLayoutData(griddata);
		netTable.addListener(SWT.MouseDown, new Listener() {  
			public void handleEvent(Event event) { 
				if(netTable.getSelectionCount()>=1
						&&netTable.getItemCount()>1
						&&objectVM.getRecord()!=null
						&&objectVM.getRecord().powerState.equals(Types.VmPowerState.HALTED)) {
					deleteButton.setEnabled(true);
				}
				if(netTable.getSelectionCount()==1
						&&objectVM.getRecord()!=null
						&&objectVM.getRecord().powerState.equals(Types.VmPowerState.RUNNING)) {
					editButton.setEnabled(true);
				}
				if(objectVM.getRecord()!=null
						&&objectVM.getRecord().powerState.equals(Types.VmPowerState.HALTED)) {
					bindButton.setEnabled(true);
				}
			}
		});  

		{
			TableColumn  net= new TableColumn(netTable, SWT.CENTER|SWT.BOLD);
			net.setText("网络");
			net.setWidth(100);
			TableColumn mac = new TableColumn(netTable, SWT.CENTER|SWT.BOLD);
			mac.setText("mac地址");
			mac.setWidth(400);
			TableColumn vlan = new TableColumn(netTable, SWT.CENTER|SWT.BOLD);
			vlan.setText("V LAN");
			vlan.setWidth(100);
			TableColumn phy = new TableColumn(netTable, SWT.CENTER|SWT.BOLD);
			phy.setText("物理网卡");
			phy.setWidth(100);
//			TableColumn ip = new TableColumn(netTable,SWT.CENTER|SWT.BOLD);
//			ip.setText("ip地址");
//			ip.setWidth(200);
//			TableColumn subNetMask = new TableColumn(netTable,SWT.CENTER|SWT.BOLD);
//			subNetMask.setText("子网掩码");
//			subNetMask.setWidth(200);
//
//			TableColumn gateway = new TableColumn(netTable,SWT.CENTER|SWT.BOLD);
//			gateway.setText("网关");
//			gateway.setWidth(200);
			
//			TableColumn rate = new TableColumn(netTable, SWT.CENTER|SWT.BOLD);
//			rate.setText("网络限速（kbs）");
//			rate.setWidth(150);
//			TableColumn burst = new TableColumn(netTable, SWT.CENTER|SWT.BOLD);
//			burst.setText("最大浮动（kbs）");
//			burst.setWidth(150);
//			TableColumn state = new TableColumn(netTable, SWT.CENTER|SWT.BOLD);
//			state.setText("状态");
//			state.setWidth(100);
		}
		
		
		tableViewer = new TableViewer(netTable);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider());
		
		//Network net = new Network("network 0",objectVM.getIpAddress(), objectVM.getSubNetMask(),objectVM.getMacAddress(),objectVM.getGateway(),objectVM.getRate(),objectVM.getBurst());
		//netList.add(net);
		tableViewer.setInput(netList);
//		
//		Timer refreshPerformTimer = new Timer("RefreshPerformTimer");
//		refreshPerformTimer.schedule(new RefreshPerformTimer(this,PlatformUI.getWorkbench().getDisplay()), 15000, 15000);
//		objectVM.timerList.add(refreshPerformTimer);
		
		
		Composite setting = new Composite(composite,SWT.NONE);
		GridData griddata1 = new GridData(GridData.FILL_HORIZONTAL);
		griddata1.minimumHeight = 100;
		griddata1.heightHint = 100;
		setting.setLayoutData(griddata1);
		setting.setLayout(new GridLayout(5,false));
		{
			refreshButton = new Button(setting, SWT.NONE);
			refreshButton.setText("刷新");
			refreshButton.setEnabled(false);
			refreshButton.addSelectionListener(new SelectionAdapter() {    
	            public void widgetSelected(SelectionEvent e) {    
	            	refreshButton.setEnabled(false);
	            	RefreshVifJob job = new RefreshVifJob(PlatformUI.getWorkbench().getDisplay());
	        		job.schedule();
	             }    
	         }); 
			addButton = new Button(setting, SWT.NONE);
			addButton.setText("添加网络");
			addButton.setEnabled(true);
//			保证虚拟机开机或者关机状态下都能添加网卡，所以将下面代码注释
//			if(objectVM.getRecord()!=null
//					&&objectVM.getRecord().powerState.equals(Types.VmPowerState.HALTED)){
//				addButton.setEnabled(true);
//			}else{
//				addButton.setEnabled(false);
//			}
			addButton.addSelectionListener(new SelectionAdapter() {    
	            public void widgetSelected(SelectionEvent e) {    
	            	addButton.setEnabled(false);
	            	MessageBox messageBox = new MessageBox(new Shell(), SWT.YES|SWT.NO); 
	    			messageBox.setText("提示");
	    			messageBox.setMessage("是否要为虚拟机增加一块虚拟网卡？");
	    			int choice = messageBox.open();
	    			if(choice ==  SWT.YES){
	    				AddVifJob job = new AddVifJob(PlatformUI.getWorkbench().getDisplay());
		        		job.schedule();
	    			}else{
	    				addButton.setEnabled(true);
	    			}
	             }    
	         }); 
			
			editButton = new Button(setting, SWT.NONE); 
			editButton.setText("修改属性");
			editButton.setEnabled(false);
			editButton.addSelectionListener(new SelectionAdapter() {    
	            public void widgetSelected(SelectionEvent e) {  
	            	editButton.setEnabled(false);
	            	int index = netTable.getSelectionIndex();
	            	NetworkInClient net = netList.get(index);
	            	EditNetworkDialog dialog = new EditNetworkDialog(null,net);
	            	dialog.create();
	            	if(0==dialog.open()){
	            		EditJob job = new EditJob(PlatformUI.getWorkbench().getDisplay(),net);
		        		job.schedule();
	            	}else{
	            		editButton.setEnabled(true);
	            	}
	             }    
	         }); 
			deleteButton = new Button(setting, SWT.NONE);
			deleteButton.setText("删除网络");
			deleteButton.setEnabled(false);
			deleteButton.addSelectionListener(new SelectionAdapter() {    
	            public void widgetSelected(SelectionEvent e) {    
	            	deleteButton.setEnabled(false);
	            	int index = netTable.getSelectionIndex();
	            	NetworkInClient net = netList.get(index);
	            	MessageBox messageBox = new MessageBox(new Shell(), SWT.YES|SWT.NO); 
	    			messageBox.setText("提示");
	    			messageBox.setMessage("是否要删除网卡"+net.getMac()+"?");
	    			int choice = messageBox.open();
	    			if (choice ==  SWT.YES) {
	    				DeleteVifJob job = new DeleteVifJob(PlatformUI.getWorkbench().getDisplay(),net);
		        		job.schedule();
	    			} else{
	    				deleteButton.setEnabled(true);
	    			}
	             }    
	         }); 
			bindButton = new Button(setting, SWT.NONE);
			bindButton.setText("绑定网卡");
			bindButton.setEnabled(false);
			bindButton.addSelectionListener(new SelectionAdapter() {    
				public void widgetSelected(SelectionEvent e) {  
					bindButton.setEnabled(false);
	            	int index = netTable.getSelectionIndex();
	            	NetworkInClient net = netList.get(index);
	            	if (net == null) {
	            		MessageBox messageBox = new MessageBox(new Shell(), SWT.CHECK); 
		    			messageBox.setText("提示");
		    			messageBox.setMessage("您没有选中任何网卡，操作取消。");
	            	}
	            	BindNetworkDialog dialog = new BindNetworkDialog(null,net,objectVM);
	            	dialog.create();
	            	if(0==dialog.open()){
	            		BindJob job = new BindJob(PlatformUI.getWorkbench().getDisplay(),net);
		        		job.schedule();
	            	}else{
	            		bindButton.setEnabled(true);
	            	}
	             }      
	        }); 
//			Button activate = new Button(setting, SWT.NONE);
//			activate.setText("激活网络");
//			activate.setEnabled(false);
//			Label title = new Label(setting,SWT.NONE);
//			title.setFont(SWTResourceManager.getFont("微软雅黑", 9, SWT.BOLD));
//			title.setText("网络");
//			new Label(setting,SWT.NONE);
//			new Label(setting,SWT.NONE);
//			
//			network = new Label(setting,SWT.NONE);
//			network.setText("Network_Rate:   ");
//			network_rate = new Text(setting,SWT.BORDER);
//			network_rate.setText("");
//			network_rate.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//			kbs1 = new Label(setting,SWT.NONE);
//			kbs1.setText("  kbs  ");
//			
//			burst = new Label(setting,SWT.NONE);
//			burst.setText("Burst_Rate:  ");
//			burst_rate = new Text(setting,SWT.BORDER);
//			burst_rate.setText("");
//			burst_rate.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//			kbs2 = new Label(setting,SWT.NONE);
//			kbs2.setText("  kbs  ");
//			
//			net_ok = new Button(setting,SWT.NONE);
//			net_ok.setText("确认");
//			net_ok.addMouseListener(new MouseAdapter() {
////				@Override
////				public void mouseDown(MouseEvent arg0) {
////					VM vm=(VM)object.getApiObject();
////					NetworkControl qos = NetworkControl.getNetworkControl(object.getConnection());
////					Map<String, String> params = new HashMap<String, String>();
////					// the max throughput is 10Mbps 
////					params.put(Constants.NETWORK_RATE, network_rate.getText());
////					// this parameter is optional, if it is setting, the max throughput will be (NETWORK_RATE + BURST_RATE)
////					params.put(Constants.BURST_RATE, burst_rate.getText());
////					try {
////						qos.bindQoS(object.getConnection(), vm, params);
////						
////					} catch (Exception e) {
////						
////						e.printStackTrace();
////					}
////				}
//			});
		}
		
		RefreshVifJob job = new RefreshVifJob(PlatformUI.getWorkbench().getDisplay());
		job.schedule();
		
		composite.layout();
		return true;
	}
	
	public void refreshButton(){
		if(objectVM.getRecord()!=null){
			addButton.setEnabled(true);
//			if(objectVM.getRecord().powerState.equals(Types.VmPowerState.HALTED)){
//				addButton.setEnabled(true);
//			}else{
//				addButton.setEnabled(false);
//			}
		}
	}
	
	public void refresh(){
		if(composite!=null){
			RefreshVifJob job = new RefreshVifJob(PlatformUI.getWorkbench().getDisplay());
			job.schedule();
		}
		
	}
	
	class RefreshVifJob extends Job
	{
		private Display display;
		public RefreshVifJob(Display display) {
			super("刷新");
			this.display = display;
		}
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("刷新", 100); 
			netList.clear();
			try {
				for(VIF vif:vm.getVIFs(objectVM.getConnection())){
					VIF.Record record = vm.getVIFRecord(objectVM.getConnection(), vif);
					String tag = vm.getTag(objectVM.getConnection(), vif);
					String phy = vm.getNetworkRecord(objectVM.getConnection(), vif).nameLabel;
//					String phy = record.network.getNameLabel(objectVM.getConnection());
					NetworkInClient net = new NetworkInClient(vif,record.device,null,null
							,record.MAC,null,objectVM.getRate(),objectVM.getBurst()
							,tag,phy);
					netList.add(net);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	tableViewer.setInput(netList); 
			        	refreshButton.setEnabled(true);
			        	refreshButton();
			        }
			    };
			    this.display.syncExec(runnable); 
			}
			monitor.done();
			return Status.OK_STATUS;
		}
	}
	
	class AddVifJob extends Job
	{
		private Display display;
		public AddVifJob(Display display) {
			super("添加");
			this.display = display;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("添加", 100); 
			boolean isSuccess = VMUtil.createVIF(vm,objectVM.getConnection(),1500l
					,objectVM.getRecord().residentOn,"");
			if(isSuccess){
				if (!this.display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
				        	addButton.setEnabled(true);
				        	refreshButton.setEnabled(false);
				        }
				    };
				    this.display.syncExec(runnable); 
				}
            	RefreshVifJob job = new RefreshVifJob(PlatformUI.getWorkbench().getDisplay());
        		job.schedule();
			}else{
				if (!this.display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
				        	MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
			    			messageBox.setText("提示");
			    			messageBox.setMessage("添加失败！");
			    			messageBox.open();
				        }
				    };
				    this.display.syncExec(runnable); 
				}
			}
			
			monitor.done();
			return Status.OK_STATUS;
		}
	}
	
	class EditJob extends Job
	{
		private Display display;
		private NetworkInClient net;
		public EditJob(Display display,NetworkInClient net) {
			super("编辑");
			this.display = display;
			this.net = net;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("编辑", 100); 
			objectVM.setRate(net.getRate());
			objectVM.setBurst(net.getBurst());
			try {
				vm.setTag(objectVM.getConnection(), net.getVif(), net.vlan);
			} catch (Exception e) {
				e.printStackTrace();
				if (!this.display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
				        	MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
			    			messageBox.setText("提示");
			    			messageBox.setMessage("修改失败！");
			    			messageBox.open();
				        }
				    };
				    this.display.syncExec(runnable); 
				}
			}
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	tableViewer.setInput(netList); 
			        	editButton.setEnabled(true);
			        }
			    };
			    this.display.syncExec(runnable); 
			}
			monitor.done();
			return Status.OK_STATUS;
		}
	}
	
	class DeleteVifJob extends Job
	{
		private Display display;
		private NetworkInClient network;
		public DeleteVifJob(Display display,NetworkInClient network) {
			super("删除");
			this.display = display;
			this.network = network;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("删除", 100); 
			try{
				vm.destroyVIF(objectVM.getConnection(), network.getVif());
			}catch(Exception e){
				e.printStackTrace();
				if (!this.display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
				        	MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
			    			messageBox.setText("提示");
			    			messageBox.setMessage("删除失败！");
			    			messageBox.open();
				        }
				    };
				    this.display.syncExec(runnable); 
				}
			}
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	refreshButton.setEnabled(false);
			        }
			    };
			    this.display.syncExec(runnable); 
			}
        	RefreshVifJob job = new RefreshVifJob(PlatformUI.getWorkbench().getDisplay());
    		job.schedule();
			
			monitor.done();
			return Status.OK_STATUS;
		}
	}
	
	class BindJob extends Job
	{
		private Display display;
//		private NetworkInClient net;
		public BindJob(Display display,NetworkInClient net) {
			super("绑定");
			this.display = display;
//			this.net = net;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("绑定", 100); 
//			objectVM.setRate(net.getRate());
//			objectVM.setBurst(net.getBurst());
			try {
//				vm.setTag(objectVM.getConnection(), net.getVif(), net.vlan);
			} catch (Exception e) {
				e.printStackTrace();
				if (!this.display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
				        	MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
			    			messageBox.setText("提示");
			    			messageBox.setMessage("绑定失败！");
			    			messageBox.open();
				        }
				    };
				    this.display.syncExec(runnable); 
				}
			}
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	tableViewer.setInput(netList); 
			        	bindButton.setEnabled(true);
			        }
			    };
			    this.display.syncExec(runnable); 
			}
			monitor.done();
			return Status.OK_STATUS;
		}
	}
	
	public class NetworkInClient{
		private VIF vif;
		private String name;
		private String ip;
		private String subNetMask;
		private String mac;
		private String gateway;
		private String rate;
		private String burst;
		private String vlan;
		private String phy;
		public NetworkInClient(VIF vif, String name, String ip,
				String subNetMask, String mac, String gateway, String rate,
				String burst, String vlan, String phy) {
			super();
			this.vif = vif;
			this.name = name;
			this.ip = ip;
			this.subNetMask = subNetMask;
			this.mac = mac;
			this.gateway = gateway;
			this.rate = rate;
			this.burst = burst;
			this.vlan = vlan;
			this.phy = phy;
		}
		public String getGateway() {
			return gateway;
		}
		public void setGateway(String gateway) {
			this.gateway = gateway;
		}
		public String getIp() {
			return ip;
		}
		public void setIp(String ip) {
			this.ip = ip;
		}
		public String getSubNetMask() {
			return subNetMask;
		}
		public void setSubNetMask(String subNet) {
			this.subNetMask = subNet;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getMac() {
			return mac;
		}
		public void setMac(String mac) {
			this.mac = mac;
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
		public VIF getVif() {
			return vif;
		}
		public void setVif(VIF vif) {
			this.vif = vif;
		}
		public String getVlan() {
			return vlan;
		}
		public void setVlan(String vlan) {
			this.vlan = vlan;
		}
		public String getPhy() {
			return phy;
		}
		public void setPhy(String phy) {
			this.phy = phy;
		}
	}
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

		 @Override
		 public String getColumnText(Object element, int columnIndex) {
		  if(element instanceof NetworkInClient) {
			  NetworkInClient net = (NetworkInClient)element;
		   switch(columnIndex) {
		   case 0:
			   return net.getName();
		   case 1:
			   return net.getMac();
		   case 2:
			   return net.getVlan();
		   case 3:
			   return net.getPhy();
//		   case 4:
//			   return net.getBurst();
//		   case 3:
//			   return net.getSubNetMask();
//		   case 4:
//		   {
//			   TableEditor editor   =   new   TableEditor(netTable);
//		        final Text rate  =   new Text(netTable,SWT.NONE); 
//		        rate.setText(((Network) element).getRate());
//		        rate.addTraverseListener(new TraverseListener() {  
//				      public void keyTraversed(TraverseEvent e) {  
//				        if (e.keyCode == 13) {  
//				        	objectVM.setRate(rate.getText());
//				        	netTable.forceFocus();
//				        }  
//				      }  
//				});  
//		        rate.pack();
//		        rate.setSize(100, netTable.getItemHeight());
//				editor.horizontalAlignment = SWT.CENTER;
//				editor.minimumWidth = rate.getSize().x;
//		        editor.minimumHeight = netTable.getItemHeight();
//		        editor.setEditor(rate,netTable.getItem(netList.indexOf(net)),4); 
//		        //diskTimesMap.put(disk, editor);
//		        editorList.add(editor);
//		        return "";
//		   }
			   
//		   case 5:
//		   {
//			   TableEditor editor   =   new   TableEditor(netTable);
//		        final Text burst  =   new Text(netTable,SWT.NONE); 
//		        burst.setText(((Network) element).getBurst());
//		        burst.addTraverseListener(new TraverseListener() {  
//				      public void keyTraversed(TraverseEvent e) {  
//				        if (e.keyCode == 13) {  
//				        	objectVM.setRate(burst.getText());
//				        	netTable.forceFocus();
//				        }  
//				      }  
//				});  
//		        burst.pack();
//		        burst.setSize(100, netTable.getItemHeight());
//				editor.horizontalAlignment = SWT.CENTER;
//				editor.minimumWidth = burst.getSize().x;
//		        editor.minimumHeight = netTable.getItemHeight();
//		        editor.setEditor(burst,netTable.getItem(netList.indexOf((Network) element)),5); 
//		        //diskTimesMap.put(disk, editor);
//		        editorList.add(editor);
//		        return "";
//		   }
		   
//		   case 6:
//			   return "已激活";
		   }
		  }
		  return null;
		 }

		@Override
		public Image getColumnImage(Object arg0, int arg1) {
			return null;
		}
	}
	
}
