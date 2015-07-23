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
		   newShell.setText("修改属性");
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
		
		new Label (area,SWT.NONE).setText("网络：");
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
		
//		new Label(area,SWT.NONE).setText("ip地址：");
//		ip = new IpAddressPane(area,SWT.BORDER);
//		
//		new Label(area,SWT.NONE).setText("子网掩码：");
//		subNetMask = new IpAddressPane(area,SWT.BORDER);
//		subNetMask.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		
//		new Label(area,SWT.NONE).setText("默认网关：");
//		gateway = new IpAddressPane(area,SWT.BORDER);
		
		
//		new Label (area,SWT.NONE).setText("网路限速：");
//		rate = new Text(area,SWT.BORDER);
//		rate.setText(net.getRate());
//		rate.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		
//		new Label (area,SWT.NONE).setText("最大波动：");
//		burst = new Text(area,SWT.BORDER);
//		burst.setText(net.getBurst());
//		burst.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		return area;
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
	   //使用父类中创建按钮的方法创建登陆和退出按钮
	   createButton(parent, 0, "确定", true);
	   createButton(parent, 1, "取消", false);
	}
	/*
	* （非 Javadoc）
	* 
	* @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	* 当单击对话框中的按钮时，调用此方法
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
