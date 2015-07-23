package oncecenter.wizard.newvmfromtemp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import oncecenter.Constants;
import oncecenter.util.ConfigUtil;
import oncecenter.util.TypeUtil;
import oncecenter.util.VMUtil;
import oncecenter.util.VMUtil.DiskInfo;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectTemplate;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.Console;
import com.once.xenapi.Host;
import com.once.xenapi.Network;
import com.once.xenapi.SR;
import com.once.xenapi.Types;
import com.once.xenapi.VBD;
import com.once.xenapi.VDI;
import com.once.xenapi.VIF;
import com.once.xenapi.VM;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.XenAPIException;

public class NewVmFTWizard extends Wizard {

	VMTreeObject selection;
	Connection connection;
	
	SelectTemplateWizardPage templatePage;
	VmNameWizardPage namePage;
	SelectMediaWizardPage mediaPage;
	SelectServerWizardPage serverPage;
	CpuMemoryWizardPage cpuPage;
	SelectStorageWizardPage storagePage;
	FinishWizardPage finishPage;
	SelectNetworkWizardPage networkPage;
	
	public boolean isPool;
	public VMTreeObjectRoot root;
	
	public VMTreeObject selectedTemp;
	public VMTreeObject selectedHost;
	public VDI.Record selectedMedia;
	public Network selectedNet;
	public VMTreeObjectSR selectedSR;
	public ArrayList<VMTreeObjectTemplate> templates = new ArrayList<VMTreeObjectTemplate>();
	public ArrayList<VMTreeObjectHost> hosts = new ArrayList<VMTreeObjectHost>();
	public ArrayList<Network> nets = new ArrayList<Network>();
	public ArrayList<VDI.Record> medias = new ArrayList<VDI.Record>();
	public ArrayList<VMTreeObjectSR> srs = new ArrayList<VMTreeObjectSR>();
	public ArrayList<DiskInfo> diskList = new ArrayList<DiskInfo>();
	
	public String vmName;
	public String vmDescription;
	public int vcpu;
	public int memory; 
	public long storage;
	public boolean isShare;
	public boolean isAssignHost;
	public String net;
	
	public String templateName;
	
	public NewVmFTWizard() {
//		setWindowTitle("新建虚拟机");
//		StructuredSelection select = (StructuredSelection)Constants.treeView.getViewer().getSelection();
//		selection = (VMTreeObject)select.getFirstElement();
//		Init();		
	}

	public NewVmFTWizard(VMTreeObject object) {
		setWindowTitle("新建虚拟机");
		selection = object;
		Init();
	}

	public void Init(){
		connection = selection.getConnection();
		if(selection instanceof VMTreeObjectPool){
			isPool = true;
			root = (VMTreeObjectRoot)selection;
		}else if(selection instanceof VMTreeObjectHost){
			selectedHost = selection;
			if(selection.getParent() instanceof VMTreeObjectPool){
				isPool = true;
				root = (VMTreeObjectRoot)selection.getParent();
			}else{
				isPool = false;
				root = (VMTreeObjectRoot)selection;
			}
		}
		else if(selection instanceof VMTreeObjectTemplate)
		{
			selectedTemp = selection;
			if(selection instanceof VMTreeObjectPool){
				isPool = true;
			}else{
				isPool = false;
			}
			root = (VMTreeObjectRoot)selection.getParent();
		}
		if(root.templateMap.values() != null)
			templates.addAll(root.templateMap.values());
		hosts.addAll(root.hostMap.values());
		srs.addAll(root.srMap.values());
		for(VMTreeObjectSR sr:srs){
			this.getMedias(sr);
		}
		
//		Connection conn = selection.getConnection();
//		try {
//			Iterator<Network> i = Network.getAll(conn).iterator();
//			while(i.hasNext()){
//				Network net = i.next();
//				if(net.getVIFs(conn).size()!=0){
//					nets.add(net);
//				}				
//			}
//		} catch (BadServerResponse e) {
//			
//			e.printStackTrace();
//		} catch (XenAPIException e) {
//			
//			e.printStackTrace();
//		} catch (XmlRpcException e) {
//			
//			e.printStackTrace();
//		}
	}
	
