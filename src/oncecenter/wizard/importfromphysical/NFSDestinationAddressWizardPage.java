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
		
		this.setTitle("��дҪת������Ŀ���ַ�������Ϣ");
	}

	@Override
	public void createControl(Composite parent) {
		
		Composite composite = new Composite(parent,SWT.NULL);
		
		dAddressLabel = new Label(composite,SWT.NULL);
		dAddressLabel.setText("Ŀ�ĵ�ַ");
		dAddressLabel.setBounds(20, 30, 100, 25);
		dAddressText = new Text(composite, SWT.BORDER);
		dAddressText.setBounds(130,30,310,25);
		
		dAddressDirLabel = new Label(composite, SWT.NULL);
		dAddressDirLabel.setText("Ŀ�ĵ�ַĿ¼");
		dAddressDirLabel.setBounds(20,70,100,25);
		dAddressDirText = new Text(composite, SWT.BORDER);
		dAddressDirText.setBounds(130, 70, 310, 25);
		
		sAddressDirLabel = new Label(composite,SWT.NULL);
		sAddressDirLabel.setText("Դ��ַĿ¼");
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
//					String msgInfo = "��д��Ŀ�ĵ�ַ��Ϣ��Ч����������д";
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
			msgInfo = "��д��Ŀ�ĵ�ַ��Ϣ��Ч����������д";
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
			msgInfo = "Դ��ַ���ɴ���û������������";
			ssh.CloseSsh();
			return false;
		}
		try {		
			ret = ssh.Command(cmdBuffer.toString());
			if(ssh.getExitCode() != 0) {
				msgInfo = "����ʧ�ܣ�����Ŀ¼�Ƿ���ں�NFS�����Ƿ�����";
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
