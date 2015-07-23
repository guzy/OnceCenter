package oncecenter.action.template;

import oncecenter.Constants;
import oncecenter.action.OnceAction;
import oncecenter.util.ImageRegistry;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.VM;

public class DeleteTempAction extends OnceAction {
	VMTreeObject selection;
	Connection conn;
	VM vm;
	
	public DeleteTempAction(String text,String image,String disabledImage){
		super(text,image,disabledImage);
	}
	
	public DeleteTempAction(VMTreeObject selection){
		super();
		this.selection=selection;
		setText("ÒÆ³ý");		
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.DELETE));
	}
	
	class VMJob extends Job{
		VMTreeView viewer;
		Display display;
		public VMJob(Display display,VMTreeView viewer){
			super("É¾³ýÄ£°å"+selection.getName());
			this.viewer=viewer;
			this.display=display;
		}
		@Override 
	    protected IStatus run(IProgressMonitor monitor) { 
	        monitor.beginTask("É¾³ýÄ£°å"+selection.getName(), 100); 
	        selection.setItemState(ItemState.changing);
	        if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	ISelection s = new StructuredSelection(new Object[]{Constants.CONNECTIONS_TREE});
			    		viewer.getViewer().setSelection(s);
			    		viewer.getViewer().refresh();
			        }
			    };
			    this.display.asyncExec(runnable); 
			}
	        
	        try {
	        	vm.destroy(conn, true);
			} catch (Exception e) {
				
				e.printStackTrace();
				
				 monitor.done();
				 Constants.jobs.remove(this);
			     return Status.CANCEL_STATUS; 
			}
	        if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	VMTreeObjectRoot p = (VMTreeObjectRoot)selection.getParent();
						p.templateMap.remove(vm);
			        	p.getChildrenList().remove(selection);
						viewer.getViewer().remove(selection);
						viewer.getViewer().refresh();
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
			VMTreeView viewer = Constants.treeView;
			StructuredSelection select = (StructuredSelection)viewer.getViewer().getSelection();
			selection = (VMTreeObject)select.getFirstElement();
		}
		vm = (VM)selection.getApiObject();
		conn=selection.getConnection();
		try {
			
			DeleteDialog dialog = new DeleteDialog(Display.getCurrent().getActiveShell(),selection.getName());
			dialog.open();	
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	class DeleteDialog extends Dialog
	{
		private String name = null;
		private static final int OK_ID = 0;
		private static final String OK_LABEL = "OK";
		private static final int CLOSE_ID = 1;
		private static final String CLOSE_LABEL = "Close";
		private CLabel rebootCLabel;
		protected DeleteDialog(Shell parentShell,String name) {
			super(parentShell);
			
			this.name = name;
			
		}

		protected Control createDialogArea(Composite parent)
		{
			Composite composite = (Composite)super.createDialogArea(parent);
			rebootCLabel = new CLabel(composite,SWT.NONE);
			rebootCLabel.setImage(ImageRegistry.getImage(ImageRegistry.REBOOT_OR_NOT));
			rebootCLabel.setText("È·ÊµÒªÉ¾³ýÄ£°å" + name +"Âð?");
			return parent;
		}
		
		protected void createButtonsForButtonBar(Composite parent)
		{
			createButton(parent,DeleteDialog.OK_ID,DeleteDialog.OK_LABEL, true);
			createButton(parent,DeleteDialog.CLOSE_ID,DeleteDialog.CLOSE_LABEL,true);
		}
		
		protected void buttonPressed(int buttonId)
		{
			if(DeleteDialog.OK_ID == buttonId)
			{
				VMTreeView viewer  = Constants.treeView;
				VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay(),viewer);
				Constants.jobs.add(job);
				job.schedule();
				close();
			}
			else if(DeleteDialog.CLOSE_ID == buttonId)
			{
				close();
			}
		}
		
	}
}
