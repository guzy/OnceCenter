package oncecenter.wizard.changeiso;

import java.util.ArrayList;
import java.util.Date;

import oncecenter.Constants;
import oncecenter.util.ImageRegistry;
import oncecenter.util.Ssh;
import oncecenter.util.TypeUtil;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectDefault;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.SR;
import com.once.xenapi.VDI;
import com.once.xenapi.VM;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.XenAPIException;

public class ChangeVMIsoWizard extends Wizard {

	VMTreeObjectVM selection;
	Connection connection;
	VM vm;
	
	ArrayList<VDI.Record> medias = new ArrayList<VDI.Record>();
	
	ChangeVMIsoWizardPage mediaPage;
	
	public String mediaName;
	
	public ChangeVMIsoWizard(VMTreeObjectVM selection)
	{
		setWindowTitle("选择要更换的光盘名");
		this.selection = selection;
		this.connection = this.selection.getConnection();
		this.vm = (VM)selection.getApiObject();
		Init();
	}
	
	@Override
	public void addPages() {
		mediaPage = new ChangeVMIsoWizardPage(medias);
		addPage(mediaPage);
	}
	
	private void Init()
	{
		VMTreeObjectRoot parent = null;
		VMTreeObject object = selection.getParent().getParent();
		if(object instanceof VMTreeObjectRoot)
		{
			parent = (VMTreeObjectRoot) object;
		}
		else if(object instanceof VMTreeObjectDefault)
		{
			parent = (VMTreeObjectRoot)selection.getParent();
		}
		if(parent != null)
		{	
			for(VMTreeObjectSR sr:parent.srMap.values()){
				getMedias(sr);
			}
		}
	}
	
	public void getMedias(VMTreeObjectSR o){
		
		if(o.getSrType().contains(TypeUtil.isoSign)){
			SR sr = (SR)o.getApiObject();
			try {
				sr.update(o.getConnection());
				for(VDI vdi : sr.getVDIs(o.getConnection())){
					medias.add(vdi.getRecord(o.getConnection()));
				}
			} catch (BadServerResponse e) {
				e.printStackTrace();
			} catch (XenAPIException e) {
				e.printStackTrace();
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	public boolean performFinish() {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell()); 
		try {
			dialog.run(true, true, new IRunnableWithProgress(){ 
			    public void run(IProgressMonitor monitor) {
			        monitor.beginTask("更换中...",  IProgressMonitor.UNKNOWN); 
			        try{
			        	vm.mediaChange(connection, mediaName);
			        	Display display=PlatformUI.getWorkbench().getDisplay();
						if (!display.isDisposed()){
						    Runnable runnable = new Runnable(){
						        public void run( ){
						        	MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
									messageBox.setText("提示");
									messageBox.setMessage("更换成功！");
									messageBox.open();
						        }
							};
						    display.syncExec(runnable); 
						}
			        }catch(Exception e){
			        	e.printStackTrace();
			        	Display display=PlatformUI.getWorkbench().getDisplay();
						if (!display.isDisposed()){
						    Runnable runnable = new Runnable(){
						        public void run( ){
						        	MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
									messageBox.setText("提示");
									messageBox.setMessage("更换失败！");
									messageBox.open();
						        }
							};
						    display.syncExec(runnable); 
						}
			        }
			        monitor.done(); 
			    } 
			});
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		final VMEvent event=new VMEvent();
		event.setDatetime(new Date());
		event.setDescription("更换虚拟机的 '" + selection.getName() + "'ISO");
		event.setTarget(selection);
		event.setTask("");
		event.setType(eventType.info);
		event.setUser(selection.getRoot().getUsername());
		event.setImage(ImageRegistry.getImage(ImageRegistry.INFO));
		selection.events.add(event);
		Constants.logView.logFresh(event);

		return true;
	}

}
