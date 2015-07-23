package oncecenter.action.vm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import oncecenter.Constants;
import oncecenter.util.CommonUtil;
import oncecenter.util.ImageRegistry;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.VM;

public class VMBackupAction extends Action {
	
	VMTreeObjectVM selection;
	List<VMTreeObjectVM> selectionList;
	Connection connection;
	
	public VMBackupAction(VMTreeObjectVM selection){
		super();
		this.selection=selection;
		this.connection=selection.getConnection();
		setText("克隆");		
	}
	
	public VMBackupAction(List<VMTreeObjectVM> selectionList){
		super();
		this.selectionList=selectionList;
		connection=selectionList.get(0).getConnection();
		setText("克隆");		
	}
	
	class VMJob extends Job{
		VMTreeView viewer;
		Display display;
		String newName;
		VMTreeObject newP;
		VMTreeObject oldP;
		VM newVm;
		VM.Record newRecord;
		VM temp;
		VMTreeObjectVM newObject;
		VMTreeObjectRoot parent;
		public VMJob(Display display,VMTreeView viewer){
			super("克隆虚拟机");
			this.viewer=viewer;
			this.display=display;
		}
		@Override 
	    protected IStatus run(IProgressMonitor monitor) { 
	        monitor.beginTask("正在克隆...", 100); 
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
	        	newName = selection.getName()+"_"+CommonUtil.getCurrentTime();
	        	
	        	parent = selection.getRoot();
	        	newObject = new VMTreeObjectVM(newName,connection
    					,null,null);
        		newObject.setItemState(ItemState.changing);
        		if(parent.temporaryList==null){
        			parent.temporaryList = new ArrayList<VMTreeObject>();
        		}
        		parent.temporaryList.add(newObject);
        		parent.addChild(newObject);
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
        		
        		temp = (VM)selection.getApiObject();
        		try{
        			newVm = temp.createClone(connection, newName);
        			newRecord = newVm.getRecord(connection);
        		}catch(Exception e){
        			e.printStackTrace();
        			if(newVm!=null){
        				try{
        					newVm.destroy(connection, true);
        				}catch(Exception e1){
        					e1.printStackTrace();
        				}
        			}
        			if (!this.display.isDisposed()){
        			    Runnable runnable = new Runnable(){
        			        public void run(){
        			        	newObject.getParent().getChildrenList().remove(newObject);
        			        	viewer.getViewer().remove(newObject);
        			        	viewer.getViewer().refresh();
        			        	if(Constants.groupView!=null)
        			        		Constants.groupView.getViewer().refresh();
        			        }
        			    };
        			    this.display.asyncExec(runnable); 
        			}
        		}
				
				parent.vmMap.put(newVm, newObject);
				
        		newObject.setApiObject(newVm);
        		newObject.setRecord(newRecord);
        		newObject.setItemState(ItemState.able);
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
	        }
	        monitor.done();
	        Constants.jobs.remove(this);
	        return Status.OK_STATUS; 
	    }
	}
	public void run(){
		if(selectionList==null){
			selectionList = new ArrayList<VMTreeObjectVM>();
			selectionList.add(selection);
		}
		final VMEvent event=new VMEvent();
		event.setDatetime(new Date());
		event.setDescription("克隆虚拟机 '" + selection.getName() + "'");
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
//		try {
//			String msgInfo = "确实要克隆虚拟机" + selection.getName() + "吗?";
//			VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay(),Constants.treeView);
//			Constants.jobs.add(job);
//			QuestionDialog dialog = new QuestionDialog(Display.getCurrent().getActiveShell(),msgInfo,job);
//			dialog.open();	
//		} catch (Exception e) {
//			
//			e.printStackTrace();
//		}
	}
}
