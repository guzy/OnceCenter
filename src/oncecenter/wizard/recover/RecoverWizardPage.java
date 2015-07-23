package oncecenter.wizard.recover;

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

public class RecoverWizardPage extends WizardPage {

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
	public RecoverWizardPage() {
		super("博纳讯动服务器虚拟化软件");
//		super("网驰平台");
		setTitle("修复");
		setDescription("请输入你要修复的主机IP地址。");
		//setDescription("Enter the IP address of the server you want to add  and your user \nlogin credentials for that server.");
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
		username.setText("root");
		if(initialUser!=null&&!initialUser.equals("")){
			username.setText(initialUser);
//		}else{
//			username.setText("admin");
		}
		//username.setText("root");
		
		new Label(composite, SWT.NONE).setText("密码:");
		password = new Text(composite, SWT.PASSWORD);
		password.setLayoutData(gridData);
		
		//password.setText("oncecloud");
		//password.setText("onceas");
		
		
		setControl(composite);
	}
}
