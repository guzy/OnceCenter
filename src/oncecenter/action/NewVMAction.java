package oncecenter.action;

import oncecenter.Constants;
import oncecenter.util.ImageRegistry;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.wizard.newvmfromtemp.NewVMDialog;
import oncecenter.wizard.newvmfromtemp.NewVmFTWizard;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;

public class NewVMAction extends OnceAction {
	
	private VMTreeObject selection;
	
	public NewVMAction(){
		super();
	}
	
	public NewVMAction(String text,String image,String disabledImage){
		super(text,image,disabledImage);
		
	}
	
	public NewVMAction(VMTreeObject selection){
		super();
		this.selection=selection;
		setText("ÐÂ½¨ÐéÄâ»ú");		
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.ADDVM));
	}

	public void run(){
		if(selection==null){
			StructuredSelection select = (StructuredSelection)Constants.treeView.getViewer().getSelection();
			selection = (VMTreeObject)select.getFirstElement();
		}
		NewVmFTWizard wizard = new NewVmFTWizard(selection);
		NewVMDialog dialog = new NewVMDialog(Display.getCurrent().getActiveShell(),
				                 wizard);
		dialog.setPageSize(400, 300);
		dialog.create();
		dialog.open();
	}
}
