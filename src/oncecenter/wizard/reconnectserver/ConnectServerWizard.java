//package oncecenter.wizard.reconnectserver;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Timer;
//
//import oncecenter.Constants;
//import oncecenter.action.addlostelement.CompareRootAction;
//import oncecenter.daemon.GetPerformTimer;
//import oncecenter.daemon.GetRecordTimer;
//import oncecenter.tree.VMTreeObject;
//import oncecenter.tree.VMTreeObjectDefault;
//import oncecenter.tree.VMTreeObjectHost;
//import oncecenter.tree.VMTreeObjectPool;
//import oncecenter.tree.VMTreeObjectRoot;
//import oncecenter.tree.VMTreeObject.ItemState;
//import oncecenter.util.AddServerUtil;
//
//import org.eclipse.core.runtime.IProgressMonitor;
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.jface.dialogs.ProgressMonitorDialog;
//import org.eclipse.jface.operation.IRunnableWithProgress;
//import org.eclipse.jface.viewers.ISelection;
//import org.eclipse.jface.viewers.StructuredSelection;
//import org.eclipse.jface.wizard.Wizard;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.ui.PlatformUI;
//
//public class ConnectServerWizard extends Wizard {
//
//	String ip;
//	String username;
//	String password;
//	ConnectServerWizardPage mainpage;
//	VMTreeObjectRoot selection;
//	private boolean flag = true;
//	VMTreeObjectRoot newObject;
//	
//	VMTreeObjectDefault xenObject;
//	
//	List<VMTreeObjectHost> lostHosts;
//	
//	public ConnectServerWizard(VMTreeObjectRoot selection) {
//		setWindowTitle("重新连接");
//		this.selection=selection;
//		lostHosts = new ArrayList<VMTreeObjectHost>();
//	}
//
//	@Override
//	public void addPages() {
//		mainpage = new ConnectServerWizardPage("重新连接",selection.getIpAddress(),selection.getUsername());
//    	addPage(mainpage);
//	}
//
//	@Override
//	public boolean performFinish() {
//		ip = mainpage.getIp().getText();
//		username = mainpage.getUsername().getText();
//		password = mainpage.getPassword().getText();
//		flag = true;
//		boolean isExist=false;
//		for(VMTreeObject o:Constants.CONNECTIONS_TREE.getChildrenList()){
//			VMTreeObjectRoot r = (VMTreeObjectRoot)o;
//			if(r.getItemState().equals(ItemState.able)&&r.getIpAddress().equals(ip)){
//				isExist=true;
//			}
//		} 
//		if(isExist){
//			selection.getParent().getChildrenList().remove(selection);
//			Display display=PlatformUI.getWorkbench().getDisplay();
//			if (!display.isDisposed()){
//			    Runnable runnable = new Runnable(){
//			        public void run( ){
//			        	MessageDialog.openError(getShell(), "登录失败","不要重复连接");
//			        	Constants.treeView.getViewer().remove(selection);
//			        }
//				};
//			    display.syncExec(runnable); 
//			}
//			return false;
//		}
//		xenObject = Constants.CONNECTIONS_TREE;
//		
//		final Display display = PlatformUI.getWorkbench().getDisplay();
//		ProgressMonitorDialog dialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
//		try {
//			dialog.run(true, true, new IRunnableWithProgress(){ 
//
//				public void run(IProgressMonitor monitor) { 
//			        monitor.beginTask("正在连接...",  IProgressMonitor.UNKNOWN); 
//					try{	
//						newObject = AddServerUtil.ConnectByIp(ip, username, password);
//						
//						if(selection instanceof VMTreeObjectPool){
//							CompareRootAction action = new CompareRootAction(selection,newObject);
//							action.run();
//						}
//						
//						//xenObject.addChild(newObject);
//						
//						if(Constants.BackStack.size()>0){
//							Constants.BackStack.pop();
//						}
//						if (!display.isDisposed()) {
//		    				Runnable runnable = new Runnable() {
//		    					public void run() {
//		    						xenObject.getChildrenList().remove(selection);
//		    						
//		    						Constants.treeView.getViewer().remove(selection);
//		    						Constants.treeView.getViewer().refresh();
//		    						Constants.treeView.getViewer().expandAll();
//		    						
//		    						ISelection s1 = new StructuredSelection(new Object[]{newObject});
//		    						Constants.treeView.getViewer().setSelection(s1);	
//		    					}
//		    				};
//		    				display.asyncExec(runnable);
//		    			}
//						
//						
//						
//						
//						monitor.worked(3);
//						if(monitor.isCanceled())
//							return;
//						
//						
//						
//						
//						
//				} catch (Exception e) {
//					
//					e.printStackTrace();
//					Display display=PlatformUI.getWorkbench().getDisplay();
//					if (!display.isDisposed()){
//					    Runnable runnable = new Runnable(){
//					        public void run( ){
//					        	MessageDialog.openError(getShell(), "登录失败","登录失败");
//					        }
//						};
//					    display.syncExec(runnable); 
//					}
//					monitor.done();
//					return;
//				}
//					monitor.done();
//			         
//			    } 
//			});
//			if(newObject!=null){
//				newObject.getRecordTimer = new Timer("recordTimer");
//				newObject.getRecordTimer.schedule(new GetRecordTimer(newObject), 3000,5000);
//				newObject.getPerformTimer = new Timer("performTimer");
//				newObject.getPerformTimer.schedule(new GetPerformTimer(newObject), 0,15000);
//			}
//			return flag;
//		} catch (Exception e1) {
//			
//			e1.printStackTrace();
//			return false;
//		}		
//	}
//}
