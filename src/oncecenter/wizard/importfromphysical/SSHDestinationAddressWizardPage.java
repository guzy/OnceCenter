package oncecenter.wizard.importfromphysical;

import java.io.IOException;

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

public class SSHDestinationAddressWizardPage extends ImportFromPhysicalPage {

	private Label dAddressLabel;
	private Text dAddressText;
	
	private Label dAddressUserLabel;
	private Text dAddressUserText;
	
	private Label dAddressPasLabel;
	private Text dAddressPasText;
	
	private Label dAddressDirLabel;
	private Text dAddressDirText;
	
	private String msgInfo = "";
	protected SSHDestinationAddressWizardPage(String pageName) {
		super(pageName);
		
		this.setTitle("填写要转换到的目标地址的相关信息");
	}

	
	@Override
	public void createControl(Composite parent) {
		
		Composite composite = new Composite(parent,SWT.NULL);
		
		dAddressLabel = new Label(composite,SWT.NULL);
		dAddressLabel.setText("目的地址");
		dAddressLabel.setBounds(20, 30, 100, 25);
		dAddressText = new Text(composite, SWT.BORDER);
		dAddressText.setBounds(130,30,310,25);
		
		dAddressUserLabel = new Label(composite,SWT.NULL);
		dAddressUserLabel.setText("目的地址用户名");
		dAddressUserLabel.setBounds(20, 70, 100, 25);
		
		dAddressUserText = new Text(composite,SWT.BORDER);
		dAddressUserText.setBounds(130,70,310,25);
		dAddressUserText.setText("root");
		
		dAddressPasLabel = new Label(composite,SWT.NULL);
		dAddressPasLabel.setText("目的地址密码");
		dAddressPasLabel.setBounds(20, 110, 100, 25);
		dAddressPasText = new Text(composite,SWT.PASSWORD);
		dAddressPasText.setBounds(130,110,310,25);
		
		dAddressDirLabel = new Label(composite, SWT.NULL);
		dAddressDirLabel.setText("目的地址目录");
		dAddressDirLabel.setBounds(20,150,100,25);
		dAddressDirText = new Text(composite, SWT.BORDER);
		dAddressDirText.setBounds(130, 150, 310, 25);

		
		this.setControl(composite);
	}
	
	boolean isAddressValid(String address)
	{
		if(address.length() == 0 || address == null) {
			msgInfo = "填写的目的地址信息无效，请重新填写！";
			return false;
		}
		// do something
		String ret = "";
		Ssh ssh = new Ssh(dAddressText.getText().trim(), dAddressUserText.getText().trim(), dAddressPasText.getText().trim());
		if(!ssh.Connect()) {
			msgInfo = "目的地址不可达，或用户名，密码错误！";
			ssh.CloseSsh();
			return false;
		}
		
		String dir = dAddressDirText.getText().trim();
		if(dir.length() == 0 || dir == null) {
			msgInfo = "填写的目的地址目录信息无效，请重新填写！";
			return false;
		}
		StringBuffer cmdBuffer = new StringBuffer("ls ");
		cmdBuffer.append(dir);
		try {		
			ret = ssh.Command(cmdBuffer.toString());
			if(ssh.getExitCode() != 0) {
				msgInfo = "目的地址目录不存在！";
				return false;
			}
		} catch (IOException e) {
			msgInfo = e.toString();
			return false;
		} catch (Exception e) {
			msgInfo = e.toString();
			return false;
		} finally {
			ssh.CloseSsh();
		}
		
		return true;
	}
	
	public Text getdAddressPasText() {
		return dAddressPasText;
	}
	
	public Text getdAddressText() {
		return dAddressText;
	}

	public Text getdAddressUserText() {
		return dAddressUserText;
	}
	
	public IWizardPage getNextPage()
	{
		return ((ImportFromPhysicalWizard)this.getWizard()).sshFinPage;
	}

	public Text getdAddressDirText() {
		return dAddressDirText;
	}

	@Override
	protected boolean nextButtonClick() {
		
		if(!isAddressValid(dAddressText.getText()))
		{
			MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell(),msgInfo);
			dialog.open();
			return false;
		}
		return true;
	}
	
	

}
