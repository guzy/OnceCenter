package oncecenter.action.host;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.wizard.bandtoovs.BondtoOvsWizard;
import oncecenter.wizard.newvmfromtemp.NewVMDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;

public class BondtoOvsAction extends Action {

	VMTreeObjectHost host;
	
	public BondtoOvsAction(VMTreeObjectHost host){
		super();
		this.host = host;
		setText("ÍøÂçÐéÄâ»¯");
	}
	
	public void run(){
		BondtoOvsWizard wizard = new BondtoOvsWizard(host);
		NewVMDialog dialog = new NewVMDialog(Display.getCurrent().getActiveShell(),
                wizard);
		dialog.setPageSize(400, 300);
		dialog.create();
		dialog.open();
	}
}
