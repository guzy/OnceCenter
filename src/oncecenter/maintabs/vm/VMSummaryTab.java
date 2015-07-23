package oncecenter.maintabs.vm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import oncecenter.Constants;
import oncecenter.action.vm.ChangetoTemplateAction;
import oncecenter.action.vm.PerformAlarmAction;
import oncecenter.action.vm.RebootAction;
import oncecenter.action.vm.ShutDownAction;
import oncecenter.action.vm.StartAction;
import oncecenter.maintabs.OnceTabItem;
import oncecenter.maintabs.OnceVMTabItem;
import oncecenter.util.ImageRegistry;
import oncecenter.util.MathUtil;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM.Disk;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM.ResourceTypes;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wb.swt.SWTResourceManager;

import com.once.xenapi.Types;
import com.once.xenapi.VM;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.XenAPIException;

public class VMSummaryTab extends OnceVMTabItem {
	
	VM vm;
	private Label name;
	private Text nameValue;
	private Label ip;
	private Combo ipValue;
	private Label uuid;
	private Text uuidValue;
	private Label description;
	private Label descriptionValue;
	private Label state;
	private Label stateValue;
	private Label os;
	private Label osValue;
	private Link host;
	private Label cpu;
	private Label memory;
	
	private Label type;
	private Label typeValue;
	private Label status;
	private CLabel statusValue;
	private Label configuration;
	private Label problem;
	private Label problemValue;
	private Label solution;
	private Link solutionValue;
	

	private Text minValue;
	private Text maxValue;
	private Button configurationButton;
	private Label cpuUsage;
	private Label cpuTotal;
	private Label memoryUsage;
	private Label memoryTotal;
	
	private Table Storage;
	private Table Network;
	private TableViewer tableViewer;
	
	private Composite generalComposite;
	private Composite rescourceComposite;
	private Composite commandComposite;
	private Composite stateComposite;
	
	private String cpuTotalValue="";
	private String cpuUsageValue="";
	private double cpuUsagePercent=0;
	
//	private ArrayList<Double> wnets = new ArrayList<Double>();
//	private ArrayList<Double> rnets = new ArrayList<Double>();
	
	private String memoryTotalValue="";
	private String memoryUsageValue="";
	private double memoryUsagePercent=0;
	
	private double diskTotalValue=0;
	private double diskUsageValue=0;
	private double diskUsagePercent=0;
	
	private Canvas cpuCanvas;
	private Canvas memoryCanvas;
	
	private ArrayList<String> netNames = new ArrayList<String>();
	private ArrayList<String> netWs = new ArrayList<String>();
	private ArrayList<String> netRs = new ArrayList<String>();

	private ArrayList<Disk> diskList = new ArrayList<Disk>();
	
	
	private VM.Record record;
	
	public Timer generalTimer;
	public Timer performTimer;
	public Timer stateTimer;
	
	CLabel startCLabel;
	CLabel rebootCLabel;
	CLabel shutDownCLabel;
	CLabel templateCLabel;
	CLabel openConsoleCLabel;
	
	Types.VmPowerState vmState;
	
	VMTreeObjectHost hostObject;
	
	public VMSummaryTab(CTabFolder arg0, int arg1, VMTreeObjectVM object) {
		super(arg0, arg1,object);
		setText("常规");
		this.objectVM = (VMTreeObjectVM)object;
		this.vm = (VM)object.getApiObject();
		Init();
	}

