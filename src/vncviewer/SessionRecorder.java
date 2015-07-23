package vncviewer;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

class SessionRecorder
{
  protected FileOutputStream f;
  protected DataOutputStream df;
  protected long startTime;
  protected long lastTimeOffset;
  protected byte[] buffer;
  protected int bufferSize;
  protected int bufferBytes;

  public SessionRecorder(String paramString, int paramInt)
    throws IOException
  {
    this.f = new FileOutputStream(paramString);
    this.df = new DataOutputStream(this.f);
    this.startTime = System.currentTimeMillis();
    this.lastTimeOffset = 0L;

    this.bufferSize = paramInt;
    this.bufferBytes = 0;
    this.buffer = new byte[this.bufferSize];
  }

  public SessionRecorder(String paramString) throws IOException {
    this(paramString, 65536);
  }

  public void close()
    throws IOException
  {
    try
    {
      flush();
    }
    catch (IOException localIOException) {
    }
    this.df = null;
    this.f.close();
    this.f = null;
    this.buffer = null;
  }

  public void writeHeader()
    throws IOException
  {
    this.df.write("FBS 001.000\n".getBytes());
  }

  public void writeByte(int paramInt)
    throws IOException
  {
    prepareWriting();
    this.buffer[(this.bufferBytes++)] = (byte)paramInt;
  }

  public void writeShortBE(int paramInt)
    throws IOException
  {
    prepareWriting();
    this.buffer[(this.bufferBytes++)] = (byte)(paramInt >> 8);
    this.buffer[(this.bufferBytes++)] = (byte)paramInt;
  }

  public void writeIntBE(int paramInt)
    throws IOException
  {
    prepareWriting();
    this.buffer[this.bufferBytes] = (byte)(paramInt >> 24);
    this.buffer[(this.bufferBytes + 1)] = (byte)(paramInt >> 16);
    this.buffer[(this.bufferBytes + 2)] = (byte)(paramInt >> 8);
    this.buffer[(this.bufferBytes + 3)] = (byte)paramInt;
    this.bufferBytes += 4;
  }

  public void writeShortLE(int paramInt)
    throws IOException
  {
    prepareWriting();
    this.buffer[(this.bufferBytes++)] = (byte)paramInt;
    this.buffer[(this.bufferBytes++)] = (byte)(paramInt >> 8);
  }

  public void writeIntLE(int paramInt)
    throws IOException
  {
    prepareWriting();
    this.buffer[this.bufferBytes] = (byte)paramInt;
    this.buffer[(this.bufferBytes + 1)] = (byte)(paramInt >> 8);
    this.buffer[(this.bufferBytes + 2)] = (byte)(paramInt >> 16);
    this.buffer[(this.bufferBytes + 3)] = (byte)(paramInt >> 24);
    this.bufferBytes += 4;
  }

  public void write(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    prepareWriting();
    while (paramInt2 > 0) {
      if (this.bufferBytes > this.bufferSize - 4)
        flush(false);
      int i;
      if (this.bufferBytes + paramInt2 > this.bufferSize)
        i = this.bufferSize - this.bufferBytes;
      else {
        i = paramInt2;
      }
      System.arraycopy(paramArrayOfByte, paramInt1, this.buffer, this.bufferBytes, i);
      this.bufferBytes += i;
      paramInt1 += i;
      paramInt2 -= i;
    }
  }

  public void write(byte[] paramArrayOfByte) throws IOException {
    write(paramArrayOfByte, 0, paramArrayOfByte.length);
  }

  public void flush(boolean paramBoolean)
    throws IOException
  {
    if (this.bufferBytes > 0) {
      this.df.writeInt(this.bufferBytes);
      this.df.write(this.buffer, 0, this.bufferBytes + 3 & 0x7FFFFFFC);
      this.df.writeInt((int)this.lastTimeOffset);
      this.bufferBytes = 0;
      if (paramBoolean)
        this.lastTimeOffset = -1L;
    }
  }

  public void flush() throws IOException {
    flush(true);
  }

  protected void prepareWriting()
    throws IOException
  {
    if (this.lastTimeOffset == -1L)
      this.lastTimeOffset = (System.currentTimeMillis() - this.startTime);
    if (this.bufferBytes > this.bufferSize - 4)
      flush(false);
  }
}