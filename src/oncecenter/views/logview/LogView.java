 package oncecenter.views.logview;

import java.text.SimpleDateFormat;

import oncecenter.Constants;
import oncecenter.util.ImageRegistry;
import oncecenter.views.logview.VMEvent.eventType;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class LogView extends ViewPart {

	public final static String ID="oncecenter.views.LogView";
	private Table eventTable;
	private TableViewer tableViewer;
	private Timer colorTimer;
	boolean flag=true;
	
	final Color colorOriginal = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
	final Color colorBlink = Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
	
	Map<VMEvent,Color> colorMap = new HashMap<VMEvent,Color>();
	
	ArrayList<VMEvent> eventList = new ArrayList<VMEvent>();
	@Override
	public void createPartControl(Composite parent) {
		
		parent.setBackground(SWTResourceManager.getColor(255, 255, 255));
		parent.setLayout(new GridLayout(1,false));
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		eventTable=new Table(parent, SWT.BORDER | SWT.V_SCROLL
		        | SWT.H_SCROLL|SWT.FULL_SELECTION);
		eventTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		eventTable.setHeaderVisible(true);
		eventTable.setLinesVisible(false);
		
		tableViewer = new TableViewer(eventTable);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider());
		
		TableColumn type = new TableColumn(eventTable, SWT.LEFT|SWT.BOLD);
		type.setText("类型");
		type.setWidth(100);
		TableColumn time = new TableColumn(eventTable, SWT.LEFT|SWT.BOLD);
		time.setText("日期时间");
		time.setWidth(150);
		TableColumn description = new TableColumn(eventTable, SWT.LEFT|SWT.BOLD);
		description.setText("描述");
		description.setWidth(1000);
		TableColumn state = new TableColumn(eventTable, SWT.LEFT|SWT.BOLD);
		state.setText("状态");
		state.setWidth(1000);
		
		eventTable.addListener(SWT.MouseDoubleClick, new Listener(){

			@Override
			public void handleEvent(Event e) {
				// 双击事件，还没处理
				 System.out.println("双击事件"); 
				//VMEvent event = (VMEvent)tableViewer.getElementAt(eventTable.getSelectionIndex());
//				if(event.getType().equals(eventType.warning)){
//					VMTreeObjectVM treeObject = (VMTreeObjectVM)event.getTarget();
//					OpenConsoleAction action = new OpenConsoleAction(treeObject);
//					action.run();
//					IViewReference reference=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
//							.findViewReference(MainView.ID, treeObject.getConsoleID()+"");
//					if(reference!=null){
//		        		MainView console=(MainView)reference.getView(false);
//		        		CTabFolder folder = console.tabFolder;
//		        		if(treeObject.getAppType().equals(ResourceTypes.CPU))
//						   {
//							   CTabItem items[] = folder.getItems();
//							   int i = 0;
//							   for(; i < folder.getItemCount(); ++i)
//							   {
//								   if(items[i] instanceof CPUBindTab)
//								   {
//									   CPUBindTab tab = (CPUBindTab)items[i];
//									   if(tab.composite==null)
//										   tab.Init();
//									   folder.setSelection(items[i]);
//									   break;
//								   }
//							   }
//						   }
//						   else if(treeObject.getAppType().equals(ResourceTypes.DISK))
//						   {
//							   CTabItem items[] = folder.getItems();
//							   int i = 0;
//							   for(; i < folder.getItemCount(); ++i)
//							   {
//								   if(items[i] instanceof DiskAdjustTab)
//								   {
//									   DiskAdjustTab tab = (DiskAdjustTab)items[i];
//									   if(tab.composite==null)
//										   tab.Init();
//									   folder.setSelection(items[i]);
//									   
//									   break;
//								   }
//							   }
//							   
//						   }
//					}
//				}
			}
		});
		
		Constants.logView=this;
		
	}

	class TableLabelProvider extends LabelProvider implements ITableLabelProvider,ITableColorProvider,ITableFontProvider {

		
		 @Override
		 public Image getColumnImage(Object element, int columnIndex) {
			 if(element instanceof VMEvent) {
				  VMEvent event = (VMEvent) element;
			   
			   switch(columnIndex) {
			   
			   case 0:
				   if(event.getType().equals(eventType.info))
					   return ImageRegistry.getImage(ImageRegistry.INFO);
				   else
					   
					   return ImageRegistry.getImage(ImageRegistry.DISABLE);
			   }   
			  }
			  
			  return null;
		 }

		 @Override
		 public String getColumnText(Object element, int columnIndex) {
		  if(element instanceof VMEvent) {
			  VMEvent event = (VMEvent) element;
		   
		   switch(columnIndex) {		   
		   case 0:
			   if(event.getType().equals(eventType.info))
				   return "信息";
			   else
				   return "警告";
			   
		   case 2:
			    return event.getDescription();
			    
		   case 1:
			   return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(event.getDatetime());
			   
		   }
		  }
		  
		  return null;
		 }

		@Override
		public Color getForeground(Object element, int columnIndex) {
			
			 if(element instanceof VMEvent) {
				  VMEvent event = (VMEvent) element;
				  
				   if(event.getType().equals(eventType.warning)){
					   if(colorMap.get(event).equals(colorOriginal)){
						   //System.out.println("original");
						   if(columnIndex==2){
							   colorMap.put(event, colorBlink);
						   }
						   return colorBlink;
					   }else{
						   //System.out.println("blink");
						   if(columnIndex==2){
							   colorMap.put(event, colorOriginal);
						   }
						   return colorOriginal;
					   }
				   }					   
				   else
					  return null;  
			  }
			return null;
		}

		@Override
		public Color getBackground(Object element, int columnIndex) {
			
			 if(element instanceof VMEvent) {
				  VMEvent event = (VMEvent) element;
			   
				   if(event.getType().equals(eventType.warning))
					   return Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
				   else
					  return null;
			  }
			return null;
		}
		FontRegistry registry = new FontRegistry(); 
		@Override
		public Font getFont(Object element, int columnIndex) {
			
			 if(element instanceof VMEvent) {
				  VMEvent event = (VMEvent) element;
			   
			   switch(columnIndex) {
			   
			   case 0:
				   if(event.getType().equals(eventType.warning))
					   return registry.getBold(Display.getCurrent().getSystemFont()  
			                    .getFontData()[0].getName());  
				   else
					   return null;
			   }   
			  }
			  
			return null;
		}

		}

	class ColorTimer extends TimerTask
	{
		int index=0;
		Display display;
		public ColorTimer(Display display) {
			this.display=display;
		}
		public void run() {
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	tableViewer.setInput(eventList);
					}
			    };
			    this.display.asyncExec(runnable); 
			}
		}
		
	}
	
	class CancelTimer extends TimerTask{
		public CancelTimer() {
		}
		public void run() {
			colorTimer.cancel();
		}
	}
	public void logFresh(VMEvent event){
		//composite.pack();
		if(eventList.size()>0&&event.getDescription().equals(eventList.get(eventList.size()-1).getDescription())){
			return;
		}
		eventList.add(event);
		colorMap.put(event, colorOriginal);
		tableViewer.setInput(eventList);
//		for(int i=0;i<3;i++){
//			eventTable.getColumn(i).pack();
//		}
		eventTable.setSelection(eventTable.getItemCount()-1);
		if(event.getType().equals(eventType.warning)){
			colorTimer = new Timer("ColorTimer");
			colorTimer.schedule(new ColorTimer(PlatformUI.getWorkbench().getDisplay()), 200, 200);
			Timer cancelTimer = new Timer("");
			cancelTimer.schedule(new CancelTimer(), 2000);
		}
	}
	@Override
	public void setFocus() {
		

	}

}
