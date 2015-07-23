package oncecenter.maintabs.vm;

import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import oncecenter.Constants;
import oncecenter.maintabs.OnceVMTabItem;
import oncecenter.util.ImageRegistry;
import oncecenter.util.MathUtil;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;
import oncecenter.wizard.adjustvmdisk.AdjustDiskWizard;
import oncecenter.wizard.managedisk.AddVMDiskWizard;
import oncecenter.wizard.newvmfromtemp.NewVMDialog;

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
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
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
import com.once.xenapi.VDI;
import com.once.xenapi.VM;

public class DiskAdjustTab extends OnceVMTabItem {

	private VM vm;

	private Composite stateComposite;
	private CLabel statusValue;
	//private Canvas expandCanvas;
	public Button autoExpand;
	private Button normalExpand;
	private Button manageDisk;
	
	private Canvas adjustChartCanvas;
	
	private Table vdiTable;
	private TableViewer tableViewer;
	private Composite tableComposite;
	
	private Image adjustImage;
	private JFreeChart adjustChart;
	private ArrayList<Disk> diskList = new ArrayList<Disk>();
	
	public Timer refreshTimer;
	private String afterExpandMessage = "���ݳɹ����������������Ч";
	
	VMJob expandJob;
	
	public DiskAdjustTab(CTabFolder arg0, int arg1, VMTreeObjectVM object) {
		super(arg0, arg1, object);
		setText("��Դ���������̴�С��");
		this.objectVM = (VMTreeObjectVM)object;
		this.vm = (VM)object.getApiObject();
	}

	public DiskAdjustTab(CTabFolder arg0, int arg1, int arg2, VMTreeObjectVM object) {
		super(arg0, arg1, arg2, object);
		setText("��Դ���������̴�С��");
		this.objectVM = (VMTreeObjectVM)object;
		this.vm = (VM)object.getApiObject();
	}
	
