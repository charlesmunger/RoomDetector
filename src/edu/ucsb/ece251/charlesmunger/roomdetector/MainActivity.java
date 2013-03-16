package edu.ucsb.ece251.charlesmunger.roomdetector;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import android.app.PendingIntent;
import android.content.Intent;
import android.view.View;

@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActivity {

	private static final String EXTRA_STRING_PARAMS = "org.leetzone.android.yatsewidget.EXTRA_STRING_PARAMS";
	private static final String ACTION_MEDIA_COMMAND = "org.leetzone.android.yatsewidget.ACTION_MEDIA_COMMAND";

	public void xbmcButton(View v) {
		final Intent send = new Intent(ACTION_MEDIA_COMMAND);
		send.putExtra(EXTRA_STRING_PARAMS, "pause");
		sendToService(send);
	}

	public void callButton(View v) {
		sendToService(new Intent(this, CallSilencerService.class));
	}

	private void sendToService(Intent send) {
		final Intent i = new Intent(this, InRoomService.class);
		i.putExtra(InRoomService.PENDING_INTENT,
				PendingIntent.getService(this, 0, send, 0));
		startService(i);
	}
}
