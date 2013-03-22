package com.nautka.swdist;

import java.util.List;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class SwimmingDistanceActivity extends Activity {
 
	//Constants
	private static final int UP = 0;
	private static final int DOWN = 1;
	
	//Parametrization
	private static final long minMilis = 600;
	
	SensorManager sensorManager;
	Sensor accelerometerSensor;
	boolean accelerometerPresent;
	
	boolean notified = false;
	
	int currentFace = UP;
	int previousFace = UP;
	int lapCounter = 0;
	int distance = 0;
	long startTime = 0;
	
	
	long lastChangeUp = 0;
	long lastChangeDown = 0;
	
	//Layout Elements
	TextView tvFace, tvLaps, tvDistSw, tvSpeed;
	EditText etDistance, etTotalDist;
	CheckBox cbSound;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		tvFace = (TextView)findViewById(R.id.tvFace);
		tvLaps = (TextView)findViewById(R.id.tvLaps);
		tvDistSw = (TextView)findViewById(R.id.tvDistSw);
		tvSpeed = (TextView)findViewById(R.id.tvSpeed);
		etDistance = (EditText)findViewById(R.id.etDistance);
		etTotalDist = (EditText)findViewById(R.id.etTotalDist);
		cbSound = (CheckBox)findViewById(R.id.cbSound);

		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		
		if(sensorList.size() > 0){
			accelerometerPresent = true;
			accelerometerSensor = sensorList.get(0);  
		}
		else{
			accelerometerPresent = false;  
			tvFace.setText("No accelerometer present!");
		}
		
		startTime = System.currentTimeMillis();
	
	}

	@Override
	protected void onResume() {
		 // TODO Auto-generated method stub
		super.onResume();
		
		if(accelerometerPresent)
			sensorManager.registerListener(accelerometerListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);  
	
	}

	@Override
	protected void onStop() {
	
		// TODO Auto-generated method stub
		super.onStop();
		
		if(accelerometerPresent)
			sensorManager.unregisterListener(accelerometerListener);  
	}
	
	public void playNotification(int _notificationType){
		try {
	        Uri notification = RingtoneManager.getDefaultUri(_notificationType);
	        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
	        r.play();
	    } catch (Exception e) {}
	}

	public void updateView(){
	
		switch(currentFace){
			case UP:
				tvFace.setText("Face UP");
				break;
			case DOWN:
				tvFace.setText("Face DOWN");
				break;
		}
		
		tvLaps.setText(String.valueOf(lapCounter));
		
		tvDistSw.setText(String.valueOf(distance) + " mts");
		
		long diff = (System.currentTimeMillis()-startTime) / 1000l;
		tvSpeed.setText(String.valueOf((float)((long)distance/diff)) + " m/s");
		Log.d("SD", distance + ":" + startTime + ":" + System.currentTimeMillis() + ":" + diff);
		
	}
	private SensorEventListener accelerometerListener = new SensorEventListener(){

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// TODO Auto-generated method stub
   
		}

		@Override
		public void onSensorChanged(SensorEvent arg0) {
			// TODO Auto-generated method stub
			float z_value = arg0.values[2];
			if ((int)z_value >=0){
				currentFace = UP;
			}
			else{
				currentFace = DOWN;
			}
			
			//distance = (int)z_value;
			if (currentFace != previousFace){
				if (currentFace == UP){
					lastChangeDown = System.currentTimeMillis();
					Log.d("SD", "Up " + String.valueOf(lastChangeDown));
				}
				else{
					lastChangeUp = System.currentTimeMillis();
					Log.d("SD", "Down " + String.valueOf(lastChangeUp));
				}
				
				if (Math.abs(lastChangeDown-lastChangeUp) > minMilis){
					if (currentFace == UP){
						lapCounter++;
						distance = lapCounter*Integer.valueOf(etDistance.getText().toString());
						updateView();
						if (cbSound.isChecked())
							playNotification(RingtoneManager.TYPE_NOTIFICATION);
						
						if (distance >= Integer.valueOf(etTotalDist.getText().toString()) && !notified){
							playNotification(RingtoneManager.TYPE_ALARM);
							notified = true;
						}
						Log.d("SD", "Lap " + String.valueOf(lapCounter));
					}
				}
			}

			previousFace = currentFace;
			
		}
	};

}