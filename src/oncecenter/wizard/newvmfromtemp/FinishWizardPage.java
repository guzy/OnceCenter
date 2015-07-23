package oncecenter.wizard.newvmfromtemp;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;


public class FinishWizardPage extends NewVMPage {

	private VMTreeObject server;
	private Table table;
	private TableItem templateTableItem;
	private TableItem nameTableItem;
	private TableItem setupModeTableItem;
	private TableItem setupSourceTableItem;
	private TableItem hostTableItem;
	private TableItem vcpusTableItem;
	private TableItem diskTableItem;
	private TableItem memoryTableItem;
	private TableItem networkTableItem;
	

	SelectTemplateWizardPage templatePage;
	VmNameWizardPage namePage;
	SelectServerWizardPage serverPage;
	CpuMemoryWizardPage cpuPage; 
	SelectStorageWizardPage storagePage;

	/**
	 * Create the wizard.
	 */
	public FinishWizardPage() {
		super("wizardPage");
		init();
	}

	/**
	 * @wbp.parser.constructor
	 */
	public FinishWizardPage(VMTreeObject server) {
		super("wizardPage");
		//init();
		this.server = server;
	}

	private void init() {
		setTitle("准备开始创建虚拟机");
		setDescription("所有需要的参数已经被列出在下面的表格中，请您进行最后的检查");
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(1,false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		table = new Table( composite, SWT.BORDER | SWT.V_SCROLL
		        | SWT.H_SCROLL|SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		TableColumn tc1 = new TableColumn(table, SWT.CENTER);
		TableColumn tc2 = new TableColumn(table, SWT.CENTER);
		tc1.setText("Property");
		tc2.setText("Value");
		tc1.setWidth(110);
		tc2.setWidth(383);
		table.setHeaderVisible(true);

		templateTableItem = new TableItem(table, SWT.NONE);
		templateTableItem.setText(new String[] {
				"模板",((NewVmFTWizard)this.getWizard()).templateName });
		nameTableItem = new TableItem(table, SWT.NONE);
		nameTableItem.setText(new String[] {
				"名称",((NewVmFTWizard)this.getWizard()).vmName });
		setupModeTableItem = new TableItem(table, SWT.NONE);
		setupModeTableItem.setText(new String[] {
				"安装模式",((NewVmFTWizard)this.getWizard()).selectedTemp==null?
						"CD":"从用户模板安装" });
		setupSourceTableItem = new TableItem(table, SWT.NONE);
		setupSourceTableItem.setText(new String[] {
				"安装源",((NewVmFTWizard)this.getWizard()).selectedMedia==null?
						"<empty>":((NewVmFTWizard)this.getWizard()).selectedMedia.nameLabel });
		hostTableItem = new TableItem(table, SWT.NONE);
		if(!((NewVmFTWizard)this.getWizard()).isAssignHost){
			hostTableItem.setText(new String[] {
					"主机","未指定" });
		}else{
			hostTableItem.setText(new String[] {
					"主机",((NewVmFTWizard)this.getWizard()).selectedHost.getName() });
		}
		vcpusTableItem = new TableItem(table, SWT.NONE);
		vcpusTableItem.setText(new String[] {
				"vCPUs",((NewVmFTWizard)this.getWizard()).vcpu+"" });
		memoryTableItem = new TableItem(table, SWT.NONE);
		memoryTableItem.setText(new String[] {
				"Memory",((NewVmFTWizard)this.getWizard()).memory +" MB" });
		diskTableItem = new TableItem(table, SWT.NONE);
		diskTableItem.setText(new String[] {
				"Disk 0",((NewVmFTWizard)this.getWizard()).storage+"GB" });
		networkTableItem = new TableItem(table, SWT.NONE);
		networkTableItem.setText(new String[] {
				"Network Interface",((NewVmFTWizard)this.getWizard()).net });
		setControl(composite);

//		Button btnCheckButton = new Button(composite, SWT.CHECK);
//		btnCheckButton.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent arg0) {
//			}
//		});
//		btnCheckButton.setBounds(10, 200, 210, 20);
//		btnCheckButton.setText("Start the new VM automatically");
	}

	@Override
	protected boolean nextButtonClick() {
		
		return true;
	}
	
	public void refresh()
	{
		if(this.getControl() == null)
			return;
		templateTableItem.setText(new String[] {
				"模板",((NewVmFTWizard)this.getWizard()).templateName });
		nameTableItem.setText(new String[] {
				"名称",((NewVmFTWizard)this.getWizard()).vmName });
		setupModeTableItem.setText(new String[] {
				"安装模式",((NewVmFTWizard)this.getWizard()).selectedTemp==null?
						"CD":"从用户模板安装" });
		setupSourceTableItem.setText(new String[] {
				"安装源",((NewVmFTWizard)this.getWizard()).selectedMedia==null?
						"<empty>":((NewVmFTWizard)this.getWizard()).selectedMedia.nameLabel });
		if(!((NewVmFTWizard)this.getWizard()).isAssignHost){
			hostTableItem.setText(new String[] {
					"主机","未指定" });
		}else{
			hostTableItem.setText(new String[] {
					"主机",((NewVmFTWizard)this.getWizard()).selectedHost.getName() });
		}
		vcpusTableItem.setText(new String[] {
				"vCPUs",((NewVmFTWizard)this.getWizard()).vcpu+"" });
		memoryTableItem.setText(new String[] {
				"Memory",((NewVmFTWizard)this.getWizard()).memory +" MB" });
		diskTableItem.setText(new String[] {
				"Disk 0",((NewVmFTWizard)this.getWizard()).storage+"GB" });
		networkTableItem.setText(new String[] {
				"Network Interface",((NewVmFTWizard)this.getWizard()).net });
	}
}
