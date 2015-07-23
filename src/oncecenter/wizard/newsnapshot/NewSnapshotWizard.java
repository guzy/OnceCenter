package oncecenter.wizard.newsnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import oncecenter.Constants;
import oncecenter.maintabs.vm.VMSnapShotsTab;
import oncecenter.util.ImageRegistry;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.VM;

public class NewSnapshotWizard extends Wizard {

	private VMTreeObjectVM objectVM;
	private Connection connection;
	private NewSnapshotPage page;
	VM vm;
	private String snapshotName;
	private VMSnapShotsTab tab;
	
	public NewSnapshotWizard(VMTreeObjectVM objectVM,VMSnapShotsTab tab)
	{
		this.setWindowTitle("创建快照");
		this.objectVM = objectVM;
		this.tab = tab;
	}
	
	@Override
	public void addPages()
	{
		page = new NewSnapshotPage("new snapshot");
		this.addPage(page);
	}
	@Override
	public boolean performFinish() {
		final VMEvent event=new VMEvent();
		event.setDatetime(new Date());
		event.setDescription("连接到 '" + objectVM.getName() + "'");
		event.setTarget(objectVM);
		event.setTask("");
		event.setType(eventType.info);
		event.setUser(objectVM.getRoot().getUsername());
		event.setImage(ImageRegistry.getImage(ImageRegistry.INFO));
		objectVM.events.add(event);
		Constants.logView.logFresh(event);

		snapshotName = page.getName() + "_" + getCurrentTime();
		VMJob job = new VMJob(Constants.treeView, PlatformUI.getWorkbench().getDisplay());
		job.schedule();
		return true;
	}
	
	private String getCurrentTime()
	{
		int year,month,day,hour,minute,second;
		String sMonth,sDay;
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"));
		year = cal.get(Calendar.YEAR);
		month = cal.get(Calendar.MONTH)+1;//月份从0开始
		day = cal.get(Calendar.DATE);
		hour = cal.get(Calendar.HOUR_OF_DAY);
		minute = cal.get(Calendar.MINUTE);
		second = cal.get(Calendar.SECOND);
		if(month < 10)
			sMonth = "0" + month;
		else
			sMonth = "" + month;
		if(day < 10)
			sDay = "0" + day;
		else
			sDay = "" + day;
		String currentTime = "" + year + sMonth + sDay + "T" +  hour + ":" + minute + ":" + second;
		return currentTime;
	}
	class VMJob extends Job
	{
		VMTreeView viewer;
		Display display;
		public VMJob(VMTreeView viewer,Display display) {
			super("创建虚拟机"+objectVM.getName()+"的快照");
			
			this.viewer = viewer;
			this.display = display;
			connection = objectVM.getConnection();
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			monitor.beginTask("create sanpshot", 100);
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
			//创建快照
			objectVM.setItemState(ItemState.changing);
			if (!this.display.isDisposed()){
				   Runnable runnable = new Runnable(){
				       public void run(){
				        viewer.getViewer().refresh();
				       }
				   };
				   this.display.syncExec(runnable); 
			}
			try{
				vm = (VM)objectVM.getApiObject();
				vm.snapshot(connection, snapshotName);
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
						tab.refreshTable();
			        }
				};
			    this.display.syncExec(runnable); 
			}
			monitor.done();
			return Status.OK_STATUS;
		}
	}

}
