package oncecenter.maintabs;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;

import org.eclipse.swt.custom.CTabFolder;

public class OnceVMTabItem extends OnceTabItem {

	public VMTreeObjectVM objectVM;
	
	public OnceVMTabItem(CTabFolder arg0, int arg1,
			VMTreeObjectVM object) {
		super(arg0, arg1, object);
		this.objectVM=object;
	}

	public OnceVMTabItem(CTabFolder arg0, int arg1, int arg2,
			VMTreeObjectVM object) {
		super(arg0, arg1, arg2, object);
		this.objectVM=object;
	}

	

	@Override
	public boolean Init() {
		return false;
	}

}
