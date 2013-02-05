package edu.ucsb.ece251.charlesmunger.roomdetector;

import com.google.inject.Inject;

import roboguice.service.RoboIntentService;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

public class CallSilencerService extends RoboIntentService {

	private static final String TAG = "CallSilencerService";
	@Inject AudioManager am;
	public CallSilencerService() {
		super(TAG);
	}

	@Override
	public void onHandleIntent(Intent intent) {
		Log.d(TAG,"Silencing ringer");
		am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		am.setStreamMute(AudioManager.STREAM_RING, true);
	}

}
