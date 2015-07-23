package oncecenter.action.vm;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.wizard.adddesc.AddVMDescWizard;
import oncecenter.wizard.newvmfromtemp.NewVMDialog;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;

public class AddVMDescription extends Action {
private VMTreeObjectVM selection;
	
	public AddVMDescription(VMTreeObjectVM objectHost)
	{
		super("添加虚拟机描述信息");
		this.selection = objectHost;
//		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.ADJUST_CPU_MEMORY));
	}
	
	public void run(){
		AddVMDescWizard wizard = new AddVMDescWizard(selection);
		NewVMDialog dialog = new NewVMDialog(Display.getCurrent().getActiveShell(),
                wizard);
		dialog.setPageSize(150, 100);
		dialog.create();
		dialog.open();
	}
}