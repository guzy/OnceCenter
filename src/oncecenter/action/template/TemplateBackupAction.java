package oncecenter.action.template;

import java.util.ArrayList;
import java.util.Date;

import oncecenter.Constants;
import oncecenter.util.CommonUtil;
import oncecenter.util.ImageRegistry;
import oncecenter.util.dialog.QuestionDialog;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectTemplate;
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
import com.once.xenapi.VM;

public class TemplateBackupAction extends Action {
	
	VMTreeObjectTemplate selection;
	Connection conn;
	
	public TemplateBackupAction()
	{
		super();
		setText("克隆");
	}
	public TemplateBackupAction(VMTreeObjectTemplate selection){
		super();
		this.selection=selection;
		this.conn=selection.getConnection();
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
		VMTreeObjectTemplate newObject;
		VMTreeObject parent;
		//int index = 0;
		public VMJob(Display display,VMTreeView viewer){
			super("克隆模板"+ selection.getName());
			this.viewer=viewer;
			this.display=display;
			newName = selection.getName()+"_"+CommonUtil.getCurrentTime();
		}
//		public void getIndex(VMTreeObject root){
//			for(VMTreeObject o :root.getChildrenList()){
//				String name = o.getName();
//				if(!name.equals(this.newName)&&name.indexOf(this.newName)==0){
//					try{
//						int i = Integer.parseInt(name.substring(this.newName.length()));
//						if(index<i){
//							index=i;
//						}
//					}catch(Exception e){
//						e.printStackTrace();
//						continue;
//					}
//					
//				}
//				getIndex(o);
//			}
//		}
		@Override 
	    protected IStatus run(IProgressMonitor monitor) { 
	        monitor.beginTask("正在克隆...", 100); 
	        try {
	        	if(selection instanceof VMTreeObjectTemplate){
	        		
//	        		if(selection.getParent() instanceof VMTreeObjectPool){
//	        			
//	        		}else{
//	        			monitor.done();
//						return Status.CANCEL_STATUS;
//	        		} 
	        		
	        		VMTreeObjectRoot root = (VMTreeObjectRoot)selection.getParent();
	        		//getIndex(selection.getParent());
	        		
        			//newName = this.newName+(index+1);
        			parent = selection.getParent();
        			newObject = new VMTreeObjectTemplate(newName,conn
        					,null,null);
	        		newObject.setItemState(ItemState.changing);
	        		selection.setItemState(ItemState.changing);
	        		
	        		if(root.temporaryList==null){
	        			root.temporaryList = new ArrayList<VMTreeObject>();
	        		}
	        		root.temporaryList.add(newObject);
	        		
	        		parent.addChild(newObject);
	        		
	        		if (!this.display.isDisposed()) {
	    				Runnable runnable = new Runnable() {
	    					public void run() {
	    						viewer.getViewer().expandAll();
	    						viewer.getViewer().refresh();	
	    					}
	    				};
	    				this.display.asyncExec(runnable);
	    			}
        			temp = (VM)selection.getApiObject();
					newVm = temp.createClone(conn, newName);
					newVm.setIsATemplate(conn, true);
					root.templateMap.put(newVm, newObject);
					
					newRecord = newVm.getRecord(conn);
					
	        		newObject.setApiObject(newVm);
	        		newObject.setRecord(newRecord);
	        		newObject.setItemState(ItemState.able);
	        		selection.setItemState(ItemState.able);
	        		
	        		if (!this.display.isDisposed()) {
	    				Runnable runnable = new Runnable() {
	    					public void run() {
	    						viewer.getViewer().refresh();						
	    					}
	    				};
	    				this.display.asyncExec(runnable);
	    			}
	        	}else{
	        		monitor.done();
	        		Constants.jobs.remove(this);
					return Status.CANCEL_STATUS;
	        	}
			} catch (Exception e1) {
				
				e1.printStackTrace();
				if (!this.display.isDisposed()) {
    				Runnable runnable = new Runnable() {
    					public void run() {
    						newObject.getParent().getChildrenList().remove(newObject);
    						viewer.getViewer().remove(newObject);
    						viewer.getViewer().expandAll();
    						viewer.getViewer().refresh();	
    					}
    				};
    				this.display.asyncExec(runnable);
    			}
				monitor.done();
				Constants.jobs.remove(this);
				return Status.CANCEL_STATUS;
			}
	        monitor.done(); 
	        Constants.jobs.remove(this);
	        return Status.OK_STATUS; 
	    }
	}
	public void run(){
		if(selection==null){
			VMTreeView viewer = Constants.treeView;
			StructuredSelection select = (StructuredSelection)viewer.getViewer().getSelection();
			selection = (VMTreeObjectTemplate)select.getFirstElement();
		}
		try {
			String msgInfo = "确实要克隆模板" + selection.getName() + "吗?";
			final VMEvent event=new VMEvent();
			event.setDatetime(new Date());
			event.setDescription("克隆模板 '" + selection.getName() + "'");
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
