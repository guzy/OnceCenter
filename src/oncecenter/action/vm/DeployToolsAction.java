package oncecenter.action.vm;

import java.util.Date;

import oncecenter.Constants;
import oncecenter.action.UploadIsoAction;
import oncecenter.util.ImageRegistry;
import oncecenter.util.TypeUtil;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectDefault;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.SR;
import com.once.xenapi.VDI;
import com.once.xenapi.VM;

public class DeployToolsAction extends Action {
	VMTreeObjectVM selection;
	Connection connection;
	VM vm;
	
	VMTreeObjectRoot root;
	VMTreeObjectSR srObject;
	
	public DeployToolsAction(){
		super();
		setText("部署虚拟机工具");		
	}
	
	public DeployToolsAction(VMTreeObjectVM selection){
		super();
		this.selection=selection;		
		setText("部署虚拟机工具");		
	}
	
	class VMJob extends Job{
		VMTreeView viewer;
		Display display;
		public VMJob(Display display,VMTreeView viewer){
			super("部署虚拟机工具"+selection.getName());
			this.viewer=viewer;
			this.display=display;
		}
		@Override 
	    protected IStatus run(IProgressMonitor monitor) { 
	        monitor.beginTask("deploying...", 100); 
	        selection.setItemState(ItemState.changing);
	        if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
						viewer.getViewer().refresh();
			        }
				};
			    this.display.syncExec(runnable); 
			}
	        
	        try{
	        	String s = VDI.getByNameLabel(connection, Constants.vmToolPath).toWireString();
	        	if(s==null||s.length()==0){
	        		if (!this.display.isDisposed()){
	    			    Runnable runnable = new Runnable(){
	    			        public void run(){
	    			        	MessageDialog.openError(new Shell(), "提醒","缺少安装文件，请先上传文件之后，再使用该命令");
	    		        		UploadIsoAction action = new UploadIsoAction();
	    		        		action.run();
	    			        }
	    				};
	    			    this.display.syncExec(runnable); 
	    			}
	        		handleError();
		        	monitor.done(); 
		 	        Constants.jobs.remove(this);
		 	        return Status.CANCEL_STATUS; 
	        	}
	        }catch(Exception e){
	        	e.printStackTrace();
				handleError();
	        	monitor.done(); 
	 	        Constants.jobs.remove(this);
	 	        return Status.CANCEL_STATUS; 
	        }
	        

	        try {
	        	vm.mediaChange(connection, Constants.vmToolPath);
			} catch (Exception e) {
				e.printStackTrace();
				handleError();
	        	monitor.done(); 
	 	        Constants.jobs.remove(this);
	 	        return Status.CANCEL_STATUS; 
			}
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	selection.setItemState(ItemState.able);
						viewer.getViewer().refresh();
			        }
				};
			    this.display.syncExec(runnable); 
			}
			
	        monitor.done(); 
	        Constants.jobs.remove(this);
	        return Status.OK_STATUS; 
	    } 
		
		public void handleError() {
			selection.setItemState(ItemState.able);
			if (!this.display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						viewer.getViewer().refresh();
					}
				};
				this.display.syncExec(runnable);
			}
		}
	}
	
	public VMTreeObjectRoot getRoot(VMTreeObject object){
		if(object.getParent() instanceof VMTreeObjectDefault)
			return (VMTreeObjectRoot)object;
		else
			return getRoot(object.getParent());
	}
	
	public void run(){
		if(selection==null){
			VMTreeView viewer = Constants.treeView;
			StructuredSelection select = (StructuredSelection)viewer.getViewer().getSelection();
			selection = (VMTreeObjectVM)select.getFirstElement();
		}
		vm = (VM)selection.getApiObject();
		connection=selection.getConnection();
		final VMEvent event=new VMEvent();
		event.setDatetime(new Date());
		event.setDescription("为虚拟机 '" + selection.getName() + "'部署虚拟机工具");
		event.setTarget(selection);
		event.setTask("");
		event.setType(eventType.info);
		event.setUser(selection.getRoot().getUsername());
		event.setImage(ImageRegistry.getImage(ImageRegistry.INFO));
		selection.events.add(event);
		Constants.logView.logFresh(event);

		VMTreeView viewer = Constants.treeView;
		VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay(),viewer);
		Constants.jobs.add(job);
		job.schedule();
	}
	
}
