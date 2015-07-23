package vncviewer;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;

public class VncViewer extends Applet implements Runnable, WindowListener {
	static final String DEFAULT_PASSWORD = "onceas";
	public boolean inAnApplet = false;
	public boolean inSeparateFrame = true;
	public String[] mainArgs;
	public RfbProto rfb;
	public Thread rfbThread;
	public Frame vncFrame;
	public Container vncContainer;
	ScrollPane desktopScrollPane;
	GridBagLayout gridbag;
	ButtonPanel buttonPanel;
	Label connStatusLabel;
	VncCanvas vc;
	public OptionsFrame options;
	public ClipboardFrame clipboard;
	public RecordingFrame rec;
	public Object recordingSync;
	public String sessionFileName;
	public boolean recordingActive;
	public boolean recordingStatusChanged;
	public String cursorUpdatesDef;
	public String eightBitColorsDef;
	String socketFactory;
	String host;
	int port;
	String passwordParam;
	public boolean showControls;
	boolean offerRelogin;
	boolean showOfflineDesktop;
	int deferScreenUpdates;
	int deferCursorUpdates;
	int deferUpdateRequests;
	int debugStatsExcludeUpdates;
	int debugStatsMeasureUpdates;
	public static Applet refApplet;
	int[] encodingsSaved;
	int nEncodingsSaved;
	
	public boolean isFinished=false;

//	public static void main(String[] paramArrayOfString) {
//		VncViewer localVncViewer = new VncViewer();
//		localVncViewer.mainArgs = paramArrayOfString;
//		localVncViewer.inAnApplet = false;
//		localVncViewer.inSeparateFrame = true;
//
//		localVncViewer.init();
//		localVncViewer.start();
//	}

	public void init() {
		readParameters();
		refApplet = this;
		System.out.println("this.inSeparateFrame = " + this.inSeparateFrame);
		if (this.inSeparateFrame) {
			this.vncFrame = new Frame("TightVNC");
			System.out.println("this.inAnApplet = " + this.inAnApplet);
			if (!this.inAnApplet) {
				this.vncFrame.add("Center", this);
			}
			this.vncContainer = this.vncFrame;
		} else {
			this.vncContainer = this;
		}

		this.recordingSync = new Object();

		this.options = new OptionsFrame(this);
		this.clipboard = new ClipboardFrame(this);
		if (RecordingFrame.checkSecurity()) {
			this.rec = new RecordingFrame(this);
		}
		this.sessionFileName = null;
		this.recordingActive = false;
		this.recordingStatusChanged = false;
		this.cursorUpdatesDef = null;
		this.eightBitColorsDef = null;

		if (this.inSeparateFrame) {
			this.vncFrame.addWindowListener(this);
		}
		this.rfbThread = new Thread(this);
		this.rfbThread.start();
	}

	public void update(Graphics paramGraphics) {
	}

