package vncviewer;

import java.awt.Button;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.EventObject;

public class OptionsFrame extends Frame
  implements WindowListener, ActionListener, ItemListener
{
  static String[] names = { "Encoding", "Compression level", "JPEG image quality", "Cursor shape updates", "Use CopyRect", "Restricted colors", "Mouse buttons 2 and 3", "View only", "Scale remote cursor", "Share desktop" };

  static String[][] values = { { "Auto", "Raw", "RRE", "CoRRE", "Hextile", "Zlib", "Tight", "ZRLE" }, { "Default", "1", "2", "3", "4", "5", "6", "7", "8", "9" }, { "JPEG off", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" }, { "Enable", "Ignore", "Disable" }, { "Yes", "No" }, { "Yes", "No" }, { "Normal", "Reversed" }, { "Yes", "No" }, { "No", "50%", "75%", "125%", "150%" }, { "Yes", "No" } };

  final int encodingIndex = 0; final int compressLevelIndex = 1; final int jpegQualityIndex = 2; final int cursorUpdatesIndex = 3; final int useCopyRectIndex = 4; final int eightBitColorsIndex = 5; final int mouseButtonIndex = 6; final int viewOnlyIndex = 7; final int scaleCursorIndex = 8; final int shareDesktopIndex = 9;

  Label[] labels = new Label[names.length];
  Choice[] choices = new Choice[names.length];
  Button closeButton;
  VncViewer viewer;
  int preferredEncoding;
  int compressLevel;
  int jpegQuality;
  boolean useCopyRect;
  boolean requestCursorUpdates;
  boolean ignoreCursorUpdates;
  boolean eightBitColors;
  boolean reverseMouseButtons2And3;
  boolean shareDesktop;
  boolean viewOnly;
  int scaleCursor;
  boolean autoScale;
  int scalingFactor;

  public OptionsFrame(VncViewer paramVncViewer)
  {
    super("TightVNC Options");

    this.viewer = paramVncViewer;

    GridBagLayout localGridBagLayout = new GridBagLayout();
    setLayout(localGridBagLayout);

    GridBagConstraints localGridBagConstraints = new GridBagConstraints();
    localGridBagConstraints.fill = 1;

    for (int i = 0; i < names.length; i++) {
      this.labels[i] = new Label(names[i]);
      localGridBagConstraints.gridwidth = 1;
      localGridBagLayout.setConstraints(this.labels[i], localGridBagConstraints);
      add(this.labels[i]);

      this.choices[i] = new Choice();
      localGridBagConstraints.gridwidth = 0;
      localGridBagLayout.setConstraints(this.choices[i], localGridBagConstraints);
      add(this.choices[i]);
      this.choices[i].addItemListener(this);

      for (int j = 0; j < values[i].length; j++) {
        this.choices[i].addItem(values[i][j]);
      }
    }

    this.closeButton = new Button("Close");
    localGridBagConstraints.gridwidth = 0;
    localGridBagLayout.setConstraints(this.closeButton, localGridBagConstraints);
    add(this.closeButton);
    this.closeButton.addActionListener(this);

    pack();

    addWindowListener(this);

    this.choices[0].select("Auto");
    this.choices[1].select("Default");
    this.choices[2].select("6");
    this.choices[3].select("Enable");
    this.choices[4].select("Yes");
    this.choices[5].select("No");
    this.choices[6].select("Normal");
    this.choices[7].select("No");
    this.choices[8].select("No");
    this.choices[9].select("Yes");

    int i;
    for (i = 0; i < names.length; i++) {
      String str2 = this.viewer.readParameter(names[i], false);
      if (str2 != null) {
        for (int k = 0; k < values[i].length; k++) {
          if (str2.equalsIgnoreCase(values[i][k])) {
            this.choices[i].select(k);
          }
        }

      }

    }

    this.autoScale = false;
    this.scalingFactor = 100;
    String str1 = this.viewer.readParameter("Scaling Factor", false);
    if (str1 != null) {
      if (str1.equalsIgnoreCase("Auto")) {
        this.autoScale = true;
      }
      else {
        if (str1.charAt(str1.length() - 1) == '%') {
          str1 = str1.substring(0, str1.length() - 1);
        }
        try
        {
          this.scalingFactor = Integer.parseInt(str1);
        }
        catch (NumberFormatException localNumberFormatException) {
          this.scalingFactor = 100;
        }

        if (this.scalingFactor < 1)
          this.scalingFactor = 1;
        else if (this.scalingFactor > 1000) {
          this.scalingFactor = 1000;
        }

      }

    }

    setEncodings();
    setColorFormat();
    setOtherOptions();
  }

  void disableShareDesktop()
  {
    this.labels[9].setEnabled(false);
    this.choices[9].setEnabled(false);
  }

  void setEncodings()
  {
    this.useCopyRect = this.choices[4].getSelectedItem().equals("Yes");

    this.preferredEncoding = 0;
    boolean bool = false;

    if (this.choices[0].getSelectedItem().equals("RRE")) {
      this.preferredEncoding = 2;
    } else if (this.choices[0].getSelectedItem().equals("CoRRE")) {
      this.preferredEncoding = 4;
    } else if (this.choices[0].getSelectedItem().equals("Hextile")) {
      this.preferredEncoding = 5;
    } else if (this.choices[0].getSelectedItem().equals("ZRLE")) {
      this.preferredEncoding = 16;
    } else if (this.choices[0].getSelectedItem().equals("Zlib")) {
      this.preferredEncoding = 6;
      bool = true;
    } else if (this.choices[0].getSelectedItem().equals("Tight")) {
      this.preferredEncoding = 7;
      bool = true;
    } else if (this.choices[0].getSelectedItem().equals("Auto")) {
      this.preferredEncoding = -1;
    }

    try
    {
      this.compressLevel = Integer.parseInt(this.choices[1].getSelectedItem());
    }
    catch (NumberFormatException localNumberFormatException1)
    {
      this.compressLevel = -1;
    }
    if ((this.compressLevel < 1) || (this.compressLevel > 9)) {
      this.compressLevel = -1;
    }
    this.labels[1].setEnabled(bool);
    this.choices[1].setEnabled(bool);
    try
    {
      this.jpegQuality = Integer.parseInt(this.choices[2].getSelectedItem());
    }
    catch (NumberFormatException localNumberFormatException2)
    {
      this.jpegQuality = -1;
    }
    if ((this.jpegQuality < 0) || (this.jpegQuality > 9)) {
      this.jpegQuality = -1;
    }

    this.requestCursorUpdates = (!this.choices[3].getSelectedItem().equals("Disable"));

    if (this.requestCursorUpdates) {
      this.ignoreCursorUpdates = this.choices[3].getSelectedItem().equals("Ignore");
    }

    this.viewer.setEncodings();
  }

  void setColorFormat()
  {
    this.eightBitColors = this.choices[5].getSelectedItem().equals("Yes");

    boolean bool = !this.eightBitColors;

    this.labels[2].setEnabled(bool);
    this.choices[2].setEnabled(bool);
  }

  void setOtherOptions()
  {
    this.reverseMouseButtons2And3 = this.choices[6].getSelectedItem().equals("Reversed");

    this.viewOnly = this.choices[7].getSelectedItem().equals("Yes");

    if (this.viewer.vc != null) {
      this.viewer.vc.enableInput(!this.viewOnly);
    }
    this.shareDesktop = this.choices[9].getSelectedItem().equals("Yes");

    String str = this.choices[8].getSelectedItem();
    if (str.endsWith("%"))
      str = str.substring(0, str.length() - 1);
    try {
      this.scaleCursor = Integer.parseInt(str);
    }
    catch (NumberFormatException localNumberFormatException) {
      this.scaleCursor = 0;
    }
    if ((this.scaleCursor < 10) || (this.scaleCursor > 500)) {
      this.scaleCursor = 0;
    }
    if ((this.requestCursorUpdates) && (!this.ignoreCursorUpdates) && (!this.viewOnly)) {
      this.labels[8].setEnabled(true);
      this.choices[8].setEnabled(true);
    } else {
      this.labels[8].setEnabled(false);
      this.choices[8].setEnabled(false);
    }
    if (this.viewer.vc != null)
      this.viewer.vc.createSoftCursor();
  }

  public void itemStateChanged(ItemEvent paramItemEvent)
  {
    Object localObject = paramItemEvent.getSource();

    if ((localObject == this.choices[0]) || (localObject == this.choices[1]) || (localObject == this.choices[2]) || (localObject == this.choices[3]) || (localObject == this.choices[4]))
    {
      setEncodings();

      if (localObject == this.choices[3]) {
        setOtherOptions();
      }
    }
    else if (localObject == this.choices[5])
    {
      setColorFormat();
    }
    else if ((localObject == this.choices[6]) || (localObject == this.choices[9]) || (localObject == this.choices[7]) || (localObject == this.choices[8]))
    {
      setOtherOptions();
    }
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    if (paramActionEvent.getSource() == this.closeButton)
      setVisible(false);
  }

  public void windowClosing(WindowEvent paramWindowEvent)
  {
    setVisible(false);
  }

  public void windowActivated(WindowEvent paramWindowEvent)
  {
  }

  public void windowDeactivated(WindowEvent paramWindowEvent)
  {
  }

  public void windowOpened(WindowEvent paramWindowEvent)
  {
  }

  public void windowClosed(WindowEvent paramWindowEvent)
  {
  }

  public void windowIconified(WindowEvent paramWindowEvent)
  {
  }

  public void windowDeiconified(WindowEvent paramWindowEvent)
  {
  }
}