	public VMSummaryTab(CTabFolder arg0, int arg1, int arg2, VMTreeObjectVM object) {
		super(arg0, arg1, arg2,object);
		setText("常规");
		this.objectVM = (VMTreeObjectVM)object;
		this.vm = (VM)object.getApiObject();
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
				nameValue.setText(objectVM.getName());
				
				description=new Label(composite_1, SWT.NONE);
				description.setText("  描述：      ");			
				descriptionValue=new Label(composite_1,SWT.NONE);
				
				uuid=new Label(composite_1, SWT.NONE);
				uuid.setText("  UUID:       ");
				uuidValue=new Text(composite_1, SWT.NONE);
				
				new Label(composite_1,SWT.NONE).setText("  主机：  ");
				host = new Link(composite_1,SWT.NONE);
				
				state=new Label(composite_1, SWT.NONE);
				state.setText("  状态：      ");			
				stateValue=new Label(composite_1,SWT.NONE);
				//由于后台不支持，暂时屏蔽显示		
//				os=new Label(composite_1, SWT.NONE);
//				os.setText("  操作系统：      ");			
//				osValue=new Label(composite_1,SWT.NONE);
//				osValue.setText("");
				
				new Label(composite_1,SWT.NONE).setText("  CPU：  ");
				cpu = new Label(composite_1,SWT.NONE);
				
				new Label(composite_1,SWT.NONE).setText("  内存：  ");
				memory = new Label(composite_1,SWT.NONE);
				
				//由于后台不支持，暂时屏蔽显示	
//				ip=new Label(composite_1, SWT.NONE);
//				ip.setText("  IP地址：  ");
//				ipValue = new Combo(composite_1, SWT.BORDER|SWT.DROP_DOWN | SWT.READ_ONLY);
				
				
//			}else{
//				name=new Label(composite_1, SWT.NONE);
//				name.setText("  名称：      ");
//				nameValue=new Label(composite_1,SWT.NONE);
//				nameValue.setText(object.getName());
//				
//				state=new Label(composite_1, SWT.NONE);
//				state.setText("  状态：      ");			
//				stateValue=new Label(composite_1,SWT.NONE);
//				stateValue.setText("未连接");
//				
//				uuid=new Label(composite_1, SWT.NONE);
//				uuid.setText("  UUID:       ");
//				uuidValue=new Label(composite_1, SWT.NONE);
//				uuidValue.setText("                              ");
//
//				description=new Label(composite_1, SWT.NONE);
//				description.setText("  描述：      ");			
//				descriptionValue=new Label(composite_1,SWT.NONE);
//				descriptionValue.setText("                              ");
//				
//				os=new Label(composite_1, SWT.NONE);
//				os.setText("  操作系统：      ");			
//				osValue=new Label(composite_1,SWT.NONE);
//				osValue.setText("");
//				
//				new Label(composite_1,SWT.NONE).setText("  主机：  ");
//				host = new Label(composite_1,SWT.NONE);
//				host.setText(object.getParent().getName());
//			}
		}
		
