package oncecenter.p2v.wizard.fromwindows;

import java.util.HashMap;
import java.util.UUID;

import oncecenter.Constants;
import oncecenter.p2v.wizard.fromlinux.P2vFromLinuxDestinationPage;
import oncecenter.p2v.wizard.fromlinux.P2vFromLinuxVMPage;
import oncecenter.util.ConfigUtil;
import oncecenter.util.Ssh;
import oncecenter.util.TypeUtil;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;
import oncecenter.wizard.newvmfromtemp.CpuMemoryWizardPage;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.Console;
import com.once.xenapi.Network;
import com.once.xenapi.SR;
import com.once.xenapi.Types;
import com.once.xenapi.VBD;
import com.once.xenapi.VDI;
import com.once.xenapi.VIF;
import com.once.xenapi.VM;

public class P2vFromWindowsWizard extends Wizard {

	P2vFromLinuxDestinationPage srPage;
	P2vFromWindowsFileSelectWizardPage filePage;
	
	private VMTreeObjectPool selectedPool;
	private VMTreeObjectSR selectedSR;
	
	VMTreeObjectVM newVMObject;
	VM newVM;
	
	private VMTreeObjectPool poolObject;
	private VMTreeObjectSR srObject;
	private Connection connection;
	private P2vFromLinuxVMPage namePage;
	private CpuMemoryWizardPage cpuPage;
	private String vmName;
	private String vmDescription;
	private int vcpu;
	private long memory;
	
	long storage;
	
	String fileName;
	
	public P2vFromWindowsWizard() {
		setWindowTitle("物理机转化为虚拟机（Windows系统）");
	}
	
	@Override
	public boolean canFinish() {
		IWizardPage page = getContainer().getCurrentPage();
		if ((page instanceof CpuMemoryWizardPage) && ((CpuMemoryWizardPage) page).canFinish())
		{
			if(((CpuMemoryWizardPage) page).canFinish())
				System.out.println("fhaskfh: true");
			else
				System.out.println("fhaskfh: false");
			return true;
		}
		else
			return false;
	}

	@Override
	public void addPages() {
		srPage = new P2vFromLinuxDestinationPage("destination page");
		filePage = new P2vFromWindowsFileSelectWizardPage("file selection page");
		namePage = new P2vFromLinuxVMPage("name page");
		cpuPage = new CpuMemoryWizardPage();
		this.addPage(srPage);
		this.addPage(filePage);
		this.addPage(namePage);
		this.addPage(cpuPage);
	}
	
	@Override
	public boolean performFinish() {
		getConfig();
		VMTreeView viewer = Constants.treeView;
		VMJob job = new VMJob(PlatformUI.getWorkbench().getDisplay(), viewer);
		Constants.jobs.add(job);
		job.schedule();
		return true;
	}

	public void getConfig(){
		poolObject = srPage.getSelectedPool();
		srObject = srPage.getSelectedSR();

		connection = poolObject.getConnection();
		
		fileName = filePage.fileName;
		
		vmName = namePage.getName();
		vmDescription = namePage.getDescription();
		
		vcpu = cpuPage.getCPUNum();
		memory = cpuPage.getMemoryValue();
		
	}
	
	class VMJob extends Job {
		Display display;
		VMTreeView viewer;
		VM.Record record;
		String dLocation;
		String dFileName;

		public VMJob(Display display, VMTreeView viewer) {
			super("导入虚拟机");
			this.display = display;
			this.viewer = viewer;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("导入虚拟机...", 100);

			newVMObject = new VMTreeObjectVM(vmName, connection, null, null);
			newVMObject.setItemState(ItemState.changing);
			poolObject.temporaryList.add(newVMObject);
			poolObject.addChild(newVMObject);
			if (!this.display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						viewer.getViewer().refresh();
					}
				};
				this.display.syncExec(runnable);
			}

			Ssh ssh = new Ssh(filePage.srIp, filePage.srUsername, filePage.srPassword);
			if (!ssh.Connect()) {
				System.err.println("author or password isn't right!");
				handleError();
				monitor.done();
				Constants.jobs.remove(this);
				return Status.CANCEL_STATUS;
			}
			
			storage = getStorage(ssh); 
			
			if (!createVM()) {
				monitor.done();
				Constants.jobs.remove(this);
				return Status.CANCEL_STATUS;
			}

			StringBuffer cmdBuffer = new StringBuffer("qemu-img convert ");
			cmdBuffer.append(Constants.p2vwindowsvhdPath+"/"+fileName);
			cmdBuffer.append(" ");

			cmdBuffer.append(dLocation.substring(dLocation.indexOf(":")+1)+"/");
			cmdBuffer.append(dFileName);
			System.out.println(cmdBuffer.toString());

			try {
				ssh.Command(cmdBuffer.toString());
			} catch (Exception e) {
				
				e.printStackTrace();
			}

			newVMObject.setApiObject(newVM);
			newVMObject.setRecord(record);
			poolObject.vmMap.put(newVM, newVMObject);
			newVMObject.setItemState(ItemState.able);

