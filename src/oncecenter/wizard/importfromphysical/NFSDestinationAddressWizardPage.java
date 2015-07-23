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

public class NFSDestinationAddressWizardPage extends ImportFromPhysicalPage {

	private Label dAddressLabel;
	private Text dAddressText;
	
	private Label dAddressDirLabel;
	private Text dAddressDirText;
	
	private Label sAddressDirLabel;
	private Text sAddressDirText;
	
	private String msgInfo = "";
	
	protected NFSDestinationAddressWizardPage(String pageName) {
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
		
		dAddressDirLabel = new Label(composite, SWT.NULL);
		dAddressDirLabel.setText("目的地址目录");
		dAddressDirLabel.setBounds(20,70,100,25);
		dAddressDirText = new Text(composite, SWT.BORDER);
		dAddressDirText.setBounds(130, 70, 310, 25);
		
		sAddressDirLabel = new Label(composite,SWT.NULL);
		sAddressDirLabel.setText("源地址目录");
		sAddressDirLabel.setBounds(20, 110, 100, 25);
		sAddressDirText = new Text(composite, SWT.BORDER);
		sAddressDirText.setBounds(130,110,310,25);
		

//		dAddressText.addKeyListener(new KeyListener(){
//
//			@Override
//			public void keyPressed(KeyEvent arg0) {
//				
//				
//			}
//
//			@Override
//			public void keyReleased(KeyEvent arg0) {
//				
//				if(!isAddressValid(dAddressText.getText()))
//				{
//					String msgInfo = "填写的目的地址信息无效，请重新填写";
//					MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell(),msgInfo);
//					dialog.open();
//					setPageComplete(false);
//				}
//				else
//				{
//					setPageComplete(true);
//				}
//			}
//			
//		});
		
		this.setControl(composite);
	}
	
	boolean isAddressValid(String address)
	{
		if(address.length() == 0 || address == null) {
			msgInfo = "填写的目的地址信息无效，请重新填写";
			return false;
		}
		// do something
		SourceAddressWizardPage saPae = ((ImportFromPhysicalWizard)this.getWizard()).saPage;
		String ret = "";
		StringBuffer cmdBuffer = new StringBuffer("mount ");
		cmdBuffer.append(dAddressText.getText().trim());
		cmdBuffer.append(":");
		cmdBuffer.append(dAddressDirText.getText().trim());
		cmdBuffer.append(" ");
		cmdBuffer.append(sAddressDirText.getText().trim());
		Ssh ssh = new Ssh(saPae.getsAddressText().getText(), saPae.getsAddressUserText().getText(), saPae.getsAddressPasText().getText());
		if(!ssh.Connect()) {
			msgInfo = "源地址不可达，或用户名，密码错误！";
			ssh.CloseSsh();
			return false;
		}
		try {		
			ret = ssh.Command(cmdBuffer.toString());
			if(ssh.getExitCode() != 0) {
				msgInfo = "挂载失败，请检查目录是否存在和NFS服务是否开启！";
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
	
	public Text getdAddressText() {
		return dAddressText;
	}

	public Text getdAddressDirText() {
		return dAddressDirText;
	}

	protected boolean nextButtonClick()
	{
		if(!isAddressValid(dAddressText.getText()))
		{
			MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell(),msgInfo);
			dialog.open();
			return false;
		}
		return true;
	}

	public Text getsAddressDirText() {
		return sAddressDirText;
	}
	
	public IWizardPage getNextPage()
	{
		return ((ImportFromPhysicalWizard)this.getWizard()).nfsFinPage;
	}
	
}
