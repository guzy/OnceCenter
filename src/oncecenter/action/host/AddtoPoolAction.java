package oncecenter.action.host;

import java.util.Date;
import java.util.Set;

import oncecenter.Constants;
import oncecenter.util.AddServerUtil;
import oncecenter.util.ImageRegistry;
import oncecenter.util.dialog.ErrorMessageDialog;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.Host;
import com.once.xenapi.Pool;

public class AddtoPoolAction extends Action {
	VMTreeObjectHost hostObject;
	VMTreeObjectPool poolObject;
	Connection connection;
	Host host;

	public AddtoPoolAction(VMTreeObjectHost hostObject,VMTreeObjectPool poolObject){
		super();
		this.hostObject=hostObject;
		this.poolObject=poolObject;
		connection=hostObject.getConnection();
		host = (Host) hostObject.getApiObject();
		setText(poolObject.getName());		
	}
	
	class HostJob extends Job
	{
		VMTreeView viewer;
		Display display;
		
		public HostJob(Display display, VMTreeView viewer) {
			super("加入池");
			this.display = display;
			this.viewer = viewer;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("正在加入到池中...", 100);
			unableObject(hostObject);
			unableObject(poolObject);
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	viewer.getViewer().refresh();
			        }
			    };
			    this.display.asyncExec(runnable); 
			}
			try {
				Set<Host> hosts = Host.getAll(poolObject.getConnection());
				//主机已经在池中
				if(hosts.contains(host))
				{
					enableObject(hostObject);
					enableObject(poolObject);
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
			} catch (Exception e) {
				e.printStackTrace();
				enableObject(hostObject);
				enableObject(poolObject);
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
			if(hostObject.getRecordTimer!=null)
        		hostObject.getRecordTimer.cancel();
        	if(hostObject.getPerformTimer!=null)
        		hostObject.getPerformTimer.cancel();
			try {
				Pool.join(connection, poolObject.getIpAddress(), poolObject.getUsername(), poolObject.getPassword());
			} catch (final Exception e) {
				
				e.printStackTrace();
				enableObject(hostObject);
				enableObject(poolObject);
				if (!this.display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
				        	if(e.toString().equals("SR_CONFLICT"))
				        	{
				        		Image image = ImageRegistry.getImage(ImageRegistry.ADDTOPOOLERROR);
					        	ErrorMessageDialog dialog = new ErrorMessageDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(),"主机添加到资源池失败!  \n主机间sr冲突 ",image);
								dialog.open();
				        	}
				        	viewer.getViewer().refresh();
				        }
				    };
				    this.display.asyncExec(runnable); 
				}
				monitor.done();
				return Status.CANCEL_STATUS;
			}    
//			if (!this.display.isDisposed()){
//			    Runnable runnable = new Runnable(){
//			        public void run(){
//			        	//将主机加入到池中
//						Constants.addHosttoPool(hostObject,poolObject);
//			        }
//			    };
//			    this.display.asyncExec(runnable); 
//			}
//			if (!this.display.isDisposed()){
//			    Runnable runnable = new Runnable(){
//			        public void run(){
//			        	enableObject(poolObject);
//						viewer.getViewer().refresh();
//			        }
//			    };
//			    this.display.asyncExec(runnable); 
//			}
			final VMTreeObjectRoot newPoolObject = AddServerUtil.ConnectByIp(poolObject.getIpAddress(), poolObject.getUsername(), poolObject.getPassword());
			 if(newPoolObject!=null){
		        	Constants.CONNECTIONS_TREE.addChild(newPoolObject);
			        Display display=PlatformUI.getWorkbench().getDisplay();
					if (!display.isDisposed()){
					    Runnable runnable = new Runnable(){
					        public void run( ){
					        	Constants.CONNECTIONS_TREE.getChildrenList().remove(hostObject);
					        	Constants.CONNECTIONS_TREE.getChildrenList().remove(poolObject);
								viewer.getViewer().remove(hostObject);
								viewer.getViewer().remove(poolObject);
					        	Constants.treeView.getViewer().refresh();
					        	Constants.treeView.getViewer().expandAll();
					        }
						};
					    display.syncExec(runnable); 
					}
		       }
			monitor.done();
			return Status.OK_STATUS;
		}
	}
	
	public void unableObject(VMTreeObject host){
		host.setItemState(ItemState.changing);
		for(VMTreeObject child : host.getChildren()){
			unableObject(child);
		}
	}
	
	public void enableObject(VMTreeObject host){
		host.setItemState(ItemState.able);
		for(VMTreeObject child : host.getChildren()){
			enableObject(child);
		}
	}
	
	public void run(){
		if(poolObject==null||hostObject==null)
			return;
		for(VMTreeObject o :poolObject.getChildren()){
			if(o.getName().equals(hostObject.getName())){
				MessageDialog.openError(new Shell(), "提示", "目标资源池中有同名主机，请对主机进行重命名");
				new RenameHostAction(hostObject,true).run();
				return;
			}
		}
		try{
			VMEvent event = new VMEvent();
			event.setDatetime(new Date());
			event.setDescription(hostObject.getName() + "加入到池" + poolObject.getName() + "中。");
			event.setTarget(hostObject);
			event.setTask("");
			event.setType(eventType.info);
			event.setImage(ImageRegistry.getImage(ImageRegistry.INFO));
			hostObject.events.add(event);
			poolObject.events.add(event);
			
			Constants.logView.logFresh(event);
			VMTreeView viewer = Constants.treeView;
			HostJob job=new HostJob(PlatformUI.getWorkbench().getDisplay(),viewer);
			job.schedule();
		}
		catch(Exception e)
		{
			
			e.printStackTrace();
		}
	}
}
