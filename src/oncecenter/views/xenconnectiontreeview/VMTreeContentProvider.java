package oncecenter.views.xenconnectiontreeview;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;


public class VMTreeContentProvider implements IStructuredContentProvider, ITreeContentProvider{

	@Override
	public void dispose() {
		
		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		
		
	}

	@Override
	public Object[] getChildren(Object parent) {
		if (parent instanceof VMTreeObject) {
			VMTreeObject[] objects = ((VMTreeObject)parent).getChildren();
			return objects;
		}
		return new Object[0];
	}

	@Override
	public Object getParent(Object child) {
		if (child instanceof VMTreeObject) {
			return ((VMTreeObject)child).getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object parent) {
		
		if (parent instanceof VMTreeObject) {
			if(((VMTreeObject)parent).getChildren().length>0)
				return true;
		}
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		
		return getChildren(inputElement);
	}
	
	

}
