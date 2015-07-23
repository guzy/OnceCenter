package test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class DTDialog extends Dialog
{
    private Object result;

    private Shell shell;

    private DateTime dateTime;

    private DateTimeDemo parent;

    private static DTDialog instance;

    public static DTDialog getInstance(DateTimeDemo parent)
    {
        if(instance == null)
        {
            instance = new DTDialog(parent);
        }

        return instance;
    }

    private DTDialog(DateTimeDemo parent)
    {
        super(parent, SWT.NO_TRIM);
        this.parent = parent;
    }

    public Object open()
    {
        if(shell == null || shell.isDisposed())
        {
            createContents();
            shell.open();
            shell.layout();
            Display display = getParent().getDisplay();

            while(!shell.isDisposed())
            {
                if(!display.readAndDispatch())
                {
                    display.sleep();
                }
            }

            display.dispose();
        }
        else
        {
            shell.setLocation(parent.getDtLocation());
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
        shell.setLocation(parent.getDtLocation());
        shell.setText("SWT Dialog");
        dateTime = new DateTime(shell, SWT.CALENDAR);

        dateTime.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                parent.setDate(formatDt());
            }
        });

        dateTime.addFocusListener(new FocusAdapter()
        {
            public void focusLost(FocusEvent e)
            {
                parent.setDate(formatDt());
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
}

