package oncecenter.views.grouptreeview.elements;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;

public class VMTreeObjectHostinGroup extends VMTreeObjectRootinGroup {

	private VMTreeObjectHost hostObject;
	
	public VMTreeObjectHostinGroup(VMTreeObjectHost hostObject) {
		super(hostObject.getName());
		this.setHostObject(hostObject);
		
	}
	@Override
	public void addChild(VMTreeObject object) {
		
		super.addChild(object);
	}
	public VMTreeObjectHost getHostObject() {
		return hostObject;
	}
	public void setHostObject(VMTreeObjectHost hostObject) {
		this.hostObject = hostObject;
	}

}
