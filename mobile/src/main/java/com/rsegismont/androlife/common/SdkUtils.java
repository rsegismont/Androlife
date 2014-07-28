package com.rsegismont.androlife.common;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.widget.TextView;

/**
 * General utility class, with a lot of useful methods.
 * 
 * @version 1.0
 * @since 1.0
 * 
 */
public class SdkUtils {

	public static final int ONE_SECOND = 1000;
	public static final int ONE_MINUTE = ONE_SECOND * 60;
	public static final int ONE_HOUR = ONE_MINUTE * 60;
	public static final int ONE_DAY = ONE_HOUR * 24;

	public static boolean isATablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	public static int getDiffDate(long dateUtc) {

		final Calendar calendarBefore = Calendar.getInstance();
		calendarBefore.setTimeInMillis(System.currentTimeMillis());
		calendarBefore.set(Calendar.HOUR_OF_DAY, 0);
		calendarBefore.set(Calendar.MINUTE, 0);
		calendarBefore.set(Calendar.SECOND, 0);
		calendarBefore.set(Calendar.MILLISECOND, 0);

		final long diffLong = dateUtc - calendarBefore.getTimeInMillis();
		return ((int) diffLong / SdkUtils.ONE_DAY);
	}

	public static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	public static boolean isTabletPortrait(Context context) {
		return ((context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) && (context
				.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE);
	}

	public static boolean isTabletLandscape(Context context) {
		return ((context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) && (context
				.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE);
	}

	private static Map<String, Typeface> fontsMap = new HashMap<String, Typeface>();

	public static void setTextViewRoboto(TextView textview, String fontString) {
		if (textview != null) {

			Typeface typeFace = fontsMap.get(fontString);
			if (typeFace == null) {
				typeFace = Typeface.createFromAsset(textview.getContext().getAssets(), "fonts/Roboto/" + fontString
						+ ".ttf");
				fontsMap.put(fontString, typeFace);
			}
			textview.setTypeface(typeFace);
		}
	}

	/**
	 * Method to know if a {@link Service} is activated or not
	 * 
	 * @since 1.0
	 * 
	 * @param context
	 *            the context of your application
	 * @param serviceName
	 *            the name of the service you're looking for.
	 * @return true if the service is running, false otherwise
	 */
	public static final boolean isServiceEnabled(Context context, String serviceName) {
		final ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		final List<ActivityManager.RunningServiceInfo> services = manager.getRunningServices(100);
		final String pname = context.getPackageName();
		for (int i = 0; i < services.size(); i++) {
			try {
				if (services.get(i).service.toString().contains(serviceName) && services.get(i).started
						&& services.get(i).service.toString().contains(pname)) {
					return true;
				}
			} catch (Exception e) {
			}
		}
		return false;
	}

	/**
	 * Method to send an email
	 * 
	 * @since 1.0
	 * 
	 * @param context
	 *            the context of your application
	 * @param mailAdress
	 *            the mail adressses (in an array).
	 * @param subject
	 *            the subject of the mail
	 * @param body
	 *            the content of the mail
	 * @return true if succeeded false otherwise
	 */
	public static final boolean sendEmail(Context context, String[] mailAdress, String subject, String body) {
		try {
			final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, mailAdress);
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
			emailIntent.setType("message/rfc822");
			context.startActivity(emailIntent);
			return true;
		} catch (Throwable e1) {
			try {
				final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
				emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, mailAdress);
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
				emailIntent.setType("text/plain");
				context.startActivity(emailIntent);
				return true;
			} catch (Throwable e2) {
				return false;
			}
		}

	}

	/**
	 * Method to restore the requested orientation to an activity
	 * 
	 * @since 1.0
	 * 
	 * @param activity
	 *            the activity you want to restore
	 * @param requestedOrientation
	 *            the requested oriention
	 * @see ActivityInfo
	 */
	public static final void restoreOrientation(Activity activity, int requestedOrientation) {
		activity.setRequestedOrientation(requestedOrientation);
	}

