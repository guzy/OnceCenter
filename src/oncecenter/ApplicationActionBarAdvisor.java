package oncecenter;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

import oncecenter.action.BackAction;
import oncecenter.action.NextAction;
import oncecenter.action.RecorverAction;
import oncecenter.action.UpdateSystemAction;
import oncecenter.action.vm.RebootAction;
import oncecenter.action.vm.StartAction;
//import oncecenter.action.toolbar.UploadIsoOnToolbar;
import oncecenter.util.menuutil.MenuUtil;

import org.eclipse.jface.action.ToolBarManager;

public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
	
	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	protected void makeActions(IWorkbenchWindow window) {
	}
	
	protected void fillCoolBar(ICoolBarManager coolBar) {
		Constants.toolBar = new ToolBarManager(coolBar.getStyle() | SWT.RIGHT);
		MenuUtil.fillMenu(Constants.toolBar);
		coolBar.add(Constants.toolBar);
	}

	protected void fillMenuBar(IMenuManager menuBar) {
		MenuUtil.fillMenu(menuBar);
		Constants.parentMenu = menuBar;
	}

}
