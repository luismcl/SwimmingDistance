package swdist;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DistanceManager {

	private Date startDate;
	private List<Date> laps = new ArrayList<Date>();
	private int poolSize = 25;
	
	public void newLap(){
		laps.add(new Date());
	}
	
	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}
	
	public void start(){
		laps = new ArrayList<Date>();
		startDate = new Date();
	}
	
	public int getLaps(){
		return laps.size();
	}
	
	public long getSpeed(){
		
		long last;
		if (laps.size() > 1){
			last = laps.get(laps.size()-2).getTime();
		}else{
			last = startDate.getTime();
		}
		
		if (laps.size() > 0){
			long current = laps.get(laps.size()-1).getTime();
			Long speed = (current-last) * 100 / poolSize;
			return speed;
		}
		
		return 0;
	}
	
	public int getDistance(){
		return laps.size() * poolSize;
	}
	
	public long getLastFaceSpeed(){
		long last;
		if (laps.size() > 4){
			last = laps.get(laps.size()-4).getTime();
		}else{
			last = startDate.getTime();
		}

		if (laps.size() > 0){
			long current = laps.get(laps.size()-1).getTime();
			return current - last;
		}
		
		return 0;
	}
	
	public Long getTotalTime(){
		return new Date().getTime() - startDate.getTime();
	}
}
