package oncecenter.wizard.newpool;

import java.util.ArrayList;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;

public class NewPoolWizardPage extends WizardPage {

	private Text name;
	CheckboxTableViewer tableViewer;
	private Text description;
	private Combo master;
	private ArrayList<VMTreeObjectHost> hosts;
	//public ArrayList<VMTreeParent> selectedHosts;
	public Combo getMaster() {
		return master;
	}

	public ArrayList<VMTreeObjectHost> getHosts() {
		return hosts;
	}

	private Table poolTable;
	private Button newServer;
	
	/**
	 * Create the wizard.
	 */
	public NewPoolWizardPage(String name, ArrayList<VMTreeObjectHost> hosts) {
		super(name);
		setTitle("新建资源池");
//		setDescription("请输入资源池的名称和描述，选择要加入资源池的主机，并指定其中一台主机为主节点");
		this.hosts=hosts;
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(2,false));
		
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		composite.setLayoutData(gridData);	
		
		new Label(composite, SWT.NONE).setText("资源池名称:");
		name = new Text(composite, SWT.BORDER);
		name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		name.setText("new pool");
		
		new Label(composite, SWT.NONE).setText("资源池描述:");
		description = new Text(composite, SWT.BORDER);
		description.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		description.setText("请输入描述信息");
		description.selectAll();
		
		Group Servers = new Group(composite, SWT.NONE);
		Servers.setLayout(new GridLayout(2,false));
		Servers.setLayoutData(gridData);
		Servers.setText("Servers");
		
		new Label(Servers, SWT.NONE).setText("请选择主节点:");
		master = new Combo(Servers,SWT.NONE);
		master.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		for(VMTreeObjectHost host:hosts){
//			master.add(host.getName());
//		}
		
		master.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				for(TableItem item:poolTable.getItems()){
					item.setText(1, "");
				}
				for(TableItem item:poolTable.getItems()){
					Item i=(Item)item.getData();
					if(item.getText(0).equals(master.getText())){
						item.setText(1,"主节点");
						i.setMaster(true);
						item.setChecked(true);
					}else{
						item.setText(1,"");
						i.setMaster(false);
					}
				}
				setPageState();
			}
		});
		new Label(Servers, SWT.NONE).setText("可选的主机列表:");
		new Label(Servers, SWT.NONE);
		
		poolTable = new Table(Servers,SWT.BORDER | SWT.V_SCROLL
		        | SWT.H_SCROLL|SWT.CHECK|SWT.FULL_SELECTION);
		poolTable.setHeaderVisible(false);
		poolTable.setLinesVisible(false);
		
		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = SWT.FILL;
		gridData1.verticalAlignment = SWT.FILL;
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.horizontalSpan=5;
		gridData1.heightHint = 60;
		poolTable.setLayoutData(gridData1);	
		
		TableColumn slaves = new TableColumn(poolTable, SWT.None);
		//vcpu.setText("VCPU");
		slaves.setWidth(200);
		TableColumn isMaster = new TableColumn(poolTable, SWT.None);
		//vcpu.setText("VCPU");
		isMaster.setWidth(100);
		
		tableViewer = new CheckboxTableViewer(poolTable);
		tableViewer.addDoubleClickListener(new IDoubleClickListener(){
			   @Override
			   public void doubleClick(DoubleClickEvent event) {
			    IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			    Object obj = selection.getFirstElement();
			    	tableViewer.setChecked(obj, 
			       !tableViewer.getChecked(obj));
			    	setPageState();
			   }
			  });
		
		poolTable.addListener(SWT.MouseDown, new Listener() {  
            public void handleEvent(Event event) {  
            	setPageState();
            }  
        });  

		ArrayList<Item> elements=new ArrayList<Item>();
		for(VMTreeObjectHost host:hosts){
			master.add(host.getName());
			Item i=new Item();
			i.setHost(host);
			i.setMaster(false);
			elements.add(i);
		}
		
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new CheckTableLabelProvider());
		tableViewer.setInput(elements);

		setPageComplete(false);
		
		if(elements.size() > 0) {
			master.select(0);
			Item item = elements.get(0);
			item.setMaster(true);
			poolTable.getItem(0).setText(1, "主节点");
			poolTable.getItem(0).setChecked(true);
			setPageState();
		}
		setControl(composite);
	}
	
	public void setPageState() {
		int index = master.getSelectionIndex();
		if(index>=0&&poolTable.getItem(index).getChecked())
			setPageComplete(true);
		else
			setPageComplete(false);
	}
	
	class Item{
		private VMTreeObjectHost host;
		private boolean isMaster;
		public VMTreeObjectHost getHost() {
			return host;
		}
		public void setHost(VMTreeObjectHost host) {
			this.host = host;
		}
		public boolean isMaster() {
			return isMaster;
		}
		public void setMaster(boolean isMaster) {
			this.isMaster = isMaster;
		}
	}

		public ArrayList<Item> getSelection() {
		  Object[] objects = tableViewer.getCheckedElements();
		  
		  ArrayList<Item> list = new ArrayList<Item>();
		  for(Object obj : objects) {
		   if(obj instanceof Item) {
		    Item i=(Item)obj;
		    list.add(i);
		   }
		  }
		  return list;
		}

	
	
	class CheckTableLabelProvider extends LabelProvider implements ITableLabelProvider {

		 @Override
		 public Image getColumnImage(Object element, int columnIndex) {
		  return null;
		 }

		 @Override
		 public String getColumnText(Object element, int columnIndex) {
		  if(element instanceof Item) {
			  Item item = (Item) element;
		   
		   switch(columnIndex) {
		   case 0:
		    return item.getHost().getName();
		   
		   case 1:
		    if(item.isMaster())
		    	return "master";
		    else
		    	return "";
		   }
		  }
		  
		  return null;
		 }
		}
	public String getName() {
		return name.getText();
	}
	public String getDescription() {
		return description.getText();
	}

}
