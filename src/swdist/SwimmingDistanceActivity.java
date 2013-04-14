package swdist;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.swdist.R;

public class SwimmingDistanceActivity extends Activity {

	//Constants
	private static final int UP = 0;
	private static final int DOWN = 1;
	
	private static int SAMPLED_INTERVAL = 2000; //Time it takes to turn by compass method
	private static int SAMPLING_RATE = 200;
	private static int TIME_BETWEEN_CHANGES = 10000; //Minimun Lap time, should be calculated depending on Length distance
	private static float COMPASS_THRESHOLD = 120f;
	
	private DistanceManager distanceManager;
	private NotificationManager notificationManager;
	private LapChangeManager lapChangeManager;

	private SimpleDateFormat minutes = new SimpleDateFormat("mm:ss");

	SensorManager sensorManager;
	List<Sensor> sensorList;
	//Sensor accelerometerSensor;
	boolean sensorsPresent;

	boolean notified = false;

	// Layout Elements
	Context rootContext;
	TextView tvFace, tvLaps, tvDistSw, tvSpeed;
	TextView tvAcc, tvOri, tvArray;
	EditText etDistance, etTotalDist;
	CheckBox cbSound;

	private Button btStart;
	private Button btStop;
	private Button btReset;
	private Button btSimulate;

	private TextView txtTotalTime;

	public void start() {
		initializeSensors();

		if (sensorsPresent) {
			int i = sensorList.size();
			sensorManager.registerListener(mySensorEventListener,
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
			sensorManager.registerListener(mySensorEventListener,
					sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
		}

		distanceManager = new DistanceManager();
		distanceManager.setPoolSize(Integer.valueOf(etDistance.getText()
				.toString()));
		distanceManager.start();


	}

	public void stop() {
		if (sensorsPresent) {
			sensorManager.unregisterListener(mySensorEventListener);
		}
	}

	private void initializeSensors() {
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorList = sensorManager
				.getSensorList(Sensor.TYPE_ACCELEROMETER | Sensor.TYPE_ORIENTATION);

		if (sensorList.size() > 0) {
			sensorsPresent = true;
			//accelerometerSensor = sensorList.get(0);
		} else {
			sensorsPresent = false;
			tvFace.setText("No sensor present!");
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		setLayoutElements();
		
		this.lapChangeManager = new LapChangeManager(SAMPLED_INTERVAL, SAMPLING_RATE, 
													TIME_BETWEEN_CHANGES, COMPASS_THRESHOLD);
		
		this.notificationManager = new NotificationManager(
				getApplicationContext());
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	protected void onStop() {

		// TODO Auto-generated method stub
		super.onStop();

		if (sensorsPresent)
			sensorManager.unregisterListener(mySensorEventListener);
	}

	public void setLayoutElements() {
		rootContext = this;
		tvFace = (TextView) findViewById(R.id.tvFace);
		tvLaps = (TextView) findViewById(R.id.tvLaps);
		tvDistSw = (TextView) findViewById(R.id.tvDistSw);
		tvSpeed = (TextView) findViewById(R.id.tvSpeed);
		etDistance = (EditText) findViewById(R.id.etDistance);
		etTotalDist = (EditText) findViewById(R.id.etTotalDist);
		cbSound = (CheckBox) findViewById(R.id.cbSound);

		txtTotalTime = (TextView) findViewById(R.id.txtTotalTime);

		btStart = (Button) findViewById(R.id.btStart);
		btStart.setOnClickListener(btStartListener);

		btStop = (Button) findViewById(R.id.btStop);
		btStop.setOnClickListener(btStopListener);

		btReset = (Button) findViewById(R.id.btReset);
		btReset.setOnClickListener(btResetListener);

		btSimulate = (Button) findViewById(R.id.btSImulate);
		btSimulate.setOnClickListener(btSimulateListener);
		
		tvAcc = (TextView) findViewById(R.id.tvAcc); 
		tvOri  = (TextView) findViewById(R.id.tvOri);
		tvArray = (TextView) findViewById(R.id.tvArray);
		
		return;
	}

	public void updateView() {

		switch (lapChangeManager.getCurrentFace()) {
			case UP:
				tvFace.setText("Face UP");
				break;
			case DOWN:
				tvFace.setText("Face DOWN");
				break;
		}

		tvLaps.setText(String.valueOf(distanceManager.getLaps()));
		tvDistSw.setText(distanceManager.getDistance() + " mts");
		tvSpeed.setText(minutes.format(new Date(distanceManager.getSpeed()))
				+ " minutos por 100");

		Log.d("SD",
				"distance:" + distanceManager.getDistance() + " Laps:"
						+ distanceManager.getLaps() + " Speed:"
						+ distanceManager.getSpeed());

		if (distanceManager.getDistance() % 100 == 0) {
			notificationManager.notifyTime(distanceManager.getDistance(),
					distanceManager.getLastFaceSpeed());
		}
		
		return;
	}

	public void newLapProcedure(){
		distanceManager.newLap();
		
		if (cbSound.isChecked())
			notificationManager
					.playNotification(RingtoneManager.TYPE_NOTIFICATION);

		if (distanceManager.getDistance() >= Integer
				.valueOf(etTotalDist.getText().toString())
				&& !notified) {
			notificationManager
					.playNotification(RingtoneManager.TYPE_ALARM);
			notified = true;
		}

		Log.d("SD",	"Lap " + String.valueOf(distanceManager
								.getDistance()));
		
		return;
	}
	
	private SensorEventListener mySensorEventListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSensorChanged(SensorEvent event) {

			Sensor sensor = event.sensor;
            int type = sensor.getType();

            switch (type) {
                
            	case Sensor.TYPE_ACCELEROMETER:
            		tvAcc.setText(String.valueOf(event.values[2]));
            		if (lapChangeManager.lapChangeByAccelerometer(event.values[2]))
            			newLapProcedure();
            		
                    break;
                    
            	case Sensor.TYPE_ORIENTATION:
            		tvOri.setText(String.valueOf(event.values[0]));
            		if (lapChangeManager.recordLecture(event.values[0]))
            			if (lapChangeManager.lapChangeByCompass())
            				newLapProcedure();
            		
            		/*String a =String.valueOf(lapChangeManager.compassLectures.length);
            		for (int i=0 ;i<lapChangeManager.compassLectures.length; i++)
            			a=a.concat(String.valueOf(lapChangeManager.compassLectures[i])).concat(",");
            		tvArray.setText(a);*/
            		
            		break;
            }
            
            updateView();
            
		}
	};

	View.OnClickListener btStartListener = new View.OnClickListener() {
		public void onClick(View v) {
			Toast.makeText(rootContext, "Start!", Toast.LENGTH_SHORT).show();
			start();
		}
	};

	View.OnClickListener btStopListener = new View.OnClickListener() {
		public void onClick(View v) {
			Toast.makeText(rootContext, "Stop!", Toast.LENGTH_SHORT).show();
			stop();
		}
	};

	View.OnClickListener btResetListener = new View.OnClickListener() {
		public void onClick(View v) {
			distanceManager = new DistanceManager();
			tvLaps.setText("0");
			tvDistSw.setText("0 mts");
			tvSpeed.setText("0 minutos por 100mts");
		}
	};

	View.OnClickListener btSimulateListener = new View.OnClickListener() {
		public void onClick(View v) {
			distanceManager.newLap();
			updateView();
			Log.d("SD", "SIMULATE!");
		}
	};
}