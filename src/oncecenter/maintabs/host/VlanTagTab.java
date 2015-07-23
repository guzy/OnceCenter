package oncecenter.maintabs.host;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Types;
import com.once.xenapi.VM;

import oncecenter.maintabs.OnceHostTabItem;
import oncecenter.util.FileUtil;
import oncecenter.util.ImageRegistry;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectTemplate;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import twaver.Link;
import twaver.Node;
import twaver.TDataBox;
import twaver.network.TNetwork;
import twaver.network.background.ColorBackground;

public class VlanTagTab extends OnceHostTabItem {
	
	//CTabFolder innerFolder;
	
	Timer refreshTimer;
	
	Table vlanTable;
	TableViewer tableViewer;
	
	Composite chartComp;
	Frame chartFrame;
	Frame parentFrame;
	
	//CTabItem chartItem;
	
	//ArrayList<VMTreeObjectVM> vmList = new ArrayList<VMTreeObjectVM>();
	ArrayList<VMTag> vmTagList = new ArrayList<VMTag>();
	ArrayList<VMTag> newVmTagList = new ArrayList<VMTag>();
	Map<VMTreeObjectVM,TableEditor> vmTextMap = new HashMap<VMTreeObjectVM,TableEditor>();
	Map<VMTreeObjectVM,TableEditor> okButtonMap = new HashMap<VMTreeObjectVM,TableEditor>();
	Map<VMTreeObjectVM,TableEditor> cancelButtonMap = new HashMap<VMTreeObjectVM,TableEditor>();
	Map<TableEditor,VMTreeObjectVM> okButtonReverseMap = new HashMap<TableEditor,VMTreeObjectVM>();
	Map<TableEditor,VMTreeObjectVM> cancelButtonReverseMap = new HashMap<TableEditor,VMTreeObjectVM>();

	 int rowHeight = 30;

	 int heightSpan = 5;

	private CTabItem chartItem;

	private CTabFolder innerFolder;
	
	//ArrayList<TableEditor> editorList = new ArrayList<TableEditor>();
	
	public VlanTagTab(CTabFolder arg0, int arg1, int arg2,
			VMTreeObjectHost object) {
		super(arg0, arg1, arg2, object);
		setText("网络虚拟化");
		
	}

	public boolean Init(){
		composite = new Composite(folder, SWT.NONE); 
		this.setControl(composite);
		composite.setLayout(new GridLayout(1,false));
		composite.setBackground(new Color(null,255,255,255));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		//composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		
		Label label=new Label(composite,SWT.NONE);
		label.setBackground(new Color(null,255,255,255));
		label.setText("定制虚拟机vlan信息，有效范围为-1~4094。当物理机重启（相当于交换机重启），请重新设置vlan信息。Vlan取值为-1代表该虚拟机没有任何tag信息。");
		
		innerFolder = new CTabFolder(composite, SWT.NONE);
		innerFolder.setTabHeight(20);
		innerFolder.setLayout(new GridLayout(1,false));
		innerFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		innerFolder.setMaximizeVisible(true);
		innerFolder.setMinimizeVisible(true);
		
		chartItem = new CTabItem(innerFolder,SWT.NONE,0);
		chartItem.setText("网络虚拟化");
		
		chartComp = new Composite(innerFolder, SWT.EMBEDDED | SWT.NONE);
		chartComp.setLayout(new GridLayout(1, false));
		chartComp.setLayoutData(new GridData(GridData.FILL_BOTH));

		chartFrame = SWT_AWT.new_Frame(chartComp);
		chartFrame.setLayout(new BorderLayout());
		chartFrame.setBackground(java.awt.Color.white);
				
		JLabel msgLabel = new JLabel("正在绘图",JLabel.CENTER);
		msgLabel.setText("正在绘图，请稍候.......");
		msgLabel.setForeground(java.awt.Color.black);
		java.awt.Font font = new java.awt.Font("Serif",java.awt.Font.BOLD,12);
		msgLabel.setFont(font);
		chartFrame.add(msgLabel);
		
		chartItem.setControl(chartComp);
		chartComp.layout();
		
		CTabItem tableItem = new CTabItem(innerFolder,SWT.NONE,1);
		tableItem.setText("修改vlan配置");
		Composite tableComposite = new Composite(innerFolder, SWT.NONE);
		tableComposite.setLayout(new GridLayout(1,false));
		tableComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableItem.setControl(tableComposite);
		vlanTable=new Table(tableComposite, SWT.BORDER | SWT.V_SCROLL
		        | SWT.H_SCROLL|SWT.FULL_SELECTION);
		vlanTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		vlanTable.setHeaderVisible(true);
		vlanTable.setLinesVisible(true);
		
//		vlanTable.addListener(SWT.MeasureItem, new Listener() {
//		    public void handleEvent(Event event) {
//		        event.height =rowHeight ;
//		    }
//		});

		tableViewer = new TableViewer(vlanTable);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider());
		
