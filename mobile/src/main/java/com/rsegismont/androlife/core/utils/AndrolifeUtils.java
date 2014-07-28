/* The MIT License (MIT)
 *
 * Copyright (c) 2013 Romain Segismont
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */

package com.rsegismont.androlife.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HttpContext;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.rsegismont.androlife.R;
import com.rsegismont.androlife.common.Constantes;
import com.rsegismont.androlife.common.SdkUtils;
import com.rsegismont.androlife.common.SharedInformation;
import com.rsegismont.androlife.common.SharedInformation.DatabaseColumn;
import com.rsegismont.androlife.common.api.AndrolifeApi9;

public class AndrolifeUtils {

	private static final String ENCODING_GZIP = "gzip";
	private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";

	public static long timeToConfirm = 0;
	public static final String androlifeCore = "com.rsegismont.android.androLife.core";

	public static boolean checkExternalDispose() {
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		final String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		return mExternalStorageAvailable && mExternalStorageWriteable;
	}

	public static final String dateToString(Date paramDate, String paramString) {
		SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(paramString, Locale.FRANCE);
		localSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
		return localSimpleDateFormat.format(paramDate);
	}

	public static void openAndrolifeCoreSettings(Context context) {
		if (Build.VERSION.SDK_INT >= 9) {
			try {
				// Open the specific App Info page:
				final Intent intent = AndrolifeApi9.getSettingsIntent();
				intent.setData(Uri.parse("package:" + androlifeCore));
				context.startActivity(intent);

			} catch (ActivityNotFoundException e) {
				// e.printStackTrace();
				Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
				context.startActivity(intent);

			}
		} else {
			Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
			context.startActivity(intent);

		}

	}

	public static Object fetch(String address) {
		try {
			URL url = new URL(address);
			Object content = url.getContent();
			return content;
		} catch (Exception e) {
			return null;
		}
	}

	public static Cursor getAndrolifeCursor(Uri paramUri, Context paramContext, int paramInt,
			String[] paramArrayOfString, Calendar... paramVarArgs) {
		try {
			String[][] arrayOfString = getCursorValues(paramContext, paramInt, paramVarArgs);
			Cursor localCursor = paramContext.getContentResolver().query(paramUri, paramArrayOfString,
					arrayOfString[0][0], arrayOfString[1],
					SharedInformation.DatabaseColumn.DATE_UTC.stringValue + " ASC");
			return localCursor;
		} catch (Throwable localThrowable) {
			localThrowable.printStackTrace();
		}
		return null;
	}

	public static Loader<Cursor> getAndrolifeLoader(Context paramContext, int paramInt, String[] paramArrayOfString,
			Calendar... paramVarArgs) {
		try {
			String[][] arrayOfString = getCursorValues(paramContext, paramInt, paramVarArgs);
			CursorLoader localCursorLoader = new CursorLoader(paramContext, SharedInformation.CONTENT_URI_PROGRAMMES,
					paramArrayOfString, arrayOfString[0][0], arrayOfString[1],
					SharedInformation.DatabaseColumn.DATE_UTC.stringValue + " ASC");

			return localCursorLoader;
		} catch (Throwable localThrowable) {
			localThrowable.printStackTrace();
		}
		return null;
	}

	public static String getDisplayDate(Context context, SimpleDateFormat format, long date) {
		switch (SdkUtils.getDiffDate(date)) {
		case 0:
			return context.getString(R.string.androlife_dates_today);
		case 1:
			return context.getString(R.string.androlife_dates_tommorow);

		default:
			return format.format(new Date(date));
		}
	}

	public static int getColor(String paramString) {
		try {
			if (paramString.equals("purple")) {
				return Color.parseColor("#800080");
			}
			if (paramString.equals("pink")) {
				return Color.parseColor("#F52887");
			}
			if (paramString.equals("white")) {
				return Color.parseColor("gray");
			}
			int i = Color.parseColor(paramString);
			return i;
		} catch (Throwable localThrowable) {
		}
		return Color.parseColor("gray");
	}

