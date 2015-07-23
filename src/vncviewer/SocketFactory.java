package vncviewer;

import java.applet.Applet;
import java.io.IOException;
import java.net.Socket;

public abstract interface SocketFactory
{
  public abstract Socket createSocket(String paramString, int paramInt, Applet paramApplet)
    throws IOException;

  public abstract Socket createSocket(String paramString, int paramInt, String[] paramArrayOfString)
    throws IOException;
}