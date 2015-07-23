package oncecenter.wizard.newvmfromtemp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

public class VmNameWizardPage extends NewVMPage {

	String tempName;
	int index = 1;
	ArrayList<Integer> numList = new ArrayList<Integer>();
	public Text name;
	public Text description;
	private Label msgLabel;
	//private boolean flag = false;
	Collection<VMTreeObjectVM> vmList;
	
	public VmNameWizardPage() {
		super("wizardPage");
		setTitle("名称和描述");
		setDescription("请设定新虚拟机的名称并给出描述(名称不能含有空格)，名称可以在后续操作中进行更改");
	}

	public void getIndex(VMTreeObject root){
		
		for(VMTreeObject o :root.getChildrenList()){
			String name = o.getName();
			if(!name.equals(tempName)&&name.indexOf(tempName)==0){
				try{
					int i = Integer.parseInt(name.substring(tempName.length()+1));
					numList.add(i);
				}catch(Exception e){
					//e.printStackTrace();
					continue;
				}
			}
			getIndex(o);
		}
	}
	
	@Override
	public void createControl(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(2, false));
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.WRAP;
		gridData.grabExcessHorizontalSpace = true;

		new Label(composite, SWT.NONE).setText("名称:");
		name = new Text(composite, SWT.BORDER);
		tempName = ((NewVmFTWizard)this.getWizard()).templateName;
		getIndex(((NewVmFTWizard)this.getWizard()).root);
		vmList = ((NewVmFTWizard)this.getWizard()).root.vmMap.values();
		while(true){
			if(numList.contains(index)){
				index++;
			}
			else
				break;
		}
		name.setText(tempName+"_"+index);
		name.setLayoutData(gridData);
		name.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {
		
				if(name.getText().isEmpty())
				{
					msgLabel.setText("请输入虚拟机的名称！");
					msgLabel.setVisible(true);
					setPageComplete(false);
					return;
				}
				if(name.getText().contains(" "))
				{
					msgLabel.setText("虚拟机的名称中不能包含空格！");
					msgLabel.setVisible(true);
					setPageComplete(false);
					return;
				}
				if(vmList!=null){
					boolean hasDuplication = false; 
					for(VMTreeObjectVM vm:vmList){
						if(vm.getName().equals(name.getText())){
							hasDuplication = true;
							break;
						}
					}
					if(hasDuplication){
						msgLabel.setText("系统中已有相同名称的虚拟机，请重新命名");
						msgLabel.setVisible(true);
						setPageComplete(false);
						return;
					}
				}
				msgLabel.setVisible(false);
				setPageComplete(true);
				
			}
		});
		
		new Label(composite, SWT.TOP).setText("描述:");
		description = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.WRAP |SWT.V_SCROLL);
		GridData gd_description = new GridData(GridData.FILL_HORIZONTAL);
		gd_description.heightHint = 82;
		description.setLayoutData(gd_description);
		
		description.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (!description.getText().isEmpty()) {
					
					setPageComplete(true);
				}
			}
		});
		
		new Label(composite,SWT.NONE);
		
		msgLabel = new Label(composite,SWT.NONE);
		msgLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		msgLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		msgLabel.setVisible(false);
		setControl(composite);
	}


	@Override
	protected boolean nextButtonClick() {
		
		if(this.getWizard() instanceof NewVmFTWizard){
			((NewVmFTWizard)this.getWizard()).vmName = name.getText();
		((NewVmFTWizard)this.getWizard()).vmDescription = description.getText();
		 IWizardPage nextPage = getWizard().getNextPage(this);
		 if(nextPage instanceof SelectMediaWizardPage)
			 ((SelectMediaWizardPage)nextPage).refresh();
		}
		
		return true;
	}

	
	public void refresh()
	{
		if(this.getControl() == null)
			return;
		tempName = ((NewVmFTWizard)this.getWizard()).templateName;
		this.getIndex(((NewVmFTWizard)this.getWizard()).root);
		name.setText(tempName+"_"+index);
		description.setText("");
	}
}
