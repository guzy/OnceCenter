package oncecenter.action.vm;

import java.util.Date;
import java.util.List;

import oncecenter.Constants;
import oncecenter.action.vm.ChangetoTemplateAction.VMJob;
import oncecenter.util.ImageRegistry;
import oncecenter.util.dialog.QuestionDialog;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.Types;
import com.once.xenapi.VM;

public class ForceShutAction extends Action {

	VMTreeObjectVM selection;
	Connection connection;
	VM vm;
	
	List<VMTreeObjectVM> selectionList;
		
	public ForceShutAction(){
		super();
		
		setText("强制关机");		
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.SHUTDOWN));
	}
	public ForceShutAction(VMTreeObjectVM selection){
		super();
		this.selection=selection;
		
		setText("强制关机");		
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.SHUTDOWN));
	}
	class VMJob extends Job{
		VMTreeView viewer;
		Display display;
		VM.Record record;
		public VMJob(Display display,VMTreeView viewer){
			super("强制关闭虚拟机"+selection.getName());
			this.viewer=viewer;
			this.display=display;
		}
		@Override 
	    protected IStatus run(IProgressMonitor monitor) { 
	        monitor.beginTask("强制关机中 ...", 100); 
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
				vm.hardShutdown(connection);
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
			if(selection.getParent().getParent() instanceof VMTreeObjectPool){
        		selection.getParent().getChildrenList().remove(selection);
        		VMTreeObject pool=selection.getParent().getParent();
				pool.addChild(selection);
        	}
			selection.setRecord(record);
	        selection.setItemState(ItemState.able);
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
//			        	if(reference!=null){
//			        		MainView console=(MainView)reference.getView(false);
//			        		console.getVmGeneralItem().refresh();
//			        		console.getLogItem().logFresh();
//			        		VMTreeParent parent=selection.getParent();
//			    			while(!parent.getName().equals("Xen")){
//			    				IViewReference reference1=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
//			    						.findViewReference(MainView.ID, parent.getConsoleID()+"");
//			    				if(reference1!=null){
//			    					MainView console1=(MainView)reference1.getView(false);
//				    				console1.getLogItem().logFresh();
//			    				}
//			    				parent=parent.getParent();
//			    			}
//						}
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
				event.setDescription("关闭 VM '"+selection.getName()+"'");
				event.setTarget(selection);
				event.setTask("");
				event.setType(eventType.info);
				//event.setUser(selection.getUsername());
				event.setImage(ImageRegistry.getImage(ImageRegistry.SHUTDOWN));
				parent.events.add(event);
				parent=parent.getParent();
			}
			Constants.logView.logFresh(event);
			VMTreeView viewer = Constants.treeView;
			String msgInfo = "您确定要强制关闭虚拟机" + selection.getName() + "吗?";
			VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay(),viewer);
			Constants.jobs.add(job);
			QuestionDialog dialog = new QuestionDialog(Display.getCurrent().getActiveShell(),msgInfo,job);
			dialog.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
