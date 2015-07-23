package oncecenter.wizard.adddisk;

import java.util.ArrayList;

import oncecenter.util.MathUtil;
import oncecenter.wizard.adddisk.AddVMDiskWizard.Disk;
import oncecenter.wizard.newvmfromtemp.NewVMPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class AddVMDiskWizardPage extends NewVMPage {

	ArrayList<Disk> diskList;
	Combo diskUuidCombo;
	Combo diskAvilableCombo;
	Combo diskTotalCombo;
	Combo diskMaxValueCombo;
	Button UnbindDisk;
	Button BindDisk;
	Text diskName;
	Text diskUuid;
	Text diskSize;
	
	public AddVMDiskWizardPage(ArrayList<Disk> diskList) {
		super("wizardPage");
		setTitle("管理虚拟机挂载的硬盘");
		setDescription("可以为虚拟机挂载硬盘,也可以卸载指定的硬盘。");
		this.diskList = diskList;
	}
	
	public String getNewDiskUuid(){
		return diskUuid.getText();
	}
	
	public String getNewDiskSize(){
		return diskSize.getText();
	}
	
	public String getDeleteDiskName(){
		return diskName.getText();
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(1,false));
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		composite.setLayoutData(gridData);
		
		Group selectDiskGroup = new Group(composite, SWT.SHADOW_ETCHED_OUT);
		selectDiskGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		selectDiskGroup.setLayout(new GridLayout(3,false));
		
		new Label(selectDiskGroup,SWT.NONE).setText("硬盘名称：");
		diskUuidCombo = new Combo(selectDiskGroup, SWT.BORDER|SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData g = new GridData();
		g.horizontalSpan = 2;
		diskUuidCombo.setLayoutData(g);
		diskUuidCombo.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				widgetSelected(arg0);
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				int index = diskUuidCombo.getSelectionIndex();
				diskTotalCombo.select(index);
				diskAvilableCombo.select(index);
				diskMaxValueCombo.select(index);
			}
			
		});
		
		new Label(selectDiskGroup,SWT.NONE).setText("硬盘大小：");
		diskTotalCombo = new Combo(selectDiskGroup, SWT.BORDER|SWT.DROP_DOWN | SWT.READ_ONLY);
		diskTotalCombo.setEnabled(false);
		new Label(selectDiskGroup,SWT.NONE).setText("G");
		
		new Label(selectDiskGroup,SWT.NONE).setText("可用空间：");
		diskAvilableCombo = new Combo(selectDiskGroup, SWT.BORDER|SWT.DROP_DOWN | SWT.READ_ONLY);
		diskAvilableCombo.setEnabled(false);
		new Label(selectDiskGroup,SWT.NONE).setText("G");
		
		new Label(selectDiskGroup,SWT.NONE).setText("可扩容量：");
		diskMaxValueCombo = new Combo(selectDiskGroup, SWT.BORDER|SWT.DROP_DOWN | SWT.READ_ONLY);
		diskMaxValueCombo.setEnabled(false);
		new Label(selectDiskGroup,SWT.NONE).setText("G");
		
		for(Disk disk : diskList){
			diskUuidCombo.add(disk.getUuid());
			diskTotalCombo.add(MathUtil.Rounding(disk.getTotalValue(), 2));
			diskAvilableCombo.add(MathUtil.Rounding(disk.getAvailableSpace(), 2));
			diskMaxValueCombo.add(MathUtil.Rounding(disk.getMaxValue(), 2));
		}
		
		diskUuidCombo.select(0);
		diskTotalCombo.select(0);
		diskAvilableCombo.select(0);
		diskMaxValueCombo.select(0);
		
		Group unbindGroup = new Group(composite, SWT.SHADOW_ETCHED_OUT);
		unbindGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL,GridData.FILL_VERTICAL));
		unbindGroup.setLayout(new GridLayout(3,false));
		
		UnbindDisk = new Button(unbindGroup, SWT.RADIO);
		UnbindDisk.setText("卸载硬盘");
		UnbindDisk.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				widgetSelected(arg0);
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				diskName.setEnabled(true);
				diskUuid.setEnabled(false);
				diskSize.setEnabled(false);
			}
			
		});
		new Label(unbindGroup,SWT.NONE);
		new Label(unbindGroup,SWT.NONE);
		
		new Label(unbindGroup,SWT.NONE).setText("硬盘名称");
		diskName = new Text(unbindGroup,SWT.BORDER);
		diskName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		diskName.setText("请输入正确的硬盘名称");
		diskName.selectAll();
		new Label(unbindGroup,SWT.NONE);
		
		BindDisk = new Button(unbindGroup, SWT.RADIO);
		BindDisk.setText("挂载硬盘");
		BindDisk.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				widgetSelected(arg0);
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				diskName.setEnabled(false);
				diskUuid.setEnabled(true);
				diskSize.setEnabled(true);
			}
			
		});
		new Label(unbindGroup,SWT.NONE);
		new Label(unbindGroup,SWT.NONE);
		
		new Label(unbindGroup,SWT.NONE).setText("硬盘唯一标识符");
		diskUuid = new Text(unbindGroup,SWT.BORDER);
		diskUuid.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(unbindGroup,SWT.NONE);
		new Label(unbindGroup,SWT.NONE).setText("硬盘空间大小");
		diskSize = new Text(unbindGroup,SWT.BORDER);
		diskSize.setText("单位是GB");
		diskSize.selectAll();
		diskSize.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		setControl(composite);
	}
	
	//另一个版本，可完整替换
	/*
	Text diskUuid;
	Text diskSize;

	public boolean canFinish() {
		return true;
	}

	*//**
	 * Create the wizard.
	 *//*
	public AddVMDiskWizardPage() {
		super("wizardPage");
		setTitle("为虚拟机添加磁盘信息");
	}
	
	public String getDiskUuid(){
		return diskUuid.getText();
	}
	
	public String getDiskSize(){
		return diskSize.getText();
	}
	*//**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 *//*
	public void createControl(Composite parent) {
		Composite comParent = new Composite(parent,SWT.NONE);
		comParent.setLayout(new GridLayout(2, false));		
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		comParent.setLayoutData(gridData);
		
		new Label(comParent, SWT.NONE).setText("磁盘唯一编号:");
		diskUuid = new Text(comParent, SWT.BORDER);
		diskUuid.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		diskUuid.setText("输入磁盘编号(自定义)");
		new Label(comParent, SWT.NONE).setText("磁盘空间大小:");
		diskSize = new Text(comParent, SWT.BORDER);
		diskSize.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		diskSize.setText("输入磁盘大小(单位GB)");
		setControl(comParent);
	}
*/
	}
