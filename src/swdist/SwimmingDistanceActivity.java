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

	private DistanceManager distanceManager;
	private NotificationManager notificationManager;

	private SimpleDateFormat minutes = new SimpleDateFormat("mm:ss");

	// Constants
	private static final int UP = 0;
	private static final int DOWN = 1;

	// Parametrization
	private static final long minMilis = 600;

	SensorManager sensorManager;
	Sensor accelerometerSensor;
	boolean accelerometerPresent;

	boolean notified = false;

	int currentFace = UP;
	int previousFace = UP;

	long lastChangeUp = 0;
	long lastChangeDown = 0;

	// Layout Elements
	Context rootContext;
	TextView tvFace, tvLaps, tvDistSw, tvSpeed;
	EditText etDistance, etTotalDist;
	CheckBox cbSound;

	private Button btStart;
	private Button btStop;
	private Button btReset;
	private Button btSimulate;

	private TextView txtTotalTime;

	public void start() {
		initializeSensors();

		if (accelerometerPresent) {
			sensorManager.registerListener(accelerometerListener,
					accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
		}
		
		distanceManager = new DistanceManager();
		distanceManager.setPoolSize(Integer.valueOf(etDistance.getText()
				.toString()));
		distanceManager.start();


	}

	public void stop() {
		if (accelerometerPresent) {
			sensorManager.unregisterListener(accelerometerListener);
		}
	}

	private void initializeSensors() {
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		List<Sensor> sensorList = sensorManager
				.getSensorList(Sensor.TYPE_ACCELEROMETER);

		if (sensorList.size() > 0) {
			accelerometerPresent = true;
			accelerometerSensor = sensorList.get(0);
		} else {
			accelerometerPresent = false;
			tvFace.setText("No accelerometer present!");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		setLayoutElements();
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

		if (accelerometerPresent)
			sensorManager.unregisterListener(accelerometerListener);
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
	}

	public void updateView() {

		switch (currentFace) {
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
	}

	private SensorEventListener accelerometerListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSensorChanged(SensorEvent event) {

			float z_value = event.values[2];
			if ((int) z_value >= 0) {
				currentFace = UP;
			} else {
				currentFace = DOWN;
			}

			// distance = (int)z_value;
			if (currentFace != previousFace) {
				if (currentFace == UP) {
					lastChangeDown = System.currentTimeMillis();
					Log.d("SD", "Up " + String.valueOf(lastChangeDown));
				} else {
					lastChangeUp = System.currentTimeMillis();
					Log.d("SD", "Down " + String.valueOf(lastChangeUp));
				}

				if (Math.abs(lastChangeDown - lastChangeUp) > minMilis) {
					if (currentFace == UP) {
						distanceManager.newLap();

						updateView();

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

						Log.d("SD",
								"Lap "
										+ String.valueOf(distanceManager
												.getDistance()));
					}
				}
			}
			previousFace = currentFace;

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