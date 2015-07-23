package oncecenter.action.template;

import java.util.Date;

import oncecenter.Constants;
import oncecenter.util.ImageRegistry;
import oncecenter.util.dialog.QuestionDialog;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectTemplate;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.VM;

public class ChangeToVmAction extends Action {
	
	VMTreeObjectTemplate selection;
	VM vm;
	Connection connection;
	
	public ChangeToVmAction(VMTreeObjectTemplate selection){
		super();
		this.selection = selection;
		
		setText("转化为虚拟机");		
		//setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.TEMPLATE));
	}
	
	class VMJob extends Job{
		VMTreeView viewer;
		IViewReference reference;
		Display display;
		VM.Record  record;
		public VMJob(Display display,VMTreeView viewer){
			super("模板"+ selection.getName() +"转化为虚拟机");
			this.viewer=viewer;
			this.display=display;
		}
		@Override 
	    protected IStatus run(IProgressMonitor monitor) { 
	        monitor.beginTask("转化为虚拟机...", 100); 
	        
	        try {
	        	vm.setIsATemplate(connection, false);
	        	record = vm.getRecord(connection);
			} catch (Exception e) {
				
				e.printStackTrace();
				monitor.done();
				Constants.jobs.remove(this);
				return Status.CANCEL_STATUS;
			}
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	VMTreeObjectVM newO = new VMTreeObjectVM(selection.getName()
			        			,connection,vm,record);
			    		selection.getParent().addChild(newO);
			    		selection.getParent().getChildrenList().remove(selection);
			    		VMTreeObjectRoot p = (VMTreeObjectRoot)selection.getParent();
						p.vmMap.put(vm, newO);
						p.templateMap.remove(vm);
			    		viewer.getViewer().remove(selection);
			    		viewer.getViewer().refresh();
			    		ISelection selection = new StructuredSelection(new Object[]{newO});
			    		viewer.getViewer().setSelection(selection);
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
		vm = (VM)selection.getApiObject();
		connection = selection.getConnection();
		try {
			String msgInfo = "确实要把模板" + selection.getName() + "转换成虚拟机吗?";
			final VMEvent event=new VMEvent();
			event.setDatetime(new Date());
			event.setDescription("把模板 '" + selection.getName() + "'转换成虚拟机");
			event.setTarget(selection);
			event.setTask("");
			event.setType(eventType.info);
			event.setUser(selection.getRoot().getUsername());
			event.setImage(ImageRegistry.getImage(ImageRegistry.INFO));
			selection.events.add(event);
			Constants.logView.logFresh(event);

			VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay(),Constants.treeView);
			Constants.jobs.add(job);
			QuestionDialog dialog = new QuestionDialog(Display.getCurrent().getActiveShell(),msgInfo,job);
			dialog.open();	
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
	}
}
