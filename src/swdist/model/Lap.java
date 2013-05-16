package swdist.model;

import java.util.Date;

public class Lap {

	private int size;
	private long totalTime;
	private Date timeStamp;
	

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	public Date getTimeStamp() {
		return timeStamp;
	}
}
