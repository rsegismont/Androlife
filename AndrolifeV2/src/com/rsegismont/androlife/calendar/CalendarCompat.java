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

package com.rsegismont.androlife.calendar;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import com.rsegismont.androlife.R;

/**
 * Class that will insert events into Agenda on devices before ICS
 * 
 * @author Romain Segismont
 * @version 1.0
 * @since 1.0
 * 
 */
public class CalendarCompat {

	// Content provider from Eclair (2.1) to Donut (1.6)
	private static String ECLAIR_CONTENT_PROVIDER = "content://calendar/events";

	// Content provider from Froyo (2.2) to Gingerbread (2.3)
	private static String FROYO_CONTENT_PROVIDER = "content://com.android.calendar/events";

	/** id, We need to choose from our mobile for primary its 1 */
	public static String ID = "calendar_id";

	/** Description of the event */
	public static String DESCRIPTION = "description";

	/** Location of the event */
	public static String LOCATION = "eventLocation";

	/** Time when the event start */
	public static String START_TIME = "dtstart";

	/** Time when the event end */
	public static String END_TIME = "dtend";

	/** Title of the event */
	public static String TITLE = "title";

	/** If it is bithday alarm or such kind (which should remind me for whole day) 0 for false, 1 for true */
	public static String ALL_DAY = "allDay";

	/** This information is sufficient for most entries tentative (0), confirmed (1) or canceled (2) */
	public static String STATUS = "eventStatus";

	/** visibility to default (0), confidential (1), private (2), or public (3) */
	public static String VISIBILITY = "visibility";

	/** 0 for false, 1 for true */
	public static String HAS_ALARAM = "hasAlarm";

	/**
	 * Register a new Calendar Event
	 * 
	 * @param context
	 *            the current {@link Context}
	 * @param title
	 *            title of your event
	 * @param location
	 *            location of your event
	 * @param description
	 *            description of your event
	 * @param startTime
	 *            the time when you event start
	 * @param endTime
	 *            the time when you event end
	 */
	public static void registerCalendarEvent(Context context, String title, String location, String description,
			long startTime, long endTime) {

		String provider = "";
		if (Build.VERSION.SDK_INT < 8)
			provider = ECLAIR_CONTENT_PROVIDER;
		else
			provider = FROYO_CONTENT_PROVIDER;

		try {
			ContentValues values = new ContentValues();

			values.put(ID, 1);
			values.put(TITLE, title);
			values.put(DESCRIPTION, description);
			values.put(LOCATION, location);
			values.put(START_TIME, startTime);
			values.put(END_TIME, endTime);
			values.put(ALL_DAY, 0);
			values.put(STATUS, 1);
			values.put(VISIBILITY, 1);
			values.put(HAS_ALARAM, 1);
			context.getContentResolver().insert(Uri.parse(provider), values);

			Toast.makeText(context, R.string.androlife_agenda_add_success, Toast.LENGTH_LONG).show();
		} catch (Throwable e) {
			Toast.makeText(context, R.string.androlife_agenda_add_fail, Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}

}
