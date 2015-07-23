package vncviewer;

import java.applet.Applet;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

class HTTPConnectSocketFactory
  implements SocketFactory
{
  public Socket createSocket(String paramString, int paramInt, Applet paramApplet)
    throws IOException
  {
    return createSocket(paramString, paramInt, paramApplet.getParameter("PROXYHOST1"), paramApplet.getParameter("PROXYPORT1"));
  }

  public Socket createSocket(String paramString, int paramInt, String[] paramArrayOfString)
    throws IOException
  {
    return createSocket(paramString, paramInt, readArg(paramArrayOfString, "PROXYHOST1"), readArg(paramArrayOfString, "PROXYPORT1"));
  }

  public Socket createSocket(String paramString1, int paramInt, String paramString2, String paramString3)
    throws IOException
  {
    int i = 0;
    if (paramString3 != null)
      try {
        i = Integer.parseInt(paramString3);
      }
      catch (NumberFormatException localNumberFormatException) {
      }
    if ((paramString2 == null) || (i == 0)) {
      System.out.println("Incomplete parameter list for HTTPConnectSocket");
      return new Socket(paramString1, paramInt);
    }

    System.out.println("HTTP CONNECT via proxy " + paramString2 + " port " + i);

    HTTPConnectSocket localHTTPConnectSocket = new HTTPConnectSocket(paramString1, paramInt, paramString2, i);

    return localHTTPConnectSocket;
  }

  private String readArg(String[] paramArrayOfString, String paramString)
  {
    for (int i = 0; i < paramArrayOfString.length; i += 2) {
      if (!paramArrayOfString[i].equalsIgnoreCase(paramString)) continue;
      try {
        return paramArrayOfString[(i + 1)];
      } catch (Exception localException) {
        return null;
      }
    }

    return null;
  }
}