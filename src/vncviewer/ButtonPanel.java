package vncviewer;

import java.awt.Button;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.EventObject;

class ButtonPanel extends Panel
  implements ActionListener
{
  VncViewer viewer;
  Button disconnectButton;
  Button optionsButton;
  Button recordButton;
  Button clipboardButton;
  Button ctrlAltDelButton;
  Button refreshButton;

  ButtonPanel(VncViewer paramVncViewer)
  {
    this.viewer = paramVncViewer;

    setLayout(new FlowLayout(0, 0, 0));
    this.disconnectButton = new Button("Disconnect");
    this.disconnectButton.setEnabled(false);
    add(this.disconnectButton);
    this.disconnectButton.addActionListener(this);
    this.optionsButton = new Button("Options");
    add(this.optionsButton);
    this.optionsButton.addActionListener(this);
    this.clipboardButton = new Button("Clipboard");
    this.clipboardButton.setEnabled(false);
    add(this.clipboardButton);
    this.clipboardButton.addActionListener(this);
    if (this.viewer.rec != null) {
      this.recordButton = new Button("Record");
      add(this.recordButton);
      this.recordButton.addActionListener(this);
    }
    this.ctrlAltDelButton = new Button("Send Ctrl-Alt-Del");
    this.ctrlAltDelButton.setEnabled(false);
    add(this.ctrlAltDelButton);
    this.ctrlAltDelButton.addActionListener(this);
    this.refreshButton = new Button("Refresh");
    this.refreshButton.setEnabled(false);
    add(this.refreshButton);
    this.refreshButton.addActionListener(this);
  }

  public void enableButtons()
  {
    this.disconnectButton.setEnabled(true);
    this.clipboardButton.setEnabled(true);
    this.refreshButton.setEnabled(true);
  }

  public void disableButtonsOnDisconnect()
  {
    remove(this.disconnectButton);
    this.disconnectButton = new Button("Hide desktop");
    this.disconnectButton.setEnabled(true);
    add(this.disconnectButton, 0);
    this.disconnectButton.addActionListener(this);

    this.optionsButton.setEnabled(false);
    this.clipboardButton.setEnabled(false);
    this.ctrlAltDelButton.setEnabled(false);
    this.refreshButton.setEnabled(false);

    validate();
  }

  public void enableRemoteAccessControls(boolean paramBoolean)
  {
    this.ctrlAltDelButton.setEnabled(paramBoolean);
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    this.viewer.moveFocusToDesktop();

    if (paramActionEvent.getSource() == this.disconnectButton) {
      this.viewer.disconnect();
    }
    else if (paramActionEvent.getSource() == this.optionsButton) {
      this.viewer.options.setVisible(!this.viewer.options.isVisible());
    }
    else if (paramActionEvent.getSource() == this.recordButton) {
      this.viewer.rec.setVisible(!this.viewer.rec.isVisible());
    }
    else if (paramActionEvent.getSource() == this.clipboardButton) {
      this.viewer.clipboard.setVisible(!this.viewer.clipboard.isVisible());
    }
    else if (paramActionEvent.getSource() == this.ctrlAltDelButton)
    {
      try
      {
        KeyEvent localKeyEvent = new KeyEvent(this, 401, 0L, 10, 127);

        this.viewer.rfb.writeKeyEvent(localKeyEvent);

        localKeyEvent = new KeyEvent(this, 402, 0L, 10, 127);

        this.viewer.rfb.writeKeyEvent(localKeyEvent);
      }
      catch (IOException localIOException1) {
        localIOException1.printStackTrace();
      }
    }
    else if (paramActionEvent.getSource() == this.refreshButton)
      try {
        RfbProto localRfbProto = this.viewer.rfb;
        localRfbProto.writeFramebufferUpdateRequest(0, 0, localRfbProto.framebufferWidth, localRfbProto.framebufferHeight, false);
      }
      catch (IOException localIOException2) {
        localIOException2.printStackTrace();
      }
  }
}