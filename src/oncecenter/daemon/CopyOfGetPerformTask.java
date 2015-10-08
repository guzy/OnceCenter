package oncecenter.daemon;

import java.util.TimerTask;

import oncecenter.Constants;
import oncecenter.util.Ssh;
import oncecenter.util.performance.analyse.Metric2Performance;
import oncecenter.util.performance.analyse.ReadFromDB;
import oncecenter.util.performance.analyse.XML2Metrics;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import com.once.xenapi.Types;
import com.once.xenapi.VM;

public class CopyOfGetPerformTask extends TimerTask {
	
	private VMTreeObjectRoot root;
//	private VMTreeObjectPool rootPool;
//	private VMTreeObjectHost rootHost;
//	private Connection conn;
//	private boolean isPool;
	
	public CopyOfGetPerformTask(VMTreeObjectRoot root){
		this.root=root;
//		this.conn=root.getConnection();
//		if(root instanceof VMTreeObjectPool){
//			this.isPool=true;
//			rootPool = (VMTreeObjectPool) root;
//		}else{
//			this.isPool=false;
//			rootHost = (VMTreeObjectHost) root;
//		}
		
	}
	
	public void run(){
		if(!root.getItemState().equals(ItemState.able)){
			return;
		}
		for(VMTreeObjectHost host : root.hostMap.values()){
			try{
				Ssh ssh = new Ssh(host.getIpAddress(),host.getUsername(),host.getPassword());
				ssh.Connect();
				ssh.GetFile(Constants.performpath, host.getPerformFilePath());
				ssh.CloseSsh();
			}catch(Exception e){
				e.printStackTrace();
				break;
			}
//			XML2Metrics.getMetricsTimelines(host);
			ReadFromDB.getMetricsTimelines(host);
			Metric2Performance.analyHostPerformance(host);
			host.getGrade();
			if(host.columns != 0) {
				host.step =  (host.endTime-host.startTime)/host.columns/1000;
			} else {
				host.step = 0;
			}
		}
		for(VMTreeObjectVM vm : root.vmMap.values()){
			if (vm == null){
				System.out.println("在GetPerformTask中，当前虚拟机为空");
			} else if(Constants.displayStatusData){
				VM.Record record = (VM.Record)vm.getRecord();
				VMTreeObjectHost host= root.hostMap.get(record.residentOn);
				//vm.getMetricsTimelines(host.getPerformFilePath());
				vm.setMetric(host.getMetrics());
				vm.startTime = host.startTime;
				vm.endTime = host.endTime;
				vm.columns = host.columns;
				vm.step = host.step;
				//vm.analyVMPerformance();
				Metric2Performance.analyVMPerformance(vm);
				if(record.powerState.equals(Types.VmPowerState.RUNNING)){
					vm.alarm();
				}
			}
		}
	}
	
//	public void ssss(){
//		for(VMTreeObject object:Constant.Xen.getChildrenList()){
//			if(!object.getItemState().equals(ItemState.able))
//				continue;
//			if(object.getType().equals(Type.host)){
//					try { 	
//						try{
//							Ssh ssh = new Ssh(object.getIpAddress(),object.getUsername(),object.getPassword());
//							if(ssh.Connect()){
//								ssh.GetFile(Constant.performpath, Constant.workspace+"/"+object.getName());
//							}
//							ssh.CloseSsh();
//						}catch(Exception e){
//							e.printStackTrace();
//							continue;
//						}
//						
//						
//			            if(object.hostPerform==null){
//							object.hostPerform=new FetchHostPerformance(Constant.workspace+"/"+object.getName()+"/15sec.xml"
//									,object.uuid);
//						}
//						object.hostPerform.getMetricsTimelines();
//						VMTreeParent parent = (VMTreeParent)object;
//						for(VMTreeObject o:parent.getChildren()){
//							if(o.getType().equals(Type.vm)){
//								VM vm = (VM)o.getApiObject();
//								if(o.uuid==null||o.uuid.length()==0){
//									o.uuid=vm.getUuid(o.getConnection());
//								}
//								if(o.vmPerform==null){
//									o.vmPerform=new FetchVmPerformance(Constant.workspace+"/"+object.getName()+"/15sec.xml"
//											,o.uuid);
//									
//								}
//								o.vmPerform.getMetricsTimelines();
//								
//							}
//						}
//			              
//			        } catch (Exception e) {  
//			              
//			            e.printStackTrace();  
//			        }
//			}else if(object.getType().equals(Type.pool)){
//				VMTreeParent parent = (VMTreeParent)object;
//				for(VMTreeObject o:parent.getChildrenList()){
//					if(o.getType().equals(Type.host)){
//						try { 
//							
//							try{
//								Ssh ssh = new Ssh(o.getIpAddress(),object.getUsername(),object.getPassword());
//								ssh.Connect();
//								ssh.GetFile(Constant.performpath, Constant.workspace+"/"+object.getName()+"/"+o.getName());
//								ssh.CloseSsh();
//							}catch(Exception e){
//								e.printStackTrace();
//								continue;
//							}
//							
//				            
//				            if(o.hostPerform==null){
//								o.hostPerform=new FetchHostPerformance(Constant.workspace+"/"+object.getName()+"/"+o.getName()+"/15sec.xml"
//										,o.uuid);
//							}
//							o.hostPerform.getMetricsTimelines();
//							VMTreeParent p = (VMTreeParent)o;
//							for(VMTreeObject o1:p.getChildren()){
//								if(o1.getType().equals(Type.vm)){
//									VM vm = (VM)o1.getApiObject();
//									if(o1.uuid==null||o1.uuid.length()==0){
//										o1.uuid=vm.getUuid(o1.getConnection());
//									}
//									if(o1.vmPerform==null){
//										o1.vmPerform=new FetchVmPerformance(Constant.workspace+"/"+object.getName()+"/"+o.getName()+"/15sec.xml"
//												,o1.uuid);
//									}
//									o1.vmPerform.getMetricsTimelines();
//									
//								}
//							}
//				              
//				        } catch (Exception e) {  
//				              
//				            e.printStackTrace();  
//				        }
//					}
//					
//				}
//			}
//		}			
//		try {
//			sleep(10000);
//		} catch (InterruptedException e) {
//			
//			e.printStackTrace();
//		}
//
//	}
}
