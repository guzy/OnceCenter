package oncecenter.maintabs.vm.dialog;

import oncecenter.maintabs.vm.NetWorkTab.NetworkInClient;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class EditNetworkDialog extends Dialog {

	NetworkInClient net;
	//VMTreeObjectVM objectVM;
	
	Text netName;
	Text mac;
	Text vlan;
//	IpAddressPane ip;
//	IpAddressPane subNetMask;
//	IpAddressPane gateway;
	Text rate;
	Text burst;
	
	public EditNetworkDialog(Shell parentShell,NetworkInClient net) {
		super(parentShell);
		this.net = net;
		//this.objectVM = objectVM;
		
	}

	protected void configureShell(Shell newShell) {
		   super.configureShell(newShell);
		   newShell.setText("�޸�����");
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
		
		new Label (area,SWT.NONE).setText("���磺");
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
		
//		new Label(area,SWT.NONE).setText("ip��ַ��");
//		ip = new IpAddressPane(area,SWT.BORDER);
//		
//		new Label(area,SWT.NONE).setText("�������룺");
//		subNetMask = new IpAddressPane(area,SWT.BORDER);
//		subNetMask.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		
//		new Label(area,SWT.NONE).setText("Ĭ�����أ�");
//		gateway = new IpAddressPane(area,SWT.BORDER);
		
		
//		new Label (area,SWT.NONE).setText("��·���٣�");
//		rate = new Text(area,SWT.BORDER);
//		rate.setText(net.getRate());
//		rate.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		
//		new Label (area,SWT.NONE).setText("��󲨶���");
//		burst = new Text(area,SWT.BORDER);
//		burst.setText(net.getBurst());
//		burst.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		return area;
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
	   //ʹ�ø����д�����ť�ķ���������½���˳���ť
	   createButton(parent, 0, "ȷ��", true);
	   createButton(parent, 1, "ȡ��", false);
	}
	/*
	* ���� Javadoc��
	* 
	* @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	* �������Ի����еİ�ťʱ�����ô˷���
	*/
	protected void buttonPressed(int buttonId) {
	   if (0 == buttonId){
		   net.setVlan(vlan.getText().trim());
//		   net.setRate(rate.getText().trim());
//		   net.setBurst(burst.getText().trim());
		   close();
	   }
	   else if (1 == buttonId)
	    close();
	}

}
