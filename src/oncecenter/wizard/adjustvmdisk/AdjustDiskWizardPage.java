package oncecenter.wizard.adjustvmdisk;

import java.util.ArrayList;

import oncecenter.maintabs.vm.DiskAdjustTab.Disk;
import oncecenter.util.MathUtil;
import oncecenter.wizard.newvmfromtemp.FinishWizardPage;
import oncecenter.wizard.newvmfromtemp.NewVMPage;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
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
import org.eclipse.wb.swt.SWTResourceManager;

public class AdjustDiskWizardPage extends NewVMPage {

	ArrayList<Disk> diskList;
	Combo diskUuidCombo;
	Combo diskAvilableCombo;
	Combo diskTotalCombo;
	Combo diskMaxValueCombo;
	Button ExpandAccordingTimes;
	Button ExpandAccordingValue;
	Text times;
	Text value;
	Combo unit;
	private Label msgLabel;
	
	public AdjustDiskWizardPage(ArrayList<Disk> diskList) {
		super("wizardPage");
		setTitle("Ӳ������");
		setDescription("Ϊÿһ��Ӳ��ѡ�����ݵķ�ʽ�����ݴ�С���������������ݺͰ������������ַ�ʽ��");
		this.diskList = diskList;
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
		
		new Label(selectDiskGroup,SWT.NONE).setText("Ӳ�̣�");
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
		
		new Label(selectDiskGroup,SWT.NONE).setText("Ӳ�̴�С��");
		diskTotalCombo = new Combo(selectDiskGroup, SWT.BORDER|SWT.DROP_DOWN | SWT.READ_ONLY);
		diskTotalCombo.setEnabled(false);
		new Label(selectDiskGroup,SWT.NONE).setText("G");
		
		new Label(selectDiskGroup,SWT.NONE).setText("���ÿռ䣺");
		diskAvilableCombo = new Combo(selectDiskGroup, SWT.BORDER|SWT.DROP_DOWN | SWT.READ_ONLY);
		diskAvilableCombo.setEnabled(false);
		new Label(selectDiskGroup,SWT.NONE).setText("G");
		
		new Label(selectDiskGroup,SWT.NONE).setText("����������");
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
		
		Group setGroup = new Group(composite, SWT.SHADOW_ETCHED_OUT);
		setGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		setGroup.setLayout(new GridLayout(3,false));
		
		ExpandAccordingTimes = new Button(setGroup, SWT.RADIO);
		ExpandAccordingTimes.setText("����������");
		ExpandAccordingTimes.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				widgetSelected(arg0);
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				times.setEnabled(true);
				value.setEnabled(false);
				unit.setEnabled(false);
			}
			
		});
		
		new Label(setGroup,SWT.NONE);
		new Label(setGroup,SWT.NONE);
		
		new Label(setGroup,SWT.NONE).setText("���ݵ�ԭ����");
		times = new Text(setGroup,SWT.BORDER);
		times.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		times.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {
				String s = times.getText();
				double expandTimes = 0.0;
				try{
					expandTimes = Double.parseDouble(s);
				}catch(NumberFormatException e1){
					msgLabel.setText("�����븡���͵���ֵ");
					msgLabel.setVisible(true);
					return;
				}
				if(expandTimes<1.0){
					msgLabel.setText("�������ݲ��Ϸ������������1������");
					msgLabel.setVisible(true);
					return;
				}
				if(Double.parseDouble(diskTotalCombo.getText())*(expandTimes-1)>Double.parseDouble(diskMaxValueCombo.getText())){
					msgLabel.setText("�������ݲ��Ϸ��������������������");
					msgLabel.setVisible(true);
					return;
				}
				double expandValue = Double.parseDouble(diskTotalCombo.getText())*(expandTimes-1);
				((AdjustDiskWizard)getWizard()).expandList.add(diskUuidCombo.getSelectionIndex(), expandValue);
				msgLabel.setVisible(false);
//				setPageComplete(false);
//				canFlipToNextPage();
				getContainer().updateButtons();
			}
		});
		new Label(setGroup,SWT.NONE).setText("��");
		
		new Label(setGroup,SWT.NONE);
		new Label(setGroup,SWT.NONE);
		new Label(setGroup,SWT.NONE);
		
		ExpandAccordingValue = new Button(setGroup, SWT.RADIO);
		ExpandAccordingValue.setText("����������");
		ExpandAccordingValue.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				widgetSelected(arg0);
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				times.setEnabled(false);
				value.setEnabled(true);
				unit.setEnabled(true);
			}
			
		});
		
		new Label(setGroup,SWT.NONE);
		new Label(setGroup,SWT.NONE);
		
		new Label(setGroup,SWT.NONE).setText("����");
		value = new Text(setGroup,SWT.BORDER);
		value.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		value.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {
				String s = value.getText();
				double expandValues = 0.0;
				try{
					expandValues = Double.parseDouble(s);
				}catch(NumberFormatException e1){
					msgLabel.setText("�����븡���͵���ֵ");
					msgLabel.setVisible(true);
					setPageComplete(false);
					return;
				}
				if(expandValues<=0){
					msgLabel.setText("�������ݲ��Ϸ������������0������");
					msgLabel.setVisible(true);
					setPageComplete(false);
					return;
				}
				double expandValue = Double.parseDouble(s);
				switch(unit.getSelectionIndex()){
				case 0:
					expandValue/=1024.0;
					break;
				case 2:
					expandValue*=1024.0;
					break;
				}
				if(expandValue>Double.parseDouble(diskMaxValueCombo.getText())){
					msgLabel.setText("�������ݲ��Ϸ��������������������");
					msgLabel.setVisible(true);
					setPageComplete(false);
					return;
				}
				((AdjustDiskWizard)getWizard()).expandList.add(diskUuidCombo.getSelectionIndex(), expandValue);
				msgLabel.setVisible(false);
//				canFlipToNextPage();
//				setPageComplete(true);
				getContainer().updateButtons();
			}
		});
		unit = new Combo(setGroup, SWT.BORDER|SWT.DROP_DOWN | SWT.READ_ONLY);
		unit.setItems(new String[]{"MB","GB","TB"});
		unit.select(1);
		
		times.setEnabled(false);
		value.setEnabled(false);
		unit.setEnabled(false);
		
		msgLabel = new Label(setGroup,SWT.NONE);
		msgLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		msgLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		//msgLabel.setVisible(false);
		
		setControl(composite);
		
		setPageComplete(false);
		composite.layout();
	}
	
	@Override
	public boolean canFlipToNextPage(){
		if(((AdjustDiskWizard)getWizard()).expandList.size()>0)
			return true;
		else
			return false;
	}

	@Override
	protected boolean nextButtonClick() {
		IWizardPage nextPage = getWizard().getNextPage(this);
		if(nextPage instanceof AdjustFinishWizardPage)
			 ((AdjustFinishWizardPage)nextPage).refresh();
		return true;
	}

}
