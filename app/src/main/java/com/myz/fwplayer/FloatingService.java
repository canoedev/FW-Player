package com.myz.fwplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AbsoluteLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class FloatingService extends Service {

  private WindowManager mWindowManager;
  private WindowManager.LayoutParams mLayoutParams;
  private Timer mUpdateTimer;
  private Timer mLayoutTimer;
  private Timer mVolumeTimer;
  private MyReceiver myReceiver;
  private SharedPreferences mSp;
  private SharedPreferences.Editor mSpEd;

  private String uri;// 传入的 Uri
  private String name;// 传入的文件名称
  private boolean isBufferingFinished=true;
  private boolean isActivityStarted=false;
  private boolean isVideo=false;
  private boolean isZoomed=false;
  private boolean isVolumeMin=false;
  private boolean isVolumeMax=false;
  private boolean rotaty;
  private boolean loop;
  private boolean restart;
  private int lastTime;
  private int volume;
  private float leftVolume=1.0f;
  private float rightVolume=1.0f;
  private float speed=1.0f;
  private float pitch=1.0f;

  private View mDisplayView;
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

  private Handler mHandler = new Handler(){
	@Override
	public void handleMessage(Message msg) {
	  if (msg.what == 0) {
		mSbProgress.setProgress(mVideoView.getCurrentPosition());
		mTvTime.setText(ms2hms(mVideoView.getCurrentPosition()));
	  } else if (msg.what == 1) {
		mLlTop.setVisibility(View.GONE);
		mLlBottom.setVisibility(View.GONE);
		stopUpdateTimer();
		stopLayoutTimer();
	  }
	}
  };

  @Override
  public IBinder onBind(Intent p1) {
	// TODO: Implement this method
	return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId)  {
	Notification.Builder builder = new Notification.Builder(this);
	builder.setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
	  .setSmallIcon(R.drawable.ic_music_note_white_24dp)
	  .setContentTitle("悬浮播放服务")
	  .setContentText("悬浮播放服务正在运行")
	  .setWhen(System.currentTimeMillis());
	Notification notification = builder.build();
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
	  NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	  NotificationChannel notificationChannel = new NotificationChannel(
		"play", "悬浮播放服务" , NotificationManager.IMPORTANCE_MIN);
	  manager.createNotificationChannel(notificationChannel);

	  builder.setChannelId("play");
	}
	startForeground(1, notification);

	uri = intent.getStringExtra("uri");// 获取传入的 Uri 数据
	name = intent.getStringExtra("name");// 获取传入的文件名称

	mSp = getSharedPreferences("data", MODE_PRIVATE);
	mSpEd = mSp.edit();

	rotaty = mSp.getBoolean("rotaty", false);
	loop = mSp.getBoolean("loop", true);
	restart = mSp.getBoolean("restart", false);
	leftVolume = mSp.getFloat("left", 1.0f);
	rightVolume = mSp.getFloat("right", 1.0f);
	speed = mSp.getFloat("speed", 1.0f);
	pitch = mSp.getFloat("pitch", 1.0f);

	showFloatingWindow();// 显示悬浮窗

	myReceiver = new MyReceiver();
	IntentFilter intentFilter = new IntentFilter();
	intentFilter.addAction("com.myz.fwplayer.ADJUST_ACTION");
	intentFilter.addAction("com.myz.fwplayer.CHANGE_ACTION");
	intentFilter.addAction("com.myz.fwplayer.GOTO_ACTION");
	intentFilter.addAction("com.myz.fwplayer.CANCEL_ACTION");
	intentFilter.addAction("com.myz.fwplayer.PAUSED_ACTION");
	registerReceiver(myReceiver, intentFilter);

	return super.onStartCommand(intent, flags, startId);
  }

  private void showFloatingWindow() {
	mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

	LayoutInflater layoutInflater = LayoutInflater.from(this);
	mDisplayView = layoutInflater.inflate(R.layout.view_play, null);

	initLayout();// 初始化布局控件
	mIbMore.setVisibility(View.INVISIBLE);
	mTvName.setVisibility(View.INVISIBLE);
	mLlBottom.setVisibility(View.GONE);

	mLayoutParams = new WindowManager.LayoutParams();
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {// Android 8.0及以上
	  mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
	} else {// Android 8.0以下
	  mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
	}
	mLayoutParams.format = PixelFormat.RGBA_8888;
	mLayoutParams.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 180, getResources().getDisplayMetrics());
	mLayoutParams.height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics());
	mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
	mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
	mWindowManager.addView(mDisplayView, mLayoutParams);

	mDisplayView.setOnTouchListener(new FloatingOnTouchListener());

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

				  mLlView.setVisibility(View.GONE);
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
					mLayoutParams.width = LayoutParams.WRAP_CONTENT;
				  }
				  mLayoutParams.height = LayoutParams.WRAP_CONTENT;
				  mWindowManager.updateViewLayout(mDisplayView, mLayoutParams);

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
		  Toast.makeText(FloatingService.this, "无法播放 (" + p2 + ", " + p3 + ")", Toast.LENGTH_SHORT).show();
		  if (restart) {
			mVideoView.stopPlayback();
			mVideoView.setVideoURI(Uri.parse(uri));
			mVideoView.start();
		  } else {
			stopSelf();
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
		  if (!isActivityStarted) {
			isActivityStarted = true;
			Intent menu=new Intent(FloatingService.this, MenuActivity.class);
			menu.putExtra("uri", uri);
			menu.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(menu);
		  }
		}
	  });

	mIbClose.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  stopSelf();
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
  public void onDestroy() {
	if (mWindowManager != null) {
	  mWindowManager.removeView(mDisplayView);
	}
	stopUpdateTimer();
	stopVolumeTimer();
	stopLayoutTimer();
	if (mHandler != null) {
	  mHandler.removeCallbacksAndMessages(null);
	}
	unregisterReceiver(myReceiver);
	super.onDestroy();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
	super.onConfigurationChanged(newConfig);
	if (isVideo) {
	  if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
		mLayoutParams.width = LayoutParams.WRAP_CONTENT;
	  } else {
		mLayoutParams.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());
	  }
	  mWindowManager.updateViewLayout(mDisplayView, mLayoutParams);
	}
  }

  // 初始化布局控件
  private void initLayout() {
	mVideoView = mDisplayView.findViewById(R.id.video_view);
	mLlView = mDisplayView.findViewById(R.id.ll_view);
	mPbLoad = mDisplayView.findViewById(R.id.pb_load);
	mPbLoad.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.theme), PorterDuff.Mode.SRC_ATOP);

	mLlTop = mDisplayView.findViewById(R.id.ll_top);
	mIbMore = mDisplayView.findViewById(R.id.ib_more);
	mTvName = mDisplayView.findViewById(R.id.tv_name);
	mIbClose = mDisplayView.findViewById(R.id.ib_close);
	mLlBottom = mDisplayView.findViewById(R.id.ll_bottom);
	mTvTime = mDisplayView.findViewById(R.id.tv_time);
	mSbProgress = mDisplayView.findViewById(R.id.sb_progress);
	mTvDuration = mDisplayView.findViewById(R.id.tv_duration);
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
	  } else if (intent.getAction().equals("com.myz.fwplayer.GOTO_ACTION")) {
		if (mVideoView.getDuration() > 0) {
		  mSpEd.putInt("cache_time", mVideoView.getCurrentPosition());
		  mSpEd.commit();
		}
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
	  } else if (intent.getAction().equals("com.myz.fwplayer.CANCEL_ACTION") || intent.getAction().equals("com.myz.fwplayer.PAUSED_ACTION")) {
		isActivityStarted = false;
	  }
	}
  }

  private class FloatingOnTouchListener implements View.OnTouchListener {
    private int x;
    private int y;
	private int firstX;
	private int firstY;
	private int lastX;
	private int lastY;
	private int touchX;
	private int touchY;
	private boolean zoom=false;
	@Override
	public boolean onTouch(View view, MotionEvent event) {
	  switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		  x = (int) event.getRawX();// 按下时触摸点与屏幕左边界的距离
		  y = (int) event.getRawY();// 按下时触摸点与屏幕上边界的距离
		  firstX = (int)event.getRawX();
		  firstY = (int)event.getRawY();
		  touchX = (int) event.getX();// 按下时触摸点与窗口左边界的距离
		  touchY = (int) event.getY();// 按下时触摸点与窗口上边界的距离

		  if (isVideo) {
			float dp36=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, getResources().getDisplayMetrics());
			if (mVideoView.getWidth() - touchX <= dp36 && mVideoView.getHeight() - touchY <= dp36) {
			  zoom = true;
			}
		  }
		  break;
		case MotionEvent.ACTION_MOVE:
		  int nowX = (int) event.getRawX();// 移动时触摸点与屏幕左边界的距离
		  int nowY = (int) event.getRawY();// 移动时触摸点与屏幕上边界的距离
		  int movedX = nowX - x;// 移动时相对于按下时触摸点在屏幕上往右移动的距离
		  int movedY = nowY - y;// 移动时相对于按下时触摸点在屏幕上往下移动的距离
		  x = nowX;// 更新按下时触摸点与屏幕左边界的距离
		  y = nowY;// 更新按下时触摸点与屏幕上边界的距离

		  if (zoom) {
			DisplayMetrics dm = FloatingService.this.getResources().getDisplayMetrics(); 
			int screenWidth = dm.widthPixels; 
			int screenHeight = dm.heightPixels; 
			int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
			if (resourceId > 0) {
			  screenHeight -= getResources().getDimensionPixelSize(resourceId);
			}
			boolean isMin=false;
			boolean isMax=false;
			float dp180=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 180, getResources().getDisplayMetrics());
			if (mVideoView.getWidth() <= dp180) {
			  isMin = true;
			  if (movedX > 0) {
				isMin = false;
			  }
			}
			if (mVideoView.getWidth() >= screenWidth) {
			  isMax = true;
			  if (movedX < 0) {
				isMax = false;
			  }
			}
			if (mVideoView.getHeight() >= screenHeight) {
			  isMax = true;
			  if (movedX < 0) {
				isMax = false;
			  }
			}
			if (!isMin && !isMax) {
			  isZoomed = true;
			  mLayoutParams.width = mVideoView.getWidth() + movedX;
			}
		  } else {
			mLayoutParams.x = mLayoutParams.x + movedX;// 使窗口距屏幕左边界的距离增加右移距离
			mLayoutParams.y = mLayoutParams.y + movedY;// 使窗口距屏幕上边界的距离增加下移距离
		  }
		  mWindowManager.updateViewLayout(view, mLayoutParams);// 更新悬浮窗控件布局
		  break;
		case MotionEvent.ACTION_UP:
		  lastX = (int)event.getRawX();
		  lastY = (int)event.getRawY();
		  if (zoom) {
			zoom = false;
		  }
		  if (lastX - firstX == 0 && lastY - firstY == 0) {// 判断为点击事件
			if (isVideo) {
			  if (mVideoView.isPlaying()) {
				if (mLayoutTimer != null) {
				  mVideoView.pause();
				}
				if (!isBufferingFinished) {
				  mPbLoad.setVisibility(View.GONE);
				}
				stopUpdateTimer();
			  } else {
				if (mLayoutTimer != null) {
				  mVideoView.start();
				}
				if (!isBufferingFinished) {
				  mPbLoad.setVisibility(View.VISIBLE);
				}
				startUpdateTimer();
			  }
			  mLlTop.setVisibility(View.VISIBLE);
			  mLlBottom.setVisibility(View.VISIBLE);
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
		  } else {
			// 移动后抬起
		  }
		  break;
		default:
		  break;
	  }
	  return false;
	}
  }
}
