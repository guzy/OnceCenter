package oncecenter.action.addlostelement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import oncecenter.Constants;
import oncecenter.action.sr.AlarmDialog;
import oncecenter.util.AddServerUtil;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.Pool;

public class CompareRootAction extends Action {

	VMTreeObjectRoot previousRoot;
	VMTreeObjectRoot currentRoot;
	
	ArrayList<VMTreeObjectHost> lostHosts = new ArrayList<VMTreeObjectHost>();
	ArrayList<VMTreeObjectVM> lostVMs = new ArrayList<VMTreeObjectVM>();
	
	AddLostHostDialog addLostHostDialog;
	
	ShowLostVMDialog showLostVMDialog;
	
	int returnCode;
	
	public CompareRootAction(VMTreeObjectRoot previousRoot,VMTreeObjectRoot currentRoot){
		this.previousRoot = previousRoot;
		this.currentRoot = currentRoot;
	}
	
	public void run(){
		if(previousRoot==null){
			Constants.CONNECTIONS_TREE.addChild(currentRoot);
			return;
		}
		List<VMTreeObjectHost> historyHosts = previousRoot.historyHosts;
		List<String> currentUuids = new ArrayList<String>();
		for(VMTreeObjectHost hostO:currentRoot.hostMap.values()){
			if(hostO.getUuid()!=null)
				currentUuids.add(hostO.getUuid());
		}
		if(historyHosts!=null){
			for(VMTreeObjectHost host:historyHosts){
				if(host.getUuid()!=null&&!currentUuids.contains(host.getUuid())){
					lostHosts.add(host);
				}
			}
		}

		if(lostHosts.size()>0){
			Display display = PlatformUI.getWorkbench().getDisplay();
			if (!display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						addLostHostDialog = new AddLostHostDialog(new Shell(),lostHosts);	
						returnCode = addLostHostDialog.open();
						
						if(Window.OK == returnCode){
							ProgressMonitorDialog dialog = new ProgressMonitorDialog(new Shell()); 
							try {
								dialog.run(true, true, new IRunnableWithProgress(){ 
								    public void run(IProgressMonitor monitor) { 
								    	if(currentRoot instanceof VMTreeObjectHost){
								    		try{
								    			Pool.getAll(currentRoot.getConnection()).iterator().next()
									    		.setNameLabel(currentRoot.getConnection(), "new pool");
								    		}catch(Exception e){
												e.printStackTrace();
											}
								    	}
								    	for(VMTreeObjectHost lostHost : addLostHostDialog.lostHosts){
											try{
												Connection c = new Connection("http://"+lostHost.getIpAddress()+":9363"
														,lostHost.getUsername(),lostHost.getPassword());
												Pool.join(c, currentRoot.getIpAddress(), currentRoot.getUsername(), currentRoot.getPassword());
											}catch(Exception e){
												e.printStackTrace();
											}
										}
								    	VMTreeObjectRoot root = AddServerUtil.ConnectByIp(currentRoot.getIpAddress(), currentRoot.getUsername(), currentRoot.getPassword());
								        if(root!=null){
								        	currentRoot = root;
								        }
								        
								        monitor.done(); 
								    } 
								});
								
							} catch (Exception e1) {
								
								e1.printStackTrace();
							}		
							
						}
					}
				};
				display.syncExec(runnable);
			}
		}
		
//		List<VMTreeObjectVM> historyVMs = previousRoot.historyVMs;
//		currentUuids.clear();
//		for(VMTreeObjectVM VMO:currentRoot.vmMap.values()){
//			if(VMO.getUuid()!=null)
//				currentUuids.add(VMO.getUuid());
//		}
//		if(historyVMs!=null){
//			for(VMTreeObjectVM vm:historyVMs){
//				if(vm.getUuid()!=null&&!currentUuids.contains(vm.getUuid())){
//					lostVMs.add(vm);
//				}
//			}
//		}
//
//		if(lostVMs.size()>0){
//			Display display = PlatformUI.getWorkbench().getDisplay();
//			if (!display.isDisposed()) {
//				Runnable runnable = new Runnable() {
//					public void run() {
//						showLostVMDialog = new ShowLostVMDialog(new Shell(),lostVMs);
//						showLostVMDialog.open();
//					}
//				};
//				display.syncExec(runnable);
//			}
//			
//		}
		
		Constants.CONNECTIONS_TREE.addChild(currentRoot);
	}
}
