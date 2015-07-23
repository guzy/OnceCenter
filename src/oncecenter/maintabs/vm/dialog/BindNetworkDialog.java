package oncecenter.maintabs.vm.dialog;

import oncecenter.maintabs.vm.NetWorkTab.NetworkInClient;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.once.xenapi.Connection;
import com.once.xenapi.Network;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.XenAPIException;
import com.once.xenapi.VIF;
import com.once.xenapi.VM;

public class BindNetworkDialog extends Dialog {

	NetworkInClient net;
	VMTreeObjectVM objectVM;
	VM vm;
	Connection c;
	String objectPhyVIF = null;
	
	private Combo vifList;
	public Combo getMaster() {
		return vifList;
	}

//	Text netName;
	Text mac;
	Text vlan;
//	IpAddressPane ip;
//	IpAddressPane subNetMask;
//	IpAddressPane gateway;
	Text rate;
	Text burst;
	
	public BindNetworkDialog(Shell parentShell,NetworkInClient net, VMTreeObjectVM objectVM) {
		super(parentShell);
		this.net = net;
		this.objectVM = objectVM;
		this.vm = (VM)objectVM.getApiObject();
		c = objectVM.getConnection();		
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("绑定网卡");
	}
	
//	protected Control createContents(Composite parent) {
//		   super.createContents(parent);
//		   this.getShell().setText("修改属性对话框");//设置对话框标题栏
//		   this.setTitle("修改属性");//设置标题信息
//		   this.setMessage("您可以通过该界面修改选中的虚拟机网络的属性",IMessageProvider.INFORMATION);//设置初始化对话框的提示信息
//		   return parent;
//	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		Composite area = new Composite(container, SWT.NONE);
		area.setLayout(new GridLayout(2,false));
		area.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		new Label (area,SWT.NONE).setText("可用的物理网卡：");
		vifList = new Combo(area,SWT.NONE);
		vifList.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		try {
			for (Network networks : Network.getAll(c)){
				String networkLabel = networks.getRecord(c).nameLabel;
				vifList.add(networkLabel);
			}
		} catch (BadServerResponse e2) {
			e2.printStackTrace();
		} catch (XenAPIException e2) {
			e2.printStackTrace();
		} catch (XmlRpcException e2) {
			e2.printStackTrace();
		}
		/*
		vifList.select(0);
		vifList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					for (Network networks : Network.getAll(c)){
						String networkLabel = networks.getRecord(c).nameLabel;
						if (vifList.getText().equals(networkLabel)) {
							objectVIFUuid = networks.getRecord(c).uuid;
							break;
						}
					}					
				} catch (BadServerResponse e1) {
					e1.printStackTrace();
				} catch (XenAPIException e1) {
					e1.printStackTrace();
				} catch (XmlRpcException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		netName = new Text(area,SWT.NONE);
		netName.setText(net.getName());
		netName.setEnabled(false);
		netName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		new Label (area,SWT.NONE).setText("mac地址：");
		mac = new Text(area,SWT.NONE);
		mac.setText(net.getMac());
		mac.setEnabled(false);
		mac.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		new Label(area,SWT.NONE).setText("vlan号：");
		vlan = new Text(area,SWT.NONE);
		vlan.setText(net.getVlan());
		vlan.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		*/
		return area;
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
	   //使用父类中创建按钮的方法创建登陆和退出按钮
	   createButton(parent, 0, "确定", true);
	   createButton(parent, 1, "取消", false);
	}
	/*
	* 当单击对话框中的按钮时，调用此方法
	*/
	protected void buttonPressed(int buttonId) {
	if (0 == buttonId){
		try {
			String phyNetwork = vifList.getText();
			System.out.println("点击确认之后phyNetwork = " + phyNetwork + 
					",MAC地址是 = " + net.getVif().getMAC(c));
			System.out.println("c = " + c + ",vm = " + vm.getNameLabel(c));
			net.getVif().set_physical_network(c, vm, phyNetwork);
		} catch (BadServerResponse e) {
			e.printStackTrace();
		} catch (XenAPIException e) {
			e.printStackTrace();
		} catch (XmlRpcException e) {
			e.printStackTrace();
		}
		close();
	}
	else if (1 == buttonId)
		close();
	}
}
