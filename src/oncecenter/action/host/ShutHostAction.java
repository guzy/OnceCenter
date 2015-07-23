package oncecenter.action.host;

import oncecenter.util.ImageRegistry;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;

import org.eclipse.jface.action.Action;

import com.once.xenapi.Connection;

public class ShutHostAction extends Action{
	VMTreeObjectHost selection;
	Connection connection;

	public ShutHostAction(){
		super();
		
		setText("关机");		
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.SHUTDOWN));
	}
	public ShutHostAction(VMTreeObjectHost selection){
		super();
		this.selection=selection;
		connection=selection.getConnection();
		
		setText("关机");		
		setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.SHUTDOWN));
	}
	
	public void run()
	{}
}
