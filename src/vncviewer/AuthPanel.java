package vncviewer;

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextComponent;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;

class AuthPanel extends Panel
  implements ActionListener
{
  TextField passwordField;
  Button okButton;

  public AuthPanel(VncViewer paramVncViewer)
  {
    Label localLabel1 = new Label("VNC Authentication", 1);
    localLabel1.setFont(new Font("Helvetica", 1, 18));

    Label localLabel2 = new Label("Password:", 1);

    this.passwordField = new TextField(10);
    this.passwordField.setForeground(Color.black);
    this.passwordField.setBackground(Color.white);
    this.passwordField.setEchoChar('*');

    this.okButton = new Button("OK");

    GridBagLayout localGridBagLayout = new GridBagLayout();
    GridBagConstraints localGridBagConstraints = new GridBagConstraints();

    setLayout(localGridBagLayout);

    localGridBagConstraints.gridwidth = 0;
    localGridBagConstraints.insets = new Insets(0, 0, 20, 0);
    localGridBagLayout.setConstraints(localLabel1, localGridBagConstraints);
    add(localLabel1);

    localGridBagConstraints.fill = 0;
    localGridBagConstraints.gridwidth = 1;
    localGridBagConstraints.insets = new Insets(0, 0, 0, 0);
    localGridBagLayout.setConstraints(localLabel2, localGridBagConstraints);
    add(localLabel2);

    localGridBagLayout.setConstraints(this.passwordField, localGridBagConstraints);
    add(this.passwordField);
    this.passwordField.addActionListener(this);

    localGridBagConstraints.gridwidth = 0;
    localGridBagConstraints.fill = 1;
    localGridBagConstraints.insets = new Insets(0, 20, 0, 0);
    localGridBagConstraints.ipadx = 30;
    localGridBagLayout.setConstraints(this.okButton, localGridBagConstraints);
    add(this.okButton);
    this.okButton.addActionListener(this);
  }

  public void moveFocusToDefaultField()
  {
    this.passwordField.requestFocus();
  }

  public synchronized void actionPerformed(ActionEvent paramActionEvent)
  {
    if ((paramActionEvent.getSource() == this.passwordField) || (paramActionEvent.getSource() == this.okButton)) {
      this.passwordField.setEnabled(false);
      notify();
    }
  }

  public synchronized String getPassword()
    throws Exception
  {
    try
    {
      wait(); } catch (InterruptedException localInterruptedException) {
    }
    return this.passwordField.getText();
  }
}