package oncecenter.maintabs.host;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Types;

import oncecenter.Constants;
import oncecenter.maintabs.OnceHostTabItem;
import oncecenter.maintabs.host.VlanTagTab.RefreshTimer;
import oncecenter.maintabs.host.VlanTagTab.TableLabelProvider;
import oncecenter.util.FileUtil;
import oncecenter.util.ImageRegistry;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import twaver.Link;
import twaver.Node;
import twaver.TDataBox;
import twaver.network.TNetwork;
import twaver.network.background.ColorBackground;

public class NetVirtualTab extends OnceHostTabItem {

	Composite chartComp;
	Frame chartFrame;
	Frame parentFrame;
	
	Timer refreshTimer;
	
	public NetVirtualTab(CTabFolder arg0, int arg1, int arg2,
			VMTreeObjectHost object) {
		super(arg0, arg1, arg2, object);
		setText("服务器虚拟化");
		
	}

	public boolean Init(){
		composite = new Composite(folder, SWT.NONE); 
		this.setControl(composite);
		composite.setLayout(new GridLayout(2,false));
		composite.setBackground(new Color(null,255,255,255));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		
		Composite textComp = new Composite(composite, SWT.NONE);
		textComp.setLayout(new GridLayout(1,false));
		
		{
			Label label=new Label(textComp,SWT.NONE);
			label.setBackground(new Color(null,255,255,255));
			label.setImage(ImageRegistry.getImage(ImageRegistry.SERVERVIRTUALIZATION));
		}
		
		{
			Label label=new Label(textComp,SWT.NONE);
			label.setBackground(new Color(null,255,255,255));
			label.setText("您可以在一台物理机上同时运行多个活跃的操作系统。");
		}
		chartComp = new Composite(composite, SWT.EMBEDDED | SWT.NONE);
		chartComp.setLayout(new GridLayout(1, false));
		chartComp.setLayoutData(new GridData(GridData.FILL_BOTH));

		chartFrame = SWT_AWT.new_Frame(chartComp);
		chartFrame.setLayout(new BorderLayout());
		chartFrame.setBackground(java.awt.Color.LIGHT_GRAY);
				
		JLabel msgLabel = new JLabel("正在绘图",JLabel.CENTER);
		msgLabel.setText("正在绘图，请稍候.......");
		msgLabel.setForeground(java.awt.Color.black);
		java.awt.Font font = new java.awt.Font("Serif",java.awt.Font.BOLD,12);
		msgLabel.setFont(font);
		chartFrame.add(msgLabel);
		
		composite.layout();	
		
		refreshTimer = new Timer("RefreshTimer");
		refreshTimer.schedule(new RefreshTimer(PlatformUI.getWorkbench().getDisplay()), 0, 5000);
		objectHost.timerList.add(refreshTimer);
		
		return true;
	}
	
	class RefreshTimer extends TimerTask {
		Display display;
		public RefreshTimer(Display display) {
			this.display=display;
		}
		public void run() {
			if(!display.isDisposed())
			{
				Runnable runnable = new Runnable(){

					@Override
					public void run() {
						
						parentFrame = SWT_AWT.new_Frame(chartComp);
						parentFrame.setLayout(new BorderLayout());
						parentFrame.setBackground(java.awt.Color.LIGHT_GRAY);

						TDataBox box = new TDataBox("");
						
//						VMTreeObjectRoot root = objectHost;
//						if(objectHost.getParent() instanceof VMTreeObjectRoot){
//							root = (VMTreeObjectRoot)objectHost.getParent();
//						}
						ArrayList<VMTreeObjectVM> vmList = new ArrayList<VMTreeObjectVM>();
//						if(root.vmMap!=null&&root.vmMap.size()>0){
//							vmList = new ArrayList<VMTreeObjectVM>(root.vmMap.values());
//						}else{
							for(VMTreeObject vm:objectHost.getChildren()){
								if(vm instanceof VMTreeObjectVM){
									vmList.add((VMTreeObjectVM)vm);
								}
							}
//						}
						for(VMTreeObjectVM vm:vmList){
							
							//Group g = new Group();
							
							Node node = new Node(vm.getName());
							if(vm.getRecord().powerState.equals(Types.VmPowerState.RUNNING)){
								node.setImage("file:"+FileUtil.getXenCenterRoot()+ImageRegistry.getImagePath(ImageRegistry.VMONINCHART));
							}else{
								node.setImage("file:"+FileUtil.getXenCenterRoot()+ImageRegistry.getImagePath(ImageRegistry.VMOFFINCHART));
							}
							
							node.setName(vm.getName());
							//node.setLocation(50 + i * 100, 50);
							//vcpuNodes.add(node);
							
							box.addElement(node);
							
						}

						TNetwork network = new TNetwork(box);
						network.setName("xxx.network");
						network.setToolbarByName(null);
						network.clearMovableFilters();
						network.setDoubleBuffered(true);

						network.doLayout(3,false);
						
						network.addElementDoubleClickedActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								if (e.getSource() instanceof Node) {
									Node node = (Node) e.getSource();
									String name = node.getName();
									for(final VMTreeObject o : objectHost.getChildren()){
										if(o.getName().equals(name)){
											if (!display.isDisposed()) {
												Runnable runnable = new Runnable() {
													public void run() {
														ISelection selection = new StructuredSelection(new Object[]{o});
														Constants.treeView.getViewer().setSelection(selection);
														
														Constants.pageBookView.selectionChanged(Constants.treeView, selection);
													}
												};
												display.syncExec(runnable);
											}
											break;
											
										}
									}
								}
							}
						});
						
						ColorBackground color = new ColorBackground(java.awt.Color.lightGray);
						network.setNetworkBackground(color);

						parentFrame.add(network);
					}
				};
				display.syncExec(runnable);
			}
			
			if (!display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						
						
						Frame c = chartFrame;
						chartFrame=parentFrame;
						chartComp.layout();
						c.dispose();
					}
				};
				display.syncExec(runnable);
			}
		}
	}
}
