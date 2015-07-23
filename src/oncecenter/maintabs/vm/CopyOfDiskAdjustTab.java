package oncecenter.maintabs.vm;

import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import oncecenter.Constants;
import oncecenter.maintabs.OnceVMTabItem;
import oncecenter.p2v.MessageDialog;
import oncecenter.util.ImageRegistry;
import oncecenter.util.MathUtil;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wb.swt.SWTResourceManager;
import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.once.xenapi.Connection;
import com.once.xenapi.SR;
import com.once.xenapi.Types;
import com.once.xenapi.VDI;
import com.once.xenapi.VM;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.XenAPIException;

public class CopyOfDiskAdjustTab extends OnceVMTabItem {

	private VM vm;

	private Composite stateComposite;
	private CLabel statusValue;
	private Label info;
	//private Canvas expandCanvas;
	private Button expand;
	
	private Canvas adjustChartCanvas;
	
	private Table vdiTable;
	private TableViewer tableViewer;
	
	private Image adjustImage;
	private JFreeChart adjustChart;
	private Composite tableComposite;
	private ArrayList<Disk> diskList = new ArrayList<Disk>();
	private Map<Disk,Double> diskTimesMap = new HashMap<Disk,Double>();
	private Map<Disk,Double> diskValueMap = new HashMap<Disk,Double>();
	private Map<Disk,String> diskSelectMap = new HashMap<Disk,String>();
	private Map<Button,Disk> buttonMap = new HashMap<Button,Disk>();
	private Map<Integer,Button> actionMap = new HashMap<Integer,Button>();
	private ArrayList<TableEditor> editorList = new ArrayList<TableEditor>();
	
	public Timer refreshTimer;
	
	ProgressBar proBar;
	
	VMJob expandJob;
	progressBarJob proJob;
	
	public CopyOfDiskAdjustTab(CTabFolder arg0, int arg1, VMTreeObjectVM object) {
		super(arg0, arg1, object);
		setText("资源调整（磁盘大小）");
		this.objectVM = (VMTreeObjectVM)object;
		this.vm = (VM)object.getApiObject();
	}

	public CopyOfDiskAdjustTab(CTabFolder arg0, int arg1, int arg2, VMTreeObjectVM object) {
		super(arg0, arg1, arg2, object);
		setText("资源调整（磁盘大小）");
		this.objectVM = (VMTreeObjectVM)object;
		this.vm = (VM)object.getApiObject();
	}
	
