package oncecenter.views.xenconnectiontreeview.elements;

import oncecenter.maintabs.HomeTab;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class VMTreeObjectDefault extends VMTreeObject {

	transient private HomeTab homeItem;
	public VMTreeObjectDefault(String name) {
		super(name);
		
	}

	@Override
	public void addChild(VMTreeObject object) {
		
		if(object==null)
			return;
		if(object.getItemState().equals(ItemState.able)){
			int i;
			for(i=0;i<children.size();i++){
				VMTreeObject o= children.get(i);
				if(o.getItemState().equals(ItemState.unable))
					break;
			}
			children.add(i, object);
			object.setParent(this);
			return;
		}else{
			children.add(object);
			object.setParent(this);
			return;
		}
	}

	@Override
	public void createFolder(Composite parent) {
		
		super.createFolder(parent);
		homeItem = new HomeTab(folder,SWT.NONE,0,this);
		folder.setSelection(0);
	}

}
