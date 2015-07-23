package oncecenter.action.vm;

import oncecenter.util.ImageRegistry;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.wizard.changeiso.ChangeVMIsoWizard;
import oncecenter.wizard.editvmdisk.EditVmDiskWizard;
import oncecenter.wizard.newvmfromtemp.NewVMDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;

public class ChangeIsoAction extends Action {
	private VMTreeObjectVM selection;
	public ChangeIsoAction(VMTreeObjectVM objectVM)
	{
		super("¸ü»»¹âÅÌ");
		this.selection = objectVM;
	}
	public void run()
	{
		ChangeVMIsoWizard wizard = new ChangeVMIsoWizard(selection);
		NewVMDialog dialog = new NewVMDialog(Display.getCurrent().getActiveShell(),
                wizard);
		dialog.setPageSize(400, 300);
		dialog.create();
		dialog.open();
	}
}
