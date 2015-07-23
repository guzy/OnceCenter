package oncecenter.util.dialog;

import oncecenter.Constants;
import oncecenter.util.ImageRegistry;
import oncecenter.views.xenconnectiontreeview.VMTreeView;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wb.swt.SWTResourceManager;

public class QuestionDialog extends Dialog{

	private static final int OK_ID = 0;
	private static final String OK_LABEL = "OK";
	private static final int CLOSE_ID = 1;
	private static final String CLOSE_LABEL = "Close";
	private CLabel msgCLabel;
	private String msgInfo; 
	private Job job;
	public QuestionDialog(Shell parentShell, String msgInfo, Job job) {
		super(parentShell);
		
		this.msgInfo = msgInfo;
		this.job = job;
	}
	

	protected Control createDialogArea(Composite parent)
	{
		Composite composite = (Composite)super.createDialogArea(parent);
		msgCLabel = new CLabel(composite,SWT.NONE);
		msgCLabel.setImage(ImageRegistry.getImage(ImageRegistry.REBOOT_OR_NOT));
		msgCLabel.setText(msgInfo);
		return parent;
	}
	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent,QuestionDialog.OK_ID,QuestionDialog.OK_LABEL, true);
		createButton(parent,QuestionDialog.CLOSE_ID,QuestionDialog.CLOSE_LABEL,true);
	}
	
	protected void buttonPressed(int buttonId)
	{
		if(QuestionDialog.OK_ID == buttonId)
		{
			job.schedule();
			close();
		}
		else if(QuestionDialog.CLOSE_ID == buttonId)
		{
			close();
			Constants.jobs.remove(job);
		}
	}
}
