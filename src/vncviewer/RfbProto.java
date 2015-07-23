package vncviewer;

import java.awt.AWTEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.zip.Deflater;

public class RfbProto
{
  static final String versionMsg_3_3 = "RFB 003.003\n";
  static final String versionMsg_3_7 = "RFB 003.007\n";
  static final String versionMsg_3_8 = "RFB 003.008\n";
  static final String StandardVendor = "STDV";
  static final String TridiaVncVendor = "TRDV";
  static final String TightVncVendor = "TGHT";
  static final int SecTypeInvalid = 0;
  static final int SecTypeNone = 1;
  static final int SecTypeVncAuth = 2;
  static final int SecTypeTight = 16;
  static final int NoTunneling = 0;
  static final String SigNoTunneling = "NOTUNNEL";
  static final int AuthNone = 1;
  static final int AuthVNC = 2;
  static final int AuthUnixLogin = 129;
  static final String SigAuthNone = "NOAUTH__";
  static final String SigAuthVNC = "VNCAUTH_";
  static final String SigAuthUnixLogin = "ULGNAUTH";
  static final int VncAuthOK = 0;
  static final int VncAuthFailed = 1;
  static final int VncAuthTooMany = 2;
  static final int FramebufferUpdate = 0;
  static final int SetColourMapEntries = 1;
  static final int Bell = 2;
  static final int ServerCutText = 3;
  static final int EndOfContinuousUpdates = 150;
  static final String SigEndOfContinuousUpdates = "CUS_EOCU";
  static final int SetPixelFormat = 0;
  static final int FixColourMapEntries = 1;
  static final int SetEncodings = 2;
  static final int FramebufferUpdateRequest = 3;
  static final int KeyboardEvent = 4;
  static final int PointerEvent = 5;
  static final int ClientCutText = 6;
  static final int EnableContinuousUpdates = 150;
  static final String SigEnableContinuousUpdates = "CUC_ENCU";
  static final int EncodingRaw = 0;
  static final int EncodingCopyRect = 1;
  static final int EncodingRRE = 2;
  static final int EncodingCoRRE = 4;
  static final int EncodingHextile = 5;
  static final int EncodingZlib = 6;
  static final int EncodingTight = 7;
  static final int EncodingZRLE = 16;
  static final int EncodingCompressLevel0 = -256;
  static final int EncodingQualityLevel0 = -32;
  static final int EncodingXCursor = -240;
  static final int EncodingRichCursor = -239;
  static final int EncodingPointerPos = -232;
  static final int EncodingLastRect = -224;
  static final int EncodingNewFBSize = -223;
  static final String SigEncodingRaw = "RAW_____";
  static final String SigEncodingCopyRect = "COPYRECT";
  static final String SigEncodingRRE = "RRE_____";
  static final String SigEncodingCoRRE = "CORRE___";
  static final String SigEncodingHextile = "HEXTILE_";
  static final String SigEncodingZlib = "ZLIB____";
  static final String SigEncodingTight = "TIGHT___";
  static final String SigEncodingZRLE = "ZRLE____";
  static final String SigEncodingCompressLevel0 = "COMPRLVL";
  static final String SigEncodingQualityLevel0 = "JPEGQLVL";
  static final String SigEncodingXCursor = "X11CURSR";
  static final String SigEncodingRichCursor = "RCHCURSR";
  static final String SigEncodingPointerPos = "POINTPOS";
  static final String SigEncodingLastRect = "LASTRECT";
  static final String SigEncodingNewFBSize = "NEWFBSIZ";
  static final int MaxNormalEncoding = 255;
  static final int HextileRaw = 1;
  static final int HextileBackgroundSpecified = 2;
  static final int HextileForegroundSpecified = 4;
  static final int HextileAnySubrects = 8;
  static final int HextileSubrectsColoured = 16;
  static final int TightMinToCompress = 12;
  static final int TightExplicitFilter = 4;
  static final int TightFill = 8;
  static final int TightJpeg = 9;
  static final int TightMaxSubencoding = 9;
  static final int TightFilterCopy = 0;
  static final int TightFilterPalette = 1;
  static final int TightFilterGradient = 2;
  String host;
  int port;
  Socket sock;
  OutputStream os;
  SessionRecorder rec;
  boolean inNormalProtocol = false;
  VncViewer viewer;
  private DataInputStream is;
  private long numBytesRead = 0L;

  boolean brokenKeyPressed = false;

  boolean wereZlibUpdates = false;