		TableColumn name = new TableColumn(vlanTable, SWT.CENTER|SWT.BOLD);
		name.setText("虚拟机");
		name.setWidth(400);
		TableColumn vlan = new TableColumn(vlanTable, SWT.CENTER|SWT.BOLD);
		vlan.setText("TAG");
		vlan.setWidth(150);
		TableColumn queren = new TableColumn(vlanTable, SWT.CENTER|SWT.BOLD);
		queren.setText("确认");
		queren.setWidth(150);
		TableColumn quxiao = new TableColumn(vlanTable, SWT.CENTER|SWT.BOLD);
		quxiao.setText("重置");
		quxiao.setWidth(150);
		
		//tableViewer.setInput(vmList);
//		tableItem.setControl(tableComposite);
//		tableComposite.layout();
//		
//		innerFolder.setSelection(0);
		
		composite.layout();	
		
		refreshTimer = new Timer("RefreshTimer");
		refreshTimer.schedule(new RefreshTimer(PlatformUI.getWorkbench().getDisplay()), 0, 5000);
		objectHost.timerList.add(refreshTimer);
		
		return true;
	}
	
	public void drawChart(Display display,ArrayList<VMTag> vmTagList){
		final Map<Integer,ArrayList<VMTreeObjectVM>> tagMap = new HashMap<Integer,ArrayList<VMTreeObjectVM>>();
		for(VMTag vmTag:vmTagList){
			if(tagMap.containsKey(vmTag.tag)){
				ArrayList<VMTreeObjectVM> vmList = tagMap.get(vmTag.tag);
				vmList.add(vmTag.vmObject);
			}else{
				ArrayList<VMTreeObjectVM> vmList = new ArrayList<VMTreeObjectVM>();
				vmList.add(vmTag.vmObject);
				tagMap.put(vmTag.tag, vmList);
			}
		}
		if(!display.isDisposed())
		{
			Runnable runnable = new Runnable(){

				@Override
				public void run() {
					
					parentFrame = SWT_AWT.new_Frame(chartComp);
					parentFrame.setLayout(new BorderLayout());
					parentFrame.setBackground(java.awt.Color.black);

					TDataBox box = new TDataBox("");
					
					for(int tag:tagMap.keySet()){
						
						//Group g = new Group();
						
						Node node = new Node("vlan" + tag);
						node.setImage("file:"+FileUtil.getXenCenterRoot()+ImageRegistry.getImagePath(ImageRegistry.SWITCH));
						node.setName("vlan" + tag);
						//node.setLocation(50 + i * 100, 50);
						//vcpuNodes.add(node);
						
						box.addElement(node);
						//g.addChild(node);
						for(VMTreeObjectVM vm:tagMap.get(tag)){
							Node vmNode = new Node(vm.getName());
							vmNode.setImage("file:"+FileUtil.getXenCenterRoot()+ImageRegistry.getImagePath(ImageRegistry.VMINVLAN));
							vmNode.setName(vm.getName());
							//node.setLocation(50 + i * 100, 50);
							//vcpuNodes.add(node);
							box.addElement(vmNode);
							//g.addChild(node);
							Link link = new Link();
							link.setFrom(node);
							link.setTo(vmNode);
							box.addElement(link);
							
						}
						
						//box.addElement(g);
					}

					TNetwork network = new TNetwork(box);
					network.setName("xxx.network");
					network.setToolbarByName(null);
					network.clearMovableFilters();
					network.setDoubleBuffered(true);

					network.doLayout(1,false);
					
					ColorBackground color = new ColorBackground(java.awt.Color.white);
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
					chartItem.setControl(chartComp);
					chartComp.layout();
					c.dispose();
				}
			};
			display.syncExec(runnable);
		}
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
			
		}
	}
	
	class RefreshTimer extends TimerTask {
		Display display;
		public RefreshTimer(Display display) {
			this.display=display;
		}
		public void run() {
			getTags();
			
				if (!this.display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
				        	vlanTable.removeAll();
//				        	if(editorList!=null){
//				        		for(TableEditor e:editorList){
//					        		if(e!=null&&e.getEditor()!=null){
//					        			e.getEditor().dispose();
//						        		e.dispose();
//					        		}
//					        	}
//					        	editorList.clear();
//				        	}
				        	
				        	tableViewer.setInput(vmTagList);
				        	//vlanTable.layout();
				        }
				    };
				    this.display.syncExec(runnable); 
				drawChart(display, vmTagList);
			}
		}
	}
	
	class VMTag{
		private VMTreeObjectVM vmObject;
		private int tag;
		public VMTag(VMTreeObjectVM vmObject,int tag){
			this.setVmObject(vmObject);
			this.setTag(tag);
		}
		public VMTreeObjectVM getVmObject() {
			return vmObject;
		}
		public void setVmObject(VMTreeObjectVM vmObject) {
			this.vmObject = vmObject;
		}
		public int getTag() {
			return tag;
		}
		public void setTag(int tag) {
			this.tag = tag;
		}
	}
	
	public void getTags(){
		
		vmTagList.clear();
		
		ArrayList<VMTreeObjectVM> vmList = new ArrayList<VMTreeObjectVM>();
		VMTreeObjectRoot root ;
		if(objectHost.getParent() instanceof VMTreeObjectPool){
			root = (VMTreeObjectRoot)objectHost.getParent();
		}else{
			root = (VMTreeObjectRoot)objectHost;
		}
		for(VMTreeObjectVM vmObject:root.vmMap.values()){
			VM.Record r = vmObject.getRecord();
			if(r.powerState.equals(Types.VmPowerState.RUNNING)){
				if(r.residentOn.equals(objectHost.getApiObject())){
					if(r.powerState.equals(Types.VmPowerState.RUNNING)){
						int i;
						for(i=0;i<vmList.size();i++){
							VMTreeObject o= vmList.get(i);
							if(o.getName().compareToIgnoreCase(vmObject.getName())>0)
								break;
						}
						vmList.add(i, vmObject);
					}
				}
			}
		}
		
		for(VMTreeObjectVM vmObject:vmList){
			if(vmObject.getRecord().powerState.equals(Types.VmPowerState.RUNNING)){
				VM vm = (VM)vmObject.getApiObject();
				//int tag = ovs.getTag(vmObject.getConnection(), vm);
				int tag = -1;
//				try{
//					tag = Integer.valueOf(vm.getTag(objectHost.getConnection()));
//				}catch(Exception e){
//					e.printStackTrace();
//				}
				VMTag vmTag = new VMTag(vmObject,tag);
				vmTagList.add(vmTag);
			}else{
				vmTagList.add(new VMTag(vmObject,0));
			}
		}
		
//		for(int i = 0;i<100;i++){
//			vmTagList.add(new VMTag(new VMTreeObjectVM("vm"+i,null,null,null),i));
//		}
	}
	
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider{

		@Override
		public Image getColumnImage(Object arg0, int arg1) {
			
			return new Image(null,1,rowHeight);
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			
			if(element instanceof VMTag)
			{
				VMTreeObjectVM vm = ((VMTag)element).getVmObject();
				int tag = ((VMTag)element).getTag();
				switch(columnIndex) {		   
				   case 0:
					   return vm.getName();
				   case 1:
				   	{
				   		if(vmTextMap.containsKey(vm)){
				   			TableEditor editor = vmTextMap.get(vm);
				   			Text tagText = (Text)editor.getEditor();
				   			if(!tagText.isFocusControl()){
				   				tagText.setText(tag+"");
				   			}
				   			editor.dispose();
				   			TableEditor newEditor = new TableEditor(vlanTable);
				   			newEditor.verticalAlignment = SWT.CENTER;
				   			newEditor.horizontalAlignment = SWT.CENTER;
				   			newEditor.minimumWidth = 100;
				   			newEditor.minimumHeight = rowHeight-heightSpan;
				   			newEditor.setEditor(tagText,vlanTable.getItem(vmTagList.indexOf(element)),1); 
				   			vmTextMap.put(vm,newEditor);
				   			System.out.println("contains");
				   		}else{
					        Text tagText  =   new Text(vlanTable,SWT.BORDER); 
					        tagText.setText(tag+"");
					        TableEditor editor = new TableEditor(vlanTable);
					        editor.verticalAlignment = SWT.CENTER;
					        editor.horizontalAlignment = SWT.CENTER;
							editor.minimumWidth = 100;
					        editor.minimumHeight = rowHeight-heightSpan;
					        editor.setEditor(tagText,vlanTable.getItem(vmTagList.indexOf(element)),1); 
					        vmTextMap.put(vm,editor);
					        //editorList.add(editor);
					        System.out.println("new text");
				   		}
				        return "";				
				   	}
				   case 2:
				   {
					   if(!okButtonMap.containsKey(vm)){
						   final TableEditor   editor   =   new   TableEditor(vlanTable);
					         Button okButton  =   new Button(vlanTable,SWT.NONE); 
					        okButton.setText("确定");
					        editor.verticalAlignment = SWT.TOP;
							editor.horizontalAlignment = SWT.CENTER;
							editor.minimumWidth = 100;
					        editor.minimumHeight = rowHeight-heightSpan;
					        editor.setEditor(okButton,vlanTable.getItem(vmTagList.indexOf(element)),2); 
					        okButtonMap.put(vm, editor);
					        okButtonReverseMap.put(editor,vm);
					        okButton.addListener(SWT.MouseDown,   new   Listener(){
					              public   void   handleEvent(Event   e){
					            	  
					            	  VMTreeObjectVM vm = okButtonReverseMap.get(editor);
					            	  String s = ((Text)vmTextMap.get(vm).getEditor()).getText();
					                    int tag = 0;
							        	try{
							        		tag = Integer.parseInt(s);
							        	}catch(Exception e1){
							        		MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
							    			messageBox.setText("警告");
							    			messageBox.setMessage("请输入-1~4094的整数");
							    			messageBox.open();
							    			vlanTable.setFocus();
							    			return ;
							        	}
							        	if(tag<-1||tag>4094){
							        		MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
							    			messageBox.setText("警告");
							    			messageBox.setMessage("请输入-1~4094的整数");
							    			messageBox.open();
							    			vlanTable.setFocus();
							    			return ;
							        	}
							        	SetTagJob job = new SetTagJob(vm,
							        			PlatformUI.getWorkbench().getDisplay(),
							        			tag,(Text)vmTextMap.get(vm).getEditor());
							        	job.schedule();
					                }
					        });

					        //editorList.add(editor);
					        System.out.println("new button");
					   }else{
						   TableEditor editor = okButtonMap.get(vm);
						   Button button = (Button)editor.getEditor();
				   			editor.dispose();
				   			TableEditor newEditor = new TableEditor(vlanTable);
				   			newEditor.verticalAlignment = SWT.TOP;
				   			newEditor.horizontalAlignment = SWT.CENTER;
				   			newEditor.minimumWidth = 100;
				   			newEditor.minimumHeight = rowHeight-heightSpan;
				   			newEditor.setEditor(button,vlanTable.getItem(vmTagList.indexOf(element)),2); 
				   			okButtonMap.put(vm,newEditor);
				   			okButtonReverseMap.put(newEditor,vm);
				   			System.out.println("contains");
					   }
				        return "";				
				   }
				   case 3:
				   {
					   if(!cancelButtonMap.containsKey(vm)){
						    final TableEditor   editor   =   new   TableEditor(vlanTable);
					        Button okButton  =   new Button(vlanTable,SWT.NONE); 
					        okButton.setText("重置");
					        editor.verticalAlignment = SWT.TOP;
							editor.horizontalAlignment = SWT.CENTER;
							editor.minimumWidth = 100;
					        editor.minimumHeight = rowHeight-heightSpan;
					        editor.setEditor(okButton,vlanTable.getItem(vmTagList.indexOf(element)),3); 
					        cancelButtonMap.put(vm, editor);
					        okButtonReverseMap.put(editor,vm);
					        //editorList.add(editor);
					        okButton.addListener(SWT.MouseDown,   new   Listener(){
					              public   void   handleEvent(Event   e){
					            	  VMTreeObjectVM vm = okButtonReverseMap.get(editor);
					            	    int tag = -1;
							        	SetTagJob job = new SetTagJob(vm,
							        			PlatformUI.getWorkbench().getDisplay(),
							        			tag,(Text)vmTextMap.get(vm).getEditor());
							        	job.schedule();
					                }
					        });
					        System.out.println("new button");
					   }else{
						   TableEditor editor = cancelButtonMap.get(vm);
						   Button button = (Button)editor.getEditor();
				   			editor.dispose();
				   			TableEditor newEditor = new TableEditor(vlanTable);
				   			newEditor.horizontalAlignment = SWT.CENTER;
				   			newEditor.verticalAlignment = SWT.TOP;
				   			newEditor.minimumWidth = 100;
				   			newEditor.minimumHeight = rowHeight-heightSpan;
				   			newEditor.setEditor(button,vlanTable.getItem(vmTagList.indexOf(element)),3); 
				   			cancelButtonMap.put(vm,newEditor);
				   			cancelButtonReverseMap.put(newEditor, vm);
				   			System.out.println("contains");
					   }
					   
				        return "";				
				   }
				}
			}	
			return null;
		}
	}
	
	class SetTagJob extends Job
	{
		private VMTreeObjectVM vmObject;
		private int tag;
		private Display display;
		private Text tagText;
		public SetTagJob(VMTreeObjectVM vmObject,Display display,int tag,Text tagText) {
			super("设置VLAN标示");
			
			this.vmObject = vmObject;
			this.display = display;
			this.tag = tag;
			this.tagText = tagText;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			monitor.beginTask("", 100); 
//			try{
//				VM vm = (VM)vmObject.getApiObject();
//				vm.setTag(objectHost.getConnection(), tag+"");
//			}catch(Exception e){
//				e.printStackTrace();
//			}
			
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	tagText.setText(tag+"");
			        	vlanTable.setFocus();
			        }
			    };
			    this.display.syncExec(runnable); 
			}
			monitor.done();
			return Status.OK_STATUS;
		}
		
	}

}
