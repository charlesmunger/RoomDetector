package edu.ucsb.ece251.charlesmunger.roomdetector;

import java.io.Closeable;
import java.util.Arrays;

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
		
		return true;
	}

	/*
	 * Thread to manage live recording/playback of voice input from the device's
	 * microphone.
	 */
	private class Audio extends Thread implements Closeable {
		private static final int FFT_FREQUENCY = 20000;
		private boolean stopped = false;

		/**
		 * Give the thread high priority so that it's not canceled unexpectedly,
		 * and start it
		 */
		private Audio() {
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		}

		@Override
	    public void run() { 
	        Log.i("Audio", "Running Audio Thread");
	        AudioRecord recorder = null;
	        AudioTrack track = null;
	        short[][]   buffers  = new short[256][160];
	        int ix = 0;
	        Visualizer v = null;
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
	            track.play();
	            //am.setMode(AudioManager.MODE_IN_CALL);
	            am.setBluetoothScoOn(true);
	            Log.i(TAG,"Session for mic is " + recorder.getAudioSessionId());
	            v = new Visualizer(track.getAudioSessionId());
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
//						Log.i(TAG,Arrays.toString(fft));
//						if(fft[getIndex(visualizer)] > ) {
//							
//						}
					}

					private int getIndex(Visualizer visualizer) {
						return visualizer.getCaptureSize()/2 /visualizer.getSamplingRate();
					}
				};
				int result = 0;
				result = v.setDataCaptureListener(listener, FFT_FREQUENCY, false, true);
				Log.d(TAG, "Result for listener is "+result);
				result = v.setEnabled(true);
				Log.d(TAG, "Result for enabled is "+result);
	            /*
	             * Loops until something outside of this thread stops it.
	             * Reads the data from the recorder and writes it to the audio track for playback.
	             */
	            while(!this.isInterrupted()) { 
//	                //Log.i("Map", "Writing new data to buffer");
	                short[] buffer = buffers[ix++ % buffers.length];
	                N = recorder.read(buffer,0,buffer.length);
	                track.write(buffer, 0, buffer.length);
	            }
	        }
	        catch(Throwable x) { 
	            Log.w("Audio", "Error reading voice audio", x);
	        }
	        /*
	         * Frees the thread's resources after the loop completes so that it can be run again
	         */
	        finally
	        { 
	        	v.setEnabled(false);
	        	v.release();
	            recorder.stop();
	            recorder.release();
	            track.stop();
	            track.release();
	            am.setStreamMute(AudioManager.STREAM_MUSIC, false);
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
