package oncecenter.maintabs.sr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import oncecenter.Constants;
import oncecenter.maintabs.OnceSRTabItem;
import oncecenter.util.ImageRegistry;
import oncecenter.util.TypeUtil;
import oncecenter.util.dialog.QuestionDialog;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wb.swt.SWTResourceManager;

import com.once.xenapi.Connection;
import com.once.xenapi.SR;
import com.once.xenapi.VDI;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.XenAPIException;

public class VDIListTab extends OnceSRTabItem {
	
	private Table vdiTable;
	private TableViewer tableViewer;
	private Button refreshButton;
	private Button deleteButton;
	private Button editButton;
	private Label alertLabel;
	private ArrayList<StorageUser> vdiList = new ArrayList<StorageUser>();
	public VDIListTab(CTabFolder arg0, int arg1, VMTreeObjectSR object) {
		super(arg0, arg1, object);
		setText("存储");
		this.objectSR = object;
	}

	public VDIListTab(CTabFolder arg0, int arg1, int arg2, VMTreeObjectSR object) {
		super(arg0, arg1, arg2, object);
		setText("存储");
		this.objectSR = object;
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
		
		vdiTable=new Table(composite, SWT.BORDER | SWT.V_SCROLL
		        | SWT.H_SCROLL|SWT.FULL_SELECTION);
		vdiTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		vdiTable.setHeaderVisible(true);
		vdiTable.setLinesVisible(false);
		
		tableViewer = new TableViewer(vdiTable);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider());
		
		TableColumn name = new TableColumn(vdiTable, SWT.CENTER|SWT.BOLD);
		name.setText("名称");
		name.setWidth(250);
		TableColumn description = new TableColumn(vdiTable, SWT.CENTER|SWT.BOLD);
		description.setText("描述");
		description.setWidth(100);
		TableColumn size = new TableColumn(vdiTable, SWT.CENTER|SWT.BOLD);
		size.setText("大小");
		size.setWidth(100);
		TableColumn vm = new TableColumn(vdiTable, SWT.CENTER|SWT.BOLD);
		vm.setText("虚拟机");
		vm.setWidth(200);

		
		SrJob job = new SrJob(objectSR,PlatformUI.getWorkbench().getDisplay());
		job.schedule();
		vdiTable.pack();
		
