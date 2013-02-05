package edu.ucsb.ece251.charlesmunger.roomdetector;

import roboguice.service.RoboIntentService;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.google.inject.Inject;

public class InRoomService extends RoboIntentService {

	private static final String TAG = "InRoomService";
	public static final String PENDING_INTENT = "PendingIntent";
	@Inject PowerManager pm;
	public InRoomService() {
		super(TAG);
	}

	@Override
	public void onHandleIntent(Intent intent) {
		Log.d(TAG, "acquiring wakelock");
		PowerManager.WakeLock wl= pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		wl.acquire();
		try {
			if(inRoom()) {
				Log.d(TAG, "Launching intent" + ((Intent) intent.getParcelableExtra(PENDING_INTENT)).getAction());
				startService(((Intent) intent.getParcelableExtra(PENDING_INTENT)));
			}
		} finally {
			wl.release();
		}
	}

	private boolean inRoom() {
		// TODO Auto-generated method stub
		return true;
	}
}
