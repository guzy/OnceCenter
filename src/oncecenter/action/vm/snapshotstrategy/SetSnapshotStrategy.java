package oncecenter.action.vm.snapshotstrategy;

import oncecenter.Constants;
import oncecenter.maintabs.vm.VMSnapShotsTab;
import oncecenter.util.snapshotstrategy.SnapshotStrategy;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.VM;

public class SetSnapshotStrategy extends Action {
	
	private VMTreeObjectVM objectVM;
	private Connection connection;
	private VM vm;
	private SnapshotStrategy strategy;
	private VMSnapShotsTab tab;
	public SetSnapshotStrategy(VMTreeObjectVM objectVM,SnapshotStrategy strategy,VMSnapShotsTab tab)
	{
		this.objectVM = objectVM;
		connection = objectVM.getConnection();
		vm = (VM)objectVM.getApiObject();
		this.strategy = strategy;
		this.tab = tab;
	}
	
	class VMJob extends Job
	{
		VMTreeView viewer;
		Display display;
		String reverseTime;
		String period;
		public VMJob(VMTreeView viewer, Display display) {
			super("���������" + objectVM.getName() + "�Ŀ��ղ���");
			
			this.viewer = viewer;
			this.display = display;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			monitor.beginTask("set snapshot strategy...", 100);
			//�����ж��������ûӴ��ִ�п�����Ǩ��֮��Ĳ���
			ItemState state = objectVM.getItemState();
			if(state.equals(ItemState.changing))
			{
				System.out.println("��ȴ����������ִ�е�������ɣ�");
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
			//�趨���ղ���
			try{
				reverseTime = Integer.toString(strategy.getReverseNumber());
				period = String.valueOf(strategy.getPeriod());
				//ClassCastException: java.lang.Boolean cannot be cast to [Ljava.lang.Object;
				vm.setSnapshotPolicy(connection, period ,reverseTime);
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
						MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
						messageBox.setText("��ʾ");
						messageBox.setMessage("���ò��Գɹ���");
						messageBox.open();
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
