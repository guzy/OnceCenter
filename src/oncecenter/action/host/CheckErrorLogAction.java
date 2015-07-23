package oncecenter.action.host;

import oncecenter.util.Ssh;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.Host;

public class CheckErrorLogAction extends Action {

	private VMTreeObjectHost selection;
	private String ipAddr;
	private String username;
	private String password;
	private Host.Record record;
	private Text errorText;
	private Composite composite;
	public CheckErrorLogAction(VMTreeObjectHost selection,Text text,Composite composite){
		super();
		this.selection=selection;
		setText("查看日志信息");	
		errorText = text;
		this.composite = composite;
	}
	class LogJob extends Job
	{
		private FileDialog dialog;
		private Display display;
		public LogJob(Display display) {
			super("查看日志");
			
			this.display = display;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			record = selection.getRecord();
			/**创建ssh连接**/
			ipAddr = record.address;
			username = "root";
			password = "onceas";
			Ssh ssh = new Ssh(ipAddr, username, password);
    		if (!ssh.Connect()) {
    			System.err.println("author or password isn't right!");
    			monitor.done();
				return Status.CANCEL_STATUS;
    		}
    		String ret = "";
    		String cmd = "cat /var/log/xen/xend.log | grep ERROR -A 40";
    		try {
    			ret = ssh.Command(cmd);
    		} catch (Exception e) {
    			
    			e.printStackTrace();
    		}
    		final String fullLog = ret;	
    		if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	errorText.setText(fullLog);
			        	GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true); 
			    		gridData.horizontalSpan = 2;
			    		errorText.setLayoutData(gridData); 
			    		errorText.pack(true);
			        	composite.layout();
			        }
			    };
			    this.display.asyncExec(runnable); 
			}
			return Status.OK_STATUS;
		}
		
	}
	public void run()
	{
		LogJob job = new LogJob(PlatformUI.getWorkbench().getDisplay());
		job.schedule();
	}
}
