package oncecenter.wizard.periodicsnapshot;

import org.eclipse.jface.wizard.Wizard;

public class PeriodicSnapshotWizard extends Wizard {

	PeriodicSnapshotWizardPage psPage;
	
	@Override
	public void addPages()
	{
		psPage = new PeriodicSnapshotWizardPage("psPage");
		this.addPage(psPage);
	}
	@Override
	public boolean performFinish() {
		
		return false;
	}

}
