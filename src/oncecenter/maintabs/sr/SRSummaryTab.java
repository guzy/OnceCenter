package oncecenter.maintabs.sr;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import oncecenter.maintabs.OnceSRTabItem;
import oncecenter.util.MathUtil;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wb.swt.SWTResourceManager;

import com.once.xenapi.Connection;
import com.once.xenapi.SR;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.XenAPIException;

public class SRSummaryTab extends OnceSRTabItem{
	
	private Label uuid;
	private Text uuidValue;
	private Label name;
	private Text nameValue;
	private Label description;
	private Label descriptionValue;
	private Label virtual;
	private Label virtualValue;
	private Label usage;
	private Label usageValue;
	private Label type;
	private Label typeValue;
	private Label contentType;
	private Label contentTypeValue;
	private Label share;
	private Label shareValue;
	
	private Composite generalComposite;
	private Composite commandComposite;
	
	private SR.Record record;
	
	public Timer generalTimer;
	public Timer performTimer;
	
	public SRSummaryTab(CTabFolder arg0, int arg1, int arg2, VMTreeObjectSR object) {
		super(arg0, arg1, arg2, object);
		setText("常规");
		Init();
	}

	@Override
	public boolean Init() {
		composite = new Composite(folder, SWT.NONE);
		setControl(composite);
		composite.setBackground(new Color(null,255,255,255));
		composite.setLayout(new GridLayout(2,false));
		
		generalComposite = new Composite(composite, SWT.NONE); 
		generalComposite.setBackground(new Color(null,255,255,255));
		GridLayout layout = new GridLayout(1,true);
		layout.verticalSpacing=0;
		generalComposite.setLayout(layout);
		{
			GridData gd_canvas = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
			gd_canvas.widthHint = 400;
			generalComposite.setLayoutData(gd_canvas);
		}
		
		{
			Label lblNewLabel = new Label(generalComposite, SWT.NONE);
			lblNewLabel.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
			lblNewLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
			lblNewLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
			lblNewLabel.setBounds(10, 10, 300, 20);
			lblNewLabel.setText("  常规 ");
			lblNewLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		{
			Composite composite_1 = new Composite(generalComposite, SWT.BORDER);
			composite_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			composite_1.setBounds(10, 30, 300, 200);
			composite_1.setLayout(new GridLayout(2,false));
			composite_1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			composite_1.setBackgroundMode(SWT.INHERIT_DEFAULT);
			
			uuid = new Label(composite_1, SWT.NONE);
			uuid.setText("  UUID:       ");
			uuidValue = new Text(composite_1, SWT.NONE);
			uuidValue.setText("");
			
			name = new Label(composite_1, SWT.NONE);
			name.setText("  名称:       ");
			nameValue = new Text(composite_1, SWT.NONE);
			nameValue.setText("");
			
			description = new Label(composite_1, SWT.NONE);
			description.setText("  描述:       ");
			descriptionValue = new Label(composite_1, SWT.NONE);
			descriptionValue.setText("");
			
			virtual = new Label(composite_1, SWT.NONE);
			virtual.setText("  空间总量:       ");
			virtualValue = new Label(composite_1, SWT.NONE);
			virtualValue.setText("");
			
			usage = new Label(composite_1, SWT.NONE);
			usage.setText("  已使用量:       ");
			usageValue = new Label(composite_1, SWT.NONE);
			usageValue.setText("");
			
			type = new Label(composite_1, SWT.NONE);
			type.setText("  存储类型:       ");
			typeValue = new Label(composite_1, SWT.NONE);
			typeValue.setText("");
			
			contentType = new Label(composite_1, SWT.NONE);
			contentType.setText("  内容类型:       ");
			contentTypeValue = new Label(composite_1, SWT.NONE);
			contentTypeValue.setText("");
			
			share = new Label(composite_1, SWT.NONE);
			share.setText("  是否共享:       ");
			shareValue = new Label(composite_1, SWT.NONE);
			shareValue.setText("");
			
		}
		
		
//		commandComposite = new Composite(composite, SWT.NONE); 
//		commandComposite.setBackground(new Color(null,255,255,255));
//		commandComposite.setLayout(layout);
//		{
//			GridData gd_canvas = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
//			gd_canvas.widthHint = 400;
//			commandComposite.setLayoutData(gd_canvas);
//		}
//		{
//			Label lblNewLabel = new Label(commandComposite, SWT.NONE);
//			lblNewLabel.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
//			lblNewLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
//			lblNewLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
//			lblNewLabel.setBounds(10, 10, 300, 20);
//			lblNewLabel.setText("  命令                                               ");
//			lblNewLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		}
//		{
//			Composite composite_1 = new Composite(commandComposite, SWT.BORDER);
//			composite_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
//			composite_1.setBounds(10, 30, 300, 200);
//			composite_1.setLayout(new GridLayout(1,false));
//			composite_1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//			
//			
//		}
		generalTimer = new Timer("GeneralTimer");
		generalTimer.schedule(new GeneralTimer(this,PlatformUI.getWorkbench().getDisplay()), 0, 3000);
		if(objectSR.timerList == null)
			objectSR.timerList = new ArrayList<Timer>();
		objectSR.timerList.add(generalTimer);
		composite.layout();
		return true;
	}
	
	class GeneralTimer extends TimerTask
	{
		private SRSummaryTab poolTab;
		Display display;
		
		public GeneralTimer(SRSummaryTab poolTab, Display display)
		{
			this.poolTab = poolTab;
			this.display = display;
		}

		@Override
		public void run() {
			
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	poolTab.refreshGeneral();
					}
			    };
			    this.display.asyncExec(runnable); 
			}
		}	
	}
	
	public void refreshGeneral()
	{
			if(objectSR.getItemState().equals(ItemState.changing))
				return;
			if(objectSR.getItemState().equals(ItemState.able))
			{
				record = objectSR.getRecord();
				if(record != null)
				{
					objectSR.setRecord(record);
					uuidValue.setText(record.uuid);
					SR sr = (SR)objectSR.getApiObject();					
//					nameValue.setText(record.nameLabel);
					try {
						nameValue.setText(sr.getNameLabel(objectSR.getConnection()));
					} catch (Exception e) {
						e.printStackTrace();
					}
					descriptionValue.setText(record.nameDescription);
					virtualValue.setText(MathUtil.Rounding(record.physicalSize/1024.0/1024.0/1024.0, 2) + "GB");
					double physicalUtilisation = record.physicalUtilisation/1024/1024;
					if(physicalUtilisation > 1024)
						usageValue.setText(MathUtil.Rounding(physicalUtilisation/1024, 2) + "GB");
					else
						usageValue.setText(MathUtil.Rounding(physicalUtilisation, 2) + "MB");
					if(record.type.equals("nfs_zfs")){
						typeValue.setText("gluster_zfs");
					}else{
						typeValue.setText(record.type);
					}
					
					contentTypeValue.setText(record.contentType);
					if(record.shared != null)
					{
						if(record.shared)
							shareValue.setText("可以共享");
						else
							shareValue.setText("不能共享");
					}
					else
						shareValue.setText("");
				}
			}
			else
			{
				nameValue.setText(objectSR.getName());
			}	
		uuidValue.pack();
		nameValue.pack();
		descriptionValue.pack();
		virtualValue.pack();
		usageValue.pack();
		typeValue.pack();
		contentTypeValue.pack();
		shareValue.pack();
	}
}