  boolean recordFromBeginning = true;
  boolean zlibWarningShown;
  boolean tightWarningShown;
  int numUpdatesInSession;
  boolean timing;
  long timeWaitedIn100us;
  long timedKbits;
  int serverMajor;
  int serverMinor;
  int clientMajor;
  int clientMinor;
  boolean protocolTightVNC;
  CapsContainer tunnelCaps;
  CapsContainer authCaps;
  CapsContainer serverMsgCaps;
  CapsContainer clientMsgCaps;
  CapsContainer encodingCaps;
  private boolean closed;
  String desktopName;
  public int framebufferWidth;
  public int framebufferHeight;
  int bitsPerPixel;
  int depth;
  boolean bigEndian;
  boolean trueColour;
  int redMax;
  int greenMax;
  int blueMax;
  int redShift;
  int greenShift;
  int blueShift;
  int updateNRects;
  int updateRectX;
  int updateRectY;
  int updateRectW;
  int updateRectH;
  int updateRectEncoding;
  int copyRectSrcX;
  int copyRectSrcY;
  byte[] eventBuf = new byte[72];
  int eventBufLen;
  static final int CTRL_MASK = 2;
  static final int SHIFT_MASK = 1;
  static final int META_MASK = 4;
  static final int ALT_MASK = 8;
  int pointerMask = 0;

  int oldModifiers = 0;

  public long getNumBytesRead()
  {
    return this.numBytesRead;
  }

  RfbProto(String paramString, int paramInt, VncViewer paramVncViewer)
    throws IOException
  {
    this.viewer = paramVncViewer;
    this.host = paramString;
    this.port = paramInt;

	if (this.viewer.socketFactory == null){
    	this.sock = new Socket(this.host, this.port);
    } else {
    	try {
	        Class localClass = Class.forName(this.viewer.socketFactory);
	        SocketFactory localSocketFactory = (SocketFactory)localClass.newInstance();
	        if (this.viewer.inAnApplet){
	        	this.sock = localSocketFactory.createSocket(this.host, this.port, this.viewer);
	        }
	        else{
	        	this.sock = localSocketFactory.createSocket(this.host, this.port, this.viewer.mainArgs);
	        }
    	} catch (Exception localException) {
	        localException.printStackTrace();
	        throw new IOException(localException.getMessage());
    	}
	}
    this.is = new DataInputStream(new BufferedInputStream(this.sock.getInputStream(), 16384));
    this.os = this.sock.getOutputStream();
    this.timing = false;
    this.timeWaitedIn100us = 5L;
    this.timedKbits = 0L;
  }

  synchronized void close()
  {
	try {
		this.sock.close();
		this.closed = true;
		System.out.println("RFB socket closed");
		if (this.rec != null) {
			this.rec.close();
			this.rec = null;
		}
	} catch (Exception localException) {
		localException.printStackTrace();
	}
  }

  synchronized boolean closed() {
    return this.closed;
  }

  void readVersionMsg() throws Exception
  {
    byte[] arrayOfByte = new byte[12];

    readFully(arrayOfByte);

    if ((arrayOfByte[0] != 82) || (arrayOfByte[1] != 70) || (arrayOfByte[2] != 66) || (arrayOfByte[3] != 32) || (arrayOfByte[4] < 48) || (arrayOfByte[4] > 57) || (arrayOfByte[5] < 48) || (arrayOfByte[5] > 57) || (arrayOfByte[6] < 48) || (arrayOfByte[6] > 57) || (arrayOfByte[7] != 46) || (arrayOfByte[8] < 48) || (arrayOfByte[8] > 57) || (arrayOfByte[9] < 48) || (arrayOfByte[9] > 57) || (arrayOfByte[10] < 48) || (arrayOfByte[10] > 57) || (arrayOfByte[11] != 10))
    {
      throw new Exception("Host " + this.host + " port " + this.port + " is not an RFB server");
    }

    this.serverMajor = ((arrayOfByte[4] - 48) * 100 + (arrayOfByte[5] - 48) * 10 + (arrayOfByte[6] - 48));
    this.serverMinor = ((arrayOfByte[8] - 48) * 100 + (arrayOfByte[9] - 48) * 10 + (arrayOfByte[10] - 48));

    if (this.serverMajor < 3)
      throw new Exception("RFB server does not support protocol version 3");
  }

  void writeVersionMsg()
    throws IOException
  {
    this.clientMajor = 3;
    if ((this.serverMajor > 3) || (this.serverMinor >= 8)) {
      this.clientMinor = 8;
      this.os.write("RFB 003.008\n".getBytes());
    } else if (this.serverMinor >= 7) {
      this.clientMinor = 7;
      this.os.write("RFB 003.007\n".getBytes());
    } else {
      this.clientMinor = 3;
      this.os.write("RFB 003.003\n".getBytes());
    }
    this.protocolTightVNC = false;
    initCapabilities();
  }

  int negotiateSecurity()
    throws Exception
  {
    return this.clientMinor >= 7 ? selectSecurityType() : readSecurityType();
  }

