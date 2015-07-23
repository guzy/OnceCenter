package oncecenter.action.vm;

import oncecenter.util.ImageRegistry;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.wizard.editvmdisk.EditVmDiskWizard;
import oncecenter.wizard.newvmfromtemp.NewVMDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;

public class EditVmDiskAction extends Action {

	private VMTreeObjectVM selection;
	public EditVmDiskAction(VMTreeObjectVM objectVM)
	{
		super("–ﬁ∏ƒπ‚œÀ…Ë÷√");
		this.selection = objectVM;
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.EDITDISK));
	}
	public void run()
	{
		EditVmDiskWizard wizard = new EditVmDiskWizard(selection);
		NewVMDialog dialog = new NewVMDialog(Display.getCurrent().getActiveShell(),
                wizard);
		dialog.setPageSize(400, 300);
		dialog.create();
		dialog.open();
	}
}
