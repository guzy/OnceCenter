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
		newShell.setText("������");
	}
	
//	protected Control createContents(Composite parent) {
//		   super.createContents(parent);
//		   this.getShell().setText("�޸����ԶԻ���");//���öԻ��������
//		   this.setTitle("�޸�����");//���ñ�����Ϣ
//		   this.setMessage("������ͨ���ý����޸�ѡ�е���������������",IMessageProvider.INFORMATION);//���ó�ʼ���Ի������ʾ��Ϣ
//		   return parent;
//	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		Composite area = new Composite(container, SWT.NONE);
		area.setLayout(new GridLayout(2,false));
		area.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		new Label (area,SWT.NONE).setText("���õ�����������");
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
		
		new Label (area,SWT.NONE).setText("mac��ַ��");
		mac = new Text(area,SWT.NONE);
		mac.setText(net.getMac());
		mac.setEnabled(false);
		mac.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		new Label(area,SWT.NONE).setText("vlan�ţ�");
		vlan = new Text(area,SWT.NONE);
		vlan.setText(net.getVlan());
		vlan.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		*/
		return area;
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
	   //ʹ�ø����д�����ť�ķ���������½���˳���ť
	   createButton(parent, 0, "ȷ��", true);
	   createButton(parent, 1, "ȡ��", false);
	}
	/*
	* �������Ի����еİ�ťʱ�����ô˷���
	*/
	protected void buttonPressed(int buttonId) {
	if (0 == buttonId){
		try {
			String phyNetwork = vifList.getText();
			System.out.println("���ȷ��֮��phyNetwork = " + phyNetwork + 
					",MAC��ַ�� = " + net.getVif().getMAC(c));
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
