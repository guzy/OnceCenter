package oncecenter.maintabs.host;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import oncecenter.action.NewPoolAction;
import oncecenter.action.NewVMAction;
import oncecenter.maintabs.OnceHostTabItem;
import oncecenter.util.ImageRegistry;
import oncecenter.util.MathUtil;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wb.swt.SWTResourceManager;

import com.once.xenapi.Host;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.XenAPIException;

public class SummaryTab extends OnceHostTabItem {
	private Label name;
	private Text nameValue;
	private Label ip;
	private Label ipValue;
	private Label uuid;
	private Text uuidValue;
	private Label description;
	private Label descriptionValue;
	private Label state;
	private Label stateValue;
	private Label vmCount;
	private Label cpu;
	private Label cpuInfo;
	private Label version;
	private Label kernel;
	
	private Label cpuUsage;
	private Label cpuTotal;
	private Label memoryUsage;
	private Label memoryTotal;
	
	//private Table Storage;
	private Table Network;
	private TableViewer netViewer;
	
	private Label grade;
	private CLabel gradeImage;
	private Text gradeValue;
	private Canvas gradeCanvas;
	
	
	double memoryFree = 0;
	double gradeMark;
	
	private Composite generalComposite;
	private Composite rescourceComposite;
	private Composite commandComposite;
	private Composite gradeComposite;
	
	private String cpuTotalValue="";
	private String cpuUsageValue="";
	private double cpuUsagePercent=0;

	private String memoryTotalValue="";
	private String memoryUsageValue="";
	private double memoryUsagePercent=0;
	
	private Canvas cpuCanvas;
	private Canvas memoryCanvas;
	
	public Timer generalTimer;
	public Timer performTimer;
	public Timer gradeTimer;
	
	CLabel newVmCLabel;
	CLabel newPoolCLabel;
	private ArrayList<String> netNames = new ArrayList<String>();
	private ArrayList<String> netWs = new ArrayList<String>();
	private ArrayList<String> netRs = new ArrayList<String>();
	
	private ArrayList<Network> networkList = new  ArrayList<Network>();
	

	
	public SummaryTab(CTabFolder arg0, int arg1, int arg2, VMTreeObjectHost object) {
		super(arg0, arg1, arg2, object);
		setText("常规");
		this.objectHost = (VMTreeObjectHost)object;
		Init();
	}


