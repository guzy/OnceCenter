package oncecenter.action.p2v;

import oncecenter.p2v.wizard.fromlinux.P2vFromLinuxWizard;
import oncecenter.util.ImageRegistry;
import oncecenter.util.hasPoolUtil;
import oncecenter.wizard.newvmfromtemp.NewVMDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class P2vFromLinuxAction extends Action{

	public P2vFromLinuxAction()
	{
		super();
		setText("Linux系统");		
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.IMPORTPHY));
	}
	
	public void run()
	{
		if(hasPoolUtil.hasPool()){
			P2vFromLinuxWizard wizard = new P2vFromLinuxWizard();
			NewVMDialog dialog = new NewVMDialog(Display.getCurrent().getActiveShell(),
	                 wizard);
			dialog.setPageSize(200, 240);
			dialog.create();
			dialog.open();
		}else{
			MessageDialog.openError(new Shell(), "提醒","请先连接资源池再使用该功能");
		}
	}
}
