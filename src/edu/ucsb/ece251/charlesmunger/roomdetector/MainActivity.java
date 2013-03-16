package edu.ucsb.ece251.charlesmunger.roomdetector;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import android.app.PendingIntent;
import android.content.Intent;
import android.view.View;

@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActivity {
	
	public void xbmcButton(View v) {
		Intent i = new Intent(this, InRoomService.class);
		Intent send = new Intent("org.leetzone.android.yatsewidget.ACTION_MEDIA_COMMAND");
		send.putExtra("org.leetzone.android.yatsewidget.EXTRA_STRING_PARAMS", "pause");
		i.putExtra(InRoomService.PENDING_INTENT, PendingIntent.getService(this, 0, send, 0));
		startService(i);
	}
}
