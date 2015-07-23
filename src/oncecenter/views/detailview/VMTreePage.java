package oncecenter.views.detailview;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite; 
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;

public class VMTreePage extends Page implements IVMTreePage{

	CTabFolder folder;
	Composite parent;
	public VMTreePage(Composite composite)
	{
		super();
		this.parent = composite;
	}
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		StructuredSelection select = (StructuredSelection)selection;
		
		VMTreeObject element = (VMTreeObject)select.getFirstElement();
		
		if(element.getFolder() == null) 
			element.createFolder(parent);
		folder = element.getFolder();
		if(folder==null){
			System.out.println("folder is null");
		}
		if(parent instanceof PageBook)
		{
			PageBook book = (PageBook)parent;
			book.showPage(getControl());
		}
	}

	@Override
	public void createControl(Composite parent) {
		folder = new CTabFolder(parent, SWT.NONE);
		folder.setBorderVisible(true);
	}

	@Override
	public Control getControl() {
		return folder;
	}

	@Override
	public void setFocus() {
		
	}

}

