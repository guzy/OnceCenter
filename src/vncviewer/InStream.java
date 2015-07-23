package vncviewer;

public abstract class InStream
{
  public static int maxStringLength = 65535;
  protected byte[] b;
  protected int ptr;
  protected int end;

  public final int check(int paramInt1, int paramInt2)
    throws Exception
  {
    if (this.ptr + paramInt1 * paramInt2 > this.end) {
      if (this.ptr + paramInt1 > this.end) {
        return overrun(paramInt1, paramInt2);
      }
      paramInt2 = (this.end - this.ptr) / paramInt1;
    }
    return paramInt2;
  }

  public final void check(int paramInt) throws Exception {
    if (this.ptr + paramInt > this.end)
      overrun(paramInt, 1);
  }

  public final int readS8()
    throws Exception
  {
    check(1); return this.b[(this.ptr++)];
  }

  public final int readS16() throws Exception {
    check(2); int i = this.b[(this.ptr++)];
    int j = this.b[(this.ptr++)] & 0xFF; return i << 8 | j;
  }

  public final int readS32() throws Exception {
    check(4); int i = this.b[(this.ptr++)];
    int j = this.b[(this.ptr++)] & 0xFF;
    int k = this.b[(this.ptr++)] & 0xFF;
    int m = this.b[(this.ptr++)] & 0xFF;
    return i << 24 | j << 16 | k << 8 | m;
  }

  public final int readU8() throws Exception {
    return readS8() & 0xFF;
  }

  public final int readU16() throws Exception {
    return readS16() & 0xFFFF;
  }

  public final int readU32() throws Exception {
    return readS32() & 0xFFFFFFFF;
  }

  public final String readString()
    throws Exception
  {
    int i = readU32();
    if (i > maxStringLength) {
      throw new Exception("InStream max string length exceeded");
    }
    char[] arrayOfChar = new char[i];
    int j = 0;
    while (j < i) {
      int k = j + check(1, i - j);
      while (j < k) {
        arrayOfChar[(j++)] = (char)this.b[(this.ptr++)];
      }
    }

    return new String(arrayOfChar);
  }

  public final void skip(int paramInt)
    throws Exception
  {
    while (paramInt > 0) {
      int i = check(1, paramInt);
      this.ptr += i;
      paramInt -= i;
    }
  }

  public void readBytes(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws Exception
  {
    int i = paramInt1 + paramInt2;
    while (paramInt1 < i) {
      int j = check(1, i - paramInt1);
      System.arraycopy(this.b, this.ptr, paramArrayOfByte, paramInt1, j);
      this.ptr += j;
      paramInt1 += j;
    }
  }

  public final int readOpaque8()
    throws Exception
  {
    return readU8();
  }

  public final int readOpaque16() throws Exception {
    return readU16();
  }

  public final int readOpaque32() throws Exception {
    return readU32();
  }

  public final int readOpaque24A() throws Exception {
    check(3); int i = this.b[(this.ptr++)];
    int j = this.b[(this.ptr++)]; int k = this.b[(this.ptr++)];
    return i << 24 | j << 16 | k << 8;
  }

  public final int readOpaque24B() throws Exception {
    check(3); int i = this.b[(this.ptr++)];
    int j = this.b[(this.ptr++)]; int k = this.b[(this.ptr++)];
    return i << 16 | j << 8 | k;
  }

  public abstract int pos();

  public boolean bytesAvailable()
  {
    return this.end != this.ptr;
  }

  public final byte[] getbuf()
  {
    return this.b; } 
  public final int getptr() { return this.ptr; } 
  public final int getend() { return this.end; } 
  public final void setptr(int paramInt) { this.ptr = paramInt;
  }

  protected abstract int overrun(int paramInt1, int paramInt2)
    throws Exception;
}