package vncviewer;

public class DesCipher
{
  private int[] encryptKeys = new int[32];
  private int[] decryptKeys = new int[32];

  private int[] tempInts = new int[2];

  private static byte[] bytebit = { 1, 2, 4, 8, 16, 32, 64, -128 };

  private static int[] bigbyte = { 8388608, 4194304, 2097152, 1048576, 524288, 262144, 131072, 65536, 32768, 16384, 8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1 };

  private static byte[] pc1 = { 56, 48, 40, 32, 24, 16, 8, 0, 57, 49, 41, 33, 25, 17, 9, 1, 58, 50, 42, 34, 26, 18, 10, 2, 59, 51, 43, 35, 62, 54, 46, 38, 30, 22, 14, 6, 61, 53, 45, 37, 29, 21, 13, 5, 60, 52, 44, 36, 28, 20, 12, 4, 27, 19, 11, 3 };

  private static int[] totrot = { 1, 2, 4, 6, 8, 10, 12, 14, 15, 17, 19, 21, 23, 25, 27, 28 };

  private static byte[] pc2 = { 13, 16, 10, 23, 0, 4, 2, 27, 14, 5, 20, 9, 22, 18, 11, 3, 25, 7, 15, 6, 26, 19, 12, 1, 40, 51, 30, 36, 46, 54, 29, 39, 50, 44, 32, 47, 43, 48, 38, 55, 33, 52, 45, 41, 49, 35, 28, 31 };

  private static int[] SP1 = { 16843776, 0, 65536, 16843780, 16842756, 66564, 4, 65536, 1024, 16843776, 16843780, 1024, 16778244, 16842756, 16777216, 4, 1028, 16778240, 16778240, 66560, 66560, 16842752, 16842752, 16778244, 65540, 16777220, 16777220, 65540, 0, 1028, 66564, 16777216, 65536, 16843780, 4, 16842752, 16843776, 16777216, 16777216, 1024, 16842756, 65536, 66560, 16777220, 1024, 4, 16778244, 66564, 16843780, 65540, 16842752, 16778244, 16777220, 1028, 66564, 16843776, 1028, 16778240, 16778240, 0, 65540, 66560, 0, 16842756 };

  private static int[] SP2 = { -2146402272, -2147450880, 32768, 1081376, 1048576, 32, -2146435040, -2147450848, -2147483616, -2146402272, -2146402304, -2147483648, -2147450880, 1048576, 32, -2146435040, 1081344, 1048608, -2147450848, 0, -2147483648, 32768, 1081376, -2146435072, 1048608, -2147483616, 0, 1081344, 32800, -2146402304, -2146435072, 32800, 0, 1081376, -2146435040, 1048576, -2147450848, -2146435072, -2146402304, 32768, -2146435072, -2147450880, 32, -2146402272, 1081376, 32, 32768, -2147483648, 32800, -2146402304, 1048576, -2147483616, 1048608, -2147450848, -2147483616, 1048608, 1081344, 0, -2147450880, 32800, -2147483648, -2146435040, -2146402272, 1081344 };

  private static int[] SP3 = { 520, 134349312, 0, 134348808, 134218240, 0, 131592, 134218240, 131080, 134217736, 134217736, 131072, 134349320, 131080, 134348800, 520, 134217728, 8, 134349312, 512, 131584, 134348800, 134348808, 131592, 134218248, 131584, 131072, 134218248, 8, 134349320, 512, 134217728, 134349312, 134217728, 131080, 520, 131072, 134349312, 134218240, 0, 512, 131080, 134349320, 134218240, 134217736, 512, 0, 134348808, 134218248, 131072, 134217728, 134349320, 8, 131592, 131584, 134217736, 134348800, 134218248, 520, 134348800, 131592, 8, 134348808, 131584 };

