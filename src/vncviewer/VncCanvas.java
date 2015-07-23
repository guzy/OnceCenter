package vncviewer;

import java.awt.AWTEvent;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.MemoryImageSource;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.zip.Inflater;

class VncCanvas extends Canvas
  implements KeyListener, MouseListener, MouseMotionListener
{
  VncViewer viewer;
  RfbProto rfb;
  ColorModel cm8;
  ColorModel cm24;
  Color[] colors;
  int bytesPixel;
  int maxWidth = 0; int maxHeight = 0;
  int scalingFactor;
  int scaledWidth;
  int scaledHeight;
  Image memImage;
  Graphics memGraphics;
  Image rawPixelsImage;
  MemoryImageSource pixelsSource;
  byte[] pixels8;
  int[] pixels24;
  long statStartTime;
  int statNumUpdates;
  int statNumTotalRects;
  int statNumPixelRects;
  int statNumRectsTight;
  int statNumRectsTightJPEG;
  int statNumRectsZRLE;
  int statNumRectsHextile;
  int statNumRectsRaw;
  int statNumRectsCopy;
  int statNumBytesEncoded;
  int statNumBytesDecoded;
  byte[] zrleBuf;
  int zrleBufLen = 0;
  byte[] zrleTilePixels8;
  int[] zrleTilePixels24;
  ZlibInStream zrleInStream;
  boolean zrleRecWarningShown = false;
  byte[] zlibBuf;
  int zlibBufLen = 0;
  Inflater zlibInflater;
  static final int tightZlibBufferSize = 512;
  Inflater[] tightInflaters;
  Rectangle jpegRect;
  boolean inputEnabled;
  private Color hextile_bg;
  private Color hextile_fg;
  boolean showSoftCursor = false;
  MemoryImageSource softCursorSource;
  Image softCursor;
  int cursorX = 0; int cursorY = 0;
  int cursorWidth;
  int cursorHeight;
  int origCursorWidth;
  int origCursorHeight;
  int hotX;
  int hotY;
  int origHotX;
  int origHotY;

  public VncCanvas(VncViewer paramVncViewer, int paramInt1, int paramInt2)
    throws IOException
  {
    this.viewer = paramVncViewer;
    this.maxWidth = paramInt1;
    this.maxHeight = paramInt2;

    this.rfb = this.viewer.rfb;
    this.scalingFactor = this.viewer.options.scalingFactor;

    this.tightInflaters = new Inflater[4];

    this.cm8 = new DirectColorModel(8, 7, 56, 192);
    this.cm24 = new DirectColorModel(24, 16711680, 65280, 255);

    this.colors = new Color[256];
    for (int i = 0; i < 256; i++) {
      this.colors[i] = new Color(this.cm8.getRGB(i));
    }
    setPixelFormat();

    this.inputEnabled = false;
    if (!this.viewer.options.viewOnly) {
      enableInput(true);
    }

    addKeyListener(this);
  }

  public VncCanvas(VncViewer paramVncViewer) throws IOException {
    this(paramVncViewer, 0, 0);
  }

  public Dimension getPreferredSize()
  {
    return new Dimension(this.scaledWidth, this.scaledHeight);
  }

  public Dimension getMinimumSize() {
    return new Dimension(this.scaledWidth, this.scaledHeight);
  }

  public Dimension getMaximumSize() {
    return new Dimension(this.scaledWidth, this.scaledHeight);
  }

  public void update(Graphics paramGraphics)
  {
    paint(paramGraphics);
  }

  public void paint(Graphics paramGraphics) {
    synchronized (this.memImage) {
      if (this.rfb.framebufferWidth == this.scaledWidth)
        paramGraphics.drawImage(this.memImage, 0, 0, null);
      else {
        paintScaledFrameBuffer(paramGraphics);
      }
    }
    if (this.showSoftCursor) {
      int i = this.cursorX - this.hotX; int j = this.cursorY - this.hotY;
      Rectangle localRectangle = new Rectangle(i, j, this.cursorWidth, this.cursorHeight);
      if (localRectangle.intersects(paramGraphics.getClipBounds()))
        paramGraphics.drawImage(this.softCursor, i, j, null);
    }
  }

  public void paintScaledFrameBuffer(Graphics paramGraphics)
  {
    paramGraphics.drawImage(this.memImage, 0, 0, this.scaledWidth, this.scaledHeight, null);
  }

  public boolean imageUpdate(Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
  {
    if ((paramInt1 & 0xA0) == 0) {
      return true;
    }

    if (((paramInt1 & 0x20) != 0) && 
      (this.jpegRect != null)) {
      synchronized (this.jpegRect) {
        this.memGraphics.drawImage(paramImage, this.jpegRect.x, this.jpegRect.y, null);
        scheduleRepaint(this.jpegRect.x, this.jpegRect.y, this.jpegRect.width, this.jpegRect.height);

        this.jpegRect.notify();
      }
    }

    return false;
  }

  public synchronized void enableInput(boolean paramBoolean)
  {
    if ((paramBoolean) && (!this.inputEnabled)) {
      this.inputEnabled = true;
      addMouseListener(this);
      addMouseMotionListener(this);
      if (this.viewer.showControls) {
        this.viewer.buttonPanel.enableRemoteAccessControls(true);
      }
      createSoftCursor();
    } else if ((!paramBoolean) && (this.inputEnabled)) {
      this.inputEnabled = false;
      removeMouseListener(this);
      removeMouseMotionListener(this);
      if (this.viewer.showControls) {
        this.viewer.buttonPanel.enableRemoteAccessControls(false);
      }
      createSoftCursor();
    }
  }

  public void setPixelFormat() throws IOException {
    if (this.viewer.options.eightBitColors) {
      this.rfb.writeSetPixelFormat(8, 8, false, true, 7, 7, 3, 0, 3, 6);
      this.bytesPixel = 1;
    } else {
      this.rfb.writeSetPixelFormat(32, 24, false, true, 255, 255, 255, 16, 8, 0);
      this.bytesPixel = 4;
    }
    updateFramebufferSize();
  }

  void updateFramebufferSize()
  {
    int i = this.rfb.framebufferWidth;
    int j = this.rfb.framebufferHeight;

    if ((this.maxWidth > 0) && (this.maxHeight > 0)) {
      int k = this.maxWidth * 100 / i;
      int m = this.maxHeight * 100 / j;
      this.scalingFactor = Math.min(k, m);
      if (this.scalingFactor > 100)
        this.scalingFactor = 100;
      System.out.println("Scaling desktop at " + this.scalingFactor + "%");
    }

    this.scaledWidth = ((i * this.scalingFactor + 50) / 100);
    this.scaledHeight = ((j * this.scalingFactor + 50) / 100);

    if (this.memImage == null) {
      this.memImage = this.viewer.vncContainer.createImage(i, j);
      this.memGraphics = this.memImage.getGraphics();
    } else if ((this.memImage.getWidth(null) != i) || (this.memImage.getHeight(null) != j)) {
    	synchronized (this.memImage) {
        this.memImage = this.viewer.vncContainer.createImage(i, j);
        this.memGraphics = this.memImage.getGraphics();
    }

    }

    if (this.bytesPixel == 1) {
      this.pixels24 = null;
      this.pixels8 = new byte[i * j];

      this.pixelsSource = new MemoryImageSource(i, j, this.cm8, this.pixels8, 0, i);

      this.zrleTilePixels24 = null;
      this.zrleTilePixels8 = new byte[4096];
    }
    else {
    	this.pixels8 = null;
    	this.pixels24 = new int[i * j];
    	this.pixelsSource = new MemoryImageSource(i, j, this.cm24, this.pixels24, 0, i);
    	this.zrleTilePixels8 = null;
    	this.zrleTilePixels24 = new int[4096];
    }
    this.pixelsSource.setAnimated(true);
    this.rawPixelsImage = Toolkit.getDefaultToolkit().createImage(this.pixelsSource);
    if (this.viewer.inSeparateFrame) {
    	if (this.viewer.desktopScrollPane != null){
    		resizeDesktopFrame();
    	}
    } else {
    	setSize(this.scaledWidth, this.scaledHeight);
    }
    this.viewer.moveFocusToDesktop();
  }

  void resizeDesktopFrame() {
//	  this.scaledWidth=800;
//	  this.scaledHeight=600;
    setSize(this.scaledWidth, this.scaledHeight);

    Insets localInsets = this.viewer.desktopScrollPane.getInsets();
    this.viewer.desktopScrollPane.setSize(this.scaledWidth + 2 * Math.min(localInsets.left, localInsets.right), this.scaledHeight + 2 * Math.min(localInsets.top, localInsets.bottom));

    this.viewer.vncFrame.pack();

    Dimension localDimension1 = this.viewer.vncFrame.getToolkit().getScreenSize();
    Dimension localDimension2 = this.viewer.vncFrame.getSize();
    Dimension localDimension3 = localDimension2;
    System.out.println("localDimension1.height = " + localDimension1.height + ",localDimension1.width = " + localDimension1.width);
    localDimension1.height -= 30;
    localDimension1.width -= 30;

    int i = 0;
    if (localDimension2.height > localDimension1.height) {
      localDimension3.height = localDimension1.height;
      i = 1;
    }
    if (localDimension2.width > localDimension1.width) {
      localDimension3.width = localDimension1.width;
      i = 1;
    }
    if (i != 0) {
      this.viewer.vncFrame.setSize(localDimension3);
    }

    this.viewer.desktopScrollPane.doLayout();
  }

  public void processNormalProtocol()
    throws Exception
  {
    this.viewer.checkRecordingStatus();

    this.rfb.writeFramebufferUpdateRequest(0, 0, this.rfb.framebufferWidth, this.rfb.framebufferHeight, false);

    resetStats();
    int i = 0;
    while (true)
    {
      int j = this.rfb.readServerMessageType();

      switch (j)
      {
      case 0:
        if ((this.statNumUpdates == this.viewer.debugStatsExcludeUpdates) && (i == 0))
        {
          resetStats();
          i = 1;
        } else if ((this.statNumUpdates == this.viewer.debugStatsMeasureUpdates) && (i != 0))
        {
          this.viewer.disconnect();
        }

        this.rfb.readFramebufferUpdate();
        this.statNumUpdates += 1;

        int k = 0;

        for (int m = 0; m < this.rfb.updateNRects; m++)
        {
          this.rfb.readFramebufferUpdateRectHdr();
          this.statNumTotalRects += 1;
          int n = this.rfb.updateRectX; int i2 = this.rfb.updateRectY;
          int i4 = this.rfb.updateRectW; int i5 = this.rfb.updateRectH;

          if (this.rfb.updateRectEncoding == -224) {
            break;
          }
          if (this.rfb.updateRectEncoding == -223) {
            this.rfb.setFramebufferSize(i4, i5);
            updateFramebufferSize();
            break;
          }

          if ((this.rfb.updateRectEncoding == -240) || (this.rfb.updateRectEncoding == -239))
          {
            handleCursorShapeUpdate(this.rfb.updateRectEncoding, n, i2, i4, i5);
          }
          else if (this.rfb.updateRectEncoding == -232) {
            softCursorMove(n, i2);
            k = 1;
          }
          else
          {
            long l = this.rfb.getNumBytesRead();

            this.rfb.startTiming();

            switch (this.rfb.updateRectEncoding) {
            case 0:
              this.statNumRectsRaw += 1;
              handleRawRect(n, i2, i4, i5);
              break;
            case 1:
              this.statNumRectsCopy += 1;
              handleCopyRect(n, i2, i4, i5);
              break;
            case 2:
              handleRRERect(n, i2, i4, i5);
              break;
            case 4:
              handleCoRRERect(n, i2, i4, i5);
              break;
            case 5:
              this.statNumRectsHextile += 1;
              handleHextileRect(n, i2, i4, i5);
              break;
            case 16:
              this.statNumRectsZRLE += 1;
              handleZRLERect(n, i2, i4, i5);
              break;
            case 6:
              handleZlibRect(n, i2, i4, i5);
              break;
            case 7:
              this.statNumRectsTight += 1;
              handleTightRect(n, i2, i4, i5);
              break;
            case 3:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            default:
              throw new Exception("Unknown RFB rectangle encoding " + this.rfb.updateRectEncoding);
            }

            this.rfb.stopTiming();

            this.statNumPixelRects += 1;
            this.statNumBytesDecoded += i4 * i5 * this.bytesPixel;
            this.statNumBytesEncoded += (int)(this.rfb.getNumBytesRead() - l);
          }
        }

       int m = 0;

        if (this.viewer.checkRecordingStatus()) {
          m = 1;
        }

        if ((this.viewer.deferUpdateRequests > 0) && (this.rfb.available() == 0) && (k == 0))
        {
          synchronized (this.rfb) {
            try {
              this.rfb.wait(this.viewer.deferUpdateRequests);
            }
            catch (InterruptedException localInterruptedException) {
            }
          }
        }
        this.viewer.autoSelectEncodings();

        if (this.viewer.options.eightBitColors != (this.bytesPixel == 1))
        {
          setPixelFormat();
          m = 1;
        }

        int i1 = this.rfb.framebufferWidth;
        int i3 = this.rfb.framebufferHeight;
        this.rfb.writeFramebufferUpdateRequest(0, 0, i1, i3, m == 0);

        break;
      case 1:
        throw new Exception("Can't handle SetColourMapEntries message");
      case 2:
        Toolkit.getDefaultToolkit().beep();
        break;
      case 3:
        String str = this.rfb.readServerCutText();
        this.viewer.clipboard.setCutText(str);
        break;
      default:
        throw new Exception("Unknown RFB message type " + j);
      }
    }
  }

  void handleRawRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    throws IOException
  {
    handleRawRect(paramInt1, paramInt2, paramInt3, paramInt4, true);
  }

  void handleRawRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4, boolean paramBoolean)
    throws IOException
  {
    if (this.bytesPixel == 1) {
      for (int i = paramInt2; i < paramInt2 + paramInt4; i++) {
        this.rfb.readFully(this.pixels8, i * this.rfb.framebufferWidth + paramInt1, paramInt3);
        if (this.rfb.rec != null)
          this.rfb.rec.write(this.pixels8, i * this.rfb.framebufferWidth + paramInt1, paramInt3);
      }
    }
    else {
      byte[] arrayOfByte = new byte[paramInt3 * 4];

      for (int m = paramInt2; m < paramInt2 + paramInt4; m++) {
        this.rfb.readFully(arrayOfByte);
        if (this.rfb.rec != null) {
          this.rfb.rec.write(arrayOfByte);
        }
        int k = m * this.rfb.framebufferWidth + paramInt1;
        for (int j = 0; j < paramInt3; j++) {
          this.pixels24[(k + j)] = ((arrayOfByte[(j * 4 + 2)] & 0xFF) << 16 | (arrayOfByte[(j * 4 + 1)] & 0xFF) << 8 | arrayOfByte[(j * 4)] & 0xFF);
        }

      }

    }

    handleUpdatedPixels(paramInt1, paramInt2, paramInt3, paramInt4);
    if (paramBoolean)
      scheduleRepaint(paramInt1, paramInt2, paramInt3, paramInt4);
  }

  void handleCopyRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    throws IOException
  {
    this.rfb.readCopyRect();
    this.memGraphics.copyArea(this.rfb.copyRectSrcX, this.rfb.copyRectSrcY, paramInt3, paramInt4, paramInt1 - this.rfb.copyRectSrcX, paramInt2 - this.rfb.copyRectSrcY);

    scheduleRepaint(paramInt1, paramInt2, paramInt3, paramInt4);
  }

  void handleRRERect(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    throws IOException
  {
    int i = this.rfb.readU32();

    byte[] arrayOfByte1 = new byte[this.bytesPixel];
    this.rfb.readFully(arrayOfByte1);
    Color localColor;
    if (this.bytesPixel == 1)
      localColor = this.colors[(arrayOfByte1[0] & 0xFF)];
    else {
      localColor = new Color(arrayOfByte1[2] & 0xFF, arrayOfByte1[1] & 0xFF, arrayOfByte1[0] & 0xFF);
    }
    this.memGraphics.setColor(localColor);
    this.memGraphics.fillRect(paramInt1, paramInt2, paramInt3, paramInt4);

    byte[] arrayOfByte2 = new byte[i * (this.bytesPixel + 8)];
    this.rfb.readFully(arrayOfByte2);
    DataInputStream localDataInputStream = new DataInputStream(new ByteArrayInputStream(arrayOfByte2));

    if (this.rfb.rec != null) {
      this.rfb.rec.writeIntBE(i);
      this.rfb.rec.write(arrayOfByte1);
      this.rfb.rec.write(arrayOfByte2);
    }

    for (int i1 = 0; i1 < i; i1++) {
      if (this.bytesPixel == 1) {
        localColor = this.colors[localDataInputStream.readUnsignedByte()];
      } else {
        localDataInputStream.skip(4L);
        localColor = new Color(arrayOfByte2[(i1 * 12 + 2)] & 0xFF, arrayOfByte2[(i1 * 12 + 1)] & 0xFF, arrayOfByte2[(i1 * 12)] & 0xFF);
      }

      int j = paramInt1 + localDataInputStream.readUnsignedShort();
      int k = paramInt2 + localDataInputStream.readUnsignedShort();
      int m = localDataInputStream.readUnsignedShort();
      int n = localDataInputStream.readUnsignedShort();

      this.memGraphics.setColor(localColor);
      this.memGraphics.fillRect(j, k, m, n);
    }

    scheduleRepaint(paramInt1, paramInt2, paramInt3, paramInt4);
  }

  void handleCoRRERect(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    throws IOException
  {
    int i = this.rfb.readU32();

    byte[] arrayOfByte1 = new byte[this.bytesPixel];
    this.rfb.readFully(arrayOfByte1);
    Color localColor;
    if (this.bytesPixel == 1)
      localColor = this.colors[(arrayOfByte1[0] & 0xFF)];
    else {
      localColor = new Color(arrayOfByte1[2] & 0xFF, arrayOfByte1[1] & 0xFF, arrayOfByte1[0] & 0xFF);
    }
    this.memGraphics.setColor(localColor);
    this.memGraphics.fillRect(paramInt1, paramInt2, paramInt3, paramInt4);

    byte[] arrayOfByte2 = new byte[i * (this.bytesPixel + 4)];
    this.rfb.readFully(arrayOfByte2);

    if (this.rfb.rec != null) {
      this.rfb.rec.writeIntBE(i);
      this.rfb.rec.write(arrayOfByte1);
      this.rfb.rec.write(arrayOfByte2);
    }

    int i1 = 0;

    for (int i2 = 0; i2 < i; i2++) {
      if (this.bytesPixel == 1) {
        localColor = this.colors[(arrayOfByte2[(i1++)] & 0xFF)];
      } else {
        localColor = new Color(arrayOfByte2[(i1 + 2)] & 0xFF, arrayOfByte2[(i1 + 1)] & 0xFF, arrayOfByte2[i1] & 0xFF);
        i1 += 4;
      }
      int j = paramInt1 + (arrayOfByte2[(i1++)] & 0xFF);
      int k = paramInt2 + (arrayOfByte2[(i1++)] & 0xFF);
      int m = arrayOfByte2[(i1++)] & 0xFF;
      int n = arrayOfByte2[(i1++)] & 0xFF;

      this.memGraphics.setColor(localColor);
      this.memGraphics.fillRect(j, k, m, n);
    }

    scheduleRepaint(paramInt1, paramInt2, paramInt3, paramInt4);
  }

  void handleHextileRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    throws IOException
  {
    this.hextile_bg = new Color(0);
    this.hextile_fg = new Color(0);

    for (int i = paramInt2; i < paramInt2 + paramInt4; i += 16) {
      int j = 16;
      if (paramInt2 + paramInt4 - i < 16) {
        j = paramInt2 + paramInt4 - i;
      }
      for (int k = paramInt1; k < paramInt1 + paramInt3; k += 16) {
        int m = 16;
        if (paramInt1 + paramInt3 - k < 16) {
          m = paramInt1 + paramInt3 - k;
        }
        handleHextileSubrect(k, i, m, j);
      }

      scheduleRepaint(paramInt1, paramInt2, paramInt3, paramInt4);
    }
  }

  void handleHextileSubrect(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    throws IOException
  {
    int i = this.rfb.readU8();
    if (this.rfb.rec != null) {
      this.rfb.rec.writeByte(i);
    }

    if ((i & 0x1) != 0) {
      handleRawRect(paramInt1, paramInt2, paramInt3, paramInt4, false);
      return;
    }

    byte[] arrayOfByte1 = new byte[this.bytesPixel];
    if ((i & 0x2) != 0) {
      this.rfb.readFully(arrayOfByte1);
      if (this.bytesPixel == 1)
        this.hextile_bg = this.colors[(arrayOfByte1[0] & 0xFF)];
      else {
        this.hextile_bg = new Color(arrayOfByte1[2] & 0xFF, arrayOfByte1[1] & 0xFF, arrayOfByte1[0] & 0xFF);
      }
      if (this.rfb.rec != null) {
        this.rfb.rec.write(arrayOfByte1);
      }
    }
    this.memGraphics.setColor(this.hextile_bg);
    this.memGraphics.fillRect(paramInt1, paramInt2, paramInt3, paramInt4);

    if ((i & 0x4) != 0) {
      this.rfb.readFully(arrayOfByte1);
      if (this.bytesPixel == 1)
        this.hextile_fg = this.colors[(arrayOfByte1[0] & 0xFF)];
      else {
        this.hextile_fg = new Color(arrayOfByte1[2] & 0xFF, arrayOfByte1[1] & 0xFF, arrayOfByte1[0] & 0xFF);
      }
      if (this.rfb.rec != null) {
        this.rfb.rec.write(arrayOfByte1);
      }

    }

    if ((i & 0x8) == 0) {
      return;
    }
    int j = this.rfb.readU8();
    int k = j * 2;
    if ((i & 0x10) != 0) {
      k += j * this.bytesPixel;
    }
    byte[] arrayOfByte2 = new byte[k];
    this.rfb.readFully(arrayOfByte2);
    if (this.rfb.rec != null) {
      this.rfb.rec.writeByte(j);
      this.rfb.rec.write(arrayOfByte2);
    }

    int i5 = 0;
    int i6;
    int m;
    int n;
    int i1;
    int i2;
    int i3;
    int i4;
    if ((i & 0x10) == 0)
    {
      this.memGraphics.setColor(this.hextile_fg);
      for (i6 = 0; i6 < j; i6++) {
        m = arrayOfByte2[(i5++)] & 0xFF;
        n = arrayOfByte2[(i5++)] & 0xFF;
        i1 = paramInt1 + (m >> 4);
        i2 = paramInt2 + (m & 0xF);
        i3 = (n >> 4) + 1;
        i4 = (n & 0xF) + 1;
        this.memGraphics.fillRect(i1, i2, i3, i4);
      }
    } else if (this.bytesPixel == 1)
    {
      for (i6 = 0; i6 < j; i6++) {
        this.hextile_fg = this.colors[(arrayOfByte2[(i5++)] & 0xFF)];
        m = arrayOfByte2[(i5++)] & 0xFF;
        n = arrayOfByte2[(i5++)] & 0xFF;
        i1 = paramInt1 + (m >> 4);
        i2 = paramInt2 + (m & 0xF);
        i3 = (n >> 4) + 1;
        i4 = (n & 0xF) + 1;
        this.memGraphics.setColor(this.hextile_fg);
        this.memGraphics.fillRect(i1, i2, i3, i4);
      }

    }
    else
    {
      for (i6 = 0; i6 < j; i6++) {
        this.hextile_fg = new Color(arrayOfByte2[(i5 + 2)] & 0xFF, arrayOfByte2[(i5 + 1)] & 0xFF, arrayOfByte2[i5] & 0xFF);

        i5 += 4;
        m = arrayOfByte2[(i5++)] & 0xFF;
        n = arrayOfByte2[(i5++)] & 0xFF;
        i1 = paramInt1 + (m >> 4);
        i2 = paramInt2 + (m & 0xF);
        i3 = (n >> 4) + 1;
        i4 = (n & 0xF) + 1;
        this.memGraphics.setColor(this.hextile_fg);
        this.memGraphics.fillRect(i1, i2, i3, i4);
      }
    }
  }

  void handleZRLERect(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    throws Exception
  {
    if (this.zrleInStream == null) {
      this.zrleInStream = new ZlibInStream();
    }
    int i = this.rfb.readU32();
    if (i > 67108864) {
      throw new Exception("ZRLE decoder: illegal compressed data size");
    }
    if ((this.zrleBuf == null) || (this.zrleBufLen < i)) {
      this.zrleBufLen = (i + 4096);
      this.zrleBuf = new byte[this.zrleBufLen];
    }

    this.rfb.readFully(this.zrleBuf, 0, i);

    if (this.rfb.rec != null) {
      if (this.rfb.recordFromBeginning) {
        this.rfb.rec.writeIntBE(i);
        this.rfb.rec.write(this.zrleBuf, 0, i);
      } else if (!this.zrleRecWarningShown) {
        System.out.println("Warning: ZRLE session can be recorded only from the beginning");

        System.out.println("Warning: Recorded file may be corrupted");
        this.zrleRecWarningShown = true;
      }
    }

    this.zrleInStream.setUnderlying(new MemInStream(this.zrleBuf, 0, i), i);

    for (int j = paramInt2; j < paramInt2 + paramInt4; j += 64)
    {
      int k = Math.min(paramInt2 + paramInt4 - j, 64);

      for (int m = paramInt1; m < paramInt1 + paramInt3; m += 64)
      {
        int n = Math.min(paramInt1 + paramInt3 - m, 64);

        int i1 = this.zrleInStream.readU8();
        int i2 = (i1 & 0x80) != 0 ? 1 : 0;
        int i3 = i1 & 0x7F;
        int[] arrayOfInt = new int[''];

        readZrlePalette(arrayOfInt, i3);

        if (i3 == 1) {
          int i4 = arrayOfInt[0];
          Color localColor = this.bytesPixel == 1 ? this.colors[i4] : new Color(0xFF000000 | i4);

          this.memGraphics.setColor(localColor);
          this.memGraphics.fillRect(m, j, n, k);
        }
        else
        {
          if (i2 == 0) {
            if (i3 == 0)
              readZrleRawPixels(n, k);
            else {
              readZrlePackedPixels(n, k, arrayOfInt, i3);
            }
          }
          else if (i3 == 0)
            readZrlePlainRLEPixels(n, k);
          else {
            readZrlePackedRLEPixels(n, k, arrayOfInt);
          }

          handleUpdatedZrleTile(m, j, n, k);
        }
      }
    }
    this.zrleInStream.reset();

    scheduleRepaint(paramInt1, paramInt2, paramInt3, paramInt4);
  }

  int readPixel(InStream paramInStream)
    throws Exception
  {
    int i;
    if (this.bytesPixel == 1) {
      i = paramInStream.readU8();
    } else {
      int j = paramInStream.readU8();
      int k = paramInStream.readU8();
      int m = paramInStream.readU8();
      i = (m & 0xFF) << 16 | (k & 0xFF) << 8 | j & 0xFF;
    }
    return i;
  }

  void readPixels(InStream paramInStream, int[] paramArrayOfInt, int paramInt)
    throws Exception
  {
    byte[] arrayOfByte;
    int i;
    if (this.bytesPixel == 1) {
      arrayOfByte = new byte[paramInt];
      paramInStream.readBytes(arrayOfByte, 0, paramInt);
      for (i = 0; i < paramInt; i++)
        arrayOfByte[i] &= 255;
    }
    else {
      arrayOfByte = new byte[paramInt * 3];
      paramInStream.readBytes(arrayOfByte, 0, paramInt * 3);
      for (i = 0; i < paramInt; i++)
        paramArrayOfInt[i] = ((arrayOfByte[(i * 3 + 2)] & 0xFF) << 16 | (arrayOfByte[(i * 3 + 1)] & 0xFF) << 8 | arrayOfByte[(i * 3)] & 0xFF);
    }
  }

  void readZrlePalette(int[] paramArrayOfInt, int paramInt)
    throws Exception
  {
    readPixels(this.zrleInStream, paramArrayOfInt, paramInt);
  }

  void readZrleRawPixels(int paramInt1, int paramInt2) throws Exception {
    if (this.bytesPixel == 1)
      this.zrleInStream.readBytes(this.zrleTilePixels8, 0, paramInt1 * paramInt2);
    else
      readPixels(this.zrleInStream, this.zrleTilePixels24, paramInt1 * paramInt2);
  }

  void readZrlePackedPixels(int paramInt1, int paramInt2, int[] paramArrayOfInt, int paramInt3)
    throws Exception
  {
    int i = paramInt3 > 2 ? 2 : paramInt3 > 4 ? 4 : paramInt3 > 16 ? 8 : 1;

    int j = 0;

    for (int k = 0; k < paramInt2; k++) {
      int m = j + paramInt1;
      int n = 0;
      int i1 = 0;

      while (j < m) {
        if (i1 == 0) {
          n = this.zrleInStream.readU8();
          i1 = 8;
        }
        i1 -= i;
        int i2 = n >> i1 & (1 << i) - 1 & 0x7F;
        if (this.bytesPixel == 1)
          this.zrleTilePixels8[(j++)] = (byte)paramArrayOfInt[i2];
        else
          this.zrleTilePixels24[(j++)] = paramArrayOfInt[i2];
      }
    }
  }

  void readZrlePlainRLEPixels(int paramInt1, int paramInt2) throws Exception
  {
    int i = 0;
    int j = i + paramInt1 * paramInt2;
    while (i < j) { int k = readPixel(this.zrleInStream);
      int m = 1;
      int n;
      do { n = this.zrleInStream.readU8();
        m += n; }
      while (n == 255);

      if (m > j - i) {
        throw new Exception("ZRLE decoder: assertion failed (len <= end-ptr)");
      }

      if (this.bytesPixel == 1) {
        while (m-- > 0) this.zrleTilePixels8[(i++)] = (byte)k;
      }
      while (m-- > 0) this.zrleTilePixels24[(i++)] = k;
    }
  }

  void readZrlePackedRLEPixels(int paramInt1, int paramInt2, int[] paramArrayOfInt)
    throws Exception
  {
    int i = 0;
    int j = i + paramInt1 * paramInt2;
    while (i < j) {
      int k = this.zrleInStream.readU8();
      int m = 1;
      if ((k & 0x80) != 0)
      {
    	  int n;
        do {
          n = this.zrleInStream.readU8();
          m += n;
        }while (n == 255);

        if (m > j - i) {
          throw new Exception("ZRLE decoder: assertion failed (len <= end - ptr)");
        }
      }

      k &= 127;
      int n = paramArrayOfInt[k];

      if (this.bytesPixel == 1) {
        while (m-- > 0) this.zrleTilePixels8[(i++)] = (byte)n;
      }
      while (m-- > 0) this.zrleTilePixels24[(i++)] = n;
    }
  }

  void handleUpdatedZrleTile(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    Object localObject1;
    Object localObject2;
    if (this.bytesPixel == 1) {
      localObject1 = this.zrleTilePixels8; localObject2 = this.pixels8;
    } else {
      localObject1 = this.zrleTilePixels24; localObject2 = this.pixels24;
    }
    int i = 0;
    int j = paramInt2 * this.rfb.framebufferWidth + paramInt1;
    for (int k = 0; k < paramInt4; k++) {
      System.arraycopy(localObject1, i, localObject2, j, paramInt3);
      i += paramInt3;
      j += this.rfb.framebufferWidth;
    }
    handleUpdatedPixels(paramInt1, paramInt2, paramInt3, paramInt4);
  }

  void handleZlibRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    throws Exception
  {
    int i = this.rfb.readU32();

    if ((this.zlibBuf == null) || (this.zlibBufLen < i)) {
      this.zlibBufLen = (i * 2);
      this.zlibBuf = new byte[this.zlibBufLen];
    }

    this.rfb.readFully(this.zlibBuf, 0, i);

    if ((this.rfb.rec != null) && (this.rfb.recordFromBeginning)) {
      this.rfb.rec.writeIntBE(i);
      this.rfb.rec.write(this.zlibBuf, 0, i);
    }

    if (this.zlibInflater == null) {
      this.zlibInflater = new Inflater();
    }
    this.zlibInflater.setInput(this.zlibBuf, 0, i);

    if (this.bytesPixel == 1) {
      for (int j = paramInt2; j < paramInt2 + paramInt4; j++) {
        this.zlibInflater.inflate(this.pixels8, j * this.rfb.framebufferWidth + paramInt1, paramInt3);
        if ((this.rfb.rec != null) && (!this.rfb.recordFromBeginning))
          this.rfb.rec.write(this.pixels8, j * this.rfb.framebufferWidth + paramInt1, paramInt3);
      }
    } else {
      byte[] arrayOfByte = new byte[paramInt3 * 4];

      for (int n = paramInt2; n < paramInt2 + paramInt4; n++) {
        this.zlibInflater.inflate(arrayOfByte);
        int m = n * this.rfb.framebufferWidth + paramInt1;
        for (int k = 0; k < paramInt3; k++) {
          this.pixels24[(m + k)] = ((arrayOfByte[(k * 4 + 2)] & 0xFF) << 16 | (arrayOfByte[(k * 4 + 1)] & 0xFF) << 8 | arrayOfByte[(k * 4)] & 0xFF);
        }

        if ((this.rfb.rec != null) && (!this.rfb.recordFromBeginning)) {
          this.rfb.rec.write(arrayOfByte);
        }
      }
    }
    handleUpdatedPixels(paramInt1, paramInt2, paramInt3, paramInt4);
    scheduleRepaint(paramInt1, paramInt2, paramInt3, paramInt4);
  }

  void handleTightRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    throws Exception
  {
    int i = this.rfb.readU8();
    if (this.rfb.rec != null) {
      if ((this.rfb.recordFromBeginning) || (i == 8 << 4) || (i == 9 << 4))
      {
        this.rfb.rec.writeByte(i);
      }
      else {
        this.rfb.rec.writeByte(i | 0xF);
      }

    }

    for (int j = 0; j < 4; j++) {
      if (((i & 0x1) != 0) && (this.tightInflaters[j] != null)) {
        this.tightInflaters[j] = null;
      }
      i >>= 1;
    }

    if (i > 9)
      throw new Exception("Incorrect tight subencoding: " + i);
    byte[] arrayOfByte1;
    Object localObject1;
    if (i == 8)
    {
      if (this.bytesPixel == 1) {
        int j = this.rfb.readU8();
        this.memGraphics.setColor(this.colors[j]);
        if (this.rfb.rec != null)
          this.rfb.rec.writeByte(j);
      }
      else {
        arrayOfByte1 = new byte[3];
        this.rfb.readFully(arrayOfByte1);
        if (this.rfb.rec != null) {
          this.rfb.rec.write(arrayOfByte1);
        }
        localObject1 = new Color(0xFF000000 | (arrayOfByte1[0] & 0xFF) << 16 | (arrayOfByte1[1] & 0xFF) << 8 | arrayOfByte1[2] & 0xFF);

        this.memGraphics.setColor((Color)localObject1);
      }
      this.memGraphics.fillRect(paramInt1, paramInt2, paramInt3, paramInt4);
      scheduleRepaint(paramInt1, paramInt2, paramInt3, paramInt4);
      return;
    }

    if (i == 9)
    {
      this.statNumRectsTightJPEG += 1;

      arrayOfByte1 = new byte[this.rfb.readCompactLen()];
      this.rfb.readFully(arrayOfByte1);
      if (this.rfb.rec != null) {
        if (!this.rfb.recordFromBeginning) {
          this.rfb.recordCompactLen(arrayOfByte1.length);
        }
        this.rfb.rec.write(arrayOfByte1);
      }

      localObject1 = Toolkit.getDefaultToolkit().createImage(arrayOfByte1);

      this.jpegRect = new Rectangle(paramInt1, paramInt2, paramInt3, paramInt4);

      synchronized (this.jpegRect) {
        Toolkit.getDefaultToolkit().prepareImage((Image)localObject1, -1, -1, this);
        try
        {
          this.jpegRect.wait(3000L);
        } catch (InterruptedException localInterruptedException) {
          throw new Exception("Interrupted while decoding JPEG image");
        }

      }

      this.jpegRect = null;
      return;
    }

    int k = 0; int m = paramInt3;
    byte [] ppp = new byte[2];
    int[] arrayOfInt = new int[256];
    int n = 0;
    byte[] arrayOfByte2;
    int i4;
    if ((i & 0x4) != 0) {
      int i1 = this.rfb.readU8();
      if (this.rfb.rec != null) {
        this.rfb.rec.writeByte(i1);
      }
      if (i1 == 1) {
        k = this.rfb.readU8() + 1;
        if (this.rfb.rec != null) {
          this.rfb.rec.writeByte(k - 1);
        }
        if (this.bytesPixel == 1) {
          if (k != 2) {
            throw new Exception("Incorrect tight palette size: " + k);
          }
          this.rfb.readFully(ppp);
          if (this.rfb.rec != null)
            this.rfb.rec.write(ppp);
        }
        else {
          arrayOfByte2 = new byte[k * 3];
          this.rfb.readFully(arrayOfByte2);
          if (this.rfb.rec != null) {
            this.rfb.rec.write(arrayOfByte2);
          }
          for (i4 = 0; i4 < k; i4++) {
            arrayOfInt[i4] = ((arrayOfByte2[(i4 * 3)] & 0xFF) << 16 | (arrayOfByte2[(i4 * 3 + 1)] & 0xFF) << 8 | arrayOfByte2[(i4 * 3 + 2)] & 0xFF);
          }

        }

        if (k == 2)
          m = (paramInt3 + 7) / 8;
      } else if (i1 == 2) {
        n = 1;
      } else if (i1 != 0) {
        throw new Exception("Incorrect tight filter id: " + i1);
      }
    }
    if ((k == 0) && (this.bytesPixel == 4)) {
      m *= 3;
    }

    int i1 = paramInt4 * m;
    int i5;
    if (i1 < 12)
    {
      int i6;
      if (k != 0)
      {
        arrayOfByte2 = new byte[i1];
        this.rfb.readFully(arrayOfByte2);
        if (this.rfb.rec != null) {
          this.rfb.rec.write(arrayOfByte2);
        }
        if (k == 2)
        {
          if (this.bytesPixel == 1)
            decodeMonoData(paramInt1, paramInt2, paramInt3, paramInt4, arrayOfByte2, ppp);
          else
            decodeMonoData(paramInt1, paramInt2, paramInt3, paramInt4, arrayOfByte2, arrayOfInt);
        }
        else
        {
          i4 = 0;
          for (i5 = paramInt2; i5 < paramInt2 + paramInt4; i5++) {
            for (i6 = paramInt1; i6 < paramInt1 + paramInt3; i6++) {
              this.pixels24[(i5 * this.rfb.framebufferWidth + i6)] = arrayOfInt[(arrayOfByte2[(i4++)] & 0xFF)];
            }
          }
        }
      }
      else if (n != 0)
      {
        arrayOfByte2 = new byte[paramInt3 * paramInt4 * 3];
        this.rfb.readFully(arrayOfByte2);
        if (this.rfb.rec != null) {
          this.rfb.rec.write(arrayOfByte2);
        }
        decodeGradientData(paramInt1, paramInt2, paramInt3, paramInt4, arrayOfByte2);
      }
      else if (this.bytesPixel == 1) {
        for (int i2 = paramInt2; i2 < paramInt2 + paramInt4; i2++) {
          this.rfb.readFully(this.pixels8, i2 * this.rfb.framebufferWidth + paramInt1, paramInt3);
          if (this.rfb.rec != null)
            this.rfb.rec.write(this.pixels8, i2 * this.rfb.framebufferWidth + paramInt1, paramInt3);
        }
      }
      else {
        byte[] arrayOfByte3 = new byte[paramInt3 * 3];

        for (i6 = paramInt2; i6 < paramInt2 + paramInt4; i6++) {
          this.rfb.readFully(arrayOfByte3);
          if (this.rfb.rec != null) {
            this.rfb.rec.write(arrayOfByte3);
          }
          i5 = i6 * this.rfb.framebufferWidth + paramInt1;
          for (i4 = 0; i4 < paramInt3; i4++) {
            this.pixels24[(i5 + i4)] = ((arrayOfByte3[(i4 * 3)] & 0xFF) << 16 | (arrayOfByte3[(i4 * 3 + 1)] & 0xFF) << 8 | arrayOfByte3[(i4 * 3 + 2)] & 0xFF);
          }

        }

      }

    }
    else
    {
      int i3 = this.rfb.readCompactLen();
      byte[] arrayOfByte4 = new byte[i3];
      this.rfb.readFully(arrayOfByte4);
      if ((this.rfb.rec != null) && (this.rfb.recordFromBeginning)) {
        this.rfb.rec.write(arrayOfByte4);
      }
      i5 = i & 0x3;
      if (this.tightInflaters[i5] == null) {
        this.tightInflaters[i5] = new Inflater();
      }
      Inflater localInflater = this.tightInflaters[i5];
      localInflater.setInput(arrayOfByte4);
      byte[] arrayOfByte5 = new byte[i1];
      localInflater.inflate(arrayOfByte5);
      if ((this.rfb.rec != null) && (!this.rfb.recordFromBeginning))
        this.rfb.recordCompressedData(arrayOfByte5);
      int i7;
      int i8;
      int i9;
      if (k != 0)
      {
        if (k == 2)
        {
          if (this.bytesPixel == 1)
            decodeMonoData(paramInt1, paramInt2, paramInt3, paramInt4, arrayOfByte5, ppp);
          else
            decodeMonoData(paramInt1, paramInt2, paramInt3, paramInt4, arrayOfByte5, arrayOfInt);
        }
        else
        {
          i7 = 0;
          for (i8 = paramInt2; i8 < paramInt2 + paramInt4; i8++) {
            for (i9 = paramInt1; i9 < paramInt1 + paramInt3; i9++) {
              this.pixels24[(i8 * this.rfb.framebufferWidth + i9)] = arrayOfInt[(arrayOfByte5[(i7++)] & 0xFF)];
            }
          }
        }
      }
      else if (n != 0)
      {
        decodeGradientData(paramInt1, paramInt2, paramInt3, paramInt4, arrayOfByte5);
      }
      else if (this.bytesPixel == 1) {
        i7 = paramInt2 * this.rfb.framebufferWidth + paramInt1;
        for (i8 = 0; i8 < paramInt4; i8++) {
          System.arraycopy(arrayOfByte5, i8 * paramInt3, this.pixels8, i7, paramInt3);
          i7 += this.rfb.framebufferWidth;
        }
      } else {
        i7 = 0;

        for (int i10 = 0; i10 < paramInt4; i10++) {
          localInflater.inflate(arrayOfByte5);
          i8 = (paramInt2 + i10) * this.rfb.framebufferWidth + paramInt1;
          for (i9 = 0; i9 < paramInt3; i9++) {
            this.pixels24[(i8 + i9)] = ((arrayOfByte5[i7] & 0xFF) << 16 | (arrayOfByte5[(i7 + 1)] & 0xFF) << 8 | arrayOfByte5[(i7 + 2)] & 0xFF);

            i7 += 3;
          }
        }
      }

    }

    handleUpdatedPixels(paramInt1, paramInt2, paramInt3, paramInt4);
    scheduleRepaint(paramInt1, paramInt2, paramInt3, paramInt4);
  }

  void decodeMonoData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
  {
    int m = paramInt2 * this.rfb.framebufferWidth + paramInt1;
    int n = (paramInt3 + 7) / 8;
    int i,j,k;
    for (j = 0; j < paramInt4; j++) {
      for (i = 0; i < paramInt3 / 8; i++) {
        int i1 = paramArrayOfByte1[(j * n + i)];
        for (k = 7; k >= 0; k--)
          this.pixels8[(m++)] = paramArrayOfByte2[(i1 >> k & 0x1)];
      }
      for (k = 7; k >= 8 - paramInt3 % 8; k--) {
        this.pixels8[(m++)] = paramArrayOfByte2[(paramArrayOfByte1[(j * n + i)] >> k & 0x1)];
      }
      m += this.rfb.framebufferWidth - paramInt3;
    }
  }

  void decodeMonoData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte, int[] paramArrayOfInt)
  {
    int m = paramInt2 * this.rfb.framebufferWidth + paramInt1;
    int n = (paramInt3 + 7) / 8;
    int i,j,k;
    for (j = 0; j < paramInt4; j++) {
      for (i = 0; i < paramInt3 / 8; i++) {
        int i1 = paramArrayOfByte[(j * n + i)];
        for (k = 7; k >= 0; k--)
          this.pixels24[(m++)] = paramArrayOfInt[(i1 >> k & 0x1)];
      }
      for (k = 7; k >= 8 - paramInt3 % 8; k--) {
        this.pixels24[(m++)] = paramArrayOfInt[(paramArrayOfByte[(j * n + i)] >> k & 0x1)];
      }
      m += this.rfb.framebufferWidth - paramInt3;
    }
  }

  void decodeGradientData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte)
  {
    byte[] arrayOfByte1 = new byte[paramInt3 * 3];
    byte[] arrayOfByte2 = new byte[paramInt3 * 3];
    byte[] arrayOfByte3 = new byte[3];
    int[] arrayOfInt = new int[3];

    int m = paramInt2 * this.rfb.framebufferWidth + paramInt1;

    for (int j = 0; j < paramInt4; j++)
    {
      for (int k = 0; k < 3; k++) {
        arrayOfByte3[k] = (byte)(arrayOfByte1[k] + paramArrayOfByte[(j * paramInt3 * 3 + k)]);
        arrayOfByte2[k] = arrayOfByte3[k];
      }
      this.pixels24[(m++)] = ((arrayOfByte3[0] & 0xFF) << 16 | (arrayOfByte3[1] & 0xFF) << 8 | arrayOfByte3[2] & 0xFF);

      for (int i = 1; i < paramInt3; i++) {
        for (int k = 0; k < 3; k++) {
          arrayOfInt[k] = ((arrayOfByte1[(i * 3 + k)] & 0xFF) + (arrayOfByte3[k] & 0xFF) - (arrayOfByte1[((i - 1) * 3 + k)] & 0xFF));

          if (arrayOfInt[k] > 255)
            arrayOfInt[k] = 255;
          else if (arrayOfInt[k] < 0) {
            arrayOfInt[k] = 0;
          }
          arrayOfByte3[k] = (byte)(arrayOfInt[k] + paramArrayOfByte[((j * paramInt3 + i) * 3 + k)]);
          arrayOfByte2[(i * 3 + k)] = arrayOfByte3[k];
        }
        this.pixels24[(m++)] = ((arrayOfByte3[0] & 0xFF) << 16 | (arrayOfByte3[1] & 0xFF) << 8 | arrayOfByte3[2] & 0xFF);
      }

      System.arraycopy(arrayOfByte2, 0, arrayOfByte1, 0, paramInt3 * 3);
      m += this.rfb.framebufferWidth - paramInt3;
    }
  }

  void handleUpdatedPixels(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.pixelsSource.newPixels(paramInt1, paramInt2, paramInt3, paramInt4);
    this.memGraphics.setClip(paramInt1, paramInt2, paramInt3, paramInt4);
    this.memGraphics.drawImage(this.rawPixelsImage, 0, 0, null);
    this.memGraphics.setClip(0, 0, this.rfb.framebufferWidth, this.rfb.framebufferHeight);
  }

  void scheduleRepaint(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if (this.rfb.framebufferWidth == this.scaledWidth) {
      repaint(this.viewer.deferScreenUpdates, paramInt1, paramInt2, paramInt3, paramInt4);
    } else {
      int i = paramInt1 * this.scalingFactor / 100;
      int j = paramInt2 * this.scalingFactor / 100;
      int k = ((paramInt1 + paramInt3) * this.scalingFactor + 49) / 100 - i + 1;
      int m = ((paramInt2 + paramInt4) * this.scalingFactor + 49) / 100 - j + 1;
      repaint(this.viewer.deferScreenUpdates, i, j, k, m);
    }
  }

  public void keyPressed(KeyEvent paramKeyEvent)
  {
    processLocalKeyEvent(paramKeyEvent);
  }
  public void keyReleased(KeyEvent paramKeyEvent) {
    processLocalKeyEvent(paramKeyEvent);
  }
  public void keyTyped(KeyEvent paramKeyEvent) {
    paramKeyEvent.consume();
  }

  public void mousePressed(MouseEvent paramMouseEvent) {
    processLocalMouseEvent(paramMouseEvent, false);
    //System.out.println(this.viewer.vncFrame.hasFocus());
   // System.out.println("mouse press:"+this.hasFocus());
  }
  public void mouseReleased(MouseEvent paramMouseEvent) {
    processLocalMouseEvent(paramMouseEvent, false);
   // System.out.println("mouse release:"+this.hasFocus());
  }
  public void mouseMoved(MouseEvent paramMouseEvent) {
    processLocalMouseEvent(paramMouseEvent, true);
    //System.out.println("mouse move:"+this.hasFocus());
  }
  public void mouseDragged(MouseEvent paramMouseEvent) {
    processLocalMouseEvent(paramMouseEvent, true);
   // System.out.println("mouse drag:"+this.hasFocus());
  }

  public void processLocalKeyEvent(KeyEvent paramKeyEvent) {
    if ((this.viewer.rfb != null) && (this.rfb.inNormalProtocol)) {
      if (!this.inputEnabled) {
        if (((paramKeyEvent.getKeyChar() == 'r') || (paramKeyEvent.getKeyChar() == 'R')) && (paramKeyEvent.getID() == 401))
        {
          try
          {
            this.rfb.writeFramebufferUpdateRequest(0, 0, this.rfb.framebufferWidth, this.rfb.framebufferHeight, false);
          }
          catch (IOException localIOException) {
            localIOException.printStackTrace();
          }
        }
      }
      else {
        synchronized (this.rfb) {
          try {
            this.rfb.writeKeyEvent(paramKeyEvent);
          } catch (Exception localException) {
            localException.printStackTrace();
          }
          this.rfb.notify();
        }
      }

    }

    paramKeyEvent.consume();
  }

  public void processLocalMouseEvent(MouseEvent paramMouseEvent, boolean paramBoolean) {
    if ((this.viewer.rfb != null) && (this.rfb.inNormalProtocol)) {
      if (paramBoolean) {
        softCursorMove(paramMouseEvent.getX(), paramMouseEvent.getY());
      }
      if (this.rfb.framebufferWidth != this.scaledWidth) {
        int i = (paramMouseEvent.getX() * 100 + this.scalingFactor / 2) / this.scalingFactor;
        int j = (paramMouseEvent.getY() * 100 + this.scalingFactor / 2) / this.scalingFactor;
        paramMouseEvent.translatePoint(i - paramMouseEvent.getX(), j - paramMouseEvent.getY());
      }
      synchronized (this.rfb) {
        try {
          this.rfb.writePointerEvent(paramMouseEvent);
        } catch (Exception localException) {
          localException.printStackTrace();
        }
        this.rfb.notify();
      }
    }
  }

  public void mouseClicked(MouseEvent paramMouseEvent)
  {
  }

  public void mouseEntered(MouseEvent paramMouseEvent)
  {
  }

  public void mouseExited(MouseEvent paramMouseEvent)
  {
  }

  void resetStats() {
    this.statStartTime = System.currentTimeMillis();
    this.statNumUpdates = 0;
    this.statNumTotalRects = 0;
    this.statNumPixelRects = 0;
    this.statNumRectsTight = 0;
    this.statNumRectsTightJPEG = 0;
    this.statNumRectsZRLE = 0;
    this.statNumRectsHextile = 0;
    this.statNumRectsRaw = 0;
    this.statNumRectsCopy = 0;
    this.statNumBytesEncoded = 0;
    this.statNumBytesDecoded = 0;
  }

  synchronized void handleCursorShapeUpdate(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
    throws IOException
  {
    softCursorFree();

    if (paramInt4 * paramInt5 == 0) {
      return;
    }

    if (this.viewer.options.ignoreCursorUpdates) {
      int i = (paramInt4 + 7) / 8;
      int j = i * paramInt5;

      if (paramInt1 == -240) {
        this.rfb.skipBytes(6 + j * 2);
      }
      else {
        this.rfb.skipBytes(paramInt4 * paramInt5 * this.bytesPixel + j);
      }
      return;
    }

    this.softCursorSource = decodeCursorShape(paramInt1, paramInt4, paramInt5);

    this.origCursorWidth = paramInt4;
    this.origCursorHeight = paramInt5;
    this.origHotX = paramInt2;
    this.origHotY = paramInt3;

    createSoftCursor();

    this.showSoftCursor = true;
    repaint(this.viewer.deferCursorUpdates, this.cursorX - this.hotX, this.cursorY - this.hotY, this.cursorWidth, this.cursorHeight);
  }

  synchronized MemoryImageSource decodeCursorShape(int paramInt1, int paramInt2, int paramInt3)
    throws IOException
  {
    int i = (paramInt2 + 7) / 8;
    int j = i * paramInt3;

    int[] arrayOfInt = new int[paramInt2 * paramInt3];
    byte[] arrayOfByte1;
    int [] localObject;
    int i2;
    int i1;
    int m;
    int n;
    int i3;
    if (paramInt1 == -240)
    {
      arrayOfByte1 = new byte[6];
      this.rfb.readFully(arrayOfByte1);
      localObject = new int[] { 0xFF000000 | (arrayOfByte1[3] & 0xFF) << 16 | (arrayOfByte1[4] & 0xFF) << 8 | arrayOfByte1[5] & 0xFF, 0xFF000000 | (arrayOfByte1[0] & 0xFF) << 16 | (arrayOfByte1[1] & 0xFF) << 8 | arrayOfByte1[2] & 0xFF };

      byte[] arrayOfByte2 = new byte[j];
      this.rfb.readFully(arrayOfByte2);
      byte[] arrayOfByte3 = new byte[j];
      this.rfb.readFully(arrayOfByte3);

      int i5 = 0;
      for (i2 = 0; i2 < paramInt3; i2++)
      {
        int i4;
        for (i1 = 0; i1 < paramInt2 / 8; i1++) {
          m = arrayOfByte2[(i2 * i + i1)];
          n = arrayOfByte3[(i2 * i + i1)];
          for (i3 = 7; i3 >= 0; i3--) {
            if ((n >> i3 & 0x1) != 0)
              i4 = localObject[(m >> i3 & 0x1)];
            else {
              i4 = 0;
            }
            arrayOfInt[(i5++)] = i4;
          }
        }
        for (i3 = 7; i3 >= 8 - paramInt2 % 8; i3--) {
          if ((arrayOfByte3[(i2 * i + i1)] >> i3 & 0x1) != 0)
            i4 = localObject[(arrayOfByte2[(i2 * i + i1)] >> i3 & 0x1)];
          else {
            i4 = 0;
          }
          arrayOfInt[(i5++)] = i4;
        }

      }

    }
    else
    {
      arrayOfByte1 = new byte[paramInt2 * paramInt3 * this.bytesPixel];
      this.rfb.readFully(arrayOfByte1);
      byte [] localObject1 = new byte[j];
      this.rfb.readFully(localObject1);

      i3 = 0;
      for (n = 0; n < paramInt3; n++) {
        for (m = 0; m < paramInt2 / 8; m++) {
          int k = localObject1[(n * i + m)];
          for (i1 = 7; i1 >= 0; i1--) {
            if ((k >> i1 & 0x1) != 0) {
              if (this.bytesPixel == 1)
                i2 = this.cm8.getRGB(arrayOfByte1[i3]);
              else {
                i2 = 0xFF000000 | (arrayOfByte1[(i3 * 4 + 2)] & 0xFF) << 16 | (arrayOfByte1[(i3 * 4 + 1)] & 0xFF) << 8 | arrayOfByte1[(i3 * 4)] & 0xFF;
              }

            }
            else
            {
              i2 = 0;
            }
            arrayOfInt[(i3++)] = i2;
          }
        }
        for (i1 = 7; i1 >= 8 - paramInt2 % 8; i1--) {
          if ((localObject1[(n * i + m)] >> i1 & 0x1) != 0) {
            if (this.bytesPixel == 1)
              i2 = this.cm8.getRGB(arrayOfByte1[i3]);
            else {
              i2 = 0xFF000000 | (arrayOfByte1[(i3 * 4 + 2)] & 0xFF) << 16 | (arrayOfByte1[(i3 * 4 + 1)] & 0xFF) << 8 | arrayOfByte1[(i3 * 4)] & 0xFF;
            }

          }
          else
          {
            i2 = 0;
          }
          arrayOfInt[(i3++)] = i2;
        }
      }

    }

    return (MemoryImageSource)new MemoryImageSource(paramInt2, paramInt3, arrayOfInt, 0, paramInt2);
  }

  synchronized void createSoftCursor()
  {
    if (this.softCursorSource == null) {
      return;
    }
    int i = this.viewer.options.scaleCursor;
    if ((i == 0) || (!this.inputEnabled)) {
      i = 100;
    }

    int j = this.cursorX - this.hotX;
    int k = this.cursorY - this.hotY;
    int m = this.cursorWidth;
    int n = this.cursorHeight;

    this.cursorWidth = ((this.origCursorWidth * i + 50) / 100);
    this.cursorHeight = ((this.origCursorHeight * i + 50) / 100);
    this.hotX = ((this.origHotX * i + 50) / 100);
    this.hotY = ((this.origHotY * i + 50) / 100);
    this.softCursor = Toolkit.getDefaultToolkit().createImage(this.softCursorSource);

    if (i != 100) {
      this.softCursor = this.softCursor.getScaledInstance(this.cursorWidth, this.cursorHeight, 4);
    }

    if (this.showSoftCursor)
    {
      j = Math.min(j, this.cursorX - this.hotX);
      k = Math.min(k, this.cursorY - this.hotY);
      m = Math.max(m, this.cursorWidth);
      n = Math.max(n, this.cursorHeight);

      repaint(this.viewer.deferCursorUpdates, j, k, m, n);
    }
  }

  synchronized void softCursorMove(int paramInt1, int paramInt2)
  {
    int i = this.cursorX;
    int j = this.cursorY;
    this.cursorX = paramInt1;
    this.cursorY = paramInt2;
    if (this.showSoftCursor) {
      repaint(this.viewer.deferCursorUpdates, i - this.hotX, j - this.hotY, this.cursorWidth, this.cursorHeight);

      repaint(this.viewer.deferCursorUpdates, this.cursorX - this.hotX, this.cursorY - this.hotY, this.cursorWidth, this.cursorHeight);
    }
  }

  synchronized void softCursorFree()
  {
    if (this.showSoftCursor) {
      this.showSoftCursor = false;
      this.softCursor = null;
      this.softCursorSource = null;

      repaint(this.viewer.deferCursorUpdates, this.cursorX - this.hotX, this.cursorY - this.hotY, this.cursorWidth, this.cursorHeight);
    }
  }
}