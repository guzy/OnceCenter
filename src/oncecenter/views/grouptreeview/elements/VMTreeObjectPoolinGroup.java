package oncecenter.views.grouptreeview.elements;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;

public class VMTreeObjectPoolinGroup extends VMTreeObjectRootinGroup {

	private VMTreeObjectPool poolObject;
	
	public VMTreeObjectPoolinGroup(VMTreeObjectPool poolObject) {
		super(poolObject.getName());
		this.setPoolObject(poolObject);
		
	}
	@Override
	public void addChild(VMTreeObject object) {
		
		super.addChild(object);
	}
	public VMTreeObjectPool getPoolObject() {
		return poolObject;
	}
	public void setPoolObject(VMTreeObjectPool poolObject) {
		this.poolObject = poolObject;
	}

}
