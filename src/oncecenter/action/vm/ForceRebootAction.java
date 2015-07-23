package oncecenter.action.vm;

import java.util.Date;
import java.util.List;

import oncecenter.Constants;
import oncecenter.util.ImageRegistry;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.Types;
import com.once.xenapi.VM;

public class ForceRebootAction extends Action {
	
	VMTreeObjectVM selection;
	Connection connection;
	VM vm;
	
	List<VMTreeObjectVM> selectionList;
	
	public ForceRebootAction(){
		super();
		
		setText("强制重启");		
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.REBOOT));
	}
	public ForceRebootAction(VMTreeObjectVM selection){
		super();
		
		this.selection=selection;
		setText("强制重启");		
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.REBOOT));
	}
	
	class VMJob extends Job{
		VMTreeView viewer;
		Display display;
		VM.Record record;
		public VMJob(Display display,VMTreeView viewer){
			super("强制重启虚拟机"+selection.getName());
			this.viewer=viewer;
			this.display=display;
		}
		@Override 
	    protected IStatus run(IProgressMonitor monitor) { 
	        monitor.beginTask("rebooting...", 100); 
	        try {
	        	Types.VmPowerState state=vm.getPowerState(selection.getConnection());
				if(state.equals(Types.VmPowerState.HALTED)){
					selection.setRecord(vm.getRecord(connection));
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
			} catch (Exception e1) {
				
				e1.printStackTrace();
				monitor.done();
				Constants.jobs.remove(this);
				return Status.CANCEL_STATUS;
			}
	        selection.setItemState(ItemState.changing);
	        if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	viewer.getViewer().refresh();
			        	selection.shutVM();
			        	selection.refresh();
			        }
			    };
			    this.display.syncExec(runnable); 
			}
	        try {
	        	vm.hardReboot(connection);
	        	record = vm.getRecord(connection);
			} catch (Exception e) {
				e.printStackTrace();
				selection.setItemState(ItemState.able);
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
	        selection.setRecord(record);
	        selection.setItemState(ItemState.able);
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	selection.startVM();
			        	selection.refresh();
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
		if(selection==null){
			StructuredSelection select = (StructuredSelection)Constants.treeView.getViewer().getSelection();
			selection = (VMTreeObjectVM)select.getFirstElement();	
		}
		vm = (VM)selection.getApiObject();
		connection=selection.getConnection();
		try {
			VMTreeObject parent=selection;
			VMEvent event=new VMEvent();
			while(!parent.getName().equals("Xen")){
				event=new VMEvent();
				event.setDatetime(new Date());
				event.setDescription("重启 VM '"+selection.getName()+"'");
				event.setTarget(selection);
				event.setTask("");
				event.setType(eventType.info);
				//event.setUser(selection.getUsername());
				event.setImage(ImageRegistry.getImage(ImageRegistry.REBOOT));
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
