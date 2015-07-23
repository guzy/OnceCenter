package oncecenter.action.host;

import oncecenter.util.ImageRegistry;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;

import org.eclipse.jface.action.Action;

import com.once.xenapi.Connection;

public class RebootHostAction extends Action {
	VMTreeObjectHost selection;
	Connection connection;
	
	public RebootHostAction(){
		super();
		
		setText("����");		
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.REBOOT));
	}
	public RebootHostAction(VMTreeObjectHost selection){
		super();
		this.selection=selection;
		connection=selection.getConnection();
		setText("����");		
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.REBOOT));
	}
}
