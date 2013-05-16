package swdist;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.swdist.R;

public class SwimmingDistanceActivity extends Activity {

	private static int SAMPLED_INTERVAL = 2000; // Time it takes to turn by
												// compass method
	private static int SAMPLING_RATE = 200;
	private static int TIME_BETWEEN_CHANGES = 10000; // Minimun Lap time, should
														// be calculated
														// depending on Length
														// distance
	private static float COMPASS_THRESHOLD = 120f;

	private DistanceManager distanceManager;
	private NotificationManager notificationManager;
	private LapChangeManager lapChangeManager;

	private SimpleDateFormat minutes = new SimpleDateFormat("mm:ss", Locale.US);

	SensorManager sensorManager;
	List<Sensor> sensorList;

	// Sensor accelerometerSensor;
	boolean sensorsPresent;

	boolean notified = false;

	// Layout Elements
	Context rootContext;
	TextView tvLaps, tvDistSw, tvSpeed;
	Chronometer myChrono;
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

			sensorManager.registerListener(mySensorEventListener,
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_NORMAL);
			sensorManager.registerListener(mySensorEventListener,
					sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
					SensorManager.SENSOR_DELAY_NORMAL);
		}

		distanceManager = new DistanceManager(getApplicationContext());
		distanceManager.setPoolSize(Integer.valueOf(etDistance.getText()
				.toString()));
		distanceManager.start();

	}

	public void stop() {
		distanceManager.save();
		if (sensorsPresent) {
			sensorManager.unregisterListener(mySensorEventListener);
		}
	}

	private void initializeSensors() {
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorList = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER
				| Sensor.TYPE_ORIENTATION);

		if (sensorList.size() > 0) {
			sensorsPresent = true;
			// accelerometerSensor = sensorList.get(0);
		} else {
			sensorsPresent = false;
			Log.d("SD", "No sensor present!");
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			
		setContentView(R.layout.main);

		setLayoutElements();

		this.lapChangeManager = new LapChangeManager(SAMPLED_INTERVAL,
				SAMPLING_RATE, TIME_BETWEEN_CHANGES, COMPASS_THRESHOLD);

		this.notificationManager = new NotificationManager(
				getApplicationContext());
	
		
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onStop() {

		super.onStop();

		if (sensorsPresent)
			sensorManager.unregisterListener(mySensorEventListener);
	}

	public void setLayoutElements() {
		rootContext = this;
		tvLaps = (TextView) findViewById(R.id.tvLaps);
		tvDistSw = (TextView) findViewById(R.id.tvDistSw);
		tvSpeed = (TextView) findViewById(R.id.tvSpeed);
		etDistance = (EditText) findViewById(R.id.etDistance);
		etTotalDist = (EditText) findViewById(R.id.etTotalDist);
		cbSound = (CheckBox) findViewById(R.id.cbSound);

		btStart = (Button) findViewById(R.id.btStart);
		btStart.setOnClickListener(btStartListener);

		btStop = (Button) findViewById(R.id.btStop);
		btStop.setOnClickListener(btStopListener);

		btReset = (Button) findViewById(R.id.btReset);
		btReset.setOnClickListener(btResetListener);

		btSimulate = (Button) findViewById(R.id.btSImulate);
		btSimulate.setOnClickListener(btSimulateListener);
		
		myChrono = (Chronometer) findViewById(R.id.chronometer1);
		
		tvAcc = (TextView) findViewById(R.id.tvAcc); 
		tvOri  = (TextView) findViewById(R.id.tvOri);
		tvArray = (TextView) findViewById(R.id.tvArray);

		return;
	}

	public void updateView() {

		Log.d("SD", "FACE " + lapChangeManager.getCurrentFace());

		tvLaps.setText(String.valueOf(distanceManager.getLaps()));
		tvDistSw.setText(distanceManager.getDistance() + " mts");
		tvSpeed.setText(minutes.format(new Date(distanceManager.getSpeed()))
				+ " minutos por 100");
		
		

	}

	public void newLapProcedure() {
		distanceManager.newLap();

		if (cbSound.isChecked())
			notificationManager.playNotification(RingtoneManager.TYPE_NOTIFICATION);

		if (distanceManager.getDistance() >= Integer.valueOf(etTotalDist
				.getText().toString()) && !notified) {
			notificationManager.playNotification(RingtoneManager.TYPE_ALARM);
			notified = true;
		}

		Log.d("SD", "Lap " + String.valueOf(distanceManager.getDistance()));

		Log.d("SD",
				"distance:" + distanceManager.getDistance() + " Laps:"
						+ distanceManager.getLaps() + " Speed:"
						+ distanceManager.getSpeed());

		if (cbSound.isChecked() && distanceManager.getDistance() % 100 == 0) {
			notificationManager.notifyTime(distanceManager.getDistance(),
					distanceManager.getLastFaceSpeed());
		}
		
		updateView();
	}

	private SensorEventListener mySensorEventListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
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
				break;
			}

			
			txtTotalTime.setText(minutes.format(new Date(distanceManager.getTotalTime())));
		}
	};

	View.OnClickListener btStartListener = new View.OnClickListener() {
		public void onClick(View v) {
			Toast.makeText(rootContext, "Start!", Toast.LENGTH_SHORT).show();
			myChrono.setBase(SystemClock.elapsedRealtime());
			myChrono.start();
			start();
		}
	};

	View.OnClickListener btStopListener = new View.OnClickListener() {
		public void onClick(View v) {
			Toast.makeText(rootContext, "Stop!", Toast.LENGTH_SHORT).show();
			myChrono.stop();
			stop();
		}
	};

	View.OnClickListener btResetListener = new View.OnClickListener() {
		public void onClick(View v) {
			distanceManager = new DistanceManager(getApplicationContext());
			tvLaps.setText("0");
			tvDistSw.setText("0 mts");
			tvSpeed.setText("0 minutos por 100mts");
			myChrono.setBase(SystemClock.elapsedRealtime());
		}
	};

	View.OnClickListener btSimulateListener = new View.OnClickListener() {
		public void onClick(View v) {
			newLapProcedure();
			updateView();
			Log.d("SD", "SIMULATE!");
		}
	};
}