			if (!this.display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						viewer.getViewer().refresh();
					}
				};
				this.display.syncExec(runnable);
			}
			
			String cmd = "rm -f "+Constants.p2vwindowsvhdPath+"/"+fileName;
			try {
				ssh.Command(cmd);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			
			ssh.CloseSsh();
			monitor.done();
			Constants.jobs.remove(this);
			return Status.OK_STATUS;
		}
		
		public boolean createVM() {
			try {
				record = new VM.Record();
				record.VCPUsParams = new HashMap<String, String>();
				record.nameLabel = vmName;
				record.nameDescription = vmDescription;
				record.HVMBootPolicy = "hvm";
				record.VCPUsMax = (long) vcpu;
				record.VCPUsAtStartup = (long) vcpu;
				record.memoryStaticMax = (long) memory*1204*1024;
				record.memoryDynamicMax = (long) memory*1204*1024;
				record.memoryDynamicMin = (long) memory*1204*1024;
				record.memoryStaticMin = (long) 0;
				record.actionsAfterCrash = Types.toOnCrashBehaviour("restart");
				record.actionsAfterReboot = Types.toOnNormalExit("restart");
				record.actionsAfterShutdown = Types.toOnNormalExit("destroy");
				record.platform = new HashMap<String, String>();
				record.platform.put("pae", "1");
				record.platform.put("boot", "cd");
				record.platform.put("localtime", "0");
				record.platform.put("acpi", "1");
				record.platform.put("usbdevice", "tablet");

				VM.createAsync(connection, record);
				newVM = VM.getByNameLabel(connection, vmName).iterator().next();

				Console.Record consoleRec = new Console.Record();
				consoleRec.protocol = Types.toConsoleProtocol("rfb");
				consoleRec.VM = newVM;
				consoleRec.otherConfig = new HashMap<String, String>();
				consoleRec.otherConfig.put("vnc", "1");
				consoleRec.otherConfig.put("sdl", "0");
				consoleRec.otherConfig.put("vncunused", "1");
				consoleRec.otherConfig.put("vnclisten", "0.0.0.0");
				Console.create(connection, consoleRec);

				VIF.Record vifRec = new VIF.Record();
				vifRec.VM = newVM;

				vifRec.network = Network
						.getByNameLabel(connection, ConfigUtil.getNetwork())
						.iterator().next();
				vifRec.MTU = (long) 1500;
				VIF.create(connection, vifRec);

				VDI.Record vdiRec1 = new VDI.Record();
				vdiRec1.otherConfig = new HashMap<String, String>();
				vdiRec1.otherConfig.put("virtual_machine", record.nameLabel);
				vdiRec1.otherConfig.put("vm_uuid", newVM.getUuid(connection));
				vdiRec1.virtualSize = storage;
				vdiRec1.uuid = UUID.randomUUID().toString();
				vdiRec1.type = Types.toVdiType("user");
				vdiRec1.sharable = true;
				vdiRec1.nameLabel = record.nameLabel;
				vdiRec1.SR = (SR) srObject.getApiObject();

				SR sr = (SR) srObject.getApiObject();
				
				String location = sr.getRecord(connection).otherConfig.get("location");
				
				String sr_type = srObject.getSrType();
				if (sr_type.equals(TypeUtil.nfsZfsType))
					vdiRec1.location = "file:" + Constants.srpath + "/"
							+ vdiRec1.SR.getUuid(connection) + "/"
							+ vdiRec1.uuid + "/disk.vhd";
				else
					vdiRec1.location = "file:" + Constants.srpath + "/"
							+ vdiRec1.SR.getUuid(connection) + "/"
							+ vdiRec1.uuid + ".vhd";
				VDI vdi1 = VDI.create(connection, vdiRec1);

				dLocation = location + "/"
						+ vdiRec1.SR.getUuid(connection) + "/" + vdiRec1.uuid;
				dFileName = "disk.vhd";

				VBD.Record vbdRec = new VBD.Record();
				vbdRec.VM = newVM;
				vbdRec.VDI = vdi1;
				vbdRec.bootable = true;
				vbdRec.device = "hda";
				vbdRec.mode = Types.toVbdMode("rw");
				vbdRec.type = Types.toVbdType("Disk");
				VBD.create(connection, vbdRec);

				record = newVM.getRecord(connection);

				return true;
			} catch (Exception e) {
				e.printStackTrace();
				handleError();
				return false;
			}
		}
		
		private Long getStorage(Ssh ssh) {
			return 10L;
		}

		
		public void handleError() {
			newVMObject.getParent().getChildrenList().remove(newVMObject);
			if (!this.display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						viewer.getViewer().remove(newVMObject);
						viewer.getViewer().refresh();
					}
				};
				this.display.syncExec(runnable);
			}
		}
	}
	
	
		
	public VMTreeObjectPool getSelectedPool() {
		return selectedPool;
	}

	public void setSelectedPool(VMTreeObjectPool selectedPool) {
		this.selectedPool = selectedPool;
	}

	public VMTreeObjectSR getSelectedSR() {
		return selectedSR;
	}

	public void setSelectedSR(VMTreeObjectSR selectedSR) {
		this.selectedSR = selectedSR;
	}

}
