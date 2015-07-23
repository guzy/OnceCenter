package oncecenter.wizard.importfromphysical;

import oncecenter.wizard.newvmfromtemp.NewVmFTWizard;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ImportFromPhysicalWizardPage extends ImportFromPhysicalPage {


	Button sshRadioButton;
	Button nfsRadioButton;
	boolean flag;
	Label test;
	protected ImportFromPhysicalWizardPage(String pageName) {
		super(pageName);
		
		this.setTitle("选取导入方式");
		this.setDescription("	");
	}

	@Override
	public void createControl(Composite parent) {
		
		Composite composite = new Composite(parent,SWT.NULL);
		Label sshLabel = new Label(composite, SWT.NONE);
		sshLabel.setText("SSH方式");
		sshLabel.setBounds(66, 40, 61, 17);
		sshRadioButton = new Button(composite, SWT.RADIO);
		sshRadioButton.setBounds(86, 70, 97, 17);
		sshRadioButton.setText(" SSH");
		sshRadioButton.setSelection(true);
		
		Label nfsLabel = new Label(composite, SWT.NONE);
		nfsLabel.setText("NFS方式");
		nfsLabel.setBounds(266, 40, 61, 17);
		nfsRadioButton = new Button(composite, SWT.RADIO);
		nfsRadioButton.setBounds(286, 70, 97, 17);
		nfsRadioButton.setText(" NFS");
		
		this.setControl(composite);
	}
	
	public IWizardPage getNextPage()
	{
		if(nfsRadioButton.getSelection())
		{	
			((ImportFromPhysicalWizard)this.getWizard()).isSSH = false;
		}
		else 
		{
			((ImportFromPhysicalWizard)this.getWizard()).isSSH = true;
		}
		return super.getNextPage();
	}
	
	protected boolean nextButtonClick()
	{	
		return true;
	}

}
