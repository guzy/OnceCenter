package oncecenter.util.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class ErrorMessageDialog extends Dialog{

	private static final int OK_ID = 0;
	private static final String OK_LABEL = "OK";
	private CLabel msgCLabel;
	private Image image;
	String msgInfo;
	
	public ErrorMessageDialog(Shell parentShell, String msgInfo,Image image) {
		super(parentShell);
		
		this.msgInfo = msgInfo;
		this.image = image;
	}
	protected void configureShell(Shell shell)
	{
		super.configureShell(shell);
		shell.setText("错误信息提示");
		shell.setBackground(new Color(null,255,255,255));

	}
	
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = (Composite)super.createDialogArea(parent);
		msgCLabel = new CLabel(composite,SWT.NONE);
		msgCLabel.setImage(image);
		msgCLabel.setText(msgInfo);
		msgCLabel.setBounds(20, 60, 280, 90);
		return parent;
	}
	
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent,ErrorMessageDialog.OK_ID,ErrorMessageDialog.OK_LABEL, true);
	}
	
	protected void buttonPressed(int buttonId)
	{
		if(ErrorMessageDialog.OK_ID == buttonId)
		{
			close();
		}
	}

}