		Composite operatorComposite = new Composite(composite,SWT.NULL);
		operatorComposite.setLayout(new GridLayout(5,false));
		operatorComposite.setBackground(new Color(null,255,255,255));
		operatorComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		refreshButton = new Button(operatorComposite,SWT.LEFT_TO_RIGHT);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.BEGINNING;
		refreshButton.setText("刷新存储信息");
		refreshButton.setLayoutData(gridData);
		refreshButton.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				widgetSelected(arg0);
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
//				addItem(objectSR);
//				tableViewer.setInput(vdiList);
				//tianfei
				SrJob job = new SrJob(objectSR,PlatformUI.getWorkbench().getDisplay());
				job.schedule();
			}
			
		});
		
		Label holdLabel = new Label(operatorComposite,SWT.LEFT_TO_RIGHT);
		holdLabel.setText("|");
		editButton = new Button(operatorComposite,SWT.LEFT_TO_RIGHT);
		editButton.setText("修改存储信息");
		editButton.setEnabled(false);
		editButton.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				
				int index = vdiTable.getSelectionIndex();
				if(index == -1)
				{
					alertLabel.setVisible(true);
				}
				else
				{
					alertLabel.setVisible(false);
					StorageUser iso = vdiList.get(index);
					EditIsoDialog dialog = new EditIsoDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(),index,iso.getName(),iso.getDescription());
					dialog.open();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
				widgetSelected(e);
			}
			
		});
		
		deleteButton = new Button(operatorComposite,SWT.LEFT_TO_RIGHT);
		deleteButton.setText("删除存储信息");
		deleteButton.setEnabled(false);
		deleteButton.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				
				int index = vdiTable.getSelectionIndex();
				if(index == -1)
				{
					alertLabel.setVisible(true);
				}
				else
				{
					alertLabel.setVisible(false);
					DeleteIsoDialog dialog = new DeleteIsoDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(),index);
					dialog.open();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
				widgetSelected(e);
			}
			
		});
		alertLabel = new Label(operatorComposite,SWT.NONE);
		alertLabel.setText("请选中要操作的ISO文件.");
		alertLabel.setVisible(false);
		alertLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		composite.layout();
		return true;
	}
	class StorageUser implements Comparable
	{
		private String name;
		private String description;
		private String size;
//		private String vmName;
		private VDI vdi;
		public VDI getVdi() {
			return vdi;
		}
		public void setVdi(VDI vdi) {
			this.vdi = vdi;
		}
		public StorageUser(String name, String description, String size,VDI vdi)
		{
			this.name = name;
			this.description = description;
			this.size = size;
//			this.vmName = vmName;
			this.vdi = vdi;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public String getSize() {
			return size;
		}
		public void setSize(String size) {
			this.size = size;
		}
//		public String getVmName() {
//			return vmName;
//		}
//		public void setVmName(String vmName) {
//			this.vmName = vmName;
//		}
		@Override
		public int compareTo(Object arg0) {
			
			StorageUser other = (StorageUser)arg0;
			return this.name.compareToIgnoreCase(other.name);
		}
	}
	class SrJob extends Job
	{
		private VMTreeObjectSR srObject;
		private Display display;
		public SrJob(VMTreeObjectSR srObject,Display display) {
			super("异步获取存储信息");
			
			this.srObject = srObject;
			this.display = display;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			monitor.beginTask("", 100);
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	refreshButton.setEnabled(false);
			        }
			    };
			    this.display.syncExec(runnable); 
			}
			addItem(srObject); 
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	if(!srObject.getSrType().contains(TypeUtil.isoSign))
			        	{
			        		editButton.setEnabled(false);
							deleteButton.setEnabled(false);
			        	}
			        	else
			        	{
			        		editButton.setEnabled(true);
							deleteButton.setEnabled(true);
			        	}
			        	tableViewer.setInput(vdiList); 
			        	refreshButton.setEnabled(true);
			        }
			    };
			    this.display.syncExec(runnable); 
			}
			
			monitor.done();
			return Status.OK_STATUS;
		}
		
	}
	public void addItem(VMTreeObjectSR srObject){
		SR sr = (SR)objectSR.getApiObject();
		try {
			vdiList.clear();
			for(VDI vdi:sr.getVDIs(srObject.getConnection())){
				VDI.Record record = vdi.getRecord(srObject.getConnection());
				String name = record.uuid;
				String description = record.nameDescription;
				double virtualSize = record.virtualSize/1024/1024;
				String size = "";
				if(virtualSize > 10000)
					size = virtualSize/1024 +"GB";
				else
					size = virtualSize +"MB";
//				String vmName = "";
				if(srObject.getSrType().contains(TypeUtil.isoSign))
					name = record.nameLabel;
//				if(!srObject.getSrType().equals(TypeUtil.isoSrType)){
//					vmName = record.otherConfig.get("virtual_machine");
//				
//				}else{
//					name = record.nameLabel;
//					vmName = "";
//				}
				StorageUser su = new StorageUser(name,description,size,vdi);
				vdiList.add(su);
			}
			Collections.sort(vdiList);
			//System.out.print(vdiList.size());
		} catch (BadServerResponse e) {
			
			e.printStackTrace();
		} catch (XenAPIException e) {
			
			e.printStackTrace();
		} catch (XmlRpcException e) {
			
			e.printStackTrace();
		}
		
	}
	
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider{

		@Override
		public Image getColumnImage(Object arg0, int arg1) {
			
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			
			if(element instanceof StorageUser)
			{
				StorageUser su = (StorageUser)element;
				switch(columnIndex) {		   
				   case 0:
					   return su.getName();
				   case 1:
					    return su.getDescription();
					    
				   case 2:
					   return su.getSize();
//				   case 3:
//					   return su.getVmName();					   
				   }
			}	
			return null;
		}
	}

	
	/**删除ISO文件对话框**/
	class DeleteIsoDialog extends Dialog
	{

		private static final int OK_ID = 0;
		private static final String OK_LABEL = "确定";
		private static final int CLOSE_ID = 1;
		private static final String CLOSE_LABEL = "取消";
		private CLabel msgCLabel;
		private Job job;
		private Label alertLabel;
		private int index;
		public DeleteIsoDialog(Shell parentShell,int index) {
			super(parentShell);
			
			this.index = index;
		}
		
		
		
		protected Control createDialogArea(Composite parent)
		{
			Composite composite = (Composite)super.createDialogArea(parent);
			String msgInfo = "确定要删除此ISO文件吗?";
			msgCLabel = new CLabel(composite,SWT.NONE);
			msgCLabel.setImage(ImageRegistry.getImage(ImageRegistry.REBOOT_OR_NOT));
			msgCLabel.setText(msgInfo);
			return parent;
		}
		@Override
		protected void createButtonsForButtonBar(Composite parent)
		{
			createButton(parent,DeleteIsoDialog.OK_ID,DeleteIsoDialog.OK_LABEL, true);
			createButton(parent,DeleteIsoDialog.CLOSE_ID,DeleteIsoDialog.CLOSE_LABEL,true);
		}
		
		protected void buttonPressed(int buttonId)
		{
			
			if(DeleteIsoDialog.OK_ID == buttonId)
			{
				DeleteIsoJob job = new DeleteIsoJob(objectSR.getConnection(),index);
				job.schedule();
			}
			close();
		}
	}

	class DeleteIsoJob extends Job
	{
		private Display display;
		private int index;
		private Connection connection;
		public DeleteIsoJob(Connection connection,int index) {
			super("删除镜像....");
			
			this.connection = connection;
			this.index = index;
			display = PlatformUI.getWorkbench().getDisplay();
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			monitor.beginTask("", 100);
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	deleteButton.setEnabled(false);
			        }
			    };
			    this.display.syncExec(runnable); 
			}
			VDI vdi = vdiList.get(index).getVdi();
			try {
				vdi.destroy(objectSR.getConnection());
				vdiList.remove(index);
				if (!display.isDisposed()){
				    Runnable runnable = new Runnable(){
				        public void run(){
				        	tableViewer.setInput(vdiList);
				        }
				    };
				    display.asyncExec(runnable); 
				}
				
			} catch (BadServerResponse e) {
				
				e.printStackTrace();
			} catch (XenAPIException e) {
				
				e.printStackTrace();
			} catch (XmlRpcException e) {
				
				e.printStackTrace();
			}
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	deleteButton.setEnabled(true);
			        }
			    };
			    this.display.syncExec(runnable); 
			}
			monitor.done();
			return Status.OK_STATUS;
		}
		
	}
	/**编辑ISO文件**/
	class EditIsoDialog extends Dialog
	{
		private static final int OK_ID = 0;
		private static final String OK_LABEL = "确定";
		private static final int CLOSE_ID = 1;
		private static final String CLOSE_LABEL = "取消";
		private Label nameLabel;
		private Text nameText;
		private Label descriptionLabel;
		private Text descriptionText;
		private int index;
		private String name;
		private String isoDescription;
		private Label alertLabel;
		protected EditIsoDialog(Shell parentShell,int index,String name, String description) {
			super(parentShell);
			
			this.index = index;
			this.name = name;
			this.isoDescription = description;
		}
		
		protected void configureShell(Shell shell)
		{
			super.configureShell(shell);
			shell.setText("编辑镜像信息");
			shell.setBackground(new Color(null,255,255,255));

		}

		protected Control createDialogArea(Composite parent)
		{
			Composite composite = (Composite)super.createDialogArea(parent);
			composite.setLayout(new GridLayout(1, false));
			GridData comData = new GridData();
			comData.heightHint = 260;
			comData.widthHint = 430;
			composite.setLayoutData(comData);
			
			Composite description = new Composite(composite,SWT.BORDER);
			description.setBounds(0, 0, 431, 70);
			Label infoLabel = new Label(description,SWT.NONE);
			infoLabel.setText("修改镜像信息");
			infoLabel.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
			infoLabel.setBounds(5, 10, 400, 25);
			Label msgLabel = new Label(description,SWT.NONE);
			msgLabel.setText("修改ISO镜像信息，以及ISO镜像描述信息.ISO镜像命名只支持字母和数字.");
			msgLabel.setBounds(30, 40, 400, 25);
			
			nameLabel = new Label(composite,SWT.NONE);
			nameLabel.setText("镜像名称");
			GridData gridData = new GridData();
			gridData.widthHint = 410;
			gridData.horizontalIndent = 1;
			gridData.verticalAlignment = SWT.WRAP;
			gridData.grabExcessHorizontalSpace = true;
			nameText = new Text(composite,SWT.BORDER);
			nameText.setLayoutData(gridData);
			nameText.setText(name);
			
			descriptionLabel = new Label(composite,SWT.NONE);
			descriptionLabel.setText("描述信息");
			descriptionText = new Text(composite, SWT.MULTI| SWT.BORDER | SWT.WRAP |SWT.V_SCROLL);
			GridData gd_description = new GridData();
			gd_description.horizontalIndent = 1;
			gd_description.widthHint = 390;
			gd_description.heightHint = 82;
			descriptionText.setLayoutData(gd_description);
			descriptionText.setText(isoDescription);
			
			alertLabel = new Label(composite,SWT.NONE);
			alertLabel.setVisible(false);
			alertLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
			return parent;
		}
		@Override
		protected void createButtonsForButtonBar(Composite parent)
		{
			createButton(parent,EditIsoDialog.OK_ID,EditIsoDialog.OK_LABEL, true);
			createButton(parent,EditIsoDialog.CLOSE_ID,EditIsoDialog.CLOSE_LABEL,true);
		}
		
		protected void buttonPressed(int buttonId)
		{
			if(buttonId == EditIsoDialog.OK_ID)
			{
				String newName = nameText.getText();
				if(!newName.equals("") && newName != null)
					name = newName;
				isoDescription = descriptionText.getText();
				int isoInedex = name.lastIndexOf(".iso");
				if(isoInedex == -1)
					name += ".iso";
				if(!isValidName(name,isoInedex))
				{
					alertLabel.setText("ISO镜像只支持英文字母、数字和下划线.");
					alertLabel.pack();
					alertLabel.setVisible(true);
				}
				else
				{	
					alertLabel.setVisible(false);
					VDI vdi = vdiList.get(index).getVdi();
					RenameIsoJob job = new RenameIsoJob(objectSR.getConnection(),name,vdi);
					job.schedule();
					close();
				}
			}
			else if(buttonId == EditIsoDialog.CLOSE_ID)
				close();
		
		}
	}
