package oncecenter.action.host;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;

import oncecenter.Constants;
import oncecenter.util.ImageRegistry;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectTemplate;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.Host;
import com.once.xenapi.Pool;
import com.once.xenapi.SR;
import com.once.xenapi.Types;
import com.once.xenapi.VM;

public class EjectFromPoolAction extends Action {
	VMTreeObjectHost hostObject;
	Connection connection;
	VMTreeObjectPool poolObject;
	ArrayList<VMTreeObject> removeTreeObjectList = new ArrayList<VMTreeObject>();
	
	public EjectFromPoolAction(){
		super();
		
		setText("离开池");		
	}
	public EjectFromPoolAction(VMTreeObjectHost selection){
		super();
		this.hostObject=selection;
		setText("离开池");		
	}
	
	class HostJob extends Job
	{
		VMTreeView viewer;
		Display display;
		public HostJob(VMTreeView viewer, Display display) {
			super("离开池");
			this.display = display;
			this.viewer = viewer;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("正在离开池.......", 100);
			Host host = (Host)hostObject.getApiObject();
			//离开池操作
			try {
				Pool.eject(connection, host);
			} catch (Exception e) {
				e.printStackTrace();
				if (!this.display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
				        	viewer.getViewer().refresh();
				        }
				    };
				    this.display.asyncExec(runnable); 
				}
				monitor.done();
				return Status.CANCEL_STATUS;
			}
			
			if(hostObject.timerList !=null)
			{
				for(Timer timer:hostObject.timerList){
    				timer.cancel();
    			}	
	    	}
			
			removeTreeObjectList.add(hostObject);
			poolObject.hostMap.remove(host);
			ArrayList<VM> removeVMList = new ArrayList<VM>();
			for(VM vm:poolObject.vmMap.keySet()){
				VMTreeObjectVM vmObject = poolObject.vmMap.get(vm);
				VM.Record record = (VM.Record)vmObject.getRecord();
				if(record.residentOn.equals(host)){
					if(!record.powerState.equals(Types.VmPowerState.RUNNING)){
						removeTreeObjectList.add(vmObject);
					}
					removeVMList.add(vm);
				}
			}
			for(VM vm:removeVMList){
				poolObject.vmMap.remove(vm);
			}
			removeVMList.clear();
			
			ArrayList<VM> removeTempList = new ArrayList<VM>();
			for(VM template:poolObject.templateMap.keySet()){
				VMTreeObjectTemplate templateObject = poolObject.templateMap.get(template);
				VM.Record record = (VM.Record)templateObject.getRecord();
				if(record.residentOn.equals(host)){
					removeTreeObjectList.add(templateObject);
					removeTempList.add(template);
				}
			}
			for(VM temp:removeTempList){
				poolObject.templateMap.remove(temp);
			}
			removeTempList.clear();
			
			ArrayList<SR> removeSRList = new ArrayList<SR>();
			for(VMTreeObject object:hostObject.getChildrenList()){
				if(object instanceof VMTreeObjectSR){
					removeSRList.add((SR)object.getApiObject());
				}
			}
			for(SR sr:removeSRList){
				poolObject.srMap.remove(sr);
			}
			removeTempList.clear();
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	for(VMTreeObject deleteObject:removeTreeObjectList){
			        		deleteObject.getParent().getChildrenList().remove(deleteObject);
			        		Constants.treeView.getViewer().remove(deleteObject);
			        	}
			        	if(poolObject.hostMap.size()==0){
			        		if(poolObject.getRecordTimer!=null){
			        			poolObject.getRecordTimer.cancel();
			        		}
							if(poolObject.getPerformTimer!=null){
								poolObject.getPerformTimer.cancel();
							}
							poolObject.getParent().getChildrenList().remove(poolObject);
							Constants.treeView.getViewer().remove(poolObject);
						}
			        	viewer.getViewer().refresh();
			        }
			    };
			    this.display.asyncExec(runnable); 
			}
			monitor.done();
			return Status.OK_STATUS;
		}
		
	}
	public void run(){
		if(hostObject==null){
			StructuredSelection select = (StructuredSelection)Constants.treeView.getViewer().getSelection();
			hostObject = (VMTreeObjectHost)select.getFirstElement();	
		}
		
		if(!(hostObject.getParent() instanceof VMTreeObjectPool))
			return;
		poolObject = (VMTreeObjectPool)hostObject.getParent();
		connection=poolObject.getConnection();
		
		try{
			VMEvent event = new VMEvent();
			event.setDatetime(new Date());
			event.setDescription(hostObject.getName() + "离开池" + poolObject.getName() + "。");
			event.setTarget(hostObject);
			event.setTask("");
			event.setType(eventType.info);
			event.setImage(ImageRegistry.getImage(ImageRegistry.INFO));
			hostObject.events.add(event);
			poolObject.events.add(event);
			
			Constants.logView.logFresh(event);
			VMTreeView viewer = Constants.treeView;
			HostJob job=new HostJob(viewer,PlatformUI.getWorkbench().getDisplay());
			job.schedule();
		}
		catch(Exception e)
		{
			
			e.printStackTrace();
		}
	}
}
