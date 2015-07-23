package oncecenter.views.grouptreeview.elements;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;

public class VMTreeObjectVMinGroup extends VMTreeObject {

	private VMTreeObjectVM vmObject;
	
	public VMTreeObjectVMinGroup(VMTreeObjectVM vmObject) {
		super(vmObject.getName());
		this.setVmObject(vmObject);
		
	}

	@Override
	public void addChild(VMTreeObject object) {
		
		
	}

	public VMTreeObjectVM getVmObject() {
		return vmObject;
	}

	public void setVmObject(VMTreeObjectVM vmObject) {
		this.vmObject = vmObject;
	}

	
}
