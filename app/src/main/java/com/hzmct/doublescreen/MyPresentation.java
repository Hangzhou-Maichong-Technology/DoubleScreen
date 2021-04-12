package com.hzmct.doublescreen;

import android.annotation.SuppressLint;
import android.app.Presentation;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SPUtils;

import java.io.File;

@SuppressLint("NewApi")
@SuppressWarnings("unused")
public class MyPresentation extends Presentation implements
		OnCompletionListener, OnPreparedListener, OnVideoSizeChangedListener,
		SurfaceHolder.Callback {

	public static MediaPlayer mMediaPlayer;
	public static SurfaceView mPreview;
	private Context mContext;
	private SurfaceHolder holder;
	private String videoPath = "";

	/**
	 * public Presentation (Context outerContext, Display display)
	 * 这个初始化函数，这里的outerContext必须要activity，一个activity的context，
	 * 虽然presentation会创建自己的context，但是它是在这个参数context之上的，
	 * 而且这个activity跳转后presentation就消失了，如果说用getApplicationContext()就直接报错，也不行。
	 */
	public MyPresentation(Context context, Display display, String path) {
		super(context, display);
		this.mContext = context;
		this.videoPath = path;
		releaseMediaPlayer();
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show);

		mPreview = (SurfaceView) findViewById(R.id.show_view);
		holder = mPreview.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		playVideo(mContext);
	}

	public void playVideo(Context context) {
		try {
			String path = SPUtils.getInstance().getString(MainActivity.RK_VIDEO_PATH, MainActivity.DEFAULT_PATH);

			// 创建一个MediaPlayer对象
            if (FileUtils.isFileExists(videoPath)) {
				// 设置播放的视频数据源
				mMediaPlayer = new MediaPlayer();
				// 设置播放的视频数据源
				mMediaPlayer.setDataSource(videoPath);
				mMediaPlayer.prepareAsync();
			} else {
				mMediaPlayer = MediaPlayer.create(context.getApplicationContext(), R.raw.abcd);
			}

			// 将视频输出到SurfaceView
			mMediaPlayer.setDisplay(holder);
			mMediaPlayer.setOnVideoSizeChangedListener(this);
			mMediaPlayer.setOnCompletionListener(this);
			mMediaPlayer.setOnPreparedListener(this);
			mMediaPlayer.setLooping(true);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void restartPlayVideo() {
		releaseMediaPlayer();

		mPreview = (SurfaceView) findViewById(R.id.show_view);
		holder = mPreview.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		playVideo(mContext);
	}

	public void onCompletion(MediaPlayer arg0) {
		try {
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}

			restartPlayVideo();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 获得视频的宽和高
	public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {
	}

	// 实现OnPreparedListener中的方法，当视频准备完毕会调用这个回调方法，开始播放
	public void onPrepared(MediaPlayer mediaplayer) {
		startVideoPlayback();
	}

	public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
	}

	public void surfaceDestroyed(SurfaceHolder surfaceholder) {
	}

	public void surfaceCreated(SurfaceHolder holder) {
		playVideo(mContext);
	}

	// 释放MediaPlayer
	public void releaseMediaPlayer() {
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}

		if (holder != null) {
			holder = null;
		}
	}

	// 播放视频的方法
	private void startVideoPlayback() {
		mMediaPlayer.start();
	}
}