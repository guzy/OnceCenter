package oncecenter.action.vm;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.wizard.managedisk.AddVMDiskWizard;
import oncecenter.wizard.newvmfromtemp.NewVMDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;

public class AddVMDiskAction extends Action {
private VMTreeObjectVM selection;
	
	public AddVMDiskAction(VMTreeObjectVM objectVM)
	{
		super("管理虚拟机挂载的硬盘");
		this.selection = objectVM;
	}
	
	public void run(){
		AddVMDiskWizard wizard = new AddVMDiskWizard(selection);
		NewVMDialog dialog = new NewVMDialog(Display.getCurrent().getActiveShell(),
                wizard);
		dialog.setPageSize(420, 400);
		dialog.create();
		dialog.open();
	}
}