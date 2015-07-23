package oncecenter.action;

import oncecenter.util.ImageRegistry;
import oncecenter.wizard.newvmfromtemp.NewVMDialog;
import oncecenter.wizard.recover.RecoverWizard;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;

public class RecorverAction extends OnceAction {

	public RecorverAction(String text,String image,String disabledImage){
		super(text,image,disabledImage);
	}
	
	public void run(){
		RecoverWizard wizard = new RecoverWizard();
		NewVMDialog dialog = new NewVMDialog(Display.getCurrent().getActiveShell(),
                wizard);
		dialog.setPageSize(200, 100);
		dialog.create();
		dialog.open();
	}
}
