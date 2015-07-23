package oncecenter.maintabs.host;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.once.xenapi.Host;

import oncecenter.action.host.CheckCompleteLogAction;
import oncecenter.action.host.CheckErrorLogAction;
import oncecenter.maintabs.OnceTabItem;
import oncecenter.util.FileUtil;
import oncecenter.util.ImageRegistry;
import oncecenter.util.Ssh;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;

public class HostLogTab extends OnceTabItem {
	private Text logText;
	private CCombo combo;
	private Text text;
	private Host.Record record;
	private VMTreeObjectHost hostObject;
	public HostLogTab(CTabFolder arg0, int arg1, int arg2, VMTreeObject object) {
		super(arg0, arg1, arg2, object);
		
		setText("日志信息");
		hostObject = (VMTreeObjectHost)object;
		record = hostObject.getRecord();
	}
	@Override
	public boolean Init() {
		
		composite = new Composite(folder, SWT.NONE); 
		this.setControl(composite);
		composite.setLayout(new GridLayout(2,false));
		composite.setBackground(new Color(null,255,255,255));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setBackgroundMode(SWT.INHERIT_DEFAULT);

//		Composite chooseCom = new Composite(composite,SWT.NONE);
//		chooseCom.setLayout(new GridLayout(2,false));
//		chooseCom.setBackground(new Color(null,255,255,255));
//		chooseCom.setLayoutData(new GridData(GridData.FILL_BOTH));
		CLabel label = new CLabel(composite,SWT.NONE);
		label.setText("日志类型");
		label.setImage(ImageRegistry.getImage(ImageRegistry.LOG));
		
		final Combo combo = new Combo(composite,SWT.DROP_DOWN|SWT.READ_ONLY);
		String items[] = {"查看全部日志","查看错误日志"};
		combo.setItems(items);
		combo.select(0);
		
		logText = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.WRAP 
				| SWT.READ_ONLY | SWT.V_SCROLL); 
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true); 
		gridData.horizontalSpan = 2;
		logText.setLayoutData(gridData); 
		CheckCompleteLogAction fullAction = new CheckCompleteLogAction(hostObject,logText,composite);
		fullAction.run();
		
		combo.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				
				if(combo.getSelectionIndex() == 1)
				{
					CheckErrorLogAction errorAction = new CheckErrorLogAction(hostObject,logText,composite);
					errorAction.run();
				}
				else
				{
					CheckCompleteLogAction fullAction = new CheckCompleteLogAction(hostObject,logText,composite);
					fullAction.run();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
				widgetSelected(e);
			}
		});
		composite.layout();	
		
		return true;
	}
}
