package test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DateTimeDemo extends Shell
{
    private static Display display;

    private Text text;

    public static void main(String args[])
    {
        try
        {
            display = Display.getDefault();
            DateTimeDemo shell = new DateTimeDemo(display, SWT.SHELL_TRIM);
            shell.open();
            shell.layout();

            while(!shell.isDisposed())
            {
                if(!display.readAndDispatch())
                {
                    display.sleep();
                }
            }

            display.dispose();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public DateTimeDemo(Display display, int style)
    {
        super(display, style);
        createContents();
    }

    protected void createContents()
    {
        setText("DateTime");
        setSize(471, 140);
        text = new Text(this, SWT.BORDER);
        text.setEditable(false);
        text.setBackground(new Color(display, 255, 255, 255));
        text.setBounds(122, 41, 228, 25);

        text.addMouseListener(new MouseAdapter()
        {
            public void mouseUp(MouseEvent e)
            {
                DTDialog dialog = DTDialog.getInstance(DateTimeDemo.this);
                dialog.open();
            }
        });
    }

    public Point getDtLocation()
    {
        return new Point(text.getLocation().x + DateTimeDemo.this.getLocation().x,
                        text.getLocation().y + DateTimeDemo.this.getLocation().y + 60);
    }

    public void setDate(String str)
    {
        text.setText(str);
    }

    @Override
    protected void checkSubclass()
    {
    // Disable the check that prevents subclassing of SWT components
    }
}

