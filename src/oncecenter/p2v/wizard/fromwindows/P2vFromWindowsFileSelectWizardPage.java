package oncecenter.p2v.wizard.fromwindows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import oncecenter.Constants;
import oncecenter.util.Ssh;
import oncecenter.util.decryptPassword.Decrypt;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectSR;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import com.once.xenapi.Connection;
import com.once.xenapi.SR;

public class P2vFromWindowsFileSelectWizardPage extends WizardPage {

	CLabel fileCLabel;
	Combo fileCombo;
	
	VMTreeObjectSR selectedSR;
	String srIp;
	String srUsername;
	String srPassword;
	
	String fileName = "";
	
	protected P2vFromWindowsFileSelectWizardPage(String pageName) {
		super(pageName);
		this.setTitle("选择您要转化的镜像文件");
		
	}

	@Override
	public void createControl(Composite parent) {
		
		Composite composite = new Composite(parent,SWT.NONE);
		fileCLabel = new CLabel(composite,SWT.NULL);
		fileCLabel.setText("选择文件");
		fileCLabel.setBounds(20, 30, 300, 25);
		
		fileCombo = new Combo(composite,SWT.DROP_DOWN);
		fileCombo.setBounds(50, 70, 400, 30);
		
		selectedSR = ((P2vFromWindowsWizard)this.getWizard()).getSelectedSR();
		Connection conn = selectedSR.getConnection();
		
		
		try {
			String location = "";
			Map<String,String> otherConfig = ((SR)selectedSR.getApiObject()).getRecord(conn).otherConfig;
			location = otherConfig.get("location");
			srIp = location.substring(0, location.indexOf(":"));
			srUsername = otherConfig.get("username");
			srPassword = Decrypt.getString(otherConfig.get("password"));
			
			List<String> fileNameList = getListFiles(srIp, srUsername, srPassword);
			
			for(String fileName:fileNameList){
				fileCombo.add(fileName);
			}
			
			fileCombo.addSelectionListener(new SelectionListener(){

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
					
					widgetSelected(arg0);
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					
					fileName = fileCombo.getText().trim();
					System.out.println("filename:"+fileName);
					if(fileName.length()>0){
						setPageComplete(true);
					}
				}
				
			});
			fileCombo.layout();
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		setControl(composite);
		
	}
	
	private List<String> getListFiles(String ip, String username, String passwd) {  
		List<String> fileName = new ArrayList<String>();
		Ssh ssh = new Ssh(ip, username, passwd);
		if (!ssh.Connect()) {
			System.err.println("author or password isn't right!");
			return fileName; 
		}
		
		try {
			String ret = ssh.Command("ls -a " + Constants.p2vwindowsvhdPath);
			if(ssh.getExitCode() != 0) {
				System.err.println(Constants.p2vwindowsvhdPath + "目录不存在");
			}
			else {
				String[] files = ret.split("\n");
				for(String f : files) {
					if(f.length() >=4 && f.substring(f.length() - 4).equalsIgnoreCase(".vhd"))
						fileName.add(f);
				}
			}
				

		}  catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ssh.CloseSsh();
		}
		
		return fileName; 
	}  
	
	@Override
	public boolean canFlipToNextPage(){
		System.out.println("canfliptonext"+fileName);
		if(fileName.length()>0){
			return true;
		}else{
			return false;
		}
	}

}
