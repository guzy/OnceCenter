package oncecenter.wizard.editvmdisk;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import oncecenter.util.ImageRegistry;
import oncecenter.util.VMUtil;
import oncecenter.util.dialog.ErrorMessageDialog;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectDefault;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.wizard.newvmfromtemp.NewVmFTWizard;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.once.xenapi.Connection;
import com.once.xenapi.Host;
import com.once.xenapi.SR;
import com.once.xenapi.VBD;
import com.once.xenapi.VDI;
import com.once.xenapi.VM;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.XenAPIException;

public class EditVmDiskWizardPage extends WizardPage {

	private ViewForm viewForm;
	private ToolBar toolBar;
	private Composite tableComp;
	private Table table;
	private TableViewer tableViewer;
	private EditVmDiskWizard wizard;
	private ToolItem addItem;
	private ToolItem delItem;
	private VMTreeObjectSR srObject;
	private VM vm;
	private ArrayList<Fiber> fiberList = new ArrayList<Fiber>();
	private VMTreeObjectVM selection;
	private Host host;
	private VMTreeObjectHost hostObject;
	private Connection connection;
	
	protected EditVmDiskWizardPage(String pageName,VMTreeObjectVM selection) {
		super(pageName);
		
		this.setTitle("�޸Ĺ�������");
		this.setDescription("Ϊ�������ӻ�ɾ�������豸��Ŀǰ�����ֻ֧�����һ������.");
		this.selection = selection;
		this.connection = selection.getConnection();
		host = selection.getRecord().residentOn;
		VMTreeObjectRoot root;
		if(selection.getParent().getParent() instanceof VMTreeObjectDefault){
			root = (VMTreeObjectRoot)selection.getParent();
		}else{
			root = (VMTreeObjectRoot)selection.getParent().getParent();
		}
		hostObject = root.hostMap.get(host);
	}

	@Override
	public void createControl(Composite arg0) {
		
		wizard = (EditVmDiskWizard)this.getWizard();
		srObject = wizard.srObject;
		host = wizard.host;
		vm = wizard.vm;
		connection = wizard.connection;
		getFibers();
		viewForm = new ViewForm(arg0,SWT.NONE);
		viewForm.setTopCenterSeparate(true);
		createToolBar();
		viewForm.setTopLeft(toolBar);
		createTableComp();
		viewForm.setContent(tableComp);
		this.setControl(viewForm);
	}