  int readSecurityType()
    throws Exception
  {
    int i = readU32();

    switch (i) {
    case 0:
      readConnFailedReason();
      return 0;
    case 1:
    case 2:
      return i;
    }
    throw new Exception("Unknown security type from RFB server: " + i);
  }

  int selectSecurityType()
    throws Exception
  {
    int i = 0;

    int j = readU8();
    if (j == 0) {
      readConnFailedReason();
      return 0;
    }
    byte[] arrayOfByte = new byte[j];
    readFully(arrayOfByte);

    for (int k = 0; k < j; k++) {
      if (arrayOfByte[k] == 16) {
        this.protocolTightVNC = true;
        this.os.write(16);
        return 16;
      }

    }
    int k;
    for (k = 0; k < j; k++) {
      if ((arrayOfByte[k] == 1) || (arrayOfByte[k] == 2)) {
        i = arrayOfByte[k];
        break;
      }
    }

    if (i == 0) {
      throw new Exception("Server did not offer supported security type");
    }
    this.os.write(i);

    return i;
  }

  void authenticateNone()
    throws Exception
  {
    if (this.clientMinor >= 8)
      readSecurityResult("No authentication");
  }

  void authenticateVNC(String paramString)
    throws Exception
  {
    byte[] arrayOfByte1 = new byte[16];
    readFully(arrayOfByte1);

    if (paramString.length() > 8) {
      paramString = paramString.substring(0, 8);
    }

    int i = paramString.indexOf(0);
    if (i != -1) {
      paramString = paramString.substring(0, i);
    }
    byte[] arrayOfByte2 = { 0, 0, 0, 0, 0, 0, 0, 0 };
    System.arraycopy(paramString.getBytes(), 0, arrayOfByte2, 0, paramString.length());

    DesCipher localDesCipher = new DesCipher(arrayOfByte2);

    localDesCipher.encrypt(arrayOfByte1, 0, arrayOfByte1, 0);
    localDesCipher.encrypt(arrayOfByte1, 8, arrayOfByte1, 8);

    this.os.write(arrayOfByte1);

    //�˴��������
    readSecurityResult("VNC authentication");
  }

  void readSecurityResult(String paramString)
    throws Exception
  {
    int i = readU32();

    switch (i) {
    case 0:
      System.out.println(paramString + ": success");
      break;
    case 1:
      if (this.clientMinor >= 8)
        readConnFailedReason();
      throw new Exception(paramString + ": failed");
    case 2:
      throw new Exception(paramString + ": failed, too many tries");
    default:
      throw new Exception(paramString + ": unknown result " + i);
    }
  }

  void readConnFailedReason()
    throws Exception
  {
    int i = readU32();
    byte[] arrayOfByte = new byte[i];
    readFully(arrayOfByte);
    throw new Exception(new String(arrayOfByte));
  }

  void initCapabilities()
  {
    this.tunnelCaps = new CapsContainer();
    this.authCaps = new CapsContainer();
    this.serverMsgCaps = new CapsContainer();
    this.clientMsgCaps = new CapsContainer();
    this.encodingCaps = new CapsContainer();

    this.authCaps.add(1, "STDV", "NOAUTH__", "No authentication");

    this.authCaps.add(2, "STDV", "VNCAUTH_", "Standard VNC password authentication");

    this.encodingCaps.add(1, "STDV", "COPYRECT", "Standard CopyRect encoding");

    this.encodingCaps.add(2, "STDV", "RRE_____", "Standard RRE encoding");

    this.encodingCaps.add(4, "STDV", "CORRE___", "Standard CoRRE encoding");

    this.encodingCaps.add(5, "STDV", "HEXTILE_", "Standard Hextile encoding");

    this.encodingCaps.add(16, "STDV", "ZRLE____", "Standard ZRLE encoding");

    this.encodingCaps.add(6, "TRDV", "ZLIB____", "Zlib encoding");

    this.encodingCaps.add(7, "TGHT", "TIGHT___", "Tight encoding");

    this.encodingCaps.add(-256, "TGHT", "COMPRLVL", "Compression level");

    this.encodingCaps.add(-32, "TGHT", "JPEGQLVL", "JPEG quality level");

    this.encodingCaps.add(-240, "TGHT", "X11CURSR", "X-style cursor shape update");

    this.encodingCaps.add(-239, "TGHT", "RCHCURSR", "Rich-color cursor shape update");

    this.encodingCaps.add(-232, "TGHT", "POINTPOS", "Pointer position update");

    this.encodingCaps.add(-224, "TGHT", "LASTRECT", "LastRect protocol extension");

    this.encodingCaps.add(-223, "TGHT", "NEWFBSIZ", "Framebuffer size change");
  }

