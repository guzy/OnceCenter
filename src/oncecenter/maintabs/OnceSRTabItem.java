package oncecenter.maintabs;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;

import org.eclipse.swt.custom.CTabFolder;

public class OnceSRTabItem extends OnceTabItem {

public VMTreeObjectSR objectSR;
	
	public OnceSRTabItem(CTabFolder arg0, int arg1, 
			VMTreeObjectSR object) {
		super(arg0, arg1,object);
		this.objectSR=object;
		
	}

	public OnceSRTabItem(CTabFolder arg0, int arg1, int arg2,
			VMTreeObjectSR object) {
		super(arg0, arg1, arg2,object);
		this.objectSR=object;
		
	}
	
	@Override
	public boolean Init() {
		
		return false;
	}

}
