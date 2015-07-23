package oncecenter.wizard.addlostzfs;

import java.util.List;

import oncecenter.wizard.newvmfromtemp.NewVMPage;
import oncecenter.wizard.newvmfromtemp.NewVmFTWizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.once.xenapi.Host;
import com.once.xenapi.Types;
import com.once.xenapi.VDI;

public class SelectHostWizardPage extends NewVMPage {

	List<Host.Record> hostRecordList;
	Combo hostName;
	protected SelectHostWizardPage(List<Host.Record> hostRecordList) {
		super("wizardPage");
		setTitle("选择主机");
		setDescription("选择主机");
		this.hostRecordList = hostRecordList;
		
	}

	@Override
	public void createControl(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(2,false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		(new Label(composite,SWT.NONE)).setText("主机名");
		hostName = new Combo(composite, SWT.BORDER);
		for(Host.Record r:hostRecordList){
			hostName.add(r.nameLabel);
		}
		this.setControl(composite);
	}
	
	@Override
	protected boolean nextButtonClick() {
		
		for(Host.Record r:hostRecordList){
			if(hostName.getText().equals(r.nameLabel)){
				((AddLostZfsWizard)this.getWizard()).selectedHost = Types.toHost(r.uuid);
				 break;
			}
		}
		return true;
	}

}
