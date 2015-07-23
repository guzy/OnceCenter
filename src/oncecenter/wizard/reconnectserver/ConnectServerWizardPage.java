package oncecenter.wizard.reconnectserver;

import java.util.Iterator;

import oncecenter.Constants;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ConnectServerWizardPage extends WizardPage {

	private Combo ip;
	private Text username;
	private Text password;
	
	private String initialIp;
	private String initialUser;

	public Combo getIp() {
		return ip;
	}
	
	public Text getUsername() {
		return username;
	}

	public Text getPassword() {
		return password;
	}

	/**
	 * Create the wizard.
	 */
	public ConnectServerWizardPage(String title) {
		super("博纳讯动服务器虚拟化软件");
//		super("网驰平台");		
		setTitle("重新登录");
		setDescription("请输入密码。");
		//setDescription("Enter the IP address of the server you want to add  and your user \nlogin credentials for that server.");
	}
	
	/**
	 * Create the wizard.
	 */
	public ConnectServerWizardPage(String title,String ip,String username) {
		super("博纳讯动服务器虚拟化软件");
//		super("网驰平台");
		setTitle("重新登录");
		setDescription("请输入用户名和密码重新进行连接");
		//setDescription("Enter the IP address of the server you want to add  and your user \nlogin credentials for that server.");
		this.initialIp=ip;
		this.initialUser=username;
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(2,false));
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		//composite.setLayoutData(gridData);		
		
		new Label(composite, SWT.NONE).setText("IP地址:");
		ip = new Combo(composite, SWT.BORDER);
		ip.setLayoutData(gridData);
		for(Iterator<String> i = Constants.historyServer.iterator();i.hasNext();){
			ip.add(i.next());
		}
		if(initialIp!=null&&!initialIp.equals("")){
			ip.setText(initialIp);
//		}else{
//			ip.setText("133.133.134.51");
		}
		//ip.setText("133.133.132.1");
		
		new Label(composite, SWT.NONE).setText("用户名:");
		username = new Text(composite, SWT.BORDER);
		username.setLayoutData(gridData);
		//username.setText("root");
		if(initialUser!=null&&!initialUser.equals("")){
			username.setText(initialUser);
//		}else{
//			username.setText("admin");
		}
		username.setText("root");
		
		new Label(composite, SWT.NONE).setText("密码:");
		password = new Text(composite, SWT.PASSWORD);
		password.setLayoutData(gridData);
		password.setFocus();
		//password.setText("oncecloud");
		//password.setText("onceas");
		
		
		setControl(composite);
	}
}
