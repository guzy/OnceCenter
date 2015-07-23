package oncecenter.p2v.wizard.fromlinux;

import oncecenter.wizard.newvmfromtemp.NewVMPage;
import oncecenter.wizard.newvmfromtemp.NewVmFTWizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

public class P2vFromLinuxVMPage extends NewVMPage{

	public Text name;
	public String getName() {
		return name.getText();
	}

	public String getDescription() {
		return description.getText();
	}

	public Text description;
	private Label msgLabel;
	public P2vFromLinuxVMPage(String pageName) {
		super(pageName);
		
		this.setTitle("设置要导入的虚拟机的名称和描述");
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
				}
				else if(name.getText().contains(" "))
				{
					msgLabel.setText("虚拟机的名称中不能包含空格！");
					msgLabel.setVisible(true);
					setPageComplete(false);
				}
				else
				{
					msgLabel.setVisible(false);
					setPageComplete(true);
				}
				
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
		
		return true;
	}
}
