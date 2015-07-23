package oncecenter.maintabs.vm;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;

import oncecenter.maintabs.OnceVMTabItem;
import oncecenter.util.FileUtil;
import oncecenter.util.ImageRegistry;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wb.swt.SWTResourceManager;

import twaver.Branch;
import twaver.Element;
import twaver.Link;
import twaver.Node;
import twaver.PolyLine;
import twaver.Segment;
import twaver.TDataBox;
import twaver.TWaverConst;
import twaver.network.NetworkToolBarFactory;
import twaver.network.TNetwork;
import twaver.network.background.ColorBackground;

import com.once.xenapi.Connection;
import com.once.xenapi.VM;

public class CPUBindTab extends OnceVMTabItem {
	Connection conn = null;
	VM vm = null;
	private Button oneKey ;
	private Label message;

	ArrayList<Node> vcpuNodes = new ArrayList<Node>();
	ArrayList<Node> pcpuNodes = new ArrayList<Node>();

	Table cpuTable;
	TDataBox box;
	Map<Long,Long> vcpusCpu = new HashMap<Long,Long>();
	
	long vcpUs = 0;
	long pcpu = 0;
	Composite chartComp ;
	Frame netFrame;
	public CPUBindTab(CTabFolder arg0, int arg1, VMTreeObjectVM object) {
		super(arg0, arg1, object);
		setText("资源调整（CPU绑定）");
		this.objectVM = object;
	}

	public CPUBindTab(CTabFolder arg0, int arg1, int arg2, VMTreeObjectVM object) {
		super(arg0, arg1, arg2, object);
		setText("资源调整（CPU绑定）");
		this.objectVM = object;
	}

