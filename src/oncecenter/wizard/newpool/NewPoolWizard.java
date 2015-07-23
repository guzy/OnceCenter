package oncecenter.wizard.newpool;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.Timer;

import oncecenter.Constants;
import oncecenter.action.pool.RenamePoolAction;
import oncecenter.daemon.GetPerformTask;
import oncecenter.daemon.GetRecordTask;
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
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;
import oncecenter.wizard.newpool.NewPoolWizardPage.Item;





import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Pool;

public class NewPoolWizard extends Wizard {

	NewPoolWizardPage mainpage;
	VMTreeObjectHost master;
	
	public NewPoolWizard() {
		setWindowTitle("新建资源池");
	}

	@Override
	public void addPages() {
		VMTreeObject xen = Constants.CONNECTIONS_TREE;
		ArrayList<VMTreeObjectHost> activeHosts=new ArrayList<VMTreeObjectHost>();
		for(VMTreeObject tmp:xen.getChildren()){
			if(tmp instanceof VMTreeObjectHost&&tmp.getItemState().equals(ItemState.able)){
				activeHosts.add((VMTreeObjectHost)tmp);
			}
		}
    	mainpage = new NewPoolWizardPage("New Pool",activeHosts);
    	addPage(mainpage);
    }
	
	class PoolJob extends Job
	{
		Display display;
		VMTreeView viewer;
		ArrayList<VMTreeObjectHost> selectedHosts;
		String poolName = null;
		String poolDescription;
		Pool thisPool;
		public PoolJob(Display display, VMTreeView viewer,ArrayList<VMTreeObjectHost> selectedHosts,String poolName,String poolDescription) {
			super("新建资源池");
			this.display = display;
			this.viewer = viewer;
			this.selectedHosts = selectedHosts;
			this.poolName = poolName;
			this.poolDescription = poolDescription;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("New pool....", 100);
			unableObject(master);
			for(VMTreeObjectHost slave:selectedHosts){ 
				unableObject(slave);
			}
			if (!display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run( ){
			        	Constants.treeView.getViewer().refresh();
			        }
				};
			    display.syncExec(runnable); 
			}
			try {
				Set<Pool> pools=Pool.getAll(master.getConnection());
				pools.iterator().next().setNameLabel(master.getConnection(), poolName);
				//下面的语句执行报错，提示后台没有实现setNameDescription方法
				pools.iterator().next().setNameDescription(master.getConnection(), poolDescription);
				Set<Pool> pools1=Pool.getAll(master.getConnection());
				for(Pool p:pools1){
					if(p.getNameLabel(master.getConnection())!=null
							&&!p.getNameLabel(master.getConnection()).equals("")){
						thisPool=p;
						break;
					}
				}
				if(thisPool == null){
					enableObject(master);
					for(VMTreeObjectHost slave:selectedHosts){
						enableObject(slave);
					}
					if (!display.isDisposed()){
					    Runnable runnable = new Runnable(){
					        public void run( ){
					        	Constants.treeView.getViewer().refresh();
					        }
						};
					    display.syncExec(runnable); 
					}
				}else{
//					thisPool.setNameDescription(master.getConnection(), poolDescription);
				}
			} catch (final Exception e) {
				e.printStackTrace();
				enableObject(master);
				for(VMTreeObjectHost slave:selectedHosts){
					enableObject(slave);
				}
				if (!display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run( ){
				        	Constants.treeView.getViewer().refresh();
				        }
					};
				    display.syncExec(runnable); 
				}
				monitor.done();
				return Status.CANCEL_STATUS;
			}
			try{
				for(VMTreeObjectHost slave:selectedHosts){
					Pool.join(slave.getConnection(),
							master.getIpAddress(), master.getUsername(), master.getPassword());
				}
			} catch (final Exception e) {
				e.printStackTrace();
				if (!this.display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
				        	if(e.toString().equals("SR_CONFLICT"))
							{
				        		Image image = ImageRegistry.getImage(ImageRegistry.ADDTOPOOLERROR);
					        	ErrorMessageDialog dialog = new ErrorMessageDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(),"主机添加到资源池失败!  \n主机间sr冲突 ",image);
								dialog.open();
								enableObject(master);
				        		for(final VMTreeObjectHost host:selectedHosts){
					        		enableObject(host);
								}
							}
				        	viewer.getViewer().refresh();
				        	Constants.treeView.getViewer().expandAll();
				        }
				    };
				    this.display.asyncExec(runnable); 
				}
				monitor.done();
				return Status.CANCEL_STATUS;
			}
			
			selectedHosts.add(master);
			//重新连接资源池
			final VMTreeObjectRoot poolObject = AddServerUtil.ConnectByIp(master.getIpAddress(), master.getUsername(), master.getPassword());
			if(poolObject!=null){
				Constants.CONNECTIONS_TREE.addChild(poolObject);
				Display display=PlatformUI.getWorkbench().getDisplay();
				if (!display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run( ){
				        	for(final VMTreeObjectHost host:selectedHosts){
				        		Constants.CONNECTIONS_TREE.getChildrenList().remove(host);
				        		enableObject(host);
								viewer.getViewer().remove(host);
							}
				        	Constants.treeView.getViewer().refresh();
				        	Constants.treeView.getViewer().expandAll();
				        }
					};
				    display.syncExec(runnable); 
				}
			}else{
				System.out.println("重新连接资源池失败");
			}
			poolObject.getRecordTimer = new Timer("recordTimer");
			poolObject.getRecordTimer.schedule(new GetRecordTask(poolObject), 3000,5000);
			if(Constants.displayStatusData){
				poolObject.getPerformTimer = new Timer("performTimer");
				poolObject.getPerformTimer.schedule(new GetPerformTask(poolObject), 0,15000);
			}
			
			IContributionItem [] menuItems = Constants.parentMenu.getItems();
