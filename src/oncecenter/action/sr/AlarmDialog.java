package oncecenter.action.sr;

import java.util.List;

import oncecenter.util.ImageRegistry;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
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

import com.once.xenapi.Types;
import com.once.xenapi.VM;

public class AlarmDialog extends Dialog {

	List<VMTreeObjectVM> relateVmList;
	
	protected AlarmDialog(Shell parentShell,List<VMTreeObjectVM> relateVmList) {
		super(parentShell);
		this.relateVmList = relateVmList;
		
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
		alarm.setText("以下虚拟机会由于该操作而不可用：");
		
		Table table = new Table( composite, SWT.BORDER | SWT.V_SCROLL
		        | SWT.H_SCROLL|SWT.FULL_SELECTION|SWT.MULTI);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setLinesVisible(true);


		TableColumn tc1 = new TableColumn(table, SWT.CENTER);
		TableColumn tc2 = new TableColumn(table, SWT.CENTER);
		TableColumn tc3 = new TableColumn(table, SWT.CENTER);
		tc1.setText("   ");
		tc2.setText("名称");
		tc3.setText("状态");
		tc1.setWidth(30);
		tc2.setWidth(200);
		tc3.setWidth(80);
		table.setHeaderVisible(true);
		
		TableViewer tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider());
		
		tableViewer.setInput(relateVmList);
		
		return composite; 
	} 
	
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

		 @Override
		 public Image getColumnImage(Object element, int columnIndex) {
			 if(element instanceof VMTreeObjectVM) {
			   switch(columnIndex) {
			   case 0:
				   VMTreeObjectVM vm = (VMTreeObjectVM)element;
				   Types.VmPowerState state = vm.getRecord().powerState;
					
					if(state.equals(Types.VmPowerState.RUNNING)){
							return ImageRegistry.getImage(ImageRegistry.VMON);
					}else if(state.equals(Types.VmPowerState.SUSPENDED)){
							return ImageRegistry.getImage(ImageRegistry.VMSUSPEND);
					}else{
							return ImageRegistry.getImage(ImageRegistry.VMOFF);
					}
			   }
			  }
			  return null;
		 }

		 @Override
		 public String getColumnText(Object element, int columnIndex) {
		  if(element instanceof VMTreeObjectVM) {
			  VMTreeObjectVM vm = (VMTreeObjectVM)element;
		   switch(columnIndex) {
		   case 0:
			   return "";
		   
		   case 1:
			   return vm.getName();
			   
		   case 2:
			   VM.Record vmRecord = (VM.Record)vm.getRecord();
			   return vmRecord.powerState.equals(Types.VmPowerState.RUNNING)?"运行中":"已关闭";
		  
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
	//super.createButton((Composite)getButtonBar(), IDialogConstants.OK_ID, "确定", false);
	 super.createButton((Composite)getButtonBar(), IDialogConstants.CANCEL_ID, "确定", false);
	//下面这个方法一定要加上,并且要放在创建按钮之后,否则就不会创建按钮 
	super.initializeBounds(); 
	} 
}
