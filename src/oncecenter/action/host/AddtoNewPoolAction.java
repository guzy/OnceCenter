package oncecenter.action.host;

import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.wizard.newpool.NewPoolWizard;
import oncecenter.wizard.newvmfromtemp.NewVMDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;

/*
 * δʵ��
 */
public class AddtoNewPoolAction extends Action {
	VMTreeObjectHost selection;
	Connection connection;

	public AddtoNewPoolAction(){
		super();
		setText("����Դ��");		
	}
	public AddtoNewPoolAction(VMTreeObjectHost selection){
		super();
		this.selection=selection;
		connection=selection.getConnection();
		setText("����Դ��");		
	}
	
	public void run()
	{
		NewPoolWizard wizard = new NewPoolWizard();
		NewVMDialog dialog = new NewVMDialog(Display.getCurrent().getActiveShell(),
                wizard);
		dialog.setPageSize(350, 250);
		dialog.create();
		dialog.open();
		
		VMTreeView viewer = (VMTreeView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
		        .findView(VMTreeView.ID);
		viewer.getViewer().expandAll();
	}
}
