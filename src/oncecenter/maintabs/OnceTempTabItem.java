package oncecenter.maintabs;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectTemplate;

import org.eclipse.swt.custom.CTabFolder;

public class OnceTempTabItem extends OnceTabItem {

	public VMTreeObjectTemplate objectTemplate;
	public OnceTempTabItem(CTabFolder arg0, int arg1, int arg2,
			VMTreeObjectTemplate object) {
		super(arg0, arg1, arg2, object);
		
		this.objectTemplate = object;
	}

	public OnceTempTabItem(CTabFolder arg0, int arg1,
			VMTreeObjectTemplate object) {
		
		super(arg0, arg1, object);
		
		this.objectTemplate = object;
	}

	@Override
	public boolean Init() {
		
		return false;
	}

}
