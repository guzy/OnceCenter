package oncecenter.p2v.wizard.fromlinux;

import oncecenter.p2v.MessageDialog;
import oncecenter.util.Ssh;
import oncecenter.wizard.newvmfromtemp.NewVMPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class P2vFromLinuxSourcePage extends NewVMPage {

	private Label sAddressLabel;
	private Text sAddressText;
	
	private Label sAddressUserLabel;
	private Text sAddressUserText;
	
	private Label sAddressPasLabel;
	private Text sAddressPasText;
	
	private String msgInfo = "";
	
	
	protected P2vFromLinuxSourcePage(String pageName) {
		super(pageName);
		
		this.setTitle("��д��ת���������ַ���û�����������Ϣ");
	}

	@Override
	public void createControl(Composite parent) {
		
		Composite composite = new Composite(parent,SWT.NULL);
		sAddressLabel = new Label(composite,SWT.NULL);
		sAddressLabel.setText("�������ַ");
		sAddressLabel.setBounds(20, 30, 100, 25);
		sAddressText = new Text(composite,SWT.BORDER);
		sAddressText.setBounds(130, 30, 310, 25);
		
		
		sAddressUserLabel = new Label(composite, SWT.NULL);
		sAddressUserLabel.setText("�û���");
		sAddressUserLabel.setBounds(20, 70, 100, 25);
		sAddressUserText = new Text(composite, SWT.BORDER);
		sAddressUserText.setBounds(130, 70, 310, 25);
		sAddressUserText.setText("root");
		
		
		sAddressPasLabel = new Label(composite,SWT.NULL);
		sAddressPasLabel.setText("����");
		sAddressPasLabel.setBounds(20, 110, 100, 25);
		sAddressPasText = new Text(composite,SWT.PASSWORD);
		sAddressPasText.setBounds(130,110,310,25);
		
		this.setControl(composite);
	}
	
	boolean isAddressValid(String address)
	{
		if(address.length() == 0 || address == null) {
			msgInfo = "��д���������ַ��Ϣ��Ч����������д";
			return false;
		}
		// do something
		Ssh ssh = new Ssh(sAddressText.getText(), sAddressUserText.getText(), sAddressPasText.getText());
		if(!ssh.Connect()) {
			msgInfo = "�������ַ���ɴ���û������������";
			ssh.CloseSsh();
			return false;
		}
		return true;
	}
	public Text getsAddressText() {
		return sAddressText;
	}

	protected boolean nextButtonClick()
	{
		if(!isAddressValid(sAddressText.getText()))
		{
			String msgInfo = "��д���������ַ��Ϣ��Ч����������д";
			MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell(),msgInfo);
			dialog.open();
			return false;
		}
		return true;
	}
	
	public Text getsAddressUserText() {
		return sAddressUserText;
	}

	public Text getsAddressPasText() {
		return sAddressPasText;
	}
	
	@Override
	public boolean canFlipToNextPage() {
		return true;
	}

}
