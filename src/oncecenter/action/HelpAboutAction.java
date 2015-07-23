package oncecenter.action;

import oncecenter.util.ImageRegistry;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
public class HelpAboutAction extends OnceAction {
	public HelpAboutAction(String text,String image,String disabledImage){
		super(text,image,disabledImage);
	}
	public void run(){
		HelpAboutDialog  dialog = new HelpAboutDialog(Display.getCurrent().getActiveShell());
		dialog.open();
	}
	
	class HelpAboutDialog extends Dialog
	{
		
		public static final int OK_ID = 0;
		public static final String OK_LABEL = "确定";
		
		protected HelpAboutDialog(Shell shell) {
			super(shell);
			
		}
		
		protected void configureShell(Shell shell)
		{
			super.configureShell(shell);
			shell.setText("关于博纳讯动服务器虚拟化软件");
//			shell.setText("关于网驰平台");
		}
		
		protected Control createDialogArea(Composite parent)
		{
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setBackground(new Color(null,255,255,255));
			GridLayout layout = new GridLayout(2,false);
			composite.setLayout(layout);
			
			CLabel imageCLabel = new CLabel(composite,SWT.NONE);
			imageCLabel.setImage(ImageRegistry.getImage(ImageRegistry.HELP));
			GridData gridData = new GridData();
			gridData.verticalSpan = 4;
			imageCLabel.setLayoutData(gridData);
			
			
			Label nameLabel = new Label(composite,SWT.NONE);
//			nameLabel.setText(" 网驰平台");
			nameLabel.setText(" 博纳博纳讯动服务器虚拟化软件");
			nameLabel.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
			nameLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
			nameLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			Composite versionComposite = new Composite(composite,SWT.NONE);
			versionComposite.setBackground(new Color(null,255,255,255));
			versionComposite.setLayout(new GridLayout(2,false));
			
			Label versionLabel = new Label(versionComposite,SWT.NONE);
			versionLabel.setText("Version : ");
			versionLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			Label versionValueLabel = new Label(versionComposite,SWT.NONE);
			versionValueLabel.setText("5.3");
			versionValueLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			Label bulidLabel = new Label(versionComposite,SWT.NONE);
			bulidLabel.setText("Build id : ");
			bulidLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			Label bulidValueLabel = new Label(versionComposite, SWT.NONE);
			bulidValueLabel.setText("I20110613-1736");
			bulidValueLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			Label email = new Label(versionComposite,SWT.NONE);
			email.setText("Email : ");
			email.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			Label emailLabel = new Label(versionComposite,SWT.NONE);
			emailLabel.setText("admin@beyondcent.com");
//			emailLabel.setText("oncecloud@otcaix.iscas.ac.cn");
			emailLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			Label descriptionLabel = new Label(composite, SWT.NONE);
			descriptionLabel.setText(" (c) Copyright 苏州博纳讯动软件有限公司  contributors . All rights reserved.");
//			descriptionLabel.setText(" (c) Copyright 软工中心云计算小组  contributors . All rights reserved.");
			descriptionLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
//			
//			Label messageLabel = new Label(composite,SWT.NONE);
//			messageLabel.setText(" If there is any error, please contact  软工中心VM小组");
//			messageLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			return parent;
		}
		
		protected void createButtonsForButtonBar(Composite parent)
		{
			createButton(parent,HelpAboutDialog.OK_ID,HelpAboutDialog.OK_LABEL,true);
		}
		
		protected void buttonPressed(int buttonId)
		{
			if(buttonId == HelpAboutDialog.OK_ID)
				close();
		}
	}
}
