package com.hzmct.doublescreen;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.MediaController;
import android.widget.VideoView;

import com.blankj.utilcode.util.SPUtils;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    public static final String RK_VIDEO_PATH = "rk_video_path";
    public static final String DEFAULT_PATH = "/mnt/external_sd/test.mp4";
    private static final int REQUEST_CODE_OVERLAY = 0x11;

	VideoView mVideoView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		requestOverlayPermission();
	}

	private void requestOverlayPermission() {
		if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
			Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
					Uri.parse("package:" + getPackageName()));
			startActivityForResult(intent, REQUEST_CODE_OVERLAY);
		} else {
		    init();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_OVERLAY) {
			init();
		}
	}

	private void init() {
		Intent it = getIntent();
		if (it != null) {
			try {
				SPUtils.getInstance().put(RK_VIDEO_PATH, it.getData().getPath());
				Log.i(TAG, "path == " + it.getData().getPath());

				if (isServiceRunning()) {
					Intent broadCastIntent = new Intent();
					broadCastIntent.setAction("android.set");
					broadCastIntent.putExtra("paramInt", 1);
					sendBroadcast(broadCastIntent);

					finish();
				} else {
					Log.i(TAG, "new service ...");
					Intent newIntent = new Intent(MainActivity.this, HdmiService.class);
					startService(newIntent);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (isServiceRunning()) {
			Log.i(TAG, "this service is running ....");
		} else {
			Log.i(TAG, "new service ....");
			Intent newIntent = new Intent(MainActivity.this, HdmiService.class);
			startService(newIntent);
		}

		mVideoView = (VideoView) findViewById(R.id.video_view);
		initVideo();
		playVideo();
	}

	private boolean isServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (HdmiService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private void initVideo() {
		mVideoView.setMediaController(new MediaController(this));

		mVideoView.setOnPreparedListener(new OnPreparedListener() {
			public void onPrepared(MediaPlayer mediaplayer) {
				mediaplayer.start();
			}
		});

		mVideoView.setOnCompletionListener(new OnCompletionListener() {

			public void onCompletion(MediaPlayer mediaplayer) {
				mVideoView.pause();
				playVideo();
			}
		});

		mVideoView.setOnErrorListener(new OnErrorListener() {
			public boolean onError(MediaPlayer mediaplayer, int i, int j) {
				mVideoView.pause();
				return true;
			}
		});

		MediaController mc = new MediaController(this);
		mc.setVisibility(View.INVISIBLE);
		mVideoView.setMediaController(mc);
	}

	private void playVideo() {
		mVideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video));
		mVideoView.start();
	}

	// 异显
	public void showDifferent(View v) {
		setState(1, DEFAULT_PATH);
	}

	// 同显
	public void showWith(View v) {
		setState(0, DEFAULT_PATH);
	}

	public void setState(int value, String path) {
		SPUtils.getInstance().put(RK_VIDEO_PATH, path);

		Intent broadCastIntent = new Intent();
		broadCastIntent.setAction("android.set");
		broadCastIntent.putExtra("paramInt", value);
		sendBroadcast(broadCastIntent);
	}
}
