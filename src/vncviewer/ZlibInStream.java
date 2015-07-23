package vncviewer;

import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class ZlibInStream extends InStream
{
  static final int defaultBufSize = 16384;
  private InStream underlying;
  private int bufSize;
  private int ptrOffset;
  private Inflater inflater;
  private int bytesIn;

  public ZlibInStream(int paramInt)
  {
    this.bufSize = paramInt;
    this.b = new byte[this.bufSize];
    this.ptr = (this.end = this.ptrOffset = 0);
    this.inflater = new Inflater();
  }
  public ZlibInStream() {
    this(16384);
  }
  public void setUnderlying(InStream paramInStream, int paramInt) {
    this.underlying = paramInStream;
    this.bytesIn = paramInt;
    this.ptr = (this.end = 0);
  }

  public void reset() throws Exception {
    this.ptr = (this.end = 0);
    if (this.underlying == null) return;

    while (this.bytesIn > 0) {
      decompress();
      this.end = 0;
    }
    this.underlying = null;
  }
  public int pos() {
    return this.ptrOffset + this.ptr;
  }
  protected int overrun(int paramInt1, int paramInt2) throws Exception {
    if (paramInt1 > this.bufSize)
      throw new Exception("ZlibInStream overrun: max itemSize exceeded");
    if (this.underlying == null) {
      throw new Exception("ZlibInStream overrun: no underlying stream");
    }
    if (this.end - this.ptr != 0) {
      System.arraycopy(this.b, this.ptr, this.b, 0, this.end - this.ptr);
    }
    this.ptrOffset += this.ptr;
    this.end -= this.ptr;
    this.ptr = 0;

    while (this.end < paramInt1) {
      decompress();
    }

    if (paramInt1 * paramInt2 > this.end) {
      paramInt2 = this.end / paramInt1;
    }
    return paramInt2;
  }

  private void decompress()
    throws Exception
  {
    try
    {
      this.underlying.check(1);
      int i = this.underlying.getend() - this.underlying.getptr();
      if (i > this.bytesIn) {
        i = this.bytesIn;
      }
      if (this.inflater.needsInput()) {
        this.inflater.setInput(this.underlying.getbuf(), this.underlying.getptr(), i);
      }

      int j = this.inflater.inflate(this.b, this.end, this.bufSize - this.end);

      this.end += j;
      if (this.inflater.needsInput()) {
        this.bytesIn -= i;
        this.underlying.setptr(this.underlying.getptr() + i);
      }
    } catch (DataFormatException localDataFormatException) {
      throw new Exception("ZlibInStream: inflate failed");
    }
  }
}