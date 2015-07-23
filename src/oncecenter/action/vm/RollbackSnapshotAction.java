package oncecenter.action.vm;

import java.util.Date;

import oncecenter.Constants;
import oncecenter.maintabs.vm.VMSnapShotsTab;
import oncecenter.util.ImageRegistry;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.VM;

public class RollbackSnapshotAction extends Action {

	private VMTreeObjectVM objectVM;
	private String snapshotName;
	private Connection connection;
	private VM vm;
	private VMSnapShotsTab tab;
	
	public RollbackSnapshotAction(VMTreeObjectVM objectVM, String snapshotName,VMSnapShotsTab tab)
	{
		this.objectVM = objectVM;
		this.snapshotName = snapshotName;
		this.tab = tab;
	}
	
	class VMJob extends Job
	{
		VMTreeView viewer;
		Display display;
		
		public VMJob(VMTreeView viewer, Display display) {
			super("回滚虚拟机" + objectVM.getName() + "到快照" + snapshotName);
			this.viewer = viewer;
			this.display = display;
			connection = objectVM.getConnection();
			vm = (VM)objectVM.getApiObject();
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("rollback snapshot...", 100);
			//首先判断虚拟机又没哟在执行开机、迁移之类的操作
			ItemState state = objectVM.getItemState();
			if(state.equals(ItemState.changing))
			{
				System.out.println("请等待虚拟机正在执行的任务完成！");
				if (!this.display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
				        	viewer.getViewer().refresh();
				        }
				    };
				    this.display.syncExec(runnable); 
				}
				monitor.done();
				return Status.CANCEL_STATUS;
			}
			objectVM.setItemState(ItemState.changing);
			if (!this.display.isDisposed()){
				   Runnable runnable = new Runnable(){
				       public void run(){
				        viewer.getViewer().refresh();
				       }
				   };
				   this.display.syncExec(runnable); 
			}
			boolean isSuccess;
			//回滚快照
			try{
				isSuccess = vm.rollback(connection, snapshotName);
				if(isSuccess)
					System.out.println("Roll back snapshot is success!!!");
			}
			catch(Exception e1)
			{
				e1.printStackTrace();
				objectVM.setItemState(ItemState.able);
				monitor.done();
				return Status.CANCEL_STATUS;
			}
			objectVM.setItemState(ItemState.able);
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
						viewer.getViewer().refresh();
						tab.refreshTable();
			        }
				};
			    this.display.syncExec(runnable); 
			}
			monitor.done();
			
			return Status.OK_STATUS;
		}
		
	}

	
	public void run(){
		try {
			RollbackDialog dialog = new RollbackDialog(Display.getCurrent().getActiveShell());
			dialog.open();	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	class RollbackDialog extends Dialog
	{
		private static final int OK_ID = 0;
		private static final String OK_LABEL = "OK";
		private static final int CLOSE_ID = 1;
		private static final String CLOSE_LABEL = "Close";
		private CLabel rebootCLabel;
		protected RollbackDialog(Shell parentShell) {
			super(parentShell);
			
			
		}

		protected Control createDialogArea(Composite parent)
		{
			Composite composite = (Composite)super.createDialogArea(parent);
			rebootCLabel = new CLabel(composite,SWT.NONE);
			rebootCLabel.setImage(ImageRegistry.getImage(ImageRegistry.REBOOT_OR_NOT));
			rebootCLabel.setText("回滚快照会丢失快照时间点之后的更改"+ "\n确实要回滚虚拟机" + objectVM.getName() + "到快照"+ snapshotName+ "吗?");
			return parent;
		}
		
		protected void createButtonsForButtonBar(Composite parent)
		{
			createButton(parent,RollbackDialog.OK_ID,RollbackDialog.OK_LABEL, true);
			createButton(parent,RollbackDialog.CLOSE_ID,RollbackDialog.CLOSE_LABEL,true);
		}
		
		protected void buttonPressed(int buttonId)
		{
			final VMEvent event=new VMEvent();
			event.setDatetime(new Date());
			event.setDescription("连接到 '" + objectVM.getName() + "'");
			event.setTarget(objectVM);
			event.setTask("");
			event.setType(eventType.info);
			event.setUser(objectVM.getRoot().getUsername());
			event.setImage(ImageRegistry.getImage(ImageRegistry.INFO));
			objectVM.events.add(event);
			Constants.logView.logFresh(event);

			VMTreeView viewer  = Constants.treeView;
			VMJob job=new VMJob(viewer,PlatformUI.getWorkbench().getDisplay());
			if(RollbackDialog.OK_ID == buttonId)
			{
				job.schedule();
				close();
			}
			else if(RollbackDialog.CLOSE_ID == buttonId)
			{
				close();
			}
		}
		
	}
}
