package oncecenter.action.host;

import java.util.Timer;

import oncecenter.Constants;
import oncecenter.action.addlostelement.CompareRootAction;
import oncecenter.action.pool.RenamePoolAction;
import oncecenter.daemon.GetPerformTask;
import oncecenter.daemon.GetRecordTask;
import oncecenter.util.AddServerUtil;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectDefault;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Pool;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.XenAPIException;

public class ConnectServerAction extends Action {
	
	VMTreeObjectRoot selection;
	private String ip;
	private String username;
	private String password;
	
	VMTreeObjectRoot newObject;
	VMTreeObjectDefault xenObject;
	
	public ConnectServerAction(){
		super();
		setText("连接");
	}
	public ConnectServerAction(VMTreeObjectRoot selection){
		super();
		setText("连接");
		this.selection=selection;
	}
	
	public void enableObject(VMTreeObject host){
		host.setItemState(ItemState.able);
		for(VMTreeObject child : host.getChildren()){
			enableObject(child);
		}
	}
	
	public void run(){
		ip = selection.getIpAddress();
		username = selection.getUsername();
		password = selection.getPassword();
		//boolean flag = true;
		boolean isExist=false;
		
		for(VMTreeObject o:Constants.CONNECTIONS_TREE.getChildrenList()){
			VMTreeObjectRoot r = (VMTreeObjectRoot)o;
			if(r.getItemState().equals(ItemState.able)&&r.getIpAddress().equals(ip)){
				isExist=true;
			}
		}
		if(isExist){
			selection.getParent().getChildrenList().remove(selection);
			Display display=PlatformUI.getWorkbench().getDisplay();
			if (!display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run( ){
			        	MessageDialog.openError(new Shell(), "登录失败","不要重复连接");
			        	Constants.treeView.getViewer().remove(selection);
			        }
				};
			    display.syncExec(runnable); 
			}
			return;
		}
		xenObject = Constants.CONNECTIONS_TREE;
		
		final Display display = PlatformUI.getWorkbench().getDisplay();
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
		try {
			dialog.run(true, true, new IRunnableWithProgress(){ 
				public void run(IProgressMonitor monitor) { 
			        monitor.beginTask("正在连接...",  IProgressMonitor.UNKNOWN); 
					try{	
						newObject = AddServerUtil.ConnectByIp(ip, username, password);
//						if(selection instanceof VMTreeObjectPool){
							CompareRootAction action = new CompareRootAction(selection,newObject);
							action.run();
//						}else{
//							
//						}
						//xenObject.addChild(newObject);
						if(Constants.BackStack.size()>0){
							Constants.BackStack.pop();
						}
						if (!display.isDisposed()) {
		    				Runnable runnable = new Runnable() {
		    					public void run() {
		    						xenObject.getChildrenList().remove(selection);
		    						
		    						Constants.treeView.getViewer().remove(selection);
		    						Constants.treeView.getViewer().refresh();
		    						Constants.treeView.getViewer().expandAll();
		    						
		    						ISelection s1 = new StructuredSelection(new Object[]{newObject});
		    						Constants.treeView.getViewer().setSelection(s1);	
		    					}
		    				};
		    				display.asyncExec(runnable);
		    			}
						monitor.worked(3);
						if(monitor.isCanceled())
							return;
				} catch (Exception e) {
					e.printStackTrace();
					Display display=PlatformUI.getWorkbench().getDisplay();
					if (!display.isDisposed()){
					    Runnable runnable = new Runnable(){
					        public void run( ){
					        	MessageDialog.openError(new Shell(), "登录失败","登录失败");
					        }
						};
					    display.syncExec(runnable); 
					}
					monitor.done();
					return;
				}
					monitor.done();
			    } 
			});
			if(newObject!=null){
				newObject.getRecordTimer = new Timer("recordTimer");
				newObject.getRecordTimer.schedule(new GetRecordTask(newObject), 3000,5000);
				newObject.getPerformTimer = new Timer("performTimer");
				newObject.getPerformTimer.schedule(new GetPerformTask(newObject), 0,15000);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			Display display1 = PlatformUI.getWorkbench().getDisplay();
			if (!display1.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run( ){
			        	MessageDialog.openError(new Shell(), "登录失败","登录失败");
			        }
				};
			    display1.syncExec(runnable); 
			}
			return;
		}	
		if (!display.isDisposed()) {
			Runnable runnable = new Runnable() {
				public void run() {
					ISelection s1 = new StructuredSelection(new Object[]{newObject});
					Constants.treeView.getViewer().setSelection(s1);	
				}
			};
			display.asyncExec(runnable);
		}
		if(newObject!=null){
			for(VMTreeObject o :Constants.CONNECTIONS_TREE.getChildren()){
				if(!o.equals(newObject) 
						&&o.getItemState().equals(ItemState.able)
						&&o.getName().equals(newObject.getName())){
					//Display display=PlatformUI.getWorkbench().getDisplay();
					if (!display.isDisposed()){
					    Runnable runnable = new Runnable(){
					        public void run( ){
					        	MessageDialog.openInformation(new Shell(), "提示", "系统中有重名对象，请重新命名");
					        }
						};
					    display.syncExec(runnable); 
					}
					if(newObject instanceof VMTreeObjectHost){
						new RenameHostAction((VMTreeObjectHost)newObject,true).run();
					}else if(newObject instanceof VMTreeObjectPool){
						new RenamePoolAction((VMTreeObjectPool)newObject,true).run();
					}
					break;
				}
			}
		}
	}
}
