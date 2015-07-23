package oncecenter.action.group;

import java.util.ArrayList;
import java.util.List;

import oncecenter.Constants;
import oncecenter.util.ImageRegistry;
import oncecenter.views.grouptreeview.elements.VMTreeObjectGroup;
import oncecenter.views.grouptreeview.elements.VMTreeObjectRootinGroup;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class AddGroupAction extends Action {
	
	VMTreeObjectRootinGroup root;
	String newName;
	
	public AddGroupAction(VMTreeObjectRootinGroup selection){
		this.setText("添加组");
		this.root = selection;
	}
	public void run(){
		AddGroupDialog addDialog = new AddGroupDialog(new Shell());
		if(Dialog.OK == addDialog.open()){
			VMTreeObjectGroup newGroup = new VMTreeObjectGroup(newName);
			root.addChild(newGroup);
			List<String> vmList = new ArrayList<String>();
			root.groupMap.put(newName, vmList);
			Display display = PlatformUI.getWorkbench().getDisplay();
			if (!display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						Constants.groupView.getViewer().refresh();
					}
				};
				display.asyncExec(runnable);
			}
		}
	}
	
	public class AddGroupDialog extends Dialog {
		
		CLabel addCLabel;
		Text name;
		protected AddGroupDialog(Shell parentShell) {
			super(parentShell);
		}
		
		@Override
		protected void configureShell(Shell shell){
			super.configureShell(shell);
			shell.setText("添加组");
		}
		
		@Override 
		protected Point getInitialSize() { 
		return new Point(250,130); 
		} 

		protected Control createDialogArea(Composite parent) { 
			Composite composite = new Composite(parent, SWT.NONE); 
			GridLayout layout = new GridLayout(2,true); 
			composite.setLayout(layout); 
			composite.setLayoutData(new GridData(GridData.FILL_BOTH)); 
			applyDialogFont(composite); 
			
			GridData griddata = new GridData();
			griddata.horizontalAlignment = GridData.END;
			addCLabel = new CLabel(composite, SWT.NONE);
			addCLabel.setText("虚拟机组名：");
			addCLabel.setImage(ImageRegistry.getImage(ImageRegistry.DEFAULT));
			addCLabel.setLayoutData(griddata);
			name = new Text(composite, SWT.NONE);	
			name.setLayoutData(griddata);
			
			return composite; 
		} 
		
		protected void okPressed(){
			newName = name.getText().trim();
			super.okPressed();
		}
	}
}
