package oncecenter.wizard.fastgenerate;

import java.util.ArrayList;
import java.util.Date;

import oncecenter.Constants;
import oncecenter.util.ImageRegistry;
import oncecenter.util.VMUtil;
import oncecenter.util.VMUtil.DiskInfo;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectTemplate;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;
import oncecenter.wizard.newvmfromtemp.CpuMemoryWizardPage;
import oncecenter.wizard.newvmfromtemp.NewVmFTWizard;
import oncecenter.wizard.newvmfromtemp.VmNameWizardPage;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.Host;
import com.once.xenapi.SR;
import com.once.xenapi.VM;

public class FastGenerateFromTemplateWizard extends NewVmFTWizard {

	VMTreeObjectTemplate template;
	VmNameWizardPage namePage;
	CpuMemoryWizardPage cpuPage;

	public FastGenerateFromTemplateWizard(VMTreeObjectTemplate object) {
		setWindowTitle("快速生成");
		template = object;
		templateName = template.getName();
		root = (VMTreeObjectRoot)template.getParent();
	}

	@Override
	public void addPages() {
		namePage = new VmNameWizardPage();
		addPage(namePage);
		cpuPage = new CpuMemoryWizardPage();
		addPage(cpuPage);
	}
	
	/**
	 * Override the parent class method
	 * control the finish button's status
	 */
	public boolean canFinish()
	{ 
		IWizardPage page = getContainer().getCurrentPage();
		if( page instanceof CpuMemoryWizardPage)
			return true;
		else return false;
	}
	
	@Override
	public boolean performFinish() {
		vcpu = cpuPage.getCPUNum();
		memory = (int)cpuPage.getMemoryValue();
		try {
			final VMEvent event=new VMEvent();
			event.setDatetime(new Date());
			event.setDescription("使用模板 '" + template.getName() + "'快速生成虚拟机");
			event.setTarget(template);
			event.setTask("");
			event.setType(eventType.info);
			event.setUser(template.getRoot().getUsername());
			event.setImage(ImageRegistry.getImage(ImageRegistry.INFO));
			template.events.add(event);
			Constants.logView.logFresh(event);

			VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay(),Constants.treeView);
			Constants.jobs.add(job);
			job.schedule();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	class VMJob extends Job{
		VMTreeView viewer;
		Display display;
		VM newVm;
		VM.Record newRecord;
		VM temp;
		VMTreeObjectVM newObject;
		VMTreeObject parent;
		boolean isPool;
		Connection conn;
		public VMJob(Display display,VMTreeView viewer){
			super("由模板"+ template.getName() +"快速生成虚拟机");
			this.viewer=viewer;
			this.display=display;
			conn = template.getConnection();
		}

		@Override 
	    protected IStatus run(IProgressMonitor monitor) { 
	        monitor.beginTask("快速生成...", 100); 
        		if(template.getParent() instanceof VMTreeObjectPool){
        			isPool=true;
        		}else{
        			isPool=false;
        		}
        		if(isPool){
        			parent = Constants.getSuitableHost((VMTreeObjectPool)template.getParent());
        		}else{
        			parent = template.getParent();
        		}
    			
    			newObject = new VMTreeObjectVM(vmName,conn
    					,null,null);
        		newObject.setItemState(ItemState.changing);
        		if(root.temporaryList==null){
        			root.temporaryList = new ArrayList<VMTreeObject>();
        		}
        		root.temporaryList.add(newObject);
        		
        		parent.addChild(newObject);
        		
        		if (!this.display.isDisposed()) {
    				Runnable runnable = new Runnable() {
    					public void run() {
    						viewer.getViewer().expandAll();
    						viewer.getViewer().refresh();	
    					}
    				};
    				this.display.asyncExec(runnable);
    			}
    			temp = (VM)template.getApiObject();

    			Host host = (Host)parent.getApiObject();
    			
    			try {
    				newVm = temp.createClone(conn, vmName);
    				VMUtil.AdjustCpuMemory(newVm, vcpu, memory, conn);
    			} catch (Exception e1) {
    				
    				e1.printStackTrace();
    				if (!this.display.isDisposed()) {
        				Runnable runnable = new Runnable() {
        					public void run() {
        						newObject.getParent().getChildrenList().remove(newObject);
        						viewer.getViewer().remove(newObject);
        						viewer.getViewer().expandAll();
        						viewer.getViewer().refresh();	
        					}
        				};
        				this.display.asyncExec(runnable);
        			}
    				monitor.done();
    				Constants.jobs.remove(this);
    				return Status.CANCEL_STATUS;
    			}
				root.vmMap.put(newVm, newObject);
				newObject.setApiObject(newVm);
				
				try 
				{
					newVm.startOn(conn, host, false, true);
	        	}
				 catch (Exception e1) {
    				
    				e1.printStackTrace();
    			}
				
				try {
					newRecord = newVm.getRecord(conn);
					
					newObject.setRecord(newRecord);
	        		newObject.setItemState(ItemState.able);
    			} catch (Exception e1) {
    				
    				e1.printStackTrace();
    				if (!this.display.isDisposed()) {
        				Runnable runnable = new Runnable() {
        					public void run() {
        						newObject.getParent().getChildrenList().remove(newObject);
        						viewer.getViewer().remove(newObject);
        						viewer.getViewer().expandAll();
        						viewer.getViewer().refresh();	
        					}
        				};
        				this.display.asyncExec(runnable);
        			}
    				monitor.done();
    				Constants.jobs.remove(this);
    				return Status.CANCEL_STATUS;
    			}

        		if (!this.display.isDisposed()) {
    				Runnable runnable = new Runnable() {
    					public void run() {
    						viewer.getViewer().refresh();						
    					}
    				};
    				this.display.asyncExec(runnable);
    			}
	        
	        monitor.done(); 
	        Constants.jobs.remove(this);
	        return Status.OK_STATUS;  
	    }
	}
	
}
