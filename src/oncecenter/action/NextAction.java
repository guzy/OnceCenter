package oncecenter.action;

import oncecenter.Constants;
import oncecenter.action.edit.PageBookViewState;
import oncecenter.util.ImageRegistry;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

public class NextAction extends OnceAction {
	public NextAction(String text,String image,String disabledImage){
		super(text,image,disabledImage);
	}
	public NextAction(){
		super();
		
		setText("Ç°½ø");		
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.FORWARD_FT));
		setDisabledImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.FORWARD_FT_DIS));
	}
	
	public void run(){
		PageBookViewState next = Constants.NextStack.pop();
		PageBookViewState current = Constants.BackStack.peek();
		if(next.getSelection().equals(current.getSelection())){
			StructuredSelection selection = (StructuredSelection)current.getSelection();
			VMTreeObject element = (VMTreeObject)selection.getFirstElement();
			element.getFolder().setSelection(next.getTabItem());			
		}else{
			Constants.treeView.getViewer().setSelection(next.getSelection());
			Constants.pageBookView.selectionChanged(Constants.treeView, next.getSelection());
		}
		Constants.BackStack.push(current);
		PageBookViewState.refreshMenu();
	}
}
