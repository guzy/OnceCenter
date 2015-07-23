package oncecenter.wizard.newsr;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import oncecenter.Activator;
import oncecenter.util.ImageRegistry;
import oncecenter.util.Ssh;
import oncecenter.util.TypeUtil;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.wizard.newvmfromtemp.NewVMPage;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class SRLocationWizardPage extends NewVMPage {
	
	boolean useNFS = true;
	
	private Text ipText;
	private Text pathText;
	private Text userNameText;
	private Text passwordText;
	private Button check;
	private boolean flag;

	
	private ProgressMonitorDialog proBarDialog;
	
	private String infoMsg = "";
	private String errorMsg = "";
	private String ipStr = "";
	private String pathStr = "";
	private String userStr = "";
	private String passStr = "";
	public String getIpStr() {
		return ipStr;
	}

	public String getPathStr() {
		return pathStr;
	}


	
	private CLabel image;
	
	VMTreeObjectRoot selection;
	
	public Text getText() {
		return ipText;
	}

	/**
	 * Create the wizard.
	 */
	public SRLocationWizardPage(VMTreeObjectRoot selection) {
		super("新建存储");
		setTitle("新建存储"); 
		setDescription("请输入要连接的存储的位置，包括主机的ip和存储路径");		
		this.selection=selection;
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		final Composite container = new Composite(parent, SWT.NULL);
		setControl(container);

		Label lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setBounds(45, 47, 80, 20);
		lblNewLabel.setText("主机地址");
		
		ipText = new Text(container, SWT.BORDER);
		ipText.setBounds(152, 44, 266, 23);
		
		Label pathLabel = new Label(container,SWT.NONE);
		pathLabel.setBounds(45, 77, 80, 20);
		pathLabel.setText("挂载路径");
		pathText = new Text(container,SWT.BORDER);
		pathText.setBounds(152, 74, 266, 23);
		
		Label userNameLabel = new Label(container,SWT.NONE);
		userNameLabel.setText("用户名   ");
		userNameLabel.setBounds(45, 107, 80, 20);
		userNameText = new Text(container,SWT.BORDER);
		userNameText.setBounds(152, 104, 266, 23);
		
		Label passwordLabel = new Label(container,SWT.NONE);
		passwordLabel.setText("密码  ");
		passwordLabel.setBounds(45, 137, 80, 20);
		passwordText = new Text(container,SWT.PASSWORD|SWT.BORDER);
		passwordText.setBounds(152, 134, 266, 23);
		
		refresh();
		
		check = new Button(container, SWT.PUSH);
		check.setBounds(430, 184, 50, 25);
		check.setText("检测");
		
		image = new CLabel(container,SWT.NONE);
		image.setImage(ImageRegistry.getImage(ImageRegistry.SUCCESS));
		image.setBounds(220, 180, 100, 50);
		image.setVisible(false);
		
		check.addSelectionListener(new SelectionListener(){


			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				widgetSelected(arg0);
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) 
			{
				flag = true;
				ipStr = ipText.getText();
				pathStr = pathText.getText();
				userStr = userNameText.getText();
				passStr = passwordText.getText();
				proBarDialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
				try {
					proBarDialog.run(true, true, new IRunnableWithProgress(){

						@Override
						public void run(IProgressMonitor monitor)throws InvocationTargetException,InterruptedException 
						{
							
							monitor.beginTask("正在检测........",  IProgressMonitor.UNKNOWN);
							Display display=PlatformUI.getWorkbench().getDisplay();
							
							if(useNFS){
								if(ipStr==null||ipStr.length()==0
										||pathStr==null||pathStr.length()==0
										||userStr==null||userStr.length()==0
										||passStr==null||passStr.length()==0)
								{
									flag = false;
									infoMsg = "请正确填写各项";
						        	errorMsg = "ip地址，路径，用户名，密码，均不能为空。";
								}else{
									Ssh ssh = new Ssh(ipStr, userStr, passStr);
									boolean isSuccess = ssh.Connect();
									if(!isSuccess){
										flag = false;
										infoMsg = "无法连接目标机器";
							        	errorMsg = "请检查ip地址和用户名密码。";
									}else{
										Ssh ssh1 = new Ssh(selection.getIpAddress(), selection.getUsername(), selection.getPassword());
										ssh1.Connect();
										String ret = "";
										String cmd = "showmount -e " + ipStr;
										try {
											ret = ssh1.Command(cmd);
										} catch (Exception e) {
											e.printStackTrace();
										}
										if(ret == null || ret.length() == 0)
										{
											flag = false;
								        	infoMsg = "目标" + ipStr + "未开启NFS服务";
								        	errorMsg = "无法检测到目标机器上的NFS服务，请检查设置是否正确并重试。";
										}else{
											boolean checkPath = false;
											String[] paths = ret.split("\n");
											for(int i = 1; i < paths.length; ++i)
											{
												String position = paths[i].split(" ")[0];
												if(position.equals(pathStr))
												{
													checkPath = true;
													break;
												}
											}
											if(!checkPath)
											{
												flag = false;
												infoMsg = "目标路径 "+ pathStr + "未开启NFS服务";
												errorMsg = "给定的路径未开启NFS服务，请检查其他的可用路径";
											}else{
												flag = true;
												((NewSRWizard)getWizard()).ip = ipStr;
												((NewSRWizard)getWizard()).path = pathStr;
												((NewSRWizard)getWizard()).username = userStr;
												((NewSRWizard)getWizard()).password = passStr;
											}
										}
									}
								}
								
							}else{
								if(pathStr==null||pathStr.length()==0)
								{
									flag = false;
									infoMsg = "请正确填写各项";
						        	errorMsg = "路径不能为空。";
								}else{
									boolean checkPath = true;
									String hostName = "";
									//检查路径是否存在
									if(selection instanceof VMTreeObjectHost){
										Ssh ssh = new Ssh(selection.getIpAddress(), selection.getUsername(), selection.getPassword());
										ssh.Connect();
										String cmd = "test -e " + pathStr;
										try {
											ssh.Command(cmd);
										} catch (Exception e) {
											e.printStackTrace();
											checkPath = false;
											hostName+=(selection.getName()+" ");
										}
									}else{
										for(VMTreeObject o : selection.getChildren()){
											if(o instanceof VMTreeObjectHost){
												Ssh ssh = new Ssh(((VMTreeObjectHost)o).getIpAddress(), selection.getUsername(), selection.getPassword());
												ssh.Connect();
												String cmd = "test -e " + pathStr;
												try {
													ssh.Command(cmd);
												} catch (Exception e) {
													e.printStackTrace();
													checkPath = false;
													hostName+=(o.getName()+" ");
												}
											}
										}
									}
									if(!checkPath){
										flag = false;
										infoMsg = "路径出错";
							        	errorMsg = "主机："+hostName+"上不存在路径"+pathStr+" ，请检查路径是否填写错误";
									}else{
										flag = true;										
										((NewSRWizard)getWizard()).path = pathStr;
									}
								}
							}
							if(flag){
								if (!display.isDisposed()){
								    Runnable runnable = new Runnable(){
								        public void run( ){
								        	image.setVisible(true);
											setPageComplete(true);
								        }
									};
								    display.syncExec(runnable); 
								}
							}else{
								if (!display.isDisposed()){
								    Runnable runnable = new Runnable(){
								        public void run( ){
								        	ServiceNotOpenDialog dialog = new ServiceNotOpenDialog(Display.getCurrent().getActiveShell(),infoMsg, errorMsg);
											dialog.open();
											image.setVisible(false);
											canFlipToNextPage();
								        }
									};
								    display.syncExec(runnable); 
								}
//								monitor.done();
//								return;
							}
//							if (!display.isDisposed()){
//							    Runnable runnable = new Runnable(){
//							        public void run( ){
//							        	getWizard().getContainer().updateButtons();
//							        }
//								};
//							    display.syncExec(runnable); 
//							}
							
							monitor.done();							
						}
					});
				} catch (InvocationTargetException e1) {
					
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					
					e1.printStackTrace();
				}
			}
		} );
		
	}

	public void refresh(){
		String type = ((NewSRWizard)this.getWizard()).type;
		if(type!=null&&type.contains(TypeUtil.nfsSign)){
			useNFS = true;
		}else{
			useNFS = false;
		}
		if(!useNFS){
			if(ipText!=null)
				ipText.setEnabled(false);
			if(userNameText!=null)
				userNameText.setEnabled(false);
			if(passwordText!=null)
				passwordText.setEnabled(false);
		}else{
			if(ipText!=null)
				ipText.setEnabled(true);
			if(userNameText!=null)
				userNameText.setEnabled(true);
			if(passwordText!=null)
				passwordText.setEnabled(true);
		}
	}
	
	public boolean canFlipToNextPage(){
		return flag;
	}
	
	
	public Text getUserNameText() {
		return userNameText;
	}

	public void setUserNameText(Text userNameText) {
		this.userNameText = userNameText;
	}


	public Text getPasswordText() {
		return passwordText;
	}

	public void setPasswordText(Text passwordText) {
		this.passwordText = passwordText;
	}


	class ServiceNotOpenDialog extends Dialog
	{
		private Label infoLabel;
		private Label errorLabel;
		private CLabel imageCLabel;
		
		private String infoMsg = "";
		private String errorMsg = "";
		
		private static final int CLOSE_ID = 0;
		private static final String CLOSE_LABEL = "Close";
		protected ServiceNotOpenDialog(Shell parentShell,String infoMsg, String errorMsg) {
			super(parentShell);
			
			this.infoMsg = infoMsg;
			this.errorMsg = errorMsg;
			
		}
		
		protected Control createDialogArea(Composite parent)
		{
			Composite composite = (Composite)super.createDialogArea(parent);
			GridLayout layout = new GridLayout(2,true);
			layout.verticalSpacing = 22;
			layout.horizontalSpacing = 15;
			layout.makeColumnsEqualWidth = false;
			composite.setLayout(layout);
			imageCLabel = new CLabel(composite, SWT.NONE);
			imageCLabel.setImage(ImageRegistry.getImage(ImageRegistry.SERVICENOTOPEN));
			GridData imgData = new GridData();
			imgData.verticalSpan = 2;
			imageCLabel.setLayoutData(imgData);
			
			infoLabel = new Label(composite, SWT.NONE);
			infoLabel.setText(infoMsg);
			
			errorLabel = new Label(composite, SWT.NONE);
			errorLabel.setText(errorMsg);
			
			return parent;
		}
		
		protected void createButtonsForButtonBar(Composite parent)
		{
			createButton(parent,ServiceNotOpenDialog.CLOSE_ID,ServiceNotOpenDialog.CLOSE_LABEL,true);
		}
		
		protected void buttonPressed(int buttonId)
		{
			if(ServiceNotOpenDialog.CLOSE_ID == buttonId)
				close();
		}
	}
}
