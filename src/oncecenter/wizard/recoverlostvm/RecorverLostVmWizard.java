package oncecenter.wizard.recoverlostvm;

import java.util.ArrayList;
import java.util.List;

import oncecenter.Constants;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.wizard.upgradeSystem.HostAddressWizardPage;
import oncecenter.wizard.upgradeSystem.UpgradeSystemWizardPage;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.VM;

public class RecorverLostVmWizard extends Wizard {
	
	VMTreeObjectRoot selection;
	List<String> checkedPathList = new ArrayList<String>();
	//private LostVmNameWizardPage namePage;
	private LostVmListWizardPage listPage;
	
	public RecorverLostVmWizard(VMTreeObjectRoot selection) {
		setWindowTitle("新建虚拟机");
		this.selection = selection;
		Init();
	}
	
	public void Init(){
		
	}
	@Override
	public void createPageControls(Composite pageContainer) {
	// super.createPageControls(pageContainer);
	}
	
	@Override
	public void addPages() {
		
//		namePage = new LostVmNameWizardPage();
//		this.addPage(namePage);
		listPage = new LostVmListWizardPage(selection);
		this.addPage(listPage);
	}
	@Override
	public boolean performFinish() {
		
		if(checkedPathList.size()==0)
			return true;
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell()); 
		try {
			dialog.run(true, true, new IRunnableWithProgress(){ 
			    public void run(IProgressMonitor monitor) { 
			        monitor.beginTask("恢复中...",  100);
			        int progress = 0;
			        int step = 100/checkedPathList.size();
			        for(String s:checkedPathList){
			        	monitor.worked(progress+=step);
			        	try{
			        		VM vm = VM.createFromSxp(selection.getConnection(), s);
			        		VM.Record record = vm.getRecord(selection.getConnection());
			        		VMTreeObjectVM vmObject = new VMTreeObjectVM(record.nameLabel
			        				, selection.getConnection(), vm, record);
			        		selection.addChild(vmObject);
			        		
			        	}catch(Exception e){
			        		e.printStackTrace();
			        	}
			        	Display display=PlatformUI.getWorkbench().getDisplay();
			        	if (!display.isDisposed()){
						    Runnable runnable = new Runnable(){
						        public void run(){
						        	Constants.treeView.getViewer().refresh();
						        	if(Constants.groupView!=null)
						        		Constants.groupView.getViewer().refresh();
						        }
						    };
						    display.asyncExec(runnable); 
						}
					}
			        
			        monitor.done(); 
			    } 
			});
			
		} catch (Exception e1) {
			
			e1.printStackTrace();
		}		
		
		return true;
	}

}
