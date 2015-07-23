package oncecenter.action;

import oncecenter.Constants;
import oncecenter.util.ImageRegistry;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.wizard.newpool.NewPoolWizard;
import oncecenter.wizard.newvmfromtemp.NewVMDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;

public class NewPoolAction extends OnceAction {
	VMTreeObject newObject = null;
	
	public NewPoolAction(){
		super();
	}
	
	public NewPoolAction(String text,String image,String disabledImage){
		super(text,image,disabledImage);
	}
	
	public void run(){
		NewPoolWizard wizard = new NewPoolWizard();
		NewVMDialog dialog = new NewVMDialog(Display.getCurrent().getActiveShell(),
                wizard);
		dialog.setPageSize(350, 250);
		dialog.create();
		dialog.open();
		
		Constants.treeView.getViewer().expandAll();
		
//		for(VMTreeObject o :Constants.CONNECTIONS_TREE.getChildren()){
//			if(o.getItemState().equals(ItemState.unable))
//				break;
//			newObject = o;
//		}
//		if(newObject!=null){
//			Display display=PlatformUI.getWorkbench().getDisplay();
//			if (!display.isDisposed()){
//			    Runnable runnable = new Runnable(){
//			        public void run( ){
//			        	ISelection s1 = new StructuredSelection(new Object[]{newObject});
//						Constants.treeView.getViewer().setSelection(s1);	
//			        	Constants.treeView.getViewer().refresh();
//			        }
//				};
//			    display.syncExec(runnable); 
//			}
//			for(VMTreeObject o :Constants.CONNECTIONS_TREE.getChildren()){
//				if(!o.equals(newObject)
//						&&o.getItemState().equals(ItemState.able)
//						&&o.getName().equals(newObject.getName())){
//					//Display display=PlatformUI.getWorkbench().getDisplay();
//					if (!display.isDisposed()){
//					    Runnable runnable = new Runnable(){
//					        public void run( ){
//					        	MessageDialog.openInformation(new Shell(), "提示", "系统中有重名对象，请重新命名");
//					        }
//						};
//					    display.syncExec(runnable); 
//					}
//					if(newObject instanceof VMTreeObjectPool){
//						new RenamePoolAction((VMTreeObjectPool)newObject,true).run();
//					}
//					
//					break;
//				}
//			}
//		}
	}
}
