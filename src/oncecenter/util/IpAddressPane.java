package oncecenter.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

public class IpAddressPane extends Composite implements VerifyListener,KeyListener{

	Composite parent;
	 private Text text_1;
	 private Text text_2;
	 private Text text_3;
	 private Text text_4;
	 private boolean readOnly = false;
	 
	public IpAddressPane(Composite parent, int style) {
		super(parent, style);
		
		this.parent = parent;
		this.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		CreateContent();
	}
	protected void CreateContent()
	{
		final GridLayout gridLayout = new GridLayout(7, false);
        gridLayout.marginRight = 2;
        gridLayout.marginTop = 1;
        gridLayout.marginBottom = 1;
        gridLayout.marginLeft = 3;
        gridLayout.verticalSpacing = 0;
        gridLayout.numColumns = 7;
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = 0;
        setLayout(gridLayout);
        text_1 = internalGetText() ;
        text_1.setTextLimit(3);
        final GridData gridData = new GridData(32, SWT.DEFAULT);
        text_1.setLayoutData(gridData);
        final Label lable_1 = new Label(this, SWT.SHADOW_IN);
        lable_1.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        lable_1.setText(".");
        text_2 = internalGetText() ;
        text_2.setTextLimit(3);
        text_2.setLayoutData(new GridData(32, SWT.DEFAULT));
        final Label lable_2 = new Label(this, SWT.SHADOW_IN);
        lable_2.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        lable_2.setText(".");
        text_3 = internalGetText() ;
        text_3.setTextLimit(3);
        text_3.setLayoutData(new GridData(32, SWT.DEFAULT));
        final Label lable_3 = new Label(this, SWT.SHADOW_IN);
        lable_3.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        lable_3.setText(".");
        text_4 = internalGetText() ;
        text_4.setTextLimit(3);
        text_4.setLayoutData(new GridData(32, SWT.DEFAULT));
        text_1.setFocus() ;
	}
	@Override
	public void keyPressed(KeyEvent e) {
		
		Text text = (Text) e.getSource() ;
        int position = text.getCaretPosition() ;
        if (e.stateMask != SWT.SHIFT) {
            switch (e.keyCode) {
            case SWT.ARROW_UP:
            case SWT.ARROW_LEFT:
                if (text.getText() == "")
                    previousFocus(text);
                if (text != text_1 && position == 0) {
                    previousFocus(text);
                }
                break;
            case SWT.ARROW_DOWN:
            case SWT.ARROW_RIGHT:
                if (text.getText() == "")
                    nextFocus(text);
                if (text != text_4 && (text.getText().length() == position)) {
                    nextFocus(text);
                }
                break;
            case SWT.BS:
                if (text != text_1) {
                    if (text.getText() == "")
                        previousFocus(text);
                    if(text.getCaretPosition() == 0) {
                        if(text.getSelectionText() == "")
                            previousFocus(text) ;
                    }
                }
                break;
            }
        }
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		
		
	}

	@Override
	public void verifyText(VerifyEvent e) {
		
		 Text text = (Text) e.getSource() ;
	        String text_string = text.getText() ;
	        char c = e.character ;
	        if(text_string.length() >= text.getTextLimit())
	            return ;
	        if("0123456789".indexOf(e.text) < 0) {
	            e.doit = false ;
	            return ;
	        }
	        if(e.keyCode == SWT.BS) {
	            e.doit = true ;
	            return ;
	        } else if(e.keyCode == SWT.DEL) {
	            e.doit = true ;
	            return ;
	        }
	        
	        text_string = text_string.substring(0,text.getSelection().x) ;
	        int position = text.getCaretPosition() ;
	        if(text == text_1) {
	            StringBuilder buffer = new StringBuilder(text_string) ;
	            text_string = buffer.insert(position, e.text).toString() ;
	            if(Integer.parseInt(text_string) > 233) {
	                errorMessageBox(text, text_string, "233") ;
	                return ;
	            } 
	        } else {
	            StringBuilder buffer = new StringBuilder(text_string)  ;
	            text_string = buffer.insert(position, e.text).toString() ;
	            if(Integer.parseInt(text_string) > 255) { 
	                errorMessageBox(text, text_string, "255") ;
	                return ;
	            } 
	        }
	        if(text_string.length() >= text.getTextLimit() && c != SWT.BS)
	            nextFocus(text) ;
	}

	private void nextFocus(Text text) {
        if (text == text_1) {
            text_2.setFocus();
        } else if (text == text_2) {
            text_3.setFocus();
        } else if (text == text_3) {
            text_4.setFocus();
        }
    }
    
    private void previousFocus(Text text) {
        if(text== text_2) {
            text_1.setFocus() ;
        } else if(text==text_3) {
            text_2.setFocus() ;
        } else if(text==text_4) {
            text_3.setFocus() ;
        }
    }

    public void setText(String text) throws Exception {
        if (!isValid(text))
            throw new Exception("指定的IP地址无效");
        String[] ip = text.split("\\.");
        text_1.removeVerifyListener(this);
        text_1.removeKeyListener(this) ;
        text_2.removeVerifyListener(this);
        text_2.removeKeyListener(this) ;
        text_3.removeVerifyListener(this);
        text_3.removeKeyListener(this) ;
        text_4.removeVerifyListener(this);
        text_4.removeKeyListener(this) ;
        text_1.setText(ip[0]);
        text_2.setText(ip[1]);
        text_3.setText(ip[2]);
        text_4.setText(ip[3]);
        text_1.addVerifyListener(this);
        text_1.addKeyListener(this) ;
        text_2.addVerifyListener(this);
        text_2.addKeyListener(this) ;
        text_3.addVerifyListener(this);
        text_3.addKeyListener(this) ;
        text_4.addVerifyListener(this);
        text_4.addKeyListener(this) ;
    }
    
    protected void setText(Text text, String text_string) {
        text.removeVerifyListener(this) ;
        text.setText(text_string) ;
        text.addVerifyListener(this) ;
        text.setFocus() ;
    }
    
    private Text internalGetText() {
        Text text ;
        text = new Text(this,SWT.CENTER) ;
        text.addVerifyListener(this);
        text.addKeyListener(this) ;
        return text ;
    }
    
    private void errorMessageBox(Text text,String error_string, String right_string) {
        MessageBox mbox = new MessageBox(getParent().getShell(),SWT.APPLICATION_MODAL) ;
        mbox.setText("错误") ;
        mbox.setMessage(error_string + "不是一个有效项目。请指定一个介于1和"+right_string+"之间的数值") ;
        int r = mbox.open() ;
        if(r == SWT.OK) {
            setText(text, right_string) ;
            return ;
        }
    }

    public String getText() {
        return text_1.getText() + "." + text_2.getText() + "."
                + text_3.getText() + "." + text_4.getText();
    }

    /*
     * @see org.eclipse.swt.widgets.Widget#toString()
     */
    public String toString() {
        return "Ip:" + getText();
    }

    private boolean isValid(String source) {
        String reg = "^(22[0-3]|2[0-1]\\d|1\\d{2}|[1-9]\\d|[1-9])"
                + "\\.(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)"
                + "\\.(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)"
                + "\\.(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)$";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(source);
        return m.find();
    }
}
