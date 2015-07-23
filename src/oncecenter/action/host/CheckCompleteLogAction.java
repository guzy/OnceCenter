package oncecenter.action.host;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

import oncecenter.util.FileUtil;
import oncecenter.util.Ssh;
import oncecenter.util.decryptPassword.Decrypt;
import oncecenter.util.dialog.ErrorMessageDialog;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.Host;

public class CheckCompleteLogAction extends Action{

	private VMTreeObjectHost selection;
	private Connection connection;
	private String ipAddr;
	private String username;
	private String password;
	private Host.Record record;
	private Text fullText;
	private Composite composite;
	private CTabItem fullLogItem;
	public CheckCompleteLogAction(VMTreeObjectHost selection,Text text,Composite composite){
		super();
		this.selection=selection;
		connection=selection.getConnection();
		setText("查看日志信息");	
		fullText = text;
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
    		String cmd = "cat /var/log/xen/xend.log";
    		try {
    			ret = ssh.Command(cmd);
    		} catch (Exception e) {
    			
    			e.printStackTrace();
    		}
    		final String fullLog = ret;
////    		/**下载文件到本地**/
//    		String remotePath = "/var/log/xen/xend.log";
//    		final String localDir = FileUtil.getLog();
//    		final String filePath = localDir + File.separator + record.uuid + "_xend.log";
//    		try {
//    			ssh.GetFile(remotePath, localDir);
//    			File log = new File(localDir + File.separator + "xend.log");
//    			log.renameTo(new File(filePath));
//    		} catch (Exception e) {
//    			
//    			e.printStackTrace();
//    			monitor.done(); 
//    			return Status.CANCEL_STATUS;
//    		}
//    		
    		if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	fullText.clearSelection();
			        	fullText.append(fullLog);
			        	GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true); 
			    		gridData.horizontalSpan = 2;
			    		fullText.setLayoutData(gridData); 
			    		fullText.pack(true);
			    		composite.layout();
			    		//fullLogItem.setControl(composite);
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
