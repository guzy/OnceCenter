package vncviewer;

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextComponent;
import java.awt.TextField;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.PrintStream;
import java.util.EventObject;

public class RecordingFrame extends Frame
  implements WindowListener, ActionListener
{
  boolean recording;
  TextField fnameField;
  Button browseButton;
  Label statusLabel;
  Button recordButton;
  Button nextButton;
  Button closeButton;
  VncViewer viewer;

  public static boolean checkSecurity()
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null) {
      try {
        localSecurityManager.checkPropertyAccess("user.dir");
        localSecurityManager.checkPropertyAccess("file.separator");

        System.getProperty("user.dir");
      } catch (SecurityException localSecurityException) {
        System.out.println("SecurityManager restricts session recording.");
        return false;
      }
    }
    return true;
  }

  public RecordingFrame(VncViewer paramVncViewer)
  {
    super("TightVNC Session Recording");

    this.viewer = paramVncViewer;

    String str = nextNewFilename(System.getProperty("user.dir") + System.getProperty("file.separator") + "vncsession.fbs");

    Panel localPanel = new Panel();
    GridBagLayout localGridBagLayout1 = new GridBagLayout();
    localPanel.setLayout(localGridBagLayout1);

    GridBagConstraints localGridBagConstraints1 = new GridBagConstraints();
    localGridBagConstraints1.gridwidth = -1;
    localGridBagConstraints1.fill = 1;
    localGridBagConstraints1.weightx = 4.0D;

    this.fnameField = new TextField(str, 64);
    localGridBagLayout1.setConstraints(this.fnameField, localGridBagConstraints1);
    localPanel.add(this.fnameField);
    this.fnameField.addActionListener(this);

    localGridBagConstraints1.gridwidth = 0;
    localGridBagConstraints1.weightx = 1.0D;

    this.browseButton = new Button("Browse");
    localGridBagLayout1.setConstraints(this.browseButton, localGridBagConstraints1);
    localPanel.add(this.browseButton);
    this.browseButton.addActionListener(this);

    GridBagLayout localGridBagLayout2 = new GridBagLayout();
    setLayout(localGridBagLayout2);

    GridBagConstraints localGridBagConstraints2 = new GridBagConstraints();
    localGridBagConstraints2.gridwidth = 0;
    localGridBagConstraints2.fill = 1;
    localGridBagConstraints2.weighty = 1.0D;
    localGridBagConstraints2.insets = new Insets(10, 0, 0, 0);

    Label localLabel = new Label("File name to save next recorded session in:", 1);

    localGridBagLayout2.setConstraints(localLabel, localGridBagConstraints2);
    add(localLabel);

    localGridBagConstraints2.fill = 2;
    localGridBagConstraints2.weighty = 0.0D;
    localGridBagConstraints2.insets = new Insets(0, 0, 0, 0);

    localGridBagLayout2.setConstraints(localPanel, localGridBagConstraints2);
    add(localPanel);

    localGridBagConstraints2.fill = 1;
    localGridBagConstraints2.weighty = 1.0D;
    localGridBagConstraints2.insets = new Insets(10, 0, 10, 0);

    this.statusLabel = new Label("", 1);
    localGridBagLayout2.setConstraints(this.statusLabel, localGridBagConstraints2);
    add(this.statusLabel);

    localGridBagConstraints2.fill = 2;
    localGridBagConstraints2.weightx = 1.0D;
    localGridBagConstraints2.weighty = 0.0D;
    localGridBagConstraints2.gridwidth = 1;
    localGridBagConstraints2.insets = new Insets(0, 0, 0, 0);

    this.recordButton = new Button("Record");
    localGridBagLayout2.setConstraints(this.recordButton, localGridBagConstraints2);
    add(this.recordButton);
    this.recordButton.addActionListener(this);

    this.nextButton = new Button("Next file");
    localGridBagLayout2.setConstraints(this.nextButton, localGridBagConstraints2);
    add(this.nextButton);
    this.nextButton.addActionListener(this);

    this.closeButton = new Button("Close");
    localGridBagLayout2.setConstraints(this.closeButton, localGridBagConstraints2);
    add(this.closeButton);
    this.closeButton.addActionListener(this);

    stopRecording();

    pack();

    addWindowListener(this);
  }

  protected String nextFilename(String paramString)
  {
    int i = paramString.length();
    int j = i;
    int k = 1;

    if ((i > 4) && (paramString.charAt(i - 4) == '.'))
      try {
        k = Integer.parseInt(paramString.substring(i - 3, i)) + 1;
        j = i - 4;
      }
      catch (NumberFormatException localNumberFormatException) {
      }
    char[] arrayOfChar = { '0', '0', '0' };
    String str = String.valueOf(k);
    if (str.length() < 3) {
      str = new String(arrayOfChar, 0, 3 - str.length()) + str;
    }

    return paramString.substring(0, j) + '.' + str;
  }

  protected String nextNewFilename(String paramString)
  {
    String str = paramString;
    try {
      File localFile;
      do { str = nextFilename(str);
        localFile = new File(str); }
      while (localFile.exists());
    } catch (SecurityException localSecurityException) {
    }
    return str;
  }

  protected boolean browseFile()
  {
    File localFile = new File(this.fnameField.getText());

    FileDialog localFileDialog = new FileDialog(this, "Save next session as...", 1);

    localFileDialog.setDirectory(localFile.getParent());
    localFileDialog.setVisible(true);
    if (localFileDialog.getFile() != null) {
      String str1 = localFileDialog.getDirectory();
      String str2 = System.getProperty("file.separator");
      if ((str1.length() > 0) && 
        (!str2.equals(str1.substring(str1.length() - str2.length())))) {
        str1 = str1 + str2;
      }
      String str3 = str1 + localFileDialog.getFile();
      if (str3.equals(this.fnameField.getText())) {
        this.fnameField.setText(str3);
        return true;
      }
    }
    return false;
  }

  public void startRecording()
  {
    this.statusLabel.setText("Status: Recording...");
    this.statusLabel.setFont(new Font("Helvetica", 1, 12));
    this.statusLabel.setForeground(Color.red);
    this.recordButton.setLabel("Stop recording");

    this.recording = true;

    this.viewer.setRecordingStatus(this.fnameField.getText());
  }

  public void stopRecording()
  {
    this.statusLabel.setText("Status: Not recording.");
    this.statusLabel.setFont(new Font("Helvetica", 0, 12));
    this.statusLabel.setForeground(Color.black);
    this.recordButton.setLabel("Record");

    this.recording = false;

    this.viewer.setRecordingStatus(null);
  }

  public void windowClosing(WindowEvent paramWindowEvent)
  {
    setVisible(false);
  }
  public void windowActivated(WindowEvent paramWindowEvent) {
  }
  public void windowDeactivated(WindowEvent paramWindowEvent) {
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
    if (paramActionEvent.getSource() == this.browseButton) {
      if ((browseFile()) && (this.recording))
        startRecording();
    }
    else if (paramActionEvent.getSource() == this.recordButton) {
      if (!this.recording) {
        startRecording();
      } else {
        stopRecording();
        this.fnameField.setText(nextNewFilename(this.fnameField.getText()));
      }
    }
    else if (paramActionEvent.getSource() == this.nextButton) {
      this.fnameField.setText(nextNewFilename(this.fnameField.getText()));
      if (this.recording)
        startRecording();
    }
    else if (paramActionEvent.getSource() == this.closeButton) {
      setVisible(false);
    }
  }
}