package oncecenter.wizard.importfromphysical;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

public abstract class ImportFromPhysicalPage extends WizardPage {

	protected ImportFromPhysicalPage(String pageName) {
		super(pageName);
		
	}

	@Override
	public void createControl(Composite parent) {
		

	}
	
	protected abstract boolean nextButtonClick();

}
