package oncecenter.wizard.managedisk;

import java.text.DecimalFormat;
import java.util.ArrayList;

import oncecenter.util.MathUtil;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.wizard.managedisk.AddVMDiskWizard.Disk;
import oncecenter.wizard.newvmfromtemp.NewVMPage;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.once.xenapi.Connection;
import com.once.xenapi.SR;
import com.once.xenapi.VDI;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.XenAPIException;
import com.once.xenapi.VM;

public class AddVMDiskWizardPage extends NewVMPage {

	ArrayList<Disk> diskList;
	Combo diskUuidCombo;
	Combo diskAvilableCombo;
	Combo diskTotalCombo;
	Combo diskMaxValueCombo;
	Button UnbindDisk;
	Button BindDisk;
	private Table table;
	TableViewer tableViewer;
	Text diskName;
	Text diskUuid;
	Text diskSize;
	String deleteDiskName = null;
	String VIPDiskName = null;
	ArrayList<wizardDisk> disks = new ArrayList<wizardDisk>();
	Connection c = null;
	double createDiskSize = 0;
	Label errorMsgLabel;
	Label nullMsgLabel;
	Label msgLabel;
	VM vm = null;
	String restrict = null;
	
	public AddVMDiskWizardPage(ArrayList<Disk> diskList, VMTreeObjectVM selection) {
		super("wizardPage");
		setTitle("������������ص�Ӳ��");
		this.vm = (VM)selection.getApiObject();
		this.c = selection.getConnection();
		try {
//			setTitle("��������� ["+ vm.getNameLabel(selection.getConnection()) + "] ���ص�Ӳ��");
			this.VIPDiskName = vm.getSystemVDI(c).getUuid(c);
		} catch (BadServerResponse e) {
			e.printStackTrace();
		} catch (XenAPIException e) {
			e.printStackTrace();
		} catch (XmlRpcException e) {
			e.printStackTrace();
		}
		setDescription("����Ϊ��������Ӳ�̣�Ҳ����ɾ��ָ����Ӳ�̡�");
		this.diskList = diskList;
	}
	
	public double getNewDiskSize(){
		return createDiskSize;
	}
	
	public String getDeleteDiskName(){
		if(table.getSelectionIndex() >= 0) {
			deleteDiskName = disks.get(table.getSelectionIndex()).getDiskName();
//			if (deleteDiskName.equals(VIPDiskName)){
//				System.out.println("����ɾ��ϵͳ����");
//				MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
//				messageBox.setText("��ʾ");
//				messageBox.setMessage("ϵͳ����������ɾ��");
//				messageBox.open();
//			}
			return deleteDiskName;
		}
		return null;
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
//		Group selectDiskGroup = new Group(composite, SWT.BORDER);
//		selectDiskGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL,GridData.FILL_VERTICAL));
		selectDiskGroup.setLayout(new GridLayout(3,true));
		
		UnbindDisk = new Button(selectDiskGroup, SWT.RADIO);
		UnbindDisk.setText("ɾ��Ӳ��");
		UnbindDisk.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				widgetSelected(arg0);
			}
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				table.setEnabled(true);
//				diskUuid.setEnabled(false);
				diskSize.setEnabled(false);
			}
		});
		new Label(selectDiskGroup,SWT.NONE);
		new Label(selectDiskGroup,SWT.NONE);
		
		table = new Table( selectDiskGroup, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL|SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		table.setLinesVisible(true);
		
		TableColumn tc1 = new TableColumn(table, SWT.CENTER);
		TableColumn tc2 = new TableColumn(table, SWT.CENTER);
		TableColumn tc3 = new TableColumn(table, SWT.CENTER);
		tc1.setText("Ӳ������");
		tc2.setText("����");
		tc3.setText("���ÿռ�");
		tc1.setWidth(270);
		tc2.setWidth(50);
		tc3.setWidth(100);
		table.setHeaderVisible(true);

		tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider());
		
		for (Disk disk : diskList) {
			wizardDisk wizarddisk = new wizardDisk(disk.getUuid(), disk.getTotalValue(), disk.getAvailableSpace());
			disks.add(wizarddisk);
		}
		tableViewer.setInput(disks);
