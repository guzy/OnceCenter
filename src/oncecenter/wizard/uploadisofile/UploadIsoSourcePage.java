package oncecenter.wizard.uploadisofile;

import oncecenter.util.Ssh;
import oncecenter.wizard.newvmfromtemp.NewVMPage;

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

public class UploadIsoSourcePage extends NewVMPage {

	String localFileName;
	private Text filename;
	private Label alert;
	Composite composite;
	protected UploadIsoSourcePage(String pageName) {
		super(pageName);
		this.setTitle("请选择要上传的镜像文件");
	}

	@Override
	public void createControl(Composite parent) {
		composite = new Composite(parent,SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		new Label(composite,SWT.NONE).setText("选择文件：");
		
		filename = new Text(composite,SWT.BORDER);
		filename.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filename.setEnabled(false);
		
		Button browse = new Button(composite,SWT.NONE);
		browse.setText("浏览");
		
		alert = new Label(composite,SWT.NONE);
		alert.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
		
		
		browse.addMouseListener(new org.eclipse.swt.events.MouseAdapter() {
			public void mouseDown(org.eclipse.swt.events.MouseEvent arg0) {
				FileDialog fileDialog= new FileDialog(new Shell(),SWT.SAVE); 
				fileDialog.setFilterExtensions(new String[]{"*.iso"});  
				fileDialog.setFilterNames(new String[]{"ISO Files(*.iso)","All Files(*.*)"});  
				String path = fileDialog.open();
				String name = fileDialog.getFileName();
				if(path!=null)
				{
					filename.setText(path);
//					String name = path.substring(path.lastIndexOf("/"));
					if(isGBK(name)){
						alert.setText("经检测，目标服务器不支持中文，请修改文件名");
						alert.pack();
						setPageComplete(false);
					}else{
						alert.setText("");
						alert.pack();
						setPageComplete(true);
					}
					setPageComplete(true);
				}
				localFileName = name;
			}
		});
		
		this.setControl(composite);
	}

	public String getFileName(){
		return filename.getText();
	}
	public String getFileNameBase(){
		return localFileName;
	}
	
	public boolean isGBK(String s){
		return s.getBytes().length==s.length()?false:true;
	}
	
//	@Override
//	public boolean canFlipToNextPage() {
//		if(isGBK(getFileName()))
//		{
//			return false;
//		}
//		return true;
//	}

	@Override
	protected boolean nextButtonClick() {
		if(getFileName().equals("") || getFileName() == null)
		{
			alert.setText("请选择要上传的ISO文件");
			alert.pack();
			//this.setPageComplete(false);
			//this.setControl(composite);
			return false;
		}
		return true;
	}
	
	
}
