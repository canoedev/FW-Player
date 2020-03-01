package com.myz.fwplayer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.text.ClipboardManager;
import android.text.TextPaint;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import androidx.appcompat.app.AlertDialog;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class MenuUtils {
  private SharedPreferences mSp;
  private SharedPreferences.Editor mSpEd;

  private String uri;
  private String name;
  private String s_uri;
  private boolean rotaty;
  private float leftVolume=1.0f;
  private float rightVolume=1.0f;
  private float speed=1.0f;
  private float pitch=1.0f;

  public void showMenu(final Activity act, final int i) {
	final AlertDialog alertDialog = new AlertDialog.Builder(act)
	  .setTitle("选择操作")
	  .setView(R.layout.dialog_more)
	  .setNegativeButton("返回", new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface p1, int p2) {
		  Intent cancel = new Intent("com.myz.fwplayer.CANCEL_ACTION");
		  act.sendBroadcast(cancel);
		}
	  })
	  .setOnCancelListener(new DialogInterface.OnCancelListener(){

		@Override
		public void onCancel(DialogInterface p1) {
		  // TODO: Implement this method
		  Intent cancel = new Intent("com.myz.fwplayer.CANCEL_ACTION");
		  act.sendBroadcast(cancel);
		}
	  })
	  .show();
	alertDialog.setCanceledOnTouchOutside(false);
	TextView title=alertDialog.getWindow().findViewById(R.id.alertTitle);
	title.setTextColor(act.getResources().getColor(R.color.theme));
	TextPaint tp_title = title.getPaint();
	tp_title.setFakeBoldText(true);

	Button positive=alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
	positive.setTextColor(act.getResources().getColor(R.color.theme));
	TextPaint tp_positive = positive.getPaint();
	tp_positive.setFakeBoldText(true);

	Button negative=alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
	negative.setTextColor(act.getResources().getColor(R.color.gray));
	TextPaint tp_negative = negative.getPaint();
	tp_negative.setFakeBoldText(true);

	LinearLayout ll_uri=alertDialog.findViewById(R.id.ll_uri);
	LinearLayout ll_adjust=alertDialog.findViewById(R.id.ll_adjust);
	LinearLayout ll_change=alertDialog.findViewById(R.id.ll_change);
	LinearLayout ll_mode=alertDialog.findViewById(R.id.ll_mode);

	TextView tv_mode_title=alertDialog.findViewById(R.id.tv_mode_title);
	TextView tv_mode_summary=alertDialog.findViewById(R.id.tv_mode_summary);

	if (i == 0) {
	  tv_mode_title.setText("全屏播放");
	  tv_mode_summary.setText("切换到全屏播放当前的媒体");
	} else if (i == 1) {
	  tv_mode_title.setText("显示比例");
	  tv_mode_summary.setText("调整所播放视频的显示比例");
	}

	ll_uri.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  showUriDialog(act);
		}
	  });

	ll_adjust.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  showAdjustDialog(act);
		}
	  });

	ll_change.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  if (Build.VERSION.SDK_INT >= 23) {
			showChangeDialog(act);
		  } else {
			showChangeErrorDialog(act);
		  }
		}
	  });

	ll_mode.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  if (i == 0) {
			uri = act.getIntent().getStringExtra("uri");
			name = Uri.parse(uri).getLastPathSegment();
			Intent mode=new Intent(act, PlayActivity.class);
			mode.putExtra("uri", uri);
			mode.putExtra("name", name);
			act.startActivity(mode);
			Intent go = new Intent("com.myz.fwplayer.GOTO_ACTION");
			act.sendBroadcast(go);
			act.finish();
		  } else if (i == 1) {
			showTipDialog(act);
		  }
		}
	  });
  }

  private void initData(Activity act) {
	mSp = act.getSharedPreferences("data", Context.MODE_PRIVATE);
	mSpEd = mSp.edit();
  }

  // 查看链接对话框
  private void showUriDialog(final Activity act) {
	try {
	  s_uri = URLDecoder.decode(act.getIntent().getStringExtra("uri"), "utf-8");
	} catch (UnsupportedEncodingException e) {}

	final AlertDialog alertDialog = new AlertDialog.Builder(act)
	  .setTitle("查看链接")
	  .setMessage(s_uri)
	  .setPositiveButton("关闭", null)
	  .setNegativeButton("复制", new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface p1, int p2) {
		  ClipboardManager cm = (ClipboardManager) act.getSystemService(Context.CLIPBOARD_SERVICE);
		  cm.setText(s_uri);
		  Toast.makeText(act, "已复制内容", Toast.LENGTH_SHORT).show();
		}
	  })
	  .show();
	alertDialog.setCanceledOnTouchOutside(false);
	TextView title=alertDialog.getWindow().findViewById(R.id.alertTitle);
	title.setTextColor(act.getResources().getColor(R.color.theme));
	TextPaint tp_title = title.getPaint();
	tp_title.setFakeBoldText(true);

	Button positive=alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
	positive.setTextColor(act.getResources().getColor(R.color.gray));
	TextPaint tp_positive = positive.getPaint();
	tp_positive.setFakeBoldText(true);

	Button negative=alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
	negative.setTextColor(act.getResources().getColor(R.color.theme));
	TextPaint tp_negative = negative.getPaint();
	tp_negative.setFakeBoldText(true);
  }

  // 左/右声道对话框
  private void showAdjustDialog(final Activity act) {
	initData(act);

	rotaty = mSp.getBoolean("rotaty", false);
	leftVolume = mSp.getFloat("left", 1.0f);
	rightVolume = mSp.getFloat("right", 1.0f);
	AlertDialog alertDialog = new AlertDialog.Builder(act)
	  .setTitle("左/右声道")
	  .setView(R.layout.dialog_adjust)
	  .setPositiveButton("应用", new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface p1, int p2) {
		  mSpEd.putBoolean("rotaty", rotaty);
		  if (!rotaty) {
			mSpEd.putFloat("left", leftVolume);
			mSpEd.putFloat("right", rightVolume);
		  }
		  mSpEd.commit();
		  Intent adjust = new Intent("com.myz.fwplayer.ADJUST_ACTION");
		  act.sendBroadcast(adjust);
		}
	  })
	  .setNegativeButton("取消", null)
	  .show();
	alertDialog.setCanceledOnTouchOutside(false);

	TextView title=alertDialog.getWindow().findViewById(R.id.alertTitle);
	title.setTextColor(act.getResources().getColor(R.color.theme));
	TextPaint tp_title = title.getPaint();
	tp_title.setFakeBoldText(true);

	Button positive=alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
	positive.setTextColor(act.getResources().getColor(R.color.theme));
	TextPaint tp_positive = positive.getPaint();
	tp_positive.setFakeBoldText(true);

	Button negative=alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
	negative.setTextColor(act.getResources().getColor(R.color.gray));
	TextPaint tp_negative = negative.getPaint();
	tp_negative.setFakeBoldText(true);

	LinearLayout ll_rotaty=alertDialog.findViewById(R.id.ll_rotary);
	final Switch s_rotary=alertDialog.findViewById(R.id.s_rotary);
	final TextView tv_left=alertDialog.findViewById(R.id.tv_left);
	final TextView tv_right=alertDialog.findViewById(R.id.tv_right);
	final SeekBar sb_left=alertDialog.findViewById(R.id.sb_left);
	final SeekBar sb_right=alertDialog.findViewById(R.id.sb_right);
	final ToggleButton tb_left_20p=alertDialog.findViewById(R.id.tb_left_20p);
	final ToggleButton tb_left_40p=alertDialog.findViewById(R.id.tb_left_40p);
	final ToggleButton tb_left_60p=alertDialog.findViewById(R.id.tb_left_60p);
	final ToggleButton tb_left_80p=alertDialog.findViewById(R.id.tb_left_80p);
	final ToggleButton tb_left_100p=alertDialog.findViewById(R.id.tb_left_100p);
	final ToggleButton tb_right_20p=alertDialog.findViewById(R.id.tb_right_20p);
	final ToggleButton tb_right_40p=alertDialog.findViewById(R.id.tb_right_40p);
	final ToggleButton tb_right_60p=alertDialog.findViewById(R.id.tb_right_60p);
	final ToggleButton tb_right_80p=alertDialog.findViewById(R.id.tb_right_80p);
	final ToggleButton tb_right_100p=alertDialog.findViewById(R.id.tb_right_100p);

	ll_rotaty.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  if (!s_rotary.isChecked()) {
			s_rotary.setChecked(true);
		  } else {
			s_rotary.setChecked(false);
		  }
		}
	  });

	s_rotary.getThumbDrawable().setColorFilter(act.getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);
	s_rotary.getTrackDrawable().setColorFilter(act.getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);

	if (rotaty) {
	  s_rotary.setChecked(true);
	  sb_left.setEnabled(false);
	  tb_left_20p.setEnabled(false);
	  tb_left_40p.setEnabled(false);
	  tb_left_60p.setEnabled(false);
	  tb_left_80p.setEnabled(false);
	  tb_left_100p.setEnabled(false);
	  sb_right.setEnabled(false);
	  tb_right_20p.setEnabled(false);
	  tb_right_40p.setEnabled(false);
	  tb_right_60p.setEnabled(false);
	  tb_right_80p.setEnabled(false);
	  tb_right_100p.setEnabled(false);
	} else {
	  s_rotary.setChecked(false);
	  sb_left.setEnabled(true);
	  tb_left_20p.setEnabled(true);
	  tb_left_40p.setEnabled(true);
	  tb_left_60p.setEnabled(true);
	  tb_left_80p.setEnabled(true);
	  tb_left_100p.setEnabled(true);
	  sb_right.setEnabled(true);
	  tb_right_20p.setEnabled(true);
	  tb_right_40p.setEnabled(true);
	  tb_right_60p.setEnabled(true);
	  tb_right_80p.setEnabled(true);
	  tb_right_100p.setEnabled(true);
	}

	s_rotary.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
		  // TODO: Implement this method
		  if (p2) {
			rotaty = true;
			sb_left.setEnabled(false);
			tb_left_20p.setEnabled(false);
			tb_left_40p.setEnabled(false);
			tb_left_60p.setEnabled(false);
			tb_left_80p.setEnabled(false);
			tb_left_100p.setEnabled(false);
			sb_right.setEnabled(false);
			tb_right_20p.setEnabled(false);
			tb_right_40p.setEnabled(false);
			tb_right_60p.setEnabled(false);
			tb_right_80p.setEnabled(false);
			tb_right_100p.setEnabled(false);
		  } else {
			rotaty = false;
			sb_left.setEnabled(true);
			tb_left_20p.setEnabled(true);
			tb_left_40p.setEnabled(true);
			tb_left_60p.setEnabled(true);
			tb_left_80p.setEnabled(true);
			tb_left_100p.setEnabled(true);
			sb_right.setEnabled(true);
			tb_right_20p.setEnabled(true);
			tb_right_40p.setEnabled(true);
			tb_right_60p.setEnabled(true);
			tb_right_80p.setEnabled(true);
			tb_right_100p.setEnabled(true);
		  }
		}
	  });

	sb_left.getThumb().setColorFilter(act.getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);
	sb_left.getProgressDrawable().setColorFilter(act.getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);
	sb_right.getThumb().setColorFilter(act.getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);
	sb_right.getProgressDrawable().setColorFilter(act.getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);
	tb_left_20p.getBackground().setColorFilter(act.getResources().getColor(R.color.background), PorterDuff.Mode.SRC_ATOP);
	tb_left_40p.getBackground().setColorFilter(act.getResources().getColor(R.color.background), PorterDuff.Mode.SRC_ATOP);
	tb_left_60p.getBackground().setColorFilter(act.getResources().getColor(R.color.background), PorterDuff.Mode.SRC_ATOP);
	tb_left_80p.getBackground().setColorFilter(act.getResources().getColor(R.color.background), PorterDuff.Mode.SRC_ATOP);
	tb_left_100p.getBackground().setColorFilter(act.getResources().getColor(R.color.background), PorterDuff.Mode.SRC_ATOP);
	tb_right_20p.getBackground().setColorFilter(act.getResources().getColor(R.color.background), PorterDuff.Mode.SRC_ATOP);
	tb_right_40p.getBackground().setColorFilter(act.getResources().getColor(R.color.background), PorterDuff.Mode.SRC_ATOP);
	tb_right_60p.getBackground().setColorFilter(act.getResources().getColor(R.color.background), PorterDuff.Mode.SRC_ATOP);
	tb_right_80p.getBackground().setColorFilter(act.getResources().getColor(R.color.background), PorterDuff.Mode.SRC_ATOP);
	tb_right_100p.getBackground().setColorFilter(act.getResources().getColor(R.color.background), PorterDuff.Mode.SRC_ATOP);

	sb_left.setMax(100);
	sb_right.setMax(100);
	sb_left.setProgress((int)(leftVolume * 100));
	sb_right.setProgress((int)(rightVolume * 100));
	tv_left.setText("左声道 " + (int)(leftVolume * 100) + "%");
	tv_right.setText("右声道 " + (int)(rightVolume * 100) + "%");

	sb_left.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar p1, int p2, boolean p3) {
		  // TODO: Implement this method
		  leftVolume = p2 / 100.0f;
		  tv_left.setText("左声道 " + p2 + "%");
		  if (p2 == 20) {
			tb_left_20p.setChecked(true);
		  } else {
			tb_left_20p.setChecked(false);
		  }
		  if (p2 == 40) {
			tb_left_40p.setChecked(true);
		  } else {
			tb_left_40p.setChecked(false);
		  }
		  if (p2 == 60) {
			tb_left_60p.setChecked(true);
		  } else {
			tb_left_60p.setChecked(false);
		  }
		  if (p2 == 80) {
			tb_left_80p.setChecked(true);
		  } else {
			tb_left_80p.setChecked(false);
		  }
		  if (p2 == 100) {
			tb_left_100p.setChecked(true);
		  } else {
			tb_left_100p.setChecked(false);
		  }
		}

		@Override
		public void onStartTrackingTouch(SeekBar p1) {
		  // TODO: Implement this method
		}

		@Override
		public void onStopTrackingTouch(SeekBar p1) {
		  // TODO: Implement this method
		}
	  });

	sb_right.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar p1, int p2, boolean p3) {
		  // TODO: Implement this method
		  rightVolume = p2 / 100.0f;
		  tv_right.setText("右声道 " + p2 + "%");
		  if (p2 == 20) {
			tb_right_20p.setChecked(true);
		  } else {
			tb_right_20p.setChecked(false);
		  }
		  if (p2 == 40) {
			tb_right_40p.setChecked(true);
		  } else {
			tb_right_40p.setChecked(false);
		  }
		  if (p2 == 60) {
			tb_right_60p.setChecked(true);
		  } else {
			tb_right_60p.setChecked(false);
		  }
		  if (p2 == 80) {
			tb_right_80p.setChecked(true);
		  } else {
			tb_right_80p.setChecked(false);
		  }
		  if (p2 == 100) {
			tb_right_100p.setChecked(true);
		  } else {
			tb_right_100p.setChecked(false);
		  }
		}

		@Override
		public void onStartTrackingTouch(SeekBar p1) {
		  // TODO: Implement this method
		}

		@Override
		public void onStopTrackingTouch(SeekBar p1) {
		  // TODO: Implement this method
		}
	  });

	tb_left_20p.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  tb_left_20p.setChecked(true);
		}
	  });
	tb_left_40p.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  tb_left_40p.setChecked(true);
		}
	  });
	tb_left_60p.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  tb_left_60p.setChecked(true);
		}
	  });
	tb_left_80p.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  tb_left_80p.setChecked(true);
		}
	  });
	tb_left_100p.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  tb_left_100p.setChecked(true);
		}
	  });

	tb_left_20p.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
		  // TODO: Implement this method
		  if (p2) {
			tb_left_20p.setTextColor(act.getResources().getColor(R.color.theme));
			tb_left_40p.setChecked(false);
			tb_left_60p.setChecked(false);
			tb_left_80p.setChecked(false);
			tb_left_100p.setChecked(false);
			sb_left.setProgress(20);
		  } else if (!p2) {
			tb_left_20p.setTextColor(act.getResources().getColor(R.color.gray));
		  }
		}
	  });
	tb_left_40p.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
		  // TODO: Implement this method
		  if (p2) {
			tb_left_20p.setChecked(false);
			tb_left_40p.setTextColor(act.getResources().getColor(R.color.theme));
			tb_left_60p.setChecked(false);
			tb_left_80p.setChecked(false);
			tb_left_100p.setChecked(false);
			sb_left.setProgress(40);
		  } else if (!p2) {
			tb_left_40p.setTextColor(act.getResources().getColor(R.color.gray));
		  }
		}
	  });
	tb_left_60p.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
		  // TODO: Implement this method
		  if (p2) {
			tb_left_20p.setChecked(false);
			tb_left_40p.setChecked(false);
			tb_left_60p.setTextColor(act.getResources().getColor(R.color.theme));
			tb_left_80p.setChecked(false);
			tb_left_100p.setChecked(false);
			sb_left.setProgress(60);
		  } else if (!p2) {
			tb_left_60p.setTextColor(act.getResources().getColor(R.color.gray));
		  }
		}
	  });
	tb_left_80p.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
		  // TODO: Implement this method
		  if (p2) {
			tb_left_20p.setChecked(false);
			tb_left_40p.setChecked(false);
			tb_left_60p.setChecked(false);
			tb_left_80p.setTextColor(act.getResources().getColor(R.color.theme));
			tb_left_100p.setChecked(false);
			sb_left.setProgress(80);
		  } else if (!p2) {
			tb_left_80p.setTextColor(act.getResources().getColor(R.color.gray));
		  }
		}
	  });
	tb_left_100p.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
		  // TODO: Implement this method
		  if (p2) {
			tb_left_20p.setChecked(false);
			tb_left_40p.setChecked(false);
			tb_left_60p.setChecked(false);
			tb_left_80p.setChecked(false);
			tb_left_100p.setTextColor(act.getResources().getColor(R.color.theme));
			sb_left.setProgress(100);
		  } else if (!p2) {
			tb_left_100p.setTextColor(act.getResources().getColor(R.color.gray));
		  }
		}
	  });

	if (leftVolume == 0.2f) {
	  tb_left_20p.setChecked(true);
	} else if (leftVolume == 0.4f) {
	  tb_left_40p.setChecked(true);
	} else if (leftVolume == 0.6f) {
	  tb_left_60p.setChecked(true);
	} else if (leftVolume == 0.8f) {
	  tb_left_80p.setChecked(true);
	} else if (leftVolume == 1.0f) {
	  tb_left_100p.setChecked(true);
	}

	tb_right_20p.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  tb_right_20p.setChecked(true);
		}
	  });
	tb_right_40p.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  tb_right_40p.setChecked(true);
		}
	  });
	tb_right_60p.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  tb_right_60p.setChecked(true);
		}
	  });
	tb_right_80p.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  tb_right_80p.setChecked(true);
		}
	  });
	tb_right_100p.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  tb_right_100p.setChecked(true);
		}
	  });

	tb_right_20p.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
		  // TODO: Implement this method
		  if (p2) {
			tb_right_20p.setTextColor(act.getResources().getColor(R.color.theme));
			tb_right_40p.setChecked(false);
			tb_right_60p.setChecked(false);
			tb_right_80p.setChecked(false);
			tb_right_100p.setChecked(false);
			sb_right.setProgress(20);
		  } else if (!p2) {
			tb_right_20p.setTextColor(act.getResources().getColor(R.color.gray));
		  }
		}
	  });
	tb_right_40p.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
		  // TODO: Implement this method
		  if (p2) {
			tb_right_20p.setChecked(false);
			tb_right_40p.setTextColor(act.getResources().getColor(R.color.theme));
			tb_right_60p.setChecked(false);
			tb_right_80p.setChecked(false);
			tb_right_100p.setChecked(false);
			sb_right.setProgress(40);
		  } else if (!p2) {
			tb_right_40p.setTextColor(act.getResources().getColor(R.color.gray));
		  }
		}
	  });
	tb_right_60p.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
		  // TODO: Implement this method
		  if (p2) {
			tb_right_20p.setChecked(false);
			tb_right_40p.setChecked(false);
			tb_right_60p.setTextColor(act.getResources().getColor(R.color.theme));
			tb_right_80p.setChecked(false);
			tb_right_100p.setChecked(false);
			sb_right.setProgress(60);
		  } else if (!p2) {
			tb_right_60p.setTextColor(act.getResources().getColor(R.color.gray));
		  }
		}
	  });
	tb_right_80p.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
		  // TODO: Implement this method
		  if (p2) {
			tb_right_20p.setChecked(false);
			tb_right_40p.setChecked(false);
			tb_right_60p.setChecked(false);
			tb_right_80p.setTextColor(act.getResources().getColor(R.color.theme));
			tb_right_100p.setChecked(false);
			sb_right.setProgress(80);
		  } else if (!p2) {
			tb_right_80p.setTextColor(act.getResources().getColor(R.color.gray));
		  }
		}
	  });
	tb_right_100p.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
		  // TODO: Implement this method
		  if (p2) {
			tb_right_20p.setChecked(false);
			tb_right_40p.setChecked(false);
			tb_right_60p.setChecked(false);
			tb_right_80p.setChecked(false);
			tb_right_100p.setTextColor(act.getResources().getColor(R.color.theme));
			sb_right.setProgress(100);
		  } else if (!p2) {
			tb_right_100p.setTextColor(act.getResources().getColor(R.color.gray));
		  }
		}
	  });

	if (rightVolume == 0.2f) {
	  tb_right_20p.setChecked(true);
	} else if (rightVolume == 0.4f) {
	  tb_right_40p.setChecked(true);
	} else if (rightVolume == 0.6f) {
	  tb_right_60p.setChecked(true);
	} else if (rightVolume == 0.8f) {
	  tb_right_80p.setChecked(true);
	} else if (rightVolume == 1.0f) {
	  tb_right_100p.setChecked(true);
	}
  }

  // 速度/音调对话框
  private void showChangeDialog(final Activity act) {
	initData(act);

	speed = mSp.getFloat("speed", 1.0f);
	pitch = mSp.getFloat("pitch", 1.0f);
	AlertDialog alertDialog = new AlertDialog.Builder(act)
	  .setTitle("速度/音调")
	  .setView(R.layout.dialog_change)
	  .setPositiveButton("应用", new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface p1, int p2) {
		  mSpEd.putFloat("speed", speed);
		  mSpEd.putFloat("pitch", pitch);
		  mSpEd.commit();
		  Intent change = new Intent("com.myz.fwplayer.CHANGE_ACTION");
		  act.sendBroadcast(change);
		}
	  })
	  .setNegativeButton("取消", null)
	  .show();
	alertDialog.setCanceledOnTouchOutside(false);
	alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){

		@Override
		public void onDismiss(DialogInterface p1) {
		  // TODO: Implement this method
		}
	  });

	TextView title=alertDialog.getWindow().findViewById(R.id.alertTitle);
	title.setTextColor(act.getResources().getColor(R.color.theme));
	TextPaint tp_title = title.getPaint();
	tp_title.setFakeBoldText(true);

	Button positive=alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
	positive.setTextColor(act.getResources().getColor(R.color.theme));
	TextPaint tp_positive = positive.getPaint();
	tp_positive.setFakeBoldText(true);

	Button negative=alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
	negative.setTextColor(act.getResources().getColor(R.color.gray));
	TextPaint tp_negative = negative.getPaint();
	tp_negative.setFakeBoldText(true);

	final TextView tv_speed=alertDialog.findViewById(R.id.tv_speed);
	final TextView tv_pitch=alertDialog.findViewById(R.id.tv_pitch);
	final SeekBar sb_speed=alertDialog.findViewById(R.id.sb_speed);
	final SeekBar sb_pitch=alertDialog.findViewById(R.id.sb_pitch);
	final ToggleButton tb_speed_0_5x=alertDialog.findViewById(R.id.tb_speed_0_5x);
	final ToggleButton tb_speed_0_75x=alertDialog.findViewById(R.id.tb_speed_0_75x);
	final ToggleButton tb_speed_1_0x=alertDialog.findViewById(R.id.tb_speed_1_0x);
	final ToggleButton tb_speed_1_25x=alertDialog.findViewById(R.id.tb_speed_1_25x);
	final ToggleButton tb_speed_1_5x=alertDialog.findViewById(R.id.tb_speed_1_5x);
	final ToggleButton tb_speed_2_0x=alertDialog.findViewById(R.id.tb_speed_2_0x);
	final ToggleButton tb_pitch_0_5x=alertDialog.findViewById(R.id.tb_pitch_0_5x);
	final ToggleButton tb_pitch_0_75x=alertDialog.findViewById(R.id.tb_pitch_0_75x);
	final ToggleButton tb_pitch_1_0x=alertDialog.findViewById(R.id.tb_pitch_1_0x);
	final ToggleButton tb_pitch_1_25x=alertDialog.findViewById(R.id.tb_pitch_1_25x);
	final ToggleButton tb_pitch_1_5x=alertDialog.findViewById(R.id.tb_pitch_1_5x);
	final ToggleButton tb_pitch_2_0x=alertDialog.findViewById(R.id.tb_pitch_2_0x);

	sb_speed.getThumb().setColorFilter(act.getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);
	sb_speed.getProgressDrawable().setColorFilter(act.getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);
	sb_pitch.getThumb().setColorFilter(act.getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);
	sb_pitch.getProgressDrawable().setColorFilter(act.getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);
	tb_speed_0_5x.getBackground().setColorFilter(act.getResources().getColor(R.color.background), PorterDuff.Mode.SRC_ATOP);
	tb_speed_0_75x.getBackground().setColorFilter(act.getResources().getColor(R.color.background), PorterDuff.Mode.SRC_ATOP);
	tb_speed_1_0x.getBackground().setColorFilter(act.getResources().getColor(R.color.background), PorterDuff.Mode.SRC_ATOP);
	tb_speed_1_25x.getBackground().setColorFilter(act.getResources().getColor(R.color.background), PorterDuff.Mode.SRC_ATOP);
	tb_speed_1_5x.getBackground().setColorFilter(act.getResources().getColor(R.color.background), PorterDuff.Mode.SRC_ATOP);
	tb_speed_2_0x.getBackground().setColorFilter(act.getResources().getColor(R.color.background), PorterDuff.Mode.SRC_ATOP);
	tb_pitch_0_5x.getBackground().setColorFilter(act.getResources().getColor(R.color.background), PorterDuff.Mode.SRC_ATOP);
	tb_pitch_0_75x.getBackground().setColorFilter(act.getResources().getColor(R.color.background), PorterDuff.Mode.SRC_ATOP);
	tb_pitch_1_0x.getBackground().setColorFilter(act.getResources().getColor(R.color.background), PorterDuff.Mode.SRC_ATOP);
	tb_pitch_1_25x.getBackground().setColorFilter(act.getResources().getColor(R.color.background), PorterDuff.Mode.SRC_ATOP);
	tb_pitch_1_5x.getBackground().setColorFilter(act.getResources().getColor(R.color.background), PorterDuff.Mode.SRC_ATOP);
	tb_pitch_2_0x.getBackground().setColorFilter(act.getResources().getColor(R.color.background), PorterDuff.Mode.SRC_ATOP);

	sb_speed.setMax(990);
	sb_pitch.setMax(990);
	sb_speed.setProgress((int)(speed * 100));
	sb_pitch.setProgress((int)(pitch * 100));
	tv_speed.setText("速度 " + speed + "X");
	tv_pitch.setText("音调 " + pitch + "X");

	sb_speed.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar p1, int p2, boolean p3) {
		  // TODO: Implement this method
		  speed = (p2 + 10) / 100.0f;
		  tv_speed.setText("速度 " + (p2 + 10) / 100.0 + "X");
		  if (p2 == 40) {
			tb_speed_0_5x.setChecked(true);
		  } else {
			tb_speed_0_5x.setChecked(false);
		  }
		  if (p2 == 65) {
			tb_speed_0_75x.setChecked(true);
		  } else {
			tb_speed_0_75x.setChecked(false);
		  }
		  if (p2 == 90) {
			tb_speed_1_0x.setChecked(true);
		  } else {
			tb_speed_1_0x.setChecked(false);
		  }
		  if (p2 == 115) {
			tb_speed_1_25x.setChecked(true);
		  } else {
			tb_speed_1_25x.setChecked(false);
		  }
		  if (p2 == 140) {
			tb_speed_1_5x.setChecked(true);
		  } else {
			tb_speed_1_5x.setChecked(false);
		  }
		  if (p2 == 190) {
			tb_speed_2_0x.setChecked(true);
		  } else {
			tb_speed_2_0x.setChecked(false);
		  }
		}

		@Override
		public void onStartTrackingTouch(SeekBar p1) {
		  // TODO: Implement this method
		}

		@Override
		public void onStopTrackingTouch(SeekBar p1) {
		  // TODO: Implement this method
		}
	  });

	sb_pitch.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar p1, int p2, boolean p3) {
		  // TODO: Implement this method
		  pitch = (p2 + 10) / 100.0f;
		  tv_pitch.setText("音调 " + (p2 + 10) / 100.0 + "X");
		  if (p2 == 40) {
			tb_pitch_0_5x.setChecked(true);
		  } else {
			tb_pitch_0_5x.setChecked(false);
		  }
		  if (p2 == 65) {
			tb_pitch_0_75x.setChecked(true);
		  } else {
			tb_pitch_0_75x.setChecked(false);
		  }
		  if (p2 == 90) {
			tb_pitch_1_0x.setChecked(true);
		  } else {
			tb_pitch_1_0x.setChecked(false);
		  }
		  if (p2 == 115) {
			tb_pitch_1_25x.setChecked(true);
		  } else {
			tb_pitch_1_25x.setChecked(false);
		  }
		  if (p2 == 140) {
			tb_pitch_1_5x.setChecked(true);
		  } else {
			tb_pitch_1_5x.setChecked(false);
		  }
		  if (p2 == 190) {
			tb_pitch_2_0x.setChecked(true);
		  } else {
			tb_pitch_2_0x.setChecked(false);
		  }
		}

		@Override
		public void onStartTrackingTouch(SeekBar p1) {
		  // TODO: Implement this method
		}

		@Override
		public void onStopTrackingTouch(SeekBar p1) {
		  // TODO: Implement this method
		}
	  });

	tb_speed_0_5x.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  tb_speed_0_5x.setChecked(true);
		}
	  });
	tb_speed_0_75x.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  tb_speed_0_75x.setChecked(true);
		}
	  });
	tb_speed_1_0x.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  tb_speed_1_0x.setChecked(true);
		}
	  });
	tb_speed_1_25x.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  tb_speed_1_25x.setChecked(true);
		}
	  });
	tb_speed_1_5x.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  tb_speed_1_5x.setChecked(true);
		}
	  });
	tb_speed_2_0x.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  tb_speed_2_0x.setChecked(true);
		}
	  });
	tb_speed_0_5x.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
		  // TODO: Implement this method
		  if (p2) {
			tb_speed_0_5x.setTextColor(act.getResources().getColor(R.color.theme));
			tb_speed_0_75x.setChecked(false);
			tb_speed_1_0x.setChecked(false);
			tb_speed_1_25x.setChecked(false);
			tb_speed_1_5x.setChecked(false);
			tb_speed_2_0x.setChecked(false);
			sb_speed.setProgress(40);
		  } else if (!p2) {
			tb_speed_0_5x.setTextColor(act.getResources().getColor(R.color.gray));
		  }
		}
	  });
	tb_speed_0_75x.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
		  // TODO: Implement this method
		  if (p2) {
			tb_speed_0_5x.setChecked(false);
			tb_speed_0_75x.setTextColor(act.getResources().getColor(R.color.theme));
			tb_speed_1_0x.setChecked(false);
			tb_speed_1_25x.setChecked(false);
			tb_speed_1_5x.setChecked(false);
			tb_speed_2_0x.setChecked(false);
			sb_speed.setProgress(65);
		  } else if (!p2) {
			tb_speed_0_75x.setTextColor(act.getResources().getColor(R.color.gray));
		  }
		}
	  });
	tb_speed_1_0x.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
		  // TODO: Implement this method
		  if (p2) {
			tb_speed_0_5x.setChecked(false);
			tb_speed_0_75x.setChecked(false);
			tb_speed_1_0x.setTextColor(act.getResources().getColor(R.color.theme));
			tb_speed_1_25x.setChecked(false);
			tb_speed_1_5x.setChecked(false);
			tb_speed_2_0x.setChecked(false);
			sb_speed.setProgress(90);
		  } else if (!p2) {
			tb_speed_1_0x.setTextColor(act.getResources().getColor(R.color.gray));
		  }
		}
	  });
	tb_speed_1_25x.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
		  // TODO: Implement this method
		  if (p2) {
			tb_speed_0_5x.setChecked(false);
			tb_speed_0_75x.setChecked(false);
			tb_speed_1_0x.setChecked(false);
			tb_speed_1_25x.setTextColor(act.getResources().getColor(R.color.theme));
			tb_speed_1_5x.setChecked(false);
			tb_speed_2_0x.setChecked(false);
			sb_speed.setProgress(115);
		  } else if (!p2) {
			tb_speed_1_25x.setTextColor(act.getResources().getColor(R.color.gray));
		  }
		}
	  });
	tb_speed_1_5x.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
		  // TODO: Implement this method
		  if (p2) {
			tb_speed_0_5x.setChecked(false);
			tb_speed_0_75x.setChecked(false);
			tb_speed_1_0x.setChecked(false);
			tb_speed_1_25x.setChecked(false);
			tb_speed_1_5x.setTextColor(act.getResources().getColor(R.color.theme));
			tb_speed_2_0x.setChecked(false);
			sb_speed.setProgress(140);
		  } else if (!p2) {
			tb_speed_1_5x.setTextColor(act.getResources().getColor(R.color.gray));
		  }
		}
	  });
	tb_speed_2_0x.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
		  // TODO: Implement this method
		  if (p2) {
			tb_speed_0_5x.setChecked(false);
			tb_speed_0_75x.setChecked(false);
			tb_speed_1_0x.setChecked(false);
			tb_speed_1_25x.setChecked(false);
			tb_speed_1_5x.setChecked(false);
			tb_speed_2_0x.setTextColor(act.getResources().getColor(R.color.theme));
			sb_speed.setProgress(190);
		  } else if (!p2) {
			tb_speed_2_0x.setTextColor(act.getResources().getColor(R.color.gray));
		  }
		}
	  });

	if (speed == 0.5f) {
	  tb_speed_0_5x.setChecked(true);
	} else if (speed == 0.75f) {
	  tb_speed_0_75x.setChecked(true);
	} else if (speed == 1.0f) {
	  tb_speed_1_0x.setChecked(true);
	} else if (speed == 1.25f) {
	  tb_speed_1_25x.setChecked(true);
	} else if (speed == 1.5f) {
	  tb_speed_1_5x.setChecked(true);
	} else if (speed == 2.0f) {
	  tb_speed_2_0x.setChecked(true);
	}

	tb_pitch_0_5x.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  tb_pitch_0_5x.setChecked(true);
		}
	  });
	tb_pitch_0_75x.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  tb_pitch_0_75x.setChecked(true);
		}
	  });
	tb_pitch_1_0x.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  tb_pitch_1_0x.setChecked(true);
		}
	  });
	tb_pitch_1_25x.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  tb_pitch_1_25x.setChecked(true);
		}
	  });
	tb_pitch_1_5x.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  tb_pitch_1_5x.setChecked(true);
		}
	  });
	tb_pitch_2_0x.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View p1) {
		  // TODO: Implement this method
		  tb_pitch_2_0x.setChecked(true);
		}
	  });
	tb_pitch_0_5x.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
		  // TODO: Implement this method
		  if (p2) {
			tb_pitch_0_5x.setTextColor(act.getResources().getColor(R.color.theme));
			tb_pitch_0_75x.setChecked(false);
			tb_pitch_1_0x.setChecked(false);
			tb_pitch_1_25x.setChecked(false);
			tb_pitch_1_5x.setChecked(false);
			tb_pitch_2_0x.setChecked(false);
			sb_pitch.setProgress(40);
		  } else if (!p2) {
			tb_pitch_0_5x.setTextColor(act.getResources().getColor(R.color.gray));
		  }
		}
	  });
	tb_pitch_0_75x.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
		  // TODO: Implement this method
		  if (p2) {
			tb_pitch_0_5x.setChecked(false);
			tb_pitch_0_75x.setTextColor(act.getResources().getColor(R.color.theme));
			tb_pitch_1_0x.setChecked(false);
			tb_pitch_1_25x.setChecked(false);
			tb_pitch_1_5x.setChecked(false);
			tb_pitch_2_0x.setChecked(false);
			sb_pitch.setProgress(65);
		  } else if (!p2) {
			tb_pitch_0_75x.setTextColor(act.getResources().getColor(R.color.gray));
		  }
		}
	  });
	tb_pitch_1_0x.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
		  // TODO: Implement this method
		  if (p2) {
			tb_pitch_0_5x.setChecked(false);
			tb_pitch_0_75x.setChecked(false);
			tb_pitch_1_0x.setTextColor(act.getResources().getColor(R.color.theme));
			tb_pitch_1_25x.setChecked(false);
			tb_pitch_1_5x.setChecked(false);
			tb_pitch_2_0x.setChecked(false);
			sb_pitch.setProgress(90);
		  } else if (!p2) {
			tb_pitch_1_0x.setTextColor(act.getResources().getColor(R.color.gray));
		  }
		}
	  });
	tb_pitch_1_25x.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
		  // TODO: Implement this method
		  if (p2) {
			tb_pitch_0_5x.setChecked(false);
			tb_pitch_0_75x.setChecked(false);
			tb_pitch_1_0x.setChecked(false);
			tb_pitch_1_25x.setTextColor(act.getResources().getColor(R.color.theme));
			tb_pitch_1_5x.setChecked(false);
			tb_pitch_2_0x.setChecked(false);
			sb_pitch.setProgress(115);
		  } else if (!p2) {
			tb_pitch_1_25x.setTextColor(act.getResources().getColor(R.color.gray));
		  }
		}
	  });
	tb_pitch_1_5x.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
		  // TODO: Implement this method
		  if (p2) {
			tb_pitch_0_5x.setChecked(false);
			tb_pitch_0_75x.setChecked(false);
			tb_pitch_1_0x.setChecked(false);
			tb_pitch_1_25x.setChecked(false);
			tb_pitch_1_5x.setTextColor(act.getResources().getColor(R.color.theme));
			tb_pitch_2_0x.setChecked(false);
			sb_pitch.setProgress(140);
		  } else if (!p2) {
			tb_pitch_1_5x.setTextColor(act.getResources().getColor(R.color.gray));
		  }
		}
	  });
	tb_pitch_2_0x.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
		  // TODO: Implement this method
		  if (p2) {
			tb_pitch_0_5x.setChecked(false);
			tb_pitch_0_75x.setChecked(false);
			tb_pitch_1_0x.setChecked(false);
			tb_pitch_1_25x.setChecked(false);
			tb_pitch_1_5x.setChecked(false);
			tb_pitch_2_0x.setTextColor(act.getResources().getColor(R.color.theme));
			sb_pitch.setProgress(190);
		  } else if (!p2) {
			tb_pitch_2_0x.setTextColor(act.getResources().getColor(R.color.gray));
		  }
		}
	  });

	if (pitch == 0.5f) {
	  tb_pitch_0_5x.setChecked(true);
	} else if (pitch == 0.75f) {
	  tb_pitch_0_75x.setChecked(true);
	} else if (pitch == 1.0f) {
	  tb_pitch_1_0x.setChecked(true);
	} else if (pitch == 1.25f) {
	  tb_pitch_1_25x.setChecked(true);
	} else if (pitch == 1.5f) {
	  tb_pitch_1_5x.setChecked(true);
	} else if (pitch == 2.0f) {
	  tb_pitch_2_0x.setChecked(true);
	}
  }

  // 版本过低对话框
  private void showChangeErrorDialog(Activity act) {
	String sdk=Build.VERSION.RELEASE;
	AlertDialog alertDialog = new AlertDialog.Builder(act)
	  .setTitle("提示")
	  .setMessage("抱歉，您的 Android 版本 (" + sdk + ") 小于此功能所支持的最低 Android 版本 (6.0)。")
	  .setPositiveButton("确定", null)
	  .show();
	alertDialog.setCanceledOnTouchOutside(false);
	TextView title=alertDialog.getWindow().findViewById(R.id.alertTitle);
	title.setTextColor(act.getResources().getColor(R.color.theme));
	TextPaint tp_title = title.getPaint();
	tp_title.setFakeBoldText(true);

	Button positive=alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
	positive.setTextColor(act.getResources().getColor(R.color.gray));
	TextPaint tp_positive = positive.getPaint();
	tp_positive.setFakeBoldText(true);
  }

  // 提示对话框
  private void showTipDialog(Activity act) {
	AlertDialog alertDialog = new AlertDialog.Builder(act)
	  .setTitle("提示")
	  .setMessage("您正在使用的是测试版本。部分功能暂未开发完成，请知悉。")
	  .setPositiveButton("知道了", null)
	  .show();
	alertDialog.setCanceledOnTouchOutside(false);
	TextView title=alertDialog.getWindow().findViewById(R.id.alertTitle);
	title.setTextColor(act.getResources().getColor(R.color.theme));
	TextPaint tp_title = title.getPaint();
	tp_title.setFakeBoldText(true);

	Button positive=alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
	positive.setTextColor(act.getResources().getColor(R.color.gray));
	TextPaint tp_positive = positive.getPaint();
	tp_positive.setFakeBoldText(true);
  }
}
