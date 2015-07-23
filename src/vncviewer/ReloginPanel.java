package vncviewer;

import java.applet.Applet;
import java.applet.AppletContext;
import java.awt.Button;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;

class ReloginPanel extends Panel
  implements ActionListener
{
  Button reloginButton;
  Button closeButton;
  VncViewer viewer;

  public ReloginPanel(VncViewer paramVncViewer)
  {
    this.viewer = paramVncViewer;
    setLayout(new FlowLayout(1));
    this.reloginButton = new Button("Login again");
    add(this.reloginButton);
    this.reloginButton.addActionListener(this);
    if (this.viewer.inSeparateFrame) {
      this.closeButton = new Button("Close window");
      add(this.closeButton);
      this.closeButton.addActionListener(this);
    }
  }

  public synchronized void actionPerformed(ActionEvent paramActionEvent)
  {
    if (this.viewer.inSeparateFrame)
      this.viewer.vncFrame.dispose();
    if (paramActionEvent.getSource() == this.reloginButton)
      this.viewer.getAppletContext().showDocument(this.viewer.getDocumentBase());
  }
}