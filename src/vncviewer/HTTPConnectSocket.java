package vncviewer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

class HTTPConnectSocket extends Socket
{
  public HTTPConnectSocket(String paramString1, int paramInt1, String paramString2, int paramInt2)
    throws IOException
  {
    super(paramString2, paramInt2);

    getOutputStream().write(("CONNECT " + paramString1 + ":" + paramInt1 + " HTTP/1.0\r\n\r\n").getBytes());

    DataInputStream localDataInputStream = new DataInputStream(getInputStream());
    String str = localDataInputStream.readLine();

    if (!str.startsWith("HTTP/1.0 200 ")) {
      if (str.startsWith("HTTP/1.0 "))
        str = str.substring(9);
      throw new IOException("Proxy reports \"" + str + "\"");
    }

    do
    {
      str = localDataInputStream.readLine();
    }while (str.length() != 0);
  }
}