	public static long getCurrentIndex(Cursor mCursor) {

		long dateUtc = -1;

		if (mCursor == null)
			return -1;

		if (mCursor.getCount() <= 0)
			return -1;

		final int initialPosition = mCursor.getPosition();

		int index = -1;

		for (int i = 0; i < mCursor.getCount(); i++) {
			mCursor.moveToPosition(i);
			final long value = mCursor.getLong(mCursor.getColumnIndex(DatabaseColumn.DATE_UTC.stringValue));
			if (System.currentTimeMillis() - value < 0) {
				index = i - 1;
				mCursor.moveToPosition(Math.max(0, index));
				dateUtc = mCursor.getLong(mCursor.getColumnIndex(DatabaseColumn.DATE_UTC.stringValue));
				break;
			}

			if ((i == 0) && (System.currentTimeMillis() - value < 0)) {

				mCursor.moveToPosition(0);
				dateUtc = mCursor.getLong(mCursor.getColumnIndex(DatabaseColumn.DATE_UTC.stringValue));
				break;
			}

			if (i == (mCursor.getCount() - 1)) {
				index = (mCursor.getCount() - 1);
				mCursor.moveToPosition(Math.max(0, index));
				dateUtc = mCursor.getLong(mCursor.getColumnIndex(DatabaseColumn.DATE_UTC.stringValue));
				break;
			}
		}

		if (initialPosition >= 0)
			mCursor.moveToPosition(initialPosition);
		return dateUtc;

	}

	private static String[][] getCursorValues(Context paramContext, int paramInt, Calendar... paramVarArgs) {

		String[] arrayOfString1;
		String[] arrayOfString2;
		try {
			final SharedPreferences localSharedPreferences = PreferenceManager
					.getDefaultSharedPreferences(paramContext);
			String str1 = SharedInformation.DatabaseColumn.TYPE.stringValue + "<>? AND "
					+ SharedInformation.DatabaseColumn.TYPE.stringValue + "<>? AND "
					+ SharedInformation.DatabaseColumn.TYPE.stringValue + "<>?";
			String str2 = "";
			String str3 = "";
			String str4 = "";
			if (!localSharedPreferences.getBoolean("settings_filters_clip", true)) {
				str2 = "Clip";
			}
			if (!localSharedPreferences.getBoolean("settings_filters_magazines", true)) {
				str3 = "Magazine";
			}
			if (!localSharedPreferences.getBoolean("settings_filters_series", true)) {
				str4 = "Fiction";
			}

			arrayOfString1 = new String[paramVarArgs.length >= 2 ? 5 : 3];
			arrayOfString1[0] = str2;
			arrayOfString1[1] = str4;
			arrayOfString1[2] = str3;

			if (paramVarArgs.length >= 2) {
				str1 = str1 + " AND " + SharedInformation.DatabaseColumn.DATE_UTC.stringValue + " BETWEEN ? AND ?";
				arrayOfString1[3] = "" + paramVarArgs[0].getTime().getTime();
				arrayOfString1[4] = "" + paramVarArgs[1].getTime().getTime();
			}

			switch (paramInt) {
			case Constantes.CURSOR_SELECTION:
				arrayOfString2 = new String[1 + arrayOfString1.length];
				System.arraycopy(arrayOfString1, 0, arrayOfString2, 0, arrayOfString1.length);
				str1 = str1 + " AND " + SharedInformation.DatabaseColumn.LEVELTYPE.stringValue + "=?";
				arrayOfString2[arrayOfString1.length] = "110";
				break;
			case Constantes.CURSOR_NEWS:
				arrayOfString2 = new String[1 + arrayOfString1.length];
				System.arraycopy(arrayOfString1, 0, arrayOfString2, 0, arrayOfString1.length);
				str1 = str1 + " AND " + SharedInformation.DatabaseColumn.PREMIERE_DIFFUSION.stringValue + "=?";
				arrayOfString2[arrayOfString1.length] = "1";
				break;
			default:
				arrayOfString2 = new String[arrayOfString1.length];
				System.arraycopy(arrayOfString1, 0, arrayOfString2, 0, arrayOfString1.length);
				break;
			}

			String[][] arrayOfString = (String[][]) Array.newInstance(String.class, new int[] { 2,
					arrayOfString2.length });
			arrayOfString[0][0] = str1;
			arrayOfString[1] = arrayOfString2;
			return arrayOfString;
		} catch (Throwable localThrowable) {
			localThrowable.printStackTrace();
			return null;
		}

	}

