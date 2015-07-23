package oncecenter.wizard.recoverlostvm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oncecenter.Constants;
import oncecenter.action.addlostelement.CompareRootAction;
import oncecenter.util.AddServerUtil;
import oncecenter.util.CommonUtil;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;
import oncecenter.wizard.newvmfromtemp.NewVMPage;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.VM;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.XenAPIException;

public class LostVmListWizardPage extends NewVMPage {

	private Text startTime;
	private Text endTime;
	Composite composite;
	private Button okButton;
	private Table table;
	private CheckboxTableViewer tableViewer;
	
	VMTreeObjectRoot selection;
	
	String start;
	String end;
	
	Map<String, String> lostVmMap = new HashMap<String,String>();
	List<String> lostVmPathList = new ArrayList<String>();
	List<String> lostVmNameList = new ArrayList<String>();

	public LostVmListWizardPage(VMTreeObjectRoot selection) {
		super("wizardPage");
		setTitle("找回虚拟机");
		setDescription("请尽量回忆虚拟机丢失的时间，选择一个大致的时间段，然后点击查询按钮，\n" +
				"在这段时间内被删除的虚拟机会出现在列表中,选择想要恢复的虚拟机即可恢复。");
		this.selection = selection;
	}

	@Override
	public void createControl(Composite parent) {
		
		composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(5,false));
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		composite.setLayoutData(gridData);
		
		new Label(composite,SWT.NONE).setText("开始时间：");
		startTime = new Text(composite,SWT.BORDER);
		startTime.setText("2013-09-01");
		start = startTime.getText();
		startTime.addMouseListener(new MouseAdapter(){
			public void mouseUp(MouseEvent e){
				CalendarDialog d = new CalendarDialog(new Shell(),startTime.getText());
				if(Window.OK==d.open()){
					startTime.setText(d.getDateText());
					composite.layout();
				}
				start = startTime.getText();
			}
		});
		
		new Label(composite,SWT.NONE).setText("结束时间：");
		endTime = new Text(composite,SWT.BORDER);
		endTime.setText(CommonUtil.getCurrentDate());
		end = endTime.getText();
		endTime.addMouseListener(new MouseAdapter(){
			public void mouseUp(MouseEvent e){
				CalendarDialog d = new CalendarDialog(new Shell(),endTime.getText());
				if(Window.OK==d.open()){
					endTime.setText(d.getDateText());
					composite.layout();
				}
				end = endTime.getText();
			}
		});
		okButton = new Button(composite,SWT.NONE);
		okButton.setText("查询");
		okButton.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				widgetSelected(arg0);
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				lostVmMap.clear();
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell()); 
				try {
					dialog.run(true, true, new IRunnableWithProgress(){ 
					    public void run(IProgressMonitor monitor) { 
					        monitor.beginTask("获取中...",  IProgressMonitor.UNKNOWN); 
					        try {
								lostVmMap = VM.getLostVMByDate(selection.getConnection(), start,
									 end);
								lostVmPathList.clear();
								lostVmNameList.clear();
								for(String s:lostVmMap.keySet()){
									lostVmPathList.add(s);
									lostVmNameList.add(lostVmMap.get(s));
								}
								Display display=PlatformUI.getWorkbench().getDisplay();
								if (!display.isDisposed()){
								    Runnable runnable = new Runnable(){
								        public void run( ){
								        	tableViewer.setInput(lostVmNameList);
								        }
									};
								    display.syncExec(runnable); 
								}
								
							} catch (Exception e) {
								
								e.printStackTrace();
							}
					        monitor.done(); 
					    } 
					});
					
				} catch (Exception e1) {
					
					e1.printStackTrace();
				}		
			}
			
		});
		
		table = new Table( composite, SWT.BORDER | SWT.V_SCROLL
		        | SWT.H_SCROLL|SWT.FULL_SELECTION|SWT.MULTI | SWT.CHECK );
		GridData g = new GridData(GridData.FILL_BOTH);
		g.horizontalSpan = 5;
		table.setLayoutData(g);
		table.setLinesVisible(true);
		table.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				widgetSelected(arg0);
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(tableViewer.getCheckedElements().length>0){
					setPageComplete(true);
				}else{
					setPageComplete(false);
				}
			}
			
		});
		
		TableColumn tc1 = new TableColumn(table, SWT.CENTER);
		TableColumn tc2 = new TableColumn(table, SWT.CENTER);
		tc1.setText("   ");
		tc2.setText("虚拟机名称[丢失时间]");
		tc1.setWidth(30);
		tc2.setWidth(400);
		table.setHeaderVisible(true);
		
		tableViewer = new CheckboxTableViewer(table);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider());

		//tableViewer.setInput(lostHosts);
		
		setControl(composite);
		
		setPageComplete(false);
		composite.layout();
	}

	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

		 @Override
		 public Image getColumnImage(Object element, int columnIndex) {
			  return null;
		 }

		 @Override
		 public String getColumnText(Object element, int columnIndex) {
		  if(element instanceof String) {
			  String name = (String)element;
		   switch(columnIndex) {
		   case 0:
			   return "";
		   
		   case 1:
			   return name;
			   
		  
		   }
		  }
		  
		  return null;
		 }
	}
	@Override
	protected boolean finishButtonClick() {
		
		for(Object o:tableViewer.getCheckedElements()){
			String s = (String)o;
			int index = lostVmNameList.indexOf(s);
			if(index!=-1){
				((RecorverLostVmWizard)getWizard()).checkedPathList.add(lostVmPathList.get(index));
			}
		}
		return true;
	}

}
