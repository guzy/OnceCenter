package oncecenter.wizard.recoverlostvm;

import oncecenter.wizard.adjustvmdisk.AdjustDiskWizard;
import oncecenter.wizard.newvmfromtemp.NewVMPage;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

public class LostVmNameWizardPage extends NewVMPage {

	private Button ExactButton;
	private Text exactName;
	private Button FuzzyButton;
	private Text fuzzyName;
	
	private Combo startYear;
	private Combo startMonth;
	private Combo startDay;
	private Combo endYear;
	private Combo endMonth;
	private Combo endDay;

	public LostVmNameWizardPage() {
		super("wizardPage");
		setTitle("�һ������");
		setDescription("Ϊ�һ�������ṩ��Ϣ������ѡ��ȷ���Һ�ģ���������ַ�ʽ��");
	}

	@Override
	public void createControl(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(7,false));
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		composite.setLayoutData(gridData);
		
		ExactButton = new Button(composite, SWT.RADIO);
		ExactButton.setText("��ȷ����");
		ExactButton.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				widgetSelected(arg0);
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
//				times.setEnabled(false);
//				value.setEnabled(true);
//				unit.setEnabled(true);
			}
			
		});
		
		new Label(composite,SWT.NONE);
		new Label(composite,SWT.NONE);
		new Label(composite,SWT.NONE);
		new Label(composite,SWT.NONE);
		new Label(composite,SWT.NONE);
		new Label(composite,SWT.NONE);
		
		new Label(composite,SWT.NONE).setText("���������");
		exactName = new Text(composite,SWT.BORDER);
		exactName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		exactName.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {

			}
		});
		new Label(composite,SWT.NONE);
		new Label(composite,SWT.NONE);
		new Label(composite,SWT.NONE);
		new Label(composite,SWT.NONE);
		new Label(composite,SWT.NONE);
		
		exactName.setEnabled(false);
		
		FuzzyButton = new Button(composite, SWT.RADIO);
		FuzzyButton.setText("ģ������");
		FuzzyButton.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				widgetSelected(arg0);
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
//				times.setEnabled(false);
//				value.setEnabled(true);
//				unit.setEnabled(true);
			}
			
		});
		new Label(composite,SWT.NONE);
		new Label(composite,SWT.NONE);
		new Label(composite,SWT.NONE);
		new Label(composite,SWT.NONE);
		new Label(composite,SWT.NONE);
		new Label(composite,SWT.NONE);
		
		new Label(composite,SWT.NONE).setText("���������");
		fuzzyName = new Text(composite,SWT.BORDER);
		fuzzyName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fuzzyName.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {

			}
		});
		new Label(composite,SWT.NONE);
		new Label(composite,SWT.NONE);
		new Label(composite,SWT.NONE);
		new Label(composite,SWT.NONE);
		new Label(composite,SWT.NONE);
		fuzzyName.setEnabled(false);
		
		new Label(composite,SWT.NONE).setText("�뾡�������������ʧ�����ڣ�");
		new Label(composite,SWT.NONE);
		new Label(composite,SWT.NONE);
		new Label(composite,SWT.NONE);
		new Label(composite,SWT.NONE);
		new Label(composite,SWT.NONE);
		new Label(composite,SWT.NONE);
		
		new Label(composite,SWT.NONE).setText("�ӣ�");
		
		startYear = new Combo(composite,SWT.DROP_DOWN);
		new Label(composite,SWT.NONE).setText("��");
		startMonth = new Combo(composite,SWT.DROP_DOWN);
		new Label(composite,SWT.NONE).setText("��");
		startDay = new Combo(composite,SWT.DROP_DOWN);
		new Label(composite,SWT.NONE).setText("��");
		
		new Label(composite,SWT.NONE).setText("����");
		endYear = new Combo(composite,SWT.DROP_DOWN);
		new Label(composite,SWT.NONE).setText("��");
		endMonth = new Combo(composite,SWT.DROP_DOWN);
		new Label(composite,SWT.NONE).setText("��");
		endDay = new Combo(composite,SWT.DROP_DOWN);
		new Label(composite,SWT.NONE).setText("��");
		
		for(int i=2013;i<50;i++){
			startYear.add(i+"");
			endYear.add(i+"");
		}
		for(int i=1;i<13;i++){
			startMonth.add(i+"");
			endMonth.add(i+"");
		}
		for(int i=1;i<32;i++){
			startDay.add(i+"");
			startDay.add(i+"");
		}
		setControl(composite);
		
		setPageComplete(false);
		composite.layout();
	}

//	@Override
//	protected boolean nextButtonClick() {
//		
//		return false;
//	}

}
