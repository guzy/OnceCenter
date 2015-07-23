package oncecenter.util.snapshotstrategy;

import java.io.Serializable;

public class SnapshotStrategy implements Serializable{
	private float period;//天数
	private int reverseNumber;//快照保留个数
	
	public int getReverseNumber() {
		return reverseNumber;
	}

	public void setReverseNumber(int reverseNumber) {
		this.reverseNumber = reverseNumber;
	}

	public SnapshotStrategy(float period,int reverseNumber){
		this.period = period;
		this.reverseNumber = reverseNumber;
	}

	@Override
	public boolean equals(Object otherObject)
	{
		if(this == otherObject) return true;
		if(otherObject == null) return false;
		if(otherObject.getClass() != getClass()) return false;
		SnapshotStrategy other = (SnapshotStrategy)otherObject;
		return reverseNumber == other.reverseNumber && period == other.period; 
	}
	
	@Override
	public int hashCode()
	{
		return (int) (7*reverseNumber + 11*period); 
	}
	public float getPeriod() {
		return period;
	}

	public void setPeriod(float period) {
		this.period = period;
	}
}
