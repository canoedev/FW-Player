package com.myz.fwplayer;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

  public static final String TAG = "CrashHandler";

  private Thread.UncaughtExceptionHandler mDefaultHandler;// 系统默认的 UncaughtException 处理类
  private static CrashHandler INSTANCE = new CrashHandler();// CrashHandler 实例
  private Context mContext;// Context 对象
  private Map<String, String> infos = new HashMap<String, String>();// 用来存储设备信息和异常信息
  private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");// 用于格式化日期

  // 保证只有一个 CrashHandler 实例
  private CrashHandler() {}

  // 单例模式获取 CrashHandler 实例  
  public static CrashHandler getInstance() {
	return INSTANCE;
  }

  // 初始化
  public void init(Context context) {
	mContext = context;
	mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();// 获取系统默认的 UncaughtException 处理器  
	Thread.setDefaultUncaughtExceptionHandler(this);// 设置 CrashHandler 为程序的默认异常处理器
  }

  // UncaughtException 发生时转入该函数处理 
  @Override  
  public void uncaughtException(Thread thread, Throwable ex) {
	if (!handleException(ex) && mDefaultHandler != null) {//  用户未处理
	  mDefaultHandler.uncaughtException(thread, ex);// 让系统默认的异常处理器来处理
	} else {
	  try {
		Thread.sleep(3000);
	  } catch (InterruptedException e) {}
	  android.os.Process.killProcess(android.os.Process.myPid());// 结束进程
	  System.exit(0);// 退出程序
	}
  }

  // 收集错误信息发送错误报告
  private boolean handleException(Throwable ex) {
	if (ex == null) {
	  return false;
	}
	new Thread() {
	  @Override
	  public void run() {
		Looper.prepare();
		Toast.makeText(mContext, "似乎出了点问题…", Toast.LENGTH_LONG).show();
		Looper.loop();
	  }
	}.start();
	collectDeviceInfo(mContext);// 收集设备参数信息   
	saveCrashInfo2File(ex);// 保存日志文件
	return true;
  }

  // 收集设备参数信息
  public void collectDeviceInfo(Context ctx) {
	try {
	  PackageManager pm = ctx.getPackageManager();
	  PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
	  if (pi != null) {
		String versionName = pi.versionName == null ? "null" : pi.versionName;
		String versionCode = pi.versionCode + "";
		infos.put("versionName", versionName);
		infos.put("versionCode", versionCode);
	  }
	} catch (Exception e) {
	}
	Field[] fields = Build.class.getDeclaredFields();
	for (Field field : fields) {
	  try {
		field.setAccessible(true);
		infos.put(field.getName(), field.get(null).toString());
		Log.d(TAG, field.getName() + " : " + field.get(null));
	  } catch (Exception e) {
	  }
	}
  }

  // 保存错误信息到文件中
  private String saveCrashInfo2File(Throwable ex) {
	StringBuffer sb = new StringBuffer();
	for (Map.Entry<String, String> entry : infos.entrySet()) {
	  String key = entry.getKey();
	  String value = entry.getValue();
	  sb.append(key + "=" + value + "\n");
	}

	Writer writer = new StringWriter();
	PrintWriter printWriter = new PrintWriter(writer);
	ex.printStackTrace(printWriter);
	Throwable cause = ex.getCause();
	while (cause != null) {
	  cause.printStackTrace(printWriter);
	  cause = cause.getCause();
	}
	printWriter.close();
	String result = writer.toString();
	sb.append(result);
	try {
	  long timestamp = System.currentTimeMillis();
	  String time = formatter.format(new Date());
	  String fileName = "crash-" + time + "-" + timestamp + ".log";
	  if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
		String path = Environment.getExternalStorageDirectory() + "/crash/";
		File dir = new File(path);
		if (!dir.exists()) {
		  dir.mkdirs();
		}
		FileOutputStream fos = new FileOutputStream(path + fileName);
		fos.write(sb.toString().getBytes());
		fos.close();
	  }
	  return fileName;
	} catch (Exception e) {
	}
	return null;
  }
}
