package oncecenter.maintabs.template;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import oncecenter.action.NewVMAction;
import oncecenter.action.template.DeleteTempAction;
import oncecenter.action.template.QuickCreateVMAction;
import oncecenter.maintabs.OnceTempTabItem;
import oncecenter.util.ImageRegistry;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectTemplate;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wb.swt.SWTResourceManager;

import com.once.xenapi.VM;

public class TempSummaryTab extends OnceTempTabItem {
	
	private Label name;
	private Text nameValue;
	private Label uuid;
	private Text uuidValue;
	private Label description;
	private Label descriptionValue;
	private Label os;
	private Label osValue;
	private Label cpu;
	private Label memory;
	private Label disk;
	
	private Composite generalComposite;
	private Composite commandComposite;
	
	private VM.Record record;
	
	public Timer generalTimer;
	public Timer performTimer;
	
	public TempSummaryTab(CTabFolder arg0, int arg1, VMTreeObjectTemplate object) {
		super(arg0, arg1,object);
		setText("常规");
		objectTemplate = object;
		Init();
	}

	public TempSummaryTab(CTabFolder arg0, int arg1, int arg2, VMTreeObjectTemplate object) {
		super(arg0, arg1, arg2,object);
		setText("常规");
		objectTemplate = object;
		Init();
	}
	
	public boolean Init(){
		Composite composite = new Composite(folder, SWT.FILL); 
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

			//if(record!=null&&record.powerState.equals(Types.VmPowerState.RUNNING)){
				name=new Label(composite_1, SWT.NONE);
				name.setText("  名称：      ");
				nameValue=new Text(composite_1,SWT.NONE);
				nameValue.setText(objectTemplate.getName());
				
				description=new Label(composite_1, SWT.NONE);
				description.setText("  描述：      ");			
				descriptionValue=new Label(composite_1,SWT.NONE);

				uuid=new Label(composite_1, SWT.NONE);
				uuid.setText("  UUID:       ");
				uuidValue=new Text(composite_1, SWT.NONE);
						
				os=new Label(composite_1, SWT.NONE);
				os.setText("  操作系统：      ");			
				osValue=new Label(composite_1,SWT.NONE);
				osValue.setText("");
				
				new Label(composite_1,SWT.NONE).setText("  CPU：  ");
				cpu = new Label(composite_1,SWT.NONE);
				
				new Label(composite_1,SWT.NONE).setText("  内存：  ");
				memory = new Label(composite_1,SWT.NONE);
				
				new Label(composite_1,SWT.NONE).setText("  硬盘大小：  ");
				disk = new Label(composite_1,SWT.NONE);
				
		}
				
		commandComposite = new Composite(composite, SWT.NONE); 
		commandComposite.setBackground(new Color(null,255,255,255));
		commandComposite.setLayout(layout);
		{
			GridData gd_canvas = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
			gd_canvas.widthHint = 400;
			commandComposite.setLayoutData(gd_canvas);
		}
		{
			Label lblNewLabel = new Label(commandComposite, SWT.NONE);
			lblNewLabel.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
			lblNewLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
			lblNewLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
			lblNewLabel.setBounds(10, 10, 300, 20);
			lblNewLabel.setText("  命令                                               ");
			lblNewLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		
		{
			Composite composite_1 = new Composite(commandComposite, SWT.BORDER);
			composite_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			composite_1.setBounds(10, 30, 300, 200);
			composite_1.setLayout(new GridLayout(1,false));
			composite_1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			{
				final CLabel lblNewLabel_1 = new CLabel(composite_1, SWT.MULTI);
				lblNewLabel_1.setForeground(SWTResourceManager.getColor(0, 0, 128));
				lblNewLabel_1.addMouseTrackListener(new MouseTrackAdapter() {
					@Override
					public void mouseEnter(MouseEvent arg0) {
						lblNewLabel_1.setBackground(SWTResourceManager.getColor(240, 248, 255));
						
					}
					@Override
					public void mouseExit(MouseEvent arg0) {
						lblNewLabel_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						
					}
				});
				lblNewLabel_1.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseDown(MouseEvent arg0) {
						
						NewVMAction action=new NewVMAction(objectTemplate);
						action.run();
					}
				});
				lblNewLabel_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				lblNewLabel_1.setImage(ImageRegistry.getImage(ImageRegistry.STARTUP));
				lblNewLabel_1.setBounds(57, 33, 244, 87);
				lblNewLabel_1.setText("新建虚拟机");
			}
			