		rescourceComposite = new Composite(composite, SWT.NONE); 
		rescourceComposite.setBackground(new Color(null,255,255,255));
		rescourceComposite.setLayout(layout);
		{
			GridData griddata = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
			//griddata.verticalSpan=2;
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
			lblNewLabel.setText("  资源                        ");
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
				gd_canvas.horizontalAlignment = GridData.FILL_HORIZONTAL;
				cpuCanvas.setLayoutData(gd_canvas);
				cpuCanvas.setRedraw(true);
				//cpuCanvas.setSize(100, 20);
				cpuCanvas.addPaintListener(new PaintListener() {
					public void paintControl(PaintEvent e) {
						//e.gc.drawImage(cpu_img, 0, 0);
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
				//cpuCanvas.setSize(100, 20);
				memoryCanvas.addPaintListener(new PaintListener() {
					public void paintControl(PaintEvent e) {
						e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_GRAY));
						for(int i=0;i<300;i++){
							e.gc.drawLine(i*2, 0, i*2, 10);
						}
						e.gc.setBackground(e.display.getSystemColor(SWT.COLOR_BLUE));
						e.gc.setAlpha(50);
						e.gc.fillRectangle(0, 0, (int)(memoryUsagePercent*3), 10);
					}
				});
				
				memoryTotal = new Label(composite_1, SWT.NONE);
				memoryTotal.setText(memoryTotalValue);
			}
			{
				Storage=new Table(composite_1, SWT.BORDER | SWT.V_SCROLL
				        | SWT.H_SCROLL);
				GridData gd_canvas = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
				gd_canvas.horizontalSpan=3;
				gd_canvas.widthHint=360;
				gd_canvas.heightHint = 50;
				Storage.setLayoutData(gd_canvas);
				Storage.setHeaderVisible(true);
				Storage.setLinesVisible(true);
				
				tableViewer = new TableViewer(Storage);
				tableViewer.setContentProvider(new ArrayContentProvider());
				tableViewer.setLabelProvider(new TableLabelProvider());
				
				TableColumn store = new TableColumn(Storage, SWT.CENTER|SWT.BOLD);
				store.setText("存储器");
				store.setWidth(200);
				TableColumn capacity = new TableColumn(Storage, SWT.CENTER|SWT.BOLD);
				capacity.setText("容量");
				capacity.setWidth(80);
				TableColumn free = new TableColumn(Storage, SWT.CENTER|SWT.BOLD);
				free.setText("可用空间");
				free.setWidth(80);
//				TableItem item=new TableItem(Storage,SWT.NONE);
//				item.setText(0, "storage");
//				item.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
//						ImageRegistry.getImagePath(ImageRegistry.STORAGE)).createImage());
//				item.setText(1, "");
//				item.setText(2, "");
				
				
				Storage.pack();
				
			}
			
//			{
//				Network=new Table(composite_1, SWT.BORDER | SWT.V_SCROLL
//				        | SWT.H_SCROLL);
//				GridData gd_canvas = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
//				gd_canvas.horizontalSpan=3;
//				gd_canvas.widthHint=240;
//				Network.setLayoutData(gd_canvas);
//				
//				Network.setHeaderVisible(true);
//				Network.setLinesVisible(true);
//				TableColumn network = new TableColumn(Network, SWT.CENTER|SWT.BOLD);
//				network.setText("网络");
//				network.setWidth(160);
//				TableColumn wnet = new TableColumn(Network, SWT.CENTER|SWT.BOLD);
//				wnet.setText("网络发送量");
//				wnet.setWidth(80);
//				TableColumn rnet = new TableColumn(Network, SWT.CENTER|SWT.BOLD);
//				rnet.setText("网络接收量");
//				rnet.setWidth(80);
//				
//				for(int i=0;i<3;i++){
//					Network.getColumn(i).pack();
//				}
//			}
			
			//rescourceComposite.pack();
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
				startCLabel = new CLabel(composite_1, SWT.MULTI);
				startCLabel.setForeground(SWTResourceManager.getColor(0, 0, 128));
				startCLabel.addMouseTrackListener(new MouseTrackAdapter() {
					@Override
					public void mouseEnter(MouseEvent arg0) {
						startCLabel.setBackground(SWTResourceManager.getColor(240, 248, 255));
						
					}
					@Override
					public void mouseExit(MouseEvent arg0) {
						startCLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						
					}
				});
				startCLabel.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseDown(MouseEvent arg0) {
						
						StartAction action=new StartAction(objectVM);
						action.run();
					}
				});
				startCLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				startCLabel.setImage(ImageRegistry.getImage(ImageRegistry.STARTUP));
				startCLabel.setBounds(57, 33, 244, 87);
				startCLabel.setText("启动虚拟机");
			}
			
			{
				rebootCLabel = new CLabel(composite_1, SWT.MULTI);
				rebootCLabel.setForeground(SWTResourceManager.getColor(0, 0, 128));
				rebootCLabel.addMouseTrackListener(new MouseTrackAdapter() {
					@Override
					public void mouseEnter(MouseEvent arg0) {
						rebootCLabel.setBackground(SWTResourceManager.getColor(240, 248, 255));
						
					}
					@Override
					public void mouseExit(MouseEvent arg0) {
						rebootCLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						
					}
				});
				rebootCLabel.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseDown(MouseEvent arg0) {
						RebootAction action=new RebootAction(objectVM);
						action.run();
					}
				});
				rebootCLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				rebootCLabel.setImage(ImageRegistry.getImage(ImageRegistry.REBOOT));
				rebootCLabel.setBounds(57, 33, 244, 87);
				rebootCLabel.setText("重启虚拟机");
			}
			
			{
				shutDownCLabel = new CLabel(composite_1, SWT.MULTI);
				shutDownCLabel.setForeground(SWTResourceManager.getColor(0, 0, 128));
				shutDownCLabel.addMouseTrackListener(new MouseTrackAdapter() {
					@Override
					public void mouseEnter(MouseEvent arg0) {
						shutDownCLabel.setBackground(SWTResourceManager.getColor(240, 248, 255));
						
					}
					@Override
					public void mouseExit(MouseEvent arg0) {
						shutDownCLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						
					}
				});
				shutDownCLabel.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseDown(MouseEvent arg0) {
						ShutDownAction action=new ShutDownAction(objectVM);
						action.run();
					}
				});
				shutDownCLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				shutDownCLabel.setImage(ImageRegistry.getImage(ImageRegistry.SHUTDOWN));
				shutDownCLabel.setBounds(57, 33, 244, 87);
				shutDownCLabel.setText("关闭虚拟机");
			}
			
			{
				templateCLabel = new CLabel(composite_1, SWT.MULTI);
				templateCLabel.setForeground(SWTResourceManager.getColor(0, 0, 128));
				templateCLabel.addMouseTrackListener(new MouseTrackAdapter() {
					@Override
					public void mouseEnter(MouseEvent arg0) {
						templateCLabel.setBackground(SWTResourceManager.getColor(240, 248, 255));
						
					}
					@Override
					public void mouseExit(MouseEvent arg0) {
						templateCLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						
					}
				});
				templateCLabel.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseDown(MouseEvent arg0) {
						ChangetoTemplateAction action=new ChangetoTemplateAction();
						action.run();
					}
				});
				templateCLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				templateCLabel.setImage(ImageRegistry.getImage(ImageRegistry.TEMPLATE));
				templateCLabel.setBounds(57, 33, 244, 87);
				templateCLabel.setText("生成模板");
			}
			
			{
				openConsoleCLabel = new CLabel(composite_1, SWT.MULTI);
				openConsoleCLabel.setForeground(SWTResourceManager.getColor(0, 0, 128));
				openConsoleCLabel.addMouseTrackListener(new MouseTrackAdapter() {
					@Override
					public void mouseEnter(MouseEvent arg0) {
						openConsoleCLabel.setBackground(SWTResourceManager.getColor(240, 248, 255));
						
					}
					@Override
					public void mouseExit(MouseEvent arg0) {
						openConsoleCLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						
					}
				});
				openConsoleCLabel.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseDown(MouseEvent arg0) {
						if(objectVM.getFolder() != null)
						{
							objectVM.getFolder().setSelection(1);
							if(objectVM.getFolder().getSelection() instanceof OnceTabItem)
							{
								OnceTabItem item = (OnceTabItem)objectVM.getFolder().getSelection();
								if(item.composite != null)
									item.composite.layout();
								else
									item.Init();
							}
						}
							
					}
				});
				openConsoleCLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				openConsoleCLabel.setImage(ImageRegistry.getImage(ImageRegistry.CONSOLE));
				openConsoleCLabel.setBounds(57, 33, 244, 87);
				openConsoleCLabel.setText("打开控制台");
			}
		}
		
		
		stateComposite = new Composite(composite, SWT.NONE); 
		stateComposite.setBackground(new Color(null,255,255,255));
		stateComposite.setLayout(layout);
		{
			GridData gd_canvas = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
			gd_canvas.widthHint = 400;
			stateComposite.setLayoutData(gd_canvas);
		}
		{ 
			Label lblNewLabel = new Label(stateComposite, SWT.NONE);
			lblNewLabel.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
			lblNewLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
			lblNewLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
			lblNewLabel.setBounds(10, 10, 300, 20);
			lblNewLabel.setText("  状态 ");
			lblNewLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		{
			Composite composite_1 = new Composite(stateComposite, SWT.BORDER);
			composite_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			composite_1.setBounds(10, 30, 300, 200);
			composite_1.setLayout(new GridLayout(2,false));
			composite_1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			composite_1.setBackgroundMode(SWT.INHERIT_DEFAULT);
			{
				type = new Label(composite_1, SWT.NONE);
				type.setText("  类型:    ");
				typeValue = new Label(composite_1, SWT.NONE);
				typeValue.setText("");
				
				status = new Label(composite_1, SWT.NONE);
				status.setText("  状态 :   ");
				statusValue = new CLabel(composite_1, SWT.MULTI);
				statusValue.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				
				problem = new Label(composite_1, SWT.NONE);
				problem.setText("  问题 :   ");
				problem.setVisible(true);
				
				problemValue = new Label(composite_1, SWT.NONE);
				problemValue.setVisible(true);
				problemValue.setText("无");
				
				solution = new Label(composite_1, SWT.NONE);
				solution.setText("  解决方案 :   ");
				solution.setVisible(true);
				
				solutionValue = new Link(composite_1, SWT.NONE);
				solutionValue.setText("无");
				solutionValue.setVisible(true);
						
				solutionValue.addSelectionListener(new SelectionAdapter() {
					   public void widgetSelected(SelectionEvent e) {
						  
						   if(objectVM.getAppType().equals(ResourceTypes.CPU))
						   {
							   CTabItem items[] = folder.getItems();
							   int i = 0;
							   for(; i < folder.getItemCount(); ++i)
							   {
								   if(items[i] instanceof CPUBindTab)
								   {
									   CPUBindTab tab = (CPUBindTab)items[i];
									   if(tab.composite==null)
										   tab.Init();
									   folder.setSelection(items[i]);
									   break;
								   }
							   }
							   if(i == folder.getItemCount())
							   {
								   CTabItem tabItem = new CPUBindTab(folder,SWT.NONE,objectVM);
								   folder.setSelection(tabItem);
								}
						   }
						   else if(objectVM.getAppType().equals(ResourceTypes.DISK))
						   {
							   CTabItem items[] = folder.getItems();
							   int i = 0;
							   for(; i < folder.getItemCount(); ++i)
							   {
								   if(items[i] instanceof DiskAdjustTab)
								   {
									   DiskAdjustTab tab = (DiskAdjustTab)items[i];
									   if(tab.composite==null)
										   tab.Init();
									   folder.setSelection(items[i]);
									   
									   break;
								   }
							   }
							   if(i == folder.getItemCount())
							   {
								   CTabItem tabItem = new DiskAdjustTab(folder,SWT.NONE, objectVM);
								   folder.setSelection(tabItem);
								}
						   }
						   else if(objectVM.getAppType().equals(ResourceTypes.MEMORY))
						   {}
						   else if(objectVM.getAppType().equals(ResourceTypes.UNRECOGNIZED))
						   {}
			            }  
				});
				
				configuration = new Label(composite_1, SWT.NONE);
				configuration.setText(typeValue.getText());
				Composite composite_2 = new Composite(composite_1, SWT.NONE);
				composite_2.setLayout(new GridLayout(3,false));
				minValue = new Text(composite_2, SWT.NONE|SWT.BORDER);
				Label gap = new Label(composite_2, SWT.NONE);
				gap.setText("—");
				maxValue = new Text(composite_2, SWT.NONE|SWT.BORDER );
				
				
				
				configurationButton = new Button(composite_1, SWT.NONE);
				configurationButton.setLayoutData(new GridData(GridData.END));
				configurationButton.setText("阈值配置");
				configurationButton.addMouseListener(new MouseAdapter(){
					@Override
					public void mouseDown(MouseEvent e) {
						if(objectVM.getAppType().equals(ResourceTypes.CPU))
						{
							objectVM.CPU_LOWER_LIMIT = Double.parseDouble(minValue.getText());
							objectVM.CPU_UPPER_LIMIT = Double.parseDouble(maxValue.getText());
						}
						else if(objectVM.getAppType().equals(ResourceTypes.MEMORY))
						{
							objectVM.MEMONY_LOWER_LIMIT = Double.parseDouble(minValue.getText());
							objectVM.MEMONY_UPPER_LIMIT = Double.parseDouble(maxValue.getText());
						}
						else if(objectVM.getAppType().equals(ResourceTypes.DISK))
						{
							objectVM.DISK_LOWER_LIMIT = Double.parseDouble(minValue.getText());
							objectVM.DISK_UPPER_LIMIT = Double.parseDouble(maxValue.getText());
						}
					}
				});
				
			}
			
		}
		generalTimer = new Timer("GeneralTimer");
		generalTimer.schedule(new GeneralTimer(this,PlatformUI.getWorkbench().getDisplay()), 0, 5000);
		objectVM.timerList.add(generalTimer);
		performTimer = new Timer("PerformTimer");
		performTimer.schedule(new PerformTimer(this,PlatformUI.getWorkbench().getDisplay()), 500, 5000);
		objectVM.timerList.add(performTimer);
		stateTimer = new Timer("StateTimer");
		stateTimer.schedule(new StateTimer(this,PlatformUI.getWorkbench().getDisplay()), 3000,5000);
		objectVM.timerList.add(stateTimer);
		
		composite.layout();
		return true;
	}
	
	
	class GeneralTimer extends TimerTask {
		private volatile VMSummaryTab summary;
		Display display;
		public GeneralTimer(VMSummaryTab summary,Display display) {
			this.summary = summary;
			this.display=display;
		}
		public void run() {
			if(objectVM.getItemState().equals(ItemState.changing))
				return;
			
			record = (VM.Record)objectVM.getRecord();
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
		private volatile VMSummaryTab summary;
		Display display;
		public PerformTimer(VMSummaryTab summary,Display display) {
			this.summary = summary;
			this.display=display;
		}
		public void run() {
			summary.getPerform();
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	
			        	summary.refreshPerform();
					}
			    };
			    this.display.asyncExec(runnable); 
			}
		}
	}

	
	class StateTimer extends TimerTask
	{
		private volatile VMSummaryTab summary;
		Display display;
		public StateTimer(VMSummaryTab summary, Display display)
		{
			this.summary = summary;
			this.display = display;
		}
		
		public void run()
		{
			if(!this.display.isDisposed())
			{
				Runnable runnable = new Runnable(){
					public void run()
					{
						summary.refreshState();
					}
				};
				this.display.asyncExec(runnable);
			}
		}
		
	}
	public void refreshGeneral(){
			
			if(record!=null){
				//objectVM.setRecord(record);
				nameValue.setText(record.nameLabel);
				descriptionValue.setText(record.nameDescription);
				uuidValue.setText(record.uuid);
				//所属主机
				if(objectVM.getParent().getParent() instanceof VMTreeObjectPool){
					VMTreeObjectPool pool = (VMTreeObjectPool)objectVM.getParent().getParent();
					hostObject = pool.hostMap.get(record.residentOn);
				}else if (objectVM.getParent() instanceof VMTreeObjectPool){
					VMTreeObjectPool pool = (VMTreeObjectPool)objectVM.getParent();
					hostObject = pool.hostMap.get(record.residentOn);
				}else{
					hostObject = (VMTreeObjectHost)objectVM.getParent();
				}
				if(hostObject!=null){
					host.setText("<a>"+hostObject.getName()+"</a>");
					host.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseDown(MouseEvent arg0) {
							ISelection selection = new StructuredSelection(new Object[]{hostObject});
							if(Constants.treeView.getViewer() != null && Constants.treeView != null )
								Constants.pageBookView.selectionChanged(Constants.treeView, selection);
							Constants.treeView.getViewer().setSelection(selection);
						}
					});
				}
				
				//操作系统信息现在还是空
