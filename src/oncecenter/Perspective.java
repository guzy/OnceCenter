package oncecenter;

import oncecenter.views.detailview.VMTreePageBookView;
import oncecenter.views.grouptreeview.VMTreeVMGroupView;
import oncecenter.views.logview.LogView;
import oncecenter.views.xenconnectiontreeview.VMTreeView;


import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

	/**
	 * Creates the initial layout for a page.
	 */
	public void createInitialLayout(IPageLayout layout) {
		//layout.addStandaloneView(ProgressBarView.ID, false, IPageLayout.BOTTOM, 0.9999f, IPageLayout.ID_EDITOR_AREA);
		
		
		//layout.addView(VMTreeView.ID, IPageLayout.LEFT, 0.23f, IPageLayout.ID_EDITOR_AREA);
		//layout.addStandaloneView(TreeViewID,false,IPageLayout.LEFT, 0.25f, IPageLayout.ID_EDITOR_AREA);
		//layout.addView(ConsoleID,IPageLayout.BOTTOM,0.65f,IPageLayout.ID_EDITOR_AREA); 
		
		//folder.addView(BrowserTestView.ID);
		//folder.addView(ProgressBarView.ID);
		//folder.addView(ConsoleViewID);
		//layout.addPlaceholder(ConsoleViewID+ ":*", IPageLayout.RIGHT, 0.75f, IPageLayout.ID_EDITOR_AREA);
		layout.addStandaloneView(LogView.ID,false, IPageLayout.BOTTOM, 0.85f, IPageLayout.ID_EDITOR_AREA);
		IFolderLayout folder1=layout.createFolder("tree", IPageLayout.LEFT, 0.23f, IPageLayout.ID_EDITOR_AREA);
		//if(Constant.permission.equals(Constant.Permission.admin)){
			folder1.addView(VMTreeView.ID);
			layout.getViewLayout(VMTreeView.ID).setCloseable(false);
			folder1.addView(VMTreeVMGroupView.ID);
			layout.getViewLayout(VMTreeVMGroupView.ID).setCloseable(false);
			
		//}else{
		//	folder1.addView(AppTreeView.ID);
		//}
		
		//IFolderLayout folder=layout.createFolder("message", IPageLayout.LEFT, 1.0f, IPageLayout.ID_EDITOR_AREA);
		//layout.addStandaloneViewPlaceholder(ConsoleViewID + ":*", IPageLayout.LEFT, 0.75f, IPageLayout.ID_EDITOR_AREA, false);
		//folder.addPlaceholder(MainView.ID + ":*");
		//folder.addView(HomeView.ID);
		//folder.addView(ExampleView.ID);
		//folder.(VMTreePageBookView.ID);
		layout.addStandaloneView(VMTreePageBookView.ID,false, IPageLayout.LEFT, 1.0f, IPageLayout.ID_EDITOR_AREA);
//		layout.addView(userView.ID, IPageLayout.BOTTOM, 0.4f, IPageLayout.ID_EDITOR_AREA);
//		layout.getViewLayout(VMTreeView.ID).setCloseable(false);
		layout.setEditorAreaVisible(false);
//		layout.addFastView(ProgressBarView.ID);
//		VMConsole console = new VMConsole();
//		console.openConsole();
//		VMConsole.showMessage("Welcome!\n");
		
	}

}
