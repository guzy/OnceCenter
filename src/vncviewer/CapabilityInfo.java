package vncviewer;

class CapabilityInfo
{
  protected int code;
  protected String vendorSignature;
  protected String nameSignature;
  protected String description;
  protected boolean enabled;

  public CapabilityInfo(int paramInt, String paramString1, String paramString2, String paramString3)
  {
    this.code = paramInt;
    this.vendorSignature = paramString1;
    this.nameSignature = paramString2;
    this.description = paramString3;
    this.enabled = false;
  }

  public CapabilityInfo(int paramInt, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
  {
    this.code = paramInt;
    this.vendorSignature = new String(paramArrayOfByte1);
    this.nameSignature = new String(paramArrayOfByte2);
    this.description = null;
    this.enabled = false;
  }

  public int getCode() {
    return this.code;
  }

  public String getDescription() {
    return this.description;
  }

  public boolean isEnabled() {
    return this.enabled;
  }

  public void enable() {
    this.enabled = true;
  }

  public boolean equals(CapabilityInfo paramCapabilityInfo) {
    return (paramCapabilityInfo != null) && (this.code == paramCapabilityInfo.code) && (this.vendorSignature.equals(paramCapabilityInfo.vendorSignature)) && (this.nameSignature.equals(paramCapabilityInfo.nameSignature));
  }

  public boolean enableIfEquals(CapabilityInfo paramCapabilityInfo)
  {
    if (equals(paramCapabilityInfo)) {
      enable();
    }
    return isEnabled();
  }
}