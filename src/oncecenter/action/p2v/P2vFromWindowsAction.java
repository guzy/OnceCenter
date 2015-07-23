package oncecenter.action.p2v;

import oncecenter.p2v.wizard.fromwindows.P2vFromWindowsWizard;
import oncecenter.util.ImageRegistry;
import oncecenter.util.hasPoolUtil;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class P2vFromWindowsAction extends Action{

	public P2vFromWindowsAction()
	{
		super();
		setText("Windowsϵͳ");		
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.IMPORTPHY));
	}
	
	public void run()
	{
		if(hasPoolUtil.hasPool()){
			P2vFromWindowsWizard wizard = new P2vFromWindowsWizard();
			WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(),
	                wizard);
			dialog.setPageSize(200, 240);
			dialog.create();
			dialog.open();
		}else{
			MessageDialog.openError(new Shell(), "����","����������Դ����ʹ�øù���");
		}
	}
}
