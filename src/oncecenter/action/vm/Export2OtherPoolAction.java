package oncecenter.action.vm;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import oncecenter.Constants;
import oncecenter.util.ConfigUtil;
import oncecenter.util.ImageRegistry;
import oncecenter.util.Ssh;
import oncecenter.util.TypeUtil;
import oncecenter.util.VMUtil;
import oncecenter.util.decryptPassword.Decrypt;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
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

public class Export2OtherPoolAction extends Action {

	VMTreeObjectVM sourceVMObject;
	VMTreeObjectSR destSRObject;
	VMTreeObjectPool destPoolObject;
	VMTreeObjectPool sourcePoolObject;
	
	Connection sourceConn;
	Connection destConn;
	
	String sourceVDILocation;
	String destVDILocation;
	
	String sourceSrIP;
	String destSrIP;
	
	String sourceVDIFileName;
	String destVDIFileName;
	
	String sourceSRUser;
	String sourceSRPass;
	
	public Export2OtherPoolAction(VMTreeObjectVM vm,VMTreeObjectSR sr){
		super();
		this.sourceVMObject = vm;
		sourcePoolObject = (VMTreeObjectPool)sourceVMObject.getParent();
		this.destSRObject = sr;
		this.destPoolObject = (VMTreeObjectPool)sr.getParent();
		this.sourceConn = vm.getConnection();
		this.destConn = sr.getConnection();
		sourceVDIFileName = destVDIFileName = "disk.vhd";
		setText(sr.getName());		
	}
	
	class VMJob extends Job{
		Display display;
		VMTreeView viewer;
		VM.Record record;
		VMTreeObjectVM newVMObject;
		String name;
		long vcpu;
		long memory;
		long storage;
		VM newVM;
		VDI sourceVdi;
		SR sourceSR;
		String sourceSrType;
		String destSrType;
		
		public VMJob(Display display,VMTreeView viewer){
			super("export");
			this.display=display;
			this.viewer=viewer;
			name = sourceVMObject.getName();
			storage = 10;
		}
		@Override 
	    protected IStatus run(IProgressMonitor monitor) { 
	        monitor.beginTask("export ...", 100); 
	        sourceVMObject.setItemState(ItemState.changing);
	        if (!this.display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						viewer.getViewer().refresh();
					}
				};
				this.display.syncExec(runnable);
			}
	        try{
				if(sourceVMObject.getVdi()==null){
					sourceVMObject.setVdi(VDI.getByVM(sourceConn, (VM)sourceVMObject.getApiObject()).iterator().next());
				}
				sourceVdi = sourceVMObject.getVdi();
				sourceSR = sourceVdi.getSR(sourceConn);
				
				if(sourceVdi ==null || sourceSR==null){
					handleError();
					monitor.done();
					Constants.jobs.remove(this);
			        return Status.CANCEL_STATUS;
				}
				sourceSrType = sourceSR.getType(sourceVMObject.getConnection());
				if(!sourceSrType.contains(TypeUtil.nfsSign)){
					Map<String, String> otherconfig = sourceSR.getRecord(sourceConn).otherConfig;
					String location = otherconfig.get("location");
					sourceSRUser = otherconfig.get("username");
					sourceSRPass = Decrypt.getString(otherconfig.get("password"));
					sourceSrIP = location.substring(0,location.indexOf(":"));
					sourceVDILocation = location.substring(location.indexOf(":")+1);
					sourceVDILocation = sourceVDILocation + "/"
							+ sourceSR.toWireString() + "/" + sourceVdi.toWireString();
				}else{
					sourceVDILocation = sourceSR.getLocation(sourceConn)+"/" + sourceVdi.toWireString();
				}
				
				VM.Record record = ((VM)sourceVMObject.getApiObject()).getRecord(sourceConn);
				if(record!=null){
					vcpu = record.VCPUsMax;
					memory = record.memoryStaticMax/1024/1024;
					sourceVMObject.setRecord(record);
				}else{
					vcpu = 1;
					memory = 1024*1024*1024;
				}
			}catch(Exception e){
				e.printStackTrace();
				handleError();
				monitor.done();
				Constants.jobs.remove(this);
		        return Status.CANCEL_STATUS;
			}
	        
