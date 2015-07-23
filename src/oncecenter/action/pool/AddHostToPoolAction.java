package oncecenter.action.pool;

import java.util.Date;
import java.util.Set;

import oncecenter.Constants;
import oncecenter.action.host.AddtoPoolAction;
import oncecenter.util.ImageRegistry;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
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
import com.once.xenapi.Host;
import com.once.xenapi.Pool;


public class AddHostToPoolAction extends Action {
	
	VMTreeObjectHost hostObject;
	VMTreeObjectPool poolObject;
	Connection connection;
	Host host;
	Pool pool;
	
	public AddHostToPoolAction(VMTreeObjectHost hostObject,VMTreeObjectPool poolObject){
		super();
		this.hostObject=hostObject;
		this.poolObject=poolObject;
		host = (Host)hostObject.getApiObject();
		pool = (Pool)poolObject.getApiObject();
		connection=hostObject.getConnection();
		setText(hostObject.getName());		
	}
	
//	class PoolJob extends Job
//	{
//
//		VMTreeView viewer;
//		Display display;
//		public PoolJob(VMTreeView viewer, Display display) {
//			super("加入池");
//			
//			this.display = display;
//			this.viewer = viewer;
//		}
//
//		@Override
//		protected IStatus run(IProgressMonitor monitor) {
//			
//			monitor.beginTask("正在加入到池中........", 100);
//			unableHost(hostObject);
//			if (!this.display.isDisposed()){
//			    Runnable runnable = new Runnable(){
//			        public void run(){
//			        	viewer.getViewer().refresh();
//			        }
//			    };
//			    this.display.syncExec(runnable); 
//			}
//			try {
//				Set<Host> hosts = Host.getAll(poolObject.getConnection());
//				//主机已经在池中
//				if(hosts.contains(host))
//				{
//					enableHost(hostObject);
//					if (!this.display.isDisposed()){
//					    Runnable runnable = new Runnable(){
//					        public void run(){
//					        	viewer.getViewer().refresh();
//					        }
//					    };
//					    this.display.syncExec(runnable); 
//					}
//					monitor.done();
//					return Status.CANCEL_STATUS;
//				}
//			} catch (Exception e) {
//				
//				e.printStackTrace();
//				enableHost(hostObject);
//				if (!this.display.isDisposed()){
//				    Runnable runnable = new Runnable(){
//				        public void run(){
//				        	viewer.getViewer().refresh();
//				        }
//				    };
//				    this.display.syncExec(runnable); 
//				}
//				monitor.done();
//				return Status.CANCEL_STATUS;
//			}
//			
//			try {
//				Pool.join(connection, poolObject.getIpAddress(), poolObject.getUsername(), poolObject.getPassword());
//			} catch (Exception e) {
//				
//				e.printStackTrace();
//				enableHost(hostObject);
//				if (!this.display.isDisposed()){
//				    Runnable runnable = new Runnable(){
//				        public void run(){
//				        	viewer.getViewer().refresh();
//				        }
//				    };
//				    this.display.syncExec(runnable); 
//				}
//				monitor.done();
//				return Status.CANCEL_STATUS;
//			}    
//			enableHost(hostObject);
//			if (!this.display.isDisposed()){
//			    Runnable runnable = new Runnable(){
//			        public void run(){
//			        	//将虚拟机加入到池中
//			        	if(hostObject.getRecordTimer!=null)
//			        		hostObject.getRecordTimer.cancel();
//			        	if(hostObject.getPerformTimer!=null)
//			        		hostObject.getPerformTimer.cancel();
//						Constants.addHosttoPool(hostObject,poolObject);
//			        	viewer.getViewer().refresh();
//			        }
//			    };
//			    this.display.syncExec(runnable); 
//			}
//			monitor.done();
//			return Status.OK_STATUS;
//		}
//		
//	}
//	
//	public void unableHost(VMTreeObjectHost host){
//		host.setItemState(ItemState.changing);
//		for(VMTreeObject child : host.getChildren()){
//			if(child instanceof VMTreeObjectVM)
//				child.setItemState(ItemState.changing);
//		}
//	}
//	
//	public void enableHost(VMTreeObjectHost host){
//		host.setItemState(ItemState.able);
//		for(VMTreeObject child : host.getChildren()){
//			if(child instanceof VMTreeObjectVM)
//				child.setItemState(ItemState.able);
//		}
//	}
	
	public void run(){
		
//		try{
			VMEvent event = new VMEvent();
			event.setDatetime(new Date());
			event.setDescription(hostObject.getName() + "加入到池" + poolObject.getName() + "中。");
			event.setTarget(poolObject);
			event.setTask("");
			event.setType(eventType.info);
			event.setImage(ImageRegistry.getImage(ImageRegistry.INFO));
			hostObject.events.add(event);
			poolObject.events.add(event);
			Constants.logView.logFresh(event);
			
			AddtoPoolAction action = new AddtoPoolAction(hostObject,poolObject);
			action.run();
			
//			VMTreeView viewer = Constants.treeView;
//			PoolJob job=new PoolJob(viewer, PlatformUI.getWorkbench().getDisplay());
//			job.schedule();
//		}
//		catch(Exception e)
//		{
//			
//			e.printStackTrace();
//		}
	}
}
