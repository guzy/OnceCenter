package oncecenter.views.xenconnectiontreeview.elements;

import oncecenter.views.grouptreeview.VMTreeVMGroupView;

public class VMTreeObjectConnectionRoot extends VMTreeObjectDefault {

	public VMTreeObjectConnectionRoot(String name) {
		super(name);
		
	}
	
	@Override
	public void addChild(VMTreeObject object) {
		
		super.addChild(object);
		if(object!=null && object instanceof VMTreeObjectRoot){
			VMTreeVMGroupView.addServer(object);
		}
	}

}
