package oncecenter.wizard.newsr;

import oncecenter.util.TypeUtil;
import oncecenter.wizard.newvmfromtemp.NewVMPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.PlatformUI;

public class SRTypeWizardPage extends NewVMPage {

	Button ocfs2Button;
	Button nfsvhdButton;
	Button nfszfsButton;
	Button gpfsButton;
	Button mfsButton;
	Button nfsisoButton;
	Button gpfsisoButton;
	Button nfshaButton;
	Button gpfshaButton;
	private Composite infoComp;
	private Label infoLabel;
	
	/**
	 * Create the wizard.
	 */
	public SRTypeWizardPage() {
		super("wizardPage");
		setTitle("–¬Ω®¥Ê¥¢");
		setDescription("—°‘Ò¥Ê¥¢¿‡–Õ");
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(2,true));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		setControl(composite);
		
		Composite selectComp = new Composite(composite, SWT.NULL);
		selectComp.setLayout(new GridLayout(1,false));
		selectComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label virtualDiskLabel = new Label(selectComp,SWT.NONE);
		virtualDiskLabel.setText("–Èƒ‚¥≈≈Ã¥Ê¥¢£∫");
		virtualDiskLabel.setFont(new Font(PlatformUI.getWorkbench().getDisplay(),"Lucida Grande",11,SWT.BOLD));
		
		ocfs2Button = new Button(selectComp, SWT.RADIO);
		ocfs2Button.setText(" OCFS2");
		ocfs2Button.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		        infoLabel.setText("MFS\nksjdsjldjl\nlskdfsdklfkd");
		        infoComp.layout();
		      }
		    });
		
		mfsButton = new Button(selectComp, SWT.RADIO);
		mfsButton.setText(" MFS");
		mfsButton.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		        infoLabel.setText("MFS\nksjdsjldjl\nlskdfsdklfkd");
		        infoComp.layout();
		      }
		    });
		
		gpfsButton = new Button(selectComp, SWT.RADIO);
		gpfsButton.setText(" GPFS");
		gpfsButton.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		        infoLabel.setText("GPFS\nksjdsjldjl\nlskdfsdklfkd");
		        infoComp.layout();
		      }
		    });
		nfszfsButton = new Button(selectComp, SWT.RADIO);
		nfszfsButton.setText(" NFS-ZFS");
		nfszfsButton.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		        infoLabel.setText("NFS-ZFS");
		        infoComp.layout();
		      }
		    });
		nfsvhdButton = new Button(selectComp, SWT.RADIO);
		nfsvhdButton.setText(" NFS-VHD");
		new Label(selectComp,SWT.NONE);
		
		Label isoLabel = new Label(selectComp,SWT.NONE);
		isoLabel.setText("ISOø‚£∫");
		isoLabel.setFont(new Font(PlatformUI.getWorkbench().getDisplay(),"Lucida Grande",11,SWT.BOLD));
		
		gpfsisoButton = new Button(selectComp, SWT.RADIO);
		gpfsisoButton.setText(" GPFS-ISO");
		nfsisoButton = new Button(selectComp, SWT.RADIO);
		nfsisoButton.setText(" NFS-ISO");
		new Label(selectComp,SWT.NONE);
		
		Label haLabel = new Label(selectComp,SWT.NONE);
		haLabel.setText("HAø‚£∫");
		haLabel.setFont(new Font(PlatformUI.getWorkbench().getDisplay(),"Lucida Grande",11,SWT.BOLD));
		
		gpfshaButton = new Button(selectComp, SWT.RADIO);
		gpfshaButton.setText(" GPFS-HA");
		nfshaButton = new Button(selectComp, SWT.RADIO);
		nfshaButton.setText(" NFS-HA");
		new Label(selectComp,SWT.NONE);
		
		
		infoComp = new Composite(composite, SWT.NULL);
		infoComp.setLayout(new GridLayout(1,false));
		infoComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		infoLabel = new Label(infoComp,SWT.TOP);
		infoLabel.setVisible(false);
		