			{
				final CLabel lblNewLabel_1 = new CLabel(composite_1, SWT.MULTI);
				lblNewLabel_1.setForeground(SWTResourceManager.getColor(0, 0, 128));
				lblNewLabel_1.addMouseTrackListener(new MouseTrackAdapter() {
					@Override
					public void mouseEnter(MouseEvent arg0) {
						lblNewLabel_1.setBackground(SWTResourceManager.getColor(240, 248, 255));
						
					}
					@Override
					public void mouseExit(MouseEvent arg0) {
						lblNewLabel_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						
					}
				});
				lblNewLabel_1.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseDown(MouseEvent arg0) {
						QuickCreateVMAction action=new QuickCreateVMAction((VMTreeObjectTemplate)objectTemplate);
						action.run();
					}
				});
				lblNewLabel_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				lblNewLabel_1.setImage(ImageRegistry.getImage(ImageRegistry.REBOOT));
				lblNewLabel_1.setBounds(57, 33, 244, 87);
				lblNewLabel_1.setText("快速生成虚拟机");
			}
			
			{
				final CLabel lblNewLabel_1 = new CLabel(composite_1, SWT.MULTI);
				lblNewLabel_1.setForeground(SWTResourceManager.getColor(0, 0, 128));
				lblNewLabel_1.addMouseTrackListener(new MouseTrackAdapter() {
					@Override
					public void mouseEnter(MouseEvent arg0) {
						lblNewLabel_1.setBackground(SWTResourceManager.getColor(240, 248, 255));
						
					}
					@Override
					public void mouseExit(MouseEvent arg0) {
						lblNewLabel_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						
					}
				});
				lblNewLabel_1.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseDown(MouseEvent arg0) {
						DeleteTempAction action=new DeleteTempAction(objectTemplate);
						action.run();
						
					}
				});
				lblNewLabel_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				lblNewLabel_1.setImage(ImageRegistry.getImage(ImageRegistry.SHUTDOWN));
				lblNewLabel_1.setBounds(57, 33, 244, 87);
				lblNewLabel_1.setText("删除模板");
			}			
		}
		generalTimer = new Timer("GeneralTimer");
		generalTimer.schedule(new GeneralTimer(this,PlatformUI.getWorkbench().getDisplay()), 0, 5000);
		if(objectTemplate.timerList == null)
			objectTemplate.timerList = new ArrayList<Timer>();
		objectTemplate.timerList.add(generalTimer);
		composite.layout();
		return true;
	}
	
	class GeneralTimer extends TimerTask
	{
		private TempSummaryTab poolTab;
		Display display;
		
		public GeneralTimer(TempSummaryTab poolTab, Display display)
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
	
	public void refreshGeneral(){
			if(objectTemplate.getItemState().equals(ItemState.changing))
				return;
			record = objectTemplate.getRecord();
			
			if(record!=null){
				
				objectTemplate.setRecord(record);

				nameValue.setText(record.nameLabel);
				descriptionValue.setText(record.nameDescription);
				uuidValue.setText(record.uuid);
				//操作系统信息现在还是空
				osValue.setText("");
				cpu.setText(record.VCPUsMax+" vcpu");
				memory.setText(record.memoryDynamicMax/1024/1024+" MB");
				disk.setText("");
			}
			
			nameValue.pack();
			descriptionValue.pack();
			uuidValue.pack();
			osValue.pack();
			cpu.pack();
			memory.pack();
			disk.pack();
	}
	
	public VMTreeObject getObject() {
		return objectTemplate;
	}

	public void setObject(VMTreeObjectTemplate object) {
		this.objectTemplate = object;
	}
	


}
