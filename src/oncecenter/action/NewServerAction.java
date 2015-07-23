package oncecenter.action;

import oncecenter.Constants;
import oncecenter.action.host.RenameHostAction;
import oncecenter.action.pool.RenamePoolAction;
import oncecenter.util.ImageRegistry;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;
import oncecenter.wizard.newserver.NewServerWizard;
import oncecenter.wizard.newvmfromtemp.NewVMDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class NewServerAction extends OnceAction {
	String serverIp;
	VMTreeObject newObject = null;
	
	public NewServerAction(){
		super("连接新主机","","");
	}
	
	public NewServerAction(String text,String image,String disabledImage){
		super(text,image,disabledImage);
		
	}
	
	public NewServerAction(String serverIp){
		super(); 
		this.serverIp=serverIp;
		setText("连接新主机");		 
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.ADDSERVER));
	}
	
	public void run(){
		if(serverIp==null||serverIp.length()==0){
			NewServerWizard wizard = new NewServerWizard();
			NewVMDialog dialog = new NewVMDialog(Display.getCurrent().getActiveShell(),
	                wizard);
			dialog.setPageSize(200, 100);
			dialog.create();
			dialog.open();
		}else{
			NewServerWizard wizard = new NewServerWizard(serverIp);
			NewVMDialog dialog = new NewVMDialog(Display.getCurrent().getActiveShell(),
	                wizard);
			dialog.setPageSize(200, 100);
			dialog.create();
			dialog.open();
		}
//		IContributionItem [] fileItems = Constants.newMenu.getItems();
//		((ActionContributionItem)fileItems[1]).getAction().setEnabled(true);
//		((ActionContributionItem)fileItems[2]).getAction().setEnabled(true);
		Constants.treeView.getViewer().expandAll();
		
		for(VMTreeObject o :Constants.CONNECTIONS_TREE.getChildren()){
			if(o.getItemState().equals(ItemState.unable))
				break;
			newObject = o;
		}
		if(newObject!=null){
			Display display=PlatformUI.getWorkbench().getDisplay();
			if (!display.isDisposed()){
			    Runnable runnable = new Runnable(){
			        public void run( ){
			        	ISelection s1 = new StructuredSelection(new Object[]{newObject});
						Constants.treeView.getViewer().setSelection(s1);	
			        	Constants.treeView.getViewer().refresh();
			        }
				};
			    display.syncExec(runnable); 
			}
			for(VMTreeObject o :Constants.CONNECTIONS_TREE.getChildren()){
				if(!o.equals(newObject)
						&&o.getItemState().equals(ItemState.able)
						&&o.getName().equals(newObject.getName())){
					//Display display=PlatformUI.getWorkbench().getDisplay();
					if (!display.isDisposed()){
					    Runnable runnable = new Runnable(){
					        public void run( ){
					        	MessageDialog.openInformation(new Shell(), "提示", "系统中有重名对象，请重新命名");
					        }
						};
					    display.syncExec(runnable); 
					}
					if(newObject instanceof VMTreeObjectHost){
						new RenameHostAction((VMTreeObjectHost)newObject,true).run();
					}else if(newObject instanceof VMTreeObjectPool){
						new RenamePoolAction((VMTreeObjectPool)newObject,true).run();
					}
					
					break;
				}
			}
		}
//		VMTreeView viewer = (VMTreeView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
//		        .findView(VMTreeView.ID);
//		viewer.getViewer().expandAll();
//		VMTreeParent root = (VMTreeParent)viewer.getViewer().getInput();
//		VMTreeObject[] children = root.getChildren();
//		for(VMTreeObject tmp:children){
//			if(tmp instanceof VMTreeParent && tmp.getName().equals("Xen")){
//				selection = (VMTreeParent)tmp;
//				break;
//			}
//		}
//		VMTreeParent newServer = new VMTreeParent("aaaaaa");
//		selection.addChild(newServer);
//		
		//viewer.getViewer().refresh();
//		viewer.getViewer().setExpandedState(selection,true);
	}
	
}
