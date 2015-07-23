package oncecenter.wizard.bandtoovs;

import java.util.ArrayList;

import oncecenter.network.OVS;
import oncecenter.util.ImageRegistry;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.PlatformUI;

import com.once.xenapi.Host;

public class BondtoOvsWizardPage extends WizardPage {

	List interfaceList;
	List unableList;
	List bondedInterfaceList;
	
	OVS ovs;
	
	ArrayList<String> interfaceNameList = new ArrayList<String>();
	ArrayList<String> unableInterfaceNameList = new ArrayList<String>();
	ArrayList<String> boundInterfaceNameList = new ArrayList<String>();
	
	VMTreeObjectHost hostObject;
	
	public BondtoOvsWizardPage(VMTreeObjectHost hostObject,OVS ovs) {
		super("wizardPage");
		setTitle("网络虚拟化");
		setDescription("网络虚拟化支持虚拟交换机，可将物理服务器转变成虚拟交换机，而虚拟机则通\n过虚拟网卡连接到虚拟机交换机上，对外通信。");
		this.hostObject = hostObject;
		this.ovs = ovs;
	}

	@Override
	public void createControl(Composite parent) {
		
		Composite composite = new Composite(parent,SWT.NONE);
		composite.setLayout(new GridLayout(3, true));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		new Label(composite,SWT.NONE).setText("可受管物理网卡列表");
		new Label(composite,SWT.NONE);
		new Label(composite,SWT.NONE).setText("已绑定网卡列表");
		
		Composite list = new Composite(composite,SWT.NONE);
		list.setLayout(new GridLayout(1, false));
		list.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		interfaceList = new List(list,SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL|SWT.SIMPLE);
		interfaceList.setLayoutData(new GridData(GridData.FILL_BOTH));
		Host host = (Host)hostObject.getApiObject();
		try {
			
			for(String s :host.getInterfaces(hostObject.getConnection())){
				if(!ovs.checkInterfaceIP(s)){
					interfaceNameList.add(s);
				}else{
					unableInterfaceNameList.add(s);
				}
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		for(String s:interfaceNameList){
			interfaceList.add(s);
		}
		
		new Label(list,SWT.NONE).setText("固定物理网卡列表");
		
		unableList = new List(list,SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL|SWT.SIMPLE);
		unableList.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		unableList.setEnabled(false);
		for(String s:unableInterfaceNameList){
			unableList.add(s);
		}
		
		
		Composite picture = new Composite(composite,SWT.NONE);
		picture.setLayout(new GridLayout(1, false));
		GridData g1 = new GridData(GridData.FILL_BOTH);
		g1.verticalAlignment = GridData.CENTER;
		g1.horizontalAlignment = GridData.CENTER;
		picture.setLayoutData(g1);
		{
			final CLabel imageLable = new CLabel(picture, SWT.CENTER);
			Image image = ImageRegistry.getImage(ImageRegistry.ARROW_RIGHT);
			imageLable.setImage(image);
			GridData g = new GridData();
			g.verticalAlignment = GridData.CENTER;
			g.horizontalAlignment = GridData.CENTER;
			imageLable.setLayoutData(g);
			
			imageLable.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				for(int i:interfaceList.getSelectionIndices()){
					String s = interfaceList.getItem(i);
					bondedInterfaceList.add(s);
					interfaceList.remove(i);
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				
				
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				
				
			}
			
			});	
			
			imageLable.addMouseTrackListener(new MouseTrackListener() {

				@Override
				public void mouseEnter(MouseEvent arg0) {
					
					imageLable.setBackground(PlatformUI.getWorkbench().getDisplay().getSystemColor(29));
				}

				@Override
				public void mouseExit(MouseEvent arg0) {
					
					imageLable.setBackground(PlatformUI.getWorkbench().getDisplay().getSystemColor(22));
				}

				@Override
				public void mouseHover(MouseEvent arg0) {
					
					
				}
				
				});	
		}
		{
			final CLabel imageLable = new CLabel(picture, SWT.CENTER);
			Image image = ImageRegistry.getImage(ImageRegistry.ARROW_LEFT);
			imageLable.setImage(image);
			GridData g = new GridData();
			g.verticalAlignment = GridData.CENTER;
			g.horizontalAlignment = GridData.CENTER;
			imageLable.setLayoutData(g);
			
			imageLable.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				for(int i:bondedInterfaceList.getSelectionIndices()){
					String s = bondedInterfaceList.getItem(i);
					interfaceList.add(s);
					bondedInterfaceList.remove(i);
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				
				
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				
				
			}
			
			});	
			
			imageLable.addMouseTrackListener(new MouseTrackListener() {

				@Override
				public void mouseEnter(MouseEvent arg0) {
					
					imageLable.setBackground(PlatformUI.getWorkbench().getDisplay().getSystemColor(29));
				}

				@Override
				public void mouseExit(MouseEvent arg0) {
					
					imageLable.setBackground(PlatformUI.getWorkbench().getDisplay().getSystemColor(22));
				}

				@Override
				public void mouseHover(MouseEvent arg0) {
					
					
				}
				
				});	
		}
		
		bondedInterfaceList = new List(composite,SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL|SWT.SIMPLE);
		bondedInterfaceList.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		setControl(composite);
	}

}
