package oncecenter.action;

import oncecenter.Constants;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;

import org.eclipse.jface.action.Action;

public class ItemDeleteAction extends Action {
	VMTreeObject selection;
	public ItemDeleteAction(VMTreeObject selection){
		super();
		setText("ÒÆ³ý");	
		this.selection = selection;
	}
	public void run(){
		selection.getParent().getChildrenList().remove(selection);
		Constants.treeView.getViewer().remove(selection);
		Constants.treeView.getViewer().refresh();
	}
}