	public void run() {
		this.gridbag = new GridBagLayout();
		// this.vncContainer.setLayout(this.gridbag);
		GridBagConstraints localGridBagConstraints = new GridBagConstraints();
		try {
			connectAndAuthenticate();
			doProtocolInitialisation();
			Object localObject;
			if ((this.options.autoScale) && (this.inSeparateFrame)) {
				try {
					localObject = this.vncContainer.getToolkit()
							.getScreenSize();
				} catch (Exception localException2) {
					localObject = new Dimension(0, 0);
				}
				createCanvas(this.rfb.framebufferWidth,
						this.rfb.framebufferHeight);
			} else {
				createCanvas(0, 0);
			}

			if (this.inSeparateFrame) {
				localObject = new Panel();
				((Container) localObject).setLayout(this.gridbag);
				this.gridbag.setConstraints(this.vc, localGridBagConstraints);
				((Container) localObject).add(this.vc);

				this.desktopScrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
				
				this.desktopScrollPane.add((Component) localObject);
//				this.desktopScrollPane.setBackground(Color.green);
				this.vncFrame.add(this.desktopScrollPane);
				this.vncFrame.setTitle(this.rfb.desktopName);
				this.vncFrame.pack();
				this.vc.resizeDesktopFrame();
			} else {
				this.gridbag.setConstraints(this.vc, localGridBagConstraints);
				add(this.vc);
				validate();
			}

			if (this.showControls) {
				this.buttonPanel.enableButtons();
			}
			
			isFinished=true;
			
			moveFocusToDesktop();
			processNormalProtocol();
			
		} catch (NoRouteToHostException localNoRouteToHostException) {
			fatalError("Network error: no route to server: " + this.host,
					localNoRouteToHostException);
		} catch (UnknownHostException localUnknownHostException) {
			fatalError("Network error: server name unknown: " + this.host,
					localUnknownHostException);
		} catch (ConnectException localConnectException) {
			fatalError("Network error: could not connect to server: "
					+ this.host + ":" + this.port, localConnectException);
		} catch (EOFException localEOFException) {
			if (this.showOfflineDesktop) {
				localEOFException.printStackTrace();
				System.out
						.println("Network error: remote side closed connection");
				if (this.vc != null) {
					this.vc.enableInput(false);
				}
				if (this.inSeparateFrame) {
					this.vncFrame.setTitle(this.rfb.desktopName
							+ " [disconnected]");
				}
				if ((this.rfb != null) && (!this.rfb.closed()))
					this.rfb.close();
				if ((this.showControls) && (this.buttonPanel != null)) {
					this.buttonPanel.disableButtonsOnDisconnect();
					if (this.inSeparateFrame)
						this.vncFrame.pack();
					else
						validate();
				}
			} else {
				fatalError("Network error: remote side closed connection",
						localEOFException);
			}
		} catch (IOException localIOException) {
			String str = localIOException.getMessage();
			if ((str != null) && (str.length() != 0))
				fatalError("Network Error: " + str, localIOException);
			else
				fatalError(localIOException.toString(), localIOException);
		} catch (Exception localException1) {
			String str = localException1.getMessage();
			if ((str != null) && (str.length() != 0))
				fatalError("Error: " + str, localException1);
			else
				fatalError(localException1.toString(), localException1);
		}
	}

	void createCanvas(int paramInt1, int paramInt2) throws IOException {
		this.vc = null;
		try {
			Class localClass = Class.forName("java.awt.Graphics2D");

			localClass = Class.forName("VncCanvas2");
			Class[] arrayOfClass = { getClass(), Integer.TYPE, Integer.TYPE };
			Constructor localConstructor = localClass
					.getConstructor(arrayOfClass);
			Object[] arrayOfObject = { this, new Integer(paramInt1),
					new Integer(paramInt2) };

			this.vc = ((VncCanvas) localConstructor.newInstance(arrayOfObject));
		} catch (Exception localException) {
			System.out.println("Warning: Java 2D API is not available");
		}

		if (this.vc == null)
			this.vc = new VncCanvas(this, paramInt1, paramInt2);
	}

	void processNormalProtocol() throws Exception {
		try {
			this.vc.processNormalProtocol();
		} catch (Exception localException) {
			if (this.rfbThread == null) {
				System.out.println("Ignoring RFB socket exceptions because applet is stopping");
			} else
				throw localException;
		}
	}

	void connectAndAuthenticate() throws Exception {
		showConnectionStatus("Initializing...");
		if (this.inSeparateFrame) {
			this.vncFrame.pack();
			this.vncFrame.show();
		} else {
			validate();
		}

//		showConnectionStatus("Connecting to " + this.host + ", port " + this.port + "...");

		this.rfb = new RfbProto(this.host, this.port, this);
//		showConnectionStatus("Connected to server");

		this.rfb.readVersionMsg();
		showConnectionStatus("RFB server supports protocol version "
				+ this.rfb.serverMajor + "." + this.rfb.serverMinor);

		this.rfb.writeVersionMsg();
		showConnectionStatus("Using RFB protocol version "
				+ this.rfb.clientMajor + "." + this.rfb.clientMinor);

		int i = this.rfb.negotiateSecurity();
		int j;
		if (i == 16) {
			showConnectionStatus("Enabling TightVNC protocol extensions");
			this.rfb.setupTunneling();
			j = this.rfb.negotiateAuthenticationTight();
		} else {
			j = i;
		}

		switch (j) {
		case 1:
			showConnectionStatus("No authentication needed");
			this.rfb.authenticateNone();
			break;
		case 2:
			showConnectionStatus("Performing standard VNC authentication");
			// if (this.passwordParam != null) {
			// this.rfb.authenticateVNC(this.passwordParam);
			// } else {
			// String str = askPassword();
			// this.rfb.authenticateVNC(str);
			// }

			// changed by lishun
			if (this.passwordParam == null)
				this.passwordParam = DEFAULT_PASSWORD;
			try {
				this.rfb.authenticateVNC(this.passwordParam);
				break;
			} catch (Exception e) {
				this.passwordParam = askPassword();
				connectAndAuthenticate();
			}

			break;
		default:
			throw new Exception("Unknown authentication scheme " + j);
		}
	}

