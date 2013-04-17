package swdist;

import android.util.Log;

/**
 * Class to manage sensor lectures and detect lap changes
 * All time variables are in milliseconds
 */
public class LapChangeManager {

	// Constants
	private static final int UP = 0;
	private static final int DOWN = 1;

	// Parametrization
	private static final long minMilis = 600;

	private int lecturesSize;
	private int currentLecture = 0;
	private long sampligRate = 0;
	private long timeBetweenChanges = 0;
	private long lastRecordTime = 0;
	private long lastLapChange = 0;
	public float[] compassLectures;
	private float compassThreshold;
	
	long lastChangeDown, lastChangeUp;
	int currentFace = UP;
	int previousFace = UP;
	
	/**
	 * 
	 * @param _sampledInterval
	 * @param _sampligRate
	 * @param _timeBetweenChanges
	 * @param _compassThreshold
	 */
	public LapChangeManager(long _sampledInterval, long _sampligRate, long _timeBetweenChanges, float _compassThreshold){
		
		this.sampligRate = _sampligRate;
		this.lecturesSize = (int)(_sampledInterval / sampligRate);
		this.compassLectures = new float[lecturesSize];
		
		for (int i=0; i<lecturesSize; i++)
			compassLectures[i] = 0f;
		
		this.currentLecture = lecturesSize - 1;		
		this.timeBetweenChanges = _timeBetweenChanges;		
		this.lastLapChange = System.currentTimeMillis();
		this.compassThreshold = _compassThreshold;
		
	}
	
	public int getCurrentFace() {
		return currentFace;
	}
	
	
	
	public long getLastLapChange() {
		return lastLapChange;
	}

	public void setLastLapChange(long lastLapChange) {
		this.lastLapChange = lastLapChange;
	}

	/**
	 * Save the last compass lecture in a cyclic array
	 * and update the last record time
	 * @param _compassLecture
	 * @return 	true -> If lecture was added 
	 * 			false -> If not
	 */
	public boolean recordLecture(float _compassLecture){		
		long curTime = System.currentTimeMillis();		
		long diff = curTime - (lastRecordTime + sampligRate);
		
		//Log.d("SD", String.valueOf(diff));
		
		if (diff > 0){
			
			currentLecture++;
			
			if (currentLecture >= lecturesSize )
				currentLecture = 0;
			
			compassLectures[currentLecture] = _compassLecture;
			lastRecordTime = curTime;
		
			return true;
		}
		
		return false;
	}
	
	/**
	 * Detect a lap change based on the compass lectures
	 * @return 	true -> If lap changed
	 * 			false -> If not
	 */
	public boolean lapChangeByCompass(){
		int cyclicLecture = currentLecture + 1;
		long curTime = System.currentTimeMillis();
		
		if ((lastLapChange + timeBetweenChanges) > curTime)
			return false;
		
		if (cyclicLecture >= lecturesSize )
			cyclicLecture = 0;
		
		float lectDiff = degreeDiff(compassLectures[cyclicLecture],
						 		  compassLectures[currentLecture]);
		
		if (lectDiff >= compassThreshold){
			lastLapChange = curTime;
			return true;
		}
		
		return false;
	}
	
	public boolean lapChangeByAccelerometer(float _lecture){
		float z_value = _lecture;
		boolean newLap = false;
		long curTime = System.currentTimeMillis();
		
		if ((int) z_value >= 0) {
			currentFace = UP;
		} 
		else {
			currentFace = DOWN;
		}
	
		if (currentFace != previousFace) {
			if (currentFace == UP) {
				lastChangeDown = curTime;
				Log.d("SD", "Up " + String.valueOf(lastChangeDown));
			} else {
				lastChangeUp = curTime;
				Log.d("SD", "Down " + String.valueOf(lastChangeUp));
			}
	
			if ((Math.abs(lastChangeDown - lastChangeUp) > minMilis) &&
				((lastLapChange + timeBetweenChanges) > curTime)){
				if (currentFace == UP) {
					newLap = true;
					lastLapChange = curTime;
				}
			}
		}
		previousFace = currentFace;
		
		return newLap;
	}
	
	/**
	 * 
	 * @param _angleA
	 * @param _angleB
	 * @return Angle difference between A & B (Return Value between 0..180)
	 */
	private float degreeDiff(float _angleA, float _angleB){		
		float retVal = 0f;
		
		retVal = Math.abs(_angleA - _angleB);
		if (retVal > 180)
			return (360 - retVal );
		
		return retVal;
	}

}
