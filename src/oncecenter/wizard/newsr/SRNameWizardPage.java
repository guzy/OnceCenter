package oncecenter.wizard.newsr;

import oncecenter.util.TypeUtil;
import oncecenter.wizard.newvmfromtemp.FinishWizardPage;
import oncecenter.wizard.newvmfromtemp.NewVMPage;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

public class SRNameWizardPage extends NewVMPage {
	private Text text;
	public Text getText() {
		return text;
	}

	public Text getText_1() {
		return text_1;
	}

	private Text text_1;
	private Button btnCheckButton;

	/**
	 * Create the wizard.
	 */
	public SRNameWizardPage() {
		super("wizardPage");
		setTitle("新建存储");
		setDescription("请输入要新建的存储名称和描述，描述可以自动生成");
		this.setPageComplete(false);
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		
		Label lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setBounds(53, 47, 61, 17);
		lblNewLabel.setText("\u540D\u79F0");
		
		text = new Text(container, SWT.BORDER);
		text.setBounds(137, 44, 343, 23);
		
		text.addVerifyListener(new VerifyListener(){

			@Override
			public void verifyText(VerifyEvent arg0) {
				
				if(text.getText() != null)
					setPageComplete(true);
				else
					setErrorMessage("请输入存储名！");
			}
		});
		
		btnCheckButton = new Button(container, SWT.CHECK);
		btnCheckButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(text_1.getEnabled()){
					text_1.setEnabled(false);
				}else{
					text_1.setEnabled(true);
				}
				
			}
		});
		btnCheckButton.setBounds(53, 82, 111, 17);
		btnCheckButton.setText("\u81EA\u52A8\u751F\u6210\u63CF\u8FF0");
		btnCheckButton.setSelection(true);
		
		Label lblNewLabel_1 = new Label(container, SWT.NONE);
		lblNewLabel_1.setBounds(53, 131, 61, 17);
		lblNewLabel_1.setText("\u63CF\u8FF0");
		
		text_1 = new Text(container, SWT.BORDER);
		text_1.setBounds(137, 128, 343, 79);
		text_1.setEnabled(false);
	}
	
	public boolean isDefault(){
		return btnCheckButton.getSelection();
	}
	
	@Override
	protected boolean nextButtonClick() {
		
		 IWizardPage nextPage = getWizard().getNextPage(this);
		 if(nextPage instanceof SRLocationWizardPage)
			 ((SRLocationWizardPage)nextPage).refresh();
		
		return true;
	}
}