	public boolean Init(){
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
			label1.setText("Ӳ��״̬��");
			statusValue = new CLabel(stateComposite, SWT.MULTI);
			
			Composite composite_1 = new Composite(stateComposite, SWT.NONE);
			composite_1.setLayout(new GridLayout(3,false));
			{				
				autoExpand = new Button(composite_1,SWT.NONE);
				autoExpand.setText("һ������");
				autoExpand.setEnabled(false);
				autoExpand.addSelectionListener(new SelectionAdapter() {    
		            public void widgetSelected(SelectionEvent e) {    
		                autoExpand.setText("��������");
				        autoExpand.setEnabled(false);
		                 Map<VDI,Long> vdiExpandList = new HashMap<VDI,Long>();
		                 for(Disk disk:diskList) {
//	                		 if(disk.getUsageValue()*100/disk.getTotalValue()>objectVM.DISK_UPPER_LIMIT && disk.getUsageValue()*100/disk.getTotalValue()<100)
	                		 if(disk.getTotalValue()*1.5 < disk.getMaxValue()){	                			 
	                			 vdiExpandList.put(disk.getVdi(), (long)(disk.getTotalValue()*1.5));
	                		 }
	                		 else{
	                			 afterExpandMessage = "���ݺ�ᳬ������������ƣ����ʵ����������";
	                			 vdiExpandList.put(disk.getVdi(), (long) 0);
	                		 }
	                	 }
	                	 diskExpand(vdiExpandList);
		             }    
		         }); 
				normalExpand = new Button(composite_1,SWT.NONE);
				normalExpand.setText("�ֶ�����");
				normalExpand.setEnabled(false);
				
				normalExpand.addSelectionListener(new SelectionAdapter() {    
		            public void widgetSelected(SelectionEvent e) {
		            	AdjustDiskWizard wizard = new AdjustDiskWizard(diskList,objectVM,DiskAdjustTab.this);
		            	NewVMDialog dialog = new NewVMDialog(Display.getCurrent().getActiveShell(),
		                         wizard);
		            	dialog.setPageSize(400, 380);
		            	dialog.create();
		            	dialog.open();
		             }    
		         }); 
				manageDisk = new Button(composite_1,SWT.NONE);
				manageDisk.setText("�������");
				manageDisk.setEnabled(false);
				
				manageDisk.addSelectionListener(new SelectionAdapter() {    
		            public void widgetSelected(SelectionEvent e) {
		            	manageDisk.setEnabled(false);
		            	AddVMDiskWizard wizard = new AddVMDiskWizard(objectVM);
		            	NewVMDialog dialog = new NewVMDialog(Display.getCurrent().getActiveShell(),
		                         wizard);
		            	dialog.setPageSize(400, 380);
		            	dialog.create();
		            	dialog.open();
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

		{
			tableComposite =  new Composite(composite, SWT.NONE); 
			tableComposite.setLayout(new GridLayout(1,false));
			tableComposite.setBackground(new Color(null,255,255,255));
			GridData gd_canvas = new GridData(GridData.FILL_HORIZONTAL);
			gd_canvas.heightHint = 150;
				
			tableComposite.setLayoutData(gd_canvas);
				
			new Label(tableComposite,SWT.NONE).setText("Ӳ�̾���״̬");
		       
			vdiTable=new Table(tableComposite, SWT.BORDER | SWT.V_SCROLL
				        | SWT.H_SCROLL|SWT.FULL_SELECTION);
			vdiTable.setLayoutData(new GridData(GridData.FILL_BOTH));
			vdiTable.setHeaderVisible(true);
			vdiTable.setLinesVisible(false);
				
			tableViewer = new TableViewer(vdiTable);
			tableViewer.setContentProvider(new ArrayContentProvider());
			tableViewer.setLabelProvider(new TableLabelProvider());
				
				
			TableColumn picture = new TableColumn(vdiTable, SWT.CENTER|SWT.BOLD);
			picture.setText("");
			picture.setWidth(50);
			TableColumn name = new TableColumn(vdiTable, SWT.CENTER|SWT.BOLD);
			name.setText("����");
			name.setWidth(300);
			TableColumn size1 = new TableColumn(vdiTable, SWT.CENTER|SWT.BOLD);
			size1.setText("���ÿռ�");
			size1.setWidth(100);
			TableColumn size2 = new TableColumn(vdiTable, SWT.CENTER|SWT.BOLD);
			size2.setText("�ܿռ�");
			size2.setWidth(100);
			TableColumn size3 = new TableColumn(vdiTable, SWT.CENTER|SWT.BOLD);
			size3.setText("���������");
			size3.setWidth(100);
				
		}
		drawChart();
		refreshTimer = new Timer("RefreshTimer");
		refreshTimer.schedule(new RefreshTimer(PlatformUI.getWorkbench().getDisplay()), 0, 10000);
		objectVM.timerList.add(refreshTimer);
		composite.layout();		
			
		return true;
	}
	
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider,ITableColorProvider,ITableFontProvider {

		
		 @Override
		 public Image getColumnImage(Object element, int columnIndex) {
			 if(element instanceof Disk) {
				   switch(columnIndex) {
				   case 0:
						return ImageRegistry.getImage(ImageRegistry.STORAGE);
				   }
				  }
				  return null;
		 }

		 @Override
		 public String getColumnText(Object element, int columnIndex) {
		  if(element instanceof Disk) {
			  final Disk disk = (Disk) element;
		   
		   switch(columnIndex) {	
		   case 0:
			   return "";
		   case 1:
			   return disk.getUuid();
			   
		   case 2:
			    return disk.getUsageValue()+"G";
			    
		   case 3:
			   return disk.getTotalValue()+"G";
			   
		   case 4:
			   return disk.getMaxValue()+"G";
			   
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
		expandJob = new VMJob(PlatformUI.getWorkbench().getDisplay(),vdiExpandList);
		expandJob.schedule();
	}

	class VMJob extends Job{
		Display display;
		Map<VDI,Long> vdiExpandList;
		public VMJob(Display display,Map<VDI,Long> vdiExpandList){
			super("�����������..");
			this.display=display;
			this.vdiExpandList=vdiExpandList;
		}
		@Override 
	    protected IStatus run(IProgressMonitor monitor) { 
	        monitor.beginTask("�������� ...", 100); 
	        objectVM.setItemState(ItemState.changing);
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
	        	if(getDisks()){
	        		if (!this.display.isDisposed()){
		      			 Runnable runnable = new Runnable(){
		      				 public void run(){
		      					MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
		      					messageBox.setText("��ʾ");
		      					messageBox.setMessage(afterExpandMessage);
		      					messageBox.open();
		      				 }
		      			 };
		      			 this.display.syncExec(runnable); 
		      		 }
	        	}else{
	        		throw new Exception();
	        	}
			} catch (Exception e) {
				e.printStackTrace();
				 if (!this.display.isDisposed()){
	      			 Runnable runnable = new Runnable(){
	      				 public void run(){
	      					MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
	      					messageBox.setText("����");
	      					messageBox.setMessage("����ʧ�ܣ�");
	      					messageBox.open();
	      				 }
	      			 };
	      			 this.display.syncExec(runnable); 
	      		 }
			}      
	        objectVM.setItemState(ItemState.able);
      		 if (!this.display.isDisposed()){
      			 Runnable runnable = new Runnable(){
      				 public void run(){
      					autoExpand.setText("�������");
      					autoExpand.setEnabled(true);
      					autoExpand.pack();
      					drawChart();
      					addItem(objectVM);
      					Constants.treeView.getViewer().refresh();
      				 }
      			 };
      			 this.display.syncExec(runnable); 
      		 }
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
			if(getDisks()){
				if (!this.display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
				        	drawChart();
				        	addItem(objectVM);
				        	setState();
				        	autoExpand.setEnabled(true);
				        	normalExpand.setEnabled(true);
				        	manageDisk.setEnabled(true);
				        }
				    };
				    this.display.syncExec(runnable); 
				}
			}
		}
	}

	public void setState(){
		if(!autoExpand.getText().equals("��������")){
			autoExpand.setText("һ������");
			boolean isExpand=false;
			for(Disk disk:diskList){
				if(disk.getUsageValue()/disk.getTotalValue()>(objectVM.DISK_UPPER_LIMIT/100)){
					isExpand=true;
				}
			}
			if(isExpand){
				statusValue.setImage(ImageRegistry.getImage(ImageRegistry.DISABLE));
				statusValue.setText("Σ�գ������������");
				//expandCanvas.setVisible(true);
				//expand.setVisible(true);
				//autoExpand.setEnabled(true);
			}else{
				statusValue.setImage(ImageRegistry.getImage(ImageRegistry.FREE));
				statusValue.setText("����");
				//expandCanvas.setVisible(false);
				//2013-1-29 ��һ������Ϊ��ʹ��
				autoExpand.setEnabled(true);
			}
		}
	}

	public void drawChart(){
		adjustChart = ChartFactory.createBarChart3D(
				"Ӳ����Ϣ����",//ͼ����� 
				"Ӳ��������Ϣ", 
				"����", 
				getAdjustDataSet(), 
				PlotOrientation.HORIZONTAL, 
				true, true, true);
		//���ñ���ɫ
		adjustChart.setBackgroundPaint(ChartColor.WHITE);
		CategoryPlot adjustPlot = adjustChart.getCategoryPlot();
		adjustPlot.setBackgroundPaint(ChartColor.WHITE);
		org.jfree.chart.axis.CategoryAxis adjustDomainAxis = adjustPlot.getDomainAxis();
		org.jfree.chart.axis.NumberAxis adjustNumberAxis = (NumberAxis) adjustPlot.getRangeAxis();
		adjustDomainAxis.setLowerMargin(0.1);//���þ���ͼƬ��˾����ʱΪ10%
		adjustDomainAxis.setUpperMargin(0.1);//���þ���ͼƬ�Ҷ˾����ʱΪ�ٷ�֮10
		adjustDomainAxis.setCategoryLabelPositionOffset(10);//ͼ��������ǩ�ľ���(10����)
		adjustDomainAxis.setCategoryMargin(0.2);//�����ǩ֮��ľ���20%
		//���������������
		TextTitle adjustTitle = adjustChart.getTitle();
		adjustTitle.setFont(new Font("����",Font.PLAIN,20));
		adjustDomainAxis.setTickLabelFont(new Font("sans-serif", Font.PLAIN, 11));
		adjustDomainAxis.setLabelFont(new Font("����", Font.PLAIN, 12));
		adjustNumberAxis.setTickLabelFont(new Font("sans-serif", Font.PLAIN, 12));
		adjustNumberAxis.setLabelFont(new Font("����", Font.PLAIN, 12));
		adjustChart.getLegend().setItemFont(new Font("����", Font.PLAIN, 12));
		//�趨���ӵ�����
		org.jfree.chart.axis.ValueAxis adjustRangeAxis = adjustPlot.getRangeAxis();
		adjustRangeAxis.setUpperMargin(0.2);//������ߵ�һ������ͼƬ���˵ľ���(�������10%)
		
		//����ͼ�����ɫ
		org.jfree.chart.renderer.category.BarRenderer3D adjustRenderer;
		adjustRenderer = new org.jfree.chart.renderer.category.BarRenderer3D();
		adjustRenderer.setItemMargin(0.1);//�������Ӽ��Ϊ����10%
		//��ʾÿ��������ֵ�����޸ĸ���ֵ����������
		adjustRenderer.setItemLabelGenerator(new StandardCategoryItemLabelGenerator());
		adjustRenderer.setItemLabelFont(new Font("����",Font.BOLD,12));//12�ź���Ӵ�
		adjustRenderer.setItemLabelsVisible(true);
		adjustRenderer.setWallPaint(ChartColor.WHITE);
		adjustPlot.setRenderer(adjustRenderer);//ʹ��������Ƶ�Ч��
		//�����ݺ��������ʾλ��

		adjustPlot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_LEFT);//ѧУ��ʾ���¶�(������ֱ)�����(����ˮƽ)
		adjustPlot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT); //������ʾ���¶�(����ˮƽ)�����(������ֱ)
		
	       
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
	
	public boolean getDisks(){
		//diskList.clear();
		Connection c = objectVM.getConnection();
		try{
			for(VDI v : VDI.getByVM(c, vm))
			{
				VDI.Record r = v.getRecord(c);
				boolean isExist=false;
				Disk disk = null;
				for(Disk d:diskList){
					if(d.uuid.equals(r.uuid)){
						isExist = true;
						disk = d;
						break;
					}
				}
				if(!isExist){
					disk = new Disk();
					disk.setUuid(r.uuid);
					diskList.add(disk);
				}
				disk.setLocation(r.location);
				disk.setNameDescription(r.nameDescription);
				disk.setNameLabel(r.nameLabel);
				disk.setTotalValue(MathUtil.RoundingDouble(((double)r.virtualSize)/1024.0/1024.0/1024.0, 2));
				disk.setUsageValue(MathUtil.RoundingDouble(((double)r.physicalUtilisation)/1024.0/1024.0/1024.0, 2));
				disk.setAvailableSpace(disk.getTotalValue()-disk.getUsageValue());
				disk.setVdi(v);
				SR sr = v.getSR(objectVM.getConnection());
				double maxValue = (double)(sr.getPhysicalSize(objectVM.getConnection()) - sr.getPhysicalUtilisation(objectVM.getConnection()));
				disk.setMaxValue(MathUtil.RoundingDouble(maxValue/1024.0/1024.0/1024.0, 2));
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void addItem(VMTreeObject treeObject) {
		tableViewer.setInput(diskList);
	}

	private CategoryDataset getAdjustDataSet()
	{
		 DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		 for(Disk disk:diskList){
			 dataset.addValue(disk.getUsageValue(), "��ʹ����", "");
			 dataset.addValue(disk.getTotalValue(), "����", "");
			 dataset.addValue(disk.getMaxValue(), "���������", "");
		 }
		 return dataset;
	}
	
	public class Disk
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
