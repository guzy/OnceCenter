package vncviewer;

import java.awt.Button;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.TextArea;
import java.awt.TextComponent;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.EventObject;

public class ClipboardFrame extends Frame
  implements WindowListener, ActionListener
{
  TextArea textArea;
  Button clearButton;
  Button closeButton;
  String selection;
  VncViewer viewer;

  public ClipboardFrame(VncViewer paramVncViewer)
  {
    super("TightVNC Clipboard");

    this.viewer = paramVncViewer;

    GridBagLayout localGridBagLayout = new GridBagLayout();
    setLayout(localGridBagLayout);

    GridBagConstraints localGridBagConstraints = new GridBagConstraints();
    localGridBagConstraints.gridwidth = 0;
    localGridBagConstraints.fill = 1;
    localGridBagConstraints.weighty = 1.0D;

    this.textArea = new TextArea(5, 40);
    localGridBagLayout.setConstraints(this.textArea, localGridBagConstraints);
    add(this.textArea);

    localGridBagConstraints.fill = 2;
    localGridBagConstraints.weightx = 1.0D;
    localGridBagConstraints.weighty = 0.0D;
    localGridBagConstraints.gridwidth = 1;

    this.clearButton = new Button("Clear");
    localGridBagLayout.setConstraints(this.clearButton, localGridBagConstraints);
    add(this.clearButton);
    this.clearButton.addActionListener(this);

    this.closeButton = new Button("Close");
    localGridBagLayout.setConstraints(this.closeButton, localGridBagConstraints);
    add(this.closeButton);
    this.closeButton.addActionListener(this);

    pack();

    addWindowListener(this);
  }

  void setCutText(String paramString)
  {
    this.selection = paramString;
    this.textArea.setText(paramString);
    if (isVisible())
      this.textArea.selectAll();
  }

  public void windowDeactivated(WindowEvent paramWindowEvent)
  {
    if ((this.selection != null) && (!this.selection.equals(this.textArea.getText()))) {
      this.selection = this.textArea.getText();
      this.viewer.setCutText(this.selection);
    }
  }

  public void windowClosing(WindowEvent paramWindowEvent)
  {
    setVisible(false);
  }

  public void windowActivated(WindowEvent paramWindowEvent) {
  }

  public void windowOpened(WindowEvent paramWindowEvent) {
  }

  public void windowClosed(WindowEvent paramWindowEvent) {
  }

  public void windowIconified(WindowEvent paramWindowEvent) {
  }

  public void windowDeiconified(WindowEvent paramWindowEvent) {
  }

  public void actionPerformed(ActionEvent paramActionEvent) {
    if (paramActionEvent.getSource() == this.clearButton)
      this.textArea.setText("");
    else if (paramActionEvent.getSource() == this.closeButton)
      setVisible(false);
  }
}