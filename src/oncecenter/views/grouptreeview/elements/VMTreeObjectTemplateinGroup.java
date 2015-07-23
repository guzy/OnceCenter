package oncecenter.views.grouptreeview.elements;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectTemplate;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;

public class VMTreeObjectTemplateinGroup extends VMTreeObject {

	private VMTreeObjectTemplate templateObject;
	
	public VMTreeObjectTemplateinGroup(VMTreeObjectTemplate vmObject) {
		super(vmObject.getName());
		this.setVmObject(vmObject);
		
	}

	@Override
	public void addChild(VMTreeObject object) {
		
		
	}

	public VMTreeObjectTemplate getVmObject() {
		return templateObject;
	}

	public void setVmObject(VMTreeObjectTemplate vmObject) {
		this.templateObject = vmObject;
	}

	
}
