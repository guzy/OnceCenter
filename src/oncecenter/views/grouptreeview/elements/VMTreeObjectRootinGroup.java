package oncecenter.views.grouptreeview.elements;

import java.util.List;
import java.util.Map;

import oncecenter.Constants;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;

public class VMTreeObjectRootinGroup extends VMTreeObject {

	public Map<String,List<String>> groupMap;
	public VMTreeObjectRootinGroup(String name) {
		super(name);
		
	}

	@Override
	public void addChild(VMTreeObject object) {
		
		if(object==null)
			return;
//		if(object.getName().equals(Constants.HALTED_VM_GROUP_DEFAULT_NAME)){
//			int i;
//			i = children.size();
//			children.add(i, object);
//		}else 
		if(object.getName().equals(Constants.VM_GROUP_DEFAULT_NAME)){
//			int i;
//			for(i=0;i<children.size();i++){
//				VMTreeObject o= children.get(i);
//				if(o.getName().equals(Constants.HALTED_VM_GROUP_DEFAULT_NAME))
//					break;
//			}
//			children.add(i, object);
			children.add(object);
		}else{
			int i;
			for(i=0;i<children.size();i++){
				VMTreeObject o= children.get(i);
				if(o.getName().compareToIgnoreCase(object.getName())>0
						||o.getName().equals(Constants.VM_GROUP_DEFAULT_NAME))
					break;
			}
			children.add(i, object);
		}
		object.setParent(this);	
	}

}