	void showConnectionStatus(String paramString) {
		if (paramString == null) {
			if (this.vncContainer.isAncestorOf(this.connStatusLabel)) {
				this.vncContainer.remove(this.connStatusLabel);
			}
			return;
		}

		System.out.println(paramString);

		if (this.connStatusLabel == null) {
			this.connStatusLabel = new Label("Status: " + paramString);
			this.connStatusLabel.setFont(new Font("Helvetica", 0, 12));
		} else {
			this.connStatusLabel.setText("Status: " + paramString);
		}

		if (!this.vncContainer.isAncestorOf(this.connStatusLabel)) {
			GridBagConstraints localGridBagConstraints = new GridBagConstraints();
			localGridBagConstraints.gridwidth = 0;
			localGridBagConstraints.fill = 2;
			localGridBagConstraints.anchor = 18;
			localGridBagConstraints.weightx = 1.0D;
			localGridBagConstraints.weighty = 1.0D;
			localGridBagConstraints.insets = new Insets(20, 30, 20, 30);
			this.gridbag.setConstraints(this.connStatusLabel,
					localGridBagConstraints);
			this.vncContainer.add(this.connStatusLabel);
		}

		if (this.inSeparateFrame)
			this.vncFrame.pack();
		else
			validate();
	}

	String askPassword() throws Exception {
		showConnectionStatus(null);

		AuthPanel localAuthPanel = new AuthPanel(this);
		GridBagConstraints localGridBagConstraints = new GridBagConstraints();
		localGridBagConstraints.gridwidth = 0;
		localGridBagConstraints.anchor = 18;
		localGridBagConstraints.weightx = 1.0D;
		localGridBagConstraints.weighty = 1.0D;
		localGridBagConstraints.ipadx = 100;
		localGridBagConstraints.ipady = 50;
		this.gridbag.setConstraints(localAuthPanel, localGridBagConstraints);
		this.vncContainer.add(localAuthPanel);

		this.vncContainer.setBackground(Color.darkGray);
		if (this.inSeparateFrame)
			this.vncFrame.pack();
		else {
			validate();
		}

		localAuthPanel.moveFocusToDefaultField();
		String str = localAuthPanel.getPassword();
		this.vncContainer.remove(localAuthPanel);

		return str;
	}

	void doProtocolInitialisation() throws IOException {
		this.rfb.writeClientInit();
		this.rfb.readServerInit();
//		
		setEncodings();
		showConnectionStatus(null);
	}

	void setEncodings() {
		setEncodings(false);
	}

	void autoSelectEncodings() {
		setEncodings(true);
	}

