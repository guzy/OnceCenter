package oncecenter.maintabs.vm;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import oncecenter.Constants;
import oncecenter.action.vm.CreateSnapshotAction;
import oncecenter.action.vm.DeleteSnapshotAction;
import oncecenter.action.vm.RollbackSnapshotAction;
import oncecenter.action.vm.snapshotstrategy.GetSnapshotStrategy;
import oncecenter.action.vm.snapshotstrategy.SetSnapshotStrategy;
import oncecenter.maintabs.OnceVMTabItem;
import oncecenter.util.ImageRegistry;
import oncecenter.util.snapshotstrategy.SnapshotStrategy;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;



import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wb.swt.SWTResourceManager;

import com.once.xenapi.Connection;
import com.once.xenapi.Types;
import com.once.xenapi.VM;

public class VMSnapShotsTab extends OnceVMTabItem {
	
	private Label vmNameLabel;
	private Text vmNameText;
	
	private Label lifeTimeLabel;
	private Text lifeTimeText;
	
	private Label currentReverseNumberLabel;
	private Label currentPeriodLabel;
//	private Combo lifeTimeCombo;
//	
//	private Label startTimeLabel;
//	private Combo startTimeCombo;
//	private CLabel startTimeCLabel;
//	
//	private Label endTimeLabel;
//	private Combo endTimeCombo;
//	private CLabel endTimeCLabel;
	
	private Label cycleLabel;
	private Combo cycleCombo;
	private CLabel cycleCLabel;
	
	
	private Table snapTable;
	private TableViewer tableViewer;
	
	
	private Label nameLabel;
	private Label valueLabel;
	ArrayList<Snapshot> sapList = new ArrayList<Snapshot>();
	ArrayList<TableEditor> editorList = new ArrayList<TableEditor>();
	private VMTreeObjectVM selection;
	private VMSnapShotsTab tab;
	private Button editButton;
	private SnapshotStrategy strategy = new SnapshotStrategy("1d",10);
	
	Button createSnapshotButton;
	private Button rollbackSnapshotButton;
	private Button deleteSnapshotButton;
	
	public VMSnapShotsTab(CTabFolder arg0, int arg1, VMTreeObjectVM object) {
		super(arg0, arg1, object);
		setText("快照");
		this.objectVM = object;
		selection = object;
		tab = this;
	}

	public VMSnapShotsTab(CTabFolder arg0, int arg1, int arg2, VMTreeObjectVM object) {
		super(arg0, arg1, arg2, object);
		setText("快照");
		this.objectVM = object;
		selection = object;
		tab = this;
	}
	
	public boolean Init(){
		//点击  快照  选项卡以后会在这里进行初始化操作，例如获取虚拟机之前的快照策略
		composite = new Composite(folder, SWT.FILL);
		composite.setBackground(new Color(null,255,255,255));
		composite.setBackgroundMode(SWT.INHERIT_DEFAULT);  
		composite.setLayout(new GridLayout(1,false));
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		composite.setLayoutData(gridData);
		createOneoffContent(composite);
		createPeriodicalContent(composite);
		setControl(composite);
		composite.layout();
		GetSnapshotStrategy action = new GetSnapshotStrategy(objectVM,strategy,this);
		action.run();
		return false;
	}
	
	private void createOneoffContent(Composite parent)
	{
		Composite contentCom = new Composite(parent,SWT.NULL);
		contentCom.setBackground(new Color(null,255,255,255));
		contentCom.setBackgroundMode(SWT.INHERIT_DEFAULT);  
		contentCom.setLayout(new GridLayout(6,false));
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		contentCom.setLayoutData(gridData);
		final VMTreeObjectVM selection = this.objectVM;
		createSnapshotButton = new Button(contentCom, SWT.NONE); 
		createSnapshotButton.setText("创建快照");
		createSnapshotButton.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				widgetSelected(arg0);
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				CreateSnapshotAction action = new CreateSnapshotAction(selection,tab);
				action.run();
				refreshTable();
			}
		});

		
		rollbackSnapshotButton = new Button(contentCom, SWT.NONE); 
		rollbackSnapshotButton.setText("回滚快照");
		rollbackSnapshotButton.setEnabled(false);
		rollbackSnapshotButton.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				widgetSelected(arg0);
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				rollbackSnapshotButton.setEnabled(false);
				int index = snapTable.getSelectionIndex();
				String snapshotName = sapList.get(index).getName();
				RollbackSnapshotAction action = new RollbackSnapshotAction(selection,snapshotName,tab);
				action.run();
			}
			
		});
