package oncecenter.action.vm;

import java.util.Date;
import java.util.List;

import oncecenter.Constants;
import oncecenter.action.vm.StartOnAction.VMJob;
import oncecenter.util.ImageRegistry;
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

public class StartFromCDAction extends Action {
	
	VMTreeObjectVM selection;
	Connection connection;
	VM vm;
	VMTreeObject hostObject;
	
	public StartFromCDAction(VMTreeObjectVM selection){
		super();
		this.selection=selection;
		setText("从光盘启动");		
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.STARTUP));
	}
	
	class VMJob extends Job{
		VMTreeView viewer;
		Display display;
		VM.Record record;
		public VMJob(Display display, VMTreeView viewer){
			super("启动虚拟机");
			this.viewer=viewer;
			this.display=display;
		}
		@Override 
	    protected IStatus run(IProgressMonitor monitor) { 
	        monitor.beginTask("启动中 ...", 100); 
	        selection.setItemState(ItemState.changing);
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
	       
	      //判断当前虚拟机状态
			try {
				Types.VmPowerState state=vm.getPowerState(connection);
				//当前虚拟机已经开启
				if(state.equals(Types.VmPowerState.RUNNING)){
					System.out.println("该虚拟机已经开启");
					selection.setItemState(ItemState.able);
					selection.setRecord(vm.getRecord(connection));
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
					monitor.done();
					Constants.jobs.remove(this);
					return Status.CANCEL_STATUS;
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
				monitor.done();
				Constants.jobs.remove(this);
				return Status.CANCEL_STATUS;
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
	        	vm.setBootOrder(connection, "dc");
	        	if(hostObject!=null){
	        		Host host=(Host)hostObject.getApiObject();
	        		vm.startOn(connection, host, false, true);
	        	}else{
	        		vm.start(connection, false,true);
	        	}
	        	//vm.setBootOrder(connection, "cd");
	        	record = vm.getRecord(connection);
	        }catch (Exception e) {
				e.printStackTrace();
				//此处需要补全
				if(e.toString().startsWith("MEMORY_NOT_ENOUGH")){
					if (!this.display.isDisposed()){
					    Runnable runnable = new Runnable(){
					        public void run(){
					        	MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
								messageBox.setText("警告");
								messageBox.setMessage("目标主机" + hostObject!=null?hostObject.getName():"" + "内存不足，启动失败");
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
					final String crush_vm = tmp[tmp.length-1];
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
		        monitor.done(); 
		        Constants.jobs.remove(this);
		        return Status.CANCEL_STATUS; 
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
				
			monitor.done(); 
	        Constants.jobs.remove(this);
	        return Status.OK_STATUS; 
	    } 
	}
	
	public void run(){
		if(selection==null){
			StructuredSelection select = (StructuredSelection)Constants.treeView.getViewer().getSelection();
			selection = (VMTreeObjectVM)select.getFirstElement();		
		}
		vm = (VM)selection.getApiObject();
		connection=selection.getConnection();
		try {
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
			VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay(),viewer);
			Constants.jobs.add(job);
			job.schedule();
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
}