	void setEncodings(boolean paramBoolean) {
		if ((this.options == null) || (this.rfb == null)
				|| (!this.rfb.inNormalProtocol)) {
			return;
		}
		int i = this.options.preferredEncoding;
		if (i == -1) {
			long l = this.rfb.kbitsPerSecond();
			if (this.nEncodingsSaved < 1) {
				System.out.println("Using Tight/ZRLE encodings");
				i = 7;
			} else if ((l > 2000L) && (this.encodingsSaved[0] != 5)) {
				System.out.println("Throughput " + l
						+ " kbit/s - changing to Hextile encoding");

				i = 5;
			} else if ((l < 1000L) && (this.encodingsSaved[0] != 7)) {
				System.out.println("Throughput " + l
						+ " kbit/s - changing to Tight/ZRLE encodings");

				i = 7;
			} else {
				if (paramBoolean)
					return;
				i = this.encodingsSaved[0];
			}

		} else if (paramBoolean) {
			return;
		}

		int[] arrayOfInt = new int[20];
		int j = 0;

		arrayOfInt[(j++)] = i;
		if (this.options.useCopyRect) {
			arrayOfInt[(j++)] = 1;
		}

		if (i != 7) {
			arrayOfInt[(j++)] = 7;
		}
		if (i != 16) {
			arrayOfInt[(j++)] = 16;
		}
		if (i != 5) {
			arrayOfInt[(j++)] = 5;
		}
		if (i != 6) {
			arrayOfInt[(j++)] = 6;
		}
		if (i != 4) {
			arrayOfInt[(j++)] = 4;
		}
		if (i != 2) {
			arrayOfInt[(j++)] = 2;
		}

		if ((this.options.compressLevel >= 0)
				&& (this.options.compressLevel <= 9)) {
			arrayOfInt[(j++)] = (-256 + this.options.compressLevel);
		}

		if ((this.options.jpegQuality >= 0) && (this.options.jpegQuality <= 9)) {
			arrayOfInt[(j++)] = (-32 + this.options.jpegQuality);
		}

		if (this.options.requestCursorUpdates) {
			arrayOfInt[(j++)] = -240;
			arrayOfInt[(j++)] = -239;
			if (!this.options.ignoreCursorUpdates) {
				arrayOfInt[(j++)] = -232;
			}
		}
		arrayOfInt[(j++)] = -224;
		arrayOfInt[(j++)] = -223;

		int k = 0;
		if (j != this.nEncodingsSaved)
			k = 1;
		else {
			for (int m = 0; m < j; m++) {
				if (arrayOfInt[m] != this.encodingsSaved[m]) {
					k = 1;
					break;
				}
			}
		}

		if (k != 0) {
			try {
				this.rfb.writeSetEncodings(arrayOfInt, j);
				if (this.vc != null)
					this.vc.softCursorFree();
			} catch (Exception localException) {
				localException.printStackTrace();
			}
			this.encodingsSaved = arrayOfInt;
			this.nEncodingsSaved = j;
		}
	}