  private static int[] SP4 = { 8396801, 8321, 8321, 128, 8396928, 8388737, 8388609, 8193, 0, 8396800, 8396800, 8396929, 129, 0, 8388736, 8388609, 1, 8192, 8388608, 8396801, 128, 8388608, 8193, 8320, 8388737, 1, 8320, 8388736, 8192, 8396928, 8396929, 129, 8388736, 8388609, 8396800, 8396929, 129, 0, 0, 8396800, 8320, 8388736, 8388737, 1, 8396801, 8321, 8321, 128, 8396929, 129, 1, 8192, 8388609, 8193, 8396928, 8388737, 8193, 8320, 8388608, 8396801, 128, 8388608, 8192, 8396928 };

  private static int[] SP5 = { 256, 34078976, 34078720, 1107296512, 524288, 256, 1073741824, 34078720, 1074266368, 524288, 33554688, 1074266368, 1107296512, 1107820544, 524544, 1073741824, 33554432, 1074266112, 1074266112, 0, 1073742080, 1107820800, 1107820800, 33554688, 1107820544, 1073742080, 0, 1107296256, 34078976, 33554432, 1107296256, 524544, 524288, 1107296512, 256, 33554432, 1073741824, 34078720, 1107296512, 1074266368, 33554688, 1073741824, 1107820544, 34078976, 1074266368, 256, 33554432, 1107820544, 1107820800, 524544, 1107296256, 1107820800, 34078720, 0, 1074266112, 1107296256, 524544, 33554688, 1073742080, 524288, 0, 1074266112, 34078976, 1073742080 };

  private static int[] SP6 = { 536870928, 541065216, 16384, 541081616, 541065216, 16, 541081616, 4194304, 536887296, 4210704, 4194304, 536870928, 4194320, 536887296, 536870912, 16400, 0, 4194320, 536887312, 16384, 4210688, 536887312, 16, 541065232, 541065232, 0, 4210704, 541081600, 16400, 4210688, 541081600, 536870912, 536887296, 16, 541065232, 4210688, 541081616, 4194304, 16400, 536870928, 4194304, 536887296, 536870912, 16400, 536870928, 541081616, 4210688, 541065216, 4210704, 541081600, 0, 541065232, 16, 16384, 541065216, 4210704, 16384, 4194320, 536887312, 0, 541081600, 536870912, 4194320, 536887312 };

  private static int[] SP7 = { 2097152, 69206018, 67110914, 0, 2048, 67110914, 2099202, 69208064, 69208066, 2097152, 0, 67108866, 2, 67108864, 69206018, 2050, 67110912, 2099202, 2097154, 67110912, 67108866, 69206016, 69208064, 2097154, 69206016, 2048, 2050, 69208066, 2099200, 2, 67108864, 2099200, 67108864, 2099200, 2097152, 67110914, 67110914, 69206018, 69206018, 2, 2097154, 67108864, 67110912, 2097152, 69208064, 2050, 2099202, 69208064, 2050, 67108866, 69208066, 69206016, 2099200, 0, 2, 69208066, 0, 2099202, 69206016, 2048, 67108866, 67110912, 2048, 2097154 };

  private static int[] SP8 = { 268439616, 4096, 262144, 268701760, 268435456, 268439616, 64, 268435456, 262208, 268697600, 268701760, 266240, 268701696, 266304, 4096, 64, 268697600, 268435520, 268439552, 4160, 266240, 262208, 268697664, 268701696, 4160, 0, 0, 268697664, 268435520, 268439552, 266304, 262144, 266304, 262144, 268701696, 4096, 64, 268697664, 4096, 266304, 268439552, 64, 268435520, 268697600, 268697664, 268435456, 262144, 268439616, 0, 268701760, 262208, 268435520, 268697600, 268439552, 268439616, 0, 268701760, 266240, 266240, 4160, 4160, 262208, 268435456, 268701696 };

  public DesCipher(byte[] paramArrayOfByte)
  {
    setKey(paramArrayOfByte);
  }

  public void setKey(byte[] paramArrayOfByte)
  {
    deskey(paramArrayOfByte, true, this.encryptKeys);
    deskey(paramArrayOfByte, false, this.decryptKeys);
  }

