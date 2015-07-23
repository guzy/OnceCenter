package oncecenter.wizard.importfromphysical;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NFSFinishWizardPage extends ImportFromPhysicalPage {

	private Label fileNameLabel;
	private Text fileNameText;
	
	protected NFSFinishWizardPage(String pageName) {
		super(pageName);
		
		this.setTitle("填写被转换文件的文件名");
	}

	@Override
	public void createControl(Composite parent) {
		
		Composite composite = new Composite(parent,SWT.NULL);
		
		fileNameLabel = new Label(composite, SWT.NULL);
		fileNameLabel.setText("文件名");
		fileNameLabel.setBounds(20,30,100,25);
		fileNameText = new Text(composite, SWT.BORDER);
		fileNameText.setBounds(130, 30, 310, 25);
		
		this.setControl(composite);
	}
	
	public Text getFileNameText() {
		return fileNameText;
	}
	
	@Override
	 public boolean canFlipToNextPage()
	{
		return false;
	}

	@Override
	protected boolean nextButtonClick() {
		
		return true;
	}

}
