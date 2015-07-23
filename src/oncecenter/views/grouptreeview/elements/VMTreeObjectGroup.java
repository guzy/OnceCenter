package oncecenter.views.grouptreeview.elements;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

public class VMTreeObjectGroup extends VMTreeObject {

	public VMTreeObjectGroup(String name) {
		super(name);
		
	}

	@Override
	public void addChild(VMTreeObject object) {
		
		if(object==null)
			return;
		int i;
		for(i=0;i<children.size();i++){
			VMTreeObject o= children.get(i);
			if(o.getName().compareToIgnoreCase(object.getName())>0)
				break;
		}
		children.add(i, object);
		object.setParent(this);	
	}

}
