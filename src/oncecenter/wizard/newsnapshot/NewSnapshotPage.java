package oncecenter.wizard.newsnapshot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class NewSnapshotPage extends WizardPage {

	String tempName;
	int index;
	public Text name;
	public String getName() {
		return name.getText() ;
	}

	public void setName(String name) {
		this.name.setText(name);
	}

	public Text description;
	private Label msgLabel;
	
	protected NewSnapshotPage(String pageName) {
		super(pageName);
		
		this.setTitle("填写新建快照的名字和描述信息");
		setDescription("请设定新建的名称并给出描述(名称只能有字母和数字)，名称可更改");
	}

	@Override
	public void createControl(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(2, false);
		layout.verticalSpacing = 15;
		composite.setLayout(layout);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.WRAP;
		gridData.grabExcessHorizontalSpace = true;

		new Label(composite, SWT.NONE).setText("快照名称:");
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
					msgLabel.setText("请输入快照的名称！");
					msgLabel.setVisible(true);
					setPageComplete(false);
				}
				else if(!isValid(name.getText()))
				{
					msgLabel.setText("快照的名称中只能有字母和数字！");
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
		
		new Label(composite, SWT.TOP).setText("描述信息:");
		description = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.WRAP |SWT.V_SCROLL);
		GridData gd_description = new GridData(GridData.FILL_HORIZONTAL);
		gd_description.heightHint = 100;
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
	
	private boolean isValid(String name)
	{
		if(name == null || name.length() == 0)
			return false;
		Pattern pattern = Pattern.compile("^[0-9a-zA-Z]+$");
		Matcher matcher = pattern.matcher(name);
		if(!matcher.find())
			return false;
		return true;
	}
}