	private void createToolBar()
	{
		toolBar = new ToolBar(viewForm,SWT.FLAT|SWT.RIGHT);
		addItem = new ToolItem(toolBar,SWT.PUSH);
		addItem.setText("��ӹ���");
		
		 
		delItem = new ToolItem(toolBar,SWT.PUSH);
		delItem.setText("ɾ������");
		
		if(fiberList == null || fiberList.size() == 0)
		{	
			delItem.setEnabled(false);
			delItem.setToolTipText("�޹��ˣ�ɾ������������");
			delItem.setImage(ImageRegistry.getImage(ImageRegistry.DELITEMUNABLE));
			
			addItem.setEnabled(true);
			addItem.setToolTipText("��ӹ��˲���");
			addItem.setImage(ImageRegistry.getImage(ImageRegistry.ADDITEM));
		}
		else
		{	
			addItem.setEnabled(false);
			addItem.setToolTipText("���й��ˣ���Ӳ���������");
			addItem.setImage(ImageRegistry.getImage(ImageRegistry.ADDITEMUNABLE));
			
			delItem.setEnabled(true);
			delItem.setToolTipText("ɾ�����˲���");
			delItem.setImage(ImageRegistry.getImage(ImageRegistry.DELITEM));
		}
		
		/**action.*/
		addItem.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				widgetSelected(arg0);
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				AddDiskDialog dialog = new AddDiskDialog(Display.getCurrent().getActiveShell(),wizard);
				dialog.open();
			}
			
		});
		
		delItem.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				widgetSelected(arg0);
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				/**
				 * ɾ�����˲���
				 */
				int index = table.getSelectionIndex();
				if(index != -1)
				{
					VBD vbd = fiberList.get(index).getVbd();
					try {
						vm.destroyFiber(connection,vbd);
					} catch (BadServerResponse e) {
						
						e.printStackTrace();
					} catch (XenAPIException e) {
						
						e.printStackTrace();
					} catch (XmlRpcException e) {
						
						e.printStackTrace();
					}
					getFibers();
					tableViewer.setInput(fiberList);
					/**
					 * ɾ�����˺󣬸ı�addItem����״̬
					 */
					addItem.setEnabled(true);
					delItem.setEnabled(false);
					delItem.setToolTipText("�޹��ˣ�ɾ������������");
					addItem.setToolTipText("��ӹ��˲���");
					addItem.setImage(ImageRegistry.getImage(ImageRegistry.ADDITEM));
					delItem.setDisabledImage(ImageRegistry.getImage(ImageRegistry.DELITEMUNABLE));
				}
				else
				{
					Image image = ImageRegistry.getImage(ImageRegistry.FAILURE);
					ErrorMessageDialog dialog = new ErrorMessageDialog(Display.getCurrent().getActiveShell(),"��ѡ��Ҫɾ���Ĺ��� \n ",image);
					dialog.open(); 
				}
			}
			
		});
		
	}
	
	private void createTableComp()
	{
		/**table composite content.*/
		tableComp = new Composite(viewForm,SWT.NONE);
		tableComp.setLayout(new GridLayout(1,false));
		tableComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		
		table = new Table( tableComp, SWT.BORDER | SWT.V_SCROLL
		        | SWT.H_SCROLL|SWT.FULL_SELECTION|SWT.MULTI);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setLinesVisible(true);


		TableColumn tc1 = new TableColumn(table, SWT.CENTER);
		TableColumn tc2 = new TableColumn(table, SWT.CENTER);
		TableColumn tc3 = new TableColumn(table, SWT.CENTER);
		TableColumn tc4 = new TableColumn(table, SWT.CENTER);
		TableColumn tc5 = new TableColumn(table, SWT.CENTER);
		tc1.setText("   ");
		tc2.setText("λ��");
//		tc3.setText("����");
//		tc4.setText("��С(G)");
//		tc5.setText("����");
		tc1.setWidth(30);
		tc2.setWidth(440);
//		tc3.setWidth(80);
//		tc4.setWidth(80);
//		tc5.setWidth(80);
		table.setHeaderVisible(true);

		tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(new StorageContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider());
		tableViewer.setInput(fiberList);
		table.setSelection(0);
		table.pack();
		
	}
	private void getFibers()
	{
		try {
			Set<VBD> fibersList = vm.getFibers(connection);
			if(fiberList != null || fiberList.size() != 0)
				fiberList.clear();
			for(VBD e:fibersList)
			{
//				VDI vdi = e.getVDI(connection);
//				String location = vdi.getLocation(connection);
				
				String location = vm.getVBDRecord(connection, e).userdevice;
				Fiber fiber = new Fiber("", location, e);
				fiberList.add(fiber);
				
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	class AddDiskDialog extends Dialog
	{
		private static final int OK_ID = 0;
		private static final String OK_LABEL = "ȷ��";
		private static final int CLOSE_ID = 1;
		private static final String CLOSE_LABEL = "ȡ��";
		
		private Button okButton;
		private Button closeButton;
		
		private CLabel addCLabel;
		private Combo diskCombo;
		private CLabel imageCLabel;
		private Button detectionButton;
		private String selectDiskAddress = "";
		private EditVmDiskWizard wizard;
		private Image image;
		protected AddDiskDialog(Shell parentShell,EditVmDiskWizard wizard) {
			super(parentShell);
			
			this.wizard = wizard;
		}
		
		
		protected void configureShell(Shell shell)
		{
			super.configureShell(shell);
			shell.setText("��ӹ���");
			shell.setBackground(new Color(null,255,255,255));

		}
		protected Control createDialogArea(Composite parent)
		{
			final Composite composite = new Composite(parent,SWT.NONE);
			GridData gridData = new GridData();
			gridData.heightHint = 250;
			gridData.widthHint = 430;
			composite.setLayoutData(gridData);
			Composite description = new Composite(composite,SWT.BORDER);
			description.setBounds(0, 0, 431, 70);
			Label infoLabel = new Label(description,SWT.NONE);
			infoLabel.setText("��ӹ���");
			infoLabel.setFont(SWTResourceManager.getFont("΢���ź�", 11, SWT.BOLD));
			infoLabel.setBounds(5, 10, 400, 25);
			Label msgLabel = new Label(description,SWT.NONE);
			msgLabel.setText("Ϊ��������һ���µĹ��˿�,������˿��Ƿ����.");
			msgLabel.setBounds(30, 40, 400, 25);
			image = ImageRegistry.getImage(ImageRegistry.ADDITEM);
			addCLabel = new CLabel(composite,SWT.NONE);
			addCLabel.setText("ѡ���¹��˿�");
			addCLabel.setImage(image);
			addCLabel.setBounds(20, 80, 200, 25);
			
			diskCombo = new Combo(composite,SWT.DROP_DOWN);
			diskCombo.setBounds(50, 120, 300, 30);
			for(String e:wizard.allFiberList)
			{
				diskCombo.add(e);
			}
			diskCombo.select(0);
			
			//��ȡ��������ڵ������Ĺ����豸
			if(hostObject.fiberList==null||hostObject.fiberList.isEmpty()){
				try {
					hostObject.fiberList = new ArrayList<String>(host.getAllFibers(connection));
				} catch (Exception e) {
					
					e.printStackTrace();
				}
			}
			
			
			
			detectionButton = new Button(composite,SWT.PUSH);
			detectionButton.setText("���");
			detectionButton.setBounds(370, 120, 50, 27);
			
        	imageCLabel = new CLabel(composite,SWT.NONE); 
			detectionButton.addSelectionListener(new SelectionListener(){

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
					
					widgetSelected(arg0);
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					
					imageCLabel.setVisible(true);
					int index = diskCombo.getSelectionIndex();
					if(index > -1)
						selectDiskAddress = diskCombo.getItem(index);
					if(checkDiskUsable(selectDiskAddress))
					{
						imageCLabel.setImage(ImageRegistry.getImage(ImageRegistry.RIGHT));
						imageCLabel.setBounds(140, 160, 200, 50);
						imageCLabel.setText("�˹��˿���ʹ��");
						okButton.setEnabled(true);
					}
					else
					{
						imageCLabel.setImage(ImageRegistry.getImage(ImageRegistry.FAILURE));
						imageCLabel.setBounds(140, 160, 200, 50);
						imageCLabel.setText("�˹��˿�������");
						okButton.setEnabled(false);
					}
					
				}
				
			});		
			return parent;
		}
		
		protected void createButtonsForButtonBar(Composite parent)
		{
			okButton = createButton(parent,AddDiskDialog.OK_ID,AddDiskDialog.OK_LABEL,true);
			okButton.setEnabled(false);
			closeButton = createButton(parent,AddDiskDialog.CLOSE_ID,AddDiskDialog.CLOSE_LABEL,true);
		}
		
		protected void buttonPressed(int buttonId)
		{
			if(AddDiskDialog.OK_ID == buttonId)
			{
				if(!addDisk(selectDiskAddress))
				{
					okButton.setEnabled(false);
					imageCLabel.setVisible(false);
					Image errorImage = ImageRegistry.getImage(ImageRegistry.FAILURE);
					ErrorMessageDialog dialog = new ErrorMessageDialog(Display.getCurrent().getActiveShell(),"��ӹ���ʧ�� \n������ѡ����˿�",errorImage);
					dialog.open();
				}
				else
				{
					getFibers();
					tableViewer.setInput(fiberList);
					table.setSelection(0);
					addItem.setEnabled(false);
					delItem.setEnabled(true);
					addItem.setToolTipText("���й��ˣ���Ӳ���������");
					delItem.setToolTipText("ɾ�����˲���");
					addItem.setDisabledImage(ImageRegistry.getImage(ImageRegistry.ADDITEMUNABLE));
					delItem.setImage(ImageRegistry.getImage(ImageRegistry.DELITEM));
					this.close();
				}
			}
			else if(AddDiskDialog.CLOSE_ID == buttonId)
			{
				this.close();
			}
		}
		
		private boolean checkDiskUsable(String diskAddress)
		{
			return true;
		}
		
		private boolean addDisk(String diskAddress)
		{
			try{
				String vmName = vm.getNameLabel(connection);
				SR sr = (SR)srObject.getApiObject();
				boolean flag =  VMUtil.createFiber(host, vmName, vm, sr, connection, diskAddress);
				return flag;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}
	}
	
	class StorageContentProvider implements IStructuredContentProvider 
	{
 
		protected TableViewer viewer;
		@Override
		public void dispose() {
			
		}

		@Override
		public Object[] getElements(Object inputElement) {
			
			if(inputElement instanceof List)
				return ((List)inputElement).toArray();
			return null;
		}



		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
			
		}
		
		
	}
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

		 @Override
		 public Image getColumnImage(Object element, int columnIndex) {
			 if(element instanceof Fiber) {
			   switch(columnIndex) {
			   case 0:
			    return ImageRegistry.getImage(ImageRegistry.STORAGE);
			   }
			  }
			  return null;
		 }

		 @Override
		 public String getColumnText(Object element, int columnIndex) {
		  if(element instanceof Fiber) {
			  Fiber fiber=(Fiber)element;
		   switch(columnIndex) {
		   case 0:
			   return "";
		   
		   case 1:
			   return fiber.getLocation();
		   }
		  }
		  
		  return null;
		 }
	}
	
	class Fiber {
		private String size;
		private String location;
		private VBD vbd;
		public Fiber(String size,String location,VBD vbd)
		{
			this.size = size;
			this.location = location;
			this.vbd = vbd;
		}
		public VBD getVbd() {
			return vbd;
		}
		public void setVbd(VBD vbd) {
			this.vbd = vbd;
		}
		public String getSize() {
			return size;
		}
		public void setSize(String size) {
			this.size = size;
		}
		public String getLocation() {
			return location;
		}
		public void setLocation(String location) {
			this.location = location;
		}
	}
	
	class EditDialog extends Dialog
	{
		private static final int OK_ID = 0;
		private static final String OK_LABEL = "ȷ��";
		private static final int CLOSE_ID = 1;
		private static final String CLOSE_LABEL = "ȡ��";
		
		//�Ի���ҳ��ؼ�
		private Label locationLabel;
		private Text locationText;
		
		private Label typeLabel;
		private Text typeText;
		
		private Label sizeLabel;
		private Text sizeText;
		
		private Label shareLabel;
		private Text shareText;
		
		private int index;
		private Label errorMsg;
		protected EditDialog(Shell parentShell,int index) {
			super(parentShell);
			
			this.index = index;
		}
		
		protected void configureShell(Shell shell)
		{
			super.configureShell(shell);
			shell.setText("����Ӳ�̴�С");
			shell.setBackground(new Color(null,255,255,255));
		}
		protected Control createDialogArea(Composite parent)
		{
		
			
			Composite composite = new Composite(parent,SWT.NONE);
			GridLayout layout = new GridLayout(2,true);
			layout.makeColumnsEqualWidth = false;
			composite.setLayout(layout);
			composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			locationLabel = new Label(composite,SWT.NONE);
			locationLabel.setText("�洢λ��  ��");
			locationText = new Text(composite,SWT.NONE);
			locationText.setText("����");
			locationText.setEditable(false);
			
			typeLabel = new Label(composite,SWT.NONE);
			typeLabel.setText("�洢����  ��");
			typeText = new Text(composite, SWT.NONE);
			typeText.setText("����");
			typeText.setEditable(false);
			
			sizeLabel = new Label(composite, SWT.NONE);
			sizeLabel.setText("�洢��С  ��");
			sizeText = new Text(composite, SWT.BORDER);
			sizeText.setFocus();
			sizeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			sizeText.setText("����");

			shareLabel = new Label(composite,SWT.NONE);
			shareLabel.setText("�Ƿ���  ��");
			shareText = new Text(composite,SWT.NONE);
			shareText.setText("����");
			shareText.setEditable(false);
			
			new Label(composite,SWT.NONE);
			errorMsg = new Label(composite,SWT.NONE);
			errorMsg.setText("                                            ");
			errorMsg.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
			
			return parent;
		}
		
		protected void createButtonsForButtonBar(Composite parent)
		{
			createButton(parent,EditDialog.OK_ID,EditDialog.OK_LABEL,true);
			createButton(parent,EditDialog.CLOSE_ID,EditDialog.CLOSE_LABEL,true);
		}
		
		protected void buttonPressed(int buttonId)
		{
			if(EditDialog.OK_ID == buttonId)
			{

			}
			else if(EditDialog.CLOSE_ID == buttonId)
			{
				this.close();
			}
		}

		
		
	}
	class InputErrorDialog extends Dialog
	{
		private CLabel imageCLabel;
		
		private String errorMsg = null;
		private Label errorLabel;
		private static final int CLOSE_ID = 0;
		private static final String CLOSE_LABEL = "Close";
		protected InputErrorDialog(Shell parentShell,String errorMsg) {
			super(parentShell);
			
			this.errorMsg = errorMsg;
			
		}
		
		protected Control createDialogArea(Composite parent)
		{
			Composite composite = (Composite)super.createDialogArea(parent);
			GridLayout layout = new GridLayout(2,true);
			layout.verticalSpacing = 22;
			layout.horizontalSpacing = 15;
			layout.makeColumnsEqualWidth = false;
			composite.setLayout(layout);
			imageCLabel = new CLabel(composite, SWT.NONE);
			imageCLabel.setImage(ImageRegistry.getImage(ImageRegistry.SERVICENOTOPEN));
			GridData imgData = new GridData();
			imgData.verticalSpan = 2;
			imageCLabel.setLayoutData(imgData);

			errorLabel = new Label(composite, SWT.NONE);
			errorLabel.setText(errorMsg);
			
			return parent;
		}
		
		
		protected void createButtonsForButtonBar(Composite parent)
		{
			createButton(parent,InputErrorDialog.CLOSE_ID,InputErrorDialog.CLOSE_LABEL,true);
		}
		
		protected void buttonPressed(int buttonId)
		{
			if(InputErrorDialog.CLOSE_ID == buttonId)
				close();
		}
	}
}
