package vncviewer;

import java.util.Hashtable;
import java.util.Vector;

class CapsContainer
{
  protected Hashtable infoMap;
  protected Vector orderedList;

  public CapsContainer()
  {
    this.infoMap = new Hashtable(64, 0.25F);
    this.orderedList = new Vector(32, 8);
  }

  public void add(CapabilityInfo paramCapabilityInfo) {
    Integer localInteger = new Integer(paramCapabilityInfo.getCode());
    this.infoMap.put(localInteger, paramCapabilityInfo);
  }

  public void add(int paramInt, String paramString1, String paramString2, String paramString3) {
    Integer localInteger = new Integer(paramInt);
    this.infoMap.put(localInteger, new CapabilityInfo(paramInt, paramString1, paramString2, paramString3));
  }

  public boolean isKnown(int paramInt) {
    return this.infoMap.containsKey(new Integer(paramInt));
  }

  public CapabilityInfo getInfo(int paramInt) {
    return (CapabilityInfo)this.infoMap.get(new Integer(paramInt));
  }

  public String getDescription(int paramInt) {
    CapabilityInfo localCapabilityInfo = (CapabilityInfo)this.infoMap.get(new Integer(paramInt));
    if (localCapabilityInfo == null) {
      return null;
    }
    return localCapabilityInfo.getDescription();
  }

  public boolean enable(CapabilityInfo paramCapabilityInfo) {
    Integer localInteger = new Integer(paramCapabilityInfo.getCode());
    CapabilityInfo localCapabilityInfo = (CapabilityInfo)this.infoMap.get(localInteger);
    if (localCapabilityInfo == null) {
      return false;
    }
    boolean bool = localCapabilityInfo.enableIfEquals(paramCapabilityInfo);
    if (bool) {
      this.orderedList.addElement(localInteger);
    }
    return bool;
  }

  public boolean isEnabled(int paramInt) {
    CapabilityInfo localCapabilityInfo = (CapabilityInfo)this.infoMap.get(new Integer(paramInt));
    if (localCapabilityInfo == null) {
      return false;
    }
    return localCapabilityInfo.isEnabled();
  }

  public int numEnabled() {
    return this.orderedList.size();
  }
  public int getByOrder(int paramInt) {
    int i;
    try {
      i = ((Integer)this.orderedList.elementAt(paramInt)).intValue();
    } catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException) {
      i = 0;
    }
    return i;
  }
}