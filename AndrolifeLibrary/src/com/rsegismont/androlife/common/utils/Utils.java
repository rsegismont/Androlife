/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rsegismont.androlife.common.utils;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.HONEYCOMB_MR1;
import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static android.provider.Settings.System.AIRPLANE_MODE_ON;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ThreadFactory;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Looper;
import android.os.Process;
import android.provider.Settings;

public final class Utils {
	static final String THREAD_PREFIX = "Androlife-";
	static final String THREAD_IDLE_NAME = THREAD_PREFIX + "Idle";
	static final int DEFAULT_READ_TIMEOUT = 20 * 1000; // 20s
	static final int DEFAULT_CONNECT_TIMEOUT = 15 * 1000; // 15s
	private static final String ANDROLIFE_CACHE = "androlife-cache";

	private Utils() {
		// No instances.
	}
	
	public static boolean hasFroyo() {
		// Can use static final constants like FROYO, declared in later versions
		// of the OS since they are inlined at compile time. This is guaranteed behavior.
		return Build.VERSION.SDK_INT >= 8;
	}

	public static boolean hasGingerbread() {
		return Build.VERSION.SDK_INT >= 9;
	}

	public static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= 11;
	}

	public static boolean hasHoneycombMR1() {
		return Build.VERSION.SDK_INT >= 12;
	}

	public static boolean hasJellyBean() {
		return Build.VERSION.SDK_INT >= 16;
	}
	
	public static boolean hasKitKat() {
		return Build.VERSION.SDK_INT >= 19;
	}

	static int getBitmapBytes(Bitmap bitmap) {
		int result;
		if (SDK_INT >= HONEYCOMB_MR1) {
			result = BitmapHoneycombMR1.getByteCount(bitmap);
		} else {
			result = bitmap.getRowBytes() * bitmap.getHeight();
		}
		if (result < 0) {
			throw new IllegalStateException("Negative size: " + bitmap);
		}
		return result;
	}

	static void checkNotMain() {
		if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
			throw new IllegalStateException(
					"Method call should not happen from the main thread.");
		}
	}

	static void closeQuietly(InputStream is) {
		if (is == null)
			return;
		try {
			is.close();
		} catch (IOException ignored) {
		}
	}

	/**
	 * Returns {@code true} if header indicates the response body was loaded
	 * from the disk cache.
	 */
	static boolean parseResponseSourceHeader(String header) {
		if (header == null) {
			return false;
		}
		String[] parts = header.split(" ", 2);
		if ("CACHE".equals(parts[0])) {
			return true;
		}
		if (parts.length == 1) {
			return false;
		}
		try {
			return "CONDITIONAL_CACHE".equals(parts[0])
					&& Integer.parseInt(parts[1]) == 304;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	static File createDefaultCacheDir(Context context) {
		File cache = new File(context.getApplicationContext().getCacheDir(),
				ANDROLIFE_CACHE);
		if (!cache.exists()) {
			cache.mkdirs();
		}
		return cache;
	}



	static int calculateMemoryCacheSize(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(ACTIVITY_SERVICE);
		boolean largeHeap = (context.getApplicationInfo().flags & FLAG_LARGE_HEAP) != 0;
		int memoryClass = am.getMemoryClass();
		if (largeHeap && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			memoryClass = ActivityManagerHoneycomb.getLargeMemoryClass(am);
		}
		// Target ~15% of the available heap.
		return 1024 * 1024 * memoryClass / 7;
	}

	static boolean isAirplaneModeOn(Context context) {
		ContentResolver contentResolver = context.getContentResolver();
		return Settings.System.getInt(contentResolver, AIRPLANE_MODE_ON, 0) != 0;
	}

	static boolean hasPermission(Context context, String permission) {
		return context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static class ActivityManagerHoneycomb {
		static int getLargeMemoryClass(ActivityManager activityManager) {
			return activityManager.getLargeMemoryClass();
		}
	}

	static class AndrolifeThreadFactory implements ThreadFactory {
		@SuppressWarnings("NullableProblems")
		public Thread newThread(Runnable r) {
			return new AndroidThread(r);
		}
	}

	private static class AndroidThread extends Thread {
		public AndroidThread(Runnable r) {
			super(r);
		}

		@Override
		public void run() {
			Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND);
			super.run();
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	private static class BitmapHoneycombMR1 {
		static int getByteCount(Bitmap bitmap) {
			return bitmap.getByteCount();
		}
	}

}