  void setupTunneling()
    throws IOException
  {
    int i = readU32();
    if (i != 0) {
      readCapabilityList(this.tunnelCaps, i);

      writeInt(0);
    }
  }

  int negotiateAuthenticationTight()
    throws Exception
  {
    int i = readU32();
    if (i == 0) {
      return 1;
    }
    readCapabilityList(this.authCaps, i);
    for (int j = 0; j < this.authCaps.numEnabled(); j++) {
      int k = this.authCaps.getByOrder(j);
      if ((k == 1) || (k == 2)) {
        writeInt(k);
        return k;
      }
    }
    throw new Exception("No suitable authentication scheme found");
  }

  void readCapabilityList(CapsContainer paramCapsContainer, int paramInt)
    throws IOException
  {
    byte[] arrayOfByte1 = new byte[4];
    byte[] arrayOfByte2 = new byte[8];
    for (int j = 0; j < paramInt; j++) {
      int i = readU32();
      readFully(arrayOfByte1);
      readFully(arrayOfByte2);
      paramCapsContainer.enable(new CapabilityInfo(i, arrayOfByte1, arrayOfByte2));
    }
  }

  void writeInt(int paramInt)
    throws IOException
  {
    byte[] arrayOfByte = new byte[4];
    arrayOfByte[0] = (byte)(paramInt >> 24 & 0xFF);
    arrayOfByte[1] = (byte)(paramInt >> 16 & 0xFF);
    arrayOfByte[2] = (byte)(paramInt >> 8 & 0xFF);
    arrayOfByte[3] = (byte)(paramInt & 0xFF);
    this.os.write(arrayOfByte);
  }

  void writeClientInit()
    throws IOException
  {
    if (this.viewer.options.shareDesktop)
      this.os.write(1);
    else {
      this.os.write(0);
    }
    this.viewer.options.disableShareDesktop();
  }

	void readServerInit() throws IOException {
	  	this.framebufferWidth = readU16();
	    this.framebufferHeight = readU16();
	    this.bitsPerPixel = readU8();
	    this.depth = readU8();
	    this.bigEndian = (readU8() != 0);
	    this.trueColour = (readU8() != 0);
	    this.redMax = readU16();
	    this.greenMax = readU16();
	    this.blueMax = readU16();
	    this.redShift = readU8();
	    this.greenShift = readU8();
	    this.blueShift = readU8();
	    byte[] arrayOfByte1 = new byte[3];
	    readFully(arrayOfByte1);
	    int i = readU32();
	    byte[] arrayOfByte2 = new byte[i];
	    readFully(arrayOfByte2);
	    this.desktopName = new String(arrayOfByte2);
	
	    if (this.protocolTightVNC) {
	      int j = readU16();
	      int k = readU16();
	      int m = readU16();
	      readU16();
	      readCapabilityList(this.serverMsgCaps, j);
	      readCapabilityList(this.clientMsgCaps, k);
	      readCapabilityList(this.encodingCaps, m);
	    }
	
	    this.inNormalProtocol = true;
  }

  void startSession(String paramString)
    throws IOException
  {
    this.rec = new SessionRecorder(paramString);
    this.rec.writeHeader();
    this.rec.write("RFB 003.003\n".getBytes());
    this.rec.writeIntBE(1);
    this.rec.writeShortBE(this.framebufferWidth);
    this.rec.writeShortBE(this.framebufferHeight);
    byte[] arrayOfByte = { 32, 24, 0, 1, 0, -1, 0, -1, 0, -1, 16, 8, 0, 0, 0, 0 };

    this.rec.write(arrayOfByte);
    this.rec.writeIntBE(this.desktopName.length());
    this.rec.write(this.desktopName.getBytes());
    this.numUpdatesInSession = 0;

    if (this.wereZlibUpdates) {
      this.recordFromBeginning = false;
    }
    this.zlibWarningShown = false;
    this.tightWarningShown = false;
  }

  void closeSession()
    throws IOException
  {
    if (this.rec != null) {
      this.rec.close();
      this.rec = null;
    }
  }

  void setFramebufferSize(int paramInt1, int paramInt2)
  {
    this.framebufferWidth = paramInt1;
    this.framebufferHeight = paramInt2;
  }

  int readServerMessageType()
    throws IOException
  {
    int i = readU8();

    if ((this.rec != null) && 
      (i == 2)) {
      this.rec.writeByte(i);
      if (this.numUpdatesInSession > 0) {
        this.rec.flush();
      }
    }

    return i;
  }

  void readFramebufferUpdate()
    throws IOException
  {
    skipBytes(1);
    this.updateNRects = readU16();

    if (this.rec != null) {
      this.rec.writeByte(0);
      this.rec.writeByte(0);
      this.rec.writeShortBE(this.updateNRects);
    }

    this.numUpdatesInSession += 1;
  }

