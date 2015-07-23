package oncecenter.action.pool;

import java.util.Date;

import oncecenter.Constants;
import oncecenter.util.ImageRegistry;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.Pool;
import com.once.xenapi.SR;

public class RenamePoolAction extends Action {
	VMTreeObjectPool selection;
	Connection connection;
	TreeEditor editor;
	TreeViewer viewer;
	Display display;
	Pool.Record record;
	Pool pool;
	String name;
	boolean force = false;

	public RenamePoolAction(VMTreeObjectPool selection){
		super();
		this.selection=selection;
		connection = selection.getConnection();
		setText("重命名");		
	}
	public RenamePoolAction(VMTreeObjectPool selection,boolean force){
		super();
		this.selection=selection;
		connection = selection.getConnection();
		this.force = force;
		setText("重命名");		
	}
	public void run(){
		viewer = Constants.treeView.getViewer();
		editor = Constants.treeView.editor;
		display = PlatformUI.getWorkbench().getDisplay();
		if(viewer.getTree().getSelectionCount()==0){
			if (!display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						ISelection s1 = new StructuredSelection(new Object[]{selection});
						Constants.treeView.getViewer().setSelection(s1);	
					}
				};
				display.syncExec(runnable);
			}
		}
		if(viewer.getTree().getSelectionCount()==0)
			return;
		TreeItem item = viewer.getTree().getSelection()[0];
		Text newEditor = new Text(viewer.getTree(), SWT.BORDER);
		newEditor.setText(selection.getName());
		newEditor.selectAll();
		newEditor.setFocus();
		editor.setEditor(newEditor, item);
		newEditor.addListener(SWT.FocusOut, new Listener(){

			@Override
			public void handleEvent(Event arg0) {
				
					renameAction();
			}
		});
		
		newEditor.addTraverseListener(new TraverseListener() {  
		      public void keyTraversed(TraverseEvent e) {  
		        if (e.keyCode == 13) {  
		        	//rename();
		        	Constants.treeView.getViewer().getTree().forceFocus();
		        }  
		      }  
		});  
	}
	
	private void renameAction()
	{
		Text text = (Text)editor.getEditor();
		if(text==null){
			return;
		}
		if(text.getText().equals(selection.getName())){
			if(force){
				return;
			}else{
				text.dispose();
				return;
			}
		}
		boolean hasDuplication = false; 
		for(VMTreeObject o:selection.getParent().getChildrenList()){
			if(o.getItemState().equals(ItemState.able)&&o.getName().equals(text.getText())){
				hasDuplication = true;
				break;
			}
		}
		if(hasDuplication){
			//弹出对话框返回
			MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
			messageBox.setText("警告");
			messageBox.setMessage("系统不支持重名的资源池，请重新命名");
			messageBox.open();
			return;
		}
		name = text.getText();
		final VMEvent event=new VMEvent();
		event.setDatetime(new Date());
		event.setDescription("为资源池 '" + selection.getName() + "'重命名");
		event.setTarget(selection);
		event.setTask("");
		event.setType(eventType.info);
		event.setUser(selection.getUsername());
		event.setImage(ImageRegistry.getImage(ImageRegistry.INFO));
		selection.events.add(event);
		Constants.logView.logFresh(event);

    	VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay());
		Constants.jobs.add(job);
		job.schedule();
		text.dispose();
	}
	
	class VMJob extends Job{
		Display display;
		public VMJob(Display display){
			super("重命名");
			this.display=display;
		}
		@Override 
	    protected IStatus run(IProgressMonitor monitor) { 
	        monitor.beginTask("重命名...", 100); 
	        pool = (Pool)selection.getApiObject();
	    	selection.setItemState(ItemState.changing);
	    	if (!display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	viewer.refresh();
			        }
			    };
			    display.asyncExec(runnable); 
			}
			try {
				pool.setNameLabel(connection, name);
			} catch (Exception e1) {
				
				e1.printStackTrace();
				selection.setItemState(ItemState.able);
				if (!display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
				        	viewer.refresh();
				        }
				    };
				    display.asyncExec(runnable); 
				}
				monitor.done();
		        Constants.jobs.remove(this);
		        return Status.CANCEL_STATUS; 
			}
			selection.setName(name);
			try {
				record = pool.getRecord(connection);
			} catch (Exception e1) {
				
				e1.printStackTrace();
				selection.setItemState(ItemState.able);
				if (!display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
				        	viewer.refresh();
				        }
				    };
				    display.asyncExec(runnable); 
				}
				monitor.done();
		        Constants.jobs.remove(this);
		        return Status.CANCEL_STATUS; 
			}
			selection.setRecord(record);
			selection.setItemState(ItemState.able);
			if (!display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	viewer.refresh();
			        }
			    };
			    display.asyncExec(runnable); 
			}
	        monitor.done();
	        Constants.jobs.remove(this);
	        return Status.OK_STATUS; 
	    } 
	}
}