	public boolean Init(){
		composite = new Composite(folder, SWT.FILL); 
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
			lblNewLabel.setText("  常规                                                                 ");
			lblNewLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		{
			Composite composite_1 = new Composite(generalComposite, SWT.BORDER);
			composite_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			composite_1.setBounds(10, 30, 300, 200);
			composite_1.setLayout(new GridLayout(2,false));
			composite_1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			composite_1.setBackgroundMode(SWT.INHERIT_DEFAULT);  

			uuid=new Label(composite_1, SWT.NONE);
			uuid.setText("  UUID:   ");
			uuidValue=new Text(composite_1, SWT.NONE);
			//uuidValue.setEditable(false);
			
			name=new Label(composite_1, SWT.NONE);
			name.setText("  名称：  ");
			nameValue=new Text(composite_1,SWT.NONE);
			nameValue.setText(objectHost.getName());
			
			description=new Label(composite_1, SWT.NONE);
			description.setText("  描述：  ");			
			descriptionValue=new Label(composite_1,SWT.NONE);
			
			//由于后台不支持，暂不显示
//			ip=new Label(composite_1, SWT.NONE);
//			ip.setText("  IP地址：  ");
//			ipValue=new Label(composite_1, SWT.NONE);
			//ipValue.setText(objectHost.getIpAddress());
			
			state=new Label(composite_1, SWT.NONE);
			state.setText("  状态：  ");			
			stateValue=new Label(composite_1,SWT.NONE);
			stateValue.setText("");
			
			new Label(composite_1,SWT.NONE).setText("  虚拟机和模板：");
			vmCount = new Label(composite_1,SWT.NONE);
			
			new Label(composite_1,SWT.NONE).setText("  CPU 内核：");
			cpu = new Label(composite_1,SWT.NONE);
			//由于后台不支持，暂时屏蔽显示
//			new Label(composite_1,SWT.NONE).setText("  处理器类型：");
//			cpuInfo = new Label(composite_1,SWT.NONE);
			
			new Label(composite_1,SWT.NONE).setText("  版本：");
			version = new Label(composite_1,SWT.NONE);
			
			new Label(composite_1,SWT.NONE).setText("  内核版本：");
			kernel = new Label(composite_1,SWT.NONE);
		}
		
			
		rescourceComposite = new Composite(composite, SWT.NONE); 
		rescourceComposite.setBackground(new Color(null,255,255,255));
		rescourceComposite.setLayout(layout);
		{
			GridData griddata = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
			griddata.verticalSpan=1;
			griddata.widthHint = 400;
			griddata.heightHint = 300;
			rescourceComposite.setLayoutData(griddata);
		}
		{
			Label lblNewLabel = new Label(rescourceComposite, SWT.NONE);
			lblNewLabel.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
			lblNewLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
			lblNewLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
			lblNewLabel.setBounds(10, 10, 300, 20);
			lblNewLabel.setText("  资源                                                  ");
			lblNewLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		{
			Composite composite_1 = new Composite(rescourceComposite, SWT.BORDER);
			composite_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			composite_1.setBounds(10, 30, 300, 200);
			composite_1.setLayout(new GridLayout(3,false));
			composite_1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			composite_1.setBackgroundMode(SWT.INHERIT_DEFAULT);  
			
			{
				new Label(composite_1,SWT.NONE).setText("CPU 资源使用情况： ");
				cpuUsage =  new Label(composite_1,SWT.BOLD);
				cpuUsage.setText(cpuUsageValue);
				cpuUsage.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
				new Label(composite_1,SWT.NONE).setText("");

				cpuCanvas = new Canvas(composite_1,SWT.NONE);
				GridData gd_canvas = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
				gd_canvas.horizontalSpan=2;
				gd_canvas.widthHint = 300;
				gd_canvas.heightHint = 10;
				cpuCanvas.setLayoutData(gd_canvas);
				cpuCanvas.setRedraw(true);
				cpuCanvas.addPaintListener(new PaintListener() {
					public void paintControl(PaintEvent e) {
						e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_GRAY));
						for(int i=0;i<300;i++){
							e.gc.drawLine(i*2, 0, i*2, 10);
						}
						e.gc.setBackground(e.display.getSystemColor(SWT.COLOR_BLUE));
						e.gc.setAlpha(50);
						e.gc.fillRectangle(0, 0, (int)(3*cpuUsagePercent), 10);
					}
				});
				
				cpuTotal = new Label(composite_1, SWT.NONE);
				cpuTotal.setText(cpuTotalValue);
			}
			
			{
				new Label(composite_1,SWT.NONE).setText("内存资源使用情况： ");
				memoryUsage =  new Label(composite_1,SWT.BOLD);
				memoryUsage.setText(memoryUsageValue);
				memoryUsage.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
				new Label(composite_1,SWT.NONE).setText("容量");

				memoryCanvas = new Canvas(composite_1,SWT.NONE);
				GridData gd_canvas = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
				gd_canvas.horizontalSpan=2;
				gd_canvas.widthHint = 300;
				gd_canvas.heightHint = 10;
				memoryCanvas.setLayoutData(gd_canvas);
				memoryCanvas.setRedraw(true);
				memoryCanvas.addPaintListener(new PaintListener() {
					public void paintControl(PaintEvent e) {
						e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_GRAY));
						for(int i=0;i<300;i++){
							e.gc.drawLine(i*2, 0, i*2, 10);
						}
						e.gc.setBackground(e.display.getSystemColor(SWT.COLOR_BLUE));
						e.gc.setAlpha(50);
						e.gc.fillRectangle(0, 0, (int)(3*memoryUsagePercent), 10);
					}
				});
				
