package oncecenter.action.vm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import oncecenter.Constants;
import oncecenter.action.OnceAction;
import oncecenter.util.ImageRegistry;
import oncecenter.views.grouptreeview.elements.VMTreeObjectVMinGroup;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.Host;
import com.once.xenapi.Types;
import com.once.xenapi.VM;

public class StartAction extends OnceAction {
	
	VMTreeObjectVM selection;
	VMTreeObject hostObject;
	List<VMTreeObjectVM> selectionList;
	
	public StartAction(String text,String image,String disabledImage){
		super(text,image,disabledImage);
		
	}
	
	public StartAction(VMTreeObjectVM selection){
		super();
		this.selection=selection;
		setText("启动");		
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.STARTUP));
	}
	
	public StartAction(List<VMTreeObjectVM> selectionList){
		super();
		this.selectionList=selectionList;
		setText("启动");		
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.STARTUP));
	}

	
	class VMJob extends Job{
		VMTreeView viewer;
		Display display;
		VM.Record record;
		List<VMTreeObjectVM> selectionList;
		Connection connection;
		VM vm;
		boolean notEnough = false;
		String crush_vm = null;
		public VMJob(Display display, VMTreeView viewer,List<VMTreeObjectVM> selectionList){
			super("启动虚拟机");
			this.viewer=viewer;
			this.display=display;
			this.selectionList = selectionList;
			this.connection = selectionList.get(0).getConnection();
			//this.vm = (VM)selection.getApiObject();
		}
		@Override 
	    protected IStatus run(IProgressMonitor monitor) { 
	        monitor.beginTask("启动中 ...", 100); 
	        //判断当前虚拟机状态
	        for(VMTreeObjectVM selection:selectionList){
	        	selection.setItemState(ItemState.changing);
	        }
	        if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	viewer.getViewer().refresh();
			        	if(Constants.groupView!=null)
			        		Constants.groupView.getViewer().refresh();
			        }
			    };
			    this.display.asyncExec(runnable); 
			}
	        int step = 100/selectionList.size();
	        int i = 0;
			for(final VMTreeObjectVM selection:selectionList){
				monitor.worked(step*i++);
				if(notEnough){
					selection.setItemState(ItemState.able);
					if (!this.display.isDisposed()){
					    Runnable runnable = new Runnable(){
					        public void run(){
					        	viewer.getViewer().refresh();
					        	if(Constants.groupView!=null)
					        		Constants.groupView.getViewer().refresh();
					        }
					    };
					    this.display.asyncExec(runnable); 
					}
					continue;
				}
				try {
					vm = (VM)selection.getApiObject();
					Types.VmPowerState state=vm.getPowerState(connection);
					//当前虚拟机已经开启
					if(state.equals(Types.VmPowerState.RUNNING)){
						System.out.println("该虚拟机已经开启");
						selection.setRecord(vm.getRecord(connection));
						selection.setItemState(ItemState.able);
						if (!this.display.isDisposed()){
						    Runnable runnable = new Runnable(){
						        public void run(){
						        	viewer.getViewer().refresh();
						        	if(Constants.groupView!=null)
						        		Constants.groupView.getViewer().refresh();
						        }
						    };
						    this.display.asyncExec(runnable); 
						}
						continue;
					}
				} catch (Exception e1) {
					
					e1.printStackTrace();
					selection.setItemState(ItemState.able);
					if (!this.display.isDisposed()){
					    Runnable runnable = new Runnable(){
					        public void run(){
					        	viewer.getViewer().refresh();
					        	if(Constants.groupView!=null)
					        		Constants.groupView.getViewer().refresh();
					        }
					    };
					    this.display.asyncExec(runnable); 
					}
					continue;
				}
		        
		        //找到当前虚拟机应该放置的父节点
		        VM.Record vmRecord = selection.getRecord();
		        if(selection.getParent() instanceof VMTreeObjectPool){
	        		if(vmRecord.isLocalVM){
	    	        	VMTreeObjectPool pool = (VMTreeObjectPool)selection.getParent();
	    	        	hostObject = pool.hostMap.get(vmRecord.residentOn);
	    	        }else{
	    	        	hostObject = Constants.getSuitableHost((VMTreeObjectPool)selection.getParent());
	    	        }
		        }else{
		        	hostObject = selection.getParent();
		        }
		        try {
		        	//开启虚拟机
		        	vm.setBootOrder(connection, "cd");
		        	if(hostObject!=null){
		        		Host host=(Host)hostObject.getApiObject();
		        		vm.startOn(connection, host, false, true);
		        	}else{
		        		vm.start(connection, false,true);
		        	}
		        	record = vm.getRecord(connection);
		        }catch (Exception e) {
					
					e.printStackTrace();
					//此处需要补全
					if(e.toString().startsWith("MEMORY_NOT_ENOUGH")){
						notEnough = true;
						if (!this.display.isDisposed()){
						    Runnable runnable = new Runnable(){
						        public void run(){
						        	MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
									messageBox.setText("警告");
									messageBox.setMessage("目标主机" + (hostObject!=null?hostObject.getName():"") + "内存不足，启动失败");
									messageBox.open();
						        }
						    };
						    this.display.asyncExec(runnable); 
						}
					}
					else if(e.toString().startsWith("DISK_IMG_DOES_NOT_EXIST"))
					{
						if (!this.display.isDisposed()){
						    Runnable runnable = new Runnable(){
						        public void run(){
						        	MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
									messageBox.setText("消息");
									messageBox.setMessage("虚拟机" + selection.getName() + "找不到磁盘文件，启动失败");
									messageBox.open();
						        }
						    };
						    this.display.asyncExec(runnable); 
						}
					}
					else if(e.toString().startsWith("FIBER_IN_USE"))
					{
						String [] tmp = e.toString().split(":");
						crush_vm = tmp[tmp.length-1];
						if (!this.display.isDisposed()){
						    Runnable runnable = new Runnable(){
						        public void run(){
						        	MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
									messageBox.setText("消息");
									messageBox.setMessage("虚拟机" + selection.getName() + "光纤存储与运行虚拟机"+crush_vm+"冲突, 启动失败");
									messageBox.open();
						        }
						    };
						    this.display.asyncExec(runnable); 
						}
					}
					selection.setItemState(ItemState.able);
			        if (!this.display.isDisposed()){
					    Runnable runnable = new Runnable(){
					        public void run(){
					        	viewer.getViewer().refresh();
					        	if(Constants.groupView!=null)
					        		Constants.groupView.getViewer().refresh();
					        }
					    };
					    this.display.asyncExec(runnable); 
					}
			        continue;
				}
		    
		        if(hostObject != null){
					selection.getParent().getChildrenList().remove(selection);
					hostObject.addChild(selection);
				}
		        selection.setRecord(record);
		        selection.setItemState(ItemState.able);
		        VMTreeObject vmInGroup = selection.getShadowObject();
		        if(vmInGroup!=null){
		        	VMTreeObject defaultGroup = null;
			        for(VMTreeObject group:vmInGroup.getParent().getParent().getChildrenList()){
			        	if(group.getName().equals(Constants.VM_GROUP_DEFAULT_NAME)){
			        		defaultGroup = group;
			        		break;
			        	}
			        }
			        if(defaultGroup!=null){
			        	vmInGroup.getParent().getChildrenList().remove(vmInGroup);
			        	defaultGroup.addChild(vmInGroup);
			        }
		        }
				if (!this.display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
				        	selection.startVM();
				        	selection.refresh();
							viewer.getViewer().refresh();
							if(Constants.groupView!=null)
				        		Constants.groupView.getViewer().refresh();
				        }
					};
				    this.display.asyncExec(runnable); 
				}
				
			}
			monitor.done(); 
	        Constants.jobs.remove(this);
	        return Status.OK_STATUS; 
	    } 
	}
	
