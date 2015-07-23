package oncecenter.p2v.wizard.fromlinux;

import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;
import oncecenter.wizard.newvmfromtemp.CpuMemoryWizardPage;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import oncecenter.Constants;
import oncecenter.util.ConfigUtil;
import oncecenter.util.Ssh;
import oncecenter.util.TypeUtil;

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

public class P2vFromLinuxWizard extends Wizard {

	P2vFromLinuxSourcePage spage;
	P2vFromLinuxDestinationPage fpage;

	P2vFromLinuxVMPage namePage;
	CpuMemoryWizardPage cpuPage;

	private String sAddress;
	private String sAddressUser;
	private String sAddressPas;

	private VMTreeObjectPool poolObject;
	private VMTreeObjectSR srObject;
	private Connection connection;

	private String vmName;
	private String vmDescription;
	private int vcpu;
	private long memory;

	private VMTreeObjectVM newVMObject;
	private VM newVM;
	public long storage;

	public P2vFromLinuxWizard() {
		setWindowTitle("物理机转化为虚拟机（Linux系统）");
	}
	
	/**
	 * Override the parent class method control the finish button's status
	 */
	public boolean canFinish() {
		IWizardPage page = getContainer().getCurrentPage();
		if ((page instanceof CpuMemoryWizardPage))
		{
			return true;
		}
		else
			return false;
	}

	@Override
	public void addPages() {
		spage = new P2vFromLinuxSourcePage("source page");
		fpage = new P2vFromLinuxDestinationPage("finish page");
		namePage = new P2vFromLinuxVMPage("name page");
		cpuPage = new CpuMemoryWizardPage();
		this.addPage(spage);
		this.addPage(fpage);
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

	public void getConfig() {
		sAddress = spage.getsAddressText().getText().trim();
		sAddressUser = spage.getsAddressUserText().getText().trim();
		sAddressPas = spage.getsAddressPasText().getText().trim();

		poolObject = fpage.getSelectedPool();
		srObject = fpage.getSelectedSR();

		connection = poolObject.getConnection();
		
		vmName = namePage.getName();
		vmDescription = namePage.getDescription();
		
		vcpu = cpuPage.getCPUNum();
		memory = cpuPage.getMemoryValue();
		
		System.out.println("vcpu:"+vcpu+"memory:"+memory);
	}

	class VMJob extends Job {
		Display display;
		VMTreeView viewer;
		VM.Record record;
		String mountDir;
		String dLocation;
		String dFileName;

		public VMJob(Display display, VMTreeView viewer) {
			super("从" + sAddress + "导入虚拟机");
			this.display = display;
			this.viewer = viewer;
			this.mountDir = Constants.p2vDir;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("从" + sAddress + "导入虚拟机...", 100);

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

			Ssh ssh = new Ssh(sAddress, sAddressUser, sAddressPas);
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

			if (!mount(ssh)) {
				monitor.done();
				Constants.jobs.remove(this);
				return Status.CANCEL_STATUS;
			}
			
			String dev = getDevName(ssh);

			StringBuffer cmdBuffer = new StringBuffer("dd if=/dev/");
			cmdBuffer.append(dev);
			cmdBuffer.append(" of=");

			cmdBuffer.append(mountDir);
			if (mountDir != null && !mountDir.endsWith("/"))
				cmdBuffer.append("/");
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
			
			ssh.CloseSsh();
			monitor.done();
			Constants.jobs.remove(this);
			return Status.OK_STATUS;
		}

		public boolean mkMountDir(Ssh ssh){
			String ret = "";
			try {		
				ret = ssh.Command("mkdir "+mountDir);
				System.out.println("mkdir "+mountDir);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			} finally {
				//ssh.CloseSsh();
			}
		}
		
		public boolean mount(Ssh ssh){
			if(!mkMountDir(ssh)){
				return false;
			}
			String ret = "";
			StringBuffer cmdBuffer = new StringBuffer("mount ");
			cmdBuffer.append(dLocation);
			cmdBuffer.append(" ");
			cmdBuffer.append(mountDir);
			try {		
				ret = ssh.Command(cmdBuffer.toString());
				System.out.println(cmdBuffer.toString());
				if(ssh.getExitCode() != 0) {
					System.out.println("挂载失败，请检查目录是否存在和NFS服务是否开启！");
					return false;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			} finally {
				//ssh.CloseSsh();
			}
			return true;
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

		private String getDevName(Ssh ssh) {
			String dev = null;
			String ret = null;
			try {
				ret = ssh.Command("ls /dev/");
				for (String item : ret.split("\n")) {
					if (item.endsWith("da")) {
						dev = item;
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				//ssh.CloseSsh();
			}
			return dev;
		}

		private Long getStorage(Ssh ssh) {
//			Long val = (long) 0;
//			String ret = null;
//			try {
//				ret = ssh.Command("fdisk -l");
//				String inform = ret.split(" ")[2];
//				String[] values = inform.split("\\.");
//
//				val = Long.parseLong(values[0]);
//				Long dec = Long.parseLong(values[1]);
//				if (dec > 0)
//					val += 1;
//
//			} catch (ArrayIndexOutOfBoundsException e) {
//				val = (long) 0;
//				e.printStackTrace();
//			} catch (NumberFormatException e) {
//				val = (long) 0;
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			} catch (Exception e) {
//				e.printStackTrace();
//			} finally {
//				//ssh.CloseSsh();
//			}
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

}
