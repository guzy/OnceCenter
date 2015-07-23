package oncecenter.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import oncecenter.wizard.addlostzfs.AddLostZfsWizard;
import oncecenter.wizard.newvmfromtemp.NewVMDialog;
import oncecenter.wizard.newvmfromtemp.NewVmFTWizard;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.once.xenapi.Connection;
import com.once.xenapi.Host;
import com.once.xenapi.SR;

public class AddLostZfsAction extends Action {

	Connection conn;
	List<Host.Record> hostRecordList;
	
	Set<SR> srs;
	
	public AddLostZfsAction(Connection conn, ArrayList<Host.Record> hostRecordList){
		this.conn = conn;
		this.hostRecordList = hostRecordList;
	}
	
	public void run(){
		while(true){
			try{
				srs = SR.checkZfsValid(conn);
			}catch(Exception e){
				e.printStackTrace();
				break;
			}
			if(srs == null||srs.size()<=0)
				break;
			else{
				MessageBox messageBox = new MessageBox(new Shell(), SWT.OK|SWT.CANCEL); 
				messageBox.setText("提示");
				messageBox.setMessage("SR异常，是否切换到另一台服务器？");
				if(SWT.OK == messageBox.open()){
					AddLostZfsWizard wizard = new AddLostZfsWizard(conn,hostRecordList,srs);
					NewVMDialog dialog = new NewVMDialog(Display.getCurrent().getActiveShell(),
							                 wizard);
					dialog.setPageSize(400, 300);
					dialog.create();
					dialog.open();
				}else{
					break;
				}
			}
		}

	}
}