  private void deskey(byte[] paramArrayOfByte, boolean paramBoolean, int[] paramArrayOfInt)
  {
    int[] arrayOfInt1 = new int[56];
    int[] arrayOfInt2 = new int[56];
    int[] arrayOfInt3 = new int[32];
    int k;
    int m;
    for (int j = 0; j < 56; j++)
    {
      k = pc1[j];
      m = k & 0x7;
      arrayOfInt1[j] = ((paramArrayOfByte[(k >>> 3)] & bytebit[m]) != 0 ? 1 : 0);
    }

    for (int i = 0; i < 16; i++)
    {
      if (paramBoolean)
        m = i << 1;
      else
        m = 15 - i << 1;
      int n = m + 1;
      int tmp122_121 = 0; arrayOfInt3[n] = tmp122_121; arrayOfInt3[m] = tmp122_121;
      int j;
      for (j = 0; j < 28; j++)
      {
        k = j + totrot[i];
        if (k < 28)
          arrayOfInt2[j] = arrayOfInt1[k];
        else
          arrayOfInt2[j] = arrayOfInt1[(k - 28)];
      }
      for (j = 28; j < 56; j++)
      {
        k = j + totrot[i];
        if (k < 56)
          arrayOfInt2[j] = arrayOfInt1[k];
        else
          arrayOfInt2[j] = arrayOfInt1[(k - 28)];
      }
      for (j = 0; j < 24; j++)
      {
        if (arrayOfInt2[pc2[j]] != 0)
          arrayOfInt3[m] |= bigbyte[j];
        if (arrayOfInt2[pc2[(j + 24)]] != 0)
          arrayOfInt3[n] |= bigbyte[j];
      }
    }
    cookey(arrayOfInt3, paramArrayOfInt);
  }

  private void cookey(int[] paramArrayOfInt1, int[] paramArrayOfInt2)
  {
    int n = 0; int k = 0; for (int m = 0; n < 16; n++)
    {
      int i = paramArrayOfInt1[(k++)];
      int j = paramArrayOfInt1[(k++)];
      paramArrayOfInt2[m] = ((i & 0xFC0000) << 6);
      paramArrayOfInt2[m] |= (i & 0xFC0) << 10;
      paramArrayOfInt2[m] |= (j & 0xFC0000) >>> 10;
      paramArrayOfInt2[m] |= (j & 0xFC0) >>> 6;
      m++;
      paramArrayOfInt2[m] = ((i & 0x3F000) << 12);
      paramArrayOfInt2[m] |= (i & 0x3F) << 16;
      paramArrayOfInt2[m] |= (j & 0x3F000) >>> 4;
      paramArrayOfInt2[m] |= j & 0x3F;
      m++;
    }
  }

  public void encrypt(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, int paramInt2)
  {
    squashBytesToInts(paramArrayOfByte1, paramInt1, this.tempInts, 0, 2);
    des(this.tempInts, this.tempInts, this.encryptKeys);
    spreadIntsToBytes(this.tempInts, 0, paramArrayOfByte2, paramInt2, 2);
  }

  public void decrypt(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, int paramInt2)
  {
    squashBytesToInts(paramArrayOfByte1, paramInt1, this.tempInts, 0, 2);
    des(this.tempInts, this.tempInts, this.decryptKeys);
    spreadIntsToBytes(this.tempInts, 0, paramArrayOfByte2, paramInt2, 2);
  }