				memoryTotal = new Label(composite_1, SWT.NONE);
				memoryTotal.setText(memoryTotalValue);
			}
			{
//				Storage=new Table(composite_1, SWT.BORDER | SWT.V_SCROLL
//				        | SWT.H_SCROLL);
//				GridData gd_canvas = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
//				gd_canvas.horizontalSpan=3;
//				gd_canvas.widthHint=360;
//				Storage.setLayoutData(gd_canvas);
//				
//				Storage.setHeaderVisible(true);
//				Storage.setLinesVisible(true);
//				TableColumn store = new TableColumn(Storage, SWT.CENTER|SWT.BOLD);
//				store.setText("存储器");
//				store.setWidth(200);
//				TableColumn capacity = new TableColumn(Storage, SWT.CENTER|SWT.BOLD);
//				capacity.setText("容量");
//				capacity.setWidth(80);
//				TableColumn free = new TableColumn(Storage, SWT.CENTER|SWT.BOLD);
//				free.setText("可用空间");
//				free.setWidth(80);
				
//				TableItem item=new TableItem(Storage,SWT.NONE);
//				item.setText(0, "storage1");
//				item.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
//						ImageRegistry.getImagePath(ImageRegistry.STORAGE)).createImage());
//				item.setText(1, "");
//				item.setText(2, "");
//				for(int i=0;i<3;i++){
//					Storage.getColumn(i).pack();
//				}
			}
			
			{
				Network=new Table(composite_1, SWT.BORDER | SWT.V_SCROLL
				        | SWT.H_SCROLL);
				GridData gd_canvas = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
				gd_canvas.horizontalSpan=3;
				gd_canvas.widthHint=360;
				gd_canvas.heightHint = 50;
				Network.setLayoutData(gd_canvas);
				
				netViewer = new TableViewer(Network);
				netViewer.setContentProvider(new ArrayContentProvider());
				netViewer.setLabelProvider(new TableLabelProvider());
				
				Network.setHeaderVisible(true);
				Network.setLinesVisible(true);
				
				TableColumn network = new TableColumn(Network, SWT.CENTER|SWT.BOLD);
				network.setText("网络");
				network.setWidth(80);
				TableColumn wnet = new TableColumn(Network, SWT.CENTER|SWT.BOLD);
				wnet.setText("网络发送量");
				wnet.setWidth(80);
				TableColumn rnet = new TableColumn(Network, SWT.CENTER|SWT.BOLD);
				rnet.setText("网络接收量");
				rnet.setWidth(80);
				Network.pack();
			}
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
				newVmCLabel = new CLabel(composite_1, SWT.MULTI);
				newVmCLabel.setForeground(SWTResourceManager.getColor(0, 0, 128));
				newVmCLabel.addMouseTrackListener(new MouseTrackAdapter() {
					@Override
					public void mouseEnter(MouseEvent arg0) {
						newVmCLabel.setBackground(SWTResourceManager.getColor(240, 248, 255));
						
					}
					@Override
					public void mouseExit(MouseEvent arg0) {
						newVmCLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						
					}
				});
				newVmCLabel.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseDown(MouseEvent arg0) {
						NewVMAction action=new NewVMAction(objectHost);
						action.run();
					}
				});
				newVmCLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				newVmCLabel.setImage(ImageRegistry.getImage(ImageRegistry.ADDVMDISABLE));
				newVmCLabel.setBounds(57, 33, 244, 87);
				newVmCLabel.setText("新建虚拟机");
				newVmCLabel.setEnabled(false);
			}
			
			{
				newPoolCLabel = new CLabel(composite_1, SWT.MULTI);
				newPoolCLabel.setForeground(SWTResourceManager.getColor(0, 0, 128));
				newPoolCLabel.addMouseTrackListener(new MouseTrackAdapter() {
					@Override
					public void mouseEnter(MouseEvent arg0) {
						newPoolCLabel.setBackground(SWTResourceManager.getColor(240, 248, 255));
						
					}
					@Override
					public void mouseExit(MouseEvent arg0) {
						newPoolCLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						
					}
				});
				newPoolCLabel.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseDown(MouseEvent arg0) {
						NewPoolAction action=new NewPoolAction();
						action.run();
					}
				});
				newPoolCLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				newPoolCLabel.setImage(ImageRegistry.getImage(ImageRegistry.ADDPOOLDISABLE));
				newPoolCLabel.setBounds(57, 33, 244, 87);
				newPoolCLabel.setText("新建资源池");
				newPoolCLabel.setEnabled(false);
			}
		}
		
		gradeComposite = new Composite(composite, SWT.NONE);
		gradeComposite.setBackground(new Color(null,255,255,255));
		gradeComposite.setLayout(layout);
		{
			GridData griddata = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
			griddata.widthHint = 400;
			griddata.heightHint = 300;
			gradeComposite.setLayoutData(griddata);
		}
		{
			Label lblNewLabel = new Label(gradeComposite, SWT.NONE);
			lblNewLabel.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
			lblNewLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
			lblNewLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
			lblNewLabel.setBounds(10, 10, 300, 20);
			lblNewLabel.setText("  评分                                                  ");
			lblNewLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		{
			Composite composite_1 = new Composite(gradeComposite, SWT.BORDER);
			composite_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			composite_1.setBounds(10, 30, 300, 200);
			composite_1.setLayout(new GridLayout(2,false));
			composite_1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			composite_1.setBackgroundMode(SWT.INHERIT_DEFAULT); 
			
			gradeImage = new CLabel(composite_1, SWT.NONE);
			gradeImage.setImage(ImageRegistry.getImage(ImageRegistry.GRADE));
			GridData gd_image = new GridData();
			gd_image.verticalSpan = 2;
			gradeImage.setLayoutData(gd_image);
			
			Composite composite_2 = new Composite(composite_1,SWT.NONE);
			composite_2.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			composite_2.setLayout(new GridLayout(2,false));
			composite_2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			composite_2.setBackgroundMode(SWT.INHERIT_DEFAULT); 
			
			grade = new Label(composite_2, SWT.NONE);
			grade.setText("主机评分: ");
			gradeValue = new Text(composite_2, SWT.NONE);
			gradeMark = objectHost.grade;
			gradeValue.setText(Double.toString(gradeMark));
			gradeValue.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
			gradeValue.setForeground( Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
			
			gradeCanvas = new Canvas(composite_2,SWT.NONE);
			GridData gd_canvas = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_canvas.horizontalSpan=2;
			gd_canvas.widthHint = 200;
			gd_canvas.heightHint = 10;
			gradeCanvas.setLayoutData(gd_canvas);
			gradeCanvas.setRedraw(true);
			gradeCanvas.addPaintListener(new PaintListener(){

				@Override
				public void paintControl(PaintEvent e) {
					
					e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_GRAY));
					for(int i=0;i<200;i++){
						e.gc.drawLine(i*2, 0, i*2, 10);
					}
					e.gc.setBackground(e.display.getSystemColor(SWT.COLOR_BLUE));
					e.gc.setAlpha(50);
					e.gc.fillRectangle(0, 0, (int)(2*gradeMark), 10);
				}
			});
			
		}
		
		generalTimer = new Timer("GeneralTimer");
		generalTimer.schedule(new GeneralTimer(this,PlatformUI.getWorkbench().getDisplay()), 0, 5000);
		performTimer = new Timer("PerformTimer");
		performTimer.schedule(new PerformTimer(this,PlatformUI.getWorkbench().getDisplay()), 1000, 15000);
		gradeTimer = new Timer("GradeTimer");
		gradeTimer.schedule(new GradeTimer(this,PlatformUI.getWorkbench().getDisplay()), 0, 5000);
		//InitThread t=new InitThread(this,PlatformUI.getWorkbench().getDisplay());
		//t.run();
		if(objectHost.timerList == null)
			objectHost.timerList = new ArrayList<Timer>();
		objectHost.timerList.add(generalTimer);
		objectHost.timerList.add(performTimer);
		objectHost.timerList.add(gradeTimer);
		composite.layout();
		return true;
	}
	
	class GeneralTimer extends TimerTask {
		private volatile SummaryTab summary;
		Display display;
		public GeneralTimer(SummaryTab summary,Display display) {
			this.summary = summary;
			this.display=display;
		}
		public void run() {
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	summary.refreshGeneral();
					}
			    };
			    this.display.asyncExec(runnable); 
			}
		}
	}
	
	class PerformTimer extends TimerTask {
		private volatile SummaryTab summary;
		Display display;
		public PerformTimer(SummaryTab summary,Display display) {
			this.summary = summary;
			this.display=display;
		}
		public void run() {
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	summary.getPerform();
			        	summary.refreshPerform();
					}
			    };
			    this.display.asyncExec(runnable); 
			}
		}
	}
	
	class GradeTimer extends TimerTask
	{
		private volatile SummaryTab summary;
		Display display;
		
		public GradeTimer(SummaryTab summary, Display display)
		{
			this.summary = summary;
			this.display = display;
		}

		@Override
		public void run() {
			
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	summary.refreshGrade();
					}
			    };
			    this.display.asyncExec(runnable); 
			}
		}
		
		
	}
	public void refreshGeneral(){
				if(objectHost.getItemState().equals(ItemState.able)){
					Host.Record record = objectHost.getRecord();
					Host host = (Host)objectHost.getApiObject();
					if(record!=null){
						objectHost.setRecord(record);
						nameValue.setText(record.nameLabel);
						uuidValue.setText(record.uuid);
//						descriptionValue.setText(record.nameDescription);
						try {
							descriptionValue.setText(host.getNameDescription(objectHost.getConnection()));
						} catch (BadServerResponse e) {
							
							e.printStackTrace();
						} catch (XenAPIException e) {
							
							e.printStackTrace();
						} catch (XmlRpcException e) {
							
							e.printStackTrace();
						}
						stateValue.setText("已连接");
						vmCount.setText((record.residentVMs.size() - 1) +"");
						//ip地址获取不到
//						ipValue.setText(record.address);
						cpu.setText(record.hostCPUs.size()+"个");
//						cpuInfo.setText("");
						version.setText("Xen "+record.softwareVersion.get("Xen"));
						kernel.setText(record.softwareVersion.get("system")+" "+record.softwareVersion.get("release"));
						
						newVmCLabel.setImage(ImageRegistry.getImage(ImageRegistry.ADDVM));
						newVmCLabel.setEnabled(true);
						
						newPoolCLabel.setImage(ImageRegistry.getImage(ImageRegistry.ADDPOOL));
						newPoolCLabel.setEnabled(true);

						//double memoryTotal = record.metrics.getMemoryTotal(objectHost.getConnection());
						double memoryTotal = objectHost.getMemoryTotalValue();
						//double memoryFree = record.metrics.getMemoryFree(object.getConnection());
						double memoryUsage = objectHost.getMemoryUsageValue();
						memoryTotalValue=MathUtil.Rounding(memoryTotal/1024.0/1024.0, 2)+"MB";
						memoryUsageValue=MathUtil.Rounding(memoryUsage/1024.0/1024.0, 2)+"MB";
						memoryUsagePercent=memoryUsage/memoryTotal*100.0;
						
					}
				}else{
					uuidValue.setText(objectHost.getUuid());
					stateValue.setText("已断开");
				}
			{
				uuidValue.pack();
				descriptionValue.pack();
				stateValue.pack();
//				ipValue.pack();
				vmCount.pack();
				cpu.pack();
//				cpuInfo.pack();
				version.pack();
				kernel.pack();
			}
		//}
	}
	
	public void getPerform(){
		cpuUsagePercent=objectHost.getCpuUsagePercent();
		memoryUsagePercent=objectHost.getMemoryUsagePercent();
		memoryTotalValue=MathUtil.Rounding(objectHost.getMemoryTotalValue(), 3)+"MB";
		memoryUsageValue=MathUtil.Rounding(objectHost.getMemoryUsageValue(), 3)+"MB";
		HashMap<String,Double> Wnets = objectHost.getwNetList();
		HashMap<String,Double> Rnets = objectHost.getrNetList();
		netNames.clear();
		netWs.clear();
		netRs.clear();
		if(Wnets!=null&&Rnets!=null){
			for(String key:Wnets.keySet()){
				netNames.add(key);
				netWs.add(MathUtil.Rounding(Wnets.get(key), 3)+" KB");
				if(Rnets.get(key)==null){
					netRs.add("0.0 KB");
				}else{
					netRs.add(MathUtil.Rounding(Rnets.get(key), 3)+" KB");
				}
			}
			for(String key:Rnets.keySet()){
				if(netNames.contains(key)){
					continue;
				}else{
					netNames.add(key);
					netWs.add("0.0 KB");
					netRs.add(MathUtil.Rounding(Rnets.get(key), 3)+" KB");
				}
			}
		}
		networkList.clear();
		for(int i = 0; i < netNames.size(); ++i)
		{
			Network network = new  Network();
			network.setNetName(netNames.get(i));
			network.setNetSend(netWs.get(i));
			network.setNetAccept(netRs.get(i));
			networkList.add(network);
		}
	}
	public void refreshPerform(){
		if(objectHost.getItemState().equals(ItemState.able)){
			
			try{
				cpuUsage.setText(MathUtil.Rounding(cpuUsagePercent, 3)+"%");
				memoryUsage.setText(memoryUsageValue);
				memoryTotal.setText(memoryTotalValue);
				
				if(networkList.size()>0){
					netViewer.setInput(networkList);
				}
				
				cpuCanvas.redraw();
				memoryCanvas.redraw();
    		}catch(Exception e){
    			e.printStackTrace();
    		}
		}else{
				cpuUsage.setText("");
				cpuTotal.setText("");
				memoryUsage.setText("");
				memoryTotal.setText("");
				cpuUsagePercent=0;
				memoryUsagePercent=0;
				cpuCanvas.redraw();
				memoryCanvas.redraw();
		}
		
		{
			cpuUsage.pack();
			cpuTotal.pack();
			memoryUsage.pack();
			memoryTotal.pack();
			//netViewer.refresh();
		}
	}
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider
	{

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			
			if(element instanceof Network)
			{
				switch(columnIndex)
				{
				case 0:
					return  ImageRegistry.getImage(ImageRegistry.NETWORK);
				}
			}
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			
		
			if(element instanceof Network)
			{
				Network network = (Network)element;
				switch(columnIndex)
				{
				case 0:
					//System.out.println("test:" + network.getNetName());
					return network.getNetName();
				case 1:
					return network.getNetSend();
				case 2:
					return network.getNetAccept();
				}
			}
			return null;
		}}
	
	class Network
	{
		private String netName;
		private String netSend;
		private String netAccept;
		
		public Network()
		{}
		
		public Network(String netName, String netSend, String netAccept)
		{
			this.netName = netName;
			this.netSend = netSend;
			this.netAccept = netAccept;
		}
		
		public String getNetName() {
			return netName;
		}
		public void setNetName(String netName) {
			this.netName = netName;
		}
		public String getNetSend() {
			return netSend;
		}
		public void setNetSend(String netSend) {
			this.netSend = netSend;
		}
		public String getNetAccept() {
			return netAccept;
		}
		public void setNetAccept(String netAccept) {
			this.netAccept = netAccept;
		}
	}
	public void refreshGrade()
	{
		if(objectHost.getItemState().equals(ItemState.able))
		{
			if(objectHost.grade == 0){
				objectHost.getGrade();
			}
			gradeMark = objectHost.grade;
			gradeValue.setText((int)gradeMark+"");
			gradeCanvas.redraw();
		}
		else
		{
			gradeValue.setText("");
			gradeValue.redraw();
		}
		{
			gradeValue.pack();
		}
	}
	public void refresh(){
		refreshGeneral();
		refreshPerform();
	}

	public ArrayList<String> getNetNames() {
		return netNames;
	}

	public void setNetNames(ArrayList<String> netNames) {
		this.netNames = netNames;
	}

	public ArrayList<String> getNetWs() {
		return netWs;
	}

	public void setNetWs(ArrayList<String> netWs) {
		this.netWs = netWs;
	}

	public ArrayList<String> getNetRs() {
		return netRs;
	}

	public void setNetRs(ArrayList<String> netRs) {
		this.netRs = netRs;
	}
}
