package oncecenter.action.vm.snapshotstrategy;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import oncecenter.Constants;
import oncecenter.action.vm.snapshotstrategy.SetSnapshotStrategy.VMJob;
import oncecenter.maintabs.vm.VMSnapShotsTab;
import oncecenter.util.snapshotstrategy.SnapshotStrategy;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import java.util.Iterator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.VM;

public class GetSnapshotStrategy extends Action {
	private VMTreeObjectVM objectVM;
	private Connection connection;
	private VM vm;
	private SnapshotStrategy strategy;
	private VMSnapShotsTab tab;
	public GetSnapshotStrategy(VMTreeObjectVM objectVM,SnapshotStrategy strategy, VMSnapShotsTab tab)
	{
		this.objectVM = objectVM;
		this.connection = objectVM.getConnection();
		vm = (VM) objectVM.getApiObject();
		this.strategy = strategy;
		this.tab = tab;
	}
	
	class VMJob extends Job
	{
		VMTreeView viewer;
		Display display;
		int reverseNumber;
		int period;
		public VMJob(VMTreeView viewer, Display display) {
			super("配置虚拟机" + objectVM.getName() + "的快照策略");
			
			this.viewer = viewer;
			this.display = display;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			monitor.beginTask("set snapshot strategy...", 100);
			//首先判断虚拟机又没哟在执行开机、迁移之类的操作
			ItemState state = objectVM.getItemState();
			if(state.equals(ItemState.changing))
			{
				System.out.println("请等待虚拟机正在执行的任务完成！");
				if (!this.display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
				        	viewer.getViewer().refresh();
				        }
				    };
				    this.display.syncExec(runnable); 
				}
				monitor.done();
				return Status.CANCEL_STATUS;
			}
			objectVM.setItemState(ItemState.changing);
			if (!this.display.isDisposed()){
				   Runnable runnable = new Runnable(){
				       public void run(){
				        viewer.getViewer().refresh();
				       }
				   };
				   this.display.syncExec(runnable); 
			}
			//设定快照策略
			try{
				//ClassCastException: java.lang.Boolean cannot be cast to [Ljava.lang.Object;
				Set<String> snapInfo =  vm.getSnapshotPolicy(connection);
				if(snapInfo != null && snapInfo.size() == 2)
				{
					Iterator it = snapInfo.iterator();
					period = Integer.parseInt((String) it.next());
					reverseNumber = Integer.parseInt((String) it.next());
					strategy.setPeriod(period);
					strategy.setReverseNumber(reverseNumber);
				}
			}
			catch(Exception e1)
			{
				e1.printStackTrace();
				objectVM.setItemState(ItemState.able);
				monitor.done();
				return Status.CANCEL_STATUS;
			}
			objectVM.setItemState(ItemState.able);
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
						viewer.getViewer().refresh();
						tab.refreshStrategy();
			        }
				};
			    this.display.syncExec(runnable); 
			}
			monitor.done();
			return Status.OK_STATUS;
		}
		
	}
	
	
	public void run()
	{
		VMTreeView viewer  = Constants.treeView;
		VMJob job=new VMJob(viewer,PlatformUI.getWorkbench().getDisplay());
		job.schedule();
	}

}