  void readFramebufferUpdateRectHdr()
    throws Exception
  {
    this.updateRectX = readU16();
    this.updateRectY = readU16();
    this.updateRectW = readU16();
    this.updateRectH = readU16();
    this.updateRectEncoding = readU32();

    if ((this.updateRectEncoding == 6) || (this.updateRectEncoding == 16) || (this.updateRectEncoding == 7))
    {
      this.wereZlibUpdates = true;
    }

    if (this.rec != null) {
      if (this.numUpdatesInSession > 1)
        this.rec.flush();
      this.rec.writeShortBE(this.updateRectX);
      this.rec.writeShortBE(this.updateRectY);
      this.rec.writeShortBE(this.updateRectW);
      this.rec.writeShortBE(this.updateRectH);
      if ((this.updateRectEncoding == 6) && (!this.recordFromBeginning))
      {
        if (!this.zlibWarningShown) {
          System.out.println("Warning: Raw encoding will be used instead of Zlib in recorded session.");

          this.zlibWarningShown = true;
        }
        this.rec.writeIntBE(0);
      } else {
        this.rec.writeIntBE(this.updateRectEncoding);
        if ((this.updateRectEncoding == 7) && (!this.recordFromBeginning) && (!this.tightWarningShown))
        {
          System.out.println("Warning: Re-compressing Tight-encoded updates for session recording.");

          this.tightWarningShown = true;
        }
      }
    }

    if ((this.updateRectEncoding < 0) || (this.updateRectEncoding > 255)) {
      return;
    }
    if ((this.updateRectX + this.updateRectW > this.framebufferWidth) || (this.updateRectY + this.updateRectH > this.framebufferHeight))
    {
      throw new Exception("Framebuffer update rectangle too large: " + this.updateRectW + "x" + this.updateRectH + " at (" + this.updateRectX + "," + this.updateRectY + ")");
    }
  }

  void readCopyRect()
    throws IOException
  {
    this.copyRectSrcX = readU16();
    this.copyRectSrcY = readU16();

    if (this.rec != null) {
      this.rec.writeShortBE(this.copyRectSrcX);
      this.rec.writeShortBE(this.copyRectSrcY);
    }
  }

  String readServerCutText()
    throws IOException
  {
    skipBytes(3);
    int i = readU32();
    byte[] arrayOfByte = new byte[i];
    readFully(arrayOfByte);
    return new String(arrayOfByte);
  }

  int readCompactLen()
    throws IOException
  {
    int[] arrayOfInt = new int[3];
    arrayOfInt[0] = readU8();
    int i = 1;
    int j = arrayOfInt[0] & 0x7F;
    if ((arrayOfInt[0] & 0x80) != 0) {
      arrayOfInt[1] = readU8();
      i++;
      j |= (arrayOfInt[1] & 0x7F) << 7;
      if ((arrayOfInt[1] & 0x80) != 0) {
        arrayOfInt[2] = readU8();
        i++;
        j |= (arrayOfInt[2] & 0xFF) << 14;
      }
    }

    if ((this.rec != null) && (this.recordFromBeginning)) {
      for (int k = 0; k < i; k++)
        this.rec.writeByte(arrayOfInt[k]);
    }
    return j;
  }

  public void writeFramebufferUpdateRequest(int paramInt1, int paramInt2, int paramInt3, int paramInt4, boolean paramBoolean)
    throws IOException
  {
    byte[] arrayOfByte = new byte[10];

    arrayOfByte[0] = 3;
    arrayOfByte[1] = (byte)(paramBoolean ? 1 : 0);
    arrayOfByte[2] = (byte)(paramInt1 >> 8 & 0xFF);
    arrayOfByte[3] = (byte)(paramInt1 & 0xFF);
    arrayOfByte[4] = (byte)(paramInt2 >> 8 & 0xFF);
    arrayOfByte[5] = (byte)(paramInt2 & 0xFF);
    arrayOfByte[6] = (byte)(paramInt3 >> 8 & 0xFF);
    arrayOfByte[7] = (byte)(paramInt3 & 0xFF);
    arrayOfByte[8] = (byte)(paramInt4 >> 8 & 0xFF);
    arrayOfByte[9] = (byte)(paramInt4 & 0xFF);

    this.os.write(arrayOfByte);
  }

