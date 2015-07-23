package vncviewer;

public class MemInStream extends InStream
{
  public MemInStream(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    this.b = paramArrayOfByte;
    this.ptr = paramInt1;
    this.end = (paramInt1 + paramInt2);
  }
  public int pos() {
    return this.ptr;
  }
  protected int overrun(int paramInt1, int paramInt2) throws Exception {
    throw new Exception("MemInStream overrun: end of stream");
  }
}