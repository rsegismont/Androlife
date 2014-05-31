package com.rsegismont.androlife.common;

import android.util.Log;

public class MyLog {

	public static void log(Object object, String detail) {
		if (BuildConfig.DEBUG)
			Log.e("androlife", object.getClass().getSimpleName() + " : " + detail);
	}

	public static void log(String tag, String detail) {
		if (BuildConfig.DEBUG)
			Log.e(tag, detail);
	}

}
