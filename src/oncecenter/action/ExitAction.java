package oncecenter.action;

import oncecenter.Constants;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class ExitAction extends OnceAction {

	private IWorkbenchAction exitAction = null;
	
	public ExitAction(){
		super();
	}
	
	public ExitAction(String text,String image,String disabledImage){
		super(text,image,disabledImage);
	}
	
	public void run(){
		if(exitAction==null)
			exitAction = ActionFactory.QUIT.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		exitAction.run();
	}
}