	        newVMObject = new VMTreeObjectVM(name,destConn
					,null,null);
	        newVMObject.setItemState(ItemState.changing);
	        destPoolObject.temporaryList.add(newVMObject);
	        destPoolObject.addChild(newVMObject);
			if (!this.display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						viewer.getViewer().refresh();
					}
				};
				this.display.syncExec(runnable);
			}
			
			if(!createVM()){
				handleError();
				monitor.done();
				Constants.jobs.remove(this);
		        return Status.CANCEL_STATUS;
			}

			if(!copyVDI()){
				handleError();
				monitor.done();
				Constants.jobs.remove(this);
		        return Status.CANCEL_STATUS;
			}
			newVMObject.setApiObject(newVM);
			newVMObject.setRecord(record);
			destPoolObject.vmMap.put(newVM, newVMObject);
			newVMObject.setItemState(ItemState.able);
			sourceVMObject.setItemState(ItemState.able);
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
		
		public boolean createVM() {
			VMTreeObjectHost hostObject = null;
			for(VMTreeObject o:destPoolObject.getChildren()){
				if(o instanceof VMTreeObjectHost){
					hostObject = (VMTreeObjectHost)o;
					break;
				}
			}
			newVM = VMUtil.createVmVIF(name, vcpu, memory, newVM, destConn, (Host)hostObject.getApiObject());
			try {
//				record = new VM.Record();
//				record.VCPUsParams = new HashMap<String, String>();
//				record.nameLabel = name;
//				record.nameDescription = "";
//				record.HVMBootPolicy = "hvm";
//				record.VCPUsMax = (long) vcpu;
//				record.VCPUsAtStartup = (long) vcpu;
//				record.memoryStaticMax = (long) memory*1204*1024;
//				record.memoryDynamicMax = (long) memory*1204*1024;
//				record.memoryDynamicMin = (long) memory*1204*1024;
//				record.memoryStaticMin = (long) 0;
//				record.actionsAfterCrash = Types.toOnCrashBehaviour("restart");
//				record.actionsAfterReboot = Types.toOnNormalExit("restart");
//				record.actionsAfterShutdown = Types.toOnNormalExit("destroy");
//				record.platform = new HashMap<String, String>();
//				record.platform.put("pae", "1");
//				record.platform.put("boot", "cd");
//				record.platform.put("localtime", "0");
//				record.platform.put("acpi", "1");
//				record.platform.put("usbdevice", "tablet");
//
//				VM.createAsync(destConn, record);
//				newVM = VM.getByNameLabel(destConn, name).iterator().next();
//
//				Console.Record consoleRec = new Console.Record();
//				consoleRec.protocol = Types.toConsoleProtocol("rfb");
//				consoleRec.VM = newVM;
//				consoleRec.otherConfig = new HashMap<String, String>();
//				consoleRec.otherConfig.put("vnc", "1");
//				consoleRec.otherConfig.put("sdl", "0");
//				consoleRec.otherConfig.put("vncunused", "1");
//				consoleRec.otherConfig.put("vnclisten", "0.0.0.0");
//				Console.create(destConn, consoleRec);
//
//				VIF.Record vifRec = new VIF.Record();
//				vifRec.VM = newVM;
//
//				vifRec.network = Network
//						.getByNameLabel(destConn, ConfigUtil.getNetwork())
//						.iterator().next();
//				vifRec.MTU = (long) 1500;
//				VIF.create(destConn, vifRec);

				VM.Record record = newVM.getRecord(destConn);
				
				VDI.Record vdiRec1 = new VDI.Record();
				vdiRec1.otherConfig = new HashMap<String, String>();
				vdiRec1.otherConfig.put("virtual_machine", record.nameLabel);
				vdiRec1.otherConfig.put("vm_uuid", newVM.getUuid(destConn));
				vdiRec1.virtualSize = storage;
				vdiRec1.uuid = UUID.randomUUID().toString();
				vdiRec1.type = Types.toVdiType("user");
				vdiRec1.sharable = true;
				vdiRec1.nameLabel = record.nameLabel;
				vdiRec1.SR = (SR) destSRObject.getApiObject();

				SR sr = vdiRec1.SR;
				
				String location = sr.getRecord(destConn).otherConfig.get("location");
				
				String sr_type = destSRObject.getSrType();
				if (sr_type.equals(TypeUtil.nfsZfsType))
					vdiRec1.location = "file:"+Constants.srpath+"/"+vdiRec1.SR.getUuid(destConn)+"/"+vdiRec1.uuid+"/disk.vhd";
				else if (sr_type.equals(TypeUtil.gpfsDiskType))
					vdiRec1.location = "file:"+vdiRec1.SR.getLocation(destConn)+"/"+vdiRec1.uuid+"/disk.vhd";
				else if (sr_type.equals(TypeUtil.mfsDiskType)||sr_type.equals(TypeUtil.ocfs2DiskType))
					vdiRec1.location = "tap2:aio:"+vdiRec1.SR.getLocation(destConn)+"/"+vdiRec1.uuid+"/disk.vhd";
				else
					vdiRec1.location = "file:"+Constants.srpath+"/"+vdiRec1.SR.getUuid(destConn)+"/"+vdiRec1.uuid+".vhd";
				VDI vdi1 = VDI.create(destConn, vdiRec1);

				destSrType = sr_type;
				if(!sr_type.equals(TypeUtil.nfsZfsType)){
					destVDILocation = vdiRec1.SR.getLocation(destConn)+"/" + vdiRec1.uuid;
				}else{
					destSrIP = location.substring(0,location.indexOf(":"));
					destVDILocation = location.substring(location.indexOf(":")+1);
					destVDILocation = destVDILocation + "/"
							+ sr.toWireString() + "/" + vdiRec1.uuid;
				}
				
				VBD.Record vbdRec = new VBD.Record();
				vbdRec.VM = newVM;
				vbdRec.VDI = vdi1;
				vbdRec.bootable = true;
				vbdRec.device = "hda";
				vbdRec.mode = Types.toVbdMode("rw");
				vbdRec.type = Types.toVbdType("Disk");
				VBD.create(destConn, vbdRec);

				record = newVM.getRecord(destConn);

				return true;
			} catch (Exception e) {
				e.printStackTrace();
				handleError();
				return false;
			}
		}
		
		public boolean copyVDI(){	
			Ssh ssh = null;
			String command ="";
			if(!sourceSrType.contains(TypeUtil.nfsSign)&&!destSrType.contains(TypeUtil.nfsSign)){
				ssh = new Ssh(destPoolObject.getIpAddress(), destPoolObject.getUsername(), destPoolObject.getPassword());
				if (!ssh.Connect()) {
					return false;
				}
				command += "cp -f ";
				command += sourceVDILocation + "/" + sourceVDIFileName + " ";
				command += destVDILocation + "/" + destVDIFileName;	
			}else if(sourceSrType.contains(TypeUtil.nfsSign)&&destSrType.contains(TypeUtil.nfsSign)){
				ssh = new Ssh(sourceSrIP, sourceSRUser, sourceSRPass);
				if (!ssh.Connect()) {
					return false;
				}
				if(sourceSrIP.equals(destSrIP)){
					command += "cp -f ";
					command += sourceVDILocation + "/" + sourceVDIFileName + " ";
					command += destVDILocation + "/" + destVDIFileName;				
				}else{
					if(!mountDir(ssh,destSrIP,destVDILocation)){
						return false;
					}
					command += "cp -f ";
					command += sourceVDILocation + "/" + sourceVDIFileName + " ";
					command += Constants.exportDir + "/" + destVDIFileName;		
				}	
			}else{
				if(sourceSrType.contains(TypeUtil.nfsSign)){
					ssh = new Ssh(destPoolObject.getIpAddress(), destPoolObject.getUsername(), destPoolObject.getPassword());
				}else{
					ssh = new Ssh(sourcePoolObject.getIpAddress(), sourcePoolObject.getUsername(), sourcePoolObject.getPassword());
				}
				if (!ssh.Connect()) {
					return false;
				}
				if(sourceSrType.contains(TypeUtil.nfsSign)){
					if(!mountDir(ssh,sourceSrIP,sourceVDILocation)){
						return false;
					}
					command += "cp -f ";
					command += Constants.exportDir + "/" + sourceVDIFileName + " ";
					command += destVDILocation + "/" + destVDIFileName;		
				}else{
					if(!mountDir(ssh,destSrIP,destVDILocation)){
						return false;
					}
					command += "cp -f ";
					command += sourceVDILocation + "/" + sourceVDIFileName + " ";		
					command += Constants.exportDir + "/" + destVDIFileName 	;
				}
			}
			try {
				ssh.Command(command);
				ummountDir(ssh);
			} catch (Exception e) {
				
				e.printStackTrace();
				return false;
			}finally{
				ssh.CloseSsh();
			}
			return true;
		}
		
		public boolean mountDir(Ssh ssh,String ip,String location){
			if(!ssh.MakeDir(Constants.exportDir)){
				return false;
			}
			StringBuffer cmdBuffer = new StringBuffer("mount ");
			cmdBuffer.append(ip);
			cmdBuffer.append(":");
			cmdBuffer.append(location);
			cmdBuffer.append(" ");
			cmdBuffer.append(Constants.exportDir);
			try {		
				ssh.Command(cmdBuffer.toString());
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
			} 
			return true;
		}
		
		public boolean ummountDir(Ssh ssh){
			if(!ssh.MakeDir(Constants.exportDir)){
				return false;
			}
			StringBuffer cmdBuffer = new StringBuffer("ummount ");
			cmdBuffer.append(Constants.exportDir);
			try {		
				ssh.Command(cmdBuffer.toString());
				if(ssh.getExitCode() != 0) {
					System.out.println("卸载失败");
					return false;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			} 
			return true;
		}
		public void handleError() {
			sourceVMObject.setItemState(ItemState.able);
			if (!this.display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						viewer.getViewer().refresh();
					}
				};
				this.display.syncExec(runnable);
			}
		}
	}
	public void run(){
		final VMEvent event=new VMEvent();
		event.setDatetime(new Date());
		event.setDescription("将虚拟机 '" + sourceVMObject.getName() + "'导出到资源池" + destPoolObject.getName());
		event.setTarget(sourceVMObject);
		event.setTask("");
		event.setType(eventType.info);
		event.setUser(sourceVMObject.getRoot().getUsername());
		event.setImage(ImageRegistry.getImage(ImageRegistry.INFO));
		sourceVMObject.events.add(event);
		Constants.logView.logFresh(event);

		VMTreeView viewer = Constants.treeView;
		VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay(),viewer);
		Constants.jobs.add(job);
		job.schedule();
	}
	
}
