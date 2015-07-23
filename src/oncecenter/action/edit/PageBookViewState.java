package oncecenter.action.edit;

import oncecenter.Constants;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

public class PageBookViewState {

	private ISelection selection;
	private int tabItem;
	
	public PageBookViewState(ISelection selection,int tabItem){
		this.setSelection(selection);
		this.setTabItem(tabItem);
	}

	public ISelection getSelection() {
		return selection;
	}

	public void setSelection(ISelection selection) {
		this.selection = selection;
	}

	public int getTabItem() {
		return tabItem;
	}

	public void setTabItem(int tabItem) {
		this.tabItem = tabItem;
	}
	
	public static void addState(){
		StructuredSelection selection = (StructuredSelection)Constants.treeView.getViewer().getSelection();
		VMTreeObject element = (VMTreeObject)selection.getFirstElement();
		PageBookViewState state = new PageBookViewState(selection,element.getFolder().getSelectionIndex());
		
		Constants.BackStack.push(state);
		Constants.NextStack.removeAllElements();
		
		refreshMenu();
	}
	
	public static void refreshMenu(){
		IContributionItem [] items = Constants.toolBar.getItems();
		if(Constants.BackStack.size()>1){
			((ActionContributionItem)items[0]).getAction().setEnabled(true);
		}else{
			((ActionContributionItem)items[0]).getAction().setEnabled(false);
		}
		if(Constants.NextStack.size()>0){
			((ActionContributionItem)items[1]).getAction().setEnabled(true);
		}else{
			((ActionContributionItem)items[1]).getAction().setEnabled(false);
		}
	}

}
