package oncecenter.wizard.upgradeSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import oncecenter.Constants;
import oncecenter.util.AddServerUtil;
import oncecenter.util.ImageRegistry;
import oncecenter.util.dialog.ErrorMessageDialog;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;
import oncecenter.wizard.newvmfromtemp.NewVMPage;
import oncecenter.wizard.newvmfromtemp.NewVmFTWizard;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wb.swt.SWTResourceManager;

import com.once.xenapi.Connection;
import com.once.xenapi.Host;
import com.once.xenapi.Pool;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.SessionAuthenticationFailed;
import com.once.xenapi.Types.XenAPIException;

public class HostAddressWizardPage extends NewVMPage {
	private Combo ip;
	private Text username;
	private Text password;
	private Composite composite;
	private String initialIp;
	private String initialUser;
	
	private boolean isConnected = false;
	private boolean isPool = false;
	private VMTreeObjectHost masterHost = null;
	private VMTreeObjectPool poolObject = null;
	
	private Connection connection = null;
	public VMTreeObjectPool getPoolObject() {
		return poolObject;
	}

	public VMTreeObjectHost getMasterHost() {
		return masterHost;
	}


	private Label alert;

	private ArrayList<HostInfo> hostList = new ArrayList<HostInfo>();
	public boolean isConnected() {
		return isConnected;
	}

	public boolean isPool() {
		return isPool;
	}
	

	public ArrayList<HostInfo> getHostList() {
		return hostList;
	}

	public Combo getIp() {
		return ip;
	}
	
	public Text getUsername() {
		return username;
	}

	public Text getPassword() {
		return password;
	}
	
	protected HostAddressWizardPage(String pageName) {
		super(pageName);
		
		this.setTitle("在线更新系统");
		this.setDescription("输入要更新的主机的IP地址、用户名以及密码信息.");
	}

	@Override
	public void createControl(Composite parent) {
		
		composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(2,false));
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessHorizontalSpace = true;		
		
		new Label(composite, SWT.NONE).setText("IP地址:");
		ip = new Combo(composite, SWT.BORDER);
		ip.setLayoutData(gridData);
		for(Iterator<String> i = Constants.historyServer.iterator();i.hasNext();){
			ip.add(i.next());
		}
		if(initialIp!=null&&!initialIp.equals("")){
			ip.setText(initialIp);
		}
		
		new Label(composite, SWT.NONE).setText("用户名:");
		username = new Text(composite, SWT.BORDER);
		username.setLayoutData(gridData);
		username.setText("root");
		if(initialUser!=null&&!initialUser.equals("")){
			username.setText(initialUser);
		}
		
		new Label(composite, SWT.NONE).setText("密码:");
		password = new Text(composite, SWT.PASSWORD);
		password.setLayoutData(gridData);
		
		new Label(composite,SWT.NONE);
		alert = new Label(composite,SWT.NONE);
		alert.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
		
		setControl(composite);
	}
	
	@Override
	public boolean canFlipToNextPage() {
		//if(!ip.getText().equals("")&& !username.getText().equals("") && !password.getText().equals(""))
		//{
			//alert.setVisible(false);
			//return true;
		//}
		return true;
	}


	@Override
	protected boolean nextButtonClick() {
		
		if(!ip.getText().equals("")&& !username.getText().equals("") && !password.getText().equals(""))
		{
			alert.setVisible(false);
			checkPoolAndConnection();
			return true;
		}
		alert.setText("请输入要更新的主机的ip地址、用户名及密码");
		alert.pack();
		this.setControl(composite);
		this.setPageComplete(false);
		return true;
	}