	void setCutText(String paramString) {
		try {
			if ((this.rfb != null) && (this.rfb.inNormalProtocol))
				this.rfb.writeClientCutText(paramString);
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	void setRecordingStatus(String paramString) {
		synchronized (this.recordingSync) {
			this.sessionFileName = paramString;
			this.recordingStatusChanged = true;
		}
	}

	boolean checkRecordingStatus() throws IOException {
		synchronized (this.recordingSync) {
			if (this.recordingStatusChanged) {
				this.recordingStatusChanged = false;
				if (this.sessionFileName != null) {
					startRecording();
					return true;
				}
				stopRecording();
			}
		}

		return false;
	}

	protected void startRecording() throws IOException {
		synchronized (this.recordingSync) {
			if (!this.recordingActive) {
				this.options.getClass();
				this.cursorUpdatesDef = this.options.choices[3]
						.getSelectedItem();

				this.options.getClass();
				this.eightBitColorsDef = this.options.choices[5]
						.getSelectedItem();

				this.options.getClass();
				this.options.choices[3].select("Disable");
				this.options.getClass();
				this.options.choices[3].setEnabled(false);
				this.options.setEncodings();
				this.options.getClass();
				this.options.choices[5].select("No");
				this.options.getClass();
				this.options.choices[5].setEnabled(false);
				this.options.setColorFormat();
			} else {
				this.rfb.closeSession();
			}

			System.out.println("Recording the session in "
					+ this.sessionFileName);
			this.rfb.startSession(this.sessionFileName);
			this.recordingActive = true;
		}
	}

	protected void stopRecording() throws IOException {
		synchronized (this.recordingSync) {
			if (this.recordingActive) {
				this.options.getClass();
				this.options.choices[3].select(this.cursorUpdatesDef);
				this.options.getClass();
				this.options.choices[3].setEnabled(true);
				this.options.setEncodings();
				this.options.getClass();
				this.options.choices[5].select(this.eightBitColorsDef);
				this.options.getClass();
				this.options.choices[5].setEnabled(true);
				this.options.setColorFormat();

				this.rfb.closeSession();
				System.out.println("Session recording stopped.");
			}
			this.sessionFileName = null;
			this.recordingActive = false;
		}
	}

	public void readParameters() {
		this.host = readParameter("HOST", !this.inAnApplet);
		if (this.host == null) {
			this.host = getCodeBase().getHost();
			if (this.host.equals("")) {
				fatalError("HOST parameter not specified");
			}
		}

		this.port = readIntParameter("PORT", 5900);

		readPasswordParameters();

		if (this.inAnApplet) {
			String str = readParameter("Open New Window", false);
			if ((str != null) && (str.equalsIgnoreCase("Yes"))) {
				this.inSeparateFrame = true;
			}
		}

		this.showControls = false;
		String str = readParameter("Show Controls", false);
		if ((str != null) && (str.equalsIgnoreCase("No"))) {
			this.showControls = false;
		}

		this.offerRelogin = true;
		str = readParameter("Offer Relogin", false);
		if ((str != null) && (str.equalsIgnoreCase("No"))) {
			this.offerRelogin = false;
		}

		this.showOfflineDesktop = false;
		str = readParameter("Show Offline Desktop", false);
		if ((str != null) && (str.equalsIgnoreCase("Yes"))) {
			this.showOfflineDesktop = true;
		}

		this.deferScreenUpdates = readIntParameter("Defer screen updates", 20);
		this.deferCursorUpdates = readIntParameter("Defer cursor updates", 10);
		this.deferUpdateRequests = readIntParameter("Defer update requests", 0);

		this.debugStatsExcludeUpdates = readIntParameter("DEBUG_XU", 0);
		this.debugStatsMeasureUpdates = readIntParameter("DEBUG_CU", 0);

		this.socketFactory = readParameter("SocketFactory", false);
	}

	private void readPasswordParameters() {
		String str = readParameter("ENCPASSWORD", false);
		if (str == null) {
			this.passwordParam = readParameter("PASSWORD", false);
		} else {
			byte[] arrayOfByte1 = { 0, 0, 0, 0, 0, 0, 0, 0 };
			int i = str.length() / 2;
			if (i > 8)
				i = 8;
			for (int j = 0; j < i; j++) {
				String localObject = str.substring(j * 2, j * 2 + 2);
				Integer localInteger = new Integer(Integer.parseInt(
						(String) localObject, 16));
				arrayOfByte1[j] = localInteger.byteValue();
			}

			byte[] arrayOfByte2 = { 23, 82, 107, 6, 35, 78, 88, 7 };
			Object localObject = new DesCipher(arrayOfByte2);
			((DesCipher) localObject).decrypt(arrayOfByte1, 0, arrayOfByte1, 0);
			this.passwordParam = new String(arrayOfByte1);
		}
		// added by lishun
		// this.passwordParam = "oncea";
	}

	public String readParameter(String paramString, boolean paramBoolean) {
		if (this.inAnApplet) {
			String str = getParameter(paramString);
			if ((str == null) && (paramBoolean)) {
				fatalError(paramString + " parameter not specified");
			}
			return str;
		}

		for (int i = 0; i < this.mainArgs.length; i += 2) {
			if (!this.mainArgs[i].equalsIgnoreCase(paramString))
				continue;
			try {
				return this.mainArgs[(i + 1)];
			} catch (Exception localException) {
				if (paramBoolean) {
					fatalError(paramString + " parameter not specified");
				}
				return null;
			}
		}

		if (paramBoolean) {
			fatalError(paramString + " parameter not specified");
		}
		return null;
	}

	int readIntParameter(String paramString, int paramInt) {
		String str = readParameter(paramString, false);
		int i = paramInt;
		if (str != null)
			try {
				i = Integer.parseInt(str);
			} catch (NumberFormatException localNumberFormatException) {
			}
		return i;
	}

	void moveFocusToDesktop() {
		if ((this.vncContainer != null) && (this.vc != null)
				&& (this.vncContainer.isAncestorOf(this.vc)))
			this.vc.requestFocus();
	}

	public synchronized void disconnect() {
		System.out.println("Disconnecting");

		if (this.vc != null) {
			double d1 = (System.currentTimeMillis() - this.vc.statStartTime) / 1000.0D;
			double d2 = Math.round(this.vc.statNumUpdates / d1 * 100.0D) / 100.0D;
			long i = this.vc.statNumPixelRects;
			long j = this.vc.statNumTotalRects - this.vc.statNumPixelRects;
			System.out.println("Updates received: " + this.vc.statNumUpdates
					+ " (" + i + " rectangles + " + j + " pseudo), " + d2
					+ " updates/sec");

			long k = i - this.vc.statNumRectsTight - this.vc.statNumRectsZRLE
					- this.vc.statNumRectsHextile - this.vc.statNumRectsRaw
					- this.vc.statNumRectsCopy;

			System.out.println("Rectangles: Tight=" + this.vc.statNumRectsTight
					+ "(JPEG=" + this.vc.statNumRectsTightJPEG + ") ZRLE="
					+ this.vc.statNumRectsZRLE + " Hextile="
					+ this.vc.statNumRectsHextile + " Raw="
					+ this.vc.statNumRectsRaw + " CopyRect="
					+ this.vc.statNumRectsCopy + " other=" + k);

			long m = this.vc.statNumBytesDecoded;
			long n = this.vc.statNumBytesEncoded;
			if (n > 0) {
				double d3 = Math.round(m / n * 1000.0D) / 1000.0D;
				System.out.println("Pixel data: " + this.vc.statNumBytesDecoded
						+ " bytes, " + this.vc.statNumBytesEncoded
						+ " compressed, ratio " + d3);
			}

		}

		if ((this.rfb != null) && (!this.rfb.closed()))
			this.rfb.close();
		if (this.options != null)
			this.options.dispose();
		if (this.clipboard != null)
			this.clipboard.dispose();
		if (this.rec != null) {
			this.rec.dispose();
		}
//		if (vncFrame != null)
//			vncFrame.dispose();
		// if (this.inAnApplet)
		// showMessage("Disconnected");
		// else
		// System.exit(0);
	}

	public synchronized void fatalError(String paramString) {
		System.out.println(paramString);

		if (this.inAnApplet) {
			Thread.currentThread().stop();
		} else
			System.exit(1);
	}

	public synchronized void fatalError(String paramString,
			Exception paramException) {
		if ((this.rfb != null) && (this.rfb.closed())) {
			System.out.println("RFB thread finished");
			return;
		}

		System.out.println(paramString);
		paramException.printStackTrace();

		// if (this.rfb != null) {
		// this.rfb.close();
		// }
		// if (this.inAnApplet)
		// showMessage(paramString);
		// else
		// System.exit(1);
	}

	void showMessage(String paramString) {
		this.vncContainer.removeAll();

		Label localLabel = new Label(paramString, 1);
		localLabel.setFont(new Font("Helvetica", 0, 12));

		if (this.offerRelogin) {
			Panel localPanel1 = new Panel(new GridLayout(0, 1));
			Panel localPanel2 = new Panel(new FlowLayout(0));
			localPanel2.add(localPanel1);
			this.vncContainer.setLayout(new FlowLayout(0, 30, 16));
			this.vncContainer.add(localPanel2);
			Panel localPanel3 = new Panel(new FlowLayout(1));
			localPanel3.add(localLabel);
			localPanel1.add(localPanel3);
			localPanel1.add(new ReloginPanel(this));
		} else {
			this.vncContainer.setLayout(new FlowLayout(0, 30, 30));
			this.vncContainer.add(localLabel);
		}

		if (this.inSeparateFrame)
			this.vncFrame.pack();
		else
			validate();
	}

	public void stop() {
		System.out.println("Stopping applet");
		this.rfbThread = null;
	}

	public void destroy() {
		System.out.println("Destroying applet");

		this.vncContainer.removeAll();
		this.options.dispose();
		this.clipboard.dispose();
		if (this.rec != null)
			this.rec.dispose();
		if ((this.rfb != null) && (!this.rfb.closed()))
			this.rfb.close();
		if (this.inSeparateFrame)
			this.vncFrame.dispose();
	}

	public void enableInput(boolean paramBoolean) {
		this.vc.enableInput(paramBoolean);
	}

	public void windowClosing(WindowEvent paramWindowEvent) {
		System.out.println("Closing window");
		if (this.rfb != null) {
			disconnect();
		}
		this.vncContainer.hide();

		if (!this.inAnApplet)
			System.exit(0);
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
}