//				osValue.setText("");
				cpu.setText(record.VCPUsMax+" vcpu");
				memory.setText(record.memoryDynamicMax/1024/1024+" MB");
				//ip地址目前获取不到
//				String ip = "";
//				ipValue.removeAll();
//				if(record.ipaddr.size()>0){
//					for(String s : record.ipaddr){
//						ipValue.add(s);
//					}
//				}else{
//					ipValue.add("未获取到IP");
//				}
//				ipValue.select(0);
				//vm状态信息
				refreshCommmand();
			}
			nameValue.pack();
			descriptionValue.pack();
			uuidValue.pack();
			stateValue.pack();
//			osValue.pack();
			cpu.pack(); 
			memory.pack();
//			ipValue.pack();
			host.pack();
	}
	
	public void refreshState()
	{
			typeValue.setText(appType());
			if(objectVM.getAppType().equals(ResourceTypes.UNRECOGNIZED))
			{
				statusValue.setEnabled(false);
				configuration.setText("  " + "配置:   ");
				minValue.setEnabled(false);
				maxValue.setEnabled(false);
			}
			else if(objectVM.getAppType().equals(ResourceTypes.CPU))
			{
				setStateImage(ResourceTypes.CPU ,cpuUsagePercent,objectVM.CPU_UPPER_LIMIT,objectVM.CPU_LOWER_LIMIT);
				configuration.setText("  cpu配置:   ");
				minValue.setText(Double.toString(objectVM.CPU_LOWER_LIMIT));
				maxValue.setText(Double.toString(objectVM.CPU_UPPER_LIMIT));
			}
			else if(objectVM.getAppType().equals(ResourceTypes.MEMORY))
			{
				setStateImage(ResourceTypes.MEMORY ,memoryUsagePercent,objectVM.MEMONY_UPPER_LIMIT,objectVM.MEMONY_LOWER_LIMIT);
				configuration.setText("  内存配置:   ");
				minValue.setText(Double.toString(objectVM.MEMONY_LOWER_LIMIT));
				maxValue.setText(Double.toString(objectVM.MEMONY_UPPER_LIMIT));
			}
			else if(objectVM.getAppType().equals(ResourceTypes.DISK))
			{
				setStateImage(ResourceTypes.DISK ,diskUsagePercent,objectVM.DISK_UPPER_LIMIT,objectVM.DISK_LOWER_LIMIT);
				configuration.setText("  磁盘配置:   ");
				minValue.setText(Double.toString(objectVM.DISK_LOWER_LIMIT));
				maxValue.setText(Double.toString(objectVM.DISK_UPPER_LIMIT));
			}
			typeValue.pack();
			configuration.pack();
			minValue.pack();
			maxValue.pack();
			statusValue.pack();
			problem.pack();
			problemValue.pack();
			solution.pack();
			solutionValue.pack();
	}
	public void getPerform(){
		cpuUsagePercent=objectVM.getCpuUsagePercent();
		memoryUsagePercent=objectVM.getMemoryUsagePercent();
		memoryTotalValue=MathUtil.Rounding(objectVM.getMemoryTotalValue(), 3)+"MB";
		memoryUsageValue=MathUtil.Rounding(objectVM.getMemoryUsageValue(), 3)+"MB";
		HashMap<String,Double> Wnets = objectVM.getwNetList();
		HashMap<String,Double> Rnets = objectVM.getrNetList();
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
		diskList = objectVM.getDiskList();
		diskTotalValue = objectVM.getDiskTotalValue();
		diskUsageValue = objectVM.getDiskUsageValue();
		diskUsagePercent = objectVM.getDiskUsagePercent();
	}
	public void refreshPerform(){
			//VM.Record record=(VM.Record)objectVM.getRecord();
		if(record!=null){
			if(record.powerState.equals(Types.VmPowerState.RUNNING)){
				{
					cpuUsage.setText(MathUtil.Rounding(cpuUsagePercent, 3)+"%");
					cpuTotal.setText(cpuTotalValue);
					memoryUsage.setText(memoryUsageValue);
					memoryTotal.setText(memoryTotalValue);
					
					if(diskList.size() > 0)
					{
						//Storage.removeAll();
						tableViewer.setInput(diskList);	
						//tableViewer.refresh();
					}
					
					cpuCanvas.redraw();
					memoryCanvas.redraw();
				}
			}else{
				{
					cpuUsage.setText("");
					cpuTotal.setText("");
					memoryUsage.setText("");
					memoryTotal.setText("");
					cpuUsagePercent=0;
					memoryUsagePercent=0;
					cpuCanvas.redraw();
					memoryCanvas.redraw();
				}
			}
		}
		{
			cpuUsage.pack();
			cpuTotal.pack();
			memoryUsage.pack();
			memoryTotal.pack();
			tableViewer.refresh();
		}
	}
	
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider
	{

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			
			
			if(element instanceof Disk)
			{
				Disk disk = (Disk)element;
			
				switch(columnIndex)
				{
				case 0:
					return disk.getUuid();
				case 1:
					return disk.getTotalValue()+"G";
				case 2:
					return disk.getAvailableSpace()+"G";
				}
			}
			return null;
		}
		
	}
	
	
	private void setStateImage(ResourceTypes type, double usagePercent, double UPPER_LIMIT, double LOWER_LIMIT)
	{
		statusValue.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
		if(usagePercent < (LOWER_LIMIT+UPPER_LIMIT)/2)
		{
			statusValue.setImage(ImageRegistry.getImage(ImageRegistry.FREE));
			statusValue.setText("空闲");
//			problem.setVisible(false);
//			problemValue.setVisible(false);
//			solution.setVisible(false);
//			solutionValue.setVisible(false);
			problemValue.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.NONE));
			problemValue.setText("无");
			solutionValue.setText("无");
			
		}
		else if(usagePercent >= UPPER_LIMIT)
		{
			statusValue.setImage(ImageRegistry.getImage(ImageRegistry.DISABLE));
			statusValue.setText("不可用");
			
			problemValue.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
			problem.setVisible(true);
			problemValue.setVisible(true);
			problemValue.setText(type.toString() + "过载");
			
			solution.setVisible(true);
			solutionValue.setText("<a>"+ "调整" + type.toString() + "参数" + "</a>");
			solutionValue.setVisible(true);
			
//			VMEvent event = new VMEvent();
//			event.setDatetime(new Date());
//			event.setDescription("虚拟机 '"+objectVM.getName()+"' 过载，请迅速进行资源调整！");
//			event.setType(eventType.warning);
//			event.setTarget(objectVM);
//			
//			Constant.logView.logFresh(event);
			PerformAlarmAction action = new PerformAlarmAction(objectVM, type);
			action.run();
		}
		else
		{
			statusValue.setImage(ImageRegistry.getImage(ImageRegistry.BUSY));
			statusValue.setText("繁忙");
//			problem.setVisible(false);
//			problemValue.setVisible(false);
//			solution.setVisible(false);
//			solutionValue.setVisible(false);
			problemValue.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.NONE));
			problemValue.setText("无");
			solutionValue.setText("无");
		}
	}
	
	public void refreshCommmand()
	{
		switch(record.powerState){
		case RUNNING:
			stateValue.setText("运行中");
			templateCLabel.setEnabled(false);
			templateCLabel.setImage(ImageRegistry.getImage(ImageRegistry.TEMPLATE_DISABLE));
			startCLabel.setEnabled(false);
			startCLabel.setImage(ImageRegistry.getImage(ImageRegistry.STARTUP_DISABLE));
			rebootCLabel.setEnabled(true);
			rebootCLabel.setImage(ImageRegistry.getImage(ImageRegistry.REBOOT));
			shutDownCLabel.setEnabled(true);
			shutDownCLabel.setImage(ImageRegistry.getImage(ImageRegistry.SHUTDOWN));
			openConsoleCLabel.setEnabled(true);
			openConsoleCLabel.setImage(ImageRegistry.getImage(ImageRegistry.CONSOLE));
			break;
		case HALTED:
			stateValue.setText("已关闭");
			templateCLabel.setEnabled(true);
			templateCLabel.setImage(ImageRegistry.getImage(ImageRegistry.TEMPLATE));
			startCLabel.setEnabled(true);
			startCLabel.setImage(ImageRegistry.getImage(ImageRegistry.STARTUP));
			rebootCLabel.setEnabled(false);
			rebootCLabel.setImage(ImageRegistry.getImage(ImageRegistry.REBOOT_DISABLE));
			shutDownCLabel.setEnabled(false);
			shutDownCLabel.setImage(ImageRegistry.getImage(ImageRegistry.SHUTDOWN_DISABLE));
			openConsoleCLabel.setEnabled(false);
			openConsoleCLabel.setImage(ImageRegistry.getImage(ImageRegistry.CONSOLE_DISABLE));
			break;
		case SUSPENDED:
			stateValue.setText("已挂起");
			break;
		}
	}
	
	public void refresh(){
		refreshGeneral();
		refreshPerform();
		refreshCommmand();
		refreshState();
	}
	
	public String appType(){
		if(objectVM.getAppType().equals(ResourceTypes.UNRECOGNIZED)){
			return "未知";
		}else{
			return objectVM.getAppName()+"【敏感资源："+objectVM.getAppType()+"】";
		}
	}
}
