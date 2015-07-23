package oncecenter.maintabs.vm;

import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.io.IOException;

import oncecenter.daemon.GetRecordTask;
import oncecenter.maintabs.OnceVMTabItem;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectPool;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectRoot;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wb.swt.SWTResourceManager;

import com.once.xenapi.Types;
import com.once.xenapi.VM;
import com.once.xenapi.Types.BadServerResponse;
import com.once.xenapi.Types.XenAPIException;

public class ConsoleTab extends OnceVMTabItem {

	public consoleInitAction consoleAction=new consoleInitAction(
			PlatformUI.getWorkbench().getDisplay());;
	
	//added by lishun
	private static final int BUTTON_HEIGHT = 26;
	private static final int LEFT_BUTTON_WIDTH = 150;
	private static final int RIGHT_BUTTON_WIDTH = 60;
	private static final int PADDING = 30;
	
	private static final int ALARM_PADDING = 50;

	Button defaultRebootBT;
	Button defaultReconnectBT;
	
	VM vm;
	
	public ConsoleTab(CTabFolder arg0, int arg1, int arg2, VMTreeObjectVM object) {
		super(arg0, arg1, arg2, object);
		setText("控制台");
		this.objectVM = object;
	}

	public boolean Init() {		
		composite = new Composite(folder, SWT.EMBEDDED);
		setControl(composite);
		
		Composite consoleComposite = this.defaultInit(composite);

		vm = (VM) objectVM.getApiObject();
		try {
			Types.VmPowerState state = vm.getPowerState(objectVM.getConnection());
			if (state.equals(Types.VmPowerState.RUNNING)) {
				Label label = new Label(consoleComposite, SWT.CENTER);
				label.setText("正在连接到控制台");
				label.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
				FormData fd_alarm = new FormData();
				fd_alarm.top = new FormAttachment(0, ALARM_PADDING);
				fd_alarm.left = new FormAttachment(50, -getStringWidth(label)/2);
				label.setLayoutData(fd_alarm);
				consoleAction = new consoleInitAction(
						PlatformUI.getWorkbench().getDisplay());
				consoleAction.start();
			} else {
				{
					Label label = new Label(consoleComposite, SWT.CENTER);
					label.setText("该虚拟机处于关闭状态");
					label.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
					FormData fd_alarm = new FormData();
					fd_alarm.top = new FormAttachment(0, ALARM_PADDING);
					fd_alarm.left = new FormAttachment(50, -getStringWidth(label)/2);
					label.setLayoutData(fd_alarm);
				}
				{
					Label label = new Label(consoleComposite, SWT.CENTER);
					label.setText("如果您重启了虚拟机一段时间，但控制台没有显示，请点击右下角的重连按钮");
					label.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
					FormData fd_alarm = new FormData();
					fd_alarm.top = new FormAttachment(10, ALARM_PADDING);
					fd_alarm.left = new FormAttachment(50, -getStringWidth(label)/2);
					label.setLayoutData(fd_alarm);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		composite.layout();
		return true;
	}

	public int getStringWidth(Label label){
		int width = 0;
		GC gc = new GC(label);
		for(int i=0;i<label.getText().length();i++){     
			char c = label.getText().charAt(i);     
			width += gc.getAdvanceWidth(c);     
		}     
		gc.dispose();     
		return width;
	}
	public Composite defaultInit(Composite comp){
		FormData fd_parent = new FormData();
		fd_parent.bottom = new FormAttachment(100, 0);
		fd_parent.right = new FormAttachment(100, 0);
		fd_parent.top = new FormAttachment(0, 0);
		fd_parent.left = new FormAttachment(0, 0);
		
		comp.setLayoutData(fd_parent);
		comp.setLayout(new FormLayout());
		comp.setBackground(new Color(null, 255,255,255));
		comp.setBackgroundMode(SWT.DEFAULT);

		FormData fd_console = new FormData();
		fd_console.bottom = new FormAttachment(100, -PADDING);
		fd_console.right = new FormAttachment(100, 0);
		fd_console.top = new FormAttachment(0, 0);
		fd_console.left = new FormAttachment(0, 0);
		
		Composite consoleComposite = new Composite(comp,SWT.NONE);
		consoleComposite.setLayoutData(fd_console);
		consoleComposite.setLayout(new FormLayout());

		defaultRebootBT = new Button(comp, SWT.NONE);
		FormData fd_leftBT = new FormData();
		fd_leftBT.bottom = new FormAttachment(100, 0);
		fd_leftBT.right = new FormAttachment(0, LEFT_BUTTON_WIDTH);
		fd_leftBT.top = new FormAttachment(100, -BUTTON_HEIGHT);
		fd_leftBT.left = new FormAttachment(0, 0);
		defaultRebootBT.setLayoutData(fd_leftBT);
		defaultRebootBT.setText("Ctrl+Alt+Del");
		defaultRebootBT.setEnabled(false);

		defaultReconnectBT = new Button(comp, SWT.NONE);
		final FormData fd_rightBT = new FormData();
		fd_rightBT.bottom = new FormAttachment(100, 0);
		fd_rightBT.right = new FormAttachment(100, 0);
		fd_rightBT.top = new FormAttachment(100, -BUTTON_HEIGHT);
		fd_rightBT.left = new FormAttachment(100, -RIGHT_BUTTON_WIDTH);
		defaultReconnectBT.setLayoutData(fd_rightBT);
		defaultReconnectBT.setText("重连");
		defaultReconnectBT.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent arg0) {
				if(objectVM.localVncViewer!=null){
					objectVM.localVncViewer.disconnect();
					objectVM.localVncViewer=null;
				}
				composite.dispose();
				Init();
			}
		});
		
		return consoleComposite;
	}
	
