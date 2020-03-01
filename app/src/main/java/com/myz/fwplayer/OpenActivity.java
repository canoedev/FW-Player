package com.myz.fwplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class OpenActivity extends AppCompatActivity {
  private SharedPreferences mSp;
  private SharedPreferences.Editor mSpEd;

  private Uri uri;// 传进的 Uri

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
	  } else {
		startPlayService();
	  }
	} else {
	  startPlayService();
	}
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
	  if (checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
		requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 0);
	  } else {
		startPlayService();
	  }
	} else if (requestCode == 1) {
	  if (AppUtils.haveWindowPermission(this)) {// 获取到悬浮窗权限
		if (AppUtils.isRunService(this, getPackageName() + ".FloatingService")) {// 悬浮播放服务正在运行
		  stopService(new Intent(this, FloatingService.class));// 停止悬浮播放服务
		}
		Intent playService=new Intent(this, FloatingService.class);
		playService.putExtra("uri", AppUtils.getUri(uri, this).toString());
		playService.putExtra("name", AppUtils.getUri(uri, this).getLastPathSegment());
		startService(playService);// 开启开启悬浮播放服务
		finish();
	  } else {// 未获取到悬浮窗权限
		AppUtils.showWindowDialog(this);
	  }
	}
  }

  // 开启悬浮播放服务
  private void startPlayService() {
	uri = getIntent().getData();// 获取 Intent 中的 Uri 数据
	if (uri != null) {
	  if (AppUtils.haveWindowPermission(this)) {// 获取到悬浮窗权限
		if (AppUtils.isRunService(this, getPackageName() + ".FloatingService")) {// 悬浮播放服务正在运行
		  stopService(new Intent(this, FloatingService.class));// 停止悬浮播放服务
		}
		Intent playService=new Intent(this, FloatingService.class);
		playService.putExtra("uri", AppUtils.getUri(uri, this).toString());
		playService.putExtra("name", AppUtils.getUri(uri, this).getLastPathSegment());
		startService(playService);// 开启开启悬浮播放服务
		finish();
	  } else {// 未获取到悬浮窗权限
		AppUtils.showWindowDialog(this);
	  }
	} else {
	  Toast.makeText(this, "无法获取 Uri", Toast.LENGTH_SHORT).show();
	}
  }
}
