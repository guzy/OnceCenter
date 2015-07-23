package oncecenter.maintabs.vm;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

import oncecenter.maintabs.OnceVMTabItem;
import oncecenter.util.ImageRegistry;
import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectVM;

public class PriorityTab extends OnceVMTabItem {

	Composite showComposite;
	private ProgressBar proBar;
	private Composite changeComposite;
	
	Button bt1;
	Button bt2;
	Button bt3;
	
	public PriorityTab(CTabFolder arg0, int arg1, VMTreeObjectVM object) {
		super(arg0, arg1, object);
		setText("优先级");
		this.objectVM = (VMTreeObjectVM)object;
	}

	public PriorityTab(CTabFolder arg0, int arg1, int arg2, VMTreeObjectVM object) {
		super(arg0, arg1, arg2, object);
		setText("优先级");
		this.objectVM = (VMTreeObjectVM)object;
	}
	@Override
	public boolean Init() {
		composite = new Composite(folder, SWT.FILL); 
		setControl(composite);
		composite.setBackground(new Color(null,255,255,255));
		composite.setBackgroundMode(SWT.INHERIT_DEFAULT);  
		composite.setLayout(new GridLayout(1,false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		showComposite = new Composite(composite,SWT.NONE);
		showComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		showComposite.setLayout(new GridLayout(3,false));
		
		CLabel info = new CLabel(showComposite,SWT.NONE);
		info.setText("当前虚拟机优先级");
		info.setImage(ImageRegistry.getImage(ImageRegistry.INFO));
		
		new Label(showComposite,SWT.NONE);
		new Label(showComposite,SWT.NONE);
		
		
		{
			proBar = new ProgressBar(showComposite, SWT.HORIZONTAL| SWT.SMOOTH);  
			proBar.setMinimum(0);  
			proBar.setMaximum(2);  
			GridData gd_canvas = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_canvas.horizontalSpan=3;
			gd_canvas.widthHint = 600;
			gd_canvas.heightHint = 10;
			proBar.setLayoutData(gd_canvas);
			
			
		}
		{
			CLabel l1 = new CLabel(showComposite,SWT.NONE);
			l1.setText("低");
			l1.setImage(ImageRegistry.getImage(ImageRegistry.LOWPRIORITY));
			GridData gd_canvas = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_canvas.widthHint = 300;
			l1.setLayoutData(gd_canvas);
		}
		{
			CLabel l1 = new CLabel(showComposite,SWT.NONE);
			l1.setText("中");
			l1.setImage(ImageRegistry.getImage(ImageRegistry.MIDDLEPRIORITY));
			GridData gd_canvas = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_canvas.widthHint = 300;
			l1.setLayoutData(gd_canvas);
		}
		{
			CLabel l1 = new CLabel(showComposite,SWT.NONE);
			l1.setText("高");
			l1.setImage(ImageRegistry.getImage(ImageRegistry.HIGHPRIORITY));
			GridData gd_canvas = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_canvas.widthHint = 300;
			l1.setLayoutData(gd_canvas);
		}
		
		Label l1 = new Label(composite,SWT.NONE);
		l1.setImage(ImageRegistry.getImage(ImageRegistry.BACKPRIORITY));
		
		changeComposite = new Composite(composite,SWT.NONE);
		changeComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		changeComposite.setLayout(new GridLayout(1,false));
		
		CLabel setting = new CLabel(changeComposite,SWT.NONE);
		setting.setText("设置虚拟机优先级");
		setting.setImage(ImageRegistry.getImage(ImageRegistry.SETTING));
		
		Group setGroup = new Group(composite, SWT.SHADOW_ETCHED_OUT);
		setGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		setGroup.setLayout(new GridLayout(1,false));

		bt1 = new Button(setGroup, SWT.RADIO);
		bt1.setText("低");
		bt1.setImage(ImageRegistry.getImage(ImageRegistry.LOWPRIORITY));
		bt2 = new Button(setGroup, SWT.RADIO);
		bt2.setText("中");
		bt2.setImage(ImageRegistry.getImage(ImageRegistry.MIDDLEPRIORITY));
		bt3 = new Button(setGroup, SWT.RADIO);
		bt3.setText("高");
		bt3.setImage(ImageRegistry.getImage(ImageRegistry.HIGHPRIORITY));
		
		Button finish = new Button(setGroup,SWT.NONE);
		finish.setText("确定");

		finish.addSelectionListener(new SelectionAdapter() {    
            public void widgetSelected(SelectionEvent e) {    
            	if(bt1.getSelection()){
            		objectVM.setPriority(0);
            	}else if(bt2.getSelection()){
            		objectVM.setPriority(1);
            	}else if(bt3.getSelection()){
            		objectVM.setPriority(2);
            	}
            	refresh();
             }    
         }); 
		
		refresh();
		
		composite.layout();
		return true;
	}
	
	public void refresh(){
		proBar.setSelection(objectVM.getPriority());
		switch(objectVM.getPriority()){
		case 0:
			bt1.setSelection(true);
			break;
		case 1:
			bt2.setSelection(true);
			break;
		case 2:
			bt3.setSelection(true);
			break;
		}
	}

}
