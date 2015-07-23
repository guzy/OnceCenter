package oncecenter.maintabs;

import org.eclipse.swt.custom.CTabFolder;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;



public class OncePoolTabItem extends OnceTabItem {

	public VMTreeObjectPool objectPool;
	
	public OncePoolTabItem(CTabFolder arg0, int arg1,
			VMTreeObjectPool object) {
		super(arg0, arg1,object);
		this.objectPool=object;
		
	}
	
	public OncePoolTabItem(CTabFolder arg0, int arg1, int arg2,
			VMTreeObjectPool object) {
		super(arg0, arg1, arg2,object);
		this.objectPool=object;
		
	}
	
	@Override
	public boolean Init() {
		
		return false;
	}

}
