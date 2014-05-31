package com.rsegismont.androlife.application;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.rsegismont.androlife.R;
import com.rsegismont.androlife.common.utils.AsyncTask;
import com.rsegismont.androlife.common.utils.ImageFetcher;

public class AndrolifeApplication extends Application {
	public int pauseCpt = 0;
	public int destroyCpt = 0;

	public SharedPreferences preferencesManager;
	public static AndrolifeApplication instance;
	public ImageFetcher mImageDownloader = null;
	private ConnectivityManager cm;
	public SimpleDateFormat dateFormatter;

	public AndrolifeApplication() {
		instance = this;
	}

	private void enableHttpResponseCache() {
		try {
			File localFile = new File(getCacheDir(), "http");
			Class<?> localClass = Class.forName("android.net.http.HttpResponseCache");
			Class<?>[] arrayOfClass = new Class[2];
			arrayOfClass[0] = File.class;
			arrayOfClass[1] = Long.TYPE;
			Method localMethod = localClass.getMethod("install", arrayOfClass);
			Object[] arrayOfObject = new Object[2];
			arrayOfObject[0] = localFile;
			arrayOfObject[1] = Long.valueOf(10485760L);
			localMethod.invoke(null, arrayOfObject);
		} catch (Exception localException) {

		}
	}

	public void onCreate() {
		super.onCreate();
		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		this.preferencesManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		dateFormatter = new SimpleDateFormat(getString(R.string.androlife_time_rediff), Locale.FRANCE);

		this.pauseCpt = 0;
		this.destroyCpt = 0;
		enableHttpResponseCache();
	}

	public void setImageDownloader(ImageFetcher paramImageFetcher) {
		this.mImageDownloader = paramImageFetcher;
	}

	public BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			final NetworkInfo info = cm.getActiveNetworkInfo();
			AsyncTask.DUAL_THREAD_EXECUTOR.adjustThreadCount(info);

		}
	};

}