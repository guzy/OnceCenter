package oncecenter.action;

import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.wizard.newpool.NewPoolWizard;
import oncecenter.wizard.newvmfromtemp.NewVMDialog;
import oncecenter.wizard.recoverlostvm.RecorverLostVmWizard;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;

public class RecoverVMAction extends Action {
	VMTreeObjectRoot selection;
	Connection connection;
	
	public RecoverVMAction(VMTreeObjectRoot selection){
		super();
		this.selection=selection;
		connection=selection.getConnection();
		setText("»Ö¸´¶ªÊ§µÄÐéÄâ»ú");		
	}
	
	public void run()
	{
		RecorverLostVmWizard wizard = new RecorverLostVmWizard(selection);
		NewVMDialog dialog = new NewVMDialog(Display.getCurrent().getActiveShell(),
                wizard);
		dialog.setPageSize(400, 300);
		dialog.create();
		dialog.open();
	}
}
