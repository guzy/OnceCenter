package oncecenter.action.group;

import java.util.List;

import oncecenter.Constants;
import oncecenter.views.grouptreeview.elements.VMTreeObjectGroup;
import oncecenter.views.grouptreeview.elements.VMTreeObjectRootinGroup;
import oncecenter.views.grouptreeview.elements.VMTreeObjectVMinGroup;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class AddtoGroupAction extends Action {
	VMTreeObjectVMinGroup vm;
	List<VMTreeObjectVMinGroup> vmList;
	VMTreeObjectGroup group;
	public AddtoGroupAction(VMTreeObjectVMinGroup vm,VMTreeObjectGroup group){
		this.setText(group.getName());
		this.vm = vm;
		this.group = group;
	}
	public AddtoGroupAction(List<VMTreeObjectVMinGroup> vmList,VMTreeObjectGroup group){
		this.setText(group.getName());
		this.vmList = vmList;
		this.group = group;
	}
	public void run(){
		VMTreeObjectRootinGroup root = (VMTreeObjectRootinGroup)group.getParent();
		
		if(vm!=null){
			List<String> vmList = root.groupMap.get(vm.getParent().getName());
			if(vmList!=null)
				vmList.remove(vm.getShadowObject().getUuid());
			root.groupMap.get(group.getName()).add(vm.getVmObject().getUuid());
			vm.getParent().getChildrenList().remove(vm);
			group.addChild(vm);
		}else if(vmList!=null){
			for(VMTreeObjectVMinGroup vm:vmList){
				List<String> vmList = root.groupMap.get(vm.getParent().getName());
				if(vmList!=null)
					vmList.remove(vm.getVmObject().getUuid());
				root.groupMap.get(group.getName()).add(vm.getVmObject().getUuid());
				vm.getParent().getChildrenList().remove(vm);
				group.addChild(vm);
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