/**检测输入的的主机信息**/
void checkPoolAndConnection()
{
	String ipAddress = ip.getText();
	String userName = username.getText();
	String passWord = password.getText();
	
	/**本地检索**/
	boolean isFound = false;
	if(Constants.CONNECTIONS_TREE!=null)
	{
		for(VMTreeObject o : Constants.CONNECTIONS_TREE.getChildren())
		{
			if(o.getItemState().equals(ItemState.able))
			{
				/**单机状态**/
				if(o instanceof VMTreeObjectHost)
				{
					if(((VMTreeObjectHost) o).getIpAddress().equals(ipAddress))
					{
						isConnected = true;
						isPool = false;
						VMTreeObjectHost hostObject = (VMTreeObjectHost)o;
						Host host = (Host)hostObject.getApiObject();
						Host.Record record = (Host.Record)hostObject.getRecord();
						HostInfo hostInfo = new HostInfo(ipAddress,"root","onceas");
						hostList.add(hostInfo);
						masterHost = hostObject;
						isFound = true;
						break;
					}
				}/**资源池**/
				else if(o instanceof VMTreeObjectPool)
				{
					for(VMTreeObject o1: o.getChildren())
					{
						if(o1 instanceof VMTreeObjectHost)
						{
							VMTreeObjectHost hostObject = (VMTreeObjectHost)o1;
							Host host = (Host)hostObject.getApiObject();
							Host.Record record = (Host.Record)hostObject.getRecord();
							HostInfo hostInfo = new HostInfo(record.address,"root","onceas");
							hostList.add(hostInfo);
							if(hostObject.getIpAddress().equals(ipAddress))
							{
								isConnected = true;
								isFound = true;
								masterHost = hostObject;
								isPool = true;
							}
						}
					}
					/**资源池主机多于1个**/
					if(isFound&&isPool)
					{
						poolObject = (VMTreeObjectPool)o;
						String msgInfo = "更新资源池要对资源池所有主机节点执行更新操作\n本资源池中可能有多个节点，确定对资源池执行在线更新吗？";
						Image image = ImageRegistry.getImage(ImageRegistry.INFO);
						AlertMessageDialog dialog = new AlertMessageDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(),msgInfo,image,this);
						dialog.open();
						break;
					}
				}
			}
		}
	}
	if(isFound)
		return;
	if(hostList != null)
		hostList.clear();
	isConnected = false;
	
	/**远程获取**/
	connection = AddServerUtil.getConnection(ipAddress, userName, passWord);
	if(AddServerUtil.connectionRefused)
	{
		isPool = false;
		HostInfo hostInfo = new HostInfo(ipAddress,userName,passWord);
		hostList.add(hostInfo);
	}
	else
	{
		/**获取主机信息**/
		try {
			Map<Host,Host.Record> hostMap = Host.getAllRecords(connection);
			for(Host host : hostMap.keySet())
			{
				Host.Record record = hostMap.get(host);
				String add = record.address;
				HostInfo hostInfo = new HostInfo(record.address,"root","onceas");
				hostList.add(hostInfo);
			}
			Pool pool = Constants.getPool(connection);
			if(pool != null)
			{
				isPool = true;
				String msgInfo = "更新资源池要对资源池所有主机节点执行更新操作\n本资源池中可能有多个节点，确定对资源池执行在线更新吗？";
				Image image = ImageRegistry.getImage(ImageRegistry.INFO);
				AlertMessageDialog dialog = new AlertMessageDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(),msgInfo,image,this);
				dialog.open();
			}
		} catch (BadServerResponse e) {
			
			e.printStackTrace();
		} catch (XenAPIException e) {
			
			e.printStackTrace();
		} catch (XmlRpcException e) {
			
			e.printStackTrace();
		}
	}
	
	return;
}

public Connection getConnection() {
	return connection;
}

class AlertMessageDialog extends Dialog{

	private static final int OK_ID = 0;
	private static final String OK_LABEL = "确定";
	private static final int CANCLE_ID = 1;
	private static final String CANCLE_LABEL = "取消";
	private CLabel msgCLabel;
	private Image image;
	String msgInfo;
	private Shell shell;
	private HostAddressWizardPage page;
	public AlertMessageDialog(Shell parentShell, String msgInfo,Image image,HostAddressWizardPage page) {
		super(parentShell);
		
		this.msgInfo = msgInfo;
		this.image = image;
		this.page = page;
		shell = parentShell;
	}
	protected void configureShell(Shell shell)
	{
		super.configureShell(shell);
		shell.setText("警告信息提示");
		shell.setBackground(new Color(null,255,255,255));

	}
	
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = (Composite)super.createDialogArea(parent);
		msgCLabel = new CLabel(composite,SWT.NONE);
		msgCLabel.setImage(image);
		msgCLabel.setText(msgInfo);
		msgCLabel.setBounds(20, 60, 280, 90);
		return parent;
	}
	
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent,AlertMessageDialog.OK_ID,AlertMessageDialog.OK_LABEL, true);
		createButton(parent,AlertMessageDialog.CANCLE_ID,AlertMessageDialog.CANCLE_LABEL, true);
	}
	
	protected void buttonPressed(int buttonId)
	{
		if(AlertMessageDialog.CANCLE_ID == buttonId)
		{
			shell.dispose();
		}
		close();
	}
	
}

class HostInfo{
	private String ipAddress;
	private String userName;
	private String password;
	public HostInfo(String ipAddress, String userName, String password)
	{
		this.ipAddress = ipAddress;
		this.userName = userName;
		this.password = password;
	}
	
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
}
