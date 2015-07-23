package oncecenter.wizard.periodicsnapshot;

import java.util.Calendar;

import oncecenter.util.CalendarUtil;
import oncecenter.util.ImageRegistry;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;



public class PeriodicSnapshotWizardPage extends WizardPage {

	private Label vmNameLabel;
	private Text vmNameText;
	private Label placeHolder1;
	
	private Label lifeTimeLabel;
	private Text lifeTimeText;
	private Combo lifeTimeCombo;
	
	private Label startTimeLabel;
	private Text startTimeText;
	private Calendar stCalendar;
	private CLabel startTimeCLabel;
	
	private Label endTimeLabel;
	private Text endTimeText;
	private CLabel endTimeCLabel;
	
	private Label cycleLabel;
	private Combo cycleCombo;
	private CLabel cycleCLabel;
	
	private Label workdayLabel;
	private Composite workdayCom;
	
	
	protected PeriodicSnapshotWizardPage(String pageName) {
		super(pageName);
		
		this.setTitle("配置定期快照任务");
	}

	@Override
	public void createControl(Composite parent) {
		
		Composite composite = new Composite(parent,SWT.NULL);
		GridLayout layout = new GridLayout(12,false);
		layout.verticalSpacing = 10;
		layout.horizontalSpacing = 15;
		composite.setLayout(layout);
		GridData layoutData = new GridData();
		layoutData.horizontalSpan = 10;
		layoutData.horizontalAlignment = SWT.FILL;
		layoutData.verticalAlignment = SWT.FILL;
//		layoutData.grabExcessHorizontalSpace = true;
//		layoutData.grabExcessVerticalSpace = true;
		
		vmNameLabel = new Label(composite, SWT.NULL);
		vmNameLabel.setText("虚拟机");
		vmNameText = new Text(composite, SWT.BORDER);
		vmNameText.setLayoutData(layoutData);
		vmNameText.setText("Centos11");
		placeHolder1= new Label(composite, SWT.NULL);
		
		lifeTimeLabel = new Label(composite,SWT.NULL);
		lifeTimeLabel.setText("存放时间");
		lifeTimeText = new Text(composite, SWT.BORDER);
		lifeTimeText.setLayoutData(layoutData);
		lifeTimeCombo = new Combo(composite, SWT.DROP_DOWN);
		String[] items = {"小时","天","周","月","年"};
		lifeTimeCombo.setItems(items);
		lifeTimeCombo.select(2);
		
		startTimeLabel = new Label(composite,SWT.NULL);
		startTimeLabel.setText("起始时间");
		startTimeText = new Text(composite,SWT.BORDER);
		startTimeText.setLayoutData(layoutData);
		startTimeText.addMouseListener(new MouseAdapter()
        {
            public void mouseUp(MouseEvent e)
            {
            	CalendarUtil dialog = CalendarUtil.getInstance(Display.getCurrent().getActiveShell()
            			,getDtLocation(startTimeText));
            	dialog.setText(startTimeText);
                dialog.open();
            }
        });
		startTimeCLabel = new CLabel(composite,SWT.NULL);
		startTimeCLabel.setToolTipText("在此之前不进行快照");
		startTimeCLabel.setImage(ImageRegistry.getImage(ImageRegistry.SNAPINFO));

		endTimeLabel = new Label(composite,SWT.NULL);
		endTimeLabel.setText("终止时间");
		endTimeText = new Text(composite,SWT.BORDER);
		endTimeText.setLayoutData(layoutData);
		endTimeText.addMouseListener(new MouseAdapter()
        {
            public void mouseUp(MouseEvent e)
            {
            	CalendarUtil dialog = new CalendarUtil(Display.getCurrent().getActiveShell()
            			,getDtLocation(endTimeText));
            	dialog.setText(endTimeText);
                dialog.open();
            }
        });
		endTimeCLabel = new CLabel(composite,SWT.NULL);
		endTimeCLabel.setToolTipText("在此之后不进行快照");
		endTimeCLabel.setImage(ImageRegistry.getImage(ImageRegistry.SNAPINFO));
		
		cycleLabel = new Label(composite,SWT.NULL);
		cycleLabel.setText("周期");
		cycleCombo = new Combo(composite, SWT.DROP_DOWN);
		String[] itemsCycle = {"15minutes","30minutes","1hours","2hours","3hours","4hours","6hours","12hours","1day","1week"};
		cycleCombo.setItems(itemsCycle);
		cycleCombo.select(2);
		cycleCLabel = new CLabel(composite,SWT.NULL);
		cycleCLabel.setToolTipText("两次快照尝试的时间间隔");
		cycleCLabel.setImage(ImageRegistry.getImage(ImageRegistry.SNAPINFO));
		cycleCLabel.setLayoutData(layoutData);
		
		workdayLabel = new Label(composite,SWT.NULL);
		workdayLabel.setText("工作日");
		GridData data = new GridData();
		data.verticalAlignment = SWT.TOP;
		workdayLabel.setLayoutData(data);
		workdayCom = new Composite(composite,SWT.NULL);
		workdayCom.setLayout(new GridLayout(1,false));
//		workdayCom.setBackground(new Color(null,255,255,255));
		String[] workdays = {"星期一","星期二","星期三","星期四","星期五","星期六","星期日"};
		final int workdayNum = 7;
		for(int i = 0; i < workdayNum; ++i)
		{
			Button button = new Button(workdayCom,SWT.CHECK);
			button.setText(workdays[i]);
		}
		this.setControl(composite);
		
	}
	
	 private Point getDtLocation(Text text)
	 {
		return new Point(text.getLocation().x + this.getShell().getLocation().x,
                 text.getLocation().y + this.getShell().getLocation().y + 140);
	 }
	 

}
