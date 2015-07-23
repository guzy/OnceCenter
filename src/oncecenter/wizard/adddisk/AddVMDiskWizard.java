package oncecenter.wizard.adddisk;

import java.util.ArrayList;

import oncecenter.Constants;
import oncecenter.maintabs.vm.DiskAdjustTab.Disk;
import oncecenter.util.MathUtil;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.SR;
import com.once.xenapi.VDI;
import com.once.xenapi.VM;

public class AddVMDiskWizard extends Wizard {

	private VMTreeObjectVM selection;
	ArrayList<Disk> diskList = new ArrayList<Disk>();
	private AddVMDiskWizardPage addVMDiskWizardPage;
	private VM vm;
	public AddVMDiskWizard(VMTreeObjectVM object)
	{
		setWindowTitle("管理虚拟机挂载的硬盘");
		selection = object;
		vm = (VM)selection.getApiObject();
		getDisks();
	}
	
	@Override
	public void addPages()
	{
		vm = (VM)selection.getApiObject();
		addVMDiskWizardPage = new AddVMDiskWizardPage(diskList);
		addPage(addVMDiskWizardPage);
		
	}
	class VMJob extends Job
	{
		Display display;
		TreeViewer viewer;
		String diskUuid = null;
		String diskSize = null;
		String deleteDiskName = null;
		public VMJob(Display display, TreeViewer viewer, String diskUuid, String diskSize, String deleteDiskName) {
			super("为虚拟机添加磁盘信息");
			this.display = display;
			this.viewer = viewer;
			this.diskUuid = diskUuid;
			this.diskSize = diskSize;
			this.deleteDiskName = deleteDiskName;
		}
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("creating...", 100);
			selection.setItemState(ItemState.changing);
			vm = (VM)selection.getApiObject();			
			if (!display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	viewer.refresh();
			        }
			    };
			    display.syncExec(runnable); 
			}
			try {
				ArrayList<String> diskNameList = new ArrayList<String>();
				for(Disk disk : diskList){
					diskNameList.add(disk.getNameLabel());
				}
				if(!diskNameList.contains(deleteDiskName)) {
					VDI vdi = VDI.createDataDisk(selection.getConnection(), diskUuid, Integer.parseInt(diskSize));
					if(vdi!= null){
						System.out.println("diskUuid = " + diskUuid + ",diskSize = " + diskSize);
						VM.createDataVBD(selection.getConnection(), selection.getUuid(), diskUuid);
					}
				} else {
					VM.deleteDataVBD(selection.getConnection(), selection.getUuid(), deleteDiskName);
				}
				System.out.println("处理之后有" + VDI.getByVM(selection.getConnection(), vm).size() + "个硬盘");
			} catch(Exception e) {
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
		String newDiskUuid = addVMDiskWizardPage.getNewDiskUuid();
		String newDiskSize = addVMDiskWizardPage.getNewDiskSize();
		String deleteDiskName = addVMDiskWizardPage.getDeleteDiskName();
		VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay(),Constants.treeView.getViewer(),newDiskUuid,newDiskSize,deleteDiskName);
		job.schedule();
		return true;
	}
	
	public boolean getDisks(){
		//diskList.clear();
		Connection c = selection.getConnection();
		try{
			for(VDI v : VDI.getByVM(c, vm))
			{
				VDI.Record r = v.getRecord(c);
				Disk disk = new Disk();
				disk.setUuid(r.uuid);
				disk.setLocation(r.location);
				disk.setNameDescription(r.nameDescription);
				disk.setNameLabel(r.nameLabel);
				disk.setTotalValue(MathUtil.RoundingDouble(((double)r.virtualSize)/1024.0/1024.0/1024.0, 2));
				disk.setUsageValue(MathUtil.RoundingDouble(((double)r.physicalUtilisation)/1024.0/1024.0/1024.0, 2));
				disk.setAvailableSpace(disk.getTotalValue()-disk.getUsageValue());
				disk.setVdi(v);
				SR sr = v.getSR(selection.getConnection());
				double maxValue = (double)(sr.getPhysicalSize(selection.getConnection()) - sr.getPhysicalUtilisation(selection.getConnection()));
				disk.setMaxValue(MathUtil.RoundingDouble(maxValue/1024.0/1024.0/1024.0, 2));
				diskList.add(disk);
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public class Disk
	{
		private String uuid;
		private String nameLabel;
		private String nameDescription;
		private String location;
		private double totalValue;
		private double usageValue;
		private double availableSpace;
		private VDI vdi;
		private double maxValue;
		public String getUuid() {
			return uuid;
		}
		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
		public String getNameLabel() {
			return nameLabel;
		}
		public void setNameLabel(String nameLabel) {
			this.nameLabel = nameLabel;
		}
		public String getNameDescription() {
			return nameDescription;
		}
		public void setNameDescription(String nameDescription) {
			this.nameDescription = nameDescription;
		}
		public String getLocation() {
			return location;
		}
		public void setLocation(String location) {
			this.location = location;
		}
		public double getTotalValue() {
			return totalValue;
		}
		public void setTotalValue(double totalValue) {
			this.totalValue = totalValue;
		}
		public double getUsageValue() {
			return usageValue;
		}
		public void setUsageValue(double usageValue) {
			this.usageValue = usageValue;
		}
		public double getAvailableSpace() {
			return availableSpace;
		}
		public void setAvailableSpace(double availableSpace) {
			this.availableSpace = availableSpace;
		}
		public VDI getVdi() {
			return vdi;
		}
		public void setVdi(VDI vdi) {
			this.vdi = vdi;
		}
		public double getMaxValue() {
			return maxValue;
		}
		public void setMaxValue(double maxValue) {
			this.maxValue = maxValue;
		}
	}

}
