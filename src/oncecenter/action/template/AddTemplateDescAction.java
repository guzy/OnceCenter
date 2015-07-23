package oncecenter.action.template;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectTemplate;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.wizard.adddesc.AddTemplateDescWizard;
import oncecenter.wizard.adddesc.AddVMDescWizard;
import oncecenter.wizard.newvmfromtemp.NewVMDialog;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;

public class AddTemplateDescAction extends Action {
private VMTreeObjectTemplate selection;
	
	public AddTemplateDescAction(VMTreeObjectTemplate objectHost)
	{
		super("添加模板描述信息");
		this.selection = objectHost;
//		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.ADJUST_CPU_MEMORY));
	}
	
	public void run(){
		AddTemplateDescWizard wizard = new AddTemplateDescWizard(selection);
		NewVMDialog dialog = new NewVMDialog(Display.getCurrent().getActiveShell(),
                wizard);
		dialog.setPageSize(150, 100);
		dialog.create();
		dialog.open();
	}
}