	/**
	 * Method to get a String ressource by it's name
	 * 
	 * @since 1.0
	 * 
	 * @param context
	 *            the {@link Context} of the application
	 * @param nameOfId
	 *            the name of the id to retrieve from the ressources
	 * @return
	 */
	public static final String getRessourceStringFromTag(final Context context, final String nameOfId) {
		try {
			final Resources res = context.getResources();
			return res.getString(res.getIdentifier(nameOfId, "string", context.getPackageName()));
		} catch (Throwable e) {
			return nameOfId;
		}
	}

	/**
	 * Method to get an id of a ressource by it's name
	 * 
	 * @since 1.0
	 * 
	 * @param context
	 *            the {@link Context} of the application
	 * @param nameOfId
	 *            the name of the id to retrieve from the ressources
	 * @return
	 */
	public static final int getRessourceId(final Context context, final String nameOfId, String type) {
		try {
			return context.getResources().getIdentifier(nameOfId, type, context.getPackageName());
		} catch (Throwable e) {
			return -1;
		}
	}

	/**
	 * Method to get an {@link InputStream} from an URL;
	 * 
	 * @since 1.0
	 * 
	 * @param src
	 *            the url you want to have an {@link InputStream}
	 * @return an {@link InputStream}
	 */
	public static final InputStream getInputStreamFromUrl(String src) {
		int numberOfTrials = 0;
		InputStream is = null;
		while ((is == null) && (numberOfTrials < 3)) {
			if (Build.VERSION.SDK_INT <= 8) {

				final DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
				try {
					final HttpGet method = new HttpGet(new URI(src));
					final HttpResponse response = defaultHttpClient.execute(method);
					is = response.getEntity().getContent();

				} catch (Throwable e) {

				}
			} else {
				try {
					final URL url = new URL(src);
					final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setConnectTimeout(30 * 1000);
					connection.setReadTimeout(30 * 1000);
					connection.connect();
					is = connection.getInputStream();
				} catch (Throwable e) {

				}
			}
			numberOfTrials++;
		}

		return is;
	}

	/**
	 * Method who hash a package name into a {@link String} SHA-256
	 * 
	 * @since 1.0
	 * 
	 * @param req
	 *            the package name
	 * @return a {@link String} of an SHA-256
	 */
	public static final String hashRequest(String req) {
		byte[] hash = null;
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		try {
			hash = md.digest(req.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return byteToHex(hash);
	}

	/**
	 * Method who converts an array of bites into a hexadecimal String
	 * 
	 * @since 1.0
	 * 
	 * @param bits
	 *            an array of {@link Byte}
	 * @return a hexadecimal {@link String}
	 */
	private static final String byteToHex(byte[] bits) {
		if (bits == null) {
			return null;
		}
		final StringBuffer hex = new StringBuffer(bits.length * 2); // encod(1_bit)
																	// =>
		// 2 digits
		for (int i = 0; i < bits.length; i++) {
			if (((int) bits[i] & 0xff) < 0x10) { // 0 < .. < 9
				hex.append("0");
			}
			hex.append(Integer.toString((int) bits[i] & 0xff, 16)); // [(bit+256)%256]^16
		}
		return hex.toString();
	}

	/**
	 * Checks if there is a valid internet connection.
	 * 
	 * @since 1.0
	 * 
	 * @param context
	 *            the {@link Context} of the application
	 * @return True if device has a valid internet connection
	 * 
	 */
	public static final boolean haveInternet(Context context) {

		final NetworkInfo info = (NetworkInfo) ((ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

		if (info == null || !info.isConnected()) {
			return false;
		}

		return true;
	}

	/**
	 * Checks if there is a telephony service.
	 * 
	 * @ since 1.0
	 * 
	 * @param context
	 *            the {@link Context} of the application
	 * @return True if device has valid telephony service
	 * 
	 */
	public static final boolean hasPhoneAvailable(Context context) {

		final TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		if (tel == null || (tel.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE)) {
			return false;
		}

		return true;
	}

	/**
	 * Prepare a web {@link Intent}
	 * 
	 * @since 1.0
	 * 
	 * @param context
	 *            the {@link Context} of the application
	 * @param url
	 *            the url to parse
	 * @return the web {@link Intent}
	 */
	public static final Intent prepare_web(Context context, String url) {
		final Uri uri = Uri.parse(url);
		final Intent myIntent = new Intent(Intent.ACTION_VIEW, uri);
		return myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("TYPE", "WEB");
	}

}