	private static InputStream getInputStreamFromUrl(Context paramContext, String paramString) {
		if (confirmDownload(paramContext, paramString)) {
			try {
				BasicHttpParams localBasicHttpParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(localBasicHttpParams, 30000);
				HttpConnectionParams.setSoTimeout(localBasicHttpParams, 30000);
				DefaultHttpClient localDefaultHttpClient = new DefaultHttpClient(localBasicHttpParams);
				localDefaultHttpClient.addRequestInterceptor(new HttpRequestInterceptor() {
					public void process(HttpRequest paramAnonymousHttpRequest, HttpContext paramAnonymousHttpContext) {
						if (!paramAnonymousHttpRequest.containsHeader(HEADER_ACCEPT_ENCODING)) {
							paramAnonymousHttpRequest.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
						}
					}
				});
				localDefaultHttpClient.addResponseInterceptor(new HttpResponseInterceptor() {
					public void process(HttpResponse paramAnonymousHttpResponse, HttpContext paramAnonymousHttpContext) {
						Header localHeader = paramAnonymousHttpResponse.getEntity().getContentEncoding();
						HeaderElement[] arrayOfHeaderElement = null;
						int i;
						if (localHeader != null) {
							arrayOfHeaderElement = localHeader.getElements();
							i = arrayOfHeaderElement.length;
							for (int j = 0; j < i; j++) {

								if (arrayOfHeaderElement[j].getName().equalsIgnoreCase(ENCODING_GZIP)) {
									paramAnonymousHttpResponse.setEntity(new AndrolifeUtils.InflatingEntity(
											paramAnonymousHttpResponse.getEntity()));
									return;
								}
							}
						}

					}
				});
				InputStream localInputStream = localDefaultHttpClient.execute(new HttpGet(paramString)).getEntity()
						.getContent();
				return localInputStream;
			} catch (Exception localException) {
			}
			return null;
		} else {
			return null;
		}
	}

	private static boolean confirmDownload(Context context, String stringUrl) {
		try {
			URL url = new URL(stringUrl);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			long candidateDate = urlConnection.getLastModified();
			final SharedPreferences prefsManager = PreferenceManager.getDefaultSharedPreferences(context
					.getApplicationContext());

			final long savedDate = prefsManager.getLong(stringUrl, 0);

			urlConnection.disconnect();
			if (candidateDate <= savedDate) {
				return false;
			}
			timeToConfirm = candidateDate;

			return true;

		} catch (Throwable localThrowable) {

			localThrowable.printStackTrace();
			return false;

		}
	}

	private static InputStream getInputStreamFromUrl_V9(Context paramContext, String paramString) {
		if (confirmDownload(paramContext, paramString)) {
			try {

				URL localURL = new URL(paramString);
				HttpURLConnection localHttpURLConnection2 = (HttpURLConnection) localURL.openConnection();
				localHttpURLConnection2.setRequestProperty("Accept-Charset", "UTF-8");
				localHttpURLConnection2.setReadTimeout(30000);
				localHttpURLConnection2.setConnectTimeout(30000);
				localHttpURLConnection2.setRequestMethod("GET");
				localHttpURLConnection2.setDoInput(true);
				localHttpURLConnection2.connect();
				return localHttpURLConnection2.getInputStream();

			} catch (Throwable localThrowable) {

				localThrowable.printStackTrace();
				return null;

			}
		} else {
			return null;
		}
	}

	public static InputStream getInputStreamUrl(Context context, String url) {
		if (Build.VERSION.SDK_INT < 8) {
			System.setProperty("http.keepAlive", "false");
		}
		if (Build.VERSION.SDK_INT <= 8) {
			return getInputStreamFromUrl(context, url);
		} else {
			return getInputStreamFromUrl_V9(context, url);
		}
	}