	public boolean Init() {

		conn = objectVM.getConnection();
		vm = (VM)objectVM.getApiObject();
		

		
		try {
			vcpUs = vm.getVCPUsMax(conn);
			// System.out.println("The vcpu id is from 0 to " +
			// String.valueOf((vcpUs-1)));
			pcpu = vm.getResidentOn(conn).getRecord(conn).hostCPUs.size();
			// System.out.println("The pcpu id is from 0 to " +
			// String.valueOf((pcpu-1)));
			
			vcpusCpu = vm.getVCPUsCPU(conn);
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		composite = new Composite(folder, SWT.NONE);
		setControl(composite);
		composite.setBackground(new Color(null, 255, 255, 255));
		composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite titleComp = new Composite(composite, SWT.NONE);
		titleComp.setLayout(new GridLayout(3, false));
		titleComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label l = new Label(titleComp,SWT.NONE);
		l.setText("   调整虚拟cpu和物理cpu绑定情况");
		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		oneKey = new Button(titleComp,SWT.NONE);
		oneKey.setText("一键调优");
		oneKey.setFont(SWTResourceManager.getFont("微软雅黑", 15, SWT.NONE));
		GridData g = new GridData();
		g.verticalSpan=2;
		oneKey.setLayoutData(g);
		
		
		new Label(titleComp,SWT.NONE).setText("    ");
		
		CLabel info = new CLabel(titleComp,SWT.NONE);
		info.setText("通过调整虚拟cpu和物理cpu的绑定情况，可以防止同台物理机上的其他虚拟机的干扰。");
		info.setImage(ImageRegistry.getImage(ImageRegistry.INFO));
		
		
		
		
		//dhf-改画图方式为异步
		chartComp = new Composite(composite, SWT.EMBEDDED | SWT.NONE);
		chartComp.setLayout(new GridLayout(1, false));
		chartComp.setLayoutData(new GridData(GridData.FILL_BOTH));

		netFrame = SWT_AWT.new_Frame(chartComp);
		netFrame.setLayout(new BorderLayout());
		netFrame.setBackground(java.awt.Color.white);

		
		Composite tableComposite = new Composite(composite, SWT.NONE);
		tableComposite.setLayout(new GridLayout(4, false));
		tableComposite.setBackground(new Color(null, 255, 255, 255));
		GridData gd_canvas = new GridData(GridData.FILL_HORIZONTAL);
		gd_canvas.heightHint = 230;
		tableComposite.setLayoutData(gd_canvas);

		new Label(tableComposite,SWT.NONE).setText("手动调整说明：");
		new Label(tableComposite,SWT.NONE);
		new Label(tableComposite,SWT.NONE);
		new Label(tableComposite,SWT.NONE);
		
		CLabel vCpu = new CLabel(tableComposite,SWT.NONE);
		vCpu.setText("代表虚拟CPU");
		vCpu.setImage(ImageRegistry.getImage(ImageRegistry.VCPU));
		
		CLabel pCpu = new CLabel(tableComposite,SWT.NONE);
		pCpu.setText("代表物理CPU");
		pCpu.setImage(ImageRegistry.getImage(ImageRegistry.PCPU));
		
		CLabel bind = new CLabel(tableComposite,SWT.NONE);
		bind.setText("代表绑定情况");
		bind.setImage(ImageRegistry.getImage(ImageRegistry.CPUBIND));
		//bind.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Composite infoComposite = new Composite(tableComposite, SWT.NONE);
		infoComposite.setLayout(new GridLayout(2,false));
		infoComposite.setBackground(new Color(null, 255, 255, 255));
		Button b = new Button(infoComposite, SWT.NONE);
		b.setText("确认");
		//b.setFont(SWTResourceManager.getFont("微软雅黑", 15, SWT.BOLD));
		
		message = new Label(infoComposite,SWT.NONE);
		message.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		GridData gd_mess = new GridData(GridData.FILL_BOTH);
		gd_mess.horizontalAlignment = GridData.BEGINNING;
		gd_mess.verticalAlignment = GridData.CENTER;
		message.setLayoutData(gd_mess);
		message.setText("                            ");
		b.addMouseListener(new org.eclipse.swt.events.MouseAdapter() {
			public void mouseDown(org.eclipse.swt.events.MouseEvent arg0) {

				Map<Long, String> params = new HashMap<Long, String>();
				for (Object object : box.getAllElements()) {
					if (object instanceof Link) {
						Link link = (Link) object;
						Node from = link.getFrom();
						Node to = link.getTo();
						if (from.getName().startsWith("vcpu")) {
							long f = Long.parseLong(from.getName().substring(4));
							String t = to.getName().substring(4);
							if(params.get(f)!=null){
								t=params.get(f)+","+t;
							}
							params.put(f, t);
						} else {
							long f = Long.parseLong(to.getName().substring(4));
							String t = from.getName().substring(4);
							if(params.get(f)!=null){
								t=params.get(f)+","+t;
							}
							params.put(f, t);
						}
					}
				}

				try {
					for(long i:params.keySet())
					{
						System.out.println("vcpu:"+i+" pcpu:"+params.get(i));
						vm.setVCPUsAffinity(conn, i, params.get(i));
					}
					
				}
				catch (Exception e) {
					
					e.printStackTrace();
				}
				message.setText("绑定成功");
			}
			}
		);

		Label title = new Label(tableComposite, SWT.NONE);
		title.setText("选中物理cpu当前状态");
		title.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
		new Label(tableComposite,SWT.NONE);
		new Label(tableComposite,SWT.NONE);
		new Label(tableComposite,SWT.NONE);

		cpuTable = new Table(tableComposite, SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.FULL_SELECTION);
		cpuTable.setLayout(new FillLayout());
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan=4;
		cpuTable.setLayoutData(data);
		cpuTable.setHeaderVisible(true);

		TableColumn vcpu = new TableColumn(cpuTable, SWT.CENTER | SWT.BOLD);
		vcpu.setText("虚拟cpu编号");
		vcpu.setWidth(80);
		TableColumn vmName = new TableColumn(cpuTable, SWT.CENTER | SWT.BOLD);
		vmName.setText("虚拟机");
		vmName.setWidth(200);
		TableColumn hostName = new TableColumn(cpuTable, SWT.CENTER | SWT.BOLD);
		hostName.setText("主机");
		hostName.setWidth(200);
		TableColumn poolName = new TableColumn(cpuTable, SWT.CENTER | SWT.BOLD);
		poolName.setText("资源池");
		poolName.setWidth(200);
		
	
		JLabel msgLabel = new JLabel("正在绑定到CPU",JLabel.CENTER);
		msgLabel.setText("正在绘图，请稍候.......");
		msgLabel.setForeground(java.awt.Color.black);
		java.awt.Font font = new java.awt.Font("Serif",java.awt.Font.BOLD,12);
		msgLabel.setFont(font);
		netFrame.add(msgLabel);
		
		ChartInitAction chartInitAction = new ChartInitAction(PlatformUI.getWorkbench().getDisplay());
		chartInitAction.start();
		
		composite.layout();
		return true;
	}

	class ChartInitAction extends Thread
	{
		Display display;
		Frame parentFrame;
		Frame frame;
		public ChartInitAction(Display display) {
			this.display = display;
		}
		public void run()
		{
			if(!this.display.isDisposed())
			{
				Runnable runnable = new Runnable(){

					@Override
					public void run() {
						
						parentFrame = SWT_AWT.new_Frame(chartComp);
						parentFrame.setLayout(new BorderLayout());
						parentFrame.setBackground(java.awt.Color.black);

						box = new TDataBox("");
						
						for (long i = 0; i < vcpUs; i++) {
							Node node = new Node("vcpu" + i);
							node.setImage("file:"+FileUtil.getXenCenterRoot()+ImageRegistry.getImagePath(ImageRegistry.VCPU));
							node.setName("vcpu" + i);
							node.setLocation(50 + i * 100, 50);
							vcpuNodes.add(node);
							box.addElement(node);
						}

						for (long i = 0; i < pcpu; i++) {
							Node node = new Node("pcpu" + i);
							node.setName("pcpu" + i);
							node.setLocation(50 + i * 100, 150);
							node.setImage("file:"+FileUtil.getXenCenterRoot()+ImageRegistry.getImagePath(ImageRegistry.PCPU));
							pcpuNodes.add(node);
							box.addElement(node);
						}
						
						for(long i:vcpusCpu.keySet()){
							long j = vcpusCpu.get(i);
							Link link = new Link();
							link.setFrom(vcpuNodes.get((int)i));
							link.setTo(pcpuNodes.get((int)j));
							box.addElement(link);
						}

						final TNetwork network = new TNetwork(box);
						network.setName("xxx.network");
						network.setToolbarByName(null);
						network.clearMovableFilters();
						network.setDoubleBuffered(true);

						network.setToolbar(NetworkToolBarFactory.getToolBar(
								TWaverConst.EDITOR_TOOLBAR, network));

						ColorBackground color = new ColorBackground(java.awt.Color.white);
						network.setNetworkBackground(color);

						
						network.getCanvas().addMouseListener(new MouseAdapter() {
							public void mouseClicked(MouseEvent e) {
								if (e.getClickCount() == 1) {
									Element element = network.getElementPhysicalAt(e.getPoint());
									if (element == null) {

									} else {
										if (element instanceof Node) {
											Node node = (Node) element;
											box.getSelectionModel().setSelection(element);
											if (pcpuNodes.contains(node)) {
												if (!PlatformUI.getWorkbench().getDisplay().isDisposed()) {
													Runnable runnable = new Runnable() {
														public void run() {

														}
													};
													PlatformUI.getWorkbench().getDisplay().syncExec(runnable);

												}
											}
										}
									}
								}
							}
						});

						// // 在元素上双击时打开对应的编辑器
						network.addElementDoubleClickedActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								if (e.getSource() instanceof Link) {
									Link link = (Link) e.getSource();
									box.removeElement(link);
									// network.repaint();
								}
							}
						});
						parentFrame.add(network);
					}
				};
				this.display.syncExec(runnable);
			}
			
