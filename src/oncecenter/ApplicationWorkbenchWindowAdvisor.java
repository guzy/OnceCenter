package oncecenter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.Vector;

import oncecenter.action.ExitAction;
import oncecenter.util.FileUtil;
import oncecenter.util.GroupUtil;
import oncecenter.views.xenconnectiontreeview.VMTreeViewer;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectConnectionRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectDefault;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

//	private static final Logger m_logger = Logger.getLogger(ApplicationWorkbenchWindowAdvisor.class);
	private PrintStream ps;
	
	public ApplicationWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	public ActionBarAdvisor createActionBarAdvisor(
			IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	/**************************************************************************
	 * 
	 * Post Windows Create
	 * 
	 **************************************************************************/
	public void postWindowCreate() {
		super.postWindowCreate();
		initHomwWindows();
	}

	
	private void initHomwWindows() {
		Shell homeShell = getWindowConfigurer().getWindow().getShell();
		homeShell.setMaximized(true);
	}


	/**************************************************************************
	 * 
	 * Pre Windows Create
	 * 
	 **************************************************************************/
	public void preWindowOpen() {
		initWorkspace();
		initWorkbenchWindows();
		initConnections();
		createVmGroupInfo();
		GroupUtil.loadConfig();
		//redirectOutput();
	}

	private void redirectOutput(){
		try {
			FileOutputStream fos = new FileOutputStream(FileUtil.getWorkSpace()+File.separator+"log.txt");
			BufferedOutputStream bos = new BufferedOutputStream(fos,1024);
			ps = new PrintStream(bos,false);
			System.setOut(ps);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}

	private void initWorkspace() {
		FileUtil.deleteDir(new File(FileUtil.getData()));
		FileUtil.makeDir(new File(FileUtil.getWorkSpace()));
		FileUtil.makeDir(new File(FileUtil.getConf()));
		FileUtil.makeDir(new File(FileUtil.getData()));
		FileUtil.makeDir(new File(FileUtil.getLog()));
	}
	
	private void initWorkbenchWindows() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setShowCoolBar(true);
		configurer.setShowProgressIndicator(true);
		configurer.setTitle(Constants.WORKBENCH_WINDOW_NAME); //$NON-NLS-1$
		// configurer.setShowPerspectiveBar(true);
		// configurer.setShowFastViewBars(true);
		// configurer.setShowStatusLine(false);
		// initStatusLine();
		// final IWorkbenchWindow window = getWindowConfigurer().getWindow();
		// trayItem = initTaskItem(window);
		// if(trayItem != null) {
		// hookPopupMenu(window);
		// hookMinimize(window);
		// }
	}
	
	private void initConnections() {
		ObjectInputStream in = FileUtil.getHistoryConnectionsInfo();
		if (isFirstWithoutHistory(in)) {
			createDefaultConnectionsInfo();
		} else {
			try {
				recoverHistoryConnectionsFrom(in);
			} catch (Exception e) {
				//尝试
//				m_logger.error(in);
				createDefaultConnectionsInfo();
			}
		}
		Constants.getHistory();
	}

	private boolean isFirstWithoutHistory(ObjectInputStream in) {
		return in == FileUtil.NULL_Connections_INFO;
	}

	private void recoverHistoryConnectionsFrom(ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		Object o = in.readObject();
		if (isValidConnectionsInfo(o)) {
			Constants.CONNECTIONS_TREE = (VMTreeObjectConnectionRoot) o;
		} else {
			throw new IOException("Invalid VMTreeObjectDefault Object");
		}
	}


	private boolean isValidConnectionsInfo(Object o) {
		return (o instanceof VMTreeObject) ? true : false;
	}

	private void createDefaultConnectionsInfo() {
		Constants.CONNECTIONS_TREE = new VMTreeObjectConnectionRoot(
				Constants.DEFAULT_CONNECTION_TREE_ROOT_NAME);
	}

	private void createVmGroupInfo() {
		Constants.VMGROUPS_TREE = new VMTreeObjectDefault(
				Constants.VM_GROUP_TREE_ROOT_NAME);
	}

	/**************************************************************************
	 * 
	 * Post Windows CLose (do not check)
	 * 
	 **************************************************************************/
	@Override
	public boolean preWindowShellClose() {
		
//		for(Job job : Constants.jobs)
//		{
//			if(job.getState() != Job.NONE)
//			{
//				MessageBox box = new MessageBox(PlatformUI.getWorkbench().getDisplay().getActiveShell(),SWT.ICON_WARNING);
//				box.setText("警告");
//				box.setMessage("还有正在运行中的Job，请稍等片刻");
//				box.open();
//				return false;
//			}
//		}
		if(!Constants.jobs.isEmpty())
		{
			WaitDialog dialog = new WaitDialog(Display.getCurrent().getActiveShell(),Constants.jobs);
			if(Window.OK==dialog.open()){
				
			}else{
				return false;
			}
		}
		
		GroupUtil.saveConfig();
		
		for (VMTreeObject object : Constants.CONNECTIONS_TREE.getChildrenList()) {
			VMTreeObjectRoot root = (VMTreeObjectRoot) object;
			root.setItemState(ItemState.unable);
			
			if(root.historyHosts==null){
				root.historyHosts = new ArrayList<VMTreeObjectHost>();
			}
			root.historyHosts.clear();
			if(root.hostMap!=null){
				root.historyHosts.addAll(root.hostMap.values());
			}
			if(root.historyVMs==null){
				root.historyVMs = new ArrayList<VMTreeObjectVM>();
			}
			root.historyVMs.clear();
			if(root.vmMap!=null){
				root.historyVMs.addAll(root.vmMap.values());
			}
			
			root.events.removeAllElements();
			
			ArrayList<VMTreeObject> objectList = root.getChildrenList();
			VMTreeViewer viewer = Constants.treeView.getViewer();
			for(VMTreeObject o : objectList)
			{
				if(o instanceof VMTreeObjectHost && ((VMTreeObjectHost)o).getChildrenList().size() != 0)
				{
					VMTreeObjectHost hostObject = (VMTreeObjectHost)o;
					ArrayList<VMTreeObject> childList = hostObject.getChildrenList();
					for(VMTreeObject child : childList)
						viewer.remove(child);
					childList.clear();
				}
				viewer.remove(o);
			}
			root.getChildrenList().clear();
			viewer.remove(root);
			if (root.getRecordTimer != null) {
				root.getRecordTimer.cancel();
			}
			if (root.getPerformTimer != null) {
				root.getPerformTimer.cancel();
			}

		}
		try {
			File root = new File(FileUtil.getVMsInfoConfigFile());
			if (!root.exists()) {
				root.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(FileUtil.getVMsInfoConfigFile());
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(Constants.CONNECTIONS_TREE);
			oos.flush();
			oos.close();
			
			//File snapStrategy = new File();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}

		CancelTimer(Constants.CONNECTIONS_TREE);
			
		if(ps!=null)
			ps.close();
		return true;
	}

	public void CancelTimer(VMTreeObject treeObject) {
		if (treeObject.timerList != null) {
			for (Timer timer : treeObject.timerList) {
				timer.cancel();
			}
		}
		for (VMTreeObject child : treeObject.getChildrenList()) {
			CancelTimer(child);
		}
	}

	public class WaitDialog extends Dialog
	{
		private Vector<Job> jobs;
		private ExitAction exitAction;
		public static final int EXIT_ID = 0;
		public static final int WATI_ID = 1;
		public static final String EXIT_LABEL = "强制退出";
		public static final String WAIT_LABEL = "等待任务结束";

		protected WaitDialog(Shell parentShell,Vector<Job> jobs) {
			super(parentShell);
			
			this.jobs = jobs;
		}
		
		protected void configureShell(Shell newShell)
		{
			super.configureShell(newShell);
			newShell.setText("警告");
		}
		
		protected Control createDialogArea(Composite parent)
		{
			Composite composite = (Composite)super.createDialogArea(parent);
			composite.setLayout(new GridLayout(1,false));
			Label msgLabel = new Label(composite,SWT.NULL);
			msgLabel.setText("还有未完成任务，强制退出会强制取消任务，可能会导致未知错误，请等待！");
			Table table = new Table(composite,SWT.FULL_SELECTION);
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
			GridData gridData = new GridData();
			gridData.horizontalAlignment = SWT.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.grabExcessVerticalSpace = true;
			gridData.verticalAlignment = SWT.FILL;
			table.setLayoutData(gridData);
			TableColumn nameColumn = new TableColumn(table,SWT.NONE);
			nameColumn.setText("任务");
			TableColumn stateColumn = new TableColumn(table,SWT.NONE);
			stateColumn.setText("状态");
			table.clearAll();
			for(Job job : Constants.jobs)
			{
				TableItem item = new TableItem(table,SWT.NONE);
				item.setText(new String[]{job.getName(),"正在运行"});
			}
			
			for(int i = 0; i < table.getColumnCount(); ++i)
			{
				table.getColumn(i).pack();
			}
			return parent;
		}
	
		@Override
		protected void createButtonsForButtonBar(Composite parent)
		{
			createButton(parent,WaitDialog.EXIT_ID,WaitDialog.EXIT_LABEL,true);
			createButton(parent,WaitDialog.WATI_ID,WaitDialog.WAIT_LABEL,true);
		}
		
//		protected void buttonPressed(int buttonId)
//		{
//			if(WaitDialog.EXIT_ID == buttonId)
//			{
//				close();
//				exitAction = new ExitAction();
//				exitAction.run();
//			}
//			else if(WaitDialog.WATI_ID == buttonId)
//			{
//				close();
//			}
//		}
	}
}
