package oncecenter.wizard.bandtoovs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import oncecenter.network.OVS;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class BondtoOvsWizard extends Wizard {

	BondtoOvsWizardPage bondWizardPage;
	
	VMTreeObjectHost hostObject;
	
	OVS ovs ;
	
	public BondtoOvsWizard(VMTreeObjectHost hostObject){
		setWindowTitle("网络虚拟化");
		this.hostObject = hostObject;
	}
	
	@Override
	public void addPages() {
		try {
			ovs= new OVS(hostObject.getIpAddress(),hostObject.getUsername(),hostObject.getPassword());
			bondWizardPage = new BondtoOvsWizardPage(hostObject,ovs);
			this.addPage(bondWizardPage);
		} catch (Exception e) {
			
			e.printStackTrace();
			MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
			messageBox.setText("警告");
			messageBox.setMessage("连接失败");
			messageBox.open();
		}
		
	}
	
	@Override
	public boolean performFinish() {
		
		if(!ovs.bond(new HashSet(Arrays.asList(bondWizardPage.bondedInterfaceList.getItems())))){
			MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
			messageBox.setText("警告");
			messageBox.setMessage("连接失败");
			messageBox.open();
		}
		ovs.close();
		return true;
	}

}