  void writeSetPixelFormat(int paramInt1, int paramInt2, boolean paramBoolean1, boolean paramBoolean2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, int paramInt8)
    throws IOException
  {
    byte[] arrayOfByte = new byte[20];

    arrayOfByte[0] = 0;
    arrayOfByte[4] = (byte)paramInt1;
    arrayOfByte[5] = (byte)paramInt2;
    arrayOfByte[6] = (byte)(paramBoolean1 ? 1 : 0);
    arrayOfByte[7] = (byte)(paramBoolean2 ? 1 : 0);
    arrayOfByte[8] = (byte)(paramInt3 >> 8 & 0xFF);
    arrayOfByte[9] = (byte)(paramInt3 & 0xFF);
    arrayOfByte[10] = (byte)(paramInt4 >> 8 & 0xFF);
    arrayOfByte[11] = (byte)(paramInt4 & 0xFF);
    arrayOfByte[12] = (byte)(paramInt5 >> 8 & 0xFF);
    arrayOfByte[13] = (byte)(paramInt5 & 0xFF);
    arrayOfByte[14] = (byte)paramInt6;
    arrayOfByte[15] = (byte)paramInt7;
    arrayOfByte[16] = (byte)paramInt8;

    this.os.write(arrayOfByte);
  }

  void writeFixColourMapEntries(int paramInt1, int paramInt2, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int[] paramArrayOfInt3)
    throws IOException
  {
    byte[] arrayOfByte = new byte[6 + paramInt2 * 6];

    arrayOfByte[0] = 1;
    arrayOfByte[2] = (byte)(paramInt1 >> 8 & 0xFF);
    arrayOfByte[3] = (byte)(paramInt1 & 0xFF);
    arrayOfByte[4] = (byte)(paramInt2 >> 8 & 0xFF);
    arrayOfByte[5] = (byte)(paramInt2 & 0xFF);

    for (int i = 0; i < paramInt2; i++) {
      arrayOfByte[(6 + i * 6)] = (byte)(paramArrayOfInt1[i] >> 8 & 0xFF);
      arrayOfByte[(6 + i * 6 + 1)] = (byte)(paramArrayOfInt1[i] & 0xFF);
      arrayOfByte[(6 + i * 6 + 2)] = (byte)(paramArrayOfInt2[i] >> 8 & 0xFF);
      arrayOfByte[(6 + i * 6 + 3)] = (byte)(paramArrayOfInt2[i] & 0xFF);
      arrayOfByte[(6 + i * 6 + 4)] = (byte)(paramArrayOfInt3[i] >> 8 & 0xFF);
      arrayOfByte[(6 + i * 6 + 5)] = (byte)(paramArrayOfInt3[i] & 0xFF);
    }

    this.os.write(arrayOfByte);
  }

  void writeSetEncodings(int[] paramArrayOfInt, int paramInt)
    throws IOException
  {
    byte[] arrayOfByte = new byte[4 + 4 * paramInt];

    arrayOfByte[0] = 2;
    arrayOfByte[2] = (byte)(paramInt >> 8 & 0xFF);
    arrayOfByte[3] = (byte)(paramInt & 0xFF);

    for (int i = 0; i < paramInt; i++) {
      arrayOfByte[(4 + 4 * i)] = (byte)(paramArrayOfInt[i] >> 24 & 0xFF);
      arrayOfByte[(5 + 4 * i)] = (byte)(paramArrayOfInt[i] >> 16 & 0xFF);
      arrayOfByte[(6 + 4 * i)] = (byte)(paramArrayOfInt[i] >> 8 & 0xFF);
      arrayOfByte[(7 + 4 * i)] = (byte)(paramArrayOfInt[i] & 0xFF);
    }

    this.os.write(arrayOfByte);
  }

  void writeClientCutText(String paramString)
    throws IOException
  {
    byte[] arrayOfByte = new byte[8 + paramString.length()];

    arrayOfByte[0] = 6;
    arrayOfByte[4] = (byte)(paramString.length() >> 24 & 0xFF);
    arrayOfByte[5] = (byte)(paramString.length() >> 16 & 0xFF);
    arrayOfByte[6] = (byte)(paramString.length() >> 8 & 0xFF);
    arrayOfByte[7] = (byte)(paramString.length() & 0xFF);

    System.arraycopy(paramString.getBytes(), 0, arrayOfByte, 8, paramString.length());

    this.os.write(arrayOfByte);
  }