			if (!this.display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						
						
						Frame c = netFrame;
						netFrame=parentFrame;
						setControl(composite);
						composite.layout();
						c.dispose();
					}
				};
				this.display.syncExec(runnable);
			}
		}
	}
	private void drawFrameForCpus(int leftTopMar, int rightBottomMar,
			int nodeWidth, int nodeHeight, int num, int startY) {
		Point point1 = new Point(50 - leftTopMar, startY - leftTopMar);
		Point point2 = new Point(50 + (int) (num - 1) * 100 + nodeWidth
				+ rightBottomMar, startY - leftTopMar);
		Point point3 = new Point(50 + (int) (num - 1) * 100 + nodeWidth
				+ rightBottomMar, startY + nodeHeight + rightBottomMar);
		Point point4 = new Point(50 - leftTopMar, startY + nodeHeight
				+ rightBottomMar);

		Segment seg1 = new Segment(point1, point2);
		Segment seg2 = new Segment(point2, point3);
		Segment seg3 = new Segment(point3, point4);
		Segment seg4 = new Segment(point4, point1);

		Branch branch1 = new Branch();
		if (startY == 50)
			branch1.setBranchColor(java.awt.Color.BLUE);
		else
			branch1.setBranchColor(java.awt.Color.RED);
		branch1.addSegment(seg1);
		branch1.addSegment(seg2);
		branch1.addSegment(seg3);
		branch1.addSegment(seg4);
		PolyLine pl = new PolyLine();
		pl.addBranch(branch1);
		box.addElement(pl);
	}
}
