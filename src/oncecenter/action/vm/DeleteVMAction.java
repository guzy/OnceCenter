package oncecenter.action.vm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import oncecenter.Constants;
import oncecenter.util.ImageRegistry;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.Types;
import com.once.xenapi.VM;

public class DeleteVMAction extends Action {
	
	VMTreeObjectVM selection;
	List<VMTreeObjectVM> selectionList;
	Connection connection;
	
//	public DeleteVMAction(){
//		super();
//		setText("ÒÆ³ý");		
//		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.DELETE));
//	}
	
	public DeleteVMAction(VMTreeObjectVM selection){
		super();
		this.selection=selection;	
		connection=selection.getConnection();
		setText("ÒÆ³ý");		
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.DELETE));
	}
	
	public DeleteVMAction(List<VMTreeObjectVM> selectionList){
		super();
		this.selectionList=selectionList;
		connection=selectionList.get(0).getConnection();
		setText("ÒÆ³ý");		
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.DELETE));
	}
	
	class VMJob extends Job{
		VMTreeView viewer;
		Display display;
		VM vm;
		public VMJob(Display display,VMTreeView viewer){
			super("ÒÆ³ýÐéÄâ»ú");
			this.viewer=viewer;
			this.display=display;
		}
		@Override 
	    protected IStatus run(IProgressMonitor monitor) { 
	        monitor.beginTask("É¾³ý...", 100); 
	        for(VMTreeObjectVM selection:selectionList){
	        	selection.setItemState(ItemState.changing);
	        }
	        if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	ISelection s = new StructuredSelection(new Object[]{Constants.CONNECTIONS_TREE});
			    		viewer.getViewer().setSelection(s);
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
	        	vm = (VM)selection.getApiObject();
	        	try {
		        	Types.VmPowerState state=vm.getPowerState(selection.getConnection());
					if(!state.equals(Types.VmPowerState.HALTED)){
						System.out.println("ÇëÏÈ¹Ø±ÕÐéÄâ»ú");
						selection.setRecord(vm.getRecord(connection));
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
						continue;
					}
				} catch (Exception e1) {
					e1.printStackTrace();
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
					continue;
				}
		        
		        try {
		        	vm.destroy(connection, true);
				} catch (Exception e) {
					e.printStackTrace();
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
					continue;
				}
				if (!this.display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
				        	VMTreeObjectRoot p = (VMTreeObjectRoot)selection.getParent();
							p.vmMap.remove(vm);
				        	p.getChildrenList().remove(selection);
							viewer.getViewer().remove(selection);
							viewer.getViewer().refresh();
							if(Constants.groupView!=null)
				        		Constants.groupView.getViewer().refresh();
				        }
					};
				    this.display.syncExec(runnable); 
				}
	        }
			
	        monitor.done(); 
	        Constants.jobs.remove(this);
	        return Status.OK_STATUS; 
	    } 
	}
	
	public void run(){
//		if(selection==null&&selectionList==null){
//			VMTreeView viewer = Constants.treeView;
//			StructuredSelection select = (StructuredSelection)viewer.getViewer().getSelection();
//			selection = (VMTreeObjectVM)select.getFirstElement();
//			vm = (VM)selection.getApiObject();
//			connection=selection.getConnection();
//		}
		if(selectionList==null){
			selectionList = new ArrayList<VMTreeObjectVM>();
			selectionList.add(selection);
		}
		try {
			DeleteDialog dialog = new DeleteDialog(Display.getCurrent().getActiveShell());
			dialog.open();	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	class DeleteDialog extends Dialog
	{
		private static final int OK_ID = 0;
		private static final String OK_LABEL = "OK";
		private static final int CLOSE_ID = 1;
		private static final String CLOSE_LABEL = "Close";
		private CLabel rebootCLabel;
		protected DeleteDialog(Shell parentShell) {
			super(parentShell);
			
		}

		protected Control createDialogArea(Composite parent)
		{
			Composite composite = (Composite)super.createDialogArea(parent);
			rebootCLabel = new CLabel(composite,SWT.NONE);
			rebootCLabel.setImage(ImageRegistry.getImage(ImageRegistry.REBOOT_OR_NOT));
			rebootCLabel.setText("È·ÊµÒªÉ¾³ýÒÔÏÂÐéÄâ»úÂð?");
			
			Table table = new Table( composite, SWT.BORDER | SWT.V_SCROLL
			        | SWT.H_SCROLL|SWT.FULL_SELECTION);
			GridData gridData = new GridData();
			gridData.heightHint = 150;
			table.setLayoutData(gridData);
			table.setLinesVisible(true);

			TableColumn tc1 = new TableColumn(table, SWT.CENTER);
			TableColumn tc2 = new TableColumn(table, SWT.CENTER);
			tc1.setText("   ");
			tc2.setText("Ãû³Æ");
			tc1.setWidth(30);
			tc2.setWidth(300);
			table.setHeaderVisible(true);
			
			for(VMTreeObjectVM selection:selectionList){
				TableItem item = new TableItem(table, SWT.NONE);
				item.setImage(0, ImageRegistry.getImage(ImageRegistry.VMOFF));
				item.setText(1, selection.getName());
			}
			
			return parent;
		}
		
		protected void createButtonsForButtonBar(Composite parent)
		{
			createButton(parent,DeleteDialog.OK_ID,"È·ÈÏ", true);
			createButton(parent,DeleteDialog.CLOSE_ID,"È¡Ïû",true);
		}
		
		protected void buttonPressed(int buttonId)
		{
			if(selection != null) {
				final VMEvent event=new VMEvent();
				event.setDatetime(new Date());
				event.setDescription("É¾³ýÐéÄâ»ú '" + selection.getName() + "'");
				event.setTarget(selection);
				event.setTask("");
				event.setType(eventType.info);
				event.setUser(selection.getRoot().getUsername());
				event.setImage(ImageRegistry.getImage(ImageRegistry.INFO));
				selection.events.add(event);
				Constants.logView.logFresh(event);
			}
			VMTreeView viewer  = Constants.treeView;
			VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay(),viewer);
			if(DeleteDialog.OK_ID == buttonId)
			{
				Constants.jobs.add(job);
				job.schedule();
				close();
			}
			else if(DeleteDialog.CLOSE_ID == buttonId)
			{
				//Constants.jobs.remove(job);
				close();
			}
		}
		
	}
}
