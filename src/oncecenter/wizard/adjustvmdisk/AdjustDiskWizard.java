package oncecenter.wizard.adjustvmdisk;

import java.util.ArrayList;

import oncecenter.Constants;
import oncecenter.maintabs.vm.DiskAdjustTab;
import oncecenter.maintabs.vm.DiskAdjustTab.Disk;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;
import oncecenter.wizard.newvmfromtemp.FinishWizardPage;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.SR;

public class AdjustDiskWizard extends Wizard {

	VMTreeObjectVM vm;
	DiskAdjustTab tab;
	ArrayList<Disk> diskList;
	ArrayList<Double> expandList;
	AdjustDiskWizardPage adjustWizardPage;
	AdjustFinishWizardPage finishPage;
	String afterExpandMessage = "扩容成功，重启虚拟机后生效";
	
	public AdjustDiskWizard(ArrayList<Disk> diskList,VMTreeObjectVM vm,DiskAdjustTab tab){
		setWindowTitle("手动扩容");
		this.vm = vm;
		this.tab = tab;
		this.diskList = diskList;
		expandList = new ArrayList<Double>();
	}
	
	@Override
	public void addPages() {
		adjustWizardPage = new AdjustDiskWizardPage(diskList);
		this.addPage(adjustWizardPage);
		finishPage = new AdjustFinishWizardPage();
		this.addPage(finishPage);
	}
	
	public boolean canFinish()
	{ 
		IWizardPage page = getContainer().getCurrentPage();
		if( page instanceof AdjustFinishWizardPage)
			return true;
		else return false;
	}
	
	class VMJob extends Job
	{
		Display display;
		Connection conn;
		VMTreeView treeView;
		public VMJob(Display display) {
			super("调整中.......");
			this.display = display;
			this.conn = vm.getConnection();
			treeView = Constants.treeView;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("调整中......", 100);
			vm.itemState = ItemState.changing;
			if (!this.display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						treeView.getViewer().refresh();
					}
				};
				this.display.syncExec(runnable);
			}
			try{
				for(Disk disk : diskList){
					int index = diskList.indexOf(disk);
					if (disk.getTotalValue()+expandList.get(index) < disk.getMaxValue()){
						disk.getVdi().setVirtualSize(conn, (long)(disk.getTotalValue()+expandList.get(index)));
					}else {
						afterExpandMessage = "扩容后将超过最大流量限制，请核实可扩容容量";
						disk.getVdi().setVirtualSize(conn, (long)(0));
					}					
				}
				if(tab.getDisks()){
	        		if (!this.display.isDisposed()){
		      			 Runnable runnable = new Runnable(){
		      				 public void run(){
		      					MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
		      					messageBox.setText("提示");
		      					messageBox.setMessage(afterExpandMessage);
		      					messageBox.open();
		      				 }
		      			 };
		      			 this.display.syncExec(runnable); 
		      		 }
	        	}else{
	        		throw new Exception();
	        	}
			}catch(Exception e){
				e.printStackTrace();
				if (!this.display.isDisposed()){
	      			 Runnable runnable = new Runnable(){
	      				 public void run(){
	      					MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
	      					messageBox.setText("警告");
	      					messageBox.setMessage("扩容失败！");
	      					messageBox.open();
	      				 }
	      			 };
	      			 this.display.syncExec(runnable); 
	      		 }
			}
			vm.itemState = ItemState.able;
			if (!this.display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						tab.autoExpand.setText("扩容完毕");
						tab.autoExpand.setEnabled(true);
						tab.autoExpand.pack();
						tab.drawChart();
						tab.addItem(vm);
						treeView.getViewer().refresh();
					}
				};
				this.display.syncExec(runnable);
			}
			monitor.done();
			return Status.OK_STATUS;
		}
		
	}
	@Override
	public boolean performFinish() {
		VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay());
		job.schedule();
		return true;
	}

}
