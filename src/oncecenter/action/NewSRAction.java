package oncecenter.action;

import oncecenter.util.ImageRegistry;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.wizard.newsr.NewSRWizard;
import oncecenter.wizard.newvmfromtemp.NewVMDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;

public class NewSRAction extends OnceAction {
	private VMTreeObjectRoot selection;
	
	public NewSRAction(){
		super();
	}
	
	public NewSRAction(String text,String image,String disabledImage){
		super(text,image,disabledImage);
	}
	
	public NewSRAction(VMTreeObjectRoot selection){
		super();
		this.selection=selection;
		setText("ÐÂ½¨´æ´¢");		
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.ADDSTORAGE));
	}
	
	public void run(){
		NewSRWizard wizard = new NewSRWizard(selection);
		NewVMDialog dialog = new NewVMDialog(Display.getCurrent().getActiveShell(),
                wizard);
		dialog.setPageSize(400, 300);
		dialog.create();
		dialog.open();
	}
}
