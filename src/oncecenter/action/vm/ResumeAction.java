package oncecenter.action.vm;

import java.util.Date;

import oncecenter.Constants;
import oncecenter.util.ImageRegistry;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.Host;
import com.once.xenapi.Types;
import com.once.xenapi.VM;

public class ResumeAction extends Action {
	VMTreeObjectVM selection;
	Connection connection;
	VM vm;
	
	public ResumeAction(){
		super();
		
		setText("唤醒");		
//		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
//				ImageRegistry.getImagePath(ImageRegistry.SUSPEND)));
	}
	
	public ResumeAction(VMTreeObjectVM selection){
		super();
		this.selection=selection;
		setText("唤醒");		
//		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
//				ImageRegistry.getImagePath(ImageRegistry.SUSPEND)));
	}
	
	class VMJob extends Job{
		VMTreeView viewer;
		Display display;
		VM.Record record;
		public VMJob(Display display,VMTreeView viewer){
			super("唤醒虚拟机"+selection.getName());
			this.viewer=viewer;
			this.display=display;
		}
		@Override 
	    protected IStatus run(IProgressMonitor monitor) { 
	        monitor.beginTask("唤醒中 ...", 100); 
	        try {
	        	Types.VmPowerState state=vm.getPowerState(selection.getConnection());
				if(!state.equals(Types.VmPowerState.SUSPENDED)){
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
	        //将当前被挂起的虚拟机放置在其父节点
	        VMTreeObject hostObject = null;
	        if(selection.getParent().getParent() instanceof VMTreeObjectPool)
	        {
	        	hostObject = Constants.getSuitableHost((VMTreeObjectPool)selection.getParent());
	        }
			try {
				if(hostObject != null)
				{
					Host host=(Host)hostObject.getApiObject();
					vm.resumeOn(connection, host, false, true);
				}
				else
					vm.resume(connection, false);
				record = vm.getRecord(connection);
			} catch (Exception e) {
				e.printStackTrace();
				monitor.done(); 
				Constants.jobs.remove(this);
		        return Status.CANCEL_STATUS;
			}
			
			selection.setRecord(record);
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
			//记录日志
			VMTreeObject parent=selection;
			VMEvent event=new VMEvent();
			while(!parent.getName().equals("Xen")){
				event=new VMEvent();
				event.setDatetime(new Date());
				event.setDescription("唤醒VM '"+selection.getName()+"'");
				event.setTarget(selection);
				event.setTask("");
				event.setType(eventType.info);
				//event.setUser(selection.getUsername());
				event.setImage(ImageRegistry.getImage(ImageRegistry.BOOT));
				
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
