package oncecenter.wizard.newserver;

import java.util.Iterator;

import oncecenter.Constants;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import twaver.base.A.F.E;

public class NewServerWizardPage extends WizardPage {

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
	public NewServerWizardPage(String title) {
//		super("����ƽ̨");
		super("����Ѷ�����������⻯���");		
		setTitle("��¼");
		setDescription("��������Ҫ���ӵ���������Դ�ص�IP��ַ��");
		//setDescription("Enter the IP address of the server you want to add  and your user \nlogin credentials for that server.");
	}
	
	/**
	 * Create the wizard.
	 */
	public NewServerWizardPage(String title,String ip,String username) {
		super("����Ѷ�����������⻯���");
//		super("����ƽ̨");
		setTitle("��Դ������ʧЧ");
		setDescription("��Դ������ʧЧ��ԭ����������ڵ�崻�����Դ�������·������ڵ㣬\n�������û������������½�������");
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
		
		new Label(composite, SWT.NONE).setText("IP��ַ:");
		ip = new Combo(composite, SWT.BORDER);
		ip.setLayoutData(gridData);
		for(Iterator<String> i = Constants.historyServer.iterator();i.hasNext();){
			ip.add(i.next());
		}
		if(initialIp!=null&&!initialIp.equals("")){
			ip.setText(initialIp);
		}
		ip.addModifyListener(new ModifyListener() {  
            public void modifyText(ModifyEvent e) {  
            	setPageState();
            }  
        });  
		
		new Label(composite, SWT.NONE).setText("�û���:");
		username = new Text(composite, SWT.BORDER);
		username.setLayoutData(gridData);
		username.setText("root");
		if(initialUser!=null&&!initialUser.equals("")){
			username.setText(initialUser);
		}
		username.addModifyListener(new ModifyListener() {  
            public void modifyText(ModifyEvent e) {  
            	setPageState();
            }  
        });  
		
		new Label(composite, SWT.NONE).setText("����:");
		password = new Text(composite, SWT.PASSWORD);
		password.setLayoutData(gridData);
		
		password.addModifyListener(new ModifyListener() {  
            public void modifyText(ModifyEvent e) {  
            	setPageState();
            }  
        });  
		
		setPageComplete(false);
		setPageState();
		
		setControl(composite);
	}

	public void setPageState(){
		if(ip.getText().length()>0&&username.getText().length()>0&&password.getText().length()>0)
			setPageComplete(true);
		else
			setPageComplete(false);
	}
}