  private void des(int[] paramArrayOfInt1, int[] paramArrayOfInt2, int[] paramArrayOfInt3)
  {
    int i1 = 0;

    int m = paramArrayOfInt1[0];
    int k = paramArrayOfInt1[1];

    int j = (m >>> 4 ^ k) & 0xF0F0F0F;
    k ^= j;
    m ^= j << 4;

    j = (m >>> 16 ^ k) & 0xFFFF;
    k ^= j;
    m ^= j << 16;

    j = (k >>> 2 ^ m) & 0x33333333;
    m ^= j;
    k ^= j << 2;

    j = (k >>> 8 ^ m) & 0xFF00FF;
    m ^= j;
    k ^= j << 8;
    k = k << 1 | k >>> 31 & 0x1;

    j = (m ^ k) & 0xAAAAAAAA;
    m ^= j;
    k ^= j;
    m = m << 1 | m >>> 31 & 0x1;

    for (int n = 0; n < 8; n++)
    {
      j = k << 28 | k >>> 4;
      j ^= paramArrayOfInt3[(i1++)];
      int i = SP7[(j & 0x3F)];
      i |= SP5[(j >>> 8 & 0x3F)];
      i |= SP3[(j >>> 16 & 0x3F)];
      i |= SP1[(j >>> 24 & 0x3F)];
      j = k ^ paramArrayOfInt3[(i1++)];
      i |= SP8[(j & 0x3F)];
      i |= SP6[(j >>> 8 & 0x3F)];
      i |= SP4[(j >>> 16 & 0x3F)];
      i |= SP2[(j >>> 24 & 0x3F)];
      m ^= i;
      j = m << 28 | m >>> 4;
      j ^= paramArrayOfInt3[(i1++)];
      i = SP7[(j & 0x3F)];
      i |= SP5[(j >>> 8 & 0x3F)];
      i |= SP3[(j >>> 16 & 0x3F)];
      i |= SP1[(j >>> 24 & 0x3F)];
      j = m ^ paramArrayOfInt3[(i1++)];
      i |= SP8[(j & 0x3F)];
      i |= SP6[(j >>> 8 & 0x3F)];
      i |= SP4[(j >>> 16 & 0x3F)];
      i |= SP2[(j >>> 24 & 0x3F)];
      k ^= i;
    }

    k = k << 31 | k >>> 1;
    j = (m ^ k) & 0xAAAAAAAA;
    m ^= j;
    k ^= j;
    m = m << 31 | m >>> 1;
    j = (m >>> 8 ^ k) & 0xFF00FF;
    k ^= j;
    m ^= j << 8;
    j = (m >>> 2 ^ k) & 0x33333333;
    k ^= j;
    m ^= j << 2;
    j = (k >>> 16 ^ m) & 0xFFFF;
    m ^= j;
    k ^= j << 16;
    j = (k >>> 4 ^ m) & 0xF0F0F0F;
    m ^= j;
    k ^= j << 4;
    paramArrayOfInt2[0] = k;
    paramArrayOfInt2[1] = m;
  }

  public static void squashBytesToInts(byte[] paramArrayOfByte, int paramInt1, int[] paramArrayOfInt, int paramInt2, int paramInt3)
  {
    for (int i = 0; i < paramInt3; i++)
      paramArrayOfInt[(paramInt2 + i)] = ((paramArrayOfByte[(paramInt1 + i * 4)] & 0xFF) << 24 | (paramArrayOfByte[(paramInt1 + i * 4 + 1)] & 0xFF) << 16 | (paramArrayOfByte[(paramInt1 + i * 4 + 2)] & 0xFF) << 8 | paramArrayOfByte[(paramInt1 + i * 4 + 3)] & 0xFF);
  }

  public static void spreadIntsToBytes(int[] paramArrayOfInt, int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3)
  {
    for (int i = 0; i < paramInt3; i++)
    {
      paramArrayOfByte[(paramInt2 + i * 4)] = (byte)(paramArrayOfInt[(paramInt1 + i)] >>> 24);
      paramArrayOfByte[(paramInt2 + i * 4 + 1)] = (byte)(paramArrayOfInt[(paramInt1 + i)] >>> 16);
      paramArrayOfByte[(paramInt2 + i * 4 + 2)] = (byte)(paramArrayOfInt[(paramInt1 + i)] >>> 8);
      paramArrayOfByte[(paramInt2 + i * 4 + 3)] = (byte)paramArrayOfInt[(paramInt1 + i)];
    }
  }
}