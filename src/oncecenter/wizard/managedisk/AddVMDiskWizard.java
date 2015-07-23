package oncecenter.wizard.managedisk;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import oncecenter.Constants;
import oncecenter.util.ImageRegistry;
import oncecenter.util.MathUtil;
import oncecenter.util.dialog.QuestionDialog;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.SR;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.XenAPIException;
import com.once.xenapi.VDI;
import com.once.xenapi.VM;

public class AddVMDiskWizard extends Wizard {

	private VMTreeObjectVM selection;
	ArrayList<Disk> diskList = new ArrayList<Disk>();
	private AddVMDiskWizardPage addVMDiskWizardPage;
	private VM vm;
	String VIPDiskName = null;
	Connection c = null;
	public AddVMDiskWizard(VMTreeObjectVM object)
	{
		setWindowTitle("管理虚拟机挂载的硬盘");
		selection = object;
		vm = (VM)selection.getApiObject();
		this.c = selection.getConnection();
		try {
			this.VIPDiskName = vm.getSystemVDI(c).getUuid(c);
			System.out.println("虚拟机" + vm.getNameLabel(c) + "的系统硬盘分区的名字是 = " + VIPDiskName);
		} catch (BadServerResponse e) {
			e.printStackTrace();
		} catch (XenAPIException e) {
			e.printStackTrace();
		} catch (XmlRpcException e) {
			e.printStackTrace();
		}
		getDisks();
	}
	
	@Override
	public void addPages()
	{
		vm = (VM)selection.getApiObject();
		addVMDiskWizardPage = new AddVMDiskWizardPage(diskList,selection);
		addPage(addVMDiskWizardPage);
		
	}
	class VMJob extends Job
	{
		Display display;
		TreeViewer viewer;
		long diskSize = 0;
		String deleteDiskName = null;
		public VMJob(Display display, TreeViewer viewer, long diskSize, String deleteDiskName) {
			super("正在管理虚拟机挂载的磁盘");
			this.display = display;
			this.viewer = viewer;
			this.diskSize = diskSize;
			this.deleteDiskName = deleteDiskName;
		}
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("正在进行虚拟机磁盘管理操作...", 100);
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
//				ArrayList<String> diskNameList = new ArrayList<String>();
//				for(Disk disk : diskList){
//					diskNameList.add(disk.getNameLabel());
//				}
				if(deleteDiskName == null) {
					/*
					if(diskUuid == ""){
						if (!this.display.isDisposed()){
			      			 Runnable runnable = new Runnable(){
			      				 public void run(){
			      					MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
			      					messageBox.setText("警告");
			      					messageBox.setMessage("硬盘名称为空，操作已经被终止!");
			      					messageBox.open();
			      				 }
			      			 };
			      			 this.display.syncExec(runnable); 
			      		 }
					} else */
					if (diskList.size() > 5) {
						if (!this.display.isDisposed()){
			      			 Runnable runnable = new Runnable(){
			      				 public void run(){
			      					MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
			      					messageBox.setText("警告");
			      					messageBox.setMessage("硬盘数量已经超过4个，操作已经被终止!");
			      					messageBox.open();
			      				 }
			      			 };
			      			 this.display.syncExec(runnable); 
			      		 }
					} else {
						String diskUuid = UUID.randomUUID().toString();
						VDI vdi = VDI.createDataDisk(selection.getConnection(), diskUuid, diskSize);
						if(vdi != null){
							VM.createDataVBD(selection.getConnection(), selection.getUuid(), diskUuid);
						} else {
							System.out.println("VDI创建失败……");
						}
					}
				} else {
					System.out.println("要删除的硬盘名字是" + deleteDiskName);
					if (deleteDiskName.equals(VIPDiskName)){
						if (!this.display.isDisposed()){
			      			 Runnable runnable = new Runnable(){
			      				 public void run(){
			      					MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
			      					messageBox.setText("警告");
			      					messageBox.setMessage("正在尝试删除系统分区，删除后虚拟机无法运行，操作已经被终止!");
			      					messageBox.open();
			      				 }
			      			 };
			      			 this.display.syncExec(runnable); 
			      		 }
					} else {
						VM.deleteDataVBD(selection.getConnection(), selection.getUuid(), deleteDiskName);
						VDI.deleteDataDisk(selection.getConnection(), deleteDiskName);
					}
				}
				System.out.println("处理之后有" + VDI.getByVM(selection.getConnection(), vm).size() + "个硬盘");
				System.out.println("********************************");
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
		long newDiskSize = Math.round(addVMDiskWizardPage.getNewDiskSize());
		String deleteDiskName = addVMDiskWizardPage.getDeleteDiskName();
		final VMEvent event=new VMEvent();
		event.setDatetime(new Date());
		event.setDescription("为虚拟机 '" + selection.getName() + "'添加硬盘");
		event.setTarget(selection);
		event.setTask("");
		event.setType(eventType.info);
		event.setUser(selection.getRoot().getUsername());
		event.setImage(ImageRegistry.getImage(ImageRegistry.INFO));
		selection.events.add(event);
		Constants.logView.logFresh(event);

		VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay(),Constants.treeView.getViewer(),newDiskSize,deleteDiskName);
//		job.schedule();
		String msgInfo = "确定要为虚拟机" + selection.getName() + "增加容量为" + newDiskSize + "GB的硬盘？";
		if (deleteDiskName != null) {
			msgInfo = "确定要删除虚拟机" + selection.getName() + "上的硬盘" + deleteDiskName + "?";
		}
		QuestionDialog dialog = new QuestionDialog(Display.getCurrent().getActiveShell(),msgInfo,job);
		dialog.open();
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