////		Label VHDLabel = new Label(container, SWT.NONE);
////		VHDLabel.setText("\u865A\u62DF\u78C1\u76D8\u5B58\u50A8");
////		VHDLabel.setBounds(66, 80, 61, 17);
////		VHDRadioButton = new Button(container, SWT.RADIO);
////		VHDRadioButton.setBounds(86, 110, 97, 17);
////		VHDRadioButton.setText(" NFS VHD");
////		VHDRadioButton.setSelection(true);
//
//		Label GPFSLabel = new Label(container, SWT.NONE);
//		GPFSLabel.setText("GPFS\u5E93");
//		GPFSLabel.setBounds(66, 80, 61, 17);
//		GPFSRadioButton = new Button(container, SWT.RADIO);
//		GPFSRadioButton.setText(" GPFS");
//		GPFSRadioButton.setBounds(86, 110, 97, 17);
//		
//		Label ISOLabel = new Label(container, SWT.NONE);
//		ISOLabel.setText("ISO\u5E93");
//		ISOLabel.setBounds(266, 80, 61, 17);
//		ISORadioButton = new Button(container, SWT.RADIO);
//		ISORadioButton.setText(" NFS ISO");
//		ISORadioButton.setBounds(286, 110, 97, 17);
//
//		
////		Label ZFSLabel = new Label(container, SWT.NONE);
////		ZFSLabel.setText("ZFS\u5E93");
////		ZFSLabel.setBounds(66, 200, 61, 17);
////		ZFSRadioButton = new Button(container, SWT.RADIO);
////		ZFSRadioButton.setText(" GLUSTER ZFS");
////		ZFSRadioButton.setBounds(86, 230, 97, 17);
//		
//
//		Label HALabel = new Label(container, SWT.NONE);
//		HALabel.setText("HA\u5E93");
//		HALabel.setBounds(66, 200, 61, 17);
//		HARadioButton = new Button(container, SWT.RADIO);
//		HARadioButton.setText(" NFS HA");
//		HARadioButton.setBounds(86, 230, 97, 17);
//
//		Label ZFSLabel = new Label(container, SWT.NONE);
//		ZFSLabel.setText("ZFS\u5E93");
//		ZFSLabel.setBounds(266, 200, 61, 17);
//		ZFSRadioButton = new Button(container, SWT.RADIO);
//		ZFSRadioButton.setText(" GLUSTER ZFS");
//		ZFSRadioButton.setBounds(286, 230, 97, 17);
//		
////		Label HALabel = new Label(container, SWT.NONE);
////		HALabel.setText("HA\u5E93");
////		HALabel.setBounds(266, 200, 61, 17);
////		HARadioButton = new Button(container, SWT.RADIO);
////		HARadioButton.setText(" NFS HA");
////		HARadioButton.setBounds(286, 230, 97, 17);
		
	}
	
	
	@Override
	protected boolean nextButtonClick() {
		
		String type;
		String contentType;
		if(gpfsButton.getSelection()){
			type = TypeUtil.gpfsDiskType;
			contentType = "vhd";
		}else if(ocfs2Button.getSelection()){
			type = TypeUtil.ocfs2DiskType;
			contentType = "vhd";
		}else if(nfszfsButton.getSelection()){
			type = TypeUtil.nfsZfsType;
			contentType = "vhd";
		}else if(mfsButton.getSelection()){
			type = TypeUtil.mfsDiskType;
			contentType = "vhd";
		}else if(nfsvhdButton.getSelection()){
			type = TypeUtil.nfsDiskType;
			contentType = "vhd";
		}else if (gpfsisoButton.getSelection()) {
			type = TypeUtil.gpfsIsoType;
			contentType = "iso";
		} else if (nfsisoButton.getSelection()) {
			type = TypeUtil.nfsIsoType;
			contentType = "iso";
		} else if(gpfshaButton.getSelection()){
			type = TypeUtil.gpfsHaType;
			contentType = "sxp";
		}else {
			type = TypeUtil.nfsHaType;
			contentType = "sxp";
		}
		((NewSRWizard)this.getWizard()).type = type;
		((NewSRWizard)this.getWizard()).contentType = contentType;
		
		return true;
	}

	public boolean isIso() {
		return gpfsisoButton.getSelection()||nfsisoButton.getSelection();
	}

	public boolean isHa() {
		return gpfshaButton.getSelection()||nfshaButton.getSelection();
	}
}
