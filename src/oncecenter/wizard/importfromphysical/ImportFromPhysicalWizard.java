package oncecenter.wizard.importfromphysical;

import java.io.IOException;

import oncecenter.util.Ssh;
import oncecenter.wizard.newvmfromtemp.FinishWizardPage;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

public class ImportFromPhysicalWizard extends Wizard {

	ImportFromPhysicalWizardPage mainPage;
	public boolean isSSH = false;

	SourceAddressWizardPage saPage;
	
	SSHDestinationAddressWizardPage sshDaPage;
	SSHFinishWizardPage sshFinPage;
	
	NFSDestinationAddressWizardPage nfsDaPage;
	NFSFinishWizardPage nfsFinPage;
	
	private String sAddress;
	private String sAddressUser;
	private String sAddressPas;
	private String sAddressDir;
	
	private String dAddress;
	private String dAddressUser;
	private String dAddressPas;
	private String dAddressDir;
	
	private String fileName;
	public ImportFromPhysicalWizard()
	{
		setWindowTitle("从物理机导入");
	}
	@Override
	public void addPages()
	{
		//mainPage = new ImportFromPhysicalWizardPage("Import from physical");
		//this.addPage(mainPage);
		saPage = new SourceAddressWizardPage("source page");
		//ssh pages
		//sshDaPage = new SSHDestinationAddressWizardPage("SSH Destination Address");
		//sshFinPage = new SSHFinishWizardPage("ssh finish page");
		//nfs pages
		nfsDaPage = new NFSDestinationAddressWizardPage("nfs Destination Address");
		//nfsFinPage = new NFSFinishWizardPage("nfs finish page");
		
		this.addPage(saPage);
		//this.addPage(sshDaPage);
		this.addPage(nfsDaPage);
		//this.addPage(sshFinPage);
		//this.addPage(nfsFinPage);
	}
	@Override
	public boolean canFinish()
	{ 
		IWizardPage page = getContainer().getCurrentPage();
		if( page instanceof SSHFinishWizardPage || page instanceof NFSFinishWizardPage)
			return true;
		else return false;
	}
	
	@Override
	public boolean performFinish() {
		
		sAddress = saPage.getsAddressText().getText().trim();
		sAddressUser = saPage.getsAddressUserText().getText().trim();
		sAddressPas = saPage.getsAddressPasText().getText().trim();
		
		if(isSSH)
		{
//			dAddressUser = sshDaPage.getdAddressUserText().getText().trim();
//			dAddress = sshDaPage.getdAddressText().getText().trim();
//			dAddressPas = sshDaPage.getdAddressPasText().getText().trim();
//			dAddressDir = sshDaPage.getdAddressDirText().getText().trim();
//			fileName = sshFinPage.getFileNameText().getText().trim();
//			//do something
//			Ssh ssh = new Ssh(sAddress, sAddressUser, sAddressPas);
//			if(!ssh.Connect()) {
//				System.err.println("author or password isn't right!");
//				return false;
//			}
//			String ret = "";
//			String dev = getDevName(ssh);
//			StringBuffer cmdBuffer = new StringBuffer("dd bs=512k if=/dev/");
//			if(dev != null && dev.endsWith("da"))
//				cmdBuffer.append(dev);
//			else {
//				System.err.println("");
//				ssh.CloseSsh();
//				return false;
//			}
//			ssh.CloseSsh();
//			
//			
//			cmdBuffer.append(" | sshpass -p \"");
//			cmdBuffer.append(dAddressPas);
//			cmdBuffer.append("\" ssh -o StrictHostKeyChecking=no ");
//			cmdBuffer.append(dAddressUser);
//			cmdBuffer.append("@");
//			cmdBuffer.append(dAddress);
//			cmdBuffer.append(" \"dd of=");
//			cmdBuffer.append(dAddressDir);
//			if(!dAddressDir.endsWith("/"))
//				cmdBuffer.append("/");
//			cmdBuffer.append(fileName); 
//			cmdBuffer.append(" bs=512k\"");
//			
//			new Thread(new excuteSSHCommand(sAddress, sAddressUser, sAddressPas, cmdBuffer.toString())).start();
		}
		else
		{

			sAddressDir = nfsDaPage.getsAddressDirText().getText().trim();
	
			dAddress = nfsDaPage.getdAddressText().getText().trim();
			dAddressDir = nfsDaPage.getdAddressDirText().getText().trim();
			
			fileName = nfsFinPage.getFileNameText().getText().trim();
			
			//do something
			Ssh ssh = new Ssh(sAddress, sAddressUser, sAddressPas);
			if(!ssh.Connect()) {
				System.err.println("author or password isn't right!");
				return false;
			}
			
			String ret = "";
			String dev = getDevName(ssh);
			ssh.CloseSsh();
			
			StringBuffer cmdBuffer = new StringBuffer("dd if=/dev/");
			cmdBuffer.append(dev);
			cmdBuffer.append(" of=");
			cmdBuffer.append(sAddressDir);
			if(dAddressDir != null && !sAddressDir.endsWith("/"));
				cmdBuffer.append("/");
			cmdBuffer.append(fileName); 
			
			new Thread(new excuteSSHCommand(sAddress, sAddressUser, sAddressPas, cmdBuffer.toString())).start();
		}
		return true;
	}
	
	private String getDevName(Ssh ssh) {
		String dev = null;
		String ret = null;
		try {		
			ret = ssh.Command("ls /dev/");
			for(String item : ret.split("\n")) {
				if(item.endsWith("da")) {
					dev = item;
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ssh.CloseSsh();
		}
		return dev;
	}
	
	public class excuteSSHCommand implements Runnable {
		
		private String address;
		private String username;
		private String password;
		private String cmd;
		
		public excuteSSHCommand(String address, String username, String password, String cmd) {
			this.address = address;
			this.username = username;
			this.password = password;
			this.cmd = cmd;
		}
		
		@Override
		public void run() {
			
			String ret = "";
			Ssh ssh = null;
			try {		
				ssh = new Ssh(this.address, this.username, this.password);
				if(!ssh.Connect()) {
					System.err.println("author or password isn't right!");
					return;
				}
				ret = ssh.Command(this.cmd);	
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				ssh.CloseSsh();
			}
		}
		
	}
}
