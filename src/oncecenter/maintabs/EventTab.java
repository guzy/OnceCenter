package oncecenter.maintabs;

import java.text.SimpleDateFormat;

import oncecenter.util.ImageRegistry;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

public class EventTab extends OnceTabItem {

	private Table eventTable;
	private  Composite detailComposite;
	private VMTreeObject object;
	Label typeValue;
	Label userValue;
	Label timeValue;
	Label targetValue;
	CLabel descriptionValue;
	
	TableViewer tableViewer;
	
	public EventTab(CTabFolder arg0, int arg1, VMTreeObject object) {
		super(arg0, arg1, object);
		this.object = object;
		setText("事件");
	}

	public EventTab(CTabFolder arg0, int arg1, int arg2, VMTreeObject object) {
		super(arg0, arg1, arg2, object);
		this.object = object;
		setText("事件");
	}
	

	public boolean Init(){
		composite = new Composite(folder, SWT.NONE); 
		this.setControl(composite);
		composite.setLayout(new GridLayout(1,false));
		composite.setBackground(new Color(null,255,255,255));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label label=new Label(composite,SWT.NONE);
		label.setBackground(new Color(null,255,255,255));
		label.setText(" ");
		
		eventTable=new Table(composite, SWT.BORDER | SWT.V_SCROLL
		        | SWT.H_SCROLL|SWT.FULL_SELECTION);
		eventTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		eventTable.setHeaderVisible(true);
		eventTable.setLinesVisible(false);
		
		tableViewer = new TableViewer(eventTable);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider());
		
		
		eventTable.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e)
			{
			   
			}
			public void mouseDown(MouseEvent e)
			{
				 int selIndex = eventTable.getSelectionIndex();
				 TableItem item = eventTable.getItem(selIndex);
				 typeValue.setText(item.getText(1));
				 userValue.setText(item.getText(5));
				 timeValue.setText(item.getText(2));
				 targetValue.setText(item.getText(4));
				 descriptionValue.setText(item.getText(0));
				 
				// detailComposite.pack();
			}
			public void mouseUp(MouseEvent e)
			{
			}
		});

		TableColumn description = new TableColumn(eventTable, SWT.CENTER|SWT.BOLD);
		description.setText("描述");
		description.setWidth(300);
		TableColumn type = new TableColumn(eventTable, SWT.LEFT|SWT.BOLD);
		type.setText("类型");
		type.setWidth(100);
		TableColumn time = new TableColumn(eventTable, SWT.CENTER|SWT.BOLD);
		time.setText("日期时间");
		time.setWidth(150);
		TableColumn task = new TableColumn(eventTable, SWT.CENTER|SWT.BOLD);
		task.setText("任务");
		task.setWidth(80);
		TableColumn target = new TableColumn(eventTable, SWT.CENTER|SWT.BOLD);
		target.setText("目标");
		target.setWidth(150);
		TableColumn user = new TableColumn(eventTable, SWT.CENTER|SWT.BOLD);
		user.setText("用户");
		user.setWidth(100);
		
		ExpandBar expandBar = new ExpandBar(composite,SWT.V_SCROLL); 
		expandBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		   { 
		    detailComposite = new Composite(expandBar,SWT.NONE); 
		    detailComposite.setLayout(new GridLayout(8,false)); 
		    
		    {
		    	new Label(detailComposite, SWT.NONE).setText("类型： ");
			    typeValue = new Label(detailComposite, SWT.NONE);
			    typeValue.setFont(SWTResourceManager.getFont("微软雅黑", 9, SWT.BOLD));
			    typeValue.setText("         ");
			    
			    new Label(detailComposite, SWT.NONE).setText("用户：");
			    userValue = new Label(detailComposite, SWT.NONE);
			    userValue.setFont(SWTResourceManager.getFont("微软雅黑", 9, SWT.BOLD));
			    userValue.setText("         ");
			    
			    new Label(detailComposite, SWT.NONE).setText("时间： ");
			    timeValue = new Label(detailComposite, SWT.NONE);
			    timeValue.setFont(SWTResourceManager.getFont("微软雅黑", 9, SWT.BOLD));
			    timeValue.setText("                              ");
			    
			    new Label(detailComposite, SWT.NONE).setText("目标： ");
			    targetValue = new Label(detailComposite, SWT.NONE);
			    targetValue.setFont(SWTResourceManager.getFont("微软雅黑", 9, SWT.BOLD));
			    targetValue.setText("                             ");
		    }
		    
		    {
		    	Label l = new Label(detailComposite, SWT.NONE);
		    	l.setText("描述： ");
		    	GridData g = new GridData(GridData.FILL_HORIZONTAL);
		    	g.horizontalSpan=8;
		    	l.setLayoutData(g);
		    	
		    	descriptionValue = new CLabel(detailComposite, SWT.NONE);
		    	descriptionValue.setText("   ");
		    	descriptionValue.setLayoutData(g);
		    }
		    
		    {
		    	
		    }
		    ExpandItem item1 = new ExpandItem(expandBar, SWT.NONE); 
		    item1.setText("事件详细信息"); 
		    item1.setHeight(100);
		    item1.setExpanded(true);
		    item1.setControl(detailComposite); 
		   }
		   for(int i = 0;  i < object.events.size(); ++i)
			   System.out.println("dhf_events" + object.events.get(i));
		   tableViewer.setInput(object.events);
		
		   composite.layout();
		   return true;
	}
	
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

		 @Override
		 public Image getColumnImage(Object element, int columnIndex) {
			 if(element instanceof VMEvent) {
				  VMEvent event = (VMEvent) element;
			   
			   switch(columnIndex) {
			   case 0:
			    return event.getImage();
			   
			   case 1:
				   if(event.getType().equals(eventType.info))
					   return ImageRegistry.getImage(ImageRegistry.INFO);
				   else
					   return ImageRegistry.getImage(ImageRegistry.INFO);
				   
			   case 4:
				   if(event.getTarget() instanceof VMTreeObjectVM)
					   return ImageRegistry.getImage(ImageRegistry.VM);
				   
			   
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
		    return event.getDescription();
		   
		   case 1:
			   if(event.getType().equals(eventType.info))
				   return "信息";
			   else
				   return "警告";
			   
		   case 2:
			   return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(event.getDatetime());
			   
		   case 3:
			   return event.getTask();
			   
		   case 4:
			   return event.getTarget().getName();
			   
		   case 5:
			   return event.getUser();
		   }
		  }
		  
		  return null;
		 }
		}

	public void logFresh() {
		if(composite!=null){
			tableViewer.setInput(object.events);
			//composite.pack();
			for(int i=0;i<6;i++)
				eventTable.getColumn(i).pack();
		}
	}
}
