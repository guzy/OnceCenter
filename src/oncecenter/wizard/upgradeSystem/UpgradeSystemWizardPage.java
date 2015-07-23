package oncecenter.wizard.upgradeSystem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import oncecenter.wizard.newvmfromtemp.NewVMPage;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

public class UpgradeSystemWizardPage extends NewVMPage {

	protected UpgradeSystemWizardPage(String pageName) {
		super(pageName);
		
		this.setTitle("���߸���ϵͳ");
		this.setDescription("ͨ�����߸���ѹ����������ϵͳ������״̬�����°�����update��ͷ.");
	}

	private Text filename;
	private Label alert;
	private String updateFileName = null;
	Composite composite;
	
	@Override
	public void createControl(Composite parent) {
		
		composite = new Composite(parent,SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		new Label(composite,SWT.NONE).setText("ѡ���ļ���");
		
		filename = new Text(composite,SWT.BORDER);
		filename.setEditable(false);
		filename.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Button browse = new Button(composite,SWT.NONE);
		browse.setText("���");
		
		alert = new Label(composite,SWT.NONE);
		alert.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
		
		browse.addMouseListener(new org.eclipse.swt.events.MouseAdapter() {
			public void mouseDown(org.eclipse.swt.events.MouseEvent arg0) {
				FileDialog fileDialog= new FileDialog(new Shell(),SWT.SAVE); 
				fileDialog.setFilterExtensions(new String[]{"*.tar","*.*"});  
				fileDialog.setFilterNames(new String[]{"TAR Files(*.tar)","All Files(*.*)"});  
				String path = fileDialog.open();
				if(path!=null)
				{
					filename.setText(path);
					if(!isRightUpdatePackage(path))
					{
						alert.setText("����⣬���°��ļ���������ָ����ʽ�������Ƿ�����ȷ���°�.");
						alert.pack();
						setPageComplete(false);
					}
					if(isGBK(path)){
						alert.setText("����⣬Ŀ���������֧�����ģ����޸��ļ���");
						alert.pack();
						setPageComplete(false);
					}else{
						alert.setText("");
						alert.pack();
						setPageComplete(true);
					}
				}
			}
		});
		
		this.setControl(composite);
	}
	boolean isRightUpdatePackage(String path)
	{
		if(path.equals("") || path == null)
			return false;
		int index = path.indexOf("update");
		if(index != -1)
		{
			updateFileName = path.substring(index);
			return true;
		}
		return false;
	}
	public String getUpdateFileName() {
		return updateFileName;
	}
	public String getFileName(){
		return filename.getText();
	}
	
	public boolean isGBK(String s){
		return s.getBytes().length==s.length()?false:true;
	}
	
	@Override
	public boolean canFlipToNextPage() {
		return false;
	}

	@Override
	protected boolean nextButtonClick() {
		
		if(getFileName().equals("") || getFileName() == null)
		{
			alert.setText("��ѡ������ļ�");
			alert.pack();
			this.setPageComplete(false);
			this.setControl(composite);
			return false;
		}
		return true;
	}
}
