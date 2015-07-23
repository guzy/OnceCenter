package oncecenter.wizard.newsr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import oncecenter.Constants;
import oncecenter.util.TypeUtil;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;


import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.Host;
import com.once.xenapi.Pool;
import com.once.xenapi.SR;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.XenAPIException;

public class NewSRWizard extends Wizard {

	private VMTreeObjectRoot selection;
	private SRTypeWizardPage typeWizardPage;
	private SRNameWizardPage nameWizardPage;
	private SRLocationWizardPage locationWizardPage;
	
	private Host host = null;
	private Map<String, String> deviceConfig = new HashMap<String, String>();
	private String nameLabel = null;
	private String nameDescription = null;
	String type = null;
	String contentType = null;
	private Boolean shared;
	private Map<String, String> smConfig = new HashMap<String, String>();
	
	VMTreeObjectSR srObject;
	
	String ip;
	String path;
	String username;
	String password;
	
	boolean useNFS;

	public NewSRWizard(VMTreeObjectRoot selection) {
		setWindowTitle("ÐÂ½¨´æ´¢");
		if(selection == null)
		{	
			StructuredSelection select = (StructuredSelection)Constants.treeView.getViewer().getSelection();
			selection = (VMTreeObjectRoot)select.getFirstElement();
		}
		this.selection = selection;
		if(selection.getParent() instanceof VMTreeObjectPool)
			this.selection = (VMTreeObjectRoot)selection.getParent();
	}

	@Override
	public void addPages() {
		typeWizardPage = new SRTypeWizardPage();
		this.addPage(typeWizardPage);
		nameWizardPage = new SRNameWizardPage();
		this.addPage(nameWizardPage);
		locationWizardPage = new SRLocationWizardPage(selection);
		this.addPage(locationWizardPage);

	}

	/**
	 * Override the parent class method control the finish button's status
	 */
	public boolean canFinish() {
		IWizardPage page = getContainer().getCurrentPage();
		if ((page instanceof SRLocationWizardPage) && page.canFlipToNextPage())
			return true;
		else
			return false;
	}

	@Override
	public void createPageControls(Composite pageContainer) {
	// super.createPageControls(pageContainer);
	}
	
	class VMJob extends Job
	{
		Connection conn;
		Display display;
		VMTreeView viewer;
		public VMJob(Display display, VMTreeView viewer) {
			
			super("creating.......");
			this.display = display;
			this.viewer = viewer;
			conn = selection.getConnection();
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			Constants.jobs.add(this);
			monitor.beginTask("creating......", 100);
			srObject = new VMTreeObjectSR(nameLabel, conn, null,
					null);
			srObject.setItemState(ItemState.changing);
			if(selection.temporaryList==null)
				selection.temporaryList = new ArrayList<VMTreeObject>();
			selection.temporaryList.add(srObject);
			selection.addChild(srObject);
			if (!this.display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						viewer.getViewer().refresh();
					}
				};
				this.display.syncExec(runnable);
			}
			SR sr = null;
			SR.Record record = null;
			try {
				sr = SR.create(conn, host, deviceConfig,
						(long) 50 * 1024 * 1024 * 1024, nameLabel, nameDescription,
						type, contentType, shared, smConfig);
				record = sr.getRecord(conn);
			} catch (Exception e) {
				
				e.printStackTrace();
				if (!this.display.isDisposed()) {
					Runnable runnable = new Runnable() {
						public void run() {
							selection.getChildrenList().remove(srObject);
							viewer.getViewer().remove(srObject);
							viewer.getViewer().refresh();
						}
					};
					this.display.syncExec(runnable);
				}
				Constants.jobs.remove(this);
				monitor.done();
				return Status.CANCEL_STATUS;
			}
				
			srObject.setApiObject(sr);
			srObject.setRecord(record);
			selection.srMap.put(sr, srObject);
			srObject.setItemState(ItemState.able);
			
			if (!this.display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						viewer.getViewer().refresh();
					}
				};
				this.display.syncExec(runnable);
			}
			Constants.jobs.remove(this);
			monitor.done();
			return Status.OK_STATUS;
		}
		
	}
	@Override
	public boolean performFinish() {
		if (selection instanceof VMTreeObjectPool) {
			for(VMTreeObject o:selection.getChildren()){
				if(o instanceof VMTreeObjectHost){
					if(((VMTreeObjectHost)o).isMaster){
						host = (Host) o.getApiObject();
						break;
					}
				}
			}
			if(host == null)
				return false;
		} else {
			host = (Host) selection.getApiObject();
		}
		
		if(type==null){
			return false;
		}

		if(TypeUtil.getNfsSRTypes().contains(type)){
			useNFS = true;
		}else{
			useNFS = false;
		}
		
		if(useNFS){
			deviceConfig.put("server", ip);
			deviceConfig.put("username", username);
			deviceConfig.put("password", password);
		}
		
		if (typeWizardPage.isIso() || typeWizardPage.isHa()) {
			deviceConfig.put("auto-scan", "true");
		}
		String sr_uuid = UUID.randomUUID().toString();
		deviceConfig.put("uuid", sr_uuid);
		deviceConfig.put("location", path);

		shared = true;

		nameLabel = nameWizardPage.getText().getText();
		nameDescription = "";
		
		if (nameWizardPage.isDefault()) {
			if(useNFS)
				nameDescription = nameLabel + ":" + ip + " " + path;
			else 
				nameDescription = nameLabel + ": " + path;
		} else {
			nameDescription = nameWizardPage.getText_1().getText();
		}

		smConfig = new HashMap<String, String>();
		// smConfig.put("iso_type", "nfs_iso");
		// Long physicalSize = 284404973568L;

		VMJob job=new VMJob(PlatformUI.getWorkbench().getDisplay(),Constants.treeView);
		job.schedule();
		return true;
	}

}
