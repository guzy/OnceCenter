package oncecenter.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import oncecenter.Constants;
import oncecenter.action.AddLostZfsAction;
import oncecenter.util.dialog.ErrorMessageDialog;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectTemplate;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;

import com.once.xenapi.Connection;
import com.once.xenapi.Host;
import com.once.xenapi.Pool;
import com.once.xenapi.SR;
import com.once.xenapi.Types;
import com.once.xenapi.VM;
import com.once.xenapi.Types.SessionAuthenticationFailed;

public class AddServerUtil {
	private Pool thisPool=null;
	private Pool.Record poolRecod = null;
	
	private ArrayList<Host> hostList = new  ArrayList<Host>();
	private ArrayList<Host.Record> hostRecordList = new ArrayList<Host.Record>();
	private ArrayList<Double> memoryList = new ArrayList<Double>();
	
	private ArrayList<VM> vmList = new  ArrayList<VM>();
	private ArrayList<VM.Record> vmRecordList = new ArrayList<VM.Record>();
	
	private ArrayList<SR> srList = new  ArrayList<SR>();
	private ArrayList<SR.Record> srRecordList = new ArrayList<SR.Record>();
	
	private Map<Host,VMTreeObjectHost> hostMap = new HashMap<Host,VMTreeObjectHost>();
	private Map<VM,VMTreeObjectVM> vmMap = new HashMap<VM,VMTreeObjectVM>();
	private Map<SR,VMTreeObjectSR> srMap = new HashMap<SR,VMTreeObjectSR>();
	private Map<VM,VMTreeObjectTemplate> templateMap = new HashMap<VM,VMTreeObjectTemplate>();
	
	public static boolean connectionRefused = false;
	
