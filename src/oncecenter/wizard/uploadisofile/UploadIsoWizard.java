package oncecenter.wizard.uploadisofile;

import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

import oncecenter.Constants;
import oncecenter.util.Ssh;
import oncecenter.util.TypeUtil;
import oncecenter.util.decryptPassword.Decrypt;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;







import ch.ethz.ssh2.SFTPv3DirectoryEntry;

import com.once.xenapi.SR;

public class UploadIsoWizard extends Wizard {
	
	private UploadIsoSourcePage sourcePage;
	private UploadIsoDestinationPage destinationPage;
	
	String filePath;
	VMTreeObjectPool selectedPool;
	VMTreeObjectSR selectedSR;
	SR sr;
	private Display display;
	private VMTreeView viewer;
	String finalMessage = "ISO文件上传成功！";
	String localFileName;
	boolean transComplete = false;
	
	public UploadIsoWizard() {
		setWindowTitle("上传镜像文件");
	}
	
	public void setComplete() {
		transComplete = true;
	}
	
	@Override
	public boolean canFinish() {
		IWizardPage page = getContainer().getCurrentPage();
		if ((page instanceof UploadIsoDestinationPage) && page.isPageComplete())
			return true;
		else
			return false;
	}

	@Override
	public void addPages() {
		sourcePage = new UploadIsoSourcePage("source page");
		destinationPage = new UploadIsoDestinationPage("destination page");
		this.addPage(sourcePage);
		this.addPage(destinationPage);
	}
	@Override
	public boolean performFinish() {
		getConfig();		
		viewer = Constants.treeView;
//		VMJob job = new VMJob(PlatformUI.getWorkbench().getDisplay(), viewer);
//		Constants.jobs.add(job);
//		job.schedule();
		
		//the following is used to display the progress bar
		display = PlatformUI.getWorkbench().getDisplay();
		final ProgressMonitorDialog dialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
		dialog.setCancelable(true);
		try {
			dialog.run(true, true, new IRunnableWithProgress(){ 
				String ip;
				String username;
				String password;
				String location;
			    public void run(IProgressMonitor monitor) throws InterruptedException {
			        monitor.beginTask("镜像文件上传中...",  5);
					selectedSR.setItemState(ItemState.changing);
					if (!display.isDisposed()) {
						Runnable runnable = new Runnable() {
							public void run() {
								viewer.getViewer().refresh();
							}
						};
						display.syncExec(runnable);
					}
			        monitor.subTask("建立与目标物理机的连接..."); 
			        if(selectedSR.getSrType().contains(TypeUtil.nfsSign)){
						try {
							Map<String, String> otherconfig = sr.getRecord(selectedSR.getConnection()).otherConfig;
							String location = otherconfig.get("location");
							ip = location.substring(0, location.indexOf(":"));
							location = location.substring(location.indexOf(":")+1);
							username = otherconfig.get("username");
							password = Decrypt.getString(otherconfig.get("password"));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}else{
						ip = selectedPool.getIpAddress();
						try {
							location = sr.getLocation(selectedSR.getConnection());
						} catch (Exception e) {
							e.printStackTrace();
						}
						username = selectedPool.getUsername();
						password = selectedPool.getPassword();
					}
					final Ssh ssh = new Ssh(ip, username, password);
					if (location==null || !ssh.Connect()) {
						System.err.println("author or password isn't right!");
						selectedSR.setItemState(ItemState.able);
						if (!display.isDisposed()) {
							Runnable runnable = new Runnable() {
								public void run() {
									MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
									messageBox.setText("提示");
									messageBox.setMessage("用户名或密码错误！");
									messageBox.open();
									viewer.getViewer().refresh();
								}
							};
							display.syncExec(runnable);
						}
						monitor.done();
					}
					if (monitor.isCanceled() || alreadyExist(ssh, localFileName, location)){
						if (!display.isDisposed()){
						    Runnable runnable = new Runnable(){
						        public void run( ){
						        	MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
									messageBox.setText("提示");
									if (alreadyExist(ssh, localFileName, location)) {
										messageBox.setMessage("指定目录已经包含该文件，上传操作将终止！");
									} else {
										messageBox.setMessage("上传文件操作已经取消！");
									}
									messageBox.open();
						        }
							};
						    display.syncExec(runnable); 
						}
						monitor.done();
					} else {
				        monitor.worked(1);
				        monitor.subTask("发送文件...");
				        Common c = new Common(ssh, filePath, location, monitor);
						Thread transThread = new Thread(new R1(c));
						transThread.start();
						Thread monitorThread = new Thread(new R2(c));
						monitorThread.start();
						try {							
							int transTimer = 0;
							while (!transComplete) {
								transTimer++;
								System.out.println("文件已经传送" + transTimer + "秒");
								if (monitor.isCanceled()){
									finalMessage = "ISO文件上传操作已经取消！";
									//下面的命令不起作用
									Runtime.getRuntime().exec("taskkill /F /PID " + transThread.getId());
									Runtime.getRuntime().exec("taskkill /F /PID " + monitorThread.getId());
									String toDelFileName = location + "/" + localFileName;
									ssh.rmFile(toDelFileName);
									break;
								}
								Thread.sleep(1000);
							}
							transThread.interrupt();
					        monitorThread.interrupt();
						} catch (Exception e) {
							e.printStackTrace();
							selectedSR.setItemState(ItemState.able);
							if (!display.isDisposed()) {
								Runnable runnable = new Runnable() {
									public void run() {
										viewer.getViewer().refresh();
									}
								};
								display.syncExec(runnable);
							}
							monitor.done();
						}
						monitor.worked(4);
						monitor.subTask("更新存储中的信息...");
						try{
							sr.update(selectedSR.getConnection());
						}catch (Exception e) {
							e.printStackTrace();
						}
						selectedSR.setItemState(ItemState.able);
						if (!display.isDisposed()) {
							Runnable runnable = new Runnable() {
								public void run() {
									viewer.getViewer().refresh();
								}
							};
							display.syncExec(runnable);
						}
						ssh.CloseSsh();
				        monitor.worked(5);
				        monitor.done();
						if (!display.isDisposed()){
						    Runnable runnable = new Runnable(){
						        public void run( ){
						        	MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
									messageBox.setText("提示");
									messageBox.setMessage(finalMessage);
									messageBox.open();
						        }
							};
						    display.syncExec(runnable); 
						}
					}
			    }
			});
		}catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean alreadyExist(Ssh ssh, String fileName, String dirName){
		boolean alreadyExist = false;
		Vector<SFTPv3DirectoryEntry> fileInDir = ssh.getFileNameInDir(dirName);
		Enumeration<SFTPv3DirectoryEntry> vec_enum = fileInDir.elements();  
		while(vec_enum.hasMoreElements()) {
			if(vec_enum.nextElement().filename.equals(fileName)) {
				alreadyExist = true;
				break;
			}
		}
		return alreadyExist;
	}

	public void getConfig(){
		filePath = sourcePage.getFileName();
		localFileName = sourcePage.getFileNameBase();
		selectedPool = destinationPage.getSelectedPool();
		selectedSR = destinationPage.getSelectedSR();
		sr = (SR)selectedSR.getApiObject();
	}
	
	class Common {
		private Boolean state = true;
		private Ssh ssh;
		private String filePath;
		private String location;
		private IProgressMonitor monitor;
		
		public Common(Ssh ssh, String filePath, String location,
				IProgressMonitor monitor) {
			super();
			this.ssh = ssh;
			this.filePath = filePath;
			this.location = location;
			this.monitor = monitor;
		}

		public boolean isState() {
			return state;
		}

		public void setState(boolean state) {
			this.state = state;
		}

		public synchronized void r1() {
			System.out.println("进入传文件的线程");
			try {
				while (state) {
					wait();
				}
				ssh.ScpFile(filePath, location);
				setComplete();
				state = true;
				notify();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public synchronized void r2() {
			System.out.println("进入监控的线程");
			try {
				while (!state) {
					wait();
				}
				if(monitor.isCanceled()) {
					monitor.done();
		    	}
				state = false;
				notify();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	class R1 implements Runnable {
		private Common common;

		public R1(Common common) {
			this.common = common;
		}

		@Override
		public void run() {
//			while (true) {				
				common.r1();
//			}
		}
	}

	class R2 implements Runnable {
		private Common common;

		public R2(Common common) {
			this.common = common;
		}

		@Override
		public void run() {
//			while (true) {	
				common.r2();
//			}
		}
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	FileSystemManager fsManager;
	fsManager = VFS.getManager();
	FileObject fr = fsManager.resolveFile(filePath);
	FileObject to = fsManager.resolveFile(location);
	to.copyFrom(fr, Selectors.SELECT_ALL);
	//只要执行下面的方法，就阻塞，直到文件传送完毕，因为使用的开源包只有get，set方法
	finally {  
	    if (dialog.getReturnCode() == ProgressMonitorDialog.CANCEL) {  
	    	return true;
	    }  
	} 
	String command = "pscp " + filePath + " root@" + ip + ":" + location;
	Runtime.getRuntime().exec(command);
	ThreadTest monitorThread = new ThreadTest(ssh,filePath, location,monitor);
	ThreadTest transferThread = new ThreadTest(ssh,filePath, location,monitor);
	monitorThread.setName("monitor");
	transferThread.setName("transfer");
	monitorThread.start();
    transferThread.start();
    MainThread transferThread = new MainThread(ssh);						
	transferThread.run(filePath, location);
	monitorThread.run(filePath, location);
	executorService.execute(new TransferThread(ssh,filePath, location));
	if (monitor.isCanceled()){
		System.out.println("fangfa1");
	}
	if (dialog.getReturnCode() == ProgressMonitorDialog.CANCEL) {
		System.out.println("fangfa 2");
	}
	
//	Common c = new Common(ssh, filePath, location, monitor);
//	new Thread(new R1(c)).start();
//	new Thread(new R2(c)).start();
	class MainThread extends Thread {
		private Ssh ssh;
		long transId;
	      
	    public MainThread(Ssh ssh){  
	    	this.ssh = ssh;
	    } 
	    public void run(String filePath, String location){ 
	    	System.out.println("传文件");
	    	ssh.ScpFile(filePath, location);
	    }
	}
	class ThreadTest extends Thread{
		private int count=0;
		private Object o=new Object();
		private Ssh ssh;
		private String filePath;
		private String location;
		private IProgressMonitor monitor;

		public ThreadTest(Ssh ssh, String filePath, String location,
				IProgressMonitor monitor) {
			super();
			this.ssh = ssh;
			this.filePath = filePath;
			this.location = location;
			this.monitor = monitor;
		}
		public void run() {
			synchronized (o) {
			    while(true){
			    	try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(count%2==0&&currentThread().getName().equals("monitor")){
						o.notify();
						System.out.println(currentThread().getName());
						if(monitor.isCanceled()) {   //需要随时监控这个变量
				    		System.out.println("捕获到取消操作");
				    		ssh.CloseSsh();
							monitor.done();
				    	}
						count++;
						System.out.println("comne here" + count);
						try {
							o.wait();
							System.out.println("comne here");
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					else if (count%2==1&&currentThread().getName().equals("transfer")) {
						o.notify();
						System.out.println(currentThread().getName());
				    	ssh.ScpFile(filePath, location);
						count++;
						try {
							o.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	class MainThread extends Thread {
		private Ssh ssh;
//		private String filePath;
//		private String location;
//		private IProgressMonitor monitor;
	      
	    public MainThread(Ssh ssh){  
	    	this.ssh = ssh;
//	    	this.filePath = filePath;
//	    	this.location = location;
//	    	this.monitor = monitor;
	    } 
//	    transferThread.interrupt();
//		Runtime.getRuntime().exit(0);
//	          下面的进程执行传送文件的方法，如果不是传送文件，而是一个非阻塞的输出操作，是可以被中断的
//		TransferThread transferThread = new TransferThread(ssh, filePath, location);
//		transferThread.run();
//		ssh.ScpFile(filePath, location);
//		System.out.println("子进程");	      
	    public void run(IProgressMonitor monitor){ 
	    	while(true){
	    		System.out.println("监控");
		    	if(monitor.isCanceled()) {   //需要随时监控这个变量
		    		System.out.println("捕获到取消操作");
		    		ssh.CloseSsh();
					monitor.done();
		    	}
		    	try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    	}
	    } 
	    public void run(String filePath, String location){ 
	    	System.out.println("传文件");
	    	ssh.ScpFile(filePath, location);
	    }
	}
	class TransferThread extends Thread {
		private Ssh ssh;
		private String filePath;
		private String location;
	      
	    public TransferThread(Ssh ssh, String filePath, String location){  
	    	this.ssh = ssh;
	    	this.filePath = filePath;
	    	this.location = location;
	    } 
	      
	    public void run(){
	    	System.out.println("子进程");
//	    	ssh.ScpFile(filePath, location);
	    }  
	}
	class VMJob extends Job {
		Display display;
		VMTreeView viewer;
		String ip;
		String username;
		String password;
		String location;
		
		public VMJob(Display display, VMTreeView viewer) {
			super("上传镜像文件");
			this.display = display;
			this.viewer = viewer;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("上传镜像文件中...", 100);
			selectedSR.setItemState(ItemState.changing);
			if (!this.display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						viewer.getViewer().refresh();
					}
				};
				this.display.syncExec(runnable);
			}
			if(selectedSR.getSrType().contains(TypeUtil.nfsSign)){
				try {
					Map<String, String> otherconfig = sr.getRecord(selectedSR.getConnection()).otherConfig;
					String location = otherconfig.get("location");
					ip = location.substring(0, location.indexOf(":"));
					this.location = location.substring(location.indexOf(":")+1);
					username = otherconfig.get("username");
					password = Decrypt.getString(otherconfig.get("password"));
				} catch (Exception e1) {
					
					e1.printStackTrace();
				}
			}else{
				ip = selectedPool.getIpAddress();
				try {
					location = sr.getLocation(selectedSR.getConnection());
				} catch (Exception e) {
					
					e.printStackTrace();
				}
				username = selectedPool.getUsername();
				password = selectedPool.getPassword();
			}
			
			
			Ssh ssh = new Ssh(ip, username, password);
			if (location==null||!ssh.Connect()) {
				System.err.println("author or password isn't right!");
				handleError();
				monitor.done();
				Constants.jobs.remove(this);
				return Status.CANCEL_STATUS;
			}

			try {
				ssh.ScpFile(filePath, location);
			} catch (Exception e) {
				
				e.printStackTrace();
				handleError();
				monitor.done();
				Constants.jobs.remove(this);
				return Status.CANCEL_STATUS;
			}

			try{
				sr.update(selectedSR.getConnection());
			}catch (Exception e) {
				
				e.printStackTrace();
//				handleError();
//				monitor.done();
//				Constants.jobs.remove(this);
//				return Status.CANCEL_STATUS;
			}
			
			selectedSR.setItemState(ItemState.able);
			if (!this.display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						viewer.getViewer().refresh();
					}
				};
				this.display.syncExec(runnable);
			}
			
			ssh.CloseSsh();
			monitor.done();
			Constants.jobs.remove(this);
			return Status.OK_STATUS;
		}
		
		public void handleError() {
			selectedSR.setItemState(ItemState.able);
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
	*/	
	
}