	public boolean Init(){
		getDisks();
		composite = new Composite(folder, SWT.NONE); 
		this.setControl(composite);
		
		composite.setLayout(new GridLayout(1,false));
		composite.setBackground(new Color(null,255,255,255));
		composite.setBackgroundMode(SWT.INHERIT_DEFAULT);  
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		
		stateComposite = new Composite(composite, SWT.NONE);
		stateComposite.setLayout(new GridLayout(2,false));
		{
			Label label1=new Label(stateComposite,SWT.NONE);
			label1.setText("硬盘状态：");
			statusValue = new CLabel(stateComposite, SWT.MULTI);
			
			
			info=new Label(stateComposite,SWT.NONE);
			Composite composite_1 = new Composite(stateComposite, SWT.NONE);
			composite_1.setLayout(new GridLayout(3,false));
			{
				proBar = new ProgressBar(composite_1, SWT.HORIZONTAL| SWT.SMOOTH);  
				proBar.setMinimum(0);  
				proBar.setMaximum(100);  
//				expandCanvas = new Canvas(composite_1, SWT.NONE);
				GridData gd_canvas = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
				gd_canvas.horizontalSpan=1;
				gd_canvas.widthHint = 600;
				gd_canvas.heightHint = 10;
				proBar.setLayoutData(gd_canvas);
//				expandCanvas.setLayoutData(gd_canvas);
//				expandCanvas.setRedraw(true);
//				expandCanvas.setVisible(false);
//				expandCanvas.addPaintListener(new PaintListener(){
//
//					@Override
//					public void paintControl(PaintEvent e) {
//						
//						e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_GRAY));
//						
//						e.gc.drawRoundRectangle(0, 0, 600, 10, 10, 10);
//						e.gc.setAlpha(50);
//						e.gc.setBackground(e.display.getSystemColor(SWT.COLOR_CYAN));
//						e.gc.fillRoundRectangle(0, 0, 600, 10, 10, 10);
//						e.gc.setBackground(e.display.getSystemColor(SWT.COLOR_BLUE));
//						e.gc.fillRoundRectangle(0, 0, (int)(600*progress), 10, 10, 10);
//					}
//				});
				
				Label text = new Label(composite_1, SWT.NONE);
				text.setText("          ");
				
				expand = new Button(composite_1,SWT.NONE);
				expand.setText("一键扩容");
				expand.setFont(SWTResourceManager.getFont("微软雅黑", 15, SWT.NONE));
				GridData g = new GridData();
				g.verticalSpan=2;
				g.verticalAlignment = GridData.BEGINNING;
				//expand.setVisible(false);
				expand.addSelectionListener(new SelectionAdapter() {    
		            public void widgetSelected(SelectionEvent e) {    
		                expand.setText("正在扩容");
				        expand.setEnabled(false);
		                 //进行扩容并刷新进度条，如果扩容结束
		                 Map<VDI,Long> vdiExpandList = new HashMap<VDI,Long>();
		                 for(Disk disk:diskList){
//	                		 if(disk.getUsageValue()*100/disk.getTotalValue()>objectVM.DISK_UPPER_LIMIT)
		                	 if(disk.getTotalValue()<disk.getMaxValue())
	                			 vdiExpandList.put(disk.getVdi(), (long)(disk.getTotalValue()*1.5));
	                	 }
	                	 diskExpand(vdiExpandList);
		             }    
		         }); 
			}
			
			setState();
		}
			
		adjustChartCanvas = new Canvas(composite, SWT.NONE);
		adjustChartCanvas.setLayout(new FillLayout());
		adjustChartCanvas.setLayoutData(new GridData(GridData.FILL_BOTH));
		adjustChartCanvas.addPaintListener(new PaintListener() {
    		public void paintControl(PaintEvent e) {
    			if(adjustImage!=null){
		            e.gc.drawImage(adjustImage, 0, 0);
    			}			
    		}
    	});

		
		
	       
	       tableComposite =  new Composite(composite, SWT.NONE); 
	       tableComposite.setLayout(new GridLayout(1,false));
	       tableComposite.setBackground(new Color(null,255,255,255));
			GridData gd_canvas = new GridData(GridData.FILL_HORIZONTAL);
			gd_canvas.heightHint = 150;
			
			tableComposite.setLayoutData(gd_canvas);
			
	       new Label(tableComposite,SWT.NONE).setText("硬盘具体状态");
	       
	       vdiTable=new Table(tableComposite, SWT.BORDER | SWT.V_SCROLL
			        | SWT.H_SCROLL|SWT.FULL_SELECTION);
			vdiTable.setLayoutData(new GridData(GridData.FILL_BOTH));
			vdiTable.setHeaderVisible(true);
			vdiTable.setLinesVisible(false);
			
			tableViewer = new TableViewer(vdiTable);
			tableViewer.setContentProvider(new ArrayContentProvider());
			tableViewer.setLabelProvider(new TableLabelProvider());
			
			
			TableColumn name = new TableColumn(vdiTable, SWT.CENTER|SWT.BOLD);
			name.setText("名称");
			name.setWidth(200);
			TableColumn size1 = new TableColumn(vdiTable, SWT.CENTER|SWT.BOLD);
			size1.setText("已用空间");
			size1.setWidth(50);
			TableColumn size2 = new TableColumn(vdiTable, SWT.CENTER|SWT.BOLD);
			size2.setText("总空间");
			size2.setWidth(50);
			TableColumn size3 = new TableColumn(vdiTable, SWT.CENTER|SWT.BOLD);
			size3.setText("最大扩充量");
			size3.setWidth(80);
			TableColumn operation = new TableColumn(vdiTable, SWT.CENTER|SWT.BOLD);
			operation.setText("按倍数扩容");
			operation.setWidth(100);
			TableColumn operation1 = new TableColumn(vdiTable, SWT.CENTER|SWT.BOLD);
			operation1.setText("按容量扩容");
			operation1.setWidth(100);
			TableColumn operation4 = new TableColumn(vdiTable,SWT.CENTER|SWT.BOLD);
			operation4.setText("选择扩容方式");
			operation4.setWidth(100);
			
			TableColumn operation3 = new TableColumn(vdiTable, SWT.CENTER|SWT.BOLD);
			operation3.setText("");
			operation3.setWidth(100);
			
			
			refreshTimer = new Timer("RefreshTimer");
			refreshTimer.schedule(new RefreshTimer(PlatformUI.getWorkbench().getDisplay()), 0, 5000);
			objectVM.timerList.add(refreshTimer);
			composite.layout();
		return true;
	}
	
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider,ITableColorProvider,ITableFontProvider {

		
		 @Override
		 public Image getColumnImage(Object element, int columnIndex) {
			  return null;
		 }

		 @Override
		 public String getColumnText(Object element, int columnIndex) {
		  if(element instanceof Disk) {
			  final Disk disk = (Disk) element;
		   
		   switch(columnIndex) {		   
		   case 0:
			   return disk.getUuid();
			   
		   case 1:
			    return disk.getUsageValue()+"G";
			    
		   case 2:
			   return disk.getTotalValue()+"G";
			   
		   case 3:
			   return disk.getMaxValue()+"G";
			   
		   case 4:
		   {
			   TableEditor   editor   =   new   TableEditor(vdiTable);
		        final Combo times  =   new Combo(vdiTable,SWT.NONE); 
		        times.add("1.5倍");
		        times.add("2倍");
		        times.add("3倍");
		        times.select(0); 
		        times.pack();
				editor.horizontalAlignment = SWT.CENTER;
				editor.minimumWidth = times.getSize().x;
		        editor.minimumHeight = vdiTable.getItemHeight();
		        editor.setEditor(times,vdiTable.getItem(diskList.indexOf(disk)),4); 
		        diskTimesMap.put(disk, 1.5);
		        times.addSelectionListener(new SelectionListener(){

					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) {
						
						widgetSelected(arg0);
					}

					@Override
					public void widgetSelected(SelectionEvent arg0) {
						
						String s = times.getText();
						double time = 1.5;
						if(s.contains("倍")){
							time = Double.parseDouble(s.substring(0, s.indexOf("倍")));
						}
						diskTimesMap.put(disk, time);
					}
		        	
		        });
		        editorList.add(editor);
		        return "";
		   }
		        
		   case 5:
		   {
				TableEditor   editor   =   new   TableEditor(vdiTable);
		        final Combo value  =   new Combo(vdiTable,SWT.NONE); 
		        value.add("200G");
		        value.add("400G");
		        value.add("800G");
		        value.select(0);
		        value.pack();
				editor.horizontalAlignment = SWT.CENTER;
				editor.minimumWidth = value.getSize().x;
		        editor.minimumHeight = vdiTable.getItemHeight();
		        editor.setEditor(value,vdiTable.getItem(diskList.indexOf(disk)),5); 
		        diskValueMap.put(disk, 2.0);
		        value.addSelectionListener(new SelectionListener(){

					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) {
						
						widgetSelected(arg0);
					}

					@Override
					public void widgetSelected(SelectionEvent arg0) {
						
						String s = value.getText();
						double addvalue = 2.0;
						if(s.contains("G")){
							addvalue = Double.parseDouble(s.substring(0, s.indexOf("G")));
						}
						diskValueMap.put(disk, addvalue);
					}
		        	
		        });
		        editorList.add(editor);
		        return "";
			}
		   case 6:
		   {
			   TableEditor   editor   =   new   TableEditor(vdiTable);
		        final Combo select  =   new Combo(vdiTable,SWT.NONE); 
		        select.add("按倍数扩容");
		        select.add("按容量扩容");
		        select.select(1);
		        select.pack();
				editor.horizontalAlignment = SWT.CENTER;
				editor.minimumWidth = select.getSize().x;
		        editor.minimumHeight = vdiTable.getItemHeight();
		        editor.setEditor(select,vdiTable.getItem(diskList.indexOf(disk)),6); 
		        diskSelectMap.put(disk, select.getItem(1));
		        select.addSelectionListener(new SelectionListener(){

					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) {
						
						widgetSelected(arg0);
					}

					@Override
					public void widgetSelected(SelectionEvent arg0) {
						
						int index = select.getSelectionIndex();
						diskSelectMap.put(disk, select.getItem(index));
					}
		        	
		        });
		        editorList.add(editor);
		        return "";
		   }
		   case 7:
			{
				TableEditor   editor   =   new   TableEditor(vdiTable);
		        final Button action  =   new Button(vdiTable,SWT.NONE); 
		        action.setEnabled(true);
		        action.setText( "扩容"); 
		        action.pack();
				editor.horizontalAlignment = SWT.CENTER;
				editor.minimumWidth = action.getSize().x;
		        editor.minimumHeight = vdiTable.getItemHeight();
		        editor.setEditor(action,vdiTable.getItem(diskList.indexOf(disk)),7); 
		        buttonMap.put(action, disk);
		        editorList.add(editor);
		        action.addSelectionListener(new SelectionListener(){
				       
					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) {
						
						widgetSelected(arg0);
					}

					@Override
					public void widgetSelected(SelectionEvent arg0) {
						
						buttonMap.get(action);
						Disk seletedDisk = buttonMap.get(action);
						VDI vdi = seletedDisk.getVdi();
						double times;
						double value;
						long expandValue = 0;
						String selectString = diskSelectMap.get(seletedDisk);
						if(selectString.equals("按容量扩容"))
						{
							value = diskValueMap.get(seletedDisk);
							if(disk.getTotalValue()<disk.getMaxValue())
								expandValue = (long)(value + seletedDisk.getTotalValue());
							else
								expandValue = (long) disk.getMaxValue();
						}
						else if(selectString.equals("按倍数扩容"))
						{
							times = diskTimesMap.get(seletedDisk);
							if(disk.getTotalValue()<disk.getMaxValue())
								expandValue = (long)(seletedDisk.getTotalValue()*times);
							else
								expandValue = (long) disk.getMaxValue();
						}
						Map<VDI,Long> vdiExpandList = new HashMap<VDI,Long>();
						vdiExpandList.put(vdi, expandValue);
						actionMap.put(diskList.indexOf(disk), action);
						diskExpand(vdiExpandList);
						action.setEnabled(false);
						action.setTouchEnabled(false);
					}
		        	});
		        return "";
			}
			   
		   }
		  }
		  
		  return null;
		 }

		@Override
		public Color getForeground(Object element, int columnIndex) {
			
			 if(element instanceof Disk) {
				  Disk disk = (Disk) element;
				  if(disk.getUsageValue()/disk.getTotalValue()>objectVM.DISK_UPPER_LIMIT/100)
					  return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
			  }
			return null;
		}

		@Override
		public org.eclipse.swt.graphics.Font getFont(Object element,
				int columnIndex) {
			
			return null;
		}

		@Override
		public Color getBackground(Object element, int columnIndex) {
			
			return null;
		}
	}
	
	public void diskExpand(Map<VDI,Long> vdiExpandList){
		proJob = new progressBarJob(PlatformUI.getWorkbench().getDisplay());
		proJob.schedule();	
		expandJob = new VMJob(PlatformUI.getWorkbench().getDisplay(),vdiExpandList);
		expandJob.schedule();
	}
	class progressBarJob extends Job{
		int second=40;
		public int index=100;
		Display display;
		public progressBarJob(Display display){
			super("");
			this.display=display;
		}
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			monitor.beginTask("", 100); 
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	proBar.setSelection(0); 
			        }
			    };
			    this.display.syncExec(runnable); 
			}
			while(index>0){
				if(!(index==1&&expandJob.getState()==Job.RUNNING))
				{
					index--;
					if (!this.display.isDisposed()){
					    Runnable runnable = new Runnable(){
					        public void run(){
					        	proBar.setSelection(proBar.getSelection() + 1); 
					        }
					    };
					    this.display.syncExec(runnable); 
					}
//					System.out.println("wait 之前"+index+" "+expandJob.getState());
//				     try {
//						this.wait();
//					} catch (InterruptedException e) {
//						
//						e.printStackTrace();
//					} 
				}
				
				try {
					Thread.sleep(second*1000/100);
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				}
				
				
					//try {
						
					//} catch (Exception e1) {
						
					//	e1.printStackTrace();
					//}
			}
				if (!this.display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
				        	proBar.setSelection(100);
				        	expand.setText("扩容完毕");
				        	expand.setEnabled(false);
							expand.pack();
//				        	setState();
							getDisks();
							drawChart();
							addItem(objectVM);
//							expand.setEnabled(false);
//							expand.pack();
				        }
				    };
				    this.display.syncExec(runnable); 
				}
				monitor.done();
			return Status.OK_STATUS;
		}
	}
	
	class VMJob extends Job{
		Display display;
		Map<VDI,Long> vdiExpandList;
		public VMJob(Display display,Map<VDI,Long> vdiExpandList){
			super("给虚拟机扩容..");
			this.display=display;
			this.vdiExpandList=vdiExpandList;
		}
		@Override 
	    protected IStatus run(IProgressMonitor monitor) { 
	        monitor.beginTask("正在扩容 ...", 100); 
	        objectVM.setItemState(ItemState.changing);
	        VMTreeObject parent=objectVM;
	        while(!parent.getName().equals("Xen")){
				VMEvent event=new VMEvent();
				event.setDatetime(new Date());
				event.setDescription("硬盘扩容VM '"+objectVM.getName()+"'");
				event.setTarget(objectVM);
				event.setTask("");
				event.setType(eventType.info);
				event.setUser("root");
				event.setImage(ImageRegistry.getImage(ImageRegistry.INFO));
				objectVM.events.add(event);
				parent=parent.getParent();
			}
	        if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	Constants.treeView.getViewer().refresh();
			        }
			    };
			    this.display.syncExec(runnable); 
			}
	        try {
	        	for(VDI vdi:vdiExpandList.keySet()){
					vdi.setVirtualSize(objectVM.getConnection(),vdiExpandList.get(vdi));
				}
	        	VM.Record record = (VM.Record)objectVM.getRecord();
	        	if(record.powerState.equals(Types.VmPowerState.RUNNING)){
	        		 if (!this.display.isDisposed()){
	     			    Runnable runnable = new Runnable(){
	     			        public void run(){
	     			        	objectVM.shutVM();
	     			        	Constants.treeView.getViewer().refresh();
	     			        }
	     			    };
	     			    this.display.syncExec(runnable); 
	        		 }
	        		 vm.cleanReboot(objectVM.getConnection());
	        		 record = vm.getRecord(objectVM.getConnection());
	        		 objectVM.setItemState(ItemState.able);
	        		 objectVM.setRecord(record);
	        		 if (!this.display.isDisposed()){
	        			 Runnable runnable = new Runnable(){
	        				 public void run(){
	        					 objectVM.startVM();
	        					 Constants.treeView.getViewer().refresh();
	        				 }
	        			 };
	        			 this.display.syncExec(runnable); 
	        		 }
	        	}else{
	        		record = vm.getRecord(objectVM.getConnection());
	        		 objectVM.setItemState(ItemState.able);
	        		 objectVM.setRecord(record);
	        		 if (!this.display.isDisposed()){
	        			 Runnable runnable = new Runnable(){
	        				 public void run(){
	        					 Constants.treeView.getViewer().refresh();
	        				 }
	        			 };
	        			 this.display.syncExec(runnable); 
	        		 }
	        	}
			} catch (Exception e) {
				e.printStackTrace();
				try {
					VM.Record record = vm.getRecord(objectVM.getConnection());
					 objectVM.setRecord(record);
				} catch (Exception e1) {
					
					e1.printStackTrace();
				}
       		 objectVM.setItemState(ItemState.able);
       		 if (!this.display.isDisposed()){
       			 Runnable runnable = new Runnable(){
       				 public void run(){
       					Constants.treeView.getViewer().refresh();
       					int index = vdiTable.getSelectionIndex();
       					actionMap.get(index).setEnabled(true);
       				 }
       			 };
       			 this.display.syncExec(runnable); 
       		 }
			}      
	        
	        proJob.index=-1;
	        monitor.done(); 
	        
	        return Status.OK_STATUS; 
	    } 
	}
	class RefreshTimer extends TimerTask {
		Display display;
		public RefreshTimer(Display display) {
			this.display=display;
		}
		public void run() {
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	setState();
			        	drawChart();
			        	addItem(objectVM);
			        }
			    };
			    this.display.syncExec(runnable); 
			}
		}
	}

	public void setState(){
		if(!expand.getText().equals("正在扩容")){
			expand.setText("一键扩容");
			boolean isExpand=false;
			for(Disk disk:diskList){
				if(disk.getUsageValue()/disk.getTotalValue()>(objectVM.DISK_UPPER_LIMIT/100)){
					isExpand=true;
				}
			}
			if(isExpand){
				statusValue.setImage(ImageRegistry.getImage(ImageRegistry.DISABLE));
				statusValue.setText("危险！急需进行扩容");
				//expandCanvas.setVisible(true);
				//expand.setVisible(true);
				expand.setEnabled(true);
			}else{
				statusValue.setImage(ImageRegistry.getImage(ImageRegistry.FREE));
				statusValue.setText("正常");
				//expandCanvas.setVisible(false);
				//2013-1-29 改一键扩容为可使用
				expand.setEnabled(true);
			}
		}
	}

	public void drawChart(){
		adjustChart = ChartFactory.createBarChart3D(
				"硬盘信息总览",//图表标题 
				"硬盘容量信息", 
				"容量", 
				getAdjustDataSet(), 
				PlotOrientation.HORIZONTAL, 
				true, true, true);
		//设置背景色
		adjustChart.setBackgroundPaint(ChartColor.WHITE);
		CategoryPlot adjustPlot = adjustChart.getCategoryPlot();
		adjustPlot.setBackgroundPaint(ChartColor.WHITE);
		org.jfree.chart.axis.CategoryAxis adjustDomainAxis = adjustPlot.getDomainAxis();
		org.jfree.chart.axis.NumberAxis adjustNumberAxis = (NumberAxis) adjustPlot.getRangeAxis();
		adjustDomainAxis.setLowerMargin(0.1);//设置距离图片左端距离此时为10%
		adjustDomainAxis.setUpperMargin(0.1);//设置距离图片右端距离此时为百分之10
		adjustDomainAxis.setCategoryLabelPositionOffset(10);//图表横轴与标签的距离(10像素)
		adjustDomainAxis.setCategoryMargin(0.2);//横轴标签之间的距离20%
		//解决中文乱码问题
		TextTitle adjustTitle = adjustChart.getTitle();
		adjustTitle.setFont(new Font("黑体",Font.PLAIN,20));
		adjustDomainAxis.setTickLabelFont(new Font("sans-serif", Font.PLAIN, 11));
		adjustDomainAxis.setLabelFont(new Font("宋体", Font.PLAIN, 12));
		adjustNumberAxis.setTickLabelFont(new Font("sans-serif", Font.PLAIN, 12));
		adjustNumberAxis.setLabelFont(new Font("黑体", Font.PLAIN, 12));
		adjustChart.getLegend().setItemFont(new Font("宋体", Font.PLAIN, 12));
		//设定柱子的属性
		org.jfree.chart.axis.ValueAxis adjustRangeAxis = adjustPlot.getRangeAxis();
		adjustRangeAxis.setUpperMargin(0.2);//设置最高的一个柱与图片顶端的距离(最高柱的10%)
		
		//设置图表的颜色
		org.jfree.chart.renderer.category.BarRenderer3D adjustRenderer;
		adjustRenderer = new org.jfree.chart.renderer.category.BarRenderer3D();
		adjustRenderer.setItemMargin(0.1);//组内柱子间隔为组宽的10%
		//显示每个柱的数值，并修改该数值的字体属性
		adjustRenderer.setItemLabelGenerator(new StandardCategoryItemLabelGenerator());
		adjustRenderer.setItemLabelFont(new Font("黑体",Font.BOLD,12));//12号黑体加粗
		adjustRenderer.setItemLabelsVisible(true);
		adjustRenderer.setWallPaint(ChartColor.WHITE);
		adjustPlot.setRenderer(adjustRenderer);//使用我们设计的效果
		//设置纵横坐标的显示位置

		adjustPlot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_LEFT);//学校显示在下端(柱子竖直)或左侧(柱子水平)
		adjustPlot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT); //人数显示在下端(柱子水平)或左侧(柱子竖直)
		
	       
	       byte[] adjustBuf = null;
	       ByteArrayOutputStream os = new ByteArrayOutputStream();
	       try {
	    	   if(adjustChartCanvas.getSize().x + adjustChartCanvas.getSize().y == 0){
	    		   ChartUtilities.writeChartAsPNG(os, adjustChart, folder.getSize().x-18,(folder.getSize().y)/2);
	    	   }else{
	    		   ChartUtilities.writeChartAsPNG(os, adjustChart, adjustChartCanvas.getSize().x, adjustChartCanvas.getSize().y);
	    	   }
	    	   adjustBuf=os.toByteArray();
				os.close();
			} catch (IOException e1) {
				
				e1.printStackTrace();
			}
	       ByteArrayInputStream adjustBin=new ByteArrayInputStream(adjustBuf);
	       adjustImage = null;
	       adjustImage = new Image(PlatformUI.getWorkbench().getDisplay(), adjustBin);
	       adjustChartCanvas.redraw();
	}
	
	public void getDisks(){
		diskList.clear();
		Connection c = objectVM.getConnection();
		try{
			for(VDI v : VDI.getByVM(c, vm))
			{
				VDI.Record r = v.getRecord(c);
				Disk disk = new Disk();
				disk.setUuid(r.uuid);
				disk.setLocation(r.location);
				disk.setNameDescription(r.nameDescription);
				disk.setNameLabel(r.nameLabel);
				disk.setTotalValue(MathUtil.RoundingDouble(((double)r.virtualSize)/1024.0/1024.0/1024.0, 2));
				disk.setUsageValue(MathUtil.RoundingDouble(((double)r.physicalUtilisation)/1024.0/1024.0/1024.0, 2));
				disk.setAvailableSpace(disk.getTotalValue()-disk.getUsageValue());
				disk.setVdi(v);
				diskList.add(disk);
				SR sr = v.getSR(objectVM.getConnection());
				double maxValue = (double)(sr.getPhysicalSize(objectVM.getConnection()) - sr.getPhysicalUtilisation(objectVM.getConnection()));
				disk.setMaxValue(MathUtil.RoundingDouble(maxValue/1024.0/1024.0/1024.0, 2));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void addItem(VMTreeObject treeObject) {
		for(TableEditor editor:editorList){
			editor.dispose();
		}
		tableViewer.setInput(diskList);
//		vdiTable.removeAll();
//		for(Disk disk:diskList){
//			TableItem item = new TableItem(vdiTable,SWT.NONE);
//			
//			item.setText(0, disk.getUuid());
//			item.setText(1, disk.getUsageValue()+"G");
//			item.setText(2, disk.getTotalValue()+"G");
//			if(disk.getUsageValue()/disk.getTotalValue()>0.8){
//				item.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
//			}
//			{
//				TableEditor   editor   =   new   TableEditor(vdiTable);
//		        Combo times  =   new Combo(vdiTable,SWT.NONE); 
//		        times.add("1.5倍");
//		        times.add("2倍");
//		        times.add("3倍");
//		        times.setText( "1.5倍"); 
//		        times.pack();
//				editor.horizontalAlignment = SWT.CENTER;
//				editor.minimumWidth = times.getSize().x;
//		        editor.minimumHeight = vdiTable.getItemHeight();
//		        editor.setEditor(times,item,3); 
//		        diskTimesMap.put(disk, editor);
//		        editorList.add(editor);
//			}
//			{
//				TableEditor   editor   =   new   TableEditor(vdiTable);
//		        Combo value  =   new Combo(vdiTable,SWT.NONE); 
//		        value.add("2G");
//		        value.add("4G");
//		        value.add("8G");
//		        value.setText( "2G"); 
//		        value.pack();
//				editor.horizontalAlignment = SWT.CENTER;
//				editor.minimumWidth = value.getSize().x;
//		        editor.minimumHeight = vdiTable.getItemHeight();
//		        editor.setEditor(value,item,4); 
//		        diskValueMap.put(disk, editor);
//		        editorList.add(editor);
//			}
//			{
//				TableEditor   editor   =   new   TableEditor(vdiTable);
//		        Button action  =   new Button(vdiTable,SWT.NONE); 
//		        action.setText( "扩容"); 
//		        action.pack();
//				editor.horizontalAlignment = SWT.CENTER;
//				editor.minimumWidth = action.getSize().x;
//		        editor.minimumHeight = vdiTable.getItemHeight();
//		        editor.setEditor(action,item,5); 
//		        editorList.add(editor);
//		        action.addSelectionListener(new SelectionListener(){
//
//					@Override
//					public void widgetDefaultSelected(SelectionEvent arg0) {
//						
//						widgetSelected(arg0);
//					}
//
//					@Override
//					public void widgetSelected(SelectionEvent arg0) {
//						
//						int index = vdiTable.getSelectionIndex();
//						Disk seletedDisk = diskList.get(index);
//						String timesString = ((Combo)(diskTimesMap.get(seletedDisk).getEditor())).getText();
//						String valueString = ((Combo)(diskValueMap.get(seletedDisk).getEditor())).getText();
//						int times;
//						int value;
//						if(timesString.indexOf("倍")==-1){
//							times = Integer.parseInt(timesString);
//						}else{
//							times = Integer.parseInt(timesString.substring(0, timesString.indexOf("倍")-1));
//						}
//						if(timesString.indexOf("G")==-1){
//							value = Integer.parseInt(valueString);
//						}else{
//							value = Integer.parseInt(valueString.substring(0, valueString.indexOf("G")-1));
//						}
//						VDI vdi = seletedDisk.getVdi();
//						
//						
//						try {
//							vdi.setVirtualSize(object.getConnection(), (long)(seletedDisk.getTotalValue()*times));
//						} catch (BadServerResponse e) {
//							
//							e.printStackTrace();
//						} catch (XenAPIException e) {
//							
//							e.printStackTrace();
//						} catch (XmlRpcException e) {
//							
//							e.printStackTrace();
//						}
//						
//						RebootAction action = new RebootAction(object);
//						action.run();
//						setState();
//						getDisks();
//						drawChart();
//						addItem(object);
//					}
//		        });
//			}
//		}
//		for(int i=0;i<7;i++){
//			vdiTable.getColumn(i).pack();
//		}

	}

	private CategoryDataset getAdjustDataSet()
	{
		 DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		 for(Disk disk:diskList){
			 dataset.addValue(disk.getUsageValue(), "已使用量", disk.getNameLabel());
			 dataset.addValue(disk.getTotalValue(), "总量", disk.getNameLabel());
			 
			 dataset.addValue(disk.getMaxValue(), "最大扩容量", disk.getNameLabel());
		 }
		 return dataset;
	}
	
	class Disk
	{
		private String uuid;
		private String nameLabel;
		private String nameDescription;
		private String location;
		private double totalValue;
		private double usageValue;
		private double availableSpace;
		private VDI vdi;
		private double maxValue;
		public String getUuid() {
			return uuid;
		}
		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
		public String getNameLabel() {
			return nameLabel;
		}
		public void setNameLabel(String nameLabel) {
			this.nameLabel = nameLabel;
		}
		public String getNameDescription() {
			return nameDescription;
		}
		public void setNameDescription(String nameDescription) {
			this.nameDescription = nameDescription;
		}
		public String getLocation() {
			return location;
		}
		public void setLocation(String location) {
			this.location = location;
		}
		public double getTotalValue() {
			return totalValue;
		}
		public void setTotalValue(double totalValue) {
			this.totalValue = totalValue;
		}
		public double getUsageValue() {
			return usageValue;
		}
		public void setUsageValue(double usageValue) {
			this.usageValue = usageValue;
		}
		public double getAvailableSpace() {
			return availableSpace;
		}
		public void setAvailableSpace(double availableSpace) {
			this.availableSpace = availableSpace;
		}
		public VDI getVdi() {
			return vdi;
		}
		public void setVdi(VDI vdi) {
			this.vdi = vdi;
		}
		public double getMaxValue() {
			return maxValue;
		}
		public void setMaxValue(double maxValue) {
			this.maxValue = maxValue;
		}
	}
}