	public String getConnectTreeNodeInfo(Connection connection) 
	{
		String message="";
//		for(Host h:Host.getAll(connection)){
//			try
//			{
//				Host.Record record = h.getRecord(connection);
//				double memoryTotal = h.getMetrics(connection).getRecord(connection).memoryTotal/1024.0/1024.0;
//				if(memoryTotal>Constants.maxMemoryTotal){
//					Constants.maxMemoryTotal = memoryTotal;
//				}
//				hostList.add(h);
//				hostRecordList.add(record);
//				memoryList.add(memoryTotal);
//			}
//			catch(Exception e)
//			{
//				e.printStackTrace();
//				System.out.println("dhf Host connection test;;");
//			}	
//		} 
//		for(VM vm:VM.getAll(connection)){
//			try{
//				VM.Record record = vm.getRecord(connection);
//				vmList.add(vm);
//				vmRecordList.add(record);
//			}
//			catch(Exception e)
//			{
//				e.printStackTrace();
//				System.out.println("dhf VM connection test;;" + e.getMessage());
//			}
//		}
//		for(SR sr:SR.getAll(connection)){
//			try{
//				sr.mount(connection);
//				SR.Record record = sr.getRecord(connection);
//				srList.add(sr);
//				srRecordList.add(record);
//			}
//			catch(Exception e)
//			{
//				e.printStackTrace();
//				System.out.println("dhf SR connection test;;");
//			}
//		} 	
		
		
//		System.out.println("getstatus"+System.currentTimeMillis());
//		Pool.Status status = Pool.getStatus(connection);
//		System.out.println("host"+System.currentTimeMillis());
//		for(Host host : Host.getAll(connection)){
//			Host.Record record = host.getRecord(connection);
//			double memoryTotal = host.getMetrics(connection).getRecord(connection).memoryTotal/1024.0/1024.0;
//			if(memoryTotal>Constants.maxMemoryTotal){
//				Constants.maxMemoryTotal = memoryTotal;
//			}
//			hostList.add(host);
//			hostRecordList.add(record);
//			memoryList.add(memoryTotal);
//		}
//		Map<Host, Host.Record> hostRecords = Host.getAllRecords(connection);
//		for(Host host : hostRecords.keySet()){
//			Host.Record record = hostRecords.get(host);
//			double memoryTotal = host.getMetrics(connection).getRecord(connection).memoryTotal/1024.0/1024.0;
//			if(memoryTotal>Constants.maxMemoryTotal){
//				Constants.maxMemoryTotal = memoryTotal;
//			}
//			hostList.add(host);
//			hostRecordList.add(record);
//			memoryList.add(memoryTotal);
//		}
//		System.out.println("vm"+System.currentTimeMillis());
//		for(VM.Record vmRecord : status.vmRecords){
//			VM vm = Types.toVM(vmRecord.uuid);
//			vmList.add(vm);
//			vmRecordList.add(vmRecord);
//		}
//		System.out.println("sr"+System.currentTimeMillis());
//		for(SR.Record srRecord : status.srRecords){
//			SR sr = Types.toSR(srRecord.uuid);
//			sr.mount(connection);
//			srList.add(sr);
//			srRecordList.add(srRecord);
//		}
		
		//Pool.Status status = null;
		
		long start;
		long end;
		
		try{
			thisPool = Constants.getPool(connection);
			if(thisPool!=null){
				poolRecod = thisPool.getRecord(connection);
			}
		}catch(Exception e){
			e.printStackTrace();
			message+=Constants.poolRecordError;
		}
		
		
		try{
			Map<Host, Host.Record> hostRecords = Host.getAllRecords(connection);
			for(Host host : hostRecords.keySet()){
				Host.Record record = hostRecords.get(host);
				double memoryTotal = record.memoryTotal/1024.0/1024;
				if(memoryTotal>Constants.maxMemoryTotal){
					Constants.maxMemoryTotal = memoryTotal;
				}
				hostList.add(host);
				hostRecordList.add(record);
				memoryList.add(memoryTotal);
			}
		}catch(Exception e){
			e.printStackTrace();
			message+=Constants.hostRecordError;
			hostList.clear();
			hostRecordList.clear();
			memoryList.clear();
			try{
				for(Host host:Host.getAll(connection)){
					try{
						Host.Record record = host.getRecord(connection);
						hostList.add(host);
						hostRecordList.add(record);
						double memoryTotal = record.memoryTotal/1024.0/1024.0;
						if(memoryTotal>Constants.maxMemoryTotal){
							Constants.maxMemoryTotal = memoryTotal;
						}
						memoryList.add(memoryTotal);
					} catch(Exception e1) {
						e1.printStackTrace();
					}
				}
			} catch(Exception e1) {
				e1.printStackTrace();
			}
		}
		try{
			Map<VM, VM.Record> vmRecords = VM.getAllRecords(connection);
			for(VM vm : vmRecords.keySet()){
				VM.Record vmRecord = vmRecords.get(vm);
				vmList.add(vm);
				vmRecordList.add(vmRecord);
			}
		}catch(Exception e){
			e.printStackTrace();
			message+=Constants.vmRecordError;
			System.out.println("vm get all records eroor!");
			vmList.clear();
			vmRecordList.clear();
//			status = Pool.getStatus(connection);
//			for(VM.Record vmRecord : status.vmRecords){
//				VM vm = Types.toVM(vmRecord.uuid);
//				vmList.add(vm);
//				vmRecordList.add(vmRecord);
//			}
			try{
				for(VM vm:VM.getAll(connection)){
					try{
						VM.Record record = vm.getRecord(connection);
						vmList.add(vm);
						vmRecordList.add(record);
					} catch(Exception e1) {
						System.out.println("获取虚拟机  " + vm.getNameLabel(connection) + "的record时出错");
						e1.printStackTrace();
					}
				}
			}catch(Exception e1){
				e1.printStackTrace();
			}
			
		}
		try{
			Map<SR, SR.Record> srRecords = SR.getAllRecords(connection);
			for(SR sr : srRecords.keySet()){
				SR.Record srRecord = srRecords.get(sr);
				srList.add(sr);
				srRecordList.add(srRecord);
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("sr get all records eroor!");
			srList.clear();
			srRecordList.clear();

			try{
				for(SR sr:SR.getAll(connection)){
					try{
						SR.Record record = sr.getRecord(connection);
						srList.add(sr);
						srRecordList.add(record);
					} catch(Exception e1) {
						e1.printStackTrace();
					}
				} 
			}catch(Exception e1){
				e1.printStackTrace();
			}
			message += Constants.srRecordError;
		}
			try{
				SR.mountAll(connection);
			}catch(Exception e){
				e.printStackTrace();
				String e_message = e.toString();
				if(e_message.contains(",")){
					String keyword = e_message.split(",")[0];
					String path = e_message.split(",")[1];
					if(keyword.equals(Constants.srShowMountException)){
						message+=Constants.srShowMountError;
						message+=path;
					}else if(keyword.equals(Constants.srMountException)){
						message+=Constants.srMountError;
						message+=path;
					}else{
						message+=Constants.srMountAllError;
					}
				}else{
					message+=Constants.srMountAllError;
				}
			}
		return message;
	}
	public VMTreeObjectRoot Connect(Connection connection,String ipAddress,String username,String password){
		if(thisPool!=null){
			VMTreeObjectPool newPool=new VMTreeObjectPool(poolRecod.nameLabel
					,connection,thisPool,poolRecod);
			newPool.setIpAddress(ipAddress);
			newPool.setUsername(username);
			newPool.setPassword(password);
			
			FileUtil.makeDir(new File(FileUtil.getData() + File.separator
					+ poolRecod.uuid));
//			newPool.setVmGroupConfigPath(FileUtil.getData() + File.separator
//					+ poolRecod.uuid+ File.separator
//					+Constants.VM_GROUP_CONFIG_FILE);
			
			final VMEvent event=new VMEvent();
			event.setDatetime(new Date());
			event.setDescription("连接到 '"+newPool.getName()+"'");
			event.setTarget(newPool);
			event.setTask("");
			event.setType(eventType.info);
			event.setUser(newPool.getUsername());
			event.setImage(ImageRegistry.getImage(ImageRegistry.INFO));
			
			newPool.events.add(event);
			
			//------------------------add Host-------------------
			for(int i=0;i<hostList.size();i++) {
				Host host = hostList.get(i);
				Host.Record record = hostRecordList.get(i);
				//if(record.enabled){
					VMTreeObjectHost server=new VMTreeObjectHost(record.nameLabel
							,connection,host,record);
					server.setIpAddress(record.address);
					server.setUsername(username);
					server.setPassword(password);
					server.setUuid(record.uuid);
					if(poolRecod.master.equals(host)){
						server.isMaster = true;
					}
					if(poolRecod.backup!=null&&poolRecod.backup.equals(host)){
						server.isBackup = true;
					}
					server.setPerformFilePath(FileUtil.getData() + File.separator
							+ poolRecod.uuid+ File.separator
							+ record.uuid);
					server.setMemoryTotalValue(memoryList.get(i));
					FileUtil.makeDir(new File(server.getPerformFilePath()));
					newPool.addChild(server);
					hostMap.put(host, server);
//				}else{
//					VMTreeObjectHost server=new VMTreeObjectHost("unknown",null,null,null);
//					//makeWorkSpace(Constant.workspace+"/"+newPool.getName()+"/"+server.getName());
//					newPool.addChild(server);
//					hostMap.put(host, server);
//				}
			}
			//----------------add sr------------------
			for(int i=0;i<srList.size();i++){
				SR sr = srList.get(i);
				SR.Record record = srRecordList.get(i);
				if(TypeUtil.getAllSRTypes().contains(record.type)){
					VMTreeObjectSR object = new VMTreeObjectSR(record.nameLabel
							,connection,sr,record);
					if(record.type.equals(TypeUtil.localSrType)){
						VMTreeObjectHost hostObject = hostMap.get(record.residentOn);
						hostObject.addChild(object);
					}else{
						newPool.addChild(object);
					}
					srMap.put(sr, object);
				}
			}
//			for(int i=0;i<templates.size();i++){
//				newPool.addChild(templates.get(i));
//			}	
			
			//---------------------add VM---------------------
			for(int i=0;i<vmList.size();i++) {
				VM vm = vmList.get(i);
				VM.Record record = vmRecordList.get(i);
				VMTreeObjectSR storageObject = null;
				if(record.suspendSR!=null){
					storageObject = srMap.get(record.suspendSR);
				}
//				VDI vdi = null;
//				try{
//					vdi = VDI.getByVM(connection, vm).iterator().next();
//					for(VMTreeObjectSR srObject:srMap.values()){
//						SR.Record srRecord = (SR.Record)srObject.getRecord();
//						if(srRecord.VDIs!=null&&srRecord.VDIs.contains(vdi)){
//							storageObject = srObject;
//							break;
//						}
//					}
//				}catch(Exception e){
//					e.printStackTrace();
//					storageObject = null;
//				}
				
				//判断是否是template
				if(record.isATemplate){
					//if(vm.getDomarch(connection)!=null&&!vm.getDomarch(connection).equals("")){
					VMTreeObjectTemplate object=new VMTreeObjectTemplate(record.nameLabel
							,connection,vm,record);
					newPool.addChild(object);
					templateMap.put(vm, object);
//					if(vdi!=null)
//						object.setVdi(vdi);
					if(storageObject!=null)
						object.setStorageObject(storageObject);
					//}
					continue;
				}
				//判断状态
				if(!record.powerState.equals(Types.VmPowerState.RUNNING)){
					VMTreeObjectVM object=new VMTreeObjectVM(record.nameLabel
							,connection,vm,record);
					newPool.addChild(object);
					vmMap.put(vm, object);
//					if(vdi!=null)
//						object.setVdi(vdi);
					if(storageObject!=null)
						object.setStorageObject(storageObject);
				 	continue;
				}
				
				//判断在哪台主机上
				Host host=record.residentOn;
				VMTreeObjectHost hostObject=hostMap.get(host);
				if(hostObject == null){
					continue;
				}
//				if(record.isControlDomain){
//					hostObject.controlVM = vm;
//					continue;
//				}else{
					VMTreeObjectVM object=new VMTreeObjectVM(record.nameLabel
							,connection,vm,record);
					object.setUuid(record.uuid);
					hostObject.addChild(object);
					vmMap.put(vm, object);
//					if(vdi!=null)
//						object.setVdi(vdi);
					if(storageObject!=null)
						object.setStorageObject(storageObject);
//				}
			}
//			for(int i=0;i<haltedVMs.size();i++){
//				newPool.addChild(haltedVMs.get(i));
//			}
			
			
			
			Constants.recordServer(ipAddress);
			Display display = PlatformUI.getWorkbench().getDisplay();
			if (!display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	Constants.logView.logFresh(event);
						}
			    };
			  display.asyncExec(runnable); 
			}
			newPool.hostMap = hostMap;
			newPool.vmMap = vmMap;
			newPool.srMap = srMap;
			newPool.templateMap = templateMap;
			newPool.temporaryList = new ArrayList<VMTreeObject>();
			
			IContributionItem [] menuItems = Constants.parentMenu.getItems();
//			IContributionItem [] p2vItems = ((IContributionManager) menuItems[5]).getItems();
//			((ActionContributionItem)p2vItems[0]).getAction().setEnabled(true);  
//			((ActionContributionItem)p2vItems[1]).getAction().setEnabled(true);  
			IContributionItem [] uploadItems = ((IContributionManager) menuItems[6]).getItems();
			((ActionContributionItem)uploadItems[0]).getAction().setEnabled(true);  
			return newPool;
		}else{
			Host thisHost = hostList.iterator().next();
			Host.Record hostRecord = hostRecordList.iterator().next();
			
			List<VMTreeObject> templates=new ArrayList<VMTreeObject>();
			
			VMTreeObjectHost newHost=new VMTreeObjectHost(hostRecord.nameLabel
					,connection,thisHost,hostRecord);
			newHost.setIpAddress(ipAddress);
			newHost.setUsername(username);
			newHost.setPassword(password);
			newHost.setUuid(hostRecord.uuid);
			newHost.setPerformFilePath(FileUtil.getData() + File.separator
					+ hostRecord.uuid);
//			newHost.setVmGroupConfigPath(FileUtil.getData() + File.separator
//					+ hostRecord.uuid+ File.separator
//					+Constants.VM_GROUP_CONFIG_FILE);
			newHost.setMemoryTotalValue(memoryList.get(0));
			FileUtil.makeDir(new File(newHost.getPerformFilePath()));
			hostMap.put(thisHost, newHost);
			
			final VMEvent event=new VMEvent();
			event.setDatetime(new Date());
			event.setDescription("连接到 '"+newHost.getName()+"'");
			event.setTarget(newHost);
			event.setTask("");
			event.setType(eventType.info);
			event.setUser(newHost.getUsername());
			event.setImage(ImageRegistry.getImage(ImageRegistry.INFO));
			newHost.events.add(event);
			
			//---------------------add VM---------------------
			for(int i = 0;i< vmList.size();i++) {
				VM vm = vmList.get(i);
				VM.Record record = vmRecordList.get(i);
				//判断是否是template
				if(record.isATemplate){
					//if(vm.getDomarch(connection)!=null&&!vm.getDomarch(connection).equals("")){
					VMTreeObjectTemplate object=new VMTreeObjectTemplate(record.nameLabel
							,connection,vm,record);
					templates.add(object);
					templateMap.put(vm, object);
					//}
					continue;
				}
				
//				if(record.isControlDomain){
//					newHost.controlVM = vm;
//					continue;
//				}else{
					VMTreeObjectVM object=new VMTreeObjectVM(record.nameLabel
							,connection,vm,record);
					object.setUuid(record.uuid);
					newHost.addChild(object);
					vmMap.put(vm, object);
//				}
			}
			
			//----------------add sr------------------
			for(int i=0;i<srList.size();i++){
				SR sr = srList.get(i);
				SR.Record record = srRecordList.get(i);
				if(TypeUtil.getAllSRTypes().contains(record.type)){
					VMTreeObjectSR object = new VMTreeObjectSR(record.nameLabel
							,connection,sr,record);
					newHost.addChild(object);
					srMap.put(sr, object);
				}
			}
			for(int i=0;i<templates.size();i++){
				newHost.addChild(templates.get(i));
			}						
			Constants.recordServer(ipAddress);
			Display display = PlatformUI.getWorkbench().getDisplay();
			if (!display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	Constants.logView.logFresh(event);
						}
			    };
			  display.asyncExec(runnable); 
			}
			
			
			newHost.hostMap = hostMap;
			newHost.vmMap = vmMap;
			newHost.srMap = srMap;
			newHost.templateMap = templateMap;	
			
			newHost.temporaryList = new ArrayList<VMTreeObject>();
			
			return newHost;
		}
	}
	
	public static VMTreeObjectRoot ConnectByIp(String ip,String username,String password){
		final Connection connection ;
		try {
			connection = new Connection(
					"http://"+ip+":9363"
					,username
					,password);
		} catch (SessionAuthenticationFailed e1) {
			e1.printStackTrace();
			Display display=PlatformUI.getWorkbench().getDisplay();
			if (!display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run( ){
			        	MessageDialog.openError(new Shell(), "登录失败","请检查用户名密码");
			        }
				};
			    display.syncExec(runnable); 
			}
			return null;
		}catch (final Exception e) {
			e.printStackTrace();
			Display display=PlatformUI.getWorkbench().getDisplay();
			if (!display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run( ){
			        	if(e.toString().equals("HOST_IS_SLAVE"))
			        	{
			        		Image image = ImageRegistry.getImage(ImageRegistry.FAILURE);
			        		ErrorMessageDialog dialog = new ErrorMessageDialog(new Shell(),"连接主机为资源池从节点      \n请换资源池主节点重新连接",image);
			        		dialog.open();
			        	}
			        	else
			        		MessageDialog.openError(new Shell(), "登录失败","登录失败");
			        }
				};
			    display.syncExec(runnable); 
			}
			return null;
		}
		
		final AddServerUtil action = new AddServerUtil();
		final String message = action.getConnectTreeNodeInfo(connection);
		if(message.length()>0){
			Display display=PlatformUI.getWorkbench().getDisplay();
			if (!display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run( ){
			        	MessageDialog.openError(new Shell(), "登录出现问题",message);
			        }
				};
			    display.syncExec(runnable); 
			}
		}
