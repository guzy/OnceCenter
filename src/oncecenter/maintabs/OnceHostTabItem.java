package oncecenter.maintabs;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;

import org.eclipse.swt.custom.CTabFolder;

public class OnceHostTabItem extends OnceTabItem {
	
	public VMTreeObjectHost objectHost;
	
	
	public OnceHostTabItem(CTabFolder arg0, int arg1,
			VMTreeObjectHost object) {
		super(arg0, arg1,object);
		this.objectHost=object;
		
	}
	
	public OnceHostTabItem(CTabFolder arg0, int arg1, int arg2,
			VMTreeObjectHost object) {
		super(arg0, arg1, arg2,object);
		this.objectHost=object;
		
	}
	
	
	@Override
	public boolean Init() {
		
		return false;
	}

}
