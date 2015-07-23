package oncecenter.wizard.adddesc;

import java.util.Date;

import oncecenter.Constants;
import oncecenter.util.ImageRegistry;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectTemplate;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.VM;

public class AddTemplateDescWizard extends Wizard {

	private VMTreeObjectTemplate selection;
	private AddHostDescWizardPage addDescWizardPage;
	private VM vm;
	public AddTemplateDescWizard(VMTreeObjectTemplate object)
	{
		setWindowTitle("添加模板描述信息");
		selection = object;
	}
	
	@Override
	public void addPages()
	{
		vm = (VM)selection.getApiObject();
		addDescWizardPage = new AddHostDescWizardPage();
		addPage(addDescWizardPage);
	}
	class VMJob extends Job
	{
		Display display;
		TreeViewer viewer;
		String description = null;
		public VMJob(Display display, TreeViewer viewer, String desc) {
			super("为模板添加描述信息");
			this.display = display;
			this.viewer = viewer;
			this.description = desc;
		}
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("creating...", 100);
			selection.setItemState(ItemState.changing);
			vm = (VM)selection.getApiObject();			
			if (!display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	viewer.refresh();
			        }
			    };
			    display.syncExec(runnable); 
			}
			try{
				VM.Record host_record = vm.getRecord(selection.getConnection());
				host_record.nameDescription = description;
				vm.setNameDescription(selection.getConnection(), description);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				selection.setItemState(ItemState.able);
				if (!display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
				        	viewer.refresh();
				        }
				    };
				    display.syncExec(runnable); 
				}
				monitor.done();
				Constants.jobs.remove(this);
				return Status.CANCEL_STATUS;
			}
			selection.setItemState(ItemState.able);
			if (!display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	viewer.refresh();
			        }
			    };
			    display.syncExec(runnable); 
			}
			monitor.done();
			Constants.jobs.remove(this);
			return Status.OK_STATUS;
		}
	}
	@Override
	public boolean performFinish() {
		String hostDescription = addDescWizardPage.getDescription();
		final VMEvent event=new VMEvent();
		event.setDatetime(new Date());
		event.setDescription("为模板 '" + selection.getName() + "'添加描述信息");
		event.setTarget(selection);
		event.setTask("");
		event.setType(eventType.info);
		event.setUser(selection.getRoot().getUsername());
		event.setImage(ImageRegistry.getImage(ImageRegistry.INFO));
		selection.events.add(event);
		Constants.logView.logFresh(event);

		VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay(),Constants.treeView.getViewer(),hostDescription);
		job.schedule();
		return true;
	}
	

}
