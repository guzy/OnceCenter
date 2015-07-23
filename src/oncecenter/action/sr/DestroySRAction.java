package oncecenter.action.sr;

import java.util.ArrayList;
import java.util.List;

import oncecenter.Constants;
import oncecenter.util.TypeUtil;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.SR;
import com.once.xenapi.Types;
import com.once.xenapi.VM;

public class DestroySRAction extends Action {

	VMTreeObjectSR selection;
	Connection conn;
	SR sr;
	
	public DestroySRAction(VMTreeObjectSR selection){
		super();
		this.selection=selection;
		this.conn=selection.getConnection();
		this.sr = (SR)selection.getApiObject();
		setText("É¾³ý");		
	}
	
	class VMJob extends Job{
		VMTreeView viewer;
		Display display;
		List<VMTreeObjectVM> relateVmList = new ArrayList<VMTreeObjectVM>();
		public VMJob(Display display,VMTreeView viewer){
			super("É¾³ýsr");
			this.viewer=viewer;
			this.display=display;
		}
		@Override 
	    protected IStatus run(IProgressMonitor monitor) { 
	        monitor.beginTask("É¾³ýsr...", 100); 
	        selection.setItemState(ItemState.changing);
    		if (!this.display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						viewer.getViewer().expandAll();
						viewer.getViewer().refresh();	
					}
				};
				this.display.syncExec(runnable);
			}
			
//			try {
////				for(VM vm:VM.getAll(conn)){
////					VM.Record record = vm.getRecord(conn);
////					if(record.suspendSR.equals(sr)
////							&&record.powerState.equals(Types.VmPowerState.RUNNING)){
////						//¼ÇÂ¼ÏÂÀ´
////					}
////				}
//				//±ä³É±¾µØ½øÐÐÅÐ¶Ï
				
    			boolean alarm = hasVM(selection);
    			//alarm = true;
    			if(alarm){
    				if (!this.display.isDisposed()) {
    					Runnable runnable = new Runnable() {
    						public void run() {
    							AlarmDialog alarmDialog = new AlarmDialog(new Shell(),relateVmList);
    							alarmDialog.open();
    							handleCancle();
    						}
    					};
    					this.display.asyncExec(runnable);
    				}
    				monitor.done(); 
    				Constants.jobs.remove(this);
    				return Status.CANCEL_STATUS;  
    			}
				
				try{
					sr.umount(conn);
				}catch(Exception e){
					e.printStackTrace();
					try{
						sr.destroy(conn);
					}catch(Exception e1){
						e1.printStackTrace();
						handleCancle();
						Display display=PlatformUI.getWorkbench().getDisplay();
						if (!display.isDisposed()){
						    Runnable runnable = new Runnable(){
						        public void run( ){
						        	MessageDialog.openError(new Shell(), "¾¯¸æ","É¾³ýÊ§°Ü£¡");
						        }
							};
						    display.syncExec(runnable); 
						}
						monitor.done(); 
						Constants.jobs.remove(this);
						return Status.CANCEL_STATUS;
					}
					
				}
				try{
					sr.destroy(conn);
				}catch(Exception e1){
					e1.printStackTrace();
					handleCancle();
					Display display=PlatformUI.getWorkbench().getDisplay();
					if (!display.isDisposed()){
					    Runnable runnable = new Runnable(){
					        public void run( ){
					        	MessageDialog.openError(new Shell(), "¾¯¸æ","É¾³ýÊ§°Ü£¡");
					        }
						};
					    display.syncExec(runnable); 
					}
					monitor.done(); 
					Constants.jobs.remove(this);
					return Status.CANCEL_STATUS;
				}
//			} catch (Exception e1) {
//				
//				e1.printStackTrace();
//			}
			selection.getParent().getChildrenList().remove(selection);
			if (!this.display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						viewer.getViewer().remove(selection);
						viewer.getViewer().refresh();	
					}
				};
				this.display.syncExec(runnable);
			}
			monitor.done(); 
			Constants.jobs.remove(this);
			return Status.OK_STATUS;  
		}
		
		public void handleCancle(){
			selection.setItemState(ItemState.able);
			if (!this.display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						viewer.getViewer().refresh();	
					}
				};
				this.display.syncExec(runnable);
			}
		}
		public boolean hasVM(VMTreeObjectSR sr){
			boolean has = false;
			//boolean isZfs = sr.getSrType().equals(TypeUtil.zfsSrType)?true:false;
			for(VMTreeObject o:sr.getParent().getChildren()){
				if(addtoList(o,sr))
					has = true;
				for(VMTreeObject o1:o.getChildren()){
					if(addtoList(o1,sr))
						has = true;
				}
			}
			return has;
		}
		
		public boolean addtoList(VMTreeObject o,VMTreeObjectSR sr){
			if(o instanceof VMTreeObjectVM){
				VMTreeObjectVM vm = (VMTreeObjectVM)o;
				if(TypeUtil.getDiskSRTypes().contains(sr.getSrType())){
					VM.Record vmRecord = (VM.Record)vm.getRecord();
					if(vmRecord!=null){
						if(vmRecord.connectedDiskSRs.contains((SR)sr.getApiObject())){
							relateVmList.add(vm);
							return true;
						}
					}
				}else if(sr.getSrType().contains(TypeUtil.isoSign)){
					VM.Record vmRecord = (VM.Record)vm.getRecord();
					if(vmRecord!=null){
						if(vmRecord.connectedIsoSRs.contains((SR)sr.getApiObject())){
							relateVmList.add(vm);
							return true;
						}
					}
				}else{
					relateVmList.add(vm);
					return true;
				}
			}
			return false;
		}
	}
	public void run(){
		try {
			VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay(),Constants.treeView);
			Constants.jobs.add(job);
			job.schedule();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
}
