package oncecenter.wizard.recover;

import oncecenter.Constants;
import oncecenter.util.AddServerUtil;
import oncecenter.util.Ssh;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class RecoverWizard extends Wizard {

	private RecoverWizardPage mainpage;
	private String ip;
	private String username;
	private String password;

	public RecoverWizard() {
		setWindowTitle("修复");
	}

	
	@Override
	public void addPages() {
		mainpage = new RecoverWizardPage();
    	
    	addPage(mainpage);
    }
	
	@Override
	public boolean performFinish() {
		ip = mainpage.getIp().getText();
		username = mainpage.getUsername().getText();
		password = mainpage.getPassword().getText();
		
		boolean isConnected = false;
		if(Constants.CONNECTIONS_TREE!=null){
			for(VMTreeObject o : Constants.CONNECTIONS_TREE.getChildren()){
				if(o.getItemState().equals(ItemState.able)){
					if(o instanceof VMTreeObjectHost){
						if(((VMTreeObjectHost) o).getIpAddress().equals(ip)){
							isConnected = true;
							break;
						}
					}else if (o instanceof VMTreeObjectPool){
						for(VMTreeObject o1:o.getChildren()){
							if(o1 instanceof VMTreeObjectHost){
								if(((VMTreeObjectHost) o1).getIpAddress().equals(ip)){
									isConnected = true;
									break;
								}
							}
						}
					}
				}
			}
		}
		
		if(isConnected){
			MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
			messageBox.setText("警告");
			messageBox.setMessage("该主机已经被连接，不能进行修复。");
			messageBox.open();
			return false;
		}
		
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell()); 
		try {
			dialog.run(true, true, new IRunnableWithProgress(){ 
			    public void run(IProgressMonitor monitor) {
			        monitor.beginTask("修复中...",  IProgressMonitor.UNKNOWN); 
			        Ssh ssh = new Ssh(ip, username, password);
					if (!ssh.Connect()) {
						Display display=PlatformUI.getWorkbench().getDisplay();
						if (!display.isDisposed()){
						    Runnable runnable = new Runnable(){
						        public void run( ){
						        	MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
									messageBox.setText("警告");
									messageBox.setMessage("IP不存在，或者用户名密码不匹配。");
									messageBox.open();
						        }
							};
						    display.syncExec(runnable); 
						}
						monitor.done();
						return;
					}
					try{
						ssh.Command("/etc/init.d/xend restart");
					}catch(Exception e){
						e.printStackTrace();
						Display display=PlatformUI.getWorkbench().getDisplay();
						if (!display.isDisposed()){
						    Runnable runnable = new Runnable(){
						        public void run( ){
						        	MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
									messageBox.setText("警告");
									messageBox.setMessage("修复失败！");
									messageBox.open();
						        }
							};
						    display.syncExec(runnable); 
						}
						monitor.done(); 
						return;
					}
					Display display=PlatformUI.getWorkbench().getDisplay();
					if (!display.isDisposed()){
					    Runnable runnable = new Runnable(){
					        public void run( ){
					        	MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
								messageBox.setText("提示");
								messageBox.setMessage("修复成功！");
								messageBox.open();
					        }
						};
					    display.syncExec(runnable); 
					}
			        monitor.done(); 
			    } 
			});
			
		} catch (Exception e1) {
			
			e1.printStackTrace();
		}		
		
		
		return true;
	}

}
