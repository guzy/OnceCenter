package oncecenter.action.template;

import java.util.ArrayList;

import oncecenter.Constants;
import oncecenter.action.OnceAction;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectTemplate;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;
import oncecenter.wizard.fastgenerate.FastGenerateFromTemplateWizard;
import oncecenter.wizard.newvmfromtemp.NewVMDialog;
import oncecenter.wizard.newvmfromtemp.NewVmFTWizard;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.Host;
import com.once.xenapi.VM;

public class QuickCreateVMAction extends OnceAction {
	
	VMTreeObjectTemplate selection;
	Connection conn;
	
	public QuickCreateVMAction(String text,String image,String disabledImage){
		super(text,image,disabledImage);
	}
	public QuickCreateVMAction(VMTreeObjectTemplate selection){
		super();
		this.selection=selection;
		this.conn=selection.getConnection();
		setText("快速生成");		
	}
	
	public void run(){
		if(selection==null){
			VMTreeView viewer = Constants.treeView;
			StructuredSelection select = (StructuredSelection)viewer.getViewer().getSelection();
			selection = (VMTreeObjectTemplate)select.getFirstElement();
		}
		FastGenerateFromTemplateWizard wizard = new FastGenerateFromTemplateWizard(selection);
		NewVMDialog dialog = new NewVMDialog(Display.getCurrent().getActiveShell(),
				                 wizard);
		dialog.setPageSize(400, 300);
		dialog.create();
		dialog.open();
		
	}
	
}
