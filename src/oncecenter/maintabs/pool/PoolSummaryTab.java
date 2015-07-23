package oncecenter.maintabs.pool;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import oncecenter.action.NewSRAction;
import oncecenter.action.NewVMAction;
import oncecenter.maintabs.OncePoolTabItem;
import oncecenter.util.ImageRegistry;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.apache.xmlrpc.XmlRpcException;
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

import com.once.xenapi.Host;
import com.once.xenapi.Pool;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.XenAPIException;

public class PoolSummaryTab extends OncePoolTabItem{
	private Label uuid;
	private Text uuidValue;
	private Label name;
	private Text nameValue;
	private Label description;
	private Label descriptionValue;
	private Label state;
	private Label stateValue;
	private Label master;
	private Label masterValue;
	private Label defaultSR;
	private Label defaultSRValue;
	

	private CLabel newSR;
	private CLabel newVM;
	//private CLabel deletePool;
	
	private Composite generalComposite;
	private Composite commandComposite;

	private Pool.Record record = null;
	
	public Timer generalTimer;

	public PoolSummaryTab(CTabFolder arg0, int arg1, VMTreeObjectPool object) {
		super(arg0, arg1, object);
		setText("常规");
		this.objectPool = (VMTreeObjectPool)object;
		Init();
	}
	
	public PoolSummaryTab(CTabFolder arg0, int arg1, int arg2, VMTreeObjectPool object)
	{
		super(arg0, arg1, arg2, object);
		setText("常规");
		this.objectPool = (VMTreeObjectPool)object;
		Init();
	}


	public boolean Init()
	{
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
			
			state = new Label(composite_1, SWT.NONE);
			state.setText("  连接状态:       ");
			stateValue = new Label(composite_1, SWT.NONE);
			stateValue.setText("");
			
			master = new Label(composite_1, SWT.NONE);
			master.setText("  主结点:       ");
			masterValue = new Label(composite_1, SWT.NONE);
			masterValue.setText("");
			
//			defaultSR = new Label(composite_1, SWT.NONE);
//			defaultSR.setText("  默认存储:       ");
//			defaultSRValue = new Label(composite_1, SWT.NONE);
//			defaultSRValue.setText("");
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
			
			newVM = new CLabel(composite_1, SWT.NONE);
			newVM.setForeground(SWTResourceManager.getColor(0, 0, 128));
			newVM.addMouseTrackListener(new MouseTrackAdapter(){
				@Override
				public void mouseEnter(MouseEvent arg0) {
					newVM.setBackground(SWTResourceManager.getColor(240, 248, 255));
					
				}
				@Override
				public void mouseExit(MouseEvent arg0) {
					newVM.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
					
				}
			});
			newVM.addMouseListener(new MouseAdapter(){
				@Override
				public void mouseDown(MouseEvent arg0) {
					
					NewVMAction action=new NewVMAction(objectPool);
					action.run();
				}
			});
			
			newVM.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			newVM.setImage(ImageRegistry.getImage(ImageRegistry.ADDVMDISABLE));
			newVM.setBounds(57, 33, 244, 87);
			newVM.setText("新建虚拟机");
			newVM.setEnabled(false);
			
			newSR = new CLabel(composite_1, SWT.NONE);
			newSR.setForeground(SWTResourceManager.getColor(0, 0, 128));
			newSR.addMouseTrackListener(new MouseTrackAdapter(){
				@Override
				public void mouseEnter(MouseEvent arg0) {
					newSR.setBackground(SWTResourceManager.getColor(240, 248, 255));
					
				}
				@Override
				public void mouseExit(MouseEvent arg0) {
					newSR.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
					
				}
			});
			
			newSR.addMouseListener(new MouseAdapter(){
				@Override
				public void mouseDown(MouseEvent arg0) {
					
					NewSRAction action = new NewSRAction();
					action.run();
				}
			});
			newSR.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			newSR.setImage(ImageRegistry.getImage(ImageRegistry.ADDSTORAGEDISABLE));
			newSR.setBounds(57, 33, 244, 87);
			newSR.setText("新建存储");
			newSR.setEnabled(false);
			
			
		}
		//generalTimer = new Timer("GeneralTimer");
		//generalTimer.schedule(new GeneralTimer(this,PlatformUI.getWorkbench().getDisplay()), 0, 5000);
		generalTimer = new Timer("GeneralTimer");
		generalTimer.schedule(new GeneralTimer(this,PlatformUI.getWorkbench().getDisplay()), 0, 5000);
		//InitThread t=new InitThread(this,PlatformUI.getWorkbench().getDisplay());
		//t.run();
		if(objectPool.timerList == null)
			objectPool.timerList = new ArrayList<Timer>();
		objectPool.timerList.add(generalTimer);
		composite.layout();
		return true;
	}
	
	class GeneralTimer extends TimerTask
	{
		private PoolSummaryTab poolTab;
		Display display;
		
		public GeneralTimer(PoolSummaryTab poolTab, Display display)
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
			if(objectPool.getItemState().equals(ItemState.changing))
				return;
			if(objectPool.getItemState().equals(ItemState.able))
			{
				record = objectPool.getRecord();
				if(record != null)
				{
					objectPool.setRecord(record);					
					uuidValue.setText(record.uuid);					
					nameValue.setText(record.nameLabel);
					descriptionValue.setText(record.nameDescription);
					Host host = record.master;
					String masterName = "";
					for(VMTreeObject o:objectPool.getChildren()){
						if(o.getApiObject().equals(host)){
							masterName = o.getName();
							break;
						}
					}
					masterValue.setText(masterName);
//					try {
					//defaultSR属性已经废弃
//						defaultSRValue.setText(record.defaultSR.getNameLabel(objectPool.getConnection()));
//					} catch (BadServerResponse e) {
//						e.printStackTrace();
//					} catch (XenAPIException e) {
//						e.printStackTrace();
//					} catch (XmlRpcException e) {
//						e.printStackTrace();
//					}
					newVM.setImage(ImageRegistry.getImage(ImageRegistry.ADDVM));
					newVM.setEnabled(true);
					newSR.setImage(ImageRegistry.getImage(ImageRegistry.ADDSTORAGE));
					newSR.setEnabled(true);
					
					stateValue.setText("已连接");
				}
			}
			else
			{
				nameValue.setText(objectPool.getName());
				stateValue.setText("已断开");
			}
			uuidValue.pack();
			nameValue.pack();
			stateValue.pack();
			descriptionValue.pack();
			masterValue.pack();
//			defaultSRValue.pack();
			
	}
}
