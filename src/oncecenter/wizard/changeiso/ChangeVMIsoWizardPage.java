package oncecenter.wizard.changeiso;

import java.util.ArrayList;
import java.util.List;

import oncecenter.wizard.newvmfromtemp.NewVmFTWizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.once.xenapi.VDI;

public class ChangeVMIsoWizardPage extends WizardPage {

	Table table;
	List<VDI.Record> medias;
	
	protected ChangeVMIsoWizardPage(List<VDI.Record> medias) {
		super("");
		this.medias = medias;
		
	}

	@Override
	public void createControl(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(1, false));

		table = new Table( composite, SWT.BORDER | SWT.V_SCROLL
		        | SWT.H_SCROLL|SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		TableColumn tc1 = new TableColumn(table, SWT.CENTER);
		tc1.setText("นโลฬร๛");
		tc1.setWidth(400);
		
		table.setHeaderVisible(true);
		for(VDI.Record m:medias){
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(new String[] {
					m.nameLabel });
		}

		table.addSelectionListener(new SelectionAdapter() { 
		      public void widgetSelected(SelectionEvent e) { 
		        table.getSelection()[0].getText();
		        if(table.getSelection()==null||table.getSelection().length == 0){
		        	setPageComplete(false);
		        }else{
		        	((ChangeVMIsoWizard)getWizard()).mediaName = table.getSelection()[0].getText();
		        }
		  
		      } 
		    });		

		setControl(composite);
	}

}
