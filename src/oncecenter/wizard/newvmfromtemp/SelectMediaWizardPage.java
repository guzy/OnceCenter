package oncecenter.wizard.newvmfromtemp;

import java.util.ArrayList;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectTemplate;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import com.once.xenapi.VDI;

public class SelectMediaWizardPage extends NewVMPage {

	Combo driveName;
	VMTreeObject selection;
	VMTreeObject template;
	
	private VMTreeObject selectedMedia;
	private ArrayList<VDI.Record> medias;
	
	protected SelectMediaWizardPage(VMTreeObject selection) {
		super("wizardPage");
		setTitle("安装文件");
		setDescription("选择操作系统的安装文件");
		this.selection=selection;
		
	}

	@Override
	public void createControl(Composite parent) {
		
		
		medias = ((NewVmFTWizard)this.getWizard()).medias;
		template = ((NewVmFTWizard)this.getWizard()).selectedTemp;
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(1, false));
		Button button1 = new Button(composite, SWT.RADIO);
		button1.setText("DVD Drive");
		button1.setSelection(true);

		driveName = new Combo(composite, SWT.BORDER|SWT.READ_ONLY);
		driveName.add("<empty>");
		
		for(VDI.Record m:medias){
			driveName.add(m.nameLabel);
		}
		driveName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if(template==null)
			driveName.select(1);
		else
		{
			driveName.select(0);
			driveName.setEnabled(false);
		}
		
		if(medias.size()==0&&template==null){
			setPageComplete(false);
		}
		driveName.addSelectionListener(new SelectionAdapter() { 
		      public void widgetSelected(SelectionEvent e) { 
		        if(driveName.getText().equals("<empty>")&&template==null){
		        	setPageComplete(false);
		        }else{
		        	setPageComplete(true);
		        }
		  
		      } 
		    }); 

		
		Button button2 = new Button(composite, SWT.RADIO);
		button2.setText("Boot from network");
		button2.setEnabled(false);
		

		setControl(composite);
	}

	public VMTreeObject getSelectedMedia() {
		return selectedMedia;
	}

	public void setSelectedMedia(VMTreeObject selectedMedia) {
		this.selectedMedia = selectedMedia;
	}

	
	
	@Override
	protected boolean nextButtonClick() {
		
		for(VDI.Record m:medias){
			if(driveName.getText().equals(m.nameLabel)){
				((NewVmFTWizard)this.getWizard()).selectedMedia = m;
				break;
			}
		}
		return true;
	}

	//选择由模板创建或由iso创建显示两种不同的driveName列表
	public void refresh()
	{
		if(this.getControl() == null)
			return;
		template = ((NewVmFTWizard)this.getWizard()).selectedTemp;
		if(template==null)
		{	
			driveName.select(1);
			driveName.setEnabled(true);
		}
		else
		{
			driveName.select(0);
			driveName.setEnabled(false);
		}
	}

}