//		table.setSelection(0);
//		table.pack();
		
		new Label(selectDiskGroup,SWT.NONE);
		new Label(selectDiskGroup,SWT.NONE);
		new Label(selectDiskGroup,SWT.NONE);
		new Label(selectDiskGroup,SWT.NONE);
		new Label(selectDiskGroup,SWT.NONE);
		
		BindDisk = new Button(selectDiskGroup, SWT.RADIO);
		BindDisk.setText("���Ӳ��");
		BindDisk.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				widgetSelected(arg0);
			}
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				table.setEnabled(false);
//				diskUuid.setEnabled(true);
				diskSize.setEnabled(true);
			}
		});
		new Label(selectDiskGroup,SWT.NONE);
		new Label(selectDiskGroup,SWT.NONE);
		/*
		new Label(selectDiskGroup,SWT.NONE).setText("Ӳ��Ψһ��ʶ��");
		new Label(selectDiskGroup,SWT.NONE);
		new Label(selectDiskGroup,SWT.NONE);
		diskUuid = new Text(selectDiskGroup,SWT.BORDER);
		diskUuid.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		diskUuid.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				nullMsgLabel.setVisible(false);
				if (diskUuid.getText() == ""){
					nullMsgLabel.setText("Ӳ��Ψһ��ʶ������Ϊ�գ�");
					nullMsgLabel.setVisible(true);
				}
				getContainer().updateButtons();
			}

			@Override
			public void keyReleased(KeyEvent e) {
				nullMsgLabel.setVisible(false);
				if (diskUuid.getText() == ""){
					nullMsgLabel.setText("Ӳ��Ψһ��ʶ������Ϊ�գ�");
					nullMsgLabel.setVisible(true);
				}
				getContainer().updateButtons();
			}
		});
		new Label(selectDiskGroup,SWT.NONE);
		new Label(selectDiskGroup,SWT.NONE);
		nullMsgLabel = new Label(selectDiskGroup,SWT.NONE);
		nullMsgLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nullMsgLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		new Label(selectDiskGroup,SWT.NONE);
		new Label(selectDiskGroup,SWT.NONE);
		*/
		new Label(selectDiskGroup,SWT.NONE).setText("Ӳ�̿ռ��С");
		new Label(selectDiskGroup,SWT.NONE);
		new Label(selectDiskGroup,SWT.NONE);
		diskSize = new Text(selectDiskGroup,SWT.BORDER);
		diskSize.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		diskSize.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				try {
					VDI vdi = VDI.getByVM(c, vm).iterator().next();
					SR sr = vdi.getSR(c);					
					restrict = String.valueOf(MathUtil.RoundingDouble(((double)sr.getPhysicalSize(c))/1024.0/1024.0/1024.0, 2));
				} catch (BadServerResponse e2) {
					e2.printStackTrace();
				} catch (XenAPIException e2) {
					e2.printStackTrace();
				} catch (XmlRpcException e2) {
					e2.printStackTrace();
				}
				msgLabel.setVisible(true);
				msgLabel.setText("��λ��GB����С���ܳ�������������" + restrict);
				getContainer().updateButtons();
			}

			@Override
			public void keyReleased(KeyEvent e) {
				try{
					createDiskSize = Double.parseDouble(diskSize.getText());
				}catch(NumberFormatException e1){
					errorMsgLabel.setText("��������ֵ������");
					errorMsgLabel.setVisible(true);
					return;
				}
				errorMsgLabel.setVisible(false);
				getContainer().updateButtons();
			}
		});
		new Label(selectDiskGroup,SWT.NONE);
		new Label(selectDiskGroup,SWT.NONE);		
		msgLabel = new Label(selectDiskGroup,SWT.NONE);
		msgLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		msgLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		new Label(selectDiskGroup,SWT.NONE);
		new Label(selectDiskGroup,SWT.NONE);
		errorMsgLabel = new Label(selectDiskGroup,SWT.NONE);
		errorMsgLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		errorMsgLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		setControl(composite);
	}
	
	public class wizardDisk {
		private String DiskName;
		private double DiskSpace;
		private double AvailableSpace;
		
		public wizardDisk(String DiskName,double DiskSpace,double AvailableSpace){
			this.DiskName = DiskName;
			this.DiskSpace = DiskSpace;
			this.AvailableSpace = AvailableSpace;
		}
		public String getDiskName() {
			return DiskName;
		}
		public void setDiskName(String diskName) {
			DiskName = diskName;
		}
		public double getDiskSpace() {
			return DiskSpace;
		}
		public void setDiskSpace(long diskSpace) {
			DiskSpace = diskSpace;
		}
		public double getAvailableSpace() {
			return AvailableSpace;
		}
		public void setAvailableSpace(long availableSpace) {
			AvailableSpace = availableSpace;
		}
	}
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object element, int columnIndex) {
			if(element instanceof wizardDisk) {
				wizardDisk temp=(wizardDisk)element;
				DecimalFormat df = new DecimalFormat("0.00");
				switch(columnIndex) {
				case 0:
					return temp.getDiskName();
				case 1:
					return String.valueOf(temp.getDiskSpace());
				case 2:
					return df.format(temp.getAvailableSpace());
				}
			}
			return null;
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	
	//��һ���汾���������滻
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
		setTitle("Ϊ�������Ӵ�����Ϣ");
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
		
		new Label(comParent, SWT.NONE).setText("����Ψһ���:");
		diskUuid = new Text(comParent, SWT.BORDER);
		diskUuid.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		diskUuid.setText("������̱��(�Զ���)");
		new Label(comParent, SWT.NONE).setText("���̿ռ��С:");
		diskSize = new Text(comParent, SWT.BORDER);
		diskSize.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		diskSize.setText("������̴�С(��λGB)");
		setControl(comParent);
	}
*/
	}
