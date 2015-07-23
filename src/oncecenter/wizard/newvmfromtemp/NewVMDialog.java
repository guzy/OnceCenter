package oncecenter.wizard.newvmfromtemp;

import oncecenter.Constants;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class NewVMDialog extends WizardDialog {

	public NewVMDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
		
	}
	IWizardPage nextPage = null;
	@Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.NEXT_ID){
            if (actionWhenNextButtonPressed()) {
                super.buttonPressed(buttonId);
            }
        }else if(buttonId == IDialogConstants.FINISH_ID){
        	if (actionWhenFinishButtonPressed()) {
                super.buttonPressed(buttonId);
            }
        }
        else {
            super.buttonPressed(buttonId);
        }
    }
	
	 protected boolean actionWhenNextButtonPressed() {

		 IWizardPage currentPage = getWizard().getContainer().getCurrentPage();
		 if(currentPage instanceof NewVMPage){
			 return ((NewVMPage) currentPage).nextButtonClick();
		 }else{
			 return true;
		 }
	 }

	 protected boolean actionWhenFinishButtonPressed() {

		 IWizardPage currentPage = getWizard().getContainer().getCurrentPage();
		 if(currentPage instanceof NewVMPage){
			 return ((NewVMPage) currentPage).finishButtonClick();
		 }else{
			 return true;
		 }
	 }
	 protected void createButtonsForButtonBar(Composite parent)
	{
		  super.createButtonsForButtonBar(parent);
		  Button cancelButton = super.getButton(IDialogConstants.CANCEL_ID);
		  if(cancelButton!=null)
			  cancelButton.setText(Constants.cancelButtonText);
		  Button finishButton = super.getButton(IDialogConstants.FINISH_ID);
		  if(finishButton!=null)
			  finishButton.setText(Constants.finishButtonText);
		  Button backButton = super.getButton(IDialogConstants.BACK_ID);
		  if(backButton!=null)
			  backButton.setText(Constants.backButtonText);
		  Button nextButton = super.getButton(IDialogConstants.NEXT_ID);
		  if(nextButton!=null)
			  nextButton.setText(Constants.nextButtonText);

	}

}
