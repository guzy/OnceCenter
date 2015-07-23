package oncecenter.p2v;

import oncecenter.util.ImageRegistry;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class MessageDialog extends Dialog {

	private static final int OK_ID = 0;
	private static final String OK_LABEL = "È·¶¨";
	private String msgInfo;
	private CLabel msgCLabel;
	public MessageDialog(Shell parentShell,String msgInfo) {
		super(parentShell);
		
		this.msgInfo = msgInfo;
	}
	
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = (Composite)super.createDialogArea(parent);
		msgCLabel = new CLabel(composite,SWT.NONE);
		msgCLabel.setImage(ImageRegistry.getImage(ImageRegistry.REBOOT_OR_NOT));
		msgCLabel.setText(msgInfo);
		return parent;
	}
	
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent,MessageDialog.OK_ID,MessageDialog.OK_LABEL, true);
	}
	
	protected void buttonPressed(int buttonId)
	{
		if(MessageDialog.OK_ID == buttonId)
		{
			close();
		}
	}
}
