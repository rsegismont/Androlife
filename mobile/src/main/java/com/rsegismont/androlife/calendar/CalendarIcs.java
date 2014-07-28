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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;

@TargetApi(14)
/** Class that will insert events into Agenda on ICS and More
 * 
 * @author Romain Segismont
 * @version 1.0
 * @since 1.0
 *
 */
public class CalendarIcs {

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
		try {
			final Intent calIntent = new Intent(Intent.ACTION_INSERT);
			calIntent.setType("vnd.android.cursor.item/event");
			calIntent.putExtra(Events.TITLE, title);
			calIntent.putExtra(Events.EVENT_LOCATION, location);
			calIntent.putExtra(Events.DESCRIPTION, description);

			calIntent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false);
			calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime);
			calIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime);

			context.startActivity(calIntent);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
