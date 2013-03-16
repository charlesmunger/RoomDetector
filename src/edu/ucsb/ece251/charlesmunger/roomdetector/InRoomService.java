package edu.ucsb.ece251.charlesmunger.roomdetector;

import org.hermit.dsp.FFTTransformer;

import roboguice.service.RoboIntentService;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.media.MediaRecorder.AudioSource;
import android.os.PowerManager;
import android.util.Log;

import com.google.inject.Inject;

public class InRoomService extends RoboIntentService {

	private static final String TAG = "InRoomService";
	public static final String PENDING_INTENT = "PendingIntent";
	//private static final int BEEP_FREQUENCY = 18000;
	@Inject	PowerManager pm;

	public InRoomService() {
		super(TAG);
	}

	@Override
	public void onHandleIntent(Intent intent) {
		Log.d(TAG, "acquiring wakelock");
		PowerManager.WakeLock wl = pm.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK, TAG);
		wl.acquire();
		try {
			if (inRoom()) {
				Log.d(TAG,"Launching intent from "+ ((PendingIntent) intent
										.getParcelableExtra(PENDING_INTENT)).toString());
				((PendingIntent) intent.getParcelableExtra(PENDING_INTENT)).send();
			} else {
				Log.d(TAG, "Not sending intent");
			}
		} catch (CanceledException e) {
			Log.e(TAG, "PendingIntent no longer allowing "+e.getMessage());
		} finally {
			wl.release();
			Log.d(TAG, "releasing wakelock");
		}
	}

	private boolean inRoom() {
		Audio a = new Audio();
		a.start();
		try {
			a.join();
		} catch (InterruptedException e) {
			Log.w(TAG, "should never happen",e);
		}
		return a.inRoom;
	}

	/*
	 * Thread to manage live recording/analysis of audio
	 */
	private class Audio extends Thread {
		private static final int BLOCK_SIZE = 1024;
		private static final int SAMPLE_RATE = 44100;
		public boolean inRoom = false;

		private Audio() {
			android.os.Process
					.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		}

		@Override
	    public void run() { 
	        Log.i("Audio", "Running Audio Thread");
	        final short[] buff = new short[2*BLOCK_SIZE];
	        final float[] spectrumData = new float[BLOCK_SIZE];
	        final float[][] history = new float[BLOCK_SIZE][20];

	        final FFTTransformer ft = new FFTTransformer(BLOCK_SIZE*2);

	        int N = AudioRecord.getMinBufferSize(SAMPLE_RATE,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
            AudioRecord recorder = new AudioRecord(AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, N*10);
            OnRecordPositionUpdateListener l = new OnRecordPositionUpdateListener() {
    	        int index = 0;
    	        boolean ignore = true;
				@Override
				public void onPeriodicNotification(AudioRecord recorder) {
					recorder.read(buff, 0, buff.length);
					ft.setInput(buff, 0, buff.length);
					ft.transform();
					ft.getResults(spectrumData, history, index);
					index = (index +1) % 20;
					if(index > 18) ignore = false;
					final int bucket = spectrumData[836] > spectrumData[837] ? 836:837;
					if(!ignore) {
						Log.v(TAG, " ");
						Log.v(TAG, "bucket" + 834 + " amp "+spectrumData[834]*100000);
						Log.v(TAG, "bucket" + 835 + " amp "+spectrumData[835]*100000);
						Log.d(TAG, "bucket" + 836 + " amp "+spectrumData[836]*100000);
						Log.v(TAG, "bucket" + 837 + " amp "+spectrumData[837]*100000);
						Log.v(TAG, "bucket" + 838 + " amp "+spectrumData[838]*100000);
						Log.v(TAG, " ");
					if(spectrumData[838]*3/2 < spectrumData[bucket] && spectrumData[834]*3/2 < spectrumData[bucket]) {
						Audio.this.inRoom = true;
						Audio.this.interrupt();
					}}
				}
				
				@Override public void onMarkerReached(AudioRecord recorder) {} //ignore
			};
			
	        try {
	            recorder.setRecordPositionUpdateListener(l);
	            int result = recorder.setPositionNotificationPeriod(buff.length);
				if(result != AudioRecord.SUCCESS) {Log.e(TAG, "Error code " + result);};
	            recorder.startRecording();
                recorder.read(buff, 0, buff.length);
                synchronized (this) {
					wait(1000);
				}
	        } catch(InterruptedException i) {
	        	//expected
	        } catch(Throwable x) { 
	            Log.w("Audio", "Error reading audio", x);
	        }
	        /*
	         * Frees the thread's resources after the loop completes so that it can be run again
	         */
	        finally { 
	            recorder.stop();
	            recorder.release();
	        }
	    }
	}
}
