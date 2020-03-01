package com.myz.fwplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class PlayActivity extends AppCompatActivity {

  private Timer mUpdateTimer;
  private Timer mLayoutTimer;
  private Timer mVolumeTimer;
  private MyReceiver myReceiver;
  private SharedPreferences mSp;
  private SharedPreferences.Editor mSpEd;

  private String uri;// 传入的 Uri
  private String name;// 传入的文件名称
  private boolean isBufferingFinished=true;
  private boolean isVideo=false;
  private boolean isZoomed=false;
  private boolean isVolumeMin=false;
  private boolean isVolumeMax=false;
  private boolean rotaty;
  private boolean loop;
  private boolean restart;
  private int cacheTime;
  private int lastTime;
  private int volume;
  private float leftVolume=1.0f;
  private float rightVolume=1.0f;
  private float speed=1.0f;
  private float pitch=1.0f;

  private RelativeLayout mRlParent;
  private VideoView mVideoView;
  private LinearLayout mLlTop;
  private ImageButton mIbMore;
  private TextView mTvName;
  private ImageButton mIbClose;
  private LinearLayout mLlBottom;
  private TextView mTvTime;
  private SeekBar mSbProgress;
  private TextView mTvDuration;
  private ProgressBar mPbLoad;
  private LinearLayout mLlView;
  private ImageButton mIbEvent;

  private Handler mHandler = new Handler(){
	@Override
	public void handleMessage(Message msg) {
	  if (msg.what == 0) {
		mSbProgress.setProgress(mVideoView.getCurrentPosition());
		mTvTime.setText(ms2hms(mVideoView.getCurrentPosition()));
	  } else if (msg.what == 1) {
		mLlTop.setVisibility(View.GONE);
		mLlBottom.setVisibility(View.GONE);
		mIbEvent.setVisibility(View.GONE);
		stopUpdateTimer();
		stopLayoutTimer();
	  }
	}
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_play);

	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
	  getWindow().setStatusBarColor(getResources().getColor(R.color.background));
	  getWindow().setNavigationBarColor(getResources().getColor(R.color.background));
	  getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
	}

	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 屏幕常亮

	initLayout();

	uri = getIntent().getStringExtra("uri");// 获取传入的 Uri 数据
	name = getIntent().getStringExtra("name");// 获取传入的文件名称

	mSp = getSharedPreferences("data", MODE_PRIVATE);
	mSpEd = mSp.edit();

	rotaty = mSp.getBoolean("rotaty", false);
	loop = mSp.getBoolean("loop", true);
	restart = mSp.getBoolean("restart", false);
	cacheTime = mSp.getInt("cache_time", 0);
	leftVolume = mSp.getFloat("left", 1.0f);
	rightVolume = mSp.getFloat("right", 1.0f);
	speed = mSp.getFloat("speed", 1.0f);
	pitch = mSp.getFloat("pitch", 1.0f);

	if (AppUtils.isRunService(this, getPackageName() + ".FloatingService")) {
	  stopService(new Intent(this, FloatingService.class));
	}

	initPlay();

	myReceiver = new MyReceiver();
	IntentFilter intentFilter = new IntentFilter();
	intentFilter.addAction("com.myz.fwplayer.ADJUST_ACTION");
	intentFilter.addAction("com.myz.fwplayer.CHANGE_ACTION");
	intentFilter.addAction("com.myz.fwplayer.CANCEL_ACTION");
	registerReceiver(myReceiver, intentFilter);
  }

  // 初始化播放操作
  private void initPlay() {
	mIbMore.setVisibility(View.INVISIBLE);
	mTvName.setVisibility(View.INVISIBLE);
	mLlBottom.setVisibility(View.GONE);
	mIbEvent.setVisibility(View.GONE);

	mIbMore.setImageResource(R.drawable.ic_more_vert_gray_24dp);
	mIbClose.setImageResource(R.drawable.ic_close_gray_24dp);
	mIbMore.setAlpha(0.5f);
	mIbClose.setAlpha(0.5f);

	mVideoView.setVideoURI(Uri.parse(uri));
	mVideoView.start();

	mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {  
		@Override  
		public void onPrepared(final MediaPlayer mp) {  

		  mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {  
			  @Override  
			  public boolean onInfo(MediaPlayer mp, int what, int extra) {  
				if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START)  {
				  isVideo = true;

				  getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

				  mLlView.setVisibility(View.GONE);
				  mRlParent.setBackgroundColor(getResources().getColor(R.color.black));
				  mVideoView.setBackground(null);
				  mSbProgress.getProgressDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

				  mLlTop.setBackgroundColor(getResources().getColor(R.color.bg_translate));
				  mLlBottom.setBackgroundColor(getResources().getColor(R.color.bg_translate));

				  mTvName.setTextColor(getResources().getColor(R.color.white));
				  mTvTime.setTextColor(getResources().getColor(R.color.white));
				  mTvDuration.setTextColor(getResources().getColor(R.color.white));

				  mIbMore.setImageResource(R.drawable.ic_more_vert_white_24dp);
				  mIbClose.setImageResource(R.drawable.ic_close_white_24dp);
				  mIbMore.setAlpha(1.0f);
				  mIbClose.setAlpha(1.0f);

				  Configuration mConfiguration = getResources().getConfiguration();
				  int ori = mConfiguration.orientation;
				  if (ori == mConfiguration.ORIENTATION_PORTRAIT && !isZoomed) {
				  }

				  startLayoutTimer();
				} else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START)  {
				  isBufferingFinished = false;
				  mPbLoad.setVisibility(View.VISIBLE);
				} else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END)  {
				  isBufferingFinished = true;
				  mPbLoad.setVisibility(View.GONE);
				}
				return true;  
			  }
			});

		  mPbLoad.setVisibility(View.GONE);
		  mIbMore.setVisibility(View.VISIBLE);
		  mTvName.setVisibility(View.VISIBLE);
		  mLlBottom.setVisibility(View.VISIBLE);

		  mIbEvent.setImageResource(R.drawable.ic_pause_white_24dp);
		  mTvName.setText(name);
		  mTvName.requestFocus();

		  if (mVideoView.getDuration() > 0) {
			mTvDuration.setText(ms2hms(mVideoView.getDuration()));
		  } else {
			mTvDuration.setText("直播");
		  }

		  mSbProgress.getThumb().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
		  mSbProgress.getProgressDrawable().setColorFilter(getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);
		  mSbProgress.setMax(mVideoView.getDuration());

		  if (cacheTime > 0) {
			mVideoView.seekTo(cacheTime);
			cacheTime = 0;
		  }
		  mSpEd.remove("cache_time");
		  mSpEd.commit();

		  if (lastTime > 0) {
			mVideoView.seekTo(lastTime);
		  }

		  if (loop) {
			mp.setLooping(true);
		  } else {
			mp.setLooping(false);
		  }

		  if (rotaty) {
			startVolumeTimer(mp);
		  } else {
			mp.setVolume(leftVolume, rightVolume);
		  }

		  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {// Android 6.0及以上
			if (speed != 1.0) {
			  mp.setPlaybackParams(mp.getPlaybackParams().setSpeed(speed)); 
			}
			if (pitch != 1.0) {
			  mp.setPlaybackParams(mp.getPlaybackParams().setPitch(pitch)); 
			}
		  }
		  startUpdateTimer();
		}
	  });

	mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener(){

		@Override
		public boolean onError(MediaPlayer p1, int p2, int p3) {
		  // TODO: Implement this method
		  Toast.makeText(PlayActivity.this, "无法播放 (" + p2 + ", " + p3 + ")", Toast.LENGTH_SHORT).show();
		  if (restart) {
			mVideoView.stopPlayback();
			mVideoView.setVideoURI(Uri.parse(uri));
			mVideoView.start();
		  } else {
			finish();
		  }
		  return true;
		}
	  });

	mIbMore.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  if (isVideo) {
			stopLayoutTimer();
			startLayoutTimer();
		  }
		  MenuUtils mu=new MenuUtils();
		  mu.showMenu(PlayActivity.this, 1);// 1 表示全屏模式菜单
		}
	  });

	mIbClose.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  finish();
		}
	  });

	mIbEvent.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  if (isVideo) {
			if (mVideoView.isPlaying()) {
			  if (mLayoutTimer != null) {
				mIbEvent.setImageResource(R.drawable.ic_play_arrow_white_24dp);
				mVideoView.pause();
			  }
			  if (!isBufferingFinished) {
				mPbLoad.setVisibility(View.GONE);
			  }
			  stopUpdateTimer();
			} else {
			  if (mLayoutTimer != null) {
				mIbEvent.setImageResource(R.drawable.ic_pause_white_24dp);
				mVideoView.start();
			  }
			  if (!isBufferingFinished) {
				mPbLoad.setVisibility(View.VISIBLE);
			  }
			  startUpdateTimer();
			  stopLayoutTimer();
			  startLayoutTimer();
			}
		  }
		}
	  });

	mRlParent.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  if (isVideo) {
			mLlTop.setVisibility(View.VISIBLE);
			mLlBottom.setVisibility(View.VISIBLE);
			mIbEvent.setVisibility(View.VISIBLE);
			mTvName.requestFocus();

			startUpdateTimer();
			stopLayoutTimer();
			startLayoutTimer();
		  } else {
			if (mVideoView.isPlaying()) {
			  mVideoView.pause();// 暂停
			  if (!isBufferingFinished) {
				mPbLoad.setVisibility(View.GONE);
			  }
			  stopUpdateTimer();
			} else {
			  mVideoView.start();// 播放
			  if (!isBufferingFinished) {
				mPbLoad.setVisibility(View.VISIBLE);
			  }
			  startUpdateTimer();
			}
		  }
		}
	  });

	mSbProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar p1, int p2, boolean p3) {
		  // TODO: Implement this method
		  mTvTime.setText(ms2hms(p2));
		}

		@Override
		public void onStartTrackingTouch(SeekBar p1) {
		  if (isVideo) {
			stopLayoutTimer();
		  }
		  if (mVideoView.getDuration() > 0 && mVideoView.isPlaying()) {
			stopUpdateTimer();
		  }
		  // TODO: Implement this method
		}

		@Override
		public void onStopTrackingTouch(SeekBar p1) {
		  if (isVideo) {
			startLayoutTimer();
		  }
		  int p = p1.getProgress();
		  if (mVideoView.getDuration() > 0) {
			mVideoView.seekTo(p);
			if (mVideoView.isPlaying()) {
			  startUpdateTimer();
			}
		  }
		  // TODO: Implement this method
		}
	  });
  }

  @Override
  public void onPause() {
	finish();
	super.onPause();
  }

  @Override
  public void onDestroy() {
	stopUpdateTimer();
	stopVolumeTimer();
	stopLayoutTimer();
	if (mHandler != null) {
	  mHandler.removeCallbacksAndMessages(null);
	}
	unregisterReceiver(myReceiver);
	super.onDestroy();
  }

  // 初始化布局控件
  private void initLayout() {
	mRlParent = findViewById(R.id.play_rl_parent);
	mVideoView = findViewById(R.id.play_video_view);
	mLlView = findViewById(R.id.play_ll_view);
	mPbLoad = findViewById(R.id.play_pb_load);
	mIbEvent = findViewById(R.id.play_ib_event);
	mPbLoad.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.theme), PorterDuff.Mode.SRC_ATOP);

	mLlTop = findViewById(R.id.play_ll_top);
	mIbMore = findViewById(R.id.play_ib_more);
	mTvName = findViewById(R.id.play_tv_name);
	mIbClose = findViewById(R.id.play_ib_close);
	mLlBottom = findViewById(R.id.play_ll_bottom);
	mTvTime = findViewById(R.id.play_tv_time);
	mSbProgress = findViewById(R.id.play_sb_progress);
	mTvDuration = findViewById(R.id.play_tv_duration);
  }

  // 毫秒转换成时分秒
  private String ms2hms(long time) {
	long hour=time / (60 * 60 * 1000);
	String hms=null;
	SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
	formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
	hms = formatter.format(time);
	int h_index=hms.indexOf(":");
	if (hour == 0) {
	  hms = hms.substring(h_index + 1);
	} else if (hour >= 24) {
	  hms = hour + hms.substring(h_index);
	}
	return hms;
  }

  // 开启用于更新的计时器
  private void startUpdateTimer() {
	if (mUpdateTimer == null) {
	  mUpdateTimer = new Timer();
	  mUpdateTimer.schedule(new TimerTask() {
		  @Override
		  public void run() {
			Message message=new Message();
			message.what = 0;
			mHandler.sendMessage(message);
		  }
		}, 0, 500);
	}
  }

  // 停止用于更新的计时器
  private void stopUpdateTimer() {
	if (mUpdateTimer != null) {
	  mUpdateTimer.cancel();
	  mUpdateTimer.purge();
	  mUpdateTimer = null;
	}
  }

  // 开启用于音效的计时器
  private void startVolumeTimer(final MediaPlayer mp) {
	if (mVolumeTimer == null) {
	  mVolumeTimer = new Timer();
	  mVolumeTimer.schedule(new TimerTask() {
		  @Override
		  public void run() {
			if (volume == 0) {
			  isVolumeMin = true;
			  isVolumeMax = false;
			} else if (volume == 100) {
			  isVolumeMin = false;
			  isVolumeMax = true;
			}
			if (isVolumeMin && !isVolumeMax) {
			  volume++;
			} else if (!isVolumeMin && isVolumeMax) {
			  volume--;
			}
			try {
			  mp.setVolume((float)(volume / 100.0), (float)((100 - volume) / 100.0));
			} catch (Exception e) {}
		  }
		}, 0, 50);
	}
  }

  // 停止用于音效的计时器
  private void stopVolumeTimer() {
	if (mVolumeTimer != null) {
	  mVolumeTimer.cancel();
	  mVolumeTimer.purge();
	  mVolumeTimer = null;
	}
  }

  // 开启用于布局的计时器
  private void startLayoutTimer() {
	if (mLayoutTimer == null) {
	  mLayoutTimer = new Timer();
	  mLayoutTimer.schedule(new TimerTask() {
		  @Override
		  public void run() {
			Message message=new Message();
			message.what = 1;
			mHandler.sendMessage(message);
		  }
		}, 3000, 3000);
	}
  }

  // 停止用于布局的计时器
  private void stopLayoutTimer() {
	if (mLayoutTimer != null) {
	  mLayoutTimer.cancel();
	  mLayoutTimer.purge();
	  mLayoutTimer = null;
	}
  }

  public class MyReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
	  if (intent.getAction().equals("com.myz.fwplayer.ADJUST_ACTION")) {
		mPbLoad.setVisibility(View.VISIBLE);
		stopVolumeTimer();
		if (mVideoView.getDuration() > 0) {
		  lastTime = mVideoView.getCurrentPosition();
		}
		rotaty = mSp.getBoolean("rotaty", false);
		leftVolume = mSp.getFloat("left", 1.0f);
		rightVolume = mSp.getFloat("right", 1.0f);
		mVideoView.stopPlayback();
		mVideoView.setVideoURI(Uri.parse(uri));
		mVideoView.start();
	  } else if (intent.getAction().equals("com.myz.fwplayer.CHANGE_ACTION")) {
		mPbLoad.setVisibility(View.VISIBLE);
		stopVolumeTimer();
		if (mVideoView.getDuration() > 0) {
		  lastTime = mVideoView.getCurrentPosition();
		}
		speed = mSp.getFloat("speed", 1.0f);
		pitch = mSp.getFloat("pitch", 1.0f);
		mVideoView.stopPlayback();
		mVideoView.setVideoURI(Uri.parse(uri));
		mVideoView.start();
	  } else if (intent.getAction().equals("com.myz.fwplayer.CANCEL_ACTION")) {
		if (isVideo) {
		  getWindow().getDecorView().setSystemUiVisibility(
			View.SYSTEM_UI_FLAG_LAYOUT_STABLE
			| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
			| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_FULLSCREEN
			| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
	  }
	}
  }
}
