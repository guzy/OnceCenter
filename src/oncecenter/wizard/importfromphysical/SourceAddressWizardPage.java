package oncecenter.wizard.importfromphysical;

import oncecenter.util.Ssh;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class SourceAddressWizardPage extends ImportFromPhysicalPage {

	private Label sAddressLabel;
	private Text sAddressText;
	
	private Label sAddressUserLabel;
	private Text sAddressUserText;
	
	private Label sAddressPasLabel;
	private Text sAddressPasText;
	
	private String msgInfo = "";
	
	protected SourceAddressWizardPage(String pageName) {
		super(pageName);
		
		this.setTitle("填写被转换物理机的相关地址信息");
		//setPageComplete(false);
	}

	@Override
	public void createControl(Composite parent) {
		
		Composite composite = new Composite(parent,SWT.NULL);
		sAddressLabel = new Label(composite,SWT.NULL);
		sAddressLabel.setText("源地址");
		sAddressLabel.setBounds(20, 30, 100, 25);
		sAddressText = new Text(composite,SWT.BORDER);
		sAddressText.setBounds(130, 30, 310, 25);
		
		
		sAddressUserLabel = new Label(composite, SWT.NULL);
		sAddressUserLabel.setText("源地址用户名");
		sAddressUserLabel.setBounds(20, 70, 100, 25);
		sAddressUserText = new Text(composite, SWT.BORDER);
		sAddressUserText.setBounds(130, 70, 310, 25);
		sAddressUserText.setText("root");
		
		
		sAddressPasLabel = new Label(composite,SWT.NULL);
		sAddressPasLabel.setText("源地址密码");
		sAddressPasLabel.setBounds(20, 110, 100, 25);
		sAddressPasText = new Text(composite,SWT.PASSWORD);
		sAddressPasText.setBounds(130,110,310,25);
		
		this.setControl(composite);
	}
	
	public IWizardPage getNextPage()
	{
		boolean flag = ((ImportFromPhysicalWizard)this.getWizard()).isSSH;
		if(!flag)
		{	
			return ((ImportFromPhysicalWizard)this.getWizard()).nfsDaPage;
		}
		else 
		{
			return ((ImportFromPhysicalWizard)this.getWizard()).sshDaPage;
		}
	}
	
	boolean isAddressValid(String address)
	{
		if(address.length() == 0 || address == null) {
			msgInfo = "填写的源地址信息无效，请重新填写";
			return false;
		}
		// do something
		Ssh ssh = new Ssh(sAddressText.getText(), sAddressUserText.getText(), sAddressPasText.getText());
		if(!ssh.Connect()) {
			msgInfo = "源地址不可达，或用户名，密码错误！";
			ssh.CloseSsh();
			return false;
		}
		return true;
	}
	public Text getsAddressText() {
		return sAddressText;
	}

	@Override
	protected boolean nextButtonClick()
	{
		if(!isAddressValid(sAddressText.getText()))
		{
			String msgInfo = "填写的源地址信息无效，请重新填写";
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


	
}
