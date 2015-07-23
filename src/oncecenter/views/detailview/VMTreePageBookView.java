package oncecenter.views.detailview;

import oncecenter.Constants;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectDefault;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;

public class VMTreePageBookView extends PageBookView implements ISelectionListener{

	public final static String ID = "oncecenter.views.VMTreePageBookView";
	
	@Override
	public void init(IViewSite site) throws PartInitException {
	     super.init(site);
	     site.getPage().addSelectionListener(this);
	     Constants.pageBookView = this;
	 }
	
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		StructuredSelection select = (StructuredSelection)selection;
		VMTreeObject element = (VMTreeObject)select.getFirstElement();
		if(element!=null){
			this.setPartName(element.getName());
	        if(part == this || selection ==  null )
	            return ;
	        if(!(getCurrentPage() instanceof VMTreePage))
	        	return;
	        VMTreePage page = (VMTreePage)getCurrentPage();
	        if(page != null)
	        	page.selectionChanged(part, selection);
		}
	}

	@Override
	protected IPage createDefaultPage(PageBook book) {
		VMTreePage page = new VMTreePage(book);
		VMTreeObjectDefault object = new VMTreeObjectDefault("");
		initPage(page);
		page.createControl(book);
		if(object.getFolder() == null)
			object.createFolder(book);
		page.folder = object.getFolder();
		return page;
	}

	@Override
	protected PageRec doCreatePage(IWorkbenchPart part) {
		Object obj = new VMTreePage(getPageBook());
		if(obj instanceof IVMTreePage)
		{
			IVMTreePage page = (IVMTreePage)obj;
			if(page instanceof IPageBookViewPage)
			{
				initPage((IPageBookViewPage)page);
			}
			page.createControl(getPageBook());
			return new PageRec(part, page);
		}
		return null;
	}

	@Override
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
		IVMTreePage page = (IVMTreePage) pageRecord.page;
		page.dispose();
		pageRecord.dispose();
	}

	@Override
	protected IWorkbenchPart getBootstrapPart() {
		IWorkbenchPage page  =  getSite().getPage();
	    if  (page  !=   null )
	    	return page.getActiveEditor();
	    else 
	        return null ;
	}

	@Override
	 public void createPartControl(Composite parent) {
	        super.createPartControl(parent);
	 }
	
	@Override
	protected boolean isImportant(IWorkbenchPart part) {
		return  (part  instanceof  VMTreeView);
	}
	
	@Override
	public void partActivated(IWorkbenchPart part) {
		super.partActivated(part);
		if(part instanceof VMTreeView) {
			VMTreeView view = (VMTreeView)part;
			ISelection selection = view.getViewer().getSelection();
			if(selection == null)
				return;
			this.selectionChanged(part, selection);
		}
	}

	public void setPartName(String name) {
		super.setPartName(name);
	}
	
	public void setCloseEnable(boolean flag) {
		
	}
}