//		
//		Combo button3 = new Combo(contentCom, SWT.READ_ONLY); 
//		button3.add("快照操作");
//		button3.select(0);
		
		deleteSnapshotButton = new Button(contentCom, SWT.NONE); 
		deleteSnapshotButton.setText("删除快照");
		deleteSnapshotButton.setEnabled(false);
		deleteSnapshotButton.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				widgetSelected(arg0);
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				deleteSnapshotButton.setEnabled(false);
				int index = snapTable.getSelectionIndex();
				String snapshotName = sapList.get(index).getName();
				DeleteSnapshotAction action = new DeleteSnapshotAction(selection,snapshotName,tab);
				action.run();
			}
			
		});
		
		
		
		new Label(contentCom,SWT.NONE);
		new Label(contentCom,SWT.NONE);
		
		
		
		//快照图表部分
		Composite snapComposite = new Composite(contentCom, SWT.BORDER); 
		{
			GridLayout layout = new GridLayout(1,true);
			snapComposite.setLayout(layout);
			
			GridData gd_canvas = new GridData(GridData.FILL_HORIZONTAL);
			gd_canvas.horizontalSpan=4;
			gd_canvas.heightHint = 300;
			snapComposite.setLayoutData(gd_canvas);
		}
		

		
		snapTable=new Table(snapComposite, SWT.BORDER | SWT.V_SCROLL
		        | SWT.H_SCROLL|SWT.FULL_SELECTION);
		snapTable.setHeaderVisible(true);
		snapTable.setLinesVisible(false);
		GridData griddata = new GridData(GridData.FILL_BOTH);
		//griddata.horizontalSpan=2;
		snapTable.setLayoutData(griddata);
		snapTable.addListener(SWT.MouseDown, new Listener() {  
			public void handleEvent(Event event) { 
				if(snapTable.getSelectionCount()==1
						&&objectVM.getRecord()!=null
						&&objectVM.getRecord().powerState.equals(Types.VmPowerState.HALTED))
				{
					rollbackSnapshotButton.setEnabled(true);
					deleteSnapshotButton.setEnabled(true);
				}else{
					rollbackSnapshotButton.setEnabled(false);
					deleteSnapshotButton.setEnabled(false);
				}	
			}
		});  
		
		tableViewer = new TableViewer(snapTable);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider());
		
		
		TableColumn  measure= new TableColumn(snapTable, SWT.CENTER|SWT.BOLD);
		measure.setText("类型");
		measure.setWidth(80);
		TableColumn collect = new TableColumn(snapTable, SWT.CENTER|SWT.BOLD);
		collect.setText("名称");
		collect.setWidth(200);
		TableColumn unit = new TableColumn(snapTable, SWT.CENTER|SWT.BOLD);
		unit.setText("创建于");
		unit.setWidth(150);
		TableColumn newest = new TableColumn(snapTable, SWT.CENTER|SWT.BOLD);
		newest.setText("标签");
		newest.setWidth(80);
		TableColumn highest = new TableColumn(snapTable, SWT.CENTER|SWT.BOLD);
		highest.setText("描述");
		highest.setWidth(100);
		TableColumn operation3 = new TableColumn(snapTable, SWT.CENTER|SWT.BOLD);
		operation3.setText("");
		operation3.setWidth(100);

		tableViewer.getTable().select(0);
		refreshTable();
		//详细部分
		Group detailGroup = new Group(contentCom, SWT.NONE);
		{
			GridLayout layout = new GridLayout(1,true);
			detailGroup.setLayout(layout);
			
			GridData gd_canvas = new GridData();
			gd_canvas.widthHint = 250;
			gd_canvas.verticalAlignment = SWT.TOP;
			detailGroup.setLayoutData(gd_canvas);
		}
		
		detailGroup.setText("创建于");
		
		Label picture = new Label(detailGroup,SWT.NONE);
		{
			GridData g = new GridData(GridData.FILL_HORIZONTAL);
			g.horizontalAlignment=SWT.CENTER;
			g.verticalAlignment=SWT.CENTER;
			picture.setLayoutData(g);
		}
		
		picture.setImage(ImageRegistry.getImage(ImageRegistry.EMPTY));
		
		Composite infoComp = new Composite(detailGroup,SWT.NONE);
		{
			GridLayout layout = new GridLayout(2,true);
			layout.makeColumnsEqualWidth = false;
			infoComp.setLayout(layout);
			GridData g = new GridData(GridData.FILL_BOTH);
			g.horizontalAlignment=SWT.BEGINNING;
			g.verticalAlignment=SWT.CENTER;
			g.heightHint = 150;
			infoComp.setLayoutData(g);
		}
		nameLabel = new Label(infoComp,SWT.NONE);
		nameLabel.setText("名称:");
		valueLabel = new Label(infoComp,SWT.NONE);
		valueLabel.setText("                                            ");
		
		new Label(infoComp,SWT.NONE).setText("描述：");
		new Label(infoComp,SWT.NONE).setText("<None>");
		
		new Label(infoComp,SWT.NONE).setText("类型：");
		new Label(infoComp,SWT.NONE).setText("Disk only");
		
		new Label(infoComp,SWT.NONE).setText("标签：");
		new Label(infoComp,SWT.NONE).setText("<None>");
		
		snapTable.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				widgetSelected(arg0);
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				int index = snapTable.getSelectionIndex();
				System.out.println("snapshot index: " + index);
				if(index > -1)
					valueLabel.setText(sapList.get(index).name);
			}
			
		});
