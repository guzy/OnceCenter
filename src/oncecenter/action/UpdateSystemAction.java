package oncecenter.action;

import oncecenter.Constants;
import oncecenter.util.ImageRegistry;
import oncecenter.util.hasPoolUtil;
import oncecenter.wizard.newvmfromtemp.NewVMDialog;
import oncecenter.wizard.upgradeSystem.UpgradeSystemWizard;
import oncecenter.wizard.uploadisofile.UploadIsoWizard;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class UpdateSystemAction extends OnceAction {
	
	public UpdateSystemAction(String text,String image,String disabledImage){
		super(text,image,disabledImage);
	}
	
	public UpdateSystemAction()
	{
		super();
		setText("在线更新");		
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.UPGRADE_DIS));
		//setDisabledImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.UPGRADE_DIS));
	}

	public void run(){
		
		UpgradeSystemWizard wizard = new UpgradeSystemWizard();
		NewVMDialog dialog = new NewVMDialog(Display.getCurrent().getActiveShell(),
                wizard);
		dialog.setPageSize(200, 200);
		dialog.create();
		dialog.open();
	}
}