	public void getMedias(VMTreeObjectSR o){
		
		if(o.getSrType().contains(TypeUtil.isoSign)){
			SR sr = (SR)o.getApiObject();
			try {
				sr.update(o.getConnection());
				for(VDI vdi : sr.getVDIs(o.getConnection())){
					medias.add(vdi.getRecord(o.getConnection()));
				}
			} catch (BadServerResponse e) {
				
				e.printStackTrace();
			} catch (XenAPIException e) {
				
				e.printStackTrace();
			} catch (XmlRpcException e) {
				
				e.printStackTrace();
			}
		}
	}
	@Override
	public void createPageControls(Composite pageContainer) {
	// super.createPageControls(pageContainer);
	}

	@Override
	public void addPages() {
		templatePage = new SelectTemplateWizardPage(selection);
		addPage(templatePage);
		namePage = new VmNameWizardPage();
		addPage(namePage);
		mediaPage = new SelectMediaWizardPage(selection);
		addPage(mediaPage);
		serverPage = new SelectServerWizardPage(selection);
		addPage(serverPage);
		cpuPage = new CpuMemoryWizardPage();
		addPage(cpuPage);
		storagePage = new SelectStorageWizardPage();
		addPage(storagePage);
//		networkPage = new SelectNetworkWizardPage(selection);
//		addPage(networkPage);
		finishPage = new FinishWizardPage(selection);
		addPage(finishPage);

	}

