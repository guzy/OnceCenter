package oncecenter.action.vm;

import java.util.Date;

import oncecenter.Constants;
import oncecenter.util.ImageRegistry;
import oncecenter.views.logview.VMEvent;
import oncecenter.views.logview.VMEvent.eventType;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM.ResourceTypes;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Connection;
import com.once.xenapi.VM;

public class PerformAlarmAction extends Action {
	
	VMTreeObjectVM selection;
	ResourceTypes type;
	Connection connection;
	VM vm;
	VMEvent event= new VMEvent();
	
	public PerformAlarmAction(VMTreeObjectVM selection, ResourceTypes type){
		super();
		this.selection=selection;
		this.type = type;
		setText("报警");	
	}
	
	public void run(){
		
		if(selection==null){
			StructuredSelection select = (StructuredSelection)Constants.treeView.getViewer().getSelection();
			selection = (VMTreeObjectVM)select.getFirstElement();	
		}
		
		VMTreeObject parent = selection;
		while(!parent.getName().equals("Xen"))
		{
			event = new VMEvent();
			event.setDatetime(new Date());
			event.setDescription("虚拟机" + selection.getName() + type + "资源过载，请迅速进行资源调整！");
			event.setTarget(selection);
			event.setTask("");
			event.setType(eventType.warning);
			event.setImage(ImageRegistry.getImage(ImageRegistry.DISABLE));
			parent.events.add(event);
			parent = parent.getParent();
		}
		Display display = PlatformUI.getWorkbench().getDisplay();
		if (!display.isDisposed()){
		    Runnable runnable = new Runnable(){
		        public void run(){
		        	Constants.logView.logFresh(event);
		        }
		    };
		    display.syncExec(runnable); 
		}
		
	}
}
