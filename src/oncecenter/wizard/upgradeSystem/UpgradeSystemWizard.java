package oncecenter.wizard.upgradeSystem;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import oncecenter.Constants;
import oncecenter.action.DisconnectAction;
import oncecenter.util.AddServerUtil;
import oncecenter.util.ImageRegistry;
import oncecenter.util.Ssh;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;
import oncecenter.wizard.upgradeSystem.HostAddressWizardPage.HostInfo;
import oncecenter.wizard.uploadisofile.UploadIsoDestinationPage;
import oncecenter.wizard.uploadisofile.UploadIsoSourcePage;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.Host;
import com.once.xenapi.Pool;
import com.once.xenapi.SR;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.JoiningHostCannotContainSharedSrs;
import com.once.xenapi.Types.XenAPIException;

public class UpgradeSystemWizard extends Wizard {

	UpgradeSystemWizardPage sourcePage;
	HostAddressWizardPage hostPage;
	private VMTreeObjectRoot objectRoot;
	private ArrayList<HostInfo> hostList;
	private String filePath;
	private String ipAddress;
	private String userName;
	private String password;
	private boolean isConnected;
	private boolean isPool;
	private VMTreeObjectHost masterHost;
	private VMTreeObjectPool poolObject;
	private Display display;
	private VMTreeView viewer;
	public UpgradeSystemWizard()
	{
		this.setWindowTitle("在线更新");
	}
	@Override
	public boolean canFinish() {
		IWizardPage page = getContainer().getCurrentPage();
		if ((page instanceof UpgradeSystemWizardPage) && page.isPageComplete())
			return true;
		else
			return false;	
		}
	@Override
	public void addPages() {
		
		hostPage = new HostAddressWizardPage("主机信息");
		this.addPage(hostPage);
		sourcePage = new UpgradeSystemWizardPage("在线更新");
		this.addPage(sourcePage);
	}
	
