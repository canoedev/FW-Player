package com.myz.fwplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

  private MyReceiver myReceiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	getWindow().setStatusBarColor(getResources().getColor(R.color.translate));// 状态栏透明
	getWindow().setNavigationBarColor(getResources().getColor(R.color.translate));// 导航栏透明

	myReceiver = new MyReceiver();
	IntentFilter intentFilter = new IntentFilter();
	intentFilter.addAction("com.myz.fwplayer.CANCEL_ACTION");
	registerReceiver(myReceiver, intentFilter);

	MenuUtils mu=new MenuUtils();
	mu.showMenu(this, 0);// 0 表示悬浮窗菜单
  }

  @Override
  public void onPause() {
	Intent pause = new Intent("com.myz.fwplayer.PAUSED_ACTION");
	sendBroadcast(pause);
	finish();
	super.onPause();
  }

  public class MyReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
	  if (intent.getAction().equals("com.myz.fwplayer.CANCEL_ACTION")) {
		finish();
	  }
	}
  }
}
