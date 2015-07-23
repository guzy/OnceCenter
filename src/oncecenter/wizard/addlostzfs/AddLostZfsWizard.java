package oncecenter.wizard.addlostzfs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import oncecenter.util.AddServerUtil;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.wizard.adjustvmdisk.AdjustFinishWizardPage;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.once.xenapi.Connection;
import com.once.xenapi.Host;
import com.once.xenapi.Pool;
import com.once.xenapi.SR;

public class AddLostZfsWizard extends Wizard {

	SelectHostWizardPage hostPage;
	ShowImportZfsWizardPage showPage;
	
	Connection conn;
	List<Host.Record> hostRecordList;
	Set<SR> srs;
	
	Host selectedHost;
	
	Set<String> zpools;
	
	public AddLostZfsWizard(Connection conn,List<Host.Record> hostRecordList,Set<SR> srs){
		this.conn = conn;
		this.hostRecordList = hostRecordList;
		this.srs = srs;
		Init();
	}
	
	public void Init(){
		
	}
	
	public boolean canFinish()
	{ 
		IWizardPage page = getContainer().getCurrentPage();
		if( page instanceof ShowImportZfsWizardPage)
			return true;
		else return false;
	}
	
	@Override
	public void addPages() 
	{
		hostPage = new SelectHostWizardPage(hostRecordList);
		this.addPage(hostPage);
		showPage = new ShowImportZfsWizardPage(conn);
		this.addPage(showPage);
	}
	
	@Override
	public void createPageControls(Composite pageContainer) {
	// super.createPageControls(pageContainer);
	}
	
	@Override
	public boolean performFinish() {
		
		zpools = showPage.getZpools();
		if(selectedHost==null)
			return true;
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(new Shell()); 
		try {
			dialog.run(true, true, new IRunnableWithProgress(){ 
			    public void run(IProgressMonitor monitor) { 
			    	monitor.beginTask("正在恢复...",  IProgressMonitor.UNKNOWN);
			    	try{
			    		if(zpools!=null)
			    			for (String zpool : zpools) {
								selectedHost.importZpool(conn, zpool);
							}
						for (SR sr : srs) {
							/**
							 * 获取原SR的挂载路径
							 */
							String location = sr.getLocation(conn);
							/**
							 * 先umount原SR的挂载点
							 */
							sr.umount(conn);

							/**
							 * 再修改SR的ip信息。
							 */
							SR.setZpoolHostIp(conn, location, selectedHost);
							/**
							 * 最后重新mount。
							 */
							sr.mount(conn);
						}
			    	}catch(Exception e){
			    		e.printStackTrace();
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
