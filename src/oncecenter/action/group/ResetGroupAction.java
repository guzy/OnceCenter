package oncecenter.action.group;

import java.util.List;

import oncecenter.Constants;
import oncecenter.views.grouptreeview.elements.VMTreeObjectRootinGroup;
import oncecenter.views.grouptreeview.elements.VMTreeObjectVMinGroup;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class ResetGroupAction extends Action {

	VMTreeObjectVMinGroup vm;
	List<VMTreeObjectVMinGroup> vmList;
	
	public ResetGroupAction(VMTreeObjectVMinGroup vm){
		this.setText("回到默认组");
		this.vm = vm;
	}
	
	public ResetGroupAction(List<VMTreeObjectVMinGroup> vmList){
		this.setText("回到默认组");
		this.vmList = vmList;
	}
	public void run(){
		VMTreeObject defaultGroup = null;
		VMTreeObjectVMinGroup firstVm = vm==null?vmList.get(0):vm;
		VMTreeObjectRootinGroup root = (VMTreeObjectRootinGroup)firstVm.getParent().getParent();
		for(VMTreeObject o : firstVm.getParent().getParent().getChildrenList()){
			if(o.getName().equals(Constants.VM_GROUP_DEFAULT_NAME)){
				defaultGroup = o;
				break;
			}
		}
		if(defaultGroup!=null){
			if(vm!=null){
				List<String> vmList = root.groupMap.get(vm.getParent().getName());
				vmList.remove(vm.getVmObject().getUuid());
				vm.getParent().getChildrenList().remove(vm);
				defaultGroup.addChild(vm);
			}else if(vmList!=null){
				for(VMTreeObjectVMinGroup vm:vmList){
					List<String> vmList = root.groupMap.get(vm.getParent().getName());
					vmList.remove(vm.getVmObject().getUuid());
					vm.getParent().getChildrenList().remove(vm);
					defaultGroup.addChild(vm);
				}
			}
		}

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
