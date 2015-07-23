package oncecenter.wizard.newserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import oncecenter.Constants;
import oncecenter.action.addlostelement.CompareRootAction;
import oncecenter.daemon.GetPerformTask;
import oncecenter.daemon.GetRecordTask;
import oncecenter.util.AddServerUtil;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectDefault;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Pool;

public class NewServerWizard extends Wizard {

	String serverIp="";
	NewServerWizardPage mainpage;
	public VMTreeObjectRoot newObject;
	Pool thisPool;
	List<VMTreeObject> haltedVMs=new ArrayList<VMTreeObject>();
	List<VMTreeObject> templates=new ArrayList<VMTreeObject>();
	
	String ip;
	String username;
	String password;
	
	VMTreeObjectRoot previousRoot;
	
	VMTreeObjectDefault xenObject;
	
	public NewServerWizard() {
		setWindowTitle("连接主机");
	}

	public NewServerWizard(String serverIp){
		setWindowTitle("重新连接资源池");
		this.serverIp=serverIp;
	}
	
	@Override
	public void addPages() {
		if(serverIp==null||serverIp.length()==0){
			mainpage = new NewServerWizardPage("Add New Server");
		}else{
			mainpage = new NewServerWizardPage("Add New Server",serverIp,"");
		}
    	
    	addPage(mainpage);
    }

	@Override
	public boolean performFinish() {
		ip = mainpage.getIp().getText();
		username = mainpage.getUsername().getText();
		password = mainpage.getPassword().getText();
		boolean isExist=false;
		for(VMTreeObject o:Constants.CONNECTIONS_TREE.getChildrenList()){
			VMTreeObjectRoot r = (VMTreeObjectRoot)o;
			if(r.getItemState().equals(ItemState.able)&&r.getIpAddress().equals(ip)){
				isExist=true;
			}
		}
		if(isExist){
			Display display=PlatformUI.getWorkbench().getDisplay();
			if (!display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run( ){
			        	MessageDialog.openError(getShell(), "登录失败","不要重复连接");
			        }
				};
			    display.syncExec(runnable); 
			}
			return false;
		}
		xenObject=Constants.CONNECTIONS_TREE;
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell()); 
		try {
			dialog.run(true, true, new IRunnableWithProgress(){ 
			    public void run(IProgressMonitor monitor) { 
			        monitor.beginTask("connecting...",  IProgressMonitor.UNKNOWN); 
			        newObject = AddServerUtil.ConnectByIp(ip, username, password);
			        if(newObject!=null){
			        	for(VMTreeObject o:xenObject.getChildrenList()){
							if(o.getItemState().equals(ItemState.unable)&&o instanceof VMTreeObjectRoot){
								if(((VMTreeObjectRoot)o).getIpAddress().equals(ip)){
									previousRoot  = (VMTreeObjectRoot)o;
									break;
								}
								
							}
						}
			        	
			        	CompareRootAction action = new CompareRootAction(previousRoot,newObject);
						action.run();

				        Display display=PlatformUI.getWorkbench().getDisplay();
						if (!display.isDisposed()){
						    Runnable runnable = new Runnable(){
						        public void run( ){
						        	if(previousRoot!=null){
						        		xenObject.getChildrenList().remove(previousRoot);
										Constants.treeView.getViewer().remove(previousRoot);
						        	}
						        	ISelection s1 = new StructuredSelection(new Object[]{newObject});
		    						Constants.treeView.getViewer().setSelection(s1);	
						        	Constants.treeView.getViewer().refresh();
						        }
							};
						    display.syncExec(runnable); 
						}
			        }
			        monitor.done(); 
			    } 
			});
			
		} catch (Exception e1) {
			
			e1.printStackTrace();
		}		
		
		
//		try {
//			if(Constant.permission.equals(Constant.Permission.admin)){
//				PlatformUI.getWorkbench().showPerspective("oncecenter.perspective"
//						,  PlatformUI.getWorkbench().getActiveWorkbenchWindow());
//			}else{
//				PlatformUI.getWorkbench().showPerspective("oncecenter.UserPerspective"
//						,  PlatformUI.getWorkbench().getActiveWorkbenchWindow());
//			}
//		} catch (WorkbenchException e) {
//			
//			e.printStackTrace();
//		}
		
		
		
		
//		if(Constant.permission.equals(Constant.Permission.admin)){
//			viewer.getViewer().refresh();
//			viewer.getViewer().setExpandedState(selection,true);
//			viewer.getViewer().setExpandedState(template,true);
//			try {
//				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
//						VMTreeView.ID);
//				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(
//						appViewer);
//			} catch (PartInitException e) {
//				
//				e.printStackTrace();
//			}
//		}else if(Constant.permission.equals(Constant.Permission.user)){
//			appViewer.getViewer().refresh();
//			appViewer.getViewer().setExpandedState(selection,true);
//			try {
//				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
//						AppTreeView.ID);
//				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(
//						viewer);
//			} catch (PartInitException e) {
//				
//				e.printStackTrace();
//			}
//		}
		if(newObject!=null){
			newObject.getRecordTimer = new Timer("recordTimer");
			newObject.getRecordTimer.schedule(new GetRecordTask(newObject), 3000,5000);
			if(Constants.displayStatusData){
				newObject.getPerformTimer = new Timer("performTimer");
				newObject.getPerformTimer.schedule(new GetPerformTask(newObject), 0,15000);
			}
		}
		return true;
	}

}
