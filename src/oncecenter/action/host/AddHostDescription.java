package oncecenter.action.host;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.wizard.adddesc.AddHostDescWizard;
import oncecenter.wizard.newvmfromtemp.NewVMDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;

public class AddHostDescription extends Action {
private VMTreeObjectHost selection;
	
	public AddHostDescription(VMTreeObjectHost objectHost)
	{
		super("ÃÌº”√Ë ˆ–≈œ¢");
		this.selection = objectHost;
//		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.ADJUST_CPU_MEMORY));
	}
	
	public void run(){
		AddHostDescWizard wizard = new AddHostDescWizard(selection);
		NewVMDialog dialog = new NewVMDialog(Display.getCurrent().getActiveShell(),
                wizard);
		dialog.setPageSize(150, 100);
		dialog.create();
		dialog.open();
	}
}
