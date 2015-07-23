package oncecenter.maintabs.pool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import oncecenter.Activator;
import oncecenter.maintabs.OncePoolTabItem;
import oncecenter.maintabs.OnceTabItem;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class HostListTab extends OncePoolTabItem {
	
	private Table hostTable;
	private double cpuUsage;
	private double memoryTotal;
	private double memoryUsage;
	private double disk;
	private double net;
	
	private ArrayList<TableEditor> editors = new ArrayList<TableEditor>();
	
	public HostListTab(CTabFolder arg0, int arg1, VMTreeObjectPool object) {
		super(arg0, arg1, object);
		this.objectPool = object;
		setText("����");
	}

	public HostListTab(CTabFolder arg0, int arg1, int arg2, VMTreeObjectPool object) {
		super(arg0, arg1, arg2, object);
		this.objectPool = object;
		setText("����");
	}
	
	public boolean Init(){
		composite = new Composite(folder, SWT.NONE); 
		this.setControl(composite);
		composite.setLayout(new GridLayout(1,false));
		composite.setBackground(new Color(null,255,255,255));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label label=new Label(composite,SWT.NONE);
		label.setBackground(new Color(null,255,255,255));
		label.setText(" ");
		
		hostTable=new Table(composite, SWT.BORDER | SWT.V_SCROLL
		        | SWT.H_SCROLL|SWT.FULL_SELECTION);
		hostTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		hostTable.setHeaderVisible(true);
		hostTable.setLinesVisible(false);
		
		TableColumn name = new TableColumn(hostTable, SWT.CENTER|SWT.BOLD);
		name.setText("����");
		name.setWidth(150);
		TableColumn state = new TableColumn(hostTable, SWT.CENTER|SWT.BOLD);
		state.setText("״��");
		state.setWidth(100);
		TableColumn kpi = new TableColumn(hostTable, SWT.CENTER|SWT.BOLD);
		kpi.setText("KPI");
		kpi.setWidth(100);
//		TableColumn cpu = new TableColumn(hostTable, SWT.CENTER|SWT.BOLD);
//		cpu.setText("���� CPU - %");
//		cpu.setWidth(150);
//		TableColumn memoryTotal = new TableColumn(hostTable, SWT.CENTER|SWT.BOLD);
//		memoryTotal.setText("�����ڴ� - MB");
//		memoryTotal.setWidth(150);
//		TableColumn memoryUsage = new TableColumn(hostTable, SWT.CENTER|SWT.BOLD);
//		memoryUsage.setText("�ͻ����ڴ� - %");
//		memoryUsage.setWidth(150);
//		TableColumn disk = new TableColumn(hostTable, SWT.CENTER|SWT.BOLD);
//		disk.setText("���������� - Kbps");
//		disk.setWidth(80);
//		TableColumn net = new TableColumn(hostTable, SWT.CENTER|SWT.BOLD);
//		net.setText("���������� - Kbps");
//		net.setWidth(80);
		TableColumn ip = new TableColumn(hostTable, SWT.CENTER|SWT.BOLD);
		ip.setText("IP��ַ");
		ip.setWidth(120);
		TableColumn remarks = new TableColumn(hostTable, SWT.CENTER|SWT.BOLD);
		remarks.setText("��ע");
		remarks.setWidth(80);
		
		VMTreeObject parent = (VMTreeObject)objectPool;
//		tableViewer = new TableViewer(vmTable);
//		tableViewer.setContentProvider(new ArrayContentProvider());
//		tableViewer.setLabelProvider(new TableLabelProvider());
//		tableViewer.setInput(parent.getChildren());
		for(VMTreeObject o:parent.getChildren()){
			addItem(o);
		}
		hostTable.pack();
		composite.layout();
//		Timer refreshTimer = new Timer("RefreshTimer");
//		refreshTimer.schedule(new RefreshTimer(this,PlatformUI.getWorkbench().getDisplay()), 3000, 15000);
		return true;
	}
	
	public void addItem(VMTreeObject object){
		if(object instanceof VMTreeObjectHost){
			TableItem item=new TableItem(hostTable,SWT.NONE);
			item.setText(0, object.getName());
//			item.setImage(0, AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
//					"icons/console/asrunning.png").createImage());
			item.setText(1,"������");
			item.setText(2,Math.random()+"");
			item.setText(3,((VMTreeObjectHost) object).getIpAddress());
			//ip address ��ȡ����
			//item.setText(3,((VMTreeObjectHost)object).getIpAddress());
		}
		
	}
	
	
	
}
