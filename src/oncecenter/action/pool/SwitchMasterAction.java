package oncecenter.action.pool;

import oncecenter.Constants;
import oncecenter.action.NewServerAction;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Pool;


public class SwitchMasterAction extends Action {
	
	VMTreeObjectPool pool;
	
	String newIp;
	
	public SwitchMasterAction(VMTreeObjectPool selection){
		super();
		this.pool=selection;
		setText("");		
	}
	public void run(){
		Pool.Record record = (Pool.Record)pool.getRecord();
		if(record.backup!=null){
			newIp = pool.hostMap.get(record.backup).getIpAddress();
		}
		Display display = PlatformUI.getWorkbench().getDisplay();
		if (!display.isDisposed()){
		    Runnable runnable = new Runnable(){
		        public void run(){
		        	if(pool.getRecordTimer!=null){
		        		pool.getRecordTimer.cancel();
	        		}
					if(pool.getPerformTimer!=null){
						pool.getPerformTimer.cancel();
					}
		    		Constants.CONNECTIONS_TREE.getChildrenList().remove(pool);
		    		Constants.treeView.getViewer().remove(pool);
		    		
		    		NewServerAction action = new NewServerAction(newIp);
		    		action.run();
		        }
			};
		    display.asyncExec(runnable); 
		}

		
		
	}
	
	
}