	public static long getTonightIndex(Cursor mCursor) {
		if (mCursor == null)
			return -1;

		if (mCursor.getCount() <= 0)
			return -1;

		long dateUtc = -1;

		final int initialPosition = mCursor.getPosition();

		mCursor.moveToPosition(0);

		final long initialValue = mCursor.getLong(mCursor.getColumnIndex(DatabaseColumn.DATE_UTC.stringValue));

		final Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(initialValue);
		calendar.set(Calendar.HOUR_OF_DAY, 19);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		for (int i = 0; i < mCursor.getCount(); i++) {
			mCursor.moveToPosition(i);
			final long value = mCursor.getLong(mCursor.getColumnIndex(DatabaseColumn.DATE_UTC.stringValue));

			if (calendar.getTime().getTime() - value < 0) {

				mCursor.moveToPosition(Math.max(0, i - 1));
				dateUtc = mCursor.getLong(mCursor.getColumnIndex(DatabaseColumn.DATE_UTC.stringValue));
				break;
			}
			if ((i == 0) && (calendar.getTime().getTime() - value < 0)) {

				mCursor.moveToPosition(0);
				dateUtc = mCursor.getLong(mCursor.getColumnIndex(DatabaseColumn.DATE_UTC.stringValue));
				break;
			}

			if (i == (mCursor.getCount() - 1)) {

				mCursor.moveToPosition(Math.max(0, mCursor.getCount() - 1));
				dateUtc = mCursor.getLong(mCursor.getColumnIndex(DatabaseColumn.DATE_UTC.stringValue));
				break;
			}
		}

		if (initialPosition >= 0)
			mCursor.moveToPosition(initialPosition);

		return dateUtc;
	}

	public static final boolean isNetworkAvailable(Context paramContext) {
		ConnectivityManager localConnectivityManager = (ConnectivityManager) paramContext
				.getSystemService(Activity.CONNECTIVITY_SERVICE);
		final NetworkInfo localNetworkInfo1 = localConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		final NetworkInfo localNetworkInfo2 = localConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		return (localNetworkInfo1.isConnected() && localNetworkInfo2.isConnected());
	}

	public static boolean isToday(long dateUtc) {
		final long currentTime = System.currentTimeMillis();
		final Calendar calendarBefore = Calendar.getInstance();
		calendarBefore.setTimeInMillis(currentTime);
		calendarBefore.set(Calendar.HOUR_OF_DAY, 0);
		calendarBefore.set(Calendar.MINUTE, 0);
		calendarBefore.set(Calendar.SECOND, 0);
		calendarBefore.set(Calendar.MILLISECOND, 0);

		//
		final Calendar calendarAfter = Calendar.getInstance();
		calendarAfter.setTimeInMillis(currentTime);
		calendarAfter.set(Calendar.HOUR_OF_DAY, 23);
		calendarAfter.set(Calendar.MINUTE, 59);
		calendarAfter.set(Calendar.SECOND, 59);
		calendarAfter.set(Calendar.MILLISECOND, 250);
		if ((dateUtc >= calendarBefore.getTimeInMillis()) && (dateUtc <= calendarAfter.getTimeInMillis())) {
			return true;
		}
		return false;
	}

	public static boolean isUpdateAvailable(Context paramContext, String paramString) {
		try {
			HttpURLConnection localHttpURLConnection = (HttpURLConnection) new URL(paramString).openConnection();
			long l1 = localHttpURLConnection.getHeaderFieldDate("Last-Modified", System.currentTimeMillis());
			localHttpURLConnection.disconnect();
			long l2 = PreferenceManager.getDefaultSharedPreferences(paramContext.getApplicationContext()).getLong(
					paramString, 0L);
			boolean toReturn = l1 > l2;

			return toReturn;
		} catch (Throwable localThrowable) {
			localThrowable.printStackTrace();
		}
		return false;
	}

	public static final Date stringToDate(SimpleDateFormat dateFormat, String dateToConvert, boolean catchError) {
		try {
			Date localDate = dateFormat.parse(dateToConvert);
			return localDate;
		} catch (Throwable localThrowable) {
			if (!catchError) {
				return new Date(1000L + System.currentTimeMillis());
			}
		}
		return null;
	}

	public static final Date stringToDate(String paramString1, String paramString2, boolean paramBoolean) {
		SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(paramString2, Locale.getDefault());
		localSimpleDateFormat.setTimeZone(TimeZone.getTimeZone(TimeZone.getDefault().getID()));
		try {
			Date localDate = localSimpleDateFormat.parse(paramString1);
			return localDate;
		} catch (Throwable localThrowable) {
			if (!paramBoolean) {
				return new Date(1000L + System.currentTimeMillis());
			}
		}
		return null;
	}

	private static class InflatingEntity extends HttpEntityWrapper {
		public InflatingEntity(HttpEntity paramHttpEntity) {
			super(paramHttpEntity);
		}

		public InputStream getContent() throws IOException {
			return new GZIPInputStream(this.wrappedEntity.getContent());
		}

		public long getContentLength() {
			return -1L;
		}
	}
}