	@Override
	public boolean performFinish() {
		filePath = sourcePage.getFileName();
		ipAddress = hostPage.getIp().getText();
		userName = hostPage.getUsername().getText();
		password = hostPage.getPassword().getText();
		
		isConnected = hostPage.isConnected();
		hostList = hostPage.getHostList();
		isPool = hostPage.isPool();//资源池多于一个主机节点
		masterHost = hostPage.getMasterHost();
		poolObject = hostPage.getPoolObject();
		display = PlatformUI.getWorkbench().getDisplay();
		viewer = Constants.treeView;
		/**在线更新**/
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell()); 
		try {
			dialog.run(true, true, new IRunnableWithProgress(){ 
			    public void run(IProgressMonitor monitor) {
			        monitor.beginTask("在线更新中...",  100);
			        /**执行断开主机操作**/
			        if(isConnected){
			        	if (!display.isDisposed()){
			    		    Runnable runnable = new Runnable(){
			    		        public void run(){
			    		        	MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
									messageBox.setText("警告");
									messageBox.setMessage("在线更新会断开主机连接，请更新完成后重连！");
									messageBox.open();
			    		        	DisconnectAction action;
									if(!isPool && masterHost!= null)
										action = new DisconnectAction(masterHost);
									else
										action = new DisconnectAction(poolObject);
									action.run();
									//viewer.getViewer().refresh();
			    		        }
			    		    };
			    		    display.syncExec(runnable); 
			    		}
					}
			        monitor.worked(10);
			        /**上传更新文件包到各主机**/
			        int size = hostList.size();
			        int diff = 80/size;
			        int count = 0;
			        for(HostInfo host:hostList)
			        {
			        	String ipAddr = host.getIpAddress();
			    		String user = host.getUserName();
			    		String pass = host.getPassword();
			    		String location = "/tmp";
			    		Ssh ssh = new Ssh(ipAddr, user, pass);
			    		if (!ssh.Connect()) {
			    			System.err.println("author or password isn't right!");
			    			handleError();
			    			Display display=PlatformUI.getWorkbench().getDisplay();
							if (!display.isDisposed()){
							    Runnable runnable = new Runnable(){
							        public void run( ){
							        	MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
										messageBox.setText("警告");
										messageBox.setMessage("ssh连接失败");
										messageBox.open();
							        }
								};
							    display.syncExec(runnable); 
							}
			    			monitor.done(); 
			    			return;
			    		}
//			    		/**上传文件**/
			    		try {
			    			ssh.ScpFile(filePath, location);
			    		} catch (Exception e) {
			    			
			    			e.printStackTrace();
			    			handleError();
			    			monitor.done(); 
			    			return;
			    		}
			    		String fileName = sourcePage.getUpdateFileName();
			    		 /**解压缩文件包**/
			    		try {
//			    			ssh.Command("cd " + location);
							ssh.Command("cd " + location + " && /bin/tar xvf " + fileName);
						} catch (Exception e1) {
							
							e1.printStackTrace();
							Display display=PlatformUI.getWorkbench().getDisplay();
							if (!display.isDisposed()){
							    Runnable runnable = new Runnable(){
							        public void run( ){
							        	MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
										messageBox.setText("警告");
										messageBox.setMessage("压缩包解压失败，请检查提供的更新包是否正确！");
										messageBox.open();
							        }
								};
							    display.syncExec(runnable); 
							}
							monitor.done(); 
							return;
						}
			    		/**执行更新命令,重启xend**/
				        try{
				        	ssh.Command("cd " + location+ " ; nohup bash update.sh svn.tar.gz > /dev/null 2>&1");
							//ssh.Command("/etc/init.d/xend restart");			
						}catch(Exception e){
							e.printStackTrace();
							Display display=PlatformUI.getWorkbench().getDisplay();
							if (!display.isDisposed()){
							    Runnable runnable = new Runnable(){
							        public void run( ){
							        	MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
										messageBox.setText("警告");
										messageBox.setMessage("执行在线更新命令失败，请检查提供的更新包是否正确！");
										messageBox.open();
							        }
								};
							    display.syncExec(runnable); 
							}
							monitor.done(); 
							return;
						}
				        ++count;
				        monitor.worked(count*diff + 10);
			        }
			        monitor.worked(90);
			        if(isPool)
			        {
			        	 /**重新组成池*/
						try {
							Connection masterConnection = AddServerUtil.getConnection(ipAddress, userName, password);
							Set<Pool> pools=Pool.getAll(masterConnection);
							pools.iterator().next().setNameLabel(masterConnection, "new pool");
						} catch (BadServerResponse e1) {
							
							e1.printStackTrace();
						} catch (XenAPIException e1) {
							
							e1.printStackTrace();
						} catch (XmlRpcException e1) {
							
							e1.printStackTrace();
						}
						for(HostInfo host:hostList)
				        {
							String ipAddressSlave = host.getIpAddress();
				        	if(!ipAddressSlave.equals(ipAddress))
				        	{
				        		Connection slaveConnection = AddServerUtil.getConnection(ipAddressSlave, "root", password);
				        		try {
									Pool.join(slaveConnection,
											ipAddress, userName, password);
								} catch (BadServerResponse e) {
									
									e.printStackTrace();
								} catch (JoiningHostCannotContainSharedSrs e) {
									
									e.printStackTrace();
								} catch (XenAPIException e) {
									
									e.printStackTrace();
								} catch (XmlRpcException e) {
									
									e.printStackTrace();
								}
				        	}
				        	
				        }
			        }
			        monitor.worked(100);
					Display display=PlatformUI.getWorkbench().getDisplay();
					if (!display.isDisposed()){
					    Runnable runnable = new Runnable(){
					        public void run( ){
					        	MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
								messageBox.setText("提示");
								messageBox.setMessage("在线更新成功！");
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
		
//		IContributionItem [] items = Constants.toolBar.getItems();
//		((ActionContributionItem)items[17]).getAction().setEnabled(false);
//		((ActionContributionItem)items[19]).getAction().setEnabled(true);
		return true;
	}
	public void handleError() {
		//selectedSR.setItemState(ItemState.able);
		if (!this.display.isDisposed()) {
			Runnable runnable = new Runnable() {
				public void run() {
					viewer.getViewer().refresh();
				}
			};
			this.display.syncExec(runnable);
		}
	}
}


