package oncecenter.maintabs;

import java.util.ArrayList;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;

public abstract class OnceTabItem extends CTabItem {

	public CTabFolder folder;
	public Composite composite;
	
	public OnceTabItem(CTabFolder arg0, int arg1,VMTreeObject object) {
		super(arg0, arg1);
		this.folder=arg0;
		this.setShowClose(false);
		if(object.itemList == null)
			object.itemList = new ArrayList<OnceTabItem>();
		object.itemList.add(this);
	}
	
	public OnceTabItem(CTabFolder arg0, int arg1, int arg2,VMTreeObject object) {
		super(arg0, arg1, arg2);
		this.folder=arg0;
		this.setShowClose(false);

		if(object.itemList == null)
			object.itemList = new ArrayList<OnceTabItem>();
		object.itemList.add(this);
	}
	
	public abstract boolean Init();
}
