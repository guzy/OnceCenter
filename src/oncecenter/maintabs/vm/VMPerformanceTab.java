package oncecenter.maintabs.vm;

import java.awt.BasicStroke;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import oncecenter.maintabs.OnceVMTabItem;
import oncecenter.util.MathUtil;
import oncecenter.util.performance.drawchart.DrawVmPerformance;
import oncecenter.util.performance.drawchart.drawCPU;
import oncecenter.util.performance.drawchart.drawDisk;
import oncecenter.util.performance.drawchart.drawMemory;
import oncecenter.util.performance.drawchart.drawNet;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wb.swt.SWTResourceManager;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.once.xenapi.Host;
import com.once.xenapi.VM;

public class VMPerformanceTab extends OnceVMTabItem {
	
	private VM vm;
	
	Image image;
	Canvas chartCanvas;
	Table legendTable;
	JFreeChart chart;

	int focus=Integer.MAX_VALUE;
	TimeSeriesCollection timeSeriesCollection;
	
	Label title;
	Label starttime;
	Label endtime;
	Combo chartType;
	Combo jiange;
	Button openPerformance;
	
	Label refresh;
	
	TableViewer tableViewer;
	
	Composite tableComposite;
	
	public Timer refreshPerformTimer;
	
	String innerText = null;
	String innerMessage = null;
//	VMTreeObjectHost objectHost = (VMTreeObjectHost) objectVM.getParent();
//	Host host = (Host)objectVM.getParent().getApiObject();
//	boolean isPerformanceRecClosed = host.isPerformanceRecClosed();
	boolean isPerformanceRecClosed = false;
	
	public VMPerformanceTab(CTabFolder arg0, int arg1, VMTreeObjectVM object) {
		super(arg0, arg1, object);
		setText("性能");
		this.vm = (VM)object.getApiObject();
		this.objectVM = object;
	}

	public VMPerformanceTab(CTabFolder arg0, int arg1, int arg2, VMTreeObjectVM object) {
		super(arg0, arg1, arg2, object);
		setText("性能");
		this.vm = (VM)object.getApiObject();
		this.objectVM = object;
	}
	
	public boolean Init(){
		composite = new Composite(folder, SWT.NONE); 
		composite.setLayout(new GridLayout(1,false));
		composite.setBackground(new Color(null,255,255,255));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setBackgroundMode(SWT.INHERIT_DEFAULT);  
		setControl(composite);
		
		Composite titleComposite =  new Composite(composite, SWT.NONE); 
		titleComposite.setLayout(new GridLayout(9,false));
		titleComposite.setBackground(new Color(null,255,255,255));
		titleComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		title = new Label(titleComposite,SWT.NONE);
		title.setText("CPU/实时，");
		title.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
		
		starttime = new Label(titleComposite,SWT.NONE);
		starttime.setText("0000/00/00 00:00:00 ");
		starttime.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
		
		new Label(titleComposite,SWT.NONE).setText("-");
		
		endtime = new Label(titleComposite,SWT.NONE);
		endtime.setText(" 0000/00/00 00:00:00 ");
		endtime.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
		
		Label space = new Label(titleComposite,SWT.NONE);
		space.setText(" ");
		space.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		new Label(titleComposite,SWT.NONE).setText("时间间隔：");
		
		jiange = new Combo(titleComposite,SWT.DROP_DOWN);
		jiange.add("15秒", 0);
		jiange.add("30分钟",1);
		jiange.add("8小时",2);
		jiange.setText("15秒");
//		chartType.add("磁盘",3);
//		chartType.setText("CPU");
//		chartType.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				title.setText(chartType.getText()+"/实时，");
//				refreshView();
//			}
//		});
		
		new Label(titleComposite,SWT.NONE).setText("切换到：");
		
		chartType = new Combo(titleComposite,SWT.DROP_DOWN);
		chartType.add("CPU", 0);
		chartType.add("内存",1);
		chartType.add("网络",2);
		chartType.add("磁盘",3);
		chartType.setText("CPU");
		chartType.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				title.setText(chartType.getText()+"/实时，");
				refreshView();
			}
		});
		
//		new Label(titleComposite,SWT.NONE).setText("  ");
//		{
//			final CLabel lblNewLabel_1 = new CLabel(titleComposite, SWT.NONE);
//			lblNewLabel_1.addMouseListener(new MouseAdapter() {
//				@Override
//				public void mouseDown(MouseEvent arg0) {
//					NewServerAction action=new NewServerAction();
//					action.run();
//				}
//			});
//			lblNewLabel_1.setImage(ImageRegistry.getImage(ImageRegistry.PRINT));
//		}
		
