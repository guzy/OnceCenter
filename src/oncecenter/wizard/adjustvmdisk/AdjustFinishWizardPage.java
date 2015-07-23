package oncecenter.wizard.adjustvmdisk;

import oncecenter.maintabs.vm.DiskAdjustTab.Disk;
import oncecenter.wizard.newvmfromtemp.NewVMPage;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class AdjustFinishWizardPage extends NewVMPage {

	Table table;
	TableViewer tableViewer;
	
	public AdjustFinishWizardPage() {
		super("wizardPage");
		setTitle("”≤≈Ã¿©»›");
		setDescription("«Î»∑»œ”≤≈Ã¿©»›–≈œ¢");
	}

	@Override
	public void createControl(Composite parent) {
		
		
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(1,false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		table = new Table( composite, SWT.BORDER | SWT.V_SCROLL
		        | SWT.H_SCROLL|SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		TableColumn tc1 = new TableColumn(table, SWT.CENTER);
		TableColumn tc2 = new TableColumn(table, SWT.CENTER);
		tc1.setText("”≤≈Ã");
		tc2.setText("¿©»›¥Û–°£®G£©");
		tc1.setWidth(300);
		tc2.setWidth(100);
		table.setHeaderVisible(true);

		tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider());
		
		//tableViewer.setInput(((AdjustDiskWizard)getWizard()).diskList);
		setControl(composite);
		
		
		composite.layout();
		
		
	}
	
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

		 @Override
		 public Image getColumnImage(Object element, int columnIndex) {
			  return null;
		 }

		 @Override
		 public String getColumnText(Object element, int columnIndex) {
		  if(element instanceof Disk) {
			  int index = ((AdjustDiskWizard)getWizard()).diskList.indexOf(element);
			  Double value = ((AdjustDiskWizard)getWizard()).expandList.get(index);
			switch(columnIndex) {
		   case 0:
		    return ((Disk)element).getUuid();
		   
		   case 1:
			  return (value==null?0.0:value)+"";
			   
		   }
		  }
		  
		  return null;
		 }
		}

//	@Override
//	public boolean canFlipToNextPage(){
//		return;
//	}
	
	@Override
	protected boolean nextButtonClick() {
		return true;
	}

	public void refresh() {
		tableViewer.setInput(((AdjustDiskWizard)getWizard()).diskList);
		setPageComplete(true);
	}

}
