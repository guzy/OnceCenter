package oncecenter.wizard.newvmfromtemp;

import java.util.ArrayList;

import oncecenter.util.ImageRegistry;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectTemplate;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


public class SelectTemplateWizardPage extends NewVMPage {
	private VMTreeObject selection;

	private Table table;
	TableViewer tableViewer;
	private VMTreeObject selectedTemp;
	private ArrayList<VMTreeObjectTemplate> templates;
	
	ArrayList<wizardTemplate> temps = new ArrayList<wizardTemplate>();

	/**
	 * Create the wizard.
	 */
	public SelectTemplateWizardPage(VMTreeObject selection) {
		super("wizardPage");
		setTitle("选择模板");
		setDescription("请为新虚拟机选择一个模板");
		this.setSelection(selection);
		
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {

		templates = ((NewVmFTWizard)this.getWizard()).templates;
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(1,false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		table = new Table( composite, SWT.BORDER | SWT.V_SCROLL
		        | SWT.H_SCROLL|SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setLinesVisible(true);
		
		TableColumn tc1 = new TableColumn(table, SWT.CENTER);
		TableColumn tc2 = new TableColumn(table, SWT.LEFT);
		TableColumn tc3 = new TableColumn(table, SWT.CENTER);
		tc1.setText("  ");
		tc2.setText("名字");
		tc3.setText("类别");
		tc1.setWidth(33);
		tc2.setWidth(270);
		tc3.setWidth(100);
		table.setHeaderVisible(true);

		tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider());
		
		
//		for(VMTreeObject temp:templates){
//			wizardTemplate t = new wizardTemplate("Custom",temp.getName(),temp);
//			temps.add(t);
//		}
		
		//添加默认的模板		
		{
			wizardTemplate t = new wizardTemplate("Windows","Windows_7_32-bit",null);
			temps.add(t);
		}
		{
			wizardTemplate t = new wizardTemplate("Windows","Windows_7_64-bit",null);
			temps.add(t);
		}
		{
			wizardTemplate t = new wizardTemplate("Windows","Windows_Server_2003_32-bit",null);
			temps.add(t);
		}
		{
			wizardTemplate t = new wizardTemplate("Windows","Windows_Server_2003_64-bit",null);
			temps.add(t);
		}
		{
			wizardTemplate t = new wizardTemplate("CentOS","CentOS_4.5_32-bit",null);
			temps.add(t);
		}
		{
			wizardTemplate t = new wizardTemplate("CentOS","CentOS_4.6_32-bit",null);
			temps.add(t);
		}
		{
			wizardTemplate t = new wizardTemplate("CentOS","CentOS_4.7_32-bit",null);
			temps.add(t);
		}
		{
			wizardTemplate t = new wizardTemplate("CentOS","CentOS_4.8_32-bit",null);
			temps.add(t);
		}
		{
			wizardTemplate t = new wizardTemplate("CentOS","CentOS_5_32-bit",null);
			temps.add(t);
		}
		{
			wizardTemplate t = new wizardTemplate("CentOS","CentOS_5_64-bit",null);
			temps.add(t);
		}
		{
			wizardTemplate t = new wizardTemplate("RedHat","Red_Hat_Enterprice_Linux_4.5_32-bit",null);
			temps.add(t);
		}
		{
			wizardTemplate t = new wizardTemplate("RedHat","Red_Hat_Enterprice_Linux_4.6_32-bit",null);
			temps.add(t);
		}
		{
			wizardTemplate t = new wizardTemplate("RedHat","Red_Hat_Enterprice_Linux_4.7_32-bit",null);
			temps.add(t);
		}
		{
			wizardTemplate t = new wizardTemplate("RedHat","Red_Hat_Enterprice_Linux_4.8_32-bit",null);
			temps.add(t);
		}
		{
			wizardTemplate t = new wizardTemplate("RedHat","Red_Hat_Enterprice_Linux_5_32-bit",null);
			temps.add(t);
		}
		{
			wizardTemplate t = new wizardTemplate("RedHat","Red_Hat_Enterprice_Linux_5_64-bit",null);
			temps.add(t);
		}
		tableViewer.setInput(temps);
		table.setSelection(0);
//		TableItem item2 = new TableItem(table, SWT.NONE);
//		item2.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
//				ImageRegistry.getImagePath(ImageRegistry.WINDOWS)).createImage());
//		item2.setText(new String[] { "", "Windows 7 (32-bit)", "Windows" });
//		
//		TableItem item3 = new TableItem(table, SWT.NONE);
//		item3.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
//				ImageRegistry.getImagePath(ImageRegistry.WINDOWS)).createImage());
//		item3.setText(new String[] { "", "Windows 7 (64-bit)", "Windows" });
//		
//		TableItem item4 = new TableItem(table, SWT.NONE);
//		item4.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
//				ImageRegistry.getImagePath(ImageRegistry.WINDOWS)).createImage());
//		item4.setText(new String[] { "", "Windows Server 2003 (32-bit)", "Windows" });
//		
//		TableItem item5 = new TableItem(table, SWT.NONE);
//		item5.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
//				ImageRegistry.getImagePath(ImageRegistry.WINDOWS)).createImage());
//		item5.setText(new String[] { "", "Windows Server 2003 (64-bit)", "Windows" });
//		
//		TableItem item6 = new TableItem(table, SWT.NONE);
//		item6.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
//				ImageRegistry.getImagePath(ImageRegistry.CENTOS)).createImage());
//		item6.setText(new String[] { "", "CentOS 4.5 (32-bit)", "CentOS" });
//		
//		TableItem item7 = new TableItem(table, SWT.NONE);
//		item7.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
//				ImageRegistry.getImagePath(ImageRegistry.CENTOS)).createImage());
//		item7.setText(new String[] { "", "CentOS 4.6 (32-bit)", "CentOS" });
//		
//		TableItem item8 = new TableItem(table, SWT.NONE);
//		item8.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
//				ImageRegistry.getImagePath(ImageRegistry.CENTOS)).createImage());
//		item8.setText(new String[] { "", "CentOS 4.7 (32-bit)", "CentOS" });
//		
//		TableItem item9 = new TableItem(table, SWT.NONE);
//		item9.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
//				ImageRegistry.getImagePath(ImageRegistry.CENTOS)).createImage());
//		item9.setText(new String[] { "", "CentOS 4.8 (32-bit)", "CentOS" });
//		
//		TableItem item10 = new TableItem(table, SWT.NONE);
//		item10.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
//				ImageRegistry.getImagePath(ImageRegistry.CENTOS)).createImage());
//		item10.setText(new String[] { "", "CentOS 5 (32-bit)", "CentOS" });
//		
//		TableItem item11 = new TableItem(table, SWT.NONE);
//		item11.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
//				ImageRegistry.getImagePath(ImageRegistry.CENTOS)).createImage());
//		item11.setText(new String[] { "", "CentOS 5 (64-bit)", "CentOS" });
//
//		table.addListener(SWT.DefaultSelection, new Listener() {
//		      public void handleEvent(Event e) {
//		        
//		      }
//		    });


		table.pack();

//		final Color red = parent.getDisplay().getSystemColor(SWT.COLOR_RED);
//		final Color yellow = parent.getDisplay().getSystemColor(
//				SWT.COLOR_YELLOW);
//		table.setSelection(0);
//		table.addListener(SWT.EraseItem, new Listener() {
//			public void handleEvent(Event event) {
//				event.detail &= ~SWT.HOT;
//				if ((event.detail & SWT.SELECTED) == 0)
//					return; /* item not selected */
//				int clientWidth = table.getClientArea().width;
//				GC gc = event.gc;
//				Color oldForeground = gc.getForeground();
//				Color oldBackground = gc.getBackground();
//				gc.setForeground(red);
//				gc.setBackground(yellow);
//				gc.fillGradientRectangle(0, event.y, clientWidth, event.height,
//						false);
//				gc.setForeground(oldForeground);
//				gc.setBackground(oldBackground);
//				event.detail &= ~SWT.SELECTED;
//			}
//		});

		table.getSelectionIndex();
		

		setControl(composite);
	}
	
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

		 @Override
		 public Image getColumnImage(Object element, int columnIndex) {
			 if(element instanceof wizardTemplate) {
				 wizardTemplate temp = (wizardTemplate) element;
				 switch(columnIndex) {
				   case 0:
					   if(temp.getType().equals("Custom")){
						   return ImageRegistry.getImage(ImageRegistry.TEMPLATE);
					   }else if(temp.getType().equals("Windows")){
						   return ImageRegistry.getImage(ImageRegistry.WINDOWS);
					   }else if(temp.getType().equals("CentOS")){
						   return ImageRegistry.getImage(ImageRegistry.CENTOS);
					   }else if(temp.getType().equals("RedHat")){
						   return ImageRegistry.getImage(ImageRegistry.REDHAT);
					   }
				   }   
			  }
			  
			  return null;
		 }

		 @Override
		 public String getColumnText(Object element, int columnIndex) {
		  if(element instanceof wizardTemplate) {
			  wizardTemplate temp=(wizardTemplate)element;
		   switch(columnIndex) {
		   case 0:
		    return " ";
		   
		   case 1:
			   return temp.getName();
		   case 2:
			   return temp.getType();
		   }
		  }
		  
		  return null;
		 }
	}
	
	public class wizardTemplate {
		private String type;
		private String name;
		private VMTreeObject object;
		public wizardTemplate(String type,String name,VMTreeObject object){
			this.type=type;
			this.name=name;
			this.object=object;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public VMTreeObject getObject() {
			return object;
		}
		public void setObject(VMTreeObject object) {
			this.object = object;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
	}
	public VMTreeObject getSelection() {
		return selection;
	}

	public void setSelection(VMTreeObject selection) {
		this.selection = selection;
	}

	public ArrayList<VMTreeObjectTemplate> getTemplates() {
		return templates;
	}

	public void setTemplates(ArrayList<VMTreeObjectTemplate> templates) {
		this.templates = templates;
	}

	public VMTreeObject getSelectedTemp() {
		return selectedTemp;
	}

	public void setSelectedTemp(VMTreeObject selectedTemp) {
		this.selectedTemp = selectedTemp;
	}

	@Override
	protected boolean nextButtonClick() {
		int index = table.getSelectionIndex();
		wizardTemplate temp = temps.get(index);
		((NewVmFTWizard)this.getWizard()).templateName = temp.getName();
    	((NewVmFTWizard)this.getWizard()).selectedTemp = temp.getObject();
    	 IWizardPage nextPage = getWizard().getNextPage(this);
		 if(nextPage instanceof VmNameWizardPage)
			 ((VmNameWizardPage)nextPage).refresh();
		return true;
	}
}
