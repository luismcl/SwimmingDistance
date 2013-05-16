package swdist;

import java.util.Date;

import swdist.model.Workout;
import android.content.Context;

public class DistanceManager {
	
	
	public FileManager fileManager = new FileManager();

	private Workout workout;

	private int poolSize = 25;


	public void start() {
		workout = new Workout();
	}

	public DistanceManager(Context context) {
	
	}

	public void newLap() {
		workout.newLap(poolSize, new Date());
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	public int getLaps() {
		return workout.getLaps().size();
	}

	public long getSpeed() {

		long last;

		if (workout.getLaps().size() > 1) {
			last = workout.getLaps().get(workout.getLaps().size() - 2)
					.getTimeStamp().getTime();
		} else {
			last = workout.getStartDate().getTime();
		}

		if (workout.getLaps().size() > 0) {
			long current = workout.getLaps().get(workout.getLaps().size() - 1)
					.getTimeStamp().getTime();
			Long speed = (current - last) * 100 / poolSize;
			return speed;
		}

		return 0;
	}

	public int getDistance() {
		return workout.getDistance();
	}

	public long getLastFaceSpeed() {
		long last;
		if (workout.getLaps().size() > 4) {
			last = workout.getLaps().get(workout.getLaps().size() - 4)
					.getTimeStamp().getTime();
		} else {
			last = workout.getStartDate().getTime();
		}

		if (workout.getLaps().size() > 0) {
			long current = workout.getLaps().get(workout.getLaps().size() - 1)
					.getTimeStamp().getTime();
			return current - last;
		}

		return 0;
	}

	public Long getTotalTime() {
		return new Date().getTime() - workout.getStartDate().getTime();
	}

	public void save() {
		fileManager.writeFile(workout.getStartDate().getTime() +  "_WorkOut.json", workout);
		
	}
}
