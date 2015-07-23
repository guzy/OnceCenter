package oncecenter.wizard.adddesc;

import oncecenter.wizard.newvmfromtemp.NewVMPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class AddHostDescWizardPage extends NewVMPage {

	Text description;

	public boolean canFinish() {
		return true;
	}

	/**
	 * Create the wizard.
	 */
	public AddHostDescWizardPage() {
		super("wizardPage");
		setTitle("添加描述信息");
	}
	
	public String getDescription(){
		return description.getText();
	}
	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite comParent = new Composite(parent,SWT.NONE);
		comParent.setLayout(new GridLayout(2, false));		
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		comParent.setLayoutData(gridData);
		
		new Label(comParent, SWT.NONE).setText("描述信息:");
		description = new Text(comParent, SWT.BORDER);
		description.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		description.setText("请在此输入描述信息");
		setControl(comParent);
	}

}
