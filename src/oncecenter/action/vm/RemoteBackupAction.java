package oncecenter.action.vm;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import oncecenter.Constants;
import oncecenter.util.CommonUtil;
import oncecenter.util.ConfigUtil;
import oncecenter.util.ImageRegistry;
import oncecenter.util.TypeUtil;
import oncecenter.util.VMUtil;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
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
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.XenAPIException;

public class RemoteBackupAction extends Action {
	
	VMTreeObjectVM vmObject;
	List<VMTreeObjectVM> vmList;
	VMTreeObjectSR srObject;
	VMTreeObjectPool poolObject;
	Connection connection;
	
	public RemoteBackupAction(VMTreeObjectVM vmObject,VMTreeObjectSR srObject){
		super();
		setText(srObject.getName());	
		this.vmObject=vmObject;
		this.srObject=srObject;
		this.connection=vmObject.getConnection();
	}
	
	public RemoteBackupAction(List<VMTreeObjectVM> vmList,VMTreeObjectSR srObject){
		super();
		setText(srObject.getName());	
		this.vmList = vmList;
		this.srObject=srObject;
		this.connection=vmList.get(0).getConnection();
	}
	
	class VMJob extends Job{
		Display display;
		VMTreeView viewer;
		VM.Record record;
		VMTreeObjectVM backupVM;
		String name;
		long vcpu;
		long memory;
		long storage;
		VM newVm;
		VDI sourceVdi;
		//int index=0;
		VM.Record sourceRecord;
		public VMJob(Display display,VMTreeView viewer){
			super("异地备份虚拟机");
			this.display=display;
			this.viewer=viewer;
			storage = 10;
			//name = vmObject.getName()+"_"+CommonUtil.getCurrentTime();
		}
//		public void getIndex(VMTreeObject root){
//			for(VMTreeObject o :root.getChildrenList()){
//				String name = o.getName();
//				if(!name.equals(this.name)&&name.indexOf(this.name)==0){
//					try{
//						int i = Integer.parseInt(name.substring(this.name.length()));
//						if(index<i){
//							index=i;
//						}
//					}catch(Exception e){
//						e.printStackTrace();
//						continue;
//					}
//					
//				}
//				getIndex(o);
//			}
//		}
		@Override 
	    protected IStatus run(IProgressMonitor monitor) { 
	        monitor.beginTask("异地备份 ...", 100); 
	        for(VMTreeObjectVM vmObject:vmList){
	        	vmObject.setItemState(ItemState.changing);
	        }
	        if (!this.display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						viewer.getViewer().refresh();
					}
				};
				this.display.syncExec(runnable);
			}
	        int step = 100/vmList.size();
	        int i = 0;
	        for(VMTreeObjectVM vmObject:vmList){
	        	monitor.worked(step*i++);
	        	name = vmObject.getName()+"_"+CommonUtil.getCurrentTime();
	        	if(vmObject.getRecord() == null||vmObject.getRecord() != null && vmObject.getRecord().isLocalVM){
	        		vmObject.setItemState(ItemState.able);
	        		continue;
	        	}
		        poolObject=(VMTreeObjectPool)vmObject.getParent();
		        
		        if(vmObject.getVdi()==null){
					try{
						vmObject.setVdi(VDI.getByVM(connection, (VM)vmObject.getApiObject()).iterator().next());
					}catch(Exception e){
						e.printStackTrace();
						vmObject.setItemState(ItemState.able);
				        if (!this.display.isDisposed()) {
							Runnable runnable = new Runnable() {
								public void run() {
									viewer.getViewer().refresh();
								}
							};
							this.display.syncExec(runnable);
						}
						continue;
					}
				}
				sourceVdi = vmObject.getVdi();
				if(sourceVdi ==null){
					vmObject.setItemState(ItemState.able);
			        if (!this.display.isDisposed()) {
						Runnable runnable = new Runnable() {
							public void run() {
								viewer.getViewer().refresh();
							}
						};
						this.display.syncExec(runnable);
					}
					continue;
				}
				sourceRecord = vmObject.getRecord();
				if(sourceRecord!=null){
					vcpu = sourceRecord.VCPUsMax;
					memory = sourceRecord.memoryStaticMax/1024/1024;
					vmObject.setRecord(sourceRecord);
				}else{
					vcpu = 1;
					memory = 1024*1024*1024;
				}
				
				
//				index = 0;
//		        getIndex(poolObject);
//		        //if(index>0){
//		        	name = name +(index+1);
//		       // }
		        backupVM = new VMTreeObjectVM(name,connection
						,null,null);
		        backupVM.setItemState(ItemState.changing);
		        poolObject.temporaryList.add(backupVM);
		        poolObject.addChild(backupVM);
				if (!this.display.isDisposed()) {
					Runnable runnable = new Runnable() {
						public void run() {
							viewer.getViewer().refresh();
						}
					};
					this.display.syncExec(runnable);
				}
					
				newVm = VMUtil.createVmVIF(name, vcpu, memory, newVm, connection, sourceRecord.residentOn);
				if(newVm==null){
					backupVM.getParent().getChildrenList().remove(backupVM);
					vmObject.setItemState(ItemState.able);
					if (!this.display.isDisposed()) {
						Runnable runnable = new Runnable() {
							public void run() {
								viewer.getViewer().remove(backupVM);
								viewer.getViewer().refresh();
							}
						};
						this.display.syncExec(runnable);
					}
					continue;
				}
				try{
					VDI.Record vdiRec1 = new VDI.Record();
					vdiRec1.otherConfig = new HashMap<String, String>();
					vdiRec1.otherConfig.put("virtual_machine", name);
					vdiRec1.otherConfig.put("vm_uuid", newVm.getUuid(connection));
					vdiRec1.virtualSize = storage;
					vdiRec1.uuid = UUID.randomUUID().toString();
					vdiRec1.type = Types.toVdiType("user");
					vdiRec1.sharable = true;
					vdiRec1.nameLabel = name;
					vdiRec1.SR = (SR)srObject.getApiObject();
					String sr_type = srObject.getSrType();
					if (sr_type.equals(TypeUtil.nfsZfsType))
						vdiRec1.location = "file:"+Constants.srpath+"/"+vdiRec1.SR.getUuid(connection)+"/"+vdiRec1.uuid+"/disk.vhd";
					else if (sr_type.equals(TypeUtil.gpfsDiskType))
						vdiRec1.location = "file:"+vdiRec1.SR.getLocation(connection)+"/"+vdiRec1.uuid+"/disk.vhd";
					else if (sr_type.equals(TypeUtil.mfsDiskType)||sr_type.equals(TypeUtil.ocfs2DiskType))
						vdiRec1.location = "tap2:aio:"+vdiRec1.SR.getLocation(connection)+"/"+vdiRec1.uuid+"/disk.vhd";
					else
						vdiRec1.location = "file:"+Constants.srpath+"/"+vdiRec1.SR.getUuid(connection)+"/"+vdiRec1.uuid+".vhd";
					VDI vdi1 = VDI.createOn(connection, vdiRec1, sourceRecord.residentOn);
					
					sourceVdi.backup(connection, vdiRec1.uuid
							, ((SR)vmObject.getStorageObject().getApiObject()).getUuid(connection)
							, vdiRec1.SR.getUuid(connection));
					
					VBD.Record vbdRec = new VBD.Record();
					vbdRec.VM = newVm;
					vbdRec.VDI = vdi1;
					vbdRec.bootable = true;
					vbdRec.device = "hda";
					vbdRec.mode = Types.toVbdMode("rw");
					vbdRec.type = Types.toVbdType("Disk");
					VBD.createOn(connection, vbdRec,sourceRecord.residentOn);
					
					record = newVm.getRecord(connection);
				}catch(Exception e){
					e.printStackTrace();
					try {
						newVm.destroy(connection, true);
					} catch (Exception e1) {
						
						e1.printStackTrace();
					}
					backupVM.getParent().getChildrenList().remove(backupVM);
					vmObject.setItemState(ItemState.able);
					if (!this.display.isDisposed()) {
						Runnable runnable = new Runnable() {
							public void run() {
								viewer.getViewer().remove(backupVM);
								viewer.getViewer().refresh();
							}
						};
						this.display.syncExec(runnable);
					}
					continue;
				}
				backupVM.setApiObject(newVm);
				backupVM.setRecord(record);
				poolObject.vmMap.put(newVm, backupVM);
				backupVM.setItemState(ItemState.able);
				vmObject.setItemState(ItemState.able);
				if (!this.display.isDisposed()) {
					Runnable runnable = new Runnable() {
						public void run() {
							viewer.getViewer().refresh();
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
		if(vmList==null){
			vmList = new ArrayList<VMTreeObjectVM>();
			vmList.add(vmObject);
		}
		final VMEvent event=new VMEvent();
		event.setDatetime(new Date());
		event.setDescription("远程备份虚拟机 '" + vmObject.getName() + "'");
		event.setTarget(vmObject);
		event.setTask("");
		event.setType(eventType.info);
		event.setUser(vmObject.getRoot().getUsername());
		event.setImage(ImageRegistry.getImage(ImageRegistry.INFO));
		vmObject.events.add(event);
		Constants.logView.logFresh(event);

		VMTreeView viewer = Constants.treeView;
		VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay(),viewer);
		Constants.jobs.add(job);
		job.schedule();
	}
}