//	
//	public boolean isGBK(String s){
//		return s.getBytes().length==s.length()?false:true;
//	}
	
	public boolean isValidName(String name, int index)
	{
		String newName = null;
		if(index == -1)
			newName = name;
		else 
			newName = name.split(".iso")[0];
		String regex = "^[0-9a-zA-Z_]{1,}$";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(newName);
		if(m.find())
			return true;
		return false;
	}
	
	class RenameIsoJob extends Job
	{

		private Connection connection;
		private String name;
		private VDI vdi;
		private Display display;
		public RenameIsoJob(Connection connection, String name, VDI vdi) {
			super("镜像重命名");
			
			this.connection = connection;
			this.name = name;
			this.vdi = vdi;
			display = PlatformUI.getWorkbench().getDisplay();
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			monitor.beginTask("镜像重命名....", 100);
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	editButton.setEnabled(false);
			        }
			    };
			    this.display.syncExec(runnable); 
			}
			try {
				vdi.setNameLabel(connection, name);
				SrJob job = new SrJob(objectSR,PlatformUI.getWorkbench().getDisplay());
				job.schedule();
			} catch (BadServerResponse e) {
				
				e.printStackTrace();
			} catch (XenAPIException e) {
				
				e.printStackTrace();
			} catch (XmlRpcException e) {
				
				e.printStackTrace();
			}
			if (!this.display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run(){
			        	editButton.setEnabled(true);
			        }
			    };
			    this.display.syncExec(runnable); 
			}
			monitor.done();
			return Status.OK_STATUS;
		}
		
	}
}


