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

import android.content.Context;

/**
 * Wrapper for Agenda Events
 * 
 * @author Romain Segismont
 * @version 1.0
 * @since 1.0
 * 
 */
public class CalendarWrapper {

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

			CalendarIcs.registerCalendarEvent(context, title, location, description, startTime, endTime);
		
	}

}
