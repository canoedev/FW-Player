package com.myz.fwplayer;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
  private SharedPreferences mSp;
  private SharedPreferences.Editor mSpEd;

  private Uri uri;

  private boolean loop;
  private boolean restart;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	getWindow().setStatusBarColor(getResources().getColor(R.color.translate));// 状态栏透明
	getWindow().setNavigationBarColor(getResources().getColor(R.color.translate));// 导航栏透明

	mSp = getSharedPreferences("data", MODE_PRIVATE);
	mSpEd = mSp.edit();

	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
	  if (checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
		requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 0);
	  }
	}

	final AlertDialog alertDialog = new AlertDialog.Builder(this)
	  .setTitle(getResources().getString(R.string.app_name))
	  .setView(R.layout.dialog_main)
	  .setNegativeButton("返回", new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface p1, int p2) {
		  // TODO: Implement this method
		  finish();
		}
	  })
	  .setOnCancelListener(new DialogInterface.OnCancelListener(){

		@Override
		public void onCancel(DialogInterface p1) {
		  // TODO: Implement this method
		  finish();
		}
	  })
	  .show();
	alertDialog.setCanceledOnTouchOutside(false);
	TextView title=alertDialog.getWindow().findViewById(R.id.alertTitle);
	title.setTextColor(getResources().getColor(R.color.theme));
	TextPaint tp_title = title.getPaint();
	tp_title.setFakeBoldText(true);

	Button positive=alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
	positive.setTextColor(getResources().getColor(R.color.theme));
	TextPaint tp_positive = positive.getPaint();
	tp_positive.setFakeBoldText(true);

	Button negative=alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
	negative.setTextColor(getResources().getColor(R.color.gray));
	TextPaint tp_negative = negative.getPaint();
	tp_negative.setFakeBoldText(true);

	LinearLayout ll_choose=alertDialog.findViewById(R.id.ll_choose);
	LinearLayout ll_stream=alertDialog.findViewById(R.id.ll_stream);
	LinearLayout ll_setting=alertDialog.findViewById(R.id.ll_setting);
	LinearLayout ll_help=alertDialog.findViewById(R.id.ll_help);

	ll_choose.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  if (AppUtils.haveWindowPermission(MainActivity.this)) {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("*/*");
			intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"video/*", "audio/*"});
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			startActivityForResult(intent, 1);
		  } else {
			AppUtils.showWindowDialog(MainActivity.this);
		  }
		}
	  });

	ll_stream.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  showStreamDialog();
		}
	  });

	ll_setting.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  showSettingDialog();
		}
	  });

	ll_help.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  showHelpDialog();
		}
	  });
  }

  @Override
  public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
	if (requestCode == 0) {
	  if (grantResults.length > 0) {
		if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
		  if (shouldShowRequestPermissionRationale("android.permission.WRITE_EXTERNAL_STORAGE")) {
			AppUtils.showStorageDialogA(this);
		  } else {
			AppUtils.showStorageDialogB(this);
		  }
		}
	  }
	}
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	if (requestCode == 0) {
	  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
		if (checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
		  requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 0);
		}
	  }
	} else if (requestCode == 1) {
	  if (AppUtils.haveWindowPermission(this)) {
		if (data != null) {
		  if (AppUtils.isRunService(this, getPackageName() + ".FloatingService")) {// 悬浮播放服务正在运行
			stopService(new Intent(this, FloatingService.class));// 停止悬浮播放服务
		  }
		  Intent playService=new Intent(this, FloatingService.class);
		  playService.putExtra("uri", AppUtils.getUri(data.getData(), this).toString());
		  playService.putExtra("name", AppUtils.getUri(data.getData(), this).getLastPathSegment());
		  startService(playService);// 开启悬浮播放服务
		}
	  } else {
		AppUtils.showWindowDialog(this);
	  }
	}
  }

  // 网络串流对话框
  private void showStreamDialog() {
	final AlertDialog alertDialog = new AlertDialog.Builder(this)
	  .setTitle("网络串流")
	  .setView(R.layout.dialog_stream)
	  .setPositiveButton("确定", null)
	  .setNegativeButton("取消", null)
	  .show();
	alertDialog.setCanceledOnTouchOutside(false);
	TextView title=alertDialog.getWindow().findViewById(R.id.alertTitle);
	title.setTextColor(getResources().getColor(R.color.theme));
	TextPaint tp_title = title.getPaint();
	tp_title.setFakeBoldText(true);

	Button positive=alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
	positive.setTextColor(getResources().getColor(R.color.theme));
	TextPaint tp_positive = positive.getPaint();
	tp_positive.setFakeBoldText(true);

	Button negative=alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
	negative.setTextColor(getResources().getColor(R.color.gray));
	TextPaint tp_negative = negative.getPaint();
	tp_negative.setFakeBoldText(true);

	final EditText et_stream = alertDialog.findViewById(R.id.et_stream);

	positive.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  if (!et_stream.getText().toString().equals("")) {
			if (AppUtils.haveWindowPermission(MainActivity.this)) {
			  uri = Uri.parse(et_stream.getText().toString());
			  if (AppUtils.isRunService(MainActivity.this, getPackageName() + ".FloatingService")) {// 悬浮播放服务正在运行
				stopService(new Intent(MainActivity.this, FloatingService.class));// 停止悬浮播放服务
			  }
			  Intent playService=new Intent(MainActivity.this, FloatingService.class);
			  playService.putExtra("uri", uri.toString());
			  playService.putExtra("name", uri.getLastPathSegment());
			  startService(playService);// 开启悬浮播放服务
			  alertDialog.cancel();
			} else {
			  AppUtils.showWindowDialog(MainActivity.this);
			  alertDialog.cancel();
			}
		  } else {
			Toast.makeText(MainActivity.this, "不能为空", Toast.LENGTH_SHORT).show();
		  }
		}
	  });
  }

  // 设置/关于对话框
  private void showSettingDialog() {
	loop = mSp.getBoolean("loop", true);
	restart = mSp.getBoolean("restart", false);

	AlertDialog alertDialog = new AlertDialog.Builder(this)
	  .setTitle("设置/关于")
	  .setView(R.layout.dialog_setting)
	  .setNegativeButton("关闭", null)
	  .show();
	alertDialog.setCanceledOnTouchOutside(false);
	TextView title=alertDialog.getWindow().findViewById(R.id.alertTitle);
	title.setTextColor(getResources().getColor(R.color.theme));
	TextPaint tp_title = title.getPaint();
	tp_title.setFakeBoldText(true);

	Button positive=alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
	positive.setTextColor(getResources().getColor(R.color.theme));
	TextPaint tp_positive = positive.getPaint();
	tp_positive.setFakeBoldText(true);

	Button negative=alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
	negative.setTextColor(getResources().getColor(R.color.gray));
	TextPaint tp_negative = negative.getPaint();
	tp_negative.setFakeBoldText(true);

	LinearLayout ll_loop=alertDialog.findViewById(R.id.ll_loop);
	LinearLayout ll_restart=alertDialog.findViewById(R.id.ll_restart);
	LinearLayout ll_call=alertDialog.findViewById(R.id.ll_call);
	LinearLayout ll_email=alertDialog.findViewById(R.id.ll_email);
	final Switch s_loop=alertDialog.findViewById(R.id.s_loop);
	final Switch s_restart=alertDialog.findViewById(R.id.s_restart);

	ll_loop.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  if (s_loop.isChecked()) {
			s_loop.setChecked(false);
		  } else {
			s_loop.setChecked(true);
		  }
		}
	  });

	s_loop.getThumbDrawable().setColorFilter(getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);
	s_loop.getTrackDrawable().setColorFilter(getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);

	if (loop) {
	  s_loop.setChecked(true);
	} else {
	  s_loop.setChecked(false);
	}

	s_loop.setOnCheckedChangeListener(new OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
		  // TODO: Implement this method
		  if (p2) {
			mSpEd.putBoolean("loop", true);
			mSpEd.commit();
		  } else {
			mSpEd.putBoolean("loop", false);
			mSpEd.commit();
		  }
		}
	  });

	ll_restart.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  if (s_restart.isChecked()) {
			s_restart.setChecked(false);
		  } else {
			s_restart.setChecked(true);
		  }
		}
	  });

	s_restart.getThumbDrawable().setColorFilter(getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);
	s_restart.getTrackDrawable().setColorFilter(getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);

	if (restart) {
	  s_restart.setChecked(true);
	} else {
	  s_restart.setChecked(false);
	}

	s_restart.setOnCheckedChangeListener(new OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
		  // TODO: Implement this method
		  if (p2) {
			mSpEd.putBoolean("restart", true);
			mSpEd.commit();
		  } else {
			mSpEd.putBoolean("restart", false);
			mSpEd.commit();
		  }
		}
	  });

	ll_call.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  Intent call = new Intent();    
		  call.setAction(Intent.ACTION_VIEW);     
		  call.setData(Uri.parse("http://qm.qq.com/cgi-bin/qm/qr?k=Nx9tzX6g-LcC5PN4YyxhMfPUC4hvcoFp")); 
		  if (AppUtils.isAppInstalled(MainActivity.this, "com.tencent.mobileqq")) {
			call.setPackage("com.tencent.mobileqq");
		  } else if (AppUtils.isAppInstalled(MainActivity.this, "com.tencent.tim")) {
			call.setPackage("com.tencent.tim");
		  }
		  startActivity(call);
		}
	  });

	ll_email.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  Intent email = new Intent();    
		  email.setAction(Intent.ACTION_SENDTO);     
		  email.setData(Uri.parse("mailto:friendgxx@qq.com")); 
		  email.putExtra(Intent.EXTRA_SUBJECT, "[软件反馈] FW Player");
		  startActivity(email);
		}
	  });
  }

  // 常见问题对话框
  private void showHelpDialog() {
	StringBuffer sb = new StringBuffer();
	sb.append("1.悬浮窗的暂停与播放操作？")
	  .append("\n")
	  .append("点击暂停，再次点击播放。视频需在进度条显示时进行点击操作。")
	  .append("\n\n")
	  .append("2.如何调节视频悬浮窗大小？")
	  .append("\n")
	  .append("拖拽悬浮窗右下角，即可缩放视频悬浮窗的大小。")
	  .append("\n\n")
	  .append("3.视频悬浮窗无法调节大小？")
	  .append("\n")
	  .append("悬浮窗大小调节在部分机型上存在兼容问题，开发者正在努力适配中。")
	  .append("\n\n")
	  .append("4.悬浮窗无法弹出操作菜单？")
	  .append("\n")
	  .append("a.请检查您设备自带或第三方安全中心是否禁止了应用的“在后台弹出界面”权限。")
	  .append("\n")
	  .append("b.由于 Android 系统的限制，在您点击设备的 Home 键后，弹出菜单可能有 3-5 秒的延迟。")
	  .append("\n\n")
	  .append("5.播放网络媒体时出现卡顿？")
	  .append("\n")
	  .append("若您调整了媒体的播放速度，则有可能出现播放卡顿现象。")
	  .append("\n\n")
	  .append("6.设置项手动修改后未生效？")
	  .append("\n")
	  .append("设置项被修改后，在下次加载媒体时才会生效。");

	final AlertDialog alertDialog = new AlertDialog.Builder(this)
	  .setTitle("常见问题")
	  .setMessage(sb)
	  .setPositiveButton("关闭", null)
	  .show();
	alertDialog.setCanceledOnTouchOutside(false);
	TextView title=alertDialog.getWindow().findViewById(R.id.alertTitle);
	title.setTextColor(getResources().getColor(R.color.theme));
	TextPaint tp_title = title.getPaint();
	tp_title.setFakeBoldText(true);

	Button positive=alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
	positive.setTextColor(getResources().getColor(R.color.theme));
	TextPaint tp_positive = positive.getPaint();
	tp_positive.setFakeBoldText(true);
  }
}
