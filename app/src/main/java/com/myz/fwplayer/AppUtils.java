package com.myz.fwplayer;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextPaint;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import java.lang.reflect.Method;

public class AppUtils {

  // 判断悬浮窗权限
  public static boolean haveWindowPermission(Context context) {
	boolean permission=true;
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
	  if (!Settings.canDrawOverlays(context)) {
		permission = false;
	  }
	} else {
	  if (!checkOp(context, 24)) {// 24 代表悬浮窗权限
		permission = false;
	  }
	}
	return permission;
  }

  // 兼容模式检查权限
  private static boolean checkOp(Context context, int op) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
	  AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
	  try {
		Class clazz = AppOpsManager.class;
		Method method = clazz.getDeclaredMethod("checkOp", int.class, int.class, String.class);
		return AppOpsManager.MODE_ALLOWED == (int)method.invoke(manager, op, Binder.getCallingUid(), context.getPackageName());
	  } catch (Exception e) {
	  }
    } else {
    }
    return false;
  }

  // 存储权限提示对话框 A
  public static void showStorageDialogA(final Activity act) {
	AlertDialog alertDialog = new AlertDialog.Builder(act)
	  .setTitle("提示")
	  .setMessage("应用需要获取存储权限，以正常访问您设备上的文件和收集错误信息。")
	  .setPositiveButton("知道了", new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface p1, int p2) {
		  act.requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 0);
		}
	  })
	  .setNegativeButton("退出", new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface p1, int p2) {
		  act.finish();
		}
	  })
	  .show();
	alertDialog.setCancelable(false);
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
  }

  // 存储权限提示对话框 B
  public static void showStorageDialogB(final Activity act) {
	AlertDialog alertDialog = new AlertDialog.Builder(act)
	  .setTitle("提示")
	  .setMessage("您拒绝了应用的存储权限，请到设置中打开该权限。")
	  .setPositiveButton("去授权", new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface p1, int p2) {
		  Intent intent = new Intent();
		  intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);  
		  intent.setData(Uri.fromParts("package", act.getPackageName(), null));
		  act.startActivityForResult(intent, 0);
		}
	  })
	  .setNegativeButton("退出", new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface p1, int p2) {
		  act.finish();
		}
	  })
	  .show();
	alertDialog.setCancelable(false);
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
  }

  // 悬浮窗权限提示对话框
  public static void showWindowDialog(final Activity act) {
	AlertDialog alertDialog = new AlertDialog.Builder(act)
	  .setTitle("提示")
	  .setMessage("使用悬浮播放功能需要悬浮窗权限，请到设置中打开该权限。")
	  .setPositiveButton("去授权", new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface p1, int p2) {
		  Intent intent = new Intent();
		  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
		  } else {
			intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		  }
		  intent.setData(Uri.fromParts("package", act.getPackageName(), null));
		  act.startActivityForResult(intent, 1);
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
  }

  // 检查 APP 是否已经安装
  public static boolean isAppInstalled(Context context, String packagename) {
	PackageInfo packageInfo=null;
	try {
      packageInfo = context.getPackageManager().getPackageInfo(packagename, 0);
	} catch (PackageManager.NameNotFoundException e) {
	}
	if (packageInfo == null) {
	  return false;
	} else {
	  return true;
	}
  }

  // 判断 Service 是否正在运行
  public static boolean isRunService(Context context, String serviceName) {
	ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
	for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	  if (serviceName.equals(service.service.getClassName())) {
		return true;
	  }
	}
	return false;
  }

  // 获取 Uri
  public static Uri getUri(Uri uri, Context ctx) {
	if (DocumentsContract.isDocumentUri(ctx, uri)) {
	  if (isExternalStorageDocument(uri)) {
		final String docId = DocumentsContract.getDocumentId(uri);
		final String[] split = docId.split(":");
		final String type = split[0];
		if ("primary".equalsIgnoreCase(type)) {
		  return Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/" + split[1]);
		}
	  } else if (isDownloadsDocument(uri)) {
		final String id = DocumentsContract.getDocumentId(uri);
		final String[] split = id.split(":");
		final String type = split[0];
		if ("raw".equalsIgnoreCase(type)) {
		  return Uri.parse("file://" + split[1]);
		}
		String[] contentUriPrefixesToTry = new String[]{
		  "content://downloads/public_downloads",
		  "content://downloads/my_downloads",
		  "content://downloads/all_downloads"
		};
		for (String contentUriPrefix : contentUriPrefixesToTry) {
		  Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.valueOf(id));
		  try {
			String path = getDataColumn(ctx, contentUri, null, null);
			if (path != null) {
			  return Uri.parse("file://" + path);
			}
		  } catch (Exception e) {
		  }
		}
	  } else if (isMediaDocument(uri)) {
		final String docId = DocumentsContract.getDocumentId(uri);
		final String[] split = docId.split(":");
		final String type = split[0];
		Uri contentUri = null;
		if ("image".equals(type)) {
		  contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		} else if ("video".equals(type)) {
		  contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		} else if ("audio".equals(type)) {
		  contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		}
		final String selection = "_id=?";
		final String[] selectionArgs = new String[]{split[1]};
		return Uri.parse("file://" + getDataColumn(ctx, contentUri, selection, selectionArgs));
	  }
	} else if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
	  return uri;
	} else if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
	  try {
		return Uri.parse("file://" + getDataColumn(ctx, uri, null, null));
	  } catch (Exception e) {
		if (uri.getPath().startsWith("/root")) {
		  return Uri.parse("file://" + getUriCompat(uri));
		}
	  }
	}
	return uri;
  }

  // 华为 Uri 兼容模式转换
  private static String getUriCompat(Uri uri) {
	String path=uri.getPath();
	for (int i=0;i < 2;i++) {
	  int index = path.indexOf("/");
	  if (i == 1) {
		index -= 1;
	  }
	  path = path.substring(index + 1);
	}
	return path;
  }

  private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
	Cursor cursor = null;
	final String column = MediaStore.Images.Media.DATA;
	final String[] projection = {column};
	try {
	  cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
	  if (cursor != null && cursor.moveToFirst()) {
		final int column_index = cursor.getColumnIndexOrThrow(column);
		return cursor.getString(column_index);
	  }
	} finally {
	  if (cursor != null)
		cursor.close();
	}
	return null;
  }

  private static boolean isExternalStorageDocument(Uri uri) {
	return "com.android.externalstorage.documents".equals(uri.getAuthority());
  }

  private static boolean isDownloadsDocument(Uri uri) {
	return "com.android.providers.downloads.documents".equals(uri.getAuthority());
  }

  private static boolean isMediaDocument(Uri uri) {
	return "com.android.providers.media.documents".equals(uri.getAuthority());
  }
}
