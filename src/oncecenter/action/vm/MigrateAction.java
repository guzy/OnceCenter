package oncecenter.action.vm;

import java.util.Date;
import java.util.Map;

import oncecenter.Constants;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.core.internal.content.Activator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.once.xenapi.Connection;
import com.once.xenapi.Host;
import com.once.xenapi.VM;

public class MigrateAction extends Action {

	VMTreeObjectVM vmObject;
	VMTreeObjectHost hostObject;
	Connection connection;
	VM vm;
	Host host;
	
	public MigrateAction(VMTreeObjectVM vmObject,VMTreeObjectHost hostObject){
		super();
		setText(hostObject.getName());	
		this.vmObject=vmObject;
		this.hostObject=hostObject;
		this.connection=vmObject.getConnection();
		vm = (VM)vmObject.getApiObject();
		host = (Host)hostObject.getApiObject();
	}
	
	class VMJob extends Job{
		Display display;
		VMTreeView viewer;
		VM.Record record;
		public VMJob(Display display,VMTreeView viewer){
			super("迁移虚拟机"+vmObject.getName());
			this.display=display;
			this.viewer=viewer;
		}
		@Override 
	    protected IStatus run(IProgressMonitor monitor) { 
	        monitor.beginTask("迁移中 ...", 100); 
	        vmObject.setItemState(ItemState.changing); 
	        if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	viewer.getViewer().refresh();
			        }
			    };
			    this.display.syncExec(runnable); 
			}
	        VM vm = (VM)vmObject.getApiObject();
	        try {
				record = vm.getRecord(vmObject.getConnection());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
	        double vmMemory = 0.0;
	        if(record!=null){
	        	vmMemory = record.memoryDynamicMax/1024.0/1024.0;
	        }
	        if(hostObject.getMemoryUsageValue()==0){
	        	hostObject.getGrade();
	        }
	        double hostFreeMemory = hostObject.getMemoryTotalValue() - hostObject.getMemoryUsageValue();
	        if(hostFreeMemory<vmMemory||hostObject.getGrade()==0){
	        	vmObject.setItemState(ItemState.able); 
	        	if (!this.display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
				        	viewer.getViewer().refresh();
				        }
				    };
				    this.display.syncExec(runnable); 
				}
	        	monitor.done();
	        	Constants.jobs.remove(this);
		        return Status.CANCEL_STATUS;
	        }
	        if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	vmObject.shutVM();
			        	vmObject.refresh();
			        }
			    };
			    this.display.syncExec(runnable); 
			}
			try {
				Map<String, String> op = vm.getOtherConfig(connection);
				vm.poolMigrate(connection, host, op);
				record = vm.getRecord(connection);
			} catch (Exception e) {
				e.printStackTrace();
				vmObject.setItemState(ItemState.able);
		        if (!this.display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
				        	vmObject.startVM();
				        	vmObject.refresh();
				        	viewer.getViewer().refresh();
				        }
				    };
				    this.display.syncExec(runnable); 
				}
				monitor.done(); 
				Constants.jobs.remove(this);
		        return Status.CANCEL_STATUS;
			}
			vmObject.setRecord(record);
			vmObject.setItemState(ItemState.able);
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	VMTreeObject parent = vmObject.getParent();
			        	parent.getChildrenList().remove(vmObject);
			        	hostObject.addChild(vmObject);
			        	vmObject.startVM();
			        	vmObject.refresh();
						viewer.getViewer().refresh();
			        }
				};
			    this.display.syncExec(runnable); 
			}
			
	        monitor.done(); 
	        Constants.jobs.remove(this);
	        return Status.OK_STATUS; 
	    } 
	}
	public void run(){
//		VMTreeObject parent=vmObject;
//		while(!parent.getName().equals("Xen")){
//			VMEvent event=new VMEvent();
//			event.setDatetime(new Date());
//			event.setDescription("迁移VM '"+vmObject.getName()+"' 到主机 '"+hostobject.getName()+"'");
//			event.setTarget(vmObject);
//			event.setTask("");
//			event.setType(eventType.info);
//			event.setUser(vmObject.getUsername());
////			event.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
////					"icons/console/boot.png").createImage());
//			parent.events.add(event);
//			parent=parent.getParent();
//		}
		VMEvent event=new VMEvent();
		event.setDatetime(new Date());
		event.setDescription("迁移VM '" + vmObject.getName()+"' 到主机 '" + hostObject.getName() + "'");
		event.setTarget(vmObject);
		event.setTask("");
		event.setType(eventType.info);
		event.setUser(vmObject.getRoot().getUsername());
		hostObject.events.add(event);
//		Connection conn = vmObject.getConnection();
//		try {
//			Host host = (Host)hostobject.getApiObject();
//			VM vm = (VM)vmObject.getApiObject();
//			Map<String, String> op = vm.getOtherConfig(conn);
//			vm.poolMigrate(conn, host, op);
//		} catch (Exception e) {
//			
//			e.printStackTrace();
//		}
		VMTreeView viewer = Constants.treeView;
		VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay(),viewer);
		Constants.jobs.add(job);
		job.schedule();
	}
}
