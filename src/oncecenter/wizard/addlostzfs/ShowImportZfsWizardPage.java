package oncecenter.wizard.addlostzfs;

import java.util.Set;

import oncecenter.wizard.newvmfromtemp.NewVMPage;
import oncecenter.wizard.newvmfromtemp.NewVmFTWizard;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.once.xenapi.Connection;
import com.once.xenapi.Host;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.XenAPIException;

public class ShowImportZfsWizardPage extends NewVMPage {

	private Table table;
	
	private Host host ;
	private Connection conn;
	
	private Set<String> zpools;

	public Set<String> getZpools() {
		return zpools;
	}

	protected ShowImportZfsWizardPage(Connection conn) {
		super("wizardPage");
		setTitle("导入的存储库");
		setDescription("导入的存储库");
		this.conn = conn;
		
		
	}
	
	public void Init(){
		host = ((AddLostZfsWizard)this.getWizard()).selectedHost;
		if(host!=null){
			try {
				zpools = host.getZpoolCanImport(conn);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}
	}

	@Override
	public void createControl(Composite parent) {
		
		Init();
		
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(1,false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		table = new Table( composite, SWT.BORDER | SWT.V_SCROLL
		        | SWT.H_SCROLL|SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		TableColumn tc1 = new TableColumn(table, SWT.CENTER);
		tc1.setText("存储库");
		tc1.setWidth(400);
		table.setHeaderVisible(true);
		
		for(String s:zpools){
			TableItem tableItem = new TableItem(table, SWT.NONE);
			tableItem.setText(new String[] {
					s });
		}
		
		setControl(composite);
		
	}
	

}
