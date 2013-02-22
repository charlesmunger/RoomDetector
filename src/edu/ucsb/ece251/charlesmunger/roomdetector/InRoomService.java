package edu.ucsb.ece251.charlesmunger.roomdetector;

import roboguice.service.RoboIntentService;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.OnDataCaptureListener;
import android.os.PowerManager;
import android.util.Log;

import com.google.inject.Inject;

public class InRoomService extends RoboIntentService {

	private static final String TAG = "InRoomService";
	public static final String PENDING_INTENT = "PendingIntent";
	private static final int BEEP_FREQUENCY = 18000;
	private static final float BEEP_LOUDNESS_THRESHOLD = 0.2f;
	@Inject
	PowerManager pm;

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
		new Audio().start();
		return true;
	}

	/*
	 * Thread to manage live recording/playback of voice input from the device's
	 * microphone.
	 */
	private class Audio extends Thread {
		private boolean stopped = false;

		/**
		 * Give the thread high priority so that it's not canceled unexpectedly,
		 * and start it
		 */
		private Audio() {
			android.os.Process
					.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
			start();
		}

		@Override
	    public void run() { 
	        Log.i("Audio", "Running Audio Thread");
	        AudioRecord recorder = null;
	        AudioTrack track = null;
	        short[][]   buffers  = new short[256][160];
	        int ix = 0;

	        /*
	         * Initialize buffer to hold continuously recorded audio data, start recording, and start
	         * playback.
	         */
	        try {
	            int N = AudioRecord.getMinBufferSize(8000,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
	            recorder = new AudioRecord(AudioSource.MIC, 8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, N*10);
	            track = new AudioTrack(AudioManager.STREAM_MUSIC, 8000, 
	                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, N*10, AudioTrack.MODE_STREAM);
	            recorder.startRecording();
	            Visualizer v = new Visualizer(track.getAudioSessionId());
	            final OnDataCaptureListener listener = new OnDataCaptureListener() {
					private int average = 0;
					private int min = Integer.MIN_VALUE;
					private int max = Integer.MAX_VALUE;
					@Override
					public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform,
							int samplingRate) { //do nothing
					}
					
					@Override
					public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
						
						// Bucket index of BEEP_FREQUENCY in fft data
						int k = getIndex(visualizer); 	
					
						int difference = Math.abs(fft[k]-fft[k+1]);
						
						if (difference / fft[k] > BEEP_LOUDNESS_THRESHOLD) {
							// Beep detected
						}
					}

					private int getIndex(Visualizer visualizer) {
						return BEEP_FREQUENCY*visualizer.getCaptureSize()/(visualizer.getSamplingRate()*2);
					}
				};
				v.setDataCaptureListener(listener, rate, waveform, fft)
	            /*
	             * Loops until something outside of this thread stops it.
	             * Reads the data from the recorder and writes it to the audio track for playback.
	             */
	            while(!stopped)
	            { 
	                Log.i("Map", "Writing new data to buffer");
	                short[] buffer = buffers[ix++ % buffers.length];
	                N = recorder.read(buffer,0,buffer.length);
	                track.write(buffer, 0, buffer.length);
	                
	            }
	        }
	        catch(Throwable x)
	        { 
	            Log.w("Audio", "Error reading voice audio", x);
	        }
	        /*
	         * Frees the thread's resources after the loop completes so that it can be run again
	         */
	        finally
	        { 
	            recorder.stop();
	            recorder.release();
	            track.stop();
	            track.release();
	        }
	    }

		/**
		 * Called from outside of the thread in order to stop the
		 * recording/playback loop
		 */
		private void close() {
			stopped = true;
		}

	}
}
