package edu.ucsb.ece251.charlesmunger.roomdetector;

import java.io.Closeable;

import org.hermit.dsp.FFTTransformer;

import roboguice.service.RoboIntentService;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.media.MediaRecorder.AudioSource;
import android.os.PowerManager;
import android.util.Log;

import com.google.inject.Inject;

public class InRoomService extends RoboIntentService {

	private static final String TAG = "InRoomService";
	public static final String PENDING_INTENT = "PendingIntent";
	@Inject
	PowerManager pm;
	@Inject
	AudioManager am;

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
				Log.d(TAG,
						"Launching intent"
								+ ((Intent) intent
										.getParcelableExtra(PENDING_INTENT))
										.getAction());
				startService(((Intent) intent
						.getParcelableExtra(PENDING_INTENT)));
			}
		} finally {
			wl.release();
		}
	}

	private boolean inRoom() {
		Audio a = new Audio();
		a.start();
		try {
			a.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		a.close();

		return a.inRoom;
	}

	/*
	 * Thread to manage live recording/playback of voice input from the device's
	 * microphone.
	 */
	private class Audio extends Thread implements Closeable {
		private static final int SAMPLE_RATE = 44100;
		private static final int FFT_FREQUENCY = 20000;
		private boolean stopped = false;
		public boolean inRoom = false;

		/**
		 * Give the thread high priority so that it's not canceled unexpectedly,
		 * and start it
		 */
		private Audio() {
			android.os.Process
					.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		}

		@Override
	    public void run() { 
	        Log.i("Audio", "Running Audio Thread");
	        AudioRecord recorder = null;
	        final short[] buff = new short[2*1024];
	        final float[] spectrumData = new float[1024];

	        final FFTTransformer ft = new FFTTransformer(1024*2);
	        /*
	         * Initialize buffer to hold continuously recorded audio data, start recording, and start
	         * playback.
	         */
	        
	        int N = AudioRecord.getMinBufferSize(SAMPLE_RATE,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
            recorder = new AudioRecord(AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, N*10);
            		OnRecordPositionUpdateListener l = new OnRecordPositionUpdateListener() {

				@Override
				public void onPeriodicNotification(AudioRecord recorder) {
					recorder.read(buff, 0, buff.length);
					ft.setInput(buff, 0, buff.length);
					ft.transform();
					ft.getResults(spectrumData);
					float max = Float.MIN_VALUE;
					int bucket = 0;
					for(int i = 700;i<spectrumData.length;i++) {
						if(spectrumData[i] > max) {
							max = spectrumData[i];
							bucket = i;
						}
					}
					if(840 == Math.max(bucket, 840) && 830 == Math.min(bucket, 830)) { //determined empirically 
						Audio.this.inRoom = true;
						Audio.this.interrupt();
					}
				}
				
				@Override
				public void onMarkerReached(AudioRecord recorder) {
					///Ignore
				}
			};
			
	        try {
	            
	            recorder.setRecordPositionUpdateListener(l);
	            int result = recorder.setPositionNotificationPeriod(buff.length);
				if(result != AudioRecord.SUCCESS) {Log.e(TAG, "Error code " + result);};
	            recorder.startRecording();
                recorder.read(buff, 0, buff.length);
                synchronized (this) {
					wait(500);
				}
	        } catch(InterruptedException i) {
	        	
	        } catch(Throwable x) { 
	            Log.w("Audio", "Error reading voice audio", x);
	        }
	        /*
	         * Frees the thread's resources after the loop completes so that it can be run again
	         */
	        finally { 
	            recorder.stop();
	            recorder.release();
	        }
	    }

		/**
		 * Called from outside of the thread in order to stop the
		 * recording/playback loop
		 */
		public void close() {
			stopped = true;
		}
	}
}
