package oncecenter.action.group;

import java.util.List;

import oncecenter.Constants;
import oncecenter.views.grouptreeview.elements.VMTreeObjectGroup;
import oncecenter.views.grouptreeview.elements.VMTreeObjectRootinGroup;
import oncecenter.views.grouptreeview.elements.VMTreeObjectVMinGroup;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class DeleteGroupAction extends Action {
	
	VMTreeObjectGroup group;
	VMTreeObject defaultGroup;
	
	public DeleteGroupAction(VMTreeObjectGroup group){
		this.setText("É¾³ý×é");
		this.group = group;
	}
	public void run(){
		for(VMTreeObject o :group.getParent().getChildren()){
			if(o.getName().equals(Constants.VM_GROUP_DEFAULT_NAME)){
				defaultGroup = o;
				break;
			}
		}
		if(defaultGroup!=null){
			VMTreeObjectRootinGroup root = (VMTreeObjectRootinGroup)defaultGroup.getParent();
			List<String> vmList = root.groupMap.get(group.getName());
			for(VMTreeObject o :group.getChildren()){
				VMTreeObjectVMinGroup vm = (VMTreeObjectVMinGroup)o;
				vmList.remove(vm.getVmObject().getUuid());
				defaultGroup.addChild(o);
			}
			group.getChildrenList().clear();
		}else if(group.getChildrenList().size() == 0){
			
		}else{
			return;
		}
		Display display = PlatformUI.getWorkbench().getDisplay();
		if (!display.isDisposed()) {
			Runnable runnable = new Runnable() {
				public void run() {
					group.getParent().getChildrenList().remove(group);
					Constants.groupView.getViewer().remove(group);
					Constants.groupView.getViewer().refresh();
				}
			};
			display.asyncExec(runnable);
		}
	}
}