//		if(message.contains(Constants.srMountAllError)
//				||message.contains(Constants.srShowMountError)
//				||message.contains(Constants.srMountError)){
			Display display=PlatformUI.getWorkbench().getDisplay();
			if (!display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run( ){
			        	AddLostZfsAction a = new AddLostZfsAction(connection,action.hostRecordList);
						a.run();
			        }
				};
			    display.syncExec(runnable); 
			}
			
//		}
		return action.Connect(connection, ip, username, password);
	
	}
	
	public static Connection getConnection(String ipAddress,String username,String password)
	{
		Connection connection;
		try {
			long start =  System.currentTimeMillis();
			connection = new Connection(
					"http://"+ipAddress+":9363"
					,username
					,password);
			return connection;
		} catch (SessionAuthenticationFailed e1) {
			
			e1.printStackTrace();
			Display display=PlatformUI.getWorkbench().getDisplay();
			if (!display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run( ){
			        	MessageDialog.openError(new Shell(), "信息错误","用户名和密码不匹配");
			        }
				};
			    display.syncExec(runnable); 
			}
			return null;
		}catch (final Exception e) {
			
			e.printStackTrace();
			Display display=PlatformUI.getWorkbench().getDisplay();
			if (!display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run( ){
			        	if(e.toString().equals("HOST_IS_SLAVE"))
			        	{
			        		Image image = ImageRegistry.getImage(ImageRegistry.FAILURE);
			        		ErrorMessageDialog dialog = new ErrorMessageDialog(new Shell(),"输入节点为从节点      \n请换资源池主节点执行在线更新操作",image);
			        		dialog.open();
			        	}
			        	else
			        		connectionRefused = true;
			        }
				};
			    display.syncExec(runnable); 
			}
			connectionRefused = true;
			return null;
		}
	}
	
}
