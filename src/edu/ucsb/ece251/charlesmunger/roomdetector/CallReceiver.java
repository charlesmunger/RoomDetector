package edu.ucsb.ece251.charlesmunger.roomdetector;

import roboguice.receiver.RoboBroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CallReceiver extends RoboBroadcastReceiver {

	@Override
	public void handleReceive(Context context, Intent intent) {
		Log.d("CallReceiver", "call received");
		Intent i = new Intent(context, InRoomService.class);
		i.putExtra(InRoomService.PENDING_INTENT, new Intent(context, CallSilencerService.class));
		context.startService(i);
	}
}
