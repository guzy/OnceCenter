package oncecenter.wizard.newvmfromtemp;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.wb.swt.SWTResourceManager;

public class CpuMemoryWizardPage extends NewVMPage {

	private Spinner cpuNumSpinner;
	private Spinner memorySpinner;
	private Label msgLabel;

	private int vcpuNumber = 1;
	private int memory = 1024;
	private final int cpuLowerLimit = 1;
	private final int memoryLowerLimit = 512;
	private final int cpuUpperLimit = Integer.MAX_VALUE;
	private final int memoryUpperLimit = Integer.MAX_VALUE;
	private CpuMemoryWizardPage page;
	private Label lblMb;
	private boolean flag = true;

	public boolean canFinish() {
		return true;
	}

	/**
	 * Create the wizard.
	 */
	public CpuMemoryWizardPage() {
		super("wizardPage");
		setTitle("cpu和内存");
		setDescription("为虚拟机设定虚拟cpu的数量和内存大小");
		page = this;
		this.setPageComplete(true);
	}
	
	public CpuMemoryWizardPage(int vcpuNumber, int memory)
	{
		//,int cpuUpperLimit, int memoryUpperLimit
		super("wizardPage");
		setTitle("cpu和内存");
		setDescription("为虚拟机设定虚拟cpu的数量和内存大小");
		this.vcpuNumber = vcpuNumber;
		this.memory = memory;
		page = this;
		this.setPageComplete(true);
	}

	public int getCPUNum() {
		return cpuNumSpinner.getSelection();
	}

	public long getMemoryValue() {
		return Long.valueOf(memorySpinner.getSelection());
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite comParent = new Composite(parent,SWT.NONE);
		comParent.setLayout(new GridLayout(1, false));
		Composite container = new Composite(comParent, SWT.NULL);
		container.setLayout(new GridLayout(3, false));
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessHorizontalSpace = true;

		new Label(container, SWT.NONE).setText("虚拟cpu数量:");
		cpuNumSpinner = new Spinner(container, SWT.BORDER);
		cpuNumSpinner.setMaximum(cpuUpperLimit);
		cpuNumSpinner.setMinimum(cpuLowerLimit);
		cpuNumSpinner.setIncrement(1);
		cpuNumSpinner.setSelection(vcpuNumber);
		new Label(container, SWT.NONE);
		cpuNumSpinner.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent arg0) {
				
				int cpu = Integer.valueOf(cpuNumSpinner.getText());
				if(cpu < cpuLowerLimit || cpu > cpuUpperLimit)
				{
					page.setPageComplete(false);
					msgLabel.setText("cpu数目限制在[" + cpuLowerLimit + "  ~  "+ cpuUpperLimit + "]之间");
					flag = false;
				}
				else
				{
					msgLabel.setText("");
					page.setPageComplete(true);
					flag = true;
				}
				
				
			}
			
		});
//		cpuNumSpinner.setLayoutData(gridData);

		new Label(container, SWT.NONE).setText("内存大小:");
		memorySpinner = new Spinner(container, SWT.BORDER);
		memorySpinner.setMaximum(memoryUpperLimit);
		memorySpinner.setMinimum(memoryLowerLimit);
		memorySpinner.setIncrement(128);
		memorySpinner.setSelection(memory);
		memorySpinner.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent arg0) {
				
				String memoryValue = memorySpinner.getText();
				int memory = 0;
				if(memoryValue.length() == 0 || memoryValue.equals(""))
					memory = 0;
				else
					memory = Integer.valueOf(memorySpinner.getText());
				if(memory < memoryLowerLimit || memory > memoryUpperLimit)
				{
					msgLabel.setText("内存值大小限制在[" + memoryLowerLimit + "  ~  "+ memoryUpperLimit + "]之间");
					page.setPageComplete(false);
					flag = false;
				}
				else
				{
					msgLabel.setText("");
					page.setPageComplete(true);
					flag = true;
				}
				
			}
			
		});
		
		lblMb = new Label(container, SWT.NONE);
		lblMb.setText("MB");
		
		Composite composite = new Composite(comParent,SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		new Label(composite,SWT.NONE);
		msgLabel = new Label(composite,SWT.NONE);
		msgLabel.setText("                                                                           ");
		msgLabel.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
		msgLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		setControl(comParent);
	}

	@Override
	protected boolean nextButtonClick() {
		
		if(this.getWizard() instanceof NewVmFTWizard){
			((NewVmFTWizard)this.getWizard()).vcpu = Integer.parseInt(cpuNumSpinner.getText());
		((NewVmFTWizard)this.getWizard()).memory = Integer.parseInt(memorySpinner.getText());
		 IWizardPage nextPage = getWizard().getNextPage(this);
		 if(nextPage instanceof SelectStorageWizardPage)
			 ((SelectStorageWizardPage)nextPage).refresh();
		}
		return true;
	}
}
