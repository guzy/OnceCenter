package oncecenter.util;

import oncecenter.Constants;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

public class hasPoolUtil {

	public static boolean hasPool(){
		for(VMTreeObject child:Constants.CONNECTIONS_TREE.getChildrenList()){
			if(child.getItemState().equals(ItemState.able)&&child instanceof VMTreeObjectPool){
				return true;
			}
		}
		return false;
	}
}
