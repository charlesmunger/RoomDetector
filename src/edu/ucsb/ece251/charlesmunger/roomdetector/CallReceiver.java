package edu.ucsb.ece251.charlesmunger.roomdetector;

import com.google.inject.Inject;

import roboguice.receiver.RoboBroadcastReceiver;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallReceiver extends RoboBroadcastReceiver {
	private String TAG = "CallReceiver";
	@Inject TelephonyManager tm;
	@Override
	public void handleReceive(Context context, Intent intent) {
		Log.v("CallReceiver", "call received");
		
		Intent i = new Intent(context, CallSilencerService.class);
		i.putExtra(CallSilencerService.EXTRA_STATE, tm.getCallState());
		switch(tm.getCallState()) {
		case TelephonyManager.CALL_STATE_IDLE: break;
		case TelephonyManager.CALL_STATE_RINGING: 
			final PendingIntent temp = PendingIntent.getService(context, 0, i, 0);
			i = new Intent(context, InRoomService.class);
			i.putExtra(InRoomService.PENDING_INTENT, temp);
			break;
		case TelephonyManager.CALL_STATE_OFFHOOK: break;
		default: Log.w(TAG, "Unexpected state");
		}
		context.startService(i);
	}
}
