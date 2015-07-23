package oncecenter.action;

import oncecenter.Constants;
import oncecenter.action.edit.PageBookViewState;
import oncecenter.util.ImageRegistry;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

public class BackAction extends OnceAction {
	public BackAction(String text,String image,String disabledImage){
		super(text,image,disabledImage);
	}
	public BackAction(){
		super();
		
		setText("·µ»Ø");		
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.BACK_FT));
		setDisabledImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.BACK_FT_DIS));
	}
	
	public void run(){
		PageBookViewState current = Constants.BackStack.pop();
		PageBookViewState previous = Constants.BackStack.peek();
		if(current.getSelection().equals(previous.getSelection())){
			StructuredSelection selection = (StructuredSelection)current.getSelection();
			VMTreeObject element = (VMTreeObject)selection.getFirstElement();
			element.getFolder().setSelection(previous.getTabItem());			
		}else{
			Constants.treeView.getViewer().setSelection(previous.getSelection());
			Constants.pageBookView.selectionChanged(Constants.treeView, previous.getSelection());
		}
		Constants.NextStack.push(current);
		PageBookViewState.refreshMenu();
	}
}
