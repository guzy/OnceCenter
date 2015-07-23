package oncecenter.wizard.editvmdisk;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import oncecenter.Constants;
import oncecenter.util.ImageRegistry;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectDefault;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;

import com.once.xenapi.Connection;
import com.once.xenapi.Host;
import com.once.xenapi.SR;
import com.once.xenapi.VBD;
import com.once.xenapi.VM;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.XenAPIException;

public class EditVmDiskWizard extends Wizard {

	private EditVmDiskWizardPage page;
	private VMTreeObjectVM selection; 
	public Connection connection;
	
	public Set<VBD> fiberList = new HashSet<VBD>();
	public Set<String> allFiberList = new HashSet<String>();
	public Host host;
	public VMTreeObjectHost hostObject;
	public VMTreeObjectSR srObject;
	public VM vm;
	public EditVmDiskWizard(VMTreeObjectVM selection)
	{
		setWindowTitle("添加光纤");
		if(selection == null)
		{	
			StructuredSelection select = (StructuredSelection)Constants.treeView.getViewer().getSelection();
			selection = (VMTreeObjectVM)select.getFirstElement();
		}
		this.selection = selection;
		this.connection = this.selection.getConnection();
		Init();
	}
	
	private void Init()
	{
		vm = (VM)selection.getApiObject();
		host = selection.getRecord().residentOn;
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
			hostObject = parent.hostMap.get(host);
			VMTreeObject[] children = hostObject.getChildren();
			for(VMTreeObject e:children)
			{
				if(e instanceof VMTreeObjectSR && ((VMTreeObjectSR)e).getSrType().equals("local"))
				{	
					srObject = (VMTreeObjectSR)e;
					break;
				}
			}
		}
		try {
			fiberList = vm.getFibers(connection);
			allFiberList = host.getAllFibers(connection);
		} catch (BadServerResponse e) {
			e.printStackTrace();
		} catch (XenAPIException e) {
			e.printStackTrace();
		} catch (XmlRpcException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void addPages() 
	{
		page = new EditVmDiskWizardPage("硬盘设置",selection);
		this.addPage(page);
	}
	@Override
	public boolean performFinish() {
		final VMEvent event=new VMEvent();
		event.setDatetime(new Date());
		event.setDescription("修改虚拟机 '" + selection.getName() + "'的光纤设置");
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
