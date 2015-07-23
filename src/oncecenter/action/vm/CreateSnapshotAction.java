package oncecenter.action.vm;

import oncecenter.maintabs.vm.VMSnapShotsTab;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.wizard.newsnapshot.NewSnapshotWizard;
import oncecenter.wizard.newvmfromtemp.NewVMDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;


public class CreateSnapshotAction extends Action {

	private VMTreeObjectVM objectVM;
	private VMSnapShotsTab tab;
	public CreateSnapshotAction(VMTreeObjectVM objectVM,VMSnapShotsTab tab)
	{
		super();
		this.objectVM = objectVM;
		this.tab = tab;
		setText("¥¥Ω®øÏ’’");
	}
	
	public void run(){
		NewSnapshotWizard wizard = new NewSnapshotWizard(objectVM,tab);
		NewVMDialog dialog = new NewVMDialog(Display.getCurrent().getActiveShell(),
                wizard);
		dialog.setPageSize(250, 200);
		dialog.create();
		dialog.open();
	}
	
}
