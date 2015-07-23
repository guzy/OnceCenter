package oncecenter.maintabs;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;

import org.eclipse.swt.custom.CTabFolder;

public class OnceHomeTabItem extends OnceTabItem {

	VMTreeObject object;
	
	public OnceHomeTabItem(CTabFolder arg0, int arg1, 
			VMTreeObject object) {
		
		super(arg0, arg1, object);
		this.object = object;
	}
	
	public OnceHomeTabItem(CTabFolder arg0, int arg1, int arg2,
			VMTreeObject object) {
		
		super(arg0, arg1, arg2,object);
		this.object = object;
	}

	@Override
	public boolean Init() {
		
		return false;
	}
}
