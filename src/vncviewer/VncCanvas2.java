package vncviewer;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.lang.reflect.Method;

class VncCanvas2 extends VncCanvas
{
  public VncCanvas2(VncViewer paramVncViewer)
    throws IOException
  {
    super(paramVncViewer);
    disableFocusTraversalKeys();
  }

  public VncCanvas2(VncViewer paramVncViewer, int paramInt1, int paramInt2)
    throws IOException
  {
    super(paramVncViewer, paramInt1, paramInt2);
    disableFocusTraversalKeys();
  }

  public void paintScaledFrameBuffer(Graphics paramGraphics) {
    Graphics2D localGraphics2D = (Graphics2D)paramGraphics;
    localGraphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

    localGraphics2D.drawImage(this.memImage, 0, 0, this.scaledWidth, this.scaledHeight, null);
  }

  private void disableFocusTraversalKeys()
  {
    try
    {
      Class[] arrayOfClass = { Boolean.TYPE };
      Method localMethod = getClass().getMethod("setFocusTraversalKeysEnabled", arrayOfClass);

      Object[] arrayOfObject = { new Boolean(false) };
      localMethod.invoke(this, arrayOfObject);
    }
    catch (Exception localException)
    {
    }
  }
}