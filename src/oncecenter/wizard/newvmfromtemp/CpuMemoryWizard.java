package oncecenter.wizard.newvmfromtemp;

import java.util.Date;

import oncecenter.Activator;
import oncecenter.Constants;
import oncecenter.action.vm.RebootAction;
import oncecenter.util.ImageRegistry;
import oncecenter.util.VMUtil;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;





import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.once.xenapi.Connection;
import com.once.xenapi.Types;
import com.once.xenapi.VM;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.XenAPIException;
import com.once.xenapi.VM.Record;


public class CpuMemoryWizard extends Wizard{

	private VMTreeObjectVM selection;
	private CpuMemoryWizardPage cpuMemoryWizardPage;
	private Types.VmPowerState state; 
	private VM vm ;
	private Connection conn;
	public CpuMemoryWizard(VMTreeObjectVM object)
	{
		setWindowTitle("调整虚拟机CPU和内存");
		selection = object;
		conn = selection.getConnection();
	}
	
	@Override
	public void addPages()
	{
		vm = (VM)selection.getApiObject();
		try {
			Long vcpuNumber = vm.getVCPUsMax(conn);
			Long memory = vm.getMemoryStaticMax(conn)/1024/1024;
			cpuMemoryWizardPage = new CpuMemoryWizardPage(vcpuNumber.intValue(),memory.intValue());
			this.addPage(cpuMemoryWizardPage);
		} catch (BadServerResponse e) {
			e.printStackTrace();
		} catch (XenAPIException e) {
			e.printStackTrace();
		} catch (XmlRpcException e) {
			e.printStackTrace();
		}
	}
	class VMJob extends Job
	{
		int cpuNumber = cpuMemoryWizardPage.getCPUNum();
		long memoryValue  = cpuMemoryWizardPage.getMemoryValue();
		Display display;
		TreeViewer viewer;
		public VMJob(Display display, TreeViewer viewer) {
			super("调整虚拟机"+selection.getName()+"CPU和内存");
			this.display = display;
			this.viewer = viewer;
		}
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("creating...", 100);
			vm = (VM)selection.getApiObject();
			selection.setItemState(ItemState.changing);
			Connection conn = selection.getConnection();
			VM.Record record;
			if (!display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	viewer.refresh();
			        }
			    };
			    display.syncExec(runnable); 
			}
			
			boolean isSuccess = VMUtil.AdjustCpuMemory(vm, cpuNumber, memoryValue, conn);
			if(!isSuccess){
				selection.setItemState(ItemState.able);
				if (!display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
				        	viewer.refresh();
				        }
				    };
				    display.syncExec(runnable); 
				}
				monitor.done();
				Constants.jobs.remove(this);
				return Status.CANCEL_STATUS;
			}
			
			try
			{
				record = vm.getRecord(conn);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				selection.setItemState(ItemState.able);
				if (!display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
				        	viewer.refresh();
				        }
				    };
				    display.syncExec(runnable); 
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
			    display.syncExec(runnable); 
			}
			
			monitor.done();
			Constants.jobs.remove(this);
			return Status.OK_STATUS;
		}
	}
	@Override
	public boolean performFinish() {
		final VMEvent event=new VMEvent();
		event.setDatetime(new Date());
		event.setDescription("调整虚拟机 '" + selection.getName() + "'的内存和CPU信息");
		event.setTarget(selection);
		event.setTask("");
		event.setType(eventType.info);
		event.setUser(selection.getRoot().getUsername());
		event.setImage(ImageRegistry.getImage(ImageRegistry.INFO));
		selection.events.add(event);
		Constants.logView.logFresh(event);

		Display display = PlatformUI.getWorkbench().getDisplay();
		VMJob job=new VMJob(display,Constants.treeView.getViewer());
		Constants.jobs.add(job);
		job.schedule();
		
		if(state != null && state.equals(Types.VmPowerState.RUNNING))
		{
			RebootDialog dialog = new RebootDialog(Display.getCurrent().getActiveShell());
			dialog.open();
		}
		return true;
	}
	
	class RebootDialog extends Dialog
	{
		private static final int OK_ID = 0;
		private static final String OK_LABEL = "OK";
		private static final int CLOSE_ID = 1;
		private static final String CLOSE_LABEL = "Close";
		private CLabel rebootCLabel;
		protected RebootDialog(Shell parentShell) {
			super(parentShell);
			
			
		}

		protected Control createDialogArea(Composite parent)
		{
			Composite composite = (Composite)super.createDialogArea(parent);
			rebootCLabel = new CLabel(composite,SWT.NONE);
			rebootCLabel.setImage(ImageRegistry.getImage(ImageRegistry.REBOOT_OR_NOT));
			rebootCLabel.setText("是否立即重启，使操作生效");
			return parent;
		}
		
		protected void createButtonsForButtonBar(Composite parent)
		{
			createButton(parent,RebootDialog.OK_ID,RebootDialog.OK_LABEL, true);
			createButton(parent,RebootDialog.CLOSE_ID,RebootDialog.CLOSE_LABEL,true);
		}
		
		protected void buttonPressed(int buttonId)
		{
			if(RebootDialog.OK_ID == buttonId)
			{
				RebootAction rebootAction = new RebootAction(selection);
				rebootAction.run();
			}
			else if(RebootDialog.CLOSE_ID == buttonId)
			{
				close();
			}
		}
		
	}

}
