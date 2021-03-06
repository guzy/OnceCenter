package oncecenter.action.addlostelement;

import java.util.ArrayList;
import java.util.Collection;

import oncecenter.util.ImageRegistry;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class AddLostHostDialog extends Dialog {

	ArrayList<VMTreeObjectHost> lostHosts;
	
	Table table;
	CheckboxTableViewer tableViewer;
	
	protected AddLostHostDialog(Shell parentShell,ArrayList<VMTreeObjectHost> lostHosts) {
		super(parentShell);
		this.lostHosts = lostHosts;
		
	}
	
	@Override
	protected void configureShell(Shell shell){
		super.configureShell(shell);
		shell.setText("警告");
	}
	
	@Override 
	protected Point getInitialSize() { 
	return new Point(500,400); 
	} 

	protected Control createDialogArea(Composite parent) { 
		Composite composite = new Composite(parent, SWT.NONE); 
		GridLayout layout = new GridLayout(); 
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		 layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		 layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		 layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		 composite.setLayout(layout); 
		composite.setLayoutData(new GridData(GridData.FILL_BOTH)); 
		applyDialogFont(composite); 
		
		CLabel alarm = new CLabel(composite, SWT.NONE);
		alarm.setImage(ImageRegistry.getImage(ImageRegistry.SERVICENOTOPEN));
		alarm.setText("以下主机丢失，请选择需要重新加入资源池的主机：");
		
		table = new Table( composite, SWT.BORDER | SWT.V_SCROLL
		        | SWT.H_SCROLL|SWT.FULL_SELECTION|SWT.MULTI | SWT.CHECK );
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setLinesVisible(true);

		TableColumn tc1 = new TableColumn(table, SWT.CENTER);
		TableColumn tc2 = new TableColumn(table, SWT.CENTER);
		TableColumn tc3 = new TableColumn(table, SWT.CENTER);
		tc1.setText("   ");
		tc2.setText("名称");
		tc3.setText("ip地址");
		tc1.setWidth(30);
		tc2.setWidth(150);
		tc3.setWidth(150);
		table.setHeaderVisible(true);
		
		tableViewer = new CheckboxTableViewer(table);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider());
		
		tableViewer.setInput(lostHosts);
		
		return composite; 
	} 
	
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

		 @Override
		 public Image getColumnImage(Object element, int columnIndex) {
//			 if(element instanceof VMTreeObjectHost) {
//			   switch(columnIndex) {
//			   case 0:
//					return ImageRegistry.getImage(ImageRegistry.SERVERCONNECT);
//			   }
//			  }
			  return null;
		 }

		 @Override
		 public String getColumnText(Object element, int columnIndex) {
		  if(element instanceof VMTreeObjectHost) {
			  VMTreeObjectHost host = (VMTreeObjectHost)element;
		   switch(columnIndex) {
		   case 0:
			   return "";
		   
		   case 1:
			   return host.getName();
			   
		   case 2:
			   return host.getIpAddress();
		  
		   }
		  }
		  
		  return null;
		 }
	}
	
	@Override 
	protected Button createButton(Composite parent, int id, String label,boolean defaultButton) {
	 return null; 
	} 
	
	@Override 
	protected void initializeBounds() { 
	//我们可以利用原有的ID创建按钮,也可以用自定义的ID创建按钮 
	//但是调用的都是父类的createButton方法. 
	super.createButton((Composite)getButtonBar(), IDialogConstants.OK_ID, "确定", false);
	 super.createButton((Composite)getButtonBar(), IDialogConstants.CANCEL_ID, "取消", false);
	//下面这个方法一定要加上,并且要放在创建按钮之后,否则就不会创建按钮 
	super.initializeBounds(); 
	} 
	
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.OK_ID == buttonId) {
			lostHosts.clear();
			for(Object o : tableViewer.getCheckedElements()){
				if(o instanceof VMTreeObjectHost)
					lostHosts.add((VMTreeObjectHost)o);
			}
			super.okPressed();
		} else if (IDialogConstants.CANCEL_ID == buttonId) {
			super.cancelPressed();
		}
	}
}