	public class consoleInitAction extends Thread {
		Display display;
		Composite parent;
		Frame frame;
		public consoleInitAction(Display display) {
			this.display = display;
		}
		
		public void run() {
			if (!this.display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						parent = new Composite(folder, SWT.NONE);
						final FormData fd_parent = new FormData();
						fd_parent.bottom = new FormAttachment(100, 0);
						fd_parent.right = new FormAttachment(100, 0);
						fd_parent.top = new FormAttachment(0, 0);
						fd_parent.left = new FormAttachment(0, 0);
						
						parent.setLayoutData(fd_parent);
						parent.setLayout(new FormLayout());
						parent.setBackground(new Color(null, 255,255,255));

						final FormData fd_console = new FormData();
						fd_console.bottom = new FormAttachment(100, -PADDING);
						fd_console.right = new FormAttachment(100, 0);
						fd_console.top = new FormAttachment(0, 0);
						fd_console.left = new FormAttachment(0, 0);
						
						final Composite consoleComposite = new Composite(parent,
								SWT.EMBEDDED);
						consoleComposite.setBackground(new Color(null, 255, 255,
								255));
						consoleComposite.setLayoutData(fd_console);

						consoleComposite.addKeyListener(new KeyListener(){
							   public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
							    if(e.keyCode == SWT.TAB){
							     tabPress();
							    }
							   }
							   public void keyReleased(org.eclipse.swt.events.KeyEvent e) {}
							   });
						
						frame = SWT_AWT.new_Frame(consoleComposite);
						
						Button leftBT = new Button(parent, SWT.NONE);
						final FormData fd_leftBT = new FormData();
						fd_leftBT.bottom = new FormAttachment(100, 0);
						fd_leftBT.right = new FormAttachment(0, LEFT_BUTTON_WIDTH);
						fd_leftBT.top = new FormAttachment(100, -BUTTON_HEIGHT);
						fd_leftBT.left = new FormAttachment(0, 0);
						leftBT.setLayoutData(fd_leftBT);
						leftBT.setText("Ctrl+Alt+Del");

						leftBT.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseDown(MouseEvent arg0) {
								if(objectVM.localVncViewer!=null){
									KeyEvent localKeyEvent = new KeyEvent(objectVM.localVncViewer, 401, 0L, 10, 127);
									try {
										objectVM.localVncViewer.rfb.writeKeyEvent(localKeyEvent);
									} catch (IOException e) {
										
										e.printStackTrace();
									}
								}
								
							}
						});

						Button rightBT = new Button(parent, SWT.NONE);
						final FormData fd_rightBT = new FormData();
						fd_rightBT.bottom = new FormAttachment(100, 0);
						fd_rightBT.right = new FormAttachment(100, 0);
						fd_rightBT.top = new FormAttachment(100, -BUTTON_HEIGHT);
						fd_rightBT.left = new FormAttachment(100, -RIGHT_BUTTON_WIDTH);
						rightBT.setLayoutData(fd_rightBT);
						rightBT.setText("重连");

						
						