//			IContributionItem [] p2vItems = ((IContributionManager) menuItems[5]).getItems();
//			((ActionContributionItem)p2vItems[0]).getAction().setEnabled(true);  
//			((ActionContributionItem)p2vItems[1]).getAction().setEnabled(true);  
			IContributionItem [] uploadItems = ((IContributionManager) menuItems[6]).getItems();
			((ActionContributionItem)uploadItems[0]).getAction().setEnabled(true);  
			
			if(poolObject!=null){
				for(VMTreeObject o :Constants.CONNECTIONS_TREE.getChildren()){
					if(!o.equals(poolObject)
							&&o.getItemState().equals(ItemState.able)
							&&o.getName().equals(poolObject.getName())){
						//Display display=PlatformUI.getWorkbench().getDisplay();
						if (!display.isDisposed()){
						    Runnable runnable = new Runnable(){
						        public void run( ){
						        	MessageDialog.openInformation(new Shell(), "提示", "系统中有重名对象，请重新命名");
						        	ISelection s1 = new StructuredSelection(new Object[]{poolObject});
		    						Constants.treeView.getViewer().setSelection(s1);	
						        	new RenamePoolAction((VMTreeObjectPool)poolObject,true).run();
						        }
							};
						    display.syncExec(runnable); 
						}
						
						
						break;
					}
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
	@Override
	public boolean performFinish() {
		ArrayList<Item> activeHosts=mainpage.getSelection();
		//String masterName=mainpage.getMaster().getText();
		ArrayList<VMTreeObjectHost> selectedHosts=new ArrayList<VMTreeObjectHost>();
		for(Item i:activeHosts){
			if(i.isMaster()){
				master=i.getHost();
			}else{
				selectedHosts.add(i.getHost());
			}
		}
		String poolName = mainpage.getName();
		String poolDescription = mainpage.getDescription();
		if (master != null) {
			final VMEvent event=new VMEvent();
			event.setDatetime(new Date());
			event.setDescription("将 '" + master.getName() + "'添加到资源池" + poolName);
			event.setTarget(master);
			event.setTask("");
			event.setType(eventType.info);
			event.setUser(master.getUsername());
			event.setImage(ImageRegistry.getImage(ImageRegistry.INFO));
			master.events.add(event);
			Constants.logView.logFresh(event);
		}

		PoolJob job=new PoolJob(PlatformUI.getWorkbench().getDisplay(),Constants.treeView,selectedHosts,poolName,poolDescription);
		job.schedule();
		return true;
	}
}
