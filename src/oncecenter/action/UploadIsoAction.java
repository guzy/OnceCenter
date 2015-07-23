package oncecenter.action;

import oncecenter.util.hasPoolUtil;
import oncecenter.wizard.newvmfromtemp.NewVMDialog;
import oncecenter.wizard.uploadisofile.UploadIsoWizard;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class UploadIsoAction extends OnceAction {
	public UploadIsoAction(){
		super();
	}
	public UploadIsoAction(String text,String image,String disabledImage){
		super(text,image,disabledImage);
	}
	
	public void run(){
		if(hasPoolUtil.hasPool()){
			UploadIsoWizard wizard = new UploadIsoWizard();
			NewVMDialog dialog = new NewVMDialog(Display.getCurrent().getActiveShell(),
	                wizard);
			dialog.setPageSize(200, 200);
			dialog.create();
			dialog.open();
		}else{
			MessageDialog.openError(new Shell(), "提醒","请先连接资源池再使用该功能");
		}

	}
}