//		snapshotTimer = new Timer("SnapshotTimer");
//		snapshotTimer.schedule(new SnapshotTimer(this,PlatformUI.getWorkbench().getDisplay()), 0, 5000);
		
		composite.layout();
	}
	
	
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider,ITableColorProvider,ITableFontProvider
	{

		@Override
		public Font getFont(Object element, int columnIndex) {
			
			return null;
		}

		@Override
		public Color getForeground(Object element, int columnIndex) {
			
			return null;
		}

		@Override
		public Color getBackground(Object element, int columnIndex) {
			
			return null;
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			
			if(element instanceof Snapshot)
			{
				final Snapshot snapshot = (Snapshot)element;
				switch(columnIndex)
				{
				case 0:
					return snapshot.type;
				case 1:
					return snapshot.name;
				case 2:
					return snapshot.time.toString();
				case 3:
					return snapshot.label;
				case 4:
					return snapshot.description;
				}
			}
			return null;
		}
		
	}
	
	class Snapshot
	{
		private String type;
		private String name;
		private String time;
		private String label;
		private String description;
		
		public String getType() {
			return type;
		}
		public void setType(String type) {
			type = type;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getTime() {
			return time;
		}
		public void setTime(Date time) {
			this.time = time.toString();
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		
		
	}
	class SnapshotTimer extends TimerTask
	{
		private volatile VMSnapShotsTab snapTab;
		Display display;
		public SnapshotTimer(VMSnapShotsTab snapTab, Display display)
		{
			this.snapTab = snapTab;
			this.display = display;
		}
		@Override
		public void run() {
			
			if(!this.display.isDisposed())
			{
				Runnable runnable = new Runnable(){
					public void run()
					{
						snapTab.refreshTable();
						snapTab.refreshStrategy();
					}
				};
				this.display.syncExec(runnable);
			}
		}
		
	}
	
	public void refreshTable()
	{
		try{
			sapList.clear();
			VM vm = (VM)this.objectVM.getApiObject();
			Connection connection = this.objectVM.getConnection();
			Set<String> snapshots = vm.getSnapshots(connection);
			for(String a : snapshots)
			{
				String [] names = a.split("@");
				String name = names[names.length-1];
				Snapshot snapshot = new Snapshot();
				snapshot.type = "Disk only";
				snapshot.name = name;
				snapshot.time = getSnapshotTime(name);
				snapshot.label = "";
				snapshot.description = "";
				sapList.add(snapshot);
			}
			Collections.reverse(sapList);
			tableViewer.setInput(sapList);
			GridData griddata = new GridData(GridData.FILL_BOTH);
			//griddata.horizontalSpan=2;
			snapTable.setLayoutData(griddata);
//			int index = snapTable.getSelectionIndex();
//			System.out.println("dhf refresh index:" + index);
//			if(index > -1)
//				valueLabel.setText(sapList.get(index).name);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void refreshStrategy()
	{
		currentReverseNumberLabel.setText(strategy.getReverseNumber()+"个");
		currentReverseNumberLabel.pack();
		currentPeriodLabel.setText(strategy.getPeriod()+"天");
		currentPeriodLabel.pack();
	}
	
	private String getSnapshotTime(String a)
	{
		String[] sapName = a.split("_");
		String[] sapTime;
		if(sapName.length < 2)
			sapTime = sapName[0].split("T", 2);
		else
			sapTime = sapName[1].split("T", 2);
		String date = sapTime[0].substring(0,4) + "-" +  sapTime[0].substring(4, 6)+"-" + sapTime[0].substring(6);
		return date + " " + sapTime[1];
	}
	private void createPeriodicalContent(Composite parent)
	{
		Composite contentCom = new Composite(parent,SWT.NULL);
		contentCom.setLayout(new GridLayout(2,false));
		GridData gridData = new GridData();
		
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.END;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;

		contentCom.setLayoutData(gridData);
		
		//title info
		Label titleLabel = new Label(contentCom,SWT.NULL);
		titleLabel.setText("定期快照策略");
		titleLabel.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
		titleLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		new Label(contentCom,SWT.NULL);
		
		//配置任务面板
		Group configGroup = new Group(contentCom, SWT.NONE);
		{
			GridLayout layout = new GridLayout(3,false);
			configGroup.setLayout(layout);
			configGroup.setLayoutData(gridData);
			
			Composite imgSite = new Composite(configGroup,SWT.NONE);
			GridData isData = new GridData();
			isData.widthHint = 150;
			imgSite.setLayoutData(isData);
			imgSite.setLayout(new GridLayout(1,false));
			CLabel imgCLabel = new CLabel(imgSite,SWT.NULL);
			imgCLabel.setImage(ImageRegistry.getImage(ImageRegistry.SNAPSHOT));
			Label infoLabel = new Label(imgSite,SWT.NULL);
			infoLabel.setText("配置定期快照策略");
			GridData ilData = new GridData();
			ilData.horizontalAlignment = SWT.CENTER;
			infoLabel.setLayoutData(ilData);
			
			
			Composite configCom = new Composite(configGroup,SWT.NONE);
			configCom.setLayout(new GridLayout(4,false));

			
			vmNameLabel = new Label(configCom, SWT.NONE);
			vmNameLabel.setText("虚拟机名称");
			vmNameLabel.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
			vmNameText = new Text(configCom, SWT.NONE);
			vmNameText.setText(objectVM.getName());
			vmNameText.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
			new Label(configCom,SWT.NONE);
			new Label(configCom,SWT.NONE);
			
			lifeTimeLabel = new Label(configCom,SWT.NULL);
			lifeTimeLabel.setText("快照保留个数");
			lifeTimeText = new Text(configCom, SWT.BORDER);
			lifeTimeText.setText("10");
			GridData layoutData = new GridData();
			layoutData.grabExcessHorizontalSpace = true;
			layoutData.horizontalAlignment = GridData.FILL;
			lifeTimeText.setLayoutData(layoutData);
			new Label(configCom,SWT.NONE).setText("个");
			CLabel lifeTimeCLabel = new CLabel(configCom,SWT.NULL);
			lifeTimeCLabel.setToolTipText("快照保留个数，默认保留最近10个");
			lifeTimeCLabel.setImage(ImageRegistry.getImage(ImageRegistry.SNAPINFO));

			
			
			cycleLabel = new Label(configCom,SWT.NULL);
			cycleLabel.setText("生成快照频率");
			cycleCombo = new Combo(configCom, SWT.READ_ONLY);
//			String[] itemsCycle = {"15minutes","30minutes","1hours","2hours","3hours","4hours","6hours","12hours","1day","1week"};
			String[] itemsCycle = {"1小时","3小时","9小时","15小时","1天","7天","15天","30天"};
			cycleCombo.setItems(itemsCycle);
			cycleCombo.select(0);
			cycleCombo.setLayoutData(layoutData);
			cycleCLabel = new CLabel(configCom,SWT.NULL);
			cycleCLabel.setToolTipText("两次快照尝试的时间间隔，默认为1天");
			cycleCLabel.setImage(ImageRegistry.getImage(ImageRegistry.SNAPINFO));


			
			Composite sureCom = new Composite(configGroup,SWT.NONE);
			sureCom.setLayout(new GridLayout(1,true));
			GridData scData = new GridData();
			scData.horizontalAlignment = SWT.LEFT;
			scData.verticalAlignment = SWT.BOTTOM;
			scData.widthHint = 200;
			sureCom.setLayoutData(scData);
			GridData btData = new GridData();
			btData.verticalAlignment = SWT.BOTTOM;
			
			editButton = new Button(sureCom,SWT.PUSH);
			editButton.setLayoutData(btData);
			editButton.setText("配置策略");
			
			editButton.addSelectionListener(new SelectionListener(){

				@Override
				public void widgetSelected(SelectionEvent e) {
					String period = "";
					if(cycleCombo.getText().contains("天")){
						period = cycleCombo.getText().split("天")[0] + "d";
					}else if(cycleCombo.getText().contains("小时")){
						period = cycleCombo.getText().split("小时")[0] + "h";
					}
					strategy.setPeriod(period);
					strategy.setReverseNumber(Integer.parseInt(lifeTimeText.getText()));
					SetSnapshotStrategy action = new SetSnapshotStrategy(objectVM,strategy,tab);
					action.run();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
				
			});
		}
		configGroup.setText("配置快照策略");
		
		//详细部分
		Group detailGroup = new Group(contentCom, SWT.NONE);
		{
			GridLayout layout = new GridLayout(1,true);
			detailGroup.setLayout(layout);
			
			GridData gd_canvas = new GridData();
			gd_canvas.widthHint = 250;
			gd_canvas.verticalAlignment = SWT.TOP;
			detailGroup.setLayoutData(gd_canvas);
		}
		
		detailGroup.setText("当前快照策略");
		
		Label picture = new Label(detailGroup,SWT.NONE);
		{
			GridData g = new GridData(GridData.FILL_HORIZONTAL);
			g.horizontalAlignment=SWT.CENTER;
			g.verticalAlignment=SWT.CENTER;
			picture.setLayoutData(g);
		}
		
		picture.setImage(ImageRegistry.getImage(ImageRegistry.PERIOD));
		
		Composite infoComp = new Composite(detailGroup,SWT.NONE);
		{
			GridLayout layout = new GridLayout(2,true);
			layout.makeColumnsEqualWidth = false;
			infoComp.setLayout(layout);
			GridData g = new GridData(GridData.FILL_BOTH);
			g.horizontalAlignment=SWT.BEGINNING;
			g.verticalAlignment=SWT.CENTER;
			infoComp.setLayoutData(g);
		}
		nameLabel = new Label(infoComp,SWT.NONE);
		nameLabel.setText("虚拟机名称:");
		valueLabel = new Label(infoComp,SWT.NONE);
		valueLabel.setText(objectVM.getName());
		
		new Label(infoComp,SWT.NONE).setText("快照保留个数：");
		currentReverseNumberLabel = new Label(infoComp,SWT.NONE);
		currentReverseNumberLabel.setText(strategy.getReverseNumber() + "个");
		new Label(infoComp,SWT.NONE).setText("生成快照频率：");
		currentPeriodLabel = new Label(infoComp,SWT.NONE);
		currentPeriodLabel.setText(strategy.getPeriod() + "天");
	}
	
}