						rightBT.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseDown(MouseEvent arg0) {
								if(objectVM.localVncViewer!=null){
									objectVM.localVncViewer.disconnect();
									objectVM.localVncViewer=null;
								}
								composite.dispose();
								Init();
							}
						});
					}
				};
				this.display.syncExec(runnable);
			}
			int flag = 0;
			while (!objectVM.setVncViewer(frame)) {
				flag++;
				if(flag>2){
					return;
				}
			}
			flag = 0;
			while(objectVM.localVncViewer==null||!objectVM.localVncViewer.isFinished){
				try {
					Thread.sleep(1000);
					flag++;
					if(flag>10){
						if (!this.display.isDisposed()) {
							Runnable runnable = new Runnable() {
								public void run() {
									alarm();
								}
							};
							this.display.syncExec(runnable);
						}
						
						return;
					}
				} catch (InterruptedException e1) {
					
					e1.printStackTrace();
				}
			}
			if (!this.display.isDisposed()) {
				Runnable runnable = new Runnable() {
					public void run() {
						
						
						Composite c = composite;
						composite=parent;
						setControl(composite);
						composite.layout();
						c.dispose();
					}
				};
				this.display.syncExec(runnable);
			}
			
			while(true){
				if(objectVM.localVncViewer!=null&&objectVM.localVncViewer.isDisconnect){
					if (!this.display.isDisposed()) {
						Runnable runnable = new Runnable() {
							public void run() {
								consoleFresh(false);
								GetRecordTask.refreshTree(display);
//								VMTreeObjectRoot root;
//								if(objectVM.getParent().getParent() instanceof VMTreeObjectRoot){
//									root = (VMTreeObjectRoot)objectVM.getParent().getParent();
//								}else{
//									root = (VMTreeObjectRoot)objectVM.getParent();
//								}
//								if(root instanceof VMTreeObjectPool){
//									try {
//										VM.Record record = vm.getRecord(objectVM.getConnection());
//										GetRecordTimer.refreshVM(objectVM, record
//												, root instanceof VMTreeObjectPool?true:false, root
//														, display, root.hostMap);
//										GetRecordTimer.refreshTree(display);
//									} catch (Exception e) {
//										
//										e.printStackTrace();
//									}
//								}
								
							}
						};
						this.display.syncExec(runnable);
					}
					
				}else{
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						
						e1.printStackTrace();
					}
				}
				
			}
		}
	}

	public void alarm(){
		
		Composite oldComposite = (Composite) this.getControl();
		Composite newComposite = new Composite(folder, SWT.NONE);
		Composite consoleComposite = this.defaultInit(newComposite);
		{
			Label label = new Label(consoleComposite, SWT.CENTER);
			label.setText("连接失败！可能是由于有其他用户正在连接该虚拟机的控制台");
			label.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
			FormData fd_alarm = new FormData();
			fd_alarm.top = new FormAttachment(0, ALARM_PADDING);
			fd_alarm.left = new FormAttachment(50, -getStringWidth(label)/2);
			label.setLayoutData(fd_alarm);
		}
		{
			Label label = new Label(consoleComposite, SWT.CENTER);
			label.setText("（安全起见，OnceCloud不允许多个用户同时访问一台虚拟机的控制台）");
			label.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
			FormData fd_alarm = new FormData();
			fd_alarm.top = new FormAttachment(5, ALARM_PADDING);
			fd_alarm.left = new FormAttachment(50, -getStringWidth(label)/2);
			label.setLayoutData(fd_alarm);
		}
		composite = newComposite;
		setControl(composite);
		composite.layout();
		defaultReconnectBT.setEnabled(true);
		oldComposite.dispose();
	}
	
	public void consoleFresh(boolean state) {
		if (state) {
			composite = (Composite) this.getControl();
			consoleInitAction consoleAction = new consoleInitAction(PlatformUI
					.getWorkbench().getDisplay());
			consoleAction.start();
		} else {
			Composite oldComposite = (Composite) this.getControl();
			Composite newComposite = new Composite(folder, SWT.NONE);
			Composite consoleComposite = this.defaultInit(newComposite);
			
			{
				Label label = new Label(consoleComposite, SWT.CENTER);
			label.setText("该虚拟机处于关闭状态");
			label.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
			FormData fd_alarm = new FormData();
			fd_alarm.top = new FormAttachment(0, ALARM_PADDING);
			fd_alarm.left = new FormAttachment(50, -getStringWidth(label)/2);
			label.setLayoutData(fd_alarm);
			}
			{
				Label label = new Label(consoleComposite, SWT.CENTER);
				label.setText("如果您重启了虚拟机一段时间，但控制台没有显示，请点击右下角的重连按钮");
				label.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
				FormData fd_alarm = new FormData();
				fd_alarm.top = new FormAttachment(10, ALARM_PADDING);
				fd_alarm.left = new FormAttachment(50, -getStringWidth(label)/2);
				label.setLayoutData(fd_alarm);
			}
			
			composite = newComposite;
			setControl(composite);
			composite.layout();
			
			oldComposite.dispose();
			
			if(objectVM.localVncViewer!=null){
				objectVM.localVncViewer.disconnect();
				objectVM.localVncViewer=null;
			}
			
		}
	}
	
	public void tabPress(){
		KeyEvent tabEvent =
		          new KeyEvent(objectVM.localVncViewer, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_TAB);
		try {
			objectVM.localVncViewer.rfb.writeKeyEvent(tabEvent);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
}
