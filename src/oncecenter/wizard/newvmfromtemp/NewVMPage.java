package oncecenter.wizard.newvmfromtemp;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

public abstract class NewVMPage extends WizardPage {

	protected NewVMPage(String pageName) {
		super(pageName);
		
	}

	@Override
	public void createControl(Composite parent) {
		

	}

	protected boolean nextButtonClick(){
		return true;
	}

	protected boolean finishButtonClick() {
		
		return true;
	}


}