	class VMJob extends Job {
		Display display;
		VM newVm;
		VM.Record record;
		VMTreeView viewer;
		VMTreeObjectVM newObject;
		public VMJob(Display display,VMTreeView viewer) {
			super("creating...");
			this.display=display;
			this.viewer = viewer;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("creating...", 100);
			
			newObject = new VMTreeObjectVM(vmName,connection
					,null,null);
			newObject.setItemState(ItemState.changing);
			if(selectedHost==null){
				selectedHost = Constants.getSuitableHost((VMTreeObjectPool)root);
			}
			if(root.temporaryList==null)
				root.temporaryList = new ArrayList<VMTreeObject>();
			root.temporaryList.add(newObject);
			selectedHost.addChild(newObject);
			if (!this.display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						viewer.getViewer().refresh();
					}
				};
				this.display.syncExec(runnable);
			}
			if(selectedMedia!=null){
				try{
					newVm = VMUtil.create(vmName, vcpu, memory, newVm, connection
							, (Host)selectedHost.getApiObject(), isShare, selectedMedia.uuid
							, diskList);
//					VM.Record record = new VM.Record();
//					record.VCPUsParams = new HashMap<String, String> ();
//					record.nameLabel = vmName;
//					record.HVMBootPolicy = "hvm";
//					record.VCPUsMax = (long) vcpu;
//					record.VCPUsAtStartup = (long) vcpu;
//					record.memoryStaticMax = (long) memory * 1024 * 1024;
//					record.memoryDynamicMax = (long) memory * 1024 * 1024;
//					record.memoryDynamicMin = (long) memory * 1024 * 1024;
//					record.memoryStaticMin =  0l;
//					record.actionsAfterCrash = Types.toOnCrashBehaviour("restart");
//					record.actionsAfterReboot = Types.toOnNormalExit("restart");
//					record.actionsAfterShutdown = Types.toOnNormalExit("destroy");
//					record.platform = new HashMap<String, String>();
//					record.platform.put("pae", "1");
//					record.platform.put("boot", "cd");
//					record.platform.put("localtime", "0");
//					record.platform.put("acpi", "1");
//					record.platform.put("usbdevice", "tablet");
//					record.platform.put("serial", "pty");
//					record.platform.put("usb", "1");
//					record.platform.put("parallel", "none");
//					record.platform.put("apic", "1");
//					
////					if (isShare)
////						VM.create(connection, record);
////					else
//					newVm = VM.createOn(connection, record, (Host)selectedHost.getApiObject());
//					
//					Console.Record consoleRec = new Console.Record();
//					consoleRec.protocol = Types.toConsoleProtocol("rfb");
//					consoleRec.VM = newVm;
//					consoleRec.otherConfig = new HashMap<String, String>();
//					consoleRec.otherConfig.put("vnc", "1");
//					consoleRec.otherConfig.put("sdl", "0");
//					consoleRec.otherConfig.put("vncunused", "1");
//					consoleRec.otherConfig.put("vnclisten", "0.0.0.0");
////					if (isShare)
////						Console.create(connection, consoleRec);
////					else
//						Console.createOn(connection, consoleRec, (Host)selectedHost.getApiObject());
//
//
//					VIF.Record vifRec = new VIF.Record();
//					vifRec.VM = newVm;
//					
//					vifRec.network = Network.getByNameLabel(connection, ConfigUtil.getNetwork()).iterator().next();
//					vifRec.MTU = (long) 1500;
////					if (isShare)
////						VIF.create(connection, vifRec);
////					else
//						VIF.createOn(connection, vifRec, (Host)selectedHost.getApiObject());
//
//					VDI.Record vdiRec1 = new VDI.Record();
//					vdiRec1.otherConfig = new HashMap<String, String>();
//					vdiRec1.otherConfig.put("virtual_machine", record.nameLabel);
//					vdiRec1.otherConfig.put("vm_uuid", newVm.getUuid(connection));
//					vdiRec1.virtualSize = storage;
//					vdiRec1.uuid = UUID.randomUUID().toString();
//					vdiRec1.type = Types.toVdiType("user");
//					if (isShare)
//						vdiRec1.sharable = true;
//					else
//						vdiRec1.sharable = false;
//					vdiRec1.nameLabel = record.nameLabel;
//					vdiRec1.SR = (SR)selectedSR.getApiObject();
////					vdiRec1.location = "file:/home/share/"+vdiRec1.SR.getUuid(c)+"/"+vdiRec1.uuid+".vhd";
//					SR sr = (SR)selectedSR.getApiObject();
////					String location = sr.getOtherConfig(selectedSR.getConnection()).get("location");
//					String sr_type = selectedSR.getSrType();
//					if (sr_type.equals(TypeUtil.zfsSrType))
//						vdiRec1.location = "file:"+Constants.srpath+"/"+vdiRec1.SR.getUuid(connection)+"/"+vdiRec1.uuid+"/disk.vhd";
//					else if (sr_type.equals("local"))
//						vdiRec1.location = "file:"+Constants.localsr+"/"+vdiRec1.uuid+".vhd";
//					else
//						vdiRec1.location = "file:"+Constants.srpath+"/"+vdiRec1.SR.getUuid(connection)+"/"+vdiRec1.uuid+".vhd";
//					VDI vdi1;
////					if (isShare)
////						vdi1 = VDI.create(connection, vdiRec1);
////					else
//					vdi1 = VDI.createOn(connection, vdiRec1, (Host)selectedHost.getApiObject());
//					
//					VBD.Record vbdRec = new VBD.Record();
//					vbdRec.VM = newVm;
//					vbdRec.VDI = vdi1;
//					vbdRec.bootable = true;
//					vbdRec.device = "hda";
//					vbdRec.mode = Types.toVbdMode("rw");
//					vbdRec.type = Types.toVbdType("Disk");
////					if (isShare)
////						VBD.create(connection, vbdRec);
////					else
//					VBD.createOn(connection, vbdRec, (Host)selectedHost.getApiObject());
//
//					VDI vdi2 = VDI.getByUuid(connection, selectedMedia.uuid);
//					VBD.Record cdrom = new VBD.Record();
//					cdrom.VM = newVm;
//					cdrom.VDI = vdi2;
//					cdrom.bootable = true;
//					cdrom.type = Types.toVbdType("CD");
//					cdrom.device = "hdc";
////					if (isShare)
////						VBD.create(connection, cdrom);
////					else
//						VBD.createOn(connection, cdrom, (Host)selectedHost.getApiObject());	
					
				}catch(Exception e){
					e.printStackTrace();
					Constants.jobs.remove(this);
					return handleException(monitor);
				}
				//启动虚拟机
				try
				{
					Host h = (Host)selectedHost.getApiObject();
					if(newVm != null)
						newVm.startOn(connection, h, false, true);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				try
				{
					//slkdflsklf
					record = newVm.getRecord(connection);
					newObject.setApiObject(newVm);
					newObject.setRecord(record);
					root.vmMap.put(newVm, newObject);
					newObject.setItemState(ItemState.able);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					Constants.jobs.remove(this);
					return handleException(monitor);
				}
				
			}
			else if(selectedTemp!=null){
				//根据模板创建，待实现
				VM.Record newRecord;
				VM template = (VM)selectedTemp.getApiObject();
				try {
					newVm = template.createClone(connection, vmName);
					VMUtil.AdjustCpuMemory(newVm, vcpu, memory, connection);
//					//设置vcpu和内存
//					Long vcpuNumber = template.getVCPUsMax(connection);
//					if(vcpuNumber > (long)vcpu)
//					{
//						//从大变小
//						template.setVCPUsNumberLive(connection, new Long(vcpu));
//						template.setVCPUsMax(connection, new Long(vcpu));
//						template.setVCPUsAtStartup(connection, new Long(vcpu));
//					}
//					else
//					{
//						//从小变大
//						template.setVCPUsMax(connection, new Long(vcpu));
//						template.setVCPUsNumberLive(connection, new Long(vcpu));
//						template.setVCPUsAtStartup(connection, new Long(vcpu));
//					}
//					Long memorySize = template.getMemoryStaticMax(connection);
//					if(memorySize > new Long((long)memory * 1024 * 1024 ))
//					{
//						//从大变小
//						template.setMemoryDynamicMax(connection, new Long((long)memory * 1024 * 1024));
//						template.setMemoryDynamicMin(connection, new Long((long)memory * 1024 * 1024));
//						template.setMemoryStaticMax(connection, new Long((long)memory * 1024 * 1024 ));
//					}
//					else if(memorySize < new Long((long)memory * 1024 * 1024 ))
//					{
//						//从小变大
//						template.setMemoryStaticMax(connection, new Long((long)memory * 1024 * 1024 ));
//						template.setMemoryDynamicMax(connection, new Long((long)memory * 1024 * 1024));
//						template.setMemoryDynamicMin(connection, new Long((long)memory * 1024 * 1024));
//					}
					//设置存储？
				} catch (Exception e) {
					
					e.printStackTrace();
					Constants.jobs.remove(this);
					return handleException(monitor);
				}
				
				root.vmMap.put(newVm, newObject);
				newObject.setApiObject(newVm);
				//启动虚拟机
				try
				{
					Host h = (Host)selectedHost.getApiObject();
					newVm.startOn(connection, h, false, false);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
				try
				{
					newRecord = newVm.getRecord(connection);
					newObject.setRecord(newRecord);
	        		newObject.setItemState(ItemState.able);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					Constants.jobs.remove(this);
					return handleException(monitor);
				}
			}else{
				Constants.jobs.remove(this);
				return handleException(monitor);
			}
			if (!this.display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						viewer.getViewer().refresh();  
					}
				};
				this.display.syncExec(runnable);
			}

