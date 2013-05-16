package swdist.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Workout {

	private Integer distance = 0 ;
	private long totalTime = 0;
	private Date startDate = new Date();

	private List<Lap> laps = new ArrayList<Lap>();

	public Integer getDistance() {
		return distance;
	}

	public void setDistance(Integer distance) {
		this.distance = distance;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public List<Lap> getLaps() {
		return laps;
	}

	public void setLaps(List<Lap> laps) {
		this.laps = laps;
	}

	public void newLap(int poolSize, Date date) {
		Lap lap = new Lap();
		lap.setSize(poolSize);
		lap.setTimeStamp(new Date());
		lap.setTotalTime(calculateLastLapTime(lap));

		laps.add(lap);
		
		totalTime += lap.getTotalTime();
		
		distance += poolSize;
	}

	private long calculateLastLapTime(Lap lap) {
		long last;
		if (getLaps().size() > 1) {
			last = getLaps().get(getLaps().size() - 2).getTimeStamp().getTime();
		} else {
			last = getStartDate().getTime();
		}

		return lap.getTimeStamp().getTime() - last;
	}

}