  void writePointerEvent(MouseEvent paramMouseEvent)
    throws IOException
  {
    int i = paramMouseEvent.getModifiers();

    int j = 2;
    int k = 4;
    if (this.viewer.options.reverseMouseButtons2And3) {
      j = 4;
      k = 2;
    }

    if (paramMouseEvent.getID() == 501) {
      if ((i & 0x8) != 0) {
        this.pointerMask = j;
        i &= -9;
      } else if ((i & 0x4) != 0) {
        this.pointerMask = k;
        i &= -5;
      } else {
        this.pointerMask = 1;
      }
    } else if (paramMouseEvent.getID() == 502) {
      this.pointerMask = 0;
      if ((i & 0x8) != 0)
        i &= -9;
      else if ((i & 0x4) != 0) {
        i &= -5;
      }
    }

    this.eventBufLen = 0;
    writeModifierKeyEvents(i);

    int m = paramMouseEvent.getX();
    int n = paramMouseEvent.getY();

    if (m < 0) m = 0;
    if (n < 0) n = 0;

    this.eventBuf[(this.eventBufLen++)] = 5;
    this.eventBuf[(this.eventBufLen++)] = (byte)this.pointerMask;
    this.eventBuf[(this.eventBufLen++)] = (byte)(m >> 8 & 0xFF);
    this.eventBuf[(this.eventBufLen++)] = (byte)(m & 0xFF);
    this.eventBuf[(this.eventBufLen++)] = (byte)(n >> 8 & 0xFF);
    this.eventBuf[(this.eventBufLen++)] = (byte)(n & 0xFF);

    if (this.pointerMask == 0) {
      writeModifierKeyEvents(0);
    }

    this.os.write(this.eventBuf, 0, this.eventBufLen);
  }

  public void writeKeyEvent(KeyEvent paramKeyEvent)
    throws IOException
  {
    int i = paramKeyEvent.getKeyChar();
    if (i == 0) {
      i = 65535;
    }
    if (i == 65535) {
     int j = paramKeyEvent.getKeyCode();
      if ((j == 17) || (j == 16) || (j == 157) || (j == 18))
      {
        return;
      }

    }

    int j = paramKeyEvent.getID() == 401 ? 1 : 0;
    int k;
    if (paramKeyEvent.isActionKey())
    {
      switch (paramKeyEvent.getKeyCode()) { case 36:
        k = 65360; break;
      case 37:
        k = 65361; break;
      case 38:
        k = 65362; break;
      case 39:
        k = 65363; break;
      case 40:
        k = 65364; break;
      case 33:
        k = 65365; break;
      case 34:
        k = 65366; break;
      case 35:
        k = 65367; break;
      case 155:
        k = 65379; break;
      case 112:
        k = 65470; break;
      case 113:
        k = 65471; break;
      case 114:
        k = 65472; break;
      case 115:
        k = 65473; break;
      case 116:
        k = 65474; break;
      case 117:
        k = 65475; break;
      case 118:
        k = 65476; break;
      case 119:
        k = 65477; break;
      case 120:
        k = 65478; break;
      case 121:
        k = 65479; break;
      case 122:
        k = 65480; break;
      case 123:
        k = 65481; break;
      default:
        return;
      }

    }
    else
    {
      k = i;

      if (k < 32) {
        if (paramKeyEvent.isControlDown())
          k += 96;
        else
          switch (k) { case 8:
            k = 65288; break;
          case 9:
            k = 65289; break;
          case 10:
            k = 65293; break;
          case 27:
            k = 65307;
          }
      }
      else if (k == 127)
      {
        k = 65535;
      } else if (k > 255)
      {
        if (((k < 65280) || (k > 65535)) && ((k < 8352) || (k > 8367)))
        {
          return;
        }
      }
    }

    if ((k == 229) || (k == 197) || (k == 228) || (k == 196) || (k == 246) || (k == 214) || (k == 167) || (k == 189) || (k == 163))
    {
      if (j != 0) {
        this.brokenKeyPressed = true;
      }
      if ((j == 0) && (!this.brokenKeyPressed))
      {
        this.eventBufLen = 0;
        writeModifierKeyEvents(paramKeyEvent.getModifiers());
        writeKeyEvent(k, true);
        this.os.write(this.eventBuf, 0, this.eventBufLen);
      }

      if (j == 0) {
        this.brokenKeyPressed = false;
      }
    }
    this.eventBufLen = 0;
    writeModifierKeyEvents(paramKeyEvent.getModifiers());
    writeKeyEvent(k, j!=0);

    if (j == 0) {
      writeModifierKeyEvents(0);
    }
    this.os.write(this.eventBuf, 0, this.eventBufLen);
  }

  void writeKeyEvent(int paramInt, boolean paramBoolean)
  {
    this.eventBuf[(this.eventBufLen++)] = 4;
    this.eventBuf[(this.eventBufLen++)] = (byte)(paramBoolean ? 1 : 0);
    this.eventBuf[(this.eventBufLen++)] = 0;
    this.eventBuf[(this.eventBufLen++)] = 0;
    this.eventBuf[(this.eventBufLen++)] = (byte)(paramInt >> 24 & 0xFF);
    this.eventBuf[(this.eventBufLen++)] = (byte)(paramInt >> 16 & 0xFF);
    this.eventBuf[(this.eventBufLen++)] = (byte)(paramInt >> 8 & 0xFF);
    this.eventBuf[(this.eventBufLen++)] = (byte)(paramInt & 0xFF);
  }

