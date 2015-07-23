package oncecenter.action.vm;

import oncecenter.util.ImageRegistry;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.wizard.newvmfromtemp.CpuMemoryWizard;
import oncecenter.wizard.newvmfromtemp.NewVMDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;

public class AdjustCpuAndMemoryAction extends Action {

	private VMTreeObjectVM selection;
	
	public AdjustCpuAndMemoryAction(VMTreeObjectVM objectVM)
	{
		super("调整CPU和内存");
		this.selection = objectVM;
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.ADJUST_CPU_MEMORY));
	}
	
	public void run(){
		CpuMemoryWizard wizard = new CpuMemoryWizard(selection);
		NewVMDialog dialog = new NewVMDialog(Display.getCurrent().getActiveShell(),
                wizard);
		dialog.setPageSize(400, 300);
		dialog.create();
		dialog.open();
	}
}
