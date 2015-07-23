package oncecenter.action.host;

import java.util.Date;

import oncecenter.Constants;
import oncecenter.util.ImageRegistry;
import oncecenter.util.dialog.QuestionDialog;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectTemplate;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.Host;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.XenAPIException;
import com.once.xenapi.VM;

public class ManageHAAction extends Action {

	
	VMTreeObjectHost selection;
	Host host;
	Connection connection;
	String messageStr = "开启HA功能";
	boolean isHAOpen;
	
	public ManageHAAction(){
		super();
		setText("开启/关闭HA功能");		
	}
	
	public ManageHAAction(VMTreeObjectHost selection){
		super();
		this.selection = selection;
		this.host = (Host)selection.getApiObject();
		this.connection = selection.getConnection();
		setText("开启/关闭HA功能");		
	}
	
	class VMJob extends Job{
		VMTreeView viewer;
		IViewReference reference;
		Display display;
		Host.Record  record;
		public VMJob(Display display,VMTreeView viewer){
			super("物理机" + selection.getName() + messageStr);
			this.viewer=viewer;
			this.display=display;
		}
		
		@Override 
	    protected IStatus run(IProgressMonitor monitor) { 
	        monitor.beginTask(messageStr, 100); 
	        try {
	        	try {
	    			isHAOpen = Host.getHA(connection, host);
	    			if (isHAOpen == true) {    		
	    				Host.setHA(connection, host, false);
	    			} else {
		        		Host.setHA(connection, host, true);
		        	}
	        	} catch (Exception e) {
	        		e.printStackTrace();
	        	}
			} catch (Exception e) {
				e.printStackTrace();
				monitor.done();
				Constants.jobs.remove(this);
				return Status.CANCEL_STATUS;
			}
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	
			        }
				};
			    this.display.syncExec(runnable); 
			}
			
	        monitor.done();
	        Constants.jobs.remove(this);
	        return Status.OK_STATUS; 
	    } 
	}
	
	public void run(){
		try {
			isHAOpen = Host.getHA(connection, host);
			if (isHAOpen == true) {    		
				messageStr = "关闭HA功能";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(selection==null){
			VMTreeView viewer = Constants.treeView;
			StructuredSelection select = (StructuredSelection)viewer.getViewer().getSelection();
			selection = (VMTreeObjectHost)select.getFirstElement();
		}
		
		try {
			String msgInfo = "确实要为物理机" + selection.getName() + messageStr + "吗?";
			final VMEvent event=new VMEvent();
			event.setDatetime(new Date());
			event.setDescription("开/关物理机 '" + selection.getName() + "'的HA功能");
			event.setTarget(selection);
			event.setTask("");
			event.setType(eventType.info);
			event.setUser(selection.getUsername());
			event.setImage(ImageRegistry.getImage(ImageRegistry.INFO));
			selection.events.add(event);
			Constants.logView.logFresh(event);

			VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay(),Constants.treeView);
			Constants.jobs.add(job);
			QuestionDialog dialog = new QuestionDialog(Display.getCurrent().getActiveShell(),msgInfo,job);
			dialog.open();	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
