package oncecenter.wizard.newvmfromtemp;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import oncecenter.util.ImageRegistry;
import oncecenter.util.TypeUtil;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
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
import org.eclipse.wb.swt.SWTResourceManager;

import com.once.xenapi.SR;

public class SelectStorageWizardPage extends NewVMPage {

	TableViewer tableViewer;
	private Table table;
	ArrayList<Storage> srs = new ArrayList<Storage>();
	private String input = null;
	VMTreeObject template;
	Button editButton ;

	/**
	 * Create the wizard.
	 */
	public SelectStorageWizardPage() {
		super("wizardPage");
		setTitle("硬盘设置");
		setDescription("为虚拟机选择一个硬盘存储位置并设定硬盘大小");
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		
		getStorageList();
		template = ((NewVmFTWizard)this.getWizard()).selectedTemp;
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(1,false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite operateComp = new Composite(composite,SWT.NULL);
		operateComp.setLayout(new GridLayout(3,false));

		
		table = new Table( composite, SWT.BORDER | SWT.V_SCROLL
		        | SWT.H_SCROLL|SWT.FULL_SELECTION|SWT.MULTI);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setLinesVisible(true);


		TableColumn tc1 = new TableColumn(table, SWT.CENTER);
		TableColumn tc2 = new TableColumn(table, SWT.CENTER);
		TableColumn tc3 = new TableColumn(table, SWT.CENTER);
		TableColumn tc4 = new TableColumn(table, SWT.CENTER);
		TableColumn tc6 = new TableColumn(table, SWT.CENTER);
		TableColumn tc5 = new TableColumn(table, SWT.CENTER);
		tc1.setText("   ");
		tc2.setText("位置");
		tc3.setText("类型");
		tc4.setText("大小(G)");
		tc6.setText("可用大小(G)");
		tc5.setText("共享");
		tc1.setWidth(30);
		tc2.setWidth(180);
		tc3.setWidth(80);
		tc4.setWidth(60);
		tc6.setWidth(80);
		tc5.setWidth(60);
		table.setHeaderVisible(true);

		tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(new StorageContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider());
		
		tableViewer.setInput(srs);
		int flag = 0;
		for(Storage s:srs){
			if(s.getSize()>0)
				break;
			else
				flag++;
		}
		if(flag<srs.size()){
			table.setSelection(flag);
		}
		
		table.pack();
		
		if(table.getItemCount()==0)
			this.setPageComplete(false);
		
		editButton = new Button(composite,SWT.PUSH);
		editButton.setText("设置硬盘大小");
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.END;
		editButton.setLayoutData(gridData);
		
		editButton.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				widgetSelected(arg0);
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				int index = table.getSelectionIndex();
				if(index != -1)
				{
					EditDialog dialog = new EditDialog(Display.getCurrent().getActiveShell(),index);
					dialog.open();
				}
			}
			
		});
		if(srs.isEmpty())
			editButton.setEnabled(false);
		//如果选择用模板创建，不用选择存储
		if(template != null)
		{	
			table.setVisible(false);
			editButton.setVisible(false);
		}
		setControl(composite);

	}
	
	private void getStorageList()
	{
		boolean isAssignHost = ((NewVmFTWizard)this.getWizard()).isAssignHost;
		if(srs != null )
			srs.clear();
		if(((NewVmFTWizard)this.getWizard()).isPool){
			for(VMTreeObjectSR o:((NewVmFTWizard)this.getWizard()).srs){
				if(TypeUtil.getDiskSRTypes().contains(o.getSrType())
						&&!o.getSrType().equals(TypeUtil.localSrType)){
					Storage sr = new Storage(10,o);
					int index=0;
					for(;index<srs.size();index++){
						if(srs.get(index).getMaxSize()<sr.maxSize)
							break;
					}
					srs.add(index, sr);
				}
			}
			if(isAssignHost){
				VMTreeObjectSR localSR = (VMTreeObjectSR)((NewVmFTWizard)this.getWizard()).selectedSR;
				if(localSR != null)
				{
					Storage sr = new Storage(10,localSR);
					int index=0;
					for(;index<srs.size();index++){
						if(srs.get(index).getMaxSize()<sr.maxSize)
							break;
					}
					srs.add(index, sr);
				}
			}
		}
		else
		{
			VMTreeObjectSR localSR = (VMTreeObjectSR)((NewVmFTWizard)this.getWizard()).selectedSR;
			if(localSR != null)
			{
				Storage sr = new Storage(10,localSR);
				int index=0;
				for(;index<srs.size();index++){
					if(srs.get(index).getMaxSize()<sr.maxSize)
						break;
				}
				srs.add(index, sr);
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
			 if(element instanceof Storage) {
			   switch(columnIndex) {
			   case 0:
			    return ImageRegistry.getImage(ImageRegistry.STORAGE);
			   }
			  }
			  return null;
		 }

		 @Override
		 public String getColumnText(Object element, int columnIndex) {
		  if(element instanceof Storage) {
			  Storage storage=(Storage)element;
		   switch(columnIndex) {
		   case 0:
			   return "";
		   
		   case 1:
//			   if(storage.getObject().getParent()==null){
//				   System.out.println("这个sr没有parent"+storage.getObject().getName());
//			   }
		   {
			   VMTreeObjectSR srObject = storage.getObject();
			   if(srObject.getSrType().equals(TypeUtil.localSrType)){
				   return srObject.getName() + " on " + srObject.getParent().getName();
			   }else {
				   SR.Record srRecord = srObject.getRecord();
				   String location = "";
				   if(srRecord!=null){
					   String descrip = srRecord.nameDescription;
					   if(descrip!=null){
						   String [] m = descrip.split(":");
						   if(m!=null&&m.length>1){
							   String [] n = m[1].split(" ");
							   if(n!=null&&n.length>1){
								   location += n[0];
							   }
						   }
					   }
				   }
				   if(location.length()>0){
					   return srObject.getName() + " on " + location;
				   }else{
					   return srObject.getName();
				   }
			   }
		   }
			   
		   case 2:
		   {
			   String type = storage.getObject().getSrType();
			   if(type.equals("nfs_zfs")){
					return "gluster_zfs";
				}else{
					return type;
				}
		   }

		   case 3:
			   return storage.getSize()+"";
		   case 4:
			   return storage.getMaxSize()+"";
		   case 5:
			   return storage.getObject().getSrType().equals("local")?"不共享":"共享";
		  
		   }
		  }
		  
		  return null;
		 }
	}

	private boolean isValid(String inputSize,Storage sr)
	{
		//如果输入为空，返回false
		if(inputSize.length() == 0 || inputSize.equals(null))
			return false;
		//利用正则表达式判断输入是不是浮点数或整数数值
		Pattern p = Pattern.compile("^(\\+|-)?\\d+$",Pattern.CANON_EQ);
		Matcher matcher = p.matcher(inputSize);
		if(!matcher.find())
			return false;
		//判断输入不是超出最大最小值范围
		double size = Double.parseDouble(inputSize);
		if(size > sr.maxSize || size<= sr.minSize)
			return false;
		return true;
	}
	
	public void refresh()
	{
		if(this.getControl() == null)
			return;
		if(template != null)
		{	
			table.setVisible(false);
			editButton.setVisible(false);
		}
		else
		{
			getStorageList();
			tableViewer.setInput(srs);
			table.select(0);
		}
	}
	class EditDialog extends Dialog
	{
		private static final int OK_ID = 0;
		private static final String OK_LABEL = "确定";
		private static final int CLOSE_ID = 1;
		private static final String CLOSE_LABEL = "取消";
		
		//对话框页面控件
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
			shell.setText("设置硬盘大小");
			shell.setBackground(new Color(null,255,255,255));
		}
		protected Control createDialogArea(Composite parent)
		{
		
			Storage sr = srs.get(index);
			
			Composite composite = new Composite(parent,SWT.NONE);
			GridLayout layout = new GridLayout(3,true);
			layout.makeColumnsEqualWidth = false;
			composite.setLayout(layout);
			composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			locationLabel = new Label(composite,SWT.NONE);
			locationLabel.setText("存储位置  ：");
			locationText = new Text(composite,SWT.NONE);
			locationText.setText(sr.getObject().getName() + " on "+sr.getObject().getParent().getName());
			locationText.setEditable(false);
			new Label(composite,SWT.NONE);
			
			typeLabel = new Label(composite,SWT.NONE);
			typeLabel.setText("存储类型  ：");
			typeText = new Text(composite, SWT.NONE);
			typeText.setText(sr.getObject().getSrType());
			typeText.setEditable(false);
			new Label(composite,SWT.NONE);
			
			sizeLabel = new Label(composite, SWT.NONE);
			sizeLabel.setText("存储大小  ：");
			sizeText = new Text(composite, SWT.BORDER);
			sizeText.setFocus();
			sizeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			sizeText.setText(sr.getSize()+"");
			new Label(composite,SWT.NONE).setText("G");
//		 	sizeText.addModifyListener( new ModifyListener(){  
//		         public void modifyText(ModifyEvent e) {
//		        	input = sizeText.getText();
//		        	Storage storage = (Storage)tableViewer.getElementAt(table.getSelectionIndex());
//		         	if(!isValid(input,storage))
//		         	{
//		         		String errorMsg = "请输入在" + storage.minSize + "--" + storage.maxSize + "之间的正整数";
//		        		InputErrorDialog dialog = new InputErrorDialog(Display.getCurrent().getActiveShell(),errorMsg);
//		        		dialog.open();
//		         	}
//		         	int index = table.getSelectionIndex();
//		        	srs.get(index).setSize(input);
//		         }
//		        }) ;
			shareLabel = new Label(composite,SWT.NONE);
			shareLabel.setText("是否共享  ：");
			shareText = new Text(composite,SWT.NONE);
			shareText.setText(sr.getObject().getSrType().equals("local")?"不共享":"共享");
			shareText.setEditable(false);
			new Label(composite,SWT.NONE);
			
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
				input = sizeText.getText().split("G")[0];
				System.out.println("input:" + input);
	        	Storage storage = (Storage)tableViewer.getElementAt(table.getSelectionIndex());
	         	if(!isValid(input,storage))
	         	{
	         		String msg = "请输入在" + storage.minSize + "--" + storage.maxSize + "之间的正整数";
	         		errorMsg.setText(msg);
	         	}
	         	else
	         	{
	         		int index = table.getSelectionIndex();
		        	srs.get(index).setSize(input);
					tableViewer.setInput(srs);
					//tableViewer.refresh();
					this.close();
	         	}
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
		
		
	class Storage {
		private Long size;
		private VMTreeObjectSR object;
		private double maxSize;
		private double minSize;
		public Storage(double size,VMTreeObjectSR object){
			//this.setSize(size);
			this.setObject(object);
			SR.Record srRecord = (SR.Record)object.getRecord();
			if(srRecord.physicalSize==null||srRecord.physicalUtilisation==null){
				this.setMaxSize(Double.MAX_VALUE);
			}else{
				this.setMaxSize((srRecord.physicalSize - srRecord.physicalUtilisation)/1024/1024/1024);
			}
//			System.out.println(srRecord.nameLabel+"的总大小是"+srRecord.physicalSize);
//			System.out.println(srRecord.nameLabel+"的使用量是"+srRecord.physicalUtilisation);
//			System.out.println(srRecord.nameLabel+"的剩余空间是"+this.maxSize+"G");
			this.setMinSize(0);
			this.setSize((size>this.getMaxSize())?this.getMaxSize():size);
		}
		
		public VMTreeObjectSR getObject() {
			return object;
		}
		public void setObject(VMTreeObjectSR object) {
			this.object = object;
		}

		public double getMaxSize() {
			return maxSize;
		}

		public void setMaxSize(double maxSize) {
			this.maxSize = maxSize;
		}

		public double getMinSize() {
			return minSize;
		}

		public void setMinSize(double minSize) {
			this.minSize = minSize;
		}

		public Long getSize() {
			return size;
		}

		public void setSize(Long size) {
			this.size = size;
		}
		
		public void setSize(String size) {
			this.size = Long.parseLong(size);
		}
		
		public void setSize(double size) {
			this.size = (long) Math.floor(size);
		}
	}
	@Override
	protected boolean nextButtonClick() {
		
		int index = table.getSelectionIndex();
		
		if(index != -1&&table.getItemCount()>0)
		{
			Storage sr = srs.get(index);
			((NewVmFTWizard)this.getWizard()).storage = sr.getSize();
			((NewVmFTWizard)this.getWizard()).selectedSR = sr.getObject();
			((NewVmFTWizard)this.getWizard()).isShare = sr.getObject().getSrType().equals("local")?
					false:true;
			 IWizardPage nextPage = getWizard().getNextPage(this);
			 if(nextPage instanceof FinishWizardPage)
				 ((FinishWizardPage)nextPage).refresh();
			return true;
		}
		return false;
		
	}
	
	
}
