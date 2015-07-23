package oncecenter.action;

import java.util.ArrayList;

import oncecenter.Constants;
import oncecenter.views.xenconnectiontreeview.VMTreeViewer;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject.ItemState;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class DisconnectAction extends Action {

	private VMTreeObjectRoot root;
	
	public DisconnectAction(VMTreeObjectRoot root)
	{
		this.setText("断开连接");
		this.root = root;
	}
	
	public void run()
	{
		Display display = PlatformUI.getWorkbench().getDisplay();
		/**断开连接**/
		final VMTreeViewer viewer = Constants.treeView.getViewer();
		root.setItemState(ItemState.unable);
		root.events.removeAllElements();
		ArrayList<VMTreeObject> objectList = root.getChildrenList();
		for(VMTreeObject o : objectList)
		{
			if(o instanceof VMTreeObjectHost && ((VMTreeObjectHost)o).getChildrenList().size() != 0)
			{
				VMTreeObjectHost hostObject = (VMTreeObjectHost)o;
				ArrayList<VMTreeObject> childList = hostObject.getChildrenList();
				for(VMTreeObject child : childList)
					viewer.remove(child);
				childList.clear();
			}
			viewer.remove(o);
		}
		root.getChildrenList().clear();
		if (root.getRecordTimer != null) {
			root.getRecordTimer.cancel();
		}
		if (root.getPerformTimer != null) {
			root.getPerformTimer.cancel();
		}
		viewer.remove(root);
		if (!display.isDisposed()){
		    Runnable runnable = new Runnable(){
		        public void run(){
		        	viewer.refresh();
		        }
		    };
		    display.syncExec(runnable); 
		}
		
	}
	
}