			monitor.done();
			Constants.jobs.remove(this);
			return Status.OK_STATUS;
		}

		private IStatus handleException(IProgressMonitor monitor) {
			newObject.getParent().getChildrenList().remove(newObject);
			if (!this.display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						viewer.getViewer().remove(newObject);
						viewer.getViewer().refresh();
					}
				};
				this.display.syncExec(runnable);
			}
			monitor.done();
			return Status.CANCEL_STATUS;
		}
	}

	/**
	 * Override the parent class method
	 * control the finish button's status
	 */
	public boolean canFinish()
	{ 
		IWizardPage page = getContainer().getCurrentPage();
		if( page instanceof FinishWizardPage)
			return true;
		else return false;
	}
	
	
	@Override
	public boolean performFinish() {
//		Connection connection = selection.getConnection();
//		VM template = templatePage.getSelectedVM();
//		String name = newVMpage.getName();
//		String descrip = newVMpage.getDescription();
//		Host host = serverPage.getHost();
//		int cpuNum = cpuPage.getCPUNum();
//		//long memoryValue = cpuPage.getMemoryValue();
//		SR sr = storagePage.getSR();
//		

		SR sr = (SR)selectedSR.getApiObject();
		String srType = selectedSR.getSrType();
		DiskInfo diskInfo = new DiskInfo(storage,sr,srType);
		diskList.add(diskInfo);
		VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay(),Constants.treeView);
		job.schedule();
		Constants.jobs.add(job);
		return true;
	}

}