//	class VMJob extends Thread{
//		VMTreeView viewer;
//		Display display;
//		VM.Record record;
//		public VMJob(Display display, VMTreeView viewer){
//			super("启动虚拟机"+selection.getName());
//			this.viewer=viewer;
//			this.display=display;
//		}
//	    public void run() { 
//	        //判断当前虚拟机状态
//			try {
//				Types.VmPowerState state=vm.getPowerState(connection);
//				//当前虚拟机已经开启
//				if(state.equals(Types.VmPowerState.RUNNING)){
//					selection.setRecord(vm.getRecord(connection));
//					if (!this.display.isDisposed()){
//					    Runnable runnable = new Runnable(){
//					        public void run(){
//					        	viewer.getViewer().refresh();
//					        }
//					    };
//					    this.display.asyncExec(runnable); 
//					}
//					monitor.done();
//					Constants.jobs.remove(this);
//					return Status.CANCEL_STATUS;
//				}
//			} catch (Exception e1) {
//				
//				e1.printStackTrace();
//				monitor.done();
//				Constants.jobs.remove(this);
//				return Status.CANCEL_STATUS;
//			}
//			//当前虚拟机未开启
//			selection.setItemState(ItemState.changing);
//	        if (!this.display.isDisposed()){
//			    Runnable runnable = new Runnable(){
//			        public void run(){
//			        	viewer.getViewer().refresh();
//			        }
//			    };
//			    this.display.asyncExec(runnable); 
//			}
//	        
//	        //找到当前虚拟机应该放置的父节点
//	        VMTreeObject hostObject=null;
//	        VM.Record vmRecord = selection.getRecord();
//	        if(selection.getParent() instanceof VMTreeObjectPool){
//        		if(vmRecord.isLocalVM){
//    	        	VMTreeObjectPool pool = (VMTreeObjectPool)selection.getParent();
//    	        	hostObject = pool.hostMap.get(vmRecord.residentOn);
//    	        }else{
//    	        	hostObject = Constants.getSuitableHost((VMTreeObjectPool)selection.getParent());
//    	        }
//	        }else{
//	        	hostObject = selection.getParent();
//	        }
//        
//	        try {
//	        	//开启虚拟机
//	        	if(hostObject!=null){
//	        		Host host=(Host)hostObject.getApiObject();
//	        		vm.startOn(connection, host, false, true);
//	        	}else{
//	        		vm.start(connection, false,true);
//	        	}
//	        	record = vm.getRecord(connection);
//	        }catch (Exception e) {
//				
//				e.printStackTrace();
//				selection.setItemState(ItemState.able);
//		        if (!this.display.isDisposed()){
//				    Runnable runnable = new Runnable(){
//				        public void run(){
//				        	viewer.getViewer().refresh();
//				        }
//				    };
//				    this.display.asyncExec(runnable); 
//				}
//			}
//	        try {
//				Thread.sleep(5000);
//			} catch (InterruptedException e) {
//				
//				e.printStackTrace();
//			}
//	        if(hostObject != null){
//				selection.getParent().getChildrenList().remove(selection);
//				hostObject.addChild(selection);
//			}
//	        selection.setRecord(record);
//	        selection.setItemState(ItemState.able);
//			if (!this.display.isDisposed()){
//			    Runnable runnable = new Runnable(){
//			        public void run(){
//			        	selection.startVM();
//			        	selection.refresh();
//						viewer.getViewer().refresh();
//			        }
//				};
//			    this.display.asyncExec(runnable); 
//			}
//	    } 
//	}
	public void run(){
		if(selection==null&&selectionList==null){
			StructuredSelection select = (StructuredSelection)Constants.treeView.getViewer().getSelection();
			selection = (VMTreeObjectVM)select.getFirstElement();		
		}
		if(selectionList != null){
			//记录日志暂时没写，回头补上
			VMTreeView viewer = Constants.treeView;
			//for(VMTreeObjectVM vm:selectionList){
				VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay(),viewer,selectionList);
				Constants.jobs.add(job);
				job.schedule();
			//}
		}else{
			//记录日志
			VMTreeObject parent=selection;
			VMEvent event=new VMEvent();
			while(!parent.getName().equals("Xen")){
				event=new VMEvent();
				event.setDatetime(new Date());
				event.setDescription("开启VM '"+selection.getName()+"'");
				event.setTarget(selection);
				event.setTask("");
				event.setType(eventType.info);
				//event.setUser(selection.getUsername());
				event.setImage(ImageRegistry.getImage(ImageRegistry.STARTUP));
				parent.events.add(event);
				
				parent=parent.getParent();
			}
			Constants.logView.logFresh(event);
			VMTreeView viewer = Constants.treeView;
			selectionList = new ArrayList<VMTreeObjectVM>();
			selectionList.add(selection);
			VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay(),viewer,selectionList);
			Constants.jobs.add(job);
			job.schedule();
		}
		
			

		
	}
}
