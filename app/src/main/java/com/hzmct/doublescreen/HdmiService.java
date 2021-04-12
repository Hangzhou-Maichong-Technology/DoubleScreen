package com.hzmct.doublescreen;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.blankj.utilcode.util.SPUtils;

public class HdmiService extends Service {
	private static final String TAG = "HdmiService";
	
	private DisplayManager mDisplayManager;
	private static MyPresentation myPresentation;

	public IBinder onBind(Intent paramIntent) {
		return null;
	}

	private BroadcastReceiver HdmiReceiver = new BroadcastReceiver() {
		@SuppressLint("NewApi")
		public void onReceive(Context context, Intent intent) {
			System.out.println("xxx");
			if (intent.getAction().equals("android.set")) {
				if (intent.getIntExtra("paramInt", 0) == 2) {
					if (myPresentation != null) {
						myPresentation.releaseMediaPlayer();
						myPresentation.dismiss();
						myPresentation.cancel();
						myPresentation = null;
					}
				} else {
					String path = SPUtils.getInstance().getString(MainActivity.RK_VIDEO_PATH);
					Log.i(TAG, "HdmiRecevier video path == " + path);
					updateContents(intent.getIntExtra("paramInt", 0), path);
				}
			}
			registerMountEvent();
		}
	};

	public void onCreate() {
		super.onCreate();

		String path = SPUtils.getInstance().getString(MainActivity.RK_VIDEO_PATH, MainActivity.DEFAULT_PATH);
		Log.i(TAG, "path == " + path);

		updateContents(1, path);
		registerMountEvent();
	}

	@SuppressLint("NewApi")
	public void updateContents(int state, String path) {
		mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
		Display[] displays = mDisplayManager.getDisplays();
		Log.i(TAG, "hdmi path ==>" + displays.length);
		showPresentation(displays[state], path);
	}

	@SuppressLint("NewApi")
	private void showPresentation(Display display, String path) {
		if (myPresentation != null) {
			myPresentation.releaseMediaPlayer();
			myPresentation.cancel();
			myPresentation = null;
		}

		myPresentation = new MyPresentation(this, display, path);
		myPresentation.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				//todo 监听消失，保存当前播放位置。
			}
		});
		myPresentation.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
		myPresentation.show();
	}

	private void registerMountEvent() {
		IntentFilter localIntentFilter = new IntentFilter();
		localIntentFilter.addAction("android.set");
		registerReceiver(this.HdmiReceiver, localIntentFilter);
	}

	private void unregisterMountEvent() {
		unregisterReceiver(this.HdmiReceiver);
	}

	public void onDestroy() {
		super.onDestroy();
		unregisterMountEvent();
	}
}
