//package oncecenter.wizard.newpool;
//
//import java.lang.reflect.InvocationTargetException;
//import java.net.MalformedURLException;
//import java.util.ArrayList;
//import java.util.Set;
//import java.util.Timer;
//
//import oncecenter.Constants;
//import oncecenter.action.pool.AddHostToPoolAction;
//import oncecenter.daemon.GetPerformTimer;
//import oncecenter.daemon.GetRecordTimer;
//import oncecenter.tree.VMTreeObject;
//import oncecenter.tree.VMTreeObject.ItemState;
//import oncecenter.tree.VMTreeObjectDefault;
//import oncecenter.tree.VMTreeObjectHost;
//import oncecenter.tree.VMTreeObjectPool;
//import oncecenter.tree.VMTreeObjectRoot;
//import oncecenter.util.AddServerUtil;
//import oncecenter.wizard.newserver.NewServerWizardPage;
//
//import org.apache.xmlrpc.XmlRpcException;
//import org.eclipse.core.runtime.IProgressMonitor;
//import org.eclipse.core.runtime.Status;
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.jface.dialogs.ProgressMonitorDialog;
//import org.eclipse.jface.operation.IRunnableWithProgress;
//import org.eclipse.jface.wizard.Wizard;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.ui.PlatformUI;
//
//import com.xensource.xenapi.Connection;
//import com.xensource.xenapi.Host;
//import com.xensource.xenapi.Pool;
//
//
//
//public class NewServertoPoolWizard  extends Wizard{
//
//	VMTreeObjectPool poolObject;
//	VMTreeObjectHost hostObject;
//	VMTreeObjectDefault xenObject;
//	
//	String serverIp = "";
//	NewServerWizardPage mainPage;
//	public VMTreeObjectRoot newObject;
//	
//	Pool thisPool;
//
//	ArrayList<VMTreeObject> haltedVMs = new ArrayList<VMTreeObject>();
//	ArrayList<VMTreeObject> templates=new ArrayList<VMTreeObject>();
//	
//	String ip;
//	String username;
//	String password;
//	
//	public NewServertoPoolWizard(VMTreeObjectPool poolObject)
//	{
//		setWindowTitle("连接主机");
//		this.poolObject = poolObject;
//	}
//	
//	public NewServertoPoolWizard(String serverIp, VMTreeObjectPool poolObject)
//	{
//		setWindowTitle("重新连接资源");
//		this.serverIp = serverIp;
//		this.poolObject = poolObject;
//	}
//	
//	@Override
//	public void addPages()
//	{
//		if(serverIp ==null || serverIp.length() == 0)
//			mainPage = new NewServerWizardPage("Add New Server");
//		else
//			mainPage = new NewServerWizardPage("Add New Server",serverIp,"");
//		addPage(mainPage);
//	}
//	@Override
//	public boolean performFinish() {
//		
//		ip = mainPage.getIp().getText();
//		username = mainPage.getUsername().getText();
//		password = mainPage.getPassword().getText();
//		
//		xenObject = Constants.CONNECTIONS_TREE;
//		ProgressMonitorDialog dialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
//		try {
//			dialog.run(true, true, new IRunnableWithProgress(){
//
//				@SuppressWarnings("deprecation")
//				@Override
//				public void run(IProgressMonitor monitor)
//						throws InvocationTargetException, InterruptedException {
//					
//					monitor.beginTask("connecting.....", IProgressMonitor.UNKNOWN);
//					newObject = AddServerUtil.ConnectByIp( ip, username, password);
//					try
//					{
//						if(newObject instanceof VMTreeObjectHost)
//						{	
//							hostObject = (VMTreeObjectHost)newObject;
//							Host host = (Host) hostObject.getApiObject();
//							Set<Host> hosts = Host.getAll(poolObject.getConnection());
//							//主机已经在池中
//							if(hosts.contains(host))
//							{
//								Display display=PlatformUI.getWorkbench().getDisplay();
//								if (!display.isDisposed()){
//								    Runnable runnable = new Runnable(){
//								        public void run( ){
//								        	MessageDialog.openError(getShell(), "主机已经在池中","主机已经在池中");
//								        }
//									};
//								    display.syncExec(runnable); 
//								}
//								monitor.done();
//								return;
//							}
//							
//							//将主机加入到池中
//							Pool.join(hostObject.getConnection(), poolObject.getIpAddress(), poolObject.getUsername(), poolObject.getPassword());
//							Display display=PlatformUI.getWorkbench().getDisplay();
//							if (!display.isDisposed()){
//							    Runnable runnable = new Runnable(){
//							        public void run( ){
//							        	for(VMTreeObject o:xenObject.getChildrenList()){
//											if(o.getItemState().equals(ItemState.unable)&&o instanceof VMTreeObjectRoot){
//												if(((VMTreeObjectRoot)o).getIpAddress().equals(ip)){
//													xenObject.getChildrenList().remove(o);
//													Constants.treeView.getViewer().remove(o);
//													break;
//												}
//											}
//										}
//										Constants.addHosttoPool(hostObject,poolObject);
//							        	Constants.treeView.getViewer().refresh();
//							        }
//								};
//							    display.syncExec(runnable); 
//							}
//						}
//						
//					}
//					catch(Exception e)
//					{
//						
//						e.printStackTrace();
//						Display display=PlatformUI.getWorkbench().getDisplay();
//						if (!display.isDisposed()){
//						    Runnable runnable = new Runnable(){
//						        public void run( ){
//						        	MessageDialog.openError(getShell(), "加入池失败","加入池失败");
//						        }
//							};
//						    display.syncExec(runnable); 
//						}
//					}
//
//					
//					monitor.done();
//				}
//			});
//			if(newObject!=null){
//				newObject.getRecordTimer = new Timer("recordTimer");
//				newObject.getRecordTimer.schedule(new GetRecordTimer(newObject), 3000,5000);
//				newObject.getPerformTimer = new Timer("performTimer");
//				newObject.getPerformTimer.schedule(new GetPerformTimer(newObject), 0,15000);
//			}
//			return true;
//		} catch (InvocationTargetException e) {
//			
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			
//			e.printStackTrace();
//		}
//		return false;
//	}
//
//}
