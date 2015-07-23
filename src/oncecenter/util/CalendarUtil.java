package oncecenter.util;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;




public class CalendarUtil extends Dialog{

	private Object result;
    private Shell shell;
    private DateTime dateTime;
    private Point location;
    private Text text;
    private static CalendarUtil instance;
    
    public static CalendarUtil getInstance(Shell shell,Point point)
    {
        if(instance == null)
        {
            instance = new CalendarUtil(shell,point);
        }

        return instance;
    }

    public CalendarUtil(Shell shell,Point point)
    {
        super(shell, SWT.NO_TRIM);
        this.location = point;
    }

    public Object open()
    {
        if(shell == null || shell.isDisposed())
        {
            createContents();
            shell.open();
            shell.layout();
//            Display display = getParent().getDisplay();
//
//            while(!shell.isDisposed())
//            {
//                if(!display.readAndDispatch())
//                {
//                    display.sleep();
//                }
//            }
//
//            display.dispose();
        }
        else
        {
            shell.setLocation(location);
            shell.setVisible(true);
            shell.setFocus();
        }

        return result;
    }

	
	   protected void createContents()
	    {
	        shell = new Shell(getParent(), SWT.NO_TRIM);
	        shell.setLayout(new FillLayout());
	        shell.setSize(272, 140);
	        shell.setLocation(location);
	        shell.setText("SWT Dialog");
	        dateTime = new DateTime(shell, SWT.CALENDAR);

	        dateTime.addSelectionListener(new SelectionAdapter()
	        {
	            public void widgetSelected(SelectionEvent e)
	            {
	            	 text.setText(formatDt());
	            }
	        });

	        dateTime.addFocusListener(new FocusAdapter()
	        {
	            public void focusLost(FocusEvent e)
	            {
	                text.setText(formatDt());
	                shell.setVisible(false);
	            }
	        });
	    }
	   
	   private String formatDt()
	    {
	        return dateTime.getYear() + "-" + dateTime.getMonth() + "-" + dateTime.getDay();
	    }

	    public Shell getShell()
	    {
	        return shell;
	    }
	    
	    public void setText(Text text)
	    {
	    	this.text = text;
	    }

}
