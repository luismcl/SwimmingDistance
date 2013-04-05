package swdist;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

public class NotificationManager {

	private Context context;
	
	private TextToSpeech textToSpeech;
	
	
	private SimpleDateFormat simpleMinute= new SimpleDateFormat("mm");
	private SimpleDateFormat simpleSecond= new SimpleDateFormat("ss");
	
	private String second = "seconds";
	private String minutes = "minutes";

	private String meters = "meters";

	private String interval ="phace";
	
	
	public NotificationManager(Context applicationContext) {
		context = applicationContext;
		textToSpeech = new TextToSpeech(applicationContext, listener);
		
		if (textToSpeech.isLanguageAvailable(new Locale("spa")) == TextToSpeech.LANG_AVAILABLE){
			textToSpeech.setLanguage(new Locale("spa"));
			second = "segundos";
			minutes = "minutos";
			meters = "metros";
			interval = "fase";
		}
		
	}
	
	public void notifyTime(int distance, long time){
		
		StringBuilder sb = new StringBuilder();
		
		Date dateTime = new Date(time);
		sb.append(Integer.toString(distance));
		sb.append(" ");
		sb.append(meters);
		sb.append(" ");
		sb.append(interval);
		sb.append(" ");
		sb.append(simpleMinute.format(dateTime));
		sb.append(" ");
		sb.append(minutes);
		sb.append(" ");
		sb.append(simpleSecond.format(dateTime));
		sb.append(" ");
		sb.append(second);
		
		Log.d("SD", sb.toString());		
		textToSpeech.speak(sb.toString(), TextToSpeech.QUEUE_FLUSH, null);
				
	}

	public void playNotification(int _notificationType){
		try {
	        Uri notification = RingtoneManager.getDefaultUri(_notificationType);
	        Ringtone r = RingtoneManager.getRingtone(context, notification);
	        r.play();
	    } catch (Exception e) {}
	}
	
	public void speak(String text){
		textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}
	
	private TextToSpeech.OnInitListener listener = new OnInitListener() {
		
		@Override
		public void onInit(int status) {
			
		}
	};
	
	public void shutdown(){
		textToSpeech.shutdown();
		}
	
}
