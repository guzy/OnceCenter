package oncecenter.wizard.recoverlostvm;

import java.util.ArrayList;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObjectHost;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
 

public class CalendarDialog extends Dialog implements MouseListener{  
  
    private String selectedDate;// 选择的日期  
//    private Display display;  
//    private Shell dialog;  
    private DateTime calendar;  
  
    int year;
    int month;
    int day;
    
    public String getDateText() {  
        if(selectedDate == null){  
            return "";  
        }  
        return selectedDate.toString();  
    }  
    protected CalendarDialog(Shell parentShell,String date) {
		super(parentShell);
		String [] dateList = date.split("-");
		year = Integer.parseInt(dateList[0]);
		month = Integer.parseInt(dateList[1]);
		day = Integer.parseInt(dateList[2]);
	}
	
	@Override
	protected void configureShell(Shell shell){
		super.configureShell(shell);
		shell.setText("选择日期");
	}
	
	@Override 
	protected Point getInitialSize() { 
	return new Point(240,260); 
	} 
  
	protected Control createDialogArea(Composite parent) { 
		Composite composite = new Composite(parent, SWT.NONE); 
		GridLayout layout = new GridLayout(1, false); 
		 composite.setLayout(layout); 
		composite.setLayoutData(new GridData(GridData.FILL_BOTH)); 
		applyDialogFont(composite); 
		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);  
        //data.horizontalSpan = 3;  
        calendar = new DateTime(composite, SWT.CALENDAR | SWT.BORDER);  
        calendar.setLayoutData(data);  
        calendar.setYear(year);
        calendar.setMonth(month-1);
        calendar.setDay(day);
        //date = new DateTime(composite, SWT.DATE | SWT.SHORT);  
        //time = new DateTime(composite, SWT.TIME | SWT.SHORT);  
  
//        Button ok = new Button(composite, SWT.PUSH);  
//        ok.setText(" OK ");  
//        ok.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));  
//  
//        ok.addMouseListener(this);  
////        dialog.setDefaultButton(ok);  
////        dialog.pack();  
////        dialog.open();  
////          
////        while (!dialog.isDisposed()) {  
////            if (!display.readAndDispatch())  
////                display.sleep();  
////        }  
        
        return composite;
	}
//    public void open(int x, int y) {  
//        display = Display.getDefault();  
//        dialog = new Shell(getParentShell(),SWT.DIALOG_TRIM);  
//        dialog.setBounds(x, y, 230, 220);  
//          
//        dialog.setLayout(new GridLayout(3, false));  
//        
//    }  
  
  
    @Override  
    public void mouseDoubleClick(MouseEvent e) {  
          
    }  
  
  
  
    @Override  
    public void mouseDown(MouseEvent e) {  
          
//        int month = (calendar.getMonth() + 1);  
//        int day = calendar.getDay();  
//        selectedDate =  calendar.getYear() +"-" + (month<10?"0"+month:month)+   
//                "-" + (day<10?"0"+day:day) ;  
        //this.dialog.close();  
    }  
  
  
  
    @Override  
    public void mouseUp(MouseEvent e) {  
          
          
    }  
      
    @Override 
	protected Button createButton(Composite parent, int id, String label,boolean defaultButton) {
	 return null; 
	} 
	
	@Override 
	protected void initializeBounds() { 
	//我们可以利用原有的ID创建按钮,也可以用自定义的ID创建按钮 
	//但是调用的都是父类的createButton方法. 
	super.createButton((Composite)getButtonBar(), IDialogConstants.OK_ID, "确定", false);
	 super.createButton((Composite)getButtonBar(), IDialogConstants.CANCEL_ID, "取消", false);
	//下面这个方法一定要加上,并且要放在创建按钮之后,否则就不会创建按钮 
	super.initializeBounds(); 
	} 
	
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.OK_ID == buttonId) {
			int month = (calendar.getMonth() + 1);  
	        int day = calendar.getDay();  
	        selectedDate =  calendar.getYear() +"-" + (month<10?"0"+month:month)+   
	                "-" + (day<10?"0"+day:day) ;  
			super.okPressed();
		} else if (IDialogConstants.CANCEL_ID == buttonId) {
			super.cancelPressed();
		}
	}
}  