//		{
//			final CLabel lblNewLabel_1 = new CLabel(titleComposite, SWT.NONE);
//			lblNewLabel_1.addMouseListener(new MouseAdapter() {
//				@Override
//				public void mouseDown(MouseEvent arg0) {
////					String uuid="1fd71485-80ac-22df-5521-fa7ebe7e7004";
////
////		        	HashMap<String, double[]> metricsHostTimelines = FetchHostPerformance.getMetricsTimelines(
////		    				"E:/xen1/15sec.xml"
////		    				,uuid);
////		    		
////		    		DrawHostPerformance.drawHost(metricsHostTimelines, 
////		    				FetchHostPerformance.endTime, 
////		    				FetchHostPerformance.totalMemory, 
////		    				uuid);
////		    		
////		    		refreshView();
//				}
//			});
//			lblNewLabel_1.setImage(ImageRegistry.getImage(ImageRegistry.REFRESH));
//		}
//		{
//			final CLabel lblNewLabel_1 = new CLabel(titleComposite, SWT.NONE);
//			lblNewLabel_1.addMouseListener(new MouseAdapter() {
//				@Override
//				public void mouseDown(MouseEvent arg0) {
//					NewServerAction action=new NewServerAction();
//					action.run();
//				}
//			});
//			lblNewLabel_1.setImage(ImageRegistry.getImage(ImageRegistry.SAVE));
//		}
		refresh = new Label(titleComposite,SWT.NONE);
		refresh.setText("每 15 秒刷新一次图形");
		refresh.setFont(SWTResourceManager.getFont("微软雅黑", 7, SWT.NONE));
		refresh.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		new Label(titleComposite,SWT.NONE);
		new Label(titleComposite,SWT.NONE);
		new Label(titleComposite,SWT.NONE);
		new Label(titleComposite,SWT.NONE);
		/*openPerformance = new Button(titleComposite, SWT.NONE);
    	if (isPerformanceRecClosed == true) {
			innerText = "打开记录开关";			
		} else {
			innerText = "关闭记录开关";			
		}
		openPerformance.setText(innerText);
		openPerformance.setEnabled(true);		
		openPerformance.addSelectionListener(new SelectionAdapter() {    
            public void widgetSelected(SelectionEvent e) {
            	openPerformance.setEnabled(false);
            	MessageBox messageBox = new MessageBox(new Shell(), SWT.YES | SWT.NO); 
    			messageBox.setText("提示");
    			if (innerText == "打开记录开关") {
    				innerMessage = "记录虚拟机性能数据会影响性能，是否开启？";
    			} else {
    				innerMessage = "停止记录虚拟机性能数据？";
    			}
    			messageBox.setMessage(innerMessage);
    			int choice = messageBox.open();
    			if(choice ==  SWT.YES){
    				OpenPerformanceJob job = new OpenPerformanceJob(PlatformUI.getWorkbench().getDisplay());
	        		job.schedule();	        		
    			}else{
    				openPerformance.setEnabled(true);
    			}
             }    
         });
         */ 		
		chartCanvas = new Canvas(composite, SWT.NONE); 
		chartCanvas.setLayout(new FillLayout());
		chartCanvas.setLayoutData(new GridData(GridData.FILL_BOTH));
		chartCanvas.addPaintListener(new PaintListener() {
    		public void paintControl(PaintEvent e) {
    			if(image!=null){
		            e.gc.drawImage(image, 0, 0);
    			}			
    			
    		}
    	});
		
		tableComposite =  new Composite(composite, SWT.NONE); 
		tableComposite.setLayout(new GridLayout(1,false));
		tableComposite.setBackground(new Color(null,255,255,255));
		GridData gd_canvas = new GridData(GridData.FILL_HORIZONTAL);
		gd_canvas.heightHint = 150;
		tableComposite.setLayoutData(gd_canvas);
		
		Label legendTitle = new Label(tableComposite,SWT.NONE);
		legendTitle.setText("性能图表图例");
		legendTitle.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
		
		legendTable=new Table(tableComposite,SWT.BORDER | SWT.V_SCROLL
		        | SWT.H_SCROLL|SWT.FULL_SELECTION);
		legendTable.setLayout(new FillLayout());
		legendTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		legendTable.setHeaderVisible(true);
		
		TableColumn item = new TableColumn(legendTable, SWT.CENTER|SWT.BOLD);
		item.setText("项");
		item.setWidth(50);
		TableColumn object = new TableColumn(legendTable, SWT.CENTER|SWT.BOLD);
		object.setText("对象");
		object.setWidth(100);
		TableColumn  measure= new TableColumn(legendTable, SWT.CENTER|SWT.BOLD);
		measure.setText("测量");
		measure.setWidth(150);
		TableColumn collect = new TableColumn(legendTable, SWT.CENTER|SWT.BOLD);
		collect.setText("汇总");
		collect.setWidth(80);
		TableColumn unit = new TableColumn(legendTable, SWT.CENTER|SWT.BOLD);
		unit.setText("单位");
		unit.setWidth(100);
		TableColumn newest = new TableColumn(legendTable, SWT.CENTER|SWT.BOLD);
		newest.setText("最新");
		newest.setWidth(80);
		TableColumn highest = new TableColumn(legendTable, SWT.CENTER|SWT.BOLD);
		highest.setText("最高");
		highest.setWidth(80);
		TableColumn lowest = new TableColumn(legendTable, SWT.CENTER|SWT.BOLD);
		lowest.setText("最低");
		lowest.setWidth(80);
		TableColumn average = new TableColumn(legendTable, SWT.CENTER|SWT.BOLD);
		average.setText("平均值");
		average.setWidth(80);
		
		legendTable.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
			   
			}
			public void mouseDown(MouseEvent e) {
				focus = legendTable.getSelectionIndex();
				XYPlot xyplot = (XYPlot) chart.getPlot();
				XYLineAndShapeRenderer xylinerenderer=(XYLineAndShapeRenderer)xyplot.getRenderer();
				for(int i=0;i<xyplot.getSeriesCount();i++){
					xylinerenderer.setSeriesStroke(i,new BasicStroke());
				}
				if(focus!=Integer.MAX_VALUE){
				 	xylinerenderer.setSeriesStroke(focus,new BasicStroke(2F));
				}
				byte[] buf=null;
				ByteArrayOutputStream bout=new ByteArrayOutputStream();
				try {
					ChartUtilities.writeChartAsPNG(bout, chart, chartCanvas.getSize().x, chartCanvas.getSize().y);
					buf=bout.toByteArray();
					bout.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				ByteArrayInputStream bin=new ByteArrayInputStream(buf);
				image = null;
				image = new Image(PlatformUI.getWorkbench().getDisplay(), bin);
				chartCanvas.redraw();
			}
			public void mouseUp(MouseEvent e) {
			}
		});
		
		tableViewer = new TableViewer(legendTable);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider());
		//tableViewer.setInput(new ArrayList<TimeSeries>());

		refreshView();
		
		refreshPerformTimer = new Timer("RefreshPerformTimer");
		refreshPerformTimer.schedule(new RefreshPerformTimer(this,PlatformUI.getWorkbench().getDisplay()), 5000, 15000);
		objectVM.timerList.add(refreshPerformTimer);
		
		composite.layout();
		return true;
	}

	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

		 @Override
		 public Image getColumnImage(Object element, int columnIndex) {
			 if(element instanceof TimeSeries) {
					 TimeSeries series = (TimeSeries) element;
					  XYPlot xyplot = (XYPlot) chart.getPlot();
					  XYLineAndShapeRenderer xylinerenderer=(XYLineAndShapeRenderer)xyplot.getRenderer();
					  java.awt.Color color=(java.awt.Color)xylinerenderer.getSeriesPaint(timeSeriesCollection.indexOf(series));
					  if(color==null)
						  return null;
						  switch(columnIndex) {
						   case 0:
							   Image image = new Image (null, 13, 13); 
							   GC gc = new GC (image); 
							   gc.drawRectangle(0, 0, 12, 12);
							   Color c=new Color(null,color.getRed(),color.getGreen(),color.getBlue());
							   gc.setBackground(c);
							   gc.fillRectangle(1, 1, 11, 11);
							   gc.dispose();
							   return image;
						   }   
			  }
			  return null;
		 }

		 @Override
		 public String getColumnText(Object element, int columnIndex) {
		  if(element instanceof TimeSeries) {
			  //int seriesId = (Integer) element;
			  //TimeSeries series=DrawHostPerformance.lineDatasetCPU.getSeries(seriesId);
			  TimeSeries series=(TimeSeries)element;
		   //double [] value=metricsHostTimelines.get(key);
		   double high=0.000;
		   double low=Double.MAX_VALUE;
		   double total=0.000;
		   double average=0.000;
		   double newest=0.000;
		   if(series.getItemCount()>0){
			   for(int i=0;i<series.getItemCount();i++){
				   if(Double.parseDouble(series.getValue(i) + "") > high)
					   high = Double.parseDouble(series.getValue(i)+"");
				   if(Double.parseDouble(series.getValue(i) + "") < low)
					   low = Double.parseDouble(series.getValue(i) + "");
				   total += Double.parseDouble(series.getValue(i) + "");
			   }
			   average = total/series.getItemCount();
			   newest = Double.parseDouble(series.getValue(series.getItemCount()-1) + "");
		   }
		   switch(columnIndex) {
		   case 0:
		    return " ";
		   
		   case 1:
			   switch(chartType.getSelectionIndex()){
				case 0:
					return series.getKey().toString();
				case 1:
					return objectVM.getName();
				case 2:
					return objectVM.getName();
				case 3:
					return objectVM.getName();
				default:
					return series.getKey().toString();
				}
		   case 2:
			   switch(chartType.getSelectionIndex()){
				case 0:
					return "使用情况";
				case 1:
					return "已消耗";
				case 2:
					if(series.getKey().toString().endsWith("Send")){
						return "数据发送速度";
					}else{
						return "数据接收速度";
					}
				case 3:
					if(series.getKey().toString().endsWith("Write")){
						return "写入速度";
					}else{
						return "读取速度";
					}
				default:
					return "使用情况";
				}	
			   
		   case 3:
			   return "平均值";
			   
		   case 4:
			   switch(chartType.getSelectionIndex()){
				case 0:
					return "%";
				case 1:
					return "MB";
				case 2:
					return "KBps";
				case 3:
					return "MBps";
				default:
					return "";
				}		
			   
		   case 5:
			   return MathUtil.Rounding(newest,3);
			   
		   case 6:
			   return MathUtil.Rounding(high,3);
			   
		   case 7:
			   return MathUtil.Rounding(low,3);
			   
		   case 8:
			   return MathUtil.Rounding(average,3);
		   }
		  }
		  
		  return null;
		 }
		}
	
	public void refreshView(){
		if(objectVM.drawVM==null){
			if(chartType!=null){
				switch(chartType.getSelectionIndex()){
				case 0:
					chart = drawCPU.draw(new TimeSeriesCollection());
					break;
				case 1:
					chart = drawMemory.draw(new TimeSeriesCollection(),1024);
					break;
				case 2:
					chart = drawNet.draw(new TimeSeriesCollection());
					break;
				case 3:
					chart = drawDisk.draw(new TimeSeriesCollection());
					break;
				default:
					chart = drawCPU.draw(new TimeSeriesCollection());
				}
			}else{
				chart = drawCPU.draw(new TimeSeriesCollection());
			}
			byte[] buf=null;
		       ByteArrayOutputStream bout=new ByteArrayOutputStream();
		       try {
		    	   if(chartCanvas.getSize().x + chartCanvas.getSize().y==0){
		    		   ChartUtilities.writeChartAsPNG(bout, chart, folder.getSize().x-18, folder.getSize().y-272);
		    	   }else{
		    		   ChartUtilities.writeChartAsPNG(bout, chart, chartCanvas.getSize().x, chartCanvas.getSize().y);
		    	   }
					buf=bout.toByteArray();
					bout.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		       ByteArrayInputStream bin=new ByteArrayInputStream(buf);
		       image = new Image(PlatformUI.getWorkbench().getDisplay(), bin);
		       chartCanvas.redraw();
			return;
		}
		switch(chartType.getSelectionIndex()){
		case 0:
			chart=objectVM.drawVM.cpuChart;
			break;
		case 1:
			chart=objectVM.drawVM.memoryChart;
			break;
		case 2:
			chart=objectVM.drawVM.netChart;
			break;
		case 3:
			chart=objectVM.drawVM.diskChart;
			break;
		default:
			chart=objectVM.drawVM.cpuChart;
		}
		
		XYPlot xyplot = (XYPlot) chart.getPlot();
		XYLineAndShapeRenderer xylinerenderer=(XYLineAndShapeRenderer)xyplot.getRenderer();
		for(int i=0;i < xyplot.getSeriesCount();i++){
			xylinerenderer.setSeriesStroke(i,new BasicStroke());
		}
		if(focus!=Integer.MAX_VALUE){
			xylinerenderer.setSeriesStroke(focus,new BasicStroke(2F));
		}
		byte[] buf=null;
		ByteArrayOutputStream bout=new ByteArrayOutputStream();
		try {
			if(chartCanvas.getSize().x+chartCanvas.getSize().y==0){
				ChartUtilities.writeChartAsPNG(bout, chart, folder.getSize().x-18, folder.getSize().y-272);
			}else{
				ChartUtilities.writeChartAsPNG(bout, chart, chartCanvas.getSize().x, chartCanvas.getSize().y);
			}
			buf=bout.toByteArray();
			bout.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        ByteArrayInputStream bin=new ByteArrayInputStream(buf);
        image = new Image(PlatformUI.getWorkbench().getDisplay(), bin);
        chartCanvas.redraw();
        switch(chartType.getSelectionIndex()){
			case 0:
				timeSeriesCollection=objectVM.drawVM.lineDatasetCPU;
				break;
			case 1:
				timeSeriesCollection=objectVM.drawVM.lineDatasetMemory;
				break;
			case 2:
				timeSeriesCollection=objectVM.drawVM.lineDatasetNet;
				break;
			case 3:
				timeSeriesCollection=objectVM.drawVM.lineDatasetDisk;
				break;
			default:
				timeSeriesCollection=objectVM.drawVM.lineDatasetCPU;
		 }
         tableViewer.setInput(timeSeriesCollection.getSeries());
//       //legendTable.pack();
//       for(int i=5;i<legendTable.getColumnCount();i++){
//       	legendTable.getColumn(i).pack();
//       	legendTable.getColumn(i).setWidth(100);
//      }
//       //tableComposite.pack();
//       GridData gd_canvas = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
//		gd_canvas.heightHint = 150;
//		tableComposite.setLayoutData(gd_canvas);
//       legendTable.setLayoutData(new GridData(GridData.FILL_BOTH));
	}
	
	class RefreshPerformTimer extends TimerTask {
		private volatile VMPerformanceTab perform;
		Display display;
		public RefreshPerformTimer(VMPerformanceTab perform,Display display) {
			this.perform = perform;
			this.display=display;
		}
		public void run() {
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	if(objectVM.metricsTimelines != null && objectVM.metricsTimelines.size() > 0){
			        		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			        		if(objectVM.drawVM==null){
			        			objectVM.drawVM =  new DrawVmPerformance();
				        	}
				        	if(objectVM.step != 0) {
				        		starttime.setText(df.format(new Date(objectVM.startTime)));
				        		endtime.setText(df.format(new Date(objectVM.endTime)));
				        		starttime.pack();
				        		endtime.pack();
				        		objectVM.drawVM.drawVm(objectVM.metricsTimelines ,
				        				objectVM.endTime, 
				        				objectVM.getMemoryTotalValue(), 
				        				objectVM.columns,
				        				objectVM.getUuid(),
				        				objectVM.step);
				        		perform.refreshView();
				        	} else {
				        		MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
		      					messageBox.setText("警告");
		      					messageBox.setMessage("该虚拟机没有存储任何性能数据信息!");
		      					messageBox.open();
				        	}
			        	} 
			        }
			    };
			    this.display.syncExec(runnable); 
			}
		}
	}
	
	class OpenPerformanceJob extends Job
	{
		private Display display;
		String successMessage = "开启成功！";
		String failMessage = "开启失败！";
		public OpenPerformanceJob(Display display) {
			super(innerText);
			this.display = display;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("开始记录性能数据", 100);
			try {
				if (isPerformanceRecClosed == true){
//					host.startPerformanceXML(objectVM.getConnection());					
				} else {
//					host.stopPerformanceXML(objectVM.getConnection());
					successMessage = "关闭成功！";					
				}
				if (!this.display.isDisposed()){
				    Runnable runnable = new Runnable() {
				        public void run(){
				        	openPerformance.setEnabled(true);
				        	MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
			    			messageBox.setText("提示");
			    			messageBox.setMessage(successMessage);
			    			messageBox.open();	
			    			if (isPerformanceRecClosed == true) {
			    				isPerformanceRecClosed = false;
//			    				host.setPerformanceRecClosed(false);
			    				innerText = "关闭记录开关";
			    		    } else {
			    		    	isPerformanceRecClosed = true;
//			    		    	host.setPerformanceRecClosed(true);
			    		    	innerText = "打开记录开关";
			    		    }
			    			openPerformance.setText(innerText);
				        }
				    };
				    this.display.syncExec(runnable); 
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (!this.display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
				        	openPerformance.setEnabled(true);
				        	MessageBox messageBox = new MessageBox(new Shell(), SWT.OK); 
			    			messageBox.setText("提示");
			    			if (isPerformanceRecClosed == false) {
			    				failMessage = "关闭失败！";
			    			}
			    			messageBox.setMessage(failMessage);
			    			messageBox.open();
				        }
				    };
				    this.display.syncExec(runnable); 
				}
			}
			monitor.done();
			return Status.OK_STATUS;
		}
	}
}