  void writeModifierKeyEvents(int paramInt)
  {
    if ((paramInt & 0x2) != (this.oldModifiers & 0x2)) {
      writeKeyEvent(65507, (paramInt & 0x2) != 0);
    }
    if ((paramInt & 0x1) != (this.oldModifiers & 0x1)) {
      writeKeyEvent(65505, (paramInt & 0x1) != 0);
    }
    if ((paramInt & 0x4) != (this.oldModifiers & 0x4)) {
      writeKeyEvent(65511, (paramInt & 0x4) != 0);
    }
    if ((paramInt & 0x8) != (this.oldModifiers & 0x8)) {
      writeKeyEvent(65513, (paramInt & 0x8) != 0);
    }
    this.oldModifiers = paramInt;
  }

  void recordCompressedData(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    Deflater localDeflater = new Deflater();
    localDeflater.setInput(paramArrayOfByte, paramInt1, paramInt2);
    int i = paramInt2 + paramInt2 / 100 + 12;
    byte[] arrayOfByte = new byte[i];
    localDeflater.finish();
    int j = localDeflater.deflate(arrayOfByte);
    recordCompactLen(j);
    this.rec.write(arrayOfByte, 0, j);
  }

  void recordCompressedData(byte[] paramArrayOfByte) throws IOException {
    recordCompressedData(paramArrayOfByte, 0, paramArrayOfByte.length);
  }

  void recordCompactLen(int paramInt)
    throws IOException
  {
    byte[] arrayOfByte = new byte[3];
    int i = 0;
    arrayOfByte[(i++)] = (byte)(paramInt & 0x7F);
    if (paramInt > 127)
    {
      int tmp27_26 = (i - 1);
      byte[] tmp27_23 = arrayOfByte; tmp27_23[tmp27_26] = (byte)(tmp27_23[tmp27_26] | 0x80);
      arrayOfByte[(i++)] = (byte)(paramInt >> 7 & 0x7F);
      if (paramInt > 16383)
      {
        int tmp60_59 = (i - 1);
        byte[] tmp60_56 = arrayOfByte; tmp60_56[tmp60_59] = (byte)(tmp60_56[tmp60_59] | 0x80);
        arrayOfByte[(i++)] = (byte)(paramInt >> 14 & 0xFF);
      }
    }
    this.rec.write(arrayOfByte, 0, i);
  }

  public void startTiming() {
    this.timing = true;

    if (this.timeWaitedIn100us > 10000L) {
      this.timedKbits = (this.timedKbits * 10000L / this.timeWaitedIn100us);
      this.timeWaitedIn100us = 10000L;
    }
  }

  public void stopTiming() {
    this.timing = false;
    if (this.timeWaitedIn100us < this.timedKbits / 2L)
      this.timeWaitedIn100us = (this.timedKbits / 2L);
  }

  public long kbitsPerSecond() {
    return this.timedKbits * 10000L / this.timeWaitedIn100us;
  }

  public long timeWaited() {
    return this.timeWaitedIn100us;
  }

  public void readFully(byte[] paramArrayOfByte)
    throws IOException
  {
    readFully(paramArrayOfByte, 0, paramArrayOfByte.length);
  }

  public void readFully(byte[] paramArrayOfByte, int paramInt1, int paramInt2) throws IOException {
    long l1 = 0L;
    if (this.timing) {
      l1 = System.currentTimeMillis();
    }
    this.is.readFully(paramArrayOfByte, paramInt1, paramInt2);

    if (this.timing) {
      long l2 = System.currentTimeMillis();
      long l3 = (l2 - l1) * 10L;
      int i = paramInt2 * 8 / 1000;

      if (l3 > i * 1000) l3 = i * 1000;
      if (l3 < i / 4) l3 = i / 4;

      this.timeWaitedIn100us += l3;
      this.timedKbits += i;
    }

    this.numBytesRead += paramInt2;
  }

  final int available() throws IOException {
    return this.is.available();
  }

  final int skipBytes(int paramInt)
    throws IOException
  {
    int i = this.is.skipBytes(paramInt);
    this.numBytesRead += i;
    return i;
  }

  final int readU8() throws IOException {
    int i = this.is.readUnsignedByte();
    this.numBytesRead += 1L;
    return i;
  }

	final int readU16() throws IOException {
	    int i = this.is.readUnsignedShort();
	    this.numBytesRead += 2L;
	    return i;
	}

  final int readU32() throws IOException {
    int i = this.is.readInt();
    this.numBytesRead += 4L;
    